package com.marketplace.infrastructure.ratelimit;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.marketplace.common.response.ErrorResponse;
import com.marketplace.infrastructure.redis.CacheKeyUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Instant;

@Slf4j
@Component
@Order(1)
@RequiredArgsConstructor
public class RateLimitFilter extends OncePerRequestFilter {

    private final RateLimitService rateLimitService;
    private final RateLimitProperties props;
    private final ObjectMapper objectMapper;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String path = request.getRequestURI();
        String ip = resolveClientIp(request);

        try {
            applyRateLimit(path, ip, request);
        } catch (RateLimitExceededException ex) {
            log.warn("Rate limit exceeded: path={}, ip={}", path, ip);
            sendRateLimitResponse(response);
            return;
        }

        filterChain.doFilter(request, response);
    }

    private void applyRateLimit(String path, String ip, HttpServletRequest request) {
        RateLimitProperties.Rule login = props.getLogin();
        RateLimitProperties.Rule register = props.getRegister();
        RateLimitProperties.Rule general = props.getGeneral();

        if (path.equals("/api/v1/auth/login")) {
            String email = extractEmailFromBody(request);
            String key = CacheKeyUtil.rateLimitLogin(ip, email != null ? email : ip);
            rateLimitService.enforce(key, login.getMaxRequests(), login.getWindowSeconds());

        } else if (path.equals("/api/v1/auth/register")) {
            String key = CacheKeyUtil.rateLimitRegister(ip);
            rateLimitService.enforce(key, register.getMaxRequests(), register.getWindowSeconds());

        } else if (path.startsWith("/api/")) {
            // General API rate limit per IP
            String key = CacheKeyUtil.rateLimitGeneral(ip);
            rateLimitService.enforce(key, general.getMaxRequests(), general.getWindowSeconds());
        }
    }

    private String resolveClientIp(HttpServletRequest request) {
        String forwarded = request.getHeader("X-Forwarded-For");
        if (forwarded != null && !forwarded.isBlank()) {
            return forwarded.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }

    private String extractEmailFromBody(HttpServletRequest request) {
        // We avoid reading the request body here to prevent consuming the stream.
        // Use a request parameter or rely on IP only for login rate limiting.
        String email = request.getParameter("email");
        return email != null ? email.toLowerCase().trim() : null;
    }

    private void sendRateLimitResponse(HttpServletResponse response) throws IOException {
        ErrorResponse body = ErrorResponse.builder()
                .success(false)
                .message("Too many requests. Please try again later.")
                .errorCode("RATE_LIMIT_EXCEEDED")
                .timestamp(Instant.now())
                .build();

        response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        objectMapper.writeValue(response.getWriter(), body);
    }
}

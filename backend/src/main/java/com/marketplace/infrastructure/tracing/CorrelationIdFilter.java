package com.marketplace.infrastructure.tracing;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@Order(0)
public class CorrelationIdFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        try {
            String correlationId = request.getHeader(CorrelationIdUtil.HEADER_NAME);
            if (!StringUtils.hasText(correlationId)) {
                correlationId = CorrelationIdUtil.generate();
            }
            CorrelationIdUtil.set(correlationId);
            response.setHeader(CorrelationIdUtil.HEADER_NAME, correlationId);
            filterChain.doFilter(request, response);
        } finally {
            CorrelationIdUtil.clear();
        }
    }
}

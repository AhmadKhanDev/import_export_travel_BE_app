package com.marketplace.realtime.config;

import com.marketplace.realtime.socket.RealtimeWebSocketHandler;
import com.marketplace.realtime.socket.UserPrincipalHandshakeHandler;
import com.marketplace.realtime.socket.WebSocketAuthHandshakeInterceptor;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

import java.util.Arrays;

@Configuration
@EnableWebSocket
@RequiredArgsConstructor
public class RealtimeWebSocketConfig implements WebSocketConfigurer {

    private final RealtimeWebSocketHandler realtimeWebSocketHandler;
    private final WebSocketAuthHandshakeInterceptor authHandshakeInterceptor;
    private final UserPrincipalHandshakeHandler handshakeHandler;

    @Value("${app.cors.allowed-origins:http://localhost:5174,http://localhost:5173,http://localhost:3000}")
    private String allowedOrigins;

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(realtimeWebSocketHandler, "/ws")
                .setHandshakeHandler(handshakeHandler)
                .addInterceptors(authHandshakeInterceptor)
                .setAllowedOrigins(Arrays.stream(allowedOrigins.split(","))
                        .map(String::trim)
                        .toArray(String[]::new));
    }
}

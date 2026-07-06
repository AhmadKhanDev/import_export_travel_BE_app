package com.marketplace.realtime.socket;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.marketplace.common.exception.BadRequestException;
import com.marketplace.common.security.UserPrincipal;
import com.marketplace.realtime.dto.RealtimeClientMessage;
import com.marketplace.realtime.service.RealtimeAuthorizationService;
import com.marketplace.realtime.service.RealtimeChannels;
import com.marketplace.realtime.service.RealtimeSessionRegistry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class RealtimeWebSocketHandler extends TextWebSocketHandler {

    private final ObjectMapper objectMapper;
    private final RealtimeSessionRegistry sessionRegistry;
    private final RealtimeAuthorizationService authorizationService;

    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        sessionRegistry.register(session);
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws IOException {
        RealtimeClientMessage clientMessage = objectMapper.readValue(message.getPayload(), RealtimeClientMessage.class);
        String type = clientMessage.getType() == null ? "" : clientMessage.getType().trim().toLowerCase();
        String channelKey = resolveChannelKey(clientMessage, currentPrincipal(session));

        switch (type) {
            case "subscribe" -> sessionRegistry.subscribe(session, channelKey);
            case "unsubscribe" -> sessionRegistry.unsubscribe(session.getId(), channelKey);
            default -> throw new BadRequestException("Unsupported realtime message type");
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        sessionRegistry.unregister(session);
    }

    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) {
        log.debug("Realtime transport error on session {}", session.getId(), exception);
        sessionRegistry.unregister(session);
    }

    private String resolveChannelKey(RealtimeClientMessage message, UserPrincipal principal) {
        String channel = message.getChannel() == null ? "" : message.getChannel().trim();

        return switch (channel) {
            case RealtimeChannels.TRACKING_BOOKING -> {
                UUID bookingId = parseUuid(message.getBookingId(), "bookingId");
                authorizationService.assertTrackingAccess(bookingId, principal);
                yield RealtimeChannels.trackingBookingKey(bookingId);
            }
            case RealtimeChannels.CHAT_ROOM -> {
                UUID roomId = parseUuid(message.getRoomId(), "roomId");
                authorizationService.assertChatRoomAccess(roomId, principal);
                yield RealtimeChannels.chatRoomKey(roomId);
            }
            case RealtimeChannels.CHAT_INBOX -> RealtimeChannels.chatInboxKey(principal.getId());
            default -> throw new BadRequestException("Unsupported realtime channel");
        };
    }

    private UUID parseUuid(String rawValue, String fieldName) {
        try {
            return UUID.fromString(rawValue);
        } catch (Exception ex) {
            throw new BadRequestException(fieldName + " must be a valid UUID");
        }
    }

    private UserPrincipal currentPrincipal(WebSocketSession session) {
        Object principal = session.getPrincipal();
        if (principal instanceof UsernamePasswordAuthenticationToken auth
                && auth.getPrincipal() instanceof UserPrincipal userPrincipal) {
            return userPrincipal;
        }
        throw new BadRequestException("Unauthenticated realtime session");
    }
}

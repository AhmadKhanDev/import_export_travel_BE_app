package com.marketplace.realtime.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.marketplace.realtime.dto.RealtimeServerEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import java.io.IOException;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Service
@RequiredArgsConstructor
public class RealtimeSessionRegistry {

    private final ObjectMapper objectMapper;
    private final ConcurrentHashMap<String, WebSocketSession> sessions = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, Set<String>> sessionsByChannel = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, Set<String>> channelsBySession = new ConcurrentHashMap<>();

    public void register(WebSocketSession session) {
        sessions.put(session.getId(), session);
        channelsBySession.putIfAbsent(session.getId(), ConcurrentHashMap.newKeySet());
    }

    public void unregister(WebSocketSession session) {
        Set<String> channels = channelsBySession.remove(session.getId());
        if (channels != null) {
            channels.forEach(channel -> unsubscribe(session.getId(), channel));
        }
        sessions.remove(session.getId());
    }

    public void subscribe(WebSocketSession session, String channelKey) {
        register(session);
        sessionsByChannel.computeIfAbsent(channelKey, key -> ConcurrentHashMap.newKeySet()).add(session.getId());
        channelsBySession.computeIfAbsent(session.getId(), key -> ConcurrentHashMap.newKeySet()).add(channelKey);
    }

    public void unsubscribe(String sessionId, String channelKey) {
        Set<String> subscriberIds = sessionsByChannel.get(channelKey);
        if (subscriberIds != null) {
            subscriberIds.remove(sessionId);
            if (subscriberIds.isEmpty()) {
                sessionsByChannel.remove(channelKey);
            }
        }

        Set<String> channelKeys = channelsBySession.get(sessionId);
        if (channelKeys != null) {
            channelKeys.remove(channelKey);
        }
    }

    public void publish(String channelKey, RealtimeServerEvent event) {
        Set<String> subscriberIds = sessionsByChannel.get(channelKey);
        if (subscriberIds == null || subscriberIds.isEmpty()) {
            return;
        }

        String payload = toJson(event);
        subscriberIds.forEach(sessionId -> {
            WebSocketSession session = sessions.get(sessionId);
            if (session == null || !session.isOpen()) {
                unsubscribe(sessionId, channelKey);
                return;
            }

            try {
                synchronized (session) {
                    session.sendMessage(new TextMessage(payload));
                }
            } catch (IOException ex) {
                log.debug("Failed to publish realtime event to session {}", sessionId, ex);
                unregister(session);
            }
        });
    }

    private String toJson(RealtimeServerEvent event) {
        try {
            return objectMapper.writeValueAsString(event);
        } catch (JsonProcessingException ex) {
            throw new IllegalStateException("Unable to serialize realtime event", ex);
        }
    }
}

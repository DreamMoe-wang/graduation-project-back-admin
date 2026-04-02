package com.example.admin.websocket;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * 聊天实时通知
 */
@Component
public class ChatRealtimeNotifier {

    private final ObjectMapper objectMapper;
    private final ChatWebSocketSessionManager sessionManager;

    public ChatRealtimeNotifier(ObjectMapper objectMapper,
                                ChatWebSocketSessionManager sessionManager) {
        this.objectMapper = objectMapper;
        this.sessionManager = sessionManager;
    }

    public void notifySessionUpdated(Long sessionId, Long senderId, Collection<Long> receivers) {
        if (sessionId == null || receivers == null || receivers.isEmpty()) {
            return;
        }

        Map<String, Object> payload = new HashMap<>();
        payload.put("type", "session_updated");
        payload.put("sessionId", sessionId);
        if (senderId != null) {
            payload.put("senderId", senderId);
        }

        try {
            sessionManager.sendToUsers(receivers, objectMapper.writeValueAsString(payload));
        } catch (Exception ignored) {
            // ignore
        }
    }
}

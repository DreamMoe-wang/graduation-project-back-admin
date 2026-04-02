package com.example.admin.websocket;

import cn.hutool.core.convert.Convert;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * WebSocket 会话管理
 */
@Component
public class ChatWebSocketSessionManager {

    private final Map<Long, Set<WebSocketSession>> userSessions = new ConcurrentHashMap<>();

    public void register(Long userId, WebSocketSession session) {
        if (userId == null || session == null) {
            return;
        }
        userSessions.computeIfAbsent(userId, key -> ConcurrentHashMap.newKeySet()).add(session);
    }

    public void unregister(WebSocketSession session) {
        Long userId = resolveUserId(session);
        if (userId == null) {
            return;
        }
        Set<WebSocketSession> sessions = userSessions.get(userId);
        if (sessions == null) {
            return;
        }
        sessions.remove(session);
        if (sessions.isEmpty()) {
            userSessions.remove(userId);
        }
    }

    public Long resolveUserId(WebSocketSession session) {
        if (session == null) {
            return null;
        }
        Object value = session.getAttributes().get(ChatHandshakeInterceptor.ATTR_USER_ID);
        return Convert.toLong(value, null);
    }

    public void sendToSession(WebSocketSession session, String payload) {
        if (session == null || payload == null) {
            return;
        }
        send(session, payload);
    }

    public void sendToUser(Long userId, String payload) {
        if (userId == null || payload == null) {
            return;
        }
        Set<WebSocketSession> sessions = userSessions.getOrDefault(userId, Collections.emptySet());
        for (WebSocketSession session : sessions) {
            send(session, payload);
        }
    }

    public void sendToUsers(Collection<Long> userIds, String payload) {
        if (userIds == null || userIds.isEmpty() || payload == null) {
            return;
        }
        for (Long userId : userIds) {
            sendToUser(userId, payload);
        }
    }

    private void send(WebSocketSession session, String payload) {
        if (session == null || !session.isOpen()) {
            return;
        }
        synchronized (session) {
            if (!session.isOpen()) {
                return;
            }
            try {
                session.sendMessage(new TextMessage(payload));
            } catch (IOException ignored) {
                // 忽略已断开的连接
            }
        }
    }
}

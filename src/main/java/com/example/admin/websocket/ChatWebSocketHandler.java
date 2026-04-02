package com.example.admin.websocket;

import cn.hutool.core.util.StrUtil;
import com.example.admin.dto.ChatMessageSendDTO;
import com.example.admin.service.ChatService;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * 聊天 WebSocket 消息处理
 */
@Component
public class ChatWebSocketHandler extends TextWebSocketHandler {

    private final ObjectMapper objectMapper;
    private final ChatService chatService;
    private final ChatWebSocketSessionManager sessionManager;

    public ChatWebSocketHandler(ObjectMapper objectMapper,
                                ChatService chatService,
                                ChatWebSocketSessionManager sessionManager) {
        this.objectMapper = objectMapper;
        this.chatService = chatService;
        this.sessionManager = sessionManager;
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws IOException {
        Long userId = sessionManager.resolveUserId(session);
        if (userId == null) {
            session.close(CloseStatus.NOT_ACCEPTABLE.withReason("未授权连接"));
            return;
        }
        sessionManager.register(userId, session);
        sessionManager.sendToSession(session, toJson(payload("connected", null, null, null)));
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) {
        Long userId = sessionManager.resolveUserId(session);
        if (userId == null) {
            return;
        }

        String clientMessageId = null;
        Long sessionId = null;

        try {
            Map<String, Object> body = objectMapper.readValue(message.getPayload(), new TypeReference<Map<String, Object>>() {});
            String type = toString(body.get("type"));
            clientMessageId = toString(body.get("clientMessageId"));
            sessionId = toLong(body.get("sessionId"));

            if ("ping".equalsIgnoreCase(type)) {
                sessionManager.sendToSession(session, toJson(payload("pong", null, null, clientMessageId)));
                return;
            }

            if (!"send".equalsIgnoreCase(type)) {
                sessionManager.sendToSession(session, toJson(payload("error", null, "不支持的消息类型", clientMessageId)));
                return;
            }

            String content = toString(body.get("content"));
            if (sessionId == null || sessionId <= 0 || StrUtil.isBlank(content)) {
                sessionManager.sendToSession(session, toJson(payload("error", sessionId, "消息内容不能为空", clientMessageId)));
                return;
            }

            ChatMessageSendDTO sendDTO = new ChatMessageSendDTO();
            sendDTO.setContent(content);
            boolean sent = chatService.sendMessageByUser(sessionId, userId, sendDTO);
            if (sent) {
                sessionManager.sendToSession(session, toJson(payload("send_ack", sessionId, null, clientMessageId)));
            } else {
                sessionManager.sendToSession(session, toJson(payload("error", sessionId, "发送失败", clientMessageId)));
            }
        } catch (Exception ex) {
            String messageText = StrUtil.blankToDefault(ex.getMessage(), "发送失败");
            sessionManager.sendToSession(session, toJson(payload("error", sessionId, messageText, clientMessageId)));
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        sessionManager.unregister(session);
    }

    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) {
        sessionManager.unregister(session);
        if (session.isOpen()) {
            try {
                session.close(CloseStatus.SERVER_ERROR);
            } catch (IOException ignored) {
                // ignore
            }
        }
    }

    private String toJson(Map<String, Object> payload) {
        try {
            return objectMapper.writeValueAsString(payload);
        } catch (Exception ex) {
            return "{\"type\":\"error\",\"message\":\"消息序列化失败\"}";
        }
    }

    private Map<String, Object> payload(String type, Long sessionId, String message, String clientMessageId) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("type", type);
        if (sessionId != null) {
            payload.put("sessionId", sessionId);
        }
        if (StrUtil.isNotBlank(message)) {
            payload.put("message", message);
        }
        if (StrUtil.isNotBlank(clientMessageId)) {
            payload.put("clientMessageId", clientMessageId);
        }
        return payload;
    }

    private Long toLong(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof Number) {
            return ((Number) value).longValue();
        }
        try {
            return Long.valueOf(String.valueOf(value));
        } catch (NumberFormatException ex) {
            return null;
        }
    }

    private String toString(Object value) {
        return value == null ? null : String.valueOf(value);
    }
}

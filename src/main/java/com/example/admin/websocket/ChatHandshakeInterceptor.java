package com.example.admin.websocket;

import cn.hutool.core.util.StrUtil;
import com.example.admin.security.JwtTokenService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;

/**
 * WebSocket 握手鉴权
 */
@Component
public class ChatHandshakeInterceptor implements HandshakeInterceptor {

    public static final String ATTR_USER_ID = "chatUserId";

    @Value("${jwt.header}")
    private String authHeader;

    @Value("${jwt.prefix}")
    private String tokenPrefix;

    private final JwtTokenService jwtTokenService;

    public ChatHandshakeInterceptor(JwtTokenService jwtTokenService) {
        this.jwtTokenService = jwtTokenService;
    }

    @Override
    public boolean beforeHandshake(ServerHttpRequest request, ServerHttpResponse response,
                                   WebSocketHandler wsHandler, Map<String, Object> attributes) {
        if (!(request instanceof ServletServerHttpRequest)) {
            return false;
        }

        HttpServletRequest servletRequest = ((ServletServerHttpRequest) request).getServletRequest();
        String token = resolveToken(servletRequest.getHeader(authHeader));
        if (StrUtil.isBlank(token)) {
            token = resolveToken(servletRequest.getParameter("token"));
        }

        if (StrUtil.isBlank(token) || !jwtTokenService.validateToken(token)) {
            return false;
        }

        Long userId = jwtTokenService.getUserId(token);
        if (userId == null || userId <= 0) {
            return false;
        }

        attributes.put(ATTR_USER_ID, userId);
        return true;
    }

    @Override
    public void afterHandshake(ServerHttpRequest request, ServerHttpResponse response,
                               WebSocketHandler wsHandler, Exception exception) {
        // no-op
    }

    private String resolveToken(String value) {
        if (StrUtil.isBlank(value)) {
            return null;
        }
        String prefix = tokenPrefix + " ";
        if (value.startsWith(prefix)) {
            return value.substring(prefix.length());
        }
        return value;
    }
}

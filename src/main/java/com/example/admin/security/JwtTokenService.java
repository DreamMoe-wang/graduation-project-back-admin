package com.example.admin.security;

import cn.hutool.core.convert.Convert;
import cn.hutool.core.util.StrUtil;
import cn.hutool.jwt.JWT;
import cn.hutool.jwt.JWTUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * JWT 令牌服务
 */
@Component
public class JwtTokenService {

    @Value("${jwt.secret}")
    private String jwtSecret;

    @Value("${jwt.expiration}")
    private Long jwtExpiration;

    @Value("${jwt.prefix}")
    private String tokenPrefix;

    public String createToken(SecurityUser securityUser) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("userId", securityUser.getId());
        payload.put("username", securityUser.getUsername());
        payload.put("nickname", securityUser.getNickname());
        payload.put("roles", securityUser.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.joining(",")));
        payload.put("exp", System.currentTimeMillis() + jwtExpiration);
        return JWTUtil.createToken(payload, jwtSecret.getBytes());
    }

    public boolean validateToken(String token) {
        if (StrUtil.isBlank(token)) {
            return false;
        }
        if (!JWTUtil.verify(token, jwtSecret.getBytes())) {
            return false;
        }
        JWT jwt = JWTUtil.parseToken(token);
        Object expValue = jwt.getPayload("exp");
        long exp = Convert.toLong(expValue, 0L);
        return exp > System.currentTimeMillis();
    }

    public String getUsername(String token) {
        JWT jwt = JWTUtil.parseToken(token);
        return Convert.toStr(jwt.getPayload("username"));
    }

    public Long getUserId(String token) {
        JWT jwt = JWTUtil.parseToken(token);
        return Convert.toLong(jwt.getPayload("userId"));
    }

    public String getNickname(String token) {
        JWT jwt = JWTUtil.parseToken(token);
        return Convert.toStr(jwt.getPayload("nickname"));
    }

    public List<String> getAuthorities(String token) {
        JWT jwt = JWTUtil.parseToken(token);
        String authorities = Convert.toStr(jwt.getPayload("roles"));
        if (StrUtil.isBlank(authorities)) {
            return java.util.Collections.emptyList();
        }
        return Stream.of(authorities.split(","))
                .map(String::trim)
                .filter(StrUtil::isNotBlank)
                .distinct()
                .collect(Collectors.toList());
    }

    public Long getExpiration() {
        return jwtExpiration;
    }

    public String getTokenType() {
        return tokenPrefix;
    }
}

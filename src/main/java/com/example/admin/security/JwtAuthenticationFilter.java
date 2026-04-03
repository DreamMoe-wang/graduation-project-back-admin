package com.example.admin.security;

import cn.hutool.core.util.StrUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.annotation.Resource;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

/**
 * JWT 认证过滤器
 */
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    @Value("${jwt.header}")
    private String authHeader;

    @Value("${jwt.prefix}")
    private String tokenPrefix;

    @Resource
    private JwtTokenService jwtTokenService;

    @Resource
    private CustomUserDetailsService userDetailsService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        String header = request.getHeader(authHeader);
        String token = resolveToken(header);

        if (StrUtil.isNotBlank(token)
                && jwtTokenService.validateToken(token)
                && SecurityContextHolder.getContext().getAuthentication() == null) {
            UsernamePasswordAuthenticationToken authentication = buildAuthenticationByToken(token, request);
            if (authentication == null) {
                String username = jwtTokenService.getUsername(token);
                UserDetails userDetails = userDetailsService.loadUserByUsername(username);
                authentication = new UsernamePasswordAuthenticationToken(
                        userDetails, null, userDetails.getAuthorities());
                authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
            }
            SecurityContextHolder.getContext().setAuthentication(authentication);
        }

        filterChain.doFilter(request, response);
    }

    private String resolveToken(String header) {
        if (StrUtil.isBlank(header)) {
            return null;
        }
        String prefix = tokenPrefix + " ";
        if (header.startsWith(prefix)) {
            return header.substring(prefix.length());
        }
        return header;
    }

    private UsernamePasswordAuthenticationToken buildAuthenticationByToken(String token, HttpServletRequest request) {
        Long userId = jwtTokenService.getUserId(token);
        String username = jwtTokenService.getUsername(token);
        if (userId == null || userId <= 0 || StrUtil.isBlank(username)) {
            return null;
        }

        List<String> authorityCodes = jwtTokenService.getAuthorities(token);
        if (authorityCodes == null || authorityCodes.isEmpty()) {
            return null;
        }

        List<SimpleGrantedAuthority> authorities = authorityCodes.stream()
                .map(SimpleGrantedAuthority::new)
                .collect(Collectors.toList());
        SecurityUser securityUser = new SecurityUser(
                userId,
                username,
                "",
                StrUtil.blankToDefault(jwtTokenService.getNickname(token), username),
                true,
                authorities
        );

        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                securityUser, null, securityUser.getAuthorities());
        authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
        return authentication;
    }
}

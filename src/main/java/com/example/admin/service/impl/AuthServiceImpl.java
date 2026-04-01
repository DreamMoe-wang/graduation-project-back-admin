package com.example.admin.service.impl;

import cn.hutool.core.util.StrUtil;
import com.example.admin.common.assembler.UserProfileAssembler;
import com.example.admin.dto.LoginDTO;
import com.example.admin.entity.Role;
import com.example.admin.entity.User;
import com.example.admin.entity.UserRole;
import com.example.admin.mapper.RoleMapper;
import com.example.admin.mapper.UserMapper;
import com.example.admin.mapper.UserRoleMapper;
import com.example.admin.security.JwtTokenService;
import com.example.admin.security.SecurityUser;
import com.example.admin.security.SecurityUtils;
import com.example.admin.service.AuthService;
import com.example.admin.service.MenuService;
import com.example.admin.vo.CurrentUserVO;
import com.example.admin.vo.LoginVO;
import com.example.admin.vo.MenuVO;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 认证服务实现
 */
@Service
public class AuthServiceImpl implements AuthService {

    @Resource
    private AuthenticationManager authenticationManager;

    @Resource
    private JwtTokenService jwtTokenService;

    @Resource
    private UserMapper userMapper;

    @Resource
    private UserRoleMapper userRoleMapper;

    @Resource
    private RoleMapper roleMapper;

    @Resource
    private UserProfileAssembler userProfileAssembler;

    @Resource
    private MenuService menuService;

    @Override
    public LoginVO login(LoginDTO loginDTO) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(loginDTO.getUsername(), loginDTO.getPassword())
        );

        SecurityUser securityUser = (SecurityUser) authentication.getPrincipal();
        User user = userMapper.selectById(securityUser.getId());
        user.setLastLoginTime(LocalDateTime.now());
        userMapper.updateLastLoginTime(user.getId(), user.getLastLoginTime());

        List<String> authorities = securityUser.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toList());
        List<String> permissions = menuService.getPermissionCodesByUserId(user.getId());
        List<Role> roles = getRoles(user.getId());

        return LoginVO.builder()
                .userId(securityUser.getId())
                .username(securityUser.getUsername())
                .nickname(StrUtil.blankToDefault(securityUser.getNickname(), securityUser.getUsername()))
                .displayName(StrUtil.blankToDefault(securityUser.getNickname(), securityUser.getUsername()))
                .avatar(user.getAvatar())
                .phone(user.getPhone())
                .email(user.getEmail())
                .token(jwtTokenService.createToken(securityUser))
                .tokenType(jwtTokenService.getTokenType())
                .expiresIn(jwtTokenService.getExpiration())
                .roles(roles.stream().map(Role::getRoleCode).collect(Collectors.toList()))
                .roleNames(roles.stream().map(Role::getRoleName).collect(Collectors.toList()))
                .authorities(authorities)
                .permissions(permissions)
                .userInfo(userProfileAssembler.toProfileWithRoles(user))
                .menus(menuService.getMenuTreeByUserId(user.getId()))
                .build();
    }

    @Override
    public CurrentUserVO currentUser() {
        Long userId = SecurityUtils.requireCurrentUserId();
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new RuntimeException("当前用户不存在");
        }

        List<Role> roles = getRoles(userId);
        List<String> permissions = menuService.getPermissionCodesByUserId(userId);
        return CurrentUserVO.builder()
                .userId(user.getId())
                .username(user.getUsername())
                .nickname(StrUtil.blankToDefault(user.getNickname(), user.getUsername()))
                .displayName(StrUtil.blankToDefault(user.getNickname(), user.getUsername()))
                .avatar(user.getAvatar())
                .phone(user.getPhone())
                .email(user.getEmail())
                .roles(roles.stream().map(Role::getRoleCode).collect(Collectors.toList()))
                .roleNames(roles.stream().map(Role::getRoleName).collect(Collectors.toList()))
                .authorities(roles.stream()
                        .map(Role::getRoleCode)
                        .map(code -> code.startsWith("ROLE_") ? code : "ROLE_" + code)
                        .collect(Collectors.toList()))
                .permissions(permissions)
                .userInfo(userProfileAssembler.toProfileWithRoles(user))
                .menus(menuService.getCurrentMenuTree())
                .build();
    }

    @Override
    public List<MenuVO> currentMenus() {
        return menuService.getCurrentMenuTree();
    }

    @Override
    public List<String> currentPermissions() {
        return menuService.getCurrentPermissionCodes();
    }

    private List<Role> getRoles(Long userId) {
        List<UserRole> userRoles = userRoleMapper.selectByUserId(userId);
        if (userRoles.isEmpty()) {
            return Collections.emptyList();
        }
        List<Long> roleIds = userRoles.stream().map(UserRole::getRoleId).collect(Collectors.toList());
        return roleMapper.selectByIds(roleIds);
    }
}

package com.example.admin.security;

import com.example.admin.entity.Menu;
import com.example.admin.entity.Role;
import com.example.admin.entity.RoleMenu;
import com.example.admin.entity.User;
import com.example.admin.entity.UserRole;
import com.example.admin.mapper.MenuMapper;
import com.example.admin.mapper.RoleMapper;
import com.example.admin.mapper.RoleMenuMapper;
import com.example.admin.mapper.UserMapper;
import com.example.admin.mapper.UserRoleMapper;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 用户认证加载服务
 */
@Service
public class CustomUserDetailsService implements UserDetailsService {

    @Resource
    private UserMapper userMapper;

    @Resource
    private UserRoleMapper userRoleMapper;

    @Resource
    private RoleMapper roleMapper;

    @Resource
    private RoleMenuMapper roleMenuMapper;

    @Resource
    private MenuMapper menuMapper;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userMapper.selectByUsername(username);
        if (user == null || user.getDeleted() != null && user.getDeleted() == 1) {
            throw new UsernameNotFoundException("用户不存在");
        }

        return new SecurityUser(
                user.getId(),
                user.getUsername(),
                user.getPassword(),
                user.getNickname(),
                user.getStatus() != null && user.getStatus() == 1,
                loadAuthorities(user.getId())
        );
    }

    private List<SimpleGrantedAuthority> loadAuthorities(Long userId) {
        List<UserRole> userRoles = userRoleMapper.selectByUserId(userId);
        if (userRoles.isEmpty()) {
            return Collections.emptyList();
        }

        List<Long> roleIds = userRoles.stream()
                .map(UserRole::getRoleId)
                .collect(Collectors.toList());

        List<SimpleGrantedAuthority> roleAuthorities = roleMapper.selectByIds(roleIds).stream()
                .filter(role -> role.getStatus() != null && role.getStatus() == 1)
                .map(Role::getRoleCode)
                .map(code -> code.startsWith("ROLE_") ? code : "ROLE_" + code)
                .map(SimpleGrantedAuthority::new)
                .collect(Collectors.toList());

        List<RoleMenu> roleMenus = roleMenuMapper.selectByRoleIds(roleIds);
        if (roleMenus.isEmpty()) {
            return roleAuthorities;
        }

        List<Long> menuIds = roleMenus.stream()
                .map(RoleMenu::getMenuId)
                .distinct()
                .collect(Collectors.toList());

        List<SimpleGrantedAuthority> permissionAuthorities = menuMapper.selectEnabledByIds(menuIds).stream()
                .map(Menu::getPermissionCode)
                .filter(code -> code != null && !code.trim().isEmpty())
                .map(SimpleGrantedAuthority::new)
                .collect(Collectors.toList());

        roleAuthorities.addAll(permissionAuthorities);
        return roleAuthorities.stream()
                .collect(Collectors.collectingAndThen(
                        Collectors.toMap(SimpleGrantedAuthority::getAuthority, item -> item, (left, right) -> left),
                        Map::values
                ))
                .stream()
                .collect(Collectors.toList());
    }
}

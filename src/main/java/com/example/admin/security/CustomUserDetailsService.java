package com.example.admin.security;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.admin.entity.Role;
import com.example.admin.entity.User;
import com.example.admin.entity.UserRole;
import com.example.admin.mapper.RoleMapper;
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

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getUsername, username);
        User user = userMapper.selectOne(wrapper);
        if (user == null || user.getDeleted() != null && user.getDeleted() == 1) {
            throw new UsernameNotFoundException("用户不存在");
        }

        List<SimpleGrantedAuthority> authorities = loadAuthorities(user.getId());
        return new SecurityUser(
                user.getId(),
                user.getUsername(),
                user.getPassword(),
                user.getNickname(),
                user.getStatus() != null && user.getStatus() == 1,
                authorities
        );
    }

    private List<SimpleGrantedAuthority> loadAuthorities(Long userId) {
        LambdaQueryWrapper<UserRole> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserRole::getUserId, userId);
        List<UserRole> userRoleList = userRoleMapper.selectList(wrapper);
        if (userRoleList.isEmpty()) {
            return Collections.emptyList();
        }

        List<Long> roleIds = userRoleList.stream()
                .map(UserRole::getRoleId)
                .collect(Collectors.toList());
        return roleMapper.selectBatchIds(roleIds).stream()
                .filter(role -> role.getStatus() != null && role.getStatus() == 1)
                .map(Role::getRoleCode)
                .map(code -> code.startsWith("ROLE_") ? code : "ROLE_" + code)
                .map(SimpleGrantedAuthority::new)
                .collect(Collectors.toList());
    }
}

package com.example.admin.common.assembler;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.admin.entity.Role;
import com.example.admin.entity.User;
import com.example.admin.entity.UserRole;
import com.example.admin.mapper.RoleMapper;
import com.example.admin.mapper.UserRoleMapper;
import com.example.admin.vo.UserProfileVO;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 用户展示对象组装器
 */
@Component
public class UserProfileAssembler {

    @Resource
    private UserRoleMapper userRoleMapper;

    @Resource
    private RoleMapper roleMapper;

    public UserProfileVO toProfile(User user) {
        return toProfile(user, false);
    }

    public UserProfileVO toProfileWithRoles(User user) {
        return toProfile(user, true);
    }

    private UserProfileVO toProfile(User user, boolean includeRoles) {
        if (user == null) {
            return null;
        }

        List<String> roleCodes = Collections.emptyList();
        List<String> roleNames = Collections.emptyList();
        if (includeRoles) {
            List<Role> roles = loadRoles(user.getId());
            roleCodes = roles.stream().map(Role::getRoleCode).collect(Collectors.toList());
            roleNames = roles.stream().map(Role::getRoleName).collect(Collectors.toList());
        }

        return UserProfileVO.builder()
                .id(user.getId())
                .userId(user.getId())
                .username(user.getUsername())
                .nickname(user.getNickname())
                .displayName(StrUtil.blankToDefault(user.getNickname(), user.getUsername()))
                .avatar(user.getAvatar())
                .phone(user.getPhone())
                .email(user.getEmail())
                .roles(roleCodes)
                .roleNames(roleNames)
                .build();
    }

    private List<Role> loadRoles(Long userId) {
        LambdaQueryWrapper<UserRole> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserRole::getUserId, userId);
        List<UserRole> userRoles = userRoleMapper.selectList(wrapper);
        if (userRoles.isEmpty()) {
            return Collections.emptyList();
        }
        List<Long> roleIds = userRoles.stream()
                .map(UserRole::getRoleId)
                .collect(Collectors.toList());
        return roleMapper.selectBatchIds(roleIds);
    }
}

package com.example.admin.service.impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.admin.common.assembler.UserProfileAssembler;
import com.example.admin.dto.LoginDTO;
import com.example.admin.dto.UserDTO;
import com.example.admin.entity.Role;
import com.example.admin.entity.User;
import com.example.admin.entity.UserRole;
import com.example.admin.mapper.RoleMapper;
import com.example.admin.mapper.UserMapper;
import com.example.admin.mapper.UserRoleMapper;
import com.example.admin.security.SecurityUtils;
import com.example.admin.service.AuthService;
import com.example.admin.service.UserService;
import com.example.admin.vo.UserProfileVO;
import org.springframework.beans.BeanUtils;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

/**
 * 用户服务实现类
 */
@Service
public class UserServiceImpl implements UserService {

    private static final String DEFAULT_USER_ROLE_CODE = "USER";

    @Resource
    private UserMapper userMapper;

    @Resource
    private UserRoleMapper userRoleMapper;

    @Resource
    private RoleMapper roleMapper;

    @Resource
    private PasswordEncoder passwordEncoder;

    @Resource
    private AuthService authService;

    @Resource
    private UserProfileAssembler userProfileAssembler;

    @Override
    public User getById(Long id) {
        return userMapper.selectById(id);
    }

    @Override
    public UserProfileVO getProfileById(Long id) {
        User user = getById(id);
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }
        assertUserAccessible(user);
        return userProfileAssembler.toProfileWithRoles(user);
    }

    @Override
    public User getByUsername(String username) {
        return userMapper.selectByUsername(username);
    }

    @Override
    public Page<User> pageList(int pageNum, int pageSize) {
        int currentPage = Math.max(pageNum, 1);
        int size = Math.max(pageSize, 1);
        long total = userMapper.countPage();
        List<User> records = userMapper.selectPage((long) (currentPage - 1) * size, size);
        records.forEach(item -> item.setPassword(null));

        Page<User> page = new Page<>(currentPage, size);
        page.setTotal(total);
        page.setRecords(records);
        return page;
    }

    @Override
    public List<User> list() {
        List<User> list = userMapper.selectAll();
        list.forEach(item -> item.setPassword(null));
        return list;
    }

    @Override
    public boolean create(UserDTO userDTO) {
        checkUsernameUnique(userDTO.getUsername(), null);
        checkEmailUnique(userDTO.getEmail(), null);

        User user = new User();
        BeanUtils.copyProperties(userDTO, user);
        user.setPassword(passwordEncoder.encode(userDTO.getPassword()));
        user.setNickname(StrUtil.blankToDefault(userDTO.getNickname(), userDTO.getUsername()));
        user.setStatus(resolveStatus(userDTO.getStatus()));
        user.setDeleted(0);

        boolean inserted = userMapper.insertUser(user) > 0;
        if (inserted) {
            bindDefaultUserRole(user.getId());
        }
        return inserted;
    }

    @Override
    public boolean update(Long id, UserDTO userDTO) {
        User user = getById(id);
        if (user == null) {
            return false;
        }

        checkUsernameUnique(userDTO.getUsername(), id);
        checkEmailUnique(userDTO.getEmail(), id);

        BeanUtils.copyProperties(userDTO, user, "password");
        if (StrUtil.isNotBlank(userDTO.getPassword())) {
            user.setPassword(passwordEncoder.encode(userDTO.getPassword()));
        }
        if (StrUtil.isBlank(user.getNickname())) {
            user.setNickname(user.getUsername());
        }
        if (user.getStatus() == null) {
            user.setStatus(1);
        }

        return userMapper.updateUser(user) > 0;
    }

    @Override
    public boolean delete(Long id) {
        return userMapper.deleteById(id) > 0;
    }

    @Override
    public String login(String username, String password) {
        LoginDTO loginDTO = new LoginDTO();
        loginDTO.setUsername(username);
        loginDTO.setPassword(password);
        return authService.login(loginDTO).getToken();
    }

    private void bindDefaultUserRole(Long userId) {
        Role role = roleMapper.selectByRoleCode(DEFAULT_USER_ROLE_CODE);
        if (role == null) {
            throw new RuntimeException("默认用户角色不存在");
        }
        if (userRoleMapper.countByUserIdAndRoleId(userId, role.getId()) > 0) {
            return;
        }

        UserRole relation = new UserRole();
        relation.setUserId(userId);
        relation.setRoleId(role.getId());
        userRoleMapper.insertUserRole(relation);
    }

    private int resolveStatus(Integer status) {
        if (status == null) {
            return 1;
        }
        return SecurityUtils.isAdmin() ? status : 1;
    }

    private void checkUsernameUnique(String username, Long excludeId) {
        if (StrUtil.isBlank(username)) {
            return;
        }
        if (userMapper.countByUsername(username, excludeId) > 0) {
            throw new RuntimeException("用户名已存在");
        }
    }

    private void checkEmailUnique(String email, Long excludeId) {
        if (StrUtil.isBlank(email)) {
            return;
        }
        if (userMapper.countByEmail(email, excludeId) > 0) {
            throw new RuntimeException("邮箱已存在");
        }
    }

    private void assertUserAccessible(User user) {
        if (SecurityUtils.isAdmin()) {
            return;
        }
        Long currentUserId = SecurityUtils.requireCurrentUserId();
        if (!currentUserId.equals(user.getId())) {
            throw new AccessDeniedException("无权查看该用户信息");
        }
    }
}

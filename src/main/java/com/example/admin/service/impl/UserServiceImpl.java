package com.example.admin.service.impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.admin.common.assembler.UserProfileAssembler;
import com.example.admin.dto.LoginDTO;
import com.example.admin.dto.UserDTO;
import com.example.admin.dto.UserProfileUpdateDTO;
import com.example.admin.entity.Role;
import com.example.admin.entity.User;
import com.example.admin.entity.UserProfile;
import com.example.admin.entity.UserRole;
import com.example.admin.mapper.RoleMapper;
import com.example.admin.mapper.UserMapper;
import com.example.admin.mapper.UserProfileMapper;
import com.example.admin.mapper.UserRoleMapper;
import com.example.admin.security.SecurityUtils;
import com.example.admin.service.AuthService;
import com.example.admin.service.UserService;
import com.example.admin.vo.UserProfileVO;
import org.springframework.beans.BeanUtils;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 用户服务实现类
 */
@Service
public class UserServiceImpl implements UserService {

    private static final String DEFAULT_USER_ROLE_CODE = "USER";
    private static final BigDecimal DEFAULT_WALLET_BALANCE = new BigDecimal("100000.00");

    @Resource
    private UserMapper userMapper;

    @Resource
    private UserProfileMapper userProfileMapper;

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
        return buildUserProfileVO(user, true);
    }

    @Override
    public UserProfileVO getCurrentProfile() {
        User user = requireCurrentUser();
        return buildUserProfileVO(user, false);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public UserProfileVO updateCurrentProfile(UserProfileUpdateDTO updateDTO) {
        User user = requireCurrentUser();
        if (!StrUtil.equals(updateDTO.getEmail(), user.getEmail())) {
            checkEmailUnique(updateDTO.getEmail(), user.getId());
        }

        user.setNickname(StrUtil.blankToDefault(updateDTO.getNickname(), user.getUsername()));
        user.setAvatar(updateDTO.getAvatar());
        user.setPhone(updateDTO.getPhone());
        user.setEmail(updateDTO.getEmail());
        userMapper.updateUser(user);

        UserProfile userProfile = ensureUserProfile(user.getId());
        userProfile.setRealName(updateDTO.getRealName());
        userProfile.setGender(updateDTO.getGender());
        userProfile.setBirthday(parseBirthday(updateDTO.getBirthday()));
        userProfile.setCityName(updateDTO.getCityName());
        userProfile.setAreaName(updateDTO.getAreaName());
        userProfile.setAddress(updateDTO.getAddress());
        userProfile.setLongitude(updateDTO.getLongitude());
        userProfile.setLatitude(updateDTO.getLatitude());
        userProfile.setBio(updateDTO.getBio());
        userProfileMapper.updateUserProfile(userProfile);

        return buildUserProfileVO(user, false);
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
    @Transactional(rollbackFor = Exception.class)
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
            syncUserRoles(user.getId(), userDTO.getRoleIds(), true);
            createEmptyUserProfile(user.getId());
        }
        return inserted;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
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

        boolean updated = userMapper.updateUser(user) > 0;
        if (updated && userDTO.getRoleIds() != null) {
            syncUserRoles(user.getId(), userDTO.getRoleIds(), false);
        }
        return updated;
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

    private void syncUserRoles(Long userId, List<Long> roleIds, boolean fallbackToDefaultRole) {
        List<Long> normalizedRoleIds = normalizeRoleIds(roleIds);
        if (normalizedRoleIds.isEmpty() && fallbackToDefaultRole) {
            bindDefaultUserRole(userId);
            return;
        }

        userRoleMapper.deleteByUserId(userId);
        for (Long roleId : normalizedRoleIds) {
            UserRole relation = new UserRole();
            relation.setUserId(userId);
            relation.setRoleId(roleId);
            userRoleMapper.insertUserRole(relation);
        }
    }

    private List<Long> normalizeRoleIds(List<Long> roleIds) {
        if (roleIds == null || roleIds.isEmpty()) {
            return new ArrayList<>();
        }

        Set<Long> uniqueRoleIds = roleIds.stream()
                .filter(item -> item != null && item > 0)
                .collect(Collectors.toCollection(LinkedHashSet::new));
        if (uniqueRoleIds.isEmpty()) {
            return new ArrayList<>();
        }

        List<Role> roles = roleMapper.selectByIds(new ArrayList<>(uniqueRoleIds));
        if (roles.size() != uniqueRoleIds.size()) {
            throw new RuntimeException("部分角色不存在");
        }

        return roles.stream()
                .map(Role::getId)
                .collect(Collectors.toList());
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

    private User requireCurrentUser() {
        Long currentUserId = SecurityUtils.requireCurrentUserId();
        User user = getById(currentUserId);
        if (user == null) {
            throw new RuntimeException("当前用户不存在");
        }
        return user;
    }

    private UserProfile ensureUserProfile(Long userId) {
        UserProfile userProfile = userProfileMapper.selectByUserId(userId);
        if (userProfile != null) {
            return userProfile;
        }
        createEmptyUserProfile(userId);
        return userProfileMapper.selectByUserId(userId);
    }

    private void createEmptyUserProfile(Long userId) {
        UserProfile userProfile = new UserProfile();
        userProfile.setUserId(userId);
        userProfile.setGender(0);
        userProfile.setWalletBalance(DEFAULT_WALLET_BALANCE);
        userProfileMapper.insertUserProfile(userProfile);
    }

    private LocalDate parseBirthday(String birthday) {
        return StrUtil.isBlank(birthday) ? null : LocalDate.parse(birthday);
    }

    private UserProfileVO buildUserProfileVO(User user, boolean includeRoles) {
        UserProfileVO profileVO = includeRoles
                ? userProfileAssembler.toProfileWithRoles(user)
                : userProfileAssembler.toProfile(user);
        UserProfile userProfile = ensureUserProfile(user.getId());
        profileVO.setRealName(userProfile.getRealName());
        profileVO.setGender(userProfile.getGender());
        profileVO.setBirthday(userProfile.getBirthday() == null ? null : userProfile.getBirthday().toString());
        profileVO.setCityName(userProfile.getCityName());
        profileVO.setAreaName(userProfile.getAreaName());
        profileVO.setAddress(userProfile.getAddress());
        profileVO.setLongitude(userProfile.getLongitude());
        profileVO.setLatitude(userProfile.getLatitude());
        profileVO.setBio(userProfile.getBio());
        profileVO.setWalletBalance(userProfile.getWalletBalance());
        return profileVO;
    }
}

package com.example.admin.service.impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.admin.common.PageResult;
import com.example.admin.dto.RoleDTO;
import com.example.admin.entity.Role;
import com.example.admin.entity.UserRole;
import com.example.admin.mapper.RoleMapper;
import com.example.admin.mapper.UserRoleMapper;
import com.example.admin.service.RoleService;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

/**
 * 角色服务实现
 */
@Service
public class RoleServiceImpl implements RoleService {

    @Resource
    private RoleMapper roleMapper;

    @Resource
    private UserRoleMapper userRoleMapper;

    @Override
    public PageResult<List<Role>> page(Integer pageNum, Integer pageSize) {
        Page<Role> page = new Page<>(pageNum == null || pageNum < 1 ? 1 : pageNum,
                pageSize == null || pageSize < 1 ? 10 : pageSize);
        Page<Role> result = roleMapper.selectPage(page, null);
        return PageResult.of(result.getTotal(), result.getRecords());
    }

    @Override
    public Role getById(Long id) {
        Role role = roleMapper.selectById(id);
        if (role == null) {
            throw new RuntimeException("角色不存在");
        }
        return role;
    }

    @Override
    public boolean create(RoleDTO roleDTO) {
        checkRoleCodeUnique(roleDTO.getRoleCode(), null);
        Role role = new Role();
        BeanUtils.copyProperties(roleDTO, role);
        return roleMapper.insert(role) > 0;
    }

    @Override
    public boolean update(Long id, RoleDTO roleDTO) {
        Role role = getById(id);
        checkRoleCodeUnique(roleDTO.getRoleCode(), id);
        BeanUtils.copyProperties(roleDTO, role);
        return roleMapper.updateById(role) > 0;
    }

    @Override
    public boolean delete(Long id) {
        getById(id);
        LambdaQueryWrapper<UserRole> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserRole::getRoleId, id);
        if (userRoleMapper.selectCount(wrapper) > 0) {
            throw new RuntimeException("该角色已绑定用户，不能删除");
        }
        return roleMapper.deleteById(id) > 0;
    }

    private void checkRoleCodeUnique(String roleCode, Long excludeId) {
        if (StrUtil.isBlank(roleCode)) {
            return;
        }
        LambdaQueryWrapper<Role> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Role::getRoleCode, roleCode);
        if (excludeId != null) {
            wrapper.ne(Role::getId, excludeId);
        }
        if (roleMapper.selectCount(wrapper) > 0) {
            throw new RuntimeException("角色编码已存在");
        }
    }
}

package com.example.admin.service.impl;

import cn.hutool.core.util.StrUtil;
import com.example.admin.common.PageResult;
import com.example.admin.dto.RoleDTO;
import com.example.admin.entity.Role;
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
        int currentPage = pageNum == null || pageNum < 1 ? 1 : pageNum;
        int size = pageSize == null || pageSize < 1 ? 10 : pageSize;
        long total = roleMapper.countPage();
        List<Role> records = roleMapper.selectPage((long) (currentPage - 1) * size, size);
        return PageResult.of(total, records);
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
        return roleMapper.insertRole(role) > 0;
    }

    @Override
    public boolean update(Long id, RoleDTO roleDTO) {
        Role role = getById(id);
        checkRoleCodeUnique(roleDTO.getRoleCode(), id);
        BeanUtils.copyProperties(roleDTO, role);
        return roleMapper.updateRole(role) > 0;
    }

    @Override
    public boolean delete(Long id) {
        getById(id);
        if (userRoleMapper.countByRoleId(id) > 0) {
            throw new RuntimeException("该角色已绑定用户，不能删除");
        }
        return roleMapper.deleteById(id) > 0;
    }

    private void checkRoleCodeUnique(String roleCode, Long excludeId) {
        if (StrUtil.isBlank(roleCode)) {
            return;
        }
        if (roleMapper.countByRoleCode(roleCode, excludeId) > 0) {
            throw new RuntimeException("角色编码已存在");
        }
    }
}

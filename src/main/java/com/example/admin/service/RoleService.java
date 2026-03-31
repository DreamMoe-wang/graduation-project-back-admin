package com.example.admin.service;

import com.example.admin.common.PageResult;
import com.example.admin.dto.RoleDTO;
import com.example.admin.entity.Role;

import java.util.List;

/**
 * 角色服务
 */
public interface RoleService {

    /**
     * 角色分页
     */
    PageResult<List<Role>> page(Integer pageNum, Integer pageSize);

    /**
     * 角色详情
     */
    Role getById(Long id);

    /**
     * 新增角色
     */
    boolean create(RoleDTO roleDTO);

    /**
     * 更新角色
     */
    boolean update(Long id, RoleDTO roleDTO);

    /**
     * 删除角色
     */
    boolean delete(Long id);
}

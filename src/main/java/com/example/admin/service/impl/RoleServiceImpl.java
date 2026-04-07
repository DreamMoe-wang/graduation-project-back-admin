package com.example.admin.service.impl;

import cn.hutool.core.util.StrUtil;
import com.example.admin.common.PageResult;
import com.example.admin.dto.RoleDTO;
import com.example.admin.entity.Menu;
import com.example.admin.entity.Role;
import com.example.admin.entity.RoleMenu;
import com.example.admin.mapper.MenuMapper;
import com.example.admin.mapper.RoleMapper;
import com.example.admin.mapper.RoleMenuMapper;
import com.example.admin.mapper.UserRoleMapper;
import com.example.admin.service.RoleService;
import com.example.admin.vo.RoleDetailVO;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 角色服务实现
 */
@Service
public class RoleServiceImpl implements RoleService {

    @Resource
    private RoleMapper roleMapper;

    @Resource
    private UserRoleMapper userRoleMapper;

    @Resource
    private RoleMenuMapper roleMenuMapper;

    @Resource
    private MenuMapper menuMapper;

    @Override
    public PageResult<List<Role>> page(Integer pageNum, Integer pageSize) {
        int currentPage = pageNum == null || pageNum < 1 ? 1 : pageNum;
        int size = pageSize == null || pageSize < 1 ? 10 : pageSize;
        long total = roleMapper.countPage();
        List<Role> records = roleMapper.selectPage((long) (currentPage - 1) * size, size);
        return PageResult.of(total, records);
    }

    @Override
    public RoleDetailVO getById(Long id) {
        Role role = requireRole(id);
        List<Long> menuIds = roleMenuMapper.selectByRoleId(id).stream()
                .map(RoleMenu::getMenuId)
                .distinct()
                .collect(Collectors.toList());
        return RoleDetailVO.builder()
                .id(role.getId())
                .roleName(role.getRoleName())
                .roleCode(role.getRoleCode())
                .status(role.getStatus())
                .remark(role.getRemark())
                .menuIds(menuIds)
                .build();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean create(RoleDTO roleDTO) {
        normalizeRoleDTO(roleDTO);
        checkRoleCodeUnique(roleDTO.getRoleCode(), null);
        Role role = new Role();
        BeanUtils.copyProperties(roleDTO, role);
        boolean success = roleMapper.insertRole(role) > 0;
        if (success) {
            saveRoleMenus(role.getId(), roleDTO.getMenuIds());
        }
        return success;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean update(Long id, RoleDTO roleDTO) {
        normalizeRoleDTO(roleDTO);
        Role role = requireRole(id);
        checkRoleCodeUnique(roleDTO.getRoleCode(), id);
        BeanUtils.copyProperties(roleDTO, role);
        boolean success = roleMapper.updateRole(role) > 0;
        if (success) {
            saveRoleMenus(id, roleDTO.getMenuIds());
        }
        return success;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean delete(Long id) {
        requireRole(id);
        if (userRoleMapper.countByRoleId(id) > 0) {
            throw new RuntimeException("该角色已绑定用户，不能删除");
        }
        roleMenuMapper.deleteByRoleId(id);
        return roleMapper.deleteById(id) > 0;
    }

    private Role requireRole(Long id) {
        Role role = roleMapper.selectById(id);
        if (role == null) {
            throw new RuntimeException("角色不存在");
        }
        return role;
    }

    private void normalizeRoleDTO(RoleDTO roleDTO) {
        roleDTO.setRoleName(StrUtil.trim(roleDTO.getRoleName()));
        roleDTO.setRoleCode(StrUtil.trim(roleDTO.getRoleCode()));
        roleDTO.setRemark(StrUtil.emptyToNull(StrUtil.trim(roleDTO.getRemark())));
    }

    private void checkRoleCodeUnique(String roleCode, Long excludeId) {
        if (StrUtil.isBlank(roleCode)) {
            return;
        }
        if (roleMapper.countByRoleCode(roleCode, excludeId) > 0) {
            throw new RuntimeException("角色编码已存在");
        }
    }

    private void saveRoleMenus(Long roleId, List<Long> menuIds) {
        roleMenuMapper.deleteByRoleId(roleId);
        List<Long> normalizedMenuIds = normalizeMenuIds(menuIds);
        for (Long menuId : normalizedMenuIds) {
            RoleMenu roleMenu = new RoleMenu();
            roleMenu.setRoleId(roleId);
            roleMenu.setMenuId(menuId);
            roleMenuMapper.insertRoleMenu(roleMenu);
        }
    }

    private List<Long> normalizeMenuIds(List<Long> menuIds) {
        if (menuIds == null || menuIds.isEmpty()) {
            return Collections.emptyList();
        }
        Map<Long, Menu> menuMap = menuMapper.selectAll().stream()
                .collect(Collectors.toMap(Menu::getId, Function.identity()));
        LinkedHashSet<Long> normalized = new LinkedHashSet<>();
        for (Long menuId : menuIds) {
            Long currentId = menuId;
            while (currentId != null && currentId > 0) {
                if (!normalized.add(currentId)) {
                    break;
                }
                Menu currentMenu = menuMap.get(currentId);
                if (currentMenu == null) {
                    break;
                }
                currentId = currentMenu.getParentId();
            }
        }
        return new ArrayList<>(normalized);
    }
}

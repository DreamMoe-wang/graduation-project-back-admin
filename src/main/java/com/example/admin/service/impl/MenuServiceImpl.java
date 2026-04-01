package com.example.admin.service.impl;

import cn.hutool.core.util.StrUtil;
import com.example.admin.common.PageResult;
import com.example.admin.dto.MenuDTO;
import com.example.admin.entity.Menu;
import com.example.admin.entity.RoleMenu;
import com.example.admin.entity.UserRole;
import com.example.admin.mapper.MenuMapper;
import com.example.admin.mapper.RoleMenuMapper;
import com.example.admin.mapper.UserRoleMapper;
import com.example.admin.security.SecurityUtils;
import com.example.admin.service.MenuService;
import com.example.admin.vo.MenuVO;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 菜单服务实现
 */
@Service
public class MenuServiceImpl implements MenuService {

    @Resource
    private MenuMapper menuMapper;

    @Resource
    private RoleMenuMapper roleMenuMapper;

    @Resource
    private UserRoleMapper userRoleMapper;

    @Override
    public PageResult<List<MenuVO>> page(Integer pageNum, Integer pageSize) {
        int currentPage = pageNum == null || pageNum < 1 ? 1 : pageNum;
        int size = pageSize == null || pageSize < 1 ? 10 : pageSize;
        long total = menuMapper.countPage();
        List<MenuVO> records = menuMapper.selectPage((long) (currentPage - 1) * size, size)
                .stream()
                .map(this::toVOWithoutChildren)
                .collect(Collectors.toList());
        return PageResult.of(total, records);
    }

    @Override
    public MenuVO getById(Long id) {
        Menu menu = menuMapper.selectById(id);
        if (menu == null) {
            throw new RuntimeException("菜单不存在");
        }
        return toVOWithoutChildren(menu);
    }

    @Override
    public boolean create(MenuDTO menuDTO) {
        validateMenu(menuDTO, null);
        Menu menu = new Menu();
        BeanUtils.copyProperties(menuDTO, menu);
        fillDefault(menu);
        return menuMapper.insertMenu(menu) > 0;
    }

    @Override
    public boolean update(Long id, MenuDTO menuDTO) {
        Menu menu = menuMapper.selectById(id);
        if (menu == null) {
            throw new RuntimeException("菜单不存在");
        }
        validateMenu(menuDTO, id);
        BeanUtils.copyProperties(menuDTO, menu);
        fillDefault(menu);
        return menuMapper.updateMenu(menu) > 0;
    }

    @Override
    public boolean delete(Long id) {
        if (menuMapper.countByParentId(id) > 0) {
            throw new RuntimeException("请先删除子菜单");
        }
        if (roleMenuMapper.countByMenuId(id) > 0) {
            roleMenuMapper.deleteByMenuId(id);
        }
        return menuMapper.deleteById(id) > 0;
    }

    @Override
    public List<MenuVO> getCurrentMenuTree() {
        return getMenuTreeByUserId(SecurityUtils.requireCurrentUserId());
    }

    @Override
    public List<MenuVO> getMenuTreeByUserId(Long userId) {
        List<UserRole> userRoles = userRoleMapper.selectByUserId(userId);
        if (userRoles.isEmpty()) {
            return Collections.emptyList();
        }
        List<Long> roleIds = userRoles.stream().map(UserRole::getRoleId).collect(Collectors.toList());
        List<RoleMenu> roleMenus = roleMenuMapper.selectByRoleIds(roleIds);
        if (roleMenus.isEmpty()) {
            return Collections.emptyList();
        }
        List<Long> menuIds = roleMenus.stream()
                .map(RoleMenu::getMenuId)
                .distinct()
                .collect(Collectors.toList());
        List<Menu> menus = menuMapper.selectEnabledByIds(menuIds).stream()
                .filter(item -> item.getStatus() != null && item.getStatus() == 1)
                .filter(item -> item.getVisible() != null && item.getVisible() == 1)
                .collect(Collectors.toList());
        return buildTree(menus);
    }

    @Override
    public List<String> getCurrentPermissionCodes() {
        return getPermissionCodesByUserId(SecurityUtils.requireCurrentUserId());
    }

    @Override
    public List<String> getPermissionCodesByUserId(Long userId) {
        List<UserRole> userRoles = userRoleMapper.selectByUserId(userId);
        if (userRoles.isEmpty()) {
            return Collections.emptyList();
        }
        List<Long> roleIds = userRoles.stream().map(UserRole::getRoleId).collect(Collectors.toList());
        List<RoleMenu> roleMenus = roleMenuMapper.selectByRoleIds(roleIds);
        if (roleMenus.isEmpty()) {
            return Collections.emptyList();
        }
        List<Long> menuIds = roleMenus.stream()
                .map(RoleMenu::getMenuId)
                .distinct()
                .collect(Collectors.toList());
        return menuMapper.selectEnabledByIds(menuIds).stream()
                .map(Menu::getPermissionCode)
                .filter(StrUtil::isNotBlank)
                .distinct()
                .collect(Collectors.toList());
    }

    private void validateMenu(MenuDTO menuDTO, Long excludeId) {
        if (StrUtil.isNotBlank(menuDTO.getPath()) && menuMapper.countByPath(menuDTO.getPath(), excludeId) > 0) {
            throw new RuntimeException("菜单路径已存在");
        }
        if (StrUtil.isNotBlank(menuDTO.getPermissionCode())
                && menuMapper.countByPermissionCode(menuDTO.getPermissionCode(), excludeId) > 0) {
            throw new RuntimeException("权限标识已存在");
        }
    }

    private void fillDefault(Menu menu) {
        if (menu.getParentId() == null) {
            menu.setParentId(0L);
        }
        if (menu.getSortNo() == null) {
            menu.setSortNo(0);
        }
        if (menu.getVisible() == null) {
            menu.setVisible(1);
        }
        if (menu.getStatus() == null) {
            menu.setStatus(1);
        }
    }

    private List<MenuVO> buildTree(List<Menu> menus) {
        List<MenuVO> allNodes = menus.stream()
                .map(this::toVOWithChildren)
                .collect(Collectors.toList());
        Map<Long, MenuVO> nodeMap = allNodes.stream()
                .collect(Collectors.toMap(MenuVO::getId, Function.identity()));

        List<MenuVO> roots = new ArrayList<>();
        for (MenuVO node : allNodes) {
            if (node.getParentId() == null || node.getParentId() == 0) {
                roots.add(node);
                continue;
            }
            MenuVO parent = nodeMap.get(node.getParentId());
            if (parent != null) {
                parent.getChildren().add(node);
            } else {
                roots.add(node);
            }
        }
        sortTree(roots);
        return roots;
    }

    private void sortTree(List<MenuVO> nodes) {
        nodes.sort((left, right) -> {
            int sortCompare = Integer.compare(defaultSort(left.getSortNo()), defaultSort(right.getSortNo()));
            if (sortCompare != 0) {
                return sortCompare;
            }
            return Long.compare(left.getId(), right.getId());
        });
        for (MenuVO node : nodes) {
            if (node.getChildren() != null && !node.getChildren().isEmpty()) {
                sortTree(node.getChildren());
            }
        }
    }

    private int defaultSort(Integer sortNo) {
        return sortNo == null ? 0 : sortNo;
    }

    private MenuVO toVOWithoutChildren(Menu menu) {
        return MenuVO.builder()
                .id(menu.getId())
                .parentId(menu.getParentId())
                .name(menu.getMenuName())
                .menuType(menu.getMenuType())
                .path(menu.getPath())
                .routeName(menu.getRouteName())
                .component(menu.getComponent())
                .icon(menu.getIcon())
                .permissionCode(menu.getPermissionCode())
                .sortNo(menu.getSortNo())
                .visible(menu.getVisible())
                .status(menu.getStatus())
                .remark(menu.getRemark())
                .build();
    }

    private MenuVO toVOWithChildren(Menu menu) {
        MenuVO menuVO = toVOWithoutChildren(menu);
        menuVO.setChildren(new ArrayList<>());
        return menuVO;
    }
}

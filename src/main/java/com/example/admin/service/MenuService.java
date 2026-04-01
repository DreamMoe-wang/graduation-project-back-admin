package com.example.admin.service;

import com.example.admin.common.PageResult;
import com.example.admin.dto.MenuDTO;
import com.example.admin.vo.MenuVO;

import java.util.List;

/**
 * 菜单服务
 */
public interface MenuService {

    PageResult<List<MenuVO>> page(Integer pageNum, Integer pageSize);

    MenuVO getById(Long id);

    boolean create(MenuDTO menuDTO);

    boolean update(Long id, MenuDTO menuDTO);

    boolean delete(Long id);

    List<MenuVO> getCurrentMenuTree();

    List<MenuVO> getMenuTreeByUserId(Long userId);

    List<String> getCurrentPermissionCodes();

    List<String> getPermissionCodesByUserId(Long userId);
}

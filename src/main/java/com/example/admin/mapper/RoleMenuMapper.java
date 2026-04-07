package com.example.admin.mapper;

import com.example.admin.entity.RoleMenu;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 角色菜单关联 Mapper
 */
@Mapper
public interface RoleMenuMapper {

    List<RoleMenu> selectByRoleIds(@Param("roleIds") List<Long> roleIds);

    List<RoleMenu> selectByRoleId(@Param("roleId") Long roleId);

    long countByMenuId(@Param("menuId") Long menuId);

    long countByRoleIdAndMenuId(@Param("roleId") Long roleId, @Param("menuId") Long menuId);

    int insertRoleMenu(RoleMenu roleMenu);

    int deleteByRoleId(@Param("roleId") Long roleId);

    int deleteByMenuId(@Param("menuId") Long menuId);
}

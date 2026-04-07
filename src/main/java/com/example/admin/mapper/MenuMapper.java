package com.example.admin.mapper;

import com.example.admin.entity.Menu;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 菜单 Mapper
 */
@Mapper
public interface MenuMapper {

    Menu selectById(@Param("id") Long id);

    List<Menu> selectPage(@Param("offset") long offset, @Param("pageSize") long pageSize);

    long countPage();

    List<Menu> selectAll();

    List<Menu> selectAllEnabled();

    List<Menu> selectByIds(@Param("ids") List<Long> ids);

    List<Menu> selectEnabledByIds(@Param("ids") List<Long> ids);

    long countByPath(@Param("path") String path, @Param("excludeId") Long excludeId);

    long countByPermissionCode(@Param("permissionCode") String permissionCode, @Param("excludeId") Long excludeId);

    long countByParentId(@Param("parentId") Long parentId);

    int insertMenu(Menu menu);

    int updateMenu(Menu menu);

    int deleteById(@Param("id") Long id);
}

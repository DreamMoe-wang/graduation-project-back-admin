package com.example.admin.mapper;

import com.example.admin.entity.Role;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 角色 Mapper
 */
@Mapper
public interface RoleMapper {

    Role selectById(@Param("id") Long id);

    Role selectByRoleCode(@Param("roleCode") String roleCode);

    List<Role> selectByIds(@Param("ids") List<Long> ids);

    List<Role> selectPage(@Param("offset") long offset, @Param("pageSize") long pageSize);

    long countPage();

    long countByRoleCode(@Param("roleCode") String roleCode, @Param("excludeId") Long excludeId);

    int insertRole(Role role);

    int updateRole(Role role);

    int deleteById(@Param("id") Long id);
}

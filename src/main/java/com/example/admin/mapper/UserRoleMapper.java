package com.example.admin.mapper;

import com.example.admin.entity.UserRole;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 用户角色关联 Mapper
 */
@Mapper
public interface UserRoleMapper {

    List<UserRole> selectByUserId(@Param("userId") Long userId);

    long countByRoleId(@Param("roleId") Long roleId);

    long countByUserIdAndRoleId(@Param("userId") Long userId, @Param("roleId") Long roleId);

    int insertUserRole(UserRole userRole);

    int deleteByUserId(@Param("userId") Long userId);
}

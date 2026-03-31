package com.example.admin.mapper;

import com.example.admin.entity.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 用户 Mapper
 */
@Mapper
public interface UserMapper {

    User selectById(@Param("id") Long id);

    User selectByUsername(@Param("username") String username);

    List<User> selectPage(@Param("offset") long offset, @Param("pageSize") long pageSize);

    long countPage();

    List<User> selectAll();

    long countByUsername(@Param("username") String username, @Param("excludeId") Long excludeId);

    long countByEmail(@Param("email") String email, @Param("excludeId") Long excludeId);

    long countActiveUsers();

    int insertUser(User user);

    int updateUser(User user);

    int updateLastLoginTime(@Param("id") Long id, @Param("lastLoginTime") LocalDateTime lastLoginTime);

    int deleteById(@Param("id") Long id);
}

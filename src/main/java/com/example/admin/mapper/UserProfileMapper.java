package com.example.admin.mapper;

import com.example.admin.entity.UserProfile;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 用户扩展资料 Mapper
 */
@Mapper
public interface UserProfileMapper {

    UserProfile selectByUserId(@Param("userId") Long userId);

    int insertUserProfile(UserProfile userProfile);

    int updateUserProfile(UserProfile userProfile);
}
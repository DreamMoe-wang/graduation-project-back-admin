package com.example.admin.mapper;

import com.example.admin.entity.UserProfile;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.math.BigDecimal;

/**
 * 用户扩展资料 Mapper
 */
@Mapper
public interface UserProfileMapper {

    UserProfile selectByUserId(@Param("userId") Long userId);

    int insertUserProfile(UserProfile userProfile);

    int updateUserProfile(UserProfile userProfile);

    int increaseWalletBalance(@Param("userId") Long userId, @Param("delta") BigDecimal delta);
}

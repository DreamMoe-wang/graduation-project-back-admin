package com.example.admin.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

/**
 * 前端展示用用户信息
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserProfileVO {

    private Long id;

    private Long userId;

    private String username;

    private String nickname;

    private String displayName;

    private String avatar;

    private String phone;

    private String email;

    private String realName;

    private Integer gender;

    private String birthday;

    private String cityName;

    private String areaName;

    private String address;

    private Double longitude;

    private Double latitude;

    private String bio;

    private BigDecimal walletBalance;

    private List<Long> roleIds;

    private List<String> roles;

    private List<String> roleNames;
}

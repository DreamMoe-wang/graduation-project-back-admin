package com.example.admin.entity;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 用户扩展资料实体
 */
@Data
public class UserProfile implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;

    private Long userId;

    private String realName;

    private Integer gender;

    private LocalDate birthday;

    private String cityName;

    private String areaName;

    private String address;

    private String bio;

    private BigDecimal walletBalance;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}

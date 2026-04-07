package com.example.admin.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * 交易信息
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TradeVO {

    private Long id;

    private String title;

    private String clientName;

    private String clientPhone;

    private String workerName;

    private String workerPhone;

    private UserProfileVO publisher;

    private UserProfileVO worker;

    private BigDecimal amount;

    private String location;

    private String cityName;

    private String areaName;

    private String status;

    private String createTime;

    private String description;
}

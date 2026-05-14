package com.example.admin.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * 首页概览数据
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DashboardOverviewVO {

    private Long visitCount;

    private Long userCount;

    private Long orderCount;

    private BigDecimal salesAmount;

    private Long publishOrderCount;

    private Long receiveOrderCount;

    private BigDecimal publishAmount;

    private BigDecimal receiveAmount;
}

package com.example.admin.dto;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 交易查询参数
 */
@Data
public class TradeQueryDTO {

    /**
     * 页码
     */
    private Integer pageNum = 1;

    /**
     * 每页条数
     */
    private Integer pageSize = 10;

    /**
     * 交易标题
     */
    private String title;

    /**
     * 状态
     */
    private String status;

    /**
     * 最低金额
     */
    private BigDecimal minAmount;

    /**
     * 最高金额
     */
    private BigDecimal maxAmount;

    /**
     * 开始日期，格式：yyyy-MM-dd
     */
    private String startDate;

    /**
     * 结束日期，格式：yyyy-MM-dd
     */
    private String endDate;
}

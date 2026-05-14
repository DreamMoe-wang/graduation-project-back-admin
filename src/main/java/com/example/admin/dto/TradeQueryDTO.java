package com.example.admin.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * 交易查询参数
 */
@Data
public class TradeQueryDTO {

    private Integer pageNum = 1;

    private Integer pageSize = 10;

    private String title;

    private String status;

    private List<Integer> statusList;

    private BigDecimal minAmount;

    private BigDecimal maxAmount;

    private String startDate;

    private String endDate;

    private List<String> categoryNames;

    private String userCityName;

    private String userAreaName;

    private Double userLongitude;

    private Double userLatitude;
}

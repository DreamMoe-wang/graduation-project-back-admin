package com.example.admin.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * 订单信息
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TradeOrderVO {

    private Long id;

    private String orderNo;

    private String title;

    private String area;

    private String createTime;

    private BigDecimal price;

    private String status;

    private String statusText;
}

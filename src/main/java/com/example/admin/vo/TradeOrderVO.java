package com.example.admin.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

/**
 * 订单信息
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TradeOrderVO {

    private Long id;

    private Long postId;

    private String orderNo;

    private String title;

    private String area;

    private UserProfileVO publisher;

    private UserProfileVO receiver;

    private String createTime;

    private BigDecimal price;

    private String status;

    private String statusText;

    private String payStatus;

    private String payStatusText;

    private String payGateway;

    private String payTime;

    private List<String> imageUrls;
}

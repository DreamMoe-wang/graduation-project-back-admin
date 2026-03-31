package com.example.admin.entity;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 发布审核记录实体
 */
@Data
public class TradePostReview implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;

    private Long postId;

    private Long reviewerId;

    private Integer reviewResult;

    private String reviewRemark;

    private LocalDateTime createTime;
}

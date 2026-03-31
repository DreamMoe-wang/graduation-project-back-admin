package com.example.admin.entity;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 发布图片实体
 */
@Data
public class TradePostImage implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;

    private Long postId;

    private String imageUrl;

    private Integer sortNo;

    private LocalDateTime createTime;
}

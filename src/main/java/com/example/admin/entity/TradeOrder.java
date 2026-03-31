package com.example.admin.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 订单实体
 */
@Data
public class TradeOrder implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;

    private String orderNo;

    private Long postId;

    private Long publisherId;

    private Long receiverId;

    private BigDecimal amount;

    private Integer status;

    private String remark;

    private LocalDateTime confirmTime;

    private LocalDateTime finishTime;

    private LocalDateTime cancelTime;

    private String cancelReason;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}

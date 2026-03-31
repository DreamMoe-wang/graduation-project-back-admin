package com.example.admin.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 聊天会话实体
 */
@Data
public class ChatSession implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;

    private Integer sessionType;

    private Long postId;

    private Long orderId;

    private Long lastMessageId;

    private LocalDateTime lastMessageTime;

    private Integer status;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}

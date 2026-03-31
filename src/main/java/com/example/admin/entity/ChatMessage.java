package com.example.admin.entity;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 聊天消息实体
 */
@Data
public class ChatMessage implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;

    private Long sessionId;

    private Long senderId;

    private Integer messageType;

    private String content;

    private String extraJson;

    private Integer isRecall;

    private LocalDateTime createTime;
}

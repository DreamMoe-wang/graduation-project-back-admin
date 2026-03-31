package com.example.admin.entity;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 会话成员实体
 */
@Data
public class ChatSessionUser implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;

    private Long sessionId;

    private Long userId;

    private Long lastReadMessageId;

    private LocalDateTime lastReadTime;

    private Integer unreadCount;

    private LocalDateTime createTime;
}

package com.example.admin.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 聊天会话
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatSessionVO {

    private Long id;

    private Long tradeId;

    private Long orderId;

    private String tradeTitle;

    private String name;

    private String avatar;

    private String lastMessage;

    private String time;

    private Integer unread;
}

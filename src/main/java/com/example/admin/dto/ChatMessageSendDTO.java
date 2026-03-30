package com.example.admin.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;

/**
 * 发送聊天消息参数
 */
@Data
public class ChatMessageSendDTO {

    /**
     * 消息内容
     */
    @NotBlank(message = "消息内容不能为空")
    private String content;
}

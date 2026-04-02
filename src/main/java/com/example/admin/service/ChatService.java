package com.example.admin.service;

import com.example.admin.dto.ChatMessageSendDTO;
import com.example.admin.vo.ChatMessageVO;
import com.example.admin.vo.ChatSessionVO;

import java.util.List;

/**
 * 聊天模块服务
 */
public interface ChatService {

    /**
     * 获取会话列表
     */
    List<ChatSessionVO> getSessions(String keyword);

    /**
     * 根据交易打开或创建私聊会话
     */
    ChatSessionVO openTradeSession(Long tradeId);

    /**
     * 获取会话消息
     */
    List<ChatMessageVO> getMessages(Long sessionId);

    /**
     * 发送消息
     */
    boolean sendMessage(Long sessionId, ChatMessageSendDTO sendDTO);

    /**
     * 标记已读
     */
    boolean markSessionRead(Long sessionId);
}
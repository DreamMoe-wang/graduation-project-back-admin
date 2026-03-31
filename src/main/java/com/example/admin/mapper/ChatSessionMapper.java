package com.example.admin.mapper;

import com.example.admin.entity.ChatSession;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 聊天会话 Mapper
 */
@Mapper
public interface ChatSessionMapper {

    ChatSession selectById(@Param("id") Long id);

    int updateChatSession(ChatSession chatSession);
}

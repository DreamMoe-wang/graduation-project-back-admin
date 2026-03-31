package com.example.admin.mapper;

import com.example.admin.entity.ChatMessage;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 聊天消息 Mapper
 */
@Mapper
public interface ChatMessageMapper {

    ChatMessage selectById(@Param("id") Long id);

    ChatMessage selectLatestBySessionId(@Param("sessionId") Long sessionId);

    List<ChatMessage> selectBySessionId(@Param("sessionId") Long sessionId);

    int insertChatMessage(ChatMessage chatMessage);
}

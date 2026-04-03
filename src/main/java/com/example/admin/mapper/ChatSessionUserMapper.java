package com.example.admin.mapper;

import com.example.admin.entity.ChatSessionUser;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 会话成员 Mapper
 */
@Mapper
public interface ChatSessionUserMapper {

    List<ChatSessionUser> selectByUserId(@Param("userId") Long userId);

    List<ChatSessionUser> selectBySessionId(@Param("sessionId") Long sessionId);

    ChatSessionUser selectBySessionIdAndUserId(@Param("sessionId") Long sessionId, @Param("userId") Long userId);

    ChatSessionUser selectOtherMember(@Param("sessionId") Long sessionId, @Param("userId") Long userId);

    int insertChatSessionUser(ChatSessionUser chatSessionUser);

    int updateChatSessionUser(ChatSessionUser chatSessionUser);

    int deleteBySessionIdAndUserId(@Param("sessionId") Long sessionId, @Param("userId") Long userId);

    long countBySessionId(@Param("sessionId") Long sessionId);
}

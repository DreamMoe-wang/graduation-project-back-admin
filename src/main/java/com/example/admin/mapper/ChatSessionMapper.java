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

    ChatSession selectPrivateSessionByPostAndUsers(@Param("postId") Long postId,
                                                   @Param("userIdA") Long userIdA,
                                                   @Param("userIdB") Long userIdB);

    ChatSession selectByOrderId(@Param("orderId") Long orderId);

    int insertChatSession(ChatSession chatSession);

    int updateChatSession(ChatSession chatSession);
}

package com.example.admin.service.impl;

import cn.hutool.core.util.StrUtil;
import com.example.admin.dto.ChatMessageSendDTO;
import com.example.admin.entity.ChatMessage;
import com.example.admin.entity.ChatSession;
import com.example.admin.entity.ChatSessionUser;
import com.example.admin.entity.User;
import com.example.admin.mapper.ChatMessageMapper;
import com.example.admin.entity.TradePost;
import com.example.admin.mapper.ChatSessionMapper;
import com.example.admin.mapper.ChatSessionUserMapper;
import com.example.admin.mapper.TradePostMapper;
import com.example.admin.mapper.UserMapper;
import com.example.admin.security.SecurityUtils;
import com.example.admin.service.ChatService;
import com.example.admin.vo.ChatMessageVO;
import com.example.admin.vo.ChatSessionVO;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 聊天模块服务实现
 */
@Service
public class ChatServiceImpl implements ChatService {

    private static final DateTimeFormatter SESSION_TIME_FORMATTER = DateTimeFormatter.ofPattern("MM-dd HH:mm");
    private static final DateTimeFormatter MESSAGE_TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");

    @Resource
    private ChatSessionMapper chatSessionMapper;

    @Resource
    private ChatSessionUserMapper chatSessionUserMapper;

    @Resource
    private ChatMessageMapper chatMessageMapper;

    @Resource
    private UserMapper userMapper;

    @Resource
    private TradePostMapper tradePostMapper;

    @Override
    public List<ChatSessionVO> getSessions(String keyword) {
        Long currentUserId = currentUserId();
        return chatSessionUserMapper.selectByUserId(currentUserId).stream()
                .map(item -> buildSessionVO(item.getSessionId(), currentUserId, item.getUnreadCount()))
                .filter(Objects::nonNull)
                .filter(item -> StrUtil.isBlank(keyword)
                        || StrUtil.containsIgnoreCase(item.getName(), keyword)
                        || StrUtil.containsIgnoreCase(item.getLastMessage(), keyword))
                .sorted(Comparator.comparing(ChatSessionVO::getTime, Comparator.nullsLast(String::compareTo)).reversed())
                .collect(Collectors.toList());
    }

    @Override
    public ChatSessionVO openTradeSession(Long tradeId) {
        Long currentUserId = currentUserId();
        TradePost tradePost = tradePostMapper.selectById(tradeId);
        if (tradePost == null) {
            throw new RuntimeException("交易不存在");
        }
        if (currentUserId.equals(tradePost.getPublisherId())) {
            throw new RuntimeException("不能和自己发起私聊");
        }

        ChatSession session = chatSessionMapper.selectPrivateSessionByPostAndUsers(tradeId, currentUserId, tradePost.getPublisherId());
        if (session == null) {
            session = new ChatSession();
            session.setSessionType(1);
            session.setPostId(tradeId);
            session.setStatus(1);
            chatSessionMapper.insertChatSession(session);

            ChatSessionUser currentMember = new ChatSessionUser();
            currentMember.setSessionId(session.getId());
            currentMember.setUserId(currentUserId);
            currentMember.setUnreadCount(0);
            chatSessionUserMapper.insertChatSessionUser(currentMember);

            ChatSessionUser publisherMember = new ChatSessionUser();
            publisherMember.setSessionId(session.getId());
            publisherMember.setUserId(tradePost.getPublisherId());
            publisherMember.setUnreadCount(0);
            chatSessionUserMapper.insertChatSessionUser(publisherMember);
        }

        return buildSessionVO(session.getId(), currentUserId, 0);
    }

    @Override
    public List<ChatMessageVO> getMessages(Long sessionId) {
        Long currentUserId = currentUserId();
        ensureSessionAccessible(sessionId, currentUserId);
        return chatMessageMapper.selectBySessionId(sessionId).stream()
                .map(item -> toChatMessageVO(item, currentUserId))
                .collect(Collectors.toList());
    }

    @Override
    public boolean sendMessage(Long sessionId, ChatMessageSendDTO sendDTO) {
        Long currentUserId = currentUserId();
        ensureSessionAccessible(sessionId, currentUserId);

        ChatMessage message = new ChatMessage();
        message.setSessionId(sessionId);
        message.setSenderId(currentUserId);
        message.setMessageType(1);
        message.setContent(sendDTO.getContent().trim());
        message.setIsRecall(0);

        int inserted = chatMessageMapper.insertChatMessage(message);
        if (inserted > 0) {
            ChatSession session = chatSessionMapper.selectById(sessionId);
            session.setLastMessageId(message.getId());
            session.setLastMessageTime(LocalDateTime.now());
            chatSessionMapper.updateChatSession(session);

            List<ChatSessionUser> members = chatSessionUserMapper.selectBySessionId(sessionId);
            for (ChatSessionUser member : members) {
                if (currentUserId.equals(member.getUserId())) {
                    member.setLastReadMessageId(message.getId());
                    member.setLastReadTime(LocalDateTime.now());
                    member.setUnreadCount(0);
                } else {
                    member.setUnreadCount(member.getUnreadCount() == null ? 1 : member.getUnreadCount() + 1);
                }
                chatSessionUserMapper.updateChatSessionUser(member);
            }
        }
        return inserted > 0;
    }

    @Override
    public boolean markSessionRead(Long sessionId) {
        Long currentUserId = currentUserId();
        ChatSessionUser sessionUser = ensureSessionAccessible(sessionId, currentUserId);
        ChatMessage latestMessage = chatMessageMapper.selectLatestBySessionId(sessionId);
        sessionUser.setLastReadTime(LocalDateTime.now());
        sessionUser.setUnreadCount(0);
        if (latestMessage != null) {
            sessionUser.setLastReadMessageId(latestMessage.getId());
        }
        return chatSessionUserMapper.updateChatSessionUser(sessionUser) > 0;
    }

    private ChatSessionVO buildSessionVO(Long sessionId, Long currentUserId, Integer unreadCount) {
        ChatSession session = chatSessionMapper.selectById(sessionId);
        if (session == null) {
            return null;
        }
        ChatSessionUser otherMember = chatSessionUserMapper.selectOtherMember(sessionId, currentUserId);
        User otherUser = otherMember == null ? null : userMapper.selectById(otherMember.getUserId());
        ChatMessage lastMessage = session.getLastMessageId() == null ? null : chatMessageMapper.selectById(session.getLastMessageId());

        return ChatSessionVO.builder()
                .id(session.getId())
                .name(otherUser == null ? "未知用户" : buildDisplayName(otherUser))
                .avatar(otherUser == null ? null : otherUser.getAvatar())
                .lastMessage(lastMessage == null ? "" : lastMessage.getContent())
                .time(session.getLastMessageTime() == null ? null : session.getLastMessageTime().format(SESSION_TIME_FORMATTER))
                .unread(unreadCount == null ? 0 : unreadCount)
                .build();
    }

    private ChatMessageVO toChatMessageVO(ChatMessage message, Long currentUserId) {
        return ChatMessageVO.builder()
                .id(message.getId())
                .type(currentUserId.equals(message.getSenderId()) ? "sent" : "received")
                .content(message.getContent())
                .time(message.getCreateTime() == null ? null : message.getCreateTime().format(MESSAGE_TIME_FORMATTER))
                .build();
    }

    private ChatSessionUser ensureSessionAccessible(Long sessionId, Long currentUserId) {
        if (sessionId == null || sessionId <= 0) {
            throw new RuntimeException("会话不存在");
        }
        ChatSessionUser sessionUser = chatSessionUserMapper.selectBySessionIdAndUserId(sessionId, currentUserId);
        if (sessionUser == null) {
            throw new RuntimeException("当前用户无权访问该会话");
        }
        return sessionUser;
    }

    private String buildDisplayName(User user) {
        return user == null ? "未知用户" : StrUtil.blankToDefault(user.getNickname(), user.getUsername());
    }

    private Long currentUserId() {
        return SecurityUtils.requireCurrentUserId();
    }
}

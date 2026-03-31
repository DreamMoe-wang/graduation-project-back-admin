package com.example.admin.service.impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.admin.dto.ChatMessageSendDTO;
import com.example.admin.entity.ChatMessage;
import com.example.admin.entity.ChatSession;
import com.example.admin.entity.ChatSessionUser;
import com.example.admin.entity.User;
import com.example.admin.mapper.ChatMessageMapper;
import com.example.admin.mapper.ChatSessionMapper;
import com.example.admin.mapper.ChatSessionUserMapper;
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

    @Override
    public List<ChatSessionVO> getSessions(String keyword) {
        Long currentUserId = currentUserId();

        LambdaQueryWrapper<ChatSessionUser> memberWrapper = new LambdaQueryWrapper<>();
        memberWrapper.eq(ChatSessionUser::getUserId, currentUserId);
        List<ChatSessionUser> sessionUsers = chatSessionUserMapper.selectList(memberWrapper);

        return sessionUsers.stream()
                .map(item -> buildSessionVO(item.getSessionId(), currentUserId, item.getUnreadCount()))
                .filter(Objects::nonNull)
                .filter(item -> StrUtil.isBlank(keyword)
                        || StrUtil.containsIgnoreCase(item.getName(), keyword)
                        || StrUtil.containsIgnoreCase(item.getLastMessage(), keyword))
                .sorted(Comparator.comparing(ChatSessionVO::getTime, Comparator.nullsLast(String::compareTo)).reversed())
                .collect(Collectors.toList());
    }

    @Override
    public List<ChatMessageVO> getMessages(Long sessionId) {
        Long currentUserId = currentUserId();
        ensureSessionAccessible(sessionId, currentUserId);

        LambdaQueryWrapper<ChatMessage> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ChatMessage::getSessionId, sessionId)
                .orderByAsc(ChatMessage::getCreateTime);
        return chatMessageMapper.selectList(wrapper).stream()
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
        int inserted = chatMessageMapper.insert(message);

        if (inserted > 0) {
            ChatSession session = chatSessionMapper.selectById(sessionId);
            session.setLastMessageId(message.getId());
            session.setLastMessageTime(LocalDateTime.now());
            chatSessionMapper.updateById(session);

            LambdaQueryWrapper<ChatSessionUser> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(ChatSessionUser::getSessionId, sessionId);
            List<ChatSessionUser> members = chatSessionUserMapper.selectList(wrapper);
            for (ChatSessionUser member : members) {
                if (currentUserId.equals(member.getUserId())) {
                    member.setLastReadMessageId(message.getId());
                    member.setLastReadTime(LocalDateTime.now());
                    member.setUnreadCount(0);
                } else {
                    member.setUnreadCount(member.getUnreadCount() == null ? 1 : member.getUnreadCount() + 1);
                }
                chatSessionUserMapper.updateById(member);
            }
        }
        return inserted > 0;
    }

    @Override
    public boolean markSessionRead(Long sessionId) {
        Long currentUserId = currentUserId();
        ChatSessionUser sessionUser = ensureSessionAccessible(sessionId, currentUserId);

        LambdaQueryWrapper<ChatMessage> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ChatMessage::getSessionId, sessionId)
                .orderByDesc(ChatMessage::getId)
                .last("limit 1");
        ChatMessage latestMessage = chatMessageMapper.selectOne(wrapper);

        sessionUser.setLastReadTime(LocalDateTime.now());
        sessionUser.setUnreadCount(0);
        if (latestMessage != null) {
            sessionUser.setLastReadMessageId(latestMessage.getId());
        }
        return chatSessionUserMapper.updateById(sessionUser) > 0;
    }

    private ChatSessionVO buildSessionVO(Long sessionId, Long currentUserId, Integer unreadCount) {
        ChatSession session = chatSessionMapper.selectById(sessionId);
        if (session == null) {
            return null;
        }

        LambdaQueryWrapper<ChatSessionUser> memberWrapper = new LambdaQueryWrapper<>();
        memberWrapper.eq(ChatSessionUser::getSessionId, sessionId)
                .ne(ChatSessionUser::getUserId, currentUserId)
                .last("limit 1");
        ChatSessionUser otherMember = chatSessionUserMapper.selectOne(memberWrapper);
        User otherUser = otherMember == null ? null : userMapper.selectById(otherMember.getUserId());

        ChatMessage lastMessage = session.getLastMessageId() == null ? null : chatMessageMapper.selectById(session.getLastMessageId());
        String displayName = otherUser == null ? "未知用户" : buildDisplayName(otherUser);
        String time = session.getLastMessageTime() == null ? null : session.getLastMessageTime().format(SESSION_TIME_FORMATTER);

        return ChatSessionVO.builder()
                .id(session.getId())
                .name(displayName)
                .avatar(otherUser == null ? null : otherUser.getAvatar())
                .lastMessage(lastMessage == null ? "" : lastMessage.getContent())
                .time(time)
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

        LambdaQueryWrapper<ChatSessionUser> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ChatSessionUser::getSessionId, sessionId)
                .eq(ChatSessionUser::getUserId, currentUserId)
                .last("limit 1");
        ChatSessionUser sessionUser = chatSessionUserMapper.selectOne(wrapper);
        if (sessionUser == null) {
            throw new RuntimeException("当前用户无权访问该会话");
        }
        return sessionUser;
    }

    private String buildDisplayName(User user) {
        if (user == null) {
            return "未知用户";
        }
        if (StrUtil.isNotBlank(user.getNickname())) {
            return user.getNickname();
        }
        return StrUtil.blankToDefault(user.getUsername(), "用户");
    }

    private Long currentUserId() {
        return SecurityUtils.requireCurrentUserId();
    }
}

package com.example.admin.service.impl;

import cn.hutool.core.util.StrUtil;
import com.example.admin.dto.ChatMessageSendDTO;
import com.example.admin.entity.ChatMessage;
import com.example.admin.entity.ChatSession;
import com.example.admin.entity.ChatSessionUser;
import com.example.admin.entity.TradeOrder;
import com.example.admin.entity.User;
import com.example.admin.mapper.ChatMessageMapper;
import com.example.admin.entity.TradePost;
import com.example.admin.mapper.ChatSessionMapper;
import com.example.admin.mapper.ChatSessionUserMapper;
import com.example.admin.mapper.TradeOrderMapper;
import com.example.admin.mapper.TradePostMapper;
import com.example.admin.mapper.UserMapper;
import com.example.admin.security.SecurityUtils;
import com.example.admin.service.ChatService;
import com.example.admin.vo.ChatMessageVO;
import com.example.admin.vo.ChatSessionVO;
import com.example.admin.websocket.ChatRealtimeNotifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
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

    @Resource
    private TradeOrderMapper tradeOrderMapper;

    @Resource
    private ChatRealtimeNotifier chatRealtimeNotifier;

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
    @Transactional(rollbackFor = Exception.class)
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
    @Transactional(rollbackFor = Exception.class)
    public ChatSessionVO openOrderSession(Long orderId) {
        Long currentUserId = currentUserId();
        TradeOrder order = tradeOrderMapper.selectById(orderId);
        if (order == null) {
            throw new RuntimeException("订单不存在");
        }
        if (order.getPublisherId() == null || order.getPublisherId() <= 0) {
            throw new RuntimeException("订单发布方信息缺失");
        }
        if (order.getReceiverId() == null || order.getReceiverId() <= 0) {
            throw new RuntimeException("当前订单还没有接单方，无法创建会话");
        }
        if (!currentUserId.equals(order.getPublisherId()) && !currentUserId.equals(order.getReceiverId())) {
            throw new RuntimeException("当前用户无权发起订单会话");
        }

        Set<Long> expectedUserIds = new LinkedHashSet<>();
        expectedUserIds.add(order.getPublisherId());
        expectedUserIds.add(order.getReceiverId());

        ChatSession session = chatSessionMapper.selectByOrderId(orderId);
        if (session != null) {
            List<ChatSessionUser> existingMembers = chatSessionUserMapper.selectBySessionId(session.getId());
            boolean hasUnexpectedMember = existingMembers.stream()
                    .map(ChatSessionUser::getUserId)
                    .filter(Objects::nonNull)
                    .anyMatch(userId -> !expectedUserIds.contains(userId));
            if (hasUnexpectedMember) {
                for (ChatSessionUser member : existingMembers) {
                    chatSessionUserMapper.deleteBySessionIdAndUserId(session.getId(), member.getUserId());
                }
                session.setOrderId(null);
                session.setStatus(0);
                chatSessionMapper.updateChatSession(session);
                session = null;
            }
        }

        if (session == null) {
            session = new ChatSession();
            session.setSessionType(2);
            session.setPostId(order.getPostId());
            session.setOrderId(orderId);
            session.setStatus(1);
            chatSessionMapper.insertChatSession(session);
        } else {
            boolean changed = false;
            if (!Objects.equals(session.getSessionType(), 2)) {
                session.setSessionType(2);
                changed = true;
            }
            if (!Objects.equals(session.getPostId(), order.getPostId())) {
                session.setPostId(order.getPostId());
                changed = true;
            }
            if (session.getStatus() == null || session.getStatus() != 1) {
                session.setStatus(1);
                changed = true;
            }
            if (changed) {
                chatSessionMapper.updateChatSession(session);
            }
        }

        syncSessionMembers(session, order.getPublisherId(), order.getReceiverId());
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
        return sendMessageByUser(sessionId, currentUserId, sendDTO);
    }

    @Override
    public boolean sendMessageByUser(Long sessionId, Long senderUserId, ChatMessageSendDTO sendDTO) {
        if (senderUserId == null || senderUserId <= 0) {
            throw new RuntimeException("鏈幏鍙栧埌褰撳墠鐢ㄦ埛");
        }
        String content = sendDTO == null ? null : StrUtil.trim(sendDTO.getContent());
        if (StrUtil.isBlank(content)) {
            throw new RuntimeException("娑堟伅鍐呭涓嶈兘涓虹┖");
        }
        ensureSessionAccessible(sessionId, senderUserId);

        ChatMessage message = new ChatMessage();
        message.setSessionId(sessionId);
        message.setSenderId(senderUserId);
        message.setMessageType(1);
        message.setContent(content);
        message.setIsRecall(0);

        int inserted = chatMessageMapper.insertChatMessage(message);
        if (inserted > 0) {
            LocalDateTime now = LocalDateTime.now();
            ChatSession session = chatSessionMapper.selectById(sessionId);
            session.setLastMessageId(message.getId());
            session.setLastMessageTime(now);
            chatSessionMapper.updateChatSession(session);

            List<ChatSessionUser> members = chatSessionUserMapper.selectBySessionId(sessionId);
            for (ChatSessionUser member : members) {
                if (senderUserId.equals(member.getUserId())) {
                    member.setLastReadMessageId(message.getId());
                    member.setLastReadTime(now);
                    member.setUnreadCount(0);
                } else {
                    member.setUnreadCount(member.getUnreadCount() == null ? 1 : member.getUnreadCount() + 1);
                }
                chatSessionUserMapper.updateChatSessionUser(member);
            }

            List<Long> receiverIds = members.stream()
                    .map(ChatSessionUser::getUserId)
                    .filter(Objects::nonNull)
                    .distinct()
                    .collect(Collectors.toList());
            chatRealtimeNotifier.notifySessionUpdated(sessionId, senderUserId, receiverIds);
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

    @Override
    public boolean deleteSession(Long sessionId) {
        Long currentUserId = currentUserId();
        ensureSessionAccessible(sessionId, currentUserId);

        int deleted = chatSessionUserMapper.deleteBySessionIdAndUserId(sessionId, currentUserId);
        if (deleted <= 0) {
            return false;
        }

        long remainCount = chatSessionUserMapper.countBySessionId(sessionId);
        if (remainCount <= 0) {
            ChatSession session = chatSessionMapper.selectById(sessionId);
            if (session != null && (session.getStatus() == null || session.getStatus() != 0)) {
                session.setStatus(0);
                chatSessionMapper.updateChatSession(session);
            }
        }
        return true;
    }

    private ChatSessionVO buildSessionVO(Long sessionId, Long currentUserId, Integer unreadCount) {
        ChatSession session = chatSessionMapper.selectById(sessionId);
        if (session == null || session.getStatus() == null || session.getStatus() != 1) {
            return null;
        }
        TradePost tradePost = session.getPostId() == null ? null : tradePostMapper.selectById(session.getPostId());
        ChatSessionUser otherMember = chatSessionUserMapper.selectOtherMember(sessionId, currentUserId);
        User otherUser = otherMember == null ? null : userMapper.selectById(otherMember.getUserId());
        ChatMessage lastMessage = session.getLastMessageId() == null ? null : chatMessageMapper.selectById(session.getLastMessageId());

        return ChatSessionVO.builder()
                .id(session.getId())
                .tradeId(session.getPostId())
                .orderId(session.getOrderId())
                .tradeTitle(tradePost == null ? null : tradePost.getTitle())
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

    private void syncSessionMembers(ChatSession session, Long... userIds) {
        if (session == null || session.getId() == null) {
            throw new RuntimeException("会话不存在");
        }

        Set<Long> expectedUserIds = new LinkedHashSet<>();
        if (userIds != null) {
            for (Long userId : userIds) {
                if (userId != null && userId > 0) {
                    expectedUserIds.add(userId);
                }
            }
        }

        List<ChatSessionUser> members = chatSessionUserMapper.selectBySessionId(session.getId());
        for (ChatSessionUser member : members) {
            if (member.getUserId() == null || !expectedUserIds.contains(member.getUserId())) {
                chatSessionUserMapper.deleteBySessionIdAndUserId(session.getId(), member.getUserId());
            }
        }

        ChatMessage latestMessage = session.getLastMessageId() == null ? null : chatMessageMapper.selectById(session.getLastMessageId());
        for (Long userId : expectedUserIds) {
            ChatSessionUser sessionUser = chatSessionUserMapper.selectBySessionIdAndUserId(session.getId(), userId);
            if (sessionUser != null) {
                continue;
            }

            ChatSessionUser member = new ChatSessionUser();
            member.setSessionId(session.getId());
            member.setUserId(userId);
            member.setLastReadMessageId(latestMessage == null ? null : latestMessage.getId());
            member.setLastReadTime(session.getLastMessageTime());
            member.setUnreadCount(0);
            chatSessionUserMapper.insertChatSessionUser(member);
        }
    }

    private String buildDisplayName(User user) {
        return user == null ? "未知用户" : StrUtil.blankToDefault(user.getNickname(), user.getUsername());
    }

    private Long currentUserId() {
        return SecurityUtils.requireCurrentUserId();
    }
}

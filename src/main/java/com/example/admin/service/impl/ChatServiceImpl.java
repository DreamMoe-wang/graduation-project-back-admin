package com.example.admin.service.impl;

import cn.hutool.core.util.StrUtil;
import com.example.admin.dto.ChatMessageSendDTO;
import com.example.admin.service.ChatService;
import com.example.admin.vo.ChatMessageVO;
import com.example.admin.vo.ChatSessionVO;
import org.springframework.stereotype.Service;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 聊天模块服务实现
 *
 * <p>当前阶段先返回与前端页面兼容的示例数据，数据库表完成后再接入持久化。</p>
 */
@Service
public class ChatServiceImpl implements ChatService {

    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");

    @Override
    public List<ChatSessionVO> getSessions(String keyword) {
        return buildSessions().stream()
                .filter(item -> StrUtil.isBlank(keyword)
                        || StrUtil.contains(item.getName(), keyword)
                        || StrUtil.contains(item.getLastMessage(), keyword))
                .collect(Collectors.toList());
    }

    @Override
    public List<ChatMessageVO> getMessages(Long sessionId) {
        if (sessionId == null || sessionId < 1) {
            throw new RuntimeException("会话不存在");
        }
        return Arrays.asList(
                ChatMessageVO.builder().id(1L).type("received").content("你好，请问这个任务还可以接吗？").time("10:25").build(),
                ChatMessageVO.builder().id(2L).type("sent").content("可以的，你什么时候方便？").time("10:26").build(),
                ChatMessageVO.builder().id(3L).type("received").content("我下午 2 点以后都可以").time("10:28").build(),
                ChatMessageVO.builder().id(4L).type("sent").content("好的，那就下午 2 点见").time("10:29").build(),
                ChatMessageVO.builder().id(5L).type("received").content("好的，我一会儿就到").time("10:30").build()
        );
    }

    @Override
    public boolean sendMessage(Long sessionId, ChatMessageSendDTO sendDTO) {
        if (sessionId == null || sessionId < 1) {
            throw new RuntimeException("会话不存在");
        }
        return StrUtil.isNotBlank(sendDTO.getContent());
    }

    @Override
    public boolean markSessionRead(Long sessionId) {
        if (sessionId == null || sessionId < 1) {
            throw new RuntimeException("会话不存在");
        }
        return true;
    }

    private List<ChatSessionVO> buildSessions() {
        String currentTime = LocalTime.now().format(TIME_FORMATTER);
        return Arrays.asList(
                ChatSessionVO.builder()
                        .id(1L)
                        .name("张三")
                        .avatar("")
                        .lastMessage("好的，我一会儿就到")
                        .time(currentTime)
                        .unread(2)
                        .build(),
                ChatSessionVO.builder()
                        .id(2L)
                        .name("李四")
                        .avatar("")
                        .lastMessage("这个任务我可以接")
                        .time("09:15")
                        .unread(0)
                        .build(),
                ChatSessionVO.builder()
                        .id(3L)
                        .name("王五")
                        .avatar("")
                        .lastMessage("谢谢，已经解决了")
                        .time("昨天")
                        .unread(0)
                        .build()
        );
    }
}

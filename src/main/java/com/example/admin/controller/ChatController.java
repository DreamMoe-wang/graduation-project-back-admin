package com.example.admin.controller;

import com.example.admin.common.Result;
import com.example.admin.dto.ChatMessageSendDTO;
import com.example.admin.service.ChatService;
import com.example.admin.vo.ChatMessageVO;
import com.example.admin.vo.ChatSessionVO;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.List;

/**
 * 聊天控制器
 */
@RestController
@RequestMapping("/chat")
public class ChatController {

    @Resource
    private ChatService chatService;

    /**
     * 获取会话列表
     */
    @GetMapping("/sessions")
    public Result<List<ChatSessionVO>> sessions(@RequestParam(required = false) String keyword) {
        return Result.success(chatService.getSessions(keyword));
    }

    /**
     * 根据交易打开私聊会话
     */
    @PostMapping("/trade/{tradeId}/session")
    public Result<ChatSessionVO> openTradeSession(@PathVariable Long tradeId) {
        return Result.success(chatService.openTradeSession(tradeId));
    }

    /**
     * 获取会话消息
     */
    @GetMapping("/sessions/{sessionId}/messages")
    public Result<List<ChatMessageVO>> messages(@PathVariable Long sessionId) {
        return Result.success(chatService.getMessages(sessionId));
    }

    /**
     * 发送消息
     */
    @PostMapping("/sessions/{sessionId}/messages")
    public Result<Boolean> sendMessage(@PathVariable Long sessionId,
                                       @RequestBody @Validated ChatMessageSendDTO sendDTO) {
        boolean success = chatService.sendMessage(sessionId, sendDTO);
        return success ? Result.success("发送成功", true) : Result.error("发送失败");
    }

    /**
     * 标记已读
     */
    @PostMapping("/sessions/{sessionId}/read")
    public Result<Boolean> read(@PathVariable Long sessionId) {
        boolean success = chatService.markSessionRead(sessionId);
        return success ? Result.success("操作成功", true) : Result.error("操作失败");
    }
}
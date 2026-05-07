package com.chat.chat_backend.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.chat.chat_backend.common.result.Result;
import com.chat.chat_backend.module.dto.response.MessageVO;
import com.chat.chat_backend.module.dto.response.UnreadCountVO;
import com.chat.chat_backend.service.MessageService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.PrintWriter;
import java.util.List;

@RestController
@RequestMapping("/message")
@RequiredArgsConstructor
public class MessageController {

    private final MessageService messageService;

    /**
     * 获取聊天记录
     */
    @GetMapping("/history/{friendId}")
    public Result<Page<MessageVO>> getChatHistory(HttpServletRequest request,
                                                  @PathVariable Long friendId,
                                                  @RequestParam(defaultValue = "1") Integer page,
                                                  @RequestParam(defaultValue = "20") Integer size) {
        Long userId = (Long) request.getAttribute("userId");
        Page<MessageVO> result = messageService.getChatHistory(userId, friendId, page, size);
        return Result.success(result);
    }

    /**
     * 下载聊天记录
     */
    @GetMapping("/download/{friendId}")
    public void downloadChatHistory(HttpServletRequest request,
                                    HttpServletResponse response,
                                    @PathVariable Long friendId) throws Exception {
        Long userId = (Long) request.getAttribute("userId");
        List<MessageVO> messages = messageService.downloadChatHistory(userId, friendId);

        response.setContentType("text/plain;charset=UTF-8");
        response.setHeader("Content-Disposition", "attachment;filename=chat_history_" + friendId + ".txt");

        StringBuilder sb = new StringBuilder();
        for (MessageVO msg : messages) {
            sb.append(msg.getSendTime())
                    .append(" ")
                    .append(msg.getFromUserNickname())
                    .append(": ")
                    .append(msg.getContent())
                    .append("\n");
        }

        PrintWriter writer = response.getWriter();
        writer.write(sb.toString());
        writer.flush();
    }

    /**
     * 标记消息已读
     */
    @PutMapping("/read/{friendId}")
    public Result<Void> markAsRead(HttpServletRequest request, @PathVariable Long friendId) {
        Long userId = (Long) request.getAttribute("userId");
        messageService.markAsRead(userId, friendId);
        return Result.success("已标记已读", null);
    }

    /**
     * 获取未读消息统计
     */
    @GetMapping("/unread/count")
    public Result<UnreadCountVO> getUnreadCount(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        UnreadCountVO result = messageService.getUnreadCount(userId);
        return Result.success(result);
    }

    /**
     * 撤回消息
     */
    @PutMapping("/recall/{messageId}")
    public Result<Void> recallMessage(HttpServletRequest request, @PathVariable Long messageId) {
        Long userId = (Long) request.getAttribute("userId");
        messageService.recallMessage(userId, messageId);
        return Result.success("撤回成功", null);
    }
}
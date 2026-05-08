package com.chat.chat_backend.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.chat.chat_backend.common.constant.MessageConstants;
import com.chat.chat_backend.common.result.Result;
import com.chat.chat_backend.common.utils.OssUtil;
import com.chat.chat_backend.mapper.UserMapper;
import com.chat.chat_backend.module.dto.response.MessageVO;
import com.chat.chat_backend.module.dto.response.UnreadCountVO;
import com.chat.chat_backend.module.entity.User;
import com.chat.chat_backend.service.MessageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.net.URLEncoder;
import java.util.Date;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/message")
@RequiredArgsConstructor
public class MessageController {

    private final MessageService messageService;
    private final UserMapper userMapper;
    private final OssUtil ossUtil;

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
                                    @PathVariable Long friendId,
                                    @RequestParam(value = "limit", defaultValue = "100") Integer limit) throws Exception {
        Long userId = (Long) request.getAttribute("userId");

        log.info("下载聊天记录: userId={}, friendId={}, limit={}", userId, friendId, limit);

        if (limit > MessageConstants.MAX_DOWNLOAD_SIZE) {
            limit = MessageConstants.MAX_DOWNLOAD_SIZE;
        }
        if (limit <= 0) {
            limit = MessageConstants.DEFAULT_DOWNLOAD_SIZE;
        }

        List<MessageVO> messages = messageService.downloadChatHistory(userId, friendId, limit);

        if (messages == null || messages.isEmpty()) {
            response.setContentType("text/plain;charset=UTF-8");
            response.getWriter().write("暂无聊天记录可导出");
            return;
        }

        User friend = userMapper.selectById(friendId);
        String friendName = friend != null ? friend.getNickname() : "好友";

        response.setContentType("text/plain;charset=UTF-8");
        response.setHeader("Content-Disposition",
                "attachment;filename=" + URLEncoder.encode(friendName + "_聊天记录_" + System.currentTimeMillis() + ".txt", "UTF-8"));

        StringBuilder sb = new StringBuilder();
        sb.append("========================================\n");
        sb.append("聊天记录导出\n");
        sb.append("导出时间: ").append(new Date()).append("\n");
        sb.append("聊天对象: ").append(friendName).append("\n");
        sb.append("共 ").append(messages.size()).append(" 条消息\n");
        sb.append("========================================\n\n");

        for (MessageVO msg : messages) {
            sb.append("[").append(msg.getSendTime()).append("] ");
            sb.append(msg.getFromUserNickname()).append(": ");
            sb.append(msg.getContent()).append("\n");
        }

        response.getWriter().write(sb.toString());
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

    /**
     * 上传图片
     */
    @PostMapping("/upload/image")
    public Result<String> uploadImage(HttpServletRequest request, @RequestParam("file") MultipartFile file) {
        Long userId = (Long) request.getAttribute("userId");
        try {
            String url = ossUtil.uploadFile(file, "chat/images/");
            log.info("图片上传成功: userId={}, url={}", userId, url);
            return Result.success(url);
        } catch (Exception e) {
            log.error("上传图片失败", e);
            return Result.error("上传失败");
        }
    }

    /**
     * 上传语音
     */
    @PostMapping("/upload/voice")
    public Result<String> uploadVoice(HttpServletRequest request, @RequestParam("file") MultipartFile file) {
        Long userId = (Long) request.getAttribute("userId");
        try {
            String url = ossUtil.uploadFile(file, "chat/voice/");
            log.info("语音上传成功: userId={}, url={}", userId, url);
            return Result.success(url);
        } catch (Exception e) {
            log.error("上传语音失败", e);
            return Result.error("上传失败");
        }
    }
}
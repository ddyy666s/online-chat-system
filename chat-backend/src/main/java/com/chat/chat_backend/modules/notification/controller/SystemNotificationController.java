package com.chat.chat_backend.modules.notification.controller;

import com.chat.chat_backend.common.result.Result;
import com.chat.chat_backend.modules.notification.dto.response.SystemNotificationVO;
import com.chat.chat_backend.modules.notification.service.SystemNotificationService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/system-notification")
@RequiredArgsConstructor
public class SystemNotificationController {

    private final SystemNotificationService systemNotificationService;

    @PostMapping("/send")
    public Result<Void> sendNotification(HttpServletRequest request, @RequestBody Map<String, String> body) {
        Long adminId = (Long) request.getAttribute("userId");
        String role = (String) request.getAttribute("role");
        if (!"admin".equals(role)) {
            return Result.error("只有管理员才能发送系统通知");
        }
        String title = body.get("title");
        String content = body.get("content");
        if (title == null || title.isEmpty() || content == null || content.isEmpty()) {
            return Result.error("标题和内容不能为空");
        }
        systemNotificationService.sendNotification(adminId, title, content);
        return Result.success("通知已发送", null);
    }

    @GetMapping("/unread")
    public Result<Map<String, Object>> getUnreadNotifications(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        List<SystemNotificationVO> notifications = systemNotificationService.getUnreadNotifications(userId);
        Integer count = systemNotificationService.getUnreadCount(userId);
        return Result.success(Map.of("total", count, "notifications", notifications));
    }

    @PutMapping("/read/{notificationId}")
    public Result<Void> markAsRead(HttpServletRequest request, @PathVariable Long notificationId) {
        Long userId = (Long) request.getAttribute("userId");
        systemNotificationService.markAsRead(userId, notificationId);
        return Result.success("已标记已读", null);
    }
}

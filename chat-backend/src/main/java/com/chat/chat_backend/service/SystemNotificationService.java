package com.chat.chat_backend.service;

import com.chat.chat_backend.module.dto.response.SystemNotificationVO;
import java.util.List;

public interface SystemNotificationService {

    void sendNotification(Long adminId, String title, String content);

    List<SystemNotificationVO> getUnreadNotifications(Long userId);

    List<SystemNotificationVO> getNotificationsByAdmin(Long adminId);

    Integer getUnreadCount(Long userId);

    void markAsRead(Long userId, Long notificationId);
}

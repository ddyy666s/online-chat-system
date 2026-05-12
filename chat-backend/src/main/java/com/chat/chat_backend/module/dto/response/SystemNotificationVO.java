package com.chat.chat_backend.module.dto.response;

import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Builder
public class SystemNotificationVO {
    private Long id;
    private String title;
    private String content;
    private Long adminId;
    private String adminNickname;
    private LocalDateTime createdAt;
}

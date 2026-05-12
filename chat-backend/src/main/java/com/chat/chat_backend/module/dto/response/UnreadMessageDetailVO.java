package com.chat.chat_backend.module.dto.response;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class UnreadMessageDetailVO {
    private Long id;
    private Long fromUserId;
    private String fromUserNickname;
    private String fromUserAvatar;
    private Integer messageType;
    private String content;
    private LocalDateTime sendTime;
}
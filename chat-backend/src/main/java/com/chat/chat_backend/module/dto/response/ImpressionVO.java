package com.chat.chat_backend.module.dto.response;

import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Builder
public class ImpressionVO {
    private Long id;
    private Long fromUserId;
    private String fromUserNickname;
    private String fromUserAvatar;
    private Long toUserId;
    private String toUserNickname;
    private String toUserAvatar;
    private String content;
    private LocalDateTime createdAt;
}
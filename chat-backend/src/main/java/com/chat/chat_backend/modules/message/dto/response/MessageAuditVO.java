package com.chat.chat_backend.modules.message.dto.response;

import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Builder
public class MessageAuditVO {
    private Long id;
    private Long fromUserId;
    private String fromUserNickname;
    private Long toUserId;
    private String toUserNickname;
    private String content;
    private LocalDateTime sendTime;
}
package com.chat.chat_backend.module.dto.response;

import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Builder
public class MessageVO {
    private Long id;
    private Long fromUserId;
    private String fromUserNickname;
    private Integer duration;
    private String fromUserAvatar;
    private Long toUserId;
    private String toUserNickname;
    private Integer messageType;  // 1:文字 2:图片 3:系统消息/评价通知
    private String content;
    private Boolean isRead;
    private Boolean isRecalled;
    private LocalDateTime sendTime;
}
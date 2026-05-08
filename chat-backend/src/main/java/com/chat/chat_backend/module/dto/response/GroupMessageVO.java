package com.chat.chat_backend.module.dto.response;

import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Builder
public class GroupMessageVO {
    private Long id;
    private Long groupId;
    private Long fromUserId;
    private String fromUserNickname;
    private String fromUserAvatar;
    private String content;
    private Integer messageType;
    private LocalDateTime sendTime;
}
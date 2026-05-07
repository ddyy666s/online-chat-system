package com.chat.chat_backend.module.dto.response;

import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Builder
public class FriendRequestVO {
    private Long id;
    private Long fromUserId;
    private String fromUserNickname;
    private String fromUserAvatar;
    private String message;
    private Integer status;
    private LocalDateTime createdAt;
}
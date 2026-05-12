package com.chat.chat_backend.modules.friend.dto.request;

import lombok.Data;

@Data
public class SendFriendRequest {
    private Long toUserId;
    private String message;
}
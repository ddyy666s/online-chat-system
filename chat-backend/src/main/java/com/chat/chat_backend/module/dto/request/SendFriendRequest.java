package com.chat.chat_backend.module.dto.request;

import lombok.Data;

@Data
public class SendFriendRequest {
    private Long toUserId;
    private String message;
}
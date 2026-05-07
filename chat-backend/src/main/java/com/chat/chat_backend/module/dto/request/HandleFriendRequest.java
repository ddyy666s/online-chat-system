package com.chat.chat_backend.module.dto.request;

import lombok.Data;

@Data
public class HandleFriendRequest {
    private Integer status;  // 1同意 2拒绝
}
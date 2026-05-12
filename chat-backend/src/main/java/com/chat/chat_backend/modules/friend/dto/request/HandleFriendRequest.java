package com.chat.chat_backend.modules.friend.dto.request;

import lombok.Data;

@Data
public class HandleFriendRequest {
    private Integer status;  // 1同意 2拒绝
}
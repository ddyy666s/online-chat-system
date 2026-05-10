package com.chat.chat_backend.module.dto.response;

import lombok.Data;

@Data
public class UnreadGroupVO {
    private Long fromUserId;
    private Integer count;
}
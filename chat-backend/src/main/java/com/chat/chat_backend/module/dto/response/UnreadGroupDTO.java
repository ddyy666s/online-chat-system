package com.chat.chat_backend.module.dto.response;

import lombok.Data;

@Data
public class UnreadGroupDTO {
    private Long fromUserId;
    private Integer count;
}
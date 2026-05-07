package com.chat.chat_backend.module.dto.request;

import lombok.Data;

@Data
public class SendMessageRequest {
    private Long toUserId;
    private String content;
    private Integer messageType;  // 默认1
}
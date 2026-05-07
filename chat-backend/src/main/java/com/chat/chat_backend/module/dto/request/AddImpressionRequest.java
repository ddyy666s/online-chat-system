package com.chat.chat_backend.module.dto.request;

import lombok.Data;

@Data
public class AddImpressionRequest {
    private Long toUserId;
    private String content;  // 最多100字
}
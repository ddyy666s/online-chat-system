package com.chat.chat_backend.modules.impression.dto.request;

import lombok.Data;

@Data
public class AddImpressionRequest {
    private Long toUserId;
    private String content;  // 最多100字
}
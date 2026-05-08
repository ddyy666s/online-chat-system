package com.chat.chat_backend.module.dto.request;

import lombok.Data;

@Data
public class InviteMemberRequest {
    private Long groupId;
    private Long userId;
}
package com.chat.chat_backend.modules.group.dto.request;

import lombok.Data;

@Data
public class InviteMemberRequest {
    private Long groupId;
    private Long userId;
}
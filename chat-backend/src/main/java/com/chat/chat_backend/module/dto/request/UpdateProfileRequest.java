package com.chat.chat_backend.module.dto.request;

import lombok.Data;

@Data
public class UpdateProfileRequest {
    private String nickname;
    private String signature;
}
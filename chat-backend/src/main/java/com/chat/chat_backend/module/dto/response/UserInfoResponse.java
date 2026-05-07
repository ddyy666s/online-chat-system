package com.chat.chat_backend.module.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UserInfoResponse {
    private Long id;
    private String username;
    private String nickname;
    private String avatar;
    private String signature;
    private String role;
}
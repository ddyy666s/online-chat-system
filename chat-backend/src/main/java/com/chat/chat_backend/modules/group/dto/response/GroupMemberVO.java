package com.chat.chat_backend.modules.group.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class GroupMemberVO {
    private Long userId;
    private String nickname;
    private String avatar;
    private String groupNickname;
    private Integer role;
    private Boolean muted;
}
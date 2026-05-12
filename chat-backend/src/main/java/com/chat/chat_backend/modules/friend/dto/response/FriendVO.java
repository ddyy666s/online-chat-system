package com.chat.chat_backend.modules.friend.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class FriendVO {
    private Long id;
    private Long userId;
    private String nickname;
    private String avatar;
    private String signature;
    private String remark;
    private String groupName;
    private Boolean isOnline;
    private Integer unreadCount;
}
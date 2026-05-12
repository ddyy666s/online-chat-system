package com.chat.chat_backend.modules.message.dto.response;

import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class UnreadCountVO {
    private Integer total;
    private List<UnreadDetail> details;
    private List<UnreadMessage> messages;  // 未读消息列表

    @Data
    @Builder
    public static class UnreadDetail {
        private Long friendId;
        private String friendNickname;
        private String friendAvatar;
        private Integer unreadCount;
    }

    @Data
    @Builder
    public static class UnreadMessage {
        private Long id;
        private Long fromUserId;
        private String fromUserNickname;
        private String fromUserAvatar;
        private Integer messageType;
        private String content;
        private LocalDateTime sendTime;
    }
}
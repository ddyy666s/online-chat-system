package com.chat.chat_backend.module.dto.response;

import lombok.Builder;
import lombok.Data;
import java.util.List;

@Data
@Builder
public class UnreadCountVO {
    private Integer total;
    private List<UnreadDetail> details;
    
    @Data
    @Builder
    public static class UnreadDetail {
        private Long friendId;
        private String friendNickname;
        private Integer unreadCount;
    }
}
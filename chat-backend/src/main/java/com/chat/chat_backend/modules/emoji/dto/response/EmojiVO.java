package com.chat.chat_backend.modules.emoji.dto.response;

import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Builder
public class EmojiVO {
    private Long id;
    private String name;
    private String url;
    private String category;
    private Long userId;
    private Boolean isSystem;
    private LocalDateTime createdAt;
}
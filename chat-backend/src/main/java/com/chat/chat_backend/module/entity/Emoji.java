package com.chat.chat_backend.module.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("emoji")
public class Emoji {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String name;
    private String url;
    private String category;
    private Long userId;
    private Integer isSystem;
    private LocalDateTime createdAt;
}
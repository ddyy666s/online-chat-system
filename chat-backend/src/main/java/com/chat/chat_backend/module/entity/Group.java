package com.chat.chat_backend.module.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("chat_group")
public class Group {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String name;
    private String avatar;
    private Long ownerId;
    private String notice;
    private Integer memberCount;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
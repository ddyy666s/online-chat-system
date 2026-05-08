package com.chat.chat_backend.module.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("group_message")
public class GroupMessage {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long groupId;
    private Long fromUserId;
    private Integer messageType;
    private String content;
    private LocalDateTime sendTime;
    private LocalDateTime recallTime;
}
package com.chat.chat_backend.module.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("message")
public class Message {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long fromUserId;
    private Long toUserId;
    private Integer messageType;  // 1文字 2图片 3文件
    private String content;
    private Integer isRead;
    private LocalDateTime readTime;
    private LocalDateTime recallTime;
    private LocalDateTime sendTime;
}
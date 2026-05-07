package com.chat.chat_backend.module.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("friend_request")
public class FriendRequest {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long fromUserId;
    private Long toUserId;
    private String message;
    private Integer status;  // 0待处理 1已同意 2已拒绝 3已过期
    private LocalDateTime handledTime;
    private LocalDateTime expireTime;
    private LocalDateTime createdAt;
}
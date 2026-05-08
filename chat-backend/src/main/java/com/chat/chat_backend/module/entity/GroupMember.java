package com.chat.chat_backend.module.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("group_member")
public class GroupMember {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long groupId;
    private Long userId;
    private String nickname;
    private Integer role;
    private Integer unreadCount;
    private LocalDateTime joinTime;
    private LocalDateTime lastReadTime;
}
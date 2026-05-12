package com.chat.chat_backend.modules.group.dto.response;

import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class GroupVO {
    private Long id;
    private String name;
    private String avatar;
    private String notice;
    private Long ownerId;
    private Integer memberCount;
    private Integer unreadCount;
    private LocalDateTime createdAt;
    private List<GroupMemberVO> members;  // 引用外部定义的 GroupMemberVO
}
package com.chat.chat_backend.modules.admin.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class StatisticsVO {
    private Long totalUsers;
    private Long todayActiveUsers;
    private Long todayMessages;
    private Integer onlineUsers;
}
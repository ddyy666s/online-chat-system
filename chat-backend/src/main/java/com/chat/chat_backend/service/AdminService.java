package com.chat.chat_backend.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.chat.chat_backend.module.dto.response.MessageAuditVO;
import com.chat.chat_backend.module.dto.response.StatisticsVO;
import com.chat.chat_backend.module.dto.response.UserManageVO;

public interface AdminService {

    // 获取用户列表
    Page<UserManageVO> getUserList(Integer page, Integer size, String keyword);

    // 禁用/启用用户
    void updateUserStatus(Long userId, Integer status);

    // 获取聊天记录（审计）
    Page<MessageAuditVO> getMessageList(Integer page, Integer size,
                                        Long fromUserId, Long toUserId,
                                        String startTime, String endTime);

    // 获取统计数据
    StatisticsVO getStatistics();
}
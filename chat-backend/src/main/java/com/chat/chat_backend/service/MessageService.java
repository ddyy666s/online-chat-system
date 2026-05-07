package com.chat.chat_backend.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.chat.chat_backend.module.dto.response.MessageVO;
import com.chat.chat_backend.module.dto.response.UnreadCountVO;
import java.util.List;

public interface MessageService {

    // 获取聊天记录
    Page<MessageVO> getChatHistory(Long userId, Long friendId, Integer page, Integer size);

    // 下载聊天记录
    List<MessageVO> downloadChatHistory(Long userId, Long friendId);

    // 标记消息已读
    void markAsRead(Long userId, Long friendId);

    // 获取未读消息统计
    UnreadCountVO getUnreadCount(Long userId);

    // 撤回消息
    void recallMessage(Long userId, Long messageId);
}
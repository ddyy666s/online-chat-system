package com.chat.chat_backend.modules.message.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.chat.chat_backend.modules.message.dto.response.MessageVO;
import com.chat.chat_backend.modules.message.dto.response.UnreadCountVO;
import java.util.List;

public interface MessageService {

    Page<MessageVO> getChatHistory(Long userId, Long friendId, Integer page, Integer size);

    List<MessageVO> downloadChatHistory(Long userId, Long friendId, Integer limit);

    void markAsRead(Long userId, Long friendId);

    UnreadCountVO getUnreadCount(Long userId);

    void recallMessage(Long userId, Long messageId);
}
package com.chat.chat_backend.service.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.chat.chat_backend.common.constant.RedisConstants;
import com.chat.chat_backend.common.exception.BusinessException;
import com.chat.chat_backend.common.result.ResultCode;
import com.chat.chat_backend.common.utils.RedisUtil;
import com.chat.chat_backend.mapper.MessageMapper;
import com.chat.chat_backend.mapper.UserMapper;
import com.chat.chat_backend.module.dto.response.MessageVO;
import com.chat.chat_backend.module.dto.response.UnreadCountVO;
import com.chat.chat_backend.module.entity.Message;
import com.chat.chat_backend.module.entity.User;
import com.chat.chat_backend.service.MessageService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MessageServiceImpl implements MessageService {

    private final MessageMapper messageMapper;
    private final UserMapper userMapper;
    private final RedisUtil redisUtil;

    @Override
    public Page<MessageVO> getChatHistory(Long userId, Long friendId, Integer page, Integer size) {
        int offset = (page - 1) * size;
        List<Message> messages = messageMapper.findChatHistory(userId, friendId, offset, size);

        // 获取用户信息
        User friend = userMapper.selectById(friendId);
        User currentUser = userMapper.selectById(userId);

        List<MessageVO> voList = messages.stream()
                .map(msg -> {
                    // 获取发送者信息（重要：需要获取头像）
                    User fromUser = userMapper.selectById(msg.getFromUserId());
                    String fromUserNickname = fromUser != null ? fromUser.getNickname() : "未知用户";
                    String fromUserAvatar = fromUser != null ? fromUser.getAvatar() : null;

                    return MessageVO.builder()
                            .id(msg.getId())
                            .fromUserId(msg.getFromUserId())
                            .fromUserNickname(fromUserNickname)
                            .fromUserAvatar(fromUserAvatar)  // 添加这一行
                            .toUserId(msg.getToUserId())
                            .toUserNickname(msg.getToUserId().equals(userId) ?
                                    currentUser.getNickname() : friend.getNickname())
                            .messageType(msg.getMessageType())
                            .content(msg.getRecallTime() != null ? "对方撤回了一条消息" : msg.getContent())
                            .isRead(msg.getIsRead() == 1)
                            .isRecalled(msg.getRecallTime() != null)
                            .sendTime(msg.getSendTime())
                            .build();
                })
                .collect(Collectors.toList());

        Page<MessageVO> pageResult = new Page<>(page, size);
        pageResult.setRecords(voList);
        pageResult.setTotal(messages.size());
        return pageResult;
    }

    @Override
    public List<MessageVO> downloadChatHistory(Long userId, Long friendId) {
        List<Message> messages = messageMapper.findChatHistory(userId, friendId, 0, 9999);
        User friend = userMapper.selectById(friendId);
        User currentUser = userMapper.selectById(userId);

        return messages.stream()
                .map(msg -> {
                    User fromUser = userMapper.selectById(msg.getFromUserId());
                    String fromUserNickname = fromUser != null ? fromUser.getNickname() : "未知用户";

                    return MessageVO.builder()
                            .id(msg.getId())
                            .fromUserId(msg.getFromUserId())
                            .fromUserNickname(fromUserNickname)
                            .content(msg.getRecallTime() != null ? "对方撤回了一条消息" : msg.getContent())
                            .sendTime(msg.getSendTime())
                            .build();
                })
                .collect(Collectors.toList());
    }

    @Override
    public void markAsRead(Long userId, Long friendId) {
        // 更新数据库
        messageMapper.markAsRead(userId, friendId);

        // 清除Redis未读计数
        String unreadKey = RedisConstants.UNREAD_COUNT + userId;
        redisUtil.hashDelete(unreadKey, String.valueOf(friendId));
    }

    @Override
    public UnreadCountVO getUnreadCount(Long userId) {
        Integer total = messageMapper.countUnreadTotal(userId);
        List<MessageMapper.UnreadGroup> groups = messageMapper.groupUnreadByFriend(userId);

        List<UnreadCountVO.UnreadDetail> details = groups.stream()
                .map(g -> {
                    User friend = userMapper.selectById(g.from_user_id);
                    return UnreadCountVO.UnreadDetail.builder()
                            .friendId(g.from_user_id)
                            .friendNickname(friend != null ? friend.getNickname() : "未知用户")
                            .friendAvatar(friend != null ? friend.getAvatar() : null)  // 添加这一行
                            .unreadCount(g.count)
                            .build();
                })
                .collect(Collectors.toList());

        return UnreadCountVO.builder()
                .total(total)
                .details(details)
                .build();
    }

    @Override
    public void recallMessage(Long userId, Long messageId) {
        int updated = messageMapper.recallMessage(messageId, userId);
        if (updated == 0) {
            throw new BusinessException(ResultCode.MESSAGE_RECALL_TIMEOUT);
        }
    }
}
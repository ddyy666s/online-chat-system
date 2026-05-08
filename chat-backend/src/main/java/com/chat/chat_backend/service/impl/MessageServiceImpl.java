package com.chat.chat_backend.service.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.chat.chat_backend.common.constant.MessageConstants;
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
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
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

        // 获取总记录数
        Long total = messageMapper.countChatHistory(userId, friendId);

        User friend = userMapper.selectById(friendId);
        User currentUser = userMapper.selectById(userId);

        List<MessageVO> voList = messages.stream()
                .map(msg -> {
                    User fromUser = userMapper.selectById(msg.getFromUserId());
                    String fromUserNickname = fromUser != null ? fromUser.getNickname() : "未知用户";
                    String fromUserAvatar = fromUser != null ? fromUser.getAvatar() : null;
                    return MessageVO.builder()
                            .id(msg.getId())
                            .fromUserId(msg.getFromUserId())
                            .fromUserNickname(fromUserNickname)
                            .fromUserAvatar(fromUserAvatar)
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
        pageResult.setTotal(total);
        return pageResult;
    }

    @Override
    public List<MessageVO> downloadChatHistory(Long userId, Long friendId, Integer limit) {
        log.info("downloadChatHistory: userId={}, friendId={}, limit={}", userId, friendId, limit);

        if (limit == null || limit <= 0) {
            limit = MessageConstants.DEFAULT_DOWNLOAD_SIZE;
        }
        if (limit > MessageConstants.MAX_DOWNLOAD_SIZE) {
            limit = MessageConstants.MAX_DOWNLOAD_SIZE;
        }

        List<Message> messages = messageMapper.findChatHistory(userId, friendId, 0, limit);
        log.info("查询到消息数量: {}", messages.size());

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
        messageMapper.markAsRead(userId, friendId);
        String unreadKey = RedisConstants.UNREAD_COUNT + userId;
        redisUtil.hashDelete(unreadKey, String.valueOf(friendId));
    }

    @Override
    public UnreadCountVO getUnreadCount(Long userId) {
        Integer total = messageMapper.countUnreadTotal(userId);
        List<MessageMapper.UnreadGroup> groups = messageMapper.groupUnreadByFriend(userId);

        List<UnreadCountVO.UnreadDetail> details = new ArrayList<>();
        for (MessageMapper.UnreadGroup group : groups) {
            if (group.getFromUserId() == null) continue;
            User friend = userMapper.selectById(group.getFromUserId());
            if (friend != null) {
                details.add(UnreadCountVO.UnreadDetail.builder()
                        .friendId(group.getFromUserId())
                        .friendNickname(friend.getNickname())
                        .friendAvatar(friend.getAvatar())
                        .unreadCount(group.getCount())
                        .build());
            }
        }

        List<UnreadCountVO.UnreadMessage> messages = new ArrayList<>();
        List<MessageMapper.UnreadMessageDetail> unreadMessages = messageMapper.findUnreadMessages(userId);
        for (MessageMapper.UnreadMessageDetail msg : unreadMessages) {
            String content = msg.getContent();
            if (content.length() > 50) {
                content = content.substring(0, 50) + "...";
            }
            messages.add(UnreadCountVO.UnreadMessage.builder()
                    .id(msg.getId())
                    .fromUserId(msg.getFromUserId())
                    .fromUserNickname(msg.getFromUserNickname())
                    .fromUserAvatar(msg.getFromUserAvatar())
                    .content(content)
                    .sendTime(msg.getSendTime())
                    .build());
        }

        return UnreadCountVO.builder()
                .total(total)
                .details(details)
                .messages(messages)
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
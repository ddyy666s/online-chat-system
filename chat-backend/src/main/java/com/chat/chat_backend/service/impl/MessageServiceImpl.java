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
import com.chat.chat_backend.module.dto.response.UnreadGroupDTO;
import com.chat.chat_backend.module.dto.response.UnreadMessageDetailDTO;
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
        Long total = messageMapper.countChatHistory(userId, friendId);

        User friend = userMapper.selectById(friendId);
        User currentUser = userMapper.selectById(userId);

        List<MessageVO> voList = messages.stream()
                .map(msg -> {
                    User fromUser = userMapper.selectById(msg.getFromUserId());
                    String fromUserNickname = fromUser != null ? fromUser.getNickname() : "未知用户";
                    String fromUserAvatar = fromUser != null ? fromUser.getAvatar() : null;

                    String content = msg.getContent();
                    Integer duration = null;

                    // 解析语音消息的时长
                    if (msg.getMessageType() == 4 && content != null && content.contains("|")) {
                        String[] parts = content.split("\\|");
                        content = parts[0];
                        if (parts.length > 1) {
                            try {
                                duration = Integer.parseInt(parts[1]);
                            } catch (NumberFormatException e) {
                                duration = null;
                            }
                        }
                    }

                    return MessageVO.builder()
                            .id(msg.getId())
                            .fromUserId(msg.getFromUserId())
                            .fromUserNickname(fromUserNickname)
                            .fromUserAvatar(fromUserAvatar)
                            .toUserId(msg.getToUserId())
                            .toUserNickname(msg.getToUserId().equals(userId) ?
                                    currentUser.getNickname() : friend.getNickname())
                            .messageType(msg.getMessageType())
                            .content(content)
                            .duration(duration)
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
        if (limit == null || limit <= 0) {
            limit = MessageConstants.DEFAULT_DOWNLOAD_SIZE;
        }
        if (limit > MessageConstants.MAX_DOWNLOAD_SIZE) {
            limit = MessageConstants.MAX_DOWNLOAD_SIZE;
        }

        List<Message> messages = messageMapper.findChatHistory(userId, friendId, 0, limit);

        return messages.stream()
                .map(msg -> {
                    User fromUser = userMapper.selectById(msg.getFromUserId());
                    String fromUserNickname = fromUser != null ? fromUser.getNickname() : "未知用户";

                    String content = msg.getContent();
                    // 下载时去掉时长标记
                    if (msg.getMessageType() == 4 && content != null && content.contains("|")) {
                        content = content.split("\\|")[0];
                    }

                    return MessageVO.builder()
                            .id(msg.getId())
                            .fromUserId(msg.getFromUserId())
                            .fromUserNickname(fromUserNickname)
                            .content(content)
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

        // 使用新的 DTO 类
        List<UnreadGroupDTO> groups = messageMapper.groupUnreadByFriend(userId);

        List<UnreadCountVO.UnreadDetail> details = new ArrayList<>();
        for (UnreadGroupDTO group : groups) {
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

        // 使用新的 DTO 类
        List<UnreadMessageDetailDTO> unreadMessages = messageMapper.findUnreadMessages(userId);
        List<UnreadCountVO.UnreadMessage> messages = new ArrayList<>();
        for (UnreadMessageDetailDTO msg : unreadMessages) {
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
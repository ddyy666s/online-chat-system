package com.chat.chat_backend.websocket.service;

import cn.hutool.json.JSONUtil;
import com.chat.chat_backend.modules.message.mapper.MessageMapper;
import com.chat.chat_backend.modules.user.mapper.UserMapper;
import com.chat.chat_backend.modules.message.entity.Message;
import com.chat.chat_backend.modules.user.entity.User;
import com.chat.chat_backend.websocket.WebSocketSessionManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class MessageHandlerService {

    private final MessageMapper messageMapper;
    private final UserMapper userMapper;
    private final WebSocketSessionManager sessionManager;

    public void sendAndNotify(Long fromUserId, Long toUserId, String content, Integer messageType, Integer duration) {
        // 保存消息
        Message msg = new Message();
        msg.setFromUserId(fromUserId);
        msg.setToUserId(toUserId);
        msg.setMessageType(messageType);
        msg.setContent(formatContent(content, messageType, duration));
        msg.setIsRead(0);
        msg.setSendTime(LocalDateTime.now());
        messageMapper.insert(msg);

        // 获取发送者信息
        User fromUser = userMapper.selectById(fromUserId);
        String fromUserNickname = fromUser != null ? fromUser.getNickname() : "未知用户";

        // 构建响应
        var response = JSONUtil.createObj()
                .set("type", "message")
                .set("messageId", msg.getId())
                .set("fromUserId", fromUserId)
                .set("fromUserNickname", fromUserNickname)
                .set("content", content)
                .set("messageType", messageType)
                .set("sendTime", msg.getSendTime().toString());

        if (messageType == 4 && duration != null && duration > 0) {
            response.set("duration", duration);
        }

        // 推送给对方
        sessionManager.sendMessage(toUserId, response.toString());
    }

    private String formatContent(String content, Integer messageType, Integer duration) {
        if (messageType == 4 && duration != null && duration > 0) {
            return content + "|" + duration;
        }
        return content;
    }
}
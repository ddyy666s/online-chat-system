package com.chat.chat_backend.websocket;

import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.chat.chat_backend.common.constant.RedisConstants;
import com.chat.chat_backend.common.utils.JwtUtil;
import com.chat.chat_backend.common.utils.RedisUtil;
import com.chat.chat_backend.mapper.MessageMapper;
import com.chat.chat_backend.mapper.UserMapper;
import com.chat.chat_backend.module.entity.Message;
import com.chat.chat_backend.module.entity.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.net.URI;
import java.time.LocalDateTime;
import java.util.concurrent.TimeUnit;

@Slf4j
@Component
@RequiredArgsConstructor
public class ChatWebSocketHandler extends TextWebSocketHandler {

    private final JwtUtil jwtUtil;
    private final RedisUtil redisUtil;
    private final WebSocketSessionManager sessionManager;
    private final MessageMapper messageMapper;
    private final UserMapper userMapper;

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        Long userId = extractUserIdFromSession(session);

        if (userId == null) {
            log.warn("WebSocket连接失败：token无效");
            session.close(CloseStatus.POLICY_VIOLATION);
            return;
        }

        // 存储会话
        sessionManager.add(userId, session);
        session.getAttributes().put("userId", userId);

        // 记录在线状态到Redis
        redisUtil.addToSet(RedisConstants.ONLINE_USERS, String.valueOf(userId));
        redisUtil.expire(RedisConstants.ONLINE_USERS, RedisConstants.ONLINE_EXPIRE_SECONDS, TimeUnit.SECONDS);

        // 广播好友上线通知
        broadcastStatus(userId, true);

        log.info("用户 {} 已连接WebSocket，当前在线人数: {}", userId, sessionManager.getOnlineCount());
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        Long userId = (Long) session.getAttributes().get("userId");
        if (userId == null) {
            return;
        }

        String payload = message.getPayload();
        JSONObject json = JSONUtil.parseObj(payload);
        String type = json.getStr("type");

        if ("message".equals(type)) {
            handleChatMessage(userId, json);
        } else if ("ping".equals(type)) {
            // 心跳响应
            session.sendMessage(new TextMessage("{\"type\":\"pong\"}"));
        }
    }

    private void handleChatMessage(Long fromUserId, JSONObject json) {
        Long toUserId = json.getLong("toUserId");
        String content = json.getStr("content");
        Integer messageType = json.getInt("messageType", 1);

        // 保存消息到数据库
        Message msg = new Message();
        msg.setFromUserId(fromUserId);
        msg.setToUserId(toUserId);
        msg.setMessageType(messageType);
        msg.setContent(content);
        msg.setIsRead(0);
        msg.setSendTime(LocalDateTime.now());
        messageMapper.insert(msg);

        // 增加Redis未读计数
        String unreadKey = RedisConstants.UNREAD_COUNT + toUserId;
        redisUtil.hashIncrement(unreadKey, String.valueOf(fromUserId), 1);
        redisUtil.expire(unreadKey, 7, TimeUnit.DAYS);

        // 获取发送者信息
        User fromUser = userMapper.selectById(fromUserId);
        String fromUserNickname = fromUser != null ? fromUser.getNickname() : "未知用户";
        String fromUserAvatar = fromUser != null ? fromUser.getAvatar() : null;

        // 构建消息体
        JSONObject response = JSONUtil.createObj()
                .set("type", "message")
                .set("messageId", msg.getId())
                .set("fromUserId", fromUserId)
                .set("fromUserNickname", fromUserNickname)
                .set("fromUserAvatar", fromUserAvatar)
                .set("content", content)
                .set("messageType", messageType)
                .set("sendTime", msg.getSendTime().toString());

        // 如果对方在线，实时推送
        if (sessionManager.isOnline(toUserId)) {
            sessionManager.sendMessage(toUserId, response.toString());
        }
    }

    private void broadcastStatus(Long userId, boolean online) {
        JSONObject statusMsg = JSONUtil.createObj()
                .set("type", "status")
                .set("userId", userId)
                .set("online", online);

        String jsonMsg = statusMsg.toString();

        // 注意：需要先给 WebSocketSessionManager 添加 sendMessage 方法
        // 或者直接遍历所有会话发送
        for (WebSocketSession session : sessionManager.getAllSessions().values()) {
            if (session != null && session.isOpen()) {
                try {
                    session.sendMessage(new TextMessage(jsonMsg));
                } catch (IOException e) {
                    log.warn("广播状态失败: {}", e.getMessage());
                }
            }
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        Long userId = (Long) session.getAttributes().get("userId");
        if (userId != null) {
            sessionManager.remove(userId);
            redisUtil.removeFromSet(RedisConstants.ONLINE_USERS, String.valueOf(userId));
            broadcastStatus(userId, false);
            log.info("用户 {} 已断开WebSocket", userId);
        }
    }

    private Long extractUserIdFromSession(WebSocketSession session) {
        URI uri = session.getUri();
        if (uri == null) {
            return null;
        }

        String query = uri.getQuery();
        if (query == null || !query.contains("token=")) {
            return null;
        }

        String token = query.split("token=")[1];
        if (token.contains("&")) {
            token = token.split("&")[0];
        }

        if (jwtUtil.validateToken(token)) {
            return jwtUtil.getUserIdFromToken(token);
        }

        return null;
    }
}
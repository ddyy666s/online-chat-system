package com.chat.chat_backend.websocket;

import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.chat.chat_backend.common.constant.RedisConstants;
import com.chat.chat_backend.common.utils.JwtUtil;
import com.chat.chat_backend.common.utils.RedisUtil;
import com.chat.chat_backend.mapper.*;
import com.chat.chat_backend.module.entity.*;
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
import java.util.List;
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
    private final GroupMessageMapper groupMessageMapper;
    private final GroupMemberMapper groupMemberMapper;
    private final GroupMapper groupMapper;

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        Long userId = extractUserIdFromSession(session);

        if (userId == null) {
            log.warn("WebSocket连接失败：token无效");
            session.close(CloseStatus.POLICY_VIOLATION);
            return;
        }

        sessionManager.add(userId, session);
        session.getAttributes().put("userId", userId);

        redisUtil.addToSet(RedisConstants.ONLINE_USERS, String.valueOf(userId));
        redisUtil.expire(RedisConstants.ONLINE_USERS, RedisConstants.ONLINE_EXPIRE_SECONDS, TimeUnit.SECONDS);

        broadcastStatus(userId, true);

        log.info("用户 {} 已连接WebSocket，当前在线人数: {}", userId, sessionManager.getOnlineCount());
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        Long userId = (Long) session.getAttributes().get("userId");
        if (userId == null) {
            log.warn("消息处理失败：userId为空");
            return;
        }

        String payload = message.getPayload();
        log.info("收到消息: userId={}, payload={}", userId, payload);

        JSONObject json = JSONUtil.parseObj(payload);
        String type = json.getStr("type");

        if ("message".equals(type)) {
            handleChatMessage(userId, json);
        } else if ("group_message".equals(type)) {
            handleGroupMessage(userId, json);
        } else if ("ping".equals(type)) {
            session.sendMessage(new TextMessage("{\"type\":\"pong\"}"));
        }
    }

    // 单聊消息处理（已支持语音时长 duration）
    private void handleChatMessage(Long fromUserId, JSONObject json) {
        Long toUserId = json.getLong("toUserId");
        String content = json.getStr("content");
        Integer messageType = json.getInt("messageType", 1);
        Integer duration = json.getInt("duration");

        log.info("收到消息: from={}, to={}, type={}, duration={}", fromUserId, toUserId, messageType, duration);

        // 保存消息
        Message msg = new Message();
        msg.setFromUserId(fromUserId);
        msg.setToUserId(toUserId);
        msg.setMessageType(messageType);

        // 语音消息：存储格式 "url|duration"
        if (messageType == 4 && duration != null && duration > 0) {
            msg.setContent(content + "|" + duration);
        } else {
            msg.setContent(content);
        }

        msg.setIsRead(0);
        msg.setSendTime(LocalDateTime.now());
        messageMapper.insert(msg);

        // 获取发送者信息
        User fromUser = userMapper.selectById(fromUserId);
        String fromUserNickname = fromUser != null ? fromUser.getNickname() : "未知用户";

        // 构建返回消息（包含时长）
        JSONObject response = JSONUtil.createObj()
                .set("type", "message")
                .set("messageId", msg.getId())
                .set("fromUserId", fromUserId)
                .set("fromUserNickname", fromUserNickname)
                .set("content", content)
                .set("messageType", messageType)
                .set("sendTime", msg.getSendTime().toString());

        // 如果是语音消息，添加时长
        if (messageType == 4 && duration != null && duration > 0) {
            response.set("duration", duration);
        }

        // 推送给对方
        sessionManager.sendMessage(toUserId, response.toString());
    }

    // 群聊消息处理
    private void handleGroupMessage(Long fromUserId, JSONObject json) {
        Long groupId = json.getLong("groupId");
        String content = json.getStr("content");
        Integer messageType = json.getInt("messageType", 1);
        Integer duration = json.getInt("duration"); // 群聊也支持语音时长

        if (groupId == null || content == null || content.trim().isEmpty()) {
            log.error("群消息参数错误");
            return;
        }

        // 检查群是否存在且用户是群成员
        Group group = groupMapper.selectById(groupId);
        if (group == null) {
            log.error("群聊不存在: groupId={}", groupId);
            return;
        }

        List<GroupMember> members = groupMemberMapper.findByGroupId(groupId);
        boolean isMember = members.stream().anyMatch(m -> m.getUserId().equals(fromUserId));
        if (!isMember) {
            log.error("用户 {} 不是群 {} 的成员", fromUserId, groupId);
            return;
        }

        // 保存群消息
        GroupMessage msg = new GroupMessage();
        msg.setGroupId(groupId);
        msg.setFromUserId(fromUserId);
        msg.setMessageType(messageType);
        msg.setContent(content);
        msg.setSendTime(LocalDateTime.now());
        groupMessageMapper.insert(msg);
        log.info("群消息已保存: id={}, groupId={}, from={}, duration={}",
                msg.getId(), groupId, fromUserId, duration);

        // 增加所有群成员的未读计数（发送者除外）
        groupMemberMapper.incrementUnreadCount(groupId, fromUserId);

        // 获取发送者信息
        User fromUser = userMapper.selectById(fromUserId);
        String fromUserNickname = fromUser != null ? fromUser.getNickname() : "未知用户";

        // 构建消息体 → 携带 duration
        JSONObject response = JSONUtil.createObj()
                .set("type", "group_message")
                .set("messageId", msg.getId())
                .set("groupId", groupId)
                .set("fromUserId", fromUserId)
                .set("fromUserNickname", fromUserNickname)
                .set("content", content)
                .set("messageType", messageType)
                .set("duration", duration)
                .set("sendTime", msg.getSendTime().toString());

        String responseStr = response.toString();

        // 推送给群内所有在线成员（发送者除外）
        for (GroupMember member : members) {
            if (!member.getUserId().equals(fromUserId)) {
                if (sessionManager.isOnline(member.getUserId())) {
                    sessionManager.sendMessage(member.getUserId(), responseStr);
                    log.info("群消息已推送给用户 {}", member.getUserId());
                }
            }
        }
    }

    private void broadcastStatus(Long userId, boolean online) {
        JSONObject statusMsg = JSONUtil.createObj()
                .set("type", "status")
                .set("userId", userId)
                .set("online", online);

        String jsonMsg = statusMsg.toString();

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
            log.warn("URI为空");
            return null;
        }

        String query = uri.getQuery();
        if (query == null || !query.contains("token=")) {
            log.warn("Query中无token");
            return null;
        }

        String token = query.split("token=")[1];
        if (token.contains("&")) {
            token = token.split("&")[0];
        }

        if (jwtUtil.validateToken(token)) {
            return jwtUtil.getUserIdFromToken(token);
        }

        log.warn("Token验证失败");
        return null;
    }
}
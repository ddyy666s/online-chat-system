package com.chat.chat_backend.common.utils;

import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.chat.chat_backend.websocket.WebSocketSessionManager;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import jakarta.annotation.PostConstruct;
import java.io.IOException;

/**
 * WebSocket 工具类
 */
@Slf4j
@Component
public class WebSocketUtil {

    private static WebSocketSessionManager sessionManager;

    @Autowired
    public void setSessionManager(WebSocketSessionManager manager) {
        sessionManager = manager;
    }

    /**
     * 判断用户是否在线
     */
    public static boolean isOnline(Long userId) {
        return sessionManager != null && sessionManager.isOnline(userId);
    }

    /**
     * 获取在线人数
     */
    public static int getOnlineCount() {
        return sessionManager != null ? sessionManager.getOnlineCount() : 0;
    }

    /**
     * 获取用户会话
     */
    public static WebSocketSession getSession(Long userId) {
        return sessionManager != null ? sessionManager.get(userId) : null;
    }

    /**
     * 发送消息给指定用户
     */
    public static void sendMessage(Long userId, Object message) {
        if (sessionManager == null) {
            log.warn("WebSocketSessionManager 未初始化");
            return;
        }

        WebSocketSession session = sessionManager.get(userId);
        if (session != null && session.isOpen()) {
            try {
                String jsonMsg = message instanceof String ? (String) message : JSONUtil.toJsonStr(message);
                session.sendMessage(new TextMessage(jsonMsg));
            } catch (IOException e) {
                log.error("发送消息给用户 {} 失败: {}", userId, e.getMessage());
            }
        }
    }

    /**
     * 发送消息通知
     */
    public static void sendMessageNotification(Long toUserId, String fromUserNickname,
                                               String content, Long messageId, String sendTime) {
        JSONObject msg = JSONUtil.createObj()
                .set("type", "message")
                .set("messageId", messageId)
                .set("fromUserNickname", fromUserNickname)
                .set("content", content)
                .set("sendTime", sendTime);
        sendMessage(toUserId, msg);
    }

    /**
     * 广播状态通知（上线/下线）
     */
    public static void broadcastStatus(Long userId, boolean online) {
        if (sessionManager == null) return;

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
}
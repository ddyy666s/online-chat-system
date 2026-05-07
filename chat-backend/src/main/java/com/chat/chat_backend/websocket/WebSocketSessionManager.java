package com.chat.chat_backend.websocket;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
@Slf4j
@Component
public class WebSocketSessionManager {

    // userId -> WebSocketSession
    private final Map<Long, WebSocketSession> sessionMap = new ConcurrentHashMap<>();

    public void add(Long userId, WebSocketSession session) {
        if (userId != null && session != null) {
            sessionMap.put(userId, session);
        }
    }

    public void remove(Long userId) {
        if (userId != null) {
            sessionMap.remove(userId);
        }
    }

    public WebSocketSession get(Long userId) {
        return userId == null ? null : sessionMap.get(userId);
    }

    public boolean isOnline(Long userId) {
        return userId != null && sessionMap.containsKey(userId);
    }

    public int getOnlineCount() {
        return sessionMap.size();
    }



    public Map<Long, WebSocketSession> getAllSessions() {
        return sessionMap;
    }

    /**
     * 发送消息给指定用户
     */
    public void sendMessage(Long userId, String message) {
        WebSocketSession session = get(userId);
        if (session != null && session.isOpen()) {
            try {
                session.sendMessage(new TextMessage(message));
            } catch (IOException e) {
                log.error("发送消息给用户 {} 失败: {}", userId, e.getMessage());
            }
        }
    }

}
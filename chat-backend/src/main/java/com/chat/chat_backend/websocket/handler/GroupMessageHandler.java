package com.chat.chat_backend.websocket.handler;

import cn.hutool.json.JSONObject;
import com.chat.chat_backend.websocket.service.GroupMessageHandlerService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class GroupMessageHandler {

    private final GroupMessageHandlerService groupMessageHandlerService;

    public void handle(Long fromUserId, JSONObject json) {
        Long groupId = json.getLong("groupId");
        String content = json.getStr("content");
        Integer messageType = json.getInt("messageType", 1);
        Integer duration = json.getInt("duration");

        log.info("群聊消息: from={}, groupId={}, type={}", fromUserId, groupId, messageType);

        groupMessageHandlerService.sendAndNotify(fromUserId, groupId, content, messageType, duration);
    }
}
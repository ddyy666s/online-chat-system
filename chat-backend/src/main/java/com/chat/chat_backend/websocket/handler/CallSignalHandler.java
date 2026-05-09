package com.chat.chat_backend.websocket.handler;

import cn.hutool.json.JSONObject;
import com.chat.chat_backend.websocket.service.CallSignalHandlerService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class CallSignalHandler {

    private final CallSignalHandlerService callSignalHandlerService;

    public void handle(Long fromUserId, JSONObject json) {
        String action = json.getStr("action");
        Long toUserId = json.getLong("toUserId");
        String callType = json.getStr("callType");

        if (toUserId == null || toUserId.equals(fromUserId)) {
            log.warn("无效的通话目标用户: from={}, to={}", fromUserId, toUserId);
            return;
        }

        String sdp = json.getStr("sdp");
        String candidate = json.getStr("candidate");
        String sdpMid = json.getStr("sdpMid");
        Integer sdpMLineIndex = json.getInt("sdpMLineIndex");

        callSignalHandlerService.forward(fromUserId, toUserId, callType, action, sdp, candidate, sdpMid, sdpMLineIndex);
    }
}
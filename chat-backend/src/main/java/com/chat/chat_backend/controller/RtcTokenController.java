package com.chat.chat_backend.controller;

import cn.hutool.json.JSONObject;
import com.chat.chat_backend.common.result.Result;
import com.chat.chat_backend.config.RtcConfig;
import com.chat.chat_backend.service.RtcTokenService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/rtc")
@RequiredArgsConstructor
public class RtcTokenController {

    private final RtcTokenService rtcTokenService;
    private final RtcConfig rtcConfig;

    @PostMapping("/token")
    public Result<JSONObject> getToken(HttpServletRequest request, @RequestBody JSONObject body) {
        String channelId = body.getStr("channelId");
        Long userId = (Long) request.getAttribute("userId");

        if (channelId == null || userId == null) {
            return Result.error("channelId 和 userId 不能为空");
        }

        String userIdStr = String.valueOf(userId);
        String token = rtcTokenService.generateToken(channelId, userIdStr);

        JSONObject result = new JSONObject();
        result.set("token", token);
        result.set("appId", rtcConfig.getAppId());
        result.set("channelId", channelId);
        result.set("userId", userIdStr);

        return Result.success(result);
    }
}

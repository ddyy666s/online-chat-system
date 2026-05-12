package com.chat.chat_backend.modules.rtc.controller;

import cn.hutool.json.JSONObject;
import com.chat.chat_backend.common.result.Result;
import com.chat.chat_backend.config.RtcConfig;
import com.chat.chat_backend.modules.rtc.service.RtcTokenService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

/**
 * RTC 令牌控制器
 *
 * @author chat-backend
 * @since 2026-05-12
 */
@Slf4j
@RestController
@RequestMapping("/rtc")
@RequiredArgsConstructor
public class RtcTokenController {

    /** RTC 令牌服务 */
    private final RtcTokenService rtcTokenService;
    /** RTC 配置 */
    private final RtcConfig rtcConfig;

    /**
     * 获取 RTC 令牌
     *
     * @param request HTTP 请求对象（包含用户信息）
     * @param body    请求体（包含 channelId）
     * @return 令牌信息（包含 token、appId、channelId、userId）
     */
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

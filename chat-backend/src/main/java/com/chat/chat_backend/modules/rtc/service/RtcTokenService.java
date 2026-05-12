package com.chat.chat_backend.modules.rtc.service;

import cn.hutool.crypto.digest.HMac;
import cn.hutool.crypto.digest.HmacAlgorithm;
import com.chat.chat_backend.config.RtcConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class RtcTokenService {

    private final RtcConfig rtcConfig;

    public String generateToken(String channelId, String userId) {
        String appId = rtcConfig.getAppId();
        String appKey = rtcConfig.getAppKey();
        String nonce = UUID.randomUUID().toString().replace("-", "").substring(0, 8);
        long timestamp = System.currentTimeMillis() / 1000;

        String raw = appId + channelId + userId + nonce + timestamp;

        HMac hmac = new HMac(HmacAlgorithm.HmacSHA1, appKey.getBytes(StandardCharsets.UTF_8));
        byte[] signature = hmac.digest(raw);

        String signatureBase64 = Base64.getEncoder().encodeToString(signature);
        String token = nonce + ":" + timestamp + ":" + signatureBase64;

        log.info("生成RTC Token: appId={}, channelId={}, userId={}", appId, channelId, userId);
        return token;
    }
}

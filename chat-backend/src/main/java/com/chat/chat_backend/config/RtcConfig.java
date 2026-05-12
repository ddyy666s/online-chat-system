package com.chat.chat_backend.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 实时通信（RTC）服务配置属性类，绑定"rtc"前缀的配置项
 * @author chat-backend
 * @since 2026-05-12
 */
@Data
@Component
@ConfigurationProperties(prefix = "rtc")
public class RtcConfig {
    /** RTC应用ID */
    private String appId = "023b95b1-701e-45bf-802d-0c3cbf5d3874";
    /** RTC应用密钥 */
    private String appKey = "";
}

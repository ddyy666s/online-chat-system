package com.chat.chat_backend.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "rtc")
public class RtcConfig {
    private String appId = "023b95b1-701e-45bf-802d-0c3cbf5d3874";
    private String appKey = "";
}

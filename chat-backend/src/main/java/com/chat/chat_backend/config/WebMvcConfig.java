package com.chat.chat_backend.config;

import com.chat.chat_backend.interceptor.JwtInterceptor;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Spring MVC配置类，注册JWT拦截器和CORS跨域设置
 * @author chat-backend
 * @since 2026-05-12
 */
@Configuration
@RequiredArgsConstructor
public class WebMvcConfig implements WebMvcConfigurer {

    /** JWT认证拦截器，用于请求鉴权 */
    private final JwtInterceptor jwtInterceptor;

    /**
     * 注册拦截器，拦截所有请求，排除注册、登录、健康检查和WebSocket路径
     * @param registry 拦截器注册表
     */
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(jwtInterceptor)
                .addPathPatterns("/**")
                .excludePathPatterns(
                        "/user/register",
                        "/user/login",
                        "/actuator/**",
                        "/ws/**"
                );
    }

    /**
     * 配置CORS跨域，允许所有来源、方法和请求头，支持凭证
     * @param registry CORS注册表
     */
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOriginPatterns("*")
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                .allowCredentials(true);
    }
}
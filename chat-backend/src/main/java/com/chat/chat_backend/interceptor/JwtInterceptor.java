package com.chat.chat_backend.interceptor;

import com.chat.chat_backend.common.result.ResultCode;
import com.chat.chat_backend.common.utils.JwtUtil;
import com.chat.chat_backend.common.constant.RedisConstants;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtInterceptor implements HandlerInterceptor {

    private final JwtUtil jwtUtil;
    private final RedisTemplate<String, Object> redisTemplate;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // 获取请求头中的token
        String token = request.getHeader("Authorization");

        log.info("请求路径: {}, Authorization头: {}", request.getRequestURI(), token);

        if (token == null || !token.startsWith("Bearer ")) {
            log.warn("Token缺失或格式错误");
            response.setStatus(401);
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write("{\"code\":1005,\"message\":\"" + ResultCode.UNAUTHORIZED.getMessage() + "\"}");
            return false;
        }

        token = token.substring(7);
        log.info("提取的token: {}", token);

        // 检查黑名单
        String blacklistKey = RedisConstants.TOKEN_BLACKLIST + token;
        if (Boolean.TRUE.equals(redisTemplate.hasKey(blacklistKey))) {
            log.warn("Token已被加入黑名单");
            response.setStatus(401);
            response.getWriter().write("{\"code\":1005,\"message\":\"Token已失效\"}");
            return false;
        }

        // 验证token
        if (!jwtUtil.validateToken(token)) {
            log.warn("Token验证失败");
            response.setStatus(401);
            response.getWriter().write("{\"code\":1005,\"message\":\"" + ResultCode.UNAUTHORIZED.getMessage() + "\"}");
            return false;
        }

        // 从token中获取用户信息
        Long userId = jwtUtil.getUserIdFromToken(token);
        String role = jwtUtil.getRoleFromToken(token);

        log.info("解析结果: userId={}, role={}", userId, role);

        if (userId == null) {
            log.warn("Token中未解析到userId");
            response.setStatus(401);
            response.getWriter().write("{\"code\":1005,\"message\":\"Token无效\"}");
            return false;
        }

        // 设置到request属性
        request.setAttribute("userId", userId);
        request.setAttribute("role", role);

        log.info("已设置userId到request: {}", request.getAttribute("userId"));

        return true;
    }
}
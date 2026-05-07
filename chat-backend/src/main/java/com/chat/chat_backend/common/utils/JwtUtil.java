package com.chat.chat_backend.common.utils;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Component
public class JwtUtil {

    @Value("${jwt.secret:aB3dE5fG7hI9jK1lM2nO4pQ6rS8tU0vW2xY4zA6cC8eE0gG2iI4kK6mM8oO0qQ}")
    private String secret;

    @Value("${jwt.expiration:86400000}")
    private Long expiration;

    private SecretKey getSigningKey() {
        byte[] keyBytes = secret.getBytes(StandardCharsets.UTF_8);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    public String generateToken(Long userId, String username, String role) {
        if (userId == null) {
            log.error("生成Token失败: userId为null");
            throw new IllegalArgumentException("userId不能为null");
        }

        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", userId);
        claims.put("username", username);
        claims.put("role", role);

        Date now = new Date();
        Date expireDate = new Date(now.getTime() + expiration);

        return Jwts.builder()
                .setClaims(claims)
                .setSubject(String.valueOf(userId))
                .setIssuedAt(now)
                .setExpiration(expireDate)
                .signWith(getSigningKey())
                .compact();
    }

    public Long getUserIdFromToken(String token) {
        Claims claims = parseToken(token);
        if (claims == null) return null;
        Object userIdObj = claims.get("userId");
        if (userIdObj == null) return null;
        if (userIdObj instanceof Long) return (Long) userIdObj;
        if (userIdObj instanceof Integer) return ((Integer) userIdObj).longValue();
        if (userIdObj instanceof String) return Long.parseLong((String) userIdObj);
        return null;
    }

    public String getUsernameFromToken(String token) {
        Claims claims = parseToken(token);
        return claims != null ? (String) claims.get("username") : null;
    }

    public String getRoleFromToken(String token) {
        Claims claims = parseToken(token);
        return claims != null ? (String) claims.get("role") : null;
    }

    public boolean validateToken(String token) {
        try {
            Claims claims = parseToken(token);
            return claims != null && !claims.getExpiration().before(new Date());
        } catch (Exception e) {
            log.warn("Token验证失败: {}", e.getMessage());
            return false;
        }
    }

    private Claims parseToken(String token) {
        try {
            return Jwts.parser()
                    .verifyWith(getSigningKey())
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
        } catch (Exception e) {
            log.warn("Token解析失败: {}", e.getMessage());
            return null;
        }
    }
}
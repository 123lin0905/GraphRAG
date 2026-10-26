package com.sanxingdui.twin.security;

import com.sanxingdui.twin.config.JwtProperties;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.List;

/**
 * JWT 工具类：签发与解析令牌（HS256）
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class JwtUtil {

    private final JwtProperties jwtProperties;

    private SecretKey key() {
        return Keys.hmacShaKeyFor(jwtProperties.getSecret().getBytes(StandardCharsets.UTF_8));
    }

    /**
     * 生成令牌
     *
     * @param userId   用户ID
     * @param username 用户名
     * @param roles    角色编码
     */
    public String generateToken(Long userId, String username, List<String> roles) {
        long now = System.currentTimeMillis();
        return Jwts.builder()
                .subject(username)
                .claim("uid", userId)
                .claim("roles", roles)
                .issuedAt(new Date(now))
                .expiration(new Date(now + jwtProperties.getExpireHours() * 3600_000L))
                .signWith(key())
                .compact();
    }

    /**
     * 解析令牌，失败返回 null
     */
    public Claims parse(String token) {
        try {
            return Jwts.parser().verifyWith(key()).build().parseSignedClaims(token).getPayload();
        } catch (JwtException | IllegalArgumentException e) {
            log.debug("JWT 解析失败：{}", e.getMessage());
            return null;
        }
    }

    public long getExpireSeconds() {
        return jwtProperties.getExpireHours() * 3600L;
    }
}

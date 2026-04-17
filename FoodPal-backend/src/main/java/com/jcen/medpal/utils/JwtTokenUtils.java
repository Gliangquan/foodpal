package com.jcen.medpal.utils;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * JWT Token 工具类
 *
 * @author <a href="https://github.com/Gliangquan">小梁</a>
 */
@Component
public class JwtTokenUtils {

    @Value("${jwt.secret:your-secret-key-must-be-at-least-256-bits-long-for-hs256-algorithm}")
    private String secret;

    @Value("${jwt.expiration:86400000}")
    private Long expiration;

    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(secret.getBytes());
    }

    /**
     * 生成 Token
     *
     * @param userId 用户 ID
     * @param userAccount 用户账号
     * @return Token
     */
    public String generateToken(Long userId, String userAccount) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", userId);
        claims.put("userAccount", userAccount);
        return createToken(claims, String.valueOf(userId));
    }

    /**
     * 创建 Token
     *
     * @param claims 声明
     * @param subject 主题
     * @return Token
     */
    private String createToken(Map<String, Object> claims, String subject) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + expiration);

        return Jwts.builder()
                .setClaims(claims)
                .setSubject(subject)
                .setIssuedAt(now)
                .setExpiration(expiryDate)
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    /**
     * 从 Token 中获取用户 ID
     *
     * @param token Token
     * @return 用户 ID
     */
    public Long getUserIdFromToken(String token) {
        Claims claims = getAllClaimsFromToken(token);
        Object userId = claims.get("userId");
        if (userId instanceof Integer) {
            return ((Integer) userId).longValue();
        }
        return (Long) userId;
    }

    /**
     * 从 Token 中获取用户账号
     *
     * @param token Token
     * @return 用户账号
     */
    public String getUserAccountFromToken(String token) {
        Claims claims = getAllClaimsFromToken(token);
        return (String) claims.get("userAccount");
    }

    /**
     * 验证 Token 是否有效
     *
     * @param token Token
     * @return 是否有效
     */
    public Boolean validateToken(String token) {
        try {
            Jwts.parserBuilder()
                    .setSigningKey(getSigningKey())
                    .build()
                    .parseClaimsJws(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 从 Token 中获取所有声明
     *
     * @param token Token
     * @return 声明
     */
    private Claims getAllClaimsFromToken(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    /**
     * 检查 Token 是否过期
     *
     * @param token Token
     * @return 是否过期
     */
    public Boolean isTokenExpired(String token) {
        try {
            Claims claims = getAllClaimsFromToken(token);
            return claims.getExpiration().before(new Date());
        } catch (Exception e) {
            return true;
        }
    }
}

package com.printclub.common.util;

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

/**
 * JWT 工具
 * <p>签发 / 解析 / 校验</p>
 *
 * <p>密钥从 application.yml 的 {@code jwt.secret} 读取，至少 32 位</p>
 *
 * @author D
 */
@Slf4j
@Component
public class JwtUtil {

    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.expiration}")
    private long expiration;

    private SecretKey getKey() {
        return Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * 生成 token
     *
     * @param studentId 学号（subject）
     * @param role      角色
     */
    public String generate(String studentId, Integer role) {
        Date now = new Date();
        Date expire = new Date(now.getTime() + expiration);

        Map<String, Object> claims = new HashMap<>();
        claims.put("role", role);

        return Jwts.builder()
                .subject(studentId)
                .claims(claims)
                .issuedAt(now)
                .expiration(expire)
                .signWith(getKey())
                .compact();
    }

    /**
     * 解析 token
     *
     * @return Claims（含 subject + role）
     * @throws io.jsonwebtoken.ExpiredJwtException     已过期
     * @throws io.jsonwebtoken.JwtException           非法 token
     */
    public Claims parse(String token) {
        return Jwts.parser()
                .verifyWith(getKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    /**
     * 校验 token 是否合法（不抛异常的版本）
     */
    public boolean isValid(String token) {
        try {
            parse(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}
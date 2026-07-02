package com.printclub.common.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
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
 * <p>✅ v2.12 修复（审查发现）：启动时若 secret 仍是占位符（dev-only-placeholder...）
 *    且当前是 prod profile → 直接抛 RuntimeException 拒启动，避免被 clone 仓库即可伪造 token</p>
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

    private final Environment env;

    public JwtUtil(Environment env) {
        this.env = env;
    }

    /**
     * ✅ v2.12：启动时校验 JWT secret
     * dev / 默认 profile：默认值可通过（仅 WARN 提醒）
     * prod profile：必须是真实强密钥，否则抛 RuntimeException 拒启动
     */
    @PostConstruct
    public void validateSecret() {
        if (secret == null || secret.length() < 32) {
            throw new IllegalStateException("jwt.secret 长度必须 >= 32 字符，当前：" + (secret == null ? 0 : secret.length()));
        }
        boolean isProd = java.util.Arrays.asList(env.getActiveProfiles()).contains("prod");
        if (secret.startsWith("dev-only-placeholder") || secret.startsWith("print-club-secret-key-please-change")) {
            if (isProd) {
                throw new IllegalStateException(
                    "🔴 生产环境禁止使用 jwt.secret 默认值！\n" +
                    "请通过环境变量 JWT_SECRET 设置强密钥（>= 32 字符）后再启动。\n" +
                    "例如：export JWT_SECRET=$(openssl rand -base64 48)");
            }
            log.warn("⚠️ 当前使用 jwt.secret 默认占位符，仅适合 dev 环境。prod 环境请设置环境变量 JWT_SECRET。");
        }
    }

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
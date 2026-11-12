package com.zsb.zsbserver.common;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Date;

/**
 * JWT 工具类（交给 Spring 管理，密钥从 application.yml 读取）
 *
 * 为什么不再用「静态方法 + 写死密钥」？
 * 1. 写死密钥所有人可见，等于没有签名；
 * 2. 静态方法拿不到配置文件，换密钥只能改代码；
 * 3. 密钥必须是固定值，否则服务重启后旧 token 全部失效
 *    （原代码 Keys.secretKeyFor 每次启动都生成新密钥）。
 */
@Component
public class JwtUtils {

    private final Key key;
    private final long expire;

    public JwtUtils(@Value("${zsb.jwt.secret}") String secret,
                    @Value("${zsb.jwt.expire}") long expire) {
        this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.expire = expire;
    }

    /** 生成 token：把用户 id 放进 subject */
    public String generateToken(Long uid) {
        Date now = new Date();
        return Jwts.builder()
                .setSubject(String.valueOf(uid))
                .setIssuedAt(now)
                .setExpiration(new Date(now.getTime() + expire))
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    /**
     * 解析 token 拿到 uid。
     * 签名不对 / 过期 / 格式错误都会抛 JwtException，由拦截器统一转成 401。
     */
    public Long parseUid(String token) throws JwtException {
        Claims claims = Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody();
        return Long.valueOf(claims.getSubject());
    }
}

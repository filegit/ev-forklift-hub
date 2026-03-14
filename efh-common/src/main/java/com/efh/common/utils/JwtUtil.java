package com.efh.common.utils;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * JWT 工具类
 * 用于生成和解析 JWT Token，实现用户认证
 * 
 * @author EFH Team
 */
public class JwtUtil {
    
    // JWT 签名密钥（固定密钥，确保重启后 Token 仍然有效）
    // 注意：生产环境应该从配置文件读取，并使用更复杂的密钥
    private static final String SECRET_STRING = "EFH-Forklift-Hub-Secret-Key-2024-Very-Long-Secret-String-For-JWT-Token-Generation";
    private static final Key SECRET_KEY = Keys.hmacShaKeyFor(SECRET_STRING.getBytes(StandardCharsets.UTF_8));
    
    // Token 过期时间：7天（单位：毫秒）
    private static final long EXPIRATION = 7 * 24 * 60 * 60 * 1000;
    
    /**
     * 生成 JWT Token
     * 
     * @param userId 用户ID
     * @param username 用户名
     * @return JWT Token 字符串
     */
    public static String generateToken(Long userId, String username) {
        // 创建自定义声明
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", userId);
        claims.put("username", username);
        
        // 构建 JWT Token
        return Jwts.builder()
                .setClaims(claims)              // 设置自定义声明
                .setSubject(username)           // 设置主题（用户名）
                .setIssuedAt(new Date())        // 设置签发时间
                .setExpiration(new Date(System.currentTimeMillis() + EXPIRATION))  // 设置过期时间
                .signWith(SECRET_KEY, SignatureAlgorithm.HS256)  // 使用密钥签名
                .compact();                     // 生成 Token
    }
    
    /**
     * 解析 JWT Token
     * 
     * @param token JWT Token 字符串
     * @return Claims 对象，包含 Token 中的所有声明
     * @throws io.jsonwebtoken.JwtException 如果 Token 无效或过期
     */
    public static Claims parseToken(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(SECRET_KEY)      // 设置签名密钥
                .build()
                .parseClaimsJws(token)          // 解析 Token
                .getBody();                     // 获取声明体
    }
    
    /**
     * 从 Token 中获取用户ID
     * 
     * @param token JWT Token 字符串
     * @return 用户ID
     */
    public static Long getUserId(String token) {
        Claims claims = parseToken(token);
        return claims.get("userId", Long.class);
    }
    
    /**
     * 从 Token 中获取用户名
     * 
     * @param token JWT Token 字符串
     * @return 用户名
     */
    public static String getUsername(String token) {
        Claims claims = parseToken(token);
        return claims.get("username", String.class);
    }
    
    /**
     * 验证 Token 是否过期
     * 
     * @param token JWT Token 字符串
     * @return true-已过期，false-未过期
     */
    public static boolean isTokenExpired(String token) {
        try {
            Claims claims = parseToken(token);
            // 比较过期时间和当前时间
            return claims.getExpiration().before(new Date());
        } catch (Exception e) {
            // Token 解析失败，视为已过期
            return true;
        }
    }
    
    /**
     * 验证 Token 是否有效
     * 
     * @param token JWT Token 字符串
     * @return true-有效，false-无效
     */
    public static boolean validateToken(String token) {
        try {
            parseToken(token);
            return !isTokenExpired(token);
        } catch (Exception e) {
            return false;
        }
    }
}

package com.kamesan.erpapi.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.stream.Collectors;

/**
 * JWT Token 提供者
 *
 * <p>負責 JWT Token 的產生、解析和驗證。</p>
 *
 * <h2>主要功能：</h2>
 * <ul>
 *   <li>產生 Access Token</li>
 *   <li>產生 Refresh Token</li>
 *   <li>解析 Token 取得使用者資訊</li>
 *   <li>驗證 Token 是否有效</li>
 * </ul>
 *
 * @author ERP System Team
 * @version 1.0.0
 */
@Slf4j
@Component
public class JwtTokenProvider {

    /**
     * JWT 密鑰
     */
    @Value("${app.jwt.secret}")
    private String jwtSecret;

    /**
     * Access Token 有效期限（毫秒）
     */
    @Value("${app.jwt.expiration}")
    private long jwtExpiration;

    /**
     * Refresh Token 有效期限（毫秒）
     */
    @Value("${app.jwt.refresh-expiration}")
    private long refreshExpiration;

    /**
     * 產生 Access Token
     *
     * @param authentication 認證資訊
     * @return JWT Token
     */
    public String generateToken(Authentication authentication) {
        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
        return generateToken(userPrincipal);
    }

    /**
     * 產生 Access Token
     *
     * @param userPrincipal 使用者主體
     * @return JWT Token
     */
    public String generateToken(UserPrincipal userPrincipal) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + jwtExpiration);

        // 取得角色權限
        String authorities = userPrincipal.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.joining(","));

        return Jwts.builder()
                .subject(userPrincipal.getId().toString())
                .claim("username", userPrincipal.getUsername())
                .claim("name", userPrincipal.getName())
                .claim("roles", authorities)
                .issuedAt(now)
                .expiration(expiryDate)
                .signWith(getSigningKey())
                .compact();
    }

    /**
     * 產生 Refresh Token
     *
     * @param userPrincipal 使用者主體
     * @return Refresh Token
     */
    public String generateRefreshToken(UserPrincipal userPrincipal) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + refreshExpiration);

        return Jwts.builder()
                .subject(userPrincipal.getId().toString())
                .issuedAt(now)
                .expiration(expiryDate)
                .signWith(getSigningKey())
                .compact();
    }

    /**
     * 從 Token 取得使用者 ID
     *
     * @param token JWT Token
     * @return 使用者 ID
     */
    public Long getUserIdFromToken(String token) {
        Claims claims = parseToken(token);
        return Long.parseLong(claims.getSubject());
    }

    /**
     * 從 Token 取得使用者名稱
     *
     * @param token JWT Token
     * @return 使用者名稱
     */
    public String getUsernameFromToken(String token) {
        Claims claims = parseToken(token);
        return claims.get("username", String.class);
    }

    /**
     * 驗證 Token 是否有效
     *
     * @param token JWT Token
     * @return 是否有效
     */
    public boolean validateToken(String token) {
        try {
            parseToken(token);
            return true;
        } catch (MalformedJwtException ex) {
            log.error("無效的 JWT Token: {}", ex.getMessage());
        } catch (ExpiredJwtException ex) {
            log.error("JWT Token 已過期: {}", ex.getMessage());
        } catch (UnsupportedJwtException ex) {
            log.error("不支援的 JWT Token: {}", ex.getMessage());
        } catch (IllegalArgumentException ex) {
            log.error("JWT Token 為空: {}", ex.getMessage());
        } catch (SecurityException ex) {
            log.error("JWT Token 簽名錯誤: {}", ex.getMessage());
        }
        return false;
    }

    /**
     * 解析 Token
     *
     * @param token JWT Token
     * @return Claims
     */
    private Claims parseToken(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    /**
     * 取得簽名金鑰
     *
     * @return SecretKey
     */
    private SecretKey getSigningKey() {
        // 嘗試 Base64 解碼，如果失敗則使用 UTF-8 編碼
        byte[] keyBytes;
        try {
            keyBytes = Decoders.BASE64.decode(jwtSecret);
        } catch (Exception e) {
            // 如果不是有效的 Base64，則直接使用 UTF-8 編碼
            keyBytes = jwtSecret.getBytes(java.nio.charset.StandardCharsets.UTF_8);
        }
        return Keys.hmacShaKeyFor(keyBytes);
    }

    /**
     * 取得 Token 過期時間（毫秒）
     *
     * @return 過期時間
     */
    public long getExpiration() {
        return jwtExpiration;
    }

    /**
     * 取得 Refresh Token 過期時間（毫秒）
     *
     * @return 過期時間
     */
    public long getRefreshExpiration() {
        return refreshExpiration;
    }
}

package com.kamesan.erpapi.security.redis;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.concurrent.TimeUnit;

/**
 * JWT Token 黑名單服務
 *
 * <p>使用 Redis 儲存被撤銷的 JWT Token，實現登出功能。</p>
 *
 * <h2>Redis Key 格式：</h2>
 * <ul>
 *   <li>token:blacklist:{jti} - 單一 Token 黑名單</li>
 *   <li>token:user:{userId}:revoked_at - 使用者 Token 撤銷時間戳</li>
 * </ul>
 *
 * <h2>主要功能：</h2>
 * <ul>
 *   <li>將 Token 加入黑名單</li>
 *   <li>檢查 Token 是否在黑名單中</li>
 *   <li>撤銷使用者所有 Token（強制登出所有裝置）</li>
 * </ul>
 *
 * @author ERP System Team
 * @version 1.0.0
 */
@Slf4j
@Service
public class TokenBlacklistService {

    private static final String BLACKLIST_PREFIX = "token:blacklist:";
    private static final String USER_REVOKED_PREFIX = "token:user:";
    private static final String REVOKED_AT_SUFFIX = ":revoked_at";

    private final RedisTemplate<String, Object> redisTemplate;

    @Value("${app.jwt.secret}")
    private String jwtSecret;

    @Value("${app.jwt.expiration}")
    private long jwtExpiration;

    public TokenBlacklistService(RedisTemplate<String, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    /**
     * 將 Token 加入黑名單
     *
     * @param token JWT Token
     * @return 是否成功
     */
    public boolean addToBlacklist(String token) {
        try {
            Claims claims = parseToken(token);
            String tokenId = getTokenId(claims);
            Date expiration = claims.getExpiration();

            // 計算剩餘有效時間
            long ttlMillis = expiration.getTime() - System.currentTimeMillis();
            if (ttlMillis <= 0) {
                // Token 已過期，無需加入黑名單
                log.debug("Token 已過期，無需加入黑名單");
                return true;
            }

            // 將 Token ID 加入黑名單，TTL 設為 Token 剩餘有效時間
            String key = BLACKLIST_PREFIX + tokenId;
            redisTemplate.opsForValue().set(key, claims.getSubject(), ttlMillis, TimeUnit.MILLISECONDS);

            log.info("Token 已加入黑名單，使用者ID: {}, 將於 {} 毫秒後自動過期", claims.getSubject(), ttlMillis);
            return true;
        } catch (Exception e) {
            log.error("將 Token 加入黑名單失敗: {}", e.getMessage());
            return false;
        }
    }

    /**
     * 檢查 Token 是否在黑名單中
     *
     * @param token JWT Token
     * @return 是否在黑名單中
     */
    public boolean isBlacklisted(String token) {
        try {
            Claims claims = parseToken(token);
            String tokenId = getTokenId(claims);
            String userId = claims.getSubject();
            Date issuedAt = claims.getIssuedAt();

            // 1. 檢查單一 Token 黑名單
            String blacklistKey = BLACKLIST_PREFIX + tokenId;
            Boolean exists = redisTemplate.hasKey(blacklistKey);
            if (Boolean.TRUE.equals(exists)) {
                log.debug("Token {} 在黑名單中", tokenId);
                return true;
            }

            // 2. 檢查使用者級別撤銷（所有裝置登出）
            String userRevokedKey = USER_REVOKED_PREFIX + userId + REVOKED_AT_SUFFIX;
            Object revokedAtObj = redisTemplate.opsForValue().get(userRevokedKey);
            if (revokedAtObj != null) {
                long revokedAt = Long.parseLong(revokedAtObj.toString());
                if (issuedAt.getTime() < revokedAt) {
                    log.debug("Token 在使用者撤銷時間之前簽發，視為無效");
                    return true;
                }
            }

            return false;
        } catch (Exception e) {
            log.error("檢查 Token 黑名單失敗: {}", e.getMessage());
            // 發生錯誤時，為了安全考量，視為在黑名單中
            return true;
        }
    }

    /**
     * 撤銷使用者所有 Token（強制登出所有裝置）
     *
     * @param userId 使用者 ID
     * @return 是否成功
     */
    public boolean revokeAllUserTokens(Long userId) {
        try {
            String key = USER_REVOKED_PREFIX + userId + REVOKED_AT_SUFFIX;
            long revokedAt = System.currentTimeMillis();

            // 設定撤銷時間戳，TTL 為 Token 最大有效時間（確保舊 Token 過期後自動清除）
            redisTemplate.opsForValue().set(key, revokedAt, jwtExpiration, TimeUnit.MILLISECONDS);

            log.info("已撤銷使用者 {} 所有 Token", userId);
            return true;
        } catch (Exception e) {
            log.error("撤銷使用者 Token 失敗: {}", e.getMessage());
            return false;
        }
    }

    /**
     * 取得黑名單中的 Token 數量（用於監控）
     *
     * @return Token 數量
     */
    public long getBlacklistCount() {
        try {
            var keys = redisTemplate.keys(BLACKLIST_PREFIX + "*");
            return keys != null ? keys.size() : 0;
        } catch (Exception e) {
            log.error("取得黑名單數量失敗: {}", e.getMessage());
            return 0;
        }
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
     * 取得 Token ID（使用 Token 的雜湊值）
     *
     * @param claims Token Claims
     * @return Token ID
     */
    private String getTokenId(Claims claims) {
        // 使用 subject + issuedAt 作為唯一識別
        return claims.getSubject() + ":" + claims.getIssuedAt().getTime();
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
}

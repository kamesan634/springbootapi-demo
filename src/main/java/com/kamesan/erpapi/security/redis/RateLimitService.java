package com.kamesan.erpapi.security.redis;

import lombok.Builder;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

/**
 * API 限流服務
 *
 * <p>使用 Redis 實現 API 請求限流，防止濫用和 DDoS 攻擊。</p>
 *
 * <h2>Redis Key 格式：</h2>
 * <ul>
 *   <li>ratelimit:ip:{ip}:{endpoint} - IP 級別限流</li>
 *   <li>ratelimit:user:{userId}:{endpoint} - 使用者級別限流</li>
 *   <li>ratelimit:global:{endpoint} - 全局限流</li>
 * </ul>
 *
 * <h2>限流策略：</h2>
 * <ul>
 *   <li>固定窗口計數器 (Fixed Window Counter)</li>
 *   <li>支援 IP、使用者、全局三種限流維度</li>
 *   <li>可針對不同 API 設定不同限流規則</li>
 * </ul>
 *
 * @author ERP System Team
 * @version 1.0.0
 */
@Slf4j
@Service
public class RateLimitService {

    private static final String RATE_LIMIT_IP_PREFIX = "ratelimit:ip:";
    private static final String RATE_LIMIT_USER_PREFIX = "ratelimit:user:";
    private static final String RATE_LIMIT_GLOBAL_PREFIX = "ratelimit:global:";

    private final RedisTemplate<String, Object> redisTemplate;

    public RateLimitService(RedisTemplate<String, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    /**
     * 檢查 IP 是否超過限流
     *
     * @param ip       客戶端 IP
     * @param endpoint API 端點
     * @param limit    限制次數
     * @param windowSeconds 時間窗口（秒）
     * @return 限流檢查結果
     */
    public RateLimitResult checkIpRateLimit(String ip, String endpoint, int limit, int windowSeconds) {
        String key = RATE_LIMIT_IP_PREFIX + ip + ":" + endpoint;
        return checkRateLimit(key, limit, windowSeconds);
    }

    /**
     * 檢查使用者是否超過限流
     *
     * @param userId   使用者 ID
     * @param endpoint API 端點
     * @param limit    限制次數
     * @param windowSeconds 時間窗口（秒）
     * @return 限流檢查結果
     */
    public RateLimitResult checkUserRateLimit(Long userId, String endpoint, int limit, int windowSeconds) {
        String key = RATE_LIMIT_USER_PREFIX + userId + ":" + endpoint;
        return checkRateLimit(key, limit, windowSeconds);
    }

    /**
     * 檢查全局是否超過限流
     *
     * @param endpoint API 端點
     * @param limit    限制次數
     * @param windowSeconds 時間窗口（秒）
     * @return 限流檢查結果
     */
    public RateLimitResult checkGlobalRateLimit(String endpoint, int limit, int windowSeconds) {
        String key = RATE_LIMIT_GLOBAL_PREFIX + endpoint;
        return checkRateLimit(key, limit, windowSeconds);
    }

    /**
     * 執行限流檢查
     *
     * @param key           Redis Key
     * @param limit         限制次數
     * @param windowSeconds 時間窗口（秒）
     * @return 限流檢查結果
     */
    private RateLimitResult checkRateLimit(String key, int limit, int windowSeconds) {
        try {
            // 遞增計數器
            Long count = redisTemplate.opsForValue().increment(key);
            if (count == null) {
                count = 1L;
            }

            // 如果是第一次請求，設定過期時間
            if (count == 1) {
                redisTemplate.expire(key, windowSeconds, TimeUnit.SECONDS);
            }

            // 取得剩餘時間
            Long ttl = redisTemplate.getExpire(key, TimeUnit.SECONDS);
            if (ttl == null || ttl < 0) {
                ttl = (long) windowSeconds;
            }

            // 計算剩餘配額
            long remaining = Math.max(0, limit - count);
            boolean allowed = count <= limit;

            if (!allowed) {
                log.warn("限流觸發: key={}, count={}, limit={}", key, count, limit);
            }

            return RateLimitResult.builder()
                    .allowed(allowed)
                    .limit(limit)
                    .remaining(remaining)
                    .resetSeconds(ttl)
                    .currentCount(count)
                    .build();
        } catch (Exception e) {
            log.error("限流檢查失敗: {}", e.getMessage());
            // 發生錯誤時，預設放行（避免影響正常業務）
            return RateLimitResult.builder()
                    .allowed(true)
                    .limit(limit)
                    .remaining(limit)
                    .resetSeconds(windowSeconds)
                    .currentCount(0L)
                    .build();
        }
    }

    /**
     * 重置限流計數器
     *
     * @param ip       客戶端 IP
     * @param endpoint API 端點
     */
    public void resetIpRateLimit(String ip, String endpoint) {
        String key = RATE_LIMIT_IP_PREFIX + ip + ":" + endpoint;
        redisTemplate.delete(key);
        log.info("已重置 IP 限流: ip={}, endpoint={}", ip, endpoint);
    }

    /**
     * 重置使用者限流計數器
     *
     * @param userId   使用者 ID
     * @param endpoint API 端點
     */
    public void resetUserRateLimit(Long userId, String endpoint) {
        String key = RATE_LIMIT_USER_PREFIX + userId + ":" + endpoint;
        redisTemplate.delete(key);
        log.info("已重置使用者限流: userId={}, endpoint={}", userId, endpoint);
    }

    /**
     * 取得 IP 目前的請求次數
     *
     * @param ip       客戶端 IP
     * @param endpoint API 端點
     * @return 目前請求次數
     */
    public long getIpCurrentCount(String ip, String endpoint) {
        String key = RATE_LIMIT_IP_PREFIX + ip + ":" + endpoint;
        Object count = redisTemplate.opsForValue().get(key);
        return count != null ? Long.parseLong(count.toString()) : 0;
    }

    /**
     * 限流結果
     */
    @Data
    @Builder
    public static class RateLimitResult {
        /**
         * 是否允許請求
         */
        private boolean allowed;

        /**
         * 限制次數
         */
        private int limit;

        /**
         * 剩餘配額
         */
        private long remaining;

        /**
         * 重置時間（秒）
         */
        private long resetSeconds;

        /**
         * 目前計數
         */
        private long currentCount;
    }
}

package com.kamesan.erpapi.security.redis;

import lombok.Builder;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 快取服務
 *
 * <p>提供熱門資料快取功能，並追蹤快取命中率。</p>
 *
 * <h2>Redis Key 格式：</h2>
 * <ul>
 *   <li>cache:product:{productId} - 商品快取</li>
 *   <li>cache:category:{categoryId} - 分類快取</li>
 *   <li>cache:product:hot - 熱門商品快取（List）</li>
 *   <li>cache:stats:hits - 快取命中統計</li>
 *   <li>cache:stats:misses - 快取未命中統計</li>
 * </ul>
 *
 * @author ERP System Team
 * @version 1.0.0
 */
@Slf4j
@Service
public class CacheService {

    private static final String CACHE_PREFIX = "cache:";
    private static final String STATS_HITS = "cache:stats:hits";
    private static final String STATS_MISSES = "cache:stats:misses";
    private static final String STATS_HITS_BY_KEY = "cache:stats:hits:";
    private static final String STATS_MISSES_BY_KEY = "cache:stats:misses:";

    /**
     * 本地快取命中計數器（用於高頻統計）
     */
    private final AtomicLong localHits = new AtomicLong(0);
    private final AtomicLong localMisses = new AtomicLong(0);

    private final RedisTemplate<String, Object> redisTemplate;

    public CacheService(RedisTemplate<String, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    /**
     * 取得快取
     *
     * @param key 快取 Key
     * @param <T> 資料類型
     * @return 快取資料（可能為 null）
     */
    @SuppressWarnings("unchecked")
    public <T> T get(String key) {
        try {
            String fullKey = CACHE_PREFIX + key;
            Object value = redisTemplate.opsForValue().get(fullKey);

            if (value != null) {
                recordHit(key);
                log.debug("快取命中: {}", key);
                return (T) value;
            } else {
                recordMiss(key);
                log.debug("快取未命中: {}", key);
                return null;
            }
        } catch (Exception e) {
            log.error("取得快取失敗: key={}, error={}", key, e.getMessage());
            recordMiss(key);
            return null;
        }
    }

    /**
     * 設定快取
     *
     * @param key     快取 Key
     * @param value   快取值
     * @param ttl     過期時間
     * @param unit    時間單位
     */
    public void set(String key, Object value, long ttl, TimeUnit unit) {
        try {
            String fullKey = CACHE_PREFIX + key;
            redisTemplate.opsForValue().set(fullKey, value, ttl, unit);
            log.debug("設定快取: key={}, ttl={}ms", key, unit.toMillis(ttl));
        } catch (Exception e) {
            log.error("設定快取失敗: key={}, error={}", key, e.getMessage());
        }
    }

    /**
     * 設定快取（使用預設 TTL）
     *
     * @param key   快取 Key
     * @param value 快取值
     */
    public void set(String key, Object value) {
        set(key, value, 30, TimeUnit.MINUTES);
    }

    /**
     * 刪除快取
     *
     * @param key 快取 Key
     */
    public void delete(String key) {
        try {
            String fullKey = CACHE_PREFIX + key;
            redisTemplate.delete(fullKey);
            log.debug("刪除快取: {}", key);
        } catch (Exception e) {
            log.error("刪除快取失敗: key={}, error={}", key, e.getMessage());
        }
    }

    /**
     * 批量刪除快取（支援萬用字元）
     *
     * @param pattern 快取 Key 模式
     */
    public void deleteByPattern(String pattern) {
        try {
            String fullPattern = CACHE_PREFIX + pattern;
            Set<String> keys = redisTemplate.keys(fullPattern);
            if (keys != null && !keys.isEmpty()) {
                redisTemplate.delete(keys);
                log.info("批量刪除快取: pattern={}, count={}", pattern, keys.size());
            }
        } catch (Exception e) {
            log.error("批量刪除快取失敗: pattern={}, error={}", pattern, e.getMessage());
        }
    }

    /**
     * 檢查快取是否存在
     *
     * @param key 快取 Key
     * @return 是否存在
     */
    public boolean exists(String key) {
        try {
            String fullKey = CACHE_PREFIX + key;
            return Boolean.TRUE.equals(redisTemplate.hasKey(fullKey));
        } catch (Exception e) {
            log.error("檢查快取存在失敗: key={}, error={}", key, e.getMessage());
            return false;
        }
    }

    /**
     * 取得或設定快取
     *
     * @param key      快取 Key
     * @param loader   資料載入函數
     * @param ttl      過期時間
     * @param unit     時間單位
     * @param <T>      資料類型
     * @return 快取或載入的資料
     */
    public <T> T getOrSet(String key, CacheLoader<T> loader, long ttl, TimeUnit unit) {
        T value = get(key);
        if (value != null) {
            return value;
        }

        // 快取未命中，從資料庫載入
        try {
            value = loader.load();
            if (value != null) {
                set(key, value, ttl, unit);
            }
            return value;
        } catch (Exception e) {
            log.error("載入快取資料失敗: key={}, error={}", key, e.getMessage());
            return null;
        }
    }

    /**
     * 記錄快取命中
     */
    private void recordHit(String key) {
        try {
            localHits.incrementAndGet();
            redisTemplate.opsForValue().increment(STATS_HITS);
            redisTemplate.opsForValue().increment(STATS_HITS_BY_KEY + normalizeKey(key));
        } catch (Exception e) {
            // 統計失敗不影響業務
            log.trace("記錄快取命中統計失敗: {}", e.getMessage());
        }
    }

    /**
     * 記錄快取未命中
     */
    private void recordMiss(String key) {
        try {
            localMisses.incrementAndGet();
            redisTemplate.opsForValue().increment(STATS_MISSES);
            redisTemplate.opsForValue().increment(STATS_MISSES_BY_KEY + normalizeKey(key));
        } catch (Exception e) {
            // 統計失敗不影響業務
            log.trace("記錄快取未命中統計失敗: {}", e.getMessage());
        }
    }

    /**
     * 正規化 Key（用於統計）
     */
    private String normalizeKey(String key) {
        // 移除數字 ID，只保留類型
        return key.replaceAll(":\\d+", "");
    }

    /**
     * 取得快取統計資訊
     *
     * @return 統計資訊
     */
    public CacheStats getStats() {
        try {
            Object hitsObj = redisTemplate.opsForValue().get(STATS_HITS);
            Object missesObj = redisTemplate.opsForValue().get(STATS_MISSES);

            long hits = hitsObj != null ? Long.parseLong(hitsObj.toString()) : 0;
            long misses = missesObj != null ? Long.parseLong(missesObj.toString()) : 0;
            long total = hits + misses;
            double hitRate = total > 0 ? (double) hits / total * 100 : 0;

            return CacheStats.builder()
                    .hits(hits)
                    .misses(misses)
                    .total(total)
                    .hitRate(Math.round(hitRate * 100.0) / 100.0)
                    .localHits(localHits.get())
                    .localMisses(localMisses.get())
                    .build();
        } catch (Exception e) {
            log.error("取得快取統計失敗: {}", e.getMessage());
            return CacheStats.builder()
                    .hits(0)
                    .misses(0)
                    .total(0)
                    .hitRate(0)
                    .localHits(localHits.get())
                    .localMisses(localMisses.get())
                    .build();
        }
    }

    /**
     * 重置快取統計
     */
    public void resetStats() {
        try {
            redisTemplate.delete(STATS_HITS);
            redisTemplate.delete(STATS_MISSES);

            // 刪除所有分類統計
            Set<String> hitKeys = redisTemplate.keys(STATS_HITS_BY_KEY + "*");
            Set<String> missKeys = redisTemplate.keys(STATS_MISSES_BY_KEY + "*");
            if (hitKeys != null && !hitKeys.isEmpty()) {
                redisTemplate.delete(hitKeys);
            }
            if (missKeys != null && !missKeys.isEmpty()) {
                redisTemplate.delete(missKeys);
            }

            localHits.set(0);
            localMisses.set(0);

            log.info("已重置快取統計");
        } catch (Exception e) {
            log.error("重置快取統計失敗: {}", e.getMessage());
        }
    }

    /**
     * 快取載入函數介面
     */
    @FunctionalInterface
    public interface CacheLoader<T> {
        T load() throws Exception;
    }

    /**
     * 快取統計資訊
     */
    @Data
    @Builder
    public static class CacheStats {
        private long hits;
        private long misses;
        private long total;
        private double hitRate;
        private long localHits;
        private long localMisses;
    }
}

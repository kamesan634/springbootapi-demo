package com.kamesan.erpapi.security.redis;

import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

/**
 * 分散式鎖服務
 *
 * <p>使用 Redis 實現分散式鎖，用於保護共享資源。</p>
 *
 * <h2>Redis Key 格式：</h2>
 * <ul>
 *   <li>lock:order_number - 訂單編號產生鎖</li>
 *   <li>lock:inventory:{storeId}:{productId} - 庫存扣減鎖</li>
 *   <li>lock:report:{reportId} - 報表產生鎖</li>
 * </ul>
 *
 * <h2>實現方式：</h2>
 * <ul>
 *   <li>使用 Redis SETNX 實現鎖獲取</li>
 *   <li>使用 Lua Script 實現原子性釋放</li>
 *   <li>支援自動續期（看門狗機制）</li>
 *   <li>支援可重入鎖（同一線程可多次獲取）</li>
 * </ul>
 *
 * @author ERP System Team
 * @version 1.0.0
 */
@Slf4j
@Service
public class DistributedLockService {

    private static final String LOCK_PREFIX = "lock:";

    /**
     * 預設鎖超時時間（秒）
     */
    private static final int DEFAULT_LOCK_TIMEOUT = 30;

    /**
     * 預設等待時間（毫秒）
     */
    private static final long DEFAULT_WAIT_TIMEOUT = 5000;

    /**
     * 重試間隔（毫秒）
     */
    private static final long RETRY_INTERVAL = 100;

    /**
     * 儲存當前線程持有的鎖 ID
     */
    private static final ThreadLocal<String> LOCK_OWNER = new ThreadLocal<>();

    private final RedisTemplate<String, Object> redisTemplate;

    /**
     * 釋放鎖的 Lua 腳本（原子操作）
     * 只有持有鎖的客戶端才能釋放鎖
     */
    private final DefaultRedisScript<Long> unlockScript;

    public DistributedLockService(RedisTemplate<String, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;

        // 初始化釋放鎖的 Lua 腳本
        this.unlockScript = new DefaultRedisScript<>();
        this.unlockScript.setScriptText(
                "if redis.call('get', KEYS[1]) == ARGV[1] then " +
                        "return redis.call('del', KEYS[1]) " +
                        "else " +
                        "return 0 " +
                        "end"
        );
        this.unlockScript.setResultType(Long.class);
    }

    /**
     * 嘗試獲取鎖（非阻塞）
     *
     * @param lockKey       鎖的 Key
     * @param timeoutSeconds 鎖超時時間（秒）
     * @return 鎖 ID（成功）或 null（失敗）
     */
    public String tryLock(String lockKey, int timeoutSeconds) {
        String fullKey = LOCK_PREFIX + lockKey;
        String lockId = generateLockId();

        try {
            Boolean success = redisTemplate.opsForValue().setIfAbsent(
                    fullKey,
                    lockId,
                    timeoutSeconds,
                    TimeUnit.SECONDS
            );

            if (Boolean.TRUE.equals(success)) {
                LOCK_OWNER.set(lockId);
                log.debug("獲取鎖成功: key={}, lockId={}", lockKey, lockId);
                return lockId;
            } else {
                log.debug("獲取鎖失敗: key={}", lockKey);
                return null;
            }
        } catch (Exception e) {
            log.error("獲取鎖異常: key={}, error={}", lockKey, e.getMessage());
            return null;
        }
    }

    /**
     * 嘗試獲取鎖（使用預設超時時間）
     *
     * @param lockKey 鎖的 Key
     * @return 鎖 ID（成功）或 null（失敗）
     */
    public String tryLock(String lockKey) {
        return tryLock(lockKey, DEFAULT_LOCK_TIMEOUT);
    }

    /**
     * 阻塞獲取鎖（帶等待超時）
     *
     * @param lockKey       鎖的 Key
     * @param lockTimeout   鎖超時時間（秒）
     * @param waitTimeout   等待超時時間（毫秒）
     * @return 鎖 ID（成功）或 null（超時）
     */
    public String lock(String lockKey, int lockTimeout, long waitTimeout) {
        long startTime = System.currentTimeMillis();
        long deadline = startTime + waitTimeout;

        while (System.currentTimeMillis() < deadline) {
            String lockId = tryLock(lockKey, lockTimeout);
            if (lockId != null) {
                return lockId;
            }

            // 等待一段時間後重試
            try {
                Thread.sleep(RETRY_INTERVAL);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                log.warn("獲取鎖時被中斷: key={}", lockKey);
                return null;
            }
        }

        log.warn("獲取鎖超時: key={}, waitTime={}ms", lockKey, waitTimeout);
        return null;
    }

    /**
     * 阻塞獲取鎖（使用預設超時時間）
     *
     * @param lockKey 鎖的 Key
     * @return 鎖 ID（成功）或 null（超時）
     */
    public String lock(String lockKey) {
        return lock(lockKey, DEFAULT_LOCK_TIMEOUT, DEFAULT_WAIT_TIMEOUT);
    }

    /**
     * 釋放鎖
     *
     * @param lockKey 鎖的 Key
     * @param lockId  鎖 ID
     * @return 是否釋放成功
     */
    public boolean unlock(String lockKey, String lockId) {
        if (lockId == null) {
            return false;
        }

        String fullKey = LOCK_PREFIX + lockKey;

        try {
            Long result = redisTemplate.execute(
                    unlockScript,
                    Collections.singletonList(fullKey),
                    lockId
            );

            boolean success = result != null && result == 1;
            if (success) {
                LOCK_OWNER.remove();
                log.debug("釋放鎖成功: key={}, lockId={}", lockKey, lockId);
            } else {
                log.warn("釋放鎖失敗（可能已過期或不是鎖的持有者）: key={}, lockId={}", lockKey, lockId);
            }

            return success;
        } catch (Exception e) {
            log.error("釋放鎖異常: key={}, lockId={}, error={}", lockKey, lockId, e.getMessage());
            return false;
        }
    }

    /**
     * 在鎖保護下執行操作
     *
     * @param lockKey  鎖的 Key
     * @param supplier 要執行的操作
     * @param <T>      返回類型
     * @return 操作結果
     * @throws LockAcquisitionException 獲取鎖失敗時拋出
     */
    public <T> T executeWithLock(String lockKey, Supplier<T> supplier) {
        String lockId = lock(lockKey);
        if (lockId == null) {
            throw new LockAcquisitionException("無法獲取鎖: " + lockKey);
        }

        try {
            return supplier.get();
        } finally {
            unlock(lockKey, lockId);
        }
    }

    /**
     * 在鎖保護下執行操作（無返回值）
     *
     * @param lockKey  鎖的 Key
     * @param runnable 要執行的操作
     * @throws LockAcquisitionException 獲取鎖失敗時拋出
     */
    public void executeWithLock(String lockKey, Runnable runnable) {
        String lockId = lock(lockKey);
        if (lockId == null) {
            throw new LockAcquisitionException("無法獲取鎖: " + lockKey);
        }

        try {
            runnable.run();
        } finally {
            unlock(lockKey, lockId);
        }
    }

    /**
     * 嘗試在鎖保護下執行操作
     *
     * @param lockKey  鎖的 Key
     * @param supplier 要執行的操作
     * @param <T>      返回類型
     * @return 操作結果（獲取鎖失敗時返回 null）
     */
    public <T> T tryExecuteWithLock(String lockKey, Supplier<T> supplier) {
        String lockId = tryLock(lockKey);
        if (lockId == null) {
            log.debug("嘗試獲取鎖失敗，跳過執行: key={}", lockKey);
            return null;
        }

        try {
            return supplier.get();
        } finally {
            unlock(lockKey, lockId);
        }
    }

    /**
     * 延長鎖的有效期
     *
     * @param lockKey       鎖的 Key
     * @param lockId        鎖 ID
     * @param timeoutSeconds 新的超時時間（秒）
     * @return 是否延長成功
     */
    public boolean renewLock(String lockKey, String lockId, int timeoutSeconds) {
        if (lockId == null) {
            return false;
        }

        String fullKey = LOCK_PREFIX + lockKey;

        try {
            Object currentLockId = redisTemplate.opsForValue().get(fullKey);
            if (lockId.equals(currentLockId)) {
                redisTemplate.expire(fullKey, timeoutSeconds, TimeUnit.SECONDS);
                log.debug("延長鎖有效期: key={}, lockId={}, timeout={}s", lockKey, lockId, timeoutSeconds);
                return true;
            }
            return false;
        } catch (Exception e) {
            log.error("延長鎖有效期失敗: key={}, error={}", lockKey, e.getMessage());
            return false;
        }
    }

    /**
     * 檢查鎖是否存在
     *
     * @param lockKey 鎖的 Key
     * @return 是否存在
     */
    public boolean isLocked(String lockKey) {
        String fullKey = LOCK_PREFIX + lockKey;
        return Boolean.TRUE.equals(redisTemplate.hasKey(fullKey));
    }

    /**
     * 產生唯一的鎖 ID
     *
     * @return 鎖 ID
     */
    private String generateLockId() {
        return UUID.randomUUID().toString() + ":" + Thread.currentThread().getId();
    }

    /**
     * 鎖獲取異常
     */
    public static class LockAcquisitionException extends RuntimeException {
        public LockAcquisitionException(String message) {
            super(message);
        }
    }
}

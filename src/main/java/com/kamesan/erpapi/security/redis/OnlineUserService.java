package com.kamesan.erpapi.security.redis;

import lombok.Builder;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * 使用者在線狀態服務
 *
 * <p>使用 Redis 追蹤使用者的在線狀態，支援以下功能：</p>
 *
 * <h2>Redis Key 格式：</h2>
 * <ul>
 *   <li>online:users - 在線使用者集合（Sorted Set，score 為最後活動時間）</li>
 *   <li>online:user:{userId} - 使用者在線詳細資訊（Hash）</li>
 *   <li>online:count:total - 目前在線人數（String）</li>
 *   <li>online:count:store:{storeId} - 各門市在線人數（String）</li>
 * </ul>
 *
 * <h2>主要功能：</h2>
 * <ul>
 *   <li>記錄使用者上線</li>
 *   <li>記錄使用者下線</li>
 *   <li>更新使用者最後活動時間（心跳）</li>
 *   <li>取得在線使用者列表</li>
 *   <li>取得在線人數統計</li>
 * </ul>
 *
 * @author ERP System Team
 * @version 1.0.0
 */
@Slf4j
@Service
public class OnlineUserService {

    private static final String ONLINE_USERS_KEY = "online:users";
    private static final String ONLINE_USER_PREFIX = "online:user:";
    private static final String ONLINE_COUNT_TOTAL = "online:count:total";
    private static final String ONLINE_COUNT_STORE_PREFIX = "online:count:store:";

    /**
     * 使用者在線超時時間（分鐘）
     */
    private static final int ONLINE_TIMEOUT_MINUTES = 5;

    /**
     * 使用者詳細資訊 TTL（分鐘）
     */
    private static final int USER_INFO_TTL_MINUTES = 30;

    private final RedisTemplate<String, Object> redisTemplate;

    public OnlineUserService(RedisTemplate<String, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    /**
     * 記錄使用者上線
     *
     * @param userId   使用者 ID
     * @param username 使用者名稱
     * @param name     使用者姓名
     * @param storeId  門市 ID
     * @param ip       登入 IP
     */
    public void userOnline(Long userId, String username, String name, Long storeId, String ip) {
        try {
            long now = System.currentTimeMillis();
            String userKey = ONLINE_USER_PREFIX + userId;

            // 1. 將使用者加入在線集合（Sorted Set）
            redisTemplate.opsForZSet().add(ONLINE_USERS_KEY, userId.toString(), now);

            // 2. 儲存使用者詳細資訊（Hash）
            Map<String, Object> userInfo = new HashMap<>();
            userInfo.put("userId", userId);
            userInfo.put("username", username);
            userInfo.put("name", name);
            userInfo.put("storeId", storeId != null ? storeId : 0);
            userInfo.put("ip", ip);
            userInfo.put("loginTime", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
            userInfo.put("lastActiveTime", now);

            redisTemplate.opsForHash().putAll(userKey, userInfo);
            redisTemplate.expire(userKey, USER_INFO_TTL_MINUTES, TimeUnit.MINUTES);

            // 3. 更新在線人數統計
            updateOnlineCount();

            log.info("使用者 {} ({}) 上線，IP: {}", username, name, ip);
        } catch (Exception e) {
            log.error("記錄使用者上線失敗: {}", e.getMessage());
        }
    }

    /**
     * 記錄使用者下線
     *
     * @param userId 使用者 ID
     */
    public void userOffline(Long userId) {
        try {
            String userKey = ONLINE_USER_PREFIX + userId;

            // 1. 從在線集合移除
            redisTemplate.opsForZSet().remove(ONLINE_USERS_KEY, userId.toString());

            // 2. 刪除使用者詳細資訊
            redisTemplate.delete(userKey);

            // 3. 更新在線人數統計
            updateOnlineCount();

            log.info("使用者 {} 下線", userId);
        } catch (Exception e) {
            log.error("記錄使用者下線失敗: {}", e.getMessage());
        }
    }

    /**
     * 更新使用者最後活動時間（心跳）
     *
     * @param userId 使用者 ID
     */
    public void heartbeat(Long userId) {
        try {
            long now = System.currentTimeMillis();
            String userKey = ONLINE_USER_PREFIX + userId;

            // 1. 更新在線集合的 score
            redisTemplate.opsForZSet().add(ONLINE_USERS_KEY, userId.toString(), now);

            // 2. 更新詳細資訊中的最後活動時間
            redisTemplate.opsForHash().put(userKey, "lastActiveTime", now);

            // 3. 延長 TTL
            redisTemplate.expire(userKey, USER_INFO_TTL_MINUTES, TimeUnit.MINUTES);

            log.debug("使用者 {} 心跳更新", userId);
        } catch (Exception e) {
            log.error("更新使用者心跳失敗: {}", e.getMessage());
        }
    }

    /**
     * 檢查使用者是否在線
     *
     * @param userId 使用者 ID
     * @return 是否在線
     */
    public boolean isOnline(Long userId) {
        try {
            Double score = redisTemplate.opsForZSet().score(ONLINE_USERS_KEY, userId.toString());
            if (score == null) {
                return false;
            }

            // 檢查是否超時
            long lastActiveTime = score.longValue();
            long timeoutMillis = ONLINE_TIMEOUT_MINUTES * 60 * 1000L;
            return (System.currentTimeMillis() - lastActiveTime) < timeoutMillis;
        } catch (Exception e) {
            log.error("檢查使用者在線狀態失敗: {}", e.getMessage());
            return false;
        }
    }

    /**
     * 取得在線使用者列表
     *
     * @return 在線使用者列表
     */
    public List<OnlineUserInfo> getOnlineUsers() {
        try {
            long now = System.currentTimeMillis();
            long timeoutMillis = ONLINE_TIMEOUT_MINUTES * 60 * 1000L;
            long minScore = now - timeoutMillis;

            // 取得未超時的使用者
            Set<Object> userIds = redisTemplate.opsForZSet().rangeByScore(ONLINE_USERS_KEY, minScore, now);
            if (userIds == null || userIds.isEmpty()) {
                return Collections.emptyList();
            }

            List<OnlineUserInfo> onlineUsers = new ArrayList<>();
            for (Object userIdObj : userIds) {
                String userId = userIdObj.toString();
                String userKey = ONLINE_USER_PREFIX + userId;
                Map<Object, Object> userInfo = redisTemplate.opsForHash().entries(userKey);

                if (!userInfo.isEmpty()) {
                    onlineUsers.add(OnlineUserInfo.builder()
                            .userId(Long.parseLong(userId))
                            .username(getStringValue(userInfo, "username"))
                            .name(getStringValue(userInfo, "name"))
                            .storeId(getLongValue(userInfo, "storeId"))
                            .ip(getStringValue(userInfo, "ip"))
                            .loginTime(getStringValue(userInfo, "loginTime"))
                            .lastActiveTime(getLongValue(userInfo, "lastActiveTime"))
                            .build());
                }
            }

            return onlineUsers;
        } catch (Exception e) {
            log.error("取得在線使用者列表失敗: {}", e.getMessage());
            return Collections.emptyList();
        }
    }

    /**
     * 取得在線人數
     *
     * @return 在線人數
     */
    public long getOnlineCount() {
        try {
            Object count = redisTemplate.opsForValue().get(ONLINE_COUNT_TOTAL);
            return count != null ? Long.parseLong(count.toString()) : 0;
        } catch (Exception e) {
            log.error("取得在線人數失敗: {}", e.getMessage());
            return 0;
        }
    }

    /**
     * 取得各門市在線人數
     *
     * @return 門市在線人數 Map
     */
    public Map<Long, Long> getOnlineCountByStore() {
        try {
            List<OnlineUserInfo> onlineUsers = getOnlineUsers();

            return onlineUsers.stream()
                    .filter(u -> u.getStoreId() != null && u.getStoreId() > 0)
                    .collect(Collectors.groupingBy(
                            OnlineUserInfo::getStoreId,
                            Collectors.counting()
                    ));
        } catch (Exception e) {
            log.error("取得門市在線人數失敗: {}", e.getMessage());
            return Collections.emptyMap();
        }
    }

    /**
     * 定期清理超時的在線狀態
     * 每分鐘執行一次
     */
    @Scheduled(fixedRate = 60000)
    public void cleanupExpiredOnlineStatus() {
        try {
            long now = System.currentTimeMillis();
            long timeoutMillis = ONLINE_TIMEOUT_MINUTES * 60 * 1000L;
            long minScore = now - timeoutMillis;

            // 移除超時的使用者
            Long removed = redisTemplate.opsForZSet().removeRangeByScore(ONLINE_USERS_KEY, 0, minScore);
            if (removed != null && removed > 0) {
                log.debug("已清理 {} 個超時的在線狀態", removed);
                updateOnlineCount();
            }
        } catch (Exception e) {
            log.error("清理超時在線狀態失敗: {}", e.getMessage());
        }
    }

    /**
     * 更新在線人數統計
     */
    private void updateOnlineCount() {
        try {
            long now = System.currentTimeMillis();
            long timeoutMillis = ONLINE_TIMEOUT_MINUTES * 60 * 1000L;
            long minScore = now - timeoutMillis;

            // 計算有效在線人數
            Long count = redisTemplate.opsForZSet().count(ONLINE_USERS_KEY, minScore, now);
            redisTemplate.opsForValue().set(ONLINE_COUNT_TOTAL, count != null ? count : 0);
        } catch (Exception e) {
            log.error("更新在線人數統計失敗: {}", e.getMessage());
        }
    }

    private String getStringValue(Map<Object, Object> map, String key) {
        Object value = map.get(key);
        return value != null ? value.toString() : null;
    }

    private Long getLongValue(Map<Object, Object> map, String key) {
        Object value = map.get(key);
        if (value == null) {
            return null;
        }
        if (value instanceof Number) {
            return ((Number) value).longValue();
        }
        try {
            return Long.parseLong(value.toString());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    /**
     * 在線使用者資訊
     */
    @Data
    @Builder
    public static class OnlineUserInfo {
        private Long userId;
        private String username;
        private String name;
        private Long storeId;
        private String ip;
        private String loginTime;
        private Long lastActiveTime;
    }
}

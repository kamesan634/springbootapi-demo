package com.kamesan.erpapi.system.controller;

import com.kamesan.erpapi.common.dto.ApiResponse;
import com.kamesan.erpapi.security.redis.AuditQueueService;
import com.kamesan.erpapi.security.redis.AuditQueueService.QueueStats;
import com.kamesan.erpapi.security.redis.CacheService;
import com.kamesan.erpapi.security.redis.CacheService.CacheStats;
import com.kamesan.erpapi.security.redis.TokenBlacklistService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * Redis 管理控制器
 *
 * <p>提供 Redis 相關的管理 API，包括快取統計、佇列管理等。</p>
 *
 * @author ERP System Team
 * @version 1.0.0
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/admin/redis")
@RequiredArgsConstructor
@Tag(name = "Redis 管理", description = "快取統計與佇列管理")
@PreAuthorize("hasRole('ADMIN')")
public class RedisAdminController {

    private final CacheService cacheService;
    private final TokenBlacklistService tokenBlacklistService;
    private final AuditQueueService auditQueueService;

    /**
     * 取得 Redis 總覽資訊
     *
     * @return Redis 總覽
     */
    @GetMapping("/overview")
    @Operation(summary = "取得 Redis 總覽", description = "取得快取統計、黑名單數量、佇列狀態等資訊")
    public ApiResponse<RedisOverview> getOverview() {
        CacheStats cacheStats = cacheService.getStats();
        QueueStats queueStats = auditQueueService.getQueueStats();
        long blacklistCount = tokenBlacklistService.getBlacklistCount();

        RedisOverview overview = RedisOverview.builder()
                .cache(cacheStats)
                .queue(queueStats)
                .blacklistCount(blacklistCount)
                .build();

        return ApiResponse.success(overview);
    }

    /**
     * 取得快取統計
     *
     * @return 快取統計資訊
     */
    @GetMapping("/cache/stats")
    @Operation(summary = "取得快取統計", description = "取得快取命中率等統計資訊")
    public ApiResponse<CacheStats> getCacheStats() {
        CacheStats stats = cacheService.getStats();
        return ApiResponse.success(stats);
    }

    /**
     * 重置快取統計
     *
     * @return 操作結果
     */
    @PostMapping("/cache/stats/reset")
    @Operation(summary = "重置快取統計", description = "重置快取命中率統計資訊")
    public ApiResponse<Void> resetCacheStats() {
        cacheService.resetStats();
        log.info("快取統計已重置");
        return ApiResponse.success("快取統計已重置", null);
    }

    /**
     * 清除指定模式的快取
     *
     * @param pattern 快取 Key 模式
     * @return 操作結果
     */
    @DeleteMapping("/cache")
    @Operation(summary = "清除快取", description = "清除符合指定模式的快取")
    public ApiResponse<Void> clearCache(@RequestParam String pattern) {
        cacheService.deleteByPattern(pattern);
        log.info("已清除快取: pattern={}", pattern);
        return ApiResponse.success("快取已清除", null);
    }

    /**
     * 取得佇列統計
     *
     * @return 佇列統計資訊
     */
    @GetMapping("/queue/stats")
    @Operation(summary = "取得佇列統計", description = "取得審計紀錄佇列的統計資訊")
    public ApiResponse<QueueStats> getQueueStats() {
        QueueStats stats = auditQueueService.getQueueStats();
        return ApiResponse.success(stats);
    }

    /**
     * 清空失敗佇列
     *
     * @return 清空的紀錄數
     */
    @DeleteMapping("/queue/failed")
    @Operation(summary = "清空失敗佇列", description = "清空審計紀錄的失敗佇列")
    public ApiResponse<Long> clearFailedQueue() {
        long count = auditQueueService.clearFailedQueue();
        log.info("已清空失敗佇列，共 {} 筆紀錄", count);
        return ApiResponse.success("失敗佇列已清空", count);
    }

    /**
     * 重試失敗佇列
     *
     * @return 重試的紀錄數
     */
    @PostMapping("/queue/failed/retry")
    @Operation(summary = "重試失敗佇列", description = "將失敗佇列中的紀錄重新入隊處理")
    public ApiResponse<Long> retryFailedRecords() {
        long count = auditQueueService.retryFailedRecords();
        log.info("已重試 {} 筆失敗紀錄", count);
        return ApiResponse.success("已重試失敗紀錄", count);
    }

    /**
     * 取得 Token 黑名單數量
     *
     * @return 黑名單數量
     */
    @GetMapping("/blacklist/count")
    @Operation(summary = "取得黑名單數量", description = "取得 Token 黑名單中的數量")
    public ApiResponse<Long> getBlacklistCount() {
        long count = tokenBlacklistService.getBlacklistCount();
        return ApiResponse.success(count);
    }

    /**
     * Redis 總覽資訊
     */
    @Data
    @Builder
    public static class RedisOverview {
        private CacheStats cache;
        private QueueStats queue;
        private long blacklistCount;
    }
}

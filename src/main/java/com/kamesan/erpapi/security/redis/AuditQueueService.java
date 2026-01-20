package com.kamesan.erpapi.security.redis;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * 操作紀錄佇列服務
 *
 * <p>使用 Redis List 實現操作紀錄的非同步處理，達到「削峰填谷」效果。</p>
 *
 * <h2>Redis Key 格式：</h2>
 * <ul>
 *   <li>audit:queue - 主要佇列</li>
 *   <li>audit:queue:failed - 失敗佇列（DLQ）</li>
 *   <li>audit:stats - 統計資訊</li>
 * </ul>
 *
 * <h2>設計模式：</h2>
 * <ul>
 *   <li>生產者-消費者模式</li>
 *   <li>削峰填谷：高峰期快速入隊，定時批量消費</li>
 *   <li>失敗重試：失敗的紀錄放入 DLQ</li>
 * </ul>
 *
 * @author ERP System Team
 * @version 1.0.0
 */
@Slf4j
@Service
public class AuditQueueService {

    private static final String QUEUE_KEY = "audit:queue";
    private static final String FAILED_QUEUE_KEY = "audit:queue:failed";
    private static final String STATS_ENQUEUED = "audit:stats:enqueued";
    private static final String STATS_PROCESSED = "audit:stats:processed";
    private static final String STATS_FAILED = "audit:stats:failed";

    /**
     * 每批次處理的最大數量
     */
    private static final int BATCH_SIZE = 100;

    /**
     * 最大重試次數
     */
    private static final int MAX_RETRY_COUNT = 3;

    private final RedisTemplate<String, Object> redisTemplate;
    private final ObjectMapper objectMapper;

    /**
     * 審計紀錄處理器（需要在實際使用時注入）
     */
    private AuditRecordProcessor processor;

    public AuditQueueService(RedisTemplate<String, Object> redisTemplate, ObjectMapper objectMapper) {
        this.redisTemplate = redisTemplate;
        this.objectMapper = objectMapper;
    }

    /**
     * 設定審計紀錄處理器
     *
     * @param processor 處理器
     */
    public void setProcessor(AuditRecordProcessor processor) {
        this.processor = processor;
    }

    /**
     * 將操作紀錄加入佇列
     *
     * @param record 審計紀錄
     */
    public void enqueue(AuditRecord record) {
        try {
            if (record.getId() == null) {
                record.setId(UUID.randomUUID().toString());
            }
            if (record.getTimestamp() == null) {
                record.setTimestamp(LocalDateTime.now());
            }

            String json = objectMapper.writeValueAsString(record);
            redisTemplate.opsForList().rightPush(QUEUE_KEY, json);
            redisTemplate.opsForValue().increment(STATS_ENQUEUED);

            log.debug("審計紀錄已入隊: action={}, resource={}", record.getAction(), record.getResource());
        } catch (JsonProcessingException e) {
            log.error("序列化審計紀錄失敗: {}", e.getMessage());
        } catch (Exception e) {
            log.error("審計紀錄入隊失敗: {}", e.getMessage());
        }
    }

    /**
     * 快速記錄操作（便捷方法）
     *
     * @param userId     使用者 ID
     * @param username   使用者名稱
     * @param action     操作類型
     * @param resource   資源類型
     * @param resourceId 資源 ID
     * @param details    詳細資訊
     * @param ip         客戶端 IP
     */
    public void logAction(Long userId, String username, String action, String resource,
                          String resourceId, String details, String ip) {
        AuditRecord record = AuditRecord.builder()
                .userId(userId)
                .username(username)
                .action(action)
                .resource(resource)
                .resourceId(resourceId)
                .details(details)
                .ip(ip)
                .build();

        enqueue(record);
    }

    /**
     * 定時處理佇列（每 5 秒執行一次）
     */
    @Scheduled(fixedDelay = 5000)
    public void processQueue() {
        try {
            Long queueSize = redisTemplate.opsForList().size(QUEUE_KEY);
            if (queueSize == null || queueSize == 0) {
                return;
            }

            log.debug("開始處理審計佇列，目前佇列大小: {}", queueSize);

            List<AuditRecord> batch = new ArrayList<>();
            int processed = 0;

            // 批次取出紀錄
            while (processed < BATCH_SIZE) {
                Object item = redisTemplate.opsForList().leftPop(QUEUE_KEY);
                if (item == null) {
                    break;
                }

                try {
                    AuditRecord record = objectMapper.readValue(item.toString(), AuditRecord.class);
                    batch.add(record);
                    processed++;
                } catch (Exception e) {
                    log.error("反序列化審計紀錄失敗: {}", e.getMessage());
                    // 將無法解析的紀錄放入失敗佇列
                    redisTemplate.opsForList().rightPush(FAILED_QUEUE_KEY, item);
                    redisTemplate.opsForValue().increment(STATS_FAILED);
                }
            }

            // 批次處理
            if (!batch.isEmpty()) {
                processBatch(batch);
                redisTemplate.opsForValue().increment(STATS_PROCESSED, batch.size());
                log.info("已處理 {} 筆審計紀錄", batch.size());
            }

        } catch (Exception e) {
            log.error("處理審計佇列失敗: {}", e.getMessage());
        }
    }

    /**
     * 批次處理審計紀錄
     *
     * @param batch 審計紀錄批次
     */
    private void processBatch(List<AuditRecord> batch) {
        for (AuditRecord record : batch) {
            try {
                if (processor != null) {
                    processor.process(record);
                } else {
                    // 預設處理：記錄到日誌
                    log.info("審計紀錄: user={}, action={}, resource={}, resourceId={}, details={}",
                            record.getUsername(), record.getAction(), record.getResource(),
                            record.getResourceId(), record.getDetails());
                }
            } catch (Exception e) {
                log.error("處理審計紀錄失敗: id={}, error={}", record.getId(), e.getMessage());
                handleFailedRecord(record);
            }
        }
    }

    /**
     * 處理失敗的紀錄
     *
     * @param record 審計紀錄
     */
    private void handleFailedRecord(AuditRecord record) {
        try {
            record.setRetryCount(record.getRetryCount() + 1);

            if (record.getRetryCount() < MAX_RETRY_COUNT) {
                // 重新入隊等待重試
                String json = objectMapper.writeValueAsString(record);
                redisTemplate.opsForList().rightPush(QUEUE_KEY, json);
                log.warn("審計紀錄已重新入隊等待重試: id={}, retryCount={}", record.getId(), record.getRetryCount());
            } else {
                // 超過重試次數，放入失敗佇列
                String json = objectMapper.writeValueAsString(record);
                redisTemplate.opsForList().rightPush(FAILED_QUEUE_KEY, json);
                redisTemplate.opsForValue().increment(STATS_FAILED);
                log.error("審計紀錄重試次數已達上限，已放入失敗佇列: id={}", record.getId());
            }
        } catch (Exception e) {
            log.error("處理失敗紀錄時發生錯誤: {}", e.getMessage());
        }
    }

    /**
     * 取得佇列狀態
     *
     * @return 佇列統計資訊
     */
    public QueueStats getQueueStats() {
        try {
            Long queueSize = redisTemplate.opsForList().size(QUEUE_KEY);
            Long failedSize = redisTemplate.opsForList().size(FAILED_QUEUE_KEY);

            Object enqueuedObj = redisTemplate.opsForValue().get(STATS_ENQUEUED);
            Object processedObj = redisTemplate.opsForValue().get(STATS_PROCESSED);
            Object failedObj = redisTemplate.opsForValue().get(STATS_FAILED);

            return QueueStats.builder()
                    .queueSize(queueSize != null ? queueSize : 0)
                    .failedQueueSize(failedSize != null ? failedSize : 0)
                    .totalEnqueued(enqueuedObj != null ? Long.parseLong(enqueuedObj.toString()) : 0)
                    .totalProcessed(processedObj != null ? Long.parseLong(processedObj.toString()) : 0)
                    .totalFailed(failedObj != null ? Long.parseLong(failedObj.toString()) : 0)
                    .build();
        } catch (Exception e) {
            log.error("取得佇列狀態失敗: {}", e.getMessage());
            return QueueStats.builder().build();
        }
    }

    /**
     * 清空失敗佇列
     *
     * @return 清空的紀錄數
     */
    public long clearFailedQueue() {
        Long size = redisTemplate.opsForList().size(FAILED_QUEUE_KEY);
        redisTemplate.delete(FAILED_QUEUE_KEY);
        log.info("已清空失敗佇列，共 {} 筆紀錄", size);
        return size != null ? size : 0;
    }

    /**
     * 重試失敗佇列中的紀錄
     *
     * @return 重試的紀錄數
     */
    public long retryFailedRecords() {
        Long size = redisTemplate.opsForList().size(FAILED_QUEUE_KEY);
        if (size == null || size == 0) {
            return 0;
        }

        long retried = 0;
        while (retried < size) {
            Object item = redisTemplate.opsForList().leftPop(FAILED_QUEUE_KEY);
            if (item == null) {
                break;
            }

            try {
                AuditRecord record = objectMapper.readValue(item.toString(), AuditRecord.class);
                record.setRetryCount(0);  // 重置重試計數
                enqueue(record);
                retried++;
            } catch (Exception e) {
                log.error("重試失敗紀錄時發生錯誤: {}", e.getMessage());
                // 放回失敗佇列末尾
                redisTemplate.opsForList().rightPush(FAILED_QUEUE_KEY, item);
            }
        }

        log.info("已將 {} 筆失敗紀錄重新入隊", retried);
        return retried;
    }

    /**
     * 審計紀錄處理器介面
     */
    @FunctionalInterface
    public interface AuditRecordProcessor {
        void process(AuditRecord record) throws Exception;
    }

    /**
     * 審計紀錄
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AuditRecord {
        private String id;
        private Long userId;
        private String username;
        private String action;
        private String resource;
        private String resourceId;
        private String details;
        private String ip;
        private LocalDateTime timestamp;
        @Builder.Default
        private int retryCount = 0;
    }

    /**
     * 佇列統計
     */
    @Data
    @Builder
    public static class QueueStats {
        private long queueSize;
        private long failedQueueSize;
        private long totalEnqueued;
        private long totalProcessed;
        private long totalFailed;
    }
}

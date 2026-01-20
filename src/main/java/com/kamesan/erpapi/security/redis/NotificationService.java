package com.kamesan.erpapi.security.redis;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * 即時通知服務
 *
 * <p>使用 Redis Pub/Sub 實現即時通知功能。</p>
 *
 * <h2>通知頻道：</h2>
 * <ul>
 *   <li>notification:global - 全局通知</li>
 *   <li>notification:store:{storeId} - 門市通知</li>
 *   <li>notification:user:{userId} - 個人通知</li>
 *   <li>notification:inventory - 庫存異動通知</li>
 *   <li>notification:order - 訂單通知</li>
 * </ul>
 *
 * <h2>通知類型：</h2>
 * <ul>
 *   <li>INFO - 一般資訊</li>
 *   <li>WARNING - 警告訊息</li>
 *   <li>ERROR - 錯誤訊息</li>
 *   <li>SUCCESS - 成功訊息</li>
 * </ul>
 *
 * @author ERP System Team
 * @version 1.0.0
 */
@Slf4j
@Service
public class NotificationService {

    private static final String CHANNEL_PREFIX = "notification:";
    private static final String GLOBAL_CHANNEL = CHANNEL_PREFIX + "global";
    private static final String STORE_CHANNEL_PREFIX = CHANNEL_PREFIX + "store:";
    private static final String USER_CHANNEL_PREFIX = CHANNEL_PREFIX + "user:";
    private static final String INVENTORY_CHANNEL = CHANNEL_PREFIX + "inventory";
    private static final String ORDER_CHANNEL = CHANNEL_PREFIX + "order";

    private final RedisTemplate<String, Object> redisTemplate;
    private final ObjectMapper objectMapper;

    public NotificationService(RedisTemplate<String, Object> redisTemplate, ObjectMapper objectMapper) {
        this.redisTemplate = redisTemplate;
        this.objectMapper = objectMapper;
    }

    /**
     * 發送全局通知
     *
     * @param type    通知類型
     * @param title   標題
     * @param message 訊息內容
     */
    public void sendGlobalNotification(NotificationType type, String title, String message) {
        Notification notification = buildNotification(type, title, message, null);
        publish(GLOBAL_CHANNEL, notification);
        log.info("發送全局通知: type={}, title={}", type, title);
    }

    /**
     * 發送門市通知
     *
     * @param storeId 門市 ID
     * @param type    通知類型
     * @param title   標題
     * @param message 訊息內容
     */
    public void sendStoreNotification(Long storeId, NotificationType type, String title, String message) {
        Notification notification = buildNotification(type, title, message, null);
        notification.setStoreId(storeId);
        publish(STORE_CHANNEL_PREFIX + storeId, notification);
        log.info("發送門市通知: storeId={}, type={}, title={}", storeId, type, title);
    }

    /**
     * 發送個人通知
     *
     * @param userId  使用者 ID
     * @param type    通知類型
     * @param title   標題
     * @param message 訊息內容
     */
    public void sendUserNotification(Long userId, NotificationType type, String title, String message) {
        Notification notification = buildNotification(type, title, message, null);
        notification.setUserId(userId);
        publish(USER_CHANNEL_PREFIX + userId, notification);
        log.info("發送個人通知: userId={}, type={}, title={}", userId, type, title);
    }

    /**
     * 發送庫存異動通知
     *
     * @param storeId      門市 ID
     * @param productId    商品 ID
     * @param productName  商品名稱
     * @param oldQuantity  原數量
     * @param newQuantity  新數量
     * @param reason       異動原因
     */
    public void sendInventoryNotification(Long storeId, Long productId, String productName,
                                          int oldQuantity, int newQuantity, String reason) {
        Map<String, Object> data = new HashMap<>();
        data.put("storeId", storeId);
        data.put("productId", productId);
        data.put("productName", productName);
        data.put("oldQuantity", oldQuantity);
        data.put("newQuantity", newQuantity);
        data.put("change", newQuantity - oldQuantity);
        data.put("reason", reason);

        String title = "庫存異動";
        String message = String.format("商品 %s 庫存從 %d 變更為 %d", productName, oldQuantity, newQuantity);

        NotificationType type = newQuantity < 10 ? NotificationType.WARNING : NotificationType.INFO;

        Notification notification = buildNotification(type, title, message, data);
        notification.setStoreId(storeId);

        publish(INVENTORY_CHANNEL, notification);
        log.info("發送庫存異動通知: storeId={}, productId={}, change={}", storeId, productId, newQuantity - oldQuantity);
    }

    /**
     * 發送低庫存警告
     *
     * @param storeId     門市 ID
     * @param productId   商品 ID
     * @param productName 商品名稱
     * @param quantity    目前數量
     * @param safetyStock 安全庫存
     */
    public void sendLowStockWarning(Long storeId, Long productId, String productName,
                                    int quantity, int safetyStock) {
        Map<String, Object> data = new HashMap<>();
        data.put("storeId", storeId);
        data.put("productId", productId);
        data.put("productName", productName);
        data.put("quantity", quantity);
        data.put("safetyStock", safetyStock);

        String title = "低庫存警告";
        String message = String.format("商品 %s 庫存 (%d) 低於安全庫存 (%d)", productName, quantity, safetyStock);

        Notification notification = buildNotification(NotificationType.WARNING, title, message, data);
        notification.setStoreId(storeId);

        publish(INVENTORY_CHANNEL, notification);
        // 同時發送到門市頻道
        publish(STORE_CHANNEL_PREFIX + storeId, notification);

        log.warn("發送低庫存警告: storeId={}, productId={}, quantity={}, safetyStock={}",
                storeId, productId, quantity, safetyStock);
    }

    /**
     * 發送新訂單通知
     *
     * @param storeId     門市 ID
     * @param orderId     訂單 ID
     * @param orderNumber 訂單編號
     * @param amount      訂單金額
     */
    public void sendNewOrderNotification(Long storeId, Long orderId, String orderNumber, double amount) {
        Map<String, Object> data = new HashMap<>();
        data.put("storeId", storeId);
        data.put("orderId", orderId);
        data.put("orderNumber", orderNumber);
        data.put("amount", amount);

        String title = "新訂單";
        String message = String.format("訂單 %s，金額 $%.2f", orderNumber, amount);

        Notification notification = buildNotification(NotificationType.SUCCESS, title, message, data);
        notification.setStoreId(storeId);

        publish(ORDER_CHANNEL, notification);
        log.info("發送新訂單通知: storeId={}, orderId={}, amount={}", storeId, orderId, amount);
    }

    /**
     * 發送訂單狀態變更通知
     *
     * @param storeId     門市 ID
     * @param orderId     訂單 ID
     * @param orderNumber 訂單編號
     * @param oldStatus   原狀態
     * @param newStatus   新狀態
     */
    public void sendOrderStatusNotification(Long storeId, Long orderId, String orderNumber,
                                            String oldStatus, String newStatus) {
        Map<String, Object> data = new HashMap<>();
        data.put("storeId", storeId);
        data.put("orderId", orderId);
        data.put("orderNumber", orderNumber);
        data.put("oldStatus", oldStatus);
        data.put("newStatus", newStatus);

        String title = "訂單狀態變更";
        String message = String.format("訂單 %s 狀態從 %s 變更為 %s", orderNumber, oldStatus, newStatus);

        Notification notification = buildNotification(NotificationType.INFO, title, message, data);
        notification.setStoreId(storeId);

        publish(ORDER_CHANNEL, notification);
        log.info("發送訂單狀態變更通知: orderId={}, {} -> {}", orderId, oldStatus, newStatus);
    }

    /**
     * 發布通知到指定頻道
     *
     * @param channel      頻道
     * @param notification 通知內容
     */
    private void publish(String channel, Notification notification) {
        try {
            String json = objectMapper.writeValueAsString(notification);
            redisTemplate.convertAndSend(channel, json);
            log.debug("發布通知到頻道 {}: {}", channel, json);
        } catch (JsonProcessingException e) {
            log.error("序列化通知失敗: {}", e.getMessage());
        } catch (Exception e) {
            log.error("發布通知失敗: channel={}, error={}", channel, e.getMessage());
        }
    }

    /**
     * 建立通知物件
     */
    private Notification buildNotification(NotificationType type, String title, String message, Map<String, Object> data) {
        return Notification.builder()
                .id(java.util.UUID.randomUUID().toString())
                .type(type)
                .title(title)
                .message(message)
                .data(data)
                .timestamp(LocalDateTime.now())
                .build();
    }

    /**
     * 取得頻道 Topic
     *
     * @param channel 頻道名稱
     * @return ChannelTopic
     */
    public static ChannelTopic getTopic(String channel) {
        return new ChannelTopic(channel);
    }

    /**
     * 取得全局通知頻道
     */
    public static ChannelTopic getGlobalTopic() {
        return new ChannelTopic(GLOBAL_CHANNEL);
    }

    /**
     * 取得門市通知頻道
     */
    public static ChannelTopic getStoreTopic(Long storeId) {
        return new ChannelTopic(STORE_CHANNEL_PREFIX + storeId);
    }

    /**
     * 取得個人通知頻道
     */
    public static ChannelTopic getUserTopic(Long userId) {
        return new ChannelTopic(USER_CHANNEL_PREFIX + userId);
    }

    /**
     * 取得庫存通知頻道
     */
    public static ChannelTopic getInventoryTopic() {
        return new ChannelTopic(INVENTORY_CHANNEL);
    }

    /**
     * 取得訂單通知頻道
     */
    public static ChannelTopic getOrderTopic() {
        return new ChannelTopic(ORDER_CHANNEL);
    }

    /**
     * 通知類型
     */
    public enum NotificationType {
        INFO,
        WARNING,
        ERROR,
        SUCCESS
    }

    /**
     * 通知物件
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Notification {
        private String id;
        private NotificationType type;
        private String title;
        private String message;
        private Map<String, Object> data;
        private Long storeId;
        private Long userId;
        private LocalDateTime timestamp;
    }
}

package com.kamesan.erpapi.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kamesan.erpapi.security.redis.NotificationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.listener.PatternTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.data.redis.listener.adapter.MessageListenerAdapter;

/**
 * Redis Pub/Sub 配置
 *
 * <p>配置 Redis 訊息監聽器，用於接收即時通知。</p>
 *
 * @author ERP System Team
 * @version 1.0.0
 */
@Slf4j
@Configuration
@Profile("!test")
public class RedisPubSubConfig {

    /**
     * 配置 Redis 訊息監聽器容器
     *
     * @param connectionFactory Redis 連線工廠
     * @param listenerAdapter   訊息監聽適配器
     * @return RedisMessageListenerContainer
     */
    @Bean
    public RedisMessageListenerContainer redisMessageListenerContainer(
            RedisConnectionFactory connectionFactory,
            MessageListenerAdapter listenerAdapter) {

        RedisMessageListenerContainer container = new RedisMessageListenerContainer();
        container.setConnectionFactory(connectionFactory);

        // 訂閱所有通知頻道（使用萬用字元）
        container.addMessageListener(listenerAdapter, new PatternTopic("notification:*"));

        log.info("Redis Pub/Sub 訊息監聽器已啟動，訂閱頻道: notification:*");

        return container;
    }

    /**
     * 配置訊息監聽適配器
     *
     * @param subscriber 通知訂閱者
     * @return MessageListenerAdapter
     */
    @Bean
    public MessageListenerAdapter messageListenerAdapter(NotificationSubscriber subscriber) {
        return new MessageListenerAdapter(subscriber, "handleMessage");
    }

    /**
     * 通知訂閱者
     *
     * <p>處理從 Redis Pub/Sub 接收到的通知。</p>
     */
    @Slf4j
    public static class NotificationSubscriber {

        private final ObjectMapper objectMapper;

        public NotificationSubscriber(ObjectMapper objectMapper) {
            this.objectMapper = objectMapper;
        }

        /**
         * 處理接收到的訊息
         *
         * @param message 訊息內容
         * @param channel 頻道名稱
         */
        public void handleMessage(String message, String channel) {
            try {
                log.debug("收到通知 [{}]: {}", channel, message);

                // 解析通知
                NotificationService.Notification notification = objectMapper.readValue(
                        message, NotificationService.Notification.class);

                // 根據頻道類型處理通知
                if (channel.startsWith("notification:inventory")) {
                    handleInventoryNotification(notification);
                } else if (channel.startsWith("notification:order")) {
                    handleOrderNotification(notification);
                } else if (channel.startsWith("notification:store:")) {
                    handleStoreNotification(notification, channel);
                } else if (channel.startsWith("notification:user:")) {
                    handleUserNotification(notification, channel);
                } else if (channel.equals("notification:global")) {
                    handleGlobalNotification(notification);
                }

            } catch (Exception e) {
                log.error("處理通知失敗: channel={}, error={}", channel, e.getMessage());
            }
        }

        private void handleInventoryNotification(NotificationService.Notification notification) {
            log.info("庫存通知: type={}, title={}, message={}",
                    notification.getType(), notification.getTitle(), notification.getMessage());

            // 這裡可以實現更多邏輯，例如：
            // - 發送 WebSocket 通知給前端
            // - 發送 Email 通知
            // - 觸發自動補貨流程
        }

        private void handleOrderNotification(NotificationService.Notification notification) {
            log.info("訂單通知: type={}, title={}, message={}",
                    notification.getType(), notification.getTitle(), notification.getMessage());
        }

        private void handleStoreNotification(NotificationService.Notification notification, String channel) {
            String storeId = channel.replace("notification:store:", "");
            log.info("門市通知 [門市{}]: type={}, title={}",
                    storeId, notification.getType(), notification.getTitle());
        }

        private void handleUserNotification(NotificationService.Notification notification, String channel) {
            String userId = channel.replace("notification:user:", "");
            log.info("個人通知 [使用者{}]: type={}, title={}",
                    userId, notification.getType(), notification.getTitle());
        }

        private void handleGlobalNotification(NotificationService.Notification notification) {
            log.info("全局通知: type={}, title={}, message={}",
                    notification.getType(), notification.getTitle(), notification.getMessage());
        }
    }

    /**
     * 配置通知訂閱者 Bean
     *
     * @param objectMapper JSON 物件映射器
     * @return NotificationSubscriber
     */
    @Bean
    public NotificationSubscriber notificationSubscriber(ObjectMapper objectMapper) {
        return new NotificationSubscriber(objectMapper);
    }
}

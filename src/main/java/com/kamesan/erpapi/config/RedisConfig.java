package com.kamesan.erpapi.config;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.jsontype.impl.LaissezFaireSubTypeValidator;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

/**
 * Redis 配置
 *
 * <p>配置 Redis 連線和序列化方式，支援 JSON 格式儲存。</p>
 *
 * <h2>配置內容：</h2>
 * <ul>
 *   <li>RedisTemplate - 操作 Redis 的模板</li>
 *   <li>RedisCacheManager - 快取管理器</li>
 *   <li>序列化配置 - 使用 JSON 格式</li>
 * </ul>
 *
 * @author ERP System Team
 * @version 1.0.0
 */
@Configuration
@EnableCaching
public class RedisConfig {

    /**
     * 配置 RedisTemplate
     *
     * <p>使用 StringRedisSerializer 序列化 Key，
     * 使用 GenericJackson2JsonRedisSerializer 序列化 Value。</p>
     *
     * @param connectionFactory Redis 連線工廠
     * @return RedisTemplate
     */
    @Bean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory connectionFactory) {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);

        // 建立 JSON 序列化器
        GenericJackson2JsonRedisSerializer jsonSerializer = createJsonSerializer();

        // Key 序列化器（使用字串）
        StringRedisSerializer stringSerializer = new StringRedisSerializer();

        // 設定 Key 序列化器
        template.setKeySerializer(stringSerializer);
        template.setHashKeySerializer(stringSerializer);

        // 設定 Value 序列化器
        template.setValueSerializer(jsonSerializer);
        template.setHashValueSerializer(jsonSerializer);

        template.afterPropertiesSet();
        return template;
    }

    /**
     * 配置 Redis 快取管理器
     *
     * <p>設定不同快取區域的過期時間：</p>
     * <ul>
     *   <li>users - 使用者快取，1 小時</li>
     *   <li>products - 商品快取，30 分鐘</li>
     *   <li>categories - 分類快取，1 小時</li>
     *   <li>inventory - 庫存快取，5 分鐘</li>
     *   <li>default - 預設快取，10 分鐘</li>
     * </ul>
     *
     * @param connectionFactory Redis 連線工廠
     * @return RedisCacheManager
     */
    @Bean
    public RedisCacheManager cacheManager(RedisConnectionFactory connectionFactory) {
        // JSON 序列化器
        GenericJackson2JsonRedisSerializer jsonSerializer = createJsonSerializer();

        // 預設快取配置
        RedisCacheConfiguration defaultConfig = RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(Duration.ofMinutes(10))  // 預設 10 分鐘
                .serializeKeysWith(RedisSerializationContext.SerializationPair
                        .fromSerializer(new StringRedisSerializer()))
                .serializeValuesWith(RedisSerializationContext.SerializationPair
                        .fromSerializer(jsonSerializer))
                .disableCachingNullValues();  // 不快取 null 值

        // 各快取區域配置
        Map<String, RedisCacheConfiguration> cacheConfigurations = new HashMap<>();

        // 使用者快取 - 1 小時
        cacheConfigurations.put("users", defaultConfig.entryTtl(Duration.ofHours(1)));

        // 商品快取 - 30 分鐘
        cacheConfigurations.put("products", defaultConfig.entryTtl(Duration.ofMinutes(30)));

        // 分類快取 - 1 小時
        cacheConfigurations.put("categories", defaultConfig.entryTtl(Duration.ofHours(1)));

        // 庫存快取 - 5 分鐘（庫存變動頻繁）
        cacheConfigurations.put("inventory", defaultConfig.entryTtl(Duration.ofMinutes(5)));

        // 供應商快取 - 1 小時
        cacheConfigurations.put("suppliers", defaultConfig.entryTtl(Duration.ofHours(1)));

        // 系統參數快取 - 1 天
        cacheConfigurations.put("system_params", defaultConfig.entryTtl(Duration.ofDays(1)));

        return RedisCacheManager.builder(connectionFactory)
                .cacheDefaults(defaultConfig)
                .withInitialCacheConfigurations(cacheConfigurations)
                .build();
    }

    /**
     * 建立 JSON 序列化器
     *
     * @return GenericJackson2JsonRedisSerializer
     */
    private GenericJackson2JsonRedisSerializer createJsonSerializer() {
        ObjectMapper objectMapper = new ObjectMapper();

        // 設定可見性
        objectMapper.setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.ANY);

        // 啟用類型資訊（用於反序列化）
        objectMapper.activateDefaultTyping(
                LaissezFaireSubTypeValidator.instance,
                ObjectMapper.DefaultTyping.NON_FINAL,
                JsonTypeInfo.As.PROPERTY
        );

        // 註冊 Java 8 日期時間模組
        objectMapper.registerModule(new JavaTimeModule());

        return new GenericJackson2JsonRedisSerializer(objectMapper);
    }
}

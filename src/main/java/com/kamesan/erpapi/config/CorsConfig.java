package com.kamesan.erpapi.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

/**
 * CORS 跨域配置
 *
 * <p>配置跨域資源共享 (CORS)，允許前端應用程式存取 API。</p>
 *
 * <h2>配置項目：</h2>
 * <ul>
 *   <li>allowedOrigins - 允許的來源網域</li>
 *   <li>allowedMethods - 允許的 HTTP 方法</li>
 *   <li>allowedHeaders - 允許的請求標頭</li>
 *   <li>allowCredentials - 是否允許攜帶認證資訊</li>
 *   <li>maxAge - 預檢請求的快取時間</li>
 * </ul>
 *
 * @author ERP System Team
 * @version 1.0.0
 */
@Configuration
public class CorsConfig {

    /**
     * 允許的來源（從配置檔讀取）
     */
    @Value("${app.cors.allowed-origins:http://localhost:3000,http://localhost:5173}")
    private String allowedOrigins;

    /**
     * 允許的方法（從配置檔讀取）
     */
    @Value("${app.cors.allowed-methods:GET,POST,PUT,DELETE,OPTIONS,PATCH}")
    private String allowedMethods;

    /**
     * 允許的標頭（從配置檔讀取）
     */
    @Value("${app.cors.allowed-headers:*}")
    private String allowedHeaders;

    /**
     * 是否允許攜帶認證資訊（從配置檔讀取）
     */
    @Value("${app.cors.allow-credentials:true}")
    private boolean allowCredentials;

    /**
     * 預檢請求快取時間（秒）
     */
    @Value("${app.cors.max-age:3600}")
    private long maxAge;

    /**
     * 配置 CORS
     *
     * @return CorsConfigurationSource
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();

        // 設定允許的來源
        List<String> origins = Arrays.asList(allowedOrigins.split(","));
        configuration.setAllowedOrigins(origins);

        // 設定允許的方法
        List<String> methods = Arrays.asList(allowedMethods.split(","));
        configuration.setAllowedMethods(methods);

        // 設定允許的標頭
        if ("*".equals(allowedHeaders)) {
            configuration.addAllowedHeader("*");
        } else {
            List<String> headers = Arrays.asList(allowedHeaders.split(","));
            configuration.setAllowedHeaders(headers);
        }

        // 設定是否允許攜帶認證資訊
        configuration.setAllowCredentials(allowCredentials);

        // 設定預檢請求快取時間
        configuration.setMaxAge(maxAge);

        // 設定暴露的標頭（前端可以存取）
        configuration.setExposedHeaders(List.of(
                "Authorization",
                "Content-Disposition",
                "X-Total-Count"
        ));

        // 應用到所有路徑
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);

        return source;
    }
}

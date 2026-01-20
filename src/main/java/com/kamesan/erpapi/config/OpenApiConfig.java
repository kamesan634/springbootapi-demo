package com.kamesan.erpapi.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * OpenAPI (Swagger) 配置
 *
 * <p>配置 Swagger UI 的 API 文件資訊，包括：</p>
 * <ul>
 *   <li>API 基本資訊（標題、描述、版本）</li>
 *   <li>JWT 認證配置</li>
 *   <li>伺服器資訊</li>
 * </ul>
 *
 * <p>Swagger UI 路徑：/swagger-ui.html</p>
 *
 * @author ERP System Team
 * @version 1.0.0
 */
@Configuration
public class OpenApiConfig {

    /**
     * 伺服器埠號
     */
    @Value("${server.port:8005}")
    private int serverPort;

    /**
     * 配置 OpenAPI 文件
     *
     * @return OpenAPI 配置
     */
    @Bean
    public OpenAPI customOpenAPI() {
        // JWT 安全方案名稱
        final String securitySchemeName = "bearerAuth";

        return new OpenAPI()
                // API 基本資訊
                .info(apiInfo())
                // 伺服器資訊
                .servers(List.of(
                        new Server()
                                .url("http://localhost:" + serverPort)
                                .description("開發環境")
                ))
                // 安全方案
                .addSecurityItem(new SecurityRequirement().addList(securitySchemeName))
                .components(new Components()
                        .addSecuritySchemes(securitySchemeName,
                                new SecurityScheme()
                                        .name(securitySchemeName)
                                        .type(SecurityScheme.Type.HTTP)
                                        .scheme("bearer")
                                        .bearerFormat("JWT")
                                        .description("請輸入 JWT Token（不需要 Bearer 前綴）")
                        )
                );
    }

    /**
     * API 基本資訊
     *
     * @return Info 物件
     */
    private Info apiInfo() {
        return new Info()
                .title("零售業簡易ERP系統 API")
                .description("""
                        ## 系統說明

                        本系統為零售業ERP系統的後端 API，提供完整的銷售、庫存、採購、會員管理功能。

                        ## 功能模組

                        - **帳號管理** - 使用者、角色、權限、門市/倉庫管理
                        - **客戶管理** - 會員等級、會員資料管理
                        - **商品管理** - 商品、分類、單位、稅別管理
                        - **採購管理** - 供應商、採購單管理
                        - **庫存管理** - 庫存查詢、異動、盤點、調撥
                        - **促銷管理** - 促銷活動、優惠券管理
                        - **銷售管理** - 訂單、付款、退貨管理
                        - **報表管理** - 各類報表查詢與匯出

                        ## 認證方式

                        使用 JWT Token 進行認證，請先呼叫登入 API 取得 Token，
                        然後在請求標頭中加入：`Authorization: Bearer {token}`

                        ## 錯誤碼說明

                        | 代碼 | 說明 |
                        |------|------|
                        | 200 | 成功 |
                        | 400 | 請求參數錯誤 |
                        | 401 | 未認證或 Token 失效 |
                        | 403 | 權限不足 |
                        | 404 | 資源不存在 |
                        | 500 | 伺服器錯誤 |
                        """)
                .version("1.0.0")
                .contact(new Contact()
                        .name("ERP System Team")
                        .email("support@example.com"))
                .license(new License()
                        .name("MIT License")
                        .url("https://opensource.org/licenses/MIT"));
    }
}

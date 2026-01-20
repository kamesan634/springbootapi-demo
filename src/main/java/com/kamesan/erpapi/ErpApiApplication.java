package com.kamesan.erpapi;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * 零售業簡易ERP系統 - Spring Boot API 應用程式入口
 *
 * <p>本系統為零售業ERP系統的後端API，提供完整的銷售、庫存、採購、會員管理功能。</p>
 *
 * <h2>系統功能模組：</h2>
 * <ul>
 *   <li>帳號管理 (accounts) - 使用者、角色、權限、門市/倉庫管理</li>
 *   <li>客戶管理 (customers) - 會員等級、會員資料管理</li>
 *   <li>商品管理 (products) - 商品、分類、單位、稅別管理</li>
 *   <li>採購管理 (purchasing) - 供應商、採購單管理</li>
 *   <li>庫存管理 (inventory) - 庫存查詢、異動、盤點、調撥</li>
 *   <li>促銷管理 (promotions) - 促銷活動、優惠券管理</li>
 *   <li>銷售管理 (sales) - 訂單、付款、退貨管理</li>
 *   <li>報表管理 (reports) - 各類報表查詢與匯出</li>
 * </ul>
 *
 * <h2>技術棧：</h2>
 * <ul>
 *   <li>Java 21 + Spring Boot 3.2</li>
 *   <li>Spring Data JPA + MySQL 8.4</li>
 *   <li>Spring Security + JWT</li>
 *   <li>Redis 快取</li>
 *   <li>Flyway 資料庫遷移</li>
 *   <li>SpringDoc OpenAPI (Swagger)</li>
 * </ul>
 *
 * @author ERP System Team
 * @version 1.0.0
 */
@SpringBootApplication
@EnableScheduling
public class ErpApiApplication {

    /**
     * 應用程式主入口
     *
     * @param args 命令列參數
     */
    public static void main(String[] args) {
        SpringApplication.run(ErpApiApplication.class, args);
    }
}

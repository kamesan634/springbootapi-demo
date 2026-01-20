# 龜三的ERP Demo - Spring Boot API 

![CI](https://github.com/kamesan634/springbootapi-demo/actions/workflows/ci.yml/badge.svg)

基於 Java 21 + Spring Boot 3.2 DEMO用的零售業 ERP 系統後端 API。

## 技能樹 請點以下技能

| 技能 | 版本 | 說明 |
|------|------|------|
| Java | 21 | 程式語言 |
| Spring Boot | 3.2.2 | 核心框架 |
| Spring Data JPA | 3.2.2 | ORM 框架 |
| Spring Security | 3.2.2 | 安全框架 |
| MySQL | 8.4 | 資料庫 |
| Redis | 7 | 快取服務 |
| Flyway | - | 資料庫遷移 |
| JWT (JJWT) | 0.12.5 | Token 認證 |
| SpringDoc OpenAPI | 2.3.0 | API 文件 (Swagger) |
| Docker | - | 容器化佈署 |

## 功能模組

- **accounts** - 帳號管理（使用者、角色、門市、JWT 認證）
- **customers** - 客戶管理（會員、會員等級、積分）
- **products** - 商品管理（商品、分類、單位、稅別、條碼）
- **purchasing** - 採購管理（供應商）
- **inventory** - 庫存管理（庫存、庫存異動、預留機制）
- **sales** - 銷售管理（訂單、訂單明細、付款）
- **promotions** - 促銷管理（促銷活動、優惠券）

## 快速開始

### 環境需求

- Docker & Docker Compose
- 或 JDK 21 + Maven + MySQL 8.4 + Redis

### 使用 Docker 佈署（推薦）

```bash
# 啟動所有服務
docker-compose up -d

# 查看服務狀態
docker-compose ps

# 查看日誌
docker-compose logs -f app

# 停止服務
docker-compose down
```

### 本地開發

```bash
# 編譯專案
./mvnw clean package -DskipTests

# 執行應用程式
./mvnw spring-boot:run -Dspring-boot.run.profiles=dev
```

## Port

| 服務 | Port | 說明 |
|------|------|------|
| Spring Boot API | 8005 | REST API 服務 |
| MySQL | 3305 | 資料庫 |
| Redis | 6385 | 快取服務 |

## API 文件

啟動服務後，訪問 Swagger UI：

- **Swagger UI**: http://localhost:8005/swagger-ui.html
- **OpenAPI JSON**: http://localhost:8005/api-docs

## 測試資訊

### 測試帳號

所有帳號的密碼都是：`password123`

| 帳號 | 角色 | 說明 |
|------|------|------|
| admin | 系統管理員 | 擁有所有權限 |
| manager01 | 門市店長 | 門市管理權限 |
| cashier01 | 收銀員 | 收銀台操作權限 |
| cashier02 | 收銀員 | 收銀台操作權限 |
| warehouse01 | 倉管人員 | 倉庫管理權限 |

### 測試資料

系統已預載以下種子資料：

| 資料類型 | 數量 | 說明 |
|----------|------|------|
| 角色 | 5 | ADMIN, MANAGER, CASHIER, WAREHOUSE, VIEWER |
| 門市/倉庫 | 6 | 1 總公司 + 3 門市 + 2 物流中心 |
| 使用者 | 5 | 含各角色使用者 |
| 商品分類 | 8 | 含階層分類 |
| 計量單位 | 7 | 個、盒、包、瓶、罐、組、公斤 |
| 稅別 | 3 | 應稅5%、零稅率、免稅 |
| 商品 | 10 | 3C 產品、零食、飲料 |
| 會員等級 | 4 | 一般、銀卡、金卡、VIP |
| 會員 | 5 | 不同等級的會員 |
| 供應商 | 4 | 各類供應商 |
| 庫存 | 10 | 不同門市/倉庫的庫存 |
| 促銷活動 | 3 | 各類促銷活動 |
| 優惠券 | 3 | 各類優惠券 |
| 訂單 | 5 | 含不同狀態的訂單 |

### 重置測試資料

```bash
# 用 Script 重設
./scripts/seed.sh reset

# 或直接執行 SQL
docker exec -i erp-mysql mysql -uroot -pdev123 springbootdemo_db < scripts/reset-seed-data.sql
```

## 執行測試

### 單元測試

```bash
# 執行所有測試
./mvnw test

# 執行特定模組測試
./mvnw test -Dtest=AuthControllerTest
./mvnw test -Dtest=ProductControllerTest
./mvnw test -Dtest=CustomerControllerTest
./mvnw test -Dtest=OrderControllerTest
./mvnw test -Dtest=InventoryControllerTest
./mvnw test -Dtest=SupplierControllerTest
./mvnw test -Dtest=PromotionControllerTest
./mvnw test -Dtest=CouponControllerTest
```

### 測試覆蓋範圍

| 模組 | 測試類別 | 測試項目 |
|------|----------|----------|
| 認證 | AuthControllerTest | 登入、登出、Token 刷新、驗證錯誤處理 |
| 商品 | ProductControllerTest | CRUD、搜尋、按 SKU/條碼查詢 |
| 客戶 | CustomerControllerTest | CRUD、會員等級管理 |
| 庫存 | InventoryControllerTest | 查詢、調整庫存、異動記錄 |
| 訂單 | OrderControllerTest | CRUD、取消訂單、按狀態/門市查詢 |
| 供應商 | SupplierControllerTest | CRUD |
| 促銷 | PromotionControllerTest | CRUD |
| 優惠券 | CouponControllerTest | CRUD、按代碼查詢 |

## API 使用範例

### 登入取得 Token

```bash
curl -X POST http://localhost:8005/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username": "admin", "password": "password123"}'
```

回應：
```json
{
  "success": true,
  "message": "登入成功",
  "data": {
    "accessToken": "eyJhbGciOiJIUzI1NiJ9...",
    "refreshToken": "eyJhbGciOiJIUzI1NiJ9...",
    "user": {
      "id": 1,
      "username": "admin",
      "name": "系統管理員"
    }
  }
}
```

### 查詢商品列表

```bash
curl -X GET http://localhost:8005/api/v1/products \
  -H "Authorization: Bearer {YOUR_TOKEN}"
```

### 建立訂單

```bash
curl -X POST http://localhost:8005/api/v1/orders \
  -H "Authorization: Bearer {YOUR_TOKEN}" \
  -H "Content-Type: application/json" \
  -d '{
    "storeId": 2,
    "customerId": 1,
    "items": [
      {"productId": 1, "quantity": 2, "unitPrice": 199.00}
    ]
  }'
```

## 專案結構

```
springbootapi-demo/
├── docker-compose.yml          # Docker Compose 配置
├── Dockerfile                  # Docker 映像配置
├── pom.xml                     # Maven 配置
├── scripts/
│   ├── seed.sh                 # Seed 資料執行語法
│   └── reset-seed-data.sql     # 重置資料 SQL
├── docker/
│   └── mysql/
│       ├── conf.d/             # MySQL 配置
│       └── init/               # 初始化語法
│           └── 01-seed-data.sql
├── src/
│   ├── main/
│   │   ├── java/com/demo/erpapi/
│   │   │   ├── accounts/       # 帳號模組
│   │   │   ├── customers/      # 客戶模組
│   │   │   ├── products/       # 商品模組
│   │   │   ├── inventory/      # 庫存模組
│   │   │   ├── sales/          # 銷售模組
│   │   │   ├── purchasing/     # 採購模組
│   │   │   ├── promotions/     # 促銷模組
│   │   │   ├── common/         # 通用模組
│   │   │   ├── config/         # 配置模組
│   │   │   └── security/       # 安全模組
│   │   └── resources/
│   │       ├── application.yml
│   │       ├── application-dev.yml
│   │       ├── application-docker.yml
│   │       ├── logback-spring.xml
│   │       └── db/migration/   # Flyway 遷移語法
│   └── test/
│       ├── java/com/demo/erpapi/
│       │   ├── BaseIntegrationTest.java
│       │   ├── accounts/controller/
│       │   ├── products/controller/
│       │   ├── customers/controller/
│       │   ├── inventory/controller/
│       │   ├── sales/controller/
│       │   ├── purchasing/controller/
│       │   └── promotions/controller/
│       └── resources/
│           └── application-test.yml
```

## 資料庫連線

### Docker 環境

- Host: `localhost`
- Port: `3305`
- Database: `springbootdemo_db`
- Username: `root`
- Password: `dev123`

```bash
# 使用 MySQL 客戶端連線
mysql -h localhost -P 3305 -uroot -pdev123 springbootdemo_db

# 或進入 Docker 容器
docker exec -it erp-mysql mysql -uroot -pdev123 springbootdemo_db
```

## 健康檢查

```bash
# 檢查應用程式健康狀態
curl http://localhost:8005/actuator/health

# 檢查應用程式資訊
curl http://localhost:8005/actuator/info
```

## 常見問題

### Q: Docker 啟動失敗？

1. 確認 Docker 服務已啟動
2. 確認下列Ports 8005, 3305, 6385 未被佔用
3. 查看日誌：`docker-compose logs`

### Q: 登入失敗？

1. 確認使用正確的帳號密碼
2. 帳號連續錯誤 3 次會被鎖定 15 分鐘
3. 重置資料：`./scripts/seed.sh reset`

### Q: 測試失敗？

1. 確認使用 JDK 21
2. 測試使用 H2 內嵌資料庫，無需外部依賴
3. 執行：`./mvnw clean test`

## License

MIT License 
我一開始以為是Made In Taiwan 咧！(羞

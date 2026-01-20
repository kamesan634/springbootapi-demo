# 零售業 ERP 系統 API 文件

> Base URL: `http://localhost:8005/api/v1`
>
> 認證方式: Bearer Token (JWT)

---

## 目錄

1. [通用說明](#通用說明)
2. [認證 API](#認證-api)
3. [商品管理 API](#商品管理-api)
4. [分類管理 API](#分類管理-api)
5. [單位管理 API](#單位管理-api)
6. [稅別管理 API](#稅別管理-api)
7. [客戶管理 API](#客戶管理-api)
8. [訂單管理 API](#訂單管理-api)
9. [付款管理 API](#付款管理-api)
10. [門市/倉庫管理 API](#門市倉庫管理-api)
11. [庫存管理 API](#庫存管理-api)
12. [供應商管理 API](#供應商管理-api)
13. [促銷管理 API](#促銷管理-api)
14. [優惠券管理 API](#優惠券管理-api)
15. [報表 API](#報表-api)
16. [系統管理 API](#系統管理-api)

---

## 通用說明

### 統一回應格式

所有 API 回應都使用以下格式：

```json
{
  "success": true,
  "code": 200,
  "message": "操作成功",
  "data": { ... },
  "timestamp": "2024-01-20T10:30:00"
}
```

| 欄位 | 類型 | 說明 |
|------|------|------|
| success | boolean | 是否成功 |
| code | integer | HTTP 狀態碼 |
| message | string | 訊息說明 |
| data | object/array | 回傳資料 |
| timestamp | string | 回應時間 (ISO 8601) |

### 錯誤回應

```json
{
  "success": false,
  "code": 400,
  "message": "請求參數錯誤",
  "timestamp": "2024-01-20T10:30:00"
}
```

### 常見狀態碼

| 狀態碼 | 說明 |
|--------|------|
| 200 | 成功 |
| 400 | 請求參數錯誤 |
| 401 | 未授權（需要登入） |
| 403 | 權限不足 |
| 404 | 資源不存在 |
| 500 | 伺服器錯誤 |

### 分頁參數

支援分頁的 API 可使用以下 Query 參數：

| 參數 | 類型 | 預設值 | 說明 |
|------|------|--------|------|
| page | integer | 1 | 頁碼（從 1 開始） |
| size | integer | 20 | 每頁筆數 |
| sortBy | string | - | 排序欄位 |
| sortDir | string | asc | 排序方向 (asc/desc) |

### 分頁回應格式

```json
{
  "content": [...],
  "page": 1,
  "size": 20,
  "totalElements": 100,
  "totalPages": 5,
  "first": true,
  "last": false
}
```

---

## 認證 API

### POST /auth/login - 使用者登入

**Request Body:**
```json
{
  "username": "admin",
  "password": "password123"
}
```

| 欄位 | 類型 | 必填 | 說明 |
|------|------|------|------|
| username | string | ✓ | 使用者名稱 |
| password | string | ✓ | 密碼 |

**Response:**
```json
{
  "success": true,
  "code": 200,
  "message": "登入成功",
  "data": {
    "accessToken": "eyJhbGciOiJIUzI1NiJ9...",
    "refreshToken": "eyJhbGciOiJIUzI1NiJ9...",
    "tokenType": "Bearer",
    "expiresIn": 86400,
    "user": {
      "id": 1,
      "username": "admin",
      "name": "系統管理員",
      "email": "admin@erp.demo.com",
      "role": "ADMIN",
      "roleName": "系統管理員"
    },
    "passwordExpired": false,
    "passwordRemainingDays": 30,
    "passwordChangeRequired": false
  }
}
```

### POST /auth/refresh - 重新整理 Token

**Request Body:**
```json
{
  "refreshToken": "eyJhbGciOiJIUzI1NiJ9..."
}
```

**Response:**
```json
{
  "success": true,
  "code": 200,
  "message": "Token 已重新整理",
  "data": {
    "accessToken": "eyJhbGciOiJIUzI1NiJ9...",
    "refreshToken": "eyJhbGciOiJIUzI1NiJ9...",
    "tokenType": "Bearer",
    "expiresIn": 86400
  }
}
```

### POST /auth/logout - 登出

**Headers:**
```
Authorization: Bearer {accessToken}
```

**Response:**
```json
{
  "success": true,
  "code": 200,
  "message": "登出成功"
}
```

### POST /auth/change-password - 變更密碼

**Headers:**
```
Authorization: Bearer {accessToken}
```

**Request Body:**
```json
{
  "currentPassword": "password123",
  "newPassword": "newPassword456",
  "confirmPassword": "newPassword456"
}
```

---

## 商品管理 API

### GET /products - 查詢商品列表

**Query Parameters:**

| 參數 | 類型 | 說明 |
|------|------|------|
| page | integer | 頁碼（從 1 開始） |
| size | integer | 每頁筆數 |
| sortBy | string | 排序欄位 |
| sortDir | string | 排序方向 (asc/desc) |
| activeOnly | boolean | 是否只查詢啟用的商品 |

**Response:**
```json
{
  "success": true,
  "code": 200,
  "data": {
    "content": [
      {
        "id": 1,
        "sku": "PHN-CASE-001",
        "name": "iPhone 15 透明保護殼",
        "description": "高透明TPU材質，防摔防刮",
        "category": {
          "id": 2,
          "code": "ELEC-PHONE",
          "name": "手機配件",
          "fullPathName": "3C電子 > 手機配件"
        },
        "unit": {
          "id": 1,
          "code": "PCS",
          "name": "個"
        },
        "taxType": {
          "id": 1,
          "code": "TAX5",
          "name": "應稅5%",
          "rate": 0.05
        },
        "costPrice": 80.00,
        "sellingPrice": 199.00,
        "taxIncludedPrice": 208.95,
        "grossProfit": 119.00,
        "grossProfitMargin": 59.80,
        "barcode": "4710001000011",
        "safetyStock": 50,
        "active": true,
        "barcodes": [],
        "createdAt": "2024-01-15T10:00:00",
        "updatedAt": "2024-01-15T10:00:00"
      }
    ],
    "page": 1,
    "size": 20,
    "totalElements": 50,
    "totalPages": 3
  }
}
```

### GET /products/{id} - 取得商品詳情

**Response:** 同上單一商品格式

### GET /products/sku/{sku} - 依 SKU 查詢商品

### GET /products/barcode/{barcode} - 依條碼查詢商品

### GET /products/search - 搜尋商品

**Query Parameters:**

| 參數 | 類型 | 必填 | 說明 |
|------|------|------|------|
| keyword | string | ✓ | 搜尋關鍵字（名稱、SKU、條碼） |
| page | integer | | 頁碼 |
| size | integer | | 每頁筆數 |

### GET /products/category/{categoryId} - 依分類查詢商品

### POST /products - 建立商品

**Request Body:**
```json
{
  "sku": "SKU001",
  "name": "咖啡豆 250g",
  "description": "精選阿拉比卡咖啡豆",
  "categoryId": 1,
  "unitId": 1,
  "taxTypeId": 1,
  "costPrice": 150.00,
  "sellingPrice": 250.00,
  "barcode": "4710088123456",
  "safetyStock": 10,
  "active": true,
  "barcodes": [
    {
      "barcode": "4710088123457",
      "barcodeType": "EAN13",
      "notes": "包裝條碼"
    }
  ]
}
```

| 欄位 | 類型 | 必填 | 說明 |
|------|------|------|------|
| sku | string | ✓ | 商品貨號（唯一） |
| name | string | ✓ | 商品名稱 |
| description | string | | 商品描述 |
| categoryId | long | | 分類 ID |
| unitId | long | ✓ | 單位 ID |
| taxTypeId | long | ✓ | 稅別 ID |
| costPrice | decimal | | 成本價 |
| sellingPrice | decimal | ✓ | 售價 |
| barcode | string | | 主要條碼 |
| safetyStock | integer | | 安全庫存量 |
| active | boolean | | 是否啟用（預設 true） |
| barcodes | array | | 額外條碼列表 |

### PUT /products/{id} - 更新商品

**Request Body:** 同建立商品

### DELETE /products/{id} - 刪除商品（軟刪除）

### DELETE /products/{id}/permanent - 永久刪除商品

---

## 分類管理 API

### GET /categories - 查詢分類列表

**Query Parameters:**

| 參數 | 類型 | 說明 |
|------|------|------|
| page | integer | 頁碼 |
| size | integer | 每頁筆數 |
| activeOnly | boolean | 是否只查詢啟用的分類 |

### GET /categories/{id} - 根據 ID 查詢分類

### GET /categories/code/{code} - 根據代碼查詢分類

### GET /categories/tree - 取得分類樹狀結構

**Query Parameters:**

| 參數 | 類型 | 說明 |
|------|------|------|
| includeInactive | boolean | 是否包含停用的分類 |

**Response:**
```json
{
  "success": true,
  "code": 200,
  "data": [
    {
      "id": 1,
      "code": "FOOD",
      "name": "食品",
      "sortOrder": 1,
      "active": true,
      "children": [
        {
          "id": 2,
          "code": "BEVERAGE",
          "name": "飲料",
          "parentId": 1,
          "sortOrder": 1,
          "active": true,
          "children": []
        }
      ]
    }
  ]
}
```

### GET /categories/{parentId}/children - 取得子分類

### GET /categories/search - 搜尋分類

**Query Parameters:**

| 參數 | 類型 | 必填 | 說明 |
|------|------|------|------|
| keyword | string | ✓ | 搜尋關鍵字 |

### POST /categories - 建立分類

**Request Body:**
```json
{
  "code": "BEVERAGE",
  "name": "飲料",
  "description": "各類飲品",
  "parentId": 1,
  "sortOrder": 1,
  "active": true
}
```

### PUT /categories/{id} - 更新分類

### DELETE /categories/{id} - 刪除分類

---

## 單位管理 API

### GET /units - 查詢單位列表

**Query Parameters:**

| 參數 | 類型 | 說明 |
|------|------|------|
| page | integer | 頁碼（從 1 開始） |
| size | integer | 每頁筆數 |
| sortBy | string | 排序欄位（預設 code） |
| sortDir | string | 排序方向 (asc/desc) |

**Response:**
```json
{
  "success": true,
  "code": 200,
  "data": {
    "content": [
      {
        "id": 1,
        "code": "PCS",
        "name": "個",
        "active": true,
        "createdAt": "2024-01-01T00:00:00",
        "updatedAt": "2024-01-01T00:00:00"
      },
      {
        "id": 2,
        "code": "BOX",
        "name": "盒",
        "active": true,
        "createdAt": "2024-01-01T00:00:00",
        "updatedAt": "2024-01-01T00:00:00"
      }
    ],
    "page": 1,
    "size": 20,
    "totalElements": 6,
    "totalPages": 1
  }
}
```

### GET /units/{id} - 根據 ID 查詢單位

### GET /units/code/{code} - 根據代碼查詢單位

### GET /units/active - 查詢啟用的單位

### GET /units/search - 搜尋單位

**Query Parameters:**

| 參數 | 類型 | 必填 | 說明 |
|------|------|------|------|
| keyword | string | ✓ | 搜尋關鍵字（名稱、代碼） |

### POST /units - 建立單位

**Request Body:**
```json
{
  "code": "KG",
  "name": "公斤",
  "active": true
}
```

### PUT /units/{id} - 更新單位

### DELETE /units/{id} - 刪除單位

---

## 稅別管理 API

### GET /tax-types - 查詢稅別列表

**Query Parameters:**

| 參數 | 類型 | 說明 |
|------|------|------|
| page | integer | 頁碼（從 1 開始） |
| size | integer | 每頁筆數 |

**Response:**
```json
{
  "success": true,
  "code": 200,
  "data": {
    "content": [
      {
        "id": 1,
        "code": "TAX5",
        "name": "應稅5%",
        "rate": 5.00,
        "isDefault": true,
        "active": true
      },
      {
        "id": 2,
        "code": "ZERO",
        "name": "零稅率",
        "rate": 0.00,
        "isDefault": false,
        "active": true
      }
    ]
  }
}
```

### GET /tax-types/{id} - 根據 ID 查詢稅別

### GET /tax-types/code/{code} - 根據代碼查詢稅別

### GET /tax-types/active - 查詢啟用的稅別

### GET /tax-types/default - 取得預設稅別

### POST /tax-types - 建立稅別

**Request Body:**
```json
{
  "code": "TAX5",
  "name": "應稅5%",
  "rate": 5.00,
  "isDefault": false,
  "active": true
}
```

### PUT /tax-types/{id} - 更新稅別

### DELETE /tax-types/{id} - 刪除稅別

---

## 客戶管理 API

### GET /customers - 查詢客戶列表

**Query Parameters:**

| 參數 | 類型 | 說明 |
|------|------|------|
| page | integer | 頁碼（從 1 開始） |
| size | integer | 每頁筆數 |
| sortBy | string | 排序欄位 |
| sortDir | string | 排序方向 |

**Response:**
```json
{
  "success": true,
  "code": 200,
  "data": {
    "content": [
      {
        "id": 1,
        "memberNo": "M202401010001",
        "name": "林小明",
        "phone": "0912-111-111",
        "email": "lin.ming@example.com",
        "gender": "MALE",
        "genderDisplay": "男",
        "birthday": "1990-05-15",
        "age": 35,
        "birthdayToday": false,
        "birthdayMonth": false,
        "level": {
          "id": 3,
          "code": "GOLD",
          "name": "金卡會員",
          "discountRate": 0.90,
          "pointsMultiplier": 2.0
        },
        "totalPoints": 2500,
        "totalSpent": 25000.00,
        "registerDate": "2024-01-01T00:00:00",
        "active": true,
        "notes": "VIP 客戶"
      }
    ]
  }
}
```

### GET /customers/{id} - 根據 ID 查詢客戶

### GET /customers/member-no/{memberNo} - 根據會員編號查詢

### GET /customers/search - 搜尋客戶

**Query Parameters:**

| 參數 | 類型 | 必填 | 說明 |
|------|------|------|------|
| keyword | string | ✓ | 搜尋關鍵字（姓名、手機、Email、會員編號） |

### GET /customers/filter - 複合條件查詢

**Query Parameters:**

| 參數 | 類型 | 說明 |
|------|------|------|
| keyword | string | 搜尋關鍵字 |
| levelId | long | 等級 ID |
| active | boolean | 是否啟用 |
| gender | string | 性別 (M/F/O) |

### GET /customers/birthday/today - 今日壽星

### GET /customers/birthday/month - 本月壽星

### GET /customers/statistics - 會員統計

**Response:**
```json
{
  "success": true,
  "code": 200,
  "data": {
    "totalCustomers": 1500,
    "activeCustomers": 1200,
    "todayRegistrations": 5,
    "todayBirthdays": 3
  }
}
```

### POST /customers - 建立客戶

**Request Body:**
```json
{
  "name": "王小明",
  "phone": "0912-345-678",
  "email": "wang@example.com",
  "gender": "MALE",
  "birthday": "1990-05-15",
  "address": "台北市信義區...",
  "levelId": 1,
  "notes": "VIP 客戶"
}
```

### PUT /customers/{id} - 更新客戶

### DELETE /customers/{id} - 刪除客戶（軟刪除）

### POST /customers/{id}/points/add - 新增點數

**Query Parameters:**

| 參數 | 類型 | 必填 | 說明 |
|------|------|------|------|
| points | integer | ✓ | 增加的點數 |
| reason | string | | 原因說明 |

### POST /customers/{id}/points/deduct - 扣除點數

**Query Parameters:**

| 參數 | 類型 | 必填 | 說明 |
|------|------|------|------|
| points | integer | ✓ | 扣除的點數 |
| reason | string | | 原因說明 |

### GET /customers/{id}/points/calculate - 計算消費可獲得點數

**Query Parameters:**

| 參數 | 類型 | 必填 | 說明 |
|------|------|------|------|
| spentAmount | decimal | ✓ | 消費金額 |

### POST /customers/{id}/spending - 記錄消費

**Query Parameters:**

| 參數 | 類型 | 必填 | 說明 |
|------|------|------|------|
| spentAmount | decimal | ✓ | 消費金額 |

---

## 訂單管理 API

### GET /orders - 查詢訂單列表

**Query Parameters:**

| 參數 | 類型 | 說明 |
|------|------|------|
| orderNoKeyword | string | 訂單編號關鍵字 |
| storeId | long | 門市 ID |
| customerId | long | 客戶 ID |
| status | string | 訂單狀態 (PENDING/PAID/CANCELLED/REFUNDED) |
| startDate | date | 開始日期 (yyyy-MM-dd) |
| endDate | date | 結束日期 (yyyy-MM-dd) |

**Response:**
```json
{
  "success": true,
  "code": 200,
  "data": {
    "content": [
      {
        "id": 1,
        "orderNo": "ORD20240120001",
        "storeId": 1,
        "storeName": "台北旗艦店",
        "customerId": 100,
        "customerName": "王小明",
        "orderDate": "2024-01-20",
        "orderTime": "14:30:00",
        "subtotal": 1500.00,
        "discountAmount": 100.00,
        "taxAmount": 70.00,
        "totalAmount": 1470.00,
        "paidAmount": 1470.00,
        "changeAmount": 30.00,
        "pointsEarned": 14,
        "pointsUsed": 0,
        "status": "PAID",
        "statusName": "已付款",
        "notes": "請盡快出貨",
        "items": [
          {
            "id": 1,
            "productId": 100,
            "productSku": "SKU001",
            "productName": "咖啡豆 250g",
            "quantity": 2,
            "unitPrice": 750.00,
            "discountAmount": 0,
            "subtotal": 1500.00
          }
        ],
        "createdAt": "2024-01-20T14:30:00"
      }
    ]
  }
}
```

### GET /orders/{id} - 取得訂單詳情

### POST /orders - 建立訂單

**Request Body:**
```json
{
  "storeId": 1,
  "customerId": 100,
  "items": [
    {
      "productId": 100,
      "quantity": 2,
      "unitPrice": 750.00,
      "discountAmount": 0
    }
  ],
  "discountAmount": 100.00,
  "taxAmount": 70.00,
  "notes": "請盡快出貨"
}
```

| 欄位 | 類型 | 必填 | 說明 |
|------|------|------|------|
| storeId | long | ✓ | 門市 ID |
| customerId | long | | 客戶 ID（非會員可不填） |
| items | array | ✓ | 訂單明細（至少一項） |
| items[].productId | long | ✓ | 商品 ID |
| items[].quantity | integer | ✓ | 數量（>0） |
| items[].unitPrice | decimal | ✓ | 單價 |
| items[].discountAmount | decimal | | 折扣金額 |
| discountAmount | decimal | | 訂單折扣 |
| taxAmount | decimal | | 稅額 |
| notes | string | | 備註 |

### POST /orders/{id}/cancel - 取消訂單

### POST /orders/{id}/refund - 申請退款

---

## 付款管理 API

### POST /payments - 處理付款

**Request Body:**
```json
{
  "orderId": 1,
  "paymentMethod": "CASH",
  "amount": 1500.00,
  "receivedAmount": 2000.00,
  "referenceNo": "TXN123456"
}
```

| 欄位 | 類型 | 必填 | 說明 |
|------|------|------|------|
| orderId | long | ✓ | 訂單 ID |
| paymentMethod | string | ✓ | 付款方式 (CASH/CREDIT_CARD/DEBIT_CARD/LINE_PAY/...) |
| amount | decimal | ✓ | 付款金額 |
| receivedAmount | decimal | | 實收金額（現金時用） |
| referenceNo | string | | 交易參考號 |

**Response:**
```json
{
  "success": true,
  "code": 200,
  "data": {
    "id": 1,
    "orderId": 1,
    "orderNo": "ORD20240120001",
    "paymentMethod": "CASH",
    "paymentMethodName": "現金",
    "amount": 1500.00,
    "receivedAmount": 2000.00,
    "changeAmount": 500.00,
    "status": "COMPLETED",
    "paidAt": "2024-01-20T14:35:00"
  }
}
```

---

## 門市/倉庫管理 API

### GET /stores - 查詢門市/倉庫列表

**Query Parameters:**

| 參數 | 類型 | 說明 |
|------|------|------|
| page | integer | 頁碼（從 1 開始） |
| size | integer | 每頁筆數 |
| sortBy | string | 排序欄位（預設 sortOrder） |
| sortDir | string | 排序方向 (asc/desc) |
| type | string | 類型篩選 (STORE/WAREHOUSE) |

**Response:**
```json
{
  "success": true,
  "code": 200,
  "data": {
    "content": [
      {
        "id": 1,
        "code": "HQ",
        "name": "總公司",
        "type": "WAREHOUSE",
        "typeName": "倉庫",
        "address": "台北市信義區信義路五段7號",
        "phone": "02-2345-6789",
        "email": null,
        "businessHours": null,
        "main": false,
        "active": true,
        "sortOrder": 0,
        "notes": null,
        "createdAt": "2024-01-20T00:00:00",
        "updatedAt": "2024-01-20T00:00:00"
      },
      {
        "id": 2,
        "code": "S001",
        "name": "台北旗艦店",
        "type": "STORE",
        "typeName": "門市",
        "address": "台北市大安區忠孝東路四段123號",
        "phone": "02-2771-1234",
        "email": null,
        "businessHours": null,
        "main": false,
        "active": true,
        "sortOrder": 0,
        "notes": null,
        "createdAt": "2024-01-20T00:00:00",
        "updatedAt": "2024-01-20T00:00:00"
      }
    ],
    "page": 1,
    "size": 20,
    "totalElements": 6,
    "totalPages": 1
  }
}
```

### GET /stores/{id} - 根據 ID 查詢門市/倉庫

### GET /stores/code/{code} - 根據代碼查詢

### GET /stores/active - 查詢所有啟用的門市/倉庫

**Response:**
```json
{
  "success": true,
  "code": 200,
  "data": [
    {
      "id": 1,
      "code": "HQ",
      "name": "總公司",
      "type": "WAREHOUSE",
      "typeName": "倉庫",
      "active": true
    },
    {
      "id": 2,
      "code": "S001",
      "name": "台北旗艦店",
      "type": "STORE",
      "typeName": "門市",
      "active": true
    }
  ]
}
```

### GET /stores/type/store - 查詢所有門市

### GET /stores/type/warehouse - 查詢所有倉庫

### GET /stores/main - 查詢主倉庫/總部

### POST /stores - 建立門市/倉庫

**Request Body:**
```json
{
  "code": "S004",
  "name": "高雄旗艦店",
  "type": "STORE",
  "address": "高雄市前鎮區中山路100號",
  "phone": "07-1234-5678",
  "email": "kaohsiung@example.com",
  "businessHours": "10:00-22:00",
  "main": false,
  "active": true,
  "sortOrder": 4,
  "notes": "南部旗艦店"
}
```

| 欄位 | 類型 | 必填 | 說明 |
|------|------|------|------|
| code | string | ✓ | 代碼（唯一） |
| name | string | ✓ | 名稱 |
| type | string | | 類型 (STORE/WAREHOUSE) |
| address | string | | 地址 |
| phone | string | | 電話 |
| email | string | | Email |
| businessHours | string | | 營業時間 |
| main | boolean | | 是否為主倉庫/總部 |
| active | boolean | | 是否啟用（預設 true） |
| sortOrder | integer | | 排序順序 |
| notes | string | | 備註 |

### PUT /stores/{id} - 更新門市/倉庫

### DELETE /stores/{id} - 刪除門市/倉庫（軟刪除）

---

## 庫存管理 API

### GET /inventory/all - 查詢所有庫存（分頁）

**Query Parameters:**

| 參數 | 類型 | 說明 |
|------|------|------|
| page | integer | 頁碼（從 0 開始） |
| size | integer | 每頁筆數（預設 20） |
| sort | string | 排序（預設 productId,asc） |

**Response:**
```json
{
  "success": true,
  "code": 200,
  "data": {
    "content": [
      {
        "id": 1,
        "productId": 100,
        "productSku": "SKU001",
        "productName": "咖啡豆 250g",
        "warehouseId": 1,
        "warehouseName": "總倉",
        "quantity": 100,
        "reservedQuantity": 10,
        "availableQuantity": 90,
        "lastMovementDate": "2024-01-20T10:00:00"
      }
    ],
    "totalElements": 50,
    "totalPages": 3
  }
}
```

### GET /inventory - 查詢特定庫存

**Query Parameters:**

| 參數 | 類型 | 必填 | 說明 |
|------|------|------|------|
| productId | long | ✓ | 商品 ID |
| warehouseId | long | ✓ | 倉庫/門市 ID |

**Response:**
```json
{
  "success": true,
  "code": 200,
  "data": {
    "id": 1,
    "productId": 100,
    "productSku": "SKU001",
    "productName": "咖啡豆 250g",
    "warehouseId": 1,
    "warehouseName": "總倉",
    "quantity": 100,
    "reservedQuantity": 10,
    "availableQuantity": 90,
    "lastMovementDate": "2024-01-20T10:00:00"
  }
}
```

### GET /inventory/product/{productId} - 查詢商品所有倉庫庫存

### GET /inventory/product/{productId}/total - 查詢商品總庫存

### GET /inventory/product/{productId}/available - 查詢商品可用庫存

### GET /inventory/warehouse/{warehouseId} - 查詢倉庫所有庫存

### GET /inventory/low-stock - 取得低庫存警告

**Query Parameters:**

| 參數 | 類型 | 必填 | 說明 |
|------|------|------|------|
| threshold | integer | ✓ | 安全庫存閾值 |

**Response:**
```json
{
  "success": true,
  "code": 200,
  "data": {
    "content": [
      {
        "productId": 100,
        "productSku": "SKU001",
        "productName": "咖啡豆 250g",
        "warehouseId": 1,
        "warehouseName": "總倉",
        "quantity": 5,
        "reservedQuantity": 0,
        "availableQuantity": 5
      }
    ]
  }
}
```

### POST /inventory/adjust - 調整庫存

**Request Body:**
```json
{
  "productId": 100,
  "warehouseId": 1,
  "adjustmentType": "STOCK_IN",
  "quantity": 50,
  "reason": "進貨入庫",
  "referenceNo": "PO20240120001"
}
```

| 欄位 | 類型 | 必填 | 說明 |
|------|------|------|------|
| productId | long | ✓ | 商品 ID |
| warehouseId | long | ✓ | 倉庫 ID |
| adjustmentType | string | ✓ | 調整類型 (STOCK_IN/STOCK_OUT/ADJUSTMENT_IN/ADJUSTMENT_OUT) |
| quantity | integer | ✓ | 數量 |
| reason | string | | 原因 |
| referenceNo | string | | 參考單號 |

### GET /inventory/movements - 查詢庫存異動記錄

**Query Parameters:**

| 參數 | 類型 | 必填 | 說明 |
|------|------|------|------|
| productId | long | ✓ | 商品 ID |
| warehouseId | long | ✓ | 倉庫 ID |

### GET /inventory/movements/product/{productId} - 查詢商品異動記錄

### GET /inventory/movements/warehouse/{warehouseId} - 查詢倉庫異動記錄

### GET /inventory/movements/reference/{referenceNo} - 依單號查詢異動

### GET /inventory/movements/search - 複合條件查詢異動

**Query Parameters:**

| 參數 | 類型 | 必填 | 說明 |
|------|------|------|------|
| productId | long | | 商品 ID |
| warehouseId | long | | 倉庫 ID |
| movementType | string | | 異動類型 |
| startTime | datetime | ✓ | 開始時間 (ISO 8601) |
| endTime | datetime | ✓ | 結束時間 (ISO 8601) |

### GET /inventory/movement-types - 取得異動類型列表

---

## 供應商管理 API

### GET /suppliers - 查詢供應商列表

### GET /suppliers/{id} - 查詢供應商詳情

### POST /suppliers - 建立供應商

**Request Body:**
```json
{
  "code": "SUP001",
  "name": "咖啡原物料供應商",
  "contactPerson": "李經理",
  "phone": "02-1234-5678",
  "email": "supplier@example.com",
  "address": "台北市內湖區...",
  "taxId": "12345678",
  "paymentTerms": "月結30天",
  "bankAccount": "012-123456789",
  "notes": "主要咖啡豆供應商"
}
```

### PUT /suppliers/{id} - 更新供應商

### DELETE /suppliers/{id} - 刪除供應商

---

## 促銷管理 API

### GET /promotions - 查詢促銷活動

### GET /promotions/ongoing - 取得進行中的促銷

### POST /promotions - 建立促銷活動

**Request Body:**
```json
{
  "name": "新春特賣",
  "description": "農曆新年全館 85 折",
  "type": "PERCENTAGE_DISCOUNT",
  "discountValue": 15,
  "startDate": "2024-02-01",
  "endDate": "2024-02-15",
  "minPurchaseAmount": 500,
  "maxDiscountAmount": 1000,
  "applicableProducts": [1, 2, 3],
  "applicableCategories": [1],
  "active": true
}
```

| 欄位 | 類型 | 必填 | 說明 |
|------|------|------|------|
| name | string | ✓ | 活動名稱 |
| type | string | ✓ | 類型 (PERCENTAGE_DISCOUNT/FIXED_DISCOUNT/BUY_X_GET_Y/BUNDLE) |
| discountValue | decimal | ✓ | 折扣值 |
| startDate | date | ✓ | 開始日期 |
| endDate | date | ✓ | 結束日期 |
| minPurchaseAmount | decimal | | 最低消費金額 |
| maxDiscountAmount | decimal | | 最高折扣金額 |

---

## 優惠券管理 API

### GET /coupons - 查詢優惠券

### GET /coupons/code/{code} - 依代碼查詢優惠券

### POST /coupons - 建立優惠券

**Request Body:**
```json
{
  "code": "NEWYEAR2024",
  "name": "新年優惠券",
  "description": "滿千折百",
  "discountType": "FIXED",
  "discountValue": 100,
  "minPurchaseAmount": 1000,
  "maxUsageCount": 1000,
  "maxUsagePerCustomer": 1,
  "startDate": "2024-02-01",
  "endDate": "2024-02-29",
  "active": true
}
```

### POST /coupons/validate - 驗證優惠券

**Request Body:**
```json
{
  "code": "NEWYEAR2024",
  "customerId": 100,
  "orderAmount": 1500
}
```

**Response:**
```json
{
  "success": true,
  "code": 200,
  "data": {
    "valid": true,
    "couponId": 1,
    "code": "NEWYEAR2024",
    "discountAmount": 100.00,
    "message": "優惠券可使用"
  }
}
```

### POST /coupons/use - 使用優惠券

**Request Body:**
```json
{
  "code": "NEWYEAR2024",
  "customerId": 100,
  "orderId": 1
}
```

---

## 報表 API

### GET /dashboard/summary - 取得儀表板資料

**Response:**
```json
{
  "success": true,
  "code": 200,
  "data": {
    "todaySales": 125000.00,
    "monthSales": 3250000.00,
    "yearSales": 15000000.00,
    "todayOrderCount": 85,
    "monthOrderCount": 2100,
    "salesGrowthRate": 15.5,
    "totalProducts": 500,
    "lowStockProducts": 12,
    "outOfStockProducts": 3,
    "inventoryValue": 2500000.00,
    "pendingPurchaseOrders": 5,
    "monthPurchaseAmount": 800000.00,
    "totalCustomers": 1500,
    "newCustomersThisMonth": 45,
    "salesChart": [
      {"date": "2024-01-14", "sales": 115000, "orderCount": 75},
      {"date": "2024-01-15", "sales": 125000, "orderCount": 85}
    ],
    "categorySales": [
      {"categoryId": 1, "categoryName": "飲料", "sales": 50000, "percentage": 40}
    ],
    "topProducts": [
      {"productId": 1, "productName": "咖啡豆", "quantity": 150, "sales": 37500}
    ]
  }
}
```

### GET /reports/sales - 取得銷售報表

**Query Parameters:**

| 參數 | 類型 | 必填 | 說明 |
|------|------|------|------|
| startDate | date | ✓ | 開始日期 |
| endDate | date | ✓ | 結束日期 |
| storeId | long | | 門市 ID |

### GET /reports/profit-analysis - 取得利潤分析

**Query Parameters:**

| 參數 | 類型 | 必填 | 說明 |
|------|------|------|------|
| startDate | date | ✓ | 開始日期 |
| endDate | date | ✓ | 結束日期 |
| storeId | long | | 門市 ID |

**Response:**
```json
{
  "success": true,
  "code": 200,
  "data": {
    "startDate": "2024-01-01",
    "endDate": "2024-01-31",
    "totalRevenue": 3250000.00,
    "totalCost": 1950000.00,
    "grossProfit": 1300000.00,
    "grossProfitMargin": 40.00,
    "orderCount": 2100,
    "averageOrderValue": 1547.62,
    "averageOrderProfit": 619.05,
    "dailyProfits": [
      {
        "date": "2024-01-01",
        "orderCount": 70,
        "revenue": 105000,
        "cost": 63000,
        "grossProfit": 42000,
        "profitMargin": 40
      }
    ]
  }
}
```

### GET /reports/comparison/yoy - 年對年比較

**Query Parameters:**

| 參數 | 類型 | 說明 |
|------|------|------|
| year | integer | 年份 |
| month | integer | 月份 |

### GET /reports/comparison/mom - 月對月比較

---

## 系統管理 API

### GET /system-parameters - 取得系統參數

### PUT /system-parameters/{category}/{key} - 更新系統參數

**Request Body:**
```json
{
  "value": "100"
}
```

### GET /audit-logs - 查詢稽核日誌

**Query Parameters:**

| 參數 | 類型 | 說明 |
|------|------|------|
| userId | long | 使用者 ID |
| entityType | string | 實體類型 |
| action | string | 動作 (CREATE/UPDATE/DELETE) |
| startDate | datetime | 開始時間 |
| endDate | datetime | 結束時間 |

**Response:**
```json
{
  "success": true,
  "code": 200,
  "data": {
    "content": [
      {
        "id": 1,
        "userId": 1,
        "userName": "admin",
        "action": "UPDATE",
        "entityType": "Product",
        "entityId": 100,
        "oldValue": "{\"sellingPrice\": 200}",
        "newValue": "{\"sellingPrice\": 250}",
        "ipAddress": "192.168.1.100",
        "createdAt": "2024-01-20T10:30:00"
      }
    ]
  }
}
```

---

## 付款方式對照表

| 代碼 | 說明 |
|------|------|
| CASH | 現金 |
| CREDIT_CARD | 信用卡 |
| DEBIT_CARD | 簽帳卡 |
| LINE_PAY | LINE Pay |
| APPLE_PAY | Apple Pay |
| GOOGLE_PAY | Google Pay |
| JKOPAY | 街口支付 |
| POINTS | 點數折抵 |

## 訂單狀態對照表

| 代碼 | 說明 |
|------|------|
| PENDING | 待處理 |
| PAID | 已付款 |
| CANCELLED | 已取消 |
| REFUNDED | 已退款 |

## 促銷類型對照表

| 代碼 | 說明 |
|------|------|
| PERCENTAGE_DISCOUNT | 百分比折扣 |
| FIXED_DISCOUNT | 固定金額折扣 |
| BUY_X_GET_Y | 買X送Y |
| BUNDLE | 組合優惠 |
| FREE_SHIPPING | 免運費 |

## 門市/倉庫類型對照表

| 代碼 | 說明 |
|------|------|
| STORE | 門市 |
| WAREHOUSE | 倉庫 |

## 庫存異動類型對照表

| 代碼 | 說明 |
|------|------|
| STOCK_IN | 進貨入庫 |
| STOCK_OUT | 銷售出庫 |
| ADJUSTMENT_IN | 盤盈入庫 |
| ADJUSTMENT_OUT | 盤虧出庫 |
| TRANSFER_IN | 調撥入庫 |
| TRANSFER_OUT | 調撥出庫 |
| RETURN_IN | 退貨入庫 |
| RETURN_OUT | 退貨出庫 |

---

## 測試帳號

| 帳號 | 密碼 | 角色 |
|------|------|------|
| admin | password123 | 系統管理員 |
| manager01 | password123 | 門市店長 |
| cashier01 | password123 | 收銀員 |

---

## Swagger UI

API 文件也可透過 Swagger UI 瀏覽：
```
http://localhost:8005/swagger-ui/index.html
```

OpenAPI 規格文件：
```
http://localhost:8005/v3/api-docs
```

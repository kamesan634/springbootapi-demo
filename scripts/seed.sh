#!/bin/bash
# =============================================================================
# 零售業簡易ERP系統 - Seed Data 執行腳本
# =============================================================================
# 說明：用於載入或重置測試資料
# 使用方式：
#   ./scripts/seed.sh          # 載入種子資料
#   ./scripts/seed.sh reset    # 重置種子資料（清除後重新載入）
# =============================================================================

set -e

# 資料庫連線參數
DB_HOST="${DB_HOST:-localhost}"
DB_PORT="${DB_PORT:-3305}"
DB_NAME="${DB_NAME:-springbootapi_demo_db}"
DB_USER="${DB_USER:-root}"
DB_PASS="${DB_PASS:-dev123}"

# 腳本目錄
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_DIR="$(dirname "$SCRIPT_DIR")"

echo "=========================================="
echo " ERP 系統 - Seed Data 工具"
echo "=========================================="
echo "Database: ${DB_HOST}:${DB_PORT}/${DB_NAME}"
echo ""

# 檢查是否在 Docker 環境
if [ -f /.dockerenv ]; then
    echo "偵測到 Docker 環境..."
    DB_HOST="mysql"
    DB_PORT="3306"
fi

# 執行 SQL 檔案的函數
execute_sql() {
    local sql_file="$1"
    echo "執行: $(basename "$sql_file")..."

    if command -v docker &> /dev/null && docker ps | grep -q erp-mysql; then
        # 使用 Docker 容器執行
        docker exec -i erp-mysql mysql -u${DB_USER} -p${DB_PASS} ${DB_NAME} < "$sql_file"
    else
        # 使用本地 MySQL 客戶端執行
        mysql -h ${DB_HOST} -P ${DB_PORT} -u${DB_USER} -p${DB_PASS} ${DB_NAME} < "$sql_file"
    fi
}

# 主要邏輯
case "${1:-load}" in
    reset)
        echo "正在重置種子資料..."
        execute_sql "${SCRIPT_DIR}/reset-seed-data.sql"
        echo ""
        echo "種子資料重置完成！"
        ;;
    load)
        echo "正在載入種子資料..."
        execute_sql "${PROJECT_DIR}/docker/mysql/init/01-seed-data.sql"
        echo ""
        echo "種子資料載入完成！"
        ;;
    *)
        echo "用法: $0 [load|reset]"
        echo "  load  - 載入種子資料（預設）"
        echo "  reset - 重置種子資料（清除後重新載入）"
        exit 1
        ;;
esac

echo ""
echo "=========================================="
echo " 測試帳號資訊"
echo "=========================================="
echo " 帳號: admin       密碼: password123  (系統管理員)"
echo " 帳號: manager01   密碼: password123  (門市店長)"
echo " 帳號: cashier01   密碼: password123  (收銀員)"
echo " 帳號: cashier02   密碼: password123  (收銀員)"
echo " 帳號: warehouse01 密碼: password123  (倉管人員)"
echo "=========================================="

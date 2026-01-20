# =============================================================================
# 零售業簡易ERP系統 - Dockerfile
# =============================================================================
#
# 多階段建置配置：
#   Stage 1: Maven 建置階段 - 編譯並打包應用程式
#   Stage 2: 執行階段 - 使用精簡的 JRE 執行應用程式
#
# =============================================================================

# ===========================================================================
# Stage 1: Maven 建置階段
# ===========================================================================
FROM maven:3.9-eclipse-temurin-21-alpine AS builder

# 設定工作目錄
WORKDIR /app

# 複製 pom.xml 並下載依賴（利用 Docker 快取）
COPY pom.xml .
RUN mvn dependency:go-offline -B

# 複製原始碼並建置
COPY src ./src
RUN mvn clean package -DskipTests -B

# ===========================================================================
# Stage 2: 執行階段
# ===========================================================================
FROM eclipse-temurin:21-jre-alpine

# 建立應用程式使用者（安全性最佳實踐）
RUN addgroup -S appgroup && adduser -S appuser -G appgroup

# 設定工作目錄
WORKDIR /app

# 安裝必要工具（curl 用於健康檢查）
RUN apk add --no-cache curl

# 從建置階段複製 JAR 檔案
COPY --from=builder /app/target/*.jar app.jar

# 設定檔案擁有者
RUN chown -R appuser:appgroup /app

# 切換到非 root 使用者
USER appuser

# 設定 JVM 參數
ENV JAVA_OPTS="-Xms512m -Xmx1024m -XX:+UseG1GC -XX:MaxGCPauseMillis=200"

# 暴露應用程式埠
EXPOSE 8005

# 健康檢查端點
HEALTHCHECK --interval=30s --timeout=10s --start-period=60s --retries=3 \
    CMD curl -f http://localhost:8005/actuator/health || exit 1

# 啟動應用程式
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]

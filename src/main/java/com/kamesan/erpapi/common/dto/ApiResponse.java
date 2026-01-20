package com.kamesan.erpapi.common.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * API 統一回應格式
 *
 * <p>所有 API 回應都使用此格式，包含以下資訊：</p>
 * <ul>
 *   <li>success - 是否成功</li>
 *   <li>code - 狀態碼</li>
 *   <li>message - 訊息說明</li>
 *   <li>data - 回傳資料</li>
 *   <li>timestamp - 回應時間</li>
 * </ul>
 *
 * @param <T> 回傳資料型別
 * @author ERP System Team
 * @version 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "API 統一回應格式")
public class ApiResponse<T> {

    /**
     * 是否成功
     */
    @Schema(description = "是否成功", example = "true")
    private boolean success;

    /**
     * 狀態碼
     */
    @Schema(description = "狀態碼", example = "200")
    private int code;

    /**
     * 訊息說明
     */
    @Schema(description = "訊息說明", example = "操作成功")
    private String message;

    /**
     * 回傳資料
     */
    @Schema(description = "回傳資料")
    private T data;

    /**
     * 回應時間
     */
    @Schema(description = "回應時間")
    @Builder.Default
    private LocalDateTime timestamp = LocalDateTime.now();

    /**
     * 建立成功回應（無資料）
     *
     * @return 成功回應
     */
    public static ApiResponse<Void> success() {
        return ApiResponse.<Void>builder()
                .success(true)
                .code(200)
                .message("操作成功")
                .build();
    }

    /**
     * 建立成功回應（含資料）
     *
     * @param data 回傳資料
     * @param <T>  資料型別
     * @return 成功回應
     */
    public static <T> ApiResponse<T> success(T data) {
        return ApiResponse.<T>builder()
                .success(true)
                .code(200)
                .message("操作成功")
                .data(data)
                .build();
    }

    /**
     * 建立成功回應（含訊息和資料）
     *
     * @param message 訊息說明
     * @param data    回傳資料
     * @param <T>     資料型別
     * @return 成功回應
     */
    public static <T> ApiResponse<T> success(String message, T data) {
        return ApiResponse.<T>builder()
                .success(true)
                .code(200)
                .message(message)
                .data(data)
                .build();
    }

    /**
     * 建立失敗回應
     *
     * @param code    狀態碼
     * @param message 錯誤訊息
     * @return 失敗回應
     */
    public static ApiResponse<Void> error(int code, String message) {
        return ApiResponse.<Void>builder()
                .success(false)
                .code(code)
                .message(message)
                .build();
    }

    /**
     * 建立失敗回應（含資料）
     *
     * @param code    狀態碼
     * @param message 錯誤訊息
     * @param data    錯誤詳情
     * @param <T>     資料型別
     * @return 失敗回應
     */
    public static <T> ApiResponse<T> error(int code, String message, T data) {
        return ApiResponse.<T>builder()
                .success(false)
                .code(code)
                .message(message)
                .data(data)
                .build();
    }

    /**
     * 建立 400 Bad Request 回應
     *
     * @param message 錯誤訊息
     * @return 失敗回應
     */
    public static ApiResponse<Void> badRequest(String message) {
        return error(400, message);
    }

    /**
     * 建立 401 Unauthorized 回應
     *
     * @param message 錯誤訊息
     * @return 失敗回應
     */
    public static ApiResponse<Void> unauthorized(String message) {
        return error(401, message);
    }

    /**
     * 建立 403 Forbidden 回應
     *
     * @param message 錯誤訊息
     * @return 失敗回應
     */
    public static ApiResponse<Void> forbidden(String message) {
        return error(403, message);
    }

    /**
     * 建立 404 Not Found 回應
     *
     * @param message 錯誤訊息
     * @return 失敗回應
     */
    public static ApiResponse<Void> notFound(String message) {
        return error(404, message);
    }

    /**
     * 建立 500 Internal Server Error 回應
     *
     * @param message 錯誤訊息
     * @return 失敗回應
     */
    public static ApiResponse<Void> serverError(String message) {
        return error(500, message);
    }
}

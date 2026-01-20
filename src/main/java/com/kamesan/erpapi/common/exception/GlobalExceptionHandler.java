package com.kamesan.erpapi.common.exception;

import com.kamesan.erpapi.common.dto.ApiResponse;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.NoHandlerFoundException;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 全域異常處理器
 *
 * <p>統一處理所有 Controller 層拋出的異常，並轉換為標準的 API 回應格式。</p>
 *
 * <h2>處理的異常類型：</h2>
 * <ul>
 *   <li>BusinessException - 業務邏輯異常</li>
 *   <li>MethodArgumentNotValidException - 參數驗證失敗</li>
 *   <li>AuthenticationException - 認證失敗</li>
 *   <li>AccessDeniedException - 權限不足</li>
 *   <li>其他未預期的異常</li>
 * </ul>
 *
 * @author ERP System Team
 * @version 1.0.0
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * 處理業務邏輯異常
     *
     * @param ex 業務邏輯異常
     * @return API 回應
     */
    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ApiResponse<Void>> handleBusinessException(BusinessException ex) {
        log.warn("業務邏輯異常: {}", ex.getMessage());
        return ResponseEntity
                .status(ex.getCode())
                .body(ApiResponse.error(ex.getCode(), ex.getMessage()));
    }

    /**
     * 處理參數驗證失敗異常（@Valid 驗證）
     *
     * @param ex 參數驗證失敗異常
     * @return API 回應（包含詳細的驗證錯誤）
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Map<String, String>>> handleValidationException(
            MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach(error -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });

        String message = errors.values().stream()
                .collect(Collectors.joining(", "));

        log.warn("參數驗證失敗: {}", message);
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error(400, "參數驗證失敗: " + message, errors));
    }

    /**
     * 處理綁定異常
     *
     * @param ex 綁定異常
     * @return API 回應
     */
    @ExceptionHandler(BindException.class)
    public ResponseEntity<ApiResponse<Map<String, String>>> handleBindException(BindException ex) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach(error -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });

        log.warn("綁定異常: {}", errors);
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error(400, "參數綁定失敗", errors));
    }

    /**
     * 處理約束違反異常
     *
     * @param ex 約束違反異常
     * @return API 回應
     */
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ApiResponse<Void>> handleConstraintViolationException(
            ConstraintViolationException ex) {
        String message = ex.getConstraintViolations().stream()
                .map(violation -> violation.getPropertyPath() + ": " + violation.getMessage())
                .collect(Collectors.joining(", "));

        log.warn("約束違反: {}", message);
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error(400, message));
    }

    /**
     * 處理認證異常
     *
     * @param ex 認證異常
     * @return API 回應
     */
    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ApiResponse<Void>> handleAuthenticationException(AuthenticationException ex) {
        log.warn("認證失敗: {}", ex.getMessage());
        return ResponseEntity
                .status(HttpStatus.UNAUTHORIZED)
                .body(ApiResponse.error(401, "認證失敗: " + ex.getMessage()));
    }

    /**
     * 處理錯誤的認證資訊異常
     *
     * @param ex 錯誤的認證資訊異常
     * @return API 回應
     */
    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ApiResponse<Void>> handleBadCredentialsException(BadCredentialsException ex) {
        log.warn("帳號或密碼錯誤");
        return ResponseEntity
                .status(HttpStatus.UNAUTHORIZED)
                .body(ApiResponse.error(401, "帳號或密碼錯誤"));
    }

    /**
     * 處理權限不足異常
     *
     * @param ex 權限不足異常
     * @return API 回應
     */
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiResponse<Void>> handleAccessDeniedException(AccessDeniedException ex) {
        log.warn("權限不足: {}", ex.getMessage());
        return ResponseEntity
                .status(HttpStatus.FORBIDDEN)
                .body(ApiResponse.error(403, "權限不足，無法執行此操作"));
    }

    /**
     * 處理缺少請求參數異常
     *
     * @param ex 缺少請求參數異常
     * @return API 回應
     */
    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ApiResponse<Void>> handleMissingServletRequestParameterException(
            MissingServletRequestParameterException ex) {
        String message = String.format("缺少必要參數: %s", ex.getParameterName());
        log.warn(message);
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error(400, message));
    }

    /**
     * 處理參數類型不匹配異常
     *
     * @param ex 參數類型不匹配異常
     * @return API 回應
     */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ApiResponse<Void>> handleMethodArgumentTypeMismatchException(
            MethodArgumentTypeMismatchException ex) {
        String message = String.format("參數類型錯誤: %s 應為 %s",
                ex.getName(),
                ex.getRequiredType() != null ? ex.getRequiredType().getSimpleName() : "unknown");
        log.warn(message);
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error(400, message));
    }

    /**
     * 處理請求體無法讀取異常
     *
     * @param ex 請求體無法讀取異常
     * @return API 回應
     */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiResponse<Void>> handleHttpMessageNotReadableException(
            HttpMessageNotReadableException ex) {
        log.warn("請求體格式錯誤: {}", ex.getMessage());
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error(400, "請求體格式錯誤，請檢查 JSON 格式"));
    }

    /**
     * 處理不支持的請求方法異常
     *
     * @param ex 不支持的請求方法異常
     * @return API 回應
     */
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ApiResponse<Void>> handleHttpRequestMethodNotSupportedException(
            HttpRequestMethodNotSupportedException ex) {
        String message = String.format("不支持的請求方法: %s", ex.getMethod());
        log.warn(message);
        return ResponseEntity
                .status(HttpStatus.METHOD_NOT_ALLOWED)
                .body(ApiResponse.error(405, message));
    }

    /**
     * 處理不支持的媒體類型異常
     *
     * @param ex 不支持的媒體類型異常
     * @return API 回應
     */
    @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
    public ResponseEntity<ApiResponse<Void>> handleHttpMediaTypeNotSupportedException(
            HttpMediaTypeNotSupportedException ex) {
        String message = String.format("不支持的媒體類型: %s", ex.getContentType());
        log.warn(message);
        return ResponseEntity
                .status(HttpStatus.UNSUPPORTED_MEDIA_TYPE)
                .body(ApiResponse.error(415, message));
    }

    /**
     * 處理找不到處理器異常
     *
     * @param ex 找不到處理器異常
     * @return API 回應
     */
    @ExceptionHandler(NoHandlerFoundException.class)
    public ResponseEntity<ApiResponse<Void>> handleNoHandlerFoundException(NoHandlerFoundException ex) {
        String message = String.format("找不到路徑: %s %s", ex.getHttpMethod(), ex.getRequestURL());
        log.warn(message);
        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(ApiResponse.error(404, message));
    }

    /**
     * 處理其他未預期的異常
     *
     * @param ex 未預期的異常
     * @return API 回應
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleException(Exception ex) {
        log.error("系統異常: ", ex);
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error(500, "系統發生錯誤，請稍後再試"));
    }
}

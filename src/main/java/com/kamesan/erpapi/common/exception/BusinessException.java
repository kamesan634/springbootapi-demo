package com.kamesan.erpapi.common.exception;

import lombok.Getter;

/**
 * 業務邏輯異常
 *
 * <p>用於處理業務邏輯錯誤，如：</p>
 * <ul>
 *   <li>資料驗證失敗</li>
 *   <li>業務規則不符合</li>
 *   <li>資源不存在</li>
 *   <li>操作權限不足</li>
 * </ul>
 *
 * @author ERP System Team
 * @version 1.0.0
 */
@Getter
public class BusinessException extends RuntimeException {

    /**
     * 錯誤碼
     */
    private final int code;

    /**
     * 建構子 - 使用預設錯誤碼 400
     *
     * @param message 錯誤訊息
     */
    public BusinessException(String message) {
        super(message);
        this.code = 400;
    }

    /**
     * 建構子 - 指定錯誤碼
     *
     * @param code    錯誤碼
     * @param message 錯誤訊息
     */
    public BusinessException(int code, String message) {
        super(message);
        this.code = code;
    }

    /**
     * 建構子 - 指定錯誤碼和原因
     *
     * @param code    錯誤碼
     * @param message 錯誤訊息
     * @param cause   原因
     */
    public BusinessException(int code, String message, Throwable cause) {
        super(message, cause);
        this.code = code;
    }

    /**
     * 建立資源不存在異常
     *
     * @param resourceName 資源名稱
     * @param id           資源 ID
     * @return BusinessException
     */
    public static BusinessException notFound(String resourceName, Object id) {
        return new BusinessException(404, String.format("%s (ID: %s) 不存在", resourceName, id));
    }

    /**
     * 建立資源已存在異常
     *
     * @param resourceName 資源名稱
     * @param fieldName    欄位名稱
     * @param value        欄位值
     * @return BusinessException
     */
    public static BusinessException alreadyExists(String resourceName, String fieldName, Object value) {
        return new BusinessException(409, String.format("%s 的 %s '%s' 已存在", resourceName, fieldName, value));
    }

    /**
     * 建立權限不足異常
     *
     * @param message 錯誤訊息
     * @return BusinessException
     */
    public static BusinessException forbidden(String message) {
        return new BusinessException(403, message);
    }

    /**
     * 建立驗證失敗異常
     *
     * @param message 錯誤訊息
     * @return BusinessException
     */
    public static BusinessException validationFailed(String message) {
        return new BusinessException(400, message);
    }
}

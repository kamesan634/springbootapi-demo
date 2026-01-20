package com.kamesan.erpapi.system.dto;

import com.kamesan.erpapi.system.entity.SystemParameter;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

/**
 * 新增系統參數請求
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateSystemParameterRequest {

    @NotBlank(message = "參數類別不可為空")
    @Size(max = 50, message = "參數類別長度不可超過 50 字元")
    private String category;

    @NotBlank(message = "參數鍵不可為空")
    @Size(max = 100, message = "參數鍵長度不可超過 100 字元")
    private String paramKey;

    @Size(max = 1000, message = "參數值長度不可超過 1000 字元")
    private String paramValue;

    private SystemParameter.ParamType paramType;

    @Size(max = 500, message = "描述長度不可超過 500 字元")
    private String description;

    private Boolean isEncrypted;

    private Integer sortOrder;
}

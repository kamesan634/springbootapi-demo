package com.kamesan.erpapi.system.dto;

import com.kamesan.erpapi.system.entity.SystemParameter;
import lombok.*;
import java.time.LocalDateTime;

/**
 * 系統參數 DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SystemParameterDto {
    private Long id;
    private String category;
    private String paramKey;
    private String paramValue;
    private SystemParameter.ParamType paramType;
    private String description;
    private Boolean isSystem;
    private Boolean isEncrypted;
    private Integer sortOrder;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

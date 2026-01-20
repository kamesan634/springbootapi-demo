package com.kamesan.erpapi.system.dto;

import com.kamesan.erpapi.system.entity.NumberRule;
import lombok.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 編號規則 DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NumberRuleDto {
    private Long id;
    private String ruleCode;
    private String ruleName;
    private String prefix;
    private String suffix;
    private String dateFormat;
    private Integer sequenceLength;
    private Long currentSequence;
    private NumberRule.ResetPeriod resetPeriod;
    private LocalDate lastResetDate;
    private String sampleNumber;
    private Boolean isActive;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

package com.kamesan.erpapi.system.entity;

import com.kamesan.erpapi.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

/**
 * 編號規則實體
 *
 * <p>管理系統各類單據的編號規則：</p>
 * <ul>
 *   <li>訂單編號</li>
 *   <li>採購單編號</li>
 *   <li>退貨單編號</li>
 *   <li>盤點單編號等</li>
 * </ul>
 *
 * <p>編號格式：前綴 + 日期格式 + 流水號 + 後綴</p>
 *
 * @author ERP System Team
 * @version 1.0.0
 */
@Entity
@Table(name = "number_rules", indexes = {
        @Index(name = "idx_number_rules_code", columnList = "rule_code"),
        @Index(name = "idx_number_rules_active", columnList = "is_active")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NumberRule extends BaseEntity {

    /**
     * 規則代碼 (如 ORDER, PURCHASE, REFUND)
     */
    @Column(name = "rule_code", nullable = false, unique = true, length = 50)
    private String ruleCode;

    /**
     * 規則名稱
     */
    @Column(name = "rule_name", nullable = false, length = 100)
    private String ruleName;

    /**
     * 前綴 (如 ORD, PO, REF)
     */
    @Column(name = "prefix", length = 20)
    private String prefix;

    /**
     * 後綴
     */
    @Column(name = "suffix", length = 20)
    private String suffix;

    /**
     * 日期格式 (如 yyyyMMdd, yyMM)
     */
    @Column(name = "date_format", length = 20)
    private String dateFormat;

    /**
     * 流水號長度
     */
    @Column(name = "sequence_length", nullable = false)
    @Builder.Default
    private Integer sequenceLength = 4;

    /**
     * 目前流水號
     */
    @Column(name = "current_sequence", nullable = false)
    @Builder.Default
    private Long currentSequence = 0L;

    /**
     * 重置週期
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "reset_period", length = 20)
    @Builder.Default
    private ResetPeriod resetPeriod = ResetPeriod.DAILY;

    /**
     * 最後重置日期
     */
    @Column(name = "last_reset_date")
    private LocalDate lastResetDate;

    /**
     * 範例編號
     */
    @Column(name = "sample_number", length = 50)
    private String sampleNumber;

    /**
     * 是否啟用
     */
    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private Boolean isActive = true;

    /**
     * 重置週期列舉
     */
    public enum ResetPeriod {
        DAILY, MONTHLY, YEARLY, NEVER
    }
}

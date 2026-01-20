package com.kamesan.erpapi.system.service;

import com.kamesan.erpapi.common.exception.BusinessException;
import com.kamesan.erpapi.system.dto.NumberRuleDto;
import com.kamesan.erpapi.system.entity.NumberRule;
import com.kamesan.erpapi.system.repository.NumberRuleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 編號規則服務
 *
 * <p>提供編號規則管理和編號產生功能</p>
 *
 * @author ERP System Team
 * @version 1.0.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class NumberRuleService {

    private final NumberRuleRepository numberRuleRepository;

    /**
     * 產生下一個編號
     *
     * <p>使用悲觀鎖確保並發安全</p>
     *
     * @param ruleCode 規則代碼
     * @return 產生的編號
     */
    @Transactional
    public String generateNextNumber(String ruleCode) {
        log.debug("產生編號，規則代碼: {}", ruleCode);

        NumberRule rule = numberRuleRepository.findByRuleCodeForUpdate(ruleCode)
                .orElseThrow(() -> new BusinessException(404, "編號規則不存在：" + ruleCode));

        // 檢查是否需要重置
        checkAndResetSequence(rule);

        // 遞增流水號
        rule.setCurrentSequence(rule.getCurrentSequence() + 1);
        numberRuleRepository.save(rule);

        // 產生編號
        String number = buildNumber(rule);
        log.debug("產生編號成功: {}", number);

        return number;
    }

    /**
     * 預覽編號格式
     */
    @Transactional(readOnly = true)
    public String previewNumber(String ruleCode) {
        NumberRule rule = numberRuleRepository.findByRuleCode(ruleCode)
                .orElseThrow(() -> new BusinessException(404, "編號規則不存在：" + ruleCode));

        return buildNumber(rule);
    }

    /**
     * 取得所有編號規則
     */
    @Transactional(readOnly = true)
    public List<NumberRuleDto> getAllRules() {
        return numberRuleRepository.findByIsActiveTrueOrderByRuleCodeAsc()
                .stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    /**
     * 根據代碼取得編號規則
     */
    @Transactional(readOnly = true)
    public NumberRuleDto getRuleByCode(String ruleCode) {
        NumberRule rule = numberRuleRepository.findByRuleCode(ruleCode)
                .orElseThrow(() -> new BusinessException(404, "編號規則不存在：" + ruleCode));
        return convertToDto(rule);
    }

    /**
     * 重置流水號
     */
    @Transactional
    public void resetSequence(String ruleCode) {
        log.info("重置流水號，規則代碼: {}", ruleCode);

        NumberRule rule = numberRuleRepository.findByRuleCodeForUpdate(ruleCode)
                .orElseThrow(() -> new BusinessException(404, "編號規則不存在：" + ruleCode));

        rule.setCurrentSequence(0L);
        rule.setLastResetDate(LocalDate.now());
        numberRuleRepository.save(rule);

        log.info("流水號重置成功");
    }

    /**
     * 檢查並重置流水號
     */
    private void checkAndResetSequence(NumberRule rule) {
        LocalDate today = LocalDate.now();
        LocalDate lastReset = rule.getLastResetDate();

        boolean needReset = false;

        if (lastReset == null) {
            needReset = true;
        } else {
            switch (rule.getResetPeriod()) {
                case DAILY:
                    needReset = !today.equals(lastReset);
                    break;
                case MONTHLY:
                    needReset = today.getYear() != lastReset.getYear() ||
                            today.getMonthValue() != lastReset.getMonthValue();
                    break;
                case YEARLY:
                    needReset = today.getYear() != lastReset.getYear();
                    break;
                case NEVER:
                    needReset = false;
                    break;
            }
        }

        if (needReset) {
            rule.setCurrentSequence(0L);
            rule.setLastResetDate(today);
            log.debug("流水號已重置，規則代碼: {}", rule.getRuleCode());
        }
    }

    /**
     * 建構編號
     */
    private String buildNumber(NumberRule rule) {
        StringBuilder sb = new StringBuilder();

        // 前綴
        if (rule.getPrefix() != null && !rule.getPrefix().isEmpty()) {
            sb.append(rule.getPrefix());
        }

        // 日期
        if (rule.getDateFormat() != null && !rule.getDateFormat().isEmpty()) {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern(rule.getDateFormat());
            sb.append(LocalDate.now().format(formatter));
        }

        // 流水號
        String sequenceFormat = "%0" + rule.getSequenceLength() + "d";
        sb.append(String.format(sequenceFormat, rule.getCurrentSequence()));

        // 後綴
        if (rule.getSuffix() != null && !rule.getSuffix().isEmpty()) {
            sb.append(rule.getSuffix());
        }

        return sb.toString();
    }

    private NumberRuleDto convertToDto(NumberRule rule) {
        return NumberRuleDto.builder()
                .id(rule.getId())
                .ruleCode(rule.getRuleCode())
                .ruleName(rule.getRuleName())
                .prefix(rule.getPrefix())
                .suffix(rule.getSuffix())
                .dateFormat(rule.getDateFormat())
                .sequenceLength(rule.getSequenceLength())
                .currentSequence(rule.getCurrentSequence())
                .resetPeriod(rule.getResetPeriod())
                .lastResetDate(rule.getLastResetDate())
                .sampleNumber(buildNumber(rule))
                .isActive(rule.getIsActive())
                .createdAt(rule.getCreatedAt())
                .updatedAt(rule.getUpdatedAt())
                .build();
    }
}

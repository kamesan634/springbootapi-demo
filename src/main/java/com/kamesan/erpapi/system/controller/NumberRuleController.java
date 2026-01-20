package com.kamesan.erpapi.system.controller;

import com.kamesan.erpapi.common.dto.ApiResponse;
import com.kamesan.erpapi.system.dto.NumberRuleDto;
import com.kamesan.erpapi.system.service.NumberRuleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 編號規則 Controller
 *
 * @author ERP System Team
 * @version 1.0.0
 */
@RestController
@RequestMapping("/api/v1/number-rules")
@RequiredArgsConstructor
@Tag(name = "編號規則", description = "編號規則管理 API")
public class NumberRuleController {

    private final NumberRuleService numberRuleService;

    @GetMapping
    @Operation(summary = "取得所有規則", description = "取得所有啟用的編號規則")
    public ResponseEntity<ApiResponse<List<NumberRuleDto>>> getAllRules() {
        List<NumberRuleDto> rules = numberRuleService.getAllRules();
        return ResponseEntity.ok(ApiResponse.success(rules));
    }

    @GetMapping("/{ruleCode}")
    @Operation(summary = "取得規則", description = "根據規則代碼取得編號規則")
    public ResponseEntity<ApiResponse<NumberRuleDto>> getRuleByCode(
            @Parameter(description = "規則代碼") @PathVariable String ruleCode) {

        NumberRuleDto rule = numberRuleService.getRuleByCode(ruleCode);
        return ResponseEntity.ok(ApiResponse.success(rule));
    }

    @PostMapping("/{ruleCode}/generate")
    @Operation(summary = "產生編號", description = "根據規則產生下一個編號")
    public ResponseEntity<ApiResponse<Map<String, String>>> generateNumber(
            @Parameter(description = "規則代碼") @PathVariable String ruleCode) {

        String number = numberRuleService.generateNextNumber(ruleCode);
        return ResponseEntity.ok(ApiResponse.success(Map.of("number", number)));
    }

    @GetMapping("/{ruleCode}/preview")
    @Operation(summary = "預覽編號", description = "預覽編號格式（不會增加流水號）")
    public ResponseEntity<ApiResponse<Map<String, String>>> previewNumber(
            @Parameter(description = "規則代碼") @PathVariable String ruleCode) {

        String number = numberRuleService.previewNumber(ruleCode);
        return ResponseEntity.ok(ApiResponse.success(Map.of("preview", number)));
    }

    @PostMapping("/{ruleCode}/reset")
    @Operation(summary = "重置流水號", description = "重置指定規則的流水號")
    public ResponseEntity<ApiResponse<Void>> resetSequence(
            @Parameter(description = "規則代碼") @PathVariable String ruleCode) {

        numberRuleService.resetSequence(ruleCode);
        return ResponseEntity.ok(ApiResponse.success("流水號重置成功", null));
    }
}

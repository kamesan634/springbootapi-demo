package com.kamesan.erpapi.system.controller;

import com.kamesan.erpapi.common.dto.ApiResponse;
import com.kamesan.erpapi.system.dto.CreateSystemParameterRequest;
import com.kamesan.erpapi.system.dto.SystemParameterDto;
import com.kamesan.erpapi.system.service.SystemParameterService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 系統參數 Controller
 *
 * @author ERP System Team
 * @version 1.0.0
 */
@RestController
@RequestMapping("/api/v1/system-parameters")
@RequiredArgsConstructor
@Tag(name = "系統參數", description = "系統參數管理 API")
public class SystemParameterController {

    private final SystemParameterService systemParameterService;

    @GetMapping
    @Operation(summary = "取得所有參數", description = "取得所有系統參數")
    public ResponseEntity<ApiResponse<List<SystemParameterDto>>> getAllParameters() {
        List<SystemParameterDto> parameters = systemParameterService.getAllParameters();
        return ResponseEntity.ok(ApiResponse.success(parameters));
    }

    @GetMapping("/category/{category}")
    @Operation(summary = "根據類別取得參數", description = "取得指定類別的所有參數")
    public ResponseEntity<ApiResponse<List<SystemParameterDto>>> getParametersByCategory(
            @Parameter(description = "參數類別") @PathVariable String category) {

        List<SystemParameterDto> parameters = systemParameterService.getParametersByCategory(category);
        return ResponseEntity.ok(ApiResponse.success(parameters));
    }

    @GetMapping("/{category}/{paramKey}")
    @Operation(summary = "取得參數值", description = "根據類別和鍵取得參數值")
    public ResponseEntity<ApiResponse<String>> getParameterValue(
            @Parameter(description = "參數類別") @PathVariable String category,
            @Parameter(description = "參數鍵") @PathVariable String paramKey) {

        String value = systemParameterService.getParameterValue(category, paramKey);
        return ResponseEntity.ok(ApiResponse.success(value));
    }

    @PostMapping
    @Operation(summary = "新增參數", description = "新增系統參數")
    public ResponseEntity<ApiResponse<SystemParameterDto>> createParameter(
            @Valid @RequestBody CreateSystemParameterRequest request) {

        SystemParameterDto parameter = systemParameterService.createParameter(request);
        return ResponseEntity.ok(ApiResponse.success("系統參數新增成功", parameter));
    }

    @PutMapping("/{category}/{paramKey}")
    @Operation(summary = "更新參數值", description = "更新系統參數的值")
    public ResponseEntity<ApiResponse<SystemParameterDto>> updateParameterValue(
            @Parameter(description = "參數類別") @PathVariable String category,
            @Parameter(description = "參數鍵") @PathVariable String paramKey,
            @RequestBody Map<String, String> body) {

        String newValue = body.get("value");
        SystemParameterDto parameter = systemParameterService.updateParameterValue(category, paramKey, newValue);
        return ResponseEntity.ok(ApiResponse.success("系統參數更新成功", parameter));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "刪除參數", description = "刪除系統參數（系統參數不可刪除）")
    public ResponseEntity<ApiResponse<Void>> deleteParameter(
            @Parameter(description = "參數 ID") @PathVariable Long id) {

        systemParameterService.deleteParameter(id);
        return ResponseEntity.ok(ApiResponse.success("系統參數刪除成功", null));
    }
}

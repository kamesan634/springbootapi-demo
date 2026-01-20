package com.kamesan.erpapi.customers.controller;

import com.kamesan.erpapi.common.dto.ApiResponse;
import com.kamesan.erpapi.customers.dto.CustomerLevelDto;
import com.kamesan.erpapi.customers.service.CustomerService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 會員等級控制器
 *
 * <p>處理會員等級相關的 API 請求，包括：</p>
 * <ul>
 *   <li>GET /api/v1/customer-levels - 取得所有會員等級</li>
 *   <li>GET /api/v1/customer-levels/active - 取得啟用的會員等級</li>
 *   <li>GET /api/v1/customer-levels/{id} - 根據 ID 取得會員等級</li>
 * </ul>
 *
 * <h2>會員等級說明：</h2>
 * <p>會員等級定義了會員可享有的權益，包括折扣比率和點數倍率。</p>
 * <p>系統會根據會員的累積消費金額自動升級等級。</p>
 *
 * @author ERP System Team
 * @version 1.0.0
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/customer-levels")
@RequiredArgsConstructor
@Tag(name = "會員等級管理", description = "會員等級查詢相關 API")
public class CustomerLevelController {

    /**
     * 會員服務
     */
    private final CustomerService customerService;

    /**
     * 取得所有會員等級
     *
     * <p>回傳所有會員等級（包含停用的），依排序順序排列。</p>
     * <p>每個等級包含該等級的會員數量統計。</p>
     *
     * @return 會員等級列表
     */
    @GetMapping
    @Operation(
            summary = "取得所有會員等級",
            description = "回傳所有會員等級（包含停用的），依排序順序排列，每個等級包含會員數量統計"
    )
    public ApiResponse<List<CustomerLevelDto>> getAllLevels() {
        log.info("API 請求: 取得所有會員等級");

        List<CustomerLevelDto> levels = customerService.getAllLevels();

        log.info("取得會員等級成功，共 {} 筆", levels.size());

        return ApiResponse.success("查詢成功", levels);
    }

    /**
     * 取得啟用的會員等級
     *
     * <p>只回傳啟用狀態的會員等級，適用於前台選單等場景。</p>
     *
     * @return 啟用的會員等級列表
     */
    @GetMapping("/active")
    @Operation(
            summary = "取得啟用的會員等級",
            description = "只回傳啟用狀態的會員等級，適用於前台選單等場景"
    )
    public ApiResponse<List<CustomerLevelDto>> getActiveLevels() {
        log.info("API 請求: 取得啟用的會員等級");

        List<CustomerLevelDto> levels = customerService.getActiveLevels();

        log.info("取得啟用會員等級成功，共 {} 筆", levels.size());

        return ApiResponse.success("查詢成功", levels);
    }

    /**
     * 根據 ID 取得會員等級
     *
     * <p>取得指定 ID 的會員等級詳細資訊，包含該等級的會員數量統計。</p>
     *
     * @param id 會員等級 ID
     * @return 會員等級詳細資訊
     */
    @GetMapping("/{id}")
    @Operation(
            summary = "根據 ID 取得會員等級",
            description = "取得指定 ID 的會員等級詳細資訊，包含該等級的會員數量統計"
    )
    public ApiResponse<CustomerLevelDto> getLevelById(
            @Parameter(description = "會員等級 ID", example = "1", required = true)
            @PathVariable Long id) {
        log.info("API 請求: 取得會員等級 ID: {}", id);

        CustomerLevelDto level = customerService.getLevelById(id);

        log.info("取得會員等級成功: {} ({})", level.getName(), level.getCode());

        return ApiResponse.success("查詢成功", level);
    }
}

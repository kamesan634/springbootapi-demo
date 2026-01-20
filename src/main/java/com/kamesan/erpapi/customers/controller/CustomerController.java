package com.kamesan.erpapi.customers.controller;

import com.kamesan.erpapi.common.dto.ApiResponse;
import com.kamesan.erpapi.common.dto.PageResponse;
import com.kamesan.erpapi.customers.dto.CreateCustomerRequest;
import com.kamesan.erpapi.customers.dto.CustomerDto;
import com.kamesan.erpapi.customers.dto.UpdateCustomerRequest;
import com.kamesan.erpapi.customers.service.CustomerService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

/**
 * 會員控制器
 *
 * <p>處理會員相關的 API 請求，包括：</p>
 * <ul>
 *   <li>POST /api/v1/customers - 建立會員</li>
 *   <li>GET /api/v1/customers - 分頁查詢會員</li>
 *   <li>GET /api/v1/customers/{id} - 根據 ID 取得會員</li>
 *   <li>GET /api/v1/customers/member-no/{memberNo} - 根據會員編號取得會員</li>
 *   <li>PUT /api/v1/customers/{id} - 更新會員</li>
 *   <li>DELETE /api/v1/customers/{id} - 刪除會員（軟刪除）</li>
 *   <li>POST /api/v1/customers/{id}/points/add - 增加點數</li>
 *   <li>POST /api/v1/customers/{id}/points/deduct - 扣除點數</li>
 *   <li>POST /api/v1/customers/{id}/spending - 記錄消費</li>
 *   <li>GET /api/v1/customers/birthday/today - 今日壽星</li>
 *   <li>GET /api/v1/customers/birthday/month - 本月壽星</li>
 * </ul>
 *
 * @author ERP System Team
 * @version 1.0.0
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/customers")
@RequiredArgsConstructor
@Tag(name = "會員管理", description = "會員 CRUD、點數操作、消費記錄等相關 API")
public class CustomerController {

    /**
     * 會員服務
     */
    private final CustomerService customerService;

    // ==================== CRUD 操作 ====================

    /**
     * 建立會員
     *
     * <p>建立新會員，系統會自動產生會員編號。</p>
     * <p>若未指定等級，則使用預設等級（普通會員）。</p>
     *
     * @param request 建立會員請求
     * @return 新建的會員資訊
     */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(
            summary = "建立會員",
            description = "建立新會員，系統會自動產生會員編號。若未指定等級，則使用預設等級（普通會員）"
    )
    public ApiResponse<CustomerDto> createCustomer(
            @Valid @RequestBody CreateCustomerRequest request) {
        log.info("API 請求: 建立會員 - {}", request.getName());

        CustomerDto customer = customerService.createCustomer(request);

        log.info("會員建立成功: {} ({})", customer.getName(), customer.getMemberNo());

        return ApiResponse.success("會員建立成功", customer);
    }

    /**
     * 分頁查詢會員
     *
     * <p>支援分頁和排序功能。</p>
     *
     * @param page   頁碼（從 1 開始）
     * @param size   每頁筆數
     * @param sortBy 排序欄位
     * @param sortDir 排序方向（asc/desc）
     * @return 會員分頁結果
     */
    @GetMapping
    @Operation(
            summary = "分頁查詢會員",
            description = "支援分頁和排序功能，回傳會員列表"
    )
    public ApiResponse<PageResponse<CustomerDto>> getCustomers(
            @Parameter(description = "頁碼（從 1 開始）", example = "1")
            @RequestParam(defaultValue = "1") int page,

            @Parameter(description = "每頁筆數", example = "20")
            @RequestParam(defaultValue = "20") int size,

            @Parameter(description = "排序欄位", example = "createdAt")
            @RequestParam(defaultValue = "createdAt") String sortBy,

            @Parameter(description = "排序方向（asc/desc）", example = "desc")
            @RequestParam(defaultValue = "desc") String sortDir) {

        log.info("API 請求: 分頁查詢會員 - page: {}, size: {}", page, size);

        Sort sort = sortDir.equalsIgnoreCase("asc")
                ? Sort.by(sortBy).ascending()
                : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(Math.max(0, page - 1), size, sort);

        PageResponse<CustomerDto> result = customerService.getCustomers(pageable);

        log.info("查詢會員成功，共 {} 筆", result.getTotalElements());

        return ApiResponse.success("查詢成功", result);
    }

    /**
     * 搜尋會員
     *
     * <p>根據關鍵字搜尋會員（姓名、手機、Email、會員編號）。</p>
     *
     * @param keyword 搜尋關鍵字
     * @param page    頁碼
     * @param size    每頁筆數
     * @return 會員分頁結果
     */
    @GetMapping("/search")
    @Operation(
            summary = "搜尋會員",
            description = "根據關鍵字搜尋會員（姓名、手機、Email、會員編號）"
    )
    public ApiResponse<PageResponse<CustomerDto>> searchCustomers(
            @Parameter(description = "搜尋關鍵字", example = "王", required = true)
            @RequestParam String keyword,

            @Parameter(description = "頁碼（從 1 開始）", example = "1")
            @RequestParam(defaultValue = "1") int page,

            @Parameter(description = "每頁筆數", example = "20")
            @RequestParam(defaultValue = "20") int size) {

        log.info("API 請求: 搜尋會員 - keyword: {}", keyword);

        Pageable pageable = PageRequest.of(Math.max(0, page - 1), size);
        PageResponse<CustomerDto> result = customerService.searchCustomers(keyword, pageable);

        log.info("搜尋會員成功，共 {} 筆", result.getTotalElements());

        return ApiResponse.success("查詢成功", result);
    }

    /**
     * 複合條件查詢會員
     *
     * <p>支援多條件組合查詢。</p>
     *
     * @param keyword 搜尋關鍵字（選填）
     * @param levelId 等級 ID（選填）
     * @param active  啟用狀態（選填）
     * @param gender  性別（選填）
     * @param page    頁碼
     * @param size    每頁筆數
     * @return 會員分頁結果
     */
    @GetMapping("/filter")
    @Operation(
            summary = "複合條件查詢會員",
            description = "支援多條件組合查詢（關鍵字、等級、啟用狀態、性別）"
    )
    public ApiResponse<PageResponse<CustomerDto>> filterCustomers(
            @Parameter(description = "搜尋關鍵字")
            @RequestParam(required = false) String keyword,

            @Parameter(description = "等級 ID")
            @RequestParam(required = false) Long levelId,

            @Parameter(description = "啟用狀態")
            @RequestParam(required = false) Boolean active,

            @Parameter(description = "性別（M/F/O）")
            @RequestParam(required = false) String gender,

            @Parameter(description = "頁碼（從 1 開始）", example = "1")
            @RequestParam(defaultValue = "1") int page,

            @Parameter(description = "每頁筆數", example = "20")
            @RequestParam(defaultValue = "20") int size) {

        log.info("API 請求: 複合條件查詢會員");

        Pageable pageable = PageRequest.of(Math.max(0, page - 1), size);
        PageResponse<CustomerDto> result = customerService.findCustomers(
                keyword, levelId, active, gender, pageable);

        log.info("查詢會員成功，共 {} 筆", result.getTotalElements());

        return ApiResponse.success("查詢成功", result);
    }

    /**
     * 根據 ID 取得會員
     *
     * @param id 會員 ID
     * @return 會員詳細資訊
     */
    @GetMapping("/{id}")
    @Operation(
            summary = "根據 ID 取得會員",
            description = "取得指定 ID 的會員詳細資訊"
    )
    public ApiResponse<CustomerDto> getCustomerById(
            @Parameter(description = "會員 ID", example = "1", required = true)
            @PathVariable Long id) {
        log.info("API 請求: 取得會員 ID: {}", id);

        CustomerDto customer = customerService.getCustomerById(id);

        log.info("取得會員成功: {} ({})", customer.getName(), customer.getMemberNo());

        return ApiResponse.success("查詢成功", customer);
    }

    /**
     * 根據會員編號取得會員
     *
     * @param memberNo 會員編號
     * @return 會員詳細資訊
     */
    @GetMapping("/member-no/{memberNo}")
    @Operation(
            summary = "根據會員編號取得會員",
            description = "取得指定會員編號的會員詳細資訊"
    )
    public ApiResponse<CustomerDto> getCustomerByMemberNo(
            @Parameter(description = "會員編號", example = "M202501060001", required = true)
            @PathVariable String memberNo) {
        log.info("API 請求: 取得會員編號: {}", memberNo);

        CustomerDto customer = customerService.getCustomerByMemberNo(memberNo);

        log.info("取得會員成功: {} ({})", customer.getName(), customer.getMemberNo());

        return ApiResponse.success("查詢成功", customer);
    }

    /**
     * 更新會員
     *
     * <p>更新會員資訊，只更新有提供的欄位。</p>
     *
     * @param id      會員 ID
     * @param request 更新會員請求
     * @return 更新後的會員資訊
     */
    @PutMapping("/{id}")
    @Operation(
            summary = "更新會員",
            description = "更新會員資訊，只更新有提供的欄位"
    )
    public ApiResponse<CustomerDto> updateCustomer(
            @Parameter(description = "會員 ID", example = "1", required = true)
            @PathVariable Long id,

            @Valid @RequestBody UpdateCustomerRequest request) {
        log.info("API 請求: 更新會員 ID: {}", id);

        CustomerDto customer = customerService.updateCustomer(id, request);

        log.info("會員更新成功: {} ({})", customer.getName(), customer.getMemberNo());

        return ApiResponse.success("會員更新成功", customer);
    }

    /**
     * 刪除會員（軟刪除）
     *
     * <p>將會員設為停用狀態，而非實際刪除資料。</p>
     *
     * @param id 會員 ID
     * @return 操作結果
     */
    @DeleteMapping("/{id}")
    @Operation(
            summary = "刪除會員",
            description = "將會員設為停用狀態（軟刪除），而非實際刪除資料"
    )
    public ApiResponse<Void> deleteCustomer(
            @Parameter(description = "會員 ID", example = "1", required = true)
            @PathVariable Long id) {
        log.info("API 請求: 刪除會員 ID: {}", id);

        customerService.deleteCustomer(id);

        log.info("會員刪除（停用）成功: {}", id);

        return ApiResponse.success("會員已停用", null);
    }

    // ==================== 點數操作 ====================

    /**
     * 增加會員點數
     *
     * @param id     會員 ID
     * @param points 增加的點數
     * @param reason 原因說明
     * @return 更新後的會員資訊
     */
    @PostMapping("/{id}/points/add")
    @Operation(
            summary = "增加會員點數",
            description = "為指定會員增加點數"
    )
    public ApiResponse<CustomerDto> addPoints(
            @Parameter(description = "會員 ID", example = "1", required = true)
            @PathVariable Long id,

            @Parameter(description = "增加的點數", example = "100", required = true)
            @RequestParam int points,

            @Parameter(description = "原因說明", example = "消費回饋")
            @RequestParam(required = false, defaultValue = "手動增加") String reason) {
        log.info("API 請求: 增加點數 - 會員 {}, 點數 {}, 原因: {}", id, points, reason);

        CustomerDto customer = customerService.addPoints(id, points, reason);

        log.info("點數增加成功: {} ({}) 目前點數: {}",
                customer.getName(), customer.getMemberNo(), customer.getTotalPoints());

        return ApiResponse.success("點數增加成功", customer);
    }

    /**
     * 扣除會員點數
     *
     * @param id     會員 ID
     * @param points 扣除的點數
     * @param reason 原因說明
     * @return 更新後的會員資訊
     */
    @PostMapping("/{id}/points/deduct")
    @Operation(
            summary = "扣除會員點數",
            description = "為指定會員扣除點數（需有足夠餘額）"
    )
    public ApiResponse<CustomerDto> deductPoints(
            @Parameter(description = "會員 ID", example = "1", required = true)
            @PathVariable Long id,

            @Parameter(description = "扣除的點數", example = "50", required = true)
            @RequestParam int points,

            @Parameter(description = "原因說明", example = "點數兌換")
            @RequestParam(required = false, defaultValue = "手動扣除") String reason) {
        log.info("API 請求: 扣除點數 - 會員 {}, 點數 {}, 原因: {}", id, points, reason);

        CustomerDto customer = customerService.deductPoints(id, points, reason);

        log.info("點數扣除成功: {} ({}) 目前點數: {}",
                customer.getName(), customer.getMemberNo(), customer.getTotalPoints());

        return ApiResponse.success("點數扣除成功", customer);
    }

    /**
     * 計算消費可獲得的點數
     *
     * <p>預覽消費可獲得的點數，不會實際增加點數。</p>
     *
     * @param id          會員 ID
     * @param spentAmount 消費金額
     * @return 可獲得的點數
     */
    @GetMapping("/{id}/points/calculate")
    @Operation(
            summary = "計算消費可獲得的點數",
            description = "預覽消費可獲得的點數，不會實際增加點數"
    )
    public ApiResponse<Integer> calculatePoints(
            @Parameter(description = "會員 ID", example = "1", required = true)
            @PathVariable Long id,

            @Parameter(description = "消費金額", example = "1000.00", required = true)
            @RequestParam BigDecimal spentAmount) {
        log.info("API 請求: 計算點數 - 會員 {}, 金額 {}", id, spentAmount);

        int points = customerService.calculatePoints(id, spentAmount);

        return ApiResponse.success("計算成功", points);
    }

    // ==================== 消費記錄 ====================

    /**
     * 記錄會員消費
     *
     * <p>記錄會員消費，會自動：</p>
     * <ol>
     *   <li>累積消費金額</li>
     *   <li>計算並增加點數</li>
     *   <li>檢查並升級會員等級</li>
     * </ol>
     *
     * @param id          會員 ID
     * @param spentAmount 消費金額
     * @return 更新後的會員資訊
     */
    @PostMapping("/{id}/spending")
    @Operation(
            summary = "記錄會員消費",
            description = "記錄會員消費，會自動累積消費金額、計算點數、檢查等級升級"
    )
    public ApiResponse<CustomerDto> recordSpending(
            @Parameter(description = "會員 ID", example = "1", required = true)
            @PathVariable Long id,

            @Parameter(description = "消費金額", example = "1500.00", required = true)
            @RequestParam BigDecimal spentAmount) {
        log.info("API 請求: 記錄消費 - 會員 {}, 金額 {}", id, spentAmount);

        CustomerDto customer = customerService.recordSpending(id, spentAmount);

        log.info("消費記錄成功: {} ({}) 累積消費: {}, 目前點數: {}",
                customer.getName(), customer.getMemberNo(),
                customer.getTotalSpent(), customer.getTotalPoints());

        return ApiResponse.success("消費記錄成功", customer);
    }

    // ==================== 生日相關 ====================

    /**
     * 查詢今日壽星
     *
     * @return 今日生日的會員列表
     */
    @GetMapping("/birthday/today")
    @Operation(
            summary = "查詢今日壽星",
            description = "取得今天生日的會員列表"
    )
    public ApiResponse<List<CustomerDto>> getTodayBirthdayCustomers() {
        log.info("API 請求: 查詢今日壽星");

        List<CustomerDto> customers = customerService.getTodayBirthdayCustomers();

        log.info("今日壽星共 {} 位", customers.size());

        return ApiResponse.success("查詢成功", customers);
    }

    /**
     * 查詢本月壽星
     *
     * @return 本月生日的會員列表
     */
    @GetMapping("/birthday/month")
    @Operation(
            summary = "查詢本月壽星",
            description = "取得本月生日的會員列表"
    )
    public ApiResponse<List<CustomerDto>> getThisMonthBirthdayCustomers() {
        log.info("API 請求: 查詢本月壽星");

        List<CustomerDto> customers = customerService.getThisMonthBirthdayCustomers();

        log.info("本月壽星共 {} 位", customers.size());

        return ApiResponse.success("查詢成功", customers);
    }

    // ==================== 統計相關 ====================

    /**
     * 取得會員統計資訊
     *
     * @return 會員統計資訊
     */
    @GetMapping("/statistics")
    @Operation(
            summary = "取得會員統計資訊",
            description = "取得會員相關的統計數據"
    )
    public ApiResponse<CustomerStatistics> getStatistics() {
        log.info("API 請求: 取得會員統計");

        CustomerStatistics stats = CustomerStatistics.builder()
                .totalCustomers(customerService.countAllCustomers())
                .activeCustomers(customerService.countActiveCustomers())
                .todayRegistrations(customerService.countTodayRegistrations())
                .todayBirthdays((long) customerService.getTodayBirthdayCustomers().size())
                .build();

        return ApiResponse.success("查詢成功", stats);
    }

    /**
     * 會員統計資訊
     */
    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class CustomerStatistics {
        /**
         * 會員總數
         */
        private Long totalCustomers;

        /**
         * 啟用會員數
         */
        private Long activeCustomers;

        /**
         * 今日註冊數
         */
        private Long todayRegistrations;

        /**
         * 今日壽星數
         */
        private Long todayBirthdays;
    }
}

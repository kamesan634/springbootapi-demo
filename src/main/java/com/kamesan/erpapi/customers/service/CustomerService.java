package com.kamesan.erpapi.customers.service;

import com.kamesan.erpapi.common.dto.PageResponse;
import com.kamesan.erpapi.common.exception.BusinessException;
import com.kamesan.erpapi.customers.dto.*;
import com.kamesan.erpapi.customers.entity.Customer;
import com.kamesan.erpapi.customers.entity.CustomerLevel;
import com.kamesan.erpapi.customers.repository.CustomerLevelRepository;
import com.kamesan.erpapi.customers.repository.CustomerRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 會員服務
 *
 * <p>處理會員相關的業務邏輯，包括：</p>
 * <ul>
 *   <li>會員 CRUD 操作</li>
 *   <li>會員編號產生</li>
 *   <li>會員等級管理</li>
 *   <li>點數計算與操作</li>
 *   <li>會員等級自動升級</li>
 * </ul>
 *
 * <h2>會員編號規則：</h2>
 * <p>格式：M + 年月日 + 4位流水號，例如：M202501060001</p>
 *
 * <h2>點數計算規則：</h2>
 * <p>消費金額 x 基礎點數率 x 會員等級點數倍率</p>
 *
 * @author ERP System Team
 * @version 1.0.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CustomerService {

    /**
     * 會員 Repository
     */
    private final CustomerRepository customerRepository;

    /**
     * 會員等級 Repository
     */
    private final CustomerLevelRepository customerLevelRepository;

    /**
     * 預設等級代碼
     */
    @Value("${app.customer.default-level-code:NORMAL}")
    private String defaultLevelCode;

    /**
     * 基礎點數率（每消費 1 元可得幾點）
     */
    @Value("${app.customer.base-points-rate:0.01}")
    private BigDecimal basePointsRate;

    // ==================== 會員等級相關方法 ====================

    /**
     * 取得所有會員等級
     *
     * @return 會員等級列表
     */
    @Transactional(readOnly = true)
    public List<CustomerLevelDto> getAllLevels() {
        log.debug("查詢所有會員等級");
        return customerLevelRepository.findAllByOrderBySortOrderAsc()
                .stream()
                .map(level -> {
                    long count = customerLevelRepository.countCustomersByLevelId(level.getId());
                    return CustomerLevelDto.fromEntity(level, count);
                })
                .collect(Collectors.toList());
    }

    /**
     * 取得啟用的會員等級
     *
     * @return 啟用的會員等級列表
     */
    @Transactional(readOnly = true)
    public List<CustomerLevelDto> getActiveLevels() {
        log.debug("查詢啟用的會員等級");
        return customerLevelRepository.findByActiveTrueOrderBySortOrderAsc()
                .stream()
                .map(CustomerLevelDto::fromEntity)
                .collect(Collectors.toList());
    }

    /**
     * 根據 ID 取得會員等級
     *
     * @param id 等級 ID
     * @return 會員等級 DTO
     * @throws BusinessException 若等級不存在
     */
    @Transactional(readOnly = true)
    public CustomerLevelDto getLevelById(Long id) {
        log.debug("查詢會員等級: {}", id);
        CustomerLevel level = findLevelById(id);
        long count = customerLevelRepository.countCustomersByLevelId(id);
        return CustomerLevelDto.fromEntity(level, count);
    }

    // ==================== 會員 CRUD 方法 ====================

    /**
     * 建立會員
     *
     * @param request 建立會員請求
     * @return 新建的會員 DTO
     * @throws BusinessException 若手機或 Email 已存在
     */
    @Transactional
    public CustomerDto createCustomer(CreateCustomerRequest request) {
        log.info("建立會員: {}", request.getName());

        // 驗證手機號碼唯一性
        if (request.getPhone() != null && customerRepository.existsByPhone(request.getPhone())) {
            throw BusinessException.alreadyExists("會員", "手機號碼", request.getPhone());
        }

        // 驗證 Email 唯一性
        if (request.getEmail() != null && customerRepository.existsByEmail(request.getEmail())) {
            throw BusinessException.alreadyExists("會員", "Email", request.getEmail());
        }

        // 取得會員等級
        CustomerLevel level;
        if (request.getLevelId() != null) {
            level = findLevelById(request.getLevelId());
        } else {
            level = findDefaultLevel();
        }

        // 產生會員編號
        String memberNo = generateMemberNo();

        // 建立會員
        Customer customer = Customer.builder()
                .memberNo(memberNo)
                .name(request.getName())
                .phone(request.getPhone())
                .email(request.getEmail())
                .gender(request.getGender())
                .birthday(request.getBirthday())
                .level(level)
                .totalPoints(request.getInitialPoints() != null ? request.getInitialPoints() : 0)
                .totalSpent(BigDecimal.ZERO)
                .registerDate(LocalDateTime.now())
                .active(true)
                .address(request.getAddress())
                .notes(request.getNotes())
                .build();

        customer = customerRepository.save(customer);

        log.info("會員建立成功: {} ({})", customer.getName(), customer.getMemberNo());

        return CustomerDto.fromEntity(customer);
    }

    /**
     * 更新會員
     *
     * @param id      會員 ID
     * @param request 更新會員請求
     * @return 更新後的會員 DTO
     * @throws BusinessException 若會員不存在或手機/Email 已被使用
     */
    @Transactional
    public CustomerDto updateCustomer(Long id, UpdateCustomerRequest request) {
        log.info("更新會員: {}", id);

        Customer customer = findCustomerById(id);

        // 驗證手機號碼唯一性
        if (request.getPhone() != null &&
                customerRepository.existsByPhoneAndIdNot(request.getPhone(), id)) {
            throw BusinessException.alreadyExists("會員", "手機號碼", request.getPhone());
        }

        // 驗證 Email 唯一性
        if (request.getEmail() != null &&
                customerRepository.existsByEmailAndIdNot(request.getEmail(), id)) {
            throw BusinessException.alreadyExists("會員", "Email", request.getEmail());
        }

        // 更新欄位（只更新有提供的欄位）
        if (request.getName() != null) {
            customer.setName(request.getName());
        }
        if (request.getPhone() != null) {
            customer.setPhone(request.getPhone());
        }
        if (request.getEmail() != null) {
            customer.setEmail(request.getEmail());
        }
        if (request.getGender() != null) {
            customer.setGender(request.getGender());
        }
        if (request.getBirthday() != null) {
            customer.setBirthday(request.getBirthday());
        }
        if (request.getLevelId() != null) {
            CustomerLevel level = findLevelById(request.getLevelId());
            customer.setLevel(level);
        }
        if (request.getActive() != null) {
            customer.setActive(request.getActive());
        }
        if (request.getAddress() != null) {
            customer.setAddress(request.getAddress());
        }
        if (request.getNotes() != null) {
            customer.setNotes(request.getNotes());
        }

        customer = customerRepository.save(customer);

        log.info("會員更新成功: {} ({})", customer.getName(), customer.getMemberNo());

        return CustomerDto.fromEntity(customer);
    }

    /**
     * 刪除會員
     * <p>實際上是將會員設為停用狀態（軟刪除）</p>
     *
     * @param id 會員 ID
     * @throws BusinessException 若會員不存在
     */
    @Transactional
    public void deleteCustomer(Long id) {
        log.info("刪除會員: {}", id);

        Customer customer = findCustomerById(id);

        // 軟刪除：設為停用
        customer.setActive(false);
        customerRepository.save(customer);

        log.info("會員已停用: {} ({})", customer.getName(), customer.getMemberNo());
    }

    /**
     * 根據 ID 取得會員
     *
     * @param id 會員 ID
     * @return 會員 DTO
     * @throws BusinessException 若會員不存在
     */
    @Transactional(readOnly = true)
    public CustomerDto getCustomerById(Long id) {
        log.debug("查詢會員: {}", id);
        Customer customer = findCustomerById(id);
        return CustomerDto.fromEntity(customer);
    }

    /**
     * 根據會員編號取得會員
     *
     * @param memberNo 會員編號
     * @return 會員 DTO
     * @throws BusinessException 若會員不存在
     */
    @Transactional(readOnly = true)
    public CustomerDto getCustomerByMemberNo(String memberNo) {
        log.debug("根據會員編號查詢: {}", memberNo);
        Customer customer = customerRepository.findByMemberNo(memberNo)
                .orElseThrow(() -> new BusinessException(404, "會員編號 " + memberNo + " 不存在"));
        return CustomerDto.fromEntity(customer);
    }

    /**
     * 分頁查詢會員
     *
     * @param pageable 分頁參數
     * @return 會員分頁結果
     */
    @Transactional(readOnly = true)
    public PageResponse<CustomerDto> getCustomers(Pageable pageable) {
        log.debug("分頁查詢會員");
        Page<Customer> page = customerRepository.findAll(pageable);
        List<CustomerDto> content = page.getContent().stream()
                .map(CustomerDto::fromEntity)
                .collect(Collectors.toList());
        return PageResponse.of(page, content);
    }

    /**
     * 搜尋會員
     *
     * @param keyword  關鍵字
     * @param pageable 分頁參數
     * @return 會員分頁結果
     */
    @Transactional(readOnly = true)
    public PageResponse<CustomerDto> searchCustomers(String keyword, Pageable pageable) {
        log.debug("搜尋會員: {}", keyword);
        Page<Customer> page = customerRepository.search(keyword, pageable);
        List<CustomerDto> content = page.getContent().stream()
                .map(CustomerDto::fromEntity)
                .collect(Collectors.toList());
        return PageResponse.of(page, content);
    }

    /**
     * 複合條件查詢會員
     *
     * @param keyword  搜尋關鍵字（可為 null）
     * @param levelId  等級 ID（可為 null）
     * @param active   啟用狀態（可為 null）
     * @param gender   性別（可為 null）
     * @param pageable 分頁參數
     * @return 會員分頁結果
     */
    @Transactional(readOnly = true)
    public PageResponse<CustomerDto> findCustomers(
            String keyword, Long levelId, Boolean active, String gender, Pageable pageable) {
        log.debug("複合條件查詢會員");
        Page<Customer> page = customerRepository.findByConditions(keyword, levelId, active, gender, pageable);
        List<CustomerDto> content = page.getContent().stream()
                .map(CustomerDto::fromEntity)
                .collect(Collectors.toList());
        return PageResponse.of(page, content);
    }

    // ==================== 點數相關方法 ====================

    /**
     * 計算消費可獲得的點數
     *
     * @param customerId  會員 ID
     * @param spentAmount 消費金額
     * @return 可獲得的點數
     * @throws BusinessException 若會員不存在
     */
    @Transactional(readOnly = true)
    public int calculatePoints(Long customerId, BigDecimal spentAmount) {
        Customer customer = findCustomerById(customerId);
        return customer.getLevel().calculatePoints(spentAmount, basePointsRate);
    }

    /**
     * 增加點數
     *
     * @param customerId 會員 ID
     * @param points     點數
     * @param reason     原因說明
     * @return 更新後的會員 DTO
     * @throws BusinessException 若會員不存在或點數為負
     */
    @Transactional
    public CustomerDto addPoints(Long customerId, int points, String reason) {
        if (points < 0) {
            throw BusinessException.validationFailed("增加的點數不能為負數");
        }

        log.info("增加點數: 會員 {}, 點數 {}, 原因: {}", customerId, points, reason);

        Customer customer = findCustomerById(customerId);
        customer.addPoints(points);
        customer = customerRepository.save(customer);

        log.info("點數增加成功: {} ({}) 目前點數: {}",
                customer.getName(), customer.getMemberNo(), customer.getTotalPoints());

        return CustomerDto.fromEntity(customer);
    }

    /**
     * 扣除點數
     *
     * @param customerId 會員 ID
     * @param points     點數
     * @param reason     原因說明
     * @return 更新後的會員 DTO
     * @throws BusinessException 若會員不存在、點數為負或餘額不足
     */
    @Transactional
    public CustomerDto deductPoints(Long customerId, int points, String reason) {
        if (points < 0) {
            throw BusinessException.validationFailed("扣除的點數不能為負數");
        }

        log.info("扣除點數: 會員 {}, 點數 {}, 原因: {}", customerId, points, reason);

        Customer customer = findCustomerById(customerId);

        if (!customer.hasEnoughPoints(points)) {
            throw BusinessException.validationFailed(
                    String.format("點數餘額不足，目前: %d，需要: %d", customer.getTotalPoints(), points));
        }

        customer.deductPoints(points);
        customer = customerRepository.save(customer);

        log.info("點數扣除成功: {} ({}) 目前點數: {}",
                customer.getName(), customer.getMemberNo(), customer.getTotalPoints());

        return CustomerDto.fromEntity(customer);
    }

    /**
     * 記錄消費並計算點數
     * <p>此方法會：</p>
     * <ol>
     *   <li>累積消費金額</li>
     *   <li>計算並增加點數</li>
     *   <li>檢查是否可升級等級</li>
     * </ol>
     *
     * @param customerId  會員 ID
     * @param spentAmount 消費金額
     * @return 更新後的會員 DTO
     * @throws BusinessException 若會員不存在或金額為負
     */
    @Transactional
    public CustomerDto recordSpending(Long customerId, BigDecimal spentAmount) {
        if (spentAmount == null || spentAmount.compareTo(BigDecimal.ZERO) < 0) {
            throw BusinessException.validationFailed("消費金額不能為負數");
        }

        log.info("記錄消費: 會員 {}, 金額 {}", customerId, spentAmount);

        Customer customer = findCustomerById(customerId);

        // 累積消費金額
        customer.addSpent(spentAmount);

        // 計算點數
        int points = customer.getLevel().calculatePoints(spentAmount, basePointsRate);
        if (points > 0) {
            customer.addPoints(points);
        }

        customer = customerRepository.save(customer);

        // 檢查並執行等級升級
        checkAndUpgradeLevel(customer);

        log.info("消費記錄成功: {} ({}) 累積消費: {}, 獲得點數: {}",
                customer.getName(), customer.getMemberNo(), customer.getTotalSpent(), points);

        return CustomerDto.fromEntity(customer);
    }

    /**
     * 檢查並升級會員等級
     * <p>根據累積消費金額自動升級至對應等級</p>
     *
     * @param customer 會員
     */
    private void checkAndUpgradeLevel(Customer customer) {
        CustomerLevel currentLevel = customer.getLevel();

        // 查詢可升級至的最高等級
        customerLevelRepository.findHighestEligibleLevel(customer.getTotalSpent())
                .ifPresent(newLevel -> {
                    // 只有當新等級比目前等級更高時才升級
                    if (newLevel.getSortOrder() > currentLevel.getSortOrder()) {
                        log.info("會員等級升級: {} ({}) {} -> {}",
                                customer.getName(), customer.getMemberNo(),
                                currentLevel.getName(), newLevel.getName());
                        customer.setLevel(newLevel);
                        customerRepository.save(customer);
                    }
                });
    }

    // ==================== 生日相關方法 ====================

    /**
     * 查詢今天生日的會員
     *
     * @return 今日壽星列表
     */
    @Transactional(readOnly = true)
    public List<CustomerDto> getTodayBirthdayCustomers() {
        LocalDate today = LocalDate.now();
        log.debug("查詢今日壽星: {}/{}", today.getMonthValue(), today.getDayOfMonth());

        return customerRepository.findBirthdayCustomers(today.getMonthValue(), today.getDayOfMonth())
                .stream()
                .map(CustomerDto::fromEntity)
                .collect(Collectors.toList());
    }

    /**
     * 查詢本月生日的會員
     *
     * @return 本月壽星列表
     */
    @Transactional(readOnly = true)
    public List<CustomerDto> getThisMonthBirthdayCustomers() {
        int month = LocalDate.now().getMonthValue();
        log.debug("查詢本月壽星: {} 月", month);

        return customerRepository.findBirthdayCustomersInMonth(month)
                .stream()
                .map(CustomerDto::fromEntity)
                .collect(Collectors.toList());
    }

    // ==================== 私有輔助方法 ====================

    /**
     * 根據 ID 查詢會員（內部使用）
     *
     * @param id 會員 ID
     * @return 會員 Entity
     * @throws BusinessException 若會員不存在
     */
    private Customer findCustomerById(Long id) {
        return customerRepository.findById(id)
                .orElseThrow(() -> BusinessException.notFound("會員", id));
    }

    /**
     * 根據 ID 查詢會員等級（內部使用）
     *
     * @param id 等級 ID
     * @return 會員等級 Entity
     * @throws BusinessException 若等級不存在
     */
    private CustomerLevel findLevelById(Long id) {
        return customerLevelRepository.findById(id)
                .orElseThrow(() -> BusinessException.notFound("會員等級", id));
    }

    /**
     * 取得預設會員等級
     *
     * @return 預設等級
     * @throws BusinessException 若預設等級不存在
     */
    private CustomerLevel findDefaultLevel() {
        return customerLevelRepository.findByCode(defaultLevelCode)
                .orElseThrow(() -> new BusinessException(500,
                        "系統預設會員等級 " + defaultLevelCode + " 不存在，請聯繫系統管理員"));
    }

    /**
     * 產生會員編號
     * <p>格式：M + 年月日 + 4位流水號</p>
     * <p>例如：M202501060001</p>
     *
     * @return 新的會員編號
     */
    private String generateMemberNo() {
        String datePrefix = "M" + LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));

        // 查詢今日最大編號
        String maxMemberNo = customerRepository.findMaxMemberNoByPrefix(datePrefix)
                .orElse(null);

        int sequence;
        if (maxMemberNo == null) {
            sequence = 1;
        } else {
            // 取得流水號部分並加 1
            String sequencePart = maxMemberNo.substring(datePrefix.length());
            sequence = Integer.parseInt(sequencePart) + 1;
        }

        // 格式化為 4 位數
        return datePrefix + String.format("%04d", sequence);
    }

    // ==================== 統計相關方法 ====================

    /**
     * 統計會員總數
     *
     * @return 會員總數
     */
    @Transactional(readOnly = true)
    public long countAllCustomers() {
        return customerRepository.count();
    }

    /**
     * 統計啟用的會員數
     *
     * @return 啟用的會員數
     */
    @Transactional(readOnly = true)
    public long countActiveCustomers() {
        return customerRepository.countByActiveTrue();
    }

    /**
     * 統計今日新增會員數
     *
     * @return 今日新增會員數
     */
    @Transactional(readOnly = true)
    public long countTodayRegistrations() {
        LocalDateTime startOfDay = LocalDate.now().atStartOfDay();
        LocalDateTime endOfDay = startOfDay.plusDays(1).minusNanos(1);
        return customerRepository.countRegisteredToday(startOfDay, endOfDay);
    }
}

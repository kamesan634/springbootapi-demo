package com.kamesan.erpapi.purchasing.service;

import com.kamesan.erpapi.common.exception.BusinessException;
import com.kamesan.erpapi.purchasing.dto.CreateSupplierRequest;
import com.kamesan.erpapi.purchasing.dto.SupplierDto;
import com.kamesan.erpapi.purchasing.dto.UpdateSupplierRequest;
import com.kamesan.erpapi.purchasing.entity.Supplier;
import com.kamesan.erpapi.purchasing.repository.SupplierRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 供應商服務
 *
 * <p>提供供應商相關的業務邏輯處理，包括：</p>
 * <ul>
 *   <li>新增供應商 - {@link #createSupplier(CreateSupplierRequest)}</li>
 *   <li>查詢供應商（單筆）- {@link #getSupplierById(Long)}, {@link #getSupplierByCode(String)}</li>
 *   <li>查詢供應商（列表）- {@link #getAllSuppliers(Pageable)}, {@link #getActiveSuppliers()}</li>
 *   <li>搜尋供應商 - {@link #searchSuppliers(String, Pageable)}</li>
 *   <li>更新供應商 - {@link #updateSupplier(Long, UpdateSupplierRequest)}</li>
 *   <li>刪除供應商 - {@link #deleteSupplier(Long)}</li>
 *   <li>啟用/停用供應商 - {@link #toggleSupplierStatus(Long)}</li>
 * </ul>
 *
 * <p>資料驗證：</p>
 * <ul>
 *   <li>供應商代碼唯一性檢查</li>
 *   <li>供應商名稱唯一性檢查</li>
 *   <li>供應商存在性檢查</li>
 * </ul>
 *
 * <p>交易管理：</p>
 * <ul>
 *   <li>查詢操作使用 {@code @Transactional(readOnly = true)} 優化效能</li>
 *   <li>寫入操作使用 {@code @Transactional} 確保資料一致性</li>
 * </ul>
 *
 * @author ERP System Team
 * @version 1.0.0
 * @see SupplierRepository
 * @see Supplier
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SupplierService {

    /**
     * 供應商 Repository
     *
     * <p>用於存取供應商資料。</p>
     */
    private final SupplierRepository supplierRepository;

    /**
     * 新增供應商
     *
     * <p>建立新的供應商資料。</p>
     *
     * <p>處理流程：</p>
     * <ol>
     *   <li>檢查供應商代碼是否已存在</li>
     *   <li>檢查供應商名稱是否已存在</li>
     *   <li>建立供應商實體</li>
     *   <li>儲存至資料庫</li>
     *   <li>回傳供應商 DTO</li>
     * </ol>
     *
     * @param request 新增供應商請求，包含供應商基本資訊
     * @return 新增成功的供應商資訊
     * @throws BusinessException 當供應商代碼或名稱已存在時拋出
     */
    @Transactional
    public SupplierDto createSupplier(CreateSupplierRequest request) {
        log.info("新增供應商，代碼: {}, 名稱: {}", request.getCode(), request.getName());

        // 檢查供應商代碼是否已存在
        if (supplierRepository.existsByCode(request.getCode())) {
            log.warn("供應商代碼已存在: {}", request.getCode());
            throw BusinessException.alreadyExists("供應商", "代碼", request.getCode());
        }

        // 檢查供應商名稱是否已存在
        if (supplierRepository.existsByName(request.getName())) {
            log.warn("供應商名稱已存在: {}", request.getName());
            throw BusinessException.alreadyExists("供應商", "名稱", request.getName());
        }

        // 建立供應商實體
        Supplier supplier = Supplier.builder()
                .code(request.getCode())
                .name(request.getName())
                .contactPerson(request.getContactPerson())
                .phone(request.getPhone())
                .email(request.getEmail())
                .address(request.getAddress())
                .paymentTerms(request.getPaymentTerms())
                .isActive(request.getIsActive() != null ? request.getIsActive() : true)
                .notes(request.getNotes())
                .build();

        // 儲存並回傳
        Supplier savedSupplier = supplierRepository.save(supplier);
        log.info("供應商新增成功，ID: {}", savedSupplier.getId());

        return convertToDto(savedSupplier);
    }

    /**
     * 根據 ID 查詢供應商
     *
     * @param id 供應商 ID
     * @return 供應商資訊
     * @throws BusinessException 當供應商不存在時拋出 404 錯誤
     */
    @Transactional(readOnly = true)
    public SupplierDto getSupplierById(Long id) {
        log.debug("查詢供應商，ID: {}", id);

        Supplier supplier = supplierRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("供應商不存在，ID: {}", id);
                    return BusinessException.notFound("供應商", id);
                });

        return convertToDto(supplier);
    }

    /**
     * 根據代碼查詢供應商
     *
     * @param code 供應商代碼
     * @return 供應商資訊
     * @throws BusinessException 當供應商不存在時拋出 404 錯誤
     */
    @Transactional(readOnly = true)
    public SupplierDto getSupplierByCode(String code) {
        log.debug("查詢供應商，代碼: {}", code);

        Supplier supplier = supplierRepository.findByCode(code)
                .orElseThrow(() -> {
                    log.warn("供應商不存在，代碼: {}", code);
                    return new BusinessException(404, "供應商代碼 '" + code + "' 不存在");
                });

        return convertToDto(supplier);
    }

    /**
     * 查詢所有供應商（分頁）
     *
     * <p>回傳所有供應商資料，支援分頁和排序。</p>
     *
     * @param pageable 分頁參數，包含頁碼、每頁筆數、排序方式
     * @return 供應商分頁結果
     */
    @Transactional(readOnly = true)
    public Page<SupplierDto> getAllSuppliers(Pageable pageable) {
        log.debug("查詢所有供應商，分頁: {}", pageable);

        return supplierRepository.findAll(pageable)
                .map(this::convertToDto);
    }

    /**
     * 查詢啟用中的供應商（不分頁）
     *
     * <p>回傳所有啟用中的供應商，適用於下拉選單等場景。</p>
     *
     * @return 啟用中的供應商清單
     */
    @Transactional(readOnly = true)
    public List<SupplierDto> getActiveSuppliers() {
        log.debug("查詢啟用中的供應商");

        return supplierRepository.findByIsActiveTrue().stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    /**
     * 根據啟用狀態查詢供應商（分頁）
     *
     * @param isActive 啟用狀態（true: 啟用, false: 停用）
     * @param pageable 分頁參數
     * @return 供應商分頁結果
     */
    @Transactional(readOnly = true)
    public Page<SupplierDto> getSuppliersByStatus(Boolean isActive, Pageable pageable) {
        log.debug("根據啟用狀態查詢供應商，isActive: {}, 分頁: {}", isActive, pageable);

        return supplierRepository.findByIsActive(isActive, pageable)
                .map(this::convertToDto);
    }

    /**
     * 搜尋供應商
     *
     * <p>根據關鍵字搜尋供應商，會比對以下欄位：</p>
     * <ul>
     *   <li>供應商代碼</li>
     *   <li>供應商名稱</li>
     *   <li>聯絡人姓名</li>
     *   <li>電子郵件</li>
     *   <li>電話號碼</li>
     * </ul>
     *
     * @param keyword  搜尋關鍵字
     * @param pageable 分頁參數
     * @return 符合條件的供應商分頁結果
     */
    @Transactional(readOnly = true)
    public Page<SupplierDto> searchSuppliers(String keyword, Pageable pageable) {
        log.debug("搜尋供應商，關鍵字: {}, 分頁: {}", keyword, pageable);

        return supplierRepository.search(keyword, pageable)
                .map(this::convertToDto);
    }

    /**
     * 搜尋啟用中的供應商
     *
     * <p>根據關鍵字搜尋啟用中的供應商。</p>
     *
     * @param keyword  搜尋關鍵字
     * @param pageable 分頁參數
     * @return 符合條件的啟用供應商分頁結果
     */
    @Transactional(readOnly = true)
    public Page<SupplierDto> searchActiveSuppliers(String keyword, Pageable pageable) {
        log.debug("搜尋啟用中的供應商，關鍵字: {}, 分頁: {}", keyword, pageable);

        return supplierRepository.searchActive(keyword, pageable)
                .map(this::convertToDto);
    }

    /**
     * 更新供應商
     *
     * <p>更新指定供應商的資料。</p>
     *
     * <p>處理流程：</p>
     * <ol>
     *   <li>檢查供應商是否存在</li>
     *   <li>檢查新代碼是否與其他供應商重複</li>
     *   <li>檢查新名稱是否與其他供應商重複</li>
     *   <li>更新供應商資料</li>
     *   <li>儲存並回傳更新後的資料</li>
     * </ol>
     *
     * @param id      供應商 ID
     * @param request 更新供應商請求
     * @return 更新後的供應商資訊
     * @throws BusinessException 當供應商不存在，或代碼/名稱與其他供應商重複時拋出
     */
    @Transactional
    public SupplierDto updateSupplier(Long id, UpdateSupplierRequest request) {
        log.info("更新供應商，ID: {}", id);

        // 查詢供應商
        Supplier supplier = supplierRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("供應商不存在，ID: {}", id);
                    return BusinessException.notFound("供應商", id);
                });

        // 檢查供應商代碼是否與其他供應商重複
        if (supplierRepository.existsByCodeAndIdNot(request.getCode(), id)) {
            log.warn("供應商代碼已被其他供應商使用: {}", request.getCode());
            throw BusinessException.alreadyExists("供應商", "代碼", request.getCode());
        }

        // 檢查供應商名稱是否與其他供應商重複
        if (supplierRepository.existsByNameAndIdNot(request.getName(), id)) {
            log.warn("供應商名稱已被其他供應商使用: {}", request.getName());
            throw BusinessException.alreadyExists("供應商", "名稱", request.getName());
        }

        // 更新供應商資料
        supplier.setCode(request.getCode());
        supplier.setName(request.getName());
        supplier.setContactPerson(request.getContactPerson());
        supplier.setPhone(request.getPhone());
        supplier.setEmail(request.getEmail());
        supplier.setAddress(request.getAddress());
        supplier.setPaymentTerms(request.getPaymentTerms());
        supplier.setNotes(request.getNotes());

        // 只有當 isActive 有值時才更新
        if (request.getIsActive() != null) {
            supplier.setIsActive(request.getIsActive());
        }

        // 儲存並回傳
        Supplier updatedSupplier = supplierRepository.save(supplier);
        log.info("供應商更新成功，ID: {}", id);

        return convertToDto(updatedSupplier);
    }

    /**
     * 刪除供應商
     *
     * <p>刪除指定的供應商資料。</p>
     *
     * <p>注意事項：</p>
     * <ul>
     *   <li>此操作為永久刪除，無法復原</li>
     *   <li>若供應商有關聯的採購訂單，建議使用停用功能而非刪除</li>
     * </ul>
     *
     * @param id 供應商 ID
     * @throws BusinessException 當供應商不存在時拋出 404 錯誤
     */
    @Transactional
    public void deleteSupplier(Long id) {
        log.info("刪除供應商，ID: {}", id);

        // 檢查供應商是否存在
        if (!supplierRepository.existsById(id)) {
            log.warn("供應商不存在，ID: {}", id);
            throw BusinessException.notFound("供應商", id);
        }

        // TODO: 檢查是否有關聯的採購訂單，若有則拋出異常或改為停用

        supplierRepository.deleteById(id);
        log.info("供應商刪除成功，ID: {}", id);
    }

    /**
     * 切換供應商啟用狀態
     *
     * <p>將供應商的啟用狀態反轉：</p>
     * <ul>
     *   <li>啟用 -> 停用</li>
     *   <li>停用 -> 啟用</li>
     * </ul>
     *
     * @param id 供應商 ID
     * @return 更新後的供應商資訊（包含新的啟用狀態）
     * @throws BusinessException 當供應商不存在時拋出 404 錯誤
     */
    @Transactional
    public SupplierDto toggleSupplierStatus(Long id) {
        log.info("切換供應商啟用狀態，ID: {}", id);

        Supplier supplier = supplierRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("供應商不存在，ID: {}", id);
                    return BusinessException.notFound("供應商", id);
                });

        // 切換狀態
        boolean newStatus = !supplier.getIsActive();
        supplier.setIsActive(newStatus);

        Supplier updatedSupplier = supplierRepository.save(supplier);
        log.info("供應商狀態已切換，ID: {}, 新狀態: {}", id, newStatus ? "啟用" : "停用");

        return convertToDto(updatedSupplier);
    }

    /**
     * 啟用供應商
     *
     * @param id 供應商 ID
     * @return 更新後的供應商資訊
     * @throws BusinessException 當供應商不存在時拋出 404 錯誤
     */
    @Transactional
    public SupplierDto activateSupplier(Long id) {
        log.info("啟用供應商，ID: {}", id);

        Supplier supplier = supplierRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("供應商不存在，ID: {}", id);
                    return BusinessException.notFound("供應商", id);
                });

        supplier.setIsActive(true);
        Supplier updatedSupplier = supplierRepository.save(supplier);
        log.info("供應商已啟用，ID: {}", id);

        return convertToDto(updatedSupplier);
    }

    /**
     * 停用供應商
     *
     * @param id 供應商 ID
     * @return 更新後的供應商資訊
     * @throws BusinessException 當供應商不存在時拋出 404 錯誤
     */
    @Transactional
    public SupplierDto deactivateSupplier(Long id) {
        log.info("停用供應商，ID: {}", id);

        Supplier supplier = supplierRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("供應商不存在，ID: {}", id);
                    return BusinessException.notFound("供應商", id);
                });

        supplier.setIsActive(false);
        Supplier updatedSupplier = supplierRepository.save(supplier);
        log.info("供應商已停用，ID: {}", id);

        return convertToDto(updatedSupplier);
    }

    /**
     * 統計供應商數量
     *
     * @return 供應商總數
     */
    @Transactional(readOnly = true)
    public long countSuppliers() {
        return supplierRepository.count();
    }

    /**
     * 統計啟用中的供應商數量
     *
     * @return 啟用中的供應商數量
     */
    @Transactional(readOnly = true)
    public long countActiveSuppliers() {
        return supplierRepository.countByIsActiveTrue();
    }

    /**
     * 將供應商實體轉換為 DTO
     *
     * <p>私有方法，用於將 {@link Supplier} 實體轉換為 {@link SupplierDto}。</p>
     *
     * @param supplier 供應商實體
     * @return 供應商 DTO
     */
    private SupplierDto convertToDto(Supplier supplier) {
        return SupplierDto.builder()
                .id(supplier.getId())
                .code(supplier.getCode())
                .name(supplier.getName())
                .contactPerson(supplier.getContactPerson())
                .phone(supplier.getPhone())
                .email(supplier.getEmail())
                .address(supplier.getAddress())
                .paymentTerms(supplier.getPaymentTerms())
                .isActive(supplier.getIsActive())
                .notes(supplier.getNotes())
                .createdAt(supplier.getCreatedAt())
                .updatedAt(supplier.getUpdatedAt())
                .createdBy(supplier.getCreatedBy())
                .updatedBy(supplier.getUpdatedBy())
                .build();
    }
}

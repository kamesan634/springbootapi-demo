package com.kamesan.erpapi.inventory.service;

import com.kamesan.erpapi.common.exception.BusinessException;
import com.kamesan.erpapi.inventory.dto.AdjustInventoryRequest;
import com.kamesan.erpapi.inventory.dto.InventoryDto;
import com.kamesan.erpapi.inventory.dto.InventoryMovementDto;
import com.kamesan.erpapi.inventory.entity.Inventory;
import com.kamesan.erpapi.inventory.entity.InventoryMovement;
import com.kamesan.erpapi.inventory.entity.MovementType;
import com.kamesan.erpapi.inventory.repository.InventoryMovementRepository;
import com.kamesan.erpapi.inventory.repository.InventoryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 庫存服務
 *
 * <p>處理庫存相關的業務邏輯，包括：</p>
 * <ul>
 *   <li>庫存查詢 - 查詢商品在各倉庫的庫存</li>
 *   <li>庫存調整 - 盤盈盤虧調整</li>
 *   <li>庫存異動 - 記錄所有庫存變化</li>
 *   <li>庫存預留 - 訂單庫存預留與釋放</li>
 *   <li>庫存預警 - 低庫存預警查詢</li>
 * </ul>
 *
 * <p>所有庫存異動操作都會：</p>
 * <ol>
 *   <li>使用悲觀鎖定防止並發問題</li>
 *   <li>自動建立庫存異動記錄</li>
 *   <li>更新最後異動時間</li>
 * </ol>
 *
 * @author ERP System Team
 * @version 1.0.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class InventoryService {

    /**
     * 庫存 Repository
     */
    private final InventoryRepository inventoryRepository;

    /**
     * 庫存異動記錄 Repository
     */
    private final InventoryMovementRepository movementRepository;

    // ==================== 庫存查詢 ====================

    /**
     * 根據商品和倉庫查詢庫存
     *
     * @param productId   商品 ID
     * @param warehouseId 倉庫 ID
     * @return 庫存資訊
     * @throws BusinessException 如果庫存記錄不存在
     */
    @Transactional(readOnly = true)
    public InventoryDto getInventory(Long productId, Long warehouseId) {
        log.debug("查詢庫存: productId={}, warehouseId={}", productId, warehouseId);

        Inventory inventory = inventoryRepository
                .findByProductIdAndWarehouseId(productId, warehouseId)
                .orElseThrow(() -> BusinessException.notFound("庫存記錄",
                        String.format("productId=%d, warehouseId=%d", productId, warehouseId)));

        return InventoryDto.fromEntity(inventory);
    }

    /**
     * 查詢商品在所有倉庫的庫存
     *
     * @param productId 商品 ID
     * @return 庫存列表
     */
    @Transactional(readOnly = true)
    public List<InventoryDto> getInventoriesByProduct(Long productId) {
        log.debug("查詢商品所有倉庫庫存: productId={}", productId);

        return inventoryRepository.findByProductId(productId).stream()
                .map(InventoryDto::fromEntity)
                .collect(Collectors.toList());
    }

    /**
     * 分頁查詢所有庫存
     *
     * @param pageable 分頁參數
     * @return 庫存分頁
     */
    @Transactional(readOnly = true)
    public Page<InventoryDto> getAllInventories(Pageable pageable) {
        log.debug("分頁查詢所有庫存");

        return inventoryRepository.findAll(pageable)
                .map(InventoryDto::fromEntity);
    }

    /**
     * 分頁查詢倉庫的所有庫存
     *
     * @param warehouseId 倉庫 ID
     * @param pageable    分頁參數
     * @return 庫存分頁
     */
    @Transactional(readOnly = true)
    public Page<InventoryDto> getInventoriesByWarehouse(Long warehouseId, Pageable pageable) {
        log.debug("分頁查詢倉庫庫存: warehouseId={}", warehouseId);

        return inventoryRepository.findByWarehouseId(warehouseId, pageable)
                .map(InventoryDto::fromEntity);
    }

    /**
     * 查詢商品的總庫存數量
     *
     * @param productId 商品 ID
     * @return 所有倉庫的總庫存數量
     */
    @Transactional(readOnly = true)
    public Integer getTotalQuantity(Long productId) {
        return inventoryRepository.sumQuantityByProductId(productId);
    }

    /**
     * 查詢商品的可用庫存總數
     *
     * @param productId 商品 ID
     * @return 所有倉庫的可用庫存總數
     */
    @Transactional(readOnly = true)
    public Integer getTotalAvailableQuantity(Long productId) {
        return inventoryRepository.sumAvailableQuantityByProductId(productId);
    }

    /**
     * 查詢低於安全庫存的記錄
     *
     * @param threshold 安全庫存閾值
     * @param pageable  分頁參數
     * @return 低庫存記錄分頁
     */
    @Transactional(readOnly = true)
    public Page<InventoryDto> getLowStockInventories(Integer threshold, Pageable pageable) {
        log.debug("查詢低庫存預警: threshold={}", threshold);

        return inventoryRepository.findLowStockInventories(threshold, pageable)
                .map(InventoryDto::fromEntity);
    }

    // ==================== 庫存調整 ====================

    /**
     * 庫存調整（盤盈/盤虧）
     *
     * <p>用於盤點時調整庫存，會自動建立庫存異動記錄。</p>
     *
     * @param request    調整請求
     * @param operatorId 操作人員 ID
     * @return 調整後的庫存資訊
     * @throws BusinessException 如果調整類型無效或庫存不足
     */
    @Transactional
    public InventoryDto adjustInventory(AdjustInventoryRequest request, Long operatorId) {
        log.info("庫存調整: productId={}, warehouseId={}, type={}, quantity={}",
                request.getProductId(), request.getWarehouseId(),
                request.getAdjustmentType(), request.getQuantity());

        // 驗證調整類型
        if (!request.isValidAdjustmentType()) {
            throw BusinessException.validationFailed(
                    "調整類型必須是 ADJUST_IN（盤盈）或 ADJUST_OUT（盤虧）");
        }

        // 取得或建立庫存記錄（使用悲觀鎖定）
        Inventory inventory = getOrCreateInventoryForUpdate(
                request.getProductId(), request.getWarehouseId());

        int beforeQuantity = inventory.getQuantity();

        // 執行調整
        if (request.isAdjustIn()) {
            inventory.increaseQuantity(request.getQuantity());
        } else {
            // 檢查庫存是否足夠
            if (inventory.getQuantity() < request.getQuantity()) {
                throw BusinessException.validationFailed(
                        String.format("庫存不足，目前庫存: %d，欲調整數量: %d",
                                inventory.getQuantity(), request.getQuantity()));
            }
            inventory.decreaseQuantity(request.getQuantity());
        }

        // 儲存庫存
        inventory = inventoryRepository.save(inventory);

        // 建立異動記錄
        InventoryMovement movement = InventoryMovement.create(
                request.getProductId(),
                request.getWarehouseId(),
                request.getAdjustmentType(),
                request.getQuantity(),
                beforeQuantity,
                request.getReferenceNo(),
                request.getReason(),
                operatorId
        );
        movementRepository.save(movement);

        log.info("庫存調整完成: productId={}, warehouseId={}, before={}, after={}",
                request.getProductId(), request.getWarehouseId(),
                beforeQuantity, inventory.getQuantity());

        return InventoryDto.fromEntity(inventory);
    }

    // ==================== 庫存異動（內部使用）====================

    /**
     * 執行庫存異動
     *
     * <p>內部方法，供其他服務模組呼叫（如採購、銷售模組）。</p>
     *
     * @param productId    商品 ID
     * @param warehouseId  倉庫 ID
     * @param movementType 異動類型
     * @param quantity     異動數量
     * @param referenceNo  參考單號
     * @param remark       備註
     * @param operatorId   操作人員 ID
     * @return 異動後的庫存資訊
     * @throws BusinessException 如果庫存不足（出庫時）
     */
    @Transactional
    public InventoryDto executeMovement(
            Long productId,
            Long warehouseId,
            MovementType movementType,
            Integer quantity,
            String referenceNo,
            String remark,
            Long operatorId) {

        log.info("執行庫存異動: productId={}, warehouseId={}, type={}, quantity={}",
                productId, warehouseId, movementType, quantity);

        // 取得或建立庫存記錄（使用悲觀鎖定）
        Inventory inventory = getOrCreateInventoryForUpdate(productId, warehouseId);

        int beforeQuantity = inventory.getQuantity();

        // 根據異動類型執行操作
        if (movementType.isInbound()) {
            inventory.increaseQuantity(quantity);
        } else {
            if (inventory.getQuantity() < quantity) {
                throw BusinessException.validationFailed(
                        String.format("庫存不足，目前庫存: %d，需要數量: %d",
                                inventory.getQuantity(), quantity));
            }
            inventory.decreaseQuantity(quantity);
        }

        // 儲存庫存
        inventory = inventoryRepository.save(inventory);

        // 建立異動記錄
        InventoryMovement movement = InventoryMovement.create(
                productId, warehouseId, movementType, quantity,
                beforeQuantity, referenceNo, remark, operatorId
        );
        movementRepository.save(movement);

        log.info("庫存異動完成: productId={}, warehouseId={}, type={}, before={}, after={}",
                productId, warehouseId, movementType, beforeQuantity, inventory.getQuantity());

        return InventoryDto.fromEntity(inventory);
    }

    // ==================== 庫存預留 ====================

    /**
     * 預留庫存
     *
     * <p>當訂單成立時，預留庫存以防止超賣。</p>
     *
     * @param productId   商品 ID
     * @param warehouseId 倉庫 ID
     * @param quantity    預留數量
     * @throws BusinessException 如果可用庫存不足
     */
    @Transactional
    public void reserveStock(Long productId, Long warehouseId, Integer quantity) {
        log.info("預留庫存: productId={}, warehouseId={}, quantity={}",
                productId, warehouseId, quantity);

        Inventory inventory = inventoryRepository
                .findByProductIdAndWarehouseIdForUpdate(productId, warehouseId)
                .orElseThrow(() -> BusinessException.notFound("庫存記錄",
                        String.format("productId=%d, warehouseId=%d", productId, warehouseId)));

        if (!inventory.hasAvailableStock(quantity)) {
            throw BusinessException.validationFailed(
                    String.format("可用庫存不足，可用庫存: %d，需要預留: %d",
                            inventory.getAvailableQuantity(), quantity));
        }

        inventory.reserve(quantity);
        inventoryRepository.save(inventory);

        log.info("庫存預留成功: productId={}, warehouseId={}, reserved={}",
                productId, warehouseId, quantity);
    }

    /**
     * 釋放預留庫存
     *
     * <p>當訂單取消時，釋放預留的庫存。</p>
     *
     * @param productId   商品 ID
     * @param warehouseId 倉庫 ID
     * @param quantity    釋放數量
     * @throws BusinessException 如果預留數量不足
     */
    @Transactional
    public void releaseStock(Long productId, Long warehouseId, Integer quantity) {
        log.info("釋放預留庫存: productId={}, warehouseId={}, quantity={}",
                productId, warehouseId, quantity);

        Inventory inventory = inventoryRepository
                .findByProductIdAndWarehouseIdForUpdate(productId, warehouseId)
                .orElseThrow(() -> BusinessException.notFound("庫存記錄",
                        String.format("productId=%d, warehouseId=%d", productId, warehouseId)));

        inventory.release(quantity);
        inventoryRepository.save(inventory);

        log.info("預留庫存釋放成功: productId={}, warehouseId={}, released={}",
                productId, warehouseId, quantity);
    }

    /**
     * 確認出貨
     *
     * <p>訂單出貨時，同時減少庫存和釋放預留數量，並建立出庫記錄。</p>
     *
     * @param productId   商品 ID
     * @param warehouseId 倉庫 ID
     * @param quantity    出貨數量
     * @param referenceNo 參考單號
     * @param operatorId  操作人員 ID
     * @throws BusinessException 如果庫存或預留數量不足
     */
    @Transactional
    public void confirmShipment(
            Long productId,
            Long warehouseId,
            Integer quantity,
            String referenceNo,
            Long operatorId) {

        log.info("確認出貨: productId={}, warehouseId={}, quantity={}",
                productId, warehouseId, quantity);

        Inventory inventory = inventoryRepository
                .findByProductIdAndWarehouseIdForUpdate(productId, warehouseId)
                .orElseThrow(() -> BusinessException.notFound("庫存記錄",
                        String.format("productId=%d, warehouseId=%d", productId, warehouseId)));

        int beforeQuantity = inventory.getQuantity();

        inventory.confirmShipment(quantity);
        inventoryRepository.save(inventory);

        // 建立銷售出庫異動記錄
        InventoryMovement movement = InventoryMovement.create(
                productId, warehouseId, MovementType.SALES_OUT, quantity,
                beforeQuantity, referenceNo, "訂單出貨", operatorId
        );
        movementRepository.save(movement);

        log.info("出貨確認完成: productId={}, warehouseId={}, quantity={}",
                productId, warehouseId, quantity);
    }

    // ==================== 異動記錄查詢 ====================

    /**
     * 查詢商品的庫存異動記錄
     *
     * @param productId 商品 ID
     * @param pageable  分頁參數
     * @return 異動記錄分頁
     */
    @Transactional(readOnly = true)
    public Page<InventoryMovementDto> getMovementsByProduct(Long productId, Pageable pageable) {
        log.debug("查詢商品異動記錄: productId={}", productId);

        return movementRepository.findByProductIdOrderByCreatedAtDesc(productId, pageable)
                .map(InventoryMovementDto::fromEntity);
    }

    /**
     * 查詢倉庫的庫存異動記錄
     *
     * @param warehouseId 倉庫 ID
     * @param pageable    分頁參數
     * @return 異動記錄分頁
     */
    @Transactional(readOnly = true)
    public Page<InventoryMovementDto> getMovementsByWarehouse(Long warehouseId, Pageable pageable) {
        log.debug("查詢倉庫異動記錄: warehouseId={}", warehouseId);

        return movementRepository.findByWarehouseIdOrderByCreatedAtDesc(warehouseId, pageable)
                .map(InventoryMovementDto::fromEntity);
    }

    /**
     * 查詢商品在特定倉庫的異動記錄
     *
     * @param productId   商品 ID
     * @param warehouseId 倉庫 ID
     * @param pageable    分頁參數
     * @return 異動記錄分頁
     */
    @Transactional(readOnly = true)
    public Page<InventoryMovementDto> getMovementsByProductAndWarehouse(
            Long productId, Long warehouseId, Pageable pageable) {

        log.debug("查詢商品倉庫異動記錄: productId={}, warehouseId={}", productId, warehouseId);

        return movementRepository
                .findByProductIdAndWarehouseIdOrderByCreatedAtDesc(productId, warehouseId, pageable)
                .map(InventoryMovementDto::fromEntity);
    }

    /**
     * 根據參考單號查詢異動記錄
     *
     * @param referenceNo 參考單號
     * @return 異動記錄列表
     */
    @Transactional(readOnly = true)
    public List<InventoryMovementDto> getMovementsByReferenceNo(String referenceNo) {
        log.debug("根據參考單號查詢異動記錄: referenceNo={}", referenceNo);

        return movementRepository.findByReferenceNoOrderByCreatedAtDesc(referenceNo).stream()
                .map(InventoryMovementDto::fromEntity)
                .collect(Collectors.toList());
    }

    /**
     * 複合條件查詢異動記錄
     *
     * @param productId    商品 ID（可選）
     * @param warehouseId  倉庫 ID（可選）
     * @param movementType 異動類型（可選）
     * @param startDate    開始日期
     * @param endDate      結束日期
     * @param pageable     分頁參數
     * @return 異動記錄分頁
     */
    @Transactional(readOnly = true)
    public Page<InventoryMovementDto> searchMovements(
            Long productId,
            Long warehouseId,
            MovementType movementType,
            LocalDate startDate,
            LocalDate endDate,
            Pageable pageable) {

        log.debug("複合條件查詢異動記錄: productId={}, warehouseId={}, type={}, startDate={}, endDate={}",
                productId, warehouseId, movementType, startDate, endDate);

        // 將日期轉換為當天的開始和結束時間
        LocalDateTime startDateTime = startDate.atStartOfDay();
        LocalDateTime endDateTime = endDate.atTime(23, 59, 59);

        return movementRepository.searchMovements(
                        productId, warehouseId, movementType, startDateTime, endDateTime, pageable)
                .map(InventoryMovementDto::fromEntity);
    }

    // ==================== 私有方法 ====================

    /**
     * 取得或建立庫存記錄（使用悲觀鎖定）
     *
     * <p>如果庫存記錄不存在，則建立一筆新的記錄。</p>
     *
     * @param productId   商品 ID
     * @param warehouseId 倉庫 ID
     * @return 庫存實體
     */
    private Inventory getOrCreateInventoryForUpdate(Long productId, Long warehouseId) {
        return inventoryRepository
                .findByProductIdAndWarehouseIdForUpdate(productId, warehouseId)
                .orElseGet(() -> {
                    log.info("建立新庫存記錄: productId={}, warehouseId={}", productId, warehouseId);
                    return inventoryRepository.save(
                            Inventory.builder()
                                    .productId(productId)
                                    .warehouseId(warehouseId)
                                    .quantity(0)
                                    .reservedQuantity(0)
                                    .lastMovementDate(LocalDateTime.now())
                                    .build()
                    );
                });
    }
}

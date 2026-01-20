package com.kamesan.erpapi.inventory.repository;

import com.kamesan.erpapi.inventory.entity.InventoryMovement;
import com.kamesan.erpapi.inventory.entity.MovementType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 庫存異動記錄 Repository
 *
 * <p>提供庫存異動記錄的存取操作，包括：</p>
 * <ul>
 *   <li>根據商品查詢異動記錄</li>
 *   <li>根據倉庫查詢異動記錄</li>
 *   <li>根據異動類型查詢</li>
 *   <li>根據參考單號查詢</li>
 *   <li>時間範圍查詢</li>
 *   <li>統計分析查詢</li>
 * </ul>
 *
 * <p>注意：庫存異動記錄只能新增和查詢，不應該修改或刪除。</p>
 *
 * @author ERP System Team
 * @version 1.0.0
 */
@Repository
public interface InventoryMovementRepository extends JpaRepository<InventoryMovement, Long> {

    /**
     * 根據商品 ID 查詢異動記錄
     *
     * <p>按建立時間降序排列，最新的異動記錄在前</p>
     *
     * @param productId 商品 ID
     * @param pageable  分頁參數
     * @return 異動記錄分頁
     */
    Page<InventoryMovement> findByProductIdOrderByCreatedAtDesc(Long productId, Pageable pageable);

    /**
     * 根據倉庫 ID 查詢異動記錄
     *
     * @param warehouseId 倉庫 ID
     * @param pageable    分頁參數
     * @return 異動記錄分頁
     */
    Page<InventoryMovement> findByWarehouseIdOrderByCreatedAtDesc(Long warehouseId, Pageable pageable);

    /**
     * 根據商品 ID 和倉庫 ID 查詢異動記錄
     *
     * @param productId   商品 ID
     * @param warehouseId 倉庫 ID
     * @param pageable    分頁參數
     * @return 異動記錄分頁
     */
    Page<InventoryMovement> findByProductIdAndWarehouseIdOrderByCreatedAtDesc(
            Long productId,
            Long warehouseId,
            Pageable pageable);

    /**
     * 根據異動類型查詢記錄
     *
     * @param movementType 異動類型
     * @param pageable     分頁參數
     * @return 異動記錄分頁
     */
    Page<InventoryMovement> findByMovementTypeOrderByCreatedAtDesc(MovementType movementType, Pageable pageable);

    /**
     * 根據參考單號查詢異動記錄
     *
     * <p>用於追蹤特定業務單據相關的所有庫存異動</p>
     *
     * @param referenceNo 參考單號
     * @return 異動記錄列表
     */
    List<InventoryMovement> findByReferenceNoOrderByCreatedAtDesc(String referenceNo);

    /**
     * 根據時間範圍查詢異動記錄
     *
     * @param startTime 開始時間
     * @param endTime   結束時間
     * @param pageable  分頁參數
     * @return 異動記錄分頁
     */
    @Query("SELECT m FROM InventoryMovement m WHERE m.createdAt BETWEEN :startTime AND :endTime ORDER BY m.createdAt DESC")
    Page<InventoryMovement> findByDateRange(
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime,
            Pageable pageable);

    /**
     * 根據商品和時間範圍查詢異動記錄
     *
     * @param productId 商品 ID
     * @param startTime 開始時間
     * @param endTime   結束時間
     * @param pageable  分頁參數
     * @return 異動記錄分頁
     */
    @Query("SELECT m FROM InventoryMovement m " +
            "WHERE m.productId = :productId " +
            "AND m.createdAt BETWEEN :startTime AND :endTime " +
            "ORDER BY m.createdAt DESC")
    Page<InventoryMovement> findByProductIdAndDateRange(
            @Param("productId") Long productId,
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime,
            Pageable pageable);

    /**
     * 根據倉庫和時間範圍查詢異動記錄
     *
     * @param warehouseId 倉庫 ID
     * @param startTime   開始時間
     * @param endTime     結束時間
     * @param pageable    分頁參數
     * @return 異動記錄分頁
     */
    @Query("SELECT m FROM InventoryMovement m " +
            "WHERE m.warehouseId = :warehouseId " +
            "AND m.createdAt BETWEEN :startTime AND :endTime " +
            "ORDER BY m.createdAt DESC")
    Page<InventoryMovement> findByWarehouseIdAndDateRange(
            @Param("warehouseId") Long warehouseId,
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime,
            Pageable pageable);

    /**
     * 統計特定商品在時間範圍內的入庫總數
     *
     * @param productId 商品 ID
     * @param startTime 開始時間
     * @param endTime   結束時間
     * @return 入庫總數量
     */
    @Query("SELECT COALESCE(SUM(m.quantity), 0) FROM InventoryMovement m " +
            "WHERE m.productId = :productId " +
            "AND m.movementType IN ('PURCHASE_IN', 'RETURN_IN', 'TRANSFER_IN', 'ADJUST_IN') " +
            "AND m.createdAt BETWEEN :startTime AND :endTime")
    Integer sumInboundQuantity(
            @Param("productId") Long productId,
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime);

    /**
     * 統計特定商品在時間範圍內的出庫總數
     *
     * @param productId 商品 ID
     * @param startTime 開始時間
     * @param endTime   結束時間
     * @return 出庫總數量
     */
    @Query("SELECT COALESCE(SUM(m.quantity), 0) FROM InventoryMovement m " +
            "WHERE m.productId = :productId " +
            "AND m.movementType IN ('SALES_OUT', 'RETURN_OUT', 'TRANSFER_OUT', 'ADJUST_OUT') " +
            "AND m.createdAt BETWEEN :startTime AND :endTime")
    Integer sumOutboundQuantity(
            @Param("productId") Long productId,
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime);

    /**
     * 統計特定異動類型在時間範圍內的總數量
     *
     * @param movementType 異動類型
     * @param startTime    開始時間
     * @param endTime      結束時間
     * @return 總數量
     */
    @Query("SELECT COALESCE(SUM(m.quantity), 0) FROM InventoryMovement m " +
            "WHERE m.movementType = :movementType " +
            "AND m.createdAt BETWEEN :startTime AND :endTime")
    Integer sumQuantityByMovementType(
            @Param("movementType") MovementType movementType,
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime);

    /**
     * 查詢商品最近的異動記錄
     *
     * @param productId   商品 ID
     * @param warehouseId 倉庫 ID
     * @param limit       限制筆數
     * @return 最近的異動記錄列表
     */
    @Query("SELECT m FROM InventoryMovement m " +
            "WHERE m.productId = :productId AND m.warehouseId = :warehouseId " +
            "ORDER BY m.createdAt DESC")
    List<InventoryMovement> findRecentMovements(
            @Param("productId") Long productId,
            @Param("warehouseId") Long warehouseId,
            Pageable pageable);

    /**
     * 統計時間範圍內的異動次數
     *
     * @param warehouseId 倉庫 ID
     * @param startTime   開始時間
     * @param endTime     結束時間
     * @return 異動次數
     */
    @Query("SELECT COUNT(m) FROM InventoryMovement m " +
            "WHERE m.warehouseId = :warehouseId " +
            "AND m.createdAt BETWEEN :startTime AND :endTime")
    Long countMovementsByWarehouseAndDateRange(
            @Param("warehouseId") Long warehouseId,
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime);

    /**
     * 根據操作人員查詢異動記錄
     *
     * @param operatorId 操作人員 ID
     * @param pageable   分頁參數
     * @return 異動記錄分頁
     */
    Page<InventoryMovement> findByOperatorIdOrderByCreatedAtDesc(Long operatorId, Pageable pageable);

    /**
     * 複合條件查詢異動記錄
     *
     * @param productId    商品 ID（可選）
     * @param warehouseId  倉庫 ID（可選）
     * @param movementType 異動類型（可選）
     * @param startTime    開始時間
     * @param endTime      結束時間
     * @param pageable     分頁參數
     * @return 異動記錄分頁
     */
    @Query("SELECT m FROM InventoryMovement m " +
            "WHERE (:productId IS NULL OR m.productId = :productId) " +
            "AND (:warehouseId IS NULL OR m.warehouseId = :warehouseId) " +
            "AND (:movementType IS NULL OR m.movementType = :movementType) " +
            "AND m.createdAt BETWEEN :startTime AND :endTime " +
            "ORDER BY m.createdAt DESC")
    Page<InventoryMovement> searchMovements(
            @Param("productId") Long productId,
            @Param("warehouseId") Long warehouseId,
            @Param("movementType") MovementType movementType,
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime,
            Pageable pageable);
}

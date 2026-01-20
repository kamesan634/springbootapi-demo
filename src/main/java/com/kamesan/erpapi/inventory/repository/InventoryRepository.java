package com.kamesan.erpapi.inventory.repository;

import com.kamesan.erpapi.inventory.entity.Inventory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import jakarta.persistence.LockModeType;
import java.util.List;
import java.util.Optional;

/**
 * 庫存 Repository
 *
 * <p>提供庫存資料的存取操作，包括：</p>
 * <ul>
 *   <li>根據商品和倉庫查詢庫存</li>
 *   <li>查詢商品在所有倉庫的庫存</li>
 *   <li>查詢倉庫的所有庫存</li>
 *   <li>低庫存預警查詢</li>
 *   <li>悲觀鎖定查詢（用於庫存異動）</li>
 * </ul>
 *
 * @author ERP System Team
 * @version 1.0.0
 */
@Repository
public interface InventoryRepository extends JpaRepository<Inventory, Long> {

    /**
     * 根據商品 ID 和倉庫 ID 查詢庫存
     *
     * <p>用於查詢特定商品在特定倉庫的庫存資訊</p>
     *
     * @param productId   商品 ID
     * @param warehouseId 倉庫 ID
     * @return 庫存資訊（Optional）
     */
    Optional<Inventory> findByProductIdAndWarehouseId(Long productId, Long warehouseId);

    /**
     * 根據商品 ID 和倉庫 ID 查詢庫存（悲觀鎖定）
     *
     * <p>用於庫存異動操作，使用悲觀鎖定防止並發問題</p>
     *
     * @param productId   商品 ID
     * @param warehouseId 倉庫 ID
     * @return 庫存資訊（Optional）
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT i FROM Inventory i WHERE i.productId = :productId AND i.warehouseId = :warehouseId")
    Optional<Inventory> findByProductIdAndWarehouseIdForUpdate(
            @Param("productId") Long productId,
            @Param("warehouseId") Long warehouseId);

    /**
     * 根據商品 ID 查詢所有倉庫的庫存
     *
     * <p>用於查看商品在各倉庫的庫存分布</p>
     *
     * @param productId 商品 ID
     * @return 庫存列表
     */
    List<Inventory> findByProductId(Long productId);

    /**
     * 根據倉庫 ID 查詢所有商品的庫存
     *
     * <p>用於查看特定倉庫的所有庫存</p>
     *
     * @param warehouseId 倉庫 ID
     * @return 庫存列表
     */
    List<Inventory> findByWarehouseId(Long warehouseId);

    /**
     * 根據倉庫 ID 分頁查詢庫存
     *
     * @param warehouseId 倉庫 ID
     * @param pageable    分頁參數
     * @return 庫存分頁
     */
    Page<Inventory> findByWarehouseId(Long warehouseId, Pageable pageable);

    /**
     * 查詢商品在所有倉庫的總庫存數量
     *
     * @param productId 商品 ID
     * @return 總庫存數量，如果沒有記錄則返回 0
     */
    @Query("SELECT COALESCE(SUM(i.quantity), 0) FROM Inventory i WHERE i.productId = :productId")
    Integer sumQuantityByProductId(@Param("productId") Long productId);

    /**
     * 查詢商品在所有倉庫的可用庫存總數
     *
     * <p>可用庫存 = 庫存數量 - 保留數量</p>
     *
     * @param productId 商品 ID
     * @return 可用庫存總數
     */
    @Query("SELECT COALESCE(SUM(i.quantity - i.reservedQuantity), 0) FROM Inventory i WHERE i.productId = :productId")
    Integer sumAvailableQuantityByProductId(@Param("productId") Long productId);

    /**
     * 查詢低於安全庫存的記錄
     *
     * <p>用於庫存預警功能，查詢庫存數量低於指定閾值的記錄</p>
     *
     * @param threshold 安全庫存閾值
     * @param pageable  分頁參數
     * @return 低庫存記錄分頁
     */
    @Query("SELECT i FROM Inventory i WHERE i.quantity <= :threshold")
    Page<Inventory> findLowStockInventories(@Param("threshold") Integer threshold, Pageable pageable);

    /**
     * 查詢特定倉庫低於安全庫存的記錄
     *
     * @param warehouseId 倉庫 ID
     * @param threshold   安全庫存閾值
     * @param pageable    分頁參數
     * @return 低庫存記錄分頁
     */
    @Query("SELECT i FROM Inventory i WHERE i.warehouseId = :warehouseId AND i.quantity <= :threshold")
    Page<Inventory> findLowStockByWarehouse(
            @Param("warehouseId") Long warehouseId,
            @Param("threshold") Integer threshold,
            Pageable pageable);

    /**
     * 查詢有保留數量的庫存記錄
     *
     * <p>用於查看哪些庫存有被訂單預留</p>
     *
     * @param pageable 分頁參數
     * @return 有保留的庫存記錄分頁
     */
    @Query("SELECT i FROM Inventory i WHERE i.reservedQuantity > 0")
    Page<Inventory> findReservedInventories(Pageable pageable);

    /**
     * 檢查商品在特定倉庫是否有庫存記錄
     *
     * @param productId   商品 ID
     * @param warehouseId 倉庫 ID
     * @return 如果存在返回 true
     */
    boolean existsByProductIdAndWarehouseId(Long productId, Long warehouseId);

    /**
     * 查詢多個商品的庫存
     *
     * <p>用於批次查詢，如購物車商品庫存檢查</p>
     *
     * @param productIds  商品 ID 列表
     * @param warehouseId 倉庫 ID
     * @return 庫存列表
     */
    @Query("SELECT i FROM Inventory i WHERE i.productId IN :productIds AND i.warehouseId = :warehouseId")
    List<Inventory> findByProductIdsAndWarehouseId(
            @Param("productIds") List<Long> productIds,
            @Param("warehouseId") Long warehouseId);

    /**
     * 統計倉庫的商品種類數量
     *
     * @param warehouseId 倉庫 ID
     * @return 商品種類數量
     */
    @Query("SELECT COUNT(i) FROM Inventory i WHERE i.warehouseId = :warehouseId AND i.quantity > 0")
    Long countProductsInStock(@Param("warehouseId") Long warehouseId);

    /**
     * 查詢零庫存的記錄
     *
     * <p>用於清理或盤點</p>
     *
     * @param warehouseId 倉庫 ID
     * @return 零庫存記錄列表
     */
    @Query("SELECT i FROM Inventory i WHERE i.warehouseId = :warehouseId AND i.quantity = 0")
    List<Inventory> findZeroStockByWarehouse(@Param("warehouseId") Long warehouseId);

    /**
     * 統計低庫存商品數量（庫存 > 0 且 <= 安全庫存）
     *
     * @param threshold 安全庫存閾值
     * @return 低庫存商品數量
     */
    @Query("SELECT COUNT(DISTINCT i.productId) FROM Inventory i WHERE i.quantity > 0 AND i.quantity <= :threshold")
    Long countLowStockProducts(@Param("threshold") Integer threshold);

    /**
     * 統計缺貨商品數量（庫存 = 0）
     *
     * @return 缺貨商品數量
     */
    @Query("SELECT COUNT(DISTINCT i.productId) FROM Inventory i WHERE i.quantity = 0")
    Long countOutOfStockProducts();

    /**
     * 統計缺貨商品數量（庫存 = 0，指定倉庫）
     *
     * @param warehouseId 倉庫 ID
     * @return 缺貨商品數量
     */
    @Query("SELECT COUNT(i) FROM Inventory i WHERE i.warehouseId = :warehouseId AND i.quantity = 0")
    Long countOutOfStockByWarehouse(@Param("warehouseId") Long warehouseId);

    /**
     * 查詢缺貨商品列表
     *
     * @param pageable 分頁參數
     * @return 缺貨庫存記錄
     */
    @Query("SELECT i FROM Inventory i WHERE i.quantity = 0")
    Page<Inventory> findOutOfStockInventories(Pageable pageable);

    /**
     * 計算倉庫庫存總價值
     *
     * @param warehouseId 倉庫 ID
     * @return 庫存總價值
     */
    @Query("SELECT COALESCE(SUM(i.quantity * p.costPrice), 0) FROM Inventory i " +
           "JOIN Product p ON i.productId = p.id WHERE i.warehouseId = :warehouseId")
    java.math.BigDecimal sumInventoryValueByWarehouse(@Param("warehouseId") Long warehouseId);

    /**
     * 計算全部庫存總價值
     *
     * @return 全部庫存總價值
     */
    @Query("SELECT COALESCE(SUM(i.quantity * p.costPrice), 0) FROM Inventory i " +
           "JOIN Product p ON i.productId = p.id")
    java.math.BigDecimal sumTotalInventoryValue();
}

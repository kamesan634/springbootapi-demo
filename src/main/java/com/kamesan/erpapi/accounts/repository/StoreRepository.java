package com.kamesan.erpapi.accounts.repository;

import com.kamesan.erpapi.accounts.entity.Store;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 門市/倉庫 Repository
 *
 * <p>提供門市/倉庫資料的存取操作。</p>
 *
 * @author ERP System Team
 * @version 1.0.0
 */
@Repository
public interface StoreRepository extends JpaRepository<Store, Long> {

    /**
     * 根據代碼查詢
     *
     * @param code 門市/倉庫代碼
     * @return 門市/倉庫（Optional）
     */
    Optional<Store> findByCode(String code);

    /**
     * 檢查代碼是否存在
     *
     * @param code 門市/倉庫代碼
     * @return 是否存在
     */
    boolean existsByCode(String code);

    /**
     * 根據類型查詢
     *
     * @param type 類型
     * @return 門市/倉庫列表
     */
    List<Store> findByTypeAndActiveTrueOrderBySortOrder(Store.StoreType type);

    /**
     * 查詢所有門市
     *
     * @return 門市列表
     */
    default List<Store> findAllStores() {
        return findByTypeAndActiveTrueOrderBySortOrder(Store.StoreType.STORE);
    }

    /**
     * 查詢所有倉庫
     *
     * @return 倉庫列表
     */
    default List<Store> findAllWarehouses() {
        return findByTypeAndActiveTrueOrderBySortOrder(Store.StoreType.WAREHOUSE);
    }

    /**
     * 查詢所有啟用的門市/倉庫
     *
     * @return 門市/倉庫列表
     */
    List<Store> findByActiveTrueOrderBySortOrder();

    /**
     * 查詢主倉庫/總部
     *
     * @return 主倉庫/總部（Optional）
     */
    Optional<Store> findByMainTrue();
}

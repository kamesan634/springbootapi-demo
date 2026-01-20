package com.kamesan.erpapi.system.repository;

import com.kamesan.erpapi.system.entity.SystemParameter;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 系統參數 Repository
 */
@Repository
public interface SystemParameterRepository extends JpaRepository<SystemParameter, Long> {

    /**
     * 根據類別查詢參數
     */
    List<SystemParameter> findByCategoryOrderBySortOrderAsc(String category);

    /**
     * 根據類別和鍵查詢參數
     */
    Optional<SystemParameter> findByCategoryAndParamKey(String category, String paramKey);

    /**
     * 檢查參數是否存在
     */
    boolean existsByCategoryAndParamKey(String category, String paramKey);

    /**
     * 查詢所有類別
     */
    List<String> findDistinctCategoryBy();

    /**
     * 根據類別刪除非系統參數
     */
    void deleteByCategoryAndIsSystemFalse(String category);
}

package com.kamesan.erpapi.products.repository;

import com.kamesan.erpapi.products.entity.LabelTemplate;
import com.kamesan.erpapi.products.entity.LabelTemplate.LabelType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 標籤範本 Repository
 *
 * @author ERP System Team
 * @version 1.0.0
 */
@Repository
public interface LabelTemplateRepository extends JpaRepository<LabelTemplate, Long> {

    /**
     * 根據類型查詢
     */
    List<LabelTemplate> findByLabelType(LabelType labelType);

    /**
     * 查詢預設範本
     */
    Optional<LabelTemplate> findByLabelTypeAndIsDefaultTrue(LabelType labelType);

    /**
     * 查詢所有預設範本
     */
    List<LabelTemplate> findByIsDefaultTrue();

    /**
     * 根據名稱查詢
     */
    Optional<LabelTemplate> findByName(String name);

    /**
     * 檢查名稱是否存在
     */
    boolean existsByName(String name);

    /**
     * 取消其他預設範本
     */
    @Query("UPDATE LabelTemplate t SET t.isDefault = false WHERE t.labelType = :labelType AND t.id != :excludeId")
    void clearDefaultByType(@Param("labelType") LabelType labelType, @Param("excludeId") Long excludeId);
}

package com.kamesan.erpapi.system.repository;

import com.kamesan.erpapi.system.entity.NumberRule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import jakarta.persistence.LockModeType;
import java.util.List;
import java.util.Optional;

/**
 * 編號規則 Repository
 */
@Repository
public interface NumberRuleRepository extends JpaRepository<NumberRule, Long> {

    /**
     * 根據規則代碼查詢（帶悲觀鎖，用於產生編號）
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT n FROM NumberRule n WHERE n.ruleCode = :ruleCode AND n.isActive = true")
    Optional<NumberRule> findByRuleCodeForUpdate(@Param("ruleCode") String ruleCode);

    /**
     * 根據規則代碼查詢
     */
    Optional<NumberRule> findByRuleCode(String ruleCode);

    /**
     * 檢查規則代碼是否存在
     */
    boolean existsByRuleCode(String ruleCode);

    /**
     * 查詢所有啟用的規則
     */
    List<NumberRule> findByIsActiveTrueOrderByRuleCodeAsc();
}

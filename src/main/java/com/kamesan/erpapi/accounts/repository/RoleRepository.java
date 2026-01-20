package com.kamesan.erpapi.accounts.repository;

import com.kamesan.erpapi.accounts.entity.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 角色 Repository
 *
 * <p>提供角色資料的存取操作。</p>
 *
 * @author ERP System Team
 * @version 1.0.0
 */
@Repository
public interface RoleRepository extends JpaRepository<Role, Long> {

    /**
     * 根據角色代碼查詢
     *
     * @param code 角色代碼
     * @return 角色（Optional）
     */
    Optional<Role> findByCode(String code);

    /**
     * 檢查角色代碼是否存在
     *
     * @param code 角色代碼
     * @return 是否存在
     */
    boolean existsByCode(String code);

    /**
     * 查詢所有啟用的角色
     *
     * @return 角色列表
     */
    List<Role> findByActiveTrueOrderBySortOrder();

    /**
     * 查詢非系統角色
     *
     * @return 角色列表
     */
    List<Role> findBySystemFalseOrderBySortOrder();
}

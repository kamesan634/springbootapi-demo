package com.kamesan.erpapi.accounts.repository;

import com.kamesan.erpapi.accounts.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * 使用者 Repository
 *
 * <p>提供使用者資料的存取操作。</p>
 *
 * @author ERP System Team
 * @version 1.0.0
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    /**
     * 根據使用者名稱查詢
     *
     * @param username 使用者名稱
     * @return 使用者（Optional）
     */
    Optional<User> findByUsername(String username);

    /**
     * 根據 Email 查詢
     *
     * @param email Email
     * @return 使用者（Optional）
     */
    Optional<User> findByEmail(String email);

    /**
     * 檢查使用者名稱是否存在
     *
     * @param username 使用者名稱
     * @return 是否存在
     */
    boolean existsByUsername(String username);

    /**
     * 檢查 Email 是否存在
     *
     * @param email Email
     * @return 是否存在
     */
    boolean existsByEmail(String email);

    /**
     * 根據角色查詢使用者
     *
     * @param roleId   角色 ID
     * @param pageable 分頁參數
     * @return 使用者分頁
     */
    @Query("SELECT u FROM User u WHERE u.role.id = :roleId")
    Page<User> findByRoleId(@Param("roleId") Long roleId, Pageable pageable);

    /**
     * 根據門市查詢使用者
     *
     * @param storeId  門市 ID
     * @param pageable 分頁參數
     * @return 使用者分頁
     */
    @Query("SELECT u FROM User u JOIN u.stores s WHERE s.id = :storeId")
    Page<User> findByStoreId(@Param("storeId") Long storeId, Pageable pageable);

    /**
     * 搜尋使用者（根據名稱、使用者名稱或 Email）
     *
     * @param keyword  關鍵字
     * @param pageable 分頁參數
     * @return 使用者分頁
     */
    @Query("SELECT u FROM User u WHERE " +
            "u.name LIKE %:keyword% OR " +
            "u.username LIKE %:keyword% OR " +
            "u.email LIKE %:keyword%")
    Page<User> search(@Param("keyword") String keyword, Pageable pageable);

    /**
     * 查詢啟用的使用者
     *
     * @param pageable 分頁參數
     * @return 使用者分頁
     */
    Page<User> findByActiveTrue(Pageable pageable);
}

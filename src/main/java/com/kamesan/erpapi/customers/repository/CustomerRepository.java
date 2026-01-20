package com.kamesan.erpapi.customers.repository;

import com.kamesan.erpapi.customers.entity.Customer;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * 會員 Repository
 *
 * <p>提供會員資料的存取操作，包括：</p>
 * <ul>
 *   <li>基本的 CRUD 操作（繼承自 JpaRepository）</li>
 *   <li>根據會員編號、手機、Email 查詢</li>
 *   <li>多條件搜尋</li>
 *   <li>生日相關查詢</li>
 *   <li>統計相關查詢</li>
 * </ul>
 *
 * @author ERP System Team
 * @version 1.0.0
 */
@Repository
public interface CustomerRepository extends JpaRepository<Customer, Long> {

    /**
     * 根據會員編號查詢
     *
     * @param memberNo 會員編號
     * @return 會員（Optional）
     */
    Optional<Customer> findByMemberNo(String memberNo);

    /**
     * 根據手機號碼查詢
     *
     * @param phone 手機號碼
     * @return 會員（Optional）
     */
    Optional<Customer> findByPhone(String phone);

    /**
     * 根據 Email 查詢
     *
     * @param email Email
     * @return 會員（Optional）
     */
    Optional<Customer> findByEmail(String email);

    /**
     * 檢查會員編號是否存在
     *
     * @param memberNo 會員編號
     * @return 是否存在
     */
    boolean existsByMemberNo(String memberNo);

    /**
     * 檢查手機號碼是否存在
     *
     * @param phone 手機號碼
     * @return 是否存在
     */
    boolean existsByPhone(String phone);

    /**
     * 檢查手機號碼是否存在（排除指定 ID）
     * <p>用於更新時檢查手機是否重複</p>
     *
     * @param phone 手機號碼
     * @param id    要排除的 ID
     * @return 是否存在
     */
    boolean existsByPhoneAndIdNot(String phone, Long id);

    /**
     * 檢查 Email 是否存在
     *
     * @param email Email
     * @return 是否存在
     */
    boolean existsByEmail(String email);

    /**
     * 檢查 Email 是否存在（排除指定 ID）
     * <p>用於更新時檢查 Email 是否重複</p>
     *
     * @param email Email
     * @param id    要排除的 ID
     * @return 是否存在
     */
    boolean existsByEmailAndIdNot(String email, Long id);

    /**
     * 查詢啟用的會員
     *
     * @param pageable 分頁參數
     * @return 會員分頁
     */
    Page<Customer> findByActiveTrue(Pageable pageable);

    /**
     * 根據等級查詢會員
     *
     * @param levelId  等級 ID
     * @param pageable 分頁參數
     * @return 會員分頁
     */
    @Query("SELECT c FROM Customer c WHERE c.level.id = :levelId")
    Page<Customer> findByLevelId(@Param("levelId") Long levelId, Pageable pageable);

    /**
     * 搜尋會員（根據姓名、手機、Email 或會員編號）
     *
     * @param keyword  關鍵字
     * @param pageable 分頁參數
     * @return 會員分頁
     */
    @Query("SELECT c FROM Customer c WHERE " +
            "c.name LIKE %:keyword% OR " +
            "c.phone LIKE %:keyword% OR " +
            "c.email LIKE %:keyword% OR " +
            "c.memberNo LIKE %:keyword%")
    Page<Customer> search(@Param("keyword") String keyword, Pageable pageable);

    /**
     * 查詢今天生日的會員
     *
     * @param month 月份
     * @param day   日期
     * @return 會員列表
     */
    @Query("SELECT c FROM Customer c WHERE " +
            "c.active = true AND " +
            "MONTH(c.birthday) = :month AND " +
            "DAY(c.birthday) = :day")
    List<Customer> findBirthdayCustomers(@Param("month") int month, @Param("day") int day);

    /**
     * 查詢本月生日的會員
     *
     * @param month 月份
     * @return 會員列表
     */
    @Query("SELECT c FROM Customer c WHERE " +
            "c.active = true AND " +
            "MONTH(c.birthday) = :month")
    List<Customer> findBirthdayCustomersInMonth(@Param("month") int month);

    /**
     * 查詢指定日期範圍內生日的會員
     *
     * @param startDate 開始日期
     * @param endDate   結束日期
     * @return 會員列表
     */
    @Query("SELECT c FROM Customer c WHERE " +
            "c.active = true AND " +
            "c.birthday IS NOT NULL AND " +
            "(MONTH(c.birthday) > MONTH(:startDate) OR " +
            "(MONTH(c.birthday) = MONTH(:startDate) AND DAY(c.birthday) >= DAY(:startDate))) AND " +
            "(MONTH(c.birthday) < MONTH(:endDate) OR " +
            "(MONTH(c.birthday) = MONTH(:endDate) AND DAY(c.birthday) <= DAY(:endDate)))")
    List<Customer> findBirthdayCustomersBetween(
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);

    /**
     * 統計各等級會員數量
     *
     * @return 等級 ID 和會員數量的對應
     */
    @Query("SELECT c.level.id, COUNT(c) FROM Customer c GROUP BY c.level.id")
    List<Object[]> countByLevel();

    /**
     * 統計啟用的會員數量
     *
     * @return 啟用的會員數量
     */
    long countByActiveTrue();

    /**
     * 統計今日註冊的會員數量
     *
     * @param startOfDay 今日開始時間
     * @param endOfDay   今日結束時間
     * @return 註冊數量
     */
    @Query("SELECT COUNT(c) FROM Customer c WHERE c.registerDate BETWEEN :startOfDay AND :endOfDay")
    long countRegisteredToday(
            @Param("startOfDay") java.time.LocalDateTime startOfDay,
            @Param("endOfDay") java.time.LocalDateTime endOfDay);

    /**
     * 查詢點數大於指定值的會員
     *
     * @param points   點數門檻
     * @param pageable 分頁參數
     * @return 會員分頁
     */
    @Query("SELECT c FROM Customer c WHERE c.totalPoints >= :points AND c.active = true")
    Page<Customer> findByPointsGreaterThan(@Param("points") int points, Pageable pageable);

    /**
     * 取得今日最大的會員編號流水號
     * <p>用於產生新的會員編號</p>
     *
     * @param prefix 會員編號前綴（含日期）
     * @return 最大流水號
     */
    @Query("SELECT MAX(c.memberNo) FROM Customer c WHERE c.memberNo LIKE :prefix%")
    Optional<String> findMaxMemberNoByPrefix(@Param("prefix") String prefix);

    /**
     * 根據性別查詢會員
     *
     * @param gender   性別
     * @param pageable 分頁參數
     * @return 會員分頁
     */
    Page<Customer> findByGender(String gender, Pageable pageable);

    /**
     * 複合條件查詢會員
     *
     * @param keyword  搜尋關鍵字（可為 null）
     * @param levelId  等級 ID（可為 null）
     * @param active   啟用狀態（可為 null）
     * @param gender   性別（可為 null）
     * @param pageable 分頁參數
     * @return 會員分頁
     */
    @Query("SELECT c FROM Customer c WHERE " +
            "(:keyword IS NULL OR c.name LIKE %:keyword% OR c.phone LIKE %:keyword% OR c.email LIKE %:keyword% OR c.memberNo LIKE %:keyword%) AND " +
            "(:levelId IS NULL OR c.level.id = :levelId) AND " +
            "(:active IS NULL OR c.active = :active) AND " +
            "(:gender IS NULL OR c.gender = :gender)")
    Page<Customer> findByConditions(
            @Param("keyword") String keyword,
            @Param("levelId") Long levelId,
            @Param("active") Boolean active,
            @Param("gender") String gender,
            Pageable pageable);
}

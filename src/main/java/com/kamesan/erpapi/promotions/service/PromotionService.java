package com.kamesan.erpapi.promotions.service;

import com.kamesan.erpapi.common.exception.BusinessException;
import com.kamesan.erpapi.promotions.dto.CreatePromotionRequest;
import com.kamesan.erpapi.promotions.dto.PromotionDto;
import com.kamesan.erpapi.promotions.entity.Promotion;
import com.kamesan.erpapi.promotions.entity.PromotionType;
import com.kamesan.erpapi.promotions.repository.PromotionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 促銷活動服務
 *
 * <p>處理促銷活動相關的業務邏輯，包括：</p>
 * <ul>
 *   <li>促銷活動的 CRUD 操作</li>
 *   <li>促銷活動的查詢和搜尋</li>
 *   <li>促銷活動的狀態管理（啟用/停用）</li>
 *   <li>折扣金額的計算</li>
 * </ul>
 *
 * <p>業務規則：</p>
 * <ul>
 *   <li>促銷活動名稱不可重複</li>
 *   <li>結束日期必須在開始日期之後</li>
 *   <li>百分比折扣值不可超過 100</li>
 * </ul>
 *
 * @author ERP System Team
 * @version 1.0.0
 * @see Promotion 促銷活動實體
 * @see PromotionRepository 促銷活動 Repository
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PromotionService {

    /**
     * 促銷活動 Repository
     */
    private final PromotionRepository promotionRepository;

    /**
     * 取得所有促銷活動（分頁）
     *
     * @param pageable 分頁參數
     * @return 促銷活動分頁
     */
    @Transactional(readOnly = true)
    public Page<PromotionDto> getAllPromotions(Pageable pageable) {
        log.debug("取得所有促銷活動，分頁參數：{}", pageable);
        return promotionRepository.findAll(pageable)
                .map(PromotionDto::fromEntity);
    }

    /**
     * 根據 ID 取得促銷活動
     *
     * @param id 促銷活動 ID
     * @return 促銷活動 DTO
     * @throws BusinessException 若促銷活動不存在
     */
    @Transactional(readOnly = true)
    public PromotionDto getPromotionById(Long id) {
        log.debug("取得促銷活動，ID：{}", id);
        Promotion promotion = findPromotionById(id);
        return PromotionDto.fromEntity(promotion);
    }

    /**
     * 建立促銷活動
     *
     * @param request 建立促銷活動請求
     * @return 建立的促銷活動 DTO
     * @throws BusinessException 若促銷活動名稱已存在或資料驗證失敗
     */
    @Transactional
    public PromotionDto createPromotion(CreatePromotionRequest request) {
        log.info("建立促銷活動：{}", request.getName());

        // 驗證資料
        validatePromotionRequest(request, null);

        // 檢查名稱是否已存在
        if (promotionRepository.existsByName(request.getName())) {
            throw BusinessException.alreadyExists("促銷活動", "名稱", request.getName());
        }

        // 建立促銷活動
        Promotion promotion = request.toEntity();
        promotion = promotionRepository.save(promotion);

        log.info("促銷活動建立成功，ID：{}，名稱：{}", promotion.getId(), promotion.getName());
        return PromotionDto.fromEntity(promotion);
    }

    /**
     * 更新促銷活動
     *
     * @param id      促銷活動 ID
     * @param request 更新促銷活動請求
     * @return 更新後的促銷活動 DTO
     * @throws BusinessException 若促銷活動不存在或資料驗證失敗
     */
    @Transactional
    public PromotionDto updatePromotion(Long id, CreatePromotionRequest request) {
        log.info("更新促銷活動，ID：{}", id);

        // 取得現有促銷活動
        Promotion promotion = findPromotionById(id);

        // 驗證資料
        validatePromotionRequest(request, id);

        // 檢查名稱是否與其他促銷活動重複
        if (!promotion.getName().equals(request.getName()) &&
                promotionRepository.existsByName(request.getName())) {
            throw BusinessException.alreadyExists("促銷活動", "名稱", request.getName());
        }

        // 更新促銷活動
        request.updateEntity(promotion);
        promotion = promotionRepository.save(promotion);

        log.info("促銷活動更新成功，ID：{}，名稱：{}", promotion.getId(), promotion.getName());
        return PromotionDto.fromEntity(promotion);
    }

    /**
     * 刪除促銷活動
     *
     * @param id 促銷活動 ID
     * @throws BusinessException 若促銷活動不存在
     */
    @Transactional
    public void deletePromotion(Long id) {
        log.info("刪除促銷活動，ID：{}", id);

        // 確認促銷活動存在
        Promotion promotion = findPromotionById(id);

        promotionRepository.delete(promotion);

        log.info("促銷活動刪除成功，ID：{}，名稱：{}", id, promotion.getName());
    }

    /**
     * 啟用促銷活動
     *
     * @param id 促銷活動 ID
     * @return 更新後的促銷活動 DTO
     * @throws BusinessException 若促銷活動不存在
     */
    @Transactional
    public PromotionDto activatePromotion(Long id) {
        log.info("啟用促銷活動，ID：{}", id);

        Promotion promotion = findPromotionById(id);
        promotion.setActive(true);
        promotion = promotionRepository.save(promotion);

        log.info("促銷活動已啟用，ID：{}，名稱：{}", id, promotion.getName());
        return PromotionDto.fromEntity(promotion);
    }

    /**
     * 停用促銷活動
     *
     * @param id 促銷活動 ID
     * @return 更新後的促銷活動 DTO
     * @throws BusinessException 若促銷活動不存在
     */
    @Transactional
    public PromotionDto deactivatePromotion(Long id) {
        log.info("停用促銷活動，ID：{}", id);

        Promotion promotion = findPromotionById(id);
        promotion.setActive(false);
        promotion = promotionRepository.save(promotion);

        log.info("促銷活動已停用，ID：{}，名稱：{}", id, promotion.getName());
        return PromotionDto.fromEntity(promotion);
    }

    /**
     * 根據促銷類型查詢促銷活動
     *
     * @param type     促銷類型
     * @param pageable 分頁參數
     * @return 促銷活動分頁
     */
    @Transactional(readOnly = true)
    public Page<PromotionDto> getPromotionsByType(PromotionType type, Pageable pageable) {
        log.debug("根據促銷類型查詢促銷活動，類型：{}", type);
        return promotionRepository.findByType(type, pageable)
                .map(PromotionDto::fromEntity);
    }

    /**
     * 取得正在進行中的促銷活動
     *
     * @param pageable 分頁參數
     * @return 促銷活動分頁
     */
    @Transactional(readOnly = true)
    public Page<PromotionDto> getOngoingPromotions(Pageable pageable) {
        log.debug("取得正在進行中的促銷活動");
        return promotionRepository.findOngoingPromotions(LocalDateTime.now(), pageable)
                .map(PromotionDto::fromEntity);
    }

    /**
     * 取得正在進行中的促銷活動（列表）
     *
     * @return 促銷活動列表
     */
    @Transactional(readOnly = true)
    public List<PromotionDto> getOngoingPromotionsList() {
        log.debug("取得正在進行中的促銷活動列表");
        return promotionRepository.findOngoingPromotions(LocalDateTime.now())
                .stream()
                .map(PromotionDto::fromEntity)
                .collect(Collectors.toList());
    }

    /**
     * 取得即將開始的促銷活動
     *
     * @param pageable 分頁參數
     * @return 促銷活動分頁
     */
    @Transactional(readOnly = true)
    public Page<PromotionDto> getUpcomingPromotions(Pageable pageable) {
        log.debug("取得即將開始的促銷活動");
        return promotionRepository.findUpcomingPromotions(LocalDateTime.now(), pageable)
                .map(PromotionDto::fromEntity);
    }

    /**
     * 取得已結束的促銷活動
     *
     * @param pageable 分頁參數
     * @return 促銷活動分頁
     */
    @Transactional(readOnly = true)
    public Page<PromotionDto> getExpiredPromotions(Pageable pageable) {
        log.debug("取得已結束的促銷活動");
        return promotionRepository.findExpiredPromotions(LocalDateTime.now(), pageable)
                .map(PromotionDto::fromEntity);
    }

    /**
     * 搜尋促銷活動
     *
     * @param keyword  關鍵字（名稱或描述）
     * @param pageable 分頁參數
     * @return 促銷活動分頁
     */
    @Transactional(readOnly = true)
    public Page<PromotionDto> searchPromotions(String keyword, Pageable pageable) {
        log.debug("搜尋促銷活動，關鍵字：{}", keyword);
        return promotionRepository.search(keyword, pageable)
                .map(PromotionDto::fromEntity);
    }

    /**
     * 計算促銷折扣金額
     *
     * @param promotionId    促銷活動 ID
     * @param originalAmount 原始金額
     * @return 折扣金額
     * @throws BusinessException 若促銷活動不存在或未進行中
     */
    @Transactional(readOnly = true)
    public BigDecimal calculateDiscount(Long promotionId, BigDecimal originalAmount) {
        log.debug("計算促銷折扣金額，促銷活動 ID：{}，原始金額：{}", promotionId, originalAmount);

        Promotion promotion = findPromotionById(promotionId);

        // 檢查促銷活動是否正在進行中
        if (!promotion.isOngoing()) {
            throw BusinessException.validationFailed("促銷活動目前未進行中");
        }

        BigDecimal discount = promotion.calculateDiscount(originalAmount);
        log.debug("促銷折扣金額計算結果：{}", discount);

        return discount;
    }

    /**
     * 統計正在進行中的促銷活動數量
     *
     * @return 正在進行中的促銷活動數量
     */
    @Transactional(readOnly = true)
    public long countOngoingPromotions() {
        return promotionRepository.countOngoingPromotions(LocalDateTime.now());
    }

    /**
     * 根據 ID 查詢促銷活動實體
     *
     * @param id 促銷活動 ID
     * @return 促銷活動實體
     * @throws BusinessException 若促銷活動不存在
     */
    private Promotion findPromotionById(Long id) {
        return promotionRepository.findById(id)
                .orElseThrow(() -> BusinessException.notFound("促銷活動", id));
    }

    /**
     * 驗證促銷活動請求資料
     *
     * @param request     建立/更新促銷活動請求
     * @param excludeId   排除的促銷活動 ID（更新時使用）
     * @throws BusinessException 若驗證失敗
     */
    private void validatePromotionRequest(CreatePromotionRequest request, Long excludeId) {
        // 驗證日期範圍
        if (request.getEndDate().isBefore(request.getStartDate()) ||
                request.getEndDate().isEqual(request.getStartDate())) {
            throw BusinessException.validationFailed("結束日期必須在開始日期之後");
        }

        // 驗證百分比折扣值不超過 100
        if (request.getDiscountType() == com.kamesan.erpapi.promotions.entity.DiscountType.PERCENTAGE &&
                request.getDiscountValue().compareTo(new BigDecimal("100")) > 0) {
            throw BusinessException.validationFailed("百分比折扣值不能超過 100");
        }
    }
}

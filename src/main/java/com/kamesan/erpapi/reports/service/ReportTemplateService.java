package com.kamesan.erpapi.reports.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.kamesan.erpapi.common.exception.BusinessException;
import com.kamesan.erpapi.reports.dto.ReportTemplateDto;
import com.kamesan.erpapi.reports.dto.ReportTemplateDto.*;
import com.kamesan.erpapi.reports.entity.ReportSchedule.ReportType;
import com.kamesan.erpapi.reports.entity.ReportTemplate;
import com.kamesan.erpapi.reports.repository.ReportTemplateRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 報表範本服務
 *
 * @author ERP System Team
 * @version 1.0.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ReportTemplateService {

    private final ReportTemplateRepository templateRepository;
    private final ObjectMapper objectMapper;

    /**
     * 建立範本
     */
    @Transactional
    public ReportTemplateDto createTemplate(CreateTemplateRequest request, Long ownerId) {
        log.info("建立報表範本: {}", request.getName());

        ReportTemplate template = ReportTemplate.builder()
                .name(request.getName())
                .description(request.getDescription())
                .reportType(request.getReportType())
                .columnsConfig(toJson(request.getColumns()))
                .filtersConfig(toJson(request.getFilters()))
                .groupingConfig(toJson(request.getGrouping()))
                .sortingConfig(toJson(request.getSorting()))
                .summaryConfig(toJson(request.getSummary()))
                .styleConfig(toJson(request.getStyle()))
                .headerText(request.getHeaderText())
                .footerText(request.getFooterText())
                .isPublic(request.isPublic())
                .ownerId(ownerId)
                .isSystem(false)
                .usageCount(0)
                .build();

        ReportTemplate saved = templateRepository.save(template);
        log.info("報表範本建立成功: {}", saved.getId());

        return convertToDto(saved);
    }

    /**
     * 更新範本
     */
    @Transactional
    public ReportTemplateDto updateTemplate(Long id, UpdateTemplateRequest request, Long userId) {
        log.info("更新報表範本: {}", id);

        ReportTemplate template = templateRepository.findById(id)
                .orElseThrow(() -> new BusinessException("範本不存在: " + id));

        // 檢查權限
        if (template.isSystem()) {
            throw new BusinessException("系統範本不可修改");
        }
        if (!template.getOwnerId().equals(userId)) {
            throw new BusinessException("無權限修改此範本");
        }

        if (request.getName() != null) {
            template.setName(request.getName());
        }
        if (request.getDescription() != null) {
            template.setDescription(request.getDescription());
        }
        if (request.getColumns() != null) {
            template.setColumnsConfig(toJson(request.getColumns()));
        }
        if (request.getFilters() != null) {
            template.setFiltersConfig(toJson(request.getFilters()));
        }
        if (request.getGrouping() != null) {
            template.setGroupingConfig(toJson(request.getGrouping()));
        }
        if (request.getSorting() != null) {
            template.setSortingConfig(toJson(request.getSorting()));
        }
        if (request.getSummary() != null) {
            template.setSummaryConfig(toJson(request.getSummary()));
        }
        if (request.getStyle() != null) {
            template.setStyleConfig(toJson(request.getStyle()));
        }
        template.setHeaderText(request.getHeaderText());
        template.setFooterText(request.getFooterText());
        template.setPublic(request.isPublic());

        ReportTemplate saved = templateRepository.save(template);
        log.info("報表範本更新成功: {}", saved.getId());

        return convertToDto(saved);
    }

    /**
     * 取得範本詳情
     */
    @Transactional(readOnly = true)
    public ReportTemplateDto getTemplate(Long id) {
        ReportTemplate template = templateRepository.findById(id)
                .orElseThrow(() -> new BusinessException("範本不存在: " + id));
        return convertToDto(template);
    }

    /**
     * 查詢範本列表
     */
    @Transactional(readOnly = true)
    public Page<ReportTemplateDto> searchTemplates(String keyword, ReportType reportType, Long userId, Pageable pageable) {
        Page<ReportTemplate> templates = templateRepository.findByConditions(keyword, reportType, userId, pageable);
        return templates.map(this::convertToDto);
    }

    /**
     * 取得用戶可見的範本
     */
    @Transactional(readOnly = true)
    public List<ReportTemplateDto> getAccessibleTemplates(Long userId) {
        List<ReportTemplate> templates = templateRepository.findAccessibleByUser(userId);
        return templates.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    /**
     * 取得熱門範本
     */
    @Transactional(readOnly = true)
    public List<ReportTemplateDto> getPopularTemplates(int limit) {
        List<ReportTemplate> templates = templateRepository.findPopularTemplates(PageRequest.of(0, limit));
        return templates.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    /**
     * 刪除範本
     */
    @Transactional
    public void deleteTemplate(Long id, Long userId) {
        ReportTemplate template = templateRepository.findById(id)
                .orElseThrow(() -> new BusinessException("範本不存在: " + id));

        if (template.isSystem()) {
            throw new BusinessException("系統範本不可刪除");
        }
        if (!template.getOwnerId().equals(userId)) {
            throw new BusinessException("無權限刪除此範本");
        }

        templateRepository.delete(template);
        log.info("報表範本已刪除: {}", id);
    }

    /**
     * 複製範本
     */
    @Transactional
    public ReportTemplateDto copyTemplate(Long id, String newName, Long userId) {
        ReportTemplate source = templateRepository.findById(id)
                .orElseThrow(() -> new BusinessException("範本不存在: " + id));

        ReportTemplate copy = ReportTemplate.builder()
                .name(newName)
                .description(source.getDescription())
                .reportType(source.getReportType())
                .columnsConfig(source.getColumnsConfig())
                .filtersConfig(source.getFiltersConfig())
                .groupingConfig(source.getGroupingConfig())
                .sortingConfig(source.getSortingConfig())
                .summaryConfig(source.getSummaryConfig())
                .styleConfig(source.getStyleConfig())
                .headerText(source.getHeaderText())
                .footerText(source.getFooterText())
                .isPublic(false)
                .ownerId(userId)
                .isSystem(false)
                .usageCount(0)
                .build();

        ReportTemplate saved = templateRepository.save(copy);
        log.info("報表範本複製成功: {} -> {}", id, saved.getId());

        return convertToDto(saved);
    }

    /**
     * 增加使用次數
     */
    @Transactional
    public void incrementUsageCount(Long id) {
        ReportTemplate template = templateRepository.findById(id)
                .orElseThrow(() -> new BusinessException("範本不存在: " + id));
        template.incrementUsageCount();
        templateRepository.save(template);
    }

    /**
     * 取得報表類型的欄位定義
     */
    public List<ReportTypeDefinition> getReportTypeDefinitions() {
        List<ReportTypeDefinition> definitions = new ArrayList<>();

        // 銷售報表欄位
        definitions.add(ReportTypeDefinition.builder()
                .reportType(ReportType.SALES)
                .label("銷售報表")
                .availableFields(getSalesReportFields())
                .build());

        // 庫存報表欄位
        definitions.add(ReportTypeDefinition.builder()
                .reportType(ReportType.INVENTORY)
                .label("庫存報表")
                .availableFields(getInventoryReportFields())
                .build());

        // 採購報表欄位
        definitions.add(ReportTypeDefinition.builder()
                .reportType(ReportType.PURCHASING)
                .label("採購報表")
                .availableFields(getPurchasingReportFields())
                .build());

        return definitions;
    }

    private List<AvailableField> getSalesReportFields() {
        return Arrays.asList(
                createField("orderNo", "訂單編號", "string", true, true, false),
                createField("orderDate", "訂單日期", "datetime", true, true, false),
                createField("customerName", "客戶名稱", "string", true, true, false),
                createField("storeName", "門市名稱", "string", true, true, false),
                createField("productName", "商品名稱", "string", true, true, false),
                createField("quantity", "數量", "number", true, false, true),
                createField("unitPrice", "單價", "currency", true, false, true),
                createField("totalAmount", "金額", "currency", true, false, true),
                createField("status", "狀態", "string", true, true, false)
        );
    }

    private List<AvailableField> getInventoryReportFields() {
        return Arrays.asList(
                createField("productSku", "商品貨號", "string", true, true, false),
                createField("productName", "商品名稱", "string", true, true, false),
                createField("categoryName", "分類", "string", true, true, false),
                createField("warehouseName", "倉庫", "string", true, true, false),
                createField("quantity", "庫存數量", "number", true, false, true),
                createField("safetyStock", "安全庫存", "number", true, false, false),
                createField("costPrice", "成本價", "currency", true, false, true),
                createField("totalValue", "庫存金額", "currency", true, false, true)
        );
    }

    private List<AvailableField> getPurchasingReportFields() {
        return Arrays.asList(
                createField("poNo", "採購單號", "string", true, true, false),
                createField("poDate", "採購日期", "datetime", true, true, false),
                createField("supplierName", "供應商", "string", true, true, false),
                createField("productName", "商品名稱", "string", true, true, false),
                createField("quantity", "數量", "number", true, false, true),
                createField("unitPrice", "單價", "currency", true, false, true),
                createField("totalAmount", "金額", "currency", true, false, true),
                createField("status", "狀態", "string", true, true, false)
        );
    }

    private AvailableField createField(String field, String label, String dataType,
                                        boolean sortable, boolean filterable, boolean aggregatable) {
        List<String> operators = new ArrayList<>();
        if ("string".equals(dataType)) {
            operators = Arrays.asList("=", "!=", "contains", "startsWith", "endsWith");
        } else if ("number".equals(dataType) || "currency".equals(dataType)) {
            operators = Arrays.asList("=", "!=", ">", ">=", "<", "<=", "between");
        } else if ("datetime".equals(dataType)) {
            operators = Arrays.asList("=", ">", ">=", "<", "<=", "between");
        }

        return AvailableField.builder()
                .field(field)
                .label(label)
                .dataType(dataType)
                .sortable(sortable)
                .filterable(filterable)
                .aggregatable(aggregatable)
                .availableOperators(operators)
                .build();
    }

    private String toJson(Object obj) {
        if (obj == null) return null;
        try {
            return objectMapper.writeValueAsString(obj);
        } catch (JsonProcessingException e) {
            log.error("JSON 序列化失敗", e);
            return null;
        }
    }

    private <T> T fromJson(String json, TypeReference<T> typeRef) {
        if (json == null || json.isEmpty()) return null;
        try {
            return objectMapper.readValue(json, typeRef);
        } catch (JsonProcessingException e) {
            log.error("JSON 反序列化失敗", e);
            return null;
        }
    }

    private ReportTemplateDto convertToDto(ReportTemplate template) {
        return ReportTemplateDto.builder()
                .id(template.getId())
                .name(template.getName())
                .description(template.getDescription())
                .reportType(template.getReportType())
                .reportTypeLabel(template.getReportType().getLabel())
                .columns(fromJson(template.getColumnsConfig(), new TypeReference<List<ColumnConfig>>() {}))
                .filters(fromJson(template.getFiltersConfig(), new TypeReference<List<FilterConfig>>() {}))
                .grouping(fromJson(template.getGroupingConfig(), new TypeReference<List<GroupingConfig>>() {}))
                .sorting(fromJson(template.getSortingConfig(), new TypeReference<List<SortingConfig>>() {}))
                .summary(fromJson(template.getSummaryConfig(), new TypeReference<List<SummaryConfig>>() {}))
                .style(fromJson(template.getStyleConfig(), new TypeReference<StyleConfig>() {}))
                .headerText(template.getHeaderText())
                .footerText(template.getFooterText())
                .isPublic(template.isPublic())
                .ownerId(template.getOwnerId())
                .isSystem(template.isSystem())
                .usageCount(template.getUsageCount())
                .createdAt(template.getCreatedAt())
                .updatedAt(template.getUpdatedAt())
                .build();
    }
}

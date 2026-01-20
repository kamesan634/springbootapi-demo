package com.kamesan.erpapi.system.service;

import com.kamesan.erpapi.common.exception.BusinessException;
import com.kamesan.erpapi.system.dto.CreateSystemParameterRequest;
import com.kamesan.erpapi.system.dto.SystemParameterDto;
import com.kamesan.erpapi.system.entity.SystemParameter;
import com.kamesan.erpapi.system.repository.SystemParameterRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 系統參數服務
 *
 * <p>提供系統參數的 CRUD 操作</p>
 *
 * @author ERP System Team
 * @version 1.0.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SystemParameterService {

    private final SystemParameterRepository systemParameterRepository;

    /**
     * 根據類別查詢參數
     */
    @Transactional(readOnly = true)
    public List<SystemParameterDto> getParametersByCategory(String category) {
        return systemParameterRepository.findByCategoryOrderBySortOrderAsc(category)
                .stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    /**
     * 根據類別和鍵取得參數值
     */
    @Transactional(readOnly = true)
    public String getParameterValue(String category, String paramKey) {
        return systemParameterRepository.findByCategoryAndParamKey(category, paramKey)
                .map(SystemParameter::getParamValue)
                .orElse(null);
    }

    /**
     * 根據類別和鍵取得參數值（帶預設值）
     */
    @Transactional(readOnly = true)
    public String getParameterValue(String category, String paramKey, String defaultValue) {
        return systemParameterRepository.findByCategoryAndParamKey(category, paramKey)
                .map(SystemParameter::getParamValue)
                .orElse(defaultValue);
    }

    /**
     * 新增參數
     */
    @Transactional
    public SystemParameterDto createParameter(CreateSystemParameterRequest request) {
        log.info("新增系統參數：{}.{}", request.getCategory(), request.getParamKey());

        if (systemParameterRepository.existsByCategoryAndParamKey(request.getCategory(), request.getParamKey())) {
            throw BusinessException.alreadyExists("系統參數", "鍵", request.getCategory() + "." + request.getParamKey());
        }

        SystemParameter param = SystemParameter.builder()
                .category(request.getCategory())
                .paramKey(request.getParamKey())
                .paramValue(request.getParamValue())
                .paramType(request.getParamType() != null ? request.getParamType() : SystemParameter.ParamType.STRING)
                .description(request.getDescription())
                .isSystem(false)
                .isEncrypted(request.getIsEncrypted() != null ? request.getIsEncrypted() : false)
                .sortOrder(request.getSortOrder() != null ? request.getSortOrder() : 0)
                .build();

        SystemParameter saved = systemParameterRepository.save(param);
        log.info("系統參數新增成功，ID: {}", saved.getId());

        return convertToDto(saved);
    }

    /**
     * 更新參數值
     */
    @Transactional
    public SystemParameterDto updateParameterValue(String category, String paramKey, String newValue) {
        log.info("更新系統參數：{}.{}", category, paramKey);

        SystemParameter param = systemParameterRepository.findByCategoryAndParamKey(category, paramKey)
                .orElseThrow(() -> new BusinessException(404, "系統參數不存在：" + category + "." + paramKey));

        param.setParamValue(newValue);
        SystemParameter saved = systemParameterRepository.save(param);
        log.info("系統參數更新成功");

        return convertToDto(saved);
    }

    /**
     * 刪除參數
     */
    @Transactional
    public void deleteParameter(Long id) {
        log.info("刪除系統參數，ID: {}", id);

        SystemParameter param = systemParameterRepository.findById(id)
                .orElseThrow(() -> BusinessException.notFound("系統參數", id));

        if (param.getIsSystem()) {
            throw BusinessException.validationFailed("系統參數不可刪除");
        }

        systemParameterRepository.delete(param);
        log.info("系統參數刪除成功");
    }

    /**
     * 取得所有參數
     */
    @Transactional(readOnly = true)
    public List<SystemParameterDto> getAllParameters() {
        return systemParameterRepository.findAll()
                .stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    private SystemParameterDto convertToDto(SystemParameter param) {
        return SystemParameterDto.builder()
                .id(param.getId())
                .category(param.getCategory())
                .paramKey(param.getParamKey())
                .paramValue(param.getIsEncrypted() ? "******" : param.getParamValue())
                .paramType(param.getParamType())
                .description(param.getDescription())
                .isSystem(param.getIsSystem())
                .isEncrypted(param.getIsEncrypted())
                .sortOrder(param.getSortOrder())
                .createdAt(param.getCreatedAt())
                .updatedAt(param.getUpdatedAt())
                .build();
    }
}

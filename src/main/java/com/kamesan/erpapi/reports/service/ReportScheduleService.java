package com.kamesan.erpapi.reports.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.kamesan.erpapi.common.exception.BusinessException;
import com.kamesan.erpapi.reports.dto.ReportScheduleDto;
import com.kamesan.erpapi.reports.dto.ReportScheduleDto.*;
import com.kamesan.erpapi.reports.entity.ReportExecution;
import com.kamesan.erpapi.reports.entity.ReportSchedule;
import com.kamesan.erpapi.reports.entity.ReportSchedule.*;
import com.kamesan.erpapi.reports.repository.ReportExecutionRepository;
import com.kamesan.erpapi.reports.repository.ReportScheduleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 排程報表服務
 *
 * @author ERP System Team
 * @version 1.0.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ReportScheduleService {

    private final ReportScheduleRepository scheduleRepository;
    private final ReportExecutionRepository executionRepository;
    private final ReportService reportService;
    private final ReportExportService reportExportService;
    private final ObjectMapper objectMapper;

    /**
     * 建立排程
     */
    @Transactional
    public ReportScheduleDto createSchedule(CreateScheduleRequest request) {
        log.info("建立排程報表: {}", request.getName());

        ReportSchedule schedule = ReportSchedule.builder()
                .name(request.getName())
                .description(request.getDescription())
                .reportType(request.getReportType())
                .frequency(request.getFrequency())
                .cronExpression(request.getCronExpression())
                .runHour(request.getRunHour() != null ? request.getRunHour() : 8)
                .runMinute(request.getRunMinute() != null ? request.getRunMinute() : 0)
                .dayOfWeek(request.getDayOfWeek())
                .dayOfMonth(request.getDayOfMonth())
                .exportFormat(request.getExportFormat() != null ? request.getExportFormat() : ExportFormat.EXCEL)
                .parameters(convertParametersToJson(request.getParameters()))
                .notificationType(request.getNotificationType() != null ? request.getNotificationType() : NotificationType.EMAIL)
                .recipients(request.getRecipients())
                .active(true)
                .build();

        // 計算下次執行時間
        schedule.setNextRunTime(calculateNextRunTime(schedule));

        ReportSchedule saved = scheduleRepository.save(schedule);
        log.info("排程報表建立成功: {}", saved.getId());

        return convertToDto(saved);
    }

    /**
     * 更新排程
     */
    @Transactional
    public ReportScheduleDto updateSchedule(Long id, UpdateScheduleRequest request) {
        log.info("更新排程報表: {}", id);

        ReportSchedule schedule = scheduleRepository.findById(id)
                .orElseThrow(() -> new BusinessException("排程不存在: " + id));

        if (request.getName() != null) {
            schedule.setName(request.getName());
        }
        if (request.getDescription() != null) {
            schedule.setDescription(request.getDescription());
        }
        if (request.getFrequency() != null) {
            schedule.setFrequency(request.getFrequency());
        }
        schedule.setCronExpression(request.getCronExpression());
        if (request.getRunHour() != null) {
            schedule.setRunHour(request.getRunHour());
        }
        if (request.getRunMinute() != null) {
            schedule.setRunMinute(request.getRunMinute());
        }
        schedule.setDayOfWeek(request.getDayOfWeek());
        schedule.setDayOfMonth(request.getDayOfMonth());
        if (request.getExportFormat() != null) {
            schedule.setExportFormat(request.getExportFormat());
        }
        if (request.getParameters() != null) {
            schedule.setParameters(convertParametersToJson(request.getParameters()));
        }
        if (request.getNotificationType() != null) {
            schedule.setNotificationType(request.getNotificationType());
        }
        schedule.setRecipients(request.getRecipients());
        schedule.setActive(request.isActive());

        // 重新計算下次執行時間
        schedule.setNextRunTime(calculateNextRunTime(schedule));

        ReportSchedule saved = scheduleRepository.save(schedule);
        log.info("排程報表更新成功: {}", saved.getId());

        return convertToDto(saved);
    }

    /**
     * 取得排程詳情
     */
    @Transactional(readOnly = true)
    public ReportScheduleDto getSchedule(Long id) {
        ReportSchedule schedule = scheduleRepository.findById(id)
                .orElseThrow(() -> new BusinessException("排程不存在: " + id));
        return convertToDto(schedule);
    }

    /**
     * 查詢排程列表
     */
    @Transactional(readOnly = true)
    public Page<ReportScheduleDto> searchSchedules(String keyword, ReportType reportType, Boolean active, Pageable pageable) {
        Page<ReportSchedule> schedules = scheduleRepository.findByConditions(keyword, reportType, active, pageable);
        return schedules.map(this::convertToDto);
    }

    /**
     * 啟用/停用排程
     */
    @Transactional
    public ReportScheduleDto toggleActive(Long id) {
        ReportSchedule schedule = scheduleRepository.findById(id)
                .orElseThrow(() -> new BusinessException("排程不存在: " + id));

        schedule.setActive(!schedule.isActive());

        if (schedule.isActive()) {
            schedule.setNextRunTime(calculateNextRunTime(schedule));
        }

        ReportSchedule saved = scheduleRepository.save(schedule);
        log.info("排程 {} 狀態已變更為: {}", id, saved.isActive() ? "啟用" : "停用");

        return convertToDto(saved);
    }

    /**
     * 刪除排程
     */
    @Transactional
    public void deleteSchedule(Long id) {
        ReportSchedule schedule = scheduleRepository.findById(id)
                .orElseThrow(() -> new BusinessException("排程不存在: " + id));

        scheduleRepository.delete(schedule);
        log.info("排程報表已刪除: {}", id);
    }

    /**
     * 立即執行排程
     */
    @Transactional
    public ExecutionDto executeNow(Long id, Long executedBy) {
        log.info("手動執行排程: {}", id);

        ReportSchedule schedule = scheduleRepository.findById(id)
                .orElseThrow(() -> new BusinessException("排程不存在: " + id));

        ReportExecution execution = ReportExecution.builder()
                .schedule(schedule)
                .startTime(LocalDateTime.now())
                .status(ExecutionStatus.RUNNING)
                .parameters(schedule.getParameters())
                .executedBy(executedBy)
                .manual(true)
                .build();

        schedule.addExecution(execution);
        execution = executionRepository.save(execution);

        try {
            // 執行報表產生
            String filePath = generateReport(schedule);
            execution.markCompleted(filePath, getFileSize(filePath));

            // 更新排程狀態
            schedule.setLastRunTime(LocalDateTime.now());
            schedule.setLastRunStatus(ExecutionStatus.SUCCESS);

            log.info("排程執行成功: {}", id);
        } catch (Exception e) {
            log.error("排程執行失敗: {}", id, e);
            execution.markFailed(e.getMessage());

            schedule.setLastRunTime(LocalDateTime.now());
            schedule.setLastRunStatus(ExecutionStatus.FAILED);
        }

        executionRepository.save(execution);
        scheduleRepository.save(schedule);

        return convertExecutionToDto(execution);
    }

    /**
     * 取得執行歷史
     */
    @Transactional(readOnly = true)
    public Page<ExecutionDto> getExecutionHistory(Long scheduleId, Pageable pageable) {
        Page<ReportExecution> executions = executionRepository.findByScheduleIdOrderByStartTimeDesc(scheduleId, pageable);
        return executions.map(this::convertExecutionToDto);
    }

    /**
     * 取得排程統計
     */
    @Transactional(readOnly = true)
    public ScheduleStatsDto getScheduleStats() {
        LocalDateTime todayStart = LocalDate.now().atStartOfDay();
        LocalDateTime todayEnd = LocalDate.now().atTime(LocalTime.MAX);

        Long totalSchedules = scheduleRepository.count();
        Long activeSchedules = scheduleRepository.countActiveSchedules();
        Long todayExecutions = executionRepository.countTodayExecutions(todayStart);
        Long successExecutions = executionRepository.countSuccessByDateRange(todayStart, todayEnd);
        Long failedExecutions = executionRepository.countFailedByDateRange(todayStart, todayEnd);

        List<Object[]> typeCounts = scheduleRepository.countByReportType();
        List<ReportTypeCount> countByReportType = typeCounts.stream()
                .map(arr -> ReportTypeCount.builder()
                        .reportType((ReportType) arr[0])
                        .label(((ReportType) arr[0]).getLabel())
                        .count(((Long) arr[1]).intValue())
                        .build())
                .collect(Collectors.toList());

        return ScheduleStatsDto.builder()
                .totalSchedules(totalSchedules.intValue())
                .activeSchedules(activeSchedules.intValue())
                .todayExecutions(todayExecutions.intValue())
                .successExecutions(successExecutions.intValue())
                .failedExecutions(failedExecutions.intValue())
                .countByReportType(countByReportType)
                .build();
    }

    /**
     * 處理待執行的排程（供排程任務呼叫）
     */
    @Transactional
    public void processScheduledReports() {
        List<ReportSchedule> schedulesToRun = scheduleRepository.findSchedulesToRun(LocalDateTime.now());

        for (ReportSchedule schedule : schedulesToRun) {
            try {
                executeSchedule(schedule);
            } catch (Exception e) {
                log.error("執行排程失敗: {}", schedule.getId(), e);
            }
        }
    }

    private void executeSchedule(ReportSchedule schedule) {
        log.info("自動執行排程: {} - {}", schedule.getId(), schedule.getName());

        ReportExecution execution = ReportExecution.builder()
                .schedule(schedule)
                .startTime(LocalDateTime.now())
                .status(ExecutionStatus.RUNNING)
                .parameters(schedule.getParameters())
                .manual(false)
                .build();

        schedule.addExecution(execution);
        execution = executionRepository.save(execution);

        try {
            String filePath = generateReport(schedule);
            execution.markCompleted(filePath, getFileSize(filePath));

            schedule.setLastRunTime(LocalDateTime.now());
            schedule.setLastRunStatus(ExecutionStatus.SUCCESS);
            schedule.setNextRunTime(calculateNextRunTime(schedule));

            log.info("排程執行成功: {}", schedule.getId());
        } catch (Exception e) {
            log.error("排程執行失敗: {}", schedule.getId(), e);
            execution.markFailed(e.getMessage());

            schedule.setLastRunTime(LocalDateTime.now());
            schedule.setLastRunStatus(ExecutionStatus.FAILED);
            schedule.setNextRunTime(calculateNextRunTime(schedule));
        }

        executionRepository.save(execution);
        scheduleRepository.save(schedule);
    }

    private String generateReport(ReportSchedule schedule) throws Exception {
        Map<String, Object> params = convertJsonToParameters(schedule.getParameters());

        // 根據報表類型產生報表
        // 這裡簡化處理，實際應該根據 exportFormat 產生對應格式的檔案
        String basePath = "/tmp/reports/";
        String fileName = schedule.getReportType().name().toLowerCase() + "_" +
                LocalDateTime.now().toString().replace(":", "-") + "." +
                schedule.getExportFormat().name().toLowerCase();

        // TODO: 實際呼叫報表產生服務
        log.info("產生報表: {} -> {}", schedule.getReportType(), basePath + fileName);

        return basePath + fileName;
    }

    private Long getFileSize(String filePath) {
        // TODO: 實際取得檔案大小
        return 0L;
    }

    private LocalDateTime calculateNextRunTime(ReportSchedule schedule) {
        if (!schedule.isActive()) {
            return null;
        }

        LocalDateTime now = LocalDateTime.now();
        LocalTime runTime = LocalTime.of(schedule.getRunHour(), schedule.getRunMinute());

        switch (schedule.getFrequency()) {
            case DAILY:
                LocalDateTime nextDaily = LocalDateTime.of(now.toLocalDate(), runTime);
                if (nextDaily.isBefore(now) || nextDaily.isEqual(now)) {
                    nextDaily = nextDaily.plusDays(1);
                }
                return nextDaily;

            case WEEKLY:
                int targetDayOfWeek = schedule.getDayOfWeek() != null ? schedule.getDayOfWeek() : 1;
                LocalDateTime nextWeekly = LocalDateTime.of(now.toLocalDate(), runTime);
                int currentDayOfWeek = now.getDayOfWeek().getValue();
                int daysToAdd = targetDayOfWeek - currentDayOfWeek;
                if (daysToAdd < 0 || (daysToAdd == 0 && !nextWeekly.isAfter(now))) {
                    daysToAdd += 7;
                }
                return nextWeekly.plusDays(daysToAdd);

            case MONTHLY:
                int targetDay = schedule.getDayOfMonth() != null ? schedule.getDayOfMonth() : 1;
                LocalDateTime nextMonthly = LocalDateTime.of(
                        now.toLocalDate().withDayOfMonth(Math.min(targetDay, now.toLocalDate().lengthOfMonth())),
                        runTime);
                if (nextMonthly.isBefore(now) || nextMonthly.isEqual(now)) {
                    nextMonthly = nextMonthly.plusMonths(1);
                    nextMonthly = nextMonthly.withDayOfMonth(Math.min(targetDay, nextMonthly.toLocalDate().lengthOfMonth()));
                }
                return nextMonthly;

            default:
                return null;
        }
    }

    private String convertParametersToJson(Map<String, Object> parameters) {
        if (parameters == null || parameters.isEmpty()) {
            return null;
        }
        try {
            return objectMapper.writeValueAsString(parameters);
        } catch (JsonProcessingException e) {
            log.error("轉換參數失敗", e);
            return null;
        }
    }

    private Map<String, Object> convertJsonToParameters(String json) {
        if (json == null || json.isEmpty()) {
            return new HashMap<>();
        }
        try {
            return objectMapper.readValue(json, new TypeReference<Map<String, Object>>() {});
        } catch (JsonProcessingException e) {
            log.error("解析參數失敗", e);
            return new HashMap<>();
        }
    }

    private ReportScheduleDto convertToDto(ReportSchedule schedule) {
        return ReportScheduleDto.builder()
                .id(schedule.getId())
                .name(schedule.getName())
                .description(schedule.getDescription())
                .reportType(schedule.getReportType())
                .reportTypeLabel(schedule.getReportType().getLabel())
                .frequency(schedule.getFrequency())
                .frequencyLabel(schedule.getFrequency().getLabel())
                .cronExpression(schedule.getCronExpression())
                .runHour(schedule.getRunHour())
                .runMinute(schedule.getRunMinute())
                .dayOfWeek(schedule.getDayOfWeek())
                .dayOfMonth(schedule.getDayOfMonth())
                .exportFormat(schedule.getExportFormat())
                .exportFormatLabel(schedule.getExportFormat().getLabel())
                .parameters(convertJsonToParameters(schedule.getParameters()))
                .notificationType(schedule.getNotificationType())
                .notificationTypeLabel(schedule.getNotificationType().getLabel())
                .recipients(schedule.getRecipients())
                .active(schedule.isActive())
                .lastRunTime(schedule.getLastRunTime())
                .nextRunTime(schedule.getNextRunTime())
                .lastRunStatus(schedule.getLastRunStatus())
                .lastRunStatusLabel(schedule.getLastRunStatus() != null ? schedule.getLastRunStatus().getLabel() : null)
                .createdAt(schedule.getCreatedAt())
                .updatedAt(schedule.getUpdatedAt())
                .build();
    }

    private ExecutionDto convertExecutionToDto(ReportExecution execution) {
        return ExecutionDto.builder()
                .id(execution.getId())
                .scheduleId(execution.getSchedule().getId())
                .scheduleName(execution.getSchedule().getName())
                .startTime(execution.getStartTime())
                .endTime(execution.getEndTime())
                .durationSeconds(execution.calculateDurationSeconds())
                .status(execution.getStatus())
                .statusLabel(execution.getStatus().getLabel())
                .filePath(execution.getFilePath())
                .fileSize(execution.getFileSize())
                .errorMessage(execution.getErrorMessage())
                .notified(execution.isNotified())
                .notifiedAt(execution.getNotifiedAt())
                .executedBy(execution.getExecutedBy())
                .manual(execution.isManual())
                .createdAt(execution.getCreatedAt())
                .build();
    }
}

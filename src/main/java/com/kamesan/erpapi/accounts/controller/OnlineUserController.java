package com.kamesan.erpapi.accounts.controller;

import com.kamesan.erpapi.common.dto.ApiResponse;
import com.kamesan.erpapi.security.redis.OnlineUserService;
import com.kamesan.erpapi.security.redis.OnlineUserService.OnlineUserInfo;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 在線使用者控制器
 *
 * <p>提供在線使用者查詢相關的 API 端點。</p>
 *
 * @author ERP System Team
 * @version 1.0.0
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/online-users")
@RequiredArgsConstructor
@Tag(name = "在線使用者", description = "在線使用者查詢與管理")
public class OnlineUserController {

    private final OnlineUserService onlineUserService;

    /**
     * 取得在線使用者列表
     *
     * @return 在線使用者列表
     */
    @GetMapping
    @Operation(summary = "取得在線使用者列表", description = "取得目前所有在線使用者的資訊")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ApiResponse<List<OnlineUserInfo>> getOnlineUsers() {
        List<OnlineUserInfo> users = onlineUserService.getOnlineUsers();
        return ApiResponse.success(users);
    }

    /**
     * 取得在線人數統計
     *
     * @return 在線人數統計
     */
    @GetMapping("/count")
    @Operation(summary = "取得在線人數統計", description = "取得目前在線人數及各門市在線人數")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ApiResponse<OnlineCountResponse> getOnlineCount() {
        long total = onlineUserService.getOnlineCount();
        Map<Long, Long> byStore = onlineUserService.getOnlineCountByStore();

        OnlineCountResponse response = OnlineCountResponse.builder()
                .total(total)
                .byStore(byStore)
                .build();

        return ApiResponse.success(response);
    }

    /**
     * 檢查使用者是否在線
     *
     * @param userId 使用者 ID
     * @return 是否在線
     */
    @GetMapping("/{userId}/status")
    @Operation(summary = "檢查使用者是否在線", description = "檢查指定使用者是否目前在線")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ApiResponse<OnlineStatusResponse> checkUserOnline(@PathVariable Long userId) {
        boolean online = onlineUserService.isOnline(userId);

        OnlineStatusResponse response = OnlineStatusResponse.builder()
                .userId(userId)
                .online(online)
                .build();

        return ApiResponse.success(response);
    }

    /**
     * 使用者心跳（更新最後活動時間）
     * 前端可定期呼叫此 API 保持在線狀態
     *
     * @param userId 使用者 ID
     * @return 操作結果
     */
    @PostMapping("/{userId}/heartbeat")
    @Operation(summary = "使用者心跳", description = "更新使用者最後活動時間，保持在線狀態")
    public ApiResponse<Void> heartbeat(@PathVariable Long userId) {
        onlineUserService.heartbeat(userId);
        return ApiResponse.success("心跳更新成功", null);
    }

    /**
     * 在線人數統計回應
     */
    @Data
    @Builder
    public static class OnlineCountResponse {
        private long total;
        private Map<Long, Long> byStore;
    }

    /**
     * 在線狀態回應
     */
    @Data
    @Builder
    public static class OnlineStatusResponse {
        private Long userId;
        private boolean online;
    }
}

package com.kamesan.erpapi.common.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.domain.Page;

import java.util.List;

/**
 * 分頁回應格式
 *
 * <p>用於分頁查詢的回應，包含：</p>
 * <ul>
 *   <li>content - 資料內容</li>
 *   <li>page - 目前頁碼（從 1 開始）</li>
 *   <li>size - 每頁筆數</li>
 *   <li>totalElements - 總筆數</li>
 *   <li>totalPages - 總頁數</li>
 *   <li>first - 是否為第一頁</li>
 *   <li>last - 是否為最後一頁</li>
 * </ul>
 *
 * @param <T> 資料型別
 * @author ERP System Team
 * @version 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "分頁回應格式")
public class PageResponse<T> {

    /**
     * 資料內容
     */
    @Schema(description = "資料內容")
    private List<T> content;

    /**
     * 目前頁碼（從 1 開始）
     */
    @Schema(description = "目前頁碼（從 1 開始）", example = "1")
    private int page;

    /**
     * 每頁筆數
     */
    @Schema(description = "每頁筆數", example = "20")
    private int size;

    /**
     * 總筆數
     */
    @Schema(description = "總筆數", example = "100")
    private long totalElements;

    /**
     * 總頁數
     */
    @Schema(description = "總頁數", example = "5")
    private int totalPages;

    /**
     * 是否為第一頁
     */
    @Schema(description = "是否為第一頁", example = "true")
    private boolean first;

    /**
     * 是否為最後一頁
     */
    @Schema(description = "是否為最後一頁", example = "false")
    private boolean last;

    /**
     * 從 Spring Data Page 物件建立分頁回應
     *
     * @param page Spring Data Page 物件
     * @param <T>  資料型別
     * @return 分頁回應
     */
    public static <T> PageResponse<T> of(Page<T> page) {
        return PageResponse.<T>builder()
                .content(page.getContent())
                .page(page.getNumber() + 1)  // Spring Data Page 是從 0 開始
                .size(page.getSize())
                .totalElements(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .first(page.isFirst())
                .last(page.isLast())
                .build();
    }

    /**
     * 從 Spring Data Page 物件建立分頁回應（含轉換）
     *
     * @param page    Spring Data Page 物件
     * @param content 轉換後的資料
     * @param <T>     轉換後的資料型別
     * @param <S>     原始資料型別
     * @return 分頁回應
     */
    public static <T, S> PageResponse<T> of(Page<S> page, List<T> content) {
        return PageResponse.<T>builder()
                .content(content)
                .page(page.getNumber() + 1)
                .size(page.getSize())
                .totalElements(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .first(page.isFirst())
                .last(page.isLast())
                .build();
    }
}

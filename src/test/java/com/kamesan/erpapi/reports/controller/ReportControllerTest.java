package com.kamesan.erpapi.reports.controller;

import com.kamesan.erpapi.BaseIntegrationTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * 報表控制器測試類別
 */
@DisplayName("報表控制器測試")
class ReportControllerTest extends BaseIntegrationTest {

    private static final String REPORTS_API = "/api/v1/reports";

    @Nested
    @DisplayName("GET /api/v1/reports/dashboard - 取得儀表板")
    class GetDashboardTests {

        @Test
        @DisplayName("應返回儀表板資料")
        void getDashboard_ShouldReturnDashboardData() throws Exception {
            String token = generateAdminToken();

            mockMvc.perform(get(REPORTS_API + "/dashboard")
                            .header("Authorization", bearerToken(token)))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true));
        }

        @Test
        @DisplayName("未驗證應返回 403")
        void getDashboard_WithoutAuth_ShouldReturnForbidden() throws Exception {
            mockMvc.perform(get(REPORTS_API + "/dashboard"))
                    .andDo(print())
                    .andExpect(status().isForbidden());
        }
    }

    @Nested
    @DisplayName("GET /api/v1/reports/sales - 取得銷售報表")
    class GetSalesReportTests {

        @Test
        @DisplayName("應返回銷售報表")
        void getSalesReport_WithDateRange_ShouldReturnReport() throws Exception {
            String token = generateAdminToken();

            LocalDate startDate = LocalDate.now().minusDays(30);
            LocalDate endDate = LocalDate.now();

            mockMvc.perform(get(REPORTS_API + "/sales")
                            .header("Authorization", bearerToken(token))
                            .param("startDate", startDate.toString())
                            .param("endDate", endDate.toString()))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true));
        }
    }

    @Nested
    @DisplayName("GET /api/v1/reports/inventory - 取得庫存報表")
    class GetInventoryReportTests {

        @Test
        @DisplayName("應返回庫存報表")
        void getInventoryReport_ShouldReturnReport() throws Exception {
            String token = generateAdminToken();

            mockMvc.perform(get(REPORTS_API + "/inventory")
                            .header("Authorization", bearerToken(token)))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true));
        }
    }

    @Nested
    @DisplayName("GET /api/v1/reports/purchasing - 取得採購報表")
    class GetPurchasingReportTests {

        @Test
        @DisplayName("應返回採購報表")
        void getPurchasingReport_WithDateRange_ShouldReturnReport() throws Exception {
            String token = generateAdminToken();

            LocalDate startDate = LocalDate.now().minusDays(30);
            LocalDate endDate = LocalDate.now();

            mockMvc.perform(get(REPORTS_API + "/purchasing")
                            .header("Authorization", bearerToken(token))
                            .param("startDate", startDate.toString())
                            .param("endDate", endDate.toString()))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true));
        }
    }

    @Nested
    @DisplayName("GET /api/v1/reports/sales/daily - 取得每日銷售")
    class GetDailySalesTests {

        @Test
        @DisplayName("應返回每日銷售報表")
        void getDailySales_ShouldReturnReport() throws Exception {
            String token = generateAdminToken();

            LocalDate date = LocalDate.now();

            mockMvc.perform(get(REPORTS_API + "/sales/daily")
                            .header("Authorization", bearerToken(token))
                            .param("date", date.toString()))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true));
        }
    }

    @Nested
    @DisplayName("GET /api/v1/reports/sales/monthly - 取得月度銷售")
    class GetMonthlySalesTests {

        @Test
        @DisplayName("應返回月度銷售報表")
        void getMonthlySales_ShouldReturnReport() throws Exception {
            String token = generateAdminToken();

            mockMvc.perform(get(REPORTS_API + "/sales/monthly")
                            .header("Authorization", bearerToken(token))
                            .param("year", "2026")
                            .param("month", "1"))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true));
        }
    }

    @Nested
    @DisplayName("GET /api/v1/reports/profit-analysis - 取得利潤分析")
    class GetProfitAnalysisTests {

        @Test
        @DisplayName("應返回利潤分析報表")
        void getProfitAnalysis_ShouldReturnReport() throws Exception {
            String token = generateAdminToken();

            LocalDate startDate = LocalDate.now().minusDays(30);
            LocalDate endDate = LocalDate.now();

            mockMvc.perform(get(REPORTS_API + "/profit-analysis")
                            .header("Authorization", bearerToken(token))
                            .param("startDate", startDate.toString())
                            .param("endDate", endDate.toString()))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true));
        }
    }

    @Nested
    @DisplayName("GET /api/v1/reports/comparison/yoy - 年對年比較")
    class GetYearOverYearComparisonTests {

        @Test
        @DisplayName("應返回年對年比較報表")
        void getYoyComparison_ShouldReturnReport() throws Exception {
            String token = generateAdminToken();

            mockMvc.perform(get(REPORTS_API + "/comparison/yoy")
                            .header("Authorization", bearerToken(token))
                            .param("year", "2026")
                            .param("month", "1"))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true));
        }
    }

    @Nested
    @DisplayName("GET /api/v1/reports/comparison/mom - 月對月比較")
    class GetMonthOverMonthComparisonTests {

        @Test
        @DisplayName("應返回月對月比較報表")
        void getMomComparison_ShouldReturnReport() throws Exception {
            String token = generateAdminToken();

            mockMvc.perform(get(REPORTS_API + "/comparison/mom")
                            .header("Authorization", bearerToken(token))
                            .param("year", "2026")
                            .param("month", "1"))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true));
        }
    }

    @Nested
    @DisplayName("報表匯出 API 測試")
    class ExportReportTests {

        @Test
        @DisplayName("應匯出銷售報表 Excel")
        void exportSalesExcel_ShouldReturnExcelFile() throws Exception {
            String token = generateAdminToken();

            LocalDate startDate = LocalDate.now().minusDays(30);
            LocalDate endDate = LocalDate.now();

            mockMvc.perform(get(REPORTS_API + "/sales/export/excel")
                            .header("Authorization", bearerToken(token))
                            .param("startDate", startDate.toString())
                            .param("endDate", endDate.toString()))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(header().string("Content-Disposition", org.hamcrest.Matchers.containsString("attachment")));
        }

        @Test
        @DisplayName("應匯出銷售報表 CSV")
        void exportSalesCsv_ShouldReturnCsvFile() throws Exception {
            String token = generateAdminToken();

            LocalDate startDate = LocalDate.now().minusDays(30);
            LocalDate endDate = LocalDate.now();

            mockMvc.perform(get(REPORTS_API + "/sales/export/csv")
                            .header("Authorization", bearerToken(token))
                            .param("startDate", startDate.toString())
                            .param("endDate", endDate.toString()))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(header().string("Content-Disposition", org.hamcrest.Matchers.containsString("attachment")));
        }

        @Test
        @DisplayName("應匯出庫存報表 Excel")
        void exportInventoryExcel_ShouldReturnExcelFile() throws Exception {
            String token = generateAdminToken();

            mockMvc.perform(get(REPORTS_API + "/inventory/export/excel")
                            .header("Authorization", bearerToken(token)))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(header().string("Content-Disposition", org.hamcrest.Matchers.containsString("attachment")));
        }
    }
}

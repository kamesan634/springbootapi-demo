package com.kamesan.erpapi.reports.service;

import com.kamesan.erpapi.reports.dto.*;
import com.kamesan.erpapi.sales.repository.OrderRepository;
import com.kamesan.erpapi.sales.repository.OrderItemRepository;
import com.kamesan.erpapi.inventory.repository.InventoryRepository;
import com.kamesan.erpapi.customers.repository.CustomerRepository;
import com.kamesan.erpapi.products.repository.ProductRepository;
import com.kamesan.erpapi.products.repository.CategoryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * 報表服務
 *
 * <p>提供各類報表和儀表板資料</p>
 *
 * @author ERP System Team
 * @version 1.0.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ReportService {

    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final InventoryRepository inventoryRepository;
    private final CustomerRepository customerRepository;
    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;

    /**
     * 取得儀表板資料
     */
    @Transactional(readOnly = true)
    public DashboardDto getDashboard(Long storeId) {
        log.debug("取得儀表板資料，門市: {}", storeId);

        LocalDate today = LocalDate.now();
        LocalDate monthStart = today.withDayOfMonth(1);
        LocalDate yearStart = today.withDayOfYear(1);
        LocalDate yesterday = today.minusDays(1);

        // 銷售統計 - 查詢實際數據
        BigDecimal todaySales = orderRepository.sumTotalAmountByStatusAndDateRange(
                com.kamesan.erpapi.sales.entity.OrderStatus.PAID, today, today);
        BigDecimal monthSales = orderRepository.sumTotalAmountByStatusAndDateRange(
                com.kamesan.erpapi.sales.entity.OrderStatus.PAID, monthStart, today);
        BigDecimal yearSales = orderRepository.sumTotalAmountByStatusAndDateRange(
                com.kamesan.erpapi.sales.entity.OrderStatus.PAID, yearStart, today);
        Long todayOrderCount = orderRepository.countByDateRange(today, today);
        Long monthOrderCount = orderRepository.countByDateRange(monthStart, today);

        // 計算銷售成長率（與昨日比較）
        BigDecimal yesterdaySales = orderRepository.sumTotalAmountByStatusAndDateRange(
                com.kamesan.erpapi.sales.entity.OrderStatus.PAID, yesterday, yesterday);
        BigDecimal salesGrowthRate = BigDecimal.ZERO;
        if (yesterdaySales.compareTo(BigDecimal.ZERO) > 0) {
            salesGrowthRate = todaySales.subtract(yesterdaySales)
                    .divide(yesterdaySales, 4, RoundingMode.HALF_UP)
                    .multiply(BigDecimal.valueOf(100));
        }

        // 庫存統計
        Integer totalProducts = (int) productRepository.count();
        Integer lowStockProducts = inventoryRepository.countLowStockProducts(10).intValue();
        Integer outOfStockProducts = inventoryRepository.countOutOfStockProducts().intValue();
        BigDecimal inventoryValue = inventoryRepository.sumTotalInventoryValue();

        // 客戶統計
        Integer totalCustomers = (int) customerRepository.count();

        // 圖表資料 - 最近 7 天銷售
        LocalDate chartStart = today.minusDays(6);
        List<Object[]> salesData = orderRepository.sumSalesByDateRange(chartStart, today);

        // 建立日期到銷售資料的映射
        java.util.Map<String, DashboardDto.SalesChartData> salesMap = new java.util.HashMap<>();
        for (Object[] row : salesData) {
            String dateStr = row[0].toString();
            BigDecimal sales = row[1] != null ? (BigDecimal) row[1] : BigDecimal.ZERO;
            Long count = row[2] != null ? (Long) row[2] : 0L;
            salesMap.put(dateStr, DashboardDto.SalesChartData.builder()
                    .date(dateStr)
                    .sales(sales)
                    .orderCount(count.intValue())
                    .build());
        }

        // 填充完整 7 天資料
        List<DashboardDto.SalesChartData> salesChart = new ArrayList<>();
        for (int i = 6; i >= 0; i--) {
            LocalDate date = today.minusDays(i);
            String dateStr = date.toString();
            DashboardDto.SalesChartData chartData = salesMap.getOrDefault(dateStr,
                    DashboardDto.SalesChartData.builder()
                            .date(dateStr)
                            .sales(BigDecimal.ZERO)
                            .orderCount(0)
                            .build());
            salesChart.add(chartData);
        }

        return DashboardDto.builder()
                .todaySales(todaySales != null ? todaySales : BigDecimal.ZERO)
                .monthSales(monthSales != null ? monthSales : BigDecimal.ZERO)
                .yearSales(yearSales != null ? yearSales : BigDecimal.ZERO)
                .todayOrderCount(todayOrderCount != null ? todayOrderCount.intValue() : 0)
                .monthOrderCount(monthOrderCount != null ? monthOrderCount.intValue() : 0)
                .salesGrowthRate(salesGrowthRate)
                .totalProducts(totalProducts)
                .lowStockProducts(lowStockProducts)
                .outOfStockProducts(outOfStockProducts)
                .inventoryValue(inventoryValue != null ? inventoryValue : BigDecimal.ZERO)
                .pendingPurchaseOrders(0)
                .monthPurchaseAmount(BigDecimal.ZERO)
                .totalCustomers(totalCustomers)
                .newCustomersThisMonth(0)
                .salesChart(salesChart)
                .categorySales(new ArrayList<>())
                .topProducts(new ArrayList<>())
                .build();
    }

    /**
     * 取得銷售報表
     */
    @Transactional(readOnly = true)
    public SalesReportDto getSalesReport(LocalDate startDate, LocalDate endDate, Long storeId) {
        log.debug("取得銷售報表，期間: {} ~ {}, 門市: {}", startDate, endDate, storeId);

        // 查詢總銷售額（已付款訂單）
        BigDecimal totalSales = orderRepository.sumTotalAmountByStatusAndDateRange(
                com.kamesan.erpapi.sales.entity.OrderStatus.PAID, startDate, endDate);
        if (totalSales == null) totalSales = BigDecimal.ZERO;

        // 查詢總退款額（已退款訂單）
        BigDecimal totalRefunds = orderRepository.sumTotalAmountByStatusAndDateRange(
                com.kamesan.erpapi.sales.entity.OrderStatus.REFUNDED, startDate, endDate);
        if (totalRefunds == null) totalRefunds = BigDecimal.ZERO;

        // 查詢訂單數量
        Long paidCount = orderRepository.countByStatusAndDateRange(
                com.kamesan.erpapi.sales.entity.OrderStatus.PAID, startDate, endDate);
        Integer orderCount = paidCount != null ? paidCount.intValue() : 0;

        // 查詢退款訂單數量
        Long refundedCount = orderRepository.countByStatusAndDateRange(
                com.kamesan.erpapi.sales.entity.OrderStatus.REFUNDED, startDate, endDate);
        Integer refundCount = refundedCount != null ? refundedCount.intValue() : 0;

        // 查詢每日銷售資料
        List<Object[]> salesData = orderRepository.sumSalesByDateRange(startDate, endDate);
        java.util.Map<LocalDate, BigDecimal> salesByDate = new java.util.HashMap<>();
        java.util.Map<LocalDate, Integer> orderCountByDate = new java.util.HashMap<>();
        for (Object[] row : salesData) {
            LocalDate date = (LocalDate) row[0];
            BigDecimal sales = row[1] != null ? (BigDecimal) row[1] : BigDecimal.ZERO;
            Long count = row[2] != null ? (Long) row[2] : 0L;
            salesByDate.put(date, sales);
            orderCountByDate.put(date, count.intValue());
        }

        // 查詢每日退款資料
        List<Object[]> refundsData = orderRepository.sumRefundsByDateRange(startDate, endDate);
        java.util.Map<LocalDate, BigDecimal> refundsByDate = new java.util.HashMap<>();
        java.util.Map<LocalDate, Integer> refundCountByDate = new java.util.HashMap<>();
        for (Object[] row : refundsData) {
            LocalDate date = (LocalDate) row[0];
            BigDecimal refunds = row[1] != null ? (BigDecimal) row[1] : BigDecimal.ZERO;
            Long count = row[2] != null ? (Long) row[2] : 0L;
            refundsByDate.put(date, refunds);
            refundCountByDate.put(date, count.intValue());
        }

        // 建立每日銷售明細
        List<SalesReportDto.DailySales> dailySales = new ArrayList<>();
        LocalDate current = startDate;
        while (!current.isAfter(endDate)) {
            BigDecimal daySales = salesByDate.getOrDefault(current, BigDecimal.ZERO);
            BigDecimal dayRefunds = refundsByDate.getOrDefault(current, BigDecimal.ZERO);
            int dayOrderCount = orderCountByDate.getOrDefault(current, 0);
            int dayRefundCount = refundCountByDate.getOrDefault(current, 0);

            dailySales.add(SalesReportDto.DailySales.builder()
                    .date(current)
                    .sales(daySales)
                    .refunds(dayRefunds)
                    .netSales(daySales.subtract(dayRefunds))
                    .orderCount(dayOrderCount)
                    .refundCount(dayRefundCount)
                    .build());
            current = current.plusDays(1);
        }

        // 付款方式統計（暫時使用預設值，後續可擴充實際查詢）
        List<SalesReportDto.PaymentMethodSummary> paymentMethods = List.of(
                SalesReportDto.PaymentMethodSummary.builder()
                        .paymentMethod("現金")
                        .amount(BigDecimal.ZERO)
                        .count(0)
                        .percentage(BigDecimal.ZERO)
                        .build(),
                SalesReportDto.PaymentMethodSummary.builder()
                        .paymentMethod("信用卡")
                        .amount(BigDecimal.ZERO)
                        .count(0)
                        .percentage(BigDecimal.ZERO)
                        .build()
        );

        BigDecimal netSales = totalSales.subtract(totalRefunds);
        BigDecimal avgOrderAmount = orderCount > 0 ?
                totalSales.divide(BigDecimal.valueOf(orderCount), 2, RoundingMode.HALF_UP) :
                BigDecimal.ZERO;

        // 計算毛利（簡化版：假設成本為銷售額的 60%）
        BigDecimal estimatedCostRatio = new BigDecimal("0.60");
        BigDecimal totalCost = totalSales.multiply(estimatedCostRatio);
        BigDecimal grossProfit = totalSales.subtract(totalCost);
        BigDecimal profitMargin = totalSales.compareTo(BigDecimal.ZERO) > 0 ?
                grossProfit.multiply(BigDecimal.valueOf(100)).divide(totalSales, 2, RoundingMode.HALF_UP) :
                BigDecimal.ZERO;

        return SalesReportDto.builder()
                .startDate(startDate)
                .endDate(endDate)
                .storeId(storeId)
                .totalSales(totalSales)
                .totalRefunds(totalRefunds)
                .netSales(netSales)
                .orderCount(orderCount)
                .refundCount(refundCount)
                .avgOrderAmount(avgOrderAmount)
                .grossProfit(grossProfit)
                .profitMargin(profitMargin)
                .dailySales(dailySales)
                .paymentMethods(paymentMethods)
                .hourlySales(new ArrayList<>())
                .build();
    }

    /**
     * 取得庫存報表
     */
    @Transactional(readOnly = true)
    public InventoryReportDto getInventoryReport(Long warehouseId) {
        log.debug("取得庫存報表，倉庫: {}", warehouseId);

        Integer totalProducts = (int) productRepository.count();

        // 庫存明細
        List<InventoryReportDto.ProductInventory> inventoryList = new ArrayList<>();

        // 類別統計
        List<InventoryReportDto.CategoryInventory> categoryInventory = new ArrayList<>();

        return InventoryReportDto.builder()
                .reportDate(LocalDate.now())
                .warehouseId(warehouseId)
                .totalProducts(totalProducts)
                .totalQuantity(0)
                .totalValue(BigDecimal.ZERO)
                .lowStockCount(0)
                .outOfStockCount(0)
                .overstockCount(0)
                .inventoryList(inventoryList)
                .categoryInventory(categoryInventory)
                .movementSummary(new ArrayList<>())
                .build();
    }

    /**
     * 取得採購報表
     */
    @Transactional(readOnly = true)
    public PurchasingReportDto getPurchasingReport(LocalDate startDate, LocalDate endDate) {
        log.debug("取得採購報表，期間: {} ~ {}", startDate, endDate);

        return PurchasingReportDto.builder()
                .startDate(startDate)
                .endDate(endDate)
                .totalPurchaseAmount(BigDecimal.ZERO)
                .totalReturnAmount(BigDecimal.ZERO)
                .netPurchaseAmount(BigDecimal.ZERO)
                .purchaseOrderCount(0)
                .receiptCount(0)
                .returnCount(0)
                .avgOrderAmount(BigDecimal.ZERO)
                .supplierSummary(new ArrayList<>())
                .productPurchases(new ArrayList<>())
                .monthlyTrend(new ArrayList<>())
                .build();
    }

    /**
     * 取得利潤分析報表
     */
    @Transactional(readOnly = true)
    public ProfitAnalysisDto getProfitAnalysis(LocalDate startDate, LocalDate endDate, Long storeId) {
        log.debug("取得利潤分析，期間: {} ~ {}, 門市: {}", startDate, endDate, storeId);

        // 查詢商品銷售統計
        List<Object[]> productSales = orderItemRepository.findProductSalesByDateRange(startDate, endDate);

        // 建立商品利潤列表
        List<ProfitAnalysisDto.ProductProfit> allProductProfits = new ArrayList<>();
        BigDecimal totalRevenue = BigDecimal.ZERO;
        BigDecimal totalCost = BigDecimal.ZERO;

        // 類別利潤統計用的 Map
        java.util.Map<Long, ProfitAnalysisDto.CategoryProfit> categoryProfitMap = new java.util.HashMap<>();

        for (Object[] row : productSales) {
            Long productId = (Long) row[0];
            Long quantitySold = (Long) row[1];
            BigDecimal revenue = (BigDecimal) row[2];

            // 查詢商品資訊
            var productOpt = productRepository.findById(productId);
            if (productOpt.isEmpty()) continue;

            var product = productOpt.get();
            BigDecimal costPrice = product.getCostPrice() != null ? product.getCostPrice() : BigDecimal.ZERO;
            BigDecimal cost = costPrice.multiply(BigDecimal.valueOf(quantitySold));
            BigDecimal profit = revenue.subtract(cost);
            BigDecimal profitMargin = revenue.compareTo(BigDecimal.ZERO) > 0 ?
                    profit.multiply(BigDecimal.valueOf(100)).divide(revenue, 2, RoundingMode.HALF_UP) :
                    BigDecimal.ZERO;

            // 取得類別名稱
            String categoryName = product.getCategory() != null ? product.getCategory().getName() : "未分類";
            Long categoryId = product.getCategory() != null ? product.getCategory().getId() : 0L;

            // 加入商品利潤列表
            allProductProfits.add(ProfitAnalysisDto.ProductProfit.builder()
                    .productId(productId)
                    .productSku(product.getSku())
                    .productName(product.getName())
                    .categoryName(categoryName)
                    .quantitySold(quantitySold.intValue())
                    .revenue(revenue)
                    .cost(cost)
                    .grossProfit(profit)
                    .profitMargin(profitMargin)
                    .contributionRatio(BigDecimal.ZERO) // 稍後計算
                    .build());

            // 累計總額
            totalRevenue = totalRevenue.add(revenue);
            totalCost = totalCost.add(cost);

            // 更新類別統計
            ProfitAnalysisDto.CategoryProfit catProfit = categoryProfitMap.computeIfAbsent(categoryId,
                    k -> ProfitAnalysisDto.CategoryProfit.builder()
                            .categoryId(categoryId)
                            .categoryName(categoryName)
                            .productCount(0)
                            .quantitySold(0)
                            .revenue(BigDecimal.ZERO)
                            .cost(BigDecimal.ZERO)
                            .grossProfit(BigDecimal.ZERO)
                            .profitMargin(BigDecimal.ZERO)
                            .contributionRatio(BigDecimal.ZERO)
                            .build());

            catProfit.setProductCount(catProfit.getProductCount() + 1);
            catProfit.setQuantitySold(catProfit.getQuantitySold() + quantitySold.intValue());
            catProfit.setRevenue(catProfit.getRevenue().add(revenue));
            catProfit.setCost(catProfit.getCost().add(cost));
            catProfit.setGrossProfit(catProfit.getGrossProfit().add(profit));
        }

        // 計算總利潤
        BigDecimal grossProfit = totalRevenue.subtract(totalCost);

        // 計算商品的貢獻比例
        for (ProfitAnalysisDto.ProductProfit pp : allProductProfits) {
            if (grossProfit.compareTo(BigDecimal.ZERO) > 0) {
                pp.setContributionRatio(pp.getGrossProfit()
                        .multiply(BigDecimal.valueOf(100))
                        .divide(grossProfit, 2, RoundingMode.HALF_UP));
            }
        }

        // 排序取得 TOP 10 和 BOTTOM 10
        allProductProfits.sort((a, b) -> b.getGrossProfit().compareTo(a.getGrossProfit()));
        List<ProfitAnalysisDto.ProductProfit> topProfitProducts = allProductProfits.stream()
                .limit(10)
                .collect(java.util.stream.Collectors.toList());

        List<ProfitAnalysisDto.ProductProfit> bottomProfitProducts = allProductProfits.stream()
                .sorted((a, b) -> a.getGrossProfit().compareTo(b.getGrossProfit()))
                .limit(10)
                .collect(java.util.stream.Collectors.toList());

        // 計算類別利潤率和貢獻比例
        List<ProfitAnalysisDto.CategoryProfit> categoryProfits = new ArrayList<>(categoryProfitMap.values());
        for (ProfitAnalysisDto.CategoryProfit cp : categoryProfits) {
            if (cp.getRevenue().compareTo(BigDecimal.ZERO) > 0) {
                cp.setProfitMargin(cp.getGrossProfit()
                        .multiply(BigDecimal.valueOf(100))
                        .divide(cp.getRevenue(), 2, RoundingMode.HALF_UP));
            }
            if (grossProfit.compareTo(BigDecimal.ZERO) > 0) {
                cp.setContributionRatio(cp.getGrossProfit()
                        .multiply(BigDecimal.valueOf(100))
                        .divide(grossProfit, 2, RoundingMode.HALF_UP));
            }
        }
        // 按利潤排序
        categoryProfits.sort((a, b) -> b.getGrossProfit().compareTo(a.getGrossProfit()));

        // 訂單數量
        Long orderCount = orderRepository.countByStatusAndDateRange(
                com.kamesan.erpapi.sales.entity.OrderStatus.PAID, startDate, endDate);

        // 計算平均值
        BigDecimal grossProfitMargin = totalRevenue.compareTo(BigDecimal.ZERO) > 0 ?
                grossProfit.multiply(BigDecimal.valueOf(100)).divide(totalRevenue, 2, RoundingMode.HALF_UP) :
                BigDecimal.ZERO;
        BigDecimal averageOrderValue = orderCount > 0 ?
                totalRevenue.divide(BigDecimal.valueOf(orderCount), 2, RoundingMode.HALF_UP) :
                BigDecimal.ZERO;
        BigDecimal averageOrderProfit = orderCount > 0 ?
                grossProfit.divide(BigDecimal.valueOf(orderCount), 2, RoundingMode.HALF_UP) :
                BigDecimal.ZERO;

        // 每日利潤趨勢
        List<ProfitAnalysisDto.DailyProfit> dailyProfits = new ArrayList<>();
        LocalDate current = startDate;
        while (!current.isAfter(endDate)) {
            BigDecimal dayRevenue = orderRepository.sumTotalAmountByStatusAndDateRange(
                    com.kamesan.erpapi.sales.entity.OrderStatus.PAID, current, current);
            if (dayRevenue == null) dayRevenue = BigDecimal.ZERO;

            Long dayOrderCount = orderRepository.countByStatusAndDateRange(
                    com.kamesan.erpapi.sales.entity.OrderStatus.PAID, current, current);
            // 使用實際平均成本率計算每日成本
            BigDecimal costRatio = totalRevenue.compareTo(BigDecimal.ZERO) > 0 ?
                    totalCost.divide(totalRevenue, 4, RoundingMode.HALF_UP) :
                    new BigDecimal("0.60");
            BigDecimal dayCost = dayRevenue.multiply(costRatio);
            BigDecimal dayProfit = dayRevenue.subtract(dayCost);
            BigDecimal dayMargin = dayRevenue.compareTo(BigDecimal.ZERO) > 0 ?
                    dayProfit.multiply(BigDecimal.valueOf(100)).divide(dayRevenue, 2, RoundingMode.HALF_UP) :
                    BigDecimal.ZERO;

            dailyProfits.add(ProfitAnalysisDto.DailyProfit.builder()
                    .date(current)
                    .orderCount(dayOrderCount != null ? dayOrderCount.intValue() : 0)
                    .revenue(dayRevenue)
                    .cost(dayCost)
                    .grossProfit(dayProfit)
                    .profitMargin(dayMargin)
                    .build());

            current = current.plusDays(1);
        }

        return ProfitAnalysisDto.builder()
                .startDate(startDate)
                .endDate(endDate)
                .storeId(storeId)
                .totalRevenue(totalRevenue)
                .totalCost(totalCost)
                .grossProfit(grossProfit)
                .grossProfitMargin(grossProfitMargin)
                .orderCount(orderCount != null ? orderCount.intValue() : 0)
                .averageOrderValue(averageOrderValue)
                .averageOrderProfit(averageOrderProfit)
                .topProfitProducts(topProfitProducts)
                .bottomProfitProducts(bottomProfitProducts)
                .categoryProfits(categoryProfits)
                .dailyProfits(dailyProfits)
                .build();
    }

    /**
     * 取得期間比較報表（同比/環比）
     *
     * @param currentStart 本期開始日期
     * @param currentEnd   本期結束日期
     * @param previousStart 上期開始日期
     * @param previousEnd  上期結束日期
     * @param comparisonType 比較類型 (YOY=年對年, MOM=月對月, CUSTOM=自訂)
     */
    @Transactional(readOnly = true)
    public PeriodComparisonDto getPeriodComparison(
            LocalDate currentStart, LocalDate currentEnd,
            LocalDate previousStart, LocalDate previousEnd,
            String comparisonType) {

        log.debug("取得期間比較，本期: {} ~ {}, 上期: {} ~ {}", currentStart, currentEnd, previousStart, previousEnd);

        // 本期資料
        BigDecimal currentRevenue = orderRepository.sumTotalAmountByStatusAndDateRange(
                com.kamesan.erpapi.sales.entity.OrderStatus.PAID, currentStart, currentEnd);
        if (currentRevenue == null) currentRevenue = BigDecimal.ZERO;

        Long currentOrderCount = orderRepository.countByDateRange(currentStart, currentEnd);

        // 上期資料
        BigDecimal previousRevenue = orderRepository.sumTotalAmountByStatusAndDateRange(
                com.kamesan.erpapi.sales.entity.OrderStatus.PAID, previousStart, previousEnd);
        if (previousRevenue == null) previousRevenue = BigDecimal.ZERO;

        Long previousOrderCountLong = orderRepository.countByDateRange(previousStart, previousEnd);

        // 計算變化
        BigDecimal revenueChange = currentRevenue.subtract(previousRevenue);
        BigDecimal revenueChangeRate = previousRevenue.compareTo(BigDecimal.ZERO) > 0 ?
                revenueChange.multiply(BigDecimal.valueOf(100)).divide(previousRevenue, 2, RoundingMode.HALF_UP) :
                BigDecimal.ZERO;

        int orderCountChange = currentOrderCount.intValue() - previousOrderCountLong.intValue();
        BigDecimal orderCountChangeRate = previousOrderCountLong > 0 ?
                BigDecimal.valueOf(orderCountChange * 100.0 / previousOrderCountLong).setScale(2, RoundingMode.HALF_UP) :
                BigDecimal.ZERO;

        BigDecimal currentAvgOrder = currentOrderCount > 0 ?
                currentRevenue.divide(BigDecimal.valueOf(currentOrderCount), 2, RoundingMode.HALF_UP) :
                BigDecimal.ZERO;
        BigDecimal previousAvgOrder = previousOrderCountLong > 0 ?
                previousRevenue.divide(BigDecimal.valueOf(previousOrderCountLong), 2, RoundingMode.HALF_UP) :
                BigDecimal.ZERO;

        BigDecimal avgOrderChange = currentAvgOrder.subtract(previousAvgOrder);
        BigDecimal avgOrderChangeRate = previousAvgOrder.compareTo(BigDecimal.ZERO) > 0 ?
                avgOrderChange.multiply(BigDecimal.valueOf(100)).divide(previousAvgOrder, 2, RoundingMode.HALF_UP) :
                BigDecimal.ZERO;

        // 計算天數
        int currentDays = (int) java.time.temporal.ChronoUnit.DAYS.between(currentStart, currentEnd) + 1;
        int previousDays = (int) java.time.temporal.ChronoUnit.DAYS.between(previousStart, previousEnd) + 1;

        PeriodComparisonDto.PeriodInfo currentPeriod = PeriodComparisonDto.PeriodInfo.builder()
                .startDate(currentStart)
                .endDate(currentEnd)
                .label("本期")
                .days(currentDays)
                .build();

        PeriodComparisonDto.PeriodInfo previousPeriod = PeriodComparisonDto.PeriodInfo.builder()
                .startDate(previousStart)
                .endDate(previousEnd)
                .label("上期")
                .days(previousDays)
                .build();

        PeriodComparisonDto.ComparisonSummary summary = PeriodComparisonDto.ComparisonSummary.builder()
                .currentRevenue(currentRevenue)
                .currentOrderCount(currentOrderCount.intValue())
                .currentAverageOrder(currentAvgOrder)
                .previousRevenue(previousRevenue)
                .previousOrderCount(previousOrderCountLong.intValue())
                .previousAverageOrder(previousAvgOrder)
                .revenueChange(revenueChange)
                .revenueChangeRate(revenueChangeRate)
                .orderCountChange(orderCountChange)
                .orderCountChangeRate(orderCountChangeRate)
                .averageOrderChange(avgOrderChange)
                .averageOrderChangeRate(avgOrderChangeRate)
                .build();

        return PeriodComparisonDto.builder()
                .currentPeriod(currentPeriod)
                .previousPeriod(previousPeriod)
                .comparisonType(comparisonType)
                .summary(summary)
                .dailyComparisons(new ArrayList<>())
                .productComparisons(new ArrayList<>())
                .categoryComparisons(new ArrayList<>())
                .build();
    }

    /**
     * 取得年對年比較
     */
    @Transactional(readOnly = true)
    public PeriodComparisonDto getYearOverYearComparison(int year, int month, Long storeId) {
        LocalDate currentStart = LocalDate.of(year, month, 1);
        LocalDate currentEnd = currentStart.withDayOfMonth(currentStart.lengthOfMonth());
        LocalDate previousStart = LocalDate.of(year - 1, month, 1);
        LocalDate previousEnd = previousStart.withDayOfMonth(previousStart.lengthOfMonth());

        return getPeriodComparison(currentStart, currentEnd, previousStart, previousEnd, "YOY");
    }

    /**
     * 取得月對月比較
     */
    @Transactional(readOnly = true)
    public PeriodComparisonDto getMonthOverMonthComparison(int year, int month, Long storeId) {
        LocalDate currentStart = LocalDate.of(year, month, 1);
        LocalDate currentEnd = currentStart.withDayOfMonth(currentStart.lengthOfMonth());
        LocalDate previousStart = currentStart.minusMonths(1);
        LocalDate previousEnd = previousStart.withDayOfMonth(previousStart.lengthOfMonth());

        return getPeriodComparison(currentStart, currentEnd, previousStart, previousEnd, "MOM");
    }
}

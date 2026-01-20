package com.kamesan.erpapi.customers.service;

import com.kamesan.erpapi.customers.dto.RfmAnalysisDto;
import com.kamesan.erpapi.customers.dto.RfmAnalysisDto.*;
import com.kamesan.erpapi.customers.entity.Customer;
import com.kamesan.erpapi.customers.repository.CustomerRepository;
import com.kamesan.erpapi.sales.entity.OrderStatus;
import com.kamesan.erpapi.sales.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

/**
 * RFM 客戶分析服務
 *
 * @author ERP System Team
 * @version 1.0.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RfmAnalysisService {

    private final CustomerRepository customerRepository;
    private final OrderRepository orderRepository;

    // RFM 分數分界點 (依百分位)
    private static final int[] RECENCY_BREAKS = {7, 30, 90, 180}; // 天數
    private static final int[] FREQUENCY_BREAKS = {1, 3, 6, 12}; // 訂單數
    private static final BigDecimal[] MONETARY_BREAKS = {
            new BigDecimal("1000"),
            new BigDecimal("5000"),
            new BigDecimal("15000"),
            new BigDecimal("50000")
    };

    /**
     * 執行 RFM 分析
     *
     * @param startDate 分析起始日期
     * @param endDate   分析結束日期
     * @return RFM 分析結果
     */
    @Transactional(readOnly = true)
    public RfmAnalysisDto analyzeRfm(LocalDate startDate, LocalDate endDate) {
        log.info("執行 RFM 分析: {} ~ {}", startDate, endDate);

        LocalDate today = LocalDate.now();

        // 取得所有客戶
        List<Customer> allCustomers = customerRepository.findAll();

        // 計算每個客戶的 RFM 值
        List<CustomerRfm> customerRfms = new ArrayList<>();

        for (Customer customer : allCustomers) {
            CustomerRfm rfm = calculateCustomerRfm(customer, startDate, endDate, today);
            if (rfm != null && rfm.getFrequency() > 0) {
                customerRfms.add(rfm);
            }
        }

        // 計算分群統計
        Map<String, Integer> segmentCounts = customerRfms.stream()
                .filter(rfm -> rfm.getSegment() != null)
                .collect(Collectors.groupingBy(
                        rfm -> rfm.getSegment().name(),
                        Collectors.summingInt(e -> 1)
                ));

        // 產生分群摘要
        List<SegmentSummary> segmentSummaries = generateSegmentSummaries(customerRfms);

        // 按 RFM 分數排序（高到低）
        customerRfms.sort((a, b) -> {
            int aScore = Integer.parseInt(a.getRfmScore());
            int bScore = Integer.parseInt(b.getRfmScore());
            return Integer.compare(bScore, aScore);
        });

        return RfmAnalysisDto.builder()
                .analysisDate(today)
                .startDate(startDate)
                .endDate(endDate)
                .totalCustomers(allCustomers.size())
                .analyzedCustomers(customerRfms.size())
                .segmentCounts(segmentCounts)
                .customers(customerRfms)
                .segmentSummaries(segmentSummaries)
                .build();
    }

    private CustomerRfm calculateCustomerRfm(
            Customer customer,
            LocalDate startDate,
            LocalDate endDate,
            LocalDate today) {

        Long customerId = customer.getId();

        // 查詢客戶訂單資料
        List<Object[]> orderData = orderRepository.findCustomerOrderStats(customerId, startDate, endDate);

        if (orderData.isEmpty() || orderData.get(0)[0] == null) {
            return null;
        }

        Object[] stats = orderData.get(0);
        LocalDate lastOrderDate = (LocalDate) stats[0];
        Long orderCount = (Long) stats[1];
        BigDecimal totalAmount = stats[2] != null ? (BigDecimal) stats[2] : BigDecimal.ZERO;

        // 計算 Recency（最近購買距今天數）
        int recencyDays = (int) ChronoUnit.DAYS.between(lastOrderDate, today);

        // 計算 RFM 分數
        int recencyScore = calculateRecencyScore(recencyDays);
        int frequencyScore = calculateFrequencyScore(orderCount.intValue());
        int monetaryScore = calculateMonetaryScore(totalAmount);

        String rfmScore = String.format("%d%d%d", recencyScore, frequencyScore, monetaryScore);

        // 確定客戶分群
        CustomerSegment segment = determineSegment(recencyScore, frequencyScore, monetaryScore);

        return CustomerRfm.builder()
                .customerId(customerId)
                .customerName(customer.getName())
                .customerCode(customer.getMemberNo())
                .email(customer.getEmail())
                .phone(customer.getPhone())
                .lastPurchaseDate(lastOrderDate)
                .recencyDays(recencyDays)
                .frequency(orderCount.intValue())
                .monetary(totalAmount)
                .recencyScore(recencyScore)
                .frequencyScore(frequencyScore)
                .monetaryScore(monetaryScore)
                .rfmScore(rfmScore)
                .segment(segment)
                .build();
    }

    private int calculateRecencyScore(int days) {
        if (days <= RECENCY_BREAKS[0]) return 5;
        if (days <= RECENCY_BREAKS[1]) return 4;
        if (days <= RECENCY_BREAKS[2]) return 3;
        if (days <= RECENCY_BREAKS[3]) return 2;
        return 1;
    }

    private int calculateFrequencyScore(int frequency) {
        if (frequency >= FREQUENCY_BREAKS[3]) return 5;
        if (frequency >= FREQUENCY_BREAKS[2]) return 4;
        if (frequency >= FREQUENCY_BREAKS[1]) return 3;
        if (frequency >= FREQUENCY_BREAKS[0]) return 2;
        return 1;
    }

    private int calculateMonetaryScore(BigDecimal monetary) {
        if (monetary.compareTo(MONETARY_BREAKS[3]) >= 0) return 5;
        if (monetary.compareTo(MONETARY_BREAKS[2]) >= 0) return 4;
        if (monetary.compareTo(MONETARY_BREAKS[1]) >= 0) return 3;
        if (monetary.compareTo(MONETARY_BREAKS[0]) >= 0) return 2;
        return 1;
    }

    private CustomerSegment determineSegment(int r, int f, int m) {
        // 根據 RFM 分數組合確定分群
        int avgFM = (f + m) / 2;

        if (r >= 4 && avgFM >= 4) {
            return CustomerSegment.CHAMPIONS;
        }
        if (r >= 3 && avgFM >= 3 && avgFM < 4) {
            return CustomerSegment.LOYAL_CUSTOMERS;
        }
        if (r >= 4 && avgFM >= 2 && avgFM < 3) {
            return CustomerSegment.POTENTIAL_LOYALIST;
        }
        if (r >= 4 && f == 1) {
            return CustomerSegment.NEW_CUSTOMERS;
        }
        if (r >= 3 && avgFM < 2) {
            return CustomerSegment.PROMISING;
        }
        if (r == 3 && avgFM >= 2 && avgFM < 4) {
            return CustomerSegment.NEEDS_ATTENTION;
        }
        if (r == 2 && avgFM >= 2) {
            return CustomerSegment.ABOUT_TO_SLEEP;
        }
        if (r <= 2 && avgFM >= 4) {
            return CustomerSegment.CANT_LOSE_THEM;
        }
        if (r <= 2 && avgFM >= 2 && avgFM < 4) {
            return CustomerSegment.AT_RISK;
        }
        if (r == 2 && avgFM < 2) {
            return CustomerSegment.HIBERNATING;
        }
        return CustomerSegment.LOST;
    }

    private List<SegmentSummary> generateSegmentSummaries(List<CustomerRfm> customerRfms) {
        Map<CustomerSegment, List<CustomerRfm>> grouped = customerRfms.stream()
                .filter(rfm -> rfm.getSegment() != null)
                .collect(Collectors.groupingBy(CustomerRfm::getSegment));

        int totalCustomers = customerRfms.size();

        List<SegmentSummary> summaries = new ArrayList<>();

        for (CustomerSegment segment : CustomerSegment.values()) {
            List<CustomerRfm> segmentCustomers = grouped.getOrDefault(segment, new ArrayList<>());

            if (segmentCustomers.isEmpty()) continue;

            int count = segmentCustomers.size();
            BigDecimal percentage = totalCustomers > 0 ?
                    BigDecimal.valueOf(count * 100.0 / totalCustomers).setScale(2, RoundingMode.HALF_UP) :
                    BigDecimal.ZERO;

            BigDecimal avgRecency = segmentCustomers.stream()
                    .mapToInt(CustomerRfm::getRecencyDays)
                    .average()
                    .stream()
                    .mapToObj(d -> BigDecimal.valueOf(d).setScale(1, RoundingMode.HALF_UP))
                    .findFirst()
                    .orElse(BigDecimal.ZERO);

            BigDecimal avgFrequency = segmentCustomers.stream()
                    .mapToInt(CustomerRfm::getFrequency)
                    .average()
                    .stream()
                    .mapToObj(d -> BigDecimal.valueOf(d).setScale(1, RoundingMode.HALF_UP))
                    .findFirst()
                    .orElse(BigDecimal.ZERO);

            BigDecimal avgMonetary = segmentCustomers.stream()
                    .map(CustomerRfm::getMonetary)
                    .reduce(BigDecimal.ZERO, BigDecimal::add)
                    .divide(BigDecimal.valueOf(count), 2, RoundingMode.HALF_UP);

            BigDecimal totalRevenue = segmentCustomers.stream()
                    .map(CustomerRfm::getMonetary)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            String recommendation = getSegmentRecommendation(segment);

            summaries.add(SegmentSummary.builder()
                    .segment(segment)
                    .customerCount(count)
                    .percentage(percentage)
                    .avgRecency(avgRecency)
                    .avgFrequency(avgFrequency)
                    .avgMonetary(avgMonetary)
                    .totalRevenue(totalRevenue)
                    .recommendation(recommendation)
                    .build());
        }

        // 按客戶數排序
        summaries.sort((a, b) -> Integer.compare(b.getCustomerCount(), a.getCustomerCount()));

        return summaries;
    }

    private String getSegmentRecommendation(CustomerSegment segment) {
        return switch (segment) {
            case CHAMPIONS -> "給予獨家優惠和VIP服務，維持高滿意度";
            case LOYAL_CUSTOMERS -> "提供會員專屬折扣，增加購買頻率";
            case POTENTIAL_LOYALIST -> "提供首購優惠和新品推薦";
            case NEW_CUSTOMERS -> "發送歡迎郵件，介紹品牌和熱銷商品";
            case PROMISING -> "發送促銷資訊，提高購買意願";
            case NEEDS_ATTENTION -> "發送專屬優惠券，喚醒購買行為";
            case ABOUT_TO_SLEEP -> "發送限時優惠，挽回流失客戶";
            case AT_RISK -> "提供大額折扣，進行電話回訪";
            case CANT_LOSE_THEM -> "主動聯繫了解原因，提供專屬優惠";
            case HIBERNATING -> "發送喚醒郵件，提供回購獎勵";
            case LOST -> "進行客戶流失調查，嘗試重新激活";
        };
    }
}

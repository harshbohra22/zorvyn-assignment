package com.finance.service;

import com.finance.dto.response.CategorySummary;
import com.finance.dto.response.DashboardSummary;
import com.finance.dto.response.MonthlyTrend;
import com.finance.dto.response.RecordResponse;
import com.finance.entity.FinancialRecord;
import com.finance.enums.RecordType;
import com.finance.repository.RecordRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DashboardService {

    private final RecordRepository recordRepository;

    public DashboardSummary getSummary() {
        BigDecimal totalIncome = recordRepository.sumByType(RecordType.INCOME);
        BigDecimal totalExpenses = recordRepository.sumByType(RecordType.EXPENSE);
        BigDecimal netBalance = totalIncome.subtract(totalExpenses);

        return DashboardSummary.builder()
                .totalIncome(totalIncome)
                .totalExpenses(totalExpenses)
                .netBalance(netBalance)
                .build();
    }

    public List<CategorySummary> getCategorySummary() {
        List<Object[]> incomeByCategory = recordRepository.sumByCategoryAndType(RecordType.INCOME);
        List<Object[]> expenseByCategory = recordRepository.sumByCategoryAndType(RecordType.EXPENSE);

        Map<String, BigDecimal> incomeMap = new LinkedHashMap<>();
        for (Object[] row : incomeByCategory) {
            incomeMap.put((String) row[0], (BigDecimal) row[1]);
        }

        Map<String, BigDecimal> expenseMap = new LinkedHashMap<>();
        for (Object[] row : expenseByCategory) {
            expenseMap.put((String) row[0], (BigDecimal) row[1]);
        }

        Set<String> allCategories = new LinkedHashSet<>();
        allCategories.addAll(incomeMap.keySet());
        allCategories.addAll(expenseMap.keySet());

        return allCategories.stream()
                .map(category -> {
                    BigDecimal income = incomeMap.getOrDefault(category, BigDecimal.ZERO);
                    BigDecimal expense = expenseMap.getOrDefault(category, BigDecimal.ZERO);
                    return CategorySummary.builder()
                            .category(category)
                            .totalIncome(income)
                            .totalExpense(expense)
                            .net(income.subtract(expense))
                            .build();
                })
                .collect(Collectors.toList());
    }

    public List<MonthlyTrend> getMonthlyTrends() {
        List<Object[]> rawTrends = recordRepository.getMonthlyTrends();

        Map<String, MonthlyTrend> trendMap = new LinkedHashMap<>();

        for (Object[] row : rawTrends) {
            int year = ((Number) row[0]).intValue();
            int month = ((Number) row[1]).intValue();
            RecordType type = RecordType.valueOf((String) row[2]);
            BigDecimal amount = (BigDecimal) row[3];

            String key = year + "-" + month;
            MonthlyTrend trend = trendMap.computeIfAbsent(key, k ->
                    MonthlyTrend.builder()
                            .year(year)
                            .month(month)
                            .income(BigDecimal.ZERO)
                            .expense(BigDecimal.ZERO)
                            .net(BigDecimal.ZERO)
                            .build()
            );

            if (type == RecordType.INCOME) {
                trend.setIncome(trend.getIncome().add(amount));
            } else {
                trend.setExpense(trend.getExpense().add(amount));
            }
            trend.setNet(trend.getIncome().subtract(trend.getExpense()));
        }

        return new ArrayList<>(trendMap.values());
    }

    public List<RecordResponse> getRecentActivity() {
        return recordRepository.findTop10ByIsDeletedFalseOrderByDateDescCreatedAtDesc()
                .stream()
                .map(record -> RecordResponse.builder()
                        .id(record.getId())
                        .type(record.getType())
                        .category(record.getCategory())
                        .amount(record.getAmount())
                        .date(record.getDate())
                        .description(record.getDescription())
                        .userId(record.getUserId())
                        .createdAt(record.getCreatedAt())
                        .updatedAt(record.getUpdatedAt())
                        .build())
                .collect(Collectors.toList());
    }
}

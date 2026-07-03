package com.expensetracker.dto.response;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

public record AnalyticsSummaryResponse(
    BigDecimal totalIncome,
    BigDecimal totalExpense,
    BigDecimal netBalance,
    BigDecimal savingsRate,
    List<CategoryBreakdown> topExpenseCategories,
    List<MonthlyTrend> monthlyTrend,
    Map<String, BigDecimal> incomeVsExpenseCurrentMonth
) {
    public record CategoryBreakdown(
        String categoryName,
        String categoryIcon,
        BigDecimal amount,
        BigDecimal percentage
    ) {}

    public record MonthlyTrend(
        int year,
        int month,
        String monthLabel,
        BigDecimal income,
        BigDecimal expense
    ) {}
}

package com.expensetracker.service.impl;

import com.expensetracker.dto.response.AnalyticsSummaryResponse;
import com.expensetracker.dto.response.AnalyticsSummaryResponse.CategoryBreakdown;
import com.expensetracker.dto.response.AnalyticsSummaryResponse.MonthlyTrend;
import com.expensetracker.entity.Transaction;
import com.expensetracker.entity.TransactionType;
import com.expensetracker.entity.User;
import com.expensetracker.repository.TransactionRepository;
import com.expensetracker.service.AnalyticsService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.Month;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class AnalyticsServiceImpl implements AnalyticsService {

    private static final Logger log = LoggerFactory.getLogger(AnalyticsServiceImpl.class);

    @Autowired
    private TransactionRepository transactionRepository;

    @Override
    @Transactional(readOnly = true)
    public AnalyticsSummaryResponse getSummary(User user, int months) {
        LocalDate endDate = LocalDate.now();
        LocalDate startDate = endDate.minusMonths(months).withDayOfMonth(1);

        List<Transaction> transactions = transactionRepository
                .findByUserIdAndTransactionDateBetween(user.getId(), startDate, endDate);

        BigDecimal totalIncome = sumByType(transactions, TransactionType.INCOME);
        BigDecimal totalExpense = sumByType(transactions, TransactionType.EXPENSE);
        BigDecimal netBalance = totalIncome.subtract(totalExpense);

        BigDecimal savingsRate = totalIncome.compareTo(BigDecimal.ZERO) > 0
                ? netBalance.divide(totalIncome, 4, RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(100))
                : BigDecimal.ZERO;

        List<CategoryBreakdown> topExpenseCategories = buildCategoryBreakdown(transactions, totalExpense);
        List<MonthlyTrend> monthlyTrend = buildMonthlyTrend(transactions, months);

        LocalDate monthStart = endDate.withDayOfMonth(1);
        List<Transaction> currentMonthTxs = transactions.stream()
                .filter(t -> !t.getTransactionDate().isBefore(monthStart))
                .toList();

        Map<String, BigDecimal> incomeVsExpense = new HashMap<>();
        incomeVsExpense.put("income", sumByType(currentMonthTxs, TransactionType.INCOME));
        incomeVsExpense.put("expense", sumByType(currentMonthTxs, TransactionType.EXPENSE));

        return new AnalyticsSummaryResponse(
                totalIncome,
                totalExpense,
                netBalance,
                savingsRate.setScale(2, RoundingMode.HALF_UP),
                topExpenseCategories,
                monthlyTrend,
                incomeVsExpense
        );
    }

    private BigDecimal sumByType(List<Transaction> transactions, TransactionType type) {
        return transactions.stream()
                .filter(t -> t.getType() == type)
                .map(Transaction::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private List<CategoryBreakdown> buildCategoryBreakdown(List<Transaction> transactions, BigDecimal totalExpense) {
        Map<String, BigDecimal> byCategory = transactions.stream()
                .filter(t -> t.getType() == TransactionType.EXPENSE)
                .collect(Collectors.groupingBy(
                        t -> t.getCategory().getName(),
                        Collectors.reducing(BigDecimal.ZERO, Transaction::getAmount, BigDecimal::add)
                ));

        Map<String, String> categoryIcons = transactions.stream()
                .filter(t -> t.getType() == TransactionType.EXPENSE)
                .collect(Collectors.toMap(
                        t -> t.getCategory().getName(),
                        t -> t.getCategory().getIcon() != null ? t.getCategory().getIcon() : "💰",
                        (a, b) -> a
                ));

        return byCategory.entrySet().stream()
                .sorted(Map.Entry.<String, BigDecimal>comparingByValue().reversed())
                .limit(5)
                .map(entry -> {
                    BigDecimal pct = totalExpense.compareTo(BigDecimal.ZERO) > 0
                            ? entry.getValue().divide(totalExpense, 4, RoundingMode.HALF_UP)
                                    .multiply(BigDecimal.valueOf(100)).setScale(2, RoundingMode.HALF_UP)
                            : BigDecimal.ZERO;
                    return new CategoryBreakdown(
                            entry.getKey(),
                            categoryIcons.getOrDefault(entry.getKey(), "💰"),
                            entry.getValue(),
                            pct
                    );
                })
                .toList();
    }

    private List<MonthlyTrend> buildMonthlyTrend(List<Transaction> transactions, int months) {
        List<MonthlyTrend> trend = new ArrayList<>();
        LocalDate cursor = LocalDate.now().withDayOfMonth(1).minusMonths(months - 1L);

        for (int i = 0; i < months; i++) {
            final LocalDate periodStart = cursor;
            final LocalDate periodEnd = cursor.withDayOfMonth(cursor.lengthOfMonth());

            List<Transaction> period = transactions.stream()
                    .filter(t -> !t.getTransactionDate().isBefore(periodStart)
                              && !t.getTransactionDate().isAfter(periodEnd))
                    .toList();

            String monthLabel = Month.of(cursor.getMonthValue())
                    .getDisplayName(TextStyle.SHORT, Locale.ENGLISH) + " " + cursor.getYear();

            trend.add(new MonthlyTrend(
                    cursor.getYear(),
                    cursor.getMonthValue(),
                    monthLabel,
                    sumByType(period, TransactionType.INCOME),
                    sumByType(period, TransactionType.EXPENSE)
            ));

            cursor = cursor.plusMonths(1);
        }

        return trend;
    }
}

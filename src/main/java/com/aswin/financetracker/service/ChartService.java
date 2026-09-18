package com.aswin.financetracker.service;

import com.aswin.financetracker.dto.ChartDataDto;
import com.aswin.financetracker.model.enums.ExpenseCategory;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class ChartService {

    private final IncomeService incomeService;
    private final ExpenseService expenseService;

    public ChartService(IncomeService incomeService, ExpenseService expenseService) {
        this.incomeService = incomeService;
        this.expenseService = expenseService;
    }

    public ChartDataDto.CategoryPieChart getExpenseByCategoryChart(LocalDate startDate, LocalDate endDate) {
        if (startDate == null || endDate == null) {
            YearMonth now = YearMonth.now();
            startDate = now.atDay(1);
            endDate = now.atEndOfMonth();
        }

        List<Object[]> results = expenseService.getCategorySpending(startDate, endDate);
        List<String> labels = new ArrayList<>();
        List<BigDecimal> data = new ArrayList<>();
        List<String> colors = new ArrayList<>();

        for (Object[] row : results) {
            ExpenseCategory category = (ExpenseCategory) row[0];
            BigDecimal amount = (BigDecimal) row[1];
            if (amount != null && amount.compareTo(BigDecimal.ZERO) > 0) {
                labels.add(category.getDisplayName());
                data.add(amount);
                colors.add(category.getColorHex());
            }
        }

        return new ChartDataDto.CategoryPieChart(labels, data, colors);
    }

    public ChartDataDto.IncomeExpenseBarChart getIncomeVsExpenseMonthlyChart(int months) {
        if (months <= 0) months = 12;
        YearMonth current = YearMonth.now();
        List<YearMonth> monthList = new ArrayList<>();
        for (int i = months - 1; i >= 0; i--) {
            monthList.add(current.minusMonths(i));
        }

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMM yyyy");
        List<String> labels = new ArrayList<>();
        List<BigDecimal> incomeData = new ArrayList<>();
        List<BigDecimal> expenseData = new ArrayList<>();

        for (YearMonth ym : monthList) {
            LocalDate start = ym.atDay(1);
            LocalDate end = ym.atEndOfMonth();

            BigDecimal inc = incomeService.getIncomeBetween(start, end);
            BigDecimal exp = expenseService.getExpenseBetween(start, end);

            labels.add(ym.format(formatter));
            incomeData.add(inc != null ? inc : BigDecimal.ZERO);
            expenseData.add(exp != null ? exp : BigDecimal.ZERO);
        }

        return new ChartDataDto.IncomeExpenseBarChart(labels, incomeData, expenseData);
    }

    public ChartDataDto.SavingsTrendLineChart getSavingsTrendChart(int months) {
        if (months <= 0) months = 12;
        YearMonth current = YearMonth.now();
        List<YearMonth> monthList = new ArrayList<>();
        for (int i = months - 1; i >= 0; i--) {
            monthList.add(current.minusMonths(i));
        }

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMM yyyy");
        List<String> labels = new ArrayList<>();
        List<BigDecimal> savingsData = new ArrayList<>();

        for (YearMonth ym : monthList) {
            LocalDate start = ym.atDay(1);
            LocalDate end = ym.atEndOfMonth();

            BigDecimal inc = incomeService.getIncomeBetween(start, end);
            BigDecimal exp = expenseService.getExpenseBetween(start, end);

            BigDecimal safeInc = inc != null ? inc : BigDecimal.ZERO;
            BigDecimal safeExp = exp != null ? exp : BigDecimal.ZERO;

            labels.add(ym.format(formatter));
            savingsData.add(safeInc.subtract(safeExp));
        }

        return new ChartDataDto.SavingsTrendLineChart(labels, savingsData);
    }
}

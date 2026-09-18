package com.aswin.financetracker.controller;

import com.aswin.financetracker.dto.ChartDataDto;
import com.aswin.financetracker.service.ChartService;
import java.time.LocalDate;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/charts")
public class ChartApiController {

    private final ChartService chartService;

    public ChartApiController(ChartService chartService) {
        this.chartService = chartService;
    }

    @GetMapping("/expense-by-category")
    public ChartDataDto.CategoryPieChart getExpenseByCategory(
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate
    ) {
        return chartService.getExpenseByCategoryChart(startDate, endDate);
    }

    @GetMapping("/income-vs-expense")
    public ChartDataDto.IncomeExpenseBarChart getIncomeVsExpense(
        @RequestParam(defaultValue = "12") int months
    ) {
        return chartService.getIncomeVsExpenseMonthlyChart(months);
    }

    @GetMapping("/savings-trend")
    public ChartDataDto.SavingsTrendLineChart getSavingsTrend(
        @RequestParam(defaultValue = "12") int months
    ) {
        return chartService.getSavingsTrendChart(months);
    }
}

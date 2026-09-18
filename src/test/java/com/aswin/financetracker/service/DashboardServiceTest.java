package com.aswin.financetracker.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.aswin.financetracker.dto.DashboardSummaryDto;
import java.math.BigDecimal;
import java.time.LocalDate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class DashboardServiceTest {

    @Mock
    private IncomeService incomeService;

    @Mock
    private ExpenseService expenseService;

    private DashboardService dashboardService;

    @BeforeEach
    void setUp() {
        dashboardService = new DashboardService(incomeService, expenseService);
    }

    @Test
    @DisplayName("Should correctly calculate totals, available balance and month-over-month differences")
    void testDashboardSummaryCalculations() {
        LocalDate refDate = LocalDate.of(2026, 9, 15);

        when(incomeService.getTotalIncome()).thenReturn(new BigDecimal("10000.00"));
        when(expenseService.getTotalExpense()).thenReturn(new BigDecimal("4000.00"));

        // Current month: Sept 2026
        when(incomeService.getIncomeBetween(LocalDate.of(2026, 9, 1), LocalDate.of(2026, 9, 30)))
            .thenReturn(new BigDecimal("5000.00"));
        when(expenseService.getExpenseBetween(LocalDate.of(2026, 9, 1), LocalDate.of(2026, 9, 30)))
            .thenReturn(new BigDecimal("2000.00"));

        // Last month: Aug 2026
        when(incomeService.getIncomeBetween(LocalDate.of(2026, 8, 1), LocalDate.of(2026, 8, 31)))
            .thenReturn(new BigDecimal("4000.00"));
        when(expenseService.getExpenseBetween(LocalDate.of(2026, 8, 1), LocalDate.of(2026, 8, 31)))
            .thenReturn(new BigDecimal("1500.00"));

        DashboardSummaryDto summary = dashboardService.getDashboardSummaryForDate(refDate);

        assertNotNull(summary);
        assertEquals(new BigDecimal("10000.00"), summary.getTotalIncome());
        assertEquals(new BigDecimal("4000.00"), summary.getTotalExpense());
        assertEquals(new BigDecimal("6000.00"), summary.getAvailableBalance());

        assertEquals(new BigDecimal("5000.00"), summary.getThisMonthIncome());
        assertEquals(new BigDecimal("2000.00"), summary.getThisMonthExpense());
        assertEquals(new BigDecimal("3000.00"), summary.getThisMonthSavings());

        assertEquals(new BigDecimal("4000.00"), summary.getLastMonthIncome());
        assertEquals(new BigDecimal("1500.00"), summary.getLastMonthExpense());
        assertEquals(new BigDecimal("2500.00"), summary.getLastMonthSavings());

        // MoM income: (5000 - 4000) / 4000 * 100 = 25.00%
        assertEquals(new BigDecimal("25.00"), summary.getIncomeMoMChangePercent());
        // MoM expense: (2000 - 1500) / 1500 * 100 = 33.33%
        assertEquals(new BigDecimal("33.33"), summary.getExpenseMoMChangePercent());
        // MoM savings: (3000 - 2500) / 2500 * 100 = 20.00%
        assertEquals(new BigDecimal("20.00"), summary.getSavingsMoMChangePercent());
    }

    @Test
    @DisplayName("Should handle edge cases in percentage change calculation")
    void testPercentageChangeEdgeCases() {
        // Zero previous, positive current
        BigDecimal pct1 = dashboardService.calculatePercentageChange(BigDecimal.ZERO, new BigDecimal("100.00"));
        assertEquals(new BigDecimal("100.00"), pct1);

        // Zero previous, negative current
        BigDecimal pct2 = dashboardService.calculatePercentageChange(BigDecimal.ZERO, new BigDecimal("-50.00"));
        assertEquals(new BigDecimal("-100.00"), pct2);

        // Both zero
        BigDecimal pct3 = dashboardService.calculatePercentageChange(BigDecimal.ZERO, BigDecimal.ZERO);
        assertEquals(new BigDecimal("0.00"), pct3);

        // Normal 50% decrease
        BigDecimal pct4 = dashboardService.calculatePercentageChange(new BigDecimal("200.00"), new BigDecimal("100.00"));
        assertEquals(new BigDecimal("-50.00"), pct4);
    }
}

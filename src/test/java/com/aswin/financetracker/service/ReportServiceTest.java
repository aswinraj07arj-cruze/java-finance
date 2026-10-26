package com.aswin.financetracker.service;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.aswin.financetracker.dto.TransactionFilterDto;
import com.aswin.financetracker.dto.TransactionViewDto;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

@ExtendWith(MockitoExtension.class)
class ReportServiceTest {

    @Mock
    private TransactionService transactionService;

    @Mock
    private IncomeService incomeService;

    @Mock
    private ExpenseService expenseService;

    private ReportService reportService;

    @BeforeEach
    void setUp() {
        reportService = new ReportService(transactionService, incomeService, expenseService);
    }

    @Test
    @DisplayName("Should generate monthly report data with correct summary metrics")
    void testGetMonthlyReportData() {
        YearMonth ym = YearMonth.of(2026, 9);
        LocalDate start = ym.atDay(1);
        LocalDate end = ym.atEndOfMonth();

        when(incomeService.getIncomeBetween(start, end)).thenReturn(new BigDecimal("5000.00"));
        when(expenseService.getExpenseBetween(start, end)).thenReturn(new BigDecimal("3200.00"));

        TransactionViewDto tx = new TransactionViewDto(1L, "EXPENSE", new BigDecimal("50.00"), start, "Food", "FOOD", "Groceries", "", "");
        when(transactionService.getUnifiedTransactions(any(TransactionFilterDto.class)))
            .thenReturn(new PageImpl<>(List.of(tx), PageRequest.of(0, 1000), 1));

        Map<String, Object> data = reportService.getMonthlyReportData(ym);

        assertNotNull(data);
        assertTrue(new BigDecimal("5000.00").compareTo((BigDecimal) data.get("totalIncome")) == 0);
        assertTrue(new BigDecimal("3200.00").compareTo((BigDecimal) data.get("totalExpense")) == 0);
        assertTrue(new BigDecimal("1800.00").compareTo((BigDecimal) data.get("netSavings")) == 0);
    }

    @Test
    @DisplayName("Should generate valid CSV report with RFC 4180 headers and summary rows")
    void testGenerateCsvReport() throws IOException {
        YearMonth ym = YearMonth.of(2026, 9);
        LocalDate start = ym.atDay(1);
        LocalDate end = ym.atEndOfMonth();

        when(incomeService.getIncomeBetween(start, end)).thenReturn(new BigDecimal("5000.00"));
        when(expenseService.getExpenseBetween(start, end)).thenReturn(new BigDecimal("2000.00"));

        TransactionViewDto tx = new TransactionViewDto(1L, "INCOME", new BigDecimal("5000.00"), start, "Salary", "SALARY", "Monthly paycheck", "", "");
        when(transactionService.getUnifiedTransactions(any(TransactionFilterDto.class)))
            .thenReturn(new PageImpl<>(List.of(tx), PageRequest.of(0, 10000), 1));

        byte[] csvBytes = reportService.generateCsvReport(ym);
        String csvContent = new String(csvBytes, StandardCharsets.UTF_8);

        assertTrue(csvContent.contains("Date,Type,Category/Source,Description/Note,Amount"));
        assertTrue(csvContent.contains("2026-09-01,INCOME,Salary,Monthly paycheck,5000.00"));
        assertTrue(csvContent.contains("--- SUMMARY ---"));
        assertTrue(csvContent.contains("Total Income,,,,5000.00"));
        assertTrue(csvContent.contains("Total Expense,,,,2000.00"));
        assertTrue(csvContent.contains("Net Savings,,,,3000.00"));
    }
}


package com.aswin.financetracker.service;

import com.aswin.financetracker.dto.TransactionFilterDto;
import com.aswin.financetracker.dto.TransactionViewDto;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class ReportService {

    private final TransactionService transactionService;
    private final IncomeService incomeService;
    private final ExpenseService expenseService;

    public ReportService(TransactionService transactionService, IncomeService incomeService, ExpenseService expenseService) {
        this.transactionService = transactionService;
        this.incomeService = incomeService;
        this.expenseService = expenseService;
    }

    public Map<String, Object> getMonthlyReportData(YearMonth yearMonth) {
        LocalDate startDate = yearMonth.atDay(1);
        LocalDate endDate = yearMonth.atEndOfMonth();

        BigDecimal totalIncome = incomeService.getIncomeBetween(startDate, endDate);
        BigDecimal totalExpense = expenseService.getExpenseBetween(startDate, endDate);

        BigDecimal safeIncome = totalIncome != null ? totalIncome : BigDecimal.ZERO;
        BigDecimal safeExpense = totalExpense != null ? totalExpense : BigDecimal.ZERO;
        BigDecimal netSavings = safeIncome.subtract(safeExpense);

        TransactionFilterDto filter = new TransactionFilterDto();
        filter.setStartDate(startDate);
        filter.setEndDate(endDate);
        filter.setSize(1000); // Fetch all for reporting
        Page<TransactionViewDto> page = transactionService.getUnifiedTransactions(filter);

        Map<String, Object> report = new HashMap<>();
        report.put("yearMonth", yearMonth);
        report.put("monthName", yearMonth.format(DateTimeFormatter.ofPattern("MMMM yyyy")));
        report.put("totalIncome", safeIncome);
        report.put("totalExpense", safeExpense);
        report.put("netSavings", netSavings);
        report.put("transactions", page.getContent());
        report.put("transactionCount", page.getTotalElements());

        return report;
    }

    public byte[] generateCsvReport(YearMonth yearMonth) throws IOException {
        LocalDate startDate = yearMonth.atDay(1);
        LocalDate endDate = yearMonth.atEndOfMonth();

        TransactionFilterDto filter = new TransactionFilterDto();
        filter.setStartDate(startDate);
        filter.setEndDate(endDate);
        filter.setSize(10000); // Export all transactions for month
        List<TransactionViewDto> transactions = transactionService.getUnifiedTransactions(filter).getContent();

        BigDecimal totalIncome = incomeService.getIncomeBetween(startDate, endDate);
        BigDecimal totalExpense = expenseService.getExpenseBetween(startDate, endDate);
        BigDecimal safeIncome = totalIncome != null ? totalIncome : BigDecimal.ZERO;
        BigDecimal safeExpense = totalExpense != null ? totalExpense : BigDecimal.ZERO;
        BigDecimal netSavings = safeIncome.subtract(safeExpense);

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try (CSVPrinter printer = new CSVPrinter(
            new PrintWriter(new OutputStreamWriter(out, StandardCharsets.UTF_8)),
            CSVFormat.DEFAULT.builder()
                .setHeader("Date", "Type", "Category/Source", "Description/Note", "Amount")
                .build()
        )) {
            for (TransactionViewDto tx : transactions) {
                printer.printRecord(
                    tx.getDate().toString(),
                    tx.getType(),
                    tx.getCategoryOrSource(),
                    tx.getDescriptionOrNote(),
                    tx.getAmount().toString()
                );
            }

            // Summary rows
            printer.println();
            printer.printRecord("--- SUMMARY ---", "", "", "", "");
            printer.printRecord("Total Income", "", "", "", safeIncome.toString());
            printer.printRecord("Total Expense", "", "", "", safeExpense.toString());
            printer.printRecord("Net Savings", "", "", "", netSavings.toString());

            printer.flush();
        }

        return out.toByteArray();
    }
}

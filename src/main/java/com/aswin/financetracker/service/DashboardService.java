package com.aswin.financetracker.service;

import com.aswin.financetracker.dto.DashboardSummaryDto;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.TextStyle;
import java.util.Locale;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class DashboardService {

    private final IncomeService incomeService;
    private final ExpenseService expenseService;

    public DashboardService(IncomeService incomeService, ExpenseService expenseService) {
        this.incomeService = incomeService;
        this.expenseService = expenseService;
    }

    public DashboardSummaryDto getDashboardSummary() {
        return getDashboardSummaryForDate(LocalDate.now());
    }

    public DashboardSummaryDto getDashboardSummaryForDate(LocalDate referenceDate) {
        DashboardSummaryDto summary = new DashboardSummaryDto();

        BigDecimal totalIncome = incomeService.getTotalIncome();
        BigDecimal totalExpense = expenseService.getTotalExpense();
        summary.setTotalIncome(totalIncome != null ? totalIncome : BigDecimal.ZERO);
        summary.setTotalExpense(totalExpense != null ? totalExpense : BigDecimal.ZERO);
        summary.setAvailableBalance(summary.getTotalIncome().subtract(summary.getTotalExpense()));

        YearMonth currentMonth = YearMonth.from(referenceDate);
        LocalDate startCurrent = currentMonth.atDay(1);
        LocalDate endCurrent = currentMonth.atEndOfMonth();

        YearMonth lastMonth = currentMonth.minusMonths(1);
        LocalDate startLast = lastMonth.atDay(1);
        LocalDate endLast = lastMonth.atEndOfMonth();

        summary.setCurrentMonthName(currentMonth.getMonth().getDisplayName(TextStyle.FULL, Locale.ENGLISH) + " " + currentMonth.getYear());
        summary.setLastMonthName(lastMonth.getMonth().getDisplayName(TextStyle.FULL, Locale.ENGLISH) + " " + lastMonth.getYear());

        BigDecimal thisMonthInc = incomeService.getIncomeBetween(startCurrent, endCurrent);
        BigDecimal thisMonthExp = expenseService.getExpenseBetween(startCurrent, endCurrent);
        thisMonthInc = thisMonthInc != null ? thisMonthInc : BigDecimal.ZERO;
        thisMonthExp = thisMonthExp != null ? thisMonthExp : BigDecimal.ZERO;
        BigDecimal thisMonthSav = thisMonthInc.subtract(thisMonthExp);

        summary.setThisMonthIncome(thisMonthInc);
        summary.setThisMonthExpense(thisMonthExp);
        summary.setThisMonthSavings(thisMonthSav);

        BigDecimal lastMonthInc = incomeService.getIncomeBetween(startLast, endLast);
        BigDecimal lastMonthExp = expenseService.getExpenseBetween(startLast, endLast);
        lastMonthInc = lastMonthInc != null ? lastMonthInc : BigDecimal.ZERO;
        lastMonthExp = lastMonthExp != null ? lastMonthExp : BigDecimal.ZERO;
        BigDecimal lastMonthSav = lastMonthInc.subtract(lastMonthExp);

        summary.setLastMonthIncome(lastMonthInc);
        summary.setLastMonthExpense(lastMonthExp);
        summary.setLastMonthSavings(lastMonthSav);

        summary.setIncomeMoMChangePercent(calculatePercentageChange(lastMonthInc, thisMonthInc));
        summary.setExpenseMoMChangePercent(calculatePercentageChange(lastMonthExp, thisMonthExp));
        summary.setSavingsMoMChangePercent(calculatePercentageChange(lastMonthSav, thisMonthSav));

        return summary;
    }

    public BigDecimal calculatePercentageChange(BigDecimal previous, BigDecimal current) {
        if (previous == null) previous = BigDecimal.ZERO;
        if (current == null) current = BigDecimal.ZERO;

        if (previous.compareTo(BigDecimal.ZERO) == 0) {
            if (current.compareTo(BigDecimal.ZERO) > 0) {
                return BigDecimal.valueOf(100.00).setScale(2, RoundingMode.HALF_UP);
            } else if (current.compareTo(BigDecimal.ZERO) < 0) {
                return BigDecimal.valueOf(-100.00).setScale(2, RoundingMode.HALF_UP);
            } else {
                return BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
            }
        }

        BigDecimal difference = current.subtract(previous);
        return difference
            .divide(previous.abs(), 4, RoundingMode.HALF_UP)
            .multiply(BigDecimal.valueOf(100))
            .setScale(2, RoundingMode.HALF_UP);
    }
}

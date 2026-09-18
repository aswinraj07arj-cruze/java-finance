package com.aswin.financetracker.integration;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.aswin.financetracker.dto.DashboardSummaryDto;
import com.aswin.financetracker.repository.BudgetRepository;
import com.aswin.financetracker.repository.ExpenseRepository;
import com.aswin.financetracker.repository.IncomeRepository;
import com.aswin.financetracker.repository.SubscriptionRepository;
import com.aswin.financetracker.service.DashboardService;
import java.math.BigDecimal;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;

@SpringBootTest(properties = {
    "spring.datasource.url=jdbc:h2:mem:fresh_db_test;DB_CLOSE_DELAY=-1",
    "spring.jpa.hibernate.ddl-auto=create-drop"
})
@DirtiesContext
class EmptyDatabaseLaunchIntegrationTest {

    @Autowired
    private IncomeRepository incomeRepository;

    @Autowired
    private ExpenseRepository expenseRepository;

    @Autowired
    private BudgetRepository budgetRepository;

    @Autowired
    private SubscriptionRepository subscriptionRepository;

    @Autowired
    private DashboardService dashboardService;

    @Test
    @DisplayName("Empty initial launch: verify 0 dummy records and dashboard shows 0 balance/income/expense")
    void testEmptyLaunchHasNoDummyData() {
        assertEquals(0, incomeRepository.count(), "Income table must be completely empty on initial launch");
        assertEquals(0, expenseRepository.count(), "Expense table must be completely empty on initial launch");
        assertEquals(0, budgetRepository.count(), "Budget table must be completely empty on initial launch");
        assertEquals(0, subscriptionRepository.count(), "Subscription table must be completely empty on initial launch");

        DashboardSummaryDto summary = dashboardService.getDashboardSummary();
        assertEquals(BigDecimal.ZERO, summary.getAvailableBalance(), "Balance must be 0");
        assertEquals(BigDecimal.ZERO, summary.getThisMonthIncome(), "This month income must be 0");
        assertEquals(BigDecimal.ZERO, summary.getThisMonthExpense(), "This month expense must be 0");
        assertEquals(BigDecimal.ZERO, summary.getThisMonthSavings(), "This month savings must be 0");
    }
}

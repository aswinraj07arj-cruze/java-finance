package com.aswin.financetracker.integration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.aswin.financetracker.dto.DashboardSummaryDto;
import com.aswin.financetracker.dto.ExpenseRequestDto;
import com.aswin.financetracker.dto.IncomeRequestDto;
import com.aswin.financetracker.model.Expense;
import com.aswin.financetracker.model.Income;
import com.aswin.financetracker.model.enums.ExpenseCategory;
import com.aswin.financetracker.model.enums.IncomeSource;
import com.aswin.financetracker.repository.BudgetRepository;
import com.aswin.financetracker.repository.ExpenseRepository;
import com.aswin.financetracker.repository.IncomeRepository;
import com.aswin.financetracker.repository.SubscriptionRepository;
import com.aswin.financetracker.service.DashboardService;
import com.aswin.financetracker.service.ExpenseService;
import com.aswin.financetracker.service.IncomeService;
import java.io.File;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;

@SpringBootTest(properties = {
    "spring.datasource.url=jdbc:h2:file:./data/test_persistence_seq;AUTO_SERVER=TRUE",
    "spring.jpa.hibernate.ddl-auto=update"
})
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class DataPersistenceSequenceIntegrationTest {

    @Autowired
    private IncomeService incomeService;

    @Autowired
    private ExpenseService expenseService;

    @Autowired
    private DashboardService dashboardService;

    @Autowired
    private IncomeRepository incomeRepository;

    @Autowired
    private ExpenseRepository expenseRepository;

    @Autowired
    private BudgetRepository budgetRepository;

    @Autowired
    private SubscriptionRepository subscriptionRepository;

    private static Long createdIncomeId;
    private static Long createdExpenseId;

    @BeforeAll
    static void cleanPreviousRunFiles() {
        File f1 = new File("./data/test_persistence_seq.mv.db");
        if (f1.exists()) f1.delete();
        File f2 = new File("./data/test_persistence_seq.lock.db");
        if (f2.exists()) f2.delete();
        File f3 = new File("./data/test_persistence_seq.trace.db");
        if (f3.exists()) f3.delete();
    }

    @Test
    @Order(1)
    @DisplayName("Steps A-E: Start with fresh DB, verify empty state, add 1 income, add 1 expense, verify calculations, then trigger restart")
    @DirtiesContext
    void stepsA_through_E_verifyFreshDbAddRecordsAndCalculations() {
        // Step B: Dashboard should show zero/empty values on fresh database
        assertEquals(0, incomeRepository.count(), "Income table must start empty");
        assertEquals(0, expenseRepository.count(), "Expense table must start empty");
        assertEquals(0, budgetRepository.count(), "Budget table must start empty");
        assertEquals(0, subscriptionRepository.count(), "Subscription table must start empty");

        DashboardSummaryDto initialSummary = dashboardService.getDashboardSummary();
        assertEquals(BigDecimal.ZERO, initialSummary.getAvailableBalance(), "Initial balance must be 0");
        assertEquals(BigDecimal.ZERO, initialSummary.getThisMonthIncome(), "Initial income must be 0");
        assertEquals(BigDecimal.ZERO, initialSummary.getThisMonthExpense(), "Initial expense must be 0");

        // Step C: Add one income through Service/API (5,000.00)
        IncomeRequestDto incDto = new IncomeRequestDto(
            null,
            new BigDecimal("5000.00"),
            IncomeSource.SALARY,
            LocalDate.now(),
            "Sequence Test Salary"
        );
        Income savedIncome = incomeService.createIncome(incDto);
        assertNotNull(savedIncome.getId());
        createdIncomeId = savedIncome.getId();

        // Step D: Add one expense (1,500.00)
        ExpenseRequestDto expDto = new ExpenseRequestDto(
            null,
            new BigDecimal("1500.00"),
            ExpenseCategory.FOOD,
            LocalDate.now(),
            "Sequence Test Groceries"
        );
        Expense savedExpense = expenseService.createExpense(expDto);
        assertNotNull(savedExpense.getId());
        createdExpenseId = savedExpense.getId();

        // Step E: Verify dashboard calculations update correctly
        DashboardSummaryDto updatedSummary = dashboardService.getDashboardSummary();
        assertEquals(new BigDecimal("3500.00"), updatedSummary.getAvailableBalance(), "Balance must be 3500.00");
        assertEquals(new BigDecimal("5000.00"), updatedSummary.getThisMonthIncome(), "Income must be 5000.00");
        assertEquals(new BigDecimal("1500.00"), updatedSummary.getThisMonthExpense(), "Expense must be 1500.00");
        assertEquals(new BigDecimal("3500.00"), updatedSummary.getThisMonthSavings(), "Savings must be 3500.00");

        // Step F: Application will stop and context will be torn down due to @DirtiesContext
    }

    @Test
    @Order(2)
    @DisplayName("Steps G-I: Start application again on same database, verify income/expense present, verify no duplicates")
    void stepsG_through_I_verifyDataSurvivesRestartAndNoDuplicates() {
        // Step G: Application has started again against the same H2 file database

        // Step H: Verify income and expense are still present
        assertNotNull(createdIncomeId);
        assertNotNull(createdExpenseId);

        assertTrue(incomeRepository.findById(createdIncomeId).isPresent(), "Income must survive restart");
        Income survivingIncome = incomeRepository.findById(createdIncomeId).get();
        assertEquals(new BigDecimal("5000.00"), survivingIncome.getAmount());
        assertEquals("Sequence Test Salary", survivingIncome.getNote());

        assertTrue(expenseRepository.findById(createdExpenseId).isPresent(), "Expense must survive restart");
        Expense survivingExpense = expenseRepository.findById(createdExpenseId).get();
        assertEquals(new BigDecimal("1500.00"), survivingExpense.getAmount());
        assertEquals("Sequence Test Groceries", survivingExpense.getDescription());

        // Verify dashboard calculations across restart
        DashboardSummaryDto restartedSummary = dashboardService.getDashboardSummary();
        assertEquals(new BigDecimal("3500.00"), restartedSummary.getAvailableBalance());
        assertEquals(new BigDecimal("5000.00"), restartedSummary.getThisMonthIncome());
        assertEquals(new BigDecimal("1500.00"), restartedSummary.getThisMonthExpense());

        // Step I: Verify no duplicate records were created
        assertEquals(1, incomeRepository.count(), "Income count must remain exactly 1 (no duplicates)");
        assertEquals(1, expenseRepository.count(), "Expense count must remain exactly 1 (no duplicates)");
        assertEquals(0, budgetRepository.count(), "Budget count must remain 0 (no dummy budgets)");
        assertEquals(0, subscriptionRepository.count(), "Subscription count must remain 0 (no dummy subscriptions)");

        // Cleanup
        incomeRepository.deleteById(createdIncomeId);
        expenseRepository.deleteById(createdExpenseId);
    }
}

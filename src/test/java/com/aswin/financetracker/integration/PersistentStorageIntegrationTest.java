package com.aswin.financetracker.integration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.aswin.financetracker.config.DataLoader;
import com.aswin.financetracker.dto.ExpenseRequestDto;
import com.aswin.financetracker.dto.IncomeRequestDto;
import com.aswin.financetracker.dto.SubscriptionRequestDto;
import com.aswin.financetracker.model.Expense;
import com.aswin.financetracker.model.Income;
import com.aswin.financetracker.model.Subscription;
import com.aswin.financetracker.model.enums.BillingCycle;
import com.aswin.financetracker.model.enums.ExpenseCategory;
import com.aswin.financetracker.model.enums.IncomeSource;
import com.aswin.financetracker.repository.BudgetRepository;
import com.aswin.financetracker.repository.ExpenseRepository;
import com.aswin.financetracker.repository.IncomeRepository;
import com.aswin.financetracker.repository.SubscriptionRepository;
import com.aswin.financetracker.service.ExpenseService;
import com.aswin.financetracker.service.IncomeService;
import com.aswin.financetracker.service.SubscriptionService;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
class PersistentStorageIntegrationTest {

    @Autowired
    private IncomeService incomeService;

    @Autowired
    private ExpenseService expenseService;

    @Autowired
    private SubscriptionService subscriptionService;

    @Autowired
    private IncomeRepository incomeRepository;

    @Autowired
    private ExpenseRepository expenseRepository;

    @Autowired
    private BudgetRepository budgetRepository;

    @Autowired
    private SubscriptionRepository subscriptionRepository;

    @Autowired
    private DataLoader dataLoader;

    @Test
    @DisplayName("Create financial records and verify they can be retrieved with precision preserved")
    @Transactional
    void testRecordCreationAndRetrieval() {
        // 1. Create Income
        IncomeRequestDto incomeDto = new IncomeRequestDto(
            null,
            new BigDecimal("1250.75"),
            IncomeSource.FREELANCE,
            LocalDate.of(2026, 9, 10),
            "Integration Test Freelance Payment"
        );
        Income savedIncome = incomeService.createIncome(incomeDto);
        assertNotNull(savedIncome.getId());

        Income retrievedIncome = incomeService.getIncomeById(savedIncome.getId());
        assertEquals(new BigDecimal("1250.75"), retrievedIncome.getAmount());
        assertEquals(IncomeSource.FREELANCE, retrievedIncome.getSource());
        assertEquals("Integration Test Freelance Payment", retrievedIncome.getNote());

        // 2. Create Expense
        ExpenseRequestDto expenseDto = new ExpenseRequestDto(
            null,
            new BigDecimal("45.99"),
            ExpenseCategory.FOOD,
            LocalDate.of(2026, 9, 10),
            "Integration Test Grocery Run"
        );
        Expense savedExpense = expenseService.createExpense(expenseDto);
        assertNotNull(savedExpense.getId());

        Expense retrievedExpense = expenseService.getExpenseById(savedExpense.getId());
        assertEquals(new BigDecimal("45.99"), retrievedExpense.getAmount());
        assertEquals(ExpenseCategory.FOOD, retrievedExpense.getCategory());
        assertEquals("Integration Test Grocery Run", retrievedExpense.getDescription());
    }

    @Test
    @DisplayName("DataLoader idempotency: multiple invocations of dataLoader.run() must NOT duplicate records")
    void testDataLoaderIdempotency_MultipleCallsDoNotDuplicate() {
        long initialIncomes = incomeRepository.count();
        long initialExpenses = expenseRepository.count();
        long initialBudgets = budgetRepository.count();
        long initialSubscriptions = subscriptionRepository.count();

        // Trigger DataLoader run multiple times
        dataLoader.run();
        dataLoader.run();
        dataLoader.run();

        // Assert record counts have not changed at all
        assertEquals(initialIncomes, incomeRepository.count(), "Income count must remain identical after multiple DataLoader runs");
        assertEquals(initialExpenses, expenseRepository.count(), "Expense count must remain identical after multiple DataLoader runs");
        assertEquals(initialBudgets, budgetRepository.count(), "Budget count must remain identical after multiple DataLoader runs");
        assertEquals(initialSubscriptions, subscriptionRepository.count(), "Subscription count must remain identical after multiple DataLoader runs");
    }

    @Test
    @DisplayName("Subscription Mark-As-Paid: atomic creation of Expense and idempotent double-submission guard")
    void testSubscriptionMarkAsPaidIdempotencyAndAtomicity() {
        LocalDate today = LocalDate.now();
        SubscriptionRequestDto subDto = new SubscriptionRequestDto(
            null,
            "Integration Test News Sub",
            new BigDecimal("12.50"),
            BillingCycle.MONTHLY,
            today.plusDays(1), // Due within 3 days
            ExpenseCategory.ENTERTAINMENT,
            true
        );
        Subscription sub = subscriptionService.createSubscription(subDto);
        assertNotNull(sub.getId());

        long expenseCountBefore = expenseRepository.count();

        // 1. First Mark as Paid call
        Subscription paidSub = subscriptionService.markAsPaid(sub.getId());
        assertEquals(today.plusDays(1).plusMonths(1), paidSub.getNextDueDate());
        assertEquals(expenseCountBefore + 1, expenseRepository.count());

        // Verify the recorded Expense details
        List<Expense> expenses = expenseRepository.findAll();
        boolean foundExpense = expenses.stream()
            .anyMatch(e -> e.getDescription().equals("Subscription: Integration Test News Sub") 
                        && e.getAmount().compareTo(new BigDecimal("12.50")) == 0);
        assertTrue(foundExpense, "Corresponding Expense must be atomically recorded");

        // 2. Second duplicate Mark as Paid call (simulating double-click or retry)
        Subscription duplicateCallSub = subscriptionService.markAsPaid(sub.getId());

        // Must NOT create a second expense, and must NOT advance due date again
        assertEquals(paidSub.getNextDueDate(), duplicateCallSub.getNextDueDate());
        assertEquals(expenseCountBefore + 1, expenseRepository.count(), "Duplicate payment call must not create another Expense");

        // Cleanup
        subscriptionService.deleteSubscription(sub.getId());
    }
}

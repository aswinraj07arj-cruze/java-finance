package com.aswin.financetracker.integration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.aswin.financetracker.dto.ExpenseRequestDto;
import com.aswin.financetracker.model.Expense;
import com.aswin.financetracker.model.enums.ExpenseCategory;
import com.aswin.financetracker.repository.ExpenseRepository;
import com.aswin.financetracker.repository.IncomeRepository;
import com.aswin.financetracker.service.ExpenseService;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;

@SpringBootTest
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class ContextRestartPersistenceIntegrationTest {

    @Autowired
    private ExpenseService expenseService;

    @Autowired
    private ExpenseRepository expenseRepository;

    @Autowired
    private IncomeRepository incomeRepository;

    private static final String UNIQUE_RESTART_DESC = "Context Restart Persistence Test Entry";
    private static Long persistedExpenseId;
    private static long initialIncomeCount;
    private static long initialExpenseCount;

    @Test
    @Order(1)
    @DisplayName("Step 1: Record initial counts, create persistent entity, and trigger DirtiesContext restart")
    @DirtiesContext
    void step1_createRecordAndTriggerContextRestart() {
        initialIncomeCount = incomeRepository.count();
        initialExpenseCount = expenseRepository.count();

        ExpenseRequestDto dto = new ExpenseRequestDto(
            null,
            new BigDecimal("99.95"),
            ExpenseCategory.SHOPPING,
            LocalDate.of(2026, 9, 10),
            UNIQUE_RESTART_DESC
        );

        Expense created = expenseService.createExpense(dto);
        assertNotNull(created.getId());
        persistedExpenseId = created.getId();

        assertTrue(expenseRepository.findById(persistedExpenseId).isPresent(), "Expense should be persisted before context teardown");
    }

    @Test
    @Order(2)
    @DisplayName("Step 2: In a fresh reinitialized ApplicationContext, verify entity survives and DataLoader did not duplicate records")
    void step2_verifyEntitySurvivesAcrossRestartAndDataLoaderDidNotDuplicate() {
        // 1. Verify that the entity created before context restart is still present in the file database
        assertNotNull(persistedExpenseId, "Persisted ID should have been set in Step 1");
        Optional<Expense> survivingExpense = expenseRepository.findById(persistedExpenseId);
        assertTrue(survivingExpense.isPresent(), "Entity MUST survive across application context reinitialization / restart");
        assertEquals(UNIQUE_RESTART_DESC, survivingExpense.get().getDescription());
        assertEquals(new BigDecimal("99.95"), survivingExpense.get().getAmount());

        // 2. Verify DataLoader did NOT duplicate sample records upon context startup
        // Incomes must be exactly initial count (no sample duplicates)
        assertEquals(initialIncomeCount, incomeRepository.count(), "Income count must remain identical (DataLoader did not duplicate)");
        // Expenses must be initial count + 1 (only the new test expense was added)
        assertEquals(initialExpenseCount + 1, expenseRepository.count(), "Expense count must be initial count + 1 test record");

        // Clean up test entry
        expenseRepository.deleteById(persistedExpenseId);
    }
}

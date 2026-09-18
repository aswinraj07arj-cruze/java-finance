package com.aswin.financetracker.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.aswin.financetracker.dto.BudgetRequestDto;
import com.aswin.financetracker.dto.BudgetStatusDto;
import com.aswin.financetracker.model.Budget;
import com.aswin.financetracker.model.User;
import com.aswin.financetracker.model.enums.ExpenseCategory;
import com.aswin.financetracker.repository.BudgetRepository;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class BudgetServiceTest {

    @Mock
    private BudgetRepository budgetRepository;

    @Mock
    private ExpenseService expenseService;

    @Mock
    private UserService userService;

    private BudgetService budgetService;
    private User dummyUser;

    @BeforeEach
    void setUp() {
        budgetService = new BudgetService(budgetRepository, expenseService, userService);
        dummyUser = new User("test_user", "test@example.com");
    }

    @Test
    @DisplayName("Should correctly calculate budget usage, remaining amount, and over/under flags")
    void testBudgetsWithStatusCalculations() {
        LocalDate start = LocalDate.of(2026, 9, 1);
        LocalDate end = LocalDate.of(2026, 9, 30);

        Budget budgetFood = new Budget(ExpenseCategory.FOOD, new BigDecimal("500.00"), dummyUser);
        budgetFood.setId(1L);

        Budget budgetTravel = new Budget(ExpenseCategory.TRAVEL, new BigDecimal("200.00"), dummyUser);
        budgetTravel.setId(2L);

        when(userService.getDefaultUser()).thenReturn(dummyUser);
        when(budgetRepository.findByUserOrderByCategoryAsc(dummyUser)).thenReturn(List.of(budgetFood, budgetTravel));

        // Food spent 400 (under budget)
        when(expenseService.getExpenseByCategoryBetween(ExpenseCategory.FOOD, start, end))
            .thenReturn(new BigDecimal("400.00"));
        // Travel spent 250 (over budget)
        when(expenseService.getExpenseByCategoryBetween(ExpenseCategory.TRAVEL, start, end))
            .thenReturn(new BigDecimal("250.00"));

        List<BudgetStatusDto> statuses = budgetService.getBudgetsWithStatusForPeriod(start, end);

        assertEquals(2, statuses.size());

        BudgetStatusDto foodStatus = statuses.get(0);
        assertEquals(ExpenseCategory.FOOD, foodStatus.getCategory());
        assertEquals(new BigDecimal("500.00"), foodStatus.getMonthlyLimit());
        assertEquals(new BigDecimal("400.00"), foodStatus.getActualSpent());
        assertEquals(new BigDecimal("100.00"), foodStatus.getRemainingAmount());
        assertEquals(80.0, foodStatus.getPercentUsed());
        assertFalse(foodStatus.isOverBudget());
        assertEquals("badge-success", foodStatus.getStatusBadgeClass());

        BudgetStatusDto travelStatus = statuses.get(1);
        assertEquals(ExpenseCategory.TRAVEL, travelStatus.getCategory());
        assertEquals(new BigDecimal("200.00"), travelStatus.getMonthlyLimit());
        assertEquals(new BigDecimal("250.00"), travelStatus.getActualSpent());
        assertEquals(new BigDecimal("-50.00"), travelStatus.getRemainingAmount());
        assertEquals(125.0, travelStatus.getPercentUsed());
        assertTrue(travelStatus.isOverBudget());
        assertEquals("badge-danger", travelStatus.getStatusBadgeClass());
    }

    @Test
    @DisplayName("Should update existing budget limit if category is already present")
    void testSaveOrUpdateBudget_UpdateExisting() {
        Budget existing = new Budget(ExpenseCategory.FOOD, new BigDecimal("400.00"), dummyUser);
        existing.setId(10L);

        when(userService.getDefaultUser()).thenReturn(dummyUser);
        when(budgetRepository.findByUserAndCategory(dummyUser, ExpenseCategory.FOOD))
            .thenReturn(Optional.of(existing));
        when(budgetRepository.save(any(Budget.class))).thenAnswer(invocation -> invocation.getArgument(0));

        BudgetRequestDto dto = new BudgetRequestDto(null, ExpenseCategory.FOOD, new BigDecimal("600.00"));
        Budget updated = budgetService.saveOrUpdateBudget(dto);

        assertNotNull(updated);
        assertEquals(new BigDecimal("600.00"), updated.getMonthlyLimit());
        assertEquals(10L, updated.getId());
    }

    @Test
    @DisplayName("Should create new budget if category does not exist yet")
    void testSaveOrUpdateBudget_CreateNew() {
        when(userService.getDefaultUser()).thenReturn(dummyUser);
        when(budgetRepository.findByUserAndCategory(dummyUser, ExpenseCategory.BILLS))
            .thenReturn(Optional.empty());
        when(budgetRepository.save(any(Budget.class))).thenAnswer(invocation -> invocation.getArgument(0));

        BudgetRequestDto dto = new BudgetRequestDto(null, ExpenseCategory.BILLS, new BigDecimal("350.00"));
        Budget created = budgetService.saveOrUpdateBudget(dto);

        assertNotNull(created);
        assertEquals(ExpenseCategory.BILLS, created.getCategory());
        assertEquals(new BigDecimal("350.00"), created.getMonthlyLimit());
    }
}

package com.aswin.financetracker.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

import com.aswin.financetracker.dto.TransactionFilterDto;
import com.aswin.financetracker.dto.TransactionViewDto;
import com.aswin.financetracker.model.Expense;
import com.aswin.financetracker.model.Income;
import com.aswin.financetracker.model.User;
import com.aswin.financetracker.model.enums.ExpenseCategory;
import com.aswin.financetracker.model.enums.IncomeSource;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;

@ExtendWith(MockitoExtension.class)
class TransactionServiceTest {

    @Mock
    private IncomeService incomeService;

    @Mock
    private ExpenseService expenseService;

    private TransactionService transactionService;
    private User dummyUser;

    @BeforeEach
    void setUp() {
        transactionService = new TransactionService(incomeService, expenseService);
        dummyUser = new User("test_user", "test@example.com");
    }

    @Test
    @DisplayName("Should combine income and expense into a unified, date-sorted transaction list")
    void testUnifiedTransactions_AllTypes() {
        Income inc1 = new Income(new BigDecimal("3000.00"), IncomeSource.SALARY, LocalDate.of(2026, 9, 1), "Salary payment", dummyUser);
        inc1.setId(1L);

        Expense exp1 = new Expense(new BigDecimal("150.00"), ExpenseCategory.FOOD, LocalDate.of(2026, 9, 5), "Dinner out", dummyUser);
        exp1.setId(2L);

        when(incomeService.getIncomes(null, null, null)).thenReturn(List.of(inc1));
        when(expenseService.getExpenses(null, null, null)).thenReturn(List.of(exp1));

        TransactionFilterDto filter = new TransactionFilterDto();
        filter.setType("ALL");

        Page<TransactionViewDto> page = transactionService.getUnifiedTransactions(filter);

        assertEquals(2, page.getTotalElements());
        // Check sorting: Sept 5 first (Expense), Sept 1 second (Income)
        assertEquals("EXPENSE", page.getContent().get(0).getType());
        assertEquals("INCOME", page.getContent().get(1).getType());
    }

    @Test
    @DisplayName("Should filter by search keyword in description or category")
    void testUnifiedTransactions_SearchQuery() {
        Income inc = new Income(new BigDecimal("500.00"), IncomeSource.FREELANCE, LocalDate.of(2026, 9, 2), "React web development", dummyUser);
        inc.setId(10L);

        Expense exp = new Expense(new BigDecimal("80.00"), ExpenseCategory.FOOD, LocalDate.of(2026, 9, 3), "Pizza dinner with team", dummyUser);
        exp.setId(11L);

        when(incomeService.getIncomes(null, null, null)).thenReturn(List.of(inc));
        when(expenseService.getExpenses(null, null, null)).thenReturn(List.of(exp));

        TransactionFilterDto filter = new TransactionFilterDto();
        filter.setSearch("pizza");

        Page<TransactionViewDto> page = transactionService.getUnifiedTransactions(filter);

        assertEquals(1, page.getTotalElements());
        assertEquals("EXPENSE", page.getContent().get(0).getType());
        assertTrue(page.getContent().get(0).getDescriptionOrNote().toLowerCase().contains("pizza"));
    }

    @Test
    @DisplayName("Should paginate transactions accurately")
    void testUnifiedTransactions_Pagination() {
        Income inc1 = new Income(new BigDecimal("100.00"), IncomeSource.GIFT, LocalDate.of(2026, 9, 1), "Gift 1", dummyUser);
        inc1.setId(1L);
        Income inc2 = new Income(new BigDecimal("200.00"), IncomeSource.GIFT, LocalDate.of(2026, 9, 2), "Gift 2", dummyUser);
        inc2.setId(2L);
        Income inc3 = new Income(new BigDecimal("300.00"), IncomeSource.GIFT, LocalDate.of(2026, 9, 3), "Gift 3", dummyUser);
        inc3.setId(3L);

        when(incomeService.getIncomes(null, null, null)).thenReturn(List.of(inc1, inc2, inc3));
        when(expenseService.getExpenses(null, null, null)).thenReturn(List.of());

        TransactionFilterDto filter = new TransactionFilterDto();
        filter.setSize(2);
        filter.setPage(0);

        Page<TransactionViewDto> page1 = transactionService.getUnifiedTransactions(filter);
        assertEquals(2, page1.getContent().size());
        assertEquals(3, page1.getTotalElements());

        filter.setPage(1);
        Page<TransactionViewDto> page2 = transactionService.getUnifiedTransactions(filter);
        assertEquals(1, page2.getContent().size());
    }
}

package com.aswin.financetracker.service;

import com.aswin.financetracker.dto.ExpenseRequestDto;
import com.aswin.financetracker.model.Expense;
import com.aswin.financetracker.model.User;
import com.aswin.financetracker.model.enums.ExpenseCategory;
import com.aswin.financetracker.repository.ExpenseRepository;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.NoSuchElementException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class ExpenseService {

    private final ExpenseRepository expenseRepository;
    private final UserService userService;

    public ExpenseService(ExpenseRepository expenseRepository, UserService userService) {
        this.expenseRepository = expenseRepository;
        this.userService = userService;
    }

    @Transactional(readOnly = true)
    public List<Expense> getExpenses(LocalDate startDate, LocalDate endDate, ExpenseCategory category) {
        User user = userService.getDefaultUser();
        if (startDate != null && endDate != null && category != null) {
            return expenseRepository.findByUserAndCategoryAndDateBetweenOrderByDateDescIdDesc(user, category, startDate, endDate);
        } else if (startDate != null && endDate != null) {
            return expenseRepository.findByUserAndDateBetweenOrderByDateDescIdDesc(user, startDate, endDate);
        } else if (category != null) {
            return expenseRepository.findByUserAndCategoryOrderByDateDescIdDesc(user, category);
        } else {
            return expenseRepository.findByUserOrderByDateDescIdDesc(user);
        }
    }

    @Transactional(readOnly = true)
    public Expense getExpenseById(Long id) {
        return expenseRepository.findById(id)
            .orElseThrow(() -> new NoSuchElementException("Expense entry not found with id: " + id));
    }

    public Expense createExpense(ExpenseRequestDto dto) {
        User user = userService.getDefaultUser();
        Expense expense = new Expense(
            dto.getAmount(),
            dto.getCategory(),
            dto.getDate(),
            dto.getDescription(),
            user
        );
        return expenseRepository.save(expense);
    }

    public Expense updateExpense(Long id, ExpenseRequestDto dto) {
        Expense expense = getExpenseById(id);
        expense.setAmount(dto.getAmount());
        expense.setCategory(dto.getCategory());
        expense.setDate(dto.getDate());
        expense.setDescription(dto.getDescription());
        return expenseRepository.save(expense);
    }

    public void deleteExpense(Long id) {
        if (!expenseRepository.existsById(id)) {
            throw new NoSuchElementException("Expense entry not found with id: " + id);
        }
        expenseRepository.deleteById(id);
    }

    @Transactional(readOnly = true)
    public BigDecimal getTotalExpense() {
        User user = userService.getDefaultUser();
        return expenseRepository.sumTotalExpenseByUser(user);
    }

    @Transactional(readOnly = true)
    public BigDecimal getExpenseBetween(LocalDate startDate, LocalDate endDate) {
        User user = userService.getDefaultUser();
        return expenseRepository.sumExpenseByUserAndDateBetween(user, startDate, endDate);
    }

    @Transactional(readOnly = true)
    public BigDecimal getExpenseByCategoryBetween(ExpenseCategory category, LocalDate startDate, LocalDate endDate) {
        User user = userService.getDefaultUser();
        return expenseRepository.sumExpenseByUserAndCategoryAndDateBetween(user, category, startDate, endDate);
    }

    @Transactional(readOnly = true)
    public List<Object[]> getCategorySpending(LocalDate startDate, LocalDate endDate) {
        User user = userService.getDefaultUser();
        return expenseRepository.findCategoryTotalsByUserAndDateBetween(user, startDate, endDate);
    }
}

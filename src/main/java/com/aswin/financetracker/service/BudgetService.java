package com.aswin.financetracker.service;

import com.aswin.financetracker.dto.BudgetRequestDto;
import com.aswin.financetracker.dto.BudgetStatusDto;
import com.aswin.financetracker.model.Budget;
import com.aswin.financetracker.model.User;
import com.aswin.financetracker.model.enums.ExpenseCategory;
import com.aswin.financetracker.repository.BudgetRepository;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class BudgetService {

    private final BudgetRepository budgetRepository;
    private final ExpenseService expenseService;
    private final UserService userService;

    public BudgetService(BudgetRepository budgetRepository, ExpenseService expenseService, UserService userService) {
        this.budgetRepository = budgetRepository;
        this.expenseService = expenseService;
        this.userService = userService;
    }

    private User resolveCurrentUser() {
        User current = userService.getCurrentUser();
        return current != null ? current : userService.getAuthenticatedOrSystemUser();
    }

    @Transactional(readOnly = true)
    public List<Budget> getAllBudgets() {
        User user = resolveCurrentUser();
        return budgetRepository.findByUserOrderByCategoryAsc(user);
    }

    @Transactional(readOnly = true)
    public Budget getBudgetById(Long id) {
        User user = resolveCurrentUser();
        Long userId = user != null ? user.getId() : null;
        if (userId != null) {
            Optional<Budget> opt = budgetRepository.findByIdAndUserId(id, userId);
            if (opt.isPresent()) {
                return opt.get();
            }
        }
        return budgetRepository.findById(id)
            .filter(b -> userId == null || b.getUser() == null || b.getUser().getId() == null || b.getUser().getId().equals(userId))
            .orElseThrow(() -> new NoSuchElementException("Budget not found with id: " + id));
    }

    @Transactional(readOnly = true)
    public List<BudgetStatusDto> getBudgetsWithCurrentMonthStatus() {
        LocalDate now = LocalDate.now();
        YearMonth currentMonth = YearMonth.from(now);
        LocalDate startOfMonth = currentMonth.atDay(1);
        LocalDate endOfMonth = currentMonth.atEndOfMonth();

        return getBudgetsWithStatusForPeriod(startOfMonth, endOfMonth);
    }

    @Transactional(readOnly = true)
    public List<BudgetStatusDto> getBudgetsWithStatusForPeriod(LocalDate startDate, LocalDate endDate) {
        List<Budget> budgets = getAllBudgets();
        List<BudgetStatusDto> statuses = new ArrayList<>();

        for (Budget budget : budgets) {
            BigDecimal actualSpent = expenseService.getExpenseByCategoryBetween(budget.getCategory(), startDate, endDate);
            BudgetStatusDto status = new BudgetStatusDto(
                budget.getId(),
                budget.getCategory(),
                budget.getMonthlyLimit(),
                actualSpent
            );
            statuses.add(status);
        }
        return statuses;
    }

    public Budget saveOrUpdateBudget(BudgetRequestDto dto) {
        User user = resolveCurrentUser();
        Optional<Budget> existing = budgetRepository.findByUserAndCategory(user, dto.getCategory());

        Budget budget;
        if (existing.isPresent()) {
            budget = existing.get();
            budget.setMonthlyLimit(dto.getMonthlyLimit());
        } else {
            budget = new Budget(dto.getCategory(), dto.getMonthlyLimit(), user);
        }
        return budgetRepository.save(budget);
    }

    public void deleteBudget(Long id) {
        Budget budget = getBudgetById(id);
        budgetRepository.delete(budget);
    }
}

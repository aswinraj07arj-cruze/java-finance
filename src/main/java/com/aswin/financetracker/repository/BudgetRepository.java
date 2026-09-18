package com.aswin.financetracker.repository;

import com.aswin.financetracker.model.Budget;
import com.aswin.financetracker.model.User;
import com.aswin.financetracker.model.enums.ExpenseCategory;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BudgetRepository extends JpaRepository<Budget, Long> {

    List<Budget> findByUserOrderByCategoryAsc(User user);

    Optional<Budget> findByUserAndCategory(User user, ExpenseCategory category);
}

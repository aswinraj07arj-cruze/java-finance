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

    Optional<Budget> findByIdAndUserId(Long id, Long userId);

    Optional<Budget> findByIdAndUser(Long id, User user);

    List<Budget> findByUserOrderByCategoryAsc(User user);

    Optional<Budget> findByUserAndCategory(User user, ExpenseCategory category);

    @org.springframework.data.jpa.repository.Modifying
    @org.springframework.data.jpa.repository.Query("UPDATE Budget b SET b.user = :newUser WHERE b.user = :oldUser")
    int reassignUser(@org.springframework.data.repository.query.Param("oldUser") User oldUser, @org.springframework.data.repository.query.Param("newUser") User newUser);
}

package com.aswin.financetracker.repository;

import com.aswin.financetracker.model.Expense;
import com.aswin.financetracker.model.User;
import com.aswin.financetracker.model.enums.ExpenseCategory;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ExpenseRepository extends JpaRepository<Expense, Long> {

    List<Expense> findByUserOrderByDateDescIdDesc(User user);

    List<Expense> findByUserAndDateBetweenOrderByDateDescIdDesc(User user, LocalDate startDate, LocalDate endDate);

    List<Expense> findByUserAndCategoryOrderByDateDescIdDesc(User user, ExpenseCategory category);

    List<Expense> findByUserAndCategoryAndDateBetweenOrderByDateDescIdDesc(User user, ExpenseCategory category, LocalDate startDate, LocalDate endDate);

    @Query("SELECT COALESCE(SUM(e.amount), 0) FROM Expense e WHERE e.user = :user")
    BigDecimal sumTotalExpenseByUser(@Param("user") User user);

    @Query("SELECT COALESCE(SUM(e.amount), 0) FROM Expense e WHERE e.user = :user AND e.date BETWEEN :startDate AND :endDate")
    BigDecimal sumExpenseByUserAndDateBetween(@Param("user") User user, @Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

    @Query("SELECT COALESCE(SUM(e.amount), 0) FROM Expense e WHERE e.user = :user AND e.category = :category AND e.date BETWEEN :startDate AND :endDate")
    BigDecimal sumExpenseByUserAndCategoryAndDateBetween(@Param("user") User user, @Param("category") ExpenseCategory category, @Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

    @Query("SELECT e.category, COALESCE(SUM(e.amount), 0) FROM Expense e WHERE e.user = :user AND e.date BETWEEN :startDate AND :endDate GROUP BY e.category")
    List<Object[]> findCategoryTotalsByUserAndDateBetween(@Param("user") User user, @Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);
}

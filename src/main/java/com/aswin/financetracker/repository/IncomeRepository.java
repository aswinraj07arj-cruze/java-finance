package com.aswin.financetracker.repository;

import com.aswin.financetracker.model.Income;
import com.aswin.financetracker.model.User;
import com.aswin.financetracker.model.enums.IncomeSource;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface IncomeRepository extends JpaRepository<Income, Long> {

    Optional<Income> findByIdAndUserId(Long id, Long userId);

    Optional<Income> findByIdAndUser(Long id, User user);

    List<Income> findByUserOrderByDateDescIdDesc(User user);

    List<Income> findByUserAndDateBetweenOrderByDateDescIdDesc(User user, LocalDate startDate, LocalDate endDate);

    List<Income> findByUserAndSourceOrderByDateDescIdDesc(User user, IncomeSource source);

    List<Income> findByUserAndSourceAndDateBetweenOrderByDateDescIdDesc(User user, IncomeSource source, LocalDate startDate, LocalDate endDate);

    @Query("SELECT COALESCE(SUM(i.amount), 0) FROM Income i WHERE i.user = :user")
    BigDecimal sumTotalIncomeByUser(@Param("user") User user);

    @Query("SELECT COALESCE(SUM(i.amount), 0) FROM Income i WHERE i.user = :user AND i.date BETWEEN :startDate AND :endDate")
    BigDecimal sumIncomeByUserAndDateBetween(@Param("user") User user, @Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

    @Query("SELECT i.source, COALESCE(SUM(i.amount), 0) FROM Income i WHERE i.user = :user AND i.date BETWEEN :startDate AND :endDate GROUP BY i.source")
    List<Object[]> findSourceTotalsByUserAndDateBetween(@Param("user") User user, @Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

    @org.springframework.data.jpa.repository.Modifying
    @Query("UPDATE Income i SET i.user = :newUser WHERE i.user = :oldUser")
    int reassignUser(@Param("oldUser") User oldUser, @Param("newUser") User newUser);
}

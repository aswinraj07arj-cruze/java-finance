package com.aswin.financetracker.repository;

import com.aswin.financetracker.model.Subscription;
import com.aswin.financetracker.model.User;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SubscriptionRepository extends JpaRepository<Subscription, Long> {

    Optional<Subscription> findByIdAndUserId(Long id, Long userId);

    Optional<Subscription> findByIdAndUser(Long id, User user);

    List<Subscription> findByUserOrderByNextDueDateAsc(User user);

    List<Subscription> findByUserAndActiveTrueOrderByNextDueDateAsc(User user);

    List<Subscription> findByUserAndActiveTrueAndNextDueDateBetweenOrderByNextDueDateAsc(User user, LocalDate startDate, LocalDate endDate);

    @org.springframework.data.jpa.repository.Modifying
    @org.springframework.data.jpa.repository.Query("UPDATE Subscription s SET s.user = :newUser WHERE s.user = :oldUser")
    int reassignUser(@org.springframework.data.repository.query.Param("oldUser") User oldUser, @org.springframework.data.repository.query.Param("newUser") User newUser);
}

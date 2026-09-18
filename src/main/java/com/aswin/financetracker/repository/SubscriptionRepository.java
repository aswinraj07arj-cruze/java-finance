package com.aswin.financetracker.repository;

import com.aswin.financetracker.model.Subscription;
import com.aswin.financetracker.model.User;
import java.time.LocalDate;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SubscriptionRepository extends JpaRepository<Subscription, Long> {

    List<Subscription> findByUserOrderByNextDueDateAsc(User user);

    List<Subscription> findByUserAndActiveTrueOrderByNextDueDateAsc(User user);

    List<Subscription> findByUserAndActiveTrueAndNextDueDateBetweenOrderByNextDueDateAsc(User user, LocalDate startDate, LocalDate endDate);
}

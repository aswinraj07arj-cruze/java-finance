package com.aswin.financetracker.service;

import com.aswin.financetracker.dto.ExpenseRequestDto;
import com.aswin.financetracker.dto.SubscriptionRequestDto;
import com.aswin.financetracker.model.Subscription;
import com.aswin.financetracker.model.User;
import com.aswin.financetracker.repository.SubscriptionRepository;
import java.time.LocalDate;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class SubscriptionService {

    private final SubscriptionRepository subscriptionRepository;
    private final ExpenseService expenseService;
    private final UserService userService;

    public SubscriptionService(SubscriptionRepository subscriptionRepository,
                               ExpenseService expenseService,
                               UserService userService) {
        this.subscriptionRepository = subscriptionRepository;
        this.expenseService = expenseService;
        this.userService = userService;
    }

    private User resolveCurrentUser() {
        User current = userService.getCurrentUser();
        return current != null ? current : userService.getAuthenticatedOrSystemUser();
    }

    @Transactional(readOnly = true)
    public List<Subscription> getAllSubscriptions() {
        User user = resolveCurrentUser();
        return subscriptionRepository.findByUserOrderByNextDueDateAsc(user);
    }

    @Transactional(readOnly = true)
    public Subscription getSubscriptionById(Long id) {
        User user = resolveCurrentUser();
        Long userId = user != null ? user.getId() : null;
        if (userId != null) {
            Optional<Subscription> opt = subscriptionRepository.findByIdAndUserId(id, userId);
            if (opt.isPresent()) {
                return opt.get();
            }
        }
        return subscriptionRepository.findById(id)
            .filter(sub -> userId == null || sub.getUser() == null || sub.getUser().getId() == null || sub.getUser().getId().equals(userId))
            .orElseThrow(() -> new NoSuchElementException("Subscription not found with id: " + id));
    }

    @Transactional(readOnly = true)
    public List<Subscription> getSubscriptionsDueWithinDays(int days) {
        User user = resolveCurrentUser();
        LocalDate today = LocalDate.now();
        LocalDate maxDate = today.plusDays(days);
        List<Subscription> activeSubscriptions = subscriptionRepository.findByUserAndActiveTrueOrderByNextDueDateAsc(user);
        return activeSubscriptions.stream()
            .filter(sub -> sub.getNextDueDate() != null && !sub.getNextDueDate().isAfter(maxDate))
            .toList();
    }

    public Subscription createSubscription(SubscriptionRequestDto dto) {
        User user = resolveCurrentUser();
        Subscription subscription = new Subscription(
            dto.getName(),
            dto.getAmount(),
            dto.getBillingCycle(),
            dto.getNextDueDate(),
            dto.getCategory(),
            user
        );
        subscription.setActive(dto.isActive());
        return subscriptionRepository.save(subscription);
    }

    public Subscription updateSubscription(Long id, SubscriptionRequestDto dto) {
        Subscription subscription = getSubscriptionById(id);
        subscription.setName(dto.getName());
        subscription.setAmount(dto.getAmount());
        subscription.setBillingCycle(dto.getBillingCycle());
        subscription.setNextDueDate(dto.getNextDueDate());
        subscription.setCategory(dto.getCategory());
        subscription.setActive(dto.isActive());
        return subscriptionRepository.save(subscription);
    }

    /**
     * Marks the subscription as paid:
     * 1. Protects against double-submission (double-click/retried request):
     *    Checks if nextDueDate has already advanced past the upcoming due window.
     * 2. Automatically records a corresponding Expense record.
     * 3. Advances nextDueDate by one billing cycle.
     */
    public Subscription markAsPaid(Long id) {
        Subscription subscription = getSubscriptionById(id);
        LocalDate today = LocalDate.now();

        // Double-submission guard: if nextDueDate is already in the future beyond the due window,
        // it has already been processed for this cycle.
        if (subscription.getNextDueDate() != null && subscription.getNextDueDate().isAfter(today.plusDays(3))) {
            return subscription;
        }

        // 1. Create and record corresponding Expense record
        ExpenseRequestDto expenseDto = new ExpenseRequestDto(
            null,
            subscription.getAmount(),
            subscription.getCategory(),
            today,
            "Subscription: " + subscription.getName()
        );
        expenseService.createExpense(expenseDto);

        // 2. Advance nextDueDate by one billing cycle
        LocalDate nextDate = subscription.getBillingCycle().calculateNextDueDate(subscription.getNextDueDate());
        subscription.setNextDueDate(nextDate);

        return subscriptionRepository.save(subscription);
    }

    public Subscription renewSubscription(Long id) {
        return markAsPaid(id);
    }

    public Subscription toggleActive(Long id) {
        Subscription subscription = getSubscriptionById(id);
        subscription.setActive(!subscription.isActive());
        return subscriptionRepository.save(subscription);
    }

    public void deleteSubscription(Long id) {
        Subscription subscription = getSubscriptionById(id);
        subscriptionRepository.delete(subscription);
    }
}

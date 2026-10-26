package com.aswin.financetracker.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.aswin.financetracker.dto.ExpenseRequestDto;
import com.aswin.financetracker.model.Subscription;
import com.aswin.financetracker.model.User;
import com.aswin.financetracker.model.enums.BillingCycle;
import com.aswin.financetracker.model.enums.ExpenseCategory;
import com.aswin.financetracker.repository.SubscriptionRepository;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class SubscriptionServiceTest {

    @Mock
    private SubscriptionRepository subscriptionRepository;

    @Mock
    private ExpenseService expenseService;

    @Mock
    private UserService userService;

    private SubscriptionService subscriptionService;
    private User dummyUser;

    @BeforeEach
    void setUp() {
        subscriptionService = new SubscriptionService(subscriptionRepository, expenseService, userService);
        dummyUser = new User("test_user", "test@example.com");
    }

    @Test
    @DisplayName("Should return subscriptions due within 3 days and overdue ones")
    void testGetSubscriptionsDueWithinDays() {
        LocalDate today = LocalDate.now();

        Subscription subDueTomorrow = new Subscription("Netflix", new BigDecimal("15.99"), BillingCycle.MONTHLY, today.plusDays(1), ExpenseCategory.ENTERTAINMENT, dummyUser);
        Subscription subDueInTwoDays = new Subscription("Spotify", new BigDecimal("9.99"), BillingCycle.MONTHLY, today.plusDays(2), ExpenseCategory.ENTERTAINMENT, dummyUser);
        Subscription subDueInTenDays = new Subscription("Gym", new BigDecimal("50.00"), BillingCycle.MONTHLY, today.plusDays(10), ExpenseCategory.HEALTHCARE, dummyUser);
        Subscription subOverdue = new Subscription("Internet", new BigDecimal("60.00"), BillingCycle.MONTHLY, today.minusDays(1), ExpenseCategory.BILLS, dummyUser);

        when(userService.getCurrentUser()).thenReturn(dummyUser);
        when(subscriptionRepository.findByUserAndActiveTrueOrderByNextDueDateAsc(dummyUser))
            .thenReturn(List.of(subOverdue, subDueTomorrow, subDueInTwoDays, subDueInTenDays));

        List<Subscription> due = subscriptionService.getSubscriptionsDueWithinDays(3);

        assertEquals(3, due.size());
        assertTrue(due.contains(subOverdue));
        assertTrue(due.contains(subDueTomorrow));
        assertTrue(due.contains(subDueInTwoDays));
        assertFalse(due.contains(subDueInTenDays));
    }

    @Test
    @DisplayName("Mark as Paid: Should automatically create an Expense record and advance nextDueDate by billing cycle")
    void testMarkAsPaid_CreatesExpenseAndAdvancesDueDate() {
        LocalDate initialDueDate = LocalDate.of(2026, 9, 12);
        Subscription sub = new Subscription("Netflix 4K", new BigDecimal("19.99"), BillingCycle.MONTHLY, initialDueDate, ExpenseCategory.ENTERTAINMENT, dummyUser);
        sub.setId(42L);

        when(subscriptionRepository.findById(42L)).thenReturn(Optional.of(sub));
        when(subscriptionRepository.save(any(Subscription.class))).thenAnswer(inv -> inv.getArgument(0));

        Subscription updated = subscriptionService.markAsPaid(42L);

        // 1. Verify that ExpenseService.createExpense was called with the subscription's details
        ArgumentCaptor<ExpenseRequestDto> expenseCaptor = ArgumentCaptor.forClass(ExpenseRequestDto.class);
        verify(expenseService).createExpense(expenseCaptor.capture());

        ExpenseRequestDto recordedExpense = expenseCaptor.getValue();
        assertEquals(new BigDecimal("19.99"), recordedExpense.getAmount());
        assertEquals(ExpenseCategory.ENTERTAINMENT, recordedExpense.getCategory());
        assertEquals(LocalDate.now(), recordedExpense.getDate());
        assertTrue(recordedExpense.getDescription().contains("Netflix 4K"));

        // 2. Verify that nextDueDate was advanced by 1 month
        assertNotNull(updated);
        assertEquals(LocalDate.of(2026, 10, 12), updated.getNextDueDate());
    }

    @Test
    @DisplayName("Double-submission guard: calling markAsPaid twice in a row only creates one Expense")
    void testMarkAsPaid_DoubleSubmissionOnlyCreatesOneExpense() {
        LocalDate today = LocalDate.now();
        Subscription sub = new Subscription("Gym", new BigDecimal("45.00"), BillingCycle.MONTHLY, today, ExpenseCategory.HEALTHCARE, dummyUser);
        sub.setId(99L);

        when(subscriptionRepository.findById(99L)).thenReturn(Optional.of(sub));
        when(subscriptionRepository.save(any(Subscription.class))).thenAnswer(inv -> inv.getArgument(0));

        // First invocation
        Subscription firstCall = subscriptionService.markAsPaid(99L);
        assertEquals(today.plusMonths(1), firstCall.getNextDueDate());

        // Second duplicate invocation (e.g. double click or rapid retry)
        Subscription secondCall = subscriptionService.markAsPaid(99L);
        assertEquals(today.plusMonths(1), secondCall.getNextDueDate());

        // Assert Expense was created only ONCE, and repository save was called only ONCE
        verify(expenseService, times(1)).createExpense(any(ExpenseRequestDto.class));
        verify(subscriptionRepository, times(1)).save(any(Subscription.class));
    }

    @Test
    @DisplayName("Should toggle active status")
    void testToggleActive() {
        Subscription sub = new Subscription("Gym", new BigDecimal("30.00"), BillingCycle.MONTHLY, LocalDate.now(), ExpenseCategory.HEALTHCARE, dummyUser);
        sub.setId(7L);
        sub.setActive(true);

        when(subscriptionRepository.findById(7L)).thenReturn(Optional.of(sub));
        when(subscriptionRepository.save(any(Subscription.class))).thenAnswer(inv -> inv.getArgument(0));

        Subscription toggled = subscriptionService.toggleActive(7L);

        assertFalse(toggled.isActive());
    }
}

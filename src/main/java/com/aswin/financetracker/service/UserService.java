package com.aswin.financetracker.service;

import com.aswin.financetracker.dto.RegisterRequestDto;
import com.aswin.financetracker.model.User;
import com.aswin.financetracker.repository.BudgetRepository;
import com.aswin.financetracker.repository.ExpenseRepository;
import com.aswin.financetracker.repository.IncomeRepository;
import com.aswin.financetracker.repository.SubscriptionRepository;
import com.aswin.financetracker.repository.UserRepository;
import java.util.List;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final ExpenseRepository expenseRepository;
    private final IncomeRepository incomeRepository;
    private final BudgetRepository budgetRepository;
    private final SubscriptionRepository subscriptionRepository;
    private final String legacyOwnerEmail;

    public UserService(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            ExpenseRepository expenseRepository,
            IncomeRepository incomeRepository,
            BudgetRepository budgetRepository,
            SubscriptionRepository subscriptionRepository,
            @Value("${app.legacy-owner-email:aswin@example.com}") String legacyOwnerEmail) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.expenseRepository = expenseRepository;
        this.incomeRepository = incomeRepository;
        this.budgetRepository = budgetRepository;
        this.subscriptionRepository = subscriptionRepository;
        this.legacyOwnerEmail = legacyOwnerEmail != null ? legacyOwnerEmail.trim() : "";
    }

    @Transactional
    public User registerUser(RegisterRequestDto dto) {
        String normalizedEmail = dto.getEmail().toLowerCase().trim();
        if (!dto.getPassword().equals(dto.getConfirmPassword())) {
            throw new IllegalArgumentException("Passwords do not match.");
        }

        boolean isLegacyOwner = normalizedEmail.equalsIgnoreCase(legacyOwnerEmail);
        Optional<User> existingOpt = userRepository.findByEmail(normalizedEmail);

        if (existingOpt.isPresent()) {
            User existing = existingOpt.get();
            if (existing.isEnabled()) {
                throw new IllegalArgumentException("An account with this email address already exists.");
            }
            if (isLegacyOwner) {
                existing.setFullName(dto.getFullName().trim());
                existing.setPasswordHash(passwordEncoder.encode(dto.getPassword()));
                existing.setEnabled(true);
                User activatedUser = userRepository.save(existing);
                reassignLegacyRows(activatedUser);
                return activatedUser;
            } else {
                throw new IllegalArgumentException("An account with this email address already exists.");
            }
        }

        String passwordHash = passwordEncoder.encode(dto.getPassword());
        User user = new User(dto.getFullName().trim(), normalizedEmail, passwordHash);
        User savedUser = userRepository.save(user);

        if (isLegacyOwner) {
            reassignLegacyRows(savedUser);
        }

        return savedUser;
    }

    private void reassignLegacyRows(User targetUser) {
        List<User> disabledUsers = userRepository.findByEnabledFalse();
        for (User legacyUser : disabledUsers) {
            if (!legacyUser.getId().equals(targetUser.getId())) {
                expenseRepository.reassignUser(legacyUser, targetUser);
                incomeRepository.reassignUser(legacyUser, targetUser);
                budgetRepository.reassignUser(legacyUser, targetUser);
                subscriptionRepository.reassignUser(legacyUser, targetUser);
            }
        }
    }

    public User getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || auth instanceof AnonymousAuthenticationToken) {
            return null;
        }
        String email = auth.getName();
        if (email == null || email.isBlank()) {
            return null;
        }
        return userRepository.findByEmail(email.toLowerCase().trim()).orElse(null);
    }

    public User getAuthenticatedOrSystemUser() {
        User user = getCurrentUser();
        if (user != null) {
            return user;
        }
        return userRepository.findAll().stream().findFirst().orElseGet(() ->
            userRepository.save(new User("System", "system@local.internal", "dummyHash", "SYSTEM", false))
        );
    }
}

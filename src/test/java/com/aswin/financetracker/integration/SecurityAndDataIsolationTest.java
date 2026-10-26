package com.aswin.financetracker.integration;

import static org.hamcrest.Matchers.containsString;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.security.test.web.servlet.response.SecurityMockMvcResultMatchers.authenticated;
import static org.springframework.security.test.web.servlet.response.SecurityMockMvcResultMatchers.unauthenticated;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrlPattern;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

import com.aswin.financetracker.dto.ExpenseRequestDto;
import com.aswin.financetracker.dto.RegisterRequestDto;
import com.aswin.financetracker.model.Expense;
import com.aswin.financetracker.model.User;
import com.aswin.financetracker.model.enums.ExpenseCategory;
import com.aswin.financetracker.repository.BudgetRepository;
import com.aswin.financetracker.repository.ExpenseRepository;
import com.aswin.financetracker.repository.IncomeRepository;
import com.aswin.financetracker.repository.SubscriptionRepository;
import com.aswin.financetracker.repository.UserRepository;
import com.aswin.financetracker.service.ExpenseService;
import com.aswin.financetracker.service.UserService;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

@SpringBootTest
@AutoConfigureMockMvc
class SecurityAndDataIsolationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserService userService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ExpenseService expenseService;

    @Autowired
    private ExpenseRepository expenseRepository;

    @Autowired
    private IncomeRepository incomeRepository;

    @Autowired
    private BudgetRepository budgetRepository;

    @Autowired
    private SubscriptionRepository subscriptionRepository;

    private User userA;
    private User userB;

    private void purgeUser(String email) {
        userRepository.findByEmail(email).ifPresent(u -> {
            Long userId = u.getId();
            expenseRepository.deleteAll(expenseRepository.findAll().stream()
                .filter(e -> e.getUser() != null && userId.equals(e.getUser().getId())).toList());
            incomeRepository.deleteAll(incomeRepository.findAll().stream()
                .filter(i -> i.getUser() != null && userId.equals(i.getUser().getId())).toList());
            budgetRepository.deleteAll(budgetRepository.findAll().stream()
                .filter(b -> b.getUser() != null && userId.equals(b.getUser().getId())).toList());
            subscriptionRepository.deleteAll(subscriptionRepository.findAll().stream()
                .filter(s -> s.getUser() != null && userId.equals(s.getUser().getId())).toList());
            userRepository.delete(u);
        });
    }

    @BeforeEach
    void setUp() {
        if (!userRepository.existsByEmail("usera@example.com")) {
            userA = userService.registerUser(new RegisterRequestDto("User A", "usera@example.com", "Password123", "Password123"));
        } else {
            userA = userRepository.findByEmail("usera@example.com").orElseThrow();
        }

        if (!userRepository.existsByEmail("userb@example.com")) {
            userB = userService.registerUser(new RegisterRequestDto("User B", "userb@example.com", "Password123", "Password123"));
        } else {
            userB = userRepository.findByEmail("userb@example.com").orElseThrow();
        }
    }

    @Test
    @DisplayName("1. Unauthenticated request to protected endpoint (/dashboard) redirects to /login")
    void unauthenticatedAccessRedirectsToLogin() throws Exception {
        mockMvc.perform(get("/dashboard"))
            .andExpect(status().is3xxRedirection())
            .andExpect(redirectedUrlPattern("**/login"));
    }

    @Test
    @DisplayName("2. Public routes (/login, /register, /actuator/health) accessible without authentication (200 OK)")
    void publicRoutesAccessibleWithoutAuth() throws Exception {
        mockMvc.perform(get("/login"))
            .andExpect(status().isOk())
            .andExpect(view().name("auth/login"));

        mockMvc.perform(get("/register"))
            .andExpect(status().isOk())
            .andExpect(view().name("auth/register"));

        mockMvc.perform(get("/actuator/health"))
            .andExpect(status().isOk())
            .andExpect(content().string(containsString("UP")));
    }

    @Test
    @DisplayName("3. Duplicate email registration rejected with friendly error message")
    void registerWithDuplicateEmailRejected() throws Exception {
        mockMvc.perform(post("/register")
                .with(csrf())
                .param("fullName", "Another Aswin")
                .param("email", "usera@example.com")
                .param("password", "Password123")
                .param("confirmPassword", "Password123"))
            .andExpect(status().isOk())
            .andExpect(view().name("auth/register"))
            .andExpect(model().hasErrors())
            .andExpect(model().attributeHasFieldErrors("registerDto", "email"))
            .andExpect(content().string(containsString("already exists")));
    }

    @Test
    @DisplayName("4. Form login with valid credentials succeeds and redirects to /dashboard")
    void formLoginSucceedsWithValidCredentials() throws Exception {
        mockMvc.perform(post("/login")
                .with(csrf())
                .param("email", "usera@example.com")
                .param("password", "Password123"))
            .andExpect(status().is3xxRedirection())
            .andExpect(redirectedUrl("/dashboard"))
            .andExpect(authenticated().withUsername("usera@example.com"));
    }

    @Test
    @DisplayName("5. Form login with invalid credentials fails and redirects to /login?error")
    void formLoginFailsWithInvalidCredentials() throws Exception {
        mockMvc.perform(post("/login")
                .with(csrf())
                .param("email", "usera@example.com")
                .param("password", "WrongPassword"))
            .andExpect(status().is3xxRedirection())
            .andExpect(redirectedUrl("/login?error"))
            .andExpect(unauthenticated());
    }

    @Test
    @DisplayName("6. Multi-user isolation: User B cannot view, edit (404), or delete (404) User A's expense")
    void multiUserDataIsolationEnforced() throws Exception {
        // Step A: Create an expense belonging to User A
        Expense expenseA = new Expense(
            new BigDecimal("999.00"),
            ExpenseCategory.ENTERTAINMENT,
            LocalDate.now(),
            "User A Secret Concert Ticket",
            userA
        );
        expenseA = expenseRepository.save(expenseA);
        assertNotNull(expenseA.getId());
        Long expenseAId = expenseA.getId();

        // Step B: User B views /expenses - should NOT see User A's expense
        MvcResult listResult = mockMvc.perform(get("/expenses")
                .with(user("userb@example.com")))
            .andExpect(status().isOk())
            .andExpect(view().name("expense/list"))
            .andExpect(model().attributeExists("expenses"))
            .andReturn();

        @SuppressWarnings("unchecked")
        List<Expense> userBExpenses = (List<Expense>) listResult.getModelAndView().getModel().get("expenses");
        assertFalse(userBExpenses.stream().anyMatch(e -> e.getId().equals(expenseAId)),
            "User B must not see User A's expense in the list");

        // Step C: User B attempts to GET /expenses/edit/{expenseAId} -> MUST return 404
        mockMvc.perform(get("/expenses/edit/" + expenseAId)
                .with(user("userb@example.com")))
            .andExpect(status().isNotFound())
            .andExpect(view().name("error/404"));

        // Step D: User B attempts to POST /expenses/delete/{expenseAId} -> MUST return 404
        mockMvc.perform(post("/expenses/delete/" + expenseAId)
                .with(csrf())
                .with(user("userb@example.com")))
            .andExpect(status().isNotFound())
            .andExpect(view().name("error/404"));

        // Verify that User A's expense is still intact in the database
        assertTrue(expenseRepository.findById(expenseAId).isPresent(),
            "User A's expense must not have been deleted by User B");

        // Step E: User A CAN access their own expense
        mockMvc.perform(get("/expenses/edit/" + expenseAId)
                .with(user("usera@example.com")))
            .andExpect(status().isOk())
            .andExpect(view().name("expense/edit"));

        // Cleanup
        expenseRepository.deleteById(expenseAId);
    }

    @Test
    @DisplayName("7. Legacy claim: Register with app.legacy-owner-email (case-insensitive) moves legacy rows; second registration gets empty account")
    void legacyOwnerClaimAndSecondRegistrationGetsEmptyAccount() throws Exception {
        purgeUser("aswin@example.com");
        purgeUser("freshuser@example.com");
        purgeUser("legacy_user@local.domain");

        // Step A: Create a disabled legacy user with legacy expense rows
        User legacyUser = new User("Legacy Account", "legacy_user@local.domain", "dummyHash", "USER", false);
        legacyUser = userRepository.save(legacyUser);

        Expense legacyExpense = new Expense(
            new BigDecimal("250.00"),
            ExpenseCategory.FOOD,
            LocalDate.now(),
            "Legacy Groceries",
            legacyUser
        );
        legacyExpense = expenseRepository.save(legacyExpense);
        Long legacyExpenseId = legacyExpense.getId();

        // Step B: Register with email matching app.legacy-owner-email case-insensitively (e.g. ASWIN@EXAMPLE.COM)
        mockMvc.perform(post("/register")
                .with(csrf())
                .param("fullName", "Aswin Raj")
                .param("email", "ASWIN@EXAMPLE.COM")
                .param("password", "Password123")
                .param("confirmPassword", "Password123"))
            .andExpect(status().is3xxRedirection())
            .andExpect(redirectedUrl("/login"));

        User claimedOwner = userRepository.findByEmail("aswin@example.com").orElseThrow();
        assertTrue(claimedOwner.isEnabled(), "Claimed owner account must be enabled");

        // Verify the legacy expense was moved to the newly claimed owner account
        Expense migratedExpense = expenseRepository.findById(legacyExpenseId).orElseThrow();
        assertEquals(claimedOwner.getId(), migratedExpense.getUser().getId(),
            "Legacy expense must be reassigned to the claimed owner account");

        // Verify legacy user remains disabled
        User updatedLegacyUser = userRepository.findById(legacyUser.getId()).orElse(null);
        if (updatedLegacyUser != null) {
            assertFalse(updatedLegacyUser.isEnabled(), "Legacy user must remain disabled");
        }

        // Step C: Second registration with a different email gets an empty account
        mockMvc.perform(post("/register")
                .with(csrf())
                .param("fullName", "User Fresh")
                .param("email", "freshuser@example.com")
                .param("password", "Password123")
                .param("confirmPassword", "Password123"))
            .andExpect(status().is3xxRedirection())
            .andExpect(redirectedUrl("/login"));

        User freshUser = userRepository.findByEmail("freshuser@example.com").orElseThrow();
        List<Expense> freshUserExpenses = expenseRepository.findByUserOrderByDateDescIdDesc(freshUser);
        assertTrue(freshUserExpenses.isEmpty(), "Second registered account must have an empty account with 0 expenses");

        // Clean up test data
        purgeUser("aswin@example.com");
        purgeUser("freshuser@example.com");
        purgeUser("legacy_user@local.domain");
    }
}

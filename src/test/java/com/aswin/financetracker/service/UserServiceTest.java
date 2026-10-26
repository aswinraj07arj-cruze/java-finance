package com.aswin.financetracker.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.aswin.financetracker.dto.RegisterRequestDto;
import com.aswin.financetracker.model.User;
import com.aswin.financetracker.repository.BudgetRepository;
import com.aswin.financetracker.repository.ExpenseRepository;
import com.aswin.financetracker.repository.IncomeRepository;
import com.aswin.financetracker.repository.SubscriptionRepository;
import com.aswin.financetracker.repository.UserRepository;
import java.util.Collections;
import java.util.Optional;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private ExpenseRepository expenseRepository;

    @Mock
    private IncomeRepository incomeRepository;

    @Mock
    private BudgetRepository budgetRepository;

    @Mock
    private SubscriptionRepository subscriptionRepository;

    @Mock
    private SecurityContext securityContext;

    private UserService userService;

    @BeforeEach
    void setUp() {
        userService = new UserService(
            userRepository,
            passwordEncoder,
            expenseRepository,
            incomeRepository,
            budgetRepository,
            subscriptionRepository,
            "aswin@example.com"
        );
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("registerUser: successfully registers a new user with normalized email and encoded password")
    void registerUser_Success() {
        RegisterRequestDto dto = new RegisterRequestDto(
            "  John Doe  ",
            "  John.Doe@Example.com  ",
            "SecretPass123",
            "SecretPass123"
        );

        when(userRepository.findByEmail("john.doe@example.com")).thenReturn(Optional.empty());
        when(passwordEncoder.encode("SecretPass123")).thenReturn("encodedPasswordHash");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        User registered = userService.registerUser(dto);

        assertNotNull(registered);
        assertEquals("John Doe", registered.getFullName());
        assertEquals("john.doe@example.com", registered.getEmail());
        assertEquals("encodedPasswordHash", registered.getPasswordHash());

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());
        User savedUser = userCaptor.getValue();
        assertEquals("John Doe", savedUser.getFullName());
        assertEquals("john.doe@example.com", savedUser.getEmail());
        assertEquals("encodedPasswordHash", savedUser.getPasswordHash());
    }

    @Test
    @DisplayName("registerUser: throws IllegalArgumentException when enabled user already exists with email")
    void registerUser_ThrowsWhenEmailExists() {
        RegisterRequestDto dto = new RegisterRequestDto(
            "Jane Doe",
            "jane@example.com",
            "SecretPass123",
            "SecretPass123"
        );

        User existingUser = new User("Jane Doe", "jane@example.com", "hash", "USER", true);
        when(userRepository.findByEmail("jane@example.com")).thenReturn(Optional.of(existingUser));

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> userService.registerUser(dto));
        assertEquals("An account with this email address already exists.", ex.getMessage());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    @DisplayName("registerUser: throws IllegalArgumentException when password and confirmPassword do not match")
    void registerUser_ThrowsWhenPasswordsDoNotMatch() {
        RegisterRequestDto dto = new RegisterRequestDto(
            "Jane Doe",
            "jane@example.com",
            "SecretPass123",
            "DifferentPass123"
        );

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> userService.registerUser(dto));
        assertEquals("Passwords do not match.", ex.getMessage());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    @DisplayName("registerUser: legacy claim moves legacy rows to new account case-insensitively and second registration gets empty account")
    void registerUser_LegacyClaimAndSecondRegistrationIsolation() {
        // 1. Setup existing legacy user who is disabled
        User legacyUser = new User("Legacy User", "aswin@example.com", "oldHash", "USER", false);
        legacyUser.setId(1L);

        when(userRepository.findByEmail("aswin@example.com")).thenReturn(Optional.of(legacyUser));
        when(passwordEncoder.encode("AswinPass123")).thenReturn("newHash");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Register with uppercase email matching app.legacy-owner-email case-insensitively
        RegisterRequestDto claimDto = new RegisterRequestDto(
            "Aswin Raj",
            "ASWIN@EXAMPLE.COM",
            "AswinPass123",
            "AswinPass123"
        );

        User claimedAccount = userService.registerUser(claimDto);

        assertNotNull(claimedAccount);
        assertEquals("Aswin Raj", claimedAccount.getFullName());
        assertEquals("aswin@example.com", claimedAccount.getEmail());
        assertEquals("newHash", claimedAccount.getPasswordHash());
        org.junit.jupiter.api.Assertions.assertTrue(claimedAccount.isEnabled(), "Claimed account must be enabled");

        // 2. Second registration with a different email gets an empty account without claiming legacy rows
        RegisterRequestDto secondDto = new RegisterRequestDto(
            "Other User",
            "other@example.com",
            "OtherPass123",
            "OtherPass123"
        );

        when(userRepository.findByEmail("other@example.com")).thenReturn(Optional.empty());
        when(passwordEncoder.encode("OtherPass123")).thenReturn("otherHash");

        User secondAccount = userService.registerUser(secondDto);

        assertNotNull(secondAccount);
        assertEquals("other@example.com", secondAccount.getEmail());
        verify(expenseRepository, never()).reassignUser(any(User.class), org.mockito.ArgumentMatchers.eq(secondAccount));
        verify(incomeRepository, never()).reassignUser(any(User.class), org.mockito.ArgumentMatchers.eq(secondAccount));
        verify(budgetRepository, never()).reassignUser(any(User.class), org.mockito.ArgumentMatchers.eq(secondAccount));
        verify(subscriptionRepository, never()).reassignUser(any(User.class), org.mockito.ArgumentMatchers.eq(secondAccount));
    }

    @Test
    @DisplayName("registerUser: legacy claim when legacy user has separate email reassigns rows to newly registered account")
    void registerUser_LegacyClaimWithSeparateLegacyUser() {
        User legacyUser = new User("Old Single User", "legacy@legacy.local", "oldHash", "USER", false);
        legacyUser.setId(10L);

        when(userRepository.findByEmail("aswin@example.com")).thenReturn(Optional.empty());
        when(passwordEncoder.encode("AswinPass123")).thenReturn("aswinHash");
        when(userRepository.findByEnabledFalse()).thenReturn(java.util.List.of(legacyUser));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User u = invocation.getArgument(0);
            u.setId(20L);
            return u;
        });

        RegisterRequestDto claimDto = new RegisterRequestDto(
            "Aswin Raj",
            "aswin@example.com",
            "AswinPass123",
            "AswinPass123"
        );

        User claimed = userService.registerUser(claimDto);

        assertNotNull(claimed);
        assertEquals("aswin@example.com", claimed.getEmail());
        verify(expenseRepository).reassignUser(legacyUser, claimed);
        verify(incomeRepository).reassignUser(legacyUser, claimed);
        verify(budgetRepository).reassignUser(legacyUser, claimed);
        verify(subscriptionRepository).reassignUser(legacyUser, claimed);
        org.junit.jupiter.api.Assertions.assertFalse(legacyUser.isEnabled(), "Legacy user remains disabled");
    }

    @Test
    @DisplayName("getCurrentUser: returns null when SecurityContext has no authentication")
    void getCurrentUser_NoAuth_ReturnsNull() {
        SecurityContextHolder.setContext(securityContext);
        when(securityContext.getAuthentication()).thenReturn(null);

        User currentUser = userService.getCurrentUser();
        assertNull(currentUser);
    }

    @Test
    @DisplayName("getCurrentUser: returns null when authentication is not authenticated")
    void getCurrentUser_Unauthenticated_ReturnsNull() {
        Authentication auth = new UsernamePasswordAuthenticationToken("user@example.com", "pass");
        auth.setAuthenticated(false);
        SecurityContextHolder.getContext().setAuthentication(auth);

        User currentUser = userService.getCurrentUser();
        assertNull(currentUser);
    }

    @Test
    @DisplayName("getCurrentUser: returns null when authentication is anonymous")
    void getCurrentUser_Anonymous_ReturnsNull() {
        AnonymousAuthenticationToken anonymous = new AnonymousAuthenticationToken(
            "key",
            "anonymousUser",
            Collections.singletonList(new SimpleGrantedAuthority("ROLE_ANONYMOUS"))
        );
        SecurityContextHolder.getContext().setAuthentication(anonymous);

        User currentUser = userService.getCurrentUser();
        assertNull(currentUser);
    }

    @Test
    @DisplayName("getCurrentUser: returns user when authenticated with valid user email")
    void getCurrentUser_Authenticated_ReturnsUser() {
        UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
            "user@example.com",
            "pass",
            Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER"))
        );
        SecurityContextHolder.getContext().setAuthentication(auth);

        User mockUser = new User("User One", "user@example.com", "hash");
        when(userRepository.findByEmail("user@example.com")).thenReturn(Optional.of(mockUser));

        User currentUser = userService.getCurrentUser();
        assertNotNull(currentUser);
        assertEquals("user@example.com", currentUser.getEmail());
    }

    @Test
    @DisplayName("getCurrentUser: returns null when authenticated user is not found in database")
    void getCurrentUser_AuthenticatedUserNotFound_ReturnsNull() {
        UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
            "missing@example.com",
            "pass",
            Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER"))
        );
        SecurityContextHolder.getContext().setAuthentication(auth);

        when(userRepository.findByEmail("missing@example.com")).thenReturn(Optional.empty());

        User currentUser = userService.getCurrentUser();
        assertNull(currentUser);
    }
}

package com.aswin.financetracker.dto;

import com.aswin.financetracker.model.enums.BillingCycle;
import com.aswin.financetracker.model.enums.ExpenseCategory;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.time.LocalDate;
import org.springframework.format.annotation.DateTimeFormat;

public class SubscriptionRequestDto {

    private Long id;

    @NotBlank(message = "Subscription name is required")
    @Size(max = 150, message = "Name cannot exceed 150 characters")
    private String name;

    @NotNull(message = "Amount is required")
    @Positive(message = "Amount must be greater than zero")
    private BigDecimal amount;

    @NotNull(message = "Billing cycle is required")
    private BillingCycle billingCycle;

    @NotNull(message = "Next due date is required")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate nextDueDate;

    @NotNull(message = "Category is required")
    private ExpenseCategory category;

    private boolean active = true;

    public SubscriptionRequestDto() {
        this.nextDueDate = LocalDate.now();
        this.active = true;
    }

    public SubscriptionRequestDto(Long id, String name, BigDecimal amount, BillingCycle billingCycle, LocalDate nextDueDate, ExpenseCategory category, boolean active) {
        this.id = id;
        this.name = name;
        this.amount = amount;
        this.billingCycle = billingCycle;
        this.nextDueDate = nextDueDate;
        this.category = category;
        this.active = active;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public BillingCycle getBillingCycle() {
        return billingCycle;
    }

    public void setBillingCycle(BillingCycle billingCycle) {
        this.billingCycle = billingCycle;
    }

    public LocalDate getNextDueDate() {
        return nextDueDate;
    }

    public void setNextDueDate(LocalDate nextDueDate) {
        this.nextDueDate = nextDueDate;
    }

    public ExpenseCategory getCategory() {
        return category;
    }

    public void setCategory(ExpenseCategory category) {
        this.category = category;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }
}

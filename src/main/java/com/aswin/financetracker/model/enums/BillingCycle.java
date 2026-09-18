package com.aswin.financetracker.model.enums;

import java.time.LocalDate;

public enum BillingCycle {
    WEEKLY("Weekly"),
    MONTHLY("Monthly"),
    YEARLY("Yearly");

    private final String displayName;

    BillingCycle(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    public LocalDate calculateNextDueDate(LocalDate currentDate) {
        if (currentDate == null) {
            currentDate = LocalDate.now();
        }
        return switch (this) {
            case WEEKLY -> currentDate.plusWeeks(1);
            case MONTHLY -> currentDate.plusMonths(1);
            case YEARLY -> currentDate.plusYears(1);
        };
    }
}

package com.aswin.financetracker.model.enums;

public enum IncomeSource {
    SALARY("Salary", "bg-emerald-100 text-emerald-800"),
    SCHOLARSHIP("Scholarship", "bg-blue-100 text-blue-800"),
    FREELANCE("Freelance", "bg-purple-100 text-purple-800"),
    GIFT("Gift", "bg-pink-100 text-pink-800"),
    OTHER("Other", "bg-gray-100 text-gray-800");

    private final String displayName;
    private final String badgeClass;

    IncomeSource(String displayName, String badgeClass) {
        this.displayName = displayName;
        this.badgeClass = badgeClass;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getBadgeClass() {
        return badgeClass;
    }
}

package com.aswin.financetracker.model.enums;

public enum ExpenseCategory {
    FOOD("Food & Dining", "badge-food", "#10B981"),
    TRAVEL("Travel & Transport", "badge-travel", "#3B82F6"),
    EDUCATION("Education", "badge-education", "#8B5CF6"),
    SHOPPING("Shopping", "badge-shopping", "#EC4899"),
    BILLS("Bills & Utilities", "badge-bills", "#F59E0B"),
    HEALTHCARE("Healthcare", "badge-healthcare", "#EF4444"),
    ENTERTAINMENT("Entertainment", "badge-entertainment", "#6366F1"),
    OTHERS("Others", "badge-others", "#6B7280");

    private final String displayName;
    private final String badgeClass;
    private final String colorHex;

    ExpenseCategory(String displayName, String badgeClass, String colorHex) {
        this.displayName = displayName;
        this.badgeClass = badgeClass;
        this.colorHex = colorHex;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getBadgeClass() {
        return badgeClass;
    }

    public String getColorHex() {
        return colorHex;
    }
}

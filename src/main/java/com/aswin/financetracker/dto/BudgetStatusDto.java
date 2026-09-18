package com.aswin.financetracker.dto;

import com.aswin.financetracker.model.enums.ExpenseCategory;
import java.math.BigDecimal;
import java.math.RoundingMode;

public class BudgetStatusDto {

    private Long id;
    private ExpenseCategory category;
    private BigDecimal monthlyLimit;
    private BigDecimal actualSpent;
    private BigDecimal remainingAmount;
    private double percentUsed;
    private boolean overBudget;

    public BudgetStatusDto() {
    }

    public BudgetStatusDto(Long id, ExpenseCategory category, BigDecimal monthlyLimit, BigDecimal actualSpent) {
        this.id = id;
        this.category = category;
        this.monthlyLimit = monthlyLimit != null ? monthlyLimit : BigDecimal.ZERO;
        this.actualSpent = actualSpent != null ? actualSpent : BigDecimal.ZERO;

        this.remainingAmount = this.monthlyLimit.subtract(this.actualSpent);
        this.overBudget = this.actualSpent.compareTo(this.monthlyLimit) > 0;

        if (this.monthlyLimit.compareTo(BigDecimal.ZERO) > 0) {
            this.percentUsed = this.actualSpent
                .divide(this.monthlyLimit, 4, RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100))
                .doubleValue();
        } else {
            this.percentUsed = this.actualSpent.compareTo(BigDecimal.ZERO) > 0 ? 100.0 : 0.0;
        }
    }

    public String getStatusBadgeClass() {
        if (overBudget) {
            return "badge-danger";
        } else if (percentUsed >= 85.0) {
            return "badge-warning";
        } else {
            return "badge-success";
        }
    }

    public String getProgressBarClass() {
        if (overBudget) {
            return "progress-danger";
        } else if (percentUsed >= 85.0) {
            return "progress-warning";
        } else {
            return "progress-success";
        }
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public ExpenseCategory getCategory() {
        return category;
    }

    public void setCategory(ExpenseCategory category) {
        this.category = category;
    }

    public BigDecimal getMonthlyLimit() {
        return monthlyLimit;
    }

    public void setMonthlyLimit(BigDecimal monthlyLimit) {
        this.monthlyLimit = monthlyLimit;
    }

    public BigDecimal getActualSpent() {
        return actualSpent;
    }

    public void setActualSpent(BigDecimal actualSpent) {
        this.actualSpent = actualSpent;
    }

    public BigDecimal getRemainingAmount() {
        return remainingAmount;
    }

    public void setRemainingAmount(BigDecimal remainingAmount) {
        this.remainingAmount = remainingAmount;
    }

    public double getPercentUsed() {
        return percentUsed;
    }

    public void setPercentUsed(double percentUsed) {
        this.percentUsed = percentUsed;
    }

    public boolean isOverBudget() {
        return overBudget;
    }

    public void setOverBudget(boolean overBudget) {
        this.overBudget = overBudget;
    }
}

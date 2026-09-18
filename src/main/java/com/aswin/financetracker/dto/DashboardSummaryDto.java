package com.aswin.financetracker.dto;

import java.math.BigDecimal;

public class DashboardSummaryDto {

    private BigDecimal totalIncome = BigDecimal.ZERO;
    private BigDecimal totalExpense = BigDecimal.ZERO;
    private BigDecimal availableBalance = BigDecimal.ZERO;

    private BigDecimal thisMonthIncome = BigDecimal.ZERO;
    private BigDecimal thisMonthExpense = BigDecimal.ZERO;
    private BigDecimal thisMonthSavings = BigDecimal.ZERO;

    private BigDecimal lastMonthIncome = BigDecimal.ZERO;
    private BigDecimal lastMonthExpense = BigDecimal.ZERO;
    private BigDecimal lastMonthSavings = BigDecimal.ZERO;

    private BigDecimal savingsMoMChangePercent = BigDecimal.ZERO;
    private BigDecimal expenseMoMChangePercent = BigDecimal.ZERO;
    private BigDecimal incomeMoMChangePercent = BigDecimal.ZERO;

    private String currentMonthName;
    private String lastMonthName;

    public DashboardSummaryDto() {
    }

    public BigDecimal getTotalIncome() {
        return totalIncome;
    }

    public void setTotalIncome(BigDecimal totalIncome) {
        this.totalIncome = totalIncome;
    }

    public BigDecimal getTotalExpense() {
        return totalExpense;
    }

    public void setTotalExpense(BigDecimal totalExpense) {
        this.totalExpense = totalExpense;
    }

    public BigDecimal getAvailableBalance() {
        return availableBalance;
    }

    public void setAvailableBalance(BigDecimal availableBalance) {
        this.availableBalance = availableBalance;
    }

    public BigDecimal getThisMonthIncome() {
        return thisMonthIncome;
    }

    public void setThisMonthIncome(BigDecimal thisMonthIncome) {
        this.thisMonthIncome = thisMonthIncome;
    }

    public BigDecimal getThisMonthExpense() {
        return thisMonthExpense;
    }

    public void setThisMonthExpense(BigDecimal thisMonthExpense) {
        this.thisMonthExpense = thisMonthExpense;
    }

    public BigDecimal getThisMonthSavings() {
        return thisMonthSavings;
    }

    public void setThisMonthSavings(BigDecimal thisMonthSavings) {
        this.thisMonthSavings = thisMonthSavings;
    }

    public BigDecimal getLastMonthIncome() {
        return lastMonthIncome;
    }

    public void setLastMonthIncome(BigDecimal lastMonthIncome) {
        this.lastMonthIncome = lastMonthIncome;
    }

    public BigDecimal getLastMonthExpense() {
        return lastMonthExpense;
    }

    public void setLastMonthExpense(BigDecimal lastMonthExpense) {
        this.lastMonthExpense = lastMonthExpense;
    }

    public BigDecimal getLastMonthSavings() {
        return lastMonthSavings;
    }

    public void setLastMonthSavings(BigDecimal lastMonthSavings) {
        this.lastMonthSavings = lastMonthSavings;
    }

    public BigDecimal getSavingsMoMChangePercent() {
        return savingsMoMChangePercent;
    }

    public void setSavingsMoMChangePercent(BigDecimal savingsMoMChangePercent) {
        this.savingsMoMChangePercent = savingsMoMChangePercent;
    }

    public BigDecimal getExpenseMoMChangePercent() {
        return expenseMoMChangePercent;
    }

    public void setExpenseMoMChangePercent(BigDecimal expenseMoMChangePercent) {
        this.expenseMoMChangePercent = expenseMoMChangePercent;
    }

    public BigDecimal getIncomeMoMChangePercent() {
        return incomeMoMChangePercent;
    }

    public void setIncomeMoMChangePercent(BigDecimal incomeMoMChangePercent) {
        this.incomeMoMChangePercent = incomeMoMChangePercent;
    }

    public String getCurrentMonthName() {
        return currentMonthName;
    }

    public void setCurrentMonthName(String currentMonthName) {
        this.currentMonthName = currentMonthName;
    }

    public String getLastMonthName() {
        return lastMonthName;
    }

    public void setLastMonthName(String lastMonthName) {
        this.lastMonthName = lastMonthName;
    }
}

package com.aswin.financetracker.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public class TransactionViewDto {

    private Long id;
    private String type; // "INCOME" or "EXPENSE"
    private BigDecimal amount;
    private LocalDate date;
    private String categoryOrSource;
    private String categoryOrSourceRaw;
    private String descriptionOrNote;
    private String badgeClass;
    private String colorHex;

    public TransactionViewDto() {
    }

    public TransactionViewDto(Long id, String type, BigDecimal amount, LocalDate date,
                              String categoryOrSource, String categoryOrSourceRaw,
                              String descriptionOrNote, String badgeClass, String colorHex) {
        this.id = id;
        this.type = type;
        this.amount = amount;
        this.date = date;
        this.categoryOrSource = categoryOrSource;
        this.categoryOrSourceRaw = categoryOrSourceRaw;
        this.descriptionOrNote = descriptionOrNote;
        this.badgeClass = badgeClass;
        this.colorHex = colorHex;
    }

    public boolean isIncome() {
        return "INCOME".equalsIgnoreCase(this.type);
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public String getCategoryOrSource() {
        return categoryOrSource;
    }

    public void setCategoryOrSource(String categoryOrSource) {
        this.categoryOrSource = categoryOrSource;
    }

    public String getCategoryOrSourceRaw() {
        return categoryOrSourceRaw;
    }

    public void setCategoryOrSourceRaw(String categoryOrSourceRaw) {
        this.categoryOrSourceRaw = categoryOrSourceRaw;
    }

    public String getDescriptionOrNote() {
        return descriptionOrNote;
    }

    public void setDescriptionOrNote(String descriptionOrNote) {
        this.descriptionOrNote = descriptionOrNote;
    }

    public String getBadgeClass() {
        return badgeClass;
    }

    public void setBadgeClass(String badgeClass) {
        this.badgeClass = badgeClass;
    }

    public String getColorHex() {
        return colorHex;
    }

    public void setColorHex(String colorHex) {
        this.colorHex = colorHex;
    }
}

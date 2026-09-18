package com.aswin.financetracker.dto;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class ChartDataDto {

    public static class CategoryPieChart {
        private List<String> labels = new ArrayList<>();
        private List<BigDecimal> data = new ArrayList<>();
        private List<String> backgroundColor = new ArrayList<>();

        public CategoryPieChart() {
        }

        public CategoryPieChart(List<String> labels, List<BigDecimal> data, List<String> backgroundColor) {
            this.labels = labels;
            this.data = data;
            this.backgroundColor = backgroundColor;
        }

        public List<String> getLabels() {
            return labels;
        }

        public void setLabels(List<String> labels) {
            this.labels = labels;
        }

        public List<BigDecimal> getData() {
            return data;
        }

        public void setData(List<BigDecimal> data) {
            this.data = data;
        }

        public List<String> getBackgroundColor() {
            return backgroundColor;
        }

        public void setBackgroundColor(List<String> backgroundColor) {
            this.backgroundColor = backgroundColor;
        }
    }

    public static class IncomeExpenseBarChart {
        private List<String> labels = new ArrayList<>();
        private List<BigDecimal> incomeData = new ArrayList<>();
        private List<BigDecimal> expenseData = new ArrayList<>();

        public IncomeExpenseBarChart() {
        }

        public IncomeExpenseBarChart(List<String> labels, List<BigDecimal> incomeData, List<BigDecimal> expenseData) {
            this.labels = labels;
            this.incomeData = incomeData;
            this.expenseData = expenseData;
        }

        public List<String> getLabels() {
            return labels;
        }

        public void setLabels(List<String> labels) {
            this.labels = labels;
        }

        public List<BigDecimal> getIncomeData() {
            return incomeData;
        }

        public void setIncomeData(List<BigDecimal> incomeData) {
            this.incomeData = incomeData;
        }

        public List<BigDecimal> getExpenseData() {
            return expenseData;
        }

        public void setExpenseData(List<BigDecimal> expenseData) {
            this.expenseData = expenseData;
        }
    }

    public static class SavingsTrendLineChart {
        private List<String> labels = new ArrayList<>();
        private List<BigDecimal> savingsData = new ArrayList<>();

        public SavingsTrendLineChart() {
        }

        public SavingsTrendLineChart(List<String> labels, List<BigDecimal> savingsData) {
            this.labels = labels;
            this.savingsData = savingsData;
        }

        public List<String> getLabels() {
            return labels;
        }

        public void setLabels(List<String> labels) {
            this.labels = labels;
        }

        public List<BigDecimal> getSavingsData() {
            return savingsData;
        }

        public void setSavingsData(List<BigDecimal> savingsData) {
            this.savingsData = savingsData;
        }
    }
}

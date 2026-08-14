package com.rob.houserental.financial;

public class MonthlyFinancialTrend {

    private String month;
    private double expectedRent;
    private double collectedRent;
    private double expenses;
    private double netIncome;

    public MonthlyFinancialTrend() {
    }

    public MonthlyFinancialTrend(String month, double expectedRent, double collectedRent, double expenses, double netIncome) {
        this.month = month;
        this.expectedRent = expectedRent;
        this.collectedRent = collectedRent;
        this.expenses = expenses;
        this.netIncome = netIncome;
    }

    public String getMonth() {
        return month;
    }

    public void setMonth(String month) {
        this.month = month;
    }

    public double getExpectedRent() {
        return expectedRent;
    }

    public void setExpectedRent(double expectedRent) {
        this.expectedRent = expectedRent;
    }

    public double getCollectedRent() {
        return collectedRent;
    }

    public void setCollectedRent(double collectedRent) {
        this.collectedRent = collectedRent;
    }

    public double getExpenses() {
        return expenses;
    }

    public void setExpenses(double expenses) {
        this.expenses = expenses;
    }

    public double getNetIncome() {
        return netIncome;
    }

    public void setNetIncome(double netIncome) {
        this.netIncome = netIncome;
    }
}

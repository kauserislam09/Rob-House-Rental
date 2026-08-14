package com.rob.houserental.model;

public class MonthlyBillSummary {

    public double totalExpected;
    public double totalCollected;
    public double totalOutstanding;
    public double totalOverdue;

    public MonthlyBillSummary(
            double totalExpected,
            double totalCollected,
            double totalOutstanding,
            double totalOverdue
    ) {
        this.totalExpected = totalExpected;
        this.totalCollected = totalCollected;
        this.totalOutstanding = totalOutstanding;
        this.totalOverdue = totalOverdue;
    }
}

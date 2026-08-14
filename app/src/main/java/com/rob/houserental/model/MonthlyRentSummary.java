package com.rob.houserental.model;

public class MonthlyRentSummary {

    public double totalExpected;
    public double totalCollected;
    public double totalOutstanding;
    public double totalOverdue;

    public MonthlyRentSummary(
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

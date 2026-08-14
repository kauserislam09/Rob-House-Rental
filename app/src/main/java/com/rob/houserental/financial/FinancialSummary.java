package com.rob.houserental.financial;

public class FinancialSummary {

    private double expectedRent;
    private double collectedRent;
    private double outstandingRent;
    private double overdueRent;
    private double activeExpenses;
    private double utilityBillsPaid;
    private double netIncome;

    private int totalUnits;
    private int occupiedUnits;
    private int vacantUnits;
    private double occupancyRate;
    private double collectionRate;

    public FinancialSummary() {
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

    public double getOutstandingRent() {
        return outstandingRent;
    }

    public void setOutstandingRent(double outstandingRent) {
        this.outstandingRent = outstandingRent;
    }

    public double getOverdueRent() {
        return overdueRent;
    }

    public void setOverdueRent(double overdueRent) {
        this.overdueRent = overdueRent;
    }

    public double getActiveExpenses() {
        return activeExpenses;
    }

    public void setActiveExpenses(double activeExpenses) {
        this.activeExpenses = activeExpenses;
    }

    public double getUtilityBillsPaid() {
        return utilityBillsPaid;
    }

    public void setUtilityBillsPaid(double utilityBillsPaid) {
        this.utilityBillsPaid = utilityBillsPaid;
    }

    public double getNetIncome() {
        return netIncome;
    }

    public void setNetIncome(double netIncome) {
        this.netIncome = netIncome;
    }

    public int getTotalUnits() {
        return totalUnits;
    }

    public void setTotalUnits(int totalUnits) {
        this.totalUnits = totalUnits;
    }

    public int getOccupiedUnits() {
        return occupiedUnits;
    }

    public void setOccupiedUnits(int occupiedUnits) {
        this.occupiedUnits = occupiedUnits;
    }

    public int getVacantUnits() {
        return vacantUnits;
    }

    public void setVacantUnits(int vacantUnits) {
        this.vacantUnits = vacantUnits;
    }

    public double getOccupancyRate() {
        return occupancyRate;
    }

    public void setOccupancyRate(double occupancyRate) {
        this.occupancyRate = occupancyRate;
    }

    public double getCollectionRate() {
        return collectionRate;
    }

    public void setCollectionRate(double collectionRate) {
        this.collectionRate = collectionRate;
    }
}

package com.rob.houserental.financial;

public class PropertyFinancialSummary {

    private long propertyId;
    private String propertyName;
    private int totalUnits;
    private int occupiedUnits;
    private int vacantUnits;

    private double expectedRent;
    private double collectedRent;
    private double outstandingRent;
    private double expenses;
    private double utilityBillsPaid;
    private double netIncome;

    private double collectionRate;
    private double occupancyRate;

    public PropertyFinancialSummary() {
    }

    public long getPropertyId() {
        return propertyId;
    }

    public void setPropertyId(long propertyId) {
        this.propertyId = propertyId;
    }

    public String getPropertyName() {
        return propertyName;
    }

    public void setPropertyName(String propertyName) {
        this.propertyName = propertyName;
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

    public double getExpenses() {
        return expenses;
    }

    public void setExpenses(double expenses) {
        this.expenses = expenses;
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

    public double getCollectionRate() {
        return collectionRate;
    }

    public void setCollectionRate(double collectionRate) {
        this.collectionRate = collectionRate;
    }

    public double getOccupancyRate() {
        return occupancyRate;
    }

    public void setOccupancyRate(double occupancyRate) {
        this.occupancyRate = occupancyRate;
    }
}

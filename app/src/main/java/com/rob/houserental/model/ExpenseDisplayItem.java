package com.rob.houserental.model;

public class ExpenseDisplayItem {

    public long id;
    public long propertyId;
    public long unitId;

    public String category;
    public double amount;

    public String expenseDate;
    public String expenseMonth;

    public String description;

    public String receiptPath;
    public String receiptName;
    public String receiptMimeType;

    public String notes;
    public boolean isArchived;

    public long createdAt;
    public long updatedAt;

    public String propertyName;
    public String unitNumber;
    public int floor;
}

package com.rob.houserental.model;

public class RentRecordDisplayItem {

    public long id;
    public long tenancyId;
    public String billingMonth;
    public String dueDate;
    public double amountDue;
    public double amountPaid;
    public double remainingAmount;
    public String status;
    public String lastPaymentDate;
    public String paymentMethod;
    public String notes;

    public String tenantName;
    public String tenantPhone;
    public String unitNumber;
    public int floor;
    public String propertyName;
    public long propertyId;
}

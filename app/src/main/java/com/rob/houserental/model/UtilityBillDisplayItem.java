package com.rob.houserental.model;

public class UtilityBillDisplayItem {

    public long id;
    public long propertyId;
    public long unitId;
    public long tenancyId;

    public String billType;
    public String billingMonth;
    public String dueDate;

    public double amountDue;
    public double amountPaid;
    public double remainingAmount;

    public String status;

    public String meterNumber;
    public double previousReading;
    public double currentReading;
    public double unitsConsumed;
    public double ratePerUnit;
    public double fixedCharge;
    public double vatOrTax;
    public String billNumber;

    public String lastPaymentDate;
    public String paymentMethod;
    public String notes;

    public String propertyName;
    public String unitNumber;
    public int floor;
    public String tenantName;
    public String tenantPhone;
}

package com.rob.houserental.model;

public class PaymentDisplayItem {

    public long id;
    public long rentRecordId;
    public double amount;
    public String paymentDate;
    public String paymentMethod;
    public String reference;
    public String notes;
    public String tenantName;
    public String tenantPhone;
    public String propertyName;
    public long propertyId;
    public String unitNumber;
    public int floor;
    public String billingMonth;

    public PaymentDisplayItem() {
    }
}

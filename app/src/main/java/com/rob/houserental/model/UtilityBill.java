package com.rob.houserental.model;

import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Ignore;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(
        tableName = "utility_bills",
        foreignKeys = {
                @ForeignKey(
                        entity = Property.class,
                        parentColumns = "id",
                        childColumns = "propertyId",
                        onDelete = ForeignKey.RESTRICT
                )
        },
        indices = {
                @Index("propertyId"),
                @Index("unitId"),
                @Index("tenancyId"),
                @Index("billingMonth"),
                @Index("billType"),
                @Index("status"),
                @Index(value = {"propertyId", "unitId", "billType", "billingMonth"}, unique = true)
        }
)
public class UtilityBill {

    @PrimaryKey(autoGenerate = true)
    private long id;

    private long propertyId;
    private long unitId; // 0 if whole property
    private long tenancyId; // 0 if none

    private String billType; // ELECTRICITY, WATER, GAS, INTERNET, SERVICE_CHARGE, OTHER
    private String billingMonth; // e.g. "2026-08"
    private String dueDate; // e.g. "15 Aug 2026"

    private double amountDue;
    private double amountPaid;
    private double remainingAmount;

    private String status; // UNPAID, PARTIAL, PAID, OVERDUE, WAIVED

    // Utility-specific fields
    private String meterNumber;
    private double previousReading;
    private double currentReading;
    private double unitsConsumed;
    private double ratePerUnit;
    private double fixedCharge;
    private double vatOrTax;
    private String billNumber; // e.g. Consumer No. / Invoice No.

    private String lastPaymentDate;
    private String paymentMethod;
    private String notes;

    private long createdAt;
    private long updatedAt;

    public UtilityBill() {
        this.status = "UNPAID";
        this.billType = "ELECTRICITY";
    }

    @Ignore
    public UtilityBill(
            long propertyId,
            long unitId,
            long tenancyId,
            String billType,
            String billingMonth,
            String dueDate,
            double amountDue,
            double amountPaid,
            double remainingAmount,
            String status,
            String meterNumber,
            double previousReading,
            double currentReading,
            double unitsConsumed,
            double ratePerUnit,
            double fixedCharge,
            double vatOrTax,
            String billNumber,
            String lastPaymentDate,
            String paymentMethod,
            String notes,
            long createdAt,
            long updatedAt
    ) {
        this.propertyId = propertyId;
        this.unitId = unitId;
        this.tenancyId = tenancyId;
        this.billType = billType != null ? billType : "ELECTRICITY";
        this.billingMonth = billingMonth;
        this.dueDate = dueDate;
        this.amountDue = amountDue;
        this.amountPaid = amountPaid;
        this.remainingAmount = remainingAmount;
        this.status = status != null ? status : "UNPAID";
        this.meterNumber = meterNumber;
        this.previousReading = previousReading;
        this.currentReading = currentReading;
        this.unitsConsumed = unitsConsumed;
        this.ratePerUnit = ratePerUnit;
        this.fixedCharge = fixedCharge;
        this.vatOrTax = vatOrTax;
        this.billNumber = billNumber;
        this.lastPaymentDate = lastPaymentDate;
        this.paymentMethod = paymentMethod;
        this.notes = notes;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getPropertyId() {
        return propertyId;
    }

    public void setPropertyId(long propertyId) {
        this.propertyId = propertyId;
    }

    public long getUnitId() {
        return unitId;
    }

    public void setUnitId(long unitId) {
        this.unitId = unitId;
    }

    public long getTenancyId() {
        return tenancyId;
    }

    public void setTenancyId(long tenancyId) {
        this.tenancyId = tenancyId;
    }

    public String getBillType() {
        return billType != null ? billType : "ELECTRICITY";
    }

    public void setBillType(String billType) {
        this.billType = billType;
    }

    public String getBillingMonth() {
        return billingMonth;
    }

    public void setBillingMonth(String billingMonth) {
        this.billingMonth = billingMonth;
    }

    public String getDueDate() {
        return dueDate;
    }

    public void setDueDate(String dueDate) {
        this.dueDate = dueDate;
    }

    public double getAmountDue() {
        return amountDue;
    }

    public void setAmountDue(double amountDue) {
        this.amountDue = amountDue;
    }

    public double getAmountPaid() {
        return amountPaid;
    }

    public void setAmountPaid(double amountPaid) {
        this.amountPaid = amountPaid;
    }

    public double getRemainingAmount() {
        return remainingAmount;
    }

    public void setRemainingAmount(double remainingAmount) {
        this.remainingAmount = remainingAmount;
    }

    public String getStatus() {
        return status != null ? status : "UNPAID";
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getMeterNumber() {
        return meterNumber;
    }

    public void setMeterNumber(String meterNumber) {
        this.meterNumber = meterNumber;
    }

    public double getPreviousReading() {
        return previousReading;
    }

    public void setPreviousReading(double previousReading) {
        this.previousReading = previousReading;
    }

    public double getCurrentReading() {
        return currentReading;
    }

    public void setCurrentReading(double currentReading) {
        this.currentReading = currentReading;
    }

    public double getUnitsConsumed() {
        return unitsConsumed;
    }

    public void setUnitsConsumed(double unitsConsumed) {
        this.unitsConsumed = unitsConsumed;
    }

    public double getRatePerUnit() {
        return ratePerUnit;
    }

    public void setRatePerUnit(double ratePerUnit) {
        this.ratePerUnit = ratePerUnit;
    }

    public double getFixedCharge() {
        return fixedCharge;
    }

    public void setFixedCharge(double fixedCharge) {
        this.fixedCharge = fixedCharge;
    }

    public double getVatOrTax() {
        return vatOrTax;
    }

    public void setVatOrTax(double vatOrTax) {
        this.vatOrTax = vatOrTax;
    }

    public String getBillNumber() {
        return billNumber;
    }

    public void setBillNumber(String billNumber) {
        this.billNumber = billNumber;
    }

    public String getLastPaymentDate() {
        return lastPaymentDate;
    }

    public void setLastPaymentDate(String lastPaymentDate) {
        this.lastPaymentDate = lastPaymentDate;
    }

    public String getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(String paymentMethod) {
        this.paymentMethod = paymentMethod;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public long getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(long createdAt) {
        this.createdAt = createdAt;
    }

    public long getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(long updatedAt) {
        this.updatedAt = updatedAt;
    }
}

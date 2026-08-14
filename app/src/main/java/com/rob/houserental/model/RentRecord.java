package com.rob.houserental.model;

import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Ignore;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(
        tableName = "rent_records",
        foreignKeys = {
                @ForeignKey(
                        entity = Tenancy.class,
                        parentColumns = "id",
                        childColumns = "tenancyId",
                        onDelete = ForeignKey.RESTRICT
                )
        },
        indices = {
                @Index(value = {"tenancyId", "billingMonth"}, unique = true),
                @Index("tenancyId"),
                @Index("billingMonth"),
                @Index("status")
        }
)
public class RentRecord {

    @PrimaryKey(autoGenerate = true)
    private long id;

    private long tenancyId;

    private String billingMonth; // e.g. "2026-08" or "August 2026"
    private String dueDate; // e.g. "10 Aug 2026"

    private double amountDue;
    private double amountPaid;
    private double remainingAmount;

    private String status; // UNPAID, PARTIAL, PAID, OVERDUE, WAIVED

    private String lastPaymentDate;
    private String paymentMethod;

    private String notes;

    private long createdAt;
    private long updatedAt;

    public RentRecord() {
        this.status = "UNPAID";
    }

    @Ignore
    public RentRecord(
            long tenancyId,
            String billingMonth,
            String dueDate,
            double amountDue,
            double amountPaid,
            double remainingAmount,
            String status,
            String lastPaymentDate,
            String paymentMethod,
            String notes,
            long createdAt,
            long updatedAt
    ) {
        this.tenancyId = tenancyId;
        this.billingMonth = billingMonth;
        this.dueDate = dueDate;
        this.amountDue = amountDue;
        this.amountPaid = amountPaid;
        this.remainingAmount = remainingAmount;
        this.status = status != null ? status : "UNPAID";
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

    public long getTenancyId() {
        return tenancyId;
    }

    public void setTenancyId(long tenancyId) {
        this.tenancyId = tenancyId;
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

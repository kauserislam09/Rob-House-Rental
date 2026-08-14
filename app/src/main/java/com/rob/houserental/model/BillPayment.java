package com.rob.houserental.model;

import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Ignore;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(
        tableName = "bill_payments",
        foreignKeys = {
                @ForeignKey(
                        entity = UtilityBill.class,
                        parentColumns = "id",
                        childColumns = "billId",
                        onDelete = ForeignKey.CASCADE
                )
        },
        indices = {
                @Index("billId"),
                @Index("paymentDate")
        }
)
public class BillPayment {

    @PrimaryKey(autoGenerate = true)
    private long id;

    private long billId;

    private double amount;

    private String paymentDate;

    private String paymentMethod; // Cash, Mobile Banking, Bank Transfer, Card, Other

    private String reference; // e.g. receipt or transaction ID

    private String notes;

    private long createdAt;

    public BillPayment() {
    }

    @Ignore
    public BillPayment(
            long billId,
            double amount,
            String paymentDate,
            String paymentMethod,
            String reference,
            String notes,
            long createdAt
    ) {
        this.billId = billId;
        this.amount = amount;
        this.paymentDate = paymentDate;
        this.paymentMethod = paymentMethod;
        this.reference = reference;
        this.notes = notes;
        this.createdAt = createdAt;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getBillId() {
        return billId;
    }

    public void setBillId(long billId) {
        this.billId = billId;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public String getPaymentDate() {
        return paymentDate;
    }

    public void setPaymentDate(String paymentDate) {
        this.paymentDate = paymentDate;
    }

    public String getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(String paymentMethod) {
        this.paymentMethod = paymentMethod;
    }

    public String getReference() {
        return reference;
    }

    public void setReference(String reference) {
        this.reference = reference;
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
}

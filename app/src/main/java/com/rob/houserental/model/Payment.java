package com.rob.houserental.model;

import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Ignore;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(
        tableName = "payments",
        foreignKeys = {
                @ForeignKey(
                        entity = RentRecord.class,
                        parentColumns = "id",
                        childColumns = "rentRecordId",
                        onDelete = ForeignKey.CASCADE
                )
        },
        indices = {
                @Index("rentRecordId"),
                @Index("paymentDate")
        }
)
public class Payment {

    @PrimaryKey(autoGenerate = true)
    private long id;

    private long rentRecordId;

    private double amount;

    private String paymentDate;

    private String paymentMethod; // Cash, Bank Transfer, Mobile Banking, Card, Other

    private String reference; // e.g. receipt number or transaction id

    private String notes;

    private long createdAt;

    public Payment() {
    }

    @Ignore
    public Payment(
            long rentRecordId,
            double amount,
            String paymentDate,
            String paymentMethod,
            String reference,
            String notes,
            long createdAt
    ) {
        this.rentRecordId = rentRecordId;
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

    public long getRentRecordId() {
        return rentRecordId;
    }

    public void setRentRecordId(long rentRecordId) {
        this.rentRecordId = rentRecordId;
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

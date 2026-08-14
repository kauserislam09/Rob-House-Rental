package com.rob.houserental.model;

import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Ignore;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(
        tableName = "expenses",
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
                @Index("category"),
                @Index("expenseMonth"),
                @Index("expenseDate"),
                @Index("isArchived")
        }
)
public class Expense {

    @PrimaryKey(autoGenerate = true)
    private long id;

    private long propertyId;
    private long unitId; // 0 if Whole Property (Entire Building)

    private String category; // REPAIR, MAINTENANCE, CLEANING, SECURITY, GENERATOR, PLUMBING, ELECTRICAL, PAINTING, RENOVATION, PROPERTY_TAX, SERVICE_CHARGE, OTHER
    private double amount;

    private String expenseDate; // e.g. "10 Aug 2026"
    private String expenseMonth; // e.g. "2026-08"

    private String description;

    private String receiptPath;
    private String receiptName;
    private String receiptMimeType;

    private String notes;

    private boolean isArchived;

    private long createdAt;
    private long updatedAt;

    public Expense() {
        this.category = "REPAIR";
        this.isArchived = false;
    }

    @Ignore
    public Expense(
            long propertyId,
            long unitId,
            String category,
            double amount,
            String expenseDate,
            String expenseMonth,
            String description,
            String receiptPath,
            String receiptName,
            String receiptMimeType,
            String notes,
            boolean isArchived,
            long createdAt,
            long updatedAt
    ) {
        this.propertyId = propertyId;
        this.unitId = unitId;
        this.category = category != null ? category : "REPAIR";
        this.amount = amount;
        this.expenseDate = expenseDate;
        this.expenseMonth = expenseMonth;
        this.description = description;
        this.receiptPath = receiptPath;
        this.receiptName = receiptName;
        this.receiptMimeType = receiptMimeType;
        this.notes = notes;
        this.isArchived = isArchived;
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

    public String getCategory() {
        return category != null ? category : "REPAIR";
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public String getExpenseDate() {
        return expenseDate;
    }

    public void setExpenseDate(String expenseDate) {
        this.expenseDate = expenseDate;
    }

    public String getExpenseMonth() {
        return expenseMonth;
    }

    public void setExpenseMonth(String expenseMonth) {
        this.expenseMonth = expenseMonth;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getReceiptPath() {
        return receiptPath;
    }

    public void setReceiptPath(String receiptPath) {
        this.receiptPath = receiptPath;
    }

    public String getReceiptName() {
        return receiptName;
    }

    public void setReceiptName(String receiptName) {
        this.receiptName = receiptName;
    }

    public String getReceiptMimeType() {
        return receiptMimeType;
    }

    public void setReceiptMimeType(String receiptMimeType) {
        this.receiptMimeType = receiptMimeType;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public boolean isArchived() {
        return isArchived;
    }

    public void setArchived(boolean archived) {
        isArchived = archived;
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

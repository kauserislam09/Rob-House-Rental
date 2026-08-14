package com.rob.houserental.model;

import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(
        tableName = "maintenance_records",
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
                @Index("priority"),
                @Index("status"),
                @Index("scheduledDate"),
                @Index("completedDate")
        }
)
public class MaintenanceRecord {

    @PrimaryKey(autoGenerate = true)
    private long id;

    private long propertyId;
    private Long unitId; // Nullable Long: NULL = Property-wide maintenance, not 0

    private String title;
    private String description;

    private String category; // PLUMBING, ELECTRICAL, PAINTING, AC, APPLIANCE, CLEANING, STRUCTURAL, SECURITY, WATER, GAS, OTHER
    private String priority; // LOW, MEDIUM, HIGH, URGENT
    private String status; // OPEN, SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED

    private double estimatedCost;
    private double actualCost;

    private Long expenseId; // Nullable Long: ID of Expense record if explicitly converted

    private String vendorName;
    private String vendorPhone;

    private String scheduledDate; // yyyy-MM-dd
    private String completedDate; // yyyy-MM-dd

    private String notes;

    private long createdAt;
    private long updatedAt;

    public MaintenanceRecord() {
        this.unitId = null;
        this.category = "OTHER";
        this.priority = "MEDIUM";
        this.status = "OPEN";
        this.estimatedCost = 0.0;
        this.actualCost = 0.0;
        this.expenseId = null;
        this.createdAt = System.currentTimeMillis();
        this.updatedAt = System.currentTimeMillis();
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

    public Long getUnitId() {
        return unitId;
    }

    public void setUnitId(Long unitId) {
        this.unitId = unitId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getPriority() {
        return priority;
    }

    public void setPriority(String priority) {
        this.priority = priority;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public double getEstimatedCost() {
        return estimatedCost;
    }

    public void setEstimatedCost(double estimatedCost) {
        this.estimatedCost = estimatedCost;
    }

    public double getActualCost() {
        return actualCost;
    }

    public void setActualCost(double actualCost) {
        this.actualCost = actualCost;
    }

    public Long getExpenseId() {
        return expenseId;
    }

    public void setExpenseId(Long expenseId) {
        this.expenseId = expenseId;
    }

    public String getVendorName() {
        return vendorName;
    }

    public void setVendorName(String vendorName) {
        this.vendorName = vendorName;
    }

    public String getVendorPhone() {
        return vendorPhone;
    }

    public void setVendorPhone(String vendorPhone) {
        this.vendorPhone = vendorPhone;
    }

    public String getScheduledDate() {
        return scheduledDate;
    }

    public void setScheduledDate(String scheduledDate) {
        this.scheduledDate = scheduledDate;
    }

    public String getCompletedDate() {
        return completedDate;
    }

    public void setCompletedDate(String completedDate) {
        this.completedDate = completedDate;
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

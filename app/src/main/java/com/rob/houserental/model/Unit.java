package com.rob.houserental.model;

import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(
        tableName = "units",
        foreignKeys = @ForeignKey(
                entity = Property.class,
                parentColumns = "id",
                childColumns = "propertyId",
                onDelete = ForeignKey.RESTRICT
        ),
        indices = {
                @Index(value = "propertyId"),
                @Index(value = {"propertyId", "unitNumber"}, unique = true)
        }
)
public class Unit {

    @PrimaryKey(autoGenerate = true)
    private long id;

    private long propertyId;

    private String unitNumber;

    private int floor;

    private String unitType;

    private double monthlyRent;

    private double securityDeposit;

    private String status;

    private String notes;

    private long createdAt;

    private long updatedAt;

    public Unit() {
    }

    @androidx.room.Ignore
    public Unit(
            long propertyId,
            String unitNumber,
            int floor,
            String unitType,
            double monthlyRent,
            double securityDeposit,
            String status,
            String notes,
            long createdAt,
            long updatedAt
    ) {
        this.propertyId = propertyId;
        this.unitNumber = unitNumber;
        this.floor = floor;
        this.unitType = unitType;
        this.monthlyRent = monthlyRent;
        this.securityDeposit = securityDeposit;
        this.status = status;
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

    public String getUnitNumber() {
        return unitNumber;
    }

    public void setUnitNumber(String unitNumber) {
        this.unitNumber = unitNumber;
    }

    public int getFloor() {
        return floor;
    }

    public void setFloor(int floor) {
        this.floor = floor;
    }

    public String getUnitType() {
        return unitType;
    }

    public void setUnitType(String unitType) {
        this.unitType = unitType;
    }

    public double getMonthlyRent() {
        return monthlyRent;
    }

    public void setMonthlyRent(double monthlyRent) {
        this.monthlyRent = monthlyRent;
    }

    public double getSecurityDeposit() {
        return securityDeposit;
    }

    public void setSecurityDeposit(double securityDeposit) {
        this.securityDeposit = securityDeposit;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
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
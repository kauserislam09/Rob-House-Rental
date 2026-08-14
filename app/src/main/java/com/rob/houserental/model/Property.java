package com.rob.houserental.model;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "properties")
public class Property {

    @PrimaryKey(autoGenerate = true)
    private long id;

    private String name;
    private String address;
    private String propertyType;
    private int numberOfFloors;
    private String notes;

    private long createdAt;
    private long updatedAt;

    public Property() {
    }

    @androidx.room.Ignore
    public Property(
            String name,
            String address,
            String propertyType,
            int numberOfFloors,
            String notes,
            long createdAt,
            long updatedAt
    ) {
        this.name = name;
        this.address = address;
        this.propertyType = propertyType;
        this.numberOfFloors = numberOfFloors;
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

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getPropertyType() {
        return propertyType;
    }

    public void setPropertyType(String propertyType) {
        this.propertyType = propertyType;
    }

    public int getNumberOfFloors() {
        return numberOfFloors;
    }

    public void setNumberOfFloors(int numberOfFloors) {
        this.numberOfFloors = numberOfFloors;
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
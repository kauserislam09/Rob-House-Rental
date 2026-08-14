package com.rob.houserental.model;

import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Ignore;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(
        tableName = "tenancies",
        foreignKeys = {
                @ForeignKey(
                        entity = Unit.class,
                        parentColumns = "id",
                        childColumns = "unitId",
                        onDelete = ForeignKey.RESTRICT
                ),
                @ForeignKey(
                        entity = Tenant.class,
                        parentColumns = "id",
                        childColumns = "tenantId",
                        onDelete = ForeignKey.RESTRICT
                )
        },
        indices = {
                @Index("unitId"),
                @Index("tenantId"),
                @Index("status")
        }
)
public class Tenancy {

    @PrimaryKey(autoGenerate = true)
    private long id;

    private long unitId;
    private long tenantId;

    private String startDate;
    private String endDate;

    private double monthlyRent;
    private double serviceCharge;

    private double securityDeposit;
    private double advanceAmount;

    private String agreementNumber;

    private int rentDueDay; // Day of the month rent is due (1 - 31), default 10

    private String status; // ACTIVE, ENDED, CANCELLED

    private String notes;

    private long createdAt;
    private long updatedAt;

    public Tenancy() {
        this.status = "ACTIVE";
        this.rentDueDay = 10;
    }

    @Ignore
    public Tenancy(
            long unitId,
            long tenantId,
            String startDate,
            String endDate,
            double monthlyRent,
            double serviceCharge,
            double securityDeposit,
            double advanceAmount,
            String agreementNumber,
            int rentDueDay,
            String status,
            String notes,
            long createdAt,
            long updatedAt
    ) {
        this.unitId = unitId;
        this.tenantId = tenantId;
        this.startDate = startDate;
        this.endDate = endDate;
        this.monthlyRent = monthlyRent;
        this.serviceCharge = serviceCharge;
        this.securityDeposit = securityDeposit;
        this.advanceAmount = advanceAmount;
        this.agreementNumber = agreementNumber;
        this.rentDueDay = rentDueDay > 0 ? rentDueDay : 10;
        this.status = status != null ? status : "ACTIVE";
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

    public long getUnitId() {
        return unitId;
    }

    public void setUnitId(long unitId) {
        this.unitId = unitId;
    }

    public long getTenantId() {
        return tenantId;
    }

    public void setTenantId(long tenantId) {
        this.tenantId = tenantId;
    }

    public String getStartDate() {
        return startDate;
    }

    public void setStartDate(String startDate) {
        this.startDate = startDate;
    }

    public String getEndDate() {
        return endDate;
    }

    public void setEndDate(String endDate) {
        this.endDate = endDate;
    }

    public double getMonthlyRent() {
        return monthlyRent;
    }

    public void setMonthlyRent(double monthlyRent) {
        this.monthlyRent = monthlyRent;
    }

    public double getServiceCharge() {
        return serviceCharge;
    }

    public void setServiceCharge(double serviceCharge) {
        this.serviceCharge = serviceCharge;
    }

    public double getSecurityDeposit() {
        return securityDeposit;
    }

    public void setSecurityDeposit(double securityDeposit) {
        this.securityDeposit = securityDeposit;
    }

    public double getAdvanceAmount() {
        return advanceAmount;
    }

    public void setAdvanceAmount(double advanceAmount) {
        this.advanceAmount = advanceAmount;
    }

    public String getAgreementNumber() {
        return agreementNumber;
    }

    public void setAgreementNumber(String agreementNumber) {
        this.agreementNumber = agreementNumber;
    }

    public int getRentDueDay() {
        return rentDueDay > 0 ? rentDueDay : 10;
    }

    public void setRentDueDay(int rentDueDay) {
        this.rentDueDay = rentDueDay;
    }

    public String getStatus() {
        return status != null ? status : "ACTIVE";
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
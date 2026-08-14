package com.rob.houserental.model;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "subscription_entitlements")
public class SubscriptionEntitlement {

    @PrimaryKey
    @NonNull
    private String entitlementId = "";
    private String userId;
    private String entitlementType; // "PREMIUM"
    private String status; // "FREE", "PENDING", "ACTIVE", "GRACE_PERIOD", "EXPIRED", "CANCELLED", "SUSPENDED"
    private String planCode; // "FREE", "MONTHLY", "SIX_MONTHS", "YEARLY"
    private long startedAt;
    private long expiresAt;
    private long graceUntil;
    private String source; // "MANUAL_BKASH", "MANUAL_NAGAD", "MANUAL_ROCKET", "FREE"
    private String orderId;
    private long createdAt;
    private long updatedAt;

    public SubscriptionEntitlement() {
    }

    @NonNull
    public String getEntitlementId() {
        return entitlementId;
    }

    public void setEntitlementId(@NonNull String entitlementId) {
        this.entitlementId = entitlementId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getEntitlementType() {
        return entitlementType;
    }

    public void setEntitlementType(String entitlementType) {
        this.entitlementType = entitlementType;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getPlanCode() {
        return planCode;
    }

    public void setPlanCode(String planCode) {
        this.planCode = planCode;
    }

    public long getStartedAt() {
        return startedAt;
    }

    public void setStartedAt(long startedAt) {
        this.startedAt = startedAt;
    }

    public long getExpiresAt() {
        return expiresAt;
    }

    public void setExpiresAt(long expiresAt) {
        this.expiresAt = expiresAt;
    }

    public long getGraceUntil() {
        return graceUntil;
    }

    public void setGraceUntil(long graceUntil) {
        this.graceUntil = graceUntil;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
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

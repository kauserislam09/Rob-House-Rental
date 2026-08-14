package com.rob.houserental.model;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import androidx.room.Ignore;

@Entity(tableName = "user_accounts")
public class UserAccount {

    @PrimaryKey
    @NonNull
    private String userId = "";
    private String emailOrPhone;
    private String fullName;
    private String status; // "ACTIVE", "SUSPENDED"
    private long createdAt;
    private long updatedAt;

    public UserAccount() {
    }

    @Ignore
    public UserAccount(@NonNull String userId, String emailOrPhone, String fullName, String status, long createdAt, long updatedAt) {
        this.userId = userId;
        this.emailOrPhone = emailOrPhone;
        this.fullName = fullName;
        this.status = status;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    @NonNull
    public String getUserId() {
        return userId;
    }

    public void setUserId(@NonNull String userId) {
        this.userId = userId;
    }

    public String getEmailOrPhone() {
        return emailOrPhone;
    }

    public void setEmailOrPhone(String emailOrPhone) {
        this.emailOrPhone = emailOrPhone;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
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

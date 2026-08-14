package com.rob.houserental.model;

import androidx.room.Entity;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(
        tableName = "reminders",
        indices = {
                @Index("reminderType"),
                @Index("relatedEntityType"),
                @Index("relatedEntityId"),
                @Index("reminderDate"),
                @Index("isEnabled"),
                @Index("isCompleted")
        }
)
public class Reminder {

    @PrimaryKey(autoGenerate = true)
    private long id;

    private String title;
    private String description;

    private String reminderType; // MANUAL, RENT_DUE, TENANCY_EXPIRY, BILL_DUE, MAINTENANCE, DOCUMENT_EXPIRY
    private String relatedEntityType; // NONE, PROPERTY, UNIT, TENANT, TENANCY, RENT, BILL, MAINTENANCE, DOCUMENT
    private long relatedEntityId; // 0 if NONE

    private String reminderDate; // yyyy-MM-dd
    private String reminderTime; // HH:mm (24hr format)

    private String repeatType; // ONCE, DAILY, WEEKLY, MONTHLY, YEARLY, CUSTOM
    private int repeatInterval; // N days/weeks/months (>= 1)

    private boolean isEnabled;
    private boolean isCompleted;

    private long lastTriggeredAt; // timestamp
    private long createdAt;
    private long updatedAt;

    public Reminder() {
        this.reminderType = "MANUAL";
        this.relatedEntityType = "NONE";
        this.relatedEntityId = 0;
        this.repeatType = "ONCE";
        this.repeatInterval = 1;
        this.isEnabled = true;
        this.isCompleted = false;
        this.lastTriggeredAt = 0;
        this.createdAt = System.currentTimeMillis();
        this.updatedAt = System.currentTimeMillis();
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
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

    public String getReminderType() {
        return reminderType;
    }

    public void setReminderType(String reminderType) {
        this.reminderType = reminderType;
    }

    public String getRelatedEntityType() {
        return relatedEntityType;
    }

    public void setRelatedEntityType(String relatedEntityType) {
        this.relatedEntityType = relatedEntityType;
    }

    public long getRelatedEntityId() {
        return relatedEntityId;
    }

    public void setRelatedEntityId(long relatedEntityId) {
        this.relatedEntityId = relatedEntityId;
    }

    public String getReminderDate() {
        return reminderDate;
    }

    public void setReminderDate(String reminderDate) {
        this.reminderDate = reminderDate;
    }

    public String getReminderTime() {
        return reminderTime;
    }

    public void setReminderTime(String reminderTime) {
        this.reminderTime = reminderTime;
    }

    public String getRepeatType() {
        return repeatType;
    }

    public void setRepeatType(String repeatType) {
        this.repeatType = repeatType;
    }

    public int getRepeatInterval() {
        return repeatInterval;
    }

    public void setRepeatInterval(int repeatInterval) {
        this.repeatInterval = Math.max(1, repeatInterval);
    }

    public boolean isEnabled() {
        return isEnabled;
    }

    public void setEnabled(boolean enabled) {
        isEnabled = enabled;
    }

    public boolean isCompleted() {
        return isCompleted;
    }

    public void setCompleted(boolean completed) {
        isCompleted = completed;
    }

    public long getLastTriggeredAt() {
        return lastTriggeredAt;
    }

    public void setLastTriggeredAt(long lastTriggeredAt) {
        this.lastTriggeredAt = lastTriggeredAt;
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

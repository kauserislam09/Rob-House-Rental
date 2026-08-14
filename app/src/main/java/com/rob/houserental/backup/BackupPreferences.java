package com.rob.houserental.backup;

import android.content.Context;
import android.content.SharedPreferences;

public class BackupPreferences {

    private static final String PREF_NAME = "rob_house_rental_backup_prefs";

    public static final String SCHEDULE_OFF = "OFF";
    public static final String SCHEDULE_DAILY = "DAILY";
    public static final String SCHEDULE_7_DAYS = "EVERY_7_DAYS";
    public static final String SCHEDULE_15_DAYS = "EVERY_15_DAYS";
    public static final String SCHEDULE_30_DAYS = "EVERY_30_DAYS";

    private static final String KEY_SCHEDULE = "backup_schedule";
    private static final String KEY_DRIVE_CONNECTED = "drive_connected";
    private static final String KEY_ACCOUNT_EMAIL = "account_email";
    private static final String KEY_LAST_BACKUP_TIME = "last_backup_time";
    private static final String KEY_LAST_BACKUP_STATUS = "last_backup_status";
    private static final String KEY_LAST_BACKUP_SIZE = "last_backup_size";
    private static final String KEY_LAST_BACKUP_TYPE = "last_backup_type";
    private static final String KEY_LAST_DRIVE_FILE_ID = "last_drive_file_id";

    private final SharedPreferences prefs;

    public BackupPreferences(Context context) {
        prefs = context.getApplicationContext().getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    public String getBackupSchedule() {
        return prefs.getString(KEY_SCHEDULE, SCHEDULE_DAILY);
    }

    public void setBackupSchedule(String schedule) {
        prefs.edit().putString(KEY_SCHEDULE, schedule).apply();
    }

    public boolean isGoogleDriveConnected() {
        return prefs.getBoolean(KEY_DRIVE_CONNECTED, false);
    }

    public void setGoogleDriveConnected(boolean connected) {
        prefs.edit().putBoolean(KEY_DRIVE_CONNECTED, connected).apply();
    }

    public String getGoogleAccountEmail() {
        return prefs.getString(KEY_ACCOUNT_EMAIL, "");
    }

    public void setGoogleAccountEmail(String email) {
        prefs.edit().putString(KEY_ACCOUNT_EMAIL, email != null ? email : "").apply();
    }

    public long getLastBackupTime() {
        return prefs.getLong(KEY_LAST_BACKUP_TIME, 0);
    }

    public void setLastBackupTime(long timestamp) {
        prefs.edit().putLong(KEY_LAST_BACKUP_TIME, timestamp).apply();
    }

    public String getLastBackupStatus() {
        return prefs.getString(KEY_LAST_BACKUP_STATUS, "NONE");
    }

    public void setLastBackupStatus(String status) {
        prefs.edit().putString(KEY_LAST_BACKUP_STATUS, status).apply();
    }

    public long getLastBackupSizeBytes() {
        return prefs.getLong(KEY_LAST_BACKUP_SIZE, 0);
    }

    public void setLastBackupSizeBytes(long bytes) {
        prefs.edit().putLong(KEY_LAST_BACKUP_SIZE, bytes).apply();
    }

    public String getLastBackupType() {
        return prefs.getString(KEY_LAST_BACKUP_TYPE, "");
    }

    public void setLastBackupType(String type) {
        prefs.edit().putString(KEY_LAST_BACKUP_TYPE, type).apply();
    }

    public String getLastDriveFileId() {
        return prefs.getString(KEY_LAST_DRIVE_FILE_ID, "");
    }

    public void setLastDriveFileId(String fileId) {
        prefs.edit().putString(KEY_LAST_DRIVE_FILE_ID, fileId != null ? fileId : "").apply();
    }

    public void clearAll() {
        prefs.edit().clear().apply();
    }
}

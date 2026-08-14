package com.rob.houserental.model;

import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(
        tableName = "backup_history",
        indices = {
                @Index("createdAt"),
                @Index("status"),
                @Index("backupType")
        }
)
public class BackupHistory {

    @PrimaryKey(autoGenerate = true)
    private long id;

    private String backupId;
    private String fileName;
    private String driveFileId;
    private long createdAt;
    private long completedAt;
    private long sizeBytes;
    private String status; // IN_PROGRESS, SUCCESS, FAILED, RESTORED
    private String backupType; // MANUAL, AUTOMATIC, RESTORE, EXPORT, IMPORT
    private String errorMessage;
    private String checksum;
    private String appVersion;
    private int documentCount;

    public BackupHistory() {
    }

    @Ignore
    public BackupHistory(String backupId, String fileName, String driveFileId, long createdAt,
                         long completedAt, long sizeBytes, String status, String backupType,
                         String errorMessage, String checksum, String appVersion, int documentCount) {
        this.backupId = backupId;
        this.fileName = fileName;
        this.driveFileId = driveFileId;
        this.createdAt = createdAt;
        this.completedAt = completedAt;
        this.sizeBytes = sizeBytes;
        this.status = status;
        this.backupType = backupType;
        this.errorMessage = errorMessage;
        this.checksum = checksum;
        this.appVersion = appVersion;
        this.documentCount = documentCount;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getBackupId() {
        return backupId;
    }

    public void setBackupId(String backupId) {
        this.backupId = backupId;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getDriveFileId() {
        return driveFileId;
    }

    public void setDriveFileId(String driveFileId) {
        this.driveFileId = driveFileId;
    }

    public long getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(long createdAt) {
        this.createdAt = createdAt;
    }

    public long getCompletedAt() {
        return completedAt;
    }

    public void setCompletedAt(long completedAt) {
        this.completedAt = completedAt;
    }

    public long getSizeBytes() {
        return sizeBytes;
    }

    public void setSizeBytes(long sizeBytes) {
        this.sizeBytes = sizeBytes;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getBackupType() {
        return backupType;
    }

    public void setBackupType(String backupType) {
        this.backupType = backupType;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public String getChecksum() {
        return checksum;
    }

    public void setChecksum(String checksum) {
        this.checksum = checksum;
    }

    public String getAppVersion() {
        return appVersion;
    }

    public void setAppVersion(String appVersion) {
        this.appVersion = appVersion;
    }

    public int getDocumentCount() {
        return documentCount;
    }

    public void setDocumentCount(int documentCount) {
        this.documentCount = documentCount;
    }
}

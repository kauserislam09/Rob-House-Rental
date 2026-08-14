package com.rob.houserental.model;

import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(
        tableName = "tenant_documents",
        foreignKeys = @ForeignKey(
                entity = Tenant.class,
                parentColumns = "id",
                childColumns = "tenantId",
                onDelete = ForeignKey.CASCADE
        ),
        indices = {
                @Index(value = "tenantId")
        }
)
public class TenantDocument {

    @PrimaryKey(autoGenerate = true)
    private long id;

    private long tenantId;

    private String documentType;

    private String displayName;

    private String filePath;

    private String mimeType;

    private long createdAt;

    private long updatedAt;

    public TenantDocument() {
    }

    @androidx.room.Ignore
    public TenantDocument(
            long tenantId,
            String documentType,
            String displayName,
            String filePath,
            String mimeType,
            long createdAt,
            long updatedAt
    ) {
        this.tenantId = tenantId;
        this.documentType = documentType;
        this.displayName = displayName;
        this.filePath = filePath;
        this.mimeType = mimeType;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getTenantId() {
        return tenantId;
    }

    public void setTenantId(long tenantId) {
        this.tenantId = tenantId;
    }

    public String getDocumentType() {
        return documentType;
    }

    public void setDocumentType(String documentType) {
        this.documentType = documentType;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public String getMimeType() {
        return mimeType;
    }

    public void setMimeType(String mimeType) {
        this.mimeType = mimeType;
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

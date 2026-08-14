package com.rob.houserental.model;

import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(
        tableName = "app_documents",
        indices = {
                @Index("documentType"),
                @Index("category"),
                @Index("propertyId"),
                @Index("tenantId"),
                @Index("isArchived")
        }
)
public class AppDocument {

    @PrimaryKey(autoGenerate = true)
    private long id;

    private String documentType; // PROPERTY, TENANT, EXPENSE, RENT_PAYMENT, UTILITY_BILL, GENERAL
    private String category;     // DEED, NID, PASSPORT, AGREEMENT, VOUCHER, BILL_RECEIPT, PAYMENT_SLIP, TAX_RECORD, POLICE_FORM, OTHER

    private String displayName;
    private String fileName;
    private String filePath;
    private String mimeType;
    private long fileSize; // bytes

    private long propertyId; // 0 if not linked
    private long unitId;     // 0 if whole property / not linked
    private long tenantId;   // 0 if not linked
    private long relatedRecordId; // optional link to expenseId, rentRecordId, billId, paymentId

    private String notes;
    private boolean isArchived;

    private long createdAt;
    private long updatedAt;

    public AppDocument() {
        this.documentType = "GENERAL";
        this.category = "OTHER";
        this.isArchived = false;
    }

    @Ignore
    public AppDocument(
            String documentType,
            String category,
            String displayName,
            String fileName,
            String filePath,
            String mimeType,
            long fileSize,
            long propertyId,
            long unitId,
            long tenantId,
            long relatedRecordId,
            String notes,
            boolean isArchived,
            long createdAt,
            long updatedAt
    ) {
        this.documentType = documentType != null ? documentType : "GENERAL";
        this.category = category != null ? category : "OTHER";
        this.displayName = displayName;
        this.fileName = fileName;
        this.filePath = filePath;
        this.mimeType = mimeType;
        this.fileSize = fileSize;
        this.propertyId = propertyId;
        this.unitId = unitId;
        this.tenantId = tenantId;
        this.relatedRecordId = relatedRecordId;
        this.notes = notes;
        this.isArchived = isArchived;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getDocumentType() {
        return documentType != null ? documentType : "GENERAL";
    }

    public void setDocumentType(String documentType) {
        this.documentType = documentType;
    }

    public String getCategory() {
        return category != null ? category : "OTHER";
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
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

    public long getFileSize() {
        return fileSize;
    }

    public void setFileSize(long fileSize) {
        this.fileSize = fileSize;
    }

    public long getPropertyId() {
        return propertyId;
    }

    public void setPropertyId(long propertyId) {
        this.propertyId = propertyId;
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

    public long getRelatedRecordId() {
        return relatedRecordId;
    }

    public void setRelatedRecordId(long relatedRecordId) {
        this.relatedRecordId = relatedRecordId;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public boolean isArchived() {
        return isArchived;
    }

    public void setArchived(boolean archived) {
        isArchived = archived;
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

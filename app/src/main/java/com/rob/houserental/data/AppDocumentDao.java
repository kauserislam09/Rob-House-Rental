package com.rob.houserental.data;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.rob.houserental.model.AppDocument;
import com.rob.houserental.model.AppDocumentDisplayItem;

import java.util.List;

@Dao
public interface AppDocumentDao {

    @Insert
    long insert(AppDocument document);

    @Update
    void update(AppDocument document);

    @Delete
    void delete(AppDocument document);

    @Query("SELECT * FROM app_documents WHERE id = :id LIMIT 1")
    AppDocument getDocumentById(long id);

    @Query("SELECT * FROM app_documents WHERE isArchived = 0 ORDER BY id DESC")
    List<AppDocument> getAllDocuments();

    @Query("SELECT * FROM app_documents WHERE documentType = :documentType AND isArchived = 0 ORDER BY id DESC")
    List<AppDocument> getDocumentsByType(String documentType);

    @Query("SELECT * FROM app_documents WHERE category = :category AND isArchived = 0 ORDER BY id DESC")
    List<AppDocument> getDocumentsByCategory(String category);

    @Query("SELECT * FROM app_documents WHERE propertyId = :propertyId AND isArchived = 0 ORDER BY id DESC")
    List<AppDocument> getDocumentsByProperty(long propertyId);

    @Query("SELECT * FROM app_documents WHERE tenantId = :tenantId AND isArchived = 0 ORDER BY id DESC")
    List<AppDocument> getDocumentsByTenant(long tenantId);

    @Query("SELECT * FROM app_documents WHERE documentType = 'MAINTENANCE' AND relatedRecordId = :maintenanceId AND isArchived = 0 ORDER BY id DESC")
    List<AppDocument> getDocumentsByMaintenance(long maintenanceId);

    @Query("SELECT COUNT(*) FROM app_documents WHERE isArchived = 0")
    int getTotalDocumentCount();

    @Query("SELECT COALESCE(SUM(fileSize), 0) FROM app_documents WHERE isArchived = 0")
    long getTotalDocumentStorageBytes();

    @Query("SELECT " +
            "d.id AS id, " +
            "d.documentType AS documentType, " +
            "d.category AS category, " +
            "d.displayName AS displayName, " +
            "d.fileName AS fileName, " +
            "d.filePath AS filePath, " +
            "d.mimeType AS mimeType, " +
            "d.fileSize AS fileSize, " +
            "d.propertyId AS propertyId, " +
            "d.unitId AS unitId, " +
            "d.tenantId AS tenantId, " +
            "d.relatedRecordId AS relatedRecordId, " +
            "d.notes AS notes, " +
            "d.isArchived AS isArchived, " +
            "d.createdAt AS createdAt, " +
            "d.updatedAt AS updatedAt, " +
            "p.name AS propertyName, " +
            "u.unitNumber AS unitNumber, " +
            "t.fullName AS tenantFullName, " +
            "t.phoneNumber AS tenantPhone " +
            "FROM app_documents d " +
            "LEFT JOIN properties p ON d.propertyId = p.id " +
            "LEFT JOIN units u ON d.unitId = u.id " +
            "LEFT JOIN tenants t ON d.tenantId = t.id " +
            "WHERE d.id = :id LIMIT 1")
    AppDocumentDisplayItem getDisplayItemById(long id);

    @Query("SELECT " +
            "d.id AS id, " +
            "d.documentType AS documentType, " +
            "d.category AS category, " +
            "d.displayName AS displayName, " +
            "d.fileName AS fileName, " +
            "d.filePath AS filePath, " +
            "d.mimeType AS mimeType, " +
            "d.fileSize AS fileSize, " +
            "d.propertyId AS propertyId, " +
            "d.unitId AS unitId, " +
            "d.tenantId AS tenantId, " +
            "d.relatedRecordId AS relatedRecordId, " +
            "d.notes AS notes, " +
            "d.isArchived AS isArchived, " +
            "d.createdAt AS createdAt, " +
            "d.updatedAt AS updatedAt, " +
            "p.name AS propertyName, " +
            "u.unitNumber AS unitNumber, " +
            "t.fullName AS tenantFullName, " +
            "t.phoneNumber AS tenantPhone " +
            "FROM app_documents d " +
            "LEFT JOIN properties p ON d.propertyId = p.id " +
            "LEFT JOIN units u ON d.unitId = u.id " +
            "LEFT JOIN tenants t ON d.tenantId = t.id " +
            "WHERE d.isArchived = 0 " +
            "ORDER BY d.id DESC")
    List<AppDocumentDisplayItem> getAllDisplayItems();

    @Query("SELECT " +
            "d.id AS id, " +
            "d.documentType AS documentType, " +
            "d.category AS category, " +
            "d.displayName AS displayName, " +
            "d.fileName AS fileName, " +
            "d.filePath AS filePath, " +
            "d.mimeType AS mimeType, " +
            "d.fileSize AS fileSize, " +
            "d.propertyId AS propertyId, " +
            "d.unitId AS unitId, " +
            "d.tenantId AS tenantId, " +
            "d.relatedRecordId AS relatedRecordId, " +
            "d.notes AS notes, " +
            "d.isArchived AS isArchived, " +
            "d.createdAt AS createdAt, " +
            "d.updatedAt AS updatedAt, " +
            "p.name AS propertyName, " +
            "u.unitNumber AS unitNumber, " +
            "t.fullName AS tenantFullName, " +
            "t.phoneNumber AS tenantPhone " +
            "FROM app_documents d " +
            "LEFT JOIN properties p ON d.propertyId = p.id " +
            "LEFT JOIN units u ON d.unitId = u.id " +
            "LEFT JOIN tenants t ON d.tenantId = t.id " +
            "WHERE d.documentType = :documentType AND d.isArchived = 0 " +
            "ORDER BY d.id DESC")
    List<AppDocumentDisplayItem> getDisplayItemsByType(String documentType);

    @Query("SELECT " +
            "d.id AS id, " +
            "d.documentType AS documentType, " +
            "d.category AS category, " +
            "d.displayName AS displayName, " +
            "d.fileName AS fileName, " +
            "d.filePath AS filePath, " +
            "d.mimeType AS mimeType, " +
            "d.fileSize AS fileSize, " +
            "d.propertyId AS propertyId, " +
            "d.unitId AS unitId, " +
            "d.tenantId AS tenantId, " +
            "d.relatedRecordId AS relatedRecordId, " +
            "d.notes AS notes, " +
            "d.isArchived AS isArchived, " +
            "d.createdAt AS createdAt, " +
            "d.updatedAt AS updatedAt, " +
            "p.name AS propertyName, " +
            "u.unitNumber AS unitNumber, " +
            "t.fullName AS tenantFullName, " +
            "t.phoneNumber AS tenantPhone " +
            "FROM app_documents d " +
            "LEFT JOIN properties p ON d.propertyId = p.id " +
            "LEFT JOIN units u ON d.unitId = u.id " +
            "LEFT JOIN tenants t ON d.tenantId = t.id " +
            "WHERE d.propertyId = :propertyId AND d.isArchived = 0 " +
            "ORDER BY d.id DESC")
    List<AppDocumentDisplayItem> getDisplayItemsByProperty(long propertyId);

    @Query("SELECT " +
            "d.id AS id, " +
            "d.documentType AS documentType, " +
            "d.category AS category, " +
            "d.displayName AS displayName, " +
            "d.fileName AS fileName, " +
            "d.filePath AS filePath, " +
            "d.mimeType AS mimeType, " +
            "d.fileSize AS fileSize, " +
            "d.propertyId AS propertyId, " +
            "d.unitId AS unitId, " +
            "d.tenantId AS tenantId, " +
            "d.relatedRecordId AS relatedRecordId, " +
            "d.notes AS notes, " +
            "d.isArchived AS isArchived, " +
            "d.createdAt AS createdAt, " +
            "d.updatedAt AS updatedAt, " +
            "p.name AS propertyName, " +
            "u.unitNumber AS unitNumber, " +
            "t.fullName AS tenantFullName, " +
            "t.phoneNumber AS tenantPhone " +
            "FROM app_documents d " +
            "LEFT JOIN properties p ON d.propertyId = p.id " +
            "LEFT JOIN units u ON d.unitId = u.id " +
            "LEFT JOIN tenants t ON d.tenantId = t.id " +
            "WHERE d.tenantId = :tenantId AND d.isArchived = 0 " +
            "ORDER BY d.id DESC")
    List<AppDocumentDisplayItem> getDisplayItemsByTenant(long tenantId);

    @Query("SELECT " +
            "d.id AS id, " +
            "d.documentType AS documentType, " +
            "d.category AS category, " +
            "d.displayName AS displayName, " +
            "d.fileName AS fileName, " +
            "d.filePath AS filePath, " +
            "d.mimeType AS mimeType, " +
            "d.fileSize AS fileSize, " +
            "d.propertyId AS propertyId, " +
            "d.unitId AS unitId, " +
            "d.tenantId AS tenantId, " +
            "d.relatedRecordId AS relatedRecordId, " +
            "d.notes AS notes, " +
            "d.isArchived AS isArchived, " +
            "d.createdAt AS createdAt, " +
            "d.updatedAt AS updatedAt, " +
            "p.name AS propertyName, " +
            "u.unitNumber AS unitNumber, " +
            "t.fullName AS tenantFullName, " +
            "t.phoneNumber AS tenantPhone " +
            "FROM app_documents d " +
            "LEFT JOIN properties p ON d.propertyId = p.id " +
            "LEFT JOIN units u ON d.unitId = u.id " +
            "LEFT JOIN tenants t ON d.tenantId = t.id " +
            "WHERE (d.displayName LIKE '%' || :query || '%' OR d.category LIKE '%' || :query || '%' OR d.documentType LIKE '%' || :query || '%' OR p.name LIKE '%' || :query || '%' OR t.fullName LIKE '%' || :query || '%' OR d.notes LIKE '%' || :query || '%') AND d.isArchived = 0 " +
            "ORDER BY d.id DESC")
    List<AppDocumentDisplayItem> searchDisplayItems(String query);
}

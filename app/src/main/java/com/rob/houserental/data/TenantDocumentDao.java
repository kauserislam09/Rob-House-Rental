package com.rob.houserental.data;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.rob.houserental.model.TenantDocument;

import java.util.List;

@Dao
public interface TenantDocumentDao {

    @Insert
    long insert(TenantDocument document);

    @Update
    void update(TenantDocument document);

    @Delete
    void delete(TenantDocument document);

    @Query("SELECT * FROM tenant_documents ORDER BY createdAt DESC")
    List<TenantDocument> getAllDocuments();

    @Query("SELECT * FROM tenant_documents WHERE tenantId = :tenantId ORDER BY createdAt DESC")
    List<TenantDocument> getDocumentsByTenant(long tenantId);

    @Query("SELECT * FROM tenant_documents WHERE id = :documentId LIMIT 1")
    TenantDocument getDocumentById(long documentId);

    @Query("DELETE FROM tenant_documents WHERE id = :documentId")
    void deleteDocumentById(long documentId);
}

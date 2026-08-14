package com.rob.houserental.data;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.rob.houserental.model.Tenant;

import java.util.List;

@Dao
public interface TenantDao {

    @Insert
    long insert(Tenant tenant);

    @Update
    void update(Tenant tenant);

    @Delete
    void delete(Tenant tenant);

    @Query("SELECT * FROM tenants WHERE id = :tenantId LIMIT 1")
    Tenant getTenantById(long tenantId);

    @Query("SELECT * FROM tenants ORDER BY fullName ASC")
    List<Tenant> getAllTenants();

    @Query("SELECT * FROM tenants WHERE fullName LIKE '%' || :query || '%' OR phoneNumber LIKE '%' || :query || '%' OR alternativePhone LIKE '%' || :query || '%' OR nidNumber LIKE '%' || :query || '%' ORDER BY fullName ASC")
    List<Tenant> searchTenants(String query);

    @Query("SELECT * FROM tenants WHERE status = 'ACTIVE' ORDER BY fullName ASC")
    List<Tenant> getActiveTenants();

    @Query("SELECT * FROM tenants WHERE status = :status ORDER BY fullName ASC")
    List<Tenant> getTenantsByStatus(String status);

    @Query("SELECT COUNT(*) FROM tenants")
    int getTenantCount();

    @Query("SELECT COUNT(*) FROM tenants WHERE status = :status")
    int getTenantCountByStatus(String status);
}

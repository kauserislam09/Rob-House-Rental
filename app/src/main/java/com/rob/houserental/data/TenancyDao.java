package com.rob.houserental.data;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Transaction;
import androidx.room.Update;

import com.rob.houserental.model.Tenancy;
import com.rob.houserental.model.TenancyWithDetails;

import java.util.List;

@Dao
public interface TenancyDao {

    @Insert
    long insert(Tenancy tenancy);

    @Update
    void update(Tenancy tenancy);

    @Delete
    void delete(Tenancy tenancy);

    @Query("SELECT * FROM tenancies WHERE id = :tenancyId LIMIT 1")
    Tenancy getTenancyById(long tenancyId);

    @Query("SELECT * FROM tenancies WHERE unitId = :unitId AND status = 'ACTIVE' LIMIT 1")
    Tenancy getActiveTenancyByUnit(long unitId);

    @Query("SELECT * FROM tenancies WHERE tenantId = :tenantId AND status = 'ACTIVE' LIMIT 1")
    Tenancy getActiveTenancyByTenant(long tenantId);

    @Query("SELECT * FROM tenancies WHERE unitId = :unitId ORDER BY id DESC")
    List<Tenancy> getTenanciesByUnit(long unitId);

    @Query("SELECT * FROM tenancies WHERE tenantId = :tenantId ORDER BY id DESC")
    List<Tenancy> getTenanciesByTenant(long tenantId);

    @Query("SELECT * FROM tenancies ORDER BY id DESC")
    List<Tenancy> getAllTenancies();

    @Query("SELECT * FROM tenancies WHERE status = :status ORDER BY id DESC")
    List<Tenancy> getTenanciesByStatus(String status);

    @Query("SELECT COUNT(*) FROM tenancies WHERE status = :status")
    int getTenancyCountByStatus(String status);

    @Transaction
    @Query("SELECT * FROM tenancies WHERE id = :tenancyId LIMIT 1")
    TenancyWithDetails getTenancyWithDetailsById(long tenancyId);

    @Transaction
    @Query("SELECT * FROM tenancies ORDER BY id DESC")
    List<TenancyWithDetails> getAllTenanciesWithDetails();

    @Transaction
    @Query("SELECT * FROM tenancies WHERE status = :status ORDER BY id DESC")
    List<TenancyWithDetails> getTenanciesWithDetailsByStatus(String status);

    @Transaction
    @Query("SELECT * FROM tenancies WHERE unitId = :unitId AND status = 'ACTIVE' LIMIT 1")
    TenancyWithDetails getActiveTenancyWithDetailsByUnit(long unitId);

    @Transaction
    @Query("SELECT * FROM tenancies WHERE tenantId = :tenantId AND status = 'ACTIVE' LIMIT 1")
    TenancyWithDetails getActiveTenancyWithDetailsByTenant(long tenantId);

    @Transaction
    @Query("SELECT * FROM tenancies WHERE unitId = :unitId ORDER BY id DESC")
    List<TenancyWithDetails> getTenanciesWithDetailsByUnit(long unitId);

    @Transaction
    @Query("SELECT * FROM tenancies WHERE tenantId = :tenantId ORDER BY id DESC")
    List<TenancyWithDetails> getTenanciesWithDetailsByTenant(long tenantId);
}

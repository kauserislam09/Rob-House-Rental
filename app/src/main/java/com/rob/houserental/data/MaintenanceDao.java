package com.rob.houserental.data;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.rob.houserental.model.MaintenanceRecord;

import java.util.List;

@Dao
public interface MaintenanceDao {

    @Insert
    long insert(MaintenanceRecord record);

    @Update
    void update(MaintenanceRecord record);

    @Delete
    void delete(MaintenanceRecord record);

    @Query("SELECT * FROM maintenance_records WHERE id = :id LIMIT 1")
    MaintenanceRecord getById(long id);

    @Query("SELECT * FROM maintenance_records ORDER BY id DESC")
    List<MaintenanceRecord> getAll();

    @Query("SELECT * FROM maintenance_records WHERE propertyId = :propertyId ORDER BY id DESC")
    List<MaintenanceRecord> getByProperty(long propertyId);

    @Query("SELECT * FROM maintenance_records WHERE unitId = :unitId ORDER BY id DESC")
    List<MaintenanceRecord> getByUnit(long unitId);

    @Query("SELECT * FROM maintenance_records WHERE status = :status ORDER BY id DESC")
    List<MaintenanceRecord> getByStatus(String status);

    @Query("SELECT * FROM maintenance_records WHERE priority = :priority ORDER BY id DESC")
    List<MaintenanceRecord> getByPriority(String priority);

    @Query("SELECT * FROM maintenance_records WHERE category = :category ORDER BY id DESC")
    List<MaintenanceRecord> getByCategory(String category);

    @Query("SELECT * FROM maintenance_records WHERE status = 'OPEN' ORDER BY id DESC")
    List<MaintenanceRecord> getOpen();

    @Query("SELECT * FROM maintenance_records WHERE status = 'SCHEDULED' ORDER BY id DESC")
    List<MaintenanceRecord> getScheduled();

    @Query("SELECT * FROM maintenance_records WHERE status = 'IN_PROGRESS' ORDER BY id DESC")
    List<MaintenanceRecord> getInProgress();

    @Query("SELECT * FROM maintenance_records WHERE status = 'COMPLETED' ORDER BY id DESC")
    List<MaintenanceRecord> getCompleted();

    @Query("SELECT COUNT(*) FROM maintenance_records")
    int getMaintenanceCount();

    @Query("SELECT COALESCE(SUM(actualCost), 0) FROM maintenance_records")
    double getMaintenanceCostTotal();

    @Query("SELECT COUNT(*) FROM maintenance_records WHERE propertyId = :propertyId")
    int getPropertyMaintenanceCount(long propertyId);

    @Query("SELECT COALESCE(SUM(actualCost), 0) FROM maintenance_records WHERE propertyId = :propertyId")
    double getPropertyMaintenanceCostTotal(long propertyId);

    @Query("SELECT COUNT(*) FROM maintenance_records WHERE unitId = :unitId")
    int getUnitMaintenanceCount(long unitId);

    @Query("SELECT COALESCE(SUM(actualCost), 0) FROM maintenance_records WHERE unitId = :unitId")
    double getUnitMaintenanceCostTotal(long unitId);
}

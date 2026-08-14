package com.rob.houserental.data;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.rob.houserental.model.Unit;

import java.util.List;

@Dao
public interface UnitDao {

    @Insert
    long insert(Unit unit);

    @Update
    void update(Unit unit);

    @Delete
    void delete(Unit unit);

    @Query("SELECT * FROM units ORDER BY floor ASC, unitNumber ASC")
    List<Unit> getAllUnits();

    @Query(
            "SELECT * FROM units " +
                    "WHERE propertyId = :propertyId " +
                    "ORDER BY floor ASC, unitNumber ASC"
    )
    List<Unit> getUnitsByProperty(
            long propertyId
    );

    @Query(
            "SELECT * FROM units " +
                    "WHERE id = :unitId " +
                    "LIMIT 1"
    )
    Unit getUnitById(
            long unitId
    );

    @Query(
            "SELECT COUNT(*) FROM units " +
                    "WHERE propertyId = :propertyId"
    )
    int getUnitCount(
            long propertyId
    );

    @Query(
            "SELECT COUNT(*) FROM units " +
                    "WHERE propertyId = :propertyId " +
                    "AND status = :status"
    )
    int getUnitCountByStatus(
            long propertyId,
            String status
    );

    @Query(
            "SELECT COUNT(*) FROM units " +
                    "WHERE propertyId = :propertyId " +
                    "AND LOWER(TRIM(unitNumber)) = LOWER(TRIM(:unitNumber)) " +
                    "AND id != :excludeUnitId"
    )
    int countDuplicateUnitNumber(
            long propertyId,
            String unitNumber,
            long excludeUnitId
    );
}
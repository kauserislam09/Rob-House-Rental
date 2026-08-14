package com.rob.houserental.data;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.rob.houserental.model.Property;

import java.util.List;

@Dao
public interface PropertyDao {

    @Insert
    long insert(Property property);

    @Update
    void update(Property property);

    @Delete
    void delete(Property property);

    @Query("SELECT * FROM properties ORDER BY name ASC")
    List<Property> getAllProperties();

    @Query("SELECT * FROM properties WHERE id = :propertyId LIMIT 1")
    Property getPropertyById(long propertyId);
}
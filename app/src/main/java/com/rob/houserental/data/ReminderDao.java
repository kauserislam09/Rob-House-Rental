package com.rob.houserental.data;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.rob.houserental.model.Reminder;

import java.util.List;

@Dao
public interface ReminderDao {

    @Insert
    long insert(Reminder reminder);

    @Update
    void update(Reminder reminder);

    @Delete
    void delete(Reminder reminder);

    @Query("SELECT * FROM reminders WHERE id = :id LIMIT 1")
    Reminder getById(long id);

    @Query("SELECT * FROM reminders ORDER BY reminderDate ASC, reminderTime ASC")
    List<Reminder> getAll();

    @Query("SELECT * FROM reminders WHERE isEnabled = 1 AND isCompleted = 0 ORDER BY reminderDate ASC, reminderTime ASC")
    List<Reminder> getEnabled();

    @Query("SELECT * FROM reminders WHERE isEnabled = 1 AND isCompleted = 0 AND reminderDate >= :currentDate ORDER BY reminderDate ASC, reminderTime ASC")
    List<Reminder> getUpcoming(String currentDate);

    @Query("SELECT * FROM reminders WHERE isEnabled = 1 AND isCompleted = 0 AND reminderDate < :currentDate ORDER BY reminderDate ASC, reminderTime ASC")
    List<Reminder> getOverdue(String currentDate);

    @Query("SELECT * FROM reminders WHERE reminderDate = :date ORDER BY reminderTime ASC")
    List<Reminder> getToday(String date);

    @Query("SELECT * FROM reminders WHERE isCompleted = 1 ORDER BY updatedAt DESC")
    List<Reminder> getCompleted();

    @Query("SELECT * FROM reminders WHERE reminderType = :type ORDER BY reminderDate ASC, reminderTime ASC")
    List<Reminder> getByType(String type);

    @Query("SELECT * FROM reminders WHERE relatedEntityType = :entityType AND relatedEntityId = :entityId AND reminderType = :reminderType LIMIT 1")
    Reminder getByRelatedEntity(String entityType, long entityId, String reminderType);

    @Query("UPDATE reminders SET isCompleted = 1, updatedAt = :updatedAt WHERE id = :id")
    void markCompleted(long id, long updatedAt);

    @Query("UPDATE reminders SET isCompleted = 0, updatedAt = :updatedAt WHERE id = :id")
    void markIncomplete(long id, long updatedAt);

    @Query("UPDATE reminders SET isEnabled = :enabled, updatedAt = :updatedAt WHERE id = :id")
    void setEnabledState(long id, boolean enabled, long updatedAt);
}

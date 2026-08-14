package com.rob.houserental.data;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.rob.houserental.model.BackupHistory;

import java.util.List;

@Dao
public interface BackupHistoryDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insert(BackupHistory backupHistory);

    @Update
    void update(BackupHistory backupHistory);

    @Delete
    void delete(BackupHistory backupHistory);

    @Query("SELECT * FROM backup_history ORDER BY createdAt DESC")
    List<BackupHistory> getAllHistory();

    @Query("SELECT * FROM backup_history WHERE status = 'SUCCESS' ORDER BY createdAt DESC LIMIT 1")
    BackupHistory getLatestSuccessfulBackup();

    @Query("SELECT * FROM backup_history WHERE id = :id LIMIT 1")
    BackupHistory getById(long id);

    @Query("SELECT * FROM backup_history WHERE backupId = :backupId LIMIT 1")
    BackupHistory getByBackupId(String backupId);

    @Query("SELECT COUNT(*) FROM backup_history")
    int getTotalBackupCount();

    @Query("DELETE FROM backup_history WHERE createdAt < :cutoffTimestamp AND id NOT IN (SELECT id FROM backup_history ORDER BY createdAt DESC LIMIT 10)")
    int pruneOldHistory(long cutoffTimestamp);
}

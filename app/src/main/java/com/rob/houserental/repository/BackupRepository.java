package com.rob.houserental.repository;

import android.content.Context;

import com.rob.houserental.data.AppDatabase;
import com.rob.houserental.data.BackupHistoryDao;
import com.rob.houserental.model.BackupHistory;
import com.rob.houserental.utils.AppExecutors;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;

public class BackupRepository {

    private final AppDatabase database;
    private final BackupHistoryDao historyDao;
    private final ExecutorService executor = AppExecutors.getInstance().getDatabaseExecutor();

    public interface DatabaseCallback<T> extends com.rob.houserental.repository.DatabaseCallback<T> {}

    public BackupRepository(Context context) {
        database = AppDatabase.getInstance(context);
        historyDao = database.backupHistoryDao();
    }

    public void insertHistory(BackupHistory history, com.rob.houserental.repository.DatabaseCallback<Long> callback) {
        executor.execute(() -> {
            try {
                long id = historyDao.insert(history);
                if (callback != null) callback.onSuccess(id);
            } catch (Exception e) {
                if (callback != null) callback.onError(e);
            }
        });
    }

    public void updateHistory(BackupHistory history, com.rob.houserental.repository.DatabaseCallback<Void> callback) {
        executor.execute(() -> {
            try {
                historyDao.update(history);
                if (callback != null) callback.onSuccess(null);
            } catch (Exception e) {
                if (callback != null) callback.onError(e);
            }
        });
    }

    public void getAllHistory(com.rob.houserental.repository.DatabaseCallback<List<BackupHistory>> callback) {
        executor.execute(() -> {
            try {
                List<BackupHistory> list = historyDao.getAllHistory();
                if (callback != null) callback.onSuccess(list != null ? list : new ArrayList<>());
            } catch (Exception e) {
                if (callback != null) callback.onError(e);
            }
        });
    }

    public void getLatestSuccessfulBackup(com.rob.houserental.repository.DatabaseCallback<BackupHistory> callback) {
        executor.execute(() -> {
            try {
                BackupHistory history = historyDao.getLatestSuccessfulBackup();
                if (callback != null) callback.onSuccess(history);
            } catch (Exception e) {
                if (callback != null) callback.onError(e);
            }
        });
    }

    public void deleteHistory(BackupHistory history, com.rob.houserental.repository.DatabaseCallback<Void> callback) {
        executor.execute(() -> {
            try {
                historyDao.delete(history);
                if (callback != null) callback.onSuccess(null);
            } catch (Exception e) {
                if (callback != null) callback.onError(e);
            }
        });
    }
}

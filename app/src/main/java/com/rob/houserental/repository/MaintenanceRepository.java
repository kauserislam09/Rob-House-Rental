package com.rob.houserental.repository;

import android.content.Context;

import com.rob.houserental.data.AppDatabase;
import com.rob.houserental.data.MaintenanceDao;
import com.rob.houserental.model.MaintenanceRecord;
import com.rob.houserental.utils.AppExecutors;

import java.util.List;
import java.util.concurrent.ExecutorService;

public class MaintenanceRepository {

    private final MaintenanceDao maintenanceDao;
    private final ExecutorService executorService =
            AppExecutors.getInstance().getDatabaseExecutor();

    public interface DatabaseCallback<T> extends com.rob.houserental.repository.DatabaseCallback<T> {}

    public MaintenanceRepository(Context context) {
        AppDatabase db = AppDatabase.getInstance(context);
        this.maintenanceDao = db.maintenanceDao();
    }

    public void insert(MaintenanceRecord record, com.rob.houserental.repository.DatabaseCallback<Long> callback) {
        executorService.execute(() -> {
            try {
                long id = maintenanceDao.insert(record);
                if (callback != null) callback.onSuccess(id);
            } catch (Exception e) {
                if (callback != null) callback.onError(e);
            }
        });
    }

    public void update(MaintenanceRecord record, com.rob.houserental.repository.DatabaseCallback<Void> callback) {
        executorService.execute(() -> {
            try {
                maintenanceDao.update(record);
                if (callback != null) callback.onSuccess(null);
            } catch (Exception e) {
                if (callback != null) callback.onError(e);
            }
        });
    }

    public void delete(MaintenanceRecord record, com.rob.houserental.repository.DatabaseCallback<Void> callback) {
        executorService.execute(() -> {
            try {
                maintenanceDao.delete(record);
                if (callback != null) callback.onSuccess(null);
            } catch (Exception e) {
                if (callback != null) callback.onError(e);
            }
        });
    }

    public void getById(long id, com.rob.houserental.repository.DatabaseCallback<MaintenanceRecord> callback) {
        executorService.execute(() -> {
            try {
                MaintenanceRecord record = maintenanceDao.getById(id);
                if (callback != null) callback.onSuccess(record);
            } catch (Exception e) {
                if (callback != null) callback.onError(e);
            }
        });
    }

    public void getAll(com.rob.houserental.repository.DatabaseCallback<List<MaintenanceRecord>> callback) {
        executorService.execute(() -> {
            try {
                List<MaintenanceRecord> list = maintenanceDao.getAll();
                if (callback != null) callback.onSuccess(list);
            } catch (Exception e) {
                if (callback != null) callback.onError(e);
            }
        });
    }

    public void getByProperty(long propertyId, com.rob.houserental.repository.DatabaseCallback<List<MaintenanceRecord>> callback) {
        executorService.execute(() -> {
            try {
                List<MaintenanceRecord> list = maintenanceDao.getByProperty(propertyId);
                if (callback != null) callback.onSuccess(list);
            } catch (Exception e) {
                if (callback != null) callback.onError(e);
            }
        });
    }

    public void getByUnit(long unitId, com.rob.houserental.repository.DatabaseCallback<List<MaintenanceRecord>> callback) {
        executorService.execute(() -> {
            try {
                List<MaintenanceRecord> list = maintenanceDao.getByUnit(unitId);
                if (callback != null) callback.onSuccess(list);
            } catch (Exception e) {
                if (callback != null) callback.onError(e);
            }
        });
    }

    public void getByStatus(String status, com.rob.houserental.repository.DatabaseCallback<List<MaintenanceRecord>> callback) {
        executorService.execute(() -> {
            try {
                List<MaintenanceRecord> list = maintenanceDao.getByStatus(status);
                if (callback != null) callback.onSuccess(list);
            } catch (Exception e) {
                if (callback != null) callback.onError(e);
            }
        });
    }
}

package com.rob.houserental.repository;

import android.content.Context;

import com.rob.houserental.data.AppDatabase;
import com.rob.houserental.data.UnitDao;
import com.rob.houserental.model.Unit;
import com.rob.houserental.utils.AppExecutors;

import java.util.List;
import java.util.concurrent.ExecutorService;

public class UnitRepository {

    private final UnitDao unitDao;

    private final ExecutorService databaseExecutor =
            AppExecutors.getInstance().getDatabaseExecutor();

    public interface DatabaseCallback<T> extends com.rob.houserental.repository.DatabaseCallback<T> {}

    public UnitRepository(Context context) {

        AppDatabase database =
                AppDatabase.getInstance(context);

        unitDao = database.unitDao();
    }

    public void insert(
            Unit unit,
            com.rob.houserental.repository.DatabaseCallback<Long> callback
    ) {

        databaseExecutor.execute(() -> {

            try {

                long id =
                        unitDao.insert(unit);

                if (callback != null) {
                    callback.onSuccess(id);
                }

            } catch (Exception e) {

                if (callback != null) {
                    callback.onError(e);
                }
            }
        });
    }

    public void getUnitsByProperty(
            long propertyId,
            com.rob.houserental.repository.DatabaseCallback<List<Unit>> callback
    ) {

        databaseExecutor.execute(() -> {

            try {

                List<Unit> units =
                        unitDao.getUnitsByProperty(
                                propertyId
                        );

                if (callback != null) {
                    callback.onSuccess(units);
                }

            } catch (Exception e) {

                if (callback != null) {
                    callback.onError(e);
                }
            }
        });
    }

    public void getUnitById(
            long unitId,
            com.rob.houserental.repository.DatabaseCallback<Unit> callback
    ) {

        databaseExecutor.execute(() -> {

            try {

                Unit unit =
                        unitDao.getUnitById(
                                unitId
                        );

                if (callback != null) {
                    callback.onSuccess(unit);
                }

            } catch (Exception e) {

                if (callback != null) {
                    callback.onError(e);
                }
            }
        });
    }

    public void update(
            Unit unit,
            com.rob.houserental.repository.DatabaseCallback<Void> callback
    ) {

        databaseExecutor.execute(() -> {

            try {

                unitDao.update(unit);

                if (callback != null) {
                    callback.onSuccess(null);
                }

            } catch (Exception e) {

                if (callback != null) {
                    callback.onError(e);
                }
            }
        });
    }

    public void delete(
            Unit unit,
            com.rob.houserental.repository.DatabaseCallback<Void> callback
    ) {

        databaseExecutor.execute(() -> {

            try {

                unitDao.delete(unit);

                if (callback != null) {
                    callback.onSuccess(null);
                }

            } catch (Exception e) {

                if (callback != null) {
                    callback.onError(e);
                }
            }
        });
    }

    public void getUnitCount(
            long propertyId,
            com.rob.houserental.repository.DatabaseCallback<Integer> callback
    ) {

        databaseExecutor.execute(() -> {

            try {

                int count =
                        unitDao.getUnitCount(
                                propertyId
                        );

                if (callback != null) {
                    callback.onSuccess(count);
                }

            } catch (Exception e) {

                if (callback != null) {
                    callback.onError(e);
                }
            }
        });
    }

    public void getUnitCountByStatus(
            long propertyId,
            String status,
            com.rob.houserental.repository.DatabaseCallback<Integer> callback
    ) {

        databaseExecutor.execute(() -> {

            try {

                int count =
                        unitDao.getUnitCountByStatus(
                                propertyId,
                                status
                        );

                if (callback != null) {
                    callback.onSuccess(count);
                }

            } catch (Exception e) {

                if (callback != null) {
                    callback.onError(e);
                }
            }
        });
    }

    public void countDuplicateUnitNumber(
            long propertyId,
            String unitNumber,
            long excludeUnitId,
            com.rob.houserental.repository.DatabaseCallback<Integer> callback
    ) {
        databaseExecutor.execute(() -> {
            try {
                int count = unitDao.countDuplicateUnitNumber(
                        propertyId,
                        unitNumber,
                        excludeUnitId
                );

                if (callback != null) {
                    callback.onSuccess(count);
                }
            } catch (Exception e) {
                if (callback != null) {
                    callback.onError(e);
                }
            }
        });
    }
}
package com.rob.houserental.repository;

import android.content.Context;

import com.rob.houserental.data.AppDatabase;
import com.rob.houserental.data.PropertyDao;
import com.rob.houserental.model.Property;
import com.rob.houserental.utils.AppExecutors;

import java.util.List;
import java.util.concurrent.ExecutorService;

public class PropertyRepository {

    private final PropertyDao propertyDao;

    private final ExecutorService databaseExecutor =
            AppExecutors.getInstance().getDatabaseExecutor();

    public interface DatabaseCallback<T> extends com.rob.houserental.repository.DatabaseCallback<T> {}

    public PropertyRepository(Context context) {

        AppDatabase database =
                AppDatabase.getInstance(context);

        propertyDao = database.propertyDao();
    }

    public void insert(
            Property property,
            com.rob.houserental.repository.DatabaseCallback<Long> callback
    ) {

        databaseExecutor.execute(() -> {

            try {

                long id = propertyDao.insert(property);

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

    public void getAllProperties(
            com.rob.houserental.repository.DatabaseCallback<List<Property>> callback
    ) {

        databaseExecutor.execute(() -> {

            try {

                List<Property> properties =
                        propertyDao.getAllProperties();

                if (callback != null) {
                    callback.onSuccess(properties);
                }

            } catch (Exception e) {

                if (callback != null) {
                    callback.onError(e);
                }
            }
        });
    }

    public void update(
            Property property,
            com.rob.houserental.repository.DatabaseCallback<Void> callback
    ) {

        databaseExecutor.execute(() -> {

            try {

                propertyDao.update(property);

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
            Property property,
            com.rob.houserental.repository.DatabaseCallback<Void> callback
    ) {

        databaseExecutor.execute(() -> {

            try {

                propertyDao.delete(property);

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

    public void getPropertyById(long propertyId, com.rob.houserental.repository.DatabaseCallback<Property> callback) {
        databaseExecutor.execute(() -> {
            try {
                Property property = propertyDao.getPropertyById(propertyId);
                if (callback != null) {
                    callback.onSuccess(property);
                }
            } catch (Exception e) {
                if (callback != null) {
                    callback.onError(e);
                }
            }
        });
    }
}
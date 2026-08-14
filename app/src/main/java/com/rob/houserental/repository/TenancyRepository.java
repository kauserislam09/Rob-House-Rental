package com.rob.houserental.repository;

import android.content.Context;

import com.rob.houserental.data.AppDatabase;
import com.rob.houserental.data.TenancyDao;
import com.rob.houserental.data.UnitDao;
import com.rob.houserental.model.Tenancy;
import com.rob.houserental.model.TenancyWithDetails;
import com.rob.houserental.model.Unit;
import com.rob.houserental.utils.AppExecutors;

import java.util.List;
import java.util.concurrent.ExecutorService;

public class TenancyRepository {

    private final AppDatabase database;
    private final TenancyDao tenancyDao;
    private final UnitDao unitDao;

    private final ExecutorService databaseExecutor =
            AppExecutors.getInstance().getDatabaseExecutor();

    private final Context context;

    public interface DatabaseCallback<T> extends com.rob.houserental.repository.DatabaseCallback<T> {}

    public TenancyRepository(Context context) {
        this.context = context.getApplicationContext();
        database = AppDatabase.getInstance(this.context);
        tenancyDao = database.tenancyDao();
        unitDao = database.unitDao();
    }

    public void createTenancy(Tenancy tenancy, com.rob.houserental.repository.DatabaseCallback<Long> callback) {
        databaseExecutor.execute(() -> {
            try {
                database.runInTransaction(() -> {
                    Unit unit = unitDao.getUnitById(tenancy.getUnitId());
                    if (unit == null) {
                        throw new IllegalStateException("Unit not found");
                    }

                    if ("ACTIVE".equalsIgnoreCase(tenancy.getStatus())) {
                        Tenancy existingActive = tenancyDao.getActiveTenancyByUnit(tenancy.getUnitId());
                        if (existingActive != null) {
                            throw new IllegalStateException("Unit is already occupied");
                        }
                    }

                    long id = tenancyDao.insert(tenancy);
                    tenancy.setId(id);

                    if ("ACTIVE".equalsIgnoreCase(tenancy.getStatus())) {
                        unit.setStatus("OCCUPIED");
                        unit.setUpdatedAt(System.currentTimeMillis());
                        unitDao.update(unit);
                    }
                    com.rob.houserental.notifications.AutomaticReminderUtils.syncTenancyExpiryReminder(context, tenancy);
                });

                if (callback != null) {
                    callback.onSuccess(tenancy.getId());
                }
            } catch (Exception e) {
                if (callback != null) {
                    callback.onError(e);
                }
            }
        });
    }

    public void endTenancy(Tenancy tenancy, String endDate, com.rob.houserental.repository.DatabaseCallback<Void> callback) {
        databaseExecutor.execute(() -> {
            try {
                database.runInTransaction(() -> {
                    tenancy.setStatus("ENDED");
                    if (endDate != null && !endDate.trim().isEmpty()) {
                        tenancy.setEndDate(endDate.trim());
                    }
                    tenancy.setUpdatedAt(System.currentTimeMillis());
                    tenancyDao.update(tenancy);

                    Unit unit = unitDao.getUnitById(tenancy.getUnitId());
                    if (unit != null) {
                        unit.setStatus("VACANT");
                        unit.setUpdatedAt(System.currentTimeMillis());
                        unitDao.update(unit);
                    }
                    com.rob.houserental.notifications.AutomaticReminderUtils.syncTenancyExpiryReminder(context, tenancy);
                });

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

    public void cancelTenancy(Tenancy tenancy, com.rob.houserental.repository.DatabaseCallback<Void> callback) {
        databaseExecutor.execute(() -> {
            try {
                database.runInTransaction(() -> {
                    boolean wasActive = "ACTIVE".equalsIgnoreCase(tenancy.getStatus());
                    tenancy.setStatus("CANCELLED");
                    tenancy.setUpdatedAt(System.currentTimeMillis());
                    tenancyDao.update(tenancy);

                    if (wasActive) {
                        Unit unit = unitDao.getUnitById(tenancy.getUnitId());
                        if (unit != null) {
                            unit.setStatus("VACANT");
                            unit.setUpdatedAt(System.currentTimeMillis());
                            unitDao.update(unit);
                        }
                    }
                    com.rob.houserental.notifications.AutomaticReminderUtils.syncTenancyExpiryReminder(context, tenancy);
                });

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

    public void update(Tenancy tenancy, com.rob.houserental.repository.DatabaseCallback<Void> callback) {
        databaseExecutor.execute(() -> {
            try {
                database.runInTransaction(() -> {
                    if ("ACTIVE".equalsIgnoreCase(tenancy.getStatus())) {
                        Tenancy existingActive = tenancyDao.getActiveTenancyByUnit(tenancy.getUnitId());
                        if (existingActive != null && existingActive.getId() != tenancy.getId()) {
                            throw new IllegalStateException("Unit is already occupied");
                        }
                    }
                    tenancyDao.update(tenancy);
                    com.rob.houserental.notifications.AutomaticReminderUtils.syncTenancyExpiryReminder(context, tenancy);
                });
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

    public void delete(Tenancy tenancy, com.rob.houserental.repository.DatabaseCallback<Void> callback) {
        databaseExecutor.execute(() -> {
            try {
                tenancyDao.delete(tenancy);
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

    public void getTenancyById(long tenancyId, com.rob.houserental.repository.DatabaseCallback<Tenancy> callback) {
        databaseExecutor.execute(() -> {
            try {
                Tenancy tenancy = tenancyDao.getTenancyById(tenancyId);
                if (callback != null) {
                    callback.onSuccess(tenancy);
                }
            } catch (Exception e) {
                if (callback != null) {
                    callback.onError(e);
                }
            }
        });
    }

    public void getTenancyWithDetailsById(long tenancyId, com.rob.houserental.repository.DatabaseCallback<TenancyWithDetails> callback) {
        databaseExecutor.execute(() -> {
            try {
                TenancyWithDetails details = tenancyDao.getTenancyWithDetailsById(tenancyId);
                if (callback != null) {
                    callback.onSuccess(details);
                }
            } catch (Exception e) {
                if (callback != null) {
                    callback.onError(e);
                }
            }
        });
    }

    public void getAllTenanciesWithDetails(com.rob.houserental.repository.DatabaseCallback<List<TenancyWithDetails>> callback) {
        databaseExecutor.execute(() -> {
            try {
                List<TenancyWithDetails> list = tenancyDao.getAllTenanciesWithDetails();
                if (callback != null) {
                    callback.onSuccess(list);
                }
            } catch (Exception e) {
                if (callback != null) {
                    callback.onError(e);
                }
            }
        });
    }

    public void getTenanciesWithDetailsByStatus(String status, com.rob.houserental.repository.DatabaseCallback<List<TenancyWithDetails>> callback) {
        databaseExecutor.execute(() -> {
            try {
                List<TenancyWithDetails> list = tenancyDao.getTenanciesWithDetailsByStatus(status);
                if (callback != null) {
                    callback.onSuccess(list);
                }
            } catch (Exception e) {
                if (callback != null) {
                    callback.onError(e);
                }
            }
        });
    }

    public void getActiveTenancyWithDetailsByUnit(long unitId, com.rob.houserental.repository.DatabaseCallback<TenancyWithDetails> callback) {
        databaseExecutor.execute(() -> {
            try {
                TenancyWithDetails details = tenancyDao.getActiveTenancyWithDetailsByUnit(unitId);
                if (callback != null) {
                    callback.onSuccess(details);
                }
            } catch (Exception e) {
                if (callback != null) {
                    callback.onError(e);
                }
            }
        });
    }

    public void getActiveTenancyWithDetailsByTenant(long tenantId, com.rob.houserental.repository.DatabaseCallback<TenancyWithDetails> callback) {
        databaseExecutor.execute(() -> {
            try {
                TenancyWithDetails details = tenancyDao.getActiveTenancyWithDetailsByTenant(tenantId);
                if (callback != null) {
                    callback.onSuccess(details);
                }
            } catch (Exception e) {
                if (callback != null) {
                    callback.onError(e);
                }
            }
        });
    }

    public void getTenanciesWithDetailsByUnit(long unitId, com.rob.houserental.repository.DatabaseCallback<List<TenancyWithDetails>> callback) {
        databaseExecutor.execute(() -> {
            try {
                List<TenancyWithDetails> list = tenancyDao.getTenanciesWithDetailsByUnit(unitId);
                if (callback != null) {
                    callback.onSuccess(list);
                }
            } catch (Exception e) {
                if (callback != null) {
                    callback.onError(e);
                }
            }
        });
    }

    public void getTenanciesWithDetailsByTenant(long tenantId, com.rob.houserental.repository.DatabaseCallback<List<TenancyWithDetails>> callback) {
        databaseExecutor.execute(() -> {
            try {
                List<TenancyWithDetails> list = tenancyDao.getTenanciesWithDetailsByTenant(tenantId);
                if (callback != null) {
                    callback.onSuccess(list);
                }
            } catch (Exception e) {
                if (callback != null) {
                    callback.onError(e);
                }
            }
        });
    }

    public void getTenancyCountByStatus(String status, com.rob.houserental.repository.DatabaseCallback<Integer> callback) {
        databaseExecutor.execute(() -> {
            try {
                int count = tenancyDao.getTenancyCountByStatus(status);
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

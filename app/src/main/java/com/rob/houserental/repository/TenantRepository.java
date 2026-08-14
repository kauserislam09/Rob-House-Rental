package com.rob.houserental.repository;

import android.content.Context;

import com.rob.houserental.data.AppDatabase;
import com.rob.houserental.data.TenantDao;
import com.rob.houserental.data.TenantDocumentDao;
import com.rob.houserental.model.Tenant;
import com.rob.houserental.model.TenantDocument;
import com.rob.houserental.utils.AppExecutors;

import java.util.List;
import java.util.concurrent.ExecutorService;

public class TenantRepository {

    private final TenantDao tenantDao;
    private final TenantDocumentDao tenantDocumentDao;

    private final ExecutorService databaseExecutor =
            AppExecutors.getInstance().getDatabaseExecutor();

    public interface DatabaseCallback<T> extends com.rob.houserental.repository.DatabaseCallback<T> {}

    public TenantRepository(Context context) {
        AppDatabase database = AppDatabase.getInstance(context);
        tenantDao = database.tenantDao();
        tenantDocumentDao = database.tenantDocumentDao();
    }

    public void insert(Tenant tenant, com.rob.houserental.repository.DatabaseCallback<Long> callback) {
        databaseExecutor.execute(() -> {
            try {
                long id = tenantDao.insert(tenant);
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

    public void update(Tenant tenant, com.rob.houserental.repository.DatabaseCallback<Void> callback) {
        databaseExecutor.execute(() -> {
            try {
                tenantDao.update(tenant);
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

    public void delete(Tenant tenant, com.rob.houserental.repository.DatabaseCallback<Void> callback) {
        databaseExecutor.execute(() -> {
            try {
                tenantDao.delete(tenant);
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

    public void getTenantById(long tenantId, com.rob.houserental.repository.DatabaseCallback<Tenant> callback) {
        databaseExecutor.execute(() -> {
            try {
                Tenant tenant = tenantDao.getTenantById(tenantId);
                if (callback != null) {
                    callback.onSuccess(tenant);
                }
            } catch (Exception e) {
                if (callback != null) {
                    callback.onError(e);
                }
            }
        });
    }

    public void getAllTenants(com.rob.houserental.repository.DatabaseCallback<List<Tenant>> callback) {
        databaseExecutor.execute(() -> {
            try {
                List<Tenant> tenants = tenantDao.getAllTenants();
                if (callback != null) {
                    callback.onSuccess(tenants);
                }
            } catch (Exception e) {
                if (callback != null) {
                    callback.onError(e);
                }
            }
        });
    }

    public void searchTenants(String query, com.rob.houserental.repository.DatabaseCallback<List<Tenant>> callback) {
        databaseExecutor.execute(() -> {
            try {
                List<Tenant> tenants = tenantDao.searchTenants(query);
                if (callback != null) {
                    callback.onSuccess(tenants);
                }
            } catch (Exception e) {
                if (callback != null) {
                    callback.onError(e);
                }
            }
        });
    }

    public void getActiveTenants(com.rob.houserental.repository.DatabaseCallback<List<Tenant>> callback) {
        databaseExecutor.execute(() -> {
            try {
                List<Tenant> tenants = tenantDao.getActiveTenants();
                if (callback != null) {
                    callback.onSuccess(tenants);
                }
            } catch (Exception e) {
                if (callback != null) {
                    callback.onError(e);
                }
            }
        });
    }

    public void getTenantsByStatus(String status, com.rob.houserental.repository.DatabaseCallback<List<Tenant>> callback) {
        databaseExecutor.execute(() -> {
            try {
                List<Tenant> tenants = tenantDao.getTenantsByStatus(status);
                if (callback != null) {
                    callback.onSuccess(tenants);
                }
            } catch (Exception e) {
                if (callback != null) {
                    callback.onError(e);
                }
            }
        });
    }

    public void getTenantCount(com.rob.houserental.repository.DatabaseCallback<Integer> callback) {
        databaseExecutor.execute(() -> {
            try {
                int count = tenantDao.getTenantCount();
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

    public void getTenantCountByStatus(String status, com.rob.houserental.repository.DatabaseCallback<Integer> callback) {
        databaseExecutor.execute(() -> {
            try {
                int count = tenantDao.getTenantCountByStatus(status);
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

    // Document operations
    public void insertDocument(TenantDocument document, com.rob.houserental.repository.DatabaseCallback<Long> callback) {
        databaseExecutor.execute(() -> {
            try {
                long id = tenantDocumentDao.insert(document);
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

    public void updateDocument(TenantDocument document, com.rob.houserental.repository.DatabaseCallback<Void> callback) {
        databaseExecutor.execute(() -> {
            try {
                tenantDocumentDao.update(document);
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

    public void deleteDocument(TenantDocument document, com.rob.houserental.repository.DatabaseCallback<Void> callback) {
        databaseExecutor.execute(() -> {
            try {
                tenantDocumentDao.delete(document);
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

    public void getDocumentsByTenant(long tenantId, com.rob.houserental.repository.DatabaseCallback<List<TenantDocument>> callback) {
        databaseExecutor.execute(() -> {
            try {
                List<TenantDocument> documents = tenantDocumentDao.getDocumentsByTenant(tenantId);
                if (callback != null) {
                    callback.onSuccess(documents);
                }
            } catch (Exception e) {
                if (callback != null) {
                    callback.onError(e);
                }
            }
        });
    }

    public void getDocumentById(long documentId, com.rob.houserental.repository.DatabaseCallback<TenantDocument> callback) {
        databaseExecutor.execute(() -> {
            try {
                TenantDocument document = tenantDocumentDao.getDocumentById(documentId);
                if (callback != null) {
                    callback.onSuccess(document);
                }
            } catch (Exception e) {
                if (callback != null) {
                    callback.onError(e);
                }
            }
        });
    }
}

package com.rob.houserental.repository;

import android.content.Context;

import com.rob.houserental.data.AppDatabase;
import com.rob.houserental.data.AppDocumentDao;
import com.rob.houserental.model.AppDocument;
import com.rob.houserental.model.AppDocumentDisplayItem;
import com.rob.houserental.utils.AppExecutors;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;

public class DocumentRepository {

    private final AppDatabase database;
    private final AppDocumentDao documentDao;

    private final ExecutorService databaseExecutor =
            AppExecutors.getInstance().getDatabaseExecutor();

    public interface DatabaseCallback<T> extends com.rob.houserental.repository.DatabaseCallback<T> {}



    public DocumentRepository(Context context) {
        database = AppDatabase.getInstance(context);
        documentDao = database.appDocumentDao();
    }

    public void saveDocument(AppDocument document, com.rob.houserental.repository.DatabaseCallback<Long> callback) {
        databaseExecutor.execute(() -> {
            try {
                long currentTime = System.currentTimeMillis();
                if (document.getCreatedAt() <= 0) {
                    document.setCreatedAt(currentTime);
                }
                document.setUpdatedAt(currentTime);

                long id = documentDao.insert(document);
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

    public void updateDocument(AppDocument document, com.rob.houserental.repository.DatabaseCallback<Void> callback) {
        databaseExecutor.execute(() -> {
            try {
                document.setUpdatedAt(System.currentTimeMillis());
                documentDao.update(document);
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

    public void renameDocument(long id, String newDisplayName, com.rob.houserental.repository.DatabaseCallback<Void> callback) {
        databaseExecutor.execute(() -> {
            try {
                AppDocument document = documentDao.getDocumentById(id);
                if (document != null) {
                    document.setDisplayName(newDisplayName);
                    document.setUpdatedAt(System.currentTimeMillis());
                    documentDao.update(document);
                }
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

    public void archiveDocument(long id, com.rob.houserental.repository.DatabaseCallback<Void> callback) {
        databaseExecutor.execute(() -> {
            try {
                AppDocument document = documentDao.getDocumentById(id);
                if (document != null) {
                    document.setArchived(true);
                    document.setUpdatedAt(System.currentTimeMillis());
                    documentDao.update(document);
                }
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

    public void deleteDocument(AppDocument document, boolean deletePhysicalFile, com.rob.houserental.repository.DatabaseCallback<Void> callback) {
        databaseExecutor.execute(() -> {
            try {
                if (deletePhysicalFile && document.getFilePath() != null) {
                    try {
                        File file = new File(document.getFilePath());
                        if (file.exists()) {
                            file.delete();
                        }
                    } catch (Exception ignored) {
                    }
                }
                documentDao.delete(document);
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

    public void getDocumentById(long id, com.rob.houserental.repository.DatabaseCallback<AppDocument> callback) {
        databaseExecutor.execute(() -> {
            try {
                AppDocument document = documentDao.getDocumentById(id);
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

    public void getDisplayItemById(long id, com.rob.houserental.repository.DatabaseCallback<AppDocumentDisplayItem> callback) {
        databaseExecutor.execute(() -> {
            try {
                AppDocumentDisplayItem item = documentDao.getDisplayItemById(id);
                if (callback != null) {
                    callback.onSuccess(item);
                }
            } catch (Exception e) {
                if (callback != null) {
                    callback.onError(e);
                }
            }
        });
    }

    public void getAllDisplayItems(com.rob.houserental.repository.DatabaseCallback<List<AppDocumentDisplayItem>> callback) {
        databaseExecutor.execute(() -> {
            try {
                List<AppDocumentDisplayItem> list = documentDao.getAllDisplayItems();
                if (callback != null) {
                    callback.onSuccess(list != null ? list : new ArrayList<>());
                }
            } catch (Exception e) {
                if (callback != null) {
                    callback.onError(e);
                }
            }
        });
    }

    public void getDisplayItemsByType(String documentType, com.rob.houserental.repository.DatabaseCallback<List<AppDocumentDisplayItem>> callback) {
        databaseExecutor.execute(() -> {
            try {
                List<AppDocumentDisplayItem> list = documentDao.getDisplayItemsByType(documentType);
                if (callback != null) {
                    callback.onSuccess(list != null ? list : new ArrayList<>());
                }
            } catch (Exception e) {
                if (callback != null) {
                    callback.onError(e);
                }
            }
        });
    }

    public void getDisplayItemsByProperty(long propertyId, com.rob.houserental.repository.DatabaseCallback<List<AppDocumentDisplayItem>> callback) {
        databaseExecutor.execute(() -> {
            try {
                List<AppDocumentDisplayItem> list = documentDao.getDisplayItemsByProperty(propertyId);
                if (callback != null) {
                    callback.onSuccess(list != null ? list : new ArrayList<>());
                }
            } catch (Exception e) {
                if (callback != null) {
                    callback.onError(e);
                }
            }
        });
    }

    public void getDisplayItemsByTenant(long tenantId, com.rob.houserental.repository.DatabaseCallback<List<AppDocumentDisplayItem>> callback) {
        databaseExecutor.execute(() -> {
            try {
                List<AppDocumentDisplayItem> list = documentDao.getDisplayItemsByTenant(tenantId);
                if (callback != null) {
                    callback.onSuccess(list != null ? list : new ArrayList<>());
                }
            } catch (Exception e) {
                if (callback != null) {
                    callback.onError(e);
                }
            }
        });
    }

    public void searchDisplayItems(String query, com.rob.houserental.repository.DatabaseCallback<List<AppDocumentDisplayItem>> callback) {
        databaseExecutor.execute(() -> {
            try {
                List<AppDocumentDisplayItem> list = documentDao.searchDisplayItems(query != null ? query.trim() : "");
                if (callback != null) {
                    callback.onSuccess(list != null ? list : new ArrayList<>());
                }
            } catch (Exception e) {
                if (callback != null) {
                    callback.onError(e);
                }
            }
        });
    }

    public void getTotalDocumentCount(com.rob.houserental.repository.DatabaseCallback<Integer> callback) {
        databaseExecutor.execute(() -> {
            try {
                int count = documentDao.getTotalDocumentCount();
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

    public void getTotalDocumentStorageBytes(com.rob.houserental.repository.DatabaseCallback<Long> callback) {
        databaseExecutor.execute(() -> {
            try {
                long bytes = documentDao.getTotalDocumentStorageBytes();
                if (callback != null) {
                    callback.onSuccess(bytes);
                }
            } catch (Exception e) {
                if (callback != null) {
                    callback.onError(e);
                }
            }
        });
    }
}

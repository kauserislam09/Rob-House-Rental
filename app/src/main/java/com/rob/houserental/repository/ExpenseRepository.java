package com.rob.houserental.repository;

import android.content.Context;

import com.rob.houserental.data.AppDatabase;
import com.rob.houserental.data.ExpenseDao;
import com.rob.houserental.model.Expense;
import com.rob.houserental.model.ExpenseDisplayItem;
import com.rob.houserental.utils.AppExecutors;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;

public class ExpenseRepository {

    private final AppDatabase database;
    private final ExpenseDao expenseDao;

    private final ExecutorService databaseExecutor =
            AppExecutors.getInstance().getDatabaseExecutor();

    public interface DatabaseCallback<T> extends com.rob.houserental.repository.DatabaseCallback<T> {}



    public ExpenseRepository(Context context) {
        database = AppDatabase.getInstance(context);
        expenseDao = database.expenseDao();
    }

    public void createExpense(Expense expense, com.rob.houserental.repository.DatabaseCallback<Long> callback) {
        databaseExecutor.execute(() -> {
            try {
                if (expense.getAmount() <= 0) {
                    throw new IllegalArgumentException("Expense amount must be greater than zero");
                }

                long currentTime = System.currentTimeMillis();
                if (expense.getCreatedAt() <= 0) {
                    expense.setCreatedAt(currentTime);
                }
                expense.setUpdatedAt(currentTime);

                long id = expenseDao.insert(expense);
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

    public void updateExpense(Expense expense, com.rob.houserental.repository.DatabaseCallback<Void> callback) {
        databaseExecutor.execute(() -> {
            try {
                if (expense.getAmount() <= 0) {
                    throw new IllegalArgumentException("Expense amount must be greater than zero");
                }

                expense.setUpdatedAt(System.currentTimeMillis());
                expenseDao.update(expense);
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

    public void archiveExpense(long id, com.rob.houserental.repository.DatabaseCallback<Void> callback) {
        databaseExecutor.execute(() -> {
            try {
                Expense expense = expenseDao.getExpenseById(id);
                if (expense != null) {
                    expense.setArchived(true);
                    expense.setUpdatedAt(System.currentTimeMillis());
                    expenseDao.update(expense);
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

    public void deleteExpense(Expense expense, com.rob.houserental.repository.DatabaseCallback<Void> callback) {
        databaseExecutor.execute(() -> {
            try {
                expenseDao.delete(expense);
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

    public void getExpenseById(long id, com.rob.houserental.repository.DatabaseCallback<Expense> callback) {
        databaseExecutor.execute(() -> {
            try {
                Expense expense = expenseDao.getExpenseById(id);
                if (callback != null) {
                    callback.onSuccess(expense);
                }
            } catch (Exception e) {
                if (callback != null) {
                    callback.onError(e);
                }
            }
        });
    }

    public void getExpenseDisplayItemById(long id, com.rob.houserental.repository.DatabaseCallback<ExpenseDisplayItem> callback) {
        databaseExecutor.execute(() -> {
            try {
                ExpenseDisplayItem item = expenseDao.getExpenseDisplayItemById(id);
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

    public void getAllExpenseDisplayItems(com.rob.houserental.repository.DatabaseCallback<List<ExpenseDisplayItem>> callback) {
        databaseExecutor.execute(() -> {
            try {
                List<ExpenseDisplayItem> list = expenseDao.getAllExpenseDisplayItems();
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

    public void getExpenseDisplayItemsByMonth(String expenseMonth, com.rob.houserental.repository.DatabaseCallback<List<ExpenseDisplayItem>> callback) {
        databaseExecutor.execute(() -> {
            try {
                List<ExpenseDisplayItem> list = expenseDao.getExpenseDisplayItemsByMonth(expenseMonth);
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

    public void getExpenseDisplayItemsByProperty(long propertyId, com.rob.houserental.repository.DatabaseCallback<List<ExpenseDisplayItem>> callback) {
        databaseExecutor.execute(() -> {
            try {
                List<ExpenseDisplayItem> list = expenseDao.getExpenseDisplayItemsByProperty(propertyId);
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

    public void searchExpenses(String query, com.rob.houserental.repository.DatabaseCallback<List<ExpenseDisplayItem>> callback) {
        databaseExecutor.execute(() -> {
            try {
                List<ExpenseDisplayItem> list = expenseDao.searchExpenses(query != null ? query.trim() : "");
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

    public void getMonthlyExpenseTotal(String expenseMonth, com.rob.houserental.repository.DatabaseCallback<Double> callback) {
        databaseExecutor.execute(() -> {
            try {
                double total = expenseDao.getMonthlyExpenseTotal(expenseMonth);
                if (callback != null) {
                    callback.onSuccess(total);
                }
            } catch (Exception e) {
                if (callback != null) {
                    callback.onError(e);
                }
            }
        });
    }

    public void getMonthlyExpenseCount(String expenseMonth, com.rob.houserental.repository.DatabaseCallback<Integer> callback) {
        databaseExecutor.execute(() -> {
            try {
                int count = expenseDao.getMonthlyExpenseCount(expenseMonth);
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

    public void getPropertyExpenseTotal(long propertyId, com.rob.houserental.repository.DatabaseCallback<Double> callback) {
        databaseExecutor.execute(() -> {
            try {
                double total = expenseDao.getPropertyExpenseTotal(propertyId);
                if (callback != null) {
                    callback.onSuccess(total);
                }
            } catch (Exception e) {
                if (callback != null) {
                    callback.onError(e);
                }
            }
        });
    }

    public void getPropertyExpenseTotalByMonth(long propertyId, String expenseMonth, com.rob.houserental.repository.DatabaseCallback<Double> callback) {
        databaseExecutor.execute(() -> {
            try {
                double total = expenseDao.getPropertyExpenseTotalByMonth(propertyId, expenseMonth);
                if (callback != null) {
                    callback.onSuccess(total);
                }
            } catch (Exception e) {
                if (callback != null) {
                    callback.onError(e);
                }
            }
        });
    }

    public void getAllTimeExpenseTotal(com.rob.houserental.repository.DatabaseCallback<Double> callback) {
        databaseExecutor.execute(() -> {
            try {
                double total = expenseDao.getAllTimeExpenseTotal();
                if (callback != null) {
                    callback.onSuccess(total);
                }
            } catch (Exception e) {
                if (callback != null) {
                    callback.onError(e);
                }
            }
        });
    }

    public void unarchiveExpense(long id, com.rob.houserental.repository.DatabaseCallback<Void> callback) {
        databaseExecutor.execute(() -> {
            try {
                Expense expense = expenseDao.getExpenseById(id);
                if (expense != null) {
                    expense.setArchived(false);
                    expense.setUpdatedAt(System.currentTimeMillis());
                    expenseDao.update(expense);
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

    public void getArchivedExpenseDisplayItems(com.rob.houserental.repository.DatabaseCallback<List<ExpenseDisplayItem>> callback) {
        databaseExecutor.execute(() -> {
            try {
                List<ExpenseDisplayItem> list = expenseDao.getArchivedExpenseDisplayItems();
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

    public void getArchivedExpenseTotal(com.rob.houserental.repository.DatabaseCallback<Double> callback) {
        databaseExecutor.execute(() -> {
            try {
                double total = expenseDao.getArchivedExpenseTotal();
                if (callback != null) {
                    callback.onSuccess(total);
                }
            } catch (Exception e) {
                if (callback != null) {
                    callback.onError(e);
                }
            }
        });
    }
}

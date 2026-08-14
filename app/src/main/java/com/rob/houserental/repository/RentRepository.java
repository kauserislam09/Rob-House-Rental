package com.rob.houserental.repository;

import android.content.Context;

import com.rob.houserental.data.AppDatabase;
import com.rob.houserental.data.PaymentDao;
import com.rob.houserental.data.RentDao;
import com.rob.houserental.data.TenancyDao;
import com.rob.houserental.model.MonthlyRentSummary;
import com.rob.houserental.model.Payment;
import com.rob.houserental.model.RentRecord;
import com.rob.houserental.model.RentRecordDisplayItem;
import com.rob.houserental.model.Tenancy;
import com.rob.houserental.utils.AppExecutors;
import com.rob.houserental.utils.RentDateUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;

public class RentRepository {

    private final AppDatabase database;
    private final RentDao rentDao;
    private final PaymentDao paymentDao;
    private final TenancyDao tenancyDao;

    private final ExecutorService databaseExecutor =
            AppExecutors.getInstance().getDatabaseExecutor();

    public static class GenerationResult {
        public int createdCount;
        public int alreadyExistingCount;
        public double totalGeneratedAmount;

        public GenerationResult(int createdCount, int alreadyExistingCount, double totalGeneratedAmount) {
            this.createdCount = createdCount;
            this.alreadyExistingCount = alreadyExistingCount;
            this.totalGeneratedAmount = totalGeneratedAmount;
        }
    }

    public interface DatabaseCallback<T> extends com.rob.houserental.repository.DatabaseCallback<T> {}

    private final Context context;

    public RentRepository(Context context) {
        this.context = context.getApplicationContext();
        database = AppDatabase.getInstance(this.context);
        rentDao = database.rentDao();
        paymentDao = database.paymentDao();
        tenancyDao = database.tenancyDao();
    }

    public void generateMonthlyRent(String billingMonth, String fallbackDueDate, com.rob.houserental.repository.DatabaseCallback<GenerationResult> callback) {
        databaseExecutor.execute(() -> {
            try {
                final int[] created = {0};
                final int[] existing = {0};
                final double[] totalAmount = {0.0};

                database.runInTransaction(() -> {
                    List<Tenancy> activeTenancies = tenancyDao.getTenanciesByStatus("ACTIVE");
                    long currentTime = System.currentTimeMillis();

                    if (activeTenancies != null) {
                        for (Tenancy tenancy : activeTenancies) {
                            if (!"ACTIVE".equalsIgnoreCase(tenancy.getStatus())) {
                                continue;
                            }

                            // Skip if billing month is before the tenancy start month
                            if (RentDateUtils.isBillingMonthBeforeTenancyStart(billingMonth, tenancy.getStartDate())) {
                                continue;
                            }

                            RentRecord existingRecord = rentDao.getRentRecordByTenancyAndMonth(tenancy.getId(), billingMonth);
                            if (existingRecord != null) {
                                existing[0]++;
                            } else {
                                double amountDue = tenancy.getMonthlyRent() + tenancy.getServiceCharge();
                                String tenancyDueDate = RentDateUtils.computeDueDate(billingMonth, tenancy.getRentDueDay());
                                String initialStatus = RentDateUtils.calculateStatus(amountDue, 0.0, amountDue, tenancyDueDate, "UNPAID");

                                RentRecord record = new RentRecord(
                                        tenancy.getId(),
                                        billingMonth,
                                        tenancyDueDate,
                                        amountDue,
                                        0.0,
                                        amountDue,
                                        initialStatus,
                                        null,
                                        null,
                                        null,
                                        currentTime,
                                        currentTime
                                );
                                long recordId = rentDao.insert(record);
                                record.setId(recordId);
                                com.rob.houserental.notifications.AutomaticReminderUtils.syncRentDueReminder(context, record);
                                created[0]++;
                                totalAmount[0] += amountDue;
                            }
                        }
                    }
                });

                if (callback != null) {
                    callback.onSuccess(new GenerationResult(created[0], existing[0], totalAmount[0]));
                }
            } catch (Exception e) {
                if (callback != null) {
                    callback.onError(e);
                }
            }
        });
    }

    public void recordPayment(long rentRecordId, Payment payment, com.rob.houserental.repository.DatabaseCallback<Void> callback) {
        databaseExecutor.execute(() -> {
            try {
                database.runInTransaction(() -> {
                    RentRecord rentRecord = rentDao.getRentRecordById(rentRecordId);
                    if (rentRecord == null) {
                        throw new IllegalStateException("Rent record not found");
                    }

                    if (payment.getAmount() <= 0) {
                        throw new IllegalArgumentException("Payment amount must be greater than zero");
                    }

                    if (payment.getAmount() > rentRecord.getRemainingAmount() + 0.001) {
                        throw new IllegalArgumentException("Payment amount exceeds remaining balance");
                    }

                    payment.setRentRecordId(rentRecordId);
                    if (payment.getCreatedAt() <= 0) {
                        payment.setCreatedAt(System.currentTimeMillis());
                    }
                    paymentDao.insert(payment);

                    double totalPaid = paymentDao.getTotalPaidForRent(rentRecordId);
                    double remaining = Math.max(0.0, rentRecord.getAmountDue() - totalPaid);

                    String newStatus = RentDateUtils.calculateStatus(
                            rentRecord.getAmountDue(),
                            totalPaid,
                            remaining,
                            rentRecord.getDueDate(),
                            rentRecord.getStatus()
                    );

                    rentRecord.setAmountPaid(totalPaid);
                    rentRecord.setRemainingAmount(remaining);
                    rentRecord.setStatus(newStatus);
                    rentRecord.setLastPaymentDate(payment.getPaymentDate());
                    rentRecord.setPaymentMethod(payment.getPaymentMethod());
                    rentRecord.setUpdatedAt(System.currentTimeMillis());

                    rentDao.update(rentRecord);
                    com.rob.houserental.notifications.AutomaticReminderUtils.syncRentDueReminder(context, rentRecord);
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

    public void waiveRent(long rentRecordId, String waiverNote, com.rob.houserental.repository.DatabaseCallback<Void> callback) {
        databaseExecutor.execute(() -> {
            try {
                database.runInTransaction(() -> {
                    RentRecord rentRecord = rentDao.getRentRecordById(rentRecordId);
                    if (rentRecord != null) {
                        rentRecord.setStatus("WAIVED");
                        rentRecord.setRemainingAmount(0.0);
                        if (waiverNote != null && !waiverNote.trim().isEmpty()) {
                            String currentNotes = rentRecord.getNotes() != null ? rentRecord.getNotes() + "\n" : "";
                            rentRecord.setNotes(currentNotes + "Waived: " + waiverNote.trim());
                        }
                        rentRecord.setUpdatedAt(System.currentTimeMillis());
                        rentDao.update(rentRecord);
                        com.rob.houserental.notifications.AutomaticReminderUtils.syncRentDueReminder(context, rentRecord);
                    }
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

    public void updateRentRecord(RentRecord record, com.rob.houserental.repository.DatabaseCallback<Void> callback) {
        databaseExecutor.execute(() -> {
            try {
                database.runInTransaction(() -> {
                    double remaining = Math.max(0.0, record.getAmountDue() - record.getAmountPaid());
                    record.setRemainingAmount(remaining);

                    String calculatedStatus = RentDateUtils.calculateStatus(
                            record.getAmountDue(),
                            record.getAmountPaid(),
                            remaining,
                            record.getDueDate(),
                            record.getStatus()
                    );
                    record.setStatus(calculatedStatus);

                    record.setUpdatedAt(System.currentTimeMillis());
                    rentDao.update(record);
                    com.rob.houserental.notifications.AutomaticReminderUtils.syncRentDueReminder(context, record);
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

    public void deleteRentRecord(RentRecord record, com.rob.houserental.repository.DatabaseCallback<Void> callback) {
        databaseExecutor.execute(() -> {
            try {
                rentDao.delete(record);
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

    public void getRentRecordById(long id, com.rob.houserental.repository.DatabaseCallback<RentRecord> callback) {
        databaseExecutor.execute(() -> {
            try {
                RentRecord record = rentDao.getRentRecordById(id);
                if (record != null) {
                    // Refresh status if overdue
                    String freshStatus = RentDateUtils.calculateStatus(
                            record.getAmountDue(),
                            record.getAmountPaid(),
                            record.getRemainingAmount(),
                            record.getDueDate(),
                            record.getStatus()
                    );
                    if (!freshStatus.equalsIgnoreCase(record.getStatus())) {
                        record.setStatus(freshStatus);
                        rentDao.update(record);
                    }
                }
                if (callback != null) {
                    callback.onSuccess(record);
                }
            } catch (Exception e) {
                if (callback != null) {
                    callback.onError(e);
                }
            }
        });
    }

    public void getRentDisplayItemById(long id, com.rob.houserental.repository.DatabaseCallback<RentRecordDisplayItem> callback) {
        databaseExecutor.execute(() -> {
            try {
                RentRecordDisplayItem item = rentDao.getRentDisplayItemById(id);
                if (item != null) {
                    item.status = RentDateUtils.calculateStatus(
                            item.amountDue,
                            item.amountPaid,
                            item.remainingAmount,
                            item.dueDate,
                            item.status
                    );
                }
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

    public void getAllRentDisplayItems(com.rob.houserental.repository.DatabaseCallback<List<RentRecordDisplayItem>> callback) {
        databaseExecutor.execute(() -> {
            try {
                List<RentRecordDisplayItem> list = rentDao.getAllRentDisplayItems();
                if (list != null) {
                    for (RentRecordDisplayItem item : list) {
                        item.status = RentDateUtils.calculateStatus(
                                item.amountDue,
                                item.amountPaid,
                                item.remainingAmount,
                                item.dueDate,
                                item.status
                        );
                    }
                }
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

    public void getRentDisplayItemsByMonth(String billingMonth, com.rob.houserental.repository.DatabaseCallback<List<RentRecordDisplayItem>> callback) {
        databaseExecutor.execute(() -> {
            try {
                List<RentRecordDisplayItem> list = rentDao.getRentDisplayItemsByMonth(billingMonth);
                if (list != null) {
                    for (RentRecordDisplayItem item : list) {
                        item.status = RentDateUtils.calculateStatus(
                                item.amountDue,
                                item.amountPaid,
                                item.remainingAmount,
                                item.dueDate,
                                item.status
                        );
                    }
                }
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

    public void getRentDisplayItemsByTenancy(long tenancyId, com.rob.houserental.repository.DatabaseCallback<List<RentRecordDisplayItem>> callback) {
        databaseExecutor.execute(() -> {
            try {
                List<RentRecordDisplayItem> list = rentDao.getRentDisplayItemsByTenancy(tenancyId);
                if (list != null) {
                    for (RentRecordDisplayItem item : list) {
                        item.status = RentDateUtils.calculateStatus(
                                item.amountDue,
                                item.amountPaid,
                                item.remainingAmount,
                                item.dueDate,
                                item.status
                        );
                    }
                }
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

    public void getMonthlySummary(String billingMonth, com.rob.houserental.repository.DatabaseCallback<MonthlyRentSummary> callback) {
        databaseExecutor.execute(() -> {
            try {
                List<RentRecordDisplayItem> list = rentDao.getRentDisplayItemsByMonth(billingMonth);
                double expected = 0;
                double collected = 0;
                double outstanding = 0;
                double overdue = 0;

                if (list != null) {
                    for (RentRecordDisplayItem item : list) {
                        expected += item.amountDue;
                        collected += item.amountPaid;
                        outstanding += item.remainingAmount;

                        String calculatedStatus = RentDateUtils.calculateStatus(
                                item.amountDue,
                                item.amountPaid,
                                item.remainingAmount,
                                item.dueDate,
                                item.status
                        );

                        if ("OVERDUE".equalsIgnoreCase(calculatedStatus)) {
                            overdue += item.remainingAmount;
                        }
                    }
                }

                MonthlyRentSummary summary = new MonthlyRentSummary(expected, collected, outstanding, overdue);
                if (callback != null) {
                    callback.onSuccess(summary);
                }
            } catch (Exception e) {
                if (callback != null) {
                    callback.onError(e);
                }
            }
        });
    }

    public void getGlobalSummary(com.rob.houserental.repository.DatabaseCallback<MonthlyRentSummary> callback) {
        databaseExecutor.execute(() -> {
            try {
                List<RentRecordDisplayItem> list = rentDao.getAllRentDisplayItems();
                double expected = 0;
                double collected = 0;
                double outstanding = 0;
                double overdue = 0;

                if (list != null) {
                    for (RentRecordDisplayItem item : list) {
                        expected += item.amountDue;
                        collected += item.amountPaid;
                        outstanding += item.remainingAmount;

                        String calculatedStatus = RentDateUtils.calculateStatus(
                                item.amountDue,
                                item.amountPaid,
                                item.remainingAmount,
                                item.dueDate,
                                item.status
                        );

                        if ("OVERDUE".equalsIgnoreCase(calculatedStatus)) {
                            overdue += item.remainingAmount;
                        }
                    }
                }

                MonthlyRentSummary summary = new MonthlyRentSummary(expected, collected, outstanding, overdue);
                if (callback != null) {
                    callback.onSuccess(summary);
                }
            } catch (Exception e) {
                if (callback != null) {
                    callback.onError(e);
                }
            }
        });
    }

    public void getPaymentsByRentRecord(long rentRecordId, DatabaseCallback<List<Payment>> callback) {
        databaseExecutor.execute(() -> {
            try {
                List<Payment> list = paymentDao.getPaymentsByRent(rentRecordId);
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

    public void getTotalCumulativeOutstandingRent(String maxBillingMonth, DatabaseCallback<Double> callback) {
        databaseExecutor.execute(() -> {
            try {
                double total = rentDao.getTotalCumulativeOutstandingRent(maxBillingMonth);
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

    public void getTenancyCumulativeOutstandingRent(long tenancyId, String maxBillingMonth, DatabaseCallback<Double> callback) {
        databaseExecutor.execute(() -> {
            try {
                double total = rentDao.getTenancyCumulativeOutstandingRent(tenancyId, maxBillingMonth);
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

    public void getCumulativeOutstandingRentDisplayItems(String maxBillingMonth, DatabaseCallback<List<RentRecordDisplayItem>> callback) {
        databaseExecutor.execute(() -> {
            try {
                List<RentRecordDisplayItem> list = rentDao.getCumulativeOutstandingRentDisplayItems(maxBillingMonth);
                List<RentRecordDisplayItem> validList = new ArrayList<>();
                if (list != null) {
                    for (RentRecordDisplayItem item : list) {
                        Tenancy tenancy = tenancyDao.getTenancyById(item.tenancyId);
                        if (tenancy != null && RentDateUtils.isBillingMonthBeforeTenancyStart(item.billingMonth, tenancy.getStartDate())) {
                            continue;
                        }
                        item.status = RentDateUtils.calculateStatus(
                                item.amountDue,
                                item.amountPaid,
                                item.remainingAmount,
                                item.dueDate,
                                item.status
                        );
                        validList.add(item);
                    }
                }
                if (callback != null) {
                    callback.onSuccess(validList);
                }
            } catch (Exception e) {
                if (callback != null) {
                    callback.onError(e);
                }
            }
        });
    }
}

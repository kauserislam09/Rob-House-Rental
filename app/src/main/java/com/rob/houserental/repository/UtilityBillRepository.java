package com.rob.houserental.repository;

import android.content.Context;

import com.rob.houserental.data.AppDatabase;
import com.rob.houserental.data.BillPaymentDao;
import com.rob.houserental.data.UtilityBillDao;
import com.rob.houserental.model.BillPayment;
import com.rob.houserental.model.MonthlyBillSummary;
import com.rob.houserental.model.UtilityBill;
import com.rob.houserental.model.UtilityBillDisplayItem;
import com.rob.houserental.utils.AppExecutors;
import com.rob.houserental.utils.RentDateUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;

public class UtilityBillRepository {

    private final AppDatabase database;
    private final UtilityBillDao utilityBillDao;
    private final BillPaymentDao billPaymentDao;

    private final ExecutorService databaseExecutor =
            AppExecutors.getInstance().getDatabaseExecutor();

    public interface DatabaseCallback<T> extends com.rob.houserental.repository.DatabaseCallback<T> {}



    private final Context context;

    public UtilityBillRepository(Context context) {
        this.context = context.getApplicationContext();
        database = AppDatabase.getInstance(this.context);
        utilityBillDao = database.utilityBillDao();
        billPaymentDao = database.billPaymentDao();
    }

    public void createBill(UtilityBill bill, com.rob.houserental.repository.DatabaseCallback<Long> callback) {
        databaseExecutor.execute(() -> {
            try {
                UtilityBill existing = utilityBillDao.getDuplicateBill(
                        bill.getPropertyId(),
                        bill.getUnitId(),
                        bill.getBillType(),
                        bill.getBillingMonth()
                );
                if (existing != null) {
                    throw new IllegalStateException("A " + bill.getBillType() + " bill for " + bill.getBillingMonth() + " already exists for this property/unit.");
                }

                long currentTime = System.currentTimeMillis();
                if (bill.getCreatedAt() <= 0) {
                    bill.setCreatedAt(currentTime);
                }
                bill.setUpdatedAt(currentTime);

                bill.setAmountPaid(0.0);
                bill.setRemainingAmount(bill.getAmountDue());

                String initialStatus = RentDateUtils.calculateStatus(
                        bill.getAmountDue(),
                        0.0,
                        bill.getAmountDue(),
                        bill.getDueDate(),
                        "UNPAID"
                );
                bill.setStatus(initialStatus);

                long id = utilityBillDao.insert(bill);
                bill.setId(id);
                com.rob.houserental.notifications.AutomaticReminderUtils.syncBillDueReminder(context, bill);

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

    public void getDuplicateBill(long propertyId, long unitId, String billType, String billingMonth, com.rob.houserental.repository.DatabaseCallback<UtilityBill> callback) {
        databaseExecutor.execute(() -> {
            try {
                UtilityBill bill = utilityBillDao.getDuplicateBill(propertyId, unitId, billType, billingMonth);
                if (callback != null) {
                    callback.onSuccess(bill);
                }
            } catch (Exception e) {
                if (callback != null) {
                    callback.onError(e);
                }
            }
        });
    }

    public void updateBill(UtilityBill bill, com.rob.houserental.repository.DatabaseCallback<Void> callback) {
        databaseExecutor.execute(() -> {
            try {
                database.runInTransaction(() -> {
                    double totalPaid = billPaymentDao.getTotalPaidForBill(bill.getId());
                    double remaining = Math.max(0.0, bill.getAmountDue() - totalPaid);

                    bill.setAmountPaid(totalPaid);
                    bill.setRemainingAmount(remaining);

                    String calculatedStatus = RentDateUtils.calculateStatus(
                            bill.getAmountDue(),
                            totalPaid,
                            remaining,
                            bill.getDueDate(),
                            bill.getStatus()
                    );
                    bill.setStatus(calculatedStatus);
                    bill.setUpdatedAt(System.currentTimeMillis());

                    utilityBillDao.update(bill);
                    com.rob.houserental.notifications.AutomaticReminderUtils.syncBillDueReminder(context, bill);
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

    public void recordBillPayment(long billId, BillPayment payment, com.rob.houserental.repository.DatabaseCallback<Void> callback) {
        databaseExecutor.execute(() -> {
            try {
                database.runInTransaction(() -> {
                    UtilityBill bill = utilityBillDao.getBillById(billId);
                    if (bill == null) {
                        throw new IllegalStateException("Utility bill not found");
                    }

                    if (payment.getAmount() <= 0) {
                        throw new IllegalArgumentException("Payment amount must be greater than zero");
                    }

                    if (payment.getAmount() > bill.getRemainingAmount() + 0.001) {
                        throw new IllegalArgumentException("Payment amount cannot exceed remaining balance");
                    }

                    payment.setBillId(billId);
                    if (payment.getCreatedAt() <= 0) {
                        payment.setCreatedAt(System.currentTimeMillis());
                    }
                    billPaymentDao.insert(payment);

                    double totalPaid = billPaymentDao.getTotalPaidForBill(billId);
                    double remaining = Math.max(0.0, bill.getAmountDue() - totalPaid);

                    String newStatus = RentDateUtils.calculateStatus(
                            bill.getAmountDue(),
                            totalPaid,
                            remaining,
                            bill.getDueDate(),
                            bill.getStatus()
                    );

                    bill.setAmountPaid(totalPaid);
                    bill.setRemainingAmount(remaining);
                    bill.setStatus(newStatus);
                    bill.setLastPaymentDate(payment.getPaymentDate());
                    bill.setPaymentMethod(payment.getPaymentMethod());
                    bill.setUpdatedAt(System.currentTimeMillis());

                    utilityBillDao.update(bill);
                    com.rob.houserental.notifications.AutomaticReminderUtils.syncBillDueReminder(context, bill);
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

    public void waiveBill(long billId, String waiverNote, com.rob.houserental.repository.DatabaseCallback<Void> callback) {
        databaseExecutor.execute(() -> {
            try {
                database.runInTransaction(() -> {
                    UtilityBill bill = utilityBillDao.getBillById(billId);
                    if (bill != null) {
                        bill.setStatus("WAIVED");
                        bill.setRemainingAmount(0.0);
                        if (waiverNote != null && !waiverNote.trim().isEmpty()) {
                            String currentNotes = bill.getNotes() != null ? bill.getNotes() + "\n" : "";
                            bill.setNotes(currentNotes + "Waived: " + waiverNote.trim());
                        }
                        bill.setUpdatedAt(System.currentTimeMillis());
                        utilityBillDao.update(bill);
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

    public void deleteBill(UtilityBill bill, com.rob.houserental.repository.DatabaseCallback<Void> callback) {
        databaseExecutor.execute(() -> {
            try {
                utilityBillDao.delete(bill);
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

    public void getBillById(long id, com.rob.houserental.repository.DatabaseCallback<UtilityBill> callback) {
        databaseExecutor.execute(() -> {
            try {
                UtilityBill bill = utilityBillDao.getBillById(id);
                if (bill != null) {
                    String freshStatus = RentDateUtils.calculateStatus(
                            bill.getAmountDue(),
                            bill.getAmountPaid(),
                            bill.getRemainingAmount(),
                            bill.getDueDate(),
                            bill.getStatus()
                    );
                    if (!freshStatus.equalsIgnoreCase(bill.getStatus())) {
                        bill.setStatus(freshStatus);
                        utilityBillDao.update(bill);
                    }
                }
                if (callback != null) {
                    callback.onSuccess(bill);
                }
            } catch (Exception e) {
                if (callback != null) {
                    callback.onError(e);
                }
            }
        });
    }

    public void getBillDisplayItemById(long id, com.rob.houserental.repository.DatabaseCallback<UtilityBillDisplayItem> callback) {
        databaseExecutor.execute(() -> {
            try {
                UtilityBillDisplayItem item = utilityBillDao.getBillDisplayItemById(id);
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

    public void getAllBillDisplayItems(com.rob.houserental.repository.DatabaseCallback<List<UtilityBillDisplayItem>> callback) {
        databaseExecutor.execute(() -> {
            try {
                List<UtilityBillDisplayItem> list = utilityBillDao.getAllBillDisplayItems();
                if (list != null) {
                    for (UtilityBillDisplayItem item : list) {
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

    public void getBillDisplayItemsByMonth(String billingMonth, com.rob.houserental.repository.DatabaseCallback<List<UtilityBillDisplayItem>> callback) {
        databaseExecutor.execute(() -> {
            try {
                List<UtilityBillDisplayItem> list = utilityBillDao.getBillDisplayItemsByMonth(billingMonth);
                if (list != null) {
                    for (UtilityBillDisplayItem item : list) {
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

    public void getMonthlySummary(String billingMonth, com.rob.houserental.repository.DatabaseCallback<MonthlyBillSummary> callback) {
        databaseExecutor.execute(() -> {
            try {
                List<UtilityBillDisplayItem> list = utilityBillDao.getBillDisplayItemsByMonth(billingMonth);
                double expected = 0;
                double collected = 0;
                double outstanding = 0;
                double overdue = 0;

                if (list != null) {
                    for (UtilityBillDisplayItem item : list) {
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

                MonthlyBillSummary summary = new MonthlyBillSummary(expected, collected, outstanding, overdue);
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

    public void getGlobalSummary(com.rob.houserental.repository.DatabaseCallback<MonthlyBillSummary> callback) {
        databaseExecutor.execute(() -> {
            try {
                List<UtilityBillDisplayItem> list = utilityBillDao.getAllBillDisplayItems();
                double expected = 0;
                double collected = 0;
                double outstanding = 0;
                double overdue = 0;

                if (list != null) {
                    for (UtilityBillDisplayItem item : list) {
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

                MonthlyBillSummary summary = new MonthlyBillSummary(expected, collected, outstanding, overdue);
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

    public void getPaymentsByBill(long billId, com.rob.houserental.repository.DatabaseCallback<List<BillPayment>> callback) {
        databaseExecutor.execute(() -> {
            try {
                List<BillPayment> list = billPaymentDao.getPaymentsByBill(billId);
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
}

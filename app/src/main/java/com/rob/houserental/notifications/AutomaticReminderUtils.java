package com.rob.houserental.notifications;

import android.content.Context;
import android.util.Log;

import com.rob.houserental.R;
import com.rob.houserental.data.AppDatabase;
import com.rob.houserental.model.AppDocument;
import com.rob.houserental.model.MaintenanceRecord;
import com.rob.houserental.model.Reminder;
import com.rob.houserental.model.RentRecord;
import com.rob.houserental.model.Tenancy;
import com.rob.houserental.model.UtilityBill;

import com.rob.houserental.utils.AppExecutors;

public class AutomaticReminderUtils {

    private static final String TAG = "AutomaticReminderUtils";

    public static void syncRentDueReminder(Context context, RentRecord rent) {
        if (rent == null || rent.getId() <= 0) return;

        AppExecutors.runOnDatabase(() -> {
            try {
                Context appContext = context.getApplicationContext();
                AppDatabase db = AppDatabase.getInstance(appContext);

                // Check if paid or clear
                if ("PAID".equalsIgnoreCase(rent.getStatus()) || rent.getRemainingAmount() <= 0) {
                    Reminder existing = db.reminderDao().getByRelatedEntity("RENT", rent.getId(), "RENT_DUE");
                    if (existing != null) {
                        existing.setCompleted(true);
                        existing.setEnabled(false);
                        existing.setUpdatedAt(System.currentTimeMillis());
                        db.reminderDao().update(existing);
                        ReminderSchedulerUtils.cancelAlarm(appContext, existing);
                    }
                    return;
                }

                String title = appContext.getString(R.string.reminder_type_rent) + " (" + (rent.getBillingMonth() != null ? rent.getBillingMonth() : "") + ")";
                String desc = appContext.getString(R.string.reports_outstanding_rent) + ": ৳" + rent.getRemainingAmount();
                String dueDate = rent.getDueDate() != null && !rent.getDueDate().isEmpty() ? rent.getDueDate() : rent.getBillingMonth() + "-10";

                Reminder reminder = db.reminderDao().getByRelatedEntity("RENT", rent.getId(), "RENT_DUE");
                if (reminder == null) {
                    reminder = new Reminder();
                    reminder.setReminderType("RENT_DUE");
                    reminder.setRelatedEntityType("RENT");
                    reminder.setRelatedEntityId(rent.getId());
                    reminder.setRepeatType("ONCE");
                }

                reminder.setTitle(title);
                reminder.setDescription(desc);
                reminder.setReminderDate(dueDate);
                reminder.setReminderTime("09:00");
                reminder.setEnabled(true);
                reminder.setCompleted(false);
                reminder.setUpdatedAt(System.currentTimeMillis());

                if (reminder.getId() > 0) {
                    db.reminderDao().update(reminder);
                } else {
                    long id = db.reminderDao().insert(reminder);
                    reminder.setId(id);
                }

                ReminderSchedulerUtils.scheduleAlarm(appContext, reminder);
            } catch (Exception e) {
                Log.e(TAG, "Error syncing rent due reminder", e);
            }
        });
    }

    public static void syncBillDueReminder(Context context, UtilityBill bill) {
        if (bill == null || bill.getId() <= 0) return;

        AppExecutors.runOnDatabase(() -> {
            try {
                Context appContext = context.getApplicationContext();
                AppDatabase db = AppDatabase.getInstance(appContext);

                if ("PAID".equalsIgnoreCase(bill.getStatus()) || bill.getRemainingAmount() <= 0) {
                    Reminder existing = db.reminderDao().getByRelatedEntity("BILL", bill.getId(), "BILL_DUE");
                    if (existing != null) {
                        existing.setCompleted(true);
                        existing.setEnabled(false);
                        existing.setUpdatedAt(System.currentTimeMillis());
                        db.reminderDao().update(existing);
                        ReminderSchedulerUtils.cancelAlarm(appContext, existing);
                    }
                    return;
                }

                String title = appContext.getString(R.string.reminder_type_bill) + ": " + (bill.getBillType() != null ? bill.getBillType() : "");
                String desc = appContext.getString(R.string.reports_outstanding_rent) + ": ৳" + bill.getRemainingAmount();
                String dueDate = bill.getDueDate() != null && !bill.getDueDate().isEmpty() ? bill.getDueDate() : bill.getBillingMonth() + "-15";

                Reminder reminder = db.reminderDao().getByRelatedEntity("BILL", bill.getId(), "BILL_DUE");
                if (reminder == null) {
                    reminder = new Reminder();
                    reminder.setReminderType("BILL_DUE");
                    reminder.setRelatedEntityType("BILL");
                    reminder.setRelatedEntityId(bill.getId());
                    reminder.setRepeatType("ONCE");
                }

                reminder.setTitle(title);
                reminder.setDescription(desc);
                reminder.setReminderDate(dueDate);
                reminder.setReminderTime("09:00");
                reminder.setEnabled(true);
                reminder.setCompleted(false);
                reminder.setUpdatedAt(System.currentTimeMillis());

                if (reminder.getId() > 0) {
                    db.reminderDao().update(reminder);
                } else {
                    long id = db.reminderDao().insert(reminder);
                    reminder.setId(id);
                }

                ReminderSchedulerUtils.scheduleAlarm(appContext, reminder);
            } catch (Exception e) {
                Log.e(TAG, "Error syncing bill due reminder", e);
            }
        });
    }

    public static void syncTenancyExpiryReminder(Context context, Tenancy tenancy) {
        if (tenancy == null || tenancy.getId() <= 0 || tenancy.getEndDate() == null || tenancy.getEndDate().isEmpty()) return;

        AppExecutors.runOnDatabase(() -> {
            try {
                Context appContext = context.getApplicationContext();
                AppDatabase db = AppDatabase.getInstance(appContext);

                if ("EXPIRED".equalsIgnoreCase(tenancy.getStatus()) || "TERMINATED".equalsIgnoreCase(tenancy.getStatus())) {
                    Reminder existing = db.reminderDao().getByRelatedEntity("TENANCY", tenancy.getId(), "TENANCY_EXPIRY");
                    if (existing != null) {
                        existing.setCompleted(true);
                        existing.setEnabled(false);
                        existing.setUpdatedAt(System.currentTimeMillis());
                        db.reminderDao().update(existing);
                        ReminderSchedulerUtils.cancelAlarm(appContext, existing);
                    }
                    return;
                }

                String title = appContext.getString(R.string.reminder_type_tenancy) + " (" + tenancy.getEndDate() + ")";
                String desc = appContext.getString(R.string.tenancy_details_title);

                Reminder reminder = db.reminderDao().getByRelatedEntity("TENANCY", tenancy.getId(), "TENANCY_EXPIRY");
                if (reminder == null) {
                    reminder = new Reminder();
                    reminder.setReminderType("TENANCY_EXPIRY");
                    reminder.setRelatedEntityType("TENANCY");
                    reminder.setRelatedEntityId(tenancy.getId());
                    reminder.setRepeatType("ONCE");
                }

                reminder.setTitle(title);
                reminder.setDescription(desc);
                reminder.setReminderDate(tenancy.getEndDate());
                reminder.setReminderTime("09:00");
                reminder.setEnabled(true);
                reminder.setCompleted(false);
                reminder.setUpdatedAt(System.currentTimeMillis());

                if (reminder.getId() > 0) {
                    db.reminderDao().update(reminder);
                } else {
                    long id = db.reminderDao().insert(reminder);
                    reminder.setId(id);
                }

                ReminderSchedulerUtils.scheduleAlarm(appContext, reminder);
            } catch (Exception e) {
                Log.e(TAG, "Error syncing tenancy expiry reminder", e);
            }
        });
    }

    public static void syncMaintenanceReminder(Context context, MaintenanceRecord record) {
        if (record == null || record.getId() <= 0 || record.getScheduledDate() == null || record.getScheduledDate().isEmpty()) return;

        AppExecutors.runOnDatabase(() -> {
            try {
                Context appContext = context.getApplicationContext();
                AppDatabase db = AppDatabase.getInstance(appContext);

                if ("COMPLETED".equalsIgnoreCase(record.getStatus()) || "CANCELLED".equalsIgnoreCase(record.getStatus())) {
                    Reminder existing = db.reminderDao().getByRelatedEntity("MAINTENANCE", record.getId(), "MAINTENANCE");
                    if (existing != null) {
                        existing.setCompleted(true);
                        existing.setEnabled(false);
                        existing.setUpdatedAt(System.currentTimeMillis());
                        db.reminderDao().update(existing);
                        ReminderSchedulerUtils.cancelAlarm(appContext, existing);
                    }
                    return;
                }

                String title = appContext.getString(R.string.reminder_type_maintenance) + ": " + record.getTitle();
                String desc = appContext.getString(R.string.scheduled_label) + ": " + record.getScheduledDate();

                Reminder reminder = db.reminderDao().getByRelatedEntity("MAINTENANCE", record.getId(), "MAINTENANCE");
                if (reminder == null) {
                    reminder = new Reminder();
                    reminder.setReminderType("MAINTENANCE");
                    reminder.setRelatedEntityType("MAINTENANCE");
                    reminder.setRelatedEntityId(record.getId());
                    reminder.setRepeatType("ONCE");
                }

                reminder.setTitle(title);
                reminder.setDescription(desc);
                reminder.setReminderDate(record.getScheduledDate());
                reminder.setReminderTime("09:00");
                reminder.setEnabled(true);
                reminder.setCompleted(false);
                reminder.setUpdatedAt(System.currentTimeMillis());

                if (reminder.getId() > 0) {
                    db.reminderDao().update(reminder);
                } else {
                    long id = db.reminderDao().insert(reminder);
                    reminder.setId(id);
                }

                ReminderSchedulerUtils.scheduleAlarm(appContext, reminder);
            } catch (Exception e) {
                Log.e(TAG, "Error syncing maintenance reminder", e);
            }
        });
    }
}

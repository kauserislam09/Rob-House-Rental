package com.rob.houserental.notifications;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.rob.houserental.data.AppDatabase;
import com.rob.houserental.model.Reminder;

import java.util.concurrent.Executors;

public class ReminderBroadcastReceiver extends BroadcastReceiver {

    private static final String TAG = "ReminderBroadcastReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        long reminderId = intent.getLongExtra("reminder_id", -1);
        if (reminderId <= 0) return;

        final PendingResult pendingResult = goAsync();

        Executors.newSingleThreadExecutor().execute(() -> {
            try {
                Context appContext = context.getApplicationContext();
                AppDatabase db = AppDatabase.getInstance(appContext);
                Reminder reminder = db.reminderDao().getById(reminderId);

                if (reminder != null && reminder.isEnabled() && !reminder.isCompleted()) {
                    // Show notification
                    NotificationHelper.showReminderNotification(appContext, reminder);

                    // Update lastTriggeredAt
                    reminder.setLastTriggeredAt(System.currentTimeMillis());

                    // Handle repeat logic without creating duplicate rows
                    if ("ONCE".equalsIgnoreCase(reminder.getRepeatType())) {
                        reminder.setCompleted(true);
                    } else {
                        String nextDate = ReminderSchedulerUtils.calculateNextReminderDate(
                                reminder.getReminderDate(),
                                reminder.getRepeatType(),
                                reminder.getRepeatInterval()
                        );
                        reminder.setReminderDate(nextDate);
                    }

                    reminder.setUpdatedAt(System.currentTimeMillis());
                    db.reminderDao().update(reminder);

                    // If recurring and still enabled, schedule next alarm
                    if (reminder.isEnabled() && !reminder.isCompleted()) {
                        ReminderSchedulerUtils.scheduleAlarm(appContext, reminder);
                    }
                }
            } catch (Exception e) {
                Log.e(TAG, "Error processing received reminder ID: " + reminderId, e);
            } finally {
                pendingResult.finish();
            }
        });
    }
}

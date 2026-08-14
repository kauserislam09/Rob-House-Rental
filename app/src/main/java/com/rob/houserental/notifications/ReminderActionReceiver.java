package com.rob.houserental.notifications;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import androidx.core.app.NotificationManagerCompat;

import com.rob.houserental.data.AppDatabase;
import com.rob.houserental.model.Reminder;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;
import java.util.concurrent.Executors;

public class ReminderActionReceiver extends BroadcastReceiver {

    private static final String TAG = "ReminderActionReceiver";
    public static final String ACTION_COMPLETE = "com.rob.houserental.ACTION_COMPLETE_REMINDER";
    public static final String ACTION_SNOOZE = "com.rob.houserental.ACTION_SNOOZE_REMINDER";

    private static final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
    private static final SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent == null || intent.getAction() == null) return;
        long reminderId = intent.getLongExtra("reminder_id", -1);
        if (reminderId <= 0) return;

        final PendingResult pendingResult = goAsync();

        Executors.newSingleThreadExecutor().execute(() -> {
            try {
                Context appContext = context.getApplicationContext();
                AppDatabase db = AppDatabase.getInstance(appContext);
                Reminder reminder = db.reminderDao().getById(reminderId);

                if (reminder != null) {
                    String action = intent.getAction();
                    if (ACTION_COMPLETE.equals(action)) {
                        reminder.setCompleted(true);
                        reminder.setEnabled(false);
                        reminder.setUpdatedAt(System.currentTimeMillis());
                        db.reminderDao().update(reminder);
                        ReminderSchedulerUtils.cancelAlarm(appContext, reminder);
                    } else if (ACTION_SNOOZE.equals(action)) {
                        Calendar cal = Calendar.getInstance();
                        cal.add(Calendar.MINUTE, 30); // Snooze 30 minutes
                        reminder.setReminderDate(dateFormat.format(cal.getTime()));
                        reminder.setReminderTime(timeFormat.format(cal.getTime()));
                        reminder.setCompleted(false);
                        reminder.setEnabled(true);
                        reminder.setUpdatedAt(System.currentTimeMillis());
                        db.reminderDao().update(reminder);
                        ReminderSchedulerUtils.scheduleAlarm(appContext, reminder);
                    }

                    // Dismiss notification
                    NotificationManagerCompat.from(appContext).cancel((int) reminderId);
                }
            } catch (Exception e) {
                Log.e(TAG, "Error handling notification action", e);
            } finally {
                pendingResult.finish();
            }
        });
    }
}

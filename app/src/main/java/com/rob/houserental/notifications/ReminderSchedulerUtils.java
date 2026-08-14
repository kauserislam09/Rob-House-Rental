package com.rob.houserental.notifications;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;

import com.rob.houserental.data.AppDatabase;
import com.rob.houserental.model.Reminder;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ReminderSchedulerUtils {

    private static final String TAG = "ReminderSchedulerUtils";
    private static final SimpleDateFormat dateTimeFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());
    private static final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());

    public static void scheduleAlarm(Context context, Reminder reminder) {
        if (reminder == null || !reminder.isEnabled() || reminder.isCompleted()) return;

        try {
            AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
            if (alarmManager == null) return;

            long triggerAtMillis = calculateTriggerTimeMillis(reminder.getReminderDate(), reminder.getReminderTime());
            if (triggerAtMillis <= System.currentTimeMillis()) {
                // If past time and not recurring, do not schedule
                if ("ONCE".equalsIgnoreCase(reminder.getRepeatType())) {
                    return;
                }
            }

            Intent intent = new Intent(context, ReminderBroadcastReceiver.class);
            intent.putExtra("reminder_id", reminder.getId());

            int flags = PendingIntent.FLAG_UPDATE_CURRENT;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                flags |= PendingIntent.FLAG_IMMUTABLE;
            }

            PendingIntent pendingIntent = PendingIntent.getBroadcast(
                    context,
                    (int) reminder.getId(),
                    intent,
                    flags
            );

            // Capability-aware Exact Alarm Scheduling
            boolean canExact = true;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                canExact = alarmManager.canScheduleExactAlarms();
            }

            if (canExact) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAtMillis, pendingIntent);
                } else {
                    alarmManager.setExact(AlarmManager.RTC_WAKEUP, triggerAtMillis, pendingIntent);
                }
            } else {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAtMillis, pendingIntent);
                } else {
                    alarmManager.set(AlarmManager.RTC_WAKEUP, triggerAtMillis, pendingIntent);
                }
            }

        } catch (Exception e) {
            Log.e(TAG, "Error scheduling alarm for reminder ID: " + reminder.getId(), e);
        }
    }

    public static void cancelAlarm(Context context, Reminder reminder) {
        if (reminder == null) return;
        try {
            AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
            if (alarmManager == null) return;

            Intent intent = new Intent(context, ReminderBroadcastReceiver.class);
            int flags = PendingIntent.FLAG_NO_CREATE;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                flags |= PendingIntent.FLAG_IMMUTABLE;
            }

            PendingIntent pendingIntent = PendingIntent.getBroadcast(
                    context,
                    (int) reminder.getId(),
                    intent,
                    flags
            );

            if (pendingIntent != null) {
                alarmManager.cancel(pendingIntent);
                pendingIntent.cancel();
            }
        } catch (Exception e) {
            Log.e(TAG, "Error canceling alarm for reminder ID: " + reminder.getId(), e);
        }
    }

    public static long calculateTriggerTimeMillis(String dateStr, String timeStr) {
        try {
            String combined = dateStr + " " + (timeStr != null ? timeStr : "09:00");
            Date d = dateTimeFormat.parse(combined);
            return d != null ? d.getTime() : System.currentTimeMillis() + 60000;
        } catch (Exception e) {
            return System.currentTimeMillis() + 60000;
        }
    }

    public static String calculateNextReminderDate(String currentDateStr, String repeatType, int interval) {
        try {
            Date d = dateFormat.parse(currentDateStr);
            Calendar cal = Calendar.getInstance();
            if (d != null) cal.setTime(d);

            int n = Math.max(1, interval);

            switch (repeatType != null ? repeatType.toUpperCase() : "ONCE") {
                case "DAILY":
                    cal.add(Calendar.DAY_OF_YEAR, n);
                    break;
                case "WEEKLY":
                    cal.add(Calendar.WEEK_OF_YEAR, n);
                    break;
                case "MONTHLY":
                    cal.add(Calendar.MONTH, n);
                    break;
                case "YEARLY":
                    cal.add(Calendar.YEAR, n);
                    break;
                case "CUSTOM":
                    cal.add(Calendar.DAY_OF_YEAR, n);
                    break;
                default:
                    return currentDateStr;
            }
            return dateFormat.format(cal.getTime());
        } catch (Exception e) {
            return currentDateStr;
        }
    }

    public static void rescheduleAllEnabledReminders(Context context) {
        AppDatabase db = AppDatabase.getInstance(context.getApplicationContext());
        List<Reminder> enabledReminders = db.reminderDao().getEnabled();
        if (enabledReminders != null) {
            for (Reminder r : enabledReminders) {
                scheduleAlarm(context, r);
            }
        }
    }
}

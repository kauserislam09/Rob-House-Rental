package com.rob.houserental.notifications;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.rob.houserental.R;
import com.rob.houserental.ReminderDetailsActivity;
import com.rob.houserental.model.Reminder;

public class NotificationHelper {

    public static final String CHANNEL_ID = "rob_house_rental_reminders";

    public static void createNotificationChannel(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = context.getString(R.string.notification_channel_name);
            String description = context.getString(R.string.notification_channel_desc);
            int importance = NotificationManager.IMPORTANCE_HIGH;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);
            channel.enableVibration(true);

            NotificationManager notificationManager = context.getSystemService(NotificationManager.class);
            if (notificationManager != null) {
                notificationManager.createNotificationChannel(channel);
            }
        }
    }

    public static void showReminderNotification(Context context, Reminder reminder) {
        if (reminder == null) return;

        createNotificationChannel(context);

        Intent intent = new Intent(context, ReminderDetailsActivity.class);
        intent.putExtra("reminder_id", reminder.getId());
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);

        int flags = PendingIntent.FLAG_UPDATE_CURRENT;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            flags |= PendingIntent.FLAG_IMMUTABLE;
        }

        PendingIntent pendingIntent = PendingIntent.getActivity(
                context,
                (int) reminder.getId(),
                intent,
                flags
        );

        // Action: Complete
        Intent completeIntent = new Intent(context, ReminderActionReceiver.class);
        completeIntent.setAction(ReminderActionReceiver.ACTION_COMPLETE);
        completeIntent.putExtra("reminder_id", reminder.getId());
        PendingIntent completePendingIntent = PendingIntent.getBroadcast(
                context,
                (int) (reminder.getId() * 1000 + 1),
                completeIntent,
                flags
        );

        // Action: Snooze (30m)
        Intent snoozeIntent = new Intent(context, ReminderActionReceiver.class);
        snoozeIntent.setAction(ReminderActionReceiver.ACTION_SNOOZE);
        snoozeIntent.putExtra("reminder_id", reminder.getId());
        PendingIntent snoozePendingIntent = PendingIntent.getBroadcast(
                context,
                (int) (reminder.getId() * 1000 + 2),
                snoozeIntent,
                flags
        );

        String title = reminder.getTitle() != null ? reminder.getTitle() : context.getString(R.string.reminder);
        String text = reminder.getDescription() != null && !reminder.getDescription().isEmpty()
                ? reminder.getDescription()
                : context.getString(R.string.reminder_due_text);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_notifications)
                .setContentTitle(title)
                .setContentText(text)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(text))
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true)
                .setContentIntent(pendingIntent)
                .addAction(R.drawable.ic_notifications, context.getString(R.string.mark_completed), completePendingIntent)
                .addAction(R.drawable.ic_notifications, context.getString(R.string.snooze_reminder), snoozePendingIntent);

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
        try {
            notificationManager.notify((int) reminder.getId(), builder.build());
        } catch (SecurityException e) {
            android.util.Log.e("NotificationHelper", "Permission error showing notification", e);
        }
    }
}

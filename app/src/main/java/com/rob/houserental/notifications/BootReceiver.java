package com.rob.houserental.notifications;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import java.util.concurrent.Executors;

public class BootReceiver extends BroadcastReceiver {

    private static final String TAG = "BootReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent == null) return;
        String action = intent.getAction();
        if (Intent.ACTION_BOOT_COMPLETED.equals(action) || Intent.ACTION_MY_PACKAGE_REPLACED.equals(action)) {
            final PendingResult pendingResult = goAsync();

            Executors.newSingleThreadExecutor().execute(() -> {
                try {
                    Context appContext = context.getApplicationContext();
                    ReminderSchedulerUtils.rescheduleAllEnabledReminders(appContext);
                } catch (Exception e) {
                    Log.e(TAG, "Error rescheduling reminders on boot", e);
                } finally {
                    pendingResult.finish();
                }
            });
        }
    }
}

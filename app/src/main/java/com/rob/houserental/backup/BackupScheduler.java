package com.rob.houserental.backup;

import android.content.Context;

import androidx.work.BackoffPolicy;
import androidx.work.Constraints;
import androidx.work.ExistingPeriodicWorkPolicy;
import androidx.work.NetworkType;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;

import java.util.concurrent.TimeUnit;

public class BackupScheduler {

    public static void updateSchedule(Context context, String schedule) {
        WorkManager workManager = WorkManager.getInstance(context.getApplicationContext());

        if (BackupPreferences.SCHEDULE_OFF.equalsIgnoreCase(schedule)) {
            workManager.cancelUniqueWork(BackupWorker.UNIQUE_WORK_NAME);
            return;
        }

        long repeatInterval;
        TimeUnit timeUnit;

        if (BackupPreferences.SCHEDULE_7_DAYS.equalsIgnoreCase(schedule)) {
            repeatInterval = 7;
            timeUnit = TimeUnit.DAYS;
        } else if (BackupPreferences.SCHEDULE_15_DAYS.equalsIgnoreCase(schedule)) {
            repeatInterval = 15;
            timeUnit = TimeUnit.DAYS;
        } else if (BackupPreferences.SCHEDULE_30_DAYS.equalsIgnoreCase(schedule)) {
            repeatInterval = 30;
            timeUnit = TimeUnit.DAYS;
        } else {
            // Default: DAILY (24 hours)
            repeatInterval = 1;
            timeUnit = TimeUnit.DAYS;
        }

        Constraints constraints = new Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build();

        PeriodicWorkRequest workRequest = new PeriodicWorkRequest.Builder(
                BackupWorker.class,
                repeatInterval,
                timeUnit
        )
                .setConstraints(constraints)
                .setBackoffCriteria(
                        BackoffPolicy.EXPONENTIAL,
                        15,
                        TimeUnit.MINUTES
                )
                .build();

        workManager.enqueueUniquePeriodicWork(
                BackupWorker.UNIQUE_WORK_NAME,
                ExistingPeriodicWorkPolicy.UPDATE,
                workRequest
        );
    }

    public static void cancelSchedule(Context context) {
        WorkManager workManager = WorkManager.getInstance(context.getApplicationContext());
        workManager.cancelUniqueWork(BackupWorker.UNIQUE_WORK_NAME);
    }
}

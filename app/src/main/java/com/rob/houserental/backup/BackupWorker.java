package com.rob.houserental.backup;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.rob.houserental.data.AppDatabase;
import com.rob.houserental.model.BackupHistory;

public class BackupWorker extends Worker {

    public static final String UNIQUE_WORK_NAME = "rob_house_rental_auto_backup";

    public BackupWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    @NonNull
    @Override
    public Result doWork() {
        Context context = getApplicationContext();
        BackupPreferences prefs = new BackupPreferences(context);

        // 1. Check if automatic backup is enabled
        String schedule = prefs.getBackupSchedule();
        if (BackupPreferences.SCHEDULE_OFF.equalsIgnoreCase(schedule)) {
            return Result.success();
        }

        // 2. Check if Google Drive is authorized
        String accountEmail = prefs.getGoogleAccountEmail();
        if (!prefs.isGoogleDriveConnected() || accountEmail == null || accountEmail.trim().isEmpty()) {
            prefs.setLastBackupStatus("FAILED");
            prefs.setLastBackupTime(System.currentTimeMillis());
            return Result.failure();
        }

        AppDatabase db = AppDatabase.getInstance(context);
        long startTime = System.currentTimeMillis();

        BackupHistory history = new BackupHistory(
                "",
                BackupManager.BACKUP_FILE_NAME,
                "",
                startTime,
                0,
                0,
                "IN_PROGRESS",
                "AUTOMATIC",
                "",
                "",
                "1.0",
                0
        );

        long historyId = db.backupHistoryDao().insert(history);
        history.setId(historyId);

        try {
            // 3. Create local validated package
            BackupManager manager = new BackupManager(context);
            BackupManager.BackupResult backupResult = manager.createBackupPackage((message, percentage) -> {
            });

            // 4. Upload to Google Drive and verify
            GoogleDriveService driveService = new GoogleDriveService(context, accountEmail);
            com.google.api.services.drive.model.File verifiedFile = driveService.uploadAndVerifyBackupFile(
                    backupResult.zipFile,
                    backupResult.zipFile.getName(),
                    backupResult.manifest,
                    backupResult.archiveChecksum
            );

            if (verifiedFile == null || verifiedFile.getId() == null || verifiedFile.getId().isEmpty()) {
                throw new Exception("Drive upload verification failed: invalid remote file ID.");
            }

            long endTime = System.currentTimeMillis();
            long sizeBytes = backupResult.zipFile.length();

            history.setBackupId(backupResult.manifest.getBackupId());
            history.setFileName(backupResult.zipFile.getName());
            history.setDriveFileId(verifiedFile.getId());
            history.setCompletedAt(endTime);
            history.setSizeBytes(sizeBytes);
            history.setStatus("SUCCESS");
            history.setChecksum(backupResult.archiveChecksum);
            history.setDocumentCount(backupResult.manifest.getDocumentCount());
            history.setErrorMessage(null);
            db.backupHistoryDao().update(history);

            prefs.setLastBackupTime(endTime);
            prefs.setLastBackupStatus("SUCCESS");
            prefs.setLastBackupSizeBytes(sizeBytes);
            prefs.setLastBackupType("AUTOMATIC");
            prefs.setLastDriveFileId(verifiedFile.getId());

            return Result.success();
        } catch (Exception e) {
            history.setCompletedAt(System.currentTimeMillis());
            history.setStatus("FAILED");
            history.setErrorMessage(e.getMessage());
            db.backupHistoryDao().update(history);

            prefs.setLastBackupTime(System.currentTimeMillis());
            prefs.setLastBackupStatus("FAILED");

            if (getRunAttemptCount() < 3) {
                return Result.retry();
            }
            return Result.failure();
        }
    }
}

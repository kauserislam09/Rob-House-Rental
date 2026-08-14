package com.rob.houserental.backup;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.util.Log;

import androidx.sqlite.db.SimpleSQLiteQuery;

import com.rob.houserental.data.AppDatabase;
import com.rob.houserental.model.AppDocument;
import com.rob.houserental.model.Expense;
import com.rob.houserental.model.Property;
import com.rob.houserental.model.TenantDocument;

import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

public class BackupManager {

    private static final String TAG = "BackupManager";
    public static final String BACKUP_FILE_NAME = "RobHouseRental_Backup.zip";
    public static final String DATABASE_NAME = "rob_house_rental.db";

    private final Context context;
    private static final Object DB_SNAPSHOT_LOCK = new Object();

    public interface BackupProgressListener {
        void onProgress(String message, int percentage);
    }

    public interface RestoreCallback {
        void onRestoreSuccess(BackupManifest manifest);
        void onRestoreError(String error);
    }

    public static class BackupResult {
        public final File zipFile;
        public final BackupManifest manifest;
        public final String databaseChecksum;
        public final String archiveChecksum;

        public BackupResult(File zipFile, BackupManifest manifest, String databaseChecksum, String archiveChecksum) {
            this.zipFile = zipFile;
            this.manifest = manifest;
            this.databaseChecksum = databaseChecksum;
            this.archiveChecksum = archiveChecksum;
        }
    }

    public static class RestorePreview {
        public final File zipFile;
        public final BackupManifest manifest;
        public final List<String> propertyNames;
        public final int propertyCount;
        public final int tenantCount;
        public final int unitCount;
        public final int documentCount;
        public final long backupSizeBytes;
        public final long createdAt;

        public RestorePreview(
                File zipFile,
                BackupManifest manifest,
                List<String> propertyNames,
                int propertyCount,
                int tenantCount,
                int unitCount,
                int documentCount,
                long backupSizeBytes,
                long createdAt
        ) {
            this.zipFile = zipFile;
            this.manifest = manifest;
            this.propertyNames = propertyNames != null ? propertyNames : new ArrayList<>();
            this.propertyCount = propertyCount;
            this.tenantCount = tenantCount;
            this.unitCount = unitCount;
            this.documentCount = documentCount;
            this.backupSizeBytes = backupSizeBytes;
            this.createdAt = createdAt;
        }
    }

    public BackupManager(Context context) {
        this.context = context.getApplicationContext();
    }

    public BackupResult createBackupPackage(BackupProgressListener listener) throws Exception {
        if (listener != null) listener.onProgress("Flushing database WAL...", 10);

        File stagingDir = new File(context.getCacheDir(), "backup_staging_" + System.currentTimeMillis());
        if (stagingDir.exists()) {
            deleteRecursive(stagingDir);
        }
        stagingDir.mkdirs();

        File dbStagingDir = new File(stagingDir, "database");
        dbStagingDir.mkdirs();
        File docsStagingDir = new File(stagingDir, "documents");
        docsStagingDir.mkdirs();
        File metaStagingDir = new File(stagingDir, "metadata");
        metaStagingDir.mkdirs();
        File settingsStagingDir = new File(stagingDir, "settings");
        settingsStagingDir.mkdirs();

        String dbChecksum;
        File liveDbFile = context.getDatabasePath(DATABASE_NAME);
        File stagedDbFile = new File(dbStagingDir, DATABASE_NAME);
        BackupManifest manifest = new BackupManifest();

        // 1. Safe SQLite Checkpoint and Immutable Snapshot Creation
        synchronized (DB_SNAPSHOT_LOCK) {
            AppDatabase liveDb = AppDatabase.getInstance(context);

            // Fetch live property names and counts from active Room instance for validation comparison
            List<Property> liveProperties = liveDb.propertyDao().getAllProperties();
            Set<String> livePropertyNames = new HashSet<>();
            if (liveProperties != null) {
                for (Property p : liveProperties) {
                    if (p.getName() != null && !p.getName().trim().isEmpty()) {
                        livePropertyNames.add(p.getName().trim());
                    }
                }
            }

            // Force SQLite to flush and truncate WAL so all committed data resides in rob_house_rental.db
            try {
                Cursor cursor = liveDb.query(new SimpleSQLiteQuery("PRAGMA wal_checkpoint(TRUNCATE)"));
                if (cursor != null) {
                    if (cursor.moveToFirst()) {
                        int busy = cursor.getInt(0);
                        int log = cursor.getInt(1);
                        int checkpointed = cursor.getInt(2);
                        Log.d(TAG, "wal_checkpoint(TRUNCATE) result: busy=" + busy + ", log=" + log + ", checkpointed=" + checkpointed);
                    }
                    cursor.close();
                }
            } catch (Exception e) {
                Log.w(TAG, "Checkpoint warning: " + e.getMessage());
            }

            if (listener != null) listener.onProgress("Creating consistent database snapshot...", 25);

            if (!liveDbFile.exists()) {
                throw new IOException("Database file does not exist.");
            }

            // Copy main DB file
            copyFile(liveDbFile, stagedDbFile);

            // 2. Validate the staged snapshot database content directly
            if (listener != null) listener.onProgress("Verifying snapshot database integrity...", 35);

            List<String> snapshotPropertyNames = new ArrayList<>();
            int snapPropCount = 0;
            int snapTenantCount = 0;
            int snapUnitCount = 0;
            int snapTenancyCount = 0;
            int snapRentCount = 0;
            int snapPaymentCount = 0;
            int snapBillCount = 0;
            int snapBillPayCount = 0;
            int snapExpenseCount = 0;
            int snapAppDocCount = 0;
            int snapTenantDocCount = 0;

            try (SQLiteDatabase testDb = SQLiteDatabase.openDatabase(stagedDbFile.getAbsolutePath(), null, SQLiteDatabase.OPEN_READONLY)) {
                // Check integrity
                try (Cursor c = testDb.rawQuery("PRAGMA integrity_check", null)) {
                    if (c != null && c.moveToFirst()) {
                        String result = c.getString(0);
                        if (!"ok".equalsIgnoreCase(result)) {
                            throw new IOException("Snapshot database integrity check failed: " + result);
                        }
                    }
                }

                // Query snapshot property names
                try (Cursor c = testDb.rawQuery("SELECT name FROM properties", null)) {
                    if (c != null) {
                        while (c.moveToNext()) {
                            String name = c.getString(0);
                            if (name != null) {
                                snapshotPropertyNames.add(name.trim());
                            }
                        }
                    }
                }

                snapPropCount = queryCount(testDb, "properties");
                snapUnitCount = queryCount(testDb, "units");
                snapTenantCount = queryCount(testDb, "tenants");
                snapTenancyCount = queryCount(testDb, "tenancies");
                snapRentCount = queryCount(testDb, "rent_records");
                snapPaymentCount = queryCount(testDb, "payments");
                snapBillCount = queryCount(testDb, "utility_bills");
                snapBillPayCount = queryCount(testDb, "bill_payments");
                snapExpenseCount = queryCount(testDb, "expenses");
                snapAppDocCount = queryCount(testDb, "app_documents");
                snapTenantDocCount = queryCount(testDb, "tenant_documents");
            }

            // CRITICAL PROOF: Verify that all live properties are present in the snapshot
            for (String liveName : livePropertyNames) {
                boolean found = false;
                for (String snapName : snapshotPropertyNames) {
                    if (liveName.equalsIgnoreCase(snapName)) {
                        found = true;
                        break;
                    }
                }
                if (!found) {
                    deleteRecursive(stagingDir);
                    throw new IOException("Backup snapshot validation failed: Live property '" + liveName +
                            "' was not captured in the database snapshot. Snapshot aborted to prevent data loss.");
                }
            }

            // Populate manifest with verified snapshot counts
            manifest.setPropertyCount(snapPropCount);
            manifest.setUnitCount(snapUnitCount);
            manifest.setTenantCount(snapTenantCount);
            manifest.setTenancyCount(snapTenancyCount);
            manifest.setRentRecordCount(snapRentCount);
            manifest.setPaymentCount(snapPaymentCount);
            manifest.setUtilityBillCount(snapBillCount);
            manifest.setBillPaymentCount(snapBillPayCount);
            manifest.setExpenseCount(snapExpenseCount);
            manifest.setAppDocumentCount(snapAppDocCount);
            manifest.setTenantDocumentCount(snapTenantDocCount);
            manifest.setPropertyNames(snapshotPropertyNames);

            Log.d(TAG, "BACKUP SNAPSHOT SUMMARY:");
            Log.d(TAG, "Properties: " + snapPropCount + " (" + manifest.getPropertySummary() + ")");
            Log.d(TAG, "Units: " + snapUnitCount + ", Tenants: " + snapTenantCount + ", Tenancies: " + snapTenancyCount);
            Log.d(TAG, "Rent Records: " + snapRentCount + ", Payments: " + snapPaymentCount);
            Log.d(TAG, "Utility Bills: " + snapBillCount + ", Expenses: " + snapExpenseCount);

            dbChecksum = calculateSha256(stagedDbFile);
        }

        if (listener != null) listener.onProgress("Gathering documents and receipts...", 50);

        // 3. Copy all locally stored documents with collision-safe subpaths
        int docCount = 0;
        Set<String> backedUpAbsolutePaths = new HashSet<>();
        AppDatabase db = AppDatabase.getInstance(context);

        // 3a. AppDocument files
        try {
            List<AppDocument> appDocs = db.appDocumentDao().getAllDocuments();
            if (appDocs != null) {
                for (AppDocument doc : appDocs) {
                    if (doc.getFilePath() != null && !doc.getFilePath().trim().isEmpty()) {
                        File docFile = new File(doc.getFilePath());
                        if (docFile.exists() && docFile.isFile()) {
                            File targetDir = new File(docsStagingDir, "app/" + doc.getId());
                            targetDir.mkdirs();
                            copyFile(docFile, new File(targetDir, docFile.getName()));
                            backedUpAbsolutePaths.add(docFile.getAbsolutePath());
                            docCount++;
                        }
                    }
                }
            }
        } catch (Exception ignored) {
        }

        // 3b. TenantDocument files
        try {
            List<TenantDocument> tenantDocs = db.tenantDocumentDao().getAllDocuments();
            if (tenantDocs != null) {
                for (TenantDocument tDoc : tenantDocs) {
                    if (tDoc.getFilePath() != null && !tDoc.getFilePath().trim().isEmpty()) {
                        File docFile = new File(tDoc.getFilePath());
                        if (docFile.exists() && docFile.isFile()) {
                            File targetDir = new File(docsStagingDir, "tenant/" + tDoc.getId());
                            targetDir.mkdirs();
                            copyFile(docFile, new File(targetDir, docFile.getName()));
                            backedUpAbsolutePaths.add(docFile.getAbsolutePath());
                            docCount++;
                        }
                    }
                }
            }
        } catch (Exception ignored) {
        }

        // 3c. Expense receipts
        try {
            List<Expense> expenses = db.expenseDao().getAllExpenses();
            if (expenses != null) {
                for (Expense exp : expenses) {
                    if (exp.getReceiptPath() != null && !exp.getReceiptPath().trim().isEmpty()) {
                        File rFile = new File(exp.getReceiptPath());
                        if (rFile.exists() && rFile.isFile()) {
                            File targetDir = new File(docsStagingDir, "expense/" + exp.getId());
                            targetDir.mkdirs();
                            copyFile(rFile, new File(targetDir, rFile.getName()));
                            backedUpAbsolutePaths.add(rFile.getAbsolutePath());
                            docCount++;
                        }
                    }
                }
            }
        } catch (Exception ignored) {
        }

        // 3d. Additional files in documents directory
        File appDocsFolder = new File(context.getFilesDir(), "documents");
        if (appDocsFolder.exists() && appDocsFolder.isDirectory()) {
            File[] files = appDocsFolder.listFiles();
            if (files != null) {
                for (File f : files) {
                    if (f.isFile() && !backedUpAbsolutePaths.contains(f.getAbsolutePath())) {
                        File targetDir = new File(docsStagingDir, "misc");
                        targetDir.mkdirs();
                        copyFile(f, new File(targetDir, f.getName()));
                        backedUpAbsolutePaths.add(f.getAbsolutePath());
                        docCount++;
                    }
                }
            }
        }

        if (listener != null) listener.onProgress("Saving application settings & manifest...", 65);

        // 4. Save settings
        try {
            BackupPreferences prefs = new BackupPreferences(context);
            JSONObject settingsJson = new JSONObject();
            settingsJson.put("schedule", prefs.getBackupSchedule());
            File settingsFile = new File(settingsStagingDir, "settings.json");
            try (FileOutputStream fos = new FileOutputStream(settingsFile)) {
                fos.write(settingsJson.toString(2).getBytes(StandardCharsets.UTF_8));
            }
        } catch (Exception ignored) {
        }

        // 5. Create manifest
        String backupId = UUID.randomUUID().toString();
        manifest.setBackupId(backupId);
        manifest.setDatabaseChecksum(dbChecksum);
        manifest.setDocumentCount(docCount);

        File manifestFile = new File(metaStagingDir, "backup_manifest.json");
        try (FileOutputStream fos = new FileOutputStream(manifestFile)) {
            fos.write(manifest.toJson().getBytes(StandardCharsets.UTF_8));
        }

        if (listener != null) listener.onProgress("Compressing backup package...", 80);

        // 6. Zip into uniquely timestamped backup file
        File backupsDir = new File(context.getFilesDir(), "backups");
        if (!backupsDir.exists()) {
            backupsDir.mkdirs();
        }

        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd_HHmmss_SSS", Locale.US);
        String timestampedName = "RobHouseRental_Backup_" + sdf.format(new Date()) + ".zip";
        File outputZipFile = new File(backupsDir, timestampedName);

        zipFolder(stagingDir, outputZipFile);

        manifest.setBackupSizeBytes(outputZipFile.length());
        String archiveChecksum = calculateSha256(outputZipFile);

        // Clean up staging
        deleteRecursive(stagingDir);

        if (listener != null) listener.onProgress("Performing package self-verification...", 90);

        // 7. SELF-VERIFY THE COMPLETED ZIP ARCHIVE
        selfVerifyBackupZip(outputZipFile, manifest, dbChecksum);

        if (listener != null) listener.onProgress("Backup created and self-verified successfully.", 100);

        return new BackupResult(outputZipFile, manifest, dbChecksum, archiveChecksum);
    }

    private static int queryCount(SQLiteDatabase db, String tableName) {
        try (Cursor c = db.rawQuery("SELECT COUNT(*) FROM " + tableName, null)) {
            if (c != null && c.moveToFirst()) {
                return c.getInt(0);
            }
        } catch (Exception e) {
            Log.w(TAG, "Could not query count for table " + tableName + ": " + e.getMessage());
        }
        return 0;
    }

    private void selfVerifyBackupZip(File zipFile, BackupManifest expectedManifest, String expectedDbChecksum) throws Exception {
        File verifyStaging = new File(context.getCacheDir(), "self_verify_" + System.currentTimeMillis());
        try {
            safeUnzip(zipFile, verifyStaging);

            File verifiedDb = new File(verifyStaging, "database/" + DATABASE_NAME);
            if (!verifiedDb.exists()) {
                throw new IOException("Self-verification failed: database missing from generated ZIP.");
            }

            String readDbChecksum = calculateSha256(verifiedDb);
            if (!readDbChecksum.equalsIgnoreCase(expectedDbChecksum)) {
                throw new IOException("Self-verification failed: database checksum mismatch in generated ZIP.");
            }

            try (SQLiteDatabase testDb = SQLiteDatabase.openDatabase(verifiedDb.getAbsolutePath(), null, SQLiteDatabase.OPEN_READONLY)) {
                try (Cursor c = testDb.rawQuery("PRAGMA integrity_check", null)) {
                    if (c != null && c.moveToFirst()) {
                        String res = c.getString(0);
                        if (!"ok".equalsIgnoreCase(res)) {
                            throw new IOException("Self-verification failed: integrity check error: " + res);
                        }
                    }
                }

                // Verify property names can be read from generated ZIP
                if (expectedManifest.getPropertyNames() != null && !expectedManifest.getPropertyNames().isEmpty()) {
                    List<String> names = new ArrayList<>();
                    try (Cursor c = testDb.rawQuery("SELECT name FROM properties", null)) {
                        if (c != null) {
                            while (c.moveToNext()) {
                                names.add(c.getString(0));
                            }
                        }
                    }
                    for (String expName : expectedManifest.getPropertyNames()) {
                        boolean found = false;
                        for (String n : names) {
                            if (expName.equalsIgnoreCase(n)) {
                                found = true;
                                break;
                            }
                        }
                        if (!found) {
                            throw new IOException("Self-verification failed: Property '" + expName + "' missing from generated ZIP database.");
                        }
                    }
                }
            }
        } finally {
            deleteRecursive(verifyStaging);
        }
    }

    public BackupManifest validateBackupPackage(File zipFile) throws Exception {
        if (zipFile == null || !zipFile.exists() || zipFile.length() == 0) {
            throw new IOException("Backup archive file is missing or empty.");
        }

        BackupManifest manifest = null;
        boolean hasDatabase = false;

        try (ZipInputStream zis = new ZipInputStream(new BufferedInputStream(new FileInputStream(zipFile)))) {
            ZipEntry entry;
            byte[] buffer = new byte[4096];
            while ((entry = zis.getNextEntry()) != null) {
                String name = entry.getName().replace("\\", "/");
                if (name.contains("../") || name.contains("..\\")) {
                    throw new IOException("Security error: backup archive contains invalid path traversal.");
                }

                if (name.endsWith("backup_manifest.json")) {
                    StringBuilder sb = new StringBuilder();
                    int len;
                    while ((len = zis.read(buffer)) > 0) {
                        sb.append(new String(buffer, 0, len, StandardCharsets.UTF_8));
                    }
                    manifest = BackupManifest.fromJson(sb.toString());
                } else if (name.endsWith(DATABASE_NAME)) {
                    hasDatabase = true;
                }
                zis.closeEntry();
            }
        }

        if (manifest == null) {
            throw new IOException("Invalid backup archive: missing backup_manifest.json");
        }

        if (!hasDatabase) {
            throw new IOException("Invalid backup archive: missing database snapshot.");
        }

        if (manifest.getBackupFormatVersion() > BackupManifest.CURRENT_BACKUP_FORMAT_VERSION) {
            throw new IOException("This backup format version (" + manifest.getBackupFormatVersion() + ") requires a newer version of Rob House Rental.");
        }

        return manifest;
    }

    public RestorePreview prepareRestorePreview(File zipFile) throws Exception {
        BackupManifest manifest = validateBackupPackage(zipFile);

        File stagingDir = new File(context.getCacheDir(), "restore_preview_" + System.currentTimeMillis());
        try {
            safeUnzip(zipFile, stagingDir);

            File dbFile = new File(stagingDir, "database/" + DATABASE_NAME);
            if (!dbFile.exists()) {
                dbFile = findFile(stagingDir, DATABASE_NAME);
            }

            if (dbFile == null || !dbFile.exists()) {
                throw new IOException("Database file not found in backup package.");
            }

            // Verify checksum
            String calculatedDbChecksum = calculateSha256(dbFile);
            if (manifest.getDatabaseChecksum() != null && !manifest.getDatabaseChecksum().isEmpty()) {
                if (!manifest.getDatabaseChecksum().equalsIgnoreCase(calculatedDbChecksum)) {
                    throw new IOException("Database checksum mismatch: file is corrupted.");
                }
            }

            List<String> propertyNames = new ArrayList<>();
            int propCount = 0;
            int tenantCount = 0;
            int unitCount = 0;

            try (SQLiteDatabase testDb = SQLiteDatabase.openDatabase(dbFile.getAbsolutePath(), null, SQLiteDatabase.OPEN_READONLY)) {
                try (Cursor c = testDb.rawQuery("PRAGMA integrity_check", null)) {
                    if (c != null && c.moveToFirst()) {
                        String res = c.getString(0);
                        if (!"ok".equalsIgnoreCase(res)) {
                            throw new IOException("Extracted database integrity check failed: " + res);
                        }
                    }
                }

                try (Cursor c = testDb.rawQuery("SELECT name FROM properties", null)) {
                    if (c != null) {
                        while (c.moveToNext()) {
                            String n = c.getString(0);
                            if (n != null) propertyNames.add(n.trim());
                        }
                    }
                }

                propCount = queryCount(testDb, "properties");
                unitCount = queryCount(testDb, "units");
                tenantCount = queryCount(testDb, "tenants");
            }

            return new RestorePreview(
                    zipFile,
                    manifest,
                    propertyNames,
                    propCount,
                    tenantCount,
                    unitCount,
                    manifest.getDocumentCount(),
                    zipFile.length(),
                    manifest.getCreatedAt()
            );
        } finally {
            deleteRecursive(stagingDir);
        }
    }

    public void restoreFromPackage(File zipFile, BackupProgressListener listener, RestoreCallback callback) {
        File safetyDbCopy = null;
        File safetyDocsDir = null;
        File safetyReceiptsDir = null;
        File liveDb = context.getDatabasePath(DATABASE_NAME);
        File stagingDir = null;

        try {
            if (listener != null) listener.onProgress("Validating backup archive...", 10);

            BackupManifest manifest = validateBackupPackage(zipFile);

            if (listener != null) listener.onProgress("Extracting backup archive with security checks...", 25);

            stagingDir = new File(context.getCacheDir(), "restore_staging_" + System.currentTimeMillis());
            if (stagingDir.exists()) {
                deleteRecursive(stagingDir);
            }
            stagingDir.mkdirs();

            safeUnzip(zipFile, stagingDir);

            File restoredDbFile = new File(stagingDir, "database/" + DATABASE_NAME);
            if (!restoredDbFile.exists()) {
                restoredDbFile = findFile(stagingDir, DATABASE_NAME);
            }

            if (restoredDbFile == null || !restoredDbFile.exists()) {
                deleteRecursive(stagingDir);
                if (callback != null) callback.onRestoreError("Corrupt backup: database file not found in archive.");
                return;
            }

            // Verify checksum
            String calculatedDbChecksum = calculateSha256(restoredDbFile);
            if (manifest.getDatabaseChecksum() != null && !manifest.getDatabaseChecksum().isEmpty()) {
                if (!manifest.getDatabaseChecksum().equalsIgnoreCase(calculatedDbChecksum)) {
                    deleteRecursive(stagingDir);
                    if (callback != null) callback.onRestoreError("Checksum mismatch: database file is corrupted.");
                    return;
                }
            }

            // Test extracted DB integrity BEFORE touching live database
            if (listener != null) listener.onProgress("Validating database structure & integrity...", 35);
            try (SQLiteDatabase testDb = SQLiteDatabase.openDatabase(restoredDbFile.getAbsolutePath(), null, SQLiteDatabase.OPEN_READONLY)) {
                try (Cursor cursor = testDb.rawQuery("PRAGMA integrity_check", null)) {
                    if (cursor != null && cursor.moveToFirst()) {
                        String result = cursor.getString(0);
                        if (!"ok".equalsIgnoreCase(result)) {
                            deleteRecursive(stagingDir);
                            if (callback != null) callback.onRestoreError("Database integrity check failed: " + result);
                            return;
                        }
                    }
                }
            }

            if (listener != null) listener.onProgress("Creating exact safety snapshot...", 50);

            // Create complete safety snapshot of current live database and documents
            safetyDbCopy = new File(context.getCacheDir(), "pre_restore_safety_" + System.currentTimeMillis() + ".db");
            if (liveDb.exists()) {
                copyFile(liveDb, safetyDbCopy);
            }

            File appDocsDir = new File(context.getFilesDir(), "documents");
            File appReceiptsDir = new File(context.getFilesDir(), "expenses/receipts");

            safetyDocsDir = new File(context.getCacheDir(), "safety_docs_" + System.currentTimeMillis());
            if (appDocsDir.exists()) {
                copyFolder(appDocsDir, safetyDocsDir);
            }

            safetyReceiptsDir = new File(context.getCacheDir(), "safety_receipts_" + System.currentTimeMillis());
            if (appReceiptsDir.exists()) {
                copyFolder(appReceiptsDir, safetyReceiptsDir);
            }

            if (listener != null) listener.onProgress("Restoring database...", 65);

            // Close Room and replace DB
            synchronized (DB_SNAPSHOT_LOCK) {
                try {
                    List<com.rob.houserental.model.Reminder> oldReminders = AppDatabase.getInstance(context).reminderDao().getAll();
                    if (oldReminders != null) {
                        for (com.rob.houserental.model.Reminder r : oldReminders) {
                            com.rob.houserental.notifications.ReminderSchedulerUtils.cancelAlarm(context, r);
                        }
                    }
                } catch (Exception ignored) {}

                AppDatabase.closeDatabase();

                copyFile(restoredDbFile, liveDb);

                File walFile = new File(liveDb.getParentFile(), DATABASE_NAME + "-wal");
                if (walFile.exists()) walFile.delete();
                File shmFile = new File(liveDb.getParentFile(), DATABASE_NAME + "-shm");
                if (shmFile.exists()) shmFile.delete();
            }

            if (listener != null) listener.onProgress("Restoring application documents & receipts...", 75);

            // Restore documents atomically
            if (appDocsDir.exists()) deleteRecursive(appDocsDir);
            appDocsDir.mkdirs();

            if (appReceiptsDir.exists()) deleteRecursive(appReceiptsDir);
            appReceiptsDir.mkdirs();

            File stagedDocsFolder = new File(stagingDir, "documents");
            if (stagedDocsFolder.exists() && stagedDocsFolder.isDirectory()) {
                copyFolder(stagedDocsFolder, appDocsDir);
            }

            if (listener != null) listener.onProgress("Reinitializing & verifying database integrity...", 85);

            // Reinitialize Room
            AppDatabase restoredDb = AppDatabase.getInstance(context);

            try (Cursor cursor = restoredDb.query(new SimpleSQLiteQuery("PRAGMA integrity_check"))) {
                if (cursor != null && cursor.moveToFirst()) {
                    String integrity = cursor.getString(0);
                    if (!"ok".equalsIgnoreCase(integrity)) {
                        throw new IOException("Database integrity check failed after restore: " + integrity);
                    }
                }
            }

            // Reconstruct file paths dynamically from structured subpaths
            try {
                List<AppDocument> appDocs = restoredDb.appDocumentDao().getAllDocuments();
                if (appDocs != null) {
                    for (AppDocument doc : appDocs) {
                        boolean found = false;

                        // Primary: scan structured subfolder app/<id>/ for any file
                        File appSubDir = new File(appDocsDir, "app/" + doc.getId());
                        if (appSubDir.exists() && appSubDir.isDirectory()) {
                            File[] files = appSubDir.listFiles();
                            if (files != null && files.length > 0) {
                                doc.setFilePath(files[0].getAbsolutePath());
                                // Fix-up MIME type if bad
                                String mime = com.rob.houserental.utils.DocumentOpenUtils.resolveMimeType(files[0], doc.getMimeType(), doc.getFileName());
                                if (doc.getMimeType() == null || doc.getMimeType().trim().isEmpty()
                                        || "application/octet-stream".equalsIgnoreCase(doc.getMimeType())) {
                                    doc.setMimeType(mime);
                                }
                                restoredDb.appDocumentDao().update(doc);
                                found = true;
                            }
                        }

                        if (!found) {
                            File fallbackFile = new File(appDocsDir, doc.getFileName() != null ? doc.getFileName() : "");
                            if (fallbackFile.exists()) {
                                doc.setFilePath(fallbackFile.getAbsolutePath());
                                restoredDb.appDocumentDao().update(doc);
                            }
                        }
                    }
                }


                List<TenantDocument> tenantDocs = restoredDb.tenantDocumentDao().getAllDocuments();
                if (tenantDocs != null) {
                    for (TenantDocument tDoc : tenantDocs) {
                        boolean found = false;

                        // Primary: scan structured subfolder tenant/<id>/ for any file
                        File tenantSubDir = new File(appDocsDir, "tenant/" + tDoc.getId());
                        if (tenantSubDir.exists() && tenantSubDir.isDirectory()) {
                            File[] files = tenantSubDir.listFiles();
                            if (files != null && files.length > 0) {
                                tDoc.setFilePath(files[0].getAbsolutePath());
                                // Fix-up MIME type from actual extension if stored MIME was bad
                                String ext = com.rob.houserental.utils.DocumentOpenUtils.getFileExtension(files[0].getName());
                                String mime = com.rob.houserental.utils.DocumentOpenUtils.resolveMimeType(files[0], tDoc.getMimeType(), tDoc.getDisplayName());
                                if (tDoc.getMimeType() == null || tDoc.getMimeType().trim().isEmpty()
                                        || "application/octet-stream".equalsIgnoreCase(tDoc.getMimeType())) {
                                    tDoc.setMimeType(mime);
                                }
                                restoredDb.tenantDocumentDao().update(tDoc);
                                found = true;
                            }
                        }

                        if (!found) {
                            // Fallback: look in flat documents/ folder by displayName
                            File fallbackFile = new File(appDocsDir, tDoc.getDisplayName() != null ? tDoc.getDisplayName() : "");
                            if (fallbackFile.exists()) {
                                tDoc.setFilePath(fallbackFile.getAbsolutePath());
                                restoredDb.tenantDocumentDao().update(tDoc);
                            }
                        }
                    }
                }


                List<Expense> expenses = restoredDb.expenseDao().getAllExpenses();
                if (expenses != null) {
                    for (Expense exp : expenses) {
                        if (exp.getReceiptPath() == null && exp.getReceiptName() == null) continue;
                        boolean found = false;

                        // Primary: scan structured subfolder expense/<id>/ for any file
                        File expSubDir = new File(appDocsDir, "expense/" + exp.getId());
                        if (expSubDir.exists() && expSubDir.isDirectory()) {
                            File[] files = expSubDir.listFiles();
                            if (files != null && files.length > 0) {
                                exp.setReceiptPath(files[0].getAbsolutePath());
                                // Fix-up MIME type if bad
                                String mime = com.rob.houserental.utils.DocumentOpenUtils.resolveMimeType(files[0], exp.getReceiptMimeType(), exp.getReceiptName());
                                if (exp.getReceiptMimeType() == null || exp.getReceiptMimeType().trim().isEmpty()
                                        || "application/octet-stream".equalsIgnoreCase(exp.getReceiptMimeType())) {
                                    exp.setReceiptMimeType(mime);
                                }
                                restoredDb.expenseDao().update(exp);
                                found = true;
                            }
                        }

                        if (!found) {
                            File fallbackFile = new File(appReceiptsDir, exp.getReceiptName() != null ? exp.getReceiptName() : "");
                            if (fallbackFile.exists()) {
                                exp.setReceiptPath(fallbackFile.getAbsolutePath());
                                restoredDb.expenseDao().update(exp);
                            }
                        }
                    }
                }

            } catch (Exception ignored) {
            }

            // Confirm queries execute cleanly and properties exist
            List<Property> props = restoredDb.propertyDao().getAllProperties();
            if (manifest.getPropertyNames() != null && !manifest.getPropertyNames().isEmpty()) {
                for (String expectedName : manifest.getPropertyNames()) {
                    boolean found = false;
                    if (props != null) {
                        for (Property p : props) {
                            if (expectedName.equalsIgnoreCase(p.getName())) {
                                found = true;
                                break;
                            }
                        }
                    }
                    if (!found) {
                        throw new IOException("Post-restore verification error: Property '" + expectedName + "' was not found in restored database.");
                    }
                }
            }

            // CLEAN UP STAGING & SAFETY COPIES ONLY AFTER ALL VERIFICATIONS PASS
            if (stagingDir != null) deleteRecursive(stagingDir);
            if (safetyDbCopy != null && safetyDbCopy.exists()) safetyDbCopy.delete();
            if (safetyDocsDir != null && safetyDocsDir.exists()) deleteRecursive(safetyDocsDir);
            if (safetyReceiptsDir != null && safetyReceiptsDir.exists()) deleteRecursive(safetyReceiptsDir);

            // Reschedule enabled restored reminders off main thread
            com.rob.houserental.notifications.ReminderSchedulerUtils.rescheduleAllEnabledReminders(context);

            if (listener != null) listener.onProgress("Restore completed successfully!", 100);

            if (callback != null) {
                callback.onRestoreSuccess(manifest);
            }
        } catch (Exception e) {
            // AUTOMATIC EXACT ROLLBACK TO PRE-RESTORE SAFETY STATE
            try {
                synchronized (DB_SNAPSHOT_LOCK) {
                    AppDatabase.closeDatabase();
                    if (safetyDbCopy != null && safetyDbCopy.exists()) {
                        copyFile(safetyDbCopy, liveDb);
                        safetyDbCopy.delete();
                    }
                    if (safetyDocsDir != null && safetyDocsDir.exists()) {
                        File appDocsDir = new File(context.getFilesDir(), "documents");
                        if (appDocsDir.exists()) deleteRecursive(appDocsDir);
                        copyFolder(safetyDocsDir, appDocsDir);
                        deleteRecursive(safetyDocsDir);
                    }
                    if (safetyReceiptsDir != null && safetyReceiptsDir.exists()) {
                        File appReceiptsDir = new File(context.getFilesDir(), "expenses/receipts");
                        if (appReceiptsDir.exists()) deleteRecursive(appReceiptsDir);
                        copyFolder(safetyReceiptsDir, appReceiptsDir);
                        deleteRecursive(safetyReceiptsDir);
                    }
                    AppDatabase.getInstance(context);
                }
            } catch (Exception rollbackEx) {
            }

            if (stagingDir != null) deleteRecursive(stagingDir);

            if (callback != null) {
                callback.onRestoreError("Restore failed and was safely rolled back: " + e.getMessage());
            }
        }
    }

    public void exportBackupToUri(File zipFile, Uri targetUri) throws IOException {
        try (InputStream in = new FileInputStream(zipFile);
             OutputStream out = context.getContentResolver().openOutputStream(targetUri)) {
            if (out == null) {
                throw new IOException("Cannot open output stream for selected URI.");
            }
            byte[] buffer = new byte[8192];
            int bytesRead;
            while ((bytesRead = in.read(buffer)) != -1) {
                out.write(buffer, 0, bytesRead);
            }
            out.flush();
        }
    }

    public File importBackupFromUri(Uri sourceUri) throws IOException {
        File stagingDir = new File(context.getCacheDir(), "import_staging");
        if (!stagingDir.exists()) stagingDir.mkdirs();

        File targetZip = new File(stagingDir, "imported_backup_" + System.currentTimeMillis() + ".zip");
        try (InputStream in = context.getContentResolver().openInputStream(sourceUri);
             OutputStream out = new FileOutputStream(targetZip)) {
            if (in == null) {
                throw new IOException("Cannot open input stream for selected URI.");
            }
            byte[] buffer = new byte[8192];
            int bytesRead;
            while ((bytesRead = in.read(buffer)) != -1) {
                out.write(buffer, 0, bytesRead);
            }
            out.flush();
        }
        return targetZip;
    }

    public static void zipFolder(File srcFolder, File destZipFile) throws IOException {
        try (ZipOutputStream zos = new ZipOutputStream(new BufferedOutputStream(new FileOutputStream(destZipFile)))) {
            addFolderToZip("", srcFolder, zos);
            zos.flush();
        }
    }

    private static void addFolderToZip(String path, File srcFile, ZipOutputStream zos) throws IOException {
        if (srcFile.isDirectory()) {
            String prefix = path.isEmpty() ? "" : path + "/";
            File[] files = srcFile.listFiles();
            if (files != null) {
                for (File file : files) {
                    addFolderToZip(prefix + file.getName(), file, zos);
                }
            }
        } else {
            byte[] buffer = new byte[4096];
            try (FileInputStream in = new FileInputStream(srcFile)) {
                zos.putNextEntry(new ZipEntry(path));
                int len;
                while ((len = in.read(buffer)) > 0) {
                    zos.write(buffer, 0, len);
                }
                zos.closeEntry();
            }
        }
    }

    public static void safeUnzip(File zipFile, File targetDir) throws IOException {
        String canonicalTargetDir = targetDir.getCanonicalPath();
        try (ZipInputStream zis = new ZipInputStream(new BufferedInputStream(new FileInputStream(zipFile)))) {
            ZipEntry entry;
            byte[] buffer = new byte[4096];
            while ((entry = zis.getNextEntry()) != null) {
                File file = new File(targetDir, entry.getName());
                String canonicalDestFile = file.getCanonicalPath();

                // ZIP Slip vulnerability protection
                if (!canonicalDestFile.startsWith(canonicalTargetDir + File.separator) && !canonicalDestFile.equals(canonicalTargetDir)) {
                    throw new IOException("Security Error: ZIP entry is attempting path traversal outside target directory: " + entry.getName());
                }

                if (entry.isDirectory()) {
                    file.mkdirs();
                } else {
                    File parent = file.getParentFile();
                    if (parent != null && !parent.exists()) {
                        parent.mkdirs();
                    }
                    try (FileOutputStream fos = new FileOutputStream(file)) {
                        int len;
                        while ((len = zis.read(buffer)) > 0) {
                            fos.write(buffer, 0, len);
                        }
                    }
                }
                zis.closeEntry();
            }
        }
    }

    private static File findFile(File dir, String name) {
        if (!dir.exists()) return null;
        File direct = new File(dir, name);
        if (direct.exists()) return direct;

        File[] files = dir.listFiles();
        if (files != null) {
            for (File f : files) {
                if (f.isDirectory()) {
                    File found = findFile(f, name);
                    if (found != null) return found;
                } else if (f.getName().equalsIgnoreCase(name)) {
                    return f;
                }
            }
        }
        return null;
    }

    public static void copyFile(File src, File dst) throws IOException {
        if (!src.exists()) return;
        File parent = dst.getParentFile();
        if (parent != null && !parent.exists()) {
            parent.mkdirs();
        }
        try (InputStream in = new FileInputStream(src);
             OutputStream out = new FileOutputStream(dst)) {
            byte[] buf = new byte[8192];
            int len;
            while ((len = in.read(buf)) > 0) {
                out.write(buf, 0, len);
            }
            out.flush();
        }
    }

    public static void copyFolder(File src, File dest) throws IOException {
        if (!src.exists()) return;
        if (src.isDirectory()) {
            if (!dest.exists()) dest.mkdirs();
            File[] files = src.listFiles();
            if (files != null) {
                for (File file : files) {
                    File destFile = new File(dest, file.getName());
                    copyFolder(file, destFile);
                }
            }
        } else {
            copyFile(src, dest);
        }
    }

    public static String calculateSha256(File file) {
        if (!file.exists()) return "";
        try (InputStream is = new FileInputStream(file)) {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] buffer = new byte[8192];
            int read;
            while ((read = is.read(buffer)) > 0) {
                digest.update(buffer, 0, read);
            }
            byte[] hash = digest.digest();
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (Exception e) {
            return "";
        }
    }

    public static void deleteRecursive(File fileOrDirectory) {
        if (fileOrDirectory == null || !fileOrDirectory.exists()) return;
        if (fileOrDirectory.isDirectory()) {
            File[] files = fileOrDirectory.listFiles();
            if (files != null) {
                for (File child : files) {
                    deleteRecursive(child);
                }
            }
        }
        fileOrDirectory.delete();
    }
}

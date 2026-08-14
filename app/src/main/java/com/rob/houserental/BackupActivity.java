package com.rob.houserental;

import android.accounts.AccountManager;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.MaterialAutoCompleteTextView;
import com.rob.houserental.adapter.BackupHistoryAdapter;
import com.rob.houserental.backup.BackupManager;
import com.rob.houserental.backup.BackupManifest;
import com.rob.houserental.backup.BackupPreferences;
import com.rob.houserental.backup.BackupScheduler;
import com.rob.houserental.backup.GoogleDriveService;
import com.rob.houserental.model.BackupHistory;
import com.rob.houserental.repository.BackupRepository;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class BackupActivity extends AppCompatActivity {

    private TextView tvDriveStatusBadge;
    private TextView tvDriveAccountEmail;
    private MaterialButton btnConnectGoogleDrive;
    private MaterialButton btnDisconnectGoogleDrive;

    private TextView tvLastBackupTime;
    private TextView tvLastBackupStatus;
    private TextView tvLastBackupSize;
    private MaterialButton btnBackupNow;
    private MaterialButton btnRestoreFromDrive;

    private MaterialButton btnExportBackup;
    private MaterialButton btnImportBackup;

    private MaterialAutoCompleteTextView autoBackupSchedule;

    private TextView tvBackupHistoryCount;
    private RecyclerView recyclerBackupHistory;
    private View layoutEmptyBackupHistory;

    private BackupHistoryAdapter adapter;
    private BackupRepository repository;
    private BackupPreferences preferences;
    private BackupManager backupManager;

    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private static final SimpleDateFormat dateFormat = new SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault());

    private File pendingExportZipFile;

    // Account Picker Launcher for Google Identity / Authorization
    private final ActivityResultLauncher<Intent> accountPickerLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    String accountName = result.getData().getStringExtra(AccountManager.KEY_ACCOUNT_NAME);
                    if (accountName != null && !accountName.trim().isEmpty()) {
                        preferences.setGoogleDriveConnected(true);
                        preferences.setGoogleAccountEmail(accountName.trim());
                        updateDriveStatusUI();
                        Toast.makeText(this, getString(R.string.google_drive_connected_msg, accountName.trim()), Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(this, R.string.signin_cancelled, Toast.LENGTH_SHORT).show();
                }
            });

    // Export Launcher (SAF Create Document)
    private final ActivityResultLauncher<String> exportLauncher =
            registerForActivityResult(new ActivityResultContracts.CreateDocument("application/zip"), uri -> {
                if (uri != null && pendingExportZipFile != null) {
                    executeExportToUri(uri, pendingExportZipFile);
                }
            });

    // Import Launcher (SAF Open Document)
    private final ActivityResultLauncher<String[]> importLauncher =
            registerForActivityResult(new ActivityResultContracts.OpenDocument(), uri -> {
                if (uri != null) {
                    handleImportedBackupUri(uri);
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_backup);

        initializeViews();

        setupToolbar();

        repository = new BackupRepository(getApplicationContext());
        preferences = new BackupPreferences(getApplicationContext());
        backupManager = new BackupManager(getApplicationContext());

        setupRecyclerView();

        setupScheduleDropdown();

        setupListeners();

        updateDriveStatusUI();

        loadOverviewData();

        loadHistory();
    }

    private void initializeViews() {
        tvDriveStatusBadge = findViewById(R.id.tvDriveStatusBadge);
        tvDriveAccountEmail = findViewById(R.id.tvDriveAccountEmail);
        btnConnectGoogleDrive = findViewById(R.id.btnConnectGoogleDrive);
        btnDisconnectGoogleDrive = findViewById(R.id.btnDisconnectGoogleDrive);

        tvLastBackupTime = findViewById(R.id.tvLastBackupTime);
        tvLastBackupStatus = findViewById(R.id.tvLastBackupStatus);
        tvLastBackupSize = findViewById(R.id.tvLastBackupSize);
        btnBackupNow = findViewById(R.id.btnBackupNow);
        btnRestoreFromDrive = findViewById(R.id.btnRestoreFromDrive);

        btnExportBackup = findViewById(R.id.btnExportBackup);
        btnImportBackup = findViewById(R.id.btnImportBackup);

        autoBackupSchedule = findViewById(R.id.autoBackupSchedule);

        tvBackupHistoryCount = findViewById(R.id.tvBackupHistoryCount);
        recyclerBackupHistory = findViewById(R.id.recyclerBackupHistory);
        layoutEmptyBackupHistory = findViewById(R.id.layoutEmptyBackupHistory);
    }

    private void setupToolbar() {
        MaterialToolbar toolbar = findViewById(R.id.toolbarBackup);
        toolbar.setNavigationOnClickListener(v -> finish());
    }

    private void setupRecyclerView() {
        adapter = new BackupHistoryAdapter();
        recyclerBackupHistory.setLayoutManager(new LinearLayoutManager(this));
        recyclerBackupHistory.setAdapter(adapter);
    }

    private void setupScheduleDropdown() {
        String[] scheduleKeys = {
                BackupPreferences.SCHEDULE_OFF,
                BackupPreferences.SCHEDULE_DAILY,
                BackupPreferences.SCHEDULE_7_DAYS,
                BackupPreferences.SCHEDULE_15_DAYS,
                BackupPreferences.SCHEDULE_30_DAYS
        };
        String[] scheduleLabels = {
                getString(R.string.schedule_off),
                getString(R.string.schedule_daily),
                getString(R.string.schedule_7_days),
                getString(R.string.schedule_15_days),
                getString(R.string.schedule_30_days)
        };

        ArrayAdapter<String> scheduleAdapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_dropdown_item_1line,
                scheduleLabels
        );
        autoBackupSchedule.setAdapter(scheduleAdapter);

        String currentSchedule = preferences.getBackupSchedule();
        int selectedIndex = 1; // Default DAILY
        for (int i = 0; i < scheduleKeys.length; i++) {
            if (scheduleKeys[i].equalsIgnoreCase(currentSchedule)) {
                selectedIndex = i;
                break;
            }
        }
        autoBackupSchedule.setText(scheduleLabels[selectedIndex], false);

        autoBackupSchedule.setOnItemClickListener((parent, view, position, id) -> {
            String selectedKey = scheduleKeys[position];
            preferences.setBackupSchedule(selectedKey);
            BackupScheduler.updateSchedule(BackupActivity.this, selectedKey);
            Toast.makeText(BackupActivity.this, getString(R.string.schedule_updated_msg, scheduleLabels[position]), Toast.LENGTH_SHORT).show();
        });
    }

    private void setupListeners() {
        btnConnectGoogleDrive.setOnClickListener(v -> {
            Intent chooseIntent = GoogleDriveService.getAccountPickerIntent(preferences.getGoogleAccountEmail());
            accountPickerLauncher.launch(chooseIntent);
        });

        btnDisconnectGoogleDrive.setOnClickListener(v -> {
            preferences.setGoogleDriveConnected(false);
            preferences.setGoogleAccountEmail("");
            updateDriveStatusUI();
            Toast.makeText(BackupActivity.this, R.string.google_drive_disconnected_msg, Toast.LENGTH_SHORT).show();
        });

        btnBackupNow.setOnClickListener(v -> performManualBackup());

        btnRestoreFromDrive.setOnClickListener(v -> performDriveRestore());

        btnExportBackup.setOnClickListener(v -> performExportBackup());

        btnImportBackup.setOnClickListener(v -> importLauncher.launch(new String[]{"application/zip", "*/*"}));
    }

    private void updateDriveStatusUI() {
        String savedEmail = preferences.getGoogleAccountEmail();
        boolean isConnected = preferences.isGoogleDriveConnected() && savedEmail != null && !savedEmail.trim().isEmpty();

        if (isConnected) {
            tvDriveStatusBadge.setText(R.string.google_drive_connected);
            tvDriveStatusBadge.setTextColor(Color.parseColor("#2E7D32"));
            tvDriveAccountEmail.setVisibility(View.VISIBLE);
            tvDriveAccountEmail.setText(getString(R.string.prefix_account, savedEmail.trim()));
            btnConnectGoogleDrive.setVisibility(View.GONE);
            btnDisconnectGoogleDrive.setVisibility(View.VISIBLE);
        } else {
            tvDriveStatusBadge.setText(R.string.google_drive_not_connected);
            tvDriveStatusBadge.setTextColor(Color.parseColor("#757575"));
            tvDriveAccountEmail.setVisibility(View.GONE);
            btnConnectGoogleDrive.setVisibility(View.VISIBLE);
            btnDisconnectGoogleDrive.setVisibility(View.GONE);
        }
    }

    private void loadOverviewData() {
        long lastTime = preferences.getLastBackupTime();
        if (lastTime > 0) {
            tvLastBackupTime.setText(dateFormat.format(new Date(lastTime)));
        } else {
            tvLastBackupTime.setText(R.string.no_backup_yet);
        }

        String status = preferences.getLastBackupStatus();
        if ("SUCCESS".equalsIgnoreCase(status)) {
            tvLastBackupStatus.setText(R.string.status_success);
            tvLastBackupStatus.setTextColor(Color.parseColor("#2E7D32"));
        } else if ("LOCAL_ONLY".equalsIgnoreCase(status)) {
            tvLastBackupStatus.setText(R.string.status_local_only);
            tvLastBackupStatus.setTextColor(Color.parseColor("#EF6C00"));
        } else if ("FAILED".equalsIgnoreCase(status)) {
            tvLastBackupStatus.setText(R.string.status_failed);
            tvLastBackupStatus.setTextColor(Color.parseColor("#C62828"));
        } else if (status != null && !status.isEmpty()) {
            tvLastBackupStatus.setText(status);
        } else {
            tvLastBackupStatus.setText(R.string.not_set);
        }

        long sizeBytes = preferences.getLastBackupSizeBytes();
        tvLastBackupSize.setText(getString(R.string.prefix_backup_size, BackupHistoryAdapter.formatFileSize(sizeBytes)));
    }

    private void loadHistory() {
        repository.getAllHistory(new BackupRepository.DatabaseCallback<List<BackupHistory>>() {
            @Override
            public void onSuccess(List<BackupHistory> list) {
                runOnUiThread(() -> {
                    int count = list != null ? list.size() : 0;
                    tvBackupHistoryCount.setText(count == 1 ? getString(R.string.count_records_singular, count) : getString(R.string.count_records_plural, count));
                    if (count == 0) {
                        recyclerBackupHistory.setVisibility(View.GONE);
                        layoutEmptyBackupHistory.setVisibility(View.VISIBLE);
                        adapter.setHistory(null);
                    } else {
                        recyclerBackupHistory.setVisibility(View.VISIBLE);
                        layoutEmptyBackupHistory.setVisibility(View.GONE);
                        adapter.setHistory(list);
                    }
                });
            }

            @Override
            public void onError(Exception exception) {
            }
        });
    }

    private void performManualBackup() {
        ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setTitle(R.string.backup_now);
        progressDialog.setMessage(getString(R.string.backup_in_progress));
        progressDialog.setCancelable(false);
        progressDialog.show();

        executor.execute(() -> {
            long startTime = System.currentTimeMillis();
            BackupHistory history = new BackupHistory(
                    "",
                    BackupManager.BACKUP_FILE_NAME,
                    "",
                    startTime,
                    0,
                    0,
                    "IN_PROGRESS",
                    "MANUAL",
                    "",
                    "",
                    "1.0",
                    0
            );

            repository.insertHistory(history, new BackupRepository.DatabaseCallback<Long>() {
                @Override
                public void onSuccess(Long id) {
                    history.setId(id);
                }

                @Override
                public void onError(Exception exception) {
                }
            });

            try {
                // 1. Create local validated package (includes SQLite checkpoint & snapshot verification)
                BackupManager.BackupResult result = backupManager.createBackupPackage((message, percentage) -> {
                    runOnUiThread(() -> progressDialog.setMessage(message));
                });

                String driveFileId = null;
                boolean isDriveConnected = preferences.isGoogleDriveConnected() &&
                        preferences.getGoogleAccountEmail() != null &&
                        !preferences.getGoogleAccountEmail().trim().isEmpty();

                Exception driveUploadError = null;

                if (isDriveConnected) {
                    runOnUiThread(() -> progressDialog.setMessage(getString(R.string.uploading_to_drive)));
                    try {
                        GoogleDriveService driveService = new GoogleDriveService(BackupActivity.this, preferences.getGoogleAccountEmail());
                        com.google.api.services.drive.model.File verifiedFile = driveService.uploadAndVerifyBackupFile(
                                result.zipFile,
                                result.zipFile.getName(),
                                result.manifest,
                                result.archiveChecksum
                        );
                        if (verifiedFile != null && verifiedFile.getId() != null) {
                            driveFileId = verifiedFile.getId();
                        } else {
                            throw new Exception("Drive upload verification failed: empty file ID.");
                        }
                    } catch (Exception ex) {
                        driveUploadError = ex;
                    }
                }

                long endTime = System.currentTimeMillis();
                long sizeBytes = result.zipFile.length();

                history.setBackupId(result.manifest.getBackupId());
                history.setFileName(result.zipFile.getName());
                history.setDriveFileId(driveFileId);
                history.setCompletedAt(endTime);
                history.setSizeBytes(sizeBytes);
                history.setChecksum(result.archiveChecksum);
                history.setDocumentCount(result.manifest.getDocumentCount());

                if (driveUploadError != null) {
                    // Local package succeeded, but Drive upload failed
                    history.setStatus("FAILED");
                    history.setErrorMessage(driveUploadError.getMessage());
                    repository.updateHistory(history, null);

                    preferences.setLastBackupTime(endTime);
                    preferences.setLastBackupStatus("FAILED");
                    preferences.setLastBackupSizeBytes(sizeBytes);
                    preferences.setLastBackupType("MANUAL");

                    Exception finalError = driveUploadError;
                    runOnUiThread(() -> {
                        progressDialog.dismiss();
                        new MaterialAlertDialogBuilder(BackupActivity.this)
                                .setTitle(R.string.status_failed)
                                .setMessage(getString(R.string.local_backup_created_drive_failed, finalError.getMessage()))
                                .setPositiveButton(R.string.close, null)
                                .show();
                        loadOverviewData();
                        loadHistory();
                    });
                } else if (isDriveConnected) {
                    // Full Cloud Success
                    history.setStatus("SUCCESS");
                    history.setErrorMessage(null);
                    repository.updateHistory(history, null);

                    preferences.setLastBackupTime(endTime);
                    preferences.setLastBackupStatus("SUCCESS");
                    preferences.setLastBackupSizeBytes(sizeBytes);
                    preferences.setLastBackupType("MANUAL");
                    if (driveFileId != null) preferences.setLastDriveFileId(driveFileId);

                    runOnUiThread(() -> {
                        progressDialog.dismiss();
                        Toast.makeText(BackupActivity.this, R.string.cloud_backup_success_msg, Toast.LENGTH_LONG).show();
                        loadOverviewData();
                        loadHistory();
                    });
                } else {
                    // Local-Only Backup
                    history.setStatus("LOCAL_ONLY");
                    history.setErrorMessage(null);
                    repository.updateHistory(history, null);

                    preferences.setLastBackupTime(endTime);
                    preferences.setLastBackupStatus("LOCAL_ONLY");
                    preferences.setLastBackupSizeBytes(sizeBytes);
                    preferences.setLastBackupType("MANUAL");

                    runOnUiThread(() -> {
                        progressDialog.dismiss();
                        Toast.makeText(BackupActivity.this, R.string.local_backup_success_no_drive, Toast.LENGTH_LONG).show();
                        loadOverviewData();
                        loadHistory();
                    });
                }
            } catch (Exception e) {
                history.setCompletedAt(System.currentTimeMillis());
                history.setStatus("FAILED");
                history.setErrorMessage(e.getMessage());
                repository.updateHistory(history, null);

                preferences.setLastBackupTime(System.currentTimeMillis());
                preferences.setLastBackupStatus("FAILED");

                runOnUiThread(() -> {
                    progressDialog.dismiss();
                    String msg = e.getMessage() != null && e.getMessage().contains("snapshot validation failed")
                            ? getString(R.string.snapshot_mismatch_error)
                            : getString(R.string.backup_failed_msg);
                    new MaterialAlertDialogBuilder(BackupActivity.this)
                            .setTitle(R.string.status_failed)
                            .setMessage(msg + "\n\n" + e.getMessage())
                            .setPositiveButton(R.string.close, null)
                            .show();
                    loadOverviewData();
                    loadHistory();
                });
            }
        });
    }

    private void performDriveRestore() {
        if (!preferences.isGoogleDriveConnected() || preferences.getGoogleAccountEmail().isEmpty()) {
            Toast.makeText(this, R.string.connect_drive_first_msg, Toast.LENGTH_LONG).show();
            return;
        }

        ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setMessage(getString(R.string.searching_drive_backups));
        progressDialog.setCancelable(false);
        progressDialog.show();

        executor.execute(() -> {
            try {
                GoogleDriveService driveService = new GoogleDriveService(BackupActivity.this, preferences.getGoogleAccountEmail());
                List<com.google.api.services.drive.model.File> backups = driveService.listBackups();

                if (backups == null || backups.isEmpty()) {
                    runOnUiThread(() -> {
                        progressDialog.dismiss();
                        Toast.makeText(BackupActivity.this, R.string.no_drive_backups_found, Toast.LENGTH_LONG).show();
                    });
                    return;
                }

                runOnUiThread(progressDialog::dismiss);

                if (backups.size() == 1) {
                    downloadAndConfirmRestore(driveService, backups.get(0));
                } else {
                    runOnUiThread(() -> showDriveBackupSelectionDialog(driveService, backups));
                }
            } catch (Exception e) {
                runOnUiThread(() -> {
                    progressDialog.dismiss();
                    Toast.makeText(BackupActivity.this, getString(R.string.download_backup_failed_msg, e.getMessage()), Toast.LENGTH_LONG).show();
                });
            }
        });
    }

    private void showDriveBackupSelectionDialog(GoogleDriveService driveService, List<com.google.api.services.drive.model.File> backups) {
        String[] displayItems = new String[backups.size()];
        for (int i = 0; i < backups.size(); i++) {
            com.google.api.services.drive.model.File f = backups.get(i);
            long size = f.getSize() != null ? f.getSize() : 0;
            String formattedSize = BackupHistoryAdapter.formatFileSize(size);

            long timestamp = 0;
            String propSummary = "";
            String tenantCountStr = "";

            Map<String, String> props = f.getAppProperties();
            if (props != null) {
                String ts = props.get("createdAt");
                if (ts != null) {
                    try {
                        timestamp = Long.parseLong(ts);
                    } catch (Exception ignored) {
                    }
                }
                propSummary = props.get("propertySummary");
                tenantCountStr = props.get("tenantCount");
            }
            if (timestamp == 0 && f.getCreatedTime() != null) {
                timestamp = f.getCreatedTime().getValue();
            }

            String dateFormatted = timestamp > 0 ? dateFormat.format(new Date(timestamp)) : "Unknown Date";

            StringBuilder sb = new StringBuilder();
            sb.append("").append(dateFormatted);
            if (propSummary != null && !propSummary.isEmpty()) {
                sb.append("\n ").append(propSummary);
            }
            if (tenantCountStr != null && !tenantCountStr.isEmpty()) {
                sb.append(" • ").append(tenantCountStr).append(" Tenants");
            }
            sb.append("\n ").append(formattedSize);

            displayItems[i] = sb.toString();
        }

        new MaterialAlertDialogBuilder(this)
                .setTitle(R.string.select_backup_to_restore)
                .setItems(displayItems, (dialog, which) -> {
                    com.google.api.services.drive.model.File selectedBackup = backups.get(which);
                    downloadAndConfirmRestore(driveService, selectedBackup);
                })
                .setNegativeButton(R.string.cancel, null)
                .show();
    }

    private void downloadAndConfirmRestore(GoogleDriveService driveService, com.google.api.services.drive.model.File driveFile) {
        ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setMessage(getString(R.string.downloading_backup_drive));
        progressDialog.setCancelable(false);
        progressDialog.show();

        executor.execute(() -> {
            try {
                File tempDownloadFile = new File(getCacheDir(), "drive_restore_download_" + System.currentTimeMillis() + ".zip");
                driveService.downloadBackupFile(driveFile.getId(), tempDownloadFile);

                BackupManager.RestorePreview preview = backupManager.prepareRestorePreview(tempDownloadFile);

                runOnUiThread(() -> {
                    progressDialog.dismiss();
                    showRestorePreviewDialog(preview, "GOOGLE_DRIVE");
                });
            } catch (Exception e) {
                runOnUiThread(() -> {
                    progressDialog.dismiss();
                    Toast.makeText(BackupActivity.this, getString(R.string.download_backup_failed_msg, e.getMessage()), Toast.LENGTH_LONG).show();
                });
            }
        });
    }

    private void performExportBackup() {
        ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setMessage(getString(R.string.backup_in_progress));
        progressDialog.setCancelable(false);
        progressDialog.show();

        executor.execute(() -> {
            try {
                BackupManager.BackupResult result = backupManager.createBackupPackage((message, percentage) -> {
                    runOnUiThread(() -> progressDialog.setMessage(message));
                });
                pendingExportZipFile = result.zipFile;

                runOnUiThread(() -> {
                    progressDialog.dismiss();
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US);
                    String exportName = "RobHouseRental_Backup_" + sdf.format(new Date()) + ".zip";
                    exportLauncher.launch(exportName);
                });
            } catch (Exception e) {
                runOnUiThread(() -> {
                    progressDialog.dismiss();
                    Toast.makeText(BackupActivity.this, getString(R.string.export_prep_failed_msg, e.getMessage()), Toast.LENGTH_SHORT).show();
                });
            }
        });
    }

    private void executeExportToUri(Uri targetUri, File zipFile) {
        executor.execute(() -> {
            try {
                backupManager.exportBackupToUri(zipFile, targetUri);

                BackupHistory history = new BackupHistory(
                        "",
                        zipFile.getName(),
                        "",
                        System.currentTimeMillis(),
                        System.currentTimeMillis(),
                        zipFile.length(),
                        "SUCCESS",
                        "EXPORT",
                        null,
                        BackupManager.calculateSha256(zipFile),
                        "1.0",
                        0
                );
                repository.insertHistory(history, null);

                runOnUiThread(() -> {
                    Toast.makeText(BackupActivity.this, R.string.export_success_msg, Toast.LENGTH_LONG).show();
                    loadHistory();
                });
            } catch (Exception e) {
                runOnUiThread(() -> Toast.makeText(BackupActivity.this, R.string.export_save_failed_msg, Toast.LENGTH_SHORT).show());
            }
        });
    }

    private void handleImportedBackupUri(Uri sourceUri) {
        ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setMessage(getString(R.string.reading_imported_backup));
        progressDialog.setCancelable(false);
        progressDialog.show();

        executor.execute(() -> {
            try {
                File importedZip = backupManager.importBackupFromUri(sourceUri);
                BackupManager.RestorePreview preview = backupManager.prepareRestorePreview(importedZip);

                runOnUiThread(() -> {
                    progressDialog.dismiss();
                    showRestorePreviewDialog(preview, "IMPORT");
                });
            } catch (Exception e) {
                runOnUiThread(() -> {
                    progressDialog.dismiss();
                    Toast.makeText(BackupActivity.this, getString(R.string.invalid_backup_file_msg, e.getMessage()), Toast.LENGTH_LONG).show();
                });
            }
        });
    }

    private void showRestorePreviewDialog(BackupManager.RestorePreview preview, String source) {
        String backupDate = dateFormat.format(new Date(preview.createdAt));
        String backupSize = BackupHistoryAdapter.formatFileSize(preview.backupSizeBytes);

        StringBuilder sb = new StringBuilder();
        sb.append("").append(backupDate).append("\n");
        sb.append("").append(backupSize).append("\n\n");

        sb.append(getString(R.string.restore_preview_properties, preview.propertyCount)).append("\n");
        if (preview.propertyNames != null && !preview.propertyNames.isEmpty()) {
            for (String pName : preview.propertyNames) {
                sb.append(" • ").append(pName).append("\n");
            }
        } else {
            sb.append(" (None)\n");
        }

        sb.append("\n ").append(getString(R.string.restore_preview_tenants, preview.tenantCount)).append("\n");
        sb.append("").append(getString(R.string.restore_preview_units, preview.unitCount)).append("\n");
        sb.append("").append(getString(R.string.restore_preview_documents, preview.documentCount)).append("\n\n");
        sb.append("").append(getString(R.string.restore_preview_warning));

        new MaterialAlertDialogBuilder(this)
                .setTitle(R.string.restore_preview_title)
                .setMessage(sb.toString())
                .setPositiveButton(R.string.restore_backup_button, (dialog, which) -> {
                    executeSafeRestore(preview.zipFile, preview.manifest, source);
                })
                .setNegativeButton(R.string.cancel, null)
                .show();
    }

    private void executeSafeRestore(File zipFile, BackupManifest manifest, String source) {
        ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setTitle(R.string.restoring_in_progress);
        progressDialog.setMessage(getString(R.string.restoring_data_documents));
        progressDialog.setCancelable(false);
        progressDialog.show();

        executor.execute(() -> {
            backupManager.restoreFromPackage(zipFile, (message, percentage) -> {
                runOnUiThread(() -> progressDialog.setMessage(message));
            }, new BackupManager.RestoreCallback() {
                @Override
                public void onRestoreSuccess(BackupManifest restoredManifest) {
                    BackupHistory history = new BackupHistory(
                            restoredManifest.getBackupId(),
                            zipFile.getName(),
                            "",
                            System.currentTimeMillis(),
                            System.currentTimeMillis(),
                            zipFile.length(),
                            "RESTORED",
                            "RESTORE",
                            null,
                            restoredManifest.getDatabaseChecksum(),
                            restoredManifest.getAppVersion(),
                            restoredManifest.getDocumentCount()
                    );
                    repository.insertHistory(history, null);

                    runOnUiThread(() -> {
                        progressDialog.dismiss();
                        Toast.makeText(BackupActivity.this, R.string.restore_success_msg, Toast.LENGTH_LONG).show();
                        loadOverviewData();
                        loadHistory();
                    });
                }

                @Override
                public void onRestoreError(String error) {
                    runOnUiThread(() -> {
                        progressDialog.dismiss();
                        new MaterialAlertDialogBuilder(BackupActivity.this)
                                .setTitle(R.string.status_failed)
                                .setMessage(error + "\n\n" + getString(R.string.restore_rollback_success_msg))
                                .setPositiveButton(R.string.close, null)
                                .show();
                        loadOverviewData();
                        loadHistory();
                    });
                }
            });
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateDriveStatusUI();
        loadOverviewData();
        loadHistory();
    }
}

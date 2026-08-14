package com.rob.houserental;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.rob.houserental.adapter.ReminderAdapter;
import com.rob.houserental.model.Reminder;
import com.rob.houserental.repository.ReminderRepository;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class ReminderDetailsActivity extends AppCompatActivity {

    private static final String TAG = "ReminderDetailsActivity";

    private MaterialToolbar toolbar;
    private TextView tvTitle;
    private TextView tvDescription;
    private TextView tvDateTime;
    private TextView tvRepeat;
    private TextView tvType;
    private TextView tvStatus;
    private MaterialButton btnEdit;
    private MaterialButton btnToggleComplete;
    private MaterialButton btnToggleEnable;
    private MaterialButton btnSnooze;
    private MaterialButton btnDelete;

    private ReminderRepository reminderRepository;
    private long reminderId = -1;
    private Reminder currentReminder;

    private static final SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());
    private static final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reminder_details);

        reminderId = getIntent().getLongExtra("reminder_id", -1);
        if (reminderId <= 0) {
            finish();
            return;
        }

        reminderRepository = new ReminderRepository(this);

        initializeViews();
        setupToolbar();
        setupListeners();

        loadReminder();
    }

    private void initializeViews() {
        toolbar = findViewById(R.id.toolbarReminderDetails);
        tvTitle = findViewById(R.id.tvReminderDetailsTitle);
        tvDescription = findViewById(R.id.tvReminderDetailsDescription);
        tvDateTime = findViewById(R.id.tvReminderDetailsDateTime);
        tvRepeat = findViewById(R.id.tvReminderDetailsRepeat);
        tvType = findViewById(R.id.tvReminderDetailsType);
        tvStatus = findViewById(R.id.tvReminderDetailsStatus);
        btnEdit = findViewById(R.id.btnEditReminder);
        btnToggleComplete = findViewById(R.id.btnToggleComplete);
        btnToggleEnable = findViewById(R.id.btnToggleEnable);
        btnSnooze = findViewById(R.id.btnSnoozeReminder);
        btnDelete = findViewById(R.id.btnDeleteReminder);
    }

    private void setupToolbar() {
        if (toolbar != null) {
            toolbar.setNavigationOnClickListener(v -> finish());
        }
    }

    private void setupListeners() {
        if (btnEdit != null) {
            btnEdit.setOnClickListener(v -> {
                Intent intent = new Intent(ReminderDetailsActivity.this, AddReminderActivity.class);
                intent.putExtra("reminder_id", reminderId);
                startActivity(intent);
            });
        }
        if (btnToggleComplete != null) {
            btnToggleComplete.setOnClickListener(v -> toggleCompleted());
        }
        if (btnToggleEnable != null) {
            btnToggleEnable.setOnClickListener(v -> toggleEnabled());
        }
        if (btnSnooze != null) {
            btnSnooze.setOnClickListener(v -> showSnoozeOptions());
        }
        if (btnDelete != null) {
            btnDelete.setOnClickListener(v -> confirmDelete());
        }
    }

    private void loadReminder() {
        reminderRepository.getById(reminderId, new ReminderRepository.DatabaseCallback<Reminder>() {
            @Override
            public void onSuccess(Reminder reminder) {
                currentReminder = reminder;
                runOnUiThread(() -> {
                    if (reminder == null) {
                        finish();
                        return;
                    }

                    if (tvTitle != null) tvTitle.setText(reminder.getTitle() != null ? reminder.getTitle() : "");
                    if (tvDescription != null) {
                        if (reminder.getDescription() != null && !reminder.getDescription().isEmpty()) {
                            tvDescription.setText(reminder.getDescription());
                            tvDescription.setVisibility(android.view.View.VISIBLE);
                        } else {
                            tvDescription.setVisibility(android.view.View.GONE);
                        }
                    }

                    String dt = getString(R.string.date) + ": " + reminder.getReminderDate() + " | " + getString(R.string.hint_time) + ": " + reminder.getReminderTime();
                    if (tvDateTime != null) tvDateTime.setText(dt);

                    String repeatStr = getString(R.string.hint_repeat) + ": " + reminder.getRepeatType();
                    if ("CUSTOM".equalsIgnoreCase(reminder.getRepeatType())) {
                        repeatStr += " (" + reminder.getRepeatInterval() + getString(R.string.minutes) + ")";
                    }
                    if (tvRepeat != null) tvRepeat.setText(repeatStr);

                    if (tvType != null) {
                        String typeStr = getString(R.string.reminder) + getString(R.string.status_label) + ": " + ReminderAdapter.getTypeDisplay(ReminderDetailsActivity.this, reminder.getReminderType());
                        tvType.setText(typeStr);
                        resolveAndDisplayRelatedEntityName(reminder);
                    }

                    if (tvStatus != null) {
                        if (reminder.isCompleted()) {
                            tvStatus.setText(R.string.reminder_state_completed);
                        } else if (!reminder.isEnabled()) {
                            tvStatus.setText(R.string.disabled);
                        } else {
                            tvStatus.setText(R.string.active);
                        }
                    }

                    if (btnToggleComplete != null) {
                        btnToggleComplete.setText(reminder.isCompleted() ? R.string.mark_incomplete : R.string.mark_completed);
                    }
                    if (btnToggleEnable != null) {
                        btnToggleEnable.setText(reminder.isEnabled() ? R.string.disabled : R.string.active);
                    }
                });
            }

            @Override
            public void onError(Exception exception) {
                Log.e(TAG, "Error loading reminder details", exception);
            }
        });
    }

    private void toggleCompleted() {
        if (currentReminder == null) return;
        boolean newCompleted = !currentReminder.isCompleted();
        reminderRepository.setCompletedState(reminderId, newCompleted, new ReminderRepository.DatabaseCallback<Void>() {
            @Override
            public void onSuccess(Void result) {
                runOnUiThread(() -> loadReminder());
            }

            @Override
            public void onError(Exception exception) {
                Log.e(TAG, "Error updating completion state", exception);
            }
        });
    }

    private void toggleEnabled() {
        if (currentReminder == null) return;
        currentReminder.setEnabled(!currentReminder.isEnabled());
        reminderRepository.update(currentReminder, new ReminderRepository.DatabaseCallback<Void>() {
            @Override
            public void onSuccess(Void result) {
                runOnUiThread(() -> loadReminder());
            }

            @Override
            public void onError(Exception exception) {
                Log.e(TAG, "Error updating enabled state", exception);
            }
        });
    }

    private void showSnoozeOptions() {
        if (currentReminder == null) return;
        String[] options = {"10 " + getString(R.string.minutes), "30 " + getString(R.string.minutes), "1 " + getString(R.string.hour), getString(R.string.tomorrow)};

        new MaterialAlertDialogBuilder(this)
                .setTitle(R.string.snooze_reminder)
                .setItems(options, (dialog, which) -> {
                    Calendar cal = Calendar.getInstance();
                    if (which == 0) cal.add(Calendar.MINUTE, 10);
                    else if (which == 1) cal.add(Calendar.MINUTE, 30);
                    else if (which == 2) cal.add(Calendar.HOUR_OF_DAY, 1);
                    else if (which == 3) cal.add(Calendar.DAY_OF_YEAR, 1);

                    currentReminder.setReminderDate(dateFormat.format(cal.getTime()));
                    currentReminder.setReminderTime(timeFormat.format(cal.getTime()));
                    currentReminder.setCompleted(false);
                    currentReminder.setEnabled(true);

                    reminderRepository.update(currentReminder, new ReminderRepository.DatabaseCallback<Void>() {
                        @Override
                        public void onSuccess(Void result) {
                            runOnUiThread(() -> {
                                Toast.makeText(ReminderDetailsActivity.this, R.string.snoozed_success, Toast.LENGTH_SHORT).show();
                                loadReminder();
                            });
                        }

                        @Override
                        public void onError(Exception exception) {
                            Log.e(TAG, "Error snoozing reminder", exception);
                        }
                    });
                })
                .setNegativeButton(R.string.cancel, null)
                .show();
    }

    private void confirmDelete() {
        if (currentReminder == null) return;
        new MaterialAlertDialogBuilder(this)
                .setTitle(R.string.delete_reminder_title)
                .setMessage(R.string.delete_reminder_confirm)
                .setPositiveButton(R.string.delete, (dialog, which) -> {
                    reminderRepository.delete(currentReminder, new ReminderRepository.DatabaseCallback<Void>() {
                        @Override
                        public void onSuccess(Void result) {
                            runOnUiThread(() -> {
                                Toast.makeText(ReminderDetailsActivity.this, R.string.deleted_success, Toast.LENGTH_SHORT).show();
                                finish();
                            });
                        }

                        @Override
                        public void onError(Exception exception) {
                            Log.e(TAG, "Error deleting reminder", exception);
                        }
                    });
                })
                .setNegativeButton(R.string.cancel, null)
                .show();
    }

    private void resolveAndDisplayRelatedEntityName(Reminder reminder) {
        if (reminder == null || reminder.getRelatedEntityType() == null || "NONE".equalsIgnoreCase(reminder.getRelatedEntityType()) || reminder.getRelatedEntityId() <= 0) {
            return;
        }

        java.util.concurrent.Executors.newSingleThreadExecutor().execute(() -> {
            com.rob.houserental.data.AppDatabase db = com.rob.houserental.data.AppDatabase.getInstance(getApplicationContext());
            String entityType = reminder.getRelatedEntityType().toUpperCase();
            long entityId = reminder.getRelatedEntityId();
            String resolvedName = "";

            try {
                if ("PROPERTY".equals(entityType)) {
                    com.rob.houserental.model.Property p = db.propertyDao().getPropertyById(entityId);
                    if (p != null) resolvedName = p.getName();
                } else if ("UNIT".equals(entityType)) {
                    com.rob.houserental.model.Unit u = db.unitDao().getUnitById(entityId);
                    if (u != null) resolvedName = "Unit " + u.getUnitNumber();
                } else if ("TENANT".equals(entityType)) {
                    com.rob.houserental.model.Tenant t = db.tenantDao().getTenantById(entityId);
                    if (t != null) resolvedName = t.getFullName();
                } else if ("MAINTENANCE".equals(entityType)) {
                    com.rob.houserental.model.MaintenanceRecord m = db.maintenanceDao().getById(entityId);
                    if (m != null) resolvedName = m.getTitle();
                } else if ("DOCUMENT".equals(entityType)) {
                    com.rob.houserental.model.AppDocument d = db.appDocumentDao().getDocumentById(entityId);
                    if (d != null) resolvedName = d.getDisplayName();
                }
            } catch (Exception e) {
                Log.e(TAG, "Error resolving related entity name", e);
            }

            final String finalResolved = resolvedName;
            runOnUiThread(() -> {
                if (tvType != null && !finalResolved.isEmpty()) {
                    String currentText = tvType.getText().toString();
                    tvType.setText(currentText + "\n Related To: " + entityType + ": " + finalResolved + " (#" + entityId + ")");
                }
            });
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadReminder();
    }
}

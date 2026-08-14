package com.rob.houserental;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.rob.houserental.adapter.AppDocumentAdapter;
import com.rob.houserental.adapter.MaintenanceAdapter;
import com.rob.houserental.data.AppDatabase;
import com.rob.houserental.model.AppDocument;
import com.rob.houserental.model.Expense;
import com.rob.houserental.model.MaintenanceRecord;
import com.rob.houserental.model.Property;
import com.rob.houserental.model.Unit;
import com.rob.houserental.repository.MaintenanceRepository;
import com.rob.houserental.utils.DocumentOpenUtils;

import java.io.File;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.Executors;

public class MaintenanceDetailsActivity extends AppCompatActivity {

    private static final String TAG = "MaintenanceDetailsActivity";

    private MaterialToolbar toolbar;
    private TextView tvTitle;
    private TextView tvPropertyUnit;
    private TextView tvCategory;
    private TextView tvPriority;
    private TextView tvStatus;
    private TextView tvCosts;
    private TextView tvVendor;
    private TextView tvDates;
    private TextView tvDescription;

    private MaterialButton btnAddDocument;
    private RecyclerView recyclerDocuments;
    private TextView tvEmptyDocuments;

    private MaterialButton btnEdit;
    private MaterialButton btnChangeStatus;
    private MaterialButton btnAddToExpenses;
    private MaterialButton btnDelete;

    private MaintenanceRepository maintenanceRepository;
    private AppDocumentAdapter documentAdapter;

    private long maintenanceId = -1;
    private MaintenanceRecord currentRecord;

    private static final DecimalFormat currencyFormat = new DecimalFormat("#,##,###.##");
    private static final SimpleDateFormat monthFormat = new SimpleDateFormat("yyyy-MM", Locale.getDefault());
    private static final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maintenance_details);

        maintenanceId = getIntent().getLongExtra("maintenance_id", -1);
        if (maintenanceId <= 0) {
            finish();
            return;
        }

        maintenanceRepository = new MaintenanceRepository(this);

        initializeViews();
        setupToolbar();
        setupRecyclerView();
        setupListeners();

        loadMaintenanceRecord();
    }

    private void initializeViews() {
        toolbar = findViewById(R.id.toolbarMaintenanceDetails);
        tvTitle = findViewById(R.id.tvMaintDetailsTitle);
        tvPropertyUnit = findViewById(R.id.tvMaintDetailsPropertyUnit);
        tvCategory = findViewById(R.id.tvMaintDetailsCategory);
        tvPriority = findViewById(R.id.tvMaintDetailsPriority);
        tvStatus = findViewById(R.id.tvMaintDetailsStatus);
        tvCosts = findViewById(R.id.tvMaintDetailsCosts);
        tvVendor = findViewById(R.id.tvMaintDetailsVendor);
        tvDates = findViewById(R.id.tvMaintDetailsDates);
        tvDescription = findViewById(R.id.tvMaintDetailsDescription);

        btnAddDocument = findViewById(R.id.btnAddMaintDocument);
        recyclerDocuments = findViewById(R.id.recyclerMaintDocuments);
        tvEmptyDocuments = findViewById(R.id.tvEmptyMaintDocs);

        btnEdit = findViewById(R.id.btnEditMaintenance);
        btnChangeStatus = findViewById(R.id.btnChangeMaintStatus);
        btnAddToExpenses = findViewById(R.id.btnAddToExpenses);
        btnDelete = findViewById(R.id.btnDeleteMaintenance);
    }

    private void setupToolbar() {
        if (toolbar != null) {
            toolbar.setNavigationOnClickListener(v -> finish());
        }
    }

    private void setupRecyclerView() {
        documentAdapter = new AppDocumentAdapter();
        if (recyclerDocuments != null) {
            recyclerDocuments.setLayoutManager(new LinearLayoutManager(this));
            recyclerDocuments.setAdapter(documentAdapter);
        }

        documentAdapter.setOnDocumentClickListener(new AppDocumentAdapter.OnDocumentClickListener() {
            @Override
            public void onDocumentClick(AppDocument doc) {
                if (doc == null || doc.getFilePath() == null) {
                    Toast.makeText(MaintenanceDetailsActivity.this, R.string.no_compatible_app_error, Toast.LENGTH_SHORT).show();
                    return;
                }
                File file = new File(doc.getFilePath());
                if (!file.exists()) {
                    Toast.makeText(MaintenanceDetailsActivity.this, R.string.no_compatible_app_error, Toast.LENGTH_SHORT).show();
                    return;
                }
                try {
                    DocumentOpenUtils.openDocument(MaintenanceDetailsActivity.this, doc.getFilePath(), doc.getMimeType(), doc.getDisplayName());
                } catch (Exception e) {
                    Log.e(TAG, "Error opening maintenance document", e);
                    Toast.makeText(MaintenanceDetailsActivity.this, R.string.no_compatible_app_error, Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onDeleteClick(AppDocument doc) {
                confirmDeleteDocument(doc);
            }
        });
    }

    private void setupListeners() {
        if (btnEdit != null) {
            btnEdit.setOnClickListener(v -> {
                Intent intent = new Intent(MaintenanceDetailsActivity.this, AddMaintenanceActivity.class);
                intent.putExtra("maintenance_id", maintenanceId);
                startActivity(intent);
            });
        }

        if (btnChangeStatus != null) {
            btnChangeStatus.setOnClickListener(v -> showChangeStatusDialog());
        }

        if (btnAddDocument != null) {
            btnAddDocument.setOnClickListener(v -> {
                if (currentRecord == null) return;
                Intent intent = new Intent(MaintenanceDetailsActivity.this, AddDocumentActivity.class);
                intent.putExtra("document_type", "MAINTENANCE");
                intent.putExtra("related_record_id", maintenanceId);
                intent.putExtra("property_id", currentRecord.getPropertyId());
                if (currentRecord.getUnitId() != null) {
                    intent.putExtra("unit_id", currentRecord.getUnitId().longValue());
                }
                startActivityForResult(intent, 3001);
            });
        }

        if (btnAddToExpenses != null) {
            btnAddToExpenses.setOnClickListener(v -> confirmAddToExpenses());
        }

        if (btnDelete != null) {
            btnDelete.setOnClickListener(v -> confirmDelete());
        }
    }

    private void loadMaintenanceRecord() {
        maintenanceRepository.getById(maintenanceId, new MaintenanceRepository.DatabaseCallback<MaintenanceRecord>() {
            @Override
            public void onSuccess(MaintenanceRecord record) {
                currentRecord = record;
                if (record == null) {
                    runOnUiThread(() -> finish());
                    return;
                }

                AppDatabase db = AppDatabase.getInstance(getApplicationContext());
                Executors.newSingleThreadExecutor().execute(() -> {
                    Property prop = db.propertyDao().getPropertyById(record.getPropertyId());
                    Unit unit = record.getUnitId() != null ? db.unitDao().getUnitById(record.getUnitId()) : null;
                    List<AppDocument> docs = db.appDocumentDao().getDocumentsByMaintenance(maintenanceId);

                    runOnUiThread(() -> {
                        if (tvTitle != null) tvTitle.setText(record.getTitle() != null ? record.getTitle() : "");

                        String propName = prop != null ? prop.getName() : "";
                        String unitName = unit != null ? " • " + getString(R.string.unit_number) + unit.getUnitNumber() : " • " + getString(R.string.property_wide_entire_building);
                        if (tvPropertyUnit != null) tvPropertyUnit.setText(propName + unitName);

                        if (tvCategory != null) tvCategory.setText(getString(R.string.category) + ": " + MaintenanceAdapter.getCategoryDisplay(MaintenanceDetailsActivity.this, record.getCategory()));
                        if (tvPriority != null) tvPriority.setText(getString(R.string.priority) + ": " + MaintenanceAdapter.getPriorityDisplay(MaintenanceDetailsActivity.this, record.getPriority()));
                        if (tvStatus != null) tvStatus.setText(getString(R.string.status_label) + ": " + MaintenanceAdapter.getStatusDisplay(MaintenanceDetailsActivity.this, record.getStatus()));

                        String curr = getString(R.string.currency_symbol);
                        String costStr = getString(R.string.estimated_cost) + ": " + curr + currencyFormat.format(record.getEstimatedCost()) +
                                " | " + getString(R.string.actual_cost) + ": " + curr + currencyFormat.format(record.getActualCost());
                        if (tvCosts != null) tvCosts.setText(costStr);

                        if (tvVendor != null) {
                            if (record.getVendorName() != null && !record.getVendorName().isEmpty()) {
                                String vendorStr = getString(R.string.vendor_name) + ": " + record.getVendorName();
                                if (record.getVendorPhone() != null && !record.getVendorPhone().isEmpty()) {
                                    vendorStr += " ( " + record.getVendorPhone() + ")";
                                    tvVendor.setOnClickListener(v -> {
                                        Intent intent = new Intent(Intent.ACTION_DIAL, Uri.parse("tel:" + record.getVendorPhone()));
                                        startActivity(intent);
                                    });
                                }
                                tvVendor.setText(vendorStr);
                                tvVendor.setVisibility(View.VISIBLE);
                            } else {
                                tvVendor.setVisibility(View.GONE);
                            }
                        }

                        if (tvDates != null) {
                            String datesStr = getString(R.string.scheduled_date_optional) + ": " + (record.getScheduledDate() != null && !record.getScheduledDate().isEmpty() ? record.getScheduledDate() : "-");
                            if (record.getCompletedDate() != null && !record.getCompletedDate().isEmpty()) {
                                datesStr += " | " + getString(R.string.status_maint_completed) + ": " + record.getCompletedDate();
                            }
                            tvDates.setText(datesStr);
                        }

                        if (tvDescription != null) {
                            if (record.getDescription() != null && !record.getDescription().isEmpty()) {
                                tvDescription.setText(record.getDescription());
                                tvDescription.setVisibility(View.VISIBLE);
                            } else {
                                tvDescription.setVisibility(View.GONE);
                            }
                        }

                        // Expense Conversion Status
                        if (btnAddToExpenses != null) {
                            if (record.getExpenseId() != null && record.getExpenseId() > 0) {
                                btnAddToExpenses.setText(R.string.added_to_expenses_already);
                                btnAddToExpenses.setEnabled(false);
                            } else {
                                btnAddToExpenses.setText(R.string.add_to_expenses);
                                btnAddToExpenses.setEnabled(true);
                            }
                        }

                        // Documents List
                        if (docs == null || docs.isEmpty()) {
                            if (tvEmptyDocuments != null) tvEmptyDocuments.setVisibility(View.VISIBLE);
                            if (recyclerDocuments != null) recyclerDocuments.setVisibility(View.GONE);
                            if (documentAdapter != null) documentAdapter.setDocuments(null);
                        } else {
                            if (tvEmptyDocuments != null) tvEmptyDocuments.setVisibility(View.GONE);
                            if (recyclerDocuments != null) recyclerDocuments.setVisibility(View.VISIBLE);
                            if (documentAdapter != null) documentAdapter.setDocuments(docs);
                        }
                    });
                });
            }

            @Override
            public void onError(Exception exception) {
                Log.e(TAG, "Error loading maintenance details", exception);
            }
        });
    }

    private void showChangeStatusDialog() {
        if (currentRecord == null) return;

        String[] displays = {
                getString(R.string.status_maint_open),
                getString(R.string.status_maint_scheduled),
                getString(R.string.status_maint_in_progress),
                getString(R.string.status_maint_completed),
                getString(R.string.status_maint_cancelled)
        };
        String[] keys = {"OPEN", "SCHEDULED", "IN_PROGRESS", "COMPLETED", "CANCELLED"};

        new MaterialAlertDialogBuilder(this)
                .setTitle(R.string.change_status)
                .setItems(displays, (dialog, which) -> {
                    if (which >= 0 && which < keys.length) {
                        String newStatus = keys[which];
                        currentRecord.setStatus(newStatus);

                        // Rule 3: Set completedDate ONLY if completedDate is currently empty. Never overwrite existing completion date!
                        if ("COMPLETED".equalsIgnoreCase(newStatus)) {
                            if (currentRecord.getCompletedDate() == null || currentRecord.getCompletedDate().trim().isEmpty()) {
                                currentRecord.setCompletedDate(dateFormat.format(new Date()));
                            }
                        }

                        currentRecord.setUpdatedAt(System.currentTimeMillis());

                        maintenanceRepository.update(currentRecord, new MaintenanceRepository.DatabaseCallback<Void>() {
                            @Override
                            public void onSuccess(Void result) {
                                com.rob.houserental.notifications.AutomaticReminderUtils.syncMaintenanceReminder(MaintenanceDetailsActivity.this, currentRecord);
                                runOnUiThread(() -> {
                                    Toast.makeText(MaintenanceDetailsActivity.this, R.string.status_updated_success, Toast.LENGTH_SHORT).show();
                                    loadMaintenanceRecord();
                                });
                            }

                            @Override
                            public void onError(Exception exception) {
                                Log.e(TAG, "Error updating status", exception);
                                runOnUiThread(() -> Toast.makeText(MaintenanceDetailsActivity.this, R.string.save_failed, Toast.LENGTH_SHORT).show());
                            }
                        });
                    }
                })
                .setNegativeButton(R.string.cancel, null)
                .show();
    }

    private void confirmAddToExpenses() {
        if (currentRecord == null) return;
        double amount = currentRecord.getActualCost() > 0 ? currentRecord.getActualCost() : currentRecord.getEstimatedCost();

        if (amount <= 0) {
            Toast.makeText(this, R.string.error_enter_valid_amount, Toast.LENGTH_SHORT).show();
            return;
        }

        if (currentRecord.getExpenseId() != null && currentRecord.getExpenseId() > 0) {
            Toast.makeText(this, R.string.added_to_expenses_already, Toast.LENGTH_SHORT).show();
            return;
        }

        new MaterialAlertDialogBuilder(this)
                .setTitle(R.string.add_to_expenses)
                .setMessage(getString(R.string.add_to_expenses_confirm, currencyFormat.format(amount)))
                .setPositiveButton(R.string.save, (dialog, which) -> {
                    AppDatabase db = AppDatabase.getInstance(getApplicationContext());
                    Executors.newSingleThreadExecutor().execute(() -> {
                        try {
                            Expense expense = new Expense();
                            expense.setPropertyId(currentRecord.getPropertyId());
                            expense.setUnitId(currentRecord.getUnitId() != null ? currentRecord.getUnitId() : 0);
                            expense.setCategory(currentRecord.getCategory() != null ? currentRecord.getCategory() : "MAINTENANCE");
                            expense.setAmount(amount);
                            expense.setExpenseDate(dateFormat.format(new Date()));
                            expense.setExpenseMonth(monthFormat.format(new Date()));
                            expense.setDescription(currentRecord.getTitle());

                            long expenseId = db.expenseDao().insert(expense);

                            currentRecord.setExpenseId(expenseId);
                            currentRecord.setUpdatedAt(System.currentTimeMillis());
                            db.maintenanceDao().update(currentRecord);

                            runOnUiThread(() -> {
                                Toast.makeText(MaintenanceDetailsActivity.this, R.string.added_to_expenses_success, Toast.LENGTH_SHORT).show();
                                loadMaintenanceRecord();
                            });
                        } catch (Exception e) {
                            Log.e(TAG, "Error creating expense from maintenance", e);
                            runOnUiThread(() -> Toast.makeText(MaintenanceDetailsActivity.this, R.string.save_failed, Toast.LENGTH_SHORT).show());
                        }
                    });
                })
                .setNegativeButton(R.string.cancel, null)
                .show();
    }

    private void confirmDeleteDocument(AppDocument doc) {
        if (doc == null) return;
        new MaterialAlertDialogBuilder(this)
                .setTitle(R.string.delete)
                .setMessage(R.string.delete_unit_confirm)
                .setPositiveButton(R.string.delete, (dialog, which) -> {
                    AppDatabase db = AppDatabase.getInstance(getApplicationContext());
                    Executors.newSingleThreadExecutor().execute(() -> {
                        try {
                            db.appDocumentDao().delete(doc);
                            if (doc.getFilePath() != null) {
                                File f = new File(doc.getFilePath());
                                if (f.exists()) f.delete();
                            }
                            runOnUiThread(() -> {
                                Toast.makeText(MaintenanceDetailsActivity.this, R.string.deleted_success, Toast.LENGTH_SHORT).show();
                                loadMaintenanceRecord();
                            });
                        } catch (Exception e) {
                            Log.e(TAG, "Error deleting maintenance document", e);
                        }
                    });
                })
                .setNegativeButton(R.string.cancel, null)
                .show();
    }

    private void confirmDelete() {
        if (currentRecord == null) return;
        new MaterialAlertDialogBuilder(this)
                .setTitle(R.string.delete_maintenance_title)
                .setMessage(R.string.delete_maintenance_confirm)
                .setPositiveButton(R.string.delete, (dialog, which) -> {
                    maintenanceRepository.delete(currentRecord, new MaintenanceRepository.DatabaseCallback<Void>() {
                        @Override
                        public void onSuccess(Void result) {
                            runOnUiThread(() -> {
                                Toast.makeText(MaintenanceDetailsActivity.this, R.string.deleted_success, Toast.LENGTH_SHORT).show();
                                finish();
                            });
                        }

                        @Override
                        public void onError(Exception exception) {
                            Log.e(TAG, "Error deleting maintenance record", exception);
                        }
                    });
                })
                .setNegativeButton(R.string.cancel, null)
                .show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            loadMaintenanceRecord();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadMaintenanceRecord();
    }
}

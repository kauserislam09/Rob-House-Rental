package com.rob.houserental;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.checkbox.MaterialCheckBox;
import com.google.android.material.textfield.MaterialAutoCompleteTextView;
import com.google.android.material.textfield.TextInputEditText;
import com.rob.houserental.adapter.MaintenanceAdapter;
import com.rob.houserental.adapter.UnitAdapter;
import com.rob.houserental.data.AppDatabase;
import com.rob.houserental.model.MaintenanceRecord;
import com.rob.houserental.model.Property;
import com.rob.houserental.model.Reminder;
import com.rob.houserental.model.Unit;
import com.rob.houserental.repository.MaintenanceRepository;
import com.rob.houserental.repository.ReminderRepository;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.Executors;

public class AddMaintenanceActivity extends AppCompatActivity {

    private static final String TAG = "AddMaintenanceActivity";

    private MaterialToolbar toolbar;
    private TextInputEditText etTitle;
    private MaterialAutoCompleteTextView autoProperty;
    private MaterialAutoCompleteTextView autoUnit;
    private MaterialAutoCompleteTextView autoCategory;
    private MaterialAutoCompleteTextView autoPriority;
    private MaterialAutoCompleteTextView autoStatus;
    private TextInputEditText etDescription;
    private TextInputEditText etEstCost;
    private TextInputEditText etActualCost;
    private TextInputEditText etVendorName;
    private TextInputEditText etVendorPhone;
    private TextInputEditText etScheduledDate;
    private TextInputEditText etNotes;
    private MaterialCheckBox cbCreateReminder;
    private MaterialButton btnSave;

    private MaintenanceRepository maintenanceRepository;
    private ReminderRepository reminderRepository;

    private long maintenanceId = -1;
    private MaintenanceRecord existingRecord;

    private List<Property> propertyList = new ArrayList<>();
    private List<Unit> propertyUnitsList = new ArrayList<>();

    private long selectedPropertyId = -1;
    private Long selectedUnitId = null; // Nullable Long: NULL = Property-wide maintenance

    private String selectedCategory = "OTHER";
    private String selectedPriority = "MEDIUM";
    private String selectedStatus = "OPEN";

    private final String[] catKeys = {"PLUMBING", "ELECTRICAL", "PAINTING", "AC", "APPLIANCE", "CLEANING", "STRUCTURAL", "SECURITY", "WATER", "GAS", "OTHER"};
    private final String[] priorityKeys = {"LOW", "MEDIUM", "HIGH", "URGENT"};
    private final String[] statusKeys = {"OPEN", "SCHEDULED", "IN_PROGRESS", "COMPLETED", "CANCELLED"};

    private final Calendar scheduledCalendar = Calendar.getInstance();
    private static final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_maintenance);

        maintenanceId = getIntent().getLongExtra("maintenance_id", -1);
        long preselectedPropId = getIntent().getLongExtra("property_id", -1);

        if (preselectedPropId > 0) {
            selectedPropertyId = preselectedPropId;
        }

        maintenanceRepository = new MaintenanceRepository(this);
        reminderRepository = new ReminderRepository(this);

        initializeViews();
        setupToolbar();
        setupDropdowns();
        setupDatePicker();

        loadPropertiesFromDatabase();

        if (btnSave != null) {
            btnSave.setOnClickListener(v -> saveMaintenance());
        }
    }

    private void initializeViews() {
        toolbar = findViewById(R.id.toolbarAddMaintenance);
        etTitle = findViewById(R.id.etMaintenanceTitle);
        autoProperty = findViewById(R.id.autoMaintenanceProperty);
        autoUnit = findViewById(R.id.autoMaintenanceUnit);
        autoCategory = findViewById(R.id.autoMaintenanceCategory);
        autoPriority = findViewById(R.id.autoMaintenancePriority);
        autoStatus = findViewById(R.id.autoMaintenanceStatus);
        etDescription = findViewById(R.id.etMaintenanceDescription);
        etEstCost = findViewById(R.id.etMaintenanceEstCost);
        etActualCost = findViewById(R.id.etMaintenanceActualCost);
        etVendorName = findViewById(R.id.etMaintenanceVendorName);
        etVendorPhone = findViewById(R.id.etMaintenanceVendorPhone);
        etScheduledDate = findViewById(R.id.etMaintenanceScheduledDate);
        etNotes = findViewById(R.id.etMaintenanceNotes);
        cbCreateReminder = findViewById(R.id.cbCreateReminder);
        btnSave = findViewById(R.id.btnSaveMaintenance);
    }

    private void setupToolbar() {
        if (toolbar != null) {
            if (maintenanceId > 0) {
                toolbar.setTitle(R.string.edit);
            }
            toolbar.setNavigationOnClickListener(v -> finish());
        }
    }

    private void setupDropdowns() {
        // Category
        String[] catDisplays = {
                getString(R.string.category_plumbing),
                getString(R.string.category_electrical),
                getString(R.string.category_painting),
                getString(R.string.category_ac),
                getString(R.string.category_appliance),
                getString(R.string.category_cleaning),
                getString(R.string.category_structural),
                getString(R.string.category_security),
                getString(R.string.category_water),
                getString(R.string.category_gas),
                getString(R.string.category_other)
        };

        if (autoCategory != null) {
            autoCategory.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, catDisplays));
            autoCategory.setText(catDisplays[catDisplays.length - 1], false);
            autoCategory.setOnItemClickListener((parent, view, position, id) -> {
                if (position >= 0 && position < catKeys.length) {
                    selectedCategory = catKeys[position];
                }
            });
        }

        // Priority
        String[] priorityDisplays = {
                getString(R.string.priority_low),
                getString(R.string.priority_medium),
                getString(R.string.priority_high),
                getString(R.string.priority_urgent)
        };

        if (autoPriority != null) {
            autoPriority.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, priorityDisplays));
            autoPriority.setText(priorityDisplays[1], false);
            autoPriority.setOnItemClickListener((parent, view, position, id) -> {
                if (position >= 0 && position < priorityKeys.length) {
                    selectedPriority = priorityKeys[position];
                }
            });
        }

        // Status
        String[] statusDisplays = {
                getString(R.string.status_maint_open),
                getString(R.string.status_maint_scheduled),
                getString(R.string.status_maint_in_progress),
                getString(R.string.status_maint_completed),
                getString(R.string.status_maint_cancelled)
        };

        if (autoStatus != null) {
            autoStatus.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, statusDisplays));
            autoStatus.setText(statusDisplays[0], false);
            autoStatus.setOnItemClickListener((parent, view, position, id) -> {
                if (position >= 0 && position < statusKeys.length) {
                    selectedStatus = statusKeys[position];
                }
            });
        }
    }

    private void setupDatePicker() {
        if (etScheduledDate != null) {
            etScheduledDate.setOnClickListener(v -> {
                DatePickerDialog dialog = new DatePickerDialog(
                        this,
                        (view, year, month, dayOfMonth) -> {
                            scheduledCalendar.set(Calendar.YEAR, year);
                            scheduledCalendar.set(Calendar.MONTH, month);
                            scheduledCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                            etScheduledDate.setText(dateFormat.format(scheduledCalendar.getTime()));
                        },
                        scheduledCalendar.get(Calendar.YEAR),
                        scheduledCalendar.get(Calendar.MONTH),
                        scheduledCalendar.get(Calendar.DAY_OF_MONTH)
                );
                dialog.show();
            });
        }
    }

    private void loadPropertiesFromDatabase() {
        AppDatabase db = AppDatabase.getInstance(getApplicationContext());
        Executors.newSingleThreadExecutor().execute(() -> {
            List<Property> props = db.propertyDao().getAllProperties();
            propertyList = props != null ? props : new ArrayList<>();

            List<String> names = new ArrayList<>();
            int preselectIdx = 0;
            for (int i = 0; i < propertyList.size(); i++) {
                Property p = propertyList.get(i);
                names.add(p.getName());
                if (selectedPropertyId > 0 && p.getId() == selectedPropertyId) {
                    preselectIdx = i;
                }
            }

            final int finalPreselect = preselectIdx;

            runOnUiThread(() -> {
                if (autoProperty != null && !names.isEmpty()) {
                    ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, names);
                    autoProperty.setAdapter(adapter);
                    autoProperty.setText(names.get(finalPreselect), false);
                    selectedPropertyId = propertyList.get(finalPreselect).getId();
                    loadUnitsForProperty(selectedPropertyId);

                    autoProperty.setOnItemClickListener((parent, view, position, id) -> {
                        if (position >= 0 && position < propertyList.size()) {
                            selectedPropertyId = propertyList.get(position).getId();
                            loadUnitsForProperty(selectedPropertyId);
                        }
                    });
                }

                if (maintenanceId > 0) {
                    loadExistingMaintenanceRecord();
                }
            });
        });
    }

    private void loadUnitsForProperty(long propertyId) {
        AppDatabase db = AppDatabase.getInstance(getApplicationContext());
        Executors.newSingleThreadExecutor().execute(() -> {
            List<Unit> units = db.unitDao().getUnitsByProperty(propertyId);
            propertyUnitsList = units != null ? units : new ArrayList<>();

            List<String> unitNames = new ArrayList<>();
            unitNames.add(getString(R.string.property_wide_entire_building)); // Index 0 = Null unitId
            int preselectIdx = 0;

            for (int i = 0; i < propertyUnitsList.size(); i++) {
                Unit u = propertyUnitsList.get(i);
                unitNames.add(u.getUnitNumber() + " (" + UnitAdapter.getUnitTypeDisplay(this, u.getUnitType()) + ")");
                if (selectedUnitId != null && selectedUnitId == u.getId()) {
                    preselectIdx = i + 1;
                }
            }

            final int finalUnitPreselect = preselectIdx;

            runOnUiThread(() -> {
                if (autoUnit != null) {
                    ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, unitNames);
                    autoUnit.setAdapter(adapter);
                    autoUnit.setText(unitNames.get(finalUnitPreselect), false);

                    autoUnit.setOnItemClickListener((parent, view, position, id) -> {
                        if (position == 0) {
                            selectedUnitId = null; // Property-wide
                        } else if (position - 1 < propertyUnitsList.size()) {
                            selectedUnitId = propertyUnitsList.get(position - 1).getId();
                        }
                    });
                }
            });
        });
    }

    private void loadExistingMaintenanceRecord() {
        maintenanceRepository.getById(maintenanceId, new MaintenanceRepository.DatabaseCallback<MaintenanceRecord>() {
            @Override
            public void onSuccess(MaintenanceRecord record) {
                existingRecord = record;
                if (record == null) return;

                runOnUiThread(() -> {
                    if (etTitle != null) etTitle.setText(record.getTitle() != null ? record.getTitle() : "");
                    if (etDescription != null) etDescription.setText(record.getDescription() != null ? record.getDescription() : "");
                    if (etEstCost != null) etEstCost.setText(String.valueOf(record.getEstimatedCost()));
                    if (etActualCost != null) etActualCost.setText(String.valueOf(record.getActualCost()));
                    if (etVendorName != null) etVendorName.setText(record.getVendorName() != null ? record.getVendorName() : "");
                    if (etVendorPhone != null) etVendorPhone.setText(record.getVendorPhone() != null ? record.getVendorPhone() : "");
                    if (etScheduledDate != null) etScheduledDate.setText(record.getScheduledDate() != null ? record.getScheduledDate() : "");
                    if (etNotes != null) etNotes.setText(record.getNotes() != null ? record.getNotes() : "");

                    selectedPropertyId = record.getPropertyId();
                    selectedUnitId = record.getUnitId();
                    loadUnitsForProperty(selectedPropertyId);

                    selectedCategory = record.getCategory() != null ? record.getCategory().toUpperCase() : "OTHER";
                    for (int i = 0; i < catKeys.length; i++) {
                        if (catKeys[i].equalsIgnoreCase(selectedCategory)) {
                            if (autoCategory != null) autoCategory.setText(autoCategory.getAdapter().getItem(i).toString(), false);
                            break;
                        }
                    }

                    selectedPriority = record.getPriority() != null ? record.getPriority().toUpperCase() : "MEDIUM";
                    for (int i = 0; i < priorityKeys.length; i++) {
                        if (priorityKeys[i].equalsIgnoreCase(selectedPriority)) {
                            if (autoPriority != null) autoPriority.setText(autoPriority.getAdapter().getItem(i).toString(), false);
                            break;
                        }
                    }

                    selectedStatus = record.getStatus() != null ? record.getStatus().toUpperCase() : "OPEN";
                    for (int i = 0; i < statusKeys.length; i++) {
                        if (statusKeys[i].equalsIgnoreCase(selectedStatus)) {
                            if (autoStatus != null) autoStatus.setText(autoStatus.getAdapter().getItem(i).toString(), false);
                            break;
                        }
                    }
                });
            }

            @Override
            public void onError(Exception exception) {
                Log.e(TAG, "Error loading existing maintenance record for edit", exception);
            }
        });
    }

    private void saveMaintenance() {
        String title = etTitle != null && etTitle.getText() != null ? etTitle.getText().toString().trim() : "";
        if (title.isEmpty()) {
            Toast.makeText(this, R.string.error_title_required, Toast.LENGTH_SHORT).show();
            return;
        }

        if (selectedPropertyId <= 0) {
            Toast.makeText(this, R.string.error_property_required, Toast.LENGTH_SHORT).show();
            return;
        }

        double estCost = 0.0;
        try {
            if (etEstCost != null && etEstCost.getText() != null && !etEstCost.getText().toString().isEmpty()) {
                estCost = Double.parseDouble(etEstCost.getText().toString().trim());
            }
        } catch (Exception e) {
            estCost = -1.0;
        }

        double actCost = 0.0;
        try {
            if (etActualCost != null && etActualCost.getText() != null && !etActualCost.getText().toString().isEmpty()) {
                actCost = Double.parseDouble(etActualCost.getText().toString().trim());
            }
        } catch (Exception e) {
            actCost = -1.0;
        }

        if (estCost < 0 || actCost < 0) {
            Toast.makeText(this, R.string.error_enter_valid_amount, Toast.LENGTH_SHORT).show();
            return;
        }

        String scheduledDate = etScheduledDate != null && etScheduledDate.getText() != null ? etScheduledDate.getText().toString().trim() : "";

        MaintenanceRecord record = existingRecord != null ? existingRecord : new MaintenanceRecord();
        record.setTitle(title);
        record.setPropertyId(selectedPropertyId);
        record.setUnitId(selectedUnitId); // Nullable Long: null = Property-wide
        record.setCategory(selectedCategory);
        record.setPriority(selectedPriority);
        record.setStatus(selectedStatus);
        record.setDescription(etDescription != null && etDescription.getText() != null ? etDescription.getText().toString().trim() : "");
        record.setEstimatedCost(estCost);
        record.setActualCost(actCost);
        record.setVendorName(etVendorName != null && etVendorName.getText() != null ? etVendorName.getText().toString().trim() : "");
        record.setVendorPhone(etVendorPhone != null && etVendorPhone.getText() != null ? etVendorPhone.getText().toString().trim() : "");
        record.setScheduledDate(scheduledDate);
        record.setNotes(etNotes != null && etNotes.getText() != null ? etNotes.getText().toString().trim() : "");
        record.setUpdatedAt(System.currentTimeMillis());

        boolean shouldCreateReminder = cbCreateReminder != null && cbCreateReminder.isChecked() && !scheduledDate.isEmpty();

        if (existingRecord != null) {
            maintenanceRepository.update(record, new MaintenanceRepository.DatabaseCallback<Void>() {
                @Override
                public void onSuccess(Void result) {
                    if (shouldCreateReminder) {
                        com.rob.houserental.notifications.AutomaticReminderUtils.syncMaintenanceReminder(AddMaintenanceActivity.this, record);
                    }
                    runOnUiThread(() -> {
                        Toast.makeText(AddMaintenanceActivity.this, R.string.maintenance_saved_success, Toast.LENGTH_SHORT).show();
                        finish();
                    });
                }

                @Override
                public void onError(Exception exception) {
                    Log.e(TAG, "Error updating maintenance record", exception);
                    runOnUiThread(() -> Toast.makeText(AddMaintenanceActivity.this, R.string.save_failed, Toast.LENGTH_SHORT).show());
                }
            });
        } else {
            maintenanceRepository.insert(record, new MaintenanceRepository.DatabaseCallback<Long>() {
                @Override
                public void onSuccess(Long maintenanceId) {
                    record.setId(maintenanceId);
                    if (shouldCreateReminder) {
                        com.rob.houserental.notifications.AutomaticReminderUtils.syncMaintenanceReminder(AddMaintenanceActivity.this, record);
                    }

                    runOnUiThread(() -> {
                        Toast.makeText(AddMaintenanceActivity.this, R.string.maintenance_saved_success, Toast.LENGTH_SHORT).show();
                        finish();
                    });
                }

                @Override
                public void onError(Exception exception) {
                    Log.e(TAG, "Error saving maintenance record", exception);
                    runOnUiThread(() -> Toast.makeText(AddMaintenanceActivity.this, R.string.save_failed, Toast.LENGTH_SHORT).show());
                }
            });
        }
    }
}

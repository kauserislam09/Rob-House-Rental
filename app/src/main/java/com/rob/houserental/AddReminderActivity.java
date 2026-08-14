package com.rob.houserental;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.MaterialAutoCompleteTextView;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.rob.houserental.data.AppDatabase;
import com.rob.houserental.model.AppDocument;
import com.rob.houserental.model.MaintenanceRecord;
import com.rob.houserental.model.Property;
import com.rob.houserental.model.Reminder;
import com.rob.houserental.model.RentRecord;
import com.rob.houserental.model.Tenancy;
import com.rob.houserental.model.Tenant;
import com.rob.houserental.model.Unit;
import com.rob.houserental.model.UtilityBill;
import com.rob.houserental.repository.ReminderRepository;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.Executors;

public class AddReminderActivity extends AppCompatActivity {

    private static final String TAG = "AddReminderActivity";

    private MaterialToolbar toolbar;
    private TextInputEditText etTitle;
    private TextInputEditText etDescription;
    private TextInputEditText etDate;
    private TextInputEditText etTime;
    private MaterialAutoCompleteTextView autoRelatedEntity;
    private TextInputLayout layoutRelatedRecord;
    private MaterialAutoCompleteTextView autoRelatedRecord;
    private MaterialAutoCompleteTextView autoRepeat;
    private TextInputLayout layoutCustomInterval;
    private TextInputEditText etCustomInterval;
    private MaterialButton btnSave;

    private ReminderRepository reminderRepository;
    private final Calendar selectedCalendar = Calendar.getInstance();
    private static final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
    private static final SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());

    private long reminderId = -1;
    private Reminder existingReminder;

    private String selectedRelatedEntityType = "NONE";
    private long selectedRelatedEntityId = 0;
    private String selectedRepeatType = "ONCE";

    private final String[] entityKeys = {"NONE", "PROPERTY", "UNIT", "TENANT", "TENANCY", "RENT", "BILL", "MAINTENANCE", "DOCUMENT"};
    private final String[] repeatKeys = {"ONCE", "DAILY", "WEEKLY", "MONTHLY", "YEARLY", "CUSTOM"};

    private List<Long> availableRecordIds = new ArrayList<>();
    private List<String> availableRecordNames = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_reminder);

        reminderId = getIntent().getLongExtra("reminder_id", -1);
        reminderRepository = new ReminderRepository(this);

        initializeViews();
        setupToolbar();
        setupDropdowns();
        setupDateTimePickers();

        if (reminderId > 0) {
            loadExistingReminder();
        }

        if (btnSave != null) {
            btnSave.setOnClickListener(v -> saveReminder());
        }
    }

    private void initializeViews() {
        toolbar = findViewById(R.id.toolbarAddReminder);
        etTitle = findViewById(R.id.etReminderTitle);
        etDescription = findViewById(R.id.etReminderDescription);
        etDate = findViewById(R.id.etReminderDate);
        etTime = findViewById(R.id.etReminderTime);
        autoRelatedEntity = findViewById(R.id.autoReminderRelatedEntity);
        layoutRelatedRecord = findViewById(R.id.layoutRelatedRecord);
        autoRelatedRecord = findViewById(R.id.autoReminderRelatedRecord);
        autoRepeat = findViewById(R.id.autoReminderRepeat);
        layoutCustomInterval = findViewById(R.id.layoutCustomInterval);
        etCustomInterval = findViewById(R.id.etCustomInterval);
        btnSave = findViewById(R.id.btnSaveReminder);

        if (etDate != null) etDate.setText(dateFormat.format(new Date()));
        if (etTime != null) etTime.setText(timeFormat.format(new Date()));
    }

    private void setupToolbar() {
        if (toolbar != null) {
            if (reminderId > 0) {
                toolbar.setTitle(R.string.edit);
            }
            toolbar.setNavigationOnClickListener(v -> finish());
        }
    }

    private void setupDropdowns() {
        // Related Entity Dropdown
        List<String> entityDisplays = new ArrayList<>();
        entityDisplays.add(getString(R.string.entity_none));
        entityDisplays.add(getString(R.string.menu_properties));
        entityDisplays.add(getString(R.string.units_title));
        entityDisplays.add(getString(R.string.menu_tenants));
        entityDisplays.add(getString(R.string.menu_tenancies));
        entityDisplays.add(getString(R.string.menu_rent));
        entityDisplays.add(getString(R.string.menu_bills));
        entityDisplays.add(getString(R.string.maintenance_title));
        entityDisplays.add(getString(R.string.menu_documents));

        if (autoRelatedEntity != null) {
            ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, entityDisplays);
            autoRelatedEntity.setAdapter(adapter);
            autoRelatedEntity.setText(entityDisplays.get(0), false);
            autoRelatedEntity.setOnItemClickListener((parent, view, position, id) -> {
                if (position >= 0 && position < entityKeys.length) {
                    selectedRelatedEntityType = entityKeys[position];
                    selectedRelatedEntityId = 0;
                    loadSecondaryRecordsForType(selectedRelatedEntityType);
                }
            });
        }

        // Repeat Type Dropdown
        List<String> repeatDisplays = new ArrayList<>();
        repeatDisplays.add(getString(R.string.repeat_once));
        repeatDisplays.add(getString(R.string.repeat_daily));
        repeatDisplays.add(getString(R.string.repeat_weekly));
        repeatDisplays.add(getString(R.string.repeat_monthly));
        repeatDisplays.add(getString(R.string.repeat_yearly));
        repeatDisplays.add(getString(R.string.repeat_custom));

        if (autoRepeat != null) {
            ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, repeatDisplays);
            autoRepeat.setAdapter(adapter);
            autoRepeat.setText(repeatDisplays.get(0), false);

            autoRepeat.setOnItemClickListener((parent, view, position, id) -> {
                if (position >= 0 && position < repeatKeys.length) {
                    selectedRepeatType = repeatKeys[position];
                    if ("CUSTOM".equalsIgnoreCase(selectedRepeatType)) {
                        if (layoutCustomInterval != null) layoutCustomInterval.setVisibility(View.VISIBLE);
                    } else {
                        if (layoutCustomInterval != null) layoutCustomInterval.setVisibility(View.GONE);
                    }
                }
            });
        }
    }

    private void loadSecondaryRecordsForType(String type) {
        if ("NONE".equalsIgnoreCase(type)) {
            if (layoutRelatedRecord != null) layoutRelatedRecord.setVisibility(View.GONE);
            selectedRelatedEntityId = 0;
            return;
        }

        Executors.newSingleThreadExecutor().execute(() -> {
            AppDatabase db = AppDatabase.getInstance(getApplicationContext());
            availableRecordIds.clear();
            availableRecordNames.clear();

            try {
                if ("PROPERTY".equalsIgnoreCase(type)) {
                    List<Property> props = db.propertyDao().getAllProperties();
                    if (props != null) {
                        for (Property p : props) {
                            availableRecordIds.add(p.getId());
                            availableRecordNames.add(p.getName() != null ? p.getName() : "Property #" + p.getId());
                        }
                    }
                } else if ("UNIT".equalsIgnoreCase(type)) {
                    List<Unit> units = db.unitDao().getAllUnits();
                    if (units != null) {
                        for (Unit u : units) {
                            availableRecordIds.add(u.getId());
                            availableRecordNames.add("Unit " + u.getUnitNumber() + " (Floor " + u.getFloor() + ")");
                        }
                    }
                } else if ("TENANT".equalsIgnoreCase(type)) {
                    List<Tenant> tenants = db.tenantDao().getAllTenants();
                    if (tenants != null) {
                        for (Tenant t : tenants) {
                            availableRecordIds.add(t.getId());
                            availableRecordNames.add(t.getFullName() != null ? t.getFullName() : "Tenant #" + t.getId());
                        }
                    }
                } else if ("TENANCY".equalsIgnoreCase(type)) {
                    List<Tenancy> tenancies = db.tenancyDao().getAllTenancies();
                    if (tenancies != null) {
                        for (Tenancy tn : tenancies) {
                            availableRecordIds.add(tn.getId());
                            availableRecordNames.add("Tenancy #" + tn.getId() + " (" + tn.getStatus() + ")");
                        }
                    }
                } else if ("RENT".equalsIgnoreCase(type)) {
                    List<RentRecord> rents = db.rentDao().getAllRentRecords();
                    if (rents != null) {
                        for (RentRecord r : rents) {
                            availableRecordIds.add(r.getId());
                            availableRecordNames.add("Rent " + (r.getBillingMonth() != null ? r.getBillingMonth() : "") + " (Due: ৳" + r.getRemainingAmount() + ")");
                        }
                    }
                } else if ("BILL".equalsIgnoreCase(type)) {
                    List<UtilityBill> bills = db.utilityBillDao().getAllBills();
                    if (bills != null) {
                        for (UtilityBill b : bills) {
                            availableRecordIds.add(b.getId());
                            availableRecordNames.add((b.getBillType() != null ? b.getBillType() : "Bill") + " (" + b.getBillingMonth() + ")");
                        }
                    }
                } else if ("MAINTENANCE".equalsIgnoreCase(type)) {
                    List<MaintenanceRecord> maints = db.maintenanceDao().getAll();
                    if (maints != null) {
                        for (MaintenanceRecord m : maints) {
                            availableRecordIds.add(m.getId());
                            availableRecordNames.add(m.getTitle() != null ? m.getTitle() : "Maintenance #" + m.getId());
                        }
                    }
                } else if ("DOCUMENT".equalsIgnoreCase(type)) {
                    List<AppDocument> docs = db.appDocumentDao().getAllDocuments();
                    if (docs != null) {
                        for (AppDocument d : docs) {
                            availableRecordIds.add(d.getId());
                            availableRecordNames.add(d.getDisplayName() != null ? d.getDisplayName() : d.getFileName());
                        }
                    }
                }
            } catch (Exception e) {
                Log.e(TAG, "Error fetching secondary records for type " + type, e);
            }

            runOnUiThread(() -> {
                if (layoutRelatedRecord != null && autoRelatedRecord != null) {
                    if (availableRecordNames.isEmpty()) {
                        layoutRelatedRecord.setVisibility(View.GONE);
                        selectedRelatedEntityId = 0;
                    } else {
                        layoutRelatedRecord.setVisibility(View.VISIBLE);
                        ArrayAdapter<String> adapter = new ArrayAdapter<>(AddReminderActivity.this, android.R.layout.simple_dropdown_item_1line, availableRecordNames);
                        autoRelatedRecord.setAdapter(adapter);

                        int preselectIdx = 0;
                        if (selectedRelatedEntityId > 0) {
                            for (int i = 0; i < availableRecordIds.size(); i++) {
                                if (availableRecordIds.get(i) == selectedRelatedEntityId) {
                                    preselectIdx = i;
                                    break;
                                }
                            }
                        }

                        autoRelatedRecord.setText(availableRecordNames.get(preselectIdx), false);
                        selectedRelatedEntityId = availableRecordIds.get(preselectIdx);

                        autoRelatedRecord.setOnItemClickListener((parent, view, position, id) -> {
                            if (position >= 0 && position < availableRecordIds.size()) {
                                selectedRelatedEntityId = availableRecordIds.get(position);
                            }
                        });
                    }
                }
            });
        });
    }

    private void setupDateTimePickers() {
        if (etDate != null) {
            etDate.setOnClickListener(v -> {
                DatePickerDialog dialog = new DatePickerDialog(
                        this,
                        (view, year, month, dayOfMonth) -> {
                            selectedCalendar.set(Calendar.YEAR, year);
                            selectedCalendar.set(Calendar.MONTH, month);
                            selectedCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                            etDate.setText(dateFormat.format(selectedCalendar.getTime()));
                        },
                        selectedCalendar.get(Calendar.YEAR),
                        selectedCalendar.get(Calendar.MONTH),
                        selectedCalendar.get(Calendar.DAY_OF_MONTH)
                );
                dialog.show();
            });
        }

        if (etTime != null) {
            etTime.setOnClickListener(v -> {
                TimePickerDialog dialog = new TimePickerDialog(
                        this,
                        (view, hourOfDay, minute) -> {
                            selectedCalendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
                            selectedCalendar.set(Calendar.MINUTE, minute);
                            etTime.setText(timeFormat.format(selectedCalendar.getTime()));
                        },
                        selectedCalendar.get(Calendar.HOUR_OF_DAY),
                        selectedCalendar.get(Calendar.MINUTE),
                        true
                );
                dialog.show();
            });
        }
    }

    private void loadExistingReminder() {
        reminderRepository.getById(reminderId, new ReminderRepository.DatabaseCallback<Reminder>() {
            @Override
            public void onSuccess(Reminder reminder) {
                existingReminder = reminder;
                if (reminder == null) return;

                runOnUiThread(() -> {
                    if (etTitle != null) etTitle.setText(reminder.getTitle() != null ? reminder.getTitle() : "");
                    if (etDescription != null) etDescription.setText(reminder.getDescription() != null ? reminder.getDescription() : "");
                    if (etDate != null && reminder.getReminderDate() != null) etDate.setText(reminder.getReminderDate());
                    if (etTime != null && reminder.getReminderTime() != null) etTime.setText(reminder.getReminderTime());

                    selectedRelatedEntityType = reminder.getRelatedEntityType() != null ? reminder.getRelatedEntityType().toUpperCase() : "NONE";
                    selectedRelatedEntityId = reminder.getRelatedEntityId();

                    for (int i = 0; i < entityKeys.length; i++) {
                        if (entityKeys[i].equalsIgnoreCase(selectedRelatedEntityType)) {
                            if (autoRelatedEntity != null) autoRelatedEntity.setText(autoRelatedEntity.getAdapter().getItem(i).toString(), false);
                            break;
                        }
                    }

                    loadSecondaryRecordsForType(selectedRelatedEntityType);

                    selectedRepeatType = reminder.getRepeatType() != null ? reminder.getRepeatType().toUpperCase() : "ONCE";
                    for (int i = 0; i < repeatKeys.length; i++) {
                        if (repeatKeys[i].equalsIgnoreCase(selectedRepeatType)) {
                            if (autoRepeat != null) autoRepeat.setText(autoRepeat.getAdapter().getItem(i).toString(), false);
                            break;
                        }
                    }

                    if ("CUSTOM".equalsIgnoreCase(selectedRepeatType)) {
                        if (layoutCustomInterval != null) layoutCustomInterval.setVisibility(View.VISIBLE);
                        if (etCustomInterval != null) etCustomInterval.setText(String.valueOf(reminder.getRepeatInterval()));
                    }
                });
            }

            @Override
            public void onError(Exception exception) {
                Log.e(TAG, "Error loading existing reminder for edit", exception);
            }
        });
    }

    private void saveReminder() {
        String title = etTitle != null && etTitle.getText() != null ? etTitle.getText().toString().trim() : "";
        if (title.isEmpty()) {
            Toast.makeText(this, R.string.error_title_required, Toast.LENGTH_SHORT).show();
            return;
        }

        int interval = 1;
        if ("CUSTOM".equalsIgnoreCase(selectedRepeatType)) {
            try {
                if (etCustomInterval != null && etCustomInterval.getText() != null && !etCustomInterval.getText().toString().isEmpty()) {
                    interval = Integer.parseInt(etCustomInterval.getText().toString().trim());
                }
            } catch (Exception e) {
                interval = 0;
            }

            if (interval < 1) {
                Toast.makeText(this, R.string.error_invalid_interval, Toast.LENGTH_SHORT).show();
                return;
            }
        }

        String description = etDescription != null && etDescription.getText() != null ? etDescription.getText().toString().trim() : "";
        String date = etDate != null && etDate.getText() != null ? etDate.getText().toString().trim() : dateFormat.format(new Date());
        String time = etTime != null && etTime.getText() != null ? etTime.getText().toString().trim() : "09:00";

        Reminder reminder = existingReminder != null ? existingReminder : new Reminder();
        reminder.setTitle(title);
        reminder.setDescription(description);
        reminder.setReminderDate(date);
        reminder.setReminderTime(time);
        reminder.setRelatedEntityType(selectedRelatedEntityType);
        reminder.setRelatedEntityId(selectedRelatedEntityId);
        reminder.setRepeatType(selectedRepeatType);
        reminder.setRepeatInterval(interval);
        reminder.setEnabled(true);

        if (existingReminder != null) {
            reminderRepository.update(reminder, new ReminderRepository.DatabaseCallback<Void>() {
                @Override
                public void onSuccess(Void result) {
                    runOnUiThread(() -> {
                        Toast.makeText(AddReminderActivity.this, R.string.reminder_saved_success, Toast.LENGTH_SHORT).show();
                        finish();
                    });
                }

                @Override
                public void onError(Exception exception) {
                    Log.e(TAG, "Error updating reminder", exception);
                    runOnUiThread(() -> Toast.makeText(AddReminderActivity.this, R.string.save_failed, Toast.LENGTH_SHORT).show());
                }
            });
        } else {
            reminder.setCompleted(false);
            reminderRepository.insert(reminder, new ReminderRepository.DatabaseCallback<Long>() {
                @Override
                public void onSuccess(Long result) {
                    runOnUiThread(() -> {
                        Toast.makeText(AddReminderActivity.this, R.string.reminder_saved_success, Toast.LENGTH_SHORT).show();
                        finish();
                    });
                }

                @Override
                public void onError(Exception exception) {
                    Log.e(TAG, "Error saving reminder", exception);
                    runOnUiThread(() -> Toast.makeText(AddReminderActivity.this, R.string.save_failed, Toast.LENGTH_SHORT).show());
                }
            });
        }
    }
}

package com.rob.houserental;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.MaterialAutoCompleteTextView;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.rob.houserental.data.AppDatabase;
import com.rob.houserental.model.Property;
import com.rob.houserental.model.Tenancy;
import com.rob.houserental.model.TenancyWithDetails;
import com.rob.houserental.model.Tenant;
import com.rob.houserental.model.Unit;
import com.rob.houserental.repository.TenancyRepository;
import com.rob.houserental.utils.RentDateUtils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class AddTenancyActivity extends AppCompatActivity {

    private TextInputLayout layoutSelectProperty;
    private TextInputLayout layoutSelectUnit;
    private TextInputLayout layoutSelectTenant;
    private TextInputLayout layoutMonthlyRent;
    private TextInputLayout layoutStartDate;
    private TextInputLayout layoutEndDate;
    private TextInputLayout layoutServiceCharge;
    private TextInputLayout layoutSecurityDeposit;
    private TextInputLayout layoutAdvanceAmount;
    private TextInputLayout layoutAgreementNumber;
    private TextInputLayout layoutTenancyStatus;
    private TextInputLayout layoutTenancyNotes;

    private MaterialAutoCompleteTextView autoProperty;
    private MaterialAutoCompleteTextView autoUnit;
    private MaterialAutoCompleteTextView autoTenant;
    private MaterialAutoCompleteTextView autoTenancyStatus;

    private TextInputEditText etMonthlyRent;
    private TextInputEditText etStartDate;
    private TextInputEditText etEndDate;
    private TextInputEditText etServiceCharge;
    private TextInputEditText etSecurityDeposit;
    private TextInputEditText etAdvanceAmount;
    private TextInputEditText etAgreementNumber;
    private TextInputEditText etTenancyNotes;

    private MaterialButton btnAddTenantShortcut;
    private MaterialButton btnSaveTenancy;

    private TenancyRepository repository;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    private final List<Property> propertyList = new ArrayList<>();
    private final List<Unit> availableUnits = new ArrayList<>();
    private final List<Tenant> tenantList = new ArrayList<>();

    private Property selectedProperty;
    private Unit selectedUnit;
    private Tenant selectedTenant;

    private boolean isEditMode = false;
    private long tenancyId = -1;
    private Tenancy editingTenancy;

    private final Calendar startCalendar = Calendar.getInstance();
    private final Calendar endCalendar = Calendar.getInstance();
    private static final SimpleDateFormat dateFormat = new SimpleDateFormat("dd MMM yyyy", Locale.getDefault());

    private final ActivityResultLauncher<Intent> addTenantLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == RESULT_OK) {
                    loadTenantsAndAutoSelectNewest();
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_add_tenancy);

        tenancyId = getIntent().getLongExtra("tenancy_id", -1);

        initializeViews();

        setupToolbar();

        setupStatusDropdown();

        setupDatePickers();

        repository = new TenancyRepository(getApplicationContext());

        loadProperties();

        loadTenants();

        checkEditMode();

        setupListeners();
    }

    private void initializeViews() {
        layoutSelectProperty = findViewById(R.id.layoutSelectProperty);
        layoutSelectUnit = findViewById(R.id.layoutSelectUnit);
        layoutSelectTenant = findViewById(R.id.layoutSelectTenant);
        layoutMonthlyRent = findViewById(R.id.layoutMonthlyRent);
        layoutStartDate = findViewById(R.id.layoutStartDate);
        layoutEndDate = findViewById(R.id.layoutEndDate);
        layoutServiceCharge = findViewById(R.id.layoutServiceCharge);
        layoutSecurityDeposit = findViewById(R.id.layoutSecurityDeposit);
        layoutAdvanceAmount = findViewById(R.id.layoutAdvanceAmount);
        layoutAgreementNumber = findViewById(R.id.layoutAgreementNumber);
        layoutTenancyStatus = findViewById(R.id.layoutTenancyStatus);
        layoutTenancyNotes = findViewById(R.id.layoutTenancyNotes);

        autoProperty = findViewById(R.id.autoProperty);
        autoUnit = findViewById(R.id.autoUnit);
        autoTenant = findViewById(R.id.autoTenant);
        autoTenancyStatus = findViewById(R.id.autoTenancyStatus);

        etMonthlyRent = findViewById(R.id.etMonthlyRent);
        etStartDate = findViewById(R.id.etStartDate);
        etEndDate = findViewById(R.id.etEndDate);
        etServiceCharge = findViewById(R.id.etServiceCharge);
        etSecurityDeposit = findViewById(R.id.etSecurityDeposit);
        etAdvanceAmount = findViewById(R.id.etAdvanceAmount);
        etAgreementNumber = findViewById(R.id.etAgreementNumber);
        etTenancyNotes = findViewById(R.id.etTenancyNotes);

        btnAddTenantShortcut = findViewById(R.id.btnAddTenantShortcut);
        btnSaveTenancy = findViewById(R.id.btnSaveTenancy);
    }

    private void setupToolbar() {
        MaterialToolbar toolbar = findViewById(R.id.toolbarAddTenancy);
        toolbar.setNavigationOnClickListener(v -> finish());
    }

    private void setupStatusDropdown() {
        String[] statuses = {"ACTIVE", "ENDED", "CANCELLED"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_dropdown_item_1line,
                statuses
        );
        autoTenancyStatus.setAdapter(adapter);
        autoTenancyStatus.setText(statuses[0], false);
    }

    private void setupDatePickers() {
        // Default start date to today
        etStartDate.setText(dateFormat.format(startCalendar.getTime()));

        etStartDate.setOnClickListener(v -> {
            new DatePickerDialog(
                    AddTenancyActivity.this,
                    (view, year, month, dayOfMonth) -> {
                        startCalendar.set(Calendar.YEAR, year);
                        startCalendar.set(Calendar.MONTH, month);
                        startCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                        etStartDate.setText(dateFormat.format(startCalendar.getTime()));
                    },
                    startCalendar.get(Calendar.YEAR),
                    startCalendar.get(Calendar.MONTH),
                    startCalendar.get(Calendar.DAY_OF_MONTH)
            ).show();
        });

        etEndDate.setOnClickListener(v -> {
            new DatePickerDialog(
                    AddTenancyActivity.this,
                    (view, year, month, dayOfMonth) -> {
                        endCalendar.set(Calendar.YEAR, year);
                        endCalendar.set(Calendar.MONTH, month);
                        endCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                        etEndDate.setText(dateFormat.format(endCalendar.getTime()));
                    },
                    endCalendar.get(Calendar.YEAR),
                    endCalendar.get(Calendar.MONTH),
                    endCalendar.get(Calendar.DAY_OF_MONTH)
            ).show();
        });
    }

    private void setupListeners() {
        btnAddTenantShortcut.setOnClickListener(v -> {
            Intent intent = new Intent(AddTenancyActivity.this, AddTenantActivity.class);
            addTenantLauncher.launch(intent);
        });

        btnSaveTenancy.setOnClickListener(v -> saveTenancy());
    }

    private void loadProperties() {
        executor.execute(() -> {
            try {
                AppDatabase db = AppDatabase.getInstance(getApplicationContext());
                List<Property> properties = db.propertyDao().getAllProperties();

                runOnUiThread(() -> {
                    propertyList.clear();
                    if (properties != null) {
                        propertyList.addAll(properties);
                    }

                    List<String> propertyNames = new ArrayList<>();
                    for (Property p : propertyList) {
                        propertyNames.add(p.getName());
                    }

                    ArrayAdapter<String> adapter = new ArrayAdapter<>(
                            AddTenancyActivity.this,
                            android.R.layout.simple_dropdown_item_1line,
                            propertyNames
                    );
                    autoProperty.setAdapter(adapter);

                    autoProperty.setOnItemClickListener((parent, view, position, id) -> {
                        selectedProperty = propertyList.get(position);
                        loadUnitsForProperty(selectedProperty.getId());
                    });
                });
            } catch (Exception ignored) {
            }
        });
    }

    private void loadUnitsForProperty(long propertyId) {
        executor.execute(() -> {
            try {
                AppDatabase db = AppDatabase.getInstance(getApplicationContext());
                List<Unit> units = db.unitDao().getUnitsByProperty(propertyId);

                runOnUiThread(() -> {
                    availableUnits.clear();
                    List<String> unitNames = new ArrayList<>();

                    if (units != null) {
                        for (Unit u : units) {
                            boolean isAvailable = "VACANT".equalsIgnoreCase(u.getStatus()) ||
                                    (isEditMode && editingTenancy != null && u.getId() == editingTenancy.getUnitId());
                            if (isAvailable) {
                                availableUnits.add(u);
                                unitNames.add(getString(R.string.prefix_unit_floor_format, u.getUnitNumber(), u.getFloor()));
                            }
                        }
                    }

                    if (availableUnits.isEmpty()) {
                        layoutSelectUnit.setError(getString(R.string.no_available_units));
                        autoUnit.setText("", false);
                        selectedUnit = null;
                    } else {
                        layoutSelectUnit.setError(null);
                    }

                    ArrayAdapter<String> adapter = new ArrayAdapter<>(
                            AddTenancyActivity.this,
                            android.R.layout.simple_dropdown_item_1line,
                            unitNames
                    );
                    autoUnit.setAdapter(adapter);

                    autoUnit.setOnItemClickListener((parent, view, position, id) -> {
                        selectedUnit = availableUnits.get(position);
                        if (!isEditMode) {
                            if (selectedUnit.getMonthlyRent() > 0) {
                                etMonthlyRent.setText(String.valueOf(selectedUnit.getMonthlyRent()));
                            }
                            if (selectedUnit.getSecurityDeposit() > 0) {
                                etSecurityDeposit.setText(String.valueOf(selectedUnit.getSecurityDeposit()));
                            }
                        }
                    });
                });
            } catch (Exception ignored) {
            }
        });
    }

    private void loadTenants() {
        executor.execute(() -> {
            try {
                AppDatabase db = AppDatabase.getInstance(getApplicationContext());
                List<Tenant> tenants = db.tenantDao().getAllTenants();

                runOnUiThread(() -> {
                    tenantList.clear();
                    if (tenants != null) {
                        tenantList.addAll(tenants);
                    }

                    List<String> tenantNames = new ArrayList<>();
                    for (Tenant t : tenantList) {
                        String display = t.getFullName();
                        if (t.getPhoneNumber() != null && !t.getPhoneNumber().isEmpty()) {
                            display += " (" + t.getPhoneNumber() + ")";
                        }
                        tenantNames.add(display);
                    }

                    ArrayAdapter<String> adapter = new ArrayAdapter<>(
                            AddTenancyActivity.this,
                            android.R.layout.simple_dropdown_item_1line,
                            tenantNames
                    );
                    autoTenant.setAdapter(adapter);

                    autoTenant.setOnItemClickListener((parent, view, position, id) -> {
                        selectedTenant = tenantList.get(position);
                    });
                });
            } catch (Exception ignored) {
            }
        });
    }

    private void loadTenantsAndAutoSelectNewest() {
        executor.execute(() -> {
            try {
                AppDatabase db = AppDatabase.getInstance(getApplicationContext());
                List<Tenant> tenants = db.tenantDao().getAllTenants();

                runOnUiThread(() -> {
                    tenantList.clear();
                    if (tenants != null) {
                        tenantList.addAll(tenants);
                    }

                    List<String> tenantNames = new ArrayList<>();
                    for (Tenant t : tenantList) {
                        String display = t.getFullName();
                        if (t.getPhoneNumber() != null && !t.getPhoneNumber().isEmpty()) {
                            display += " (" + t.getPhoneNumber() + ")";
                        }
                        tenantNames.add(display);
                    }

                    ArrayAdapter<String> adapter = new ArrayAdapter<>(
                            AddTenancyActivity.this,
                            android.R.layout.simple_dropdown_item_1line,
                            tenantNames
                    );
                    autoTenant.setAdapter(adapter);

                    if (!tenantList.isEmpty()) {
                        selectedTenant = tenantList.get(0); // newly inserted tenant is at top if sorted by desc
                        autoTenant.setText(tenantNames.get(0), false);
                    }
                });
            } catch (Exception ignored) {
            }
        });
    }

    private void checkEditMode() {
        if (tenancyId == -1) {
            return;
        }

        isEditMode = true;

        repository.getTenancyWithDetailsById(tenancyId, new TenancyRepository.DatabaseCallback<TenancyWithDetails>() {
            @Override
            public void onSuccess(TenancyWithDetails details) {
                if (details != null) {
                    editingTenancy = details.tenancy;
                    runOnUiThread(() -> populateEditFields(details));
                }
            }

            @Override
            public void onError(Exception exception) {
            }
        });

        MaterialToolbar toolbar = findViewById(R.id.toolbarAddTenancy);
        toolbar.setTitle(R.string.edit_tenancy_title);
        btnSaveTenancy.setText(R.string.update_tenancy);
    }

    private void populateEditFields(TenancyWithDetails details) {
        if (details == null || details.tenancy == null) {
            return;
        }

        Tenancy t = details.tenancy;

        if (details.tenant != null) {
            selectedTenant = details.tenant;
            autoTenant.setText(details.tenant.getFullName() + " (" + details.tenant.getPhoneNumber() + ")", false);
        }

        if (details.unit != null) {
            selectedUnit = details.unit;
            autoUnit.setText("Unit " + details.unit.getUnitNumber() + " (Floor " + details.unit.getFloor() + ")", false);

            executor.execute(() -> {
                try {
                    AppDatabase db = AppDatabase.getInstance(getApplicationContext());
                    Property property = db.propertyDao().getPropertyById(details.unit.getPropertyId());
                    if (property != null) {
                        selectedProperty = property;
                        runOnUiThread(() -> autoProperty.setText(property.getName(), false));
                    }
                } catch (Exception ignored) {
                }
            });
        }

        if (t.getMonthlyRent() > 0) {
            etMonthlyRent.setText(String.valueOf(t.getMonthlyRent()));
        }

        if (t.getStartDate() != null) {
            etStartDate.setText(t.getStartDate());
        }

        if (t.getEndDate() != null) {
            etEndDate.setText(t.getEndDate());
        }

        if (t.getServiceCharge() > 0) {
            etServiceCharge.setText(String.valueOf(t.getServiceCharge()));
        }

        if (t.getSecurityDeposit() > 0) {
            etSecurityDeposit.setText(String.valueOf(t.getSecurityDeposit()));
        }

        if (t.getAdvanceAmount() > 0) {
            etAdvanceAmount.setText(String.valueOf(t.getAdvanceAmount()));
        }

        if (t.getAgreementNumber() != null) {
            etAgreementNumber.setText(t.getAgreementNumber());
        }

        if (t.getStatus() != null) {
            autoTenancyStatus.setText(t.getStatus(), false);
        }

        if (t.getNotes() != null) {
            etTenancyNotes.setText(t.getNotes());
        }
    }

    private void saveTenancy() {
        clearErrors();

        if (selectedProperty == null && !isEditMode) {
            layoutSelectProperty.setError(getString(R.string.property_required));
            return;
        }

        if (selectedUnit == null) {
            layoutSelectUnit.setError(getString(R.string.unit_required));
            return;
        }

        if (selectedTenant == null) {
            layoutSelectTenant.setError(getString(R.string.tenant_required));
            return;
        }

        String startDate = getText(etStartDate);
        if (TextUtils.isEmpty(startDate)) {
            layoutStartDate.setError(getString(R.string.start_date_required));
            return;
        }

        String endDate = getText(etEndDate);
        if (!TextUtils.isEmpty(startDate) && !TextUtils.isEmpty(endDate)) {
            try {
                Date start = dateFormat.parse(startDate);
                Date end = dateFormat.parse(endDate);
                if (start != null && end != null && end.before(start)) {
                    layoutEndDate.setError("End date cannot be before start date");
                    return;
                }
            } catch (ParseException ignored) {
            }
        }

        String rentText = getText(etMonthlyRent);
        double monthlyRent = 0;
        if (!TextUtils.isEmpty(rentText)) {
            try {
                monthlyRent = Double.parseDouble(rentText);
            } catch (NumberFormatException ignored) {
            }
        }

        if (monthlyRent <= 0) {
            layoutMonthlyRent.setError(getString(R.string.rent_required));
            etMonthlyRent.requestFocus();
            return;
        }

        double serviceCharge = parseDouble(getText(etServiceCharge));
        double securityDeposit = parseDouble(getText(etSecurityDeposit));
        double advanceAmount = parseDouble(getText(etAdvanceAmount));
        String agreementNumber = getText(etAgreementNumber);
        String status = getText(autoTenancyStatus);
        String notes = getText(etTenancyNotes);

        long currentTime = System.currentTimeMillis();

        btnSaveTenancy.setEnabled(false);

        if (isEditMode && editingTenancy != null) {
            editingTenancy.setMonthlyRent(monthlyRent);
            editingTenancy.setStartDate(startDate);
            editingTenancy.setEndDate(endDate);
            editingTenancy.setServiceCharge(serviceCharge);
            editingTenancy.setSecurityDeposit(securityDeposit);
            editingTenancy.setAdvanceAmount(advanceAmount);
            editingTenancy.setAgreementNumber(agreementNumber);
            editingTenancy.setStatus(status);
            editingTenancy.setNotes(notes);
            editingTenancy.setUpdatedAt(currentTime);

            repository.update(editingTenancy, new TenancyRepository.DatabaseCallback<Void>() {
                @Override
                public void onSuccess(Void result) {
                    runOnUiThread(() -> {
                        btnSaveTenancy.setEnabled(true);
                        Toast.makeText(AddTenancyActivity.this, R.string.tenancy_updated_success, Toast.LENGTH_SHORT).show();
                        setResult(RESULT_OK);
                        finish();
                    });
                }

                @Override
                public void onError(Exception exception) {
                    runOnUiThread(() -> {
                        btnSaveTenancy.setEnabled(true);
                        btnSaveTenancy.setError(getString(R.string.update_failed));
                    });
                }
            });
        } else {
            int rentDueDay = RentDateUtils.extractDayOfMonth(startDate);
            Tenancy tenancy = new Tenancy(
                    selectedUnit.getId(),
                    selectedTenant.getId(),
                    startDate,
                    endDate,
                    monthlyRent,
                    serviceCharge,
                    securityDeposit,
                    advanceAmount,
                    agreementNumber,
                    rentDueDay,
                    status,
                    notes,
                    currentTime,
                    currentTime
            );

            repository.createTenancy(tenancy, new TenancyRepository.DatabaseCallback<Long>() {
                @Override
                public void onSuccess(Long id) {
                    runOnUiThread(() -> {
                        btnSaveTenancy.setEnabled(true);
                        Toast.makeText(AddTenancyActivity.this, R.string.tenancy_created_success, Toast.LENGTH_SHORT).show();
                        setResult(RESULT_OK);
                        finish();
                    });
                }

                @Override
                public void onError(Exception exception) {
                    runOnUiThread(() -> {
                        btnSaveTenancy.setEnabled(true);
                        if (exception instanceof IllegalStateException && "Unit is already occupied".equals(exception.getMessage())) {
                            layoutSelectUnit.setError(getString(R.string.active_tenancy_exists_error));
                            Toast.makeText(AddTenancyActivity.this, R.string.active_tenancy_exists_error, Toast.LENGTH_LONG).show();
                        } else {
                            btnSaveTenancy.setError(getString(R.string.save_failed));
                        }
                    });
                }
            });
        }
    }

    private double parseDouble(String text) {
        if (TextUtils.isEmpty(text)) {
            return 0.0;
        }
        try {
            return Double.parseDouble(text.trim());
        } catch (NumberFormatException e) {
            return 0.0;
        }
    }

    private String getText(android.widget.TextView view) {
        if (view.getText() == null) {
            return "";
        }
        return view.getText().toString().trim();
    }

    private void clearErrors() {
        layoutSelectProperty.setError(null);
        layoutSelectUnit.setError(null);
        layoutSelectTenant.setError(null);
        layoutMonthlyRent.setError(null);
        layoutStartDate.setError(null);
        layoutEndDate.setError(null);
        btnSaveTenancy.setError(null);
    }
}

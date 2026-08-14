package com.rob.houserental;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.textfield.MaterialAutoCompleteTextView;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.rob.houserental.data.AppDatabase;
import com.rob.houserental.model.Property;
import com.rob.houserental.model.TenancyWithDetails;
import com.rob.houserental.model.Unit;
import com.rob.houserental.model.UtilityBill;
import com.rob.houserental.repository.UtilityBillRepository;
import com.rob.houserental.utils.RentDateUtils;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class AddBillActivity extends AppCompatActivity {

    private TextInputLayout layoutSelectBillProperty;
    private MaterialAutoCompleteTextView autoBillProperty;

    private TextInputLayout layoutSelectBillUnit;
    private MaterialAutoCompleteTextView autoBillUnit;

    private TextView tvOccupiedTenantInfo;

    private TextInputLayout layoutSelectBillType;
    private MaterialAutoCompleteTextView autoBillType;

    private MaterialCardView cardMeterSection;
    private TextView tvMeterSectionTitle;
    private TextInputEditText etMeterNumber;
    private TextInputEditText etPreviousReading;
    private TextInputEditText etCurrentReading;
    private TextView tvCalculatedUnits;
    private TextInputEditText etRatePerUnit;
    private TextInputEditText etFixedCharge;
    private TextInputEditText etVatTax;

    private TextInputLayout layoutBillAmount;
    private TextInputEditText etBillAmount;

    private TextInputLayout layoutBillingMonth;
    private TextInputEditText etBillingMonth;

    private TextInputLayout layoutBillDueDate;
    private TextInputEditText etBillDueDate;

    private TextInputEditText etBillNumber;
    private TextInputEditText etBillNotes;

    private MaterialButton btnSaveBill;

    private UtilityBillRepository repository;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    private final List<Property> propertyList = new ArrayList<>();
    private final List<Unit> unitList = new ArrayList<>();

    private Property selectedProperty;
    private Unit selectedUnit; // null if whole property
    private long selectedTenancyId = 0;
    private String selectedBillType = "ELECTRICITY";

    private boolean isEditMode = false;
    private long billId = -1;
    private UtilityBill editingBill;

    private final Calendar dueDateCalendar = Calendar.getInstance();
    private static final SimpleDateFormat dueDateFormat = new SimpleDateFormat("dd MMM yyyy", Locale.getDefault());
    private static final SimpleDateFormat monthFormat = new SimpleDateFormat("yyyy-MM", Locale.getDefault());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_add_bill);

        billId = getIntent().getLongExtra("bill_id", -1);
        String defaultMonth = getIntent().getStringExtra("default_billing_month");

        initializeViews();

        setupToolbar();

        repository = new UtilityBillRepository(getApplicationContext());

        setupBillTypeDropdown();

        setupMonthAndDatePicker(defaultMonth);

        setupMeterCalculationListeners();

        loadProperties();

        checkEditMode();

        setupListeners();
    }

    private void initializeViews() {
        layoutSelectBillProperty = findViewById(R.id.layoutSelectBillProperty);
        autoBillProperty = findViewById(R.id.autoBillProperty);

        layoutSelectBillUnit = findViewById(R.id.layoutSelectBillUnit);
        autoBillUnit = findViewById(R.id.autoBillUnit);

        tvOccupiedTenantInfo = findViewById(R.id.tvOccupiedTenantInfo);

        layoutSelectBillType = findViewById(R.id.layoutSelectBillType);
        autoBillType = findViewById(R.id.autoBillType);

        cardMeterSection = findViewById(R.id.cardMeterSection);
        tvMeterSectionTitle = findViewById(R.id.tvMeterSectionTitle);
        etMeterNumber = findViewById(R.id.etMeterNumber);
        etPreviousReading = findViewById(R.id.etPreviousReading);
        etCurrentReading = findViewById(R.id.etCurrentReading);
        tvCalculatedUnits = findViewById(R.id.tvCalculatedUnits);
        etRatePerUnit = findViewById(R.id.etRatePerUnit);
        etFixedCharge = findViewById(R.id.etFixedCharge);
        etVatTax = findViewById(R.id.etVatTax);

        layoutBillAmount = findViewById(R.id.layoutBillAmount);
        etBillAmount = findViewById(R.id.etBillAmount);

        layoutBillingMonth = findViewById(R.id.layoutBillingMonth);
        etBillingMonth = findViewById(R.id.etBillingMonth);

        layoutBillDueDate = findViewById(R.id.layoutBillDueDate);
        etBillDueDate = findViewById(R.id.etBillDueDate);

        etBillNumber = findViewById(R.id.etBillNumber);
        etBillNotes = findViewById(R.id.etBillNotes);

        btnSaveBill = findViewById(R.id.btnSaveBill);
    }

    private void setupToolbar() {
        MaterialToolbar toolbar = findViewById(R.id.toolbarAddBill);
        toolbar.setNavigationOnClickListener(v -> finish());
    }

    private void setupBillTypeDropdown() {
        String[] billTypeKeys = {"ELECTRICITY", "WATER", "GAS", "INTERNET", "SERVICE_CHARGE", "OTHER"};
        String[] billTypeLabels = {
                getString(R.string.bill_type_electricity),
                getString(R.string.bill_type_water),
                getString(R.string.bill_type_gas),
                getString(R.string.bill_type_internet),
                getString(R.string.bill_type_service),
                getString(R.string.bill_type_other)
        };

        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_dropdown_item_1line,
                billTypeLabels
        );
        autoBillType.setAdapter(adapter);
        autoBillType.setText(billTypeLabels[0], false);
        selectedBillType = billTypeKeys[0];

        autoBillType.setOnItemClickListener((parent, view, position, id) -> {
            selectedBillType = billTypeKeys[position];
            updateMeterSectionVisibility();
        });
    }

    private void updateMeterSectionVisibility() {
        if ("ELECTRICITY".equalsIgnoreCase(selectedBillType)) {
            cardMeterSection.setVisibility(View.VISIBLE);
            tvMeterSectionTitle.setText(R.string.meter_section_electricity);
        } else if ("WATER".equalsIgnoreCase(selectedBillType)) {
            cardMeterSection.setVisibility(View.VISIBLE);
            tvMeterSectionTitle.setText(R.string.meter_section_water);
        } else if ("GAS".equalsIgnoreCase(selectedBillType)) {
            cardMeterSection.setVisibility(View.VISIBLE);
            tvMeterSectionTitle.setText(R.string.meter_section_gas);
        } else {
            cardMeterSection.setVisibility(View.GONE);
        }
    }

    private void setupMonthAndDatePicker(String defaultMonth) {
        if (defaultMonth != null && !defaultMonth.trim().isEmpty()) {
            etBillingMonth.setText(defaultMonth.trim());
        } else {
            etBillingMonth.setText(monthFormat.format(Calendar.getInstance().getTime()));
        }

        dueDateCalendar.set(Calendar.DAY_OF_MONTH, 15);
        etBillDueDate.setText(dueDateFormat.format(dueDateCalendar.getTime()));

        etBillDueDate.setOnClickListener(v -> {
            new DatePickerDialog(
                    AddBillActivity.this,
                    (view, year, month, dayOfMonth) -> {
                        dueDateCalendar.set(Calendar.YEAR, year);
                        dueDateCalendar.set(Calendar.MONTH, month);
                        dueDateCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                        etBillDueDate.setText(dueDateFormat.format(dueDateCalendar.getTime()));
                    },
                    dueDateCalendar.get(Calendar.YEAR),
                    dueDateCalendar.get(Calendar.MONTH),
                    dueDateCalendar.get(Calendar.DAY_OF_MONTH)
            ).show();
        });
    }

    private void setupMeterCalculationListeners() {
        TextWatcher watcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                calculateMeteredBill();
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        };

        etPreviousReading.addTextChangedListener(watcher);
        etCurrentReading.addTextChangedListener(watcher);
        etRatePerUnit.addTextChangedListener(watcher);
        etFixedCharge.addTextChangedListener(watcher);
        etVatTax.addTextChangedListener(watcher);
    }

    private void calculateMeteredBill() {
        double prev = parseDouble(getText(etPreviousReading));
        double curr = parseDouble(getText(etCurrentReading));
        double rate = parseDouble(getText(etRatePerUnit));
        double fixed = parseDouble(getText(etFixedCharge));
        double vat = parseDouble(getText(etVatTax));

        if (curr >= prev && curr > 0) {
            double units = curr - prev;
            tvCalculatedUnits.setText(getString(R.string.prefix_units_consumed, units));

            if (rate > 0) {
                double total = (units * rate) + fixed + vat;
                etBillAmount.setText(String.format(Locale.US, "%.2f", total));
            }
        } else if (curr > 0 && curr < prev) {
            tvCalculatedUnits.setText(getString(R.string.reading_error));
        } else {
            tvCalculatedUnits.setText(R.string.prefix_units_consumed_zero);
        }
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
                            AddBillActivity.this,
                            android.R.layout.simple_dropdown_item_1line,
                            propertyNames
                    );
                    autoBillProperty.setAdapter(adapter);

                    autoBillProperty.setOnItemClickListener((parent, view, position, id) -> {
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
                    unitList.clear();
                    List<String> unitNames = new ArrayList<>();
                    unitNames.add(getString(R.string.whole_property_option));

                    if (units != null) {
                        unitList.addAll(units);
                        for (Unit u : units) {
                            unitNames.add(getString(R.string.prefix_unit_floor_format, u.getUnitNumber(), u.getFloor()));
                        }
                    }

                    ArrayAdapter<String> adapter = new ArrayAdapter<>(
                            AddBillActivity.this,
                            android.R.layout.simple_dropdown_item_1line,
                            unitNames
                    );
                    autoBillUnit.setAdapter(adapter);
                    autoBillUnit.setText(unitNames.get(0), false);
                    selectedUnit = null;
                    selectedTenancyId = 0;
                    tvOccupiedTenantInfo.setVisibility(View.GONE);

                    autoBillUnit.setOnItemClickListener((parent, view, position, id) -> {
                        if (position == 0) {
                            selectedUnit = null;
                            selectedTenancyId = 0;
                            tvOccupiedTenantInfo.setVisibility(View.GONE);
                        } else {
                            selectedUnit = unitList.get(position - 1);
                            checkActiveTenancyForUnit(selectedUnit.getId());
                        }
                    });
                });
            } catch (Exception ignored) {
            }
        });
    }

    private void checkActiveTenancyForUnit(long unitId) {
        executor.execute(() -> {
            try {
                AppDatabase db = AppDatabase.getInstance(getApplicationContext());
                TenancyWithDetails activeTenancy = db.tenancyDao().getActiveTenancyWithDetailsByUnit(unitId);

                runOnUiThread(() -> {
                    if (activeTenancy != null && activeTenancy.tenant != null) {
                        selectedTenancyId = activeTenancy.tenancy.getId();
                        tvOccupiedTenantInfo.setText(getString(R.string.prefix_current_tenant, activeTenancy.tenant.getFullName()) +
                                (activeTenancy.tenant.getPhoneNumber() != null ? " (" + activeTenancy.tenant.getPhoneNumber() + ")" : ""));
                        tvOccupiedTenantInfo.setVisibility(View.VISIBLE);
                    } else {
                        selectedTenancyId = 0;
                        tvOccupiedTenantInfo.setVisibility(View.GONE);
                    }
                });
            } catch (Exception ignored) {
            }
        });
    }

    private void checkEditMode() {
        if (billId == -1) {
            return;
        }

        isEditMode = true;
        MaterialToolbar toolbar = findViewById(R.id.toolbarAddBill);
        toolbar.setTitle(R.string.edit_bill_title);
        btnSaveBill.setText(R.string.edit_bill_title);

        repository.getBillById(billId, new UtilityBillRepository.DatabaseCallback<UtilityBill>() {
            @Override
            public void onSuccess(UtilityBill bill) {
                if (bill != null) {
                    editingBill = bill;
                    runOnUiThread(() -> populateEditFields(bill));
                }
            }

            @Override
            public void onError(Exception exception) {
            }
        });
    }

    private void populateEditFields(UtilityBill bill) {
        selectedBillType = bill.getBillType();
        updateMeterSectionVisibility();

        if (bill.getAmountDue() > 0) {
            etBillAmount.setText(String.valueOf(bill.getAmountDue()));
        }
        if (bill.getBillingMonth() != null) {
            etBillingMonth.setText(bill.getBillingMonth());
        }
        if (bill.getDueDate() != null) {
            etBillDueDate.setText(bill.getDueDate());
        }
        if (bill.getMeterNumber() != null) {
            etMeterNumber.setText(bill.getMeterNumber());
        }
        if (bill.getPreviousReading() > 0) {
            etPreviousReading.setText(String.valueOf(bill.getPreviousReading()));
        }
        if (bill.getCurrentReading() > 0) {
            etCurrentReading.setText(String.valueOf(bill.getCurrentReading()));
        }
        if (bill.getRatePerUnit() > 0) {
            etRatePerUnit.setText(String.valueOf(bill.getRatePerUnit()));
        }
        if (bill.getFixedCharge() > 0) {
            etFixedCharge.setText(String.valueOf(bill.getFixedCharge()));
        }
        if (bill.getVatOrTax() > 0) {
            etVatTax.setText(String.valueOf(bill.getVatOrTax()));
        }
        if (bill.getBillNumber() != null) {
            etBillNumber.setText(bill.getBillNumber());
        }
        if (bill.getNotes() != null) {
            etBillNotes.setText(bill.getNotes());
        }

        // Load property and restore exact relationships
        executor.execute(() -> {
            try {
                AppDatabase db = AppDatabase.getInstance(getApplicationContext());
                Property p = db.propertyDao().getPropertyById(bill.getPropertyId());
                if (p != null) {
                    selectedProperty = p;
                    runOnUiThread(() -> {
                        autoBillProperty.setText(p.getName(), false);
                        loadUnitsForPropertyWithSelection(p.getId(), bill.getUnitId());
                    });
                }
            } catch (Exception ignored) {
            }
        });
    }

    private void loadUnitsForPropertyWithSelection(long propertyId, long targetUnitId) {
        executor.execute(() -> {
            try {
                AppDatabase db = AppDatabase.getInstance(getApplicationContext());
                List<Unit> units = db.unitDao().getUnitsByProperty(propertyId);

                runOnUiThread(() -> {
                    unitList.clear();
                    List<String> unitNames = new ArrayList<>();
                    unitNames.add(getString(R.string.whole_property_option));

                    int targetIndex = 0;

                    if (units != null) {
                        unitList.addAll(units);
                        for (int i = 0; i < units.size(); i++) {
                            Unit u = units.get(i);
                            unitNames.add(getString(R.string.prefix_unit_floor_format, u.getUnitNumber(), u.getFloor()));
                            if (u.getId() == targetUnitId) {
                                targetIndex = i + 1;
                            }
                        }
                    }

                    ArrayAdapter<String> adapter = new ArrayAdapter<>(
                            AddBillActivity.this,
                            android.R.layout.simple_dropdown_item_1line,
                            unitNames
                    );
                    autoBillUnit.setAdapter(adapter);

                    if (targetIndex > 0 && targetIndex <= unitList.size()) {
                        selectedUnit = unitList.get(targetIndex - 1);
                        autoBillUnit.setText(unitNames.get(targetIndex), false);
                        checkActiveTenancyForUnit(selectedUnit.getId());
                    } else {
                        selectedUnit = null;
                        selectedTenancyId = 0;
                        autoBillUnit.setText(unitNames.get(0), false);
                        tvOccupiedTenantInfo.setVisibility(View.GONE);
                    }

                    autoBillUnit.setOnItemClickListener((parent, view, position, id) -> {
                        if (position == 0) {
                            selectedUnit = null;
                            selectedTenancyId = 0;
                            tvOccupiedTenantInfo.setVisibility(View.GONE);
                        } else {
                            selectedUnit = unitList.get(position - 1);
                            checkActiveTenancyForUnit(selectedUnit.getId());
                        }
                    });
                });
            } catch (Exception ignored) {
            }
        });
    }

    private void setupListeners() {
        btnSaveBill.setOnClickListener(v -> saveBill());
    }

    private void saveBill() {
        clearErrors();

        if (selectedProperty == null && !isEditMode) {
            layoutSelectBillProperty.setError(getString(R.string.property_required));
            return;
        }

        String amountText = getText(etBillAmount);
        double amountDue = 0;
        if (!TextUtils.isEmpty(amountText)) {
            try {
                amountDue = Double.parseDouble(amountText);
            } catch (NumberFormatException ignored) {
            }
        }

        if (amountDue <= 0) {
            layoutBillAmount.setError(getString(R.string.invalid_payment_amount));
            etBillAmount.requestFocus();
            return;
        }

        String billingMonth = getText(etBillingMonth);
        if (TextUtils.isEmpty(billingMonth)) {
            layoutBillingMonth.setError(getString(R.string.billing_month_required));
            return;
        }

        String dueDate = getText(etBillDueDate);
        if (TextUtils.isEmpty(dueDate)) {
            layoutBillDueDate.setError(getString(R.string.due_date_required));
            return;
        }

        double prevReading = parseDouble(getText(etPreviousReading));
        double currReading = parseDouble(getText(etCurrentReading));
        if (currReading > 0 && currReading < prevReading) {
            Toast.makeText(this, R.string.reading_error, Toast.LENGTH_LONG).show();
            return;
        }
        double unitsConsumed = Math.max(0.0, currReading - prevReading);
        double ratePerUnit = parseDouble(getText(etRatePerUnit));
        double fixedCharge = parseDouble(getText(etFixedCharge));
        double vatTax = parseDouble(getText(etVatTax));
        String meterNumber = getText(etMeterNumber);
        String billNumber = getText(etBillNumber);
        String notes = getText(etBillNotes);

        long unitId = selectedUnit != null ? selectedUnit.getId() : 0;
        long tenancyId = selectedTenancyId;
        long propertyId = selectedProperty != null ? selectedProperty.getId() : (editingBill != null ? editingBill.getPropertyId() : 0);

        long currentTime = System.currentTimeMillis();
        btnSaveBill.setEnabled(false);

        if (isEditMode && editingBill != null) {
            editingBill.setBillType(selectedBillType);
            editingBill.setBillingMonth(billingMonth);
            editingBill.setDueDate(dueDate);
            editingBill.setAmountDue(amountDue);
            editingBill.setPropertyId(propertyId);
            editingBill.setUnitId(unitId);
            editingBill.setTenancyId(tenancyId);
            editingBill.setMeterNumber(meterNumber);
            editingBill.setPreviousReading(prevReading);
            editingBill.setCurrentReading(currReading);
            editingBill.setUnitsConsumed(unitsConsumed);
            editingBill.setRatePerUnit(ratePerUnit);
            editingBill.setFixedCharge(fixedCharge);
            editingBill.setVatOrTax(vatTax);
            editingBill.setBillNumber(billNumber);
            editingBill.setNotes(notes);

            repository.updateBill(editingBill, new UtilityBillRepository.DatabaseCallback<Void>() {
                @Override
                public void onSuccess(Void result) {
                    runOnUiThread(() -> {
                        btnSaveBill.setEnabled(true);
                        Toast.makeText(AddBillActivity.this, R.string.bill_updated_success, Toast.LENGTH_SHORT).show();
                        setResult(RESULT_OK);
                        finish();
                    });
                }

                @Override
                public void onError(Exception exception) {
                    runOnUiThread(() -> {
                        btnSaveBill.setEnabled(true);
                        if (exception != null && exception.getMessage() != null && !exception.getMessage().isEmpty()) {
                            Toast.makeText(AddBillActivity.this, exception.getMessage(), Toast.LENGTH_LONG).show();
                        } else {
                            Toast.makeText(AddBillActivity.this, R.string.update_failed, Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            });
        } else {
            UtilityBill bill = new UtilityBill(
                    propertyId,
                    unitId,
                    tenancyId,
                    selectedBillType,
                    billingMonth,
                    dueDate,
                    amountDue,
                    0.0,
                    amountDue,
                    "UNPAID",
                    meterNumber,
                    prevReading,
                    currReading,
                    unitsConsumed,
                    ratePerUnit,
                    fixedCharge,
                    vatTax,
                    billNumber,
                    null,
                    null,
                    notes,
                    currentTime,
                    currentTime
            );

            repository.createBill(bill, new UtilityBillRepository.DatabaseCallback<Long>() {
                @Override
                public void onSuccess(Long id) {
                    runOnUiThread(() -> {
                        btnSaveBill.setEnabled(true);
                        Toast.makeText(AddBillActivity.this, R.string.bill_created_success, Toast.LENGTH_SHORT).show();
                        setResult(RESULT_OK);
                        finish();
                    });
                }

                @Override
                public void onError(Exception exception) {
                    runOnUiThread(() -> {
                        btnSaveBill.setEnabled(true);
                        if (exception != null && exception.getMessage() != null && !exception.getMessage().isEmpty()) {
                            Toast.makeText(AddBillActivity.this, exception.getMessage(), Toast.LENGTH_LONG).show();
                        } else {
                            Toast.makeText(AddBillActivity.this, R.string.save_failed, Toast.LENGTH_SHORT).show();
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
        if (view == null || view.getText() == null) {
            return "";
        }
        return view.getText().toString().trim();
    }

    private void clearErrors() {
        layoutSelectBillProperty.setError(null);
        layoutSelectBillUnit.setError(null);
        layoutBillAmount.setError(null);
        layoutBillingMonth.setError(null);
        layoutBillDueDate.setError(null);
    }
}

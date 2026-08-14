package com.rob.houserental;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.ArrayAdapter;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.MaterialAutoCompleteTextView;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.rob.houserental.adapter.UnitAdapter;
import com.rob.houserental.model.Property;
import com.rob.houserental.model.Unit;
import com.rob.houserental.repository.PropertyRepository;
import com.rob.houserental.repository.UnitRepository;

public class AddUnitActivity extends AppCompatActivity {

    private TextInputLayout layoutUnitNumber;
    private TextInputLayout layoutUnitFloor;
    private TextInputLayout layoutMonthlyRent;

    private TextInputEditText etUnitNumber;
    private MaterialAutoCompleteTextView autoUnitFloor;
    private TextInputEditText etMonthlyRent;
    private TextInputEditText etSecurityDeposit;
    private TextInputEditText etUnitNotes;

    private MaterialAutoCompleteTextView autoUnitType;
    private MaterialAutoCompleteTextView autoUnitStatus;

    private MaterialButton btnSaveUnit;

    private UnitRepository repository;
    private PropertyRepository propertyRepository;

    private long propertyId = -1;
    private long unitId = -1;
    private boolean isEditMode = false;
    private Unit editingUnit;
    private int maxPropertyFloors = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_add_unit);

        propertyId = getIntent().getLongExtra("property_id", -1);
        unitId = getIntent().getLongExtra("unit_id", -1);

        initializeViews();

        setupToolbar();

        setupUnitTypeDropdown();

        setupStatusDropdown();

        repository = new UnitRepository(getApplicationContext());
        propertyRepository = new PropertyRepository(getApplicationContext());

        loadPropertyFloors();

        checkEditMode();

        btnSaveUnit.setOnClickListener(v -> saveUnit());
    }

    private void initializeViews() {
        layoutUnitNumber = findViewById(R.id.layoutUnitNumber);
        layoutUnitFloor = findViewById(R.id.layoutUnitFloor);
        layoutMonthlyRent = findViewById(R.id.layoutMonthlyRent);

        etUnitNumber = findViewById(R.id.etUnitNumber);
        autoUnitFloor = findViewById(R.id.autoUnitFloor);
        autoUnitType = findViewById(R.id.autoUnitType);
        etMonthlyRent = findViewById(R.id.etMonthlyRent);
        etSecurityDeposit = findViewById(R.id.etSecurityDeposit);
        autoUnitStatus = findViewById(R.id.autoUnitStatus);
        etUnitNotes = findViewById(R.id.etUnitNotes);

        btnSaveUnit = findViewById(R.id.btnSaveUnit);

        autoUnitFloor.setOnClickListener(v -> autoUnitFloor.showDropDown());
    }

    private void setupToolbar() {
        MaterialToolbar toolbar = findViewById(R.id.toolbarAddUnit);
        toolbar.setNavigationOnClickListener(v -> finish());
    }

    private final String[] unitTypeKeys = {
            "Apartment",
            "Flat",
            "Room",
            "Shop",
            "Office",
            "Parking",
            "Other"
    };

    private final String[] statusKeys = {
            "VACANT",
            "OCCUPIED",
            "RESERVED",
            "MAINTENANCE"
    };

    private void setupUnitTypeDropdown() {
        String[] displayLabels = new String[unitTypeKeys.length];
        for (int i = 0; i < unitTypeKeys.length; i++) {
            displayLabels[i] = UnitAdapter.getUnitTypeDisplay(this, unitTypeKeys[i]);
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_dropdown_item_1line,
                displayLabels
        );

        autoUnitType.setAdapter(adapter);
        autoUnitType.setText(displayLabels[0], false);
    }

    private void setupStatusDropdown() {
        String[] displayLabels = new String[statusKeys.length];
        for (int i = 0; i < statusKeys.length; i++) {
            displayLabels[i] = UnitAdapter.getStatusDisplay(this, statusKeys[i]);
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_dropdown_item_1line,
                displayLabels
        );

        autoUnitStatus.setAdapter(adapter);
        autoUnitStatus.setText(displayLabels[0], false);
    }

    private void checkEditMode() {
        if (unitId == -1) {
            return;
        }

        isEditMode = true;

        editingUnit = new Unit();
        editingUnit.setId(unitId);
        editingUnit.setPropertyId(propertyId);

        String unitNumber = getIntent().getStringExtra("unit_number");
        int floor = getIntent().getIntExtra("unit_floor", 0);
        String unitType = getIntent().getStringExtra("unit_type");
        double monthlyRent = getIntent().getDoubleExtra("unit_monthly_rent", 0.0);
        double securityDeposit = getIntent().getDoubleExtra("unit_security_deposit", 0.0);
        String status = getIntent().getStringExtra("unit_status");
        String notes = getIntent().getStringExtra("unit_notes");
        long createdAt = getIntent().getLongExtra("unit_created_at", System.currentTimeMillis());

        editingUnit.setUnitNumber(unitNumber != null ? unitNumber : "");
        editingUnit.setFloor(floor);
        editingUnit.setUnitType(unitType != null ? unitType : "Apartment");
        editingUnit.setMonthlyRent(monthlyRent);
        editingUnit.setSecurityDeposit(securityDeposit);
        editingUnit.setStatus(status != null ? status : "VACANT");
        editingUnit.setNotes(notes != null ? notes : "");
        editingUnit.setCreatedAt(createdAt);

        populateEditFields();

        MaterialToolbar toolbar = findViewById(R.id.toolbarAddUnit);
        toolbar.setTitle(R.string.edit_unit_title);
        btnSaveUnit.setText(R.string.update_unit);
    }

    private void loadPropertyFloors() {
        if (propertyId == -1) return;
        propertyRepository.getPropertyById(propertyId, new PropertyRepository.DatabaseCallback<Property>() {
            @Override
            public void onSuccess(Property property) {
                if (property != null && property.getNumberOfFloors() > 0) {
                    maxPropertyFloors = property.getNumberOfFloors();
                    runOnUiThread(() -> setupFloorDropdown());
                }
            }

            @Override
            public void onError(Exception exception) {
            }
        });
    }

    private void setupFloorDropdown() {
        if (maxPropertyFloors <= 0) return;
        java.util.List<String> floorOptions = new java.util.ArrayList<>();
        for (int i = 1; i <= maxPropertyFloors; i++) {
            floorOptions.add(String.valueOf(i));
        }
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_dropdown_item_1line,
                floorOptions
        );
        autoUnitFloor.setAdapter(adapter);

        if (editingUnit != null && editingUnit.getFloor() > 0) {
            if (editingUnit.getFloor() > maxPropertyFloors) {
                layoutUnitFloor.setError(getString(R.string.floor_invalid_for_property));
            } else {
                autoUnitFloor.setText(String.valueOf(editingUnit.getFloor()), false);
            }
        }
    }

    private void populateEditFields() {
        if (editingUnit == null) {
            return;
        }

        etUnitNumber.setText(editingUnit.getUnitNumber());

        if (editingUnit.getFloor() > 0) {
            if (maxPropertyFloors > 0 && editingUnit.getFloor() > maxPropertyFloors) {
                layoutUnitFloor.setError(getString(R.string.floor_invalid_for_property));
            } else {
                autoUnitFloor.setText(String.valueOf(editingUnit.getFloor()), false);
            }
        }

        if (!TextUtils.isEmpty(editingUnit.getUnitType())) {
            autoUnitType.setText(UnitAdapter.getUnitTypeDisplay(this, editingUnit.getUnitType()), false);
        }

        if (editingUnit.getMonthlyRent() > 0) {
            etMonthlyRent.setText(String.valueOf(editingUnit.getMonthlyRent()));
        }

        if (editingUnit.getSecurityDeposit() > 0) {
            etSecurityDeposit.setText(String.valueOf(editingUnit.getSecurityDeposit()));
        }

        if (!TextUtils.isEmpty(editingUnit.getStatus())) {
            autoUnitStatus.setText(UnitAdapter.getStatusDisplay(this, editingUnit.getStatus()), false);
        }

        etUnitNotes.setText(editingUnit.getNotes());
    }

    private void saveUnit() {
        clearErrors();

        if (propertyId == -1) {
            btnSaveUnit.setError(getString(R.string.invalid_property));
            return;
        }

        String unitNumber = getText(etUnitNumber);
        String floorText = getText(autoUnitFloor);
        String selectedTypeDisplay = getText(autoUnitType);
        String unitType = "Apartment";
        for (String key : unitTypeKeys) {
            if (UnitAdapter.getUnitTypeDisplay(this, key).equalsIgnoreCase(selectedTypeDisplay) || key.equalsIgnoreCase(selectedTypeDisplay)) {
                unitType = key;
                break;
            }
        }

        String rentText = getText(etMonthlyRent);
        String depositText = getText(etSecurityDeposit);
        String selectedStatusDisplay = getText(autoUnitStatus);
        String status = "VACANT";
        for (String key : statusKeys) {
            if (UnitAdapter.getStatusDisplay(this, key).equalsIgnoreCase(selectedStatusDisplay) || key.equalsIgnoreCase(selectedStatusDisplay)) {
                status = key;
                break;
            }
        }
        String notes = getText(etUnitNotes);

        if (TextUtils.isEmpty(unitNumber)) {
            layoutUnitNumber.setError(getString(R.string.unit_number_required));
            etUnitNumber.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(floorText)) {
            layoutUnitFloor.setError(getString(R.string.floor_required));
            autoUnitFloor.requestFocus();
            return;
        }

        int floor = 0;
        try {
            floor = Integer.parseInt(floorText);
        } catch (NumberFormatException e) {
            layoutUnitFloor.setError(getString(R.string.invalid_floor));
            return;
        }

        if (maxPropertyFloors > 0 && (floor < 1 || floor > maxPropertyFloors)) {
            layoutUnitFloor.setError(getString(R.string.floor_invalid_for_property));
            return;
        }

        if (TextUtils.isEmpty(rentText)) {
            layoutMonthlyRent.setError(getString(R.string.rent_required));
            etMonthlyRent.requestFocus();
            return;
        }

        double monthlyRent;
        try {
            monthlyRent = Double.parseDouble(rentText);
        } catch (NumberFormatException e) {
            layoutMonthlyRent.setError(getString(R.string.invalid_rent));
            return;
        }

        double securityDeposit = 0;
        if (!TextUtils.isEmpty(depositText)) {
            try {
                securityDeposit = Double.parseDouble(depositText);
            } catch (NumberFormatException e) {
                etSecurityDeposit.setError(getString(R.string.invalid_deposit));
                return;
            }
        }

        long currentTime = System.currentTimeMillis();
        final Unit unit;

        if (isEditMode && editingUnit != null) {
            unit = editingUnit;
            unit.setPropertyId(propertyId);
            unit.setUnitNumber(unitNumber);
            unit.setFloor(floor);
            unit.setUnitType(unitType);
            unit.setMonthlyRent(monthlyRent);
            unit.setSecurityDeposit(securityDeposit);
            unit.setStatus(status);
            unit.setNotes(notes);
            unit.setUpdatedAt(currentTime);
        } else {
            unit = new Unit();
            unit.setPropertyId(propertyId);
            unit.setUnitNumber(unitNumber);
            unit.setFloor(floor);
            unit.setUnitType(unitType);
            unit.setMonthlyRent(monthlyRent);
            unit.setSecurityDeposit(securityDeposit);
            unit.setStatus(status);
            unit.setNotes(notes);
            unit.setCreatedAt(currentTime);
            unit.setUpdatedAt(currentTime);
        }

        final long excludeId = (isEditMode && editingUnit != null) ? editingUnit.getId() : -1;
        repository.countDuplicateUnitNumber(propertyId, unitNumber, excludeId, new UnitRepository.DatabaseCallback<Integer>() {
            @Override
            public void onSuccess(Integer count) {
                if (count != null && count > 0) {
                    runOnUiThread(() -> {
                        layoutUnitNumber.setError(getString(R.string.duplicate_unit_number));
                        etUnitNumber.requestFocus();
                    });
                } else {
                    runOnUiThread(() -> executeSaveUnit(unit));
                }
            }

            @Override
            public void onError(Exception exception) {
                runOnUiThread(() -> executeSaveUnit(unit));
            }
        });
    }

    private void executeSaveUnit(Unit unit) {
        btnSaveUnit.setEnabled(false);

        if (isEditMode) {
            repository.update(unit, new UnitRepository.DatabaseCallback<Void>() {
                @Override
                public void onSuccess(Void result) {
                    runOnUiThread(() -> {
                        btnSaveUnit.setEnabled(true);
                        Intent resultIntent = new Intent();
                        resultIntent.putExtra("unit_id", unit.getId());
                        setResult(RESULT_OK, resultIntent);
                        finish();
                    });
                }

                @Override
                public void onError(Exception exception) {
                    runOnUiThread(() -> {
                        btnSaveUnit.setEnabled(true);
                        if (isConstraintException(exception)) {
                            layoutUnitNumber.setError(getString(R.string.duplicate_unit_number));
                            etUnitNumber.requestFocus();
                        } else {
                            btnSaveUnit.setError(getString(R.string.update_failed));
                        }
                    });
                }
            });
        } else {
            repository.insert(unit, new UnitRepository.DatabaseCallback<Long>() {
                @Override
                public void onSuccess(Long id) {
                    runOnUiThread(() -> {
                        btnSaveUnit.setEnabled(true);
                        Intent resultIntent = new Intent();
                        resultIntent.putExtra("unit_id", id);
                        setResult(RESULT_OK, resultIntent);
                        finish();
                    });
                }

                @Override
                public void onError(Exception exception) {
                    runOnUiThread(() -> {
                        btnSaveUnit.setEnabled(true);
                        if (isConstraintException(exception)) {
                            layoutUnitNumber.setError(getString(R.string.duplicate_unit_number));
                            etUnitNumber.requestFocus();
                        } else {
                            btnSaveUnit.setError(getString(R.string.save_failed));
                        }
                    });
                }
            });
        }
    }

    private boolean isConstraintException(Exception exception) {
        if (exception == null) return false;
        if (exception instanceof android.database.sqlite.SQLiteConstraintException) return true;
        String msg = exception.getMessage();
        return msg != null && (msg.contains("UNIQUE") || msg.contains("constraint") || msg.contains("2067"));
    }

    private String getText(android.widget.TextView view) {
        if (view.getText() == null) {
            return "";
        }
        return view.getText().toString().trim();
    }

    private void clearErrors() {
        layoutUnitNumber.setError(null);
        layoutMonthlyRent.setError(null);
        layoutUnitFloor.setError(null);
        etSecurityDeposit.setError(null);
        btnSaveUnit.setError(null);
    }
}
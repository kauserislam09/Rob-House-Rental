package com.rob.houserental;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.MaterialAutoCompleteTextView;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.rob.houserental.model.Tenant;
import com.rob.houserental.repository.TenantRepository;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class AddTenantActivity extends AppCompatActivity {

    private TextInputLayout layoutFullName;
    private TextInputLayout layoutPhoneNumber;
    private TextInputLayout layoutDateOfBirth;

    private TextInputEditText etFullName;
    private TextInputEditText etPhoneNumber;
    private TextInputEditText etAlternativePhone;
    private TextInputEditText etEmail;
    private TextInputEditText etDateOfBirth;
    private TextInputEditText etNidNumber;
    private TextInputEditText etPassportNumber;
    private TextInputEditText etOccupation;
    private TextInputEditText etEmergencyName;
    private TextInputEditText etEmergencyPhone;
    private TextInputEditText etFamilyCount;
    private TextInputEditText etPresentAddress;
    private TextInputEditText etPermanentAddress;
    private MaterialAutoCompleteTextView autoTenantStatus;
    private TextInputEditText etTenantNotes;

    private MaterialButton btnSaveTenant;

    private TenantRepository repository;
    private boolean isEditMode = false;
    private long tenantId = -1;
    private Tenant editingTenant;

    private final Calendar dobCalendar = Calendar.getInstance();
    private static final SimpleDateFormat dobFormat = new SimpleDateFormat("dd MMM yyyy", Locale.getDefault());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_add_tenant);

        tenantId = getIntent().getLongExtra("tenant_id", -1);

        initializeViews();

        setupToolbar();

        setupStatusDropdown();

        setupDatePicker();

        repository = new TenantRepository(getApplicationContext());

        checkEditMode();

        btnSaveTenant.setOnClickListener(v -> saveTenant());
    }

    private void initializeViews() {
        layoutFullName = findViewById(R.id.layoutFullName);
        layoutPhoneNumber = findViewById(R.id.layoutPhoneNumber);
        layoutDateOfBirth = findViewById(R.id.layoutDateOfBirth);

        etFullName = findViewById(R.id.etFullName);
        etPhoneNumber = findViewById(R.id.etPhoneNumber);
        etAlternativePhone = findViewById(R.id.etAlternativePhone);
        etEmail = findViewById(R.id.etEmail);
        etDateOfBirth = findViewById(R.id.etDateOfBirth);
        etNidNumber = findViewById(R.id.etNidNumber);
        etPassportNumber = findViewById(R.id.etPassportNumber);
        etOccupation = findViewById(R.id.etOccupation);
        etEmergencyName = findViewById(R.id.etEmergencyName);
        etEmergencyPhone = findViewById(R.id.etEmergencyPhone);
        etFamilyCount = findViewById(R.id.etFamilyCount);
        etPresentAddress = findViewById(R.id.etPresentAddress);
        etPermanentAddress = findViewById(R.id.etPermanentAddress);
        autoTenantStatus = findViewById(R.id.autoTenantStatus);
        etTenantNotes = findViewById(R.id.etTenantNotes);

        btnSaveTenant = findViewById(R.id.btnSaveTenant);
    }

    private void setupToolbar() {
        MaterialToolbar toolbar = findViewById(R.id.toolbarAddTenant);
        toolbar.setNavigationOnClickListener(v -> finish());
    }

    private void setupStatusDropdown() {
        String[] statuses = {
                "ACTIVE",
                "INACTIVE",
                "ARCHIVED"
        };

        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_dropdown_item_1line,
                statuses
        );

        autoTenantStatus.setAdapter(adapter);
        autoTenantStatus.setText(statuses[0], false);
    }

    private void setupDatePicker() {
        // Set default calendar to approx 25 years ago
        dobCalendar.add(Calendar.YEAR, -25);

        DatePickerDialog.OnDateSetListener dateSetListener = (view, year, month, dayOfMonth) -> {
            dobCalendar.set(Calendar.YEAR, year);
            dobCalendar.set(Calendar.MONTH, month);
            dobCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
            etDateOfBirth.setText(dobFormat.format(dobCalendar.getTime()));
        };

        etDateOfBirth.setOnClickListener(v -> {
            new DatePickerDialog(
                    AddTenantActivity.this,
                    dateSetListener,
                    dobCalendar.get(Calendar.YEAR),
                    dobCalendar.get(Calendar.MONTH),
                    dobCalendar.get(Calendar.DAY_OF_MONTH)
            ).show();
        });
    }

    private void checkEditMode() {
        if (tenantId == -1) {
            return;
        }

        isEditMode = true;

        repository.getTenantById(tenantId, new TenantRepository.DatabaseCallback<Tenant>() {
            @Override
            public void onSuccess(Tenant tenant) {
                if (tenant != null) {
                    editingTenant = tenant;
                    runOnUiThread(() -> populateEditFields());
                }
            }

            @Override
            public void onError(Exception exception) {
                // If query fails, intent data fallback
            }
        });

        MaterialToolbar toolbar = findViewById(R.id.toolbarAddTenant);
        toolbar.setTitle(R.string.edit_tenant_title);
        btnSaveTenant.setText(R.string.update_tenant);
    }

    private void populateEditFields() {
        if (editingTenant == null) {
            return;
        }

        etFullName.setText(editingTenant.getFullName());
        etPhoneNumber.setText(editingTenant.getPhoneNumber());
        etAlternativePhone.setText(editingTenant.getAlternativePhone());
        etEmail.setText(editingTenant.getEmail());
        etDateOfBirth.setText(editingTenant.getDateOfBirth());
        etNidNumber.setText(editingTenant.getNidNumber());
        etPassportNumber.setText(editingTenant.getPassportNumber());
        etOccupation.setText(editingTenant.getOccupation());
        etEmergencyName.setText(editingTenant.getEmergencyContactName());
        etEmergencyPhone.setText(editingTenant.getEmergencyContactPhone());

        if (editingTenant.getFamilyMemberCount() > 0) {
            etFamilyCount.setText(String.valueOf(editingTenant.getFamilyMemberCount()));
        }

        etPresentAddress.setText(editingTenant.getPresentAddress());
        etPermanentAddress.setText(editingTenant.getPermanentAddress());

        if (!TextUtils.isEmpty(editingTenant.getStatus())) {
            autoTenantStatus.setText(editingTenant.getStatus(), false);
        }

        etTenantNotes.setText(editingTenant.getNotes());
    }

    private void saveTenant() {
        clearErrors();

        String fullName = getText(etFullName);
        String phoneNumber = getText(etPhoneNumber);
        String alternativePhone = getText(etAlternativePhone);
        String email = getText(etEmail);
        String dateOfBirth = getText(etDateOfBirth);
        String nidNumber = getText(etNidNumber);
        String passportNumber = getText(etPassportNumber);
        String occupation = getText(etOccupation);
        String emergencyName = getText(etEmergencyName);
        String emergencyPhone = getText(etEmergencyPhone);
        String familyCountText = getText(etFamilyCount);
        String presentAddress = getText(etPresentAddress);
        String permanentAddress = getText(etPermanentAddress);
        String status = getText(autoTenantStatus);
        String notes = getText(etTenantNotes);

        if (TextUtils.isEmpty(fullName)) {
            layoutFullName.setError(getString(R.string.name_required));
            etFullName.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(phoneNumber)) {
            layoutPhoneNumber.setError(getString(R.string.phone_required));
            etPhoneNumber.requestFocus();
            return;
        }

        int familyCount = 1;
        if (!TextUtils.isEmpty(familyCountText)) {
            try {
                familyCount = Integer.parseInt(familyCountText);
            } catch (NumberFormatException ignored) {
            }
        }

        long currentTime = System.currentTimeMillis();
        Tenant tenant;

        if (isEditMode && editingTenant != null) {
            tenant = editingTenant;
            tenant.setFullName(fullName);
            tenant.setPhoneNumber(phoneNumber);
            tenant.setAlternativePhone(alternativePhone);
            tenant.setEmail(email);
            tenant.setDateOfBirth(dateOfBirth);
            tenant.setNidNumber(nidNumber);
            tenant.setPassportNumber(passportNumber);
            tenant.setOccupation(occupation);
            tenant.setEmergencyContactName(emergencyName);
            tenant.setEmergencyContactPhone(emergencyPhone);
            tenant.setFamilyMemberCount(familyCount);
            tenant.setPresentAddress(presentAddress);
            tenant.setPermanentAddress(permanentAddress);
            tenant.setStatus(status);
            tenant.setNotes(notes);
            tenant.setUpdatedAt(currentTime);
        } else {
            tenant = new Tenant();
            tenant.setFullName(fullName);
            tenant.setPhoneNumber(phoneNumber);
            tenant.setAlternativePhone(alternativePhone);
            tenant.setEmail(email);
            tenant.setDateOfBirth(dateOfBirth);
            tenant.setNidNumber(nidNumber);
            tenant.setPassportNumber(passportNumber);
            tenant.setOccupation(occupation);
            tenant.setEmergencyContactName(emergencyName);
            tenant.setEmergencyContactPhone(emergencyPhone);
            tenant.setFamilyMemberCount(familyCount);
            tenant.setPresentAddress(presentAddress);
            tenant.setPermanentAddress(permanentAddress);
            tenant.setStatus(status);
            tenant.setNotes(notes);
            tenant.setCreatedAt(currentTime);
            tenant.setUpdatedAt(currentTime);
        }

        btnSaveTenant.setEnabled(false);

        if (isEditMode) {
            repository.update(tenant, new TenantRepository.DatabaseCallback<Void>() {
                @Override
                public void onSuccess(Void result) {
                    runOnUiThread(() -> {
                        btnSaveTenant.setEnabled(true);
                        Toast.makeText(AddTenantActivity.this, R.string.tenant_updated_success, Toast.LENGTH_SHORT).show();
                        Intent resultIntent = new Intent();
                        resultIntent.putExtra("tenant_id", tenant.getId());
                        setResult(RESULT_OK, resultIntent);
                        finish();
                    });
                }

                @Override
                public void onError(Exception exception) {
                    runOnUiThread(() -> {
                        btnSaveTenant.setEnabled(true);
                        btnSaveTenant.setError(getString(R.string.update_failed));
                    });
                }
            });
        } else {
            repository.insert(tenant, new TenantRepository.DatabaseCallback<Long>() {
                @Override
                public void onSuccess(Long id) {
                    runOnUiThread(() -> {
                        btnSaveTenant.setEnabled(true);
                        Toast.makeText(AddTenantActivity.this, R.string.tenant_saved_success, Toast.LENGTH_SHORT).show();
                        Intent resultIntent = new Intent();
                        resultIntent.putExtra("tenant_id", id);
                        setResult(RESULT_OK, resultIntent);
                        finish();
                    });
                }

                @Override
                public void onError(Exception exception) {
                    runOnUiThread(() -> {
                        btnSaveTenant.setEnabled(true);
                        btnSaveTenant.setError(getString(R.string.save_failed));
                    });
                }
            });
        }
    }

    private String getText(android.widget.TextView view) {
        if (view.getText() == null) {
            return "";
        }
        return view.getText().toString().trim();
    }

    private void clearErrors() {
        layoutFullName.setError(null);
        layoutPhoneNumber.setError(null);
        btnSaveTenant.setError(null);
    }
}

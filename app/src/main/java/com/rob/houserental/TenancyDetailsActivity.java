package com.rob.houserental;

import android.app.DatePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.rob.houserental.adapter.TenancyAdapter;
import com.rob.houserental.adapter.UnitAdapter;
import com.rob.houserental.data.AppDatabase;
import com.rob.houserental.model.Property;
import com.rob.houserental.model.Tenancy;
import com.rob.houserental.model.TenancyWithDetails;
import com.rob.houserental.repository.TenancyRepository;

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class TenancyDetailsActivity extends AppCompatActivity {

    private TextView tvTenancyDetailsTenantName;
    private TextView tvTenancyDetailsStatusBadge;

    private TextView tvTenancyDetailsProperty;
    private TextView tvTenancyDetailsUnit;

    private TextView tvTenancyDetailsPhone;
    private TextView tvTenancyDetailsNid;
    private MaterialButton btnViewTenantProfile;

    private TextView tvTenancyDetailsRent;
    private TextView tvTenancyDetailsCumulativeOutstanding;
    private TextView tvTenancyDetailsStartDate;
    private TextView tvTenancyDetailsEndDate;
    private TextView tvTenancyDetailsServiceCharge;
    private TextView tvTenancyDetailsDeposit;
    private TextView tvTenancyDetailsAdvance;
    private TextView tvTenancyDetailsAgreementNumber;
    private TextView tvTenancyDetailsNotes;

    private MaterialButton btnEditTenancy;
    private MaterialButton btnEndTenancy;
    private MaterialButton btnCancelTenancy;

    private TenancyRepository repository;
    private com.rob.houserental.repository.RentRepository rentRepository;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    private long tenancyId = -1;
    private TenancyWithDetails currentDetails;

    private static final NumberFormat currencyFormatter = NumberFormat.getNumberInstance(Locale.getDefault());
    private static final SimpleDateFormat dateFormat = new SimpleDateFormat("dd MMM yyyy", Locale.getDefault());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_tenancy_details);

        tenancyId = getIntent().getLongExtra("tenancy_id", -1);

        initializeViews();

        setupToolbar();

        repository = new TenancyRepository(getApplicationContext());
        rentRepository = new com.rob.houserental.repository.RentRepository(getApplicationContext());

        loadTenancyDetails();

        setupListeners();
    }

    private void initializeViews() {
        tvTenancyDetailsTenantName = findViewById(R.id.tvTenancyDetailsTenantName);
        tvTenancyDetailsStatusBadge = findViewById(R.id.tvTenancyDetailsStatusBadge);

        tvTenancyDetailsProperty = findViewById(R.id.tvTenancyDetailsProperty);
        tvTenancyDetailsUnit = findViewById(R.id.tvTenancyDetailsUnit);

        tvTenancyDetailsPhone = findViewById(R.id.tvTenancyDetailsPhone);
        tvTenancyDetailsNid = findViewById(R.id.tvTenancyDetailsNid);
        btnViewTenantProfile = findViewById(R.id.btnViewTenantProfile);

        tvTenancyDetailsRent = findViewById(R.id.tvTenancyDetailsRent);
        tvTenancyDetailsCumulativeOutstanding = findViewById(R.id.tvTenancyDetailsCumulativeOutstanding);
        tvTenancyDetailsStartDate = findViewById(R.id.tvTenancyDetailsStartDate);
        tvTenancyDetailsEndDate = findViewById(R.id.tvTenancyDetailsEndDate);
        tvTenancyDetailsServiceCharge = findViewById(R.id.tvTenancyDetailsServiceCharge);
        tvTenancyDetailsDeposit = findViewById(R.id.tvTenancyDetailsDeposit);
        tvTenancyDetailsAdvance = findViewById(R.id.tvTenancyDetailsAdvance);
        tvTenancyDetailsAgreementNumber = findViewById(R.id.tvTenancyDetailsAgreementNumber);
        tvTenancyDetailsNotes = findViewById(R.id.tvTenancyDetailsNotes);

        btnEditTenancy = findViewById(R.id.btnEditTenancy);
        btnEndTenancy = findViewById(R.id.btnEndTenancy);
        btnCancelTenancy = findViewById(R.id.btnCancelTenancy);
    }

    private void setupToolbar() {
        MaterialToolbar toolbar = findViewById(R.id.toolbarTenancyDetails);
        toolbar.setNavigationOnClickListener(v -> finish());
    }

    private void setupListeners() {
        btnViewTenantProfile.setOnClickListener(v -> {
            if (currentDetails != null && currentDetails.tenant != null) {
                Intent intent = new Intent(TenancyDetailsActivity.this, TenantDetailsActivity.class);
                intent.putExtra("tenant_id", currentDetails.tenant.getId());
                startActivity(intent);
            }
        });

        btnEditTenancy.setOnClickListener(v -> {
            if (currentDetails != null && currentDetails.tenancy != null) {
                Intent intent = new Intent(TenancyDetailsActivity.this, AddTenancyActivity.class);
                intent.putExtra("tenancy_id", currentDetails.tenancy.getId());
                startActivity(intent);
            }
        });

        btnEndTenancy.setOnClickListener(v -> {
            if (currentDetails != null && currentDetails.tenancy != null) {
                showEndTenancyDialog();
            }
        });

        btnCancelTenancy.setOnClickListener(v -> {
            if (currentDetails != null && currentDetails.tenancy != null) {
                showCancelTenancyDialog();
            }
        });
    }

    private void loadTenancyDetails() {
        if (tenancyId == -1) {
            return;
        }

        repository.getTenancyWithDetailsById(tenancyId, new TenancyRepository.DatabaseCallback<TenancyWithDetails>() {
            @Override
            public void onSuccess(TenancyWithDetails details) {
                if (details != null) {
                    currentDetails = details;
                    runOnUiThread(() -> displayTenancyDetails(details));
                }
            }

            @Override
            public void onError(Exception exception) {
            }
        });

        String currentBillingMonth = new SimpleDateFormat("yyyy-MM", Locale.getDefault()).format(Calendar.getInstance().getTime());
        rentRepository.getTenancyCumulativeOutstandingRent(tenancyId, currentBillingMonth, new com.rob.houserental.repository.RentRepository.DatabaseCallback<Double>() {
            @Override
            public void onSuccess(Double outstanding) {
                runOnUiThread(() -> {
                    if (tvTenancyDetailsCumulativeOutstanding != null) {
                        String curr = getString(R.string.currency_symbol);
                        tvTenancyDetailsCumulativeOutstanding.setText(curr + currencyFormatter.format(outstanding != null ? outstanding : 0.0));
                    }
                });
            }

            @Override
            public void onError(Exception exception) {
            }
        });
    }

    private void displayTenancyDetails(TenancyWithDetails details) {
        Tenancy tenancy = details.tenancy;

        String noneVal = getString(R.string.none_value);
        String notSetVal = getString(R.string.not_set);

        // Tenant Header
        if (details.tenant != null) {
            tvTenancyDetailsTenantName.setText(details.tenant.getFullName());
            tvTenancyDetailsPhone.setText(details.tenant.getPhoneNumber() != null ? details.tenant.getPhoneNumber() : noneVal);
            tvTenancyDetailsNid.setText(details.tenant.getNidNumber() != null ? details.tenant.getNidNumber() : notSetVal);
        } else {
            tvTenancyDetailsTenantName.setText(getString(R.string.prefix_tenant_hash, tenancy.getTenantId()));
            tvTenancyDetailsPhone.setText(noneVal);
            tvTenancyDetailsNid.setText(notSetVal);
        }

        // Status Badge
        String status = tenancy.getStatus() != null ? tenancy.getStatus().trim().toUpperCase() : "ACTIVE";
        tvTenancyDetailsStatusBadge.setText(TenancyAdapter.getStatusDisplay(this, status));
        applyStatusBadgeStyle(this, tvTenancyDetailsStatusBadge, status);

        // Unit info & Property fetch
        if (details.unit != null) {
            String unitInfo = getString(R.string.prefix_unit_floor_type_format,
                    details.unit.getUnitNumber(),
                    details.unit.getFloor(),
                    UnitAdapter.getUnitTypeDisplay(this, details.unit.getUnitType()));
            tvTenancyDetailsUnit.setText(unitInfo);

            executor.execute(() -> {
                try {
                    AppDatabase db = AppDatabase.getInstance(getApplicationContext());
                    Property property = db.propertyDao().getPropertyById(details.unit.getPropertyId());
                    if (property != null) {
                        runOnUiThread(() -> tvTenancyDetailsProperty.setText(property.getName()));
                    }
                } catch (Exception ignored) {
                }
            });
        } else {
            tvTenancyDetailsUnit.setText(getString(R.string.prefix_unit_hash, tenancy.getUnitId()));
            tvTenancyDetailsProperty.setText(getString(R.string.property_label));
        }

        // Financial & Terms
        String curr = getString(R.string.currency_symbol);
        tvTenancyDetailsRent.setText(getString(R.string.prefix_rent_format, curr + currencyFormatter.format(tenancy.getMonthlyRent()), getString(R.string.per_month)));
        tvTenancyDetailsStartDate.setText(tenancy.getStartDate() != null ? tenancy.getStartDate() : notSetVal);

        if (tenancy.getEndDate() != null && !tenancy.getEndDate().trim().isEmpty()) {
            tvTenancyDetailsEndDate.setText(tenancy.getEndDate());
        } else if ("ACTIVE".equalsIgnoreCase(status)) {
            tvTenancyDetailsEndDate.setText(R.string.present_active);
        } else {
            tvTenancyDetailsEndDate.setText(noneVal);
        }

        tvTenancyDetailsServiceCharge.setText(curr + currencyFormatter.format(tenancy.getServiceCharge()));
        tvTenancyDetailsDeposit.setText(curr + currencyFormatter.format(tenancy.getSecurityDeposit()));
        tvTenancyDetailsAdvance.setText(curr + currencyFormatter.format(tenancy.getAdvanceAmount()));
        tvTenancyDetailsAgreementNumber.setText(tenancy.getAgreementNumber() != null && !tenancy.getAgreementNumber().isEmpty() ? tenancy.getAgreementNumber() : noneVal);
        tvTenancyDetailsNotes.setText(tenancy.getNotes() != null && !tenancy.getNotes().isEmpty() ? tenancy.getNotes() : getString(R.string.no_notes));

        // Action Buttons Visibility
        if ("ACTIVE".equalsIgnoreCase(status)) {
            btnEndTenancy.setVisibility(View.VISIBLE);
            btnCancelTenancy.setVisibility(View.VISIBLE);
        } else {
            btnEndTenancy.setVisibility(View.GONE);
            btnCancelTenancy.setVisibility(View.GONE);
        }
    }

    private void applyStatusBadgeStyle(Context context, TextView badge, String status) {
        int bgColor;
        int textColor;

        switch (status) {
            case "ENDED":
                bgColor = ContextCompat.getColor(context, R.color.status_reserved_bg);
                textColor = ContextCompat.getColor(context, R.color.status_reserved_text);
                break;
            case "CANCELLED":
                bgColor = ContextCompat.getColor(context, R.color.status_maintenance_bg);
                textColor = ContextCompat.getColor(context, R.color.status_maintenance_text);
                break;
            case "ACTIVE":
            default:
                bgColor = ContextCompat.getColor(context, R.color.status_vacant_bg);
                textColor = ContextCompat.getColor(context, R.color.status_vacant_text);
                break;
        }

        GradientDrawable shape = new GradientDrawable();
        shape.setCornerRadius(24f);
        shape.setColor(bgColor);
        badge.setBackground(shape);
        badge.setTextColor(textColor);
    }

    private void showEndTenancyDialog() {
        Calendar calendar = Calendar.getInstance();
        String defaultEndDate = dateFormat.format(calendar.getTime());

        new MaterialAlertDialogBuilder(this)
                .setTitle(R.string.end_tenancy)
                .setMessage(R.string.end_tenancy_confirm)
                .setPositiveButton(R.string.end_tenancy, (dialog, which) -> {
                    // Open DatePicker for confirmation of exit date
                    new DatePickerDialog(
                            TenancyDetailsActivity.this,
                            (view, year, month, dayOfMonth) -> {
                                calendar.set(Calendar.YEAR, year);
                                calendar.set(Calendar.MONTH, month);
                                calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                                String chosenEndDate = dateFormat.format(calendar.getTime());

                                repository.endTenancy(currentDetails.tenancy, chosenEndDate, new TenancyRepository.DatabaseCallback<Void>() {
                                    @Override
                                    public void onSuccess(Void result) {
                                        runOnUiThread(() -> {
                                            Toast.makeText(TenancyDetailsActivity.this, R.string.tenancy_ended_success, Toast.LENGTH_SHORT).show();
                                            loadTenancyDetails();
                                        });
                                    }

                                    @Override
                                    public void onError(Exception exception) {
                                        runOnUiThread(() -> Toast.makeText(TenancyDetailsActivity.this, R.string.save_failed, Toast.LENGTH_SHORT).show());
                                    }
                                });
                            },
                            calendar.get(Calendar.YEAR),
                            calendar.get(Calendar.MONTH),
                            calendar.get(Calendar.DAY_OF_MONTH)
                    ).show();
                })
                .setNegativeButton(R.string.cancel, null)
                .show();
    }

    private void showCancelTenancyDialog() {
        new MaterialAlertDialogBuilder(this)
                .setTitle(R.string.cancel_tenancy)
                .setMessage(R.string.cancel_tenancy_confirm_msg)
                .setPositiveButton(R.string.cancel_tenancy, (dialog, which) -> {
                    repository.cancelTenancy(currentDetails.tenancy, new TenancyRepository.DatabaseCallback<Void>() {
                        @Override
                        public void onSuccess(Void result) {
                            runOnUiThread(() -> {
                                Toast.makeText(TenancyDetailsActivity.this, R.string.agreement_cancelled_msg, Toast.LENGTH_SHORT).show();
                                loadTenancyDetails();
                            });
                        }

                        @Override
                        public void onError(Exception exception) {
                            runOnUiThread(() -> Toast.makeText(TenancyDetailsActivity.this, R.string.save_failed, Toast.LENGTH_SHORT).show());
                        }
                    });
                })
                .setNegativeButton(R.string.cancel, null)
                .show();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (repository != null) {
            loadTenancyDetails();
        }
    }
}

package com.rob.houserental;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.MaterialAutoCompleteTextView;
import com.rob.houserental.adapter.PropertyAdapter;
import com.rob.houserental.adapter.UnitAdapter;
import com.rob.houserental.data.AppDatabase;
import com.rob.houserental.financial.FinancialFilterPeriod;
import com.rob.houserental.financial.FinancialRepository;
import com.rob.houserental.model.MaintenanceRecord;
import com.rob.houserental.model.Property;
import com.rob.houserental.model.TenancyWithDetails;
import com.rob.houserental.model.Unit;
import com.rob.houserental.repository.UnitRepository;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.Executors;

public class PropertyDetailsActivity extends AppCompatActivity {

    private static final String TAG = "PropertyDetailsActivity";

    private TextView tvPropertyName;
    private TextView tvPropertyAddress;
    private TextView tvPropertyType;
    private TextView tvPropertyFloors;
    private TextView tvPropertyNotes;
    private TextView tvPropertyStatus;
    private TextView tvPropertyId;
    private MaterialButton btnEditProperty;

    // Operational Summary Views
    private TextView tvSummaryTotalUnits;
    private TextView tvSummaryOccupiedUnits;
    private TextView tvSummaryVacantUnits;
    private TextView tvSummaryOccupancyRate;
    private TextView tvSummaryExpectedRent;
    private TextView tvSummaryCollectedRent;
    private TextView tvSummaryOutstandingRent;
    private TextView tvPropertyMonthExpenses;

    // Unit List & Filter Views
    private TextView tvUnitCount;
    private TextView tvNoUnits;
    private RecyclerView recyclerUnits;
    private MaterialButton btnAddUnit;
    private ChipGroup chipGroupUnitStatus;
    private MaterialAutoCompleteTextView autoFloorFilter;

    private UnitAdapter unitAdapter;
    private UnitRepository unitRepository;
    private FinancialRepository financialRepository;

    private long propertyId = -1;
    private Property currentProperty;

    private List<Unit> allUnitsList = new ArrayList<>();
    private int selectedStatusChipId = R.id.chipUnitAll;
    private int selectedFloorFilter = 0; // 0 = All Floors

    private static final DecimalFormat currencyFormat = new DecimalFormat("#,##,###.##");
    private static final SimpleDateFormat monthFormat = new SimpleDateFormat("yyyy-MM", Locale.getDefault());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_property_details);

        propertyId = getIntent().getLongExtra("property_id", -1);
        if (propertyId <= 0) {
            Log.e(TAG, "Invalid propertyId received: " + propertyId);
            Toast.makeText(this, R.string.unable_to_open_property_details, Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        unitRepository = new UnitRepository(getApplicationContext());
        financialRepository = new FinancialRepository(getApplicationContext());

        initializeViews();
        setupToolbar();
        setupRecyclerView();

        loadPropertyData();
        loadUnits();
        loadOperationalSummary();

        setupListeners();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 1001 && resultCode == RESULT_OK && data != null) {
            String name = data.getStringExtra("property_name");
            String address = data.getStringExtra("property_address");
            String type = data.getStringExtra("property_type");
            int floors = data.getIntExtra("property_floors", 0);
            String notes = data.getStringExtra("property_notes");

            if (tvPropertyName != null) tvPropertyName.setText(name != null ? name : "");
            if (tvPropertyAddress != null) tvPropertyAddress.setText(address != null ? address : "");
            if (tvPropertyType != null) tvPropertyType.setText(PropertyAdapter.getPropertyTypeDisplay(this, type));
            if (tvPropertyFloors != null) tvPropertyFloors.setText(String.valueOf(floors));
            if (tvPropertyNotes != null) tvPropertyNotes.setText(notes != null && !notes.isEmpty() ? notes : getString(R.string.no_notes));
            if (tvPropertyStatus != null) tvPropertyStatus.setText(R.string.status_active);

            if (currentProperty != null) {
                currentProperty.setName(name);
                currentProperty.setAddress(address);
                currentProperty.setPropertyType(type);
                currentProperty.setNumberOfFloors(floors);
                currentProperty.setNotes(notes);
            }
            setupFloorFilterDropdown(floors);
        }

        if (requestCode == 2001 && resultCode == RESULT_OK) {
            loadUnits();
            loadOperationalSummary();
        }
    }

    private void initializeViews() {
        tvPropertyName = findViewById(R.id.tvPropertyName);
        tvPropertyAddress = findViewById(R.id.tvPropertyAddress);
        tvPropertyType = findViewById(R.id.tvPropertyType);
        tvPropertyFloors = findViewById(R.id.tvPropertyFloors);
        tvPropertyNotes = findViewById(R.id.tvPropertyNotes);
        tvPropertyStatus = findViewById(R.id.tvPropertyStatus);
        tvPropertyId = findViewById(R.id.tvPropertyId);
        btnEditProperty = findViewById(R.id.btnEditProperty);

        tvSummaryTotalUnits = findViewById(R.id.tvSummaryTotalUnits);
        tvSummaryOccupiedUnits = findViewById(R.id.tvSummaryOccupiedUnits);
        tvSummaryVacantUnits = findViewById(R.id.tvSummaryVacantUnits);
        tvSummaryOccupancyRate = findViewById(R.id.tvSummaryOccupancyRate);
        tvSummaryExpectedRent = findViewById(R.id.tvSummaryExpectedRent);
        tvSummaryCollectedRent = findViewById(R.id.tvSummaryCollectedRent);
        tvSummaryOutstandingRent = findViewById(R.id.tvSummaryOutstandingRent);
        tvPropertyMonthExpenses = findViewById(R.id.tvPropertyMonthExpenses);

        tvUnitCount = findViewById(R.id.tvUnitCount);
        tvNoUnits = findViewById(R.id.tvNoUnits);
        recyclerUnits = findViewById(R.id.recyclerUnits);
        btnAddUnit = findViewById(R.id.btnAddUnit);
        chipGroupUnitStatus = findViewById(R.id.chipGroupUnitStatus);
        autoFloorFilter = findViewById(R.id.autoFloorFilter);
    }

    private void setupToolbar() {
        MaterialToolbar toolbar = findViewById(R.id.toolbarPropertyDetails);
        if (toolbar != null) {
            toolbar.setNavigationOnClickListener(v -> finish());
        }
    }

    private void setupRecyclerView() {
        unitAdapter = new UnitAdapter();
        if (recyclerUnits != null) {
            recyclerUnits.setLayoutManager(new LinearLayoutManager(this));
            recyclerUnits.setAdapter(unitAdapter);
        }

        unitAdapter.setOnUnitActionListener(new UnitAdapter.OnUnitActionListener() {
            @Override
            public void onUnitClick(Unit unit) {
                showUnitDetailsDialog(unit);
            }

            @Override
            public void onEditClick(Unit unit) {
                editUnit(unit);
            }

            @Override
            public void onStatusClick(Unit unit) {
                showChangeStatusDialog(unit);
            }

            @Override
            public void onDeleteClick(Unit unit) {
                showDeleteUnitDialog(unit);
            }
        });
    }

    private void loadPropertyData() {
        if (propertyId <= 0) return;

        // Intent Fallbacks
        Intent intent = getIntent();
        if (intent != null) {
            String name = intent.getStringExtra("property_name");
            String address = intent.getStringExtra("property_address");
            String type = intent.getStringExtra("property_type");
            int floors = intent.getIntExtra("property_floors", 0);
            String notes = intent.getStringExtra("property_notes");

            if (name != null) {
                if (tvPropertyName != null) tvPropertyName.setText(name);
                if (tvPropertyAddress != null) tvPropertyAddress.setText(address != null ? address : "");
                if (tvPropertyType != null) tvPropertyType.setText(PropertyAdapter.getPropertyTypeDisplay(this, type));
                if (tvPropertyFloors != null) tvPropertyFloors.setText(String.valueOf(floors));
                if (tvPropertyStatus != null) tvPropertyStatus.setText(R.string.status_active);
                if (tvPropertyId != null) tvPropertyId.setText("ID: #" + propertyId);
                if (tvPropertyNotes != null) tvPropertyNotes.setText(notes != null && !notes.isEmpty() ? notes : getString(R.string.no_notes));
                setupFloorFilterDropdown(floors);
            }
        }

        // Database Authoritative Query
        Executors.newSingleThreadExecutor().execute(() -> {
            try {
                AppDatabase db = AppDatabase.getInstance(getApplicationContext());
                Property prop = db.propertyDao().getPropertyById(propertyId);
                if (prop != null) {
                    currentProperty = prop;
                    runOnUiThread(() -> {
                        if (tvPropertyName != null) tvPropertyName.setText(prop.getName() != null ? prop.getName() : "");
                        if (tvPropertyAddress != null) tvPropertyAddress.setText(prop.getAddress() != null ? prop.getAddress() : "");
                        if (tvPropertyType != null) tvPropertyType.setText(PropertyAdapter.getPropertyTypeDisplay(this, prop.getPropertyType()));
                        if (tvPropertyFloors != null) tvPropertyFloors.setText(String.valueOf(prop.getNumberOfFloors()));
                        if (tvPropertyStatus != null) tvPropertyStatus.setText(R.string.status_active);
                        if (tvPropertyId != null) tvPropertyId.setText("ID: #" + prop.getId());

                        if (tvPropertyNotes != null) {
                            if (prop.getNotes() == null || prop.getNotes().isEmpty()) {
                                tvPropertyNotes.setText(R.string.no_notes);
                            } else {
                                tvPropertyNotes.setText(prop.getNotes());
                            }
                        }

                        setupFloorFilterDropdown(prop.getNumberOfFloors());
                    });
                }
            } catch (Exception e) {
                Log.e(TAG, "Error loading property data from DB", e);
            }
        });
    }

    private void setupFloorFilterDropdown(int numberOfFloors) {
        if (autoFloorFilter == null) return;

        List<String> floorOptions = new ArrayList<>();
        floorOptions.add(getString(R.string.filter_all_floors));
        if (numberOfFloors > 0) {
            for (int i = 1; i <= numberOfFloors; i++) {
                floorOptions.add(getString(R.string.floor_number, i));
            }
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_dropdown_item_1line,
                floorOptions
        );
        autoFloorFilter.setAdapter(adapter);
        autoFloorFilter.setText(floorOptions.get(0), false);

        autoFloorFilter.setOnItemClickListener((parent, view, position, id) -> {
            selectedFloorFilter = position; // 0 = All, 1 = Floor 1, ...
            applyUnitFilters();
        });
    }

    private void setupListeners() {
        if (btnEditProperty != null) {
            btnEditProperty.setOnClickListener(v -> editProperty());
        }

        if (btnAddUnit != null) {
            btnAddUnit.setOnClickListener(v -> {
                Intent intent = new Intent(PropertyDetailsActivity.this, AddUnitActivity.class);
                intent.putExtra("property_id", propertyId);
                startActivityForResult(intent, 2001);
            });
        }

        if (chipGroupUnitStatus != null) {
            chipGroupUnitStatus.setOnCheckedChangeListener((group, checkedId) -> {
                if (checkedId != View.NO_ID) {
                    selectedStatusChipId = checkedId;
                } else {
                    selectedStatusChipId = R.id.chipUnitAll;
                }
                applyUnitFilters();
            });
        }
    }

    private void editProperty() {
        Intent intent = new Intent(PropertyDetailsActivity.this, AddPropertyActivity.class);
        intent.putExtra("property_id", propertyId);
        startActivityForResult(intent, 1001);
    }

    private void loadUnits() {
        if (propertyId <= 0) return;

        unitRepository.getUnitsByProperty(propertyId, new UnitRepository.DatabaseCallback<List<Unit>>() {
            @Override
            public void onSuccess(List<Unit> units) {
                allUnitsList = units != null ? units : new ArrayList<>();
                applyUnitFilters();
            }

            @Override
            public void onError(Exception exception) {
                Log.e(TAG, "Error loading units for property", exception);
                runOnUiThread(() -> {
                    if (tvUnitCount != null) tvUnitCount.setText("0");
                    if (tvNoUnits != null) tvNoUnits.setVisibility(View.VISIBLE);
                    if (recyclerUnits != null) recyclerUnits.setVisibility(View.GONE);
                });
            }
        });
    }

    private void applyUnitFilters() {
        Executors.newSingleThreadExecutor().execute(() -> {
            try {
                List<Unit> filtered = new ArrayList<>();

                for (Unit unit : allUnitsList) {
                    if (unit == null) continue;

                    // Status Filter
                    boolean statusMatches = true;
                    if (selectedStatusChipId == R.id.chipUnitOccupied) {
                        statusMatches = "OCCUPIED".equalsIgnoreCase(unit.getStatus());
                    } else if (selectedStatusChipId == R.id.chipUnitVacant) {
                        statusMatches = "VACANT".equalsIgnoreCase(unit.getStatus());
                    }

                    if (!statusMatches) continue;

                    // Floor Filter
                    if (selectedFloorFilter > 0) {
                        if (unit.getFloor() != selectedFloorFilter) {
                            continue;
                        }
                    }

                    filtered.add(unit);
                }

                runOnUiThread(() -> updateUnitsUI(filtered));
            } catch (Exception e) {
                Log.e(TAG, "Error applying unit filters", e);
            }
        });
    }

    private void updateUnitsUI(List<Unit> units) {
        int count = units != null ? units.size() : 0;
        if (tvUnitCount != null) tvUnitCount.setText(String.valueOf(count));

        if (count == 0) {
            if (tvNoUnits != null) tvNoUnits.setVisibility(View.VISIBLE);
            if (recyclerUnits != null) recyclerUnits.setVisibility(View.GONE);
            if (unitAdapter != null) unitAdapter.setUnits(null);
        } else {
            if (tvNoUnits != null) tvNoUnits.setVisibility(View.GONE);
            if (recyclerUnits != null) recyclerUnits.setVisibility(View.VISIBLE);
            if (unitAdapter != null) unitAdapter.setUnits(units);
        }
    }

    private void loadOperationalSummary() {
        if (propertyId <= 0) return;

        FinancialFilterPeriod period = new FinancialFilterPeriod(FinancialFilterPeriod.Type.THIS_MONTH);

        Executors.newSingleThreadExecutor().execute(() -> {
            try {
                AppDatabase db = AppDatabase.getInstance(getApplicationContext());
                double expected = db.rentDao().getPropertyExpectedRentByMonthRange(propertyId, period.getStartMonth(), period.getEndMonth());
                double collected = db.paymentDao().getPropertyCollectedRentByPaymentDate(propertyId, period.getStartDate(), period.getEndDate());
                double outstanding = db.rentDao().getPropertyCumulativeOutstandingRent(propertyId, monthFormat.format(new Date()));

                double expenses = db.expenseDao().getPropertyExpenseTotalByDateRange(propertyId, period.getStartDate(), period.getEndDate());

                List<Unit> propUnits = db.unitDao().getUnitsByProperty(propertyId);
                int totalUnits = propUnits != null ? propUnits.size() : 0;
                int occupied = 0;
                if (propUnits != null) {
                    for (Unit u : propUnits) {
                        if (u != null && "OCCUPIED".equalsIgnoreCase(u.getStatus())) occupied++;
                    }
                }
                int vacant = totalUnits - occupied;
                double occRate = totalUnits > 0 ? (occupied * 100.0 / totalUnits) : 0.0;

                // Maintenance Summary for Property
                List<MaintenanceRecord> maints = db.maintenanceDao().getByProperty(propertyId);
                int openCount = 0;
                int inProgressCount = 0;
                double thisMonthMaintCost = 0.0;
                String thisMonth = monthFormat.format(new Date());

                if (maints != null) {
                    for (MaintenanceRecord m : maints) {
                        if (m == null) continue;
                        if ("OPEN".equalsIgnoreCase(m.getStatus()) || "SCHEDULED".equalsIgnoreCase(m.getStatus())) {
                            openCount++;
                        } else if ("IN_PROGRESS".equalsIgnoreCase(m.getStatus())) {
                            inProgressCount++;
                        }
                        if (m.getCompletedDate() != null && m.getCompletedDate().startsWith(thisMonth)) {
                            thisMonthMaintCost += (m.getActualCost() > 0 ? m.getActualCost() : m.getEstimatedCost());
                        }
                    }
                }

                String curr = getString(R.string.currency_symbol);

                final int finalTotal = totalUnits;
                final int finalOcc = occupied;
                final int finalVac = vacant;
                final double finalRate = occRate;
                final int finalOpenMaint = openCount;
                final int finalInProgressMaint = inProgressCount;
                final double finalMonthMaintCost = thisMonthMaintCost;

                runOnUiThread(() -> {
                    if (tvSummaryTotalUnits != null) tvSummaryTotalUnits.setText(getString(R.string.reports_total_units) + ": " + finalTotal);
                    if (tvSummaryOccupiedUnits != null) tvSummaryOccupiedUnits.setText(getString(R.string.reports_occupied_units) + ": " + finalOcc);
                    if (tvSummaryVacantUnits != null) tvSummaryVacantUnits.setText(getString(R.string.reports_vacant_units) + ": " + finalVac);
                    if (tvSummaryOccupancyRate != null) tvSummaryOccupancyRate.setText(getString(R.string.reports_occupancy_rate) + ": " + String.format(Locale.getDefault(), "%.1f%%", finalRate));

                    if (tvSummaryExpectedRent != null) tvSummaryExpectedRent.setText(getString(R.string.reports_expected_rent) + ": " + curr + currencyFormat.format(expected));
                    if (tvSummaryCollectedRent != null) tvSummaryCollectedRent.setText(getString(R.string.reports_collected_rent) + ": " + curr + currencyFormat.format(collected));
                    if (tvSummaryOutstandingRent != null) tvSummaryOutstandingRent.setText(getString(R.string.reports_outstanding_rent) + ": " + curr + currencyFormat.format(outstanding));
                    if (tvPropertyMonthExpenses != null) tvPropertyMonthExpenses.setText(curr + currencyFormat.format(expenses));

                    TextView tvOpen = findViewById(R.id.tvMaintSummaryOpen);
                    TextView tvInProg = findViewById(R.id.tvMaintSummaryInProgress);
                    TextView tvCost = findViewById(R.id.tvMaintSummaryCost);

                    if (tvOpen != null) tvOpen.setText(getString(R.string.status_maint_open) + ": " + finalOpenMaint);
                    if (tvInProg != null) tvInProg.setText(getString(R.string.status_maint_in_progress) + ": " + finalInProgressMaint);
                    if (tvCost != null) tvCost.setText(getString(R.string.actual_cost) + " (" + getString(R.string.filter_this_month) + "): " + curr + currencyFormat.format(finalMonthMaintCost));

                    View btnViewMaint = findViewById(R.id.btnViewPropertyMaintenance);
                    View btnAddMaint = findViewById(R.id.btnAddPropertyMaintenance);

                    if (btnViewMaint != null) {
                        btnViewMaint.setOnClickListener(v -> {
                            Intent intent = new Intent(PropertyDetailsActivity.this, MaintenanceActivity.class);
                            intent.putExtra("property_id", propertyId);
                            startActivity(intent);
                        });
                    }

                    if (btnAddMaint != null) {
                        btnAddMaint.setOnClickListener(v -> {
                            Intent intent = new Intent(PropertyDetailsActivity.this, AddMaintenanceActivity.class);
                            intent.putExtra("property_id", propertyId);
                            startActivity(intent);
                        });
                    }
                });
            } catch (Exception e) {
                Log.e(TAG, "Error calculating operational summary", e);
            }
        });
    }

    private void showUnitDetailsDialog(Unit unit) {
        if (unit == null) return;
        String curr = getString(R.string.currency_symbol);
        String perMonth = getString(R.string.per_month);

        Executors.newSingleThreadExecutor().execute(() -> {
            try {
                AppDatabase db = AppDatabase.getInstance(getApplicationContext());
                TenancyWithDetails activeTenancy = db.tenancyDao().getActiveTenancyWithDetailsByUnit(unit.getId());
                double outstandingRent = db.rentDao().getUnitCumulativeOutstandingRent(unit.getId(), monthFormat.format(new Date()));

                List<MaintenanceRecord> unitMaints = db.maintenanceDao().getByUnit(unit.getId());
                int unitMaintCount = unitMaints != null ? unitMaints.size() : 0;
                double unitMaintCost = 0.0;
                String recentMaintStr = "-";
                if (unitMaints != null && !unitMaints.isEmpty()) {
                    MaintenanceRecord recent = unitMaints.get(0);
                    recentMaintStr = recent.getTitle() != null ? recent.getTitle() : "";
                    for (MaintenanceRecord m : unitMaints) {
                        if (m != null) unitMaintCost += (m.getActualCost() > 0 ? m.getActualCost() : m.getEstimatedCost());
                    }
                }

                final String finalRecentMaint = recentMaintStr;
                final double finalUnitCost = unitMaintCost;

                runOnUiThread(() -> {
                    StringBuilder details = new StringBuilder();
                    details.append(getString(R.string.unit_number)).append(": ").append(unit.getUnitNumber()).append("\n");
                    if (unit.getFloor() > 0) {
                        details.append(getString(R.string.floor)).append(": ").append(unit.getFloor()).append("\n");
                    }
                    details.append(getString(R.string.property_type)).append(": ").append(currentProperty != null && currentProperty.getName() != null ? currentProperty.getName() : "").append("\n");
                    details.append(getString(R.string.unit_status)).append(": ").append(UnitAdapter.getStatusDisplay(this, unit.getStatus())).append("\n");
                    details.append(getString(R.string.monthly_rent)).append(": ").append(curr).append(currencyFormat.format(unit.getMonthlyRent())).append(" ").append(perMonth).append("\n");

                    if (outstandingRent > 0) {
                        details.append(getString(R.string.reports_outstanding_rent)).append(": ").append(curr).append(currencyFormat.format(outstandingRent)).append("\n");
                    }

                    details.append("\n🛠️ ").append(getString(R.string.maintenance_title)).append(":\n");
                    details.append("Total: ").append(unitMaintCount).append(" | Recent: ").append(finalRecentMaint).append(" | Cost: ").append(curr).append(currencyFormat.format(finalUnitCost)).append("\n");

                    if (activeTenancy != null && activeTenancy.tenant != null) {
                        details.append("\n━━━━━━━━━━━━━━━━━━━━\n");
                        details.append(getString(R.string.current_tenant)).append(":\n");
                        details.append("👤 ").append(activeTenancy.tenant.getFullName()).append("\n");
                        if (activeTenancy.tenant.getPhoneNumber() != null) {
                            details.append("📞 ").append(activeTenancy.tenant.getPhoneNumber()).append("\n");
                        }
                    } else {
                        details.append("\n(").append(getString(R.string.currently_vacant)).append(")\n");
                    }

                    MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(PropertyDetailsActivity.this)
                            .setTitle(unit.getUnitNumber() + " - " + getString(R.string.unit_details))
                            .setMessage(details.toString())
                            .setPositiveButton(R.string.edit, (dialog, which) -> editUnit(unit))
                            .setNegativeButton(R.string.close, null);

                    if (activeTenancy != null && activeTenancy.tenant != null) {
                        builder.setNeutralButton(R.string.view_tenant, (dialog, which) -> {
                            Intent intent = new Intent(PropertyDetailsActivity.this, TenantDetailsActivity.class);
                            intent.putExtra("tenant_id", activeTenancy.tenant.getId());
                            startActivity(intent);
                        });
                    }

                    builder.show();
                });
            } catch (Exception e) {
                Log.e(TAG, "Error showing unit details dialog", e);
            }
        });
    }

    private void editUnit(Unit unit) {
        if (unit == null) return;
        Intent intent = new Intent(PropertyDetailsActivity.this, AddUnitActivity.class);
        intent.putExtra("unit_id", unit.getId());
        intent.putExtra("property_id", propertyId);
        startActivityForResult(intent, 2001);
    }

    private void showChangeStatusDialog(Unit unit) {
        if (unit == null) return;
        String[] statuses = {
                getString(R.string.status_vacant),
                getString(R.string.status_occupied),
                getString(R.string.status_reserved),
                getString(R.string.status_maintenance)
        };
        String[] statusKeys = {"VACANT", "OCCUPIED", "RESERVED", "MAINTENANCE"};

        int selectedIndex = 0;
        for (int i = 0; i < statusKeys.length; i++) {
            if (statusKeys[i].equalsIgnoreCase(unit.getStatus())) {
                selectedIndex = i;
                break;
            }
        }

        new MaterialAlertDialogBuilder(this)
                .setTitle(R.string.change_status)
                .setSingleChoiceItems(statuses, selectedIndex, (dialog, which) -> {
                    String newStatus = statusKeys[which];
                    unit.setStatus(newStatus);
                    unit.setUpdatedAt(System.currentTimeMillis());
                    unitRepository.update(unit, new UnitRepository.DatabaseCallback<Void>() {
                        @Override
                        public void onSuccess(Void result) {
                            runOnUiThread(() -> {
                                Toast.makeText(PropertyDetailsActivity.this, R.string.status_updated_success, Toast.LENGTH_SHORT).show();
                                loadUnits();
                                loadOperationalSummary();
                            });
                        }

                        @Override
                        public void onError(Exception exception) {
                            Log.e(TAG, "Error updating unit status", exception);
                            runOnUiThread(() -> Toast.makeText(PropertyDetailsActivity.this, R.string.save_failed, Toast.LENGTH_SHORT).show());
                        }
                    });
                    dialog.dismiss();
                })
                .setNegativeButton(R.string.cancel, null)
                .show();
    }

    private void showDeleteUnitDialog(Unit unit) {
        if (unit == null) return;
        new MaterialAlertDialogBuilder(this)
                .setTitle(R.string.delete_unit)
                .setMessage(getString(R.string.delete_unit_confirm, unit.getUnitNumber()))
                .setPositiveButton(R.string.delete, (dialog, which) -> {
                    unitRepository.delete(unit, new UnitRepository.DatabaseCallback<Void>() {
                        @Override
                        public void onSuccess(Void result) {
                            runOnUiThread(() -> {
                                Toast.makeText(PropertyDetailsActivity.this, R.string.unit_deleted_success, Toast.LENGTH_SHORT).show();
                                loadUnits();
                                loadOperationalSummary();
                            });
                        }

                        @Override
                        public void onError(Exception exception) {
                            Log.e(TAG, "Error deleting unit", exception);
                            runOnUiThread(() -> Toast.makeText(PropertyDetailsActivity.this, R.string.save_failed, Toast.LENGTH_SHORT).show());
                        }
                    });
                })
                .setNegativeButton(R.string.cancel, null)
                .show();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (propertyId > 0 && unitRepository != null) {
            loadUnits();
            loadOperationalSummary();
        }
    }
}
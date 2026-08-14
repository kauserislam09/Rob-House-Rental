package com.rob.houserental;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputEditText;
import com.rob.houserental.adapter.RentAdapter;
import com.rob.houserental.model.MonthlyRentSummary;
import com.rob.houserental.model.RentRecordDisplayItem;
import com.rob.houserental.repository.RentRepository;

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class RentActivity extends AppCompatActivity {

    private TextView tvSelectedMonth;
    private MaterialButton btnPrevMonth;
    private MaterialButton btnNextMonth;

    private TextView tvSummaryExpected;
    private TextView tvSummaryCollected;
    private TextView tvSummaryOutstanding;
    private TextView tvSummaryOverdue;
    private TextView tvSummaryCumulativeOutstanding;

    private MaterialButton btnGenerateRent;

    private TextInputEditText etSearchRent;
    private ChipGroup chipGroupRentStatus;
    private TextView tvRentCount;
    private RecyclerView recyclerRent;
    private View layoutEmptyRent;
    private MaterialButton btnEmptyGenerateRent;

    private RentAdapter adapter;
    private RentRepository repository;

    private final Calendar currentMonthCalendar = Calendar.getInstance();
    private static final SimpleDateFormat monthFormat = new SimpleDateFormat("yyyy-MM", Locale.getDefault());
    private static final SimpleDateFormat monthDisplayFormat = new SimpleDateFormat("MMMM yyyy", Locale.getDefault());
    private static final SimpleDateFormat dueDateFormat = new SimpleDateFormat("10 MMM yyyy", Locale.getDefault());
    private static final NumberFormat currencyFormatter = NumberFormat.getNumberInstance(Locale.getDefault());

    private final List<RentRecordDisplayItem> currentMonthRecords = new ArrayList<>();
    private String currentSearchQuery = "";
    private String currentStatusFilter = "ALL";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_rent);

        initializeViews();

        setupToolbar();

        setupRecyclerView();

        repository = new RentRepository(getApplicationContext());

        setupMonthNavigation();

        setupSearchAndFilter();

        setupListeners();

        loadMonthData();
    }

    private void initializeViews() {
        tvSelectedMonth = findViewById(R.id.tvSelectedMonth);
        btnPrevMonth = findViewById(R.id.btnPrevMonth);
        btnNextMonth = findViewById(R.id.btnNextMonth);

        tvSummaryExpected = findViewById(R.id.tvSummaryExpected);
        tvSummaryCollected = findViewById(R.id.tvSummaryCollected);
        tvSummaryOutstanding = findViewById(R.id.tvSummaryOutstanding);
        tvSummaryOverdue = findViewById(R.id.tvSummaryOverdue);
        tvSummaryCumulativeOutstanding = findViewById(R.id.tvSummaryCumulativeOutstanding);

        btnGenerateRent = findViewById(R.id.btnGenerateRent);

        etSearchRent = findViewById(R.id.etSearchRent);
        chipGroupRentStatus = findViewById(R.id.chipGroupRentStatus);
        tvRentCount = findViewById(R.id.tvRentCount);
        recyclerRent = findViewById(R.id.recyclerRent);
        layoutEmptyRent = findViewById(R.id.layoutEmptyRent);
        btnEmptyGenerateRent = findViewById(R.id.btnEmptyGenerateRent);
    }

    private void setupToolbar() {
        MaterialToolbar toolbar = findViewById(R.id.toolbarRent);
        toolbar.setNavigationOnClickListener(v -> finish());
    }

    private void setupRecyclerView() {
        adapter = new RentAdapter();
        recyclerRent.setLayoutManager(new LinearLayoutManager(this));
        recyclerRent.setAdapter(adapter);

        adapter.setOnRentClickListener(item -> {
            Intent intent = new Intent(RentActivity.this, RentDetailsActivity.class);
            intent.putExtra("rent_id", item.id);
            startActivity(intent);
        });
    }

    private void setupMonthNavigation() {
        updateMonthDisplay();

        btnPrevMonth.setOnClickListener(v -> {
            currentMonthCalendar.add(Calendar.MONTH, -1);
            updateMonthDisplay();
            loadMonthData();
        });

        btnNextMonth.setOnClickListener(v -> {
            currentMonthCalendar.add(Calendar.MONTH, 1);
            updateMonthDisplay();
            loadMonthData();
        });
    }

    private void updateMonthDisplay() {
        tvSelectedMonth.setText(monthDisplayFormat.format(currentMonthCalendar.getTime()));
    }

    private void setupSearchAndFilter() {
        etSearchRent.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int count, int after) {
                currentSearchQuery = s != null ? s.toString().trim().toLowerCase() : "";
                applyFilterAndSearch();
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        chipGroupRentStatus.setOnCheckedStateChangeListener((group, checkedIds) -> {
            String previousFilter = currentStatusFilter;
            if (checkedIds.isEmpty()) {
                currentStatusFilter = "ALL";
            } else {
                int checkedId = checkedIds.get(0);
                if (checkedId == R.id.chipRentAllOutstanding) {
                    currentStatusFilter = "ALL_OUTSTANDING";
                } else if (checkedId == R.id.chipRentUnpaid) {
                    currentStatusFilter = "UNPAID";
                } else if (checkedId == R.id.chipRentPartial) {
                    currentStatusFilter = "PARTIAL";
                } else if (checkedId == R.id.chipRentPaid) {
                    currentStatusFilter = "PAID";
                } else if (checkedId == R.id.chipRentOverdue) {
                    currentStatusFilter = "OVERDUE";
                } else if (checkedId == R.id.chipRentWaived) {
                    currentStatusFilter = "WAIVED";
                } else {
                    currentStatusFilter = "ALL";
                }
            }

            if ("ALL_OUTSTANDING".equals(currentStatusFilter) || "ALL_OUTSTANDING".equals(previousFilter)) {
                loadMonthData();
            } else {
                applyFilterAndSearch();
            }
        });
    }

    private void setupListeners() {
        btnGenerateRent.setOnClickListener(v -> showGenerateRentConfirmation());
        btnEmptyGenerateRent.setOnClickListener(v -> showGenerateRentConfirmation());
    }

    private void showGenerateRentConfirmation() {
        String monthDisplay = monthDisplayFormat.format(currentMonthCalendar.getTime());
        new MaterialAlertDialogBuilder(this)
                .setTitle(R.string.generate_rent)
                .setMessage(getString(R.string.generate_rent_confirm, monthDisplay))
                .setPositiveButton(R.string.generate_rent, (dialog, which) -> generateRentForCurrentMonth())
                .setNegativeButton(R.string.cancel, null)
                .show();
    }

    private void generateRentForCurrentMonth() {
        String billingMonth = monthFormat.format(currentMonthCalendar.getTime());
        String dueDate = dueDateFormat.format(currentMonthCalendar.getTime());

        repository.generateMonthlyRent(billingMonth, dueDate, new RentRepository.DatabaseCallback<RentRepository.GenerationResult>() {
            @Override
            public void onSuccess(RentRepository.GenerationResult result) {
                runOnUiThread(() -> {
                    if (result.createdCount == 0 && result.alreadyExistingCount == 0) {
                        Toast.makeText(RentActivity.this, R.string.no_active_tenancies_for_rent, Toast.LENGTH_SHORT).show();
                    } else if (result.createdCount == 0) {
                        Toast.makeText(RentActivity.this, R.string.rent_already_generated, Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(RentActivity.this, getString(R.string.rent_generated_success, result.createdCount, result.alreadyExistingCount), Toast.LENGTH_SHORT).show();
                    }
                    loadMonthData();
                });
            }

            @Override
            public void onError(Exception exception) {
                runOnUiThread(() -> Toast.makeText(RentActivity.this, R.string.save_failed, Toast.LENGTH_SHORT).show());
            }
        });
    }

    private void loadMonthData() {
        String billingMonth = monthFormat.format(currentMonthCalendar.getTime());

        // Load Summary KPIs
        repository.getMonthlySummary(billingMonth, new RentRepository.DatabaseCallback<MonthlyRentSummary>() {
            @Override
            public void onSuccess(MonthlyRentSummary summary) {
                runOnUiThread(() -> displaySummary(summary));
            }

            @Override
            public void onError(Exception exception) {
                runOnUiThread(() -> displaySummary(new MonthlyRentSummary(0, 0, 0, 0)));
            }
        });

        // Load Cumulative Total Outstanding (all non-paid records up to current billing month)
        repository.getTotalCumulativeOutstandingRent(billingMonth, new RentRepository.DatabaseCallback<Double>() {
            @Override
            public void onSuccess(Double cumulativeTotal) {
                runOnUiThread(() -> {
                    if (tvSummaryCumulativeOutstanding != null) {
                        String curr = getString(R.string.currency_symbol);
                        tvSummaryCumulativeOutstanding.setText(curr + currencyFormatter.format(cumulativeTotal != null ? cumulativeTotal : 0.0));
                    }
                });
            }

            @Override
            public void onError(Exception exception) {
                runOnUiThread(() -> {
                    if (tvSummaryCumulativeOutstanding != null) {
                        tvSummaryCumulativeOutstanding.setText(getString(R.string.currency_symbol) + "0");
                    }
                });
            }
        });

        // Load Records List based on selected filter mode
        if ("ALL_OUTSTANDING".equalsIgnoreCase(currentStatusFilter)) {
            repository.getCumulativeOutstandingRentDisplayItems(billingMonth, new RentRepository.DatabaseCallback<List<RentRecordDisplayItem>>() {
                @Override
                public void onSuccess(List<RentRecordDisplayItem> list) {
                    runOnUiThread(() -> {
                        currentMonthRecords.clear();
                        if (list != null) {
                            currentMonthRecords.addAll(list);
                        }
                        applyFilterAndSearch();
                    });
                }

                @Override
                public void onError(Exception exception) {
                    runOnUiThread(() -> {
                        currentMonthRecords.clear();
                        applyFilterAndSearch();
                    });
                }
            });
        } else {
            repository.getRentDisplayItemsByMonth(billingMonth, new RentRepository.DatabaseCallback<List<RentRecordDisplayItem>>() {
                @Override
                public void onSuccess(List<RentRecordDisplayItem> list) {
                    runOnUiThread(() -> {
                        currentMonthRecords.clear();
                        if (list != null) {
                            currentMonthRecords.addAll(list);
                        }
                        applyFilterAndSearch();
                    });
                }

                @Override
                public void onError(Exception exception) {
                    runOnUiThread(() -> {
                        currentMonthRecords.clear();
                        applyFilterAndSearch();
                    });
                }
            });
        }
    }

    private void displaySummary(MonthlyRentSummary summary) {
        String curr = getString(R.string.currency_symbol);
        tvSummaryExpected.setText(curr + currencyFormatter.format(summary.totalExpected));
        tvSummaryCollected.setText(curr + currencyFormatter.format(summary.totalCollected));
        tvSummaryOutstanding.setText(curr + currencyFormatter.format(summary.totalOutstanding));
        if (tvSummaryOverdue != null) {
            tvSummaryOverdue.setText(curr + currencyFormatter.format(summary.totalOverdue));
        }
    }

    private void applyFilterAndSearch() {
        List<RentRecordDisplayItem> filtered = new ArrayList<>();

        for (RentRecordDisplayItem item : currentMonthRecords) {
            // Status Match
            boolean statusMatches = "ALL".equalsIgnoreCase(currentStatusFilter) ||
                    "ALL_OUTSTANDING".equalsIgnoreCase(currentStatusFilter) ||
                    currentStatusFilter.equalsIgnoreCase(item.status);

            if (!statusMatches) continue;

            // Search Match
            if (TextUtils.isEmpty(currentSearchQuery)) {
                filtered.add(item);
            } else {
                boolean matches = false;
                if (item.tenantName != null && item.tenantName.toLowerCase().contains(currentSearchQuery)) {
                    matches = true;
                } else if (item.unitNumber != null && item.unitNumber.toLowerCase().contains(currentSearchQuery)) {
                    matches = true;
                } else if (item.propertyName != null && item.propertyName.toLowerCase().contains(currentSearchQuery)) {
                    matches = true;
                }

                if (matches) {
                    filtered.add(item);
                }
            }
        }

        updateList(filtered);
    }

    private void updateList(List<RentRecordDisplayItem> list) {
        int count = list != null ? list.size() : 0;
        tvRentCount.setText(count == 1 ? getString(R.string.count_rent_records_singular, count) : getString(R.string.count_rent_records_plural, count));

        if (count == 0) {
            recyclerRent.setVisibility(View.GONE);
            layoutEmptyRent.setVisibility(View.VISIBLE);
            adapter.setRentRecords(null);
        } else {
            recyclerRent.setVisibility(View.VISIBLE);
            layoutEmptyRent.setVisibility(View.GONE);
            adapter.setRentRecords(list);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (repository != null) {
            loadMonthData();
        }
    }
}

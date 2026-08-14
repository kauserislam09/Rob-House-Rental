package com.rob.houserental;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;
import com.rob.houserental.adapter.UtilityBillAdapter;
import com.rob.houserental.model.MonthlyBillSummary;
import com.rob.houserental.model.UtilityBillDisplayItem;
import com.rob.houserental.repository.UtilityBillRepository;

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class BillsActivity extends AppCompatActivity {

    private TextView tvSelectedBillMonth;
    private MaterialButton btnPrevBillMonth;
    private MaterialButton btnNextBillMonth;

    private TextView tvBillSummaryExpected;
    private TextView tvBillSummaryCollected;
    private TextView tvBillSummaryOutstanding;
    private TextView tvBillSummaryOverdue;

    private MaterialButton btnAddBillHeader;

    private TextInputEditText etSearchBills;
    private ChipGroup chipGroupBillType;
    private TextView tvBillsCount;
    private RecyclerView recyclerBills;
    private View layoutEmptyBills;
    private MaterialButton btnEmptyAddBill;
    private ExtendedFloatingActionButton fabAddBill;

    private UtilityBillAdapter adapter;
    private UtilityBillRepository repository;

    private final Calendar currentMonthCalendar = Calendar.getInstance();
    private static final SimpleDateFormat monthFormat = new SimpleDateFormat("yyyy-MM", Locale.getDefault());
    private static final SimpleDateFormat monthDisplayFormat = new SimpleDateFormat("MMMM yyyy", Locale.getDefault());
    private static final NumberFormat currencyFormatter = NumberFormat.getNumberInstance(Locale.getDefault());

    private final List<UtilityBillDisplayItem> currentMonthBills = new ArrayList<>();
    private String currentSearchQuery = "";
    private String currentTypeFilter = "ALL";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_bills);

        initializeViews();

        setupToolbar();

        setupRecyclerView();

        repository = new UtilityBillRepository(getApplicationContext());

        setupMonthNavigation();

        setupSearchAndFilter();

        setupListeners();

        loadMonthData();
    }

    private void initializeViews() {
        tvSelectedBillMonth = findViewById(R.id.tvSelectedBillMonth);
        btnPrevBillMonth = findViewById(R.id.btnPrevBillMonth);
        btnNextBillMonth = findViewById(R.id.btnNextBillMonth);

        tvBillSummaryExpected = findViewById(R.id.tvBillSummaryExpected);
        tvBillSummaryCollected = findViewById(R.id.tvBillSummaryCollected);
        tvBillSummaryOutstanding = findViewById(R.id.tvBillSummaryOutstanding);
        tvBillSummaryOverdue = findViewById(R.id.tvBillSummaryOverdue);

        btnAddBillHeader = findViewById(R.id.btnAddBillHeader);

        etSearchBills = findViewById(R.id.etSearchBills);
        chipGroupBillType = findViewById(R.id.chipGroupBillType);
        tvBillsCount = findViewById(R.id.tvBillsCount);
        recyclerBills = findViewById(R.id.recyclerBills);
        layoutEmptyBills = findViewById(R.id.layoutEmptyBills);
        btnEmptyAddBill = findViewById(R.id.btnEmptyAddBill);
        fabAddBill = findViewById(R.id.fabAddBill);
    }

    private void setupToolbar() {
        MaterialToolbar toolbar = findViewById(R.id.toolbarBills);
        toolbar.setNavigationOnClickListener(v -> finish());
    }

    private void setupRecyclerView() {
        adapter = new UtilityBillAdapter();
        recyclerBills.setLayoutManager(new LinearLayoutManager(this));
        recyclerBills.setAdapter(adapter);

        adapter.setOnBillClickListener(item -> {
            Intent intent = new Intent(BillsActivity.this, BillDetailsActivity.class);
            intent.putExtra("bill_id", item.id);
            startActivity(intent);
        });
    }

    private void setupMonthNavigation() {
        updateMonthDisplay();

        btnPrevBillMonth.setOnClickListener(v -> {
            currentMonthCalendar.add(Calendar.MONTH, -1);
            updateMonthDisplay();
            loadMonthData();
        });

        btnNextBillMonth.setOnClickListener(v -> {
            currentMonthCalendar.add(Calendar.MONTH, 1);
            updateMonthDisplay();
            loadMonthData();
        });
    }

    private void updateMonthDisplay() {
        tvSelectedBillMonth.setText(monthDisplayFormat.format(currentMonthCalendar.getTime()));
    }

    private void setupSearchAndFilter() {
        etSearchBills.addTextChangedListener(new TextWatcher() {
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

        chipGroupBillType.setOnCheckedStateChangeListener((group, checkedIds) -> {
            if (checkedIds.isEmpty()) {
                currentTypeFilter = "ALL";
            } else {
                int checkedId = checkedIds.get(0);
                if (checkedId == R.id.chipTypeElectricity) {
                    currentTypeFilter = "ELECTRICITY";
                } else if (checkedId == R.id.chipTypeWater) {
                    currentTypeFilter = "WATER";
                } else if (checkedId == R.id.chipTypeGas) {
                    currentTypeFilter = "GAS";
                } else if (checkedId == R.id.chipTypeInternet) {
                    currentTypeFilter = "INTERNET";
                } else if (checkedId == R.id.chipTypeOther) {
                    currentTypeFilter = "OTHER";
                } else {
                    currentTypeFilter = "ALL";
                }
            }
            applyFilterAndSearch();
        });
    }

    private void setupListeners() {
        View.OnClickListener addListener = v -> {
            Intent intent = new Intent(BillsActivity.this, AddBillActivity.class);
            String billingMonth = monthFormat.format(currentMonthCalendar.getTime());
            intent.putExtra("default_billing_month", billingMonth);
            startActivity(intent);
        };

        btnAddBillHeader.setOnClickListener(addListener);
        btnEmptyAddBill.setOnClickListener(addListener);
        fabAddBill.setOnClickListener(addListener);
    }

    private void loadMonthData() {
        String billingMonth = monthFormat.format(currentMonthCalendar.getTime());

        // Load Summary KPIs
        repository.getMonthlySummary(billingMonth, new UtilityBillRepository.DatabaseCallback<MonthlyBillSummary>() {
            @Override
            public void onSuccess(MonthlyBillSummary summary) {
                runOnUiThread(() -> displaySummary(summary));
            }

            @Override
            public void onError(Exception exception) {
                runOnUiThread(() -> displaySummary(new MonthlyBillSummary(0, 0, 0, 0)));
            }
        });

        // Load Bills List
        repository.getBillDisplayItemsByMonth(billingMonth, new UtilityBillRepository.DatabaseCallback<List<UtilityBillDisplayItem>>() {
            @Override
            public void onSuccess(List<UtilityBillDisplayItem> list) {
                runOnUiThread(() -> {
                    currentMonthBills.clear();
                    if (list != null) {
                        currentMonthBills.addAll(list);
                    }
                    applyFilterAndSearch();
                });
            }

            @Override
            public void onError(Exception exception) {
                runOnUiThread(() -> {
                    currentMonthBills.clear();
                    applyFilterAndSearch();
                });
            }
        });
    }

    private void displaySummary(MonthlyBillSummary summary) {
        String curr = getString(R.string.currency_symbol);
        tvBillSummaryExpected.setText(curr + currencyFormatter.format(summary.totalExpected));
        tvBillSummaryCollected.setText(curr + currencyFormatter.format(summary.totalCollected));
        tvBillSummaryOutstanding.setText(curr + currencyFormatter.format(summary.totalOutstanding));
        if (tvBillSummaryOverdue != null) {
            tvBillSummaryOverdue.setText(curr + currencyFormatter.format(summary.totalOverdue));
        }
    }

    private void applyFilterAndSearch() {
        List<UtilityBillDisplayItem> filtered = new ArrayList<>();

        for (UtilityBillDisplayItem item : currentMonthBills) {
            // Type Match
            boolean typeMatches = "ALL".equalsIgnoreCase(currentTypeFilter) ||
                    currentTypeFilter.equalsIgnoreCase(item.billType);

            if (!typeMatches) continue;

            // Search Match
            if (TextUtils.isEmpty(currentSearchQuery)) {
                filtered.add(item);
            } else {
                boolean matches = false;
                if (item.propertyName != null && item.propertyName.toLowerCase().contains(currentSearchQuery)) {
                    matches = true;
                } else if (item.unitNumber != null && item.unitNumber.toLowerCase().contains(currentSearchQuery)) {
                    matches = true;
                } else if (item.tenantName != null && item.tenantName.toLowerCase().contains(currentSearchQuery)) {
                    matches = true;
                } else if (item.billNumber != null && item.billNumber.toLowerCase().contains(currentSearchQuery)) {
                    matches = true;
                } else if (item.meterNumber != null && item.meterNumber.toLowerCase().contains(currentSearchQuery)) {
                    matches = true;
                } else if (item.billType != null && item.billType.toLowerCase().contains(currentSearchQuery)) {
                    matches = true;
                }

                if (matches) {
                    filtered.add(item);
                }
            }
        }

        updateList(filtered);
    }

    private void updateList(List<UtilityBillDisplayItem> list) {
        int count = list != null ? list.size() : 0;
        tvBillsCount.setText(count == 1 ? getString(R.string.count_utility_bills_singular, count) : getString(R.string.count_utility_bills_plural, count));

        if (count == 0) {
            recyclerBills.setVisibility(View.GONE);
            layoutEmptyBills.setVisibility(View.VISIBLE);
            adapter.setBills(null);
        } else {
            recyclerBills.setVisibility(View.VISIBLE);
            layoutEmptyBills.setVisibility(View.GONE);
            adapter.setBills(list);
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

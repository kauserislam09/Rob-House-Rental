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
import com.rob.houserental.adapter.ExpenseAdapter;
import com.rob.houserental.model.ExpenseDisplayItem;
import com.rob.houserental.repository.ExpenseRepository;

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class ExpensesActivity extends AppCompatActivity {

    private TextView tvSelectedExpenseMonth;
    private MaterialButton btnPrevExpenseMonth;
    private MaterialButton btnNextExpenseMonth;

    private TextView tvExpenseSummaryTotal;
    private TextView tvExpenseSummaryCount;
    private MaterialButton btnAddExpenseHeader;

    private TextInputEditText etSearchExpenses;
    private ChipGroup chipGroupExpenseCategory;
    private TextView tvExpensesCount;
    private RecyclerView recyclerExpenses;
    private View layoutEmptyExpenses;
    private MaterialButton btnEmptyAddExpense;
    private ExtendedFloatingActionButton fabAddExpense;

    private ExpenseAdapter adapter;
    private ExpenseRepository repository;

    private com.google.android.material.tabs.TabLayout tabLayoutExpenses;
    private View layoutExpenseMonthNav;
    private boolean isArchivedMode = false;

    private final Calendar currentMonthCalendar = Calendar.getInstance();
    private static final SimpleDateFormat monthFormat = new SimpleDateFormat("yyyy-MM", Locale.getDefault());
    private static final SimpleDateFormat monthDisplayFormat = new SimpleDateFormat("MMMM yyyy", Locale.getDefault());
    private static final NumberFormat currencyFormatter = NumberFormat.getNumberInstance(Locale.getDefault());

    private final List<ExpenseDisplayItem> currentMonthExpenses = new ArrayList<>();
    private String currentSearchQuery = "";
    private String currentCategoryFilter = "ALL";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_expenses);

        isArchivedMode = getIntent().getBooleanExtra("show_archived", false);

        initializeViews();

        setupToolbar();

        setupRecyclerView();

        repository = new ExpenseRepository(getApplicationContext());

        setupTabs();

        setupMonthNavigation();

        setupSearchAndFilter();

        setupListeners();

        loadMonthData();
    }

    private void initializeViews() {
        tabLayoutExpenses = findViewById(R.id.tabLayoutExpenses);
        layoutExpenseMonthNav = findViewById(R.id.layoutExpenseMonthNav);

        tvSelectedExpenseMonth = findViewById(R.id.tvSelectedExpenseMonth);
        btnPrevExpenseMonth = findViewById(R.id.btnPrevExpenseMonth);
        btnNextExpenseMonth = findViewById(R.id.btnNextExpenseMonth);

        tvExpenseSummaryTotal = findViewById(R.id.tvExpenseSummaryTotal);
        tvExpenseSummaryCount = findViewById(R.id.tvExpenseSummaryCount);
        btnAddExpenseHeader = findViewById(R.id.btnAddExpenseHeader);

        etSearchExpenses = findViewById(R.id.etSearchExpenses);
        chipGroupExpenseCategory = findViewById(R.id.chipGroupExpenseCategory);
        tvExpensesCount = findViewById(R.id.tvExpensesCount);
        recyclerExpenses = findViewById(R.id.recyclerExpenses);
        layoutEmptyExpenses = findViewById(R.id.layoutEmptyExpenses);
        btnEmptyAddExpense = findViewById(R.id.btnEmptyAddExpense);
        fabAddExpense = findViewById(R.id.fabAddExpense);
    }

    private void setupTabs() {
        if (tabLayoutExpenses == null) return;

        if (isArchivedMode && tabLayoutExpenses.getTabCount() > 1) {
            com.google.android.material.tabs.TabLayout.Tab tab = tabLayoutExpenses.getTabAt(1);
            if (tab != null) tab.select();
            if (layoutExpenseMonthNav != null) layoutExpenseMonthNav.setVisibility(View.GONE);
        }

        tabLayoutExpenses.addOnTabSelectedListener(new com.google.android.material.tabs.TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(com.google.android.material.tabs.TabLayout.Tab tab) {
                isArchivedMode = tab.getPosition() == 1;
                if (layoutExpenseMonthNav != null) {
                    layoutExpenseMonthNav.setVisibility(isArchivedMode ? View.GONE : View.VISIBLE);
                }
                loadMonthData();
            }

            @Override
            public void onTabUnselected(com.google.android.material.tabs.TabLayout.Tab tab) {
            }

            @Override
            public void onTabReselected(com.google.android.material.tabs.TabLayout.Tab tab) {
            }
        });
    }

    private void setupToolbar() {
        MaterialToolbar toolbar = findViewById(R.id.toolbarExpenses);
        toolbar.setNavigationOnClickListener(v -> finish());
    }

    private void setupRecyclerView() {
        adapter = new ExpenseAdapter();
        recyclerExpenses.setLayoutManager(new LinearLayoutManager(this));
        recyclerExpenses.setAdapter(adapter);

        adapter.setOnExpenseClickListener(item -> {
            Intent intent = new Intent(ExpensesActivity.this, ExpenseDetailsActivity.class);
            intent.putExtra("expense_id", item.id);
            startActivity(intent);
        });
    }

    private void setupMonthNavigation() {
        updateMonthDisplay();

        btnPrevExpenseMonth.setOnClickListener(v -> {
            currentMonthCalendar.add(Calendar.MONTH, -1);
            updateMonthDisplay();
            loadMonthData();
        });

        btnNextExpenseMonth.setOnClickListener(v -> {
            currentMonthCalendar.add(Calendar.MONTH, 1);
            updateMonthDisplay();
            loadMonthData();
        });
    }

    private void updateMonthDisplay() {
        tvSelectedExpenseMonth.setText(monthDisplayFormat.format(currentMonthCalendar.getTime()));
    }

    private void setupSearchAndFilter() {
        etSearchExpenses.addTextChangedListener(new TextWatcher() {
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

        chipGroupExpenseCategory.setOnCheckedStateChangeListener((group, checkedIds) -> {
            if (checkedIds.isEmpty()) {
                currentCategoryFilter = "ALL";
            } else {
                int checkedId = checkedIds.get(0);
                if (checkedId == R.id.chipCatRepair) {
                    currentCategoryFilter = "REPAIR";
                } else if (checkedId == R.id.chipCatMaintenance) {
                    currentCategoryFilter = "MAINTENANCE";
                } else if (checkedId == R.id.chipCatCleaning) {
                    currentCategoryFilter = "CLEANING";
                } else if (checkedId == R.id.chipCatElectrical) {
                    currentCategoryFilter = "ELECTRICAL";
                } else if (checkedId == R.id.chipCatPlumbing) {
                    currentCategoryFilter = "PLUMBING";
                } else if (checkedId == R.id.chipCatPainting) {
                    currentCategoryFilter = "PAINTING";
                } else if (checkedId == R.id.chipCatSecurity) {
                    currentCategoryFilter = "SECURITY";
                } else if (checkedId == R.id.chipCatTax) {
                    currentCategoryFilter = "PROPERTY_TAX";
                } else if (checkedId == R.id.chipCatOther) {
                    currentCategoryFilter = "OTHER";
                } else {
                    currentCategoryFilter = "ALL";
                }
            }
            applyFilterAndSearch();
        });
    }

    private void setupListeners() {
        View.OnClickListener addListener = v -> {
            Intent intent = new Intent(ExpensesActivity.this, AddExpenseActivity.class);
            String billingMonth = monthFormat.format(currentMonthCalendar.getTime());
            intent.putExtra("default_expense_month", billingMonth);
            startActivity(intent);
        };

        btnAddExpenseHeader.setOnClickListener(addListener);
        btnEmptyAddExpense.setOnClickListener(addListener);
        fabAddExpense.setOnClickListener(addListener);
    }

    private void loadMonthData() {
        if (isArchivedMode) {
            repository.getArchivedExpenseTotal(new ExpenseRepository.DatabaseCallback<Double>() {
                @Override
                public void onSuccess(Double total) {
                    runOnUiThread(() -> {
                        String curr = getString(R.string.currency_symbol);
                        tvExpenseSummaryTotal.setText(curr + currencyFormatter.format(total != null ? total : 0.0));
                    });
                }

                @Override
                public void onError(Exception exception) {
                }
            });

            repository.getArchivedExpenseDisplayItems(new ExpenseRepository.DatabaseCallback<List<ExpenseDisplayItem>>() {
                @Override
                public void onSuccess(List<ExpenseDisplayItem> list) {
                    runOnUiThread(() -> {
                        currentMonthExpenses.clear();
                        if (list != null) {
                            currentMonthExpenses.addAll(list);
                        }
                        int count = currentMonthExpenses.size();
                        tvExpenseSummaryCount.setText(getString(R.string.expense_count_format, count));
                        applyFilterAndSearch();
                    });
                }

                @Override
                public void onError(Exception exception) {
                    runOnUiThread(() -> {
                        currentMonthExpenses.clear();
                        applyFilterAndSearch();
                    });
                }
            });
        } else {
            String expenseMonth = monthFormat.format(currentMonthCalendar.getTime());

            // Load Summary KPI
            repository.getMonthlyExpenseTotal(expenseMonth, new ExpenseRepository.DatabaseCallback<Double>() {
                @Override
                public void onSuccess(Double total) {
                    runOnUiThread(() -> {
                        String curr = getString(R.string.currency_symbol);
                        tvExpenseSummaryTotal.setText(curr + currencyFormatter.format(total != null ? total : 0.0));
                    });
                }

                @Override
                public void onError(Exception exception) {
                }
            });

            // Load List
            repository.getExpenseDisplayItemsByMonth(expenseMonth, new ExpenseRepository.DatabaseCallback<List<ExpenseDisplayItem>>() {
                @Override
                public void onSuccess(List<ExpenseDisplayItem> list) {
                    runOnUiThread(() -> {
                        currentMonthExpenses.clear();
                        if (list != null) {
                            currentMonthExpenses.addAll(list);
                        }
                        int count = currentMonthExpenses.size();
                        tvExpenseSummaryCount.setText(getString(R.string.expense_count_format, count));
                        applyFilterAndSearch();
                    });
                }

                @Override
                public void onError(Exception exception) {
                    runOnUiThread(() -> {
                        currentMonthExpenses.clear();
                        applyFilterAndSearch();
                    });
                }
            });
        }
    }

    private void applyFilterAndSearch() {
        List<ExpenseDisplayItem> filtered = new ArrayList<>();

        for (ExpenseDisplayItem item : currentMonthExpenses) {
            // Category filter
            boolean catMatches = "ALL".equalsIgnoreCase(currentCategoryFilter) ||
                    currentCategoryFilter.equalsIgnoreCase(item.category);

            if (!catMatches) continue;

            // Search filter
            if (TextUtils.isEmpty(currentSearchQuery)) {
                filtered.add(item);
            } else {
                boolean matches = false;
                if (item.propertyName != null && item.propertyName.toLowerCase().contains(currentSearchQuery)) {
                    matches = true;
                } else if (item.unitNumber != null && item.unitNumber.toLowerCase().contains(currentSearchQuery)) {
                    matches = true;
                } else if (item.category != null && item.category.toLowerCase().contains(currentSearchQuery)) {
                    matches = true;
                } else if (item.description != null && item.description.toLowerCase().contains(currentSearchQuery)) {
                    matches = true;
                } else if (item.notes != null && item.notes.toLowerCase().contains(currentSearchQuery)) {
                    matches = true;
                }

                if (matches) {
                    filtered.add(item);
                }
            }
        }

        updateList(filtered);
    }

    private void updateList(List<ExpenseDisplayItem> list) {
        int count = list != null ? list.size() : 0;
        tvExpensesCount.setText(count == 1 ? getString(R.string.count_expenses_singular, count) : getString(R.string.count_expenses_plural, count));

        if (count == 0) {
            recyclerExpenses.setVisibility(View.GONE);
            layoutEmptyExpenses.setVisibility(View.VISIBLE);
            adapter.setExpenses(null);
        } else {
            recyclerExpenses.setVisibility(View.VISIBLE);
            layoutEmptyExpenses.setVisibility(View.GONE);
            adapter.setExpenses(list);
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

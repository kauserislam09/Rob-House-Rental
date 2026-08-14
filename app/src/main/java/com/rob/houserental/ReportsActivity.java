package com.rob.houserental;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.tabs.TabLayout;
import com.rob.houserental.adapter.ExpenseAdapter;
import com.rob.houserental.adapter.PaymentReportAdapter;
import com.rob.houserental.adapter.PropertyPerformanceAdapter;
import com.rob.houserental.adapter.RentAdapter;
import com.rob.houserental.adapter.UtilityBillAdapter;
import com.rob.houserental.data.AppDatabase;
import com.rob.houserental.financial.FinancialFilterPeriod;
import com.rob.houserental.financial.FinancialRepository;
import com.rob.houserental.repository.DatabaseCallback;
import com.rob.houserental.financial.FinancialSummary;
import com.rob.houserental.financial.FinancialTrendChartView;
import com.rob.houserental.financial.MonthlyFinancialTrend;
import com.rob.houserental.financial.PropertyFinancialSummary;
import com.rob.houserental.model.ExpenseDisplayItem;
import com.rob.houserental.model.PaymentDisplayItem;
import com.rob.houserental.model.RentRecordDisplayItem;
import com.rob.houserental.model.UtilityBillDisplayItem;
import com.rob.houserental.utils.ReportExportUtils;

import java.io.OutputStream;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ReportsActivity extends AppCompatActivity {

    private MaterialToolbar toolbarReports;
    private MaterialButton btnPeriodFilter;
    private MaterialButton btnExportPdf;
    private MaterialButton btnExportCsv;
    private TabLayout tabLayoutReports;

    private View layoutDashboardContent;
    private TextView tvExpectedRent;
    private TextView tvCollectedRent;
    private TextView tvOutstandingRent;
    private TextView tvOverdueRent;
    private TextView tvTotalExpenses;
    private TextView tvUtilityBills;
    private TextView tvNetIncome;

    private TextView tvOccupancyDetails;
    private TextView tvOccupancyRate;
    private TextView tvCollectionRate;

    private FinancialTrendChartView chartMonthlyTrend;
    private RecyclerView recyclerReportsList;
    private TextView tvEmptyReports;

    private FinancialRepository financialRepository;
    private AppDatabase db;

    private FinancialFilterPeriod currentPeriod = new FinancialFilterPeriod(FinancialFilterPeriod.Type.THIS_MONTH);
    private int currentTabPosition = 0;

    private FinancialSummary currentSummary;
    private List<PropertyFinancialSummary> currentPropertySummaries = new ArrayList<>();
    private List<RentRecordDisplayItem> currentRentItems = new ArrayList<>();
    private List<PaymentDisplayItem> currentPaymentItems = new ArrayList<>();
    private List<ExpenseDisplayItem> currentExpenseItems = new ArrayList<>();
    private List<UtilityBillDisplayItem> currentBillItems = new ArrayList<>();

    private static final NumberFormat currencyFormatter = NumberFormat.getNumberInstance(Locale.getDefault());
    private static final SimpleDateFormat monthFormat = new SimpleDateFormat("yyyy-MM", Locale.getDefault());
    private static final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());

    private ActivityResultLauncher<Intent> pdfExportLauncher;
    private ActivityResultLauncher<Intent> csvExportLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reports);

        financialRepository = new FinancialRepository(getApplicationContext());
        db = AppDatabase.getInstance(getApplicationContext());

        initializeViews();
        setupToolbar();
        setupTabs();
        setupExportLaunchers();

        loadDashboardData();
    }

    private void initializeViews() {
        toolbarReports = findViewById(R.id.toolbarReports);
        btnPeriodFilter = findViewById(R.id.btnPeriodFilter);
        btnExportPdf = findViewById(R.id.btnExportPdf);
        btnExportCsv = findViewById(R.id.btnExportCsv);
        tabLayoutReports = findViewById(R.id.tabLayoutReports);

        layoutDashboardContent = findViewById(R.id.layoutDashboardContent);
        tvExpectedRent = findViewById(R.id.tvExpectedRent);
        tvCollectedRent = findViewById(R.id.tvCollectedRent);
        tvOutstandingRent = findViewById(R.id.tvOutstandingRent);
        tvOverdueRent = findViewById(R.id.tvOverdueRent);
        tvTotalExpenses = findViewById(R.id.tvTotalExpenses);
        tvUtilityBills = findViewById(R.id.tvUtilityBills);
        tvNetIncome = findViewById(R.id.tvNetIncome);

        tvOccupancyDetails = findViewById(R.id.tvOccupancyDetails);
        tvOccupancyRate = findViewById(R.id.tvOccupancyRate);
        tvCollectionRate = findViewById(R.id.tvCollectionRate);

        chartMonthlyTrend = findViewById(R.id.chartMonthlyTrend);
        recyclerReportsList = findViewById(R.id.recyclerReportsList);
        tvEmptyReports = findViewById(R.id.tvEmptyReports);

        recyclerReportsList.setLayoutManager(new LinearLayoutManager(this));

        btnPeriodFilter.setOnClickListener(v -> showPeriodFilterDialog());
        btnExportPdf.setOnClickListener(v -> launchPdfExport());
        btnExportCsv.setOnClickListener(v -> launchCsvExport());
    }

    private void setupToolbar() {
        toolbarReports.setNavigationOnClickListener(v -> finish());
    }

    private void setupTabs() {
        tabLayoutReports.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                currentTabPosition = tab.getPosition();
                if (currentTabPosition == 0) {
                    layoutDashboardContent.setVisibility(View.VISIBLE);
                    recyclerReportsList.setVisibility(View.GONE);
                    loadDashboardData();
                } else {
                    layoutDashboardContent.setVisibility(View.GONE);
                    recyclerReportsList.setVisibility(View.VISIBLE);
                    loadSubReportData(currentTabPosition);
                }
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
            }
        });
    }

    private void showPeriodFilterDialog() {
        String[] periods = new String[]{
                getString(R.string.period_this_month),
                getString(R.string.period_last_month),
                getString(R.string.period_this_year),
                getString(R.string.period_last_year),
                getString(R.string.period_custom)
        };

        new MaterialAlertDialogBuilder(this)
                .setTitle(R.string.period_this_month)
                .setItems(periods, (dialog, which) -> {
                    switch (which) {
                        case 0:
                            currentPeriod = new FinancialFilterPeriod(FinancialFilterPeriod.Type.THIS_MONTH);
                            btnPeriodFilter.setText(R.string.period_this_month);
                            break;
                        case 1:
                            currentPeriod = new FinancialFilterPeriod(FinancialFilterPeriod.Type.LAST_MONTH);
                            btnPeriodFilter.setText(R.string.period_last_month);
                            break;
                        case 2:
                            currentPeriod = new FinancialFilterPeriod(FinancialFilterPeriod.Type.THIS_YEAR);
                            btnPeriodFilter.setText(R.string.period_this_year);
                            break;
                        case 3:
                            currentPeriod = new FinancialFilterPeriod(FinancialFilterPeriod.Type.LAST_YEAR);
                            btnPeriodFilter.setText(R.string.period_last_year);
                            break;
                        case 4:
                            showCustomDateRangePicker();
                            return;
                    }
                    if (currentTabPosition == 0) {
                        loadDashboardData();
                    } else {
                        loadSubReportData(currentTabPosition);
                    }
                })
                .show();
    }

    private void showCustomDateRangePicker() {
        MaterialDatePicker<androidx.core.util.Pair<Long, Long>> datePicker =
                MaterialDatePicker.Builder.dateRangePicker()
                        .setTitleText(R.string.period_custom)
                        .build();

        datePicker.addOnPositiveButtonClickListener(selection -> {
            if (selection != null && selection.first != null && selection.second != null) {
                String startD = dateFormat.format(new Date(selection.first));
                String endD = dateFormat.format(new Date(selection.second));
                String startM = monthFormat.format(new Date(selection.first));
                String endM = monthFormat.format(new Date(selection.second));

                currentPeriod = new FinancialFilterPeriod(FinancialFilterPeriod.Type.CUSTOM, startM, endM);
                currentPeriod.setStartDate(startD);
                currentPeriod.setEndDate(endD);

                btnPeriodFilter.setText(startD + " ~ " + endD);

                if (currentTabPosition == 0) {
                    loadDashboardData();
                } else {
                    loadSubReportData(currentTabPosition);
                }
            }
        });

        datePicker.show(getSupportFragmentManager(), "DATE_RANGE_PICKER");
    }

    private void loadDashboardData() {
        financialRepository.getFinancialSummary(currentPeriod, new DatabaseCallback<FinancialSummary>() {
            @Override
            public void onSuccess(FinancialSummary summary) {
                currentSummary = summary;
                runOnUiThread(() -> populateDashboardViews(summary));
            }

            @Override
            public void onError(Exception e) {
            }
        });

        financialRepository.getMonthlyTrends(6, new DatabaseCallback<List<MonthlyFinancialTrend>>() {
            @Override
            public void onSuccess(List<MonthlyFinancialTrend> trends) {
                runOnUiThread(() -> {
                    if (chartMonthlyTrend != null) {
                        chartMonthlyTrend.setTrendData(trends);
                    }
                });
            }

            @Override
            public void onError(Exception e) {
            }
        });

        financialRepository.getPropertySummaries(currentPeriod, new DatabaseCallback<List<PropertyFinancialSummary>>() {
            @Override
            public void onSuccess(List<PropertyFinancialSummary> summaries) {
                currentPropertySummaries = summaries;
            }

            @Override
            public void onError(Exception e) {
            }
        });
    }

    private void populateDashboardViews(FinancialSummary summary) {
        if (summary == null) return;

        String curr = getString(R.string.currency_symbol);
        tvExpectedRent.setText(curr + currencyFormatter.format(summary.getExpectedRent()));
        tvCollectedRent.setText(curr + currencyFormatter.format(summary.getCollectedRent()));
        tvOutstandingRent.setText(curr + currencyFormatter.format(summary.getOutstandingRent()));
        tvOverdueRent.setText(curr + currencyFormatter.format(summary.getOverdueRent()));
        tvTotalExpenses.setText(curr + currencyFormatter.format(summary.getActiveExpenses()));
        tvUtilityBills.setText(curr + currencyFormatter.format(summary.getUtilityBillsPaid()));
        tvNetIncome.setText(curr + currencyFormatter.format(summary.getNetIncome()));

        tvOccupancyDetails.setText(getString(R.string.reports_total_units) + ": " + summary.getTotalUnits() +
                " | " + getString(R.string.reports_occupied_units) + ": " + summary.getOccupiedUnits() +
                " | " + getString(R.string.reports_vacant_units) + ": " + summary.getVacantUnits());

        tvOccupancyRate.setText(String.format(Locale.getDefault(), "%.1f%%", summary.getOccupancyRate()));
        tvCollectionRate.setText(getString(R.string.reports_collection_rate) + ": " + String.format(Locale.getDefault(), "%.1f%%", summary.getCollectionRate()));
    }

    private void loadSubReportData(int position) {
        new Thread(() -> {
            try {
                switch (position) {
                    case 1: // Rent Report
                    {
                        List<RentRecordDisplayItem> items;
                        if (currentPeriod != null && currentPeriod.getStartMonth() != null && currentPeriod.getEndMonth() != null) {
                            items = db.rentDao().getRentDisplayItemsByMonthRange(currentPeriod.getStartMonth(), currentPeriod.getEndMonth());
                        } else {
                            items = db.rentDao().getAllRentDisplayItems();
                        }
                        currentRentItems = items;
                        runOnUiThread(() -> {
                            RentAdapter adapter = new RentAdapter();
                            recyclerReportsList.setAdapter(adapter);
                            adapter.setRentRecords(items);
                        });
                        break;
                    }
                    case 2: // Outstanding Rent
                    {
                        String currentMonth = monthFormat.format(Calendar.getInstance().getTime());
                        List<RentRecordDisplayItem> items = db.rentDao().getCumulativeOutstandingRentDisplayItems(currentMonth);
                        runOnUiThread(() -> {
                            RentAdapter adapter = new RentAdapter();
                            recyclerReportsList.setAdapter(adapter);
                            adapter.setRentRecords(items);
                        });
                        break;
                    }
                    case 3: // Payment Report
                    {
                        List<PaymentDisplayItem> items;
                        if (currentPeriod != null && currentPeriod.getStartDate() != null && currentPeriod.getEndDate() != null) {
                            items = db.paymentDao().getPaymentDisplayItemsByDateRange(currentPeriod.getStartDate(), currentPeriod.getEndDate());
                        } else {
                            items = db.paymentDao().getAllPaymentDisplayItems();
                        }
                        currentPaymentItems = items;
                        runOnUiThread(() -> {
                            PaymentReportAdapter adapter = new PaymentReportAdapter();
                            recyclerReportsList.setAdapter(adapter);
                            adapter.setItems(items);
                        });
                        break;
                    }
                    case 4: // Expense Report
                    {
                        List<ExpenseDisplayItem> items;
                        if (currentPeriod != null && currentPeriod.getStartDate() != null && currentPeriod.getEndDate() != null) {
                            items = db.expenseDao().getExpenseDisplayItemsByDateRange(currentPeriod.getStartDate(), currentPeriod.getEndDate());
                        } else {
                            items = db.expenseDao().getAllExpenseDisplayItems();
                        }
                        currentExpenseItems = items;
                        runOnUiThread(() -> {
                            ExpenseAdapter adapter = new ExpenseAdapter();
                            recyclerReportsList.setAdapter(adapter);
                            adapter.setExpenses(items);
                        });
                        break;
                    }
                    case 5: // Bill Report
                    {
                        List<UtilityBillDisplayItem> items;
                        if (currentPeriod != null && currentPeriod.getStartMonth() != null && currentPeriod.getEndMonth() != null) {
                            items = db.utilityBillDao().getBillDisplayItemsByMonthRange(currentPeriod.getStartMonth(), currentPeriod.getEndMonth());
                        } else {
                            items = db.utilityBillDao().getAllBillDisplayItems();
                        }
                        currentBillItems = items;
                        runOnUiThread(() -> {
                            UtilityBillAdapter adapter = new UtilityBillAdapter();
                            recyclerReportsList.setAdapter(adapter);
                            adapter.setBills(items);
                        });
                        break;
                    }
                    case 6: // Property Performance Report
                    {
                        financialRepository.getPropertySummaries(currentPeriod, new DatabaseCallback<List<PropertyFinancialSummary>>() {
                            @Override
                            public void onSuccess(List<PropertyFinancialSummary> summaries) {
                                currentPropertySummaries = summaries;
                                runOnUiThread(() -> {
                                    PropertyPerformanceAdapter adapter = new PropertyPerformanceAdapter();
                                    recyclerReportsList.setAdapter(adapter);
                                    adapter.setItems(summaries);
                                });
                            }

                            @Override
                            public void onError(Exception e) {
                            }
                        });
                        break;
                    }
                    case 7: // Maintenance Report
                    {
                        List<com.rob.houserental.model.MaintenanceRecord> maints = db.maintenanceDao().getAll();
                        currentMaintenanceRecords = maints;
                        runOnUiThread(() -> {
                            com.rob.houserental.adapter.MaintenanceAdapter adapter = new com.rob.houserental.adapter.MaintenanceAdapter();
                            recyclerReportsList.setAdapter(adapter);
                            adapter.setItems(maints);
                        });
                        break;
                    }
                }
            } catch (Exception e) {
            }
        }).start();
    }

    private List<com.rob.houserental.model.MaintenanceRecord> currentMaintenanceRecords = new ArrayList<>();

    private void setupExportLaunchers() {
        pdfExportLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        Uri uri = result.getData().getData();
                        if (uri != null) {
                            writePdfToUri(uri);
                        }
                    }
                }
        );

        csvExportLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        Uri uri = result.getData().getData();
                        if (uri != null) {
                            writeCsvToUri(uri);
                        }
                    }
                }
        );
    }

    private void launchPdfExport() {
        Intent intent = new Intent(Intent.ACTION_CREATE_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("application/pdf");
        intent.putExtra(Intent.EXTRA_TITLE, "RobHouseRental_Report_" + System.currentTimeMillis() + ".pdf");
        pdfExportLauncher.launch(intent);
    }

    private void launchCsvExport() {
        Intent intent = new Intent(Intent.ACTION_CREATE_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("text/csv");
        intent.putExtra(Intent.EXTRA_TITLE, "RobHouseRental_Report_" + System.currentTimeMillis() + ".csv");
        csvExportLauncher.launch(intent);
    }

    private void writePdfToUri(Uri uri) {
        new Thread(() -> {
            try (OutputStream out = getContentResolver().openOutputStream(uri)) {
                if (currentTabPosition == 7) {
                    ReportExportUtils.exportMaintenanceReportPdf(this, currentMaintenanceRecords, btnPeriodFilter.getText().toString(), out);
                } else {
                    ReportExportUtils.exportFinancialSummaryPdf(this, currentSummary, currentPropertySummaries, btnPeriodFilter.getText().toString(), out);
                }
                runOnUiThread(() -> Toast.makeText(this, R.string.export_success, Toast.LENGTH_SHORT).show());
            } catch (Exception e) {
                runOnUiThread(() -> Toast.makeText(this, R.string.export_failed, Toast.LENGTH_SHORT).show());
            }
        }).start();
    }

    private void writeCsvToUri(Uri uri) {
        new Thread(() -> {
            try (OutputStream out = getContentResolver().openOutputStream(uri)) {
                if (currentTabPosition == 3) {
                    ReportExportUtils.exportPaymentReportCsv(this, currentPaymentItems, out);
                } else if (currentTabPosition == 4) {
                    ReportExportUtils.exportExpenseReportCsv(this, currentExpenseItems, out);
                } else if (currentTabPosition == 5) {
                    ReportExportUtils.exportBillReportCsv(this, currentBillItems, out);
                } else if (currentTabPosition == 6) {
                    ReportExportUtils.exportPropertyPerformanceCsv(this, currentPropertySummaries, out);
                } else if (currentTabPosition == 7) {
                    ReportExportUtils.exportMaintenanceReportCsv(this, currentMaintenanceRecords, out);
                } else {
                    ReportExportUtils.exportRentReportCsv(this, currentRentItems, out);
                }
                runOnUiThread(() -> Toast.makeText(this, R.string.export_success, Toast.LENGTH_SHORT).show());
            } catch (Exception e) {
                runOnUiThread(() -> Toast.makeText(this, R.string.export_failed, Toast.LENGTH_SHORT).show());
            }
        }).start();
    }
}

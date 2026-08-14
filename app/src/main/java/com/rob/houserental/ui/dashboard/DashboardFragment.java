package com.rob.houserental.ui.dashboard;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.progressindicator.LinearProgressIndicator;
import com.rob.houserental.AddBillActivity;
import com.rob.houserental.AddExpenseActivity;
import com.rob.houserental.AddTenantActivity;
import com.rob.houserental.PropertiesActivity;
import com.rob.houserental.R;
import com.rob.houserental.RemindersActivity;
import com.rob.houserental.RentActivity;
import com.rob.houserental.ReportsActivity;
import com.rob.houserental.TenantsActivity;
import com.rob.houserental.data.AppDatabase;
import com.rob.houserental.model.Property;
import com.rob.houserental.model.RentRecord;
import com.rob.houserental.repository.DatabaseCallback;
import com.rob.houserental.repository.PropertyRepository;
import com.rob.houserental.utils.AppExecutors;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class DashboardFragment extends Fragment {

    private static final String TAG = "DashboardFragment";

    private TextView tvGreetingTime;
    private TextView tvCurrentDate;
    private EditText etDashboardSearch;

    private TextView tvMonthlyCollected;
    private TextView tvMonthlyPending;
    private TextView btnViewFinancials;
    private MaterialCardView cardCashFlow;

    private LinearLayout layoutNeedsAttention;
    private MaterialCardView cardOverdueAlert;
    private TextView tvOverdueAlertMsg;
    private MaterialButton btnSendReminderAlert;

    private MaterialCardView cardTotalUnits;
    private TextView tvTotalUnits;
    private TextView tvOccupancyPercentage;
    private LinearProgressIndicator pbOccupancyRate;

    private MaterialCardView cardOccupiedUnits;
    private TextView tvOccupied;

    private MaterialCardView cardVacantUnits;
    private TextView tvVacant;

    private MaterialButton btnCollectRent;
    private MaterialButton btnAddTenant;
    private MaterialButton btnAddBill;
    private MaterialButton btnAddExpense;
    private MaterialButton btnViewReports;

    private PropertyRepository propertyRepository;

    public DashboardFragment() {
        // Required empty public constructor
    }

    @Nullable
    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState
    ) {
        View view = inflater.inflate(
                R.layout.layout_dashboard,
                container,
                false
        );

        if (getContext() != null) {
            propertyRepository = new PropertyRepository(getContext());
        }

        initializeViews(view);
        setupGreetingAndDate();
        setupListeners();
        loadDashboardStats();

        return view;
    }

    private void initializeViews(View view) {
        tvGreetingTime = view.findViewById(R.id.tvGreetingTime);
        tvCurrentDate = view.findViewById(R.id.tvCurrentDate);
        etDashboardSearch = view.findViewById(R.id.etDashboardSearch);

        tvMonthlyCollected = view.findViewById(R.id.tvMonthlyCollected);
        tvMonthlyPending = view.findViewById(R.id.tvMonthlyPending);
        btnViewFinancials = view.findViewById(R.id.btnViewFinancials);
        cardCashFlow = view.findViewById(R.id.cardCashFlow);

        layoutNeedsAttention = view.findViewById(R.id.layoutNeedsAttention);
        cardOverdueAlert = view.findViewById(R.id.cardOverdueAlert);
        tvOverdueAlertMsg = view.findViewById(R.id.tvOverdueAlertMsg);
        btnSendReminderAlert = view.findViewById(R.id.btnSendReminderAlert);

        cardTotalUnits = view.findViewById(R.id.cardTotalUnits);
        tvTotalUnits = view.findViewById(R.id.tvTotalUnits);
        tvOccupancyPercentage = view.findViewById(R.id.tvOccupancyPercentage);
        pbOccupancyRate = view.findViewById(R.id.pbOccupancyRate);

        cardOccupiedUnits = view.findViewById(R.id.cardOccupiedUnits);
        tvOccupied = view.findViewById(R.id.tvOccupied);

        cardVacantUnits = view.findViewById(R.id.cardVacantUnits);
        tvVacant = view.findViewById(R.id.tvVacant);

        btnCollectRent = view.findViewById(R.id.btnCollectRent);
        btnAddTenant = view.findViewById(R.id.btnAddTenant);
        btnAddBill = view.findViewById(R.id.btnAddBill);
        btnAddExpense = view.findViewById(R.id.btnAddExpense);
        btnViewReports = view.findViewById(R.id.btnViewReports);
    }

    private void setupGreetingAndDate() {
        if (tvGreetingTime != null) {
            int hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
            if (hour >= 5 && hour < 12) {
                tvGreetingTime.setText(R.string.greeting_morning);
            } else if (hour >= 12 && hour < 18) {
                tvGreetingTime.setText(R.string.greeting_afternoon);
            } else {
                tvGreetingTime.setText(R.string.greeting_evening);
            }
        }

        if (tvCurrentDate != null) {
            SimpleDateFormat dateFormat = new SimpleDateFormat("EEEE, d MMMM yyyy", Locale.getDefault());
            tvCurrentDate.setText(dateFormat.format(new Date()));
        }
    }

    private void setupListeners() {
        // Quick Action Buttons
        if (btnAddTenant != null) {
            btnAddTenant.setOnClickListener(v -> startActivity(new Intent(getActivity(), AddTenantActivity.class)));
        }

        if (btnCollectRent != null) {
            btnCollectRent.setOnClickListener(v -> startActivity(new Intent(getActivity(), RentActivity.class)));
        }

        if (btnAddBill != null) {
            btnAddBill.setOnClickListener(v -> startActivity(new Intent(getActivity(), AddBillActivity.class)));
        }

        if (btnAddExpense != null) {
            btnAddExpense.setOnClickListener(v -> startActivity(new Intent(getActivity(), AddExpenseActivity.class)));
        }

        if (btnViewReports != null) {
            btnViewReports.setOnClickListener(v -> startActivity(new Intent(getActivity(), ReportsActivity.class)));
        }

        // Cash Flow Card Click -> Reports
        if (cardCashFlow != null) {
            cardCashFlow.setOnClickListener(v -> startActivity(new Intent(getActivity(), ReportsActivity.class)));
        }

        if (btnViewFinancials != null) {
            btnViewFinancials.setOnClickListener(v -> startActivity(new Intent(getActivity(), ReportsActivity.class)));
        }

        // Stat Card Clicks
        if (cardTotalUnits != null) {
            cardTotalUnits.setOnClickListener(v -> startActivity(new Intent(getActivity(), PropertiesActivity.class)));
        }

        if (cardOccupiedUnits != null) {
            cardOccupiedUnits.setOnClickListener(v -> startActivity(new Intent(getActivity(), TenantsActivity.class)));
        }

        if (cardVacantUnits != null) {
            cardVacantUnits.setOnClickListener(v -> startActivity(new Intent(getActivity(), PropertiesActivity.class)));
        }

        // Overdue Reminder Alert Click
        if (btnSendReminderAlert != null) {
            btnSendReminderAlert.setOnClickListener(v -> startActivity(new Intent(getActivity(), RemindersActivity.class)));
        }
    }

    private void loadDashboardStats() {
        if (getContext() == null || propertyRepository == null) {
            return;
        }

        propertyRepository.getAllProperties(new DatabaseCallback<List<Property>>() {
            @Override
            public void onSuccess(List<Property> properties) {
                AppExecutors.runOnDatabase(() -> {
                    try {
                        int totalUnits = 0;
                        int occupiedUnits = 0;
                        int vacantUnits = 0;

                        if (properties != null && !properties.isEmpty()) {
                            AppDatabase db = AppDatabase.getInstance(getContext());
                            for (Property property : properties) {
                                totalUnits += db.unitDao().getUnitCount(property.getId());
                                occupiedUnits += db.unitDao().getUnitCountByStatus(property.getId(), "OCCUPIED");
                                vacantUnits += db.unitDao().getUnitCountByStatus(property.getId(), "VACANT");
                            }
                        }

                        // Calculate Financials & Overdue records
                        AppDatabase db = AppDatabase.getInstance(getContext());
                        List<RentRecord> allRentRecords = db.rentDao().getAllRentRecords();
                        
                        double totalPaid = 0;
                        double totalPending = 0;
                        int overdueCount = 0;

                        if (allRentRecords != null) {
                            for (RentRecord record : allRentRecords) {
                                totalPaid += record.getAmountPaid();
                                double remaining = record.getAmountDue() - record.getAmountPaid();
                                if (remaining > 0) {
                                    totalPending += remaining;
                                }
                                String status = record.getStatus();
                                if ("OVERDUE".equalsIgnoreCase(status) ||
                                   ("UNPAID".equalsIgnoreCase(status) && remaining > 0)) {
                                    overdueCount++;
                                }
                            }
                        }

                        updateUiStats(totalUnits, occupiedUnits, vacantUnits, totalPaid, totalPending, overdueCount);
                    } catch (Exception e) {
                        Log.e(TAG, "Error calculating dashboard stats", e);
                    }
                });
            }

            @Override
            public void onError(Exception exception) {
                Log.e(TAG, "Error loading properties for dashboard stats", exception);
            }
        });
    }

    private void updateUiStats(
            int total,
            int occupied,
            int vacant,
            double collected,
            double pending,
            int overdueCount
    ) {
        if (getActivity() != null) {
            getActivity().runOnUiThread(() -> {
                if (tvTotalUnits != null) {
                    tvTotalUnits.setText(String.valueOf(total));
                }
                if (tvOccupied != null) {
                    tvOccupied.setText(String.valueOf(occupied));
                }
                if (tvVacant != null) {
                    tvVacant.setText(String.valueOf(vacant));
                }

                // Occupancy Rate Progress
                int percentage = total > 0 ? (occupied * 100) / total : 0;
                if (tvOccupancyPercentage != null) {
                    tvOccupancyPercentage.setText(percentage + "% Occupied");
                }
                if (pbOccupancyRate != null) {
                    pbOccupancyRate.setProgress(percentage);
                }

                // Monthly Cash Flow
                if (tvMonthlyCollected != null) {
                    tvMonthlyCollected.setText(String.format(Locale.getDefault(), "৳%,.0f", collected));
                }
                if (tvMonthlyPending != null) {
                    tvMonthlyPending.setText(String.format(Locale.getDefault(), "৳%,.0f", pending));
                }

                // Needs Attention Alert Feed
                if (layoutNeedsAttention != null) {
                    if (overdueCount > 0) {
                        layoutNeedsAttention.setVisibility(View.VISIBLE);
                        if (tvOverdueAlertMsg != null) {
                            tvOverdueAlertMsg.setText(getString(R.string.alert_overdue_msg, overdueCount));
                        }
                    } else {
                        layoutNeedsAttention.setVisibility(View.GONE);
                    }
                }
            });
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        loadDashboardStats();
    }
}
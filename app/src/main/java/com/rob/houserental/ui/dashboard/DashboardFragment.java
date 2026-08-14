package com.rob.houserental.ui.dashboard;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.material.button.MaterialButton;
import com.rob.houserental.AddTenantActivity;
import com.rob.houserental.R;
import com.rob.houserental.model.Property;
import com.rob.houserental.repository.DatabaseCallback;
import com.rob.houserental.repository.PropertyRepository;
import com.rob.houserental.repository.UnitRepository;
import com.rob.houserental.utils.AppExecutors;

import java.util.List;

public class DashboardFragment extends Fragment {

    private static final String TAG = "DashboardFragment";

    private TextView tvTotalUnits;
    private TextView tvOccupied;
    private TextView tvVacant;
    private MaterialButton btnCollectRent;
    private MaterialButton btnAddTenant;
    private MaterialButton btnAddBill;

    private PropertyRepository propertyRepository;
    private UnitRepository unitRepository;

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
            unitRepository = new UnitRepository(getContext());
        }

        initializeViews(view);

        setupListeners();

        loadDashboardStats();

        return view;
    }

    private void initializeViews(View view) {
        tvTotalUnits = view.findViewById(R.id.tvTotalUnits);
        tvOccupied = view.findViewById(R.id.tvOccupied);
        tvVacant = view.findViewById(R.id.tvVacant);
        btnCollectRent = view.findViewById(R.id.btnCollectRent);
        btnAddTenant = view.findViewById(R.id.btnAddTenant);
        btnAddBill = view.findViewById(R.id.btnAddBill);
    }

    private void setupListeners() {
        if (btnAddTenant != null) {
            btnAddTenant.setOnClickListener(v -> {
                Intent intent = new Intent(getActivity(), AddTenantActivity.class);
                startActivity(intent);
            });
        }

        if (btnCollectRent != null) {
            btnCollectRent.setOnClickListener(v -> {
                Intent intent = new Intent(getActivity(), com.rob.houserental.RentActivity.class);
                startActivity(intent);
            });
        }

        if (btnAddBill != null) {
            btnAddBill.setOnClickListener(v -> {
                Intent intent = new Intent(getActivity(), com.rob.houserental.AddBillActivity.class);
                startActivity(intent);
            });
        }
    }

    private void loadDashboardStats() {
        if (getContext() == null || propertyRepository == null) {
            return;
        }

        propertyRepository.getAllProperties(new DatabaseCallback<List<Property>>() {
            @Override
            public void onSuccess(List<Property> properties) {
                if (properties == null || properties.isEmpty()) {
                    updateUiStats(0, 0, 0);
                    return;
                }

                AppExecutors.runOnDatabase(() -> {
                    try {
                        int totalUnits = 0;
                        int occupiedUnits = 0;
                        int vacantUnits = 0;

                        for (Property property : properties) {
                            int count = 0;
                            int occ = 0;
                            int vac = 0;
                            // Using DAOs or Repositories - here we use the repository callbacks/executor safely
                            // Count units directly
                            com.rob.houserental.data.AppDatabase db = com.rob.houserental.data.AppDatabase.getInstance(getContext());
                            totalUnits += db.unitDao().getUnitCount(property.getId());
                            occupiedUnits += db.unitDao().getUnitCountByStatus(property.getId(), "OCCUPIED");
                            vacantUnits += db.unitDao().getUnitCountByStatus(property.getId(), "VACANT");
                        }

                        updateUiStats(totalUnits, occupiedUnits, vacantUnits);
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

    private void updateUiStats(int total, int occupied, int vacant) {
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
            });
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        loadDashboardStats();
    }
}
package com.rob.houserental;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;
import com.rob.houserental.adapter.MaintenanceAdapter;
import com.rob.houserental.model.MaintenanceRecord;
import com.rob.houserental.repository.MaintenanceRepository;

import java.util.ArrayList;
import java.util.List;

public class MaintenanceActivity extends AppCompatActivity {

    private static final String TAG = "MaintenanceActivity";

    private MaterialToolbar toolbar;
    private TextInputEditText etSearch;
    private ChipGroup chipGroupStatus;
    private RecyclerView recyclerMaintenance;
    private TextView tvEmpty;
    private ExtendedFloatingActionButton fabAdd;

    private MaintenanceAdapter adapter;
    private MaintenanceRepository maintenanceRepository;

    private List<MaintenanceRecord> allMaintenanceList = new ArrayList<>();
    private String currentSearchQuery = "";
    private int selectedStatusChipId = R.id.chipMaintAll;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maintenance);

        maintenanceRepository = new MaintenanceRepository(this);

        initializeViews();
        setupToolbar();
        setupRecyclerView();
        setupListeners();

        loadMaintenanceData();
    }

    private void initializeViews() {
        toolbar = findViewById(R.id.toolbarMaintenance);
        etSearch = findViewById(R.id.etSearchMaintenance);
        chipGroupStatus = findViewById(R.id.chipGroupMaintenanceStatus);
        recyclerMaintenance = findViewById(R.id.recyclerMaintenance);
        tvEmpty = findViewById(R.id.tvEmptyMaintenance);
        fabAdd = findViewById(R.id.fabAddMaintenance);
    }

    private void setupToolbar() {
        if (toolbar != null) {
            toolbar.setNavigationOnClickListener(v -> finish());
        }
    }

    private void setupRecyclerView() {
        adapter = new MaintenanceAdapter();
        if (recyclerMaintenance != null) {
            recyclerMaintenance.setLayoutManager(new LinearLayoutManager(this));
            recyclerMaintenance.setAdapter(adapter);
        }

        adapter.setOnMaintenanceClickListener(record -> {
            Intent intent = new Intent(MaintenanceActivity.this, MaintenanceDetailsActivity.class);
            intent.putExtra("maintenance_id", record.getId());
            startActivity(intent);
        });
    }

    private void setupListeners() {
        if (fabAdd != null) {
            fabAdd.setOnClickListener(v -> {
                Intent intent = new Intent(MaintenanceActivity.this, AddMaintenanceActivity.class);
                startActivity(intent);
            });
        }

        if (etSearch != null) {
            etSearch.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    currentSearchQuery = s != null ? s.toString() : "";
                    applyFilters();
                }

                @Override
                public void afterTextChanged(Editable s) {}
            });
        }

        if (chipGroupStatus != null) {
            chipGroupStatus.setOnCheckedChangeListener((group, checkedId) -> {
                if (checkedId != View.NO_ID) {
                    selectedStatusChipId = checkedId;
                } else {
                    selectedStatusChipId = R.id.chipMaintAll;
                }
                applyFilters();
            });
        }
    }

    private void loadMaintenanceData() {
        maintenanceRepository.getAll(new MaintenanceRepository.DatabaseCallback<List<MaintenanceRecord>>() {
            @Override
            public void onSuccess(List<MaintenanceRecord> list) {
                allMaintenanceList = list != null ? list : new ArrayList<>();
                runOnUiThread(() -> applyFilters());
            }

            @Override
            public void onError(Exception exception) {
                Log.e(TAG, "Error loading maintenance records", exception);
                runOnUiThread(() -> updateUI(new ArrayList<>()));
            }
        });
    }

    private void applyFilters() {
        new Thread(() -> {
            List<MaintenanceRecord> filtered = new ArrayList<>();
            String queryLower = currentSearchQuery.toLowerCase().trim();

            for (MaintenanceRecord record : allMaintenanceList) {
                if (record == null) continue;

                // Status Filter
                boolean statusMatches = true;
                if (selectedStatusChipId == R.id.chipMaintOpen) {
                    statusMatches = "OPEN".equalsIgnoreCase(record.getStatus());
                } else if (selectedStatusChipId == R.id.chipMaintScheduled) {
                    statusMatches = "SCHEDULED".equalsIgnoreCase(record.getStatus());
                } else if (selectedStatusChipId == R.id.chipMaintInProgress) {
                    statusMatches = "IN_PROGRESS".equalsIgnoreCase(record.getStatus());
                } else if (selectedStatusChipId == R.id.chipMaintCompleted) {
                    statusMatches = "COMPLETED".equalsIgnoreCase(record.getStatus());
                }

                if (!statusMatches) continue;

                // Text Search
                if (!queryLower.isEmpty()) {
                    String title = record.getTitle() != null ? record.getTitle().toLowerCase() : "";
                    String desc = record.getDescription() != null ? record.getDescription().toLowerCase() : "";
                    String vendor = record.getVendorName() != null ? record.getVendorName().toLowerCase() : "";

                    boolean textMatches = title.contains(queryLower) || desc.contains(queryLower) || vendor.contains(queryLower);
                    if (!textMatches) continue;
                }

                filtered.add(record);
            }

            runOnUiThread(() -> updateUI(filtered));
        }).start();
    }

    private void updateUI(List<MaintenanceRecord> list) {
        if (list == null || list.isEmpty()) {
            if (tvEmpty != null) tvEmpty.setVisibility(View.VISIBLE);
            if (recyclerMaintenance != null) recyclerMaintenance.setVisibility(View.GONE);
            if (adapter != null) adapter.setItems(null);
        } else {
            if (tvEmpty != null) tvEmpty.setVisibility(View.GONE);
            if (recyclerMaintenance != null) recyclerMaintenance.setVisibility(View.VISIBLE);
            if (adapter != null) adapter.setItems(list);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadMaintenanceData();
    }
}

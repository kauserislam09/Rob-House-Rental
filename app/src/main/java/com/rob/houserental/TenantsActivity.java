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
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;
import com.rob.houserental.adapter.TenantAdapter;
import com.rob.houserental.model.Tenant;
import com.rob.houserental.repository.TenantRepository;

import java.util.ArrayList;
import java.util.List;

public class TenantsActivity extends AppCompatActivity {

    private RecyclerView recyclerTenants;
    private TextView tvTenantCount;
    private View layoutEmptyTenants;
    private ExtendedFloatingActionButton fabAddTenant;
    private TextInputEditText etSearchTenants;
    private ChipGroup chipGroupStatus;

    private TenantAdapter adapter;
    private TenantRepository repository;

    private String currentSearchQuery = "";
    private String currentStatusFilter = "ALL";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_tenants);

        initializeViews();

        setupToolbar();

        setupRecyclerView();

        repository = new TenantRepository(getApplicationContext());

        setupSearchAndFilter();

        setupListeners();

        loadTenants();
    }

    private void initializeViews() {
        recyclerTenants = findViewById(R.id.recyclerTenants);
        tvTenantCount = findViewById(R.id.tvTenantCount);
        layoutEmptyTenants = findViewById(R.id.layoutEmptyTenants);
        fabAddTenant = findViewById(R.id.fabAddTenant);
        etSearchTenants = findViewById(R.id.etSearchTenants);
        chipGroupStatus = findViewById(R.id.chipGroupStatus);
    }

    private void setupToolbar() {
        MaterialToolbar toolbar = findViewById(R.id.toolbarTenants);
        toolbar.setNavigationOnClickListener(v -> finish());
    }

    private void setupRecyclerView() {
        adapter = new TenantAdapter();
        recyclerTenants.setLayoutManager(new LinearLayoutManager(this));
        recyclerTenants.setAdapter(adapter);

        adapter.setOnTenantClickListener(tenant -> {
            Intent intent = new Intent(TenantsActivity.this, TenantDetailsActivity.class);
            intent.putExtra("tenant_id", tenant.getId());
            startActivity(intent);
        });
    }

    private void setupSearchAndFilter() {
        etSearchTenants.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int count, int after) {
                currentSearchQuery = s != null ? s.toString().trim() : "";
                applySearchAndFilter();
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        chipGroupStatus.setOnCheckedStateChangeListener((group, checkedIds) -> {
            if (checkedIds.isEmpty()) {
                currentStatusFilter = "ALL";
            } else {
                int checkedId = checkedIds.get(0);
                if (checkedId == R.id.chipActive) {
                    currentStatusFilter = "ACTIVE";
                } else if (checkedId == R.id.chipInactive) {
                    currentStatusFilter = "INACTIVE";
                } else if (checkedId == R.id.chipArchived) {
                    currentStatusFilter = "ARCHIVED";
                } else {
                    currentStatusFilter = "ALL";
                }
            }
            applySearchAndFilter();
        });
    }

    private void setupListeners() {
        fabAddTenant.setOnClickListener(v -> {
            Intent intent = new Intent(TenantsActivity.this, AddTenantActivity.class);
            startActivity(intent);
        });
    }

    private void loadTenants() {
        applySearchAndFilter();
    }

    private void applySearchAndFilter() {
        if (!TextUtils.isEmpty(currentSearchQuery)) {
            repository.searchTenants(currentSearchQuery, new TenantRepository.DatabaseCallback<List<Tenant>>() {
                @Override
                public void onSuccess(List<Tenant> tenants) {
                    List<Tenant> filtered = filterByStatus(tenants, currentStatusFilter);
                    runOnUiThread(() -> updateList(filtered));
                }

                @Override
                public void onError(Exception exception) {
                    runOnUiThread(() -> updateList(new ArrayList<>()));
                }
            });
        } else if (!"ALL".equals(currentStatusFilter)) {
            repository.getTenantsByStatus(currentStatusFilter, new TenantRepository.DatabaseCallback<List<Tenant>>() {
                @Override
                public void onSuccess(List<Tenant> tenants) {
                    runOnUiThread(() -> updateList(tenants));
                }

                @Override
                public void onError(Exception exception) {
                    runOnUiThread(() -> updateList(new ArrayList<>()));
                }
            });
        } else {
            repository.getAllTenants(new TenantRepository.DatabaseCallback<List<Tenant>>() {
                @Override
                public void onSuccess(List<Tenant> tenants) {
                    runOnUiThread(() -> updateList(tenants));
                }

                @Override
                public void onError(Exception exception) {
                    runOnUiThread(() -> updateList(new ArrayList<>()));
                }
            });
        }
    }

    private List<Tenant> filterByStatus(List<Tenant> list, String statusFilter) {
        if (list == null || "ALL".equals(statusFilter)) {
            return list;
        }

        List<Tenant> filtered = new ArrayList<>();
        for (Tenant tenant : list) {
            if (statusFilter.equalsIgnoreCase(tenant.getStatus())) {
                filtered.add(tenant);
            }
        }
        return filtered;
    }

    private void updateList(List<Tenant> tenants) {
        int count = tenants != null ? tenants.size() : 0;
        tvTenantCount.setText(count == 1 ? getString(R.string.count_tenants_singular, count) : getString(R.string.count_tenants_plural, count));

        if (count == 0) {
            recyclerTenants.setVisibility(View.GONE);
            layoutEmptyTenants.setVisibility(View.VISIBLE);
            adapter.setTenants(null);
        } else {
            recyclerTenants.setVisibility(View.VISIBLE);
            layoutEmptyTenants.setVisibility(View.GONE);
            adapter.setTenants(tenants);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (repository != null) {
            loadTenants();
        }
    }
}

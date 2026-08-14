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
import com.rob.houserental.adapter.TenancyAdapter;
import com.rob.houserental.model.TenancyWithDetails;
import com.rob.houserental.repository.TenancyRepository;

import java.util.ArrayList;
import java.util.List;

public class TenanciesActivity extends AppCompatActivity {

    private RecyclerView recyclerTenancies;
    private TextView tvTenancyCount;
    private View layoutEmptyTenancies;
    private MaterialButton btnEmptyCreateTenancy;
    private ExtendedFloatingActionButton fabAddTenancy;
    private TextInputEditText etSearchTenancies;
    private ChipGroup chipGroupTenancyStatus;

    private TenancyAdapter adapter;
    private TenancyRepository repository;

    private final List<TenancyWithDetails> allTenancies = new ArrayList<>();
    private String currentSearchQuery = "";
    private String currentStatusFilter = "ALL";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_tenancies);

        initializeViews();

        setupToolbar();

        setupRecyclerView();

        repository = new TenancyRepository(getApplicationContext());

        setupSearchAndFilter();

        setupListeners();

        loadTenancies();
    }

    private void initializeViews() {
        recyclerTenancies = findViewById(R.id.recyclerTenancies);
        tvTenancyCount = findViewById(R.id.tvTenancyCount);
        layoutEmptyTenancies = findViewById(R.id.layoutEmptyTenancies);
        btnEmptyCreateTenancy = findViewById(R.id.btnEmptyCreateTenancy);
        fabAddTenancy = findViewById(R.id.fabAddTenancy);
        etSearchTenancies = findViewById(R.id.etSearchTenancies);
        chipGroupTenancyStatus = findViewById(R.id.chipGroupTenancyStatus);
    }

    private void setupToolbar() {
        MaterialToolbar toolbar = findViewById(R.id.toolbarTenancies);
        toolbar.setNavigationOnClickListener(v -> finish());
    }

    private void setupRecyclerView() {
        adapter = new TenancyAdapter();
        recyclerTenancies.setLayoutManager(new LinearLayoutManager(this));
        recyclerTenancies.setAdapter(adapter);

        adapter.setOnTenancyClickListener(details -> {
            Intent intent = new Intent(TenanciesActivity.this, TenancyDetailsActivity.class);
            intent.putExtra("tenancy_id", details.tenancy.getId());
            startActivity(intent);
        });
    }

    private void setupSearchAndFilter() {
        etSearchTenancies.addTextChangedListener(new TextWatcher() {
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

        chipGroupTenancyStatus.setOnCheckedStateChangeListener((group, checkedIds) -> {
            if (checkedIds.isEmpty()) {
                currentStatusFilter = "ALL";
            } else {
                int checkedId = checkedIds.get(0);
                if (checkedId == R.id.chipTenancyActive) {
                    currentStatusFilter = "ACTIVE";
                } else if (checkedId == R.id.chipTenancyEnded) {
                    currentStatusFilter = "ENDED";
                } else if (checkedId == R.id.chipTenancyCancelled) {
                    currentStatusFilter = "CANCELLED";
                } else {
                    currentStatusFilter = "ALL";
                }
            }
            applyFilterAndSearch();
        });
    }

    private void setupListeners() {
        fabAddTenancy.setOnClickListener(v -> {
            Intent intent = new Intent(TenanciesActivity.this, AddTenancyActivity.class);
            startActivity(intent);
        });

        btnEmptyCreateTenancy.setOnClickListener(v -> {
            Intent intent = new Intent(TenanciesActivity.this, AddTenancyActivity.class);
            startActivity(intent);
        });
    }

    private void loadTenancies() {
        repository.getAllTenanciesWithDetails(new TenancyRepository.DatabaseCallback<List<TenancyWithDetails>>() {
            @Override
            public void onSuccess(List<TenancyWithDetails> list) {
                runOnUiThread(() -> {
                    allTenancies.clear();
                    if (list != null) {
                        allTenancies.addAll(list);
                    }
                    applyFilterAndSearch();
                });
            }

            @Override
            public void onError(Exception exception) {
                runOnUiThread(() -> {
                    allTenancies.clear();
                    applyFilterAndSearch();
                });
            }
        });
    }

    private void applyFilterAndSearch() {
        List<TenancyWithDetails> filtered = new ArrayList<>();

        for (TenancyWithDetails item : allTenancies) {
            if (item.tenancy == null) continue;

            // Status Match
            boolean statusMatches = "ALL".equalsIgnoreCase(currentStatusFilter) ||
                    currentStatusFilter.equalsIgnoreCase(item.tenancy.getStatus());

            if (!statusMatches) continue;

            // Search Match
            if (TextUtils.isEmpty(currentSearchQuery)) {
                filtered.add(item);
            } else {
                boolean matches = false;

                if (item.tenant != null && item.tenant.getFullName() != null &&
                        item.tenant.getFullName().toLowerCase().contains(currentSearchQuery)) {
                    matches = true;
                } else if (item.unit != null && item.unit.getUnitNumber() != null &&
                        item.unit.getUnitNumber().toLowerCase().contains(currentSearchQuery)) {
                    matches = true;
                } else if (item.tenancy.getAgreementNumber() != null &&
                        item.tenancy.getAgreementNumber().toLowerCase().contains(currentSearchQuery)) {
                    matches = true;
                }

                if (matches) {
                    filtered.add(item);
                }
            }
        }

        updateList(filtered);
    }

    private void updateList(List<TenancyWithDetails> list) {
        int count = list != null ? list.size() : 0;
        tvTenancyCount.setText(count == 1 ? getString(R.string.count_tenancies_singular, count) : getString(R.string.count_tenancies_plural, count));

        if (count == 0) {
            recyclerTenancies.setVisibility(View.GONE);
            layoutEmptyTenancies.setVisibility(View.VISIBLE);
            adapter.setTenancies(null);
        } else {
            recyclerTenancies.setVisibility(View.VISIBLE);
            layoutEmptyTenancies.setVisibility(View.GONE);
            adapter.setTenancies(list);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (repository != null) {
            loadTenancies();
        }
    }
}

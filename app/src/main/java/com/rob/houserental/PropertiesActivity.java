package com.rob.houserental;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.rob.houserental.adapter.PropertyAdapter;
import com.rob.houserental.model.Property;
import com.rob.houserental.repository.PropertyRepository;

import java.util.ArrayList;
import java.util.List;

public class PropertiesActivity extends AppCompatActivity {

    private RecyclerView recyclerProperties;
    private TextView tvPropertyCount;
    private View layoutEmptyProperties;
    private ExtendedFloatingActionButton fabAddProperty;
    private EditText etSearchProperty;
    private ChipGroup chipGroupPropertyStatus;

    private PropertyAdapter adapter;
    private PropertyRepository repository;

    private List<Property> allPropertiesList = new ArrayList<>();
    private String currentSearchQuery = "";
    private int currentStatusFilter = R.id.chipPropAll;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_properties);

        repository = new PropertyRepository(getApplicationContext());

        initializeViews();
        setupToolbar();
        setupRecyclerView();
        setupListeners();

        loadProperties();
    }

    private void initializeViews() {
        recyclerProperties = findViewById(R.id.recyclerProperties);
        tvPropertyCount = findViewById(R.id.tvPropertyCount);
        layoutEmptyProperties = findViewById(R.id.layoutEmptyProperties);
        fabAddProperty = findViewById(R.id.fabAddProperty);
        etSearchProperty = findViewById(R.id.etSearchProperty);
        chipGroupPropertyStatus = findViewById(R.id.chipGroupPropertyStatus);
    }

    private void setupToolbar() {
        MaterialToolbar toolbar = findViewById(R.id.toolbarProperties);
        toolbar.setNavigationOnClickListener(v -> finish());
    }

    private void setupRecyclerView() {
        adapter = new PropertyAdapter();
        recyclerProperties.setLayoutManager(new LinearLayoutManager(this));
        recyclerProperties.setAdapter(adapter);

        adapter.setOnPropertyClickListener(property -> {
            Intent intent = new Intent(PropertiesActivity.this, PropertyDetailsActivity.class);
            intent.putExtra("property_id", property.getId());
            intent.putExtra("property_name", property.getName());
            intent.putExtra("property_address", property.getAddress());
            intent.putExtra("property_type", property.getPropertyType());
            intent.putExtra("property_floors", property.getNumberOfFloors());
            intent.putExtra("property_notes", property.getNotes());
            startActivity(intent);
        });
    }

    private void setupListeners() {
        fabAddProperty.setOnClickListener(v -> {
            Intent intent = new Intent(PropertiesActivity.this, AddPropertyActivity.class);
            startActivity(intent);
        });

        etSearchProperty.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                currentSearchQuery = s != null ? s.toString().trim() : "";
                applyFilterAndSearch();
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        chipGroupPropertyStatus.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId != View.NO_ID) {
                currentStatusFilter = checkedId;
            } else {
                currentStatusFilter = R.id.chipPropAll;
            }
            applyFilterAndSearch();
        });
    }

    private void loadProperties() {
        repository.getAllProperties(new PropertyRepository.DatabaseCallback<List<Property>>() {
            @Override
            public void onSuccess(List<Property> properties) {
                allPropertiesList = properties != null ? properties : new ArrayList<>();
                applyFilterAndSearch();
            }

            @Override
            public void onError(Exception exception) {
                runOnUiThread(() -> tvPropertyCount.setText(R.string.unable_to_load_properties));
            }
        });
    }

    private void applyFilterAndSearch() {
        new Thread(() -> {
            List<Property> filtered = new ArrayList<>();
            String queryLower = currentSearchQuery.toLowerCase().trim();

            for (Property prop : allPropertiesList) {
                // Status Filter Check
                boolean statusMatches = true;
                if (currentStatusFilter == R.id.chipPropInactive) {
                    statusMatches = false; // DB Schema V11 properties are all active
                }

                if (!statusMatches) continue;

                // Search Check
                if (!queryLower.isEmpty()) {
                    String name = prop.getName() != null ? prop.getName().toLowerCase() : "";
                    String address = prop.getAddress() != null ? prop.getAddress().toLowerCase() : "";
                    String type = prop.getPropertyType() != null ? prop.getPropertyType().toLowerCase() : "";

                    boolean textMatches = name.contains(queryLower) || address.contains(queryLower) || type.contains(queryLower);
                    if (!textMatches) continue;
                }

                filtered.add(prop);
            }

            runOnUiThread(() -> {
                adapter.setProperties(filtered);
                updateEmptyState(filtered);
            });
        }).start();
    }

    private void updateEmptyState(List<Property> properties) {
        int count = properties.size();

        tvPropertyCount.setText(
                count == 1
                        ? getString(R.string.count_properties_singular, count)
                        : getString(R.string.count_properties_plural, count)
        );

        if (count == 0) {
            recyclerProperties.setVisibility(View.GONE);
            layoutEmptyProperties.setVisibility(View.VISIBLE);
        } else {
            recyclerProperties.setVisibility(View.VISIBLE);
            layoutEmptyProperties.setVisibility(View.GONE);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (repository != null) {
            loadProperties();
        }
    }
}
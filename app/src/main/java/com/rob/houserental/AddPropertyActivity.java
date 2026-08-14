package com.rob.houserental;

import android.os.Bundle;
import android.text.TextUtils;
import android.widget.ArrayAdapter;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.MaterialAutoCompleteTextView;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.rob.houserental.adapter.PropertyAdapter;
import com.rob.houserental.model.Property;
import com.rob.houserental.repository.PropertyRepository;

public class AddPropertyActivity extends AppCompatActivity {

    private TextInputLayout layoutPropertyName;
    private TextInputLayout layoutPropertyAddress;
    private Property editingProperty;
    private boolean isEditMode = false;
    private TextInputEditText etPropertyName;
    private TextInputEditText etPropertyAddress;
    private TextInputEditText etNumberOfFloors;
    private TextInputEditText etPropertyNotes;

    private MaterialAutoCompleteTextView autoPropertyType;

    private MaterialButton btnSaveProperty;

    private PropertyRepository repository;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(
                R.layout.activity_add_property
        );

        initializeViews();
        checkEditMode();

        setupToolbar();

        setupPropertyTypeDropdown();

        repository =
                new PropertyRepository(
                        getApplicationContext()
                );

        btnSaveProperty.setOnClickListener(
                v -> saveProperty()
        );
    }

    private void initializeViews() {

        layoutPropertyName =
                findViewById(
                        R.id.layoutPropertyName
                );

        layoutPropertyAddress =
                findViewById(
                        R.id.layoutPropertyAddress
                );

        etPropertyName =
                findViewById(
                        R.id.etPropertyName
                );

        etPropertyAddress =
                findViewById(
                        R.id.etPropertyAddress
                );

        autoPropertyType =
                findViewById(
                        R.id.autoPropertyType
                );

        etNumberOfFloors =
                findViewById(
                        R.id.etNumberOfFloors
                );

        etPropertyNotes =
                findViewById(
                        R.id.etPropertyNotes
                );

        btnSaveProperty =
                findViewById(
                        R.id.btnSaveProperty
                );
    }

    private void setupToolbar() {

        MaterialToolbar toolbar =
                findViewById(
                        R.id.toolbarAddProperty
                );

        toolbar.setNavigationOnClickListener(
                v -> finish()
        );
    }

    private final String[] propertyTypeKeys = {
            "Apartment Building",
            "House",
            "Duplex",
            "Commercial Building",
            "Shop",
            "Office",
            "Other"
    };

    private void setupPropertyTypeDropdown() {
        String[] displayLabels = new String[propertyTypeKeys.length];
        for (int i = 0; i < propertyTypeKeys.length; i++) {
            displayLabels[i] = PropertyAdapter.getPropertyTypeDisplay(this, propertyTypeKeys[i]);
        }

        ArrayAdapter<String> adapter =
                new ArrayAdapter<>(
                        this,
                        android.R.layout.simple_dropdown_item_1line,
                        displayLabels
                );

        autoPropertyType.setAdapter(adapter);

        autoPropertyType.setText(
                displayLabels[0],
                false
        );
    }

    private void saveProperty() {

        clearErrors();

        String name =
                getText(
                        etPropertyName
                );

        String address =
                getText(
                        etPropertyAddress
                );

        String selectedDisplay =
                getText(
                        autoPropertyType
                );

        String propertyType = "Apartment Building";
        for (String key : propertyTypeKeys) {
            if (PropertyAdapter.getPropertyTypeDisplay(this, key).equalsIgnoreCase(selectedDisplay) || key.equalsIgnoreCase(selectedDisplay)) {
                propertyType = key;
                break;
            }
        }

        String floorsText =
                getText(
                        etNumberOfFloors
                );

        String notes =
                getText(
                        etPropertyNotes
                );

        if (TextUtils.isEmpty(name)) {

            layoutPropertyName.setError(
                    getString(R.string.property_name_required)
            );

            etPropertyName.requestFocus();

            return;
        }

        if (TextUtils.isEmpty(address)) {

            layoutPropertyAddress.setError(
                    getString(R.string.address_required)
            );

            etPropertyAddress.requestFocus();

            return;
        }

        int numberOfFloors = 0;

        if (!TextUtils.isEmpty(floorsText)) {

            try {

                numberOfFloors =
                        Integer.parseInt(
                                floorsText
                        );

            } catch (NumberFormatException e) {

                etNumberOfFloors.setError(
                        getString(R.string.valid_number_required)
                );

                return;
            }
        }

        Property property;

        long currentTime =
                System.currentTimeMillis();

        if (isEditMode) {

            property = editingProperty;

            property.setName(name);

            property.setAddress(address);

            property.setPropertyType(propertyType);

            property.setNumberOfFloors(
                    numberOfFloors
            );

            property.setNotes(notes);

            // Keep original creation time.
            // Only update modification time.
            property.setUpdatedAt(
                    currentTime
            );

        } else {

            property = new Property();

            property.setName(name);

            property.setAddress(address);

            property.setPropertyType(propertyType);

            property.setNumberOfFloors(
                    numberOfFloors
            );

            property.setNotes(notes);

            property.setCreatedAt(
                    currentTime
            );

            property.setUpdatedAt(
                    currentTime
            );
        }

        btnSaveProperty.setEnabled(false);

        if (isEditMode) {

            repository.update(
                    property,
                    new PropertyRepository.DatabaseCallback<Void>() {

                        @Override
                        public void onSuccess(Void result) {

                            runOnUiThread(() -> {

                                btnSaveProperty.setEnabled(true);

                                Intent resultIntent =
                                        new Intent();

                                resultIntent.putExtra(
                                        "property_id",
                                        property.getId()
                                );

                                resultIntent.putExtra(
                                        "property_name",
                                        property.getName()
                                );

                                resultIntent.putExtra(
                                        "property_address",
                                        property.getAddress()
                                );

                                resultIntent.putExtra(
                                        "property_type",
                                        property.getPropertyType()
                                );

                                resultIntent.putExtra(
                                        "property_floors",
                                        property.getNumberOfFloors()
                                );

                                resultIntent.putExtra(
                                        "property_notes",
                                        property.getNotes()
                                );

                                setResult(
                                        RESULT_OK,
                                        resultIntent
                                );

                                finish();
                            });
                        }

                        @Override
                        public void onError(
                                Exception exception
                        ) {

                            runOnUiThread(() -> {

                                btnSaveProperty.setEnabled(true);

                                btnSaveProperty.setError(
                                        getString(R.string.update_failed)
                                );
                            });
                        }
                    }
            );

        } else {

            repository.insert(
                    property,
                    new PropertyRepository.DatabaseCallback<Long>() {

                        @Override
                        public void onSuccess(Long id) {

                            runOnUiThread(() -> {

                                btnSaveProperty.setEnabled(true);

                                setResult(
                                        RESULT_OK
                                );

                                finish();
                            });
                        }

                        @Override
                        public void onError(
                                Exception exception
                        ) {

                            runOnUiThread(() -> {

                                btnSaveProperty.setEnabled(true);

                                btnSaveProperty.setError(
                                        getString(R.string.save_failed)
                                );
                            });
                        }
                    }
            );
        }
    }
    private void checkEditMode() {

        long propertyId =
                getIntent().getLongExtra(
                        "property_id",
                        -1
                );

        if (propertyId == -1) {
            return;
        }

        isEditMode = true;

        editingProperty =
                new Property();

        editingProperty.setId(propertyId);

        editingProperty.setName(
                getIntent().getStringExtra(
                        "property_name"
                )
        );

        editingProperty.setAddress(
                getIntent().getStringExtra(
                        "property_address"
                )
        );

        editingProperty.setPropertyType(
                getIntent().getStringExtra(
                        "property_type"
                )
        );

        editingProperty.setNumberOfFloors(
                getIntent().getIntExtra(
                        "property_floors",
                        0
                )
        );

        editingProperty.setNotes(
                getIntent().getStringExtra(
                        "property_notes"
                )
        );

        editingProperty.setCreatedAt(
                getIntent().getLongExtra(
                        "property_created_at",
                        0
                )
        );

        editingProperty.setUpdatedAt(
                getIntent().getLongExtra(
                        "property_updated_at",
                        0
                )
        );

        populateEditFields();

        MaterialToolbar toolbar =
                findViewById(
                        R.id.toolbarAddProperty
                );

        toolbar.setTitle(
                R.string.edit_property
        );

        btnSaveProperty.setText(
                R.string.update_property
        );
    }
    private void populateEditFields() {

        etPropertyName.setText(
                editingProperty.getName()
        );

        etPropertyAddress.setText(
                editingProperty.getAddress()
        );

        autoPropertyType.setText(
                PropertyAdapter.getPropertyTypeDisplay(this, editingProperty.getPropertyType()),
                false
        );

        if (editingProperty.getNumberOfFloors() > 0) {

            etNumberOfFloors.setText(
                    String.valueOf(
                            editingProperty.getNumberOfFloors()
                    )
            );
        }

        etPropertyNotes.setText(
                editingProperty.getNotes()
        );
    }

    private String getText(
            android.widget.TextView view
    ) {

        if (view.getText() == null) {
            return "";
        }

        return view.getText()
                .toString()
                .trim();
    }

    private void clearErrors() {

        layoutPropertyName.setError(null);

        layoutPropertyAddress.setError(null);

        etNumberOfFloors.setError(null);

        btnSaveProperty.setError(null);
    }
}
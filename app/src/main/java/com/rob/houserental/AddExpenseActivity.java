package com.rob.houserental;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.MaterialAutoCompleteTextView;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.rob.houserental.data.AppDatabase;
import com.rob.houserental.model.Expense;
import com.rob.houserental.model.Property;
import com.rob.houserental.model.Unit;
import com.rob.houserental.repository.ExpenseRepository;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class AddExpenseActivity extends AppCompatActivity {

    private TextInputLayout layoutSelectExpenseProperty;
    private MaterialAutoCompleteTextView autoExpenseProperty;

    private TextInputLayout layoutSelectExpenseUnit;
    private MaterialAutoCompleteTextView autoExpenseUnit;

    private TextInputLayout layoutSelectExpenseCategory;
    private MaterialAutoCompleteTextView autoExpenseCategory;

    private TextInputLayout layoutExpenseAmount;
    private TextInputEditText etExpenseAmount;

    private TextInputLayout layoutExpenseDate;
    private TextInputEditText etExpenseDate;

    private TextInputEditText etExpenseDescription;
    private TextInputEditText etExpenseNotes;

    private MaterialButton btnAttachReceipt;
    private View layoutReceiptPreview;
    private TextView tvReceiptFileName;
    private MaterialButton btnRemoveReceipt;

    private MaterialButton btnSaveExpense;

    private ExpenseRepository repository;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    private final List<Property> propertyList = new ArrayList<>();
    private final List<Unit> unitList = new ArrayList<>();

    private Property selectedProperty;
    private Unit selectedUnit; // null if whole property
    private String selectedCategory = "REPAIR";

    private String attachedReceiptPath;
    private String attachedReceiptName;
    private String attachedReceiptMimeType;

    private boolean isEditMode = false;
    private long expenseId = -1;
    private Expense editingExpense;

    private final Calendar expenseCalendar = Calendar.getInstance();
    private static final SimpleDateFormat expenseDateFormat = new SimpleDateFormat("dd MMM yyyy", Locale.getDefault());
    private static final SimpleDateFormat monthFormat = new SimpleDateFormat("yyyy-MM", Locale.getDefault());

    private final ActivityResultLauncher<String> receiptPickerLauncher =
            registerForActivityResult(new ActivityResultContracts.GetContent(), uri -> {
                if (uri != null) {
                    saveReceiptLocally(uri);
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_add_expense);

        expenseId = getIntent().getLongExtra("expense_id", -1);
        String defaultMonth = getIntent().getStringExtra("default_expense_month");

        initializeViews();

        setupToolbar();

        repository = new ExpenseRepository(getApplicationContext());

        setupCategoryDropdown();

        setupDatePicker(defaultMonth);

        loadProperties();

        checkEditMode();

        setupListeners();
    }

    private void initializeViews() {
        layoutSelectExpenseProperty = findViewById(R.id.layoutSelectExpenseProperty);
        autoExpenseProperty = findViewById(R.id.autoExpenseProperty);

        layoutSelectExpenseUnit = findViewById(R.id.layoutSelectExpenseUnit);
        autoExpenseUnit = findViewById(R.id.autoExpenseUnit);

        layoutSelectExpenseCategory = findViewById(R.id.layoutSelectExpenseCategory);
        autoExpenseCategory = findViewById(R.id.autoExpenseCategory);

        layoutExpenseAmount = findViewById(R.id.layoutExpenseAmount);
        etExpenseAmount = findViewById(R.id.etExpenseAmount);

        layoutExpenseDate = findViewById(R.id.layoutExpenseDate);
        etExpenseDate = findViewById(R.id.etExpenseDate);

        etExpenseDescription = findViewById(R.id.etExpenseDescription);
        etExpenseNotes = findViewById(R.id.etExpenseNotes);

        btnAttachReceipt = findViewById(R.id.btnAttachReceipt);
        layoutReceiptPreview = findViewById(R.id.layoutReceiptPreview);
        tvReceiptFileName = findViewById(R.id.tvReceiptFileName);
        btnRemoveReceipt = findViewById(R.id.btnRemoveReceipt);

        btnSaveExpense = findViewById(R.id.btnSaveExpense);
    }

    private void setupToolbar() {
        MaterialToolbar toolbar = findViewById(R.id.toolbarAddExpense);
        toolbar.setNavigationOnClickListener(v -> finish());
    }

    private void setupCategoryDropdown() {
        String[] categoryKeys = {
                "REPAIR", "MAINTENANCE", "CLEANING", "SECURITY",
                "GENERATOR", "PLUMBING", "ELECTRICAL", "PAINTING",
                "RENOVATION", "PROPERTY_TAX", "SERVICE_CHARGE", "OTHER"
        };
        String[] categoryLabels = {
                getString(R.string.expense_cat_repair),
                getString(R.string.expense_cat_maintenance),
                getString(R.string.expense_cat_cleaning),
                getString(R.string.expense_cat_security),
                getString(R.string.expense_cat_generator),
                getString(R.string.expense_cat_plumbing),
                getString(R.string.expense_cat_electrical),
                getString(R.string.expense_cat_painting),
                getString(R.string.expense_cat_renovation),
                getString(R.string.expense_cat_tax),
                getString(R.string.expense_cat_service),
                getString(R.string.expense_cat_other)
        };

        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_dropdown_item_1line,
                categoryLabels
        );
        autoExpenseCategory.setAdapter(adapter);
        autoExpenseCategory.setText(categoryLabels[0], false);
        selectedCategory = categoryKeys[0];

        autoExpenseCategory.setOnItemClickListener((parent, view, position, id) -> {
            selectedCategory = categoryKeys[position];
        });
    }

    private void setupDatePicker(String defaultMonth) {
        etExpenseDate.setText(expenseDateFormat.format(expenseCalendar.getTime()));

        etExpenseDate.setOnClickListener(v -> {
            new DatePickerDialog(
                    AddExpenseActivity.this,
                    (view, year, month, dayOfMonth) -> {
                        expenseCalendar.set(Calendar.YEAR, year);
                        expenseCalendar.set(Calendar.MONTH, month);
                        expenseCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                        etExpenseDate.setText(expenseDateFormat.format(expenseCalendar.getTime()));
                    },
                    expenseCalendar.get(Calendar.YEAR),
                    expenseCalendar.get(Calendar.MONTH),
                    expenseCalendar.get(Calendar.DAY_OF_MONTH)
            ).show();
        });
    }

    private void loadProperties() {
        executor.execute(() -> {
            try {
                AppDatabase db = AppDatabase.getInstance(getApplicationContext());
                List<Property> properties = db.propertyDao().getAllProperties();

                runOnUiThread(() -> {
                    propertyList.clear();
                    if (properties != null) {
                        propertyList.addAll(properties);
                    }

                    List<String> propertyNames = new ArrayList<>();
                    for (Property p : propertyList) {
                        propertyNames.add(p.getName());
                    }

                    ArrayAdapter<String> adapter = new ArrayAdapter<>(
                            AddExpenseActivity.this,
                            android.R.layout.simple_dropdown_item_1line,
                            propertyNames
                    );
                    autoExpenseProperty.setAdapter(adapter);

                    autoExpenseProperty.setOnItemClickListener((parent, view, position, id) -> {
                        selectedProperty = propertyList.get(position);
                        loadUnitsForProperty(selectedProperty.getId(), 0);
                    });
                });
            } catch (Exception ignored) {
            }
        });
    }

    private void loadUnitsForProperty(long propertyId, long targetUnitId) {
        executor.execute(() -> {
            try {
                AppDatabase db = AppDatabase.getInstance(getApplicationContext());
                List<Unit> units = db.unitDao().getUnitsByProperty(propertyId);

                runOnUiThread(() -> {
                    unitList.clear();
                    List<String> unitNames = new ArrayList<>();
                    unitNames.add(getString(R.string.whole_property_option));

                    int targetIndex = 0;

                    if (units != null) {
                        unitList.addAll(units);
                        for (int i = 0; i < units.size(); i++) {
                            Unit u = units.get(i);
                            unitNames.add(getString(R.string.prefix_unit_floor_format, u.getUnitNumber(), u.getFloor()));
                            if (u.getId() == targetUnitId) {
                                targetIndex = i + 1;
                            }
                        }
                    }

                    ArrayAdapter<String> adapter = new ArrayAdapter<>(
                            AddExpenseActivity.this,
                            android.R.layout.simple_dropdown_item_1line,
                            unitNames
                    );
                    autoExpenseUnit.setAdapter(adapter);

                    if (targetIndex > 0 && targetIndex <= unitList.size()) {
                        selectedUnit = unitList.get(targetIndex - 1);
                        autoExpenseUnit.setText(unitNames.get(targetIndex), false);
                    } else {
                        selectedUnit = null;
                        autoExpenseUnit.setText(unitNames.get(0), false);
                    }

                    autoExpenseUnit.setOnItemClickListener((parent, view, position, id) -> {
                        if (position == 0) {
                            selectedUnit = null;
                        } else {
                            selectedUnit = unitList.get(position - 1);
                        }
                    });
                });
            } catch (Exception ignored) {
            }
        });
    }

    private void checkEditMode() {
        if (expenseId == -1) {
            return;
        }

        isEditMode = true;
        MaterialToolbar toolbar = findViewById(R.id.toolbarAddExpense);
        toolbar.setTitle(R.string.edit_expense_title);
        btnSaveExpense.setText(R.string.edit_expense_title);

        repository.getExpenseById(expenseId, new ExpenseRepository.DatabaseCallback<Expense>() {
            @Override
            public void onSuccess(Expense expense) {
                if (expense != null) {
                    editingExpense = expense;
                    runOnUiThread(() -> populateEditFields(expense));
                }
            }

            @Override
            public void onError(Exception exception) {
            }
        });
    }

    private void populateEditFields(Expense expense) {
        selectedCategory = expense.getCategory();
        setCategoryDropdownSelection(selectedCategory);

        if (expense.getAmount() > 0) {
            etExpenseAmount.setText(String.valueOf(expense.getAmount()));
        }
        if (expense.getExpenseDate() != null) {
            etExpenseDate.setText(expense.getExpenseDate());
        }
        if (expense.getDescription() != null) {
            etExpenseDescription.setText(expense.getDescription());
        }
        if (expense.getNotes() != null) {
            etExpenseNotes.setText(expense.getNotes());
        }

        if (expense.getReceiptPath() != null && !expense.getReceiptPath().isEmpty()) {
            attachedReceiptPath = expense.getReceiptPath();
            attachedReceiptName = expense.getReceiptName() != null ? expense.getReceiptName() : "voucher_receipt";
            attachedReceiptMimeType = expense.getReceiptMimeType();
            showReceiptPreview(attachedReceiptName);
        }

        // Load and preserve property & unit relationships
        executor.execute(() -> {
            try {
                AppDatabase db = AppDatabase.getInstance(getApplicationContext());
                Property p = db.propertyDao().getPropertyById(expense.getPropertyId());
                if (p != null) {
                    selectedProperty = p;
                    runOnUiThread(() -> {
                        autoExpenseProperty.setText(p.getName(), false);
                        loadUnitsForProperty(p.getId(), expense.getUnitId());
                    });
                }
            } catch (Exception ignored) {
            }
        });
    }

    private void setCategoryDropdownSelection(String catKey) {
        String[] categoryKeys = {
                "REPAIR", "MAINTENANCE", "CLEANING", "SECURITY",
                "GENERATOR", "PLUMBING", "ELECTRICAL", "PAINTING",
                "RENOVATION", "PROPERTY_TAX", "SERVICE_CHARGE", "OTHER"
        };
        String[] categoryLabels = {
                getString(R.string.expense_cat_repair),
                getString(R.string.expense_cat_maintenance),
                getString(R.string.expense_cat_cleaning),
                getString(R.string.expense_cat_security),
                getString(R.string.expense_cat_generator),
                getString(R.string.expense_cat_plumbing),
                getString(R.string.expense_cat_electrical),
                getString(R.string.expense_cat_painting),
                getString(R.string.expense_cat_renovation),
                getString(R.string.expense_cat_tax),
                getString(R.string.expense_cat_service),
                getString(R.string.expense_cat_other)
        };

        for (int i = 0; i < categoryKeys.length; i++) {
            if (categoryKeys[i].equalsIgnoreCase(catKey)) {
                autoExpenseCategory.setText(categoryLabels[i], false);
                break;
            }
        }
    }

    private void setupListeners() {
        btnAttachReceipt.setOnClickListener(v -> receiptPickerLauncher.launch("*/*"));

        btnRemoveReceipt.setOnClickListener(v -> {
            attachedReceiptPath = null;
            attachedReceiptName = null;
            attachedReceiptMimeType = null;
            layoutReceiptPreview.setVisibility(View.GONE);
        });

        btnSaveExpense.setOnClickListener(v -> saveExpense());
    }

    private void saveReceiptLocally(Uri uri) {
        executor.execute(() -> {
            try {
                InputStream inputStream = getContentResolver().openInputStream(uri);
                if (inputStream == null) return;

                File receiptsDir = new File(getFilesDir(), "expenses/receipts");
                if (!receiptsDir.exists()) {
                    receiptsDir.mkdirs();
                }

                String fileName = "receipt_" + System.currentTimeMillis();
                File targetFile = new File(receiptsDir, fileName);

                FileOutputStream outputStream = new FileOutputStream(targetFile);
                byte[] buffer = new byte[4096];
                int bytesRead;
                while ((bytesRead = inputStream.read(buffer)) != -1) {
                    outputStream.write(buffer, 0, bytesRead);
                }
                outputStream.flush();
                outputStream.close();
                inputStream.close();

                attachedReceiptPath = targetFile.getAbsolutePath();
                attachedReceiptName = fileName;
                attachedReceiptMimeType = getContentResolver().getType(uri);

                runOnUiThread(() -> showReceiptPreview(attachedReceiptName));
            } catch (Exception e) {
                runOnUiThread(() -> Toast.makeText(AddExpenseActivity.this, R.string.receipt_attach_failed, Toast.LENGTH_SHORT).show());
            }
        });
    }

    private void showReceiptPreview(String fileName) {
        tvReceiptFileName.setText(fileName);
        layoutReceiptPreview.setVisibility(View.VISIBLE);
    }

    private void saveExpense() {
        clearErrors();

        if (selectedProperty == null && !isEditMode) {
            layoutSelectExpenseProperty.setError(getString(R.string.property_required));
            return;
        }

        String amountText = getText(etExpenseAmount);
        double amount = 0;
        if (!TextUtils.isEmpty(amountText)) {
            try {
                amount = Double.parseDouble(amountText);
            } catch (NumberFormatException ignored) {
            }
        }

        if (amount <= 0) {
            layoutExpenseAmount.setError(getString(R.string.invalid_expense_amount));
            etExpenseAmount.requestFocus();
            return;
        }

        String expenseDate = getText(etExpenseDate);
        if (TextUtils.isEmpty(expenseDate)) {
            layoutExpenseDate.setError(getString(R.string.expense_date_required));
            return;
        }

        String expenseMonth = monthFormat.format(expenseCalendar.getTime());
        String description = getText(etExpenseDescription);
        String notes = getText(etExpenseNotes);

        long unitId = selectedUnit != null ? selectedUnit.getId() : 0;
        long propertyId = selectedProperty != null ? selectedProperty.getId() : (editingExpense != null ? editingExpense.getPropertyId() : 0);

        long currentTime = System.currentTimeMillis();
        btnSaveExpense.setEnabled(false);

        if (isEditMode && editingExpense != null) {
            editingExpense.setPropertyId(propertyId);
            editingExpense.setUnitId(unitId);
            editingExpense.setCategory(selectedCategory);
            editingExpense.setAmount(amount);
            editingExpense.setExpenseDate(expenseDate);
            editingExpense.setExpenseMonth(expenseMonth);
            editingExpense.setDescription(description);
            editingExpense.setReceiptPath(attachedReceiptPath);
            editingExpense.setReceiptName(attachedReceiptName);
            editingExpense.setReceiptMimeType(attachedReceiptMimeType);
            editingExpense.setNotes(notes);

            repository.updateExpense(editingExpense, new ExpenseRepository.DatabaseCallback<Void>() {
                @Override
                public void onSuccess(Void result) {
                    runOnUiThread(() -> {
                        btnSaveExpense.setEnabled(true);
                        Toast.makeText(AddExpenseActivity.this, R.string.expense_updated_success, Toast.LENGTH_SHORT).show();
                        setResult(RESULT_OK);
                        finish();
                    });
                }

                @Override
                public void onError(Exception exception) {
                    runOnUiThread(() -> {
                        btnSaveExpense.setEnabled(true);
                        Toast.makeText(AddExpenseActivity.this, R.string.update_failed, Toast.LENGTH_SHORT).show();
                    });
                }
            });
        } else {
            Expense expense = new Expense(
                    propertyId,
                    unitId,
                    selectedCategory,
                    amount,
                    expenseDate,
                    expenseMonth,
                    description,
                    attachedReceiptPath,
                    attachedReceiptName,
                    attachedReceiptMimeType,
                    notes,
                    false,
                    currentTime,
                    currentTime
            );

            repository.createExpense(expense, new ExpenseRepository.DatabaseCallback<Long>() {
                @Override
                public void onSuccess(Long id) {
                    runOnUiThread(() -> {
                        btnSaveExpense.setEnabled(true);
                        Toast.makeText(AddExpenseActivity.this, R.string.expense_created_success, Toast.LENGTH_SHORT).show();
                        setResult(RESULT_OK);
                        finish();
                    });
                }

                @Override
                public void onError(Exception exception) {
                    runOnUiThread(() -> {
                        btnSaveExpense.setEnabled(true);
                        Toast.makeText(AddExpenseActivity.this, R.string.save_failed, Toast.LENGTH_SHORT).show();
                    });
                }
            });
        }
    }

    private String getText(android.widget.TextView view) {
        if (view == null || view.getText() == null) {
            return "";
        }
        return view.getText().toString().trim();
    }

    private void clearErrors() {
        layoutSelectExpenseProperty.setError(null);
        layoutSelectExpenseUnit.setError(null);
        layoutExpenseAmount.setError(null);
        layoutExpenseDate.setError(null);
    }
}

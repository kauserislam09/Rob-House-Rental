package com.rob.houserental;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.rob.houserental.adapter.ExpenseAdapter;
import com.rob.houserental.model.Expense;
import com.rob.houserental.model.ExpenseDisplayItem;
import com.rob.houserental.repository.ExpenseRepository;

import java.io.File;
import java.text.NumberFormat;
import java.util.Locale;

public class ExpenseDetailsActivity extends AppCompatActivity {

    private long expenseId;
    private ExpenseRepository repository;
    private ExpenseDisplayItem currentDisplayItem;
    private Expense currentExpense;

    private TextView tvExpenseDetailsCategoryIcon;
    private TextView tvExpenseDetailsCategoryTitle;
    private TextView tvExpenseDetailsAmount;
    private TextView tvExpenseDetailsDate;

    private TextView tvExpenseDetailsPropertyUnit;
    private TextView tvExpenseDetailsDescription;
    private TextView tvExpenseDetailsNotes;

    private TextView tvExpenseDetailsNoReceipt;
    private View layoutExpenseDetailsReceipt;
    private TextView tvExpenseDetailsReceiptName;
    private MaterialButton btnViewExpenseReceipt;

    private MaterialButton btnEditExpense;
    private MaterialButton btnArchiveExpense;
    private MaterialButton btnDeleteExpense;

    private static final NumberFormat currencyFormatter = NumberFormat.getNumberInstance(Locale.getDefault());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_expense_details);

        expenseId = getIntent().getLongExtra("expense_id", -1);
        if (expenseId == -1) {
            Toast.makeText(this, R.string.invalid_expense_record, Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        initializeViews();

        setupToolbar();

        repository = new ExpenseRepository(getApplicationContext());

        setupListeners();

        loadExpenseDetails();
    }

    private void initializeViews() {
        tvExpenseDetailsCategoryIcon = findViewById(R.id.tvExpenseDetailsCategoryIcon);
        tvExpenseDetailsCategoryTitle = findViewById(R.id.tvExpenseDetailsCategoryTitle);
        tvExpenseDetailsAmount = findViewById(R.id.tvExpenseDetailsAmount);
        tvExpenseDetailsDate = findViewById(R.id.tvExpenseDetailsDate);

        tvExpenseDetailsPropertyUnit = findViewById(R.id.tvExpenseDetailsPropertyUnit);
        tvExpenseDetailsDescription = findViewById(R.id.tvExpenseDetailsDescription);
        tvExpenseDetailsNotes = findViewById(R.id.tvExpenseDetailsNotes);

        tvExpenseDetailsNoReceipt = findViewById(R.id.tvExpenseDetailsNoReceipt);
        layoutExpenseDetailsReceipt = findViewById(R.id.layoutExpenseDetailsReceipt);
        tvExpenseDetailsReceiptName = findViewById(R.id.tvExpenseDetailsReceiptName);
        btnViewExpenseReceipt = findViewById(R.id.btnViewExpenseReceipt);

        btnEditExpense = findViewById(R.id.btnEditExpense);
        btnArchiveExpense = findViewById(R.id.btnArchiveExpense);
        btnDeleteExpense = findViewById(R.id.btnDeleteExpense);
    }

    private void setupToolbar() {
        MaterialToolbar toolbar = findViewById(R.id.toolbarExpenseDetails);
        toolbar.setNavigationOnClickListener(v -> finish());
    }

    private void setupListeners() {
        btnEditExpense.setOnClickListener(v -> {
            Intent intent = new Intent(ExpenseDetailsActivity.this, AddExpenseActivity.class);
            intent.putExtra("expense_id", expenseId);
            startActivity(intent);
        });

        btnArchiveExpense.setOnClickListener(v -> {
            if (currentDisplayItem != null && currentDisplayItem.isArchived) {
                showUnarchiveConfirmationDialog();
            } else {
                showArchiveConfirmationDialog();
            }
        });

        btnDeleteExpense.setOnClickListener(v -> showDeleteConfirmationDialog());

        btnViewExpenseReceipt.setOnClickListener(v -> openReceiptFile());
    }

    private void loadExpenseDetails() {
        repository.getExpenseDisplayItemById(expenseId, new ExpenseRepository.DatabaseCallback<ExpenseDisplayItem>() {
            @Override
            public void onSuccess(ExpenseDisplayItem item) {
                if (item == null) {
                    runOnUiThread(() -> {
                        Toast.makeText(ExpenseDetailsActivity.this, R.string.expense_not_found, Toast.LENGTH_SHORT).show();
                        finish();
                    });
                    return;
                }
                currentDisplayItem = item;
                runOnUiThread(() -> populateViews(item));
            }

            @Override
            public void onError(Exception exception) {
                runOnUiThread(() -> Toast.makeText(ExpenseDetailsActivity.this, R.string.expense_load_failed, Toast.LENGTH_SHORT).show());
            }
        });

        repository.getExpenseById(expenseId, new ExpenseRepository.DatabaseCallback<Expense>() {
            @Override
            public void onSuccess(Expense expense) {
                currentExpense = expense;
            }

            @Override
            public void onError(Exception exception) {
            }
        });
    }

    private void populateViews(ExpenseDisplayItem item) {
        String cat = item.category != null ? item.category : "OTHER";
        tvExpenseDetailsCategoryIcon.setText(ExpenseAdapter.getCategoryIcon(cat));
        tvExpenseDetailsCategoryTitle.setText(ExpenseAdapter.getCategoryTitle(this, cat));

        String curr = getString(R.string.currency_symbol);
        tvExpenseDetailsAmount.setText(curr + currencyFormatter.format(item.amount));
        tvExpenseDetailsDate.setText((item.expenseDate != null ? item.expenseDate : ""));

        String propName = item.propertyName != null ? item.propertyName : getString(R.string.property_label);
        if (item.unitNumber != null && !item.unitNumber.isEmpty()) {
            tvExpenseDetailsPropertyUnit.setText(propName + " • " + getString(R.string.prefix_unit_format, item.unitNumber));
        } else {
            tvExpenseDetailsPropertyUnit.setText(propName + " (" + getString(R.string.whole_property_option) + ")");
        }

        if (item.description != null && !item.description.trim().isEmpty()) {
            tvExpenseDetailsDescription.setText(item.description.trim());
            tvExpenseDetailsDescription.setVisibility(View.VISIBLE);
        } else {
            tvExpenseDetailsDescription.setText(R.string.no_description_provided);
        }

        if (item.notes != null && !item.notes.trim().isEmpty()) {
            tvExpenseDetailsNotes.setText(getString(R.string.prefix_notes, item.notes.trim()));
            tvExpenseDetailsNotes.setVisibility(View.VISIBLE);
        } else {
            tvExpenseDetailsNotes.setVisibility(View.GONE);
        }

        if (item.receiptPath != null && !item.receiptPath.isEmpty()) {
            File receiptFile = new File(item.receiptPath);
            if (receiptFile.exists()) {
                tvExpenseDetailsNoReceipt.setVisibility(View.GONE);
                layoutExpenseDetailsReceipt.setVisibility(View.VISIBLE);
                tvExpenseDetailsReceiptName.setText(item.receiptName != null ? item.receiptName : receiptFile.getName());
            } else {
                tvExpenseDetailsNoReceipt.setVisibility(View.VISIBLE);
                layoutExpenseDetailsReceipt.setVisibility(View.GONE);
            }
        } else {
            tvExpenseDetailsNoReceipt.setVisibility(View.VISIBLE);
            layoutExpenseDetailsReceipt.setVisibility(View.GONE);
        }

        if (item.isArchived) {
            btnArchiveExpense.setText(R.string.unarchive_expense);
        } else {
            btnArchiveExpense.setText(R.string.archive_expense);
        }
    }

    private void openReceiptFile() {
        if (currentDisplayItem == null) return;
        com.rob.houserental.utils.DocumentOpenUtils.openDocument(
                this,
                currentDisplayItem.receiptPath,
                currentDisplayItem.receiptMimeType,
                currentDisplayItem.receiptName
        );
    }

    private void showArchiveConfirmationDialog() {
        new MaterialAlertDialogBuilder(this)
                .setTitle(R.string.archive_expense)
                .setMessage(R.string.archive_expense_confirm)
                .setPositiveButton(R.string.archive_expense, (dialog, which) -> {
                    repository.archiveExpense(expenseId, new ExpenseRepository.DatabaseCallback<Void>() {
                        @Override
                        public void onSuccess(Void result) {
                            runOnUiThread(() -> {
                                Toast.makeText(ExpenseDetailsActivity.this, R.string.expense_archived_success, Toast.LENGTH_SHORT).show();
                                finish();
                            });
                        }

                        @Override
                        public void onError(Exception exception) {
                            runOnUiThread(() -> Toast.makeText(ExpenseDetailsActivity.this, R.string.expense_archive_failed, Toast.LENGTH_SHORT).show());
                        }
                    });
                })
                .setNegativeButton(R.string.cancel, null)
                .show();
    }

    private void showUnarchiveConfirmationDialog() {
        new MaterialAlertDialogBuilder(this)
                .setTitle(R.string.unarchive_expense)
                .setMessage(R.string.unarchive_expense_confirm)
                .setPositiveButton(R.string.unarchive_expense, (dialog, which) -> {
                    repository.unarchiveExpense(expenseId, new ExpenseRepository.DatabaseCallback<Void>() {
                        @Override
                        public void onSuccess(Void result) {
                            runOnUiThread(() -> {
                                Toast.makeText(ExpenseDetailsActivity.this, R.string.expense_unarchived_success, Toast.LENGTH_SHORT).show();
                                finish();
                            });
                        }

                        @Override
                        public void onError(Exception exception) {
                            runOnUiThread(() -> Toast.makeText(ExpenseDetailsActivity.this, R.string.expense_unarchive_failed, Toast.LENGTH_SHORT).show());
                        }
                    });
                })
                .setNegativeButton(R.string.cancel, null)
                .show();
    }

    private void showDeleteConfirmationDialog() {
        if (currentDisplayItem == null) return;

        String curr = getString(R.string.currency_symbol);
        String formattedAmount = curr + currencyFormatter.format(currentDisplayItem.amount);
        String categoryTitle = ExpenseAdapter.getCategoryTitle(this, currentDisplayItem.category);

        new MaterialAlertDialogBuilder(this)
                .setTitle(R.string.delete_expense)
                .setMessage(getString(R.string.delete_expense_confirm, categoryTitle, formattedAmount))
                .setPositiveButton(R.string.delete_expense, (dialog, which) -> {
                    if (currentExpense != null) {
                        repository.deleteExpense(currentExpense, new ExpenseRepository.DatabaseCallback<Void>() {
                            @Override
                            public void onSuccess(Void result) {
                                runOnUiThread(() -> {
                                    Toast.makeText(ExpenseDetailsActivity.this, R.string.expense_deleted_success, Toast.LENGTH_SHORT).show();
                                    finish();
                                });
                            }

                            @Override
                            public void onError(Exception exception) {
                                runOnUiThread(() -> Toast.makeText(ExpenseDetailsActivity.this, R.string.delete_failed, Toast.LENGTH_SHORT).show());
                            }
                        });
                    }
                })
                .setNegativeButton(R.string.cancel, null)
                .show();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (repository != null && expenseId != -1) {
            loadExpenseDetails();
        }
    }
}

package com.rob.houserental;

import android.app.DatePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.MaterialAutoCompleteTextView;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.rob.houserental.adapter.PaymentAdapter;
import com.rob.houserental.adapter.RentAdapter;
import com.rob.houserental.model.Payment;
import com.rob.houserental.model.RentRecord;
import com.rob.houserental.model.RentRecordDisplayItem;
import com.rob.houserental.repository.RentRepository;

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class RentDetailsActivity extends AppCompatActivity {

    private TextView tvRentDetailsTenantName;
    private TextView tvRentDetailsPropertyUnit;
    private TextView tvRentDetailsBillingMonth;
    private TextView tvRentDetailsStatusBadge;

    private TextView tvRentDetailsDue;
    private TextView tvRentDetailsPaid;
    private TextView tvRentDetailsRemaining;
    private TextView tvRentDetailsDueDate;
    private TextView tvRentDetailsLastPayment;
    private TextView tvRentDetailsNotes;

    private TextView tvPaymentCount;
    private TextView tvNoPayments;
    private RecyclerView recyclerPayments;

    private MaterialButton btnRecordPayment;
    private MaterialButton btnGenerateReceipt;
    private MaterialButton btnSendReminder;
    private MaterialButton btnEditRent;
    private MaterialButton btnWaiveRent;

    private androidx.activity.result.ActivityResultLauncher<Intent> receiptLauncher;

    private RentRepository repository;
    private PaymentAdapter paymentAdapter;

    private long rentRecordId = -1;
    private RentRecordDisplayItem currentDisplayItem;
    private RentRecord currentRecord;

    private static final NumberFormat currencyFormatter = NumberFormat.getNumberInstance(Locale.getDefault());
    private static final SimpleDateFormat paymentDateFormat = new SimpleDateFormat("dd MMM yyyy", Locale.getDefault());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_rent_details);

        rentRecordId = getIntent().getLongExtra("rent_id", -1);

        initializeViews();
        setupToolbar();
        setupRecyclerView();
        setupReceiptLauncher();

        repository = new RentRepository(getApplicationContext());

        loadRentDetails();
        loadPayments();
        setupListeners();
    }

    private void initializeViews() {
        tvRentDetailsTenantName = findViewById(R.id.tvRentDetailsTenantName);
        tvRentDetailsPropertyUnit = findViewById(R.id.tvRentDetailsPropertyUnit);
        tvRentDetailsBillingMonth = findViewById(R.id.tvRentDetailsBillingMonth);
        tvRentDetailsStatusBadge = findViewById(R.id.tvRentDetailsStatusBadge);

        tvRentDetailsDue = findViewById(R.id.tvRentDetailsDue);
        tvRentDetailsPaid = findViewById(R.id.tvRentDetailsPaid);
        tvRentDetailsRemaining = findViewById(R.id.tvRentDetailsRemaining);
        tvRentDetailsDueDate = findViewById(R.id.tvRentDetailsDueDate);
        tvRentDetailsLastPayment = findViewById(R.id.tvRentDetailsLastPayment);
        tvRentDetailsNotes = findViewById(R.id.tvRentDetailsNotes);

        tvPaymentCount = findViewById(R.id.tvPaymentCount);
        tvNoPayments = findViewById(R.id.tvNoPayments);
        recyclerPayments = findViewById(R.id.recyclerPayments);

        btnRecordPayment = findViewById(R.id.btnRecordPayment);
        btnGenerateReceipt = findViewById(R.id.btnGenerateReceipt);
        btnSendReminder = findViewById(R.id.btnSendReminder);
        btnEditRent = findViewById(R.id.btnEditRent);
        btnWaiveRent = findViewById(R.id.btnWaiveRent);
    }

    private void setupToolbar() {
        MaterialToolbar toolbar = findViewById(R.id.toolbarRentDetails);
        toolbar.setNavigationOnClickListener(v -> finish());
    }

    private void setupRecyclerView() {
        paymentAdapter = new PaymentAdapter();
        recyclerPayments.setLayoutManager(new LinearLayoutManager(this));
        recyclerPayments.setAdapter(paymentAdapter);
    }

    private void setupReceiptLauncher() {
        receiptLauncher = registerForActivityResult(
                new androidx.activity.result.contract.ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        android.net.Uri uri = result.getData().getData();
                        if (uri != null && currentDisplayItem != null) {
                            writeReceiptToUri(uri);
                        }
                    }
                }
        );
    }

    private void setupListeners() {
        btnRecordPayment.setOnClickListener(v -> showRecordPaymentDialog());
        btnGenerateReceipt.setOnClickListener(v -> launchReceiptExport());
        btnSendReminder.setOnClickListener(v -> showSendReminderDialog());
        btnEditRent.setOnClickListener(v -> showEditRentDialog());
        btnWaiveRent.setOnClickListener(v -> showWaiveRentDialog());
    }

    private void loadRentDetails() {
        if (rentRecordId == -1) {
            return;
        }

        repository.getRentDisplayItemById(rentRecordId, new RentRepository.DatabaseCallback<RentRecordDisplayItem>() {
            @Override
            public void onSuccess(RentRecordDisplayItem item) {
                if (item != null) {
                    currentDisplayItem = item;
                    runOnUiThread(() -> displayRentDetails(item));
                }
            }

            @Override
            public void onError(Exception exception) {
            }
        });

        repository.getRentRecordById(rentRecordId, new RentRepository.DatabaseCallback<RentRecord>() {
            @Override
            public void onSuccess(RentRecord record) {
                currentRecord = record;
            }

            @Override
            public void onError(Exception exception) {
            }
        });
    }

    private void displayRentDetails(RentRecordDisplayItem item) {
        tvRentDetailsTenantName.setText(item.tenantName != null ? item.tenantName : getString(R.string.tenant_label));
        String propName = item.propertyName != null ? item.propertyName : getString(R.string.property_label);
        String propUnit = item.unitNumber != null && !item.unitNumber.isEmpty()
                ? propName + " • " + getString(R.string.prefix_unit_format, item.unitNumber)
                : propName;
        tvRentDetailsPropertyUnit.setText(propUnit);
        tvRentDetailsBillingMonth.setText("📅 " + (item.billingMonth != null ? item.billingMonth : ""));

        // Status Badge
        String status = item.status != null ? item.status.trim().toUpperCase() : "UNPAID";
        tvRentDetailsStatusBadge.setText(RentAdapter.getStatusDisplay(this, status));
        applyStatusBadgeStyle(this, tvRentDetailsStatusBadge, status);

        // Balances
        String curr = getString(R.string.currency_symbol);
        tvRentDetailsDue.setText(curr + currencyFormatter.format(item.amountDue));
        tvRentDetailsPaid.setText(curr + currencyFormatter.format(item.amountPaid));
        tvRentDetailsRemaining.setText(curr + currencyFormatter.format(item.remainingAmount));

        tvRentDetailsDueDate.setText(getString(R.string.prefix_due_date, (item.dueDate != null ? item.dueDate : getString(R.string.not_set))));
        tvRentDetailsLastPayment.setText(getString(R.string.prefix_last_payment, (item.lastPaymentDate != null ? item.lastPaymentDate : getString(R.string.none_value))));
        tvRentDetailsNotes.setText(item.notes != null && !item.notes.trim().isEmpty() ? item.notes.trim() : getString(R.string.no_notes));

        // Action Buttons
        if ("PAID".equalsIgnoreCase(status) || "WAIVED".equalsIgnoreCase(status)) {
            btnRecordPayment.setVisibility(View.GONE);
            btnWaiveRent.setVisibility(View.GONE);
        } else {
            btnRecordPayment.setVisibility(View.VISIBLE);
            btnWaiveRent.setVisibility(View.VISIBLE);
        }
    }

    private void applyStatusBadgeStyle(Context context, TextView badge, String status) {
        int bgColor;
        int textColor;

        switch (status) {
            case "PAID":
                bgColor = ContextCompat.getColor(context, R.color.status_vacant_bg);
                textColor = ContextCompat.getColor(context, R.color.status_vacant_text);
                break;
            case "PARTIAL":
                bgColor = ContextCompat.getColor(context, R.color.status_reserved_bg);
                textColor = ContextCompat.getColor(context, R.color.status_reserved_text);
                break;
            case "OVERDUE":
                bgColor = ContextCompat.getColor(context, R.color.status_maintenance_bg);
                textColor = ContextCompat.getColor(context, R.color.status_maintenance_text);
                break;
            case "WAIVED":
                bgColor = ContextCompat.getColor(context, R.color.status_reserved_bg);
                textColor = ContextCompat.getColor(context, R.color.status_reserved_text);
                break;
            case "UNPAID":
            default:
                bgColor = ContextCompat.getColor(context, R.color.status_maintenance_bg);
                textColor = ContextCompat.getColor(context, R.color.status_maintenance_text);
                break;
        }

        GradientDrawable shape = new GradientDrawable();
        shape.setCornerRadius(24f);
        shape.setColor(bgColor);
        badge.setBackground(shape);
        badge.setTextColor(textColor);
    }

    private void loadPayments() {
        if (rentRecordId == -1) {
            return;
        }

        repository.getPaymentsByRentRecord(rentRecordId, new RentRepository.DatabaseCallback<List<Payment>>() {
            @Override
            public void onSuccess(List<Payment> payments) {
                runOnUiThread(() -> displayPayments(payments));
            }

            @Override
            public void onError(Exception exception) {
                runOnUiThread(() -> displayPayments(null));
            }
        });
    }

    private void displayPayments(List<Payment> payments) {
        int count = payments != null ? payments.size() : 0;
        tvPaymentCount.setText(count == 1 ? getString(R.string.count_records_singular, count) : getString(R.string.count_records_plural, count));

        if (payments == null || payments.isEmpty()) {
            tvNoPayments.setVisibility(View.VISIBLE);
            recyclerPayments.setVisibility(View.GONE);
            paymentAdapter.setPayments(null);
        } else {
            tvNoPayments.setVisibility(View.GONE);
            recyclerPayments.setVisibility(View.VISIBLE);
            paymentAdapter.setPayments(payments);
        }
    }

    private void showRecordPaymentDialog() {
        if (currentDisplayItem == null) {
            return;
        }

        View dialogView = LayoutInflater.from(this).inflate(R.layout.activity_add_tenancy, null);
        // Custom simple layout or programmatic dialog
        TextInputLayout layoutAmount = new TextInputLayout(this, null, com.google.android.material.R.attr.textInputOutlinedStyle);
        layoutAmount.setHint(getString(R.string.payment_amount));
        layoutAmount.setPrefixText("৳ ");
        TextInputEditText etAmount = new TextInputEditText(layoutAmount.getContext());
        etAmount.setInputType(android.text.InputType.TYPE_CLASS_NUMBER | android.text.InputType.TYPE_NUMBER_FLAG_DECIMAL);
        etAmount.setText(String.valueOf(currentDisplayItem.remainingAmount));
        layoutAmount.addView(etAmount);

        TextInputLayout layoutDate = new TextInputLayout(this, null, com.google.android.material.R.attr.textInputOutlinedStyle);
        layoutDate.setHint(getString(R.string.payment_date));
        layoutDate.setEndIconMode(TextInputLayout.END_ICON_CUSTOM);
        TextInputEditText etDate = new TextInputEditText(layoutDate.getContext());
        etDate.setFocusable(false);
        etDate.setClickable(true);
        Calendar calendar = Calendar.getInstance();
        etDate.setText(paymentDateFormat.format(calendar.getTime()));
        layoutDate.addView(etDate);

        etDate.setOnClickListener(v -> {
            new DatePickerDialog(
                    RentDetailsActivity.this,
                    (view, year, month, dayOfMonth) -> {
                        calendar.set(Calendar.YEAR, year);
                        calendar.set(Calendar.MONTH, month);
                        calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                        etDate.setText(paymentDateFormat.format(calendar.getTime()));
                    },
                    calendar.get(Calendar.YEAR),
                    calendar.get(Calendar.MONTH),
                    calendar.get(Calendar.DAY_OF_MONTH)
            ).show();
        });

        TextInputLayout layoutMethod = new TextInputLayout(this, null, com.google.android.material.R.attr.textInputOutlinedExposedDropdownMenuStyle);
        layoutMethod.setHint(getString(R.string.payment_method));
        MaterialAutoCompleteTextView autoMethod = new MaterialAutoCompleteTextView(layoutMethod.getContext());
        String[] displayMethods = com.rob.houserental.utils.PaymentMethodUtils.getDisplayMethods(this);
        String[] methodCodes = com.rob.houserental.utils.PaymentMethodUtils.getMethodCodes();
        autoMethod.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, displayMethods));
        autoMethod.setText(displayMethods[0], false);
        layoutMethod.addView(autoMethod);

        TextInputLayout layoutRef = new TextInputLayout(this, null, com.google.android.material.R.attr.textInputOutlinedStyle);
        layoutRef.setHint(getString(R.string.payment_reference));
        TextInputEditText etRef = new TextInputEditText(layoutRef.getContext());
        layoutRef.addView(etRef);

        TextInputLayout layoutNotes = new TextInputLayout(this, null, com.google.android.material.R.attr.textInputOutlinedStyle);
        layoutNotes.setHint(getString(R.string.notes));
        TextInputEditText etNotes = new TextInputEditText(layoutNotes.getContext());
        layoutNotes.addView(etNotes);

        android.widget.LinearLayout container = new android.widget.LinearLayout(this);
        container.setOrientation(android.widget.LinearLayout.VERTICAL);
        container.setPadding(48, 24, 48, 24);
        container.addView(layoutAmount);
        container.addView(layoutDate);
        container.addView(layoutMethod);
        container.addView(layoutRef);
        container.addView(layoutNotes);

        new MaterialAlertDialogBuilder(this)
                .setTitle(R.string.record_payment)
                .setView(container)
                .setPositiveButton(R.string.record_payment, (dialog, which) -> {
                    String amountText = etAmount.getText() != null ? etAmount.getText().toString().trim() : "";
                    double amount = 0;
                    try {
                        amount = Double.parseDouble(amountText);
                    } catch (NumberFormatException ignored) {
                    }

                    if (amount <= 0) {
                        Toast.makeText(this, R.string.invalid_payment_amount, Toast.LENGTH_SHORT).show();
                        return;
                    }

                    if (amount > currentDisplayItem.remainingAmount + 0.01) {
                        Toast.makeText(this, R.string.payment_exceeds_balance, Toast.LENGTH_SHORT).show();
                        return;
                    }

                    String date = etDate.getText() != null ? etDate.getText().toString().trim() : paymentDateFormat.format(calendar.getTime());
                    String selectedDisplay = autoMethod.getText() != null ? autoMethod.getText().toString().trim() : displayMethods[0];
                    String method = com.rob.houserental.utils.PaymentMethodUtils.CODE_CASH;
                    for (int i = 0; i < displayMethods.length; i++) {
                        if (displayMethods[i].equalsIgnoreCase(selectedDisplay)) {
                            method = methodCodes[i];
                            break;
                        }
                    }
                    String ref = etRef.getText() != null ? etRef.getText().toString().trim() : "";
                    String notes = etNotes.getText() != null ? etNotes.getText().toString().trim() : "";

                    Payment payment = new Payment(
                            rentRecordId,
                            amount,
                            date,
                            method,
                            ref,
                            notes,
                            System.currentTimeMillis()
                    );

                    double finalAmount = amount;
                    repository.recordPayment(rentRecordId, payment, new RentRepository.DatabaseCallback<Void>() {
                        @Override
                        public void onSuccess(Void result) {
                            runOnUiThread(() -> {
                                Toast.makeText(RentDetailsActivity.this, getString(R.string.payment_recorded_success, getString(R.string.currency_symbol) + currencyFormatter.format(finalAmount)), Toast.LENGTH_SHORT).show();
                                loadRentDetails();
                                loadPayments();
                            });
                        }

                        @Override
                        public void onError(Exception exception) {
                            runOnUiThread(() -> {
                                if (exception != null && exception.getMessage() != null && !exception.getMessage().isEmpty()) {
                                    Toast.makeText(RentDetailsActivity.this, exception.getMessage(), Toast.LENGTH_LONG).show();
                                } else {
                                    Toast.makeText(RentDetailsActivity.this, R.string.save_failed, Toast.LENGTH_SHORT).show();
                                }
                            });
                        }
                    });
                })
                .setNegativeButton(R.string.cancel, null)
                .show();
    }

    private void showEditRentDialog() {
        if (currentRecord == null) {
            return;
        }

        TextInputLayout layoutDue = new TextInputLayout(this, null, com.google.android.material.R.attr.textInputOutlinedStyle);
        layoutDue.setHint(getString(R.string.amount_due));
        layoutDue.setPrefixText("৳ ");
        TextInputEditText etDue = new TextInputEditText(layoutDue.getContext());
        etDue.setInputType(android.text.InputType.TYPE_CLASS_NUMBER | android.text.InputType.TYPE_NUMBER_FLAG_DECIMAL);
        etDue.setText(String.valueOf(currentRecord.getAmountDue()));
        layoutDue.addView(etDue);

        TextInputLayout layoutNotes = new TextInputLayout(this, null, com.google.android.material.R.attr.textInputOutlinedStyle);
        layoutNotes.setHint(getString(R.string.notes));
        TextInputEditText etNotes = new TextInputEditText(layoutNotes.getContext());
        etNotes.setText(currentRecord.getNotes());
        layoutNotes.addView(etNotes);

        android.widget.LinearLayout container = new android.widget.LinearLayout(this);
        container.setOrientation(android.widget.LinearLayout.VERTICAL);
        container.setPadding(48, 24, 48, 24);
        container.addView(layoutDue);
        container.addView(layoutNotes);

        new MaterialAlertDialogBuilder(this)
                .setTitle(R.string.edit_rent)
                .setView(container)
                .setPositiveButton(R.string.edit, (dialog, which) -> {
                    String dueText = etDue.getText() != null ? etDue.getText().toString().trim() : "";
                    double newDue = 0;
                    try {
                        newDue = Double.parseDouble(dueText);
                    } catch (NumberFormatException ignored) {
                    }

                    if (newDue < currentRecord.getAmountPaid()) {
                        String formattedPaid = getString(R.string.currency_symbol) + currencyFormatter.format(currentRecord.getAmountPaid());
                        Toast.makeText(this, getString(R.string.amount_due_min_error, formattedPaid), Toast.LENGTH_LONG).show();
                        return;
                    }

                    currentRecord.setAmountDue(newDue);
                    currentRecord.setNotes(etNotes.getText() != null ? etNotes.getText().toString().trim() : "");

                    repository.updateRentRecord(currentRecord, new RentRepository.DatabaseCallback<Void>() {
                        @Override
                        public void onSuccess(Void result) {
                            runOnUiThread(() -> {
                                Toast.makeText(RentDetailsActivity.this, R.string.tenancy_updated_success, Toast.LENGTH_SHORT).show();
                                loadRentDetails();
                            });
                        }

                        @Override
                        public void onError(Exception exception) {
                            runOnUiThread(() -> Toast.makeText(RentDetailsActivity.this, R.string.update_failed, Toast.LENGTH_SHORT).show());
                        }
                    });
                })
                .setNegativeButton(R.string.cancel, null)
                .show();
    }

    private void showWaiveRentDialog() {
        if (currentDisplayItem == null) {
            return;
        }

        String remainingFormatted = getString(R.string.currency_symbol) + currencyFormatter.format(currentDisplayItem.remainingAmount);

        TextInputLayout layoutNotes = new TextInputLayout(this, null, com.google.android.material.R.attr.textInputOutlinedStyle);
        layoutNotes.setHint(getString(R.string.waiver_notes_hint));
        TextInputEditText etNotes = new TextInputEditText(layoutNotes.getContext());
        layoutNotes.addView(etNotes);

        android.widget.LinearLayout container = new android.widget.LinearLayout(this);
        container.setOrientation(android.widget.LinearLayout.VERTICAL);
        container.setPadding(48, 16, 48, 16);
        container.addView(layoutNotes);

        new MaterialAlertDialogBuilder(this)
                .setTitle(R.string.waive_rent)
                .setMessage(getString(R.string.waive_rent_confirm, remainingFormatted))
                .setView(container)
                .setPositiveButton(R.string.waive_rent, (dialog, which) -> {
                    String waiverNote = etNotes.getText() != null ? etNotes.getText().toString().trim() : "";
                    repository.waiveRent(rentRecordId, waiverNote, new RentRepository.DatabaseCallback<Void>() {
                        @Override
                        public void onSuccess(Void result) {
                            runOnUiThread(() -> {
                                Toast.makeText(RentDetailsActivity.this, R.string.rent_waived_success, Toast.LENGTH_SHORT).show();
                                loadRentDetails();
                            });
                        }

                        @Override
                        public void onError(Exception exception) {
                            runOnUiThread(() -> Toast.makeText(RentDetailsActivity.this, R.string.update_failed, Toast.LENGTH_SHORT).show());
                        }
                    });
                })
                .setNegativeButton(R.string.cancel, null)
                .show();
    }

    private void launchReceiptExport() {
        if (currentDisplayItem == null) return;
        Intent intent = new Intent(Intent.ACTION_CREATE_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("application/pdf");
        intent.putExtra(Intent.EXTRA_TITLE, "RentReceipt_#" + currentDisplayItem.id + "_" + currentDisplayItem.billingMonth + ".pdf");
        receiptLauncher.launch(intent);
    }

    private void writeReceiptToUri(android.net.Uri uri) {
        new Thread(() -> {
            try (java.io.OutputStream out = getContentResolver().openOutputStream(uri)) {
                com.rob.houserental.utils.ReceiptGeneratorUtils.generateRentReceiptPdf(this, currentDisplayItem, out);
                runOnUiThread(() -> Toast.makeText(this, R.string.export_success, Toast.LENGTH_SHORT).show());
            } catch (Exception e) {
                runOnUiThread(() -> Toast.makeText(this, R.string.export_failed, Toast.LENGTH_SHORT).show());
            }
        }).start();
    }

    private void showSendReminderDialog() {
        if (currentDisplayItem == null) return;
        String message = com.rob.houserental.utils.ReceiptGeneratorUtils.buildPaymentReminderMessage(this, currentDisplayItem);
        String[] options = new String[]{
                getString(R.string.action_send_whatsapp),
                getString(R.string.action_send_sms)
        };

        new MaterialAlertDialogBuilder(this)
                .setTitle(R.string.action_send_reminder)
                .setItems(options, (dialog, which) -> {
                    if (which == 0) {
                        com.rob.houserental.utils.ReceiptGeneratorUtils.sendWhatsAppReminder(this, currentDisplayItem.tenantPhone, message);
                    } else {
                        com.rob.houserental.utils.ReceiptGeneratorUtils.sendSmsReminder(this, currentDisplayItem.tenantPhone, message);
                    }
                })
                .show();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (repository != null) {
            loadRentDetails();
            loadPayments();
        }
    }
}

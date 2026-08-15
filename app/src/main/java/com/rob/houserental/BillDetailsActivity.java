package com.rob.houserental;

import android.app.DatePickerDialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.MaterialAutoCompleteTextView;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.rob.houserental.adapter.BillPaymentAdapter;
import com.rob.houserental.adapter.PaymentAdapter;
import com.rob.houserental.adapter.UtilityBillAdapter;
import com.rob.houserental.model.BillPayment;
import com.rob.houserental.model.UtilityBill;
import com.rob.houserental.model.UtilityBillDisplayItem;
import com.rob.houserental.repository.UtilityBillRepository;
import com.rob.houserental.utils.BkashSmsParserUtils;

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class BillDetailsActivity extends AppCompatActivity {

    private TextView tvBillDetailsTypeIcon;
    private TextView tvBillDetailsTitle;
    private TextView tvBillDetailsPropertyUnit;
    private TextView tvBillDetailsTenant;
    private TextView tvBillDetailsMonth;
    private TextView tvBillDetailsStatusBadge;

    private TextView tvBillDetailsDue;
    private TextView tvBillDetailsPaid;
    private TextView tvBillDetailsRemaining;
    private TextView tvBillDetailsDueDate;
    private TextView tvBillDetailsBillNumber;
    private TextView tvBillDetailsLastPayment;
    private TextView tvBillDetailsNotes;

    private MaterialCardView cardUtilityBreakdown;
    private TextView tvBreakdownMeter;
    private TextView tvBreakdownReadings;
    private TextView tvBreakdownRate;
    private TextView tvBreakdownFixedCharge;
    private TextView tvBreakdownVat;

    private TextView tvBillPaymentCount;
    private TextView tvNoBillPayments;
    private RecyclerView recyclerBillPayments;

    private MaterialButton btnPayViaBkash;
    private MaterialButton btnNotifyTenant;
    private MaterialButton btnPasteBkashSms;
    private MaterialButton btnRecordBillPayment;
    private MaterialButton btnEditBill;
    private MaterialButton btnWaiveBill;
    private MaterialButton btnDeleteBill;

    private UtilityBillRepository repository;
    private BillPaymentAdapter paymentAdapter;

    private long billId = -1;
    private UtilityBillDisplayItem currentDisplayItem;
    private UtilityBill currentBill;

    private static final NumberFormat currencyFormatter = NumberFormat.getNumberInstance(Locale.getDefault());
    private static final SimpleDateFormat paymentDateFormat = new SimpleDateFormat("dd MMM yyyy", Locale.getDefault());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_bill_details);

        billId = getIntent().getLongExtra("bill_id", -1);
        if (billId == -1) {
            finish();
            return;
        }

        initializeViews();

        setupToolbar();

        repository = new UtilityBillRepository(getApplicationContext());

        setupRecyclerView();

        setupActions();

        loadBillDetails();
    }

    private void initializeViews() {
        tvBillDetailsTitle = findViewById(R.id.tvBillDetailsTitle);
        tvBillDetailsPropertyUnit = findViewById(R.id.tvBillDetailsPropertyUnit);
        tvBillDetailsTenant = findViewById(R.id.tvBillDetailsTenant);
        tvBillDetailsMonth = findViewById(R.id.tvBillDetailsMonth);
        tvBillDetailsStatusBadge = findViewById(R.id.tvBillDetailsStatusBadge);

        tvBillDetailsDue = findViewById(R.id.tvBillDetailsDue);
        tvBillDetailsPaid = findViewById(R.id.tvBillDetailsPaid);
        tvBillDetailsRemaining = findViewById(R.id.tvBillDetailsRemaining);
        tvBillDetailsDueDate = findViewById(R.id.tvBillDetailsDueDate);
        tvBillDetailsBillNumber = findViewById(R.id.tvBillDetailsBillNumber);
        tvBillDetailsLastPayment = findViewById(R.id.tvBillDetailsLastPayment);
        tvBillDetailsNotes = findViewById(R.id.tvBillDetailsNotes);

        cardUtilityBreakdown = findViewById(R.id.cardUtilityBreakdown);
        tvBreakdownMeter = findViewById(R.id.tvBreakdownMeter);
        tvBreakdownReadings = findViewById(R.id.tvBreakdownReadings);
        tvBreakdownRate = findViewById(R.id.tvBreakdownRate);
        tvBreakdownFixedCharge = findViewById(R.id.tvBreakdownFixedCharge);
        tvBreakdownVat = findViewById(R.id.tvBreakdownVat);

        tvBillPaymentCount = findViewById(R.id.tvBillPaymentCount);
        tvNoBillPayments = findViewById(R.id.tvNoBillPayments);
        recyclerBillPayments = findViewById(R.id.recyclerBillPayments);

        btnPayViaBkash = findViewById(R.id.btnPayViaBkash);
        btnNotifyTenant = findViewById(R.id.btnNotifyTenant);
        btnPasteBkashSms = findViewById(R.id.btnPasteBkashSms);
        btnRecordBillPayment = findViewById(R.id.btnRecordBillPayment);
        btnEditBill = findViewById(R.id.btnEditBill);
        btnWaiveBill = findViewById(R.id.btnWaiveBill);
        btnDeleteBill = findViewById(R.id.btnDeleteBill);
    }

    private void setupToolbar() {
        MaterialToolbar toolbar = findViewById(R.id.toolbarBillDetails);
        toolbar.setNavigationOnClickListener(v -> finish());
    }

    private void setupRecyclerView() {
        paymentAdapter = new BillPaymentAdapter();
        recyclerBillPayments.setLayoutManager(new LinearLayoutManager(this));
        recyclerBillPayments.setAdapter(paymentAdapter);
    }

    private void setupActions() {
        btnPayViaBkash.setOnClickListener(v -> handlePayViaBkash());

        btnNotifyTenant.setOnClickListener(v -> handleNotifyTenant());

        btnPasteBkashSms.setOnClickListener(v -> showPasteBkashSmsDialog());

        btnRecordBillPayment.setOnClickListener(v -> showRecordPaymentDialog());

        btnEditBill.setOnClickListener(v -> {
            Intent intent = new Intent(BillDetailsActivity.this, AddBillActivity.class);
            intent.putExtra("bill_id", billId);
            startActivity(intent);
        });

        btnWaiveBill.setOnClickListener(v -> showWaiveBillDialog());

        btnDeleteBill.setOnClickListener(v -> showDeleteBillDialog());
    }

    private void loadBillDetails() {
        repository.getBillDisplayItemById(billId, new UtilityBillRepository.DatabaseCallback<UtilityBillDisplayItem>() {
            @Override
            public void onSuccess(UtilityBillDisplayItem item) {
                currentDisplayItem = item;
                runOnUiThread(() -> {
                    if (item != null) {
                        displayBillDetails(item);
                        loadPaymentHistory();
                    } else {
                        finish();
                    }
                });
            }

            @Override
            public void onError(Exception exception) {
                runOnUiThread(() -> finish());
            }
        });

        repository.getBillById(billId, new UtilityBillRepository.DatabaseCallback<UtilityBill>() {
            @Override
            public void onSuccess(UtilityBill bill) {
                currentBill = bill;
            }

            @Override
            public void onError(Exception exception) {
            }
        });
    }

    private void displayBillDetails(UtilityBillDisplayItem item) {
        String type = item.billType != null ? item.billType : "OTHER";
        tvBillDetailsTypeIcon.setText(getTypeIcon(type));
        tvBillDetailsTitle.setText(getTypeTitle(this, type));

        String propName = item.propertyName != null ? item.propertyName : getString(R.string.property_label);
        if (item.unitNumber != null && !item.unitNumber.isEmpty()) {
            tvBillDetailsPropertyUnit.setText(propName + " • " + getString(R.string.prefix_unit_format, item.unitNumber));
        } else {
            tvBillDetailsPropertyUnit.setText(propName + " (" + getString(R.string.whole_property_option) + ")");
        }

        if (item.tenantName != null && !item.tenantName.isEmpty()) {
            tvBillDetailsTenant.setText(getString(R.string.prefix_current_tenant, item.tenantName) +
                    (item.tenantPhone != null ? " (" + item.tenantPhone + ")" : ""));
            tvBillDetailsTenant.setVisibility(View.VISIBLE);
        } else {
            tvBillDetailsTenant.setVisibility(View.GONE);
        }

        tvBillDetailsMonth.setText((item.billingMonth != null ? item.billingMonth : ""));

        String status = item.status != null ? item.status.trim().toUpperCase() : "UNPAID";
        tvBillDetailsStatusBadge.setText(UtilityBillAdapter.getStatusDisplay(this, status));
        applyStatusBadgeStyle(this, tvBillDetailsStatusBadge, status);

        // Financial Balance
        String curr = getString(R.string.currency_symbol);
        tvBillDetailsDue.setText(curr + currencyFormatter.format(item.amountDue));
        tvBillDetailsPaid.setText(curr + currencyFormatter.format(item.amountPaid));
        tvBillDetailsRemaining.setText(curr + currencyFormatter.format(item.remainingAmount));
        tvBillDetailsDueDate.setText(getString(R.string.prefix_due_date, (item.dueDate != null ? item.dueDate : getString(R.string.not_set))));

        if (item.billNumber != null && !item.billNumber.isEmpty()) {
            tvBillDetailsBillNumber.setText(getString(R.string.prefix_bill_number, item.billNumber));
            tvBillDetailsBillNumber.setVisibility(View.VISIBLE);
        } else {
            tvBillDetailsBillNumber.setVisibility(View.GONE);
        }

        if (item.lastPaymentDate != null && !item.lastPaymentDate.isEmpty()) {
            tvBillDetailsLastPayment.setText(getString(R.string.prefix_last_payment, item.lastPaymentDate) +
                    (item.paymentMethod != null ? " (" + PaymentAdapter.getPaymentMethodDisplay(this, item.paymentMethod) + ")" : ""));
            tvBillDetailsLastPayment.setVisibility(View.VISIBLE);
        } else {
            tvBillDetailsLastPayment.setVisibility(View.GONE);
        }

        if (item.notes != null && !item.notes.isEmpty()) {
            tvBillDetailsNotes.setText(getString(R.string.prefix_notes, item.notes));
            tvBillDetailsNotes.setVisibility(View.VISIBLE);
        } else {
            tvBillDetailsNotes.setVisibility(View.GONE);
        }

        // Breakdown Card
        if (item.unitsConsumed > 0 || item.currentReading > 0 || (item.meterNumber != null && !item.meterNumber.isEmpty())) {
            cardUtilityBreakdown.setVisibility(View.VISIBLE);
            tvBreakdownMeter.setText(getString(R.string.prefix_meter_number, (item.meterNumber != null ? item.meterNumber : getString(R.string.none_value))));
            tvBreakdownReadings.setText(getString(R.string.prefix_readings, item.previousReading, item.currentReading, item.unitsConsumed));
            tvBreakdownRate.setText(getString(R.string.prefix_rate_per_unit, curr + currencyFormatter.format(item.ratePerUnit)));
            tvBreakdownFixedCharge.setText(getString(R.string.prefix_fixed_charge, curr + currencyFormatter.format(item.fixedCharge)));
            tvBreakdownVat.setText(getString(R.string.prefix_vat_tax, curr + currencyFormatter.format(item.vatOrTax)));
        } else {
            cardUtilityBreakdown.setVisibility(View.GONE);
        }

        // Action button states
        if (item.remainingAmount <= 0.001 || "PAID".equalsIgnoreCase(status) || "WAIVED".equalsIgnoreCase(status)) {
            btnRecordBillPayment.setEnabled(false);
            btnWaiveBill.setEnabled(false);
        } else {
            btnRecordBillPayment.setEnabled(true);
            btnWaiveBill.setEnabled(true);
        }
    }

    private void loadPaymentHistory() {
        repository.getPaymentsByBill(billId, new UtilityBillRepository.DatabaseCallback<List<BillPayment>>() {
            @Override
            public void onSuccess(List<BillPayment> payments) {
                runOnUiThread(() -> {
                    int count = payments != null ? payments.size() : 0;
                    tvBillPaymentCount.setText(count == 1 ? getString(R.string.count_records_singular, count) : getString(R.string.count_records_plural, count));
                    if (count == 0) {
                        tvNoBillPayments.setVisibility(View.VISIBLE);
                        recyclerBillPayments.setVisibility(View.GONE);
                        paymentAdapter.setPayments(null);
                    } else {
                        tvNoBillPayments.setVisibility(View.GONE);
                        recyclerBillPayments.setVisibility(View.VISIBLE);
                        paymentAdapter.setPayments(payments);
                    }
                });
            }

            @Override
            public void onError(Exception exception) {
            }
        });
    }

    private void showRecordPaymentDialog() {
        if (currentDisplayItem == null || currentDisplayItem.remainingAmount <= 0.001) {
            return;
        }

        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_record_bill_payment, null);

        TextView tvDialogRemaining = dialogView.findViewById(R.id.tvDialogBillRemaining);
        TextInputLayout layoutPaymentAmount = dialogView.findViewById(R.id.layoutBillPaymentAmount);
        TextInputEditText etPaymentAmount = dialogView.findViewById(R.id.etBillPaymentAmount);
        TextInputEditText etPaymentDate = dialogView.findViewById(R.id.etBillPaymentDate);
        MaterialAutoCompleteTextView autoPaymentMethod = dialogView.findViewById(R.id.autoBillPaymentMethod);
        TextInputEditText etPaymentRef = dialogView.findViewById(R.id.etBillPaymentRef);
        TextInputEditText etPaymentNotes = dialogView.findViewById(R.id.etBillPaymentNotes);

        String curr = getString(R.string.currency_symbol);
        tvDialogRemaining.setText(curr + currencyFormatter.format(currentDisplayItem.remainingAmount));

        // Default to full remaining balance
        etPaymentAmount.setText(String.format(Locale.US, "%.2f", currentDisplayItem.remainingAmount));

        // Date Picker setup
        Calendar paymentCalendar = Calendar.getInstance();
        etPaymentDate.setText(paymentDateFormat.format(paymentCalendar.getTime()));

        etPaymentDate.setOnClickListener(v -> {
            new DatePickerDialog(
                    BillDetailsActivity.this,
                    (view, year, month, dayOfMonth) -> {
                        paymentCalendar.set(Calendar.YEAR, year);
                        paymentCalendar.set(Calendar.MONTH, month);
                        paymentCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                        etPaymentDate.setText(paymentDateFormat.format(paymentCalendar.getTime()));
                    },
                    paymentCalendar.get(Calendar.YEAR),
                    paymentCalendar.get(Calendar.MONTH),
                    paymentCalendar.get(Calendar.DAY_OF_MONTH)
            ).show();
        });

        // Payment Method Dropdown
        String[] displayMethods = com.rob.houserental.utils.PaymentMethodUtils.getDisplayMethods(this);
        String[] methodCodes = com.rob.houserental.utils.PaymentMethodUtils.getMethodCodes();
        ArrayAdapter<String> methodAdapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, displayMethods);
        autoPaymentMethod.setAdapter(methodAdapter);
        autoPaymentMethod.setText(displayMethods[0], false);

        AlertDialog dialog = new MaterialAlertDialogBuilder(this)
                .setTitle(R.string.record_payment)
                .setView(dialogView)
                .setPositiveButton(R.string.record_payment, null)
                .setNegativeButton(R.string.cancel, null)
                .create();

        dialog.setOnShowListener(d -> {
            MaterialButton btnConfirm = (MaterialButton) dialog.getButton(AlertDialog.BUTTON_POSITIVE);
            btnConfirm.setOnClickListener(v -> {
                String amountStr = etPaymentAmount.getText() != null ? etPaymentAmount.getText().toString().trim() : "";
                double amount = 0;

                if (TextUtils.isEmpty(amountStr)) {
                    layoutPaymentAmount.setError(getString(R.string.payment_amount_required));
                    return;
                }

                try {
                    amount = Double.parseDouble(amountStr);
                } catch (NumberFormatException e) {
                    layoutPaymentAmount.setError(getString(R.string.invalid_payment_amount));
                    return;
                }

                if (amount <= 0) {
                    layoutPaymentAmount.setError(getString(R.string.invalid_payment_amount));
                    return;
                }

                // Overpayment check
                if (amount > currentDisplayItem.remainingAmount + 0.01) {
                    layoutPaymentAmount.setError(getString(R.string.payment_exceeds_balance));
                    Toast.makeText(BillDetailsActivity.this, R.string.payment_exceeds_balance, Toast.LENGTH_LONG).show();
                    return;
                }

                layoutPaymentAmount.setError(null);

                String date = etPaymentDate.getText() != null ? etPaymentDate.getText().toString().trim() : paymentDateFormat.format(Calendar.getInstance().getTime());
                String selectedDisplay = autoPaymentMethod.getText() != null ? autoPaymentMethod.getText().toString().trim() : displayMethods[0];
                String method = com.rob.houserental.utils.PaymentMethodUtils.CODE_CASH;
                for (int i = 0; i < displayMethods.length; i++) {
                    if (displayMethods[i].equalsIgnoreCase(selectedDisplay)) {
                        method = methodCodes[i];
                        break;
                    }
                }
                String ref = etPaymentRef.getText() != null ? etPaymentRef.getText().toString().trim() : "";
                String notes = etPaymentNotes.getText() != null ? etPaymentNotes.getText().toString().trim() : "";

                BillPayment payment = new BillPayment(
                        billId,
                        amount,
                        date,
                        method,
                        ref,
                        notes,
                        System.currentTimeMillis()
                );

                btnConfirm.setEnabled(false);

                final double finalAmount = amount;
                repository.recordBillPayment(billId, payment, new UtilityBillRepository.DatabaseCallback<Void>() {
                    @Override
                    public void onSuccess(Void result) {
                        runOnUiThread(() -> {
                            dialog.dismiss();
                            Toast.makeText(BillDetailsActivity.this,
                                    getString(R.string.payment_recorded_success, curr + currencyFormatter.format(finalAmount)),
                                    Toast.LENGTH_SHORT).show();
                            loadBillDetails();
                        });
                    }

                    @Override
                    public void onError(Exception exception) {
                        runOnUiThread(() -> {
                            btnConfirm.setEnabled(true);
                            if (exception != null && exception.getMessage() != null && !exception.getMessage().isEmpty()) {
                                Toast.makeText(BillDetailsActivity.this, exception.getMessage(), Toast.LENGTH_LONG).show();
                            } else {
                                Toast.makeText(BillDetailsActivity.this, R.string.save_failed, Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                });
            });
        });

        dialog.show();
    }

    private void showWaiveBillDialog() {
        if (currentDisplayItem == null || currentDisplayItem.remainingAmount <= 0.001) {
            return;
        }

        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_waive_bill, null);
        TextView tvWaiveBillMessage = dialogView.findViewById(R.id.tvWaiveBillMessage);
        TextInputEditText etWaiverBillNotes = dialogView.findViewById(R.id.etWaiverBillNotes);

        String curr = getString(R.string.currency_symbol);
        tvWaiveBillMessage.setText(getString(R.string.waive_bill_confirm, curr + currencyFormatter.format(currentDisplayItem.remainingAmount)));

        new MaterialAlertDialogBuilder(this)
                .setTitle(R.string.waive_bill)
                .setView(dialogView)
                .setPositiveButton(R.string.waive_bill, (dialog, which) -> {
                    String notes = etWaiverBillNotes.getText() != null ? etWaiverBillNotes.getText().toString().trim() : "";
                    repository.waiveBill(billId, notes, new UtilityBillRepository.DatabaseCallback<Void>() {
                        @Override
                        public void onSuccess(Void result) {
                            runOnUiThread(() -> {
                                Toast.makeText(BillDetailsActivity.this, R.string.bill_waived_success, Toast.LENGTH_SHORT).show();
                                loadBillDetails();
                            });
                        }

                        @Override
                        public void onError(Exception exception) {
                            runOnUiThread(() -> Toast.makeText(BillDetailsActivity.this, R.string.save_failed, Toast.LENGTH_SHORT).show());
                        }
                    });
                })
                .setNegativeButton(R.string.cancel, null)
                .show();
    }

    private void showDeleteBillDialog() {
        if (currentBill == null && currentDisplayItem != null) {
            currentBill = new UtilityBill();
            currentBill.setId(currentDisplayItem.id);
            currentBill.setPropertyId(currentDisplayItem.propertyId);
        }

        if (currentBill == null) {
            return;
        }

        String type = currentDisplayItem != null ? getTypeTitle(this, currentDisplayItem.billType) : "bill";
        String prop = currentDisplayItem != null ? currentDisplayItem.propertyName : "";

        new MaterialAlertDialogBuilder(this)
                .setTitle(R.string.delete_bill)
                .setMessage(getString(R.string.delete_bill_confirm, type, prop))
                .setPositiveButton(R.string.delete, (dialog, which) -> {
                    repository.deleteBill(currentBill, new UtilityBillRepository.DatabaseCallback<Void>() {
                        @Override
                        public void onSuccess(Void result) {
                            runOnUiThread(() -> {
                                Toast.makeText(BillDetailsActivity.this, R.string.bill_deleted_success, Toast.LENGTH_SHORT).show();
                                finish();
                            });
                        }

                        @Override
                        public void onError(Exception exception) {
                            runOnUiThread(() -> Toast.makeText(BillDetailsActivity.this, R.string.update_failed, Toast.LENGTH_SHORT).show());
                        }
                    });
                })
                .setNegativeButton(R.string.cancel, null)
                .show();
    }

    private String getTypeIcon(String type) {
        if (type == null) return "";
        switch (type.toUpperCase()) {
            case "ELECTRICITY":
                return "";
            case "WATER":
                return "";
            case "GAS":
                return "";
            case "INTERNET":
                return "";
            case "SERVICE_CHARGE":
                return "";
            case "OTHER":
            default:
                return "";
        }
    }

    private String getTypeTitle(Context context, String type) {
        if (type == null) return context.getString(R.string.bill_type_other);
        switch (type.toUpperCase()) {
            case "ELECTRICITY":
                return context.getString(R.string.bill_type_electricity);
            case "WATER":
                return context.getString(R.string.bill_type_water);
            case "GAS":
                return context.getString(R.string.bill_type_gas);
            case "INTERNET":
                return context.getString(R.string.bill_type_internet);
            case "SERVICE_CHARGE":
                return context.getString(R.string.bill_type_service);
            case "OTHER":
            default:
                return context.getString(R.string.bill_type_other);
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

    private void handlePayViaBkash() {
        if (currentBill == null && currentDisplayItem == null) return;
        String meter = currentBill != null ? currentBill.getMeterNumber() : null;
        if (meter == null || meter.trim().isEmpty()) {
            if (currentDisplayItem != null && currentDisplayItem.meterNumber != null && !currentDisplayItem.meterNumber.trim().isEmpty()) {
                meter = currentDisplayItem.meterNumber.trim();
            } else if (currentBill != null && currentBill.getBillNumber() != null && !currentBill.getBillNumber().trim().isEmpty()) {
                meter = currentBill.getBillNumber().trim();
            }
        }

        if (meter == null || meter.trim().isEmpty()) {
            Toast.makeText(this, R.string.no_meter_found_toast, Toast.LENGTH_SHORT).show();
            return;
        }

        // 1. Copy meter number to clipboard
        ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData clip = ClipData.newPlainText("Meter Account Number", meter.trim());
        if (clipboard != null) {
            clipboard.setPrimaryClip(clip);
        }

        Toast.makeText(this, getString(R.string.bkash_copied_toast, meter.trim()), Toast.LENGTH_LONG).show();

        // 2. Try launching bKash app
        Intent bKashIntent = getPackageManager().getLaunchIntentForPackage("com.bKash.customerapp");
        if (bKashIntent != null) {
            startActivity(bKashIntent);
        } else {
            // Fallback: Open Play Store for bKash
            try {
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=com.bKash.customerapp")));
            } catch (Exception e) {
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=com.bKash.customerapp")));
            }
        }
    }

    private void handleNotifyTenant() {
        if (currentDisplayItem == null) return;
        String tenantPhone = currentDisplayItem.tenantPhone;
        String tenantName = currentDisplayItem.tenantName != null ? currentDisplayItem.tenantName : "ভাড়াটিয়া";
        String month = currentDisplayItem.billingMonth != null ? currentDisplayItem.billingMonth : "";
        String typeTitle = getTypeTitle(this, currentDisplayItem.billType);
        double amount = currentDisplayItem.remainingAmount > 0 ? currentDisplayItem.remainingAmount : currentDisplayItem.amountDue;
        String meter = currentDisplayItem.meterNumber != null ? currentDisplayItem.meterNumber : "";
        String dueDate = currentDisplayItem.dueDate != null ? currentDisplayItem.dueDate : "";

        String message = "প্রিয় " + tenantName + ",\n" +
                month + " মাসের " + typeTitle + " বিল: ৳" + currencyFormatter.format(amount) + "।\n" +
                (!meter.isEmpty() ? "মিটার/হিসাব নম্বর: " + meter + "।\n" : "") +
                (!dueDate.isEmpty() ? "পরিশোধের শেষ তারিখ: " + dueDate + "।\n" : "") +
                "দয়া করে উক্ত বিল পরিশোধ করার ব্যবস্থা করুন।\nধন্যবাদ, রব হাউস রেন্টাল।";

        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("text/plain");
        intent.putExtra(Intent.EXTRA_TEXT, message);
        if (tenantPhone != null && !tenantPhone.trim().isEmpty()) {
            intent.putExtra("address", tenantPhone);
        }
        startActivity(Intent.createChooser(intent, getString(R.string.btn_notify_tenant)));
    }

    private void showPasteBkashSmsDialog() {
        if (currentBill == null) return;

        final EditText etSmsInput = new EditText(this);
        etSmsInput.setHint(R.string.bkash_sms_dialog_hint);
        etSmsInput.setMinLines(3);
        etSmsInput.setMaxLines(6);
        etSmsInput.setPadding(32, 32, 32, 32);

        new MaterialAlertDialogBuilder(this)
                .setTitle(R.string.bkash_sms_dialog_title)
                .setView(etSmsInput)
                .setPositiveButton(R.string.confirm, (dialog, which) -> {
                    String smsText = etSmsInput.getText().toString();
                    BkashSmsParserUtils.BkashSmsResult result = BkashSmsParserUtils.parseBkashSms(smsText);
                    if (result.isValid) {
                        double payAmt = result.amount > 0 ? result.amount : currentBill.getRemainingAmount();
                        String trxId = result.transactionId != null ? result.transactionId : "bKash SMS";

                        BillPayment payment = new BillPayment(
                                billId,
                                payAmt,
                                paymentDateFormat.format(Calendar.getInstance().getTime()),
                                "bKash",
                                trxId,
                                "Auto-verified via bKash SMS",
                                System.currentTimeMillis()
                        );

                        repository.recordBillPayment(billId, payment, new UtilityBillRepository.DatabaseCallback<Void>() {
                            @Override
                            public void onSuccess(Void res) {
                                runOnUiThread(() -> {
                                    Toast.makeText(BillDetailsActivity.this, getString(R.string.bkash_sms_parsed_success, trxId, payAmt), Toast.LENGTH_LONG).show();
                                    loadBillDetails();
                                });
                            }

                            @Override
                            public void onError(Exception exception) {
                                runOnUiThread(() -> Toast.makeText(BillDetailsActivity.this, R.string.payment_failed, Toast.LENGTH_SHORT).show());
                            }
                        });
                    } else {
                        Toast.makeText(BillDetailsActivity.this, R.string.bkash_sms_parsed_error, Toast.LENGTH_LONG).show();
                    }
                })
                .setNegativeButton(R.string.cancel, null)
                .show();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadBillDetails();
    }
}

package com.rob.houserental;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputEditText;
import com.rob.houserental.commercial.PaymentDestinationConfig;
import com.rob.houserental.commercial.PlanConfig;
import com.rob.houserental.commercial.SubscriptionManager;
import com.rob.houserental.commercial.SubscriptionPlan;
import com.rob.houserental.model.PaymentOrder;
import com.rob.houserental.model.SubscriptionEntitlement;
import com.rob.houserental.repository.DatabaseCallback;
import com.rob.houserental.repository.SubscriptionRepository;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class SubscriptionActivity extends AppCompatActivity {

    private static final String TAG = "SubscriptionActivity";

    private MaterialToolbar toolbar;
    private TextView tvStatusBadge;
    private TextView tvPlanName;
    private TextView tvExpiryDate;
    private TextView tvDaysRemaining;
    private TextView tvExpiredNotice;

    private RadioGroup rgPlans;
    private RadioButton rbPlanMonthly;
    private RadioButton rbPlanSixMonths;
    private RadioButton rbPlanYearly;

    private RadioGroup rgPaymentMethods;
    private RadioButton rbBkash;
    private RadioButton rbNagad;
    private RadioButton rbRocket;

    private MaterialButton btnCreateOrder;
    private MaterialCardView cardOrderDetails;
    private TextView tvOrderId;
    private TextView tvSendMoneyNumber;
    private TextView tvAmountToSend;
    private TextInputEditText etTransactionId;
    private MaterialButton btnSubmitPayment;
    private MaterialButton btnDevAdminVerify;
    private MaterialButton btnMyOrders;

    private SubscriptionRepository subscriptionRepository;
    private PaymentOrder currentActiveOrder;
    private static final DecimalFormat currencyFormat = new DecimalFormat("#,##,###");
    private static final SimpleDateFormat dateFormat = new SimpleDateFormat("dd MMMM yyyy", Locale.getDefault());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_subscription);

        subscriptionRepository = new SubscriptionRepository(this);

        initializeViews();
        setupToolbar();
        setupListeners();
    }

    private void initializeViews() {
        toolbar = findViewById(R.id.toolbarSubscription);
        tvStatusBadge = findViewById(R.id.tvStatusBadge);
        tvPlanName = findViewById(R.id.tvPlanName);
        tvExpiryDate = findViewById(R.id.tvExpiryDate);
        tvDaysRemaining = findViewById(R.id.tvDaysRemaining);
        tvExpiredNotice = findViewById(R.id.tvExpiredNotice);

        rgPlans = findViewById(R.id.rgPlans);
        rbPlanMonthly = findViewById(R.id.rbPlanMonthly);
        rbPlanSixMonths = findViewById(R.id.rbPlanSixMonths);
        rbPlanYearly = findViewById(R.id.rbPlanYearly);

        rgPaymentMethods = findViewById(R.id.rgPaymentMethods);
        rbBkash = findViewById(R.id.rbBkash);
        rbNagad = findViewById(R.id.rbNagad);
        rbRocket = findViewById(R.id.rbRocket);

        btnCreateOrder = findViewById(R.id.btnCreateOrder);
        cardOrderDetails = findViewById(R.id.cardOrderDetails);
        tvOrderId = findViewById(R.id.tvOrderId);
        tvSendMoneyNumber = findViewById(R.id.tvSendMoneyNumber);
        tvAmountToSend = findViewById(R.id.tvAmountToSend);
        etTransactionId = findViewById(R.id.etTransactionId);
        btnSubmitPayment = findViewById(R.id.btnSubmitPayment);
        btnDevAdminVerify = findViewById(R.id.btnDevAdminVerify);
        if (btnDevAdminVerify != null) {
            btnDevAdminVerify.setVisibility(BuildConfig.DEBUG ? View.VISIBLE : View.GONE);
        }
        btnMyOrders = findViewById(R.id.btnMyOrders);
    }

    private void setupToolbar() {
        if (toolbar != null) {
            toolbar.setNavigationOnClickListener(v -> finish());
        }
    }

    private void setupListeners() {
        if (btnCreateOrder != null) {
            btnCreateOrder.setOnClickListener(v -> createOrder());
        }
        if (btnSubmitPayment != null) {
            btnSubmitPayment.setOnClickListener(v -> submitTransactionId());
        }
        if (btnDevAdminVerify != null) {
            btnDevAdminVerify.setOnClickListener(v -> showDevAdminDialog());
        }
        if (btnMyOrders != null) {
            btnMyOrders.setOnClickListener(v -> {
                Intent intent = new Intent(SubscriptionActivity.this, MyOrdersActivity.class);
                startActivity(intent);
            });
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadEntitlementStatus();
    }

    private void loadEntitlementStatus() {
        SubscriptionManager.getInstance(this).refreshEntitlementAsync((entitlement, isPremium) -> runOnUiThread(() -> {
            if (entitlement == null || "FREE".equalsIgnoreCase(entitlement.getStatus())) {
                if (tvStatusBadge != null) tvStatusBadge.setText(R.string.status_sub_free);
                if (tvPlanName != null) tvPlanName.setText(R.string.status_sub_free);
                if (tvExpiryDate != null) tvExpiryDate.setVisibility(View.GONE);
                if (tvDaysRemaining != null) tvDaysRemaining.setVisibility(View.GONE);
                if (tvExpiredNotice != null) tvExpiredNotice.setVisibility(View.GONE);
            } else if ("ACTIVE".equalsIgnoreCase(entitlement.getStatus()) || "GRACE_PERIOD".equalsIgnoreCase(entitlement.getStatus())) {
                SubscriptionPlan plan = PlanConfig.getPlan(entitlement.getPlanCode());
                if (tvStatusBadge != null) tvStatusBadge.setText(R.string.status_sub_active);
                if (tvPlanName != null) tvPlanName.setText(plan.getDisplayName());

                long now = System.currentTimeMillis();
                long diffDays = Math.max(0, (entitlement.getExpiresAt() - now) / (24 * 60 * 60 * 1000L));

                if (tvExpiryDate != null) {
                    tvExpiryDate.setText(getString(R.string.expires_on, dateFormat.format(new Date(entitlement.getExpiresAt()))));
                    tvExpiryDate.setVisibility(View.VISIBLE);
                }
                if (tvDaysRemaining != null) {
                    tvDaysRemaining.setText(getString(R.string.days_remaining, (int) diffDays));
                    tvDaysRemaining.setVisibility(View.VISIBLE);
                }
                if (tvExpiredNotice != null) tvExpiredNotice.setVisibility(View.GONE);
            } else { // EXPIRED
                if (tvStatusBadge != null) tvStatusBadge.setText(R.string.status_sub_expired);
                if (tvPlanName != null) tvPlanName.setText(R.string.status_sub_expired);
                if (tvExpiryDate != null) tvExpiryDate.setVisibility(View.GONE);
                if (tvDaysRemaining != null) tvDaysRemaining.setVisibility(View.GONE);
                if (tvExpiredNotice != null) tvExpiredNotice.setVisibility(View.VISIBLE);
            }
        }));
    }

    private String getSelectedPlanCode() {
        if (rbPlanMonthly != null && rbPlanMonthly.isChecked()) return PlanConfig.PLAN_MONTHLY;
        if (rbPlanSixMonths != null && rbPlanSixMonths.isChecked()) return PlanConfig.PLAN_SIX_MONTHS;
        return PlanConfig.PLAN_YEARLY;
    }

    private String getSelectedPaymentMethod() {
        if (rbNagad != null && rbNagad.isChecked()) return PaymentDestinationConfig.METHOD_NAGAD;
        if (rbRocket != null && rbRocket.isChecked()) return PaymentDestinationConfig.METHOD_ROCKET;
        return PaymentDestinationConfig.METHOD_BKASH;
    }

    private void createOrder() {
        String planCode = getSelectedPlanCode();
        String method = getSelectedPaymentMethod();

        subscriptionRepository.createOrder(planCode, method, new DatabaseCallback<PaymentOrder>() {
            @Override
            public void onSuccess(PaymentOrder order) {
                currentActiveOrder = order;
                runOnUiThread(() -> displayOrderDetails(order));
            }

            @Override
            public void onError(Exception exception) {
                Log.e(TAG, "Error creating order", exception);
                runOnUiThread(() -> Toast.makeText(SubscriptionActivity.this, exception.getMessage(), Toast.LENGTH_SHORT).show());
            }
        });
    }

    private void displayOrderDetails(PaymentOrder order) {
        if (cardOrderDetails != null) {
            cardOrderDetails.setVisibility(View.VISIBLE);
        }
        if (tvOrderId != null) {
            tvOrderId.setText(getString(R.string.order_id_label) + order.getOrderId());
        }
        if (tvSendMoneyNumber != null) {
            String destNum = PaymentDestinationConfig.getDestinationNumber(order.getPaymentMethod());
            tvSendMoneyNumber.setText(getString(R.string.send_money_to) + destNum + " (" + order.getPaymentMethod() + ")");
        }
        if (tvAmountToSend != null) {
            tvAmountToSend.setText(getString(R.string.amount_to_send) + " ৳" + currencyFormat.format(order.getAmountMinor() / 100.0));
        }
    }

    private void submitTransactionId() {
        if (currentActiveOrder == null) {
            Toast.makeText(this, "Please create a subscription order first.", Toast.LENGTH_SHORT).show();
            return;
        }

        String trxId = etTransactionId != null && etTransactionId.getText() != null ? etTransactionId.getText().toString().trim() : "";
        if (trxId.isEmpty()) {
            Toast.makeText(this, R.string.enter_trx_id_prompt, Toast.LENGTH_SHORT).show();
            return;
        }

        subscriptionRepository.submitTransaction(currentActiveOrder.getOrderId(), trxId, new DatabaseCallback<PaymentOrder>() {
            @Override
            public void onSuccess(PaymentOrder updatedOrder) {
                currentActiveOrder = updatedOrder;
                runOnUiThread(() -> {
                    Toast.makeText(SubscriptionActivity.this, R.string.payment_submitted_toast, Toast.LENGTH_LONG).show();
                    loadEntitlementStatus();
                });
            }

            @Override
            public void onError(Exception exception) {
                Log.e(TAG, "Error submitting transaction", exception);
                runOnUiThread(() -> Toast.makeText(SubscriptionActivity.this, exception.getMessage(), Toast.LENGTH_LONG).show());
            }
        });
    }

    private void showDevAdminDialog() {
        if (currentActiveOrder == null) {
            Toast.makeText(this, "Create an order and submit a transaction first to test verification.", Toast.LENGTH_SHORT).show();
            return;
        }

        new MaterialAlertDialogBuilder(this)
                .setTitle(R.string.dev_admin_dialog_title)
                .setMessage("Simulate Admin action for Order " + currentActiveOrder.getOrderId() + "\n(TrxID: " + (currentActiveOrder.getTransactionId() != null ? currentActiveOrder.getTransactionId() : "N/A") + ")")
                .setPositiveButton(R.string.dev_admin_approve, (dialog, which) -> {
                    subscriptionRepository.simulateAdminApproveOrder(currentActiveOrder.getOrderId(), new DatabaseCallback<SubscriptionEntitlement>() {
                        @Override
                        public void onSuccess(SubscriptionEntitlement entitlement) {
                            runOnUiThread(() -> {
                                Toast.makeText(SubscriptionActivity.this, R.string.dev_approved_success, Toast.LENGTH_SHORT).show();
                                loadEntitlementStatus();
                            });
                        }

                        @Override
                        public void onError(Exception exception) {
                            runOnUiThread(() -> Toast.makeText(SubscriptionActivity.this, exception.getMessage(), Toast.LENGTH_SHORT).show());
                        }
                    });
                })
                .setNegativeButton(R.string.dev_admin_reject, (dialog, which) -> {
                    subscriptionRepository.simulateAdminRejectOrder(currentActiveOrder.getOrderId(), "Invalid TrxID", new DatabaseCallback<PaymentOrder>() {
                        @Override
                        public void onSuccess(PaymentOrder order) {
                            runOnUiThread(() -> {
                                Toast.makeText(SubscriptionActivity.this, R.string.dev_rejected_success, Toast.LENGTH_SHORT).show();
                                loadEntitlementStatus();
                            });
                        }

                        @Override
                        public void onError(Exception exception) {
                            runOnUiThread(() -> Toast.makeText(SubscriptionActivity.this, exception.getMessage(), Toast.LENGTH_SHORT).show());
                        }
                    });
                })
                .setNeutralButton(R.string.cancel, null)
                .show();
    }
}

package com.rob.houserental.commercial;

import android.content.Context;
import android.util.Log;

import com.rob.houserental.data.AppDatabase;
import com.rob.houserental.model.PaymentOrder;
import com.rob.houserental.model.SubscriptionEntitlement;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

public class MockSubscriptionBackendService {

    private static final String TAG = "SubscriptionBackend";
    private static final SimpleDateFormat orderIdDateFormat = new SimpleDateFormat("yyyyMMdd", Locale.getDefault());

    public interface ApiCallback<T> {
        void onSuccess(T result);
        void onError(String errorMessage);
    }

    public static void createSubscriptionOrder(Context context, String userId, String planCode, String paymentMethod, ApiCallback<PaymentOrder> callback) {
        try {
            AppDatabase db = AppDatabase.getInstance(context);
            SubscriptionPlan plan = PlanConfig.getPlan(planCode);

            String datePrefix = orderIdDateFormat.format(new Date());
            String randomCode = UUID.randomUUID().toString().replaceAll("-", "").substring(0, 6).toUpperCase();
            String orderId = "RR-SUB-" + datePrefix + "-" + randomCode;

            long now = System.currentTimeMillis();
            long orderExpiresAt = now + (24 * 60 * 60 * 1000L); // 24 hours to pay

            PaymentOrder order = new PaymentOrder();
            order.setOrderId(orderId);
            order.setUserId(userId);
            order.setPlanCode(plan.getPlanCode());
            order.setAmountMinor(plan.getPriceMinor());
            order.setCurrency(plan.getCurrency());
            order.setPaymentMethod(paymentMethod != null ? paymentMethod.toUpperCase() : "BKASH");
            order.setStatus("PENDING_PAYMENT");
            order.setCreatedAt(now);
            order.setExpiresAt(orderExpiresAt);

            db.paymentOrderDao().insertOrUpdate(order);

            if (callback != null) {
                callback.onSuccess(order);
            }
        } catch (Exception e) {
            Log.e(TAG, "Error creating order", e);
            if (callback != null) {
                callback.onError("Failed to create subscription order: " + e.getMessage());
            }
        }
    }

    public static void submitPaymentTransaction(Context context, String orderId, String transactionId, ApiCallback<PaymentOrder> callback) {
        try {
            AppDatabase db = AppDatabase.getInstance(context);
            PaymentOrder order = db.paymentOrderDao().getOrderById(orderId);

            if (order == null) {
                if (callback != null) callback.onError("Subscription order not found.");
                return;
            }

            if (transactionId == null || transactionId.trim().isEmpty()) {
                if (callback != null) callback.onError("Transaction ID is required.");
                return;
            }

            String cleanTxId = transactionId.trim().toUpperCase();

            // Duplicate Transaction ID Check across DB
            PaymentOrder existingTxOrder = db.paymentOrderDao().getOrderByTransactionId(cleanTxId);
            if (existingTxOrder != null && !existingTxOrder.getOrderId().equals(orderId)) {
                if (callback != null) callback.onError("This Transaction ID has already been submitted or processed for another order.");
                return;
            }

            long now = System.currentTimeMillis();
            order.setTransactionId(cleanTxId);
            order.setStatus("PAYMENT_SUBMITTED");
            order.setSubmittedAt(now);

            db.paymentOrderDao().insertOrUpdate(order);

            // IMPORTANT SECURITY RULE: Entitlement is NOT updated here!
            // Premium stays inactive until admin verification occurs.

            if (callback != null) {
                callback.onSuccess(order);
            }
        } catch (Exception e) {
            Log.e(TAG, "Error submitting transaction", e);
            if (callback != null) {
                callback.onError("Failed to submit transaction: " + e.getMessage());
            }
        }
    }

    public static void verifyAndApproveOrder(Context context, String orderId, String adminId, ApiCallback<SubscriptionEntitlement> callback) {
        try {
            AppDatabase db = AppDatabase.getInstance(context);
            PaymentOrder order = db.paymentOrderDao().getOrderById(orderId);

            if (order == null) {
                if (callback != null) callback.onError("Order not found.");
                return;
            }

            // Idempotent Check: Avoid duplicate approval
            if ("APPROVED".equalsIgnoreCase(order.getStatus())) {
                SubscriptionEntitlement existingEnt = db.subscriptionEntitlementDao().getEntitlementByUserId(order.getUserId());
                if (callback != null) callback.onSuccess(existingEnt);
                return;
            }

            long now = System.currentTimeMillis();

            // Update order status
            order.setStatus("APPROVED");
            order.setVerifiedAt(now);
            order.setVerifiedBy(adminId != null ? adminId : "ADMIN_VERIFIER");

            SubscriptionPlan plan = PlanConfig.getPlan(order.getPlanCode());
            long durationMillis = plan.getDurationDays() * 24 * 60 * 60 * 1000L;

            SubscriptionEntitlement entitlement = db.subscriptionEntitlementDao().getEntitlementByUserId(order.getUserId());
            if (entitlement == null) {
                entitlement = new SubscriptionEntitlement();
                entitlement.setEntitlementId("ENT-" + order.getUserId());
                entitlement.setUserId(order.getUserId());
                entitlement.setCreatedAt(now);
            }

            // Renewal Expiry Calculation Rule
            long startFrom;
            if ("ACTIVE".equalsIgnoreCase(entitlement.getStatus()) && entitlement.getExpiresAt() > now) {
                startFrom = entitlement.getExpiresAt(); // Extend from existing expiry
            } else {
                startFrom = now; // Fresh start from current activation time
            }

            long newExpiresAt = startFrom + durationMillis;
            long graceUntil = newExpiresAt + (3 * 24 * 60 * 60 * 1000L); // 3 days grace period

            entitlement.setEntitlementType("PREMIUM");
            entitlement.setStatus("ACTIVE");
            entitlement.setPlanCode(plan.getPlanCode());
            entitlement.setStartedAt(now);
            entitlement.setExpiresAt(newExpiresAt);
            entitlement.setGraceUntil(graceUntil);
            entitlement.setSource("MANUAL_" + order.getPaymentMethod());
            entitlement.setOrderId(order.getOrderId());
            entitlement.setUpdatedAt(now);

            order.setEntitlementId(entitlement.getEntitlementId());

            db.paymentOrderDao().insertOrUpdate(order);
            db.subscriptionEntitlementDao().insertOrUpdate(entitlement);

            SubscriptionManager.getInstance(context).loadCachedEntitlementSync();

            if (callback != null) {
                callback.onSuccess(entitlement);
            }
        } catch (Exception e) {
            Log.e(TAG, "Error approving order", e);
            if (callback != null) {
                callback.onError("Failed to approve order: " + e.getMessage());
            }
        }
    }

    public static void rejectOrder(Context context, String orderId, String adminId, String reason, ApiCallback<PaymentOrder> callback) {
        try {
            AppDatabase db = AppDatabase.getInstance(context);
            PaymentOrder order = db.paymentOrderDao().getOrderById(orderId);

            if (order == null) {
                if (callback != null) callback.onError("Order not found.");
                return;
            }

            long now = System.currentTimeMillis();
            order.setStatus("REJECTED");
            order.setRejectedAt(now);
            order.setRejectionReason(reason != null ? reason : "Invalid transaction details");
            order.setVerifiedBy(adminId != null ? adminId : "ADMIN_VERIFIER");

            db.paymentOrderDao().insertOrUpdate(order);

            if (callback != null) {
                callback.onSuccess(order);
            }
        } catch (Exception e) {
            Log.e(TAG, "Error rejecting order", e);
            if (callback != null) {
                callback.onError("Failed to reject order: " + e.getMessage());
            }
        }
    }
}

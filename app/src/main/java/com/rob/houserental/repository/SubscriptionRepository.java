package com.rob.houserental.repository;

import android.content.Context;

import com.rob.houserental.BuildConfig;
import com.rob.houserental.auth.SessionManager;
import com.rob.houserental.commercial.MockSubscriptionBackendService;
import com.rob.houserental.commercial.SubscriptionManager;
import com.rob.houserental.data.AppDatabase;
import com.rob.houserental.model.PaymentOrder;
import com.rob.houserental.model.SubscriptionEntitlement;
import com.rob.houserental.model.UserAccount;
import com.rob.houserental.network.HttpBackendClient;
import com.rob.houserental.network.SubscriptionApiService;
import com.rob.houserental.utils.AppExecutors;

import java.util.List;
import java.util.concurrent.ExecutorService;

public class SubscriptionRepository {

    private final Context context;
    private final AppDatabase database;
    private final SubscriptionApiService httpBackendClient;
    private final ExecutorService executor = AppExecutors.getInstance().getDatabaseExecutor();

    public interface DatabaseCallback<T> extends com.rob.houserental.repository.DatabaseCallback<T> {}
    public interface Callback<T> extends com.rob.houserental.repository.DatabaseCallback<T> {}

    public SubscriptionRepository(Context context) {
        this.context = context.getApplicationContext();
        this.database = AppDatabase.getInstance(this.context);
        this.httpBackendClient = new HttpBackendClient(this.context);
    }

    public void createOrder(String planCode, String paymentMethod, com.rob.houserental.repository.DatabaseCallback<PaymentOrder> callback) {
        executor.execute(() -> {
            String userId = SessionManager.getInstance(context).getUserId();
            if (userId == null) {
                UserAccount user = SubscriptionManager.getInstance(context).getCachedUserAccount();
                userId = user != null ? user.getUserId() : null;
            }

            if (userId == null) {
                if (callback != null) callback.onError(new Exception("Authentication required. Please log in first."));
                return;
            }

            // In DEBUG mode with offline dev environment fallback, use MockBackend service if HTTP backend is unconfigured
            if (BuildConfig.DEBUG) {
                MockSubscriptionBackendService.createSubscriptionOrder(context, userId, planCode, paymentMethod, new MockSubscriptionBackendService.ApiCallback<PaymentOrder>() {
                    @Override
                    public void onSuccess(PaymentOrder order) {
                        if (callback != null) callback.onSuccess(order);
                    }

                    @Override
                    public void onError(String errorMessage) {
                        if (callback != null) callback.onError(new Exception(errorMessage));
                    }
                });
            } else {
                httpBackendClient.createOrder(planCode, paymentMethod, new SubscriptionApiService.ApiCallback<PaymentOrder>() {
                    @Override
                    public void onSuccess(PaymentOrder order) {
                        executor.execute(() -> database.paymentOrderDao().insertOrUpdate(order));
                        if (callback != null) callback.onSuccess(order);
                    }

                    @Override
                    public void onError(String errorMessage) {
                        if (callback != null) callback.onError(new Exception(errorMessage));
                    }
                });
            }
        });
    }

    public void submitTransaction(String orderId, String transactionId, com.rob.houserental.repository.DatabaseCallback<PaymentOrder> callback) {
        executor.execute(() -> {
            if (BuildConfig.DEBUG) {
                MockSubscriptionBackendService.submitPaymentTransaction(context, orderId, transactionId, new MockSubscriptionBackendService.ApiCallback<PaymentOrder>() {
                    @Override
                    public void onSuccess(PaymentOrder order) {
                        if (callback != null) callback.onSuccess(order);
                    }

                    @Override
                    public void onError(String errorMessage) {
                        if (callback != null) callback.onError(new Exception(errorMessage));
                    }
                });
            } else {
                httpBackendClient.submitTransaction(orderId, transactionId, new SubscriptionApiService.ApiCallback<PaymentOrder>() {
                    @Override
                    public void onSuccess(PaymentOrder order) {
                        executor.execute(() -> database.paymentOrderDao().insertOrUpdate(order));
                        if (callback != null) callback.onSuccess(order);
                    }

                    @Override
                    public void onError(String errorMessage) {
                        if (callback != null) callback.onError(new Exception(errorMessage));
                    }
                });
            }
        });
    }

    public void simulateAdminApproveOrder(String orderId, com.rob.houserental.repository.DatabaseCallback<SubscriptionEntitlement> callback) {
        if (!BuildConfig.DEBUG) {
            if (callback != null) callback.onError(new Exception("Admin approval is not allowed on client build."));
            return;
        }
        executor.execute(() -> {
            MockSubscriptionBackendService.verifyAndApproveOrder(context, orderId, "ADMIN_DEV_CONSOLE", new MockSubscriptionBackendService.ApiCallback<SubscriptionEntitlement>() {
                @Override
                public void onSuccess(SubscriptionEntitlement entitlement) {
                    if (callback != null) callback.onSuccess(entitlement);
                }

                @Override
                public void onError(String errorMessage) {
                    if (callback != null) callback.onError(new Exception(errorMessage));
                }
            });
        });
    }

    public void simulateAdminRejectOrder(String orderId, String reason, com.rob.houserental.repository.DatabaseCallback<PaymentOrder> callback) {
        if (!BuildConfig.DEBUG) {
            if (callback != null) callback.onError(new Exception("Admin rejection is not allowed on client build."));
            return;
        }
        executor.execute(() -> {
            MockSubscriptionBackendService.rejectOrder(context, orderId, "ADMIN_DEV_CONSOLE", reason, new MockSubscriptionBackendService.ApiCallback<PaymentOrder>() {
                @Override
                public void onSuccess(PaymentOrder order) {
                    if (callback != null) callback.onSuccess(order);
                }

                @Override
                public void onError(String errorMessage) {
                    if (callback != null) callback.onError(new Exception(errorMessage));
                }
            });
        });
    }

    public void getUserOrders(com.rob.houserental.repository.DatabaseCallback<List<PaymentOrder>> callback) {
        executor.execute(() -> {
            try {
                String userId = SessionManager.getInstance(context).getUserId();
                if (userId == null) {
                    UserAccount user = SubscriptionManager.getInstance(context).getCachedUserAccount();
                    userId = user != null ? user.getUserId() : null;
                }
                List<PaymentOrder> list = database.paymentOrderDao().getOrdersByUserId(userId);
                if (callback != null) callback.onSuccess(list);
            } catch (Exception e) {
                if (callback != null) callback.onError(e);
            }
        });
    }
}

package com.rob.houserental.network;

import com.rob.houserental.commercial.SubscriptionPlan;
import com.rob.houserental.model.PaymentOrder;
import com.rob.houserental.model.SubscriptionEntitlement;
import com.rob.houserental.model.UserAccount;

import java.util.List;

public interface SubscriptionApiService {

    interface ApiCallback<T> {
        void onSuccess(T result);
        void onError(String errorMessage);
    }

    void register(String emailOrPhone, String fullName, String password, ApiCallback<UserAccount> callback);
    void login(String emailOrPhone, String password, ApiCallback<UserAccount> callback);
    void logout(ApiCallback<Void> callback);
    void getSubscriptionPlans(ApiCallback<List<SubscriptionPlan>> callback);
    void createOrder(String planCode, String paymentMethod, ApiCallback<PaymentOrder> callback);
    void submitTransaction(String orderId, String transactionId, ApiCallback<PaymentOrder> callback);
    void getUserOrders(ApiCallback<List<PaymentOrder>> callback);
    void syncEntitlement(ApiCallback<SubscriptionEntitlement> callback);
}

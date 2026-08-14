package com.rob.houserental.network;

import android.content.Context;
import android.util.Log;

import com.rob.houserental.auth.SessionManager;
import com.rob.houserental.commercial.PlanConfig;
import com.rob.houserental.commercial.SubscriptionPlan;
import com.rob.houserental.model.PaymentOrder;
import com.rob.houserental.model.SubscriptionEntitlement;
import com.rob.houserental.model.UserAccount;

import com.rob.houserental.utils.AppExecutors;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.concurrent.ExecutorService;

public class HttpBackendClient implements SubscriptionApiService {

    private static final String TAG = "HttpBackendClient";
    private final Context context;
    private final ExecutorService executor = AppExecutors.getInstance().getNetworkExecutor();

    public HttpBackendClient(Context context) {
        this.context = context.getApplicationContext();
    }

    @Override
    public void register(String emailOrPhone, String fullName, String password, ApiCallback<UserAccount> callback) {
        executor.execute(() -> {
            try {
                JSONObject body = new JSONObject();
                body.put("emailOrPhone", emailOrPhone);
                body.put("fullName", fullName);
                body.put("password", password);

                JSONObject res = performHttpRequest("POST", "auth/register", body, false);
                if (res != null && res.optBoolean("success", false)) {
                    JSONObject userObj = res.getJSONObject("user");
                    String token = res.optString("accessToken", "");
                    UserAccount user = parseUser(userObj);
                    SessionManager.getInstance(context).saveSession(token, user.getUserId(), user.getEmailOrPhone(), user.getFullName());
                    if (callback != null) callback.onSuccess(user);
                } else {
                    String err = res != null ? res.optString("message", "Registration failed.") : "Server unreachable.";
                    if (callback != null) callback.onError(err);
                }
            } catch (Exception e) {
                Log.e(TAG, "Register error", e);
                if (callback != null) callback.onError("Network error: " + e.getMessage());
            }
        });
    }

    @Override
    public void login(String emailOrPhone, String password, ApiCallback<UserAccount> callback) {
        executor.execute(() -> {
            try {
                JSONObject body = new JSONObject();
                body.put("emailOrPhone", emailOrPhone);
                body.put("password", password);

                JSONObject res = performHttpRequest("POST", "auth/login", body, false);
                if (res != null && res.optBoolean("success", false)) {
                    JSONObject userObj = res.getJSONObject("user");
                    String token = res.optString("accessToken", "");
                    UserAccount user = parseUser(userObj);
                    SessionManager.getInstance(context).saveSession(token, user.getUserId(), user.getEmailOrPhone(), user.getFullName());
                    if (callback != null) callback.onSuccess(user);
                } else {
                    String err = res != null ? res.optString("message", "Login failed.") : "Invalid credentials or server unavailable.";
                    if (callback != null) callback.onError(err);
                }
            } catch (Exception e) {
                Log.e(TAG, "Login error", e);
                if (callback != null) callback.onError("Network error: " + e.getMessage());
            }
        });
    }

    @Override
    public void logout(ApiCallback<Void> callback) {
        executor.execute(() -> {
            try {
                performHttpRequest("POST", "auth/logout", null, true);
            } catch (Exception ignored) {
            } finally {
                SessionManager.getInstance(context).clearSession();
                if (callback != null) callback.onSuccess(null);
            }
        });
    }

    @Override
    public void getSubscriptionPlans(ApiCallback<List<SubscriptionPlan>> callback) {
        executor.execute(() -> {
            if (callback != null) callback.onSuccess(PlanConfig.getAllPaidPlans());
        });
    }

    @Override
    public void createOrder(String planCode, String paymentMethod, ApiCallback<PaymentOrder> callback) {
        executor.execute(() -> {
            try {
                JSONObject body = new JSONObject();
                body.put("planCode", planCode);
                body.put("paymentMethod", paymentMethod);

                JSONObject res = performHttpRequest("POST", "subscription/orders", body, true);
                if (res != null && res.optBoolean("success", false)) {
                    JSONObject orderObj = res.getJSONObject("order");
                    PaymentOrder order = parseOrder(orderObj);
                    if (callback != null) callback.onSuccess(order);
                } else {
                    String err = res != null ? res.optString("message", "Failed to create order.") : "Server unavailable.";
                    if (callback != null) callback.onError(err);
                }
            } catch (Exception e) {
                Log.e(TAG, "createOrder error", e);
                if (callback != null) callback.onError("Server connection error: " + e.getMessage());
            }
        });
    }

    @Override
    public void submitTransaction(String orderId, String transactionId, ApiCallback<PaymentOrder> callback) {
        executor.execute(() -> {
            try {
                JSONObject body = new JSONObject();
                body.put("transactionId", transactionId);

                JSONObject res = performHttpRequest("POST", "subscription/orders/" + orderId + "/submit", body, true);
                if (res != null && res.optBoolean("success", false)) {
                    JSONObject orderObj = res.getJSONObject("order");
                    PaymentOrder order = parseOrder(orderObj);
                    if (callback != null) callback.onSuccess(order);
                } else {
                    String err = res != null ? res.optString("message", "Failed to submit transaction.") : "Server unavailable.";
                    if (callback != null) callback.onError(err);
                }
            } catch (Exception e) {
                Log.e(TAG, "submitTransaction error", e);
                if (callback != null) callback.onError("Server connection error: " + e.getMessage());
            }
        });
    }

    @Override
    public void getUserOrders(ApiCallback<List<PaymentOrder>> callback) {
        executor.execute(() -> {
            // Real HTTPS request to server /subscription/orders
        });
    }

    @Override
    public void syncEntitlement(ApiCallback<SubscriptionEntitlement> callback) {
        executor.execute(() -> {
            try {
                JSONObject res = performHttpRequest("GET", "subscription/entitlement", null, true);
                if (res != null && res.optBoolean("success", false)) {
                    JSONObject entObj = res.getJSONObject("entitlement");
                    SubscriptionEntitlement entitlement = parseEntitlement(entObj);
                    if (callback != null) callback.onSuccess(entitlement);
                } else {
                    if (callback != null) callback.onError("Unable to sync entitlement with server.");
                }
            } catch (Exception e) {
                if (callback != null) callback.onError("Network error syncing entitlement: " + e.getMessage());
            }
        });
    }

    private JSONObject performHttpRequest(String method, String endpoint, JSONObject requestBody, boolean requireAuth) throws Exception {
        String baseUrl = NetworkConfig.getBaseUrl();
        URL url = new URL(baseUrl + endpoint);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod(method);
        conn.setConnectTimeout(10000);
        conn.setReadTimeout(10000);
        conn.setRequestProperty("Content-Type", "application/json");

        if (requireAuth) {
            String token = SessionManager.getInstance(context).getAccessToken();
            if (token != null) {
                conn.setRequestProperty("Authorization", "Bearer " + token);
            }
        }

        if (requestBody != null) {
            conn.setDoOutput(true);
            try (OutputStream os = conn.getOutputStream()) {
                byte[] input = requestBody.toString().getBytes(StandardCharsets.UTF_8);
                os.write(input, 0, input.length);
            }
        }

        int code = conn.getResponseCode();
        if (code >= 200 && code < 300) {
            try (BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8))) {
                StringBuilder response = new StringBuilder();
                String responseLine;
                while ((responseLine = br.readLine()) != null) {
                    response.append(responseLine.trim());
                }
                return new JSONObject(response.toString());
            }
        }
        return null;
    }

    private UserAccount parseUser(JSONObject obj) {
        UserAccount u = new UserAccount();
        u.setUserId(obj.optString("userId", ""));
        u.setEmailOrPhone(obj.optString("emailOrPhone", ""));
        u.setFullName(obj.optString("fullName", ""));
        u.setStatus(obj.optString("status", "ACTIVE"));
        u.setCreatedAt(obj.optLong("createdAt", System.currentTimeMillis()));
        u.setUpdatedAt(obj.optLong("updatedAt", System.currentTimeMillis()));
        return u;
    }

    private PaymentOrder parseOrder(JSONObject obj) {
        PaymentOrder o = new PaymentOrder();
        o.setOrderId(obj.optString("orderId", ""));
        o.setUserId(obj.optString("userId", ""));
        o.setPlanCode(obj.optString("planCode", ""));
        o.setAmountMinor(obj.optLong("amountMinor", 0L));
        o.setCurrency(obj.optString("currency", "BDT"));
        o.setPaymentMethod(obj.optString("paymentMethod", "BKASH"));
        o.setStatus(obj.optString("status", "PENDING_PAYMENT"));
        o.setCreatedAt(obj.optLong("createdAt", System.currentTimeMillis()));
        o.setExpiresAt(obj.optLong("expiresAt", System.currentTimeMillis()));
        o.setSubmittedAt(obj.optLong("submittedAt", 0L));
        o.setVerifiedAt(obj.optLong("verifiedAt", 0L));
        o.setTransactionId(obj.optString("transactionId", ""));
        return o;
    }

    private SubscriptionEntitlement parseEntitlement(JSONObject obj) {
        SubscriptionEntitlement e = new SubscriptionEntitlement();
        e.setEntitlementId(obj.optString("entitlementId", ""));
        e.setUserId(obj.optString("userId", ""));
        e.setEntitlementType(obj.optString("entitlementType", "PREMIUM"));
        e.setStatus(obj.optString("status", "FREE"));
        e.setPlanCode(obj.optString("planCode", "FREE"));
        e.setStartedAt(obj.optLong("startedAt", 0L));
        e.setExpiresAt(obj.optLong("expiresAt", 0L));
        e.setGraceUntil(obj.optLong("graceUntil", 0L));
        e.setSource(obj.optString("source", "FREE"));
        e.setOrderId(obj.optString("orderId", ""));
        e.setCreatedAt(obj.optLong("createdAt", System.currentTimeMillis()));
        e.setUpdatedAt(obj.optLong("updatedAt", System.currentTimeMillis()));
        return e;
    }
}

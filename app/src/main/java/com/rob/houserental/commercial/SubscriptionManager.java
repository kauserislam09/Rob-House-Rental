package com.rob.houserental.commercial;

import android.content.Context;
import android.util.Log;

import com.rob.houserental.data.AppDatabase;
import com.rob.houserental.model.SubscriptionEntitlement;
import com.rob.houserental.model.UserAccount;

import java.util.concurrent.Executors;

public class SubscriptionManager {

    private static final String TAG = "SubscriptionManager";

    public static final int FREE_LIMIT_PROPERTIES = 2;
    public static final int FREE_LIMIT_UNITS_PER_PROPERTY = 5;
    public static final int FREE_LIMIT_TENANTS = 10;

    private static volatile SubscriptionManager INSTANCE;
    private final Context context;
    private SubscriptionEntitlement cachedEntitlement;
    private UserAccount cachedUserAccount;

    public interface EntitlementCallback {
        void onEntitlementLoaded(SubscriptionEntitlement entitlement, boolean isPremium);
    }

    private SubscriptionManager(Context context) {
        this.context = context.getApplicationContext();
        loadCachedEntitlementSync();
    }

    public static SubscriptionManager getInstance(Context context) {
        if (INSTANCE == null) {
            synchronized (SubscriptionManager.class) {
                if (INSTANCE == null) {
                    INSTANCE = new SubscriptionManager(context);
                }
            }
        }
        return INSTANCE;
    }

    public synchronized void loadCachedEntitlementSync() {
        try {
            AppDatabase db = AppDatabase.getInstance(context);
            UserAccount primaryUser = db.userAccountDao().getPrimaryUser();
            if (primaryUser == null) {
                primaryUser = new UserAccount("USER-DEFAULT-001", "owner@robhouserental.com", "Default Property Owner", "ACTIVE", System.currentTimeMillis(), System.currentTimeMillis());
                db.userAccountDao().insertOrUpdate(primaryUser);
            }
            cachedUserAccount = primaryUser;

            SubscriptionEntitlement entitlement = db.subscriptionEntitlementDao().getEntitlementByUserId(primaryUser.getUserId());
            if (entitlement == null) {
                entitlement = new SubscriptionEntitlement();
                entitlement.setEntitlementId("ENT-FREE-" + primaryUser.getUserId());
                entitlement.setUserId(primaryUser.getUserId());
                entitlement.setEntitlementType("PREMIUM");
                entitlement.setStatus("FREE");
                entitlement.setPlanCode("FREE");
                entitlement.setStartedAt(System.currentTimeMillis());
                entitlement.setExpiresAt(0L);
                entitlement.setGraceUntil(0L);
                entitlement.setSource("FREE");
                entitlement.setCreatedAt(System.currentTimeMillis());
                entitlement.setUpdatedAt(System.currentTimeMillis());
                db.subscriptionEntitlementDao().insertOrUpdate(entitlement);
            }
            cachedEntitlement = entitlement;
        } catch (Exception e) {
            Log.e(TAG, "Error loading cached entitlement", e);
        }
    }

    public void refreshEntitlementAsync(EntitlementCallback callback) {
        Executors.newSingleThreadExecutor().execute(() -> {
            loadCachedEntitlementSync();
            boolean premium = isPremiumActive();
            if (callback != null) {
                callback.onEntitlementLoaded(cachedEntitlement, premium);
            }
        });
    }

    public boolean isPremiumActive() {
        if (cachedEntitlement == null) {
            loadCachedEntitlementSync();
        }
        if (cachedEntitlement == null) return false;

        String status = cachedEntitlement.getStatus();
        if ("ACTIVE".equalsIgnoreCase(status)) {
            long now = System.currentTimeMillis();
            return cachedEntitlement.getExpiresAt() <= 0 || now <= cachedEntitlement.getExpiresAt();
        } else if ("GRACE_PERIOD".equalsIgnoreCase(status)) {
            long now = System.currentTimeMillis();
            return now <= cachedEntitlement.getGraceUntil();
        }
        return false;
    }

    public SubscriptionEntitlement getCachedEntitlement() {
        if (cachedEntitlement == null) loadCachedEntitlementSync();
        return cachedEntitlement;
    }

    public UserAccount getCachedUserAccount() {
        if (cachedUserAccount == null) loadCachedEntitlementSync();
        return cachedUserAccount;
    }

    public boolean isFeatureAllowed(PremiumFeature feature) {
        if (isPremiumActive()) {
            return true;
        }

        // Free plan limits
        switch (feature) {
            case PROPERTY_LIMIT:
            case UNIT_LIMIT:
            case TENANT_LIMIT:
                return true; // Governed by specific count limit methods below
            case ADVANCED_REPORTS:
            case PDF_EXPORT:
            case CSV_EXPORT:
            case GOOGLE_DRIVE_BACKUP:
            case ADVANCED_REMINDERS:
                return false; // Restricted on Free plan
            case MAINTENANCE:
            case FINANCIAL_DASHBOARD:
            default:
                return true;
        }
    }

    public boolean canAddProperty(int currentCount) {
        return isPremiumActive() || currentCount < FREE_LIMIT_PROPERTIES;
    }

    public boolean canAddUnit(int currentUnitCountOnProperty) {
        return isPremiumActive() || currentUnitCountOnProperty < FREE_LIMIT_UNITS_PER_PROPERTY;
    }

    public boolean canAddTenant(int currentTotalTenants) {
        return isPremiumActive() || currentTotalTenants < FREE_LIMIT_TENANTS;
    }
}

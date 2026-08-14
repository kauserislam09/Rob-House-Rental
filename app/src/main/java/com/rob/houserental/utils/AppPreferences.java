package com.rob.houserental.utils;

import android.content.Context;
import android.content.SharedPreferences;

public class AppPreferences {

    private static final String PREF_NAME = "rob_house_rental_prefs";

    private static final String KEY_LANDLORD_NAME = "landlord_name";
    private static final String KEY_LANDLORD_PHONE = "landlord_phone";
    private static final String KEY_LANDLORD_EMAIL = "landlord_email";
    private static final String KEY_BUSINESS_NAME = "business_name";
    private static final String KEY_PAYMENT_ACCOUNTS = "payment_accounts";

    private final SharedPreferences prefs;

    public AppPreferences(Context context) {
        this.prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    public String getLandlordName() {
        return prefs.getString(KEY_LANDLORD_NAME, "");
    }

    public void setLandlordName(String name) {
        prefs.edit().putString(KEY_LANDLORD_NAME, name).apply();
    }

    public String getLandlordPhone() {
        return prefs.getString(KEY_LANDLORD_PHONE, "");
    }

    public void setLandlordPhone(String phone) {
        prefs.edit().putString(KEY_LANDLORD_PHONE, phone).apply();
    }

    public String getLandlordEmail() {
        return prefs.getString(KEY_LANDLORD_EMAIL, "");
    }

    public void setLandlordEmail(String email) {
        prefs.edit().putString(KEY_LANDLORD_EMAIL, email).apply();
    }

    public String getBusinessName() {
        return prefs.getString(KEY_BUSINESS_NAME, "");
    }

    public void setBusinessName(String name) {
        prefs.edit().putString(KEY_BUSINESS_NAME, name).apply();
    }

    public String getPaymentAccounts() {
        return prefs.getString(KEY_PAYMENT_ACCOUNTS, "");
    }

    public void setPaymentAccounts(String accounts) {
        prefs.edit().putString(KEY_PAYMENT_ACCOUNTS, accounts).apply();
    }
}

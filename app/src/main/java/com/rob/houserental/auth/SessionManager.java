package com.rob.houserental.auth;

import android.content.Context;
import android.content.SharedPreferences;

public class SessionManager {

    private static final String PREF_NAME = "rob_house_rental_auth_session";
    private static final String KEY_TOKEN = "auth_access_token";
    private static final String KEY_USER_ID = "auth_user_id";
    private static final String KEY_USER_EMAIL = "auth_user_email";
    private static final String KEY_USER_NAME = "auth_user_name";
    private static final String KEY_IS_LOGGED_IN = "auth_is_logged_in";

    private static volatile SessionManager INSTANCE;
    private final SharedPreferences prefs;

    private SessionManager(Context context) {
        this.prefs = context.getApplicationContext().getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    public static SessionManager getInstance(Context context) {
        if (INSTANCE == null) {
            synchronized (SessionManager.class) {
                if (INSTANCE == null) {
                    INSTANCE = new SessionManager(context);
                }
            }
        }
        return INSTANCE;
    }

    public void saveSession(String accessToken, String userId, String emailOrPhone, String fullName) {
        prefs.edit()
                .putString(KEY_TOKEN, accessToken)
                .putString(KEY_USER_ID, userId)
                .putString(KEY_USER_EMAIL, emailOrPhone)
                .putString(KEY_USER_NAME, fullName)
                .putBoolean(KEY_IS_LOGGED_IN, true)
                .apply();
    }

    public boolean isLoggedIn() {
        return prefs.getBoolean(KEY_IS_LOGGED_IN, false) && getAccessToken() != null;
    }

    public String getAccessToken() {
        return prefs.getString(KEY_TOKEN, null);
    }

    public String getUserId() {
        return prefs.getString(KEY_USER_ID, null);
    }

    public String getUserEmailOrPhone() {
        return prefs.getString(KEY_USER_EMAIL, null);
    }

    public String getUserFullName() {
        return prefs.getString(KEY_USER_NAME, null);
    }

    public void clearSession() {
        prefs.edit().clear().apply();
    }
}

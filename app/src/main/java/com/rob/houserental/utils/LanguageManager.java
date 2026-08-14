package com.rob.houserental.utils;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.os.LocaleListCompat;

public class LanguageManager {

    private static final String PREF_NAME = "rob_house_rental_language_pref";
    private static final String KEY_LANGUAGE = "selected_language";

    public static final String LANGUAGE_SYSTEM = "system";
    public static final String LANGUAGE_EN = "en";
    public static final String LANGUAGE_BN = "bn";

    private final SharedPreferences preferences;

    public LanguageManager(Context context) {
        preferences = context.getApplicationContext().getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    public String getSelectedLanguage() {
        return preferences.getString(KEY_LANGUAGE, LANGUAGE_SYSTEM);
    }

    public void setLanguage(String languageCode) {
        preferences.edit().putString(KEY_LANGUAGE, languageCode).apply();
        applyLanguage(languageCode);
    }

    public static void applyLanguage(String languageCode) {
        if (LANGUAGE_EN.equalsIgnoreCase(languageCode)) {
            AppCompatDelegate.setApplicationLocales(LocaleListCompat.forLanguageTags("en"));
        } else if (LANGUAGE_BN.equalsIgnoreCase(languageCode)) {
            AppCompatDelegate.setApplicationLocales(LocaleListCompat.forLanguageTags("bn"));
        } else {
            AppCompatDelegate.setApplicationLocales(LocaleListCompat.getEmptyLocaleList());
        }
    }

    public static void initAppLocale(Context context) {
        SharedPreferences prefs = context.getApplicationContext().getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        String savedLang = prefs.getString(KEY_LANGUAGE, LANGUAGE_SYSTEM);
        applyLanguage(savedLang);
    }

    public String getLanguageDisplayName(Context context, String languageCode) {
        if (LANGUAGE_EN.equalsIgnoreCase(languageCode)) {
            return "English";
        } else if (LANGUAGE_BN.equalsIgnoreCase(languageCode)) {
            return "বাংলা";
        } else {
            return "System Default / সিস্টেম ডিফল্ট";
        }
    }
}

package com.rob.houserental;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.rob.houserental.utils.AppPreferences;
import com.rob.houserental.utils.LanguageManager;

public class SettingsActivity extends AppCompatActivity {

    private TextView tvCurrentLanguage;
    private MaterialCardView cardLanguageSetting;
    private MaterialCardView cardBackupShortcut;
    private MaterialCardView cardAppUpdates;
    private TextView tvAboutEmail;
    private TextView tvAboutWebsite;

    private EditText etLandlordName;
    private EditText etLandlordPhone;
    private EditText etBusinessName;
    private EditText etPaymentAccounts;
    private MaterialButton btnSaveProfile;

    private LanguageManager languageManager;
    private AppPreferences appPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        languageManager = new LanguageManager(this);
        appPreferences = new AppPreferences(this);

        setupToolbar();
        initializeViews();
        loadProfileData();
        updateLanguageUI();
        setupListeners();
    }

    private void setupToolbar() {
        MaterialToolbar toolbar = findViewById(R.id.toolbarSettings);
        toolbar.setNavigationOnClickListener(v -> finish());
    }

    private void initializeViews() {
        tvCurrentLanguage = findViewById(R.id.tvCurrentLanguage);
        cardLanguageSetting = findViewById(R.id.cardLanguageSetting);
        cardBackupShortcut = findViewById(R.id.cardBackupShortcut);
        cardAppUpdates = findViewById(R.id.cardAppUpdates);
        tvAboutEmail = findViewById(R.id.tvAboutEmail);
        tvAboutWebsite = findViewById(R.id.tvAboutWebsite);

        etLandlordName = findViewById(R.id.etLandlordName);
        etLandlordPhone = findViewById(R.id.etLandlordPhone);
        etBusinessName = findViewById(R.id.etBusinessName);
        etPaymentAccounts = findViewById(R.id.etPaymentAccounts);
        btnSaveProfile = findViewById(R.id.btnSaveProfile);
    }

    private void loadProfileData() {
        etLandlordName.setText(appPreferences.getLandlordName());
        etLandlordPhone.setText(appPreferences.getLandlordPhone());
        etBusinessName.setText(appPreferences.getBusinessName());
        etPaymentAccounts.setText(appPreferences.getPaymentAccounts());
    }

    private void saveProfileData() {
        appPreferences.setLandlordName(etLandlordName.getText().toString().trim());
        appPreferences.setLandlordPhone(etLandlordPhone.getText().toString().trim());
        appPreferences.setBusinessName(etBusinessName.getText().toString().trim());
        appPreferences.setPaymentAccounts(etPaymentAccounts.getText().toString().trim());

        Toast.makeText(this, R.string.settings_saved_success, Toast.LENGTH_SHORT).show();
    }

    private void updateLanguageUI() {
        String currentLang = languageManager.getSelectedLanguage();
        String displayLabel;
        if (LanguageManager.LANGUAGE_EN.equalsIgnoreCase(currentLang)) {
            displayLabel = getString(R.string.language_english);
        } else if (LanguageManager.LANGUAGE_BN.equalsIgnoreCase(currentLang)) {
            displayLabel = getString(R.string.language_bangla);
        } else {
            displayLabel = getString(R.string.language_system);
        }
        tvCurrentLanguage.setText(displayLabel);
    }

    private void setupListeners() {
        cardLanguageSetting.setOnClickListener(v -> showLanguageSelectionDialog());

        btnSaveProfile.setOnClickListener(v -> saveProfileData());

        cardBackupShortcut.setOnClickListener(v -> {
            Intent intent = new Intent(SettingsActivity.this, BackupActivity.class);
            startActivity(intent);
        });

        if (cardAppUpdates != null) {
            cardAppUpdates.setOnClickListener(v -> {
                Intent intent = new Intent(Intent.ACTION_VIEW,
                        Uri.parse("https://robtech-website.kauserislam09.workers.dev/"));
                startActivity(intent);
            });
        }

        if (tvAboutEmail != null) {
            tvAboutEmail.setOnClickListener(v -> {
                Intent emailIntent = new Intent(Intent.ACTION_SENDTO,
                        Uri.parse("mailto:kauserislam109@gmail.com"));
                startActivity(Intent.createChooser(emailIntent, getString(R.string.send_email)));
            });
        }

        if (tvAboutWebsite != null) {
            tvAboutWebsite.setOnClickListener(v -> {
                Intent intent = new Intent(Intent.ACTION_VIEW,
                        Uri.parse("https://robtech-website.kauserislam09.workers.dev/"));
                startActivity(intent);
            });
        }
    }

    private void showLanguageSelectionDialog() {
        String[] languageCodes = {
                LanguageManager.LANGUAGE_SYSTEM,
                LanguageManager.LANGUAGE_EN,
                LanguageManager.LANGUAGE_BN
        };

        String[] languageLabels = {
                getString(R.string.language_system),
                getString(R.string.language_english),
                getString(R.string.language_bangla)
        };

        String currentCode = languageManager.getSelectedLanguage();
        int selectedIndex = 0;
        for (int i = 0; i < languageCodes.length; i++) {
            if (languageCodes[i].equalsIgnoreCase(currentCode)) {
                selectedIndex = i;
                break;
            }
        }

        new MaterialAlertDialogBuilder(this)
                .setTitle(R.string.select_language_title)
                .setSingleChoiceItems(languageLabels, selectedIndex, (dialog, which) -> {
                    String chosenCode = languageCodes[which];
                    languageManager.setLanguage(chosenCode);
                    dialog.dismiss();
                    Toast.makeText(SettingsActivity.this, R.string.language_changed_toast, Toast.LENGTH_SHORT).show();
                })
                .show();
    }
}

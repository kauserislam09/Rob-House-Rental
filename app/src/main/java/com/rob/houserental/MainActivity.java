package com.rob.houserental;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.navigation.NavigationView;
import com.rob.houserental.ui.dashboard.DashboardFragment;
import com.rob.houserental.utils.LanguageManager;

public class MainActivity extends AppCompatActivity {

    private DrawerLayout drawerLayout;
    private MaterialToolbar toolbar;
    private NavigationView navigationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        LanguageManager.initAppLocale(this);
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        initializeViews();

        setupNavigationDrawer();

        showDashboard();
    }

    private void initializeViews() {

        drawerLayout = findViewById(R.id.drawer_layout);

        toolbar = findViewById(R.id.toolbar);

        navigationView = findViewById(R.id.navigation_view);
    }

    private void setupNavigationDrawer() {

        toolbar.setNavigationOnClickListener(v ->
                drawerLayout.openDrawer(
                        GravityCompat.START
                )
        );

        navigationView.setNavigationItemSelectedListener(item -> {

            int id = item.getItemId();

            // Dashboard
            if (id == R.id.nav_dashboard) {

                showDashboard();

                drawerLayout.closeDrawer(
                        GravityCompat.START
                );

                return true;
            }

            // Properties
            if (id == R.id.nav_properties) {

                Intent intent = new Intent(
                        MainActivity.this,
                        PropertiesActivity.class
                );

                startActivity(intent);

                drawerLayout.closeDrawer(
                        GravityCompat.START
                );

                return true;
            }

            // Tenants
            if (id == R.id.nav_tenants) {

                Intent intent = new Intent(
                        MainActivity.this,
                        TenantsActivity.class
                );

                startActivity(intent);

                drawerLayout.closeDrawer(
                        GravityCompat.START
                );

                return true;
            }

            // Tenancies
            if (id == R.id.nav_tenancies) {

                Intent intent = new Intent(
                        MainActivity.this,
                        TenanciesActivity.class
                );

                startActivity(intent);

                drawerLayout.closeDrawer(
                        GravityCompat.START
                );

                return true;
            }

            // Rent & Payments
            if (id == R.id.nav_rent || id == R.id.nav_payments) {

                Intent intent = new Intent(
                        MainActivity.this,
                        RentActivity.class
                );

                startActivity(intent);

                drawerLayout.closeDrawer(
                        GravityCompat.START
                );

                return true;
            }

            // Utility Bills
            if (id == R.id.nav_bills) {

                Intent intent = new Intent(
                        MainActivity.this,
                        BillsActivity.class
                );

                startActivity(intent);

                drawerLayout.closeDrawer(
                        GravityCompat.START
                );

                return true;
            }

            // Expenses
            if (id == R.id.nav_expenses) {
                Intent intent = new Intent(MainActivity.this, ExpensesActivity.class);
                startActivity(intent);
                drawerLayout.closeDrawer(GravityCompat.START);
                return true;
            }

            // Maintenance
            if (id == R.id.nav_maintenance) {
                Intent intent = new Intent(MainActivity.this, MaintenanceActivity.class);
                startActivity(intent);
                drawerLayout.closeDrawer(GravityCompat.START);
                return true;
            }

            // Documents & Receipts
            if (id == R.id.nav_documents) {
                Intent intent = new Intent(MainActivity.this, DocumentsActivity.class);
                startActivity(intent);
                drawerLayout.closeDrawer(GravityCompat.START);
                return true;
            }

            // Reminders
            if (id == R.id.nav_reminders) {
                Intent intent = new Intent(MainActivity.this, RemindersActivity.class);
                startActivity(intent);
                drawerLayout.closeDrawer(GravityCompat.START);
                return true;
            }

            // Backup & Restore
            if (id == R.id.nav_backup) {

                Intent intent = new Intent(
                        MainActivity.this,
                        BackupActivity.class
                );

                startActivity(intent);

                drawerLayout.closeDrawer(
                        GravityCompat.START
                );

                return true;
            }

            // Subscription & Premium (Paused - Coming Soon)
            if (id == R.id.nav_subscription) {
                new com.google.android.material.dialog.MaterialAlertDialogBuilder(MainActivity.this)
                        .setTitle(R.string.menu_subscription)
                        .setMessage(R.string.coming_soon_message)
                        .setPositiveButton(R.string.close, null)
                        .show();
                drawerLayout.closeDrawer(GravityCompat.START);
                return true;
            }

            // Financial Dashboard & Reports
            if (id == R.id.nav_reports) {
                Intent intent = new Intent(
                        MainActivity.this,
                        ReportsActivity.class
                );
                startActivity(intent);
                drawerLayout.closeDrawer(GravityCompat.START);
                return true;
            }

            // Settings
            if (id == R.id.nav_settings) {

                Intent intent = new Intent(
                        MainActivity.this,
                        SettingsActivity.class
                );

                startActivity(intent);

                drawerLayout.closeDrawer(
                        GravityCompat.START
                );

                return true;
            }

            return false;
        });

        navigationView.setCheckedItem(
                R.id.nav_dashboard
        );
    }

    private void showDashboard() {

        getSupportFragmentManager()
                .beginTransaction()
                .replace(
                        R.id.main_content,
                        new DashboardFragment()
                )
                .commit();

        toolbar.setTitle(
                R.string.app_name
        );
    }
}
package com.rob.houserental;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.rob.houserental.adapter.TenancyAdapter;
import com.rob.houserental.adapter.TenantAdapter;
import com.rob.houserental.adapter.TenantDocumentAdapter;
import com.rob.houserental.data.AppDatabase;
import com.rob.houserental.model.Property;
import com.rob.houserental.model.TenancyWithDetails;
import com.rob.houserental.model.Tenant;
import com.rob.houserental.model.TenantDocument;
import com.rob.houserental.repository.TenancyRepository;
import com.rob.houserental.repository.TenantRepository;
import com.rob.houserental.utils.FileUtils;

import java.io.File;
import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class TenantDetailsActivity extends AppCompatActivity {

    private TextView tvTenantDetailsName;
    private TextView tvTenantDetailsStatusBadge;

    private TextView tvTenantDetailsPhone;
    private TextView tvTenantDetailsAltPhone;
    private TextView tvTenantDetailsEmail;
    private TextView tvTenantDetailsDob;
    private TextView tvTenantDetailsNid;
    private TextView tvTenantDetailsPassport;
    private TextView tvTenantDetailsOccupation;
    private TextView tvTenantDetailsEmergency;
    private TextView tvTenantDetailsPresentAddress;
    private TextView tvTenantDetailsPermanentAddress;
    private TextView tvTenantDetailsFamilyCount;
    private TextView tvTenantDetailsNotes;

    private TextView tvCurrentTenancy;
    private TextView tvPreviousTenancies;

    private TextView tvDocumentCount;
    private TextView tvNoDocuments;
    private RecyclerView recyclerDocuments;
    private MaterialButton btnAddDocument;

    private MaterialButton btnEditTenant;
    private MaterialButton btnChangeStatus;
    private MaterialButton btnDeleteTenant;

    private TenantRepository repository;
    private TenancyRepository tenancyRepository;
    private TenantDocumentAdapter documentAdapter;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    private long tenantId = -1;
    private Tenant currentTenant;
    private String selectedDocType = "Other Document";
    private static final NumberFormat currencyFormatter = NumberFormat.getNumberInstance(Locale.getDefault());

    private final ActivityResultLauncher<String[]> filePickerLauncher =
            registerForActivityResult(new ActivityResultContracts.OpenDocument(), uri -> {
                if (uri != null && tenantId != -1) {
                    saveImportedDocument(uri);
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_tenant_details);

        tenantId = getIntent().getLongExtra("tenant_id", -1);

        initializeViews();

        setupToolbar();

        setupRecyclerView();

        repository = new TenantRepository(getApplicationContext());
        tenancyRepository = new TenancyRepository(getApplicationContext());

        loadTenant();

        loadTenancyHistory();

        loadDocuments();

        setupListeners();
    }

    private void initializeViews() {
        tvTenantDetailsName = findViewById(R.id.tvTenantDetailsName);
        tvTenantDetailsStatusBadge = findViewById(R.id.tvTenantDetailsStatusBadge);

        tvTenantDetailsPhone = findViewById(R.id.tvTenantDetailsPhone);
        tvTenantDetailsAltPhone = findViewById(R.id.tvTenantDetailsAltPhone);
        tvTenantDetailsEmail = findViewById(R.id.tvTenantDetailsEmail);
        tvTenantDetailsDob = findViewById(R.id.tvTenantDetailsDob);
        tvTenantDetailsNid = findViewById(R.id.tvTenantDetailsNid);
        tvTenantDetailsPassport = findViewById(R.id.tvTenantDetailsPassport);
        tvTenantDetailsOccupation = findViewById(R.id.tvTenantDetailsOccupation);
        tvTenantDetailsEmergency = findViewById(R.id.tvTenantDetailsEmergency);
        tvTenantDetailsPresentAddress = findViewById(R.id.tvTenantDetailsPresentAddress);
        tvTenantDetailsPermanentAddress = findViewById(R.id.tvTenantDetailsPermanentAddress);
        tvTenantDetailsFamilyCount = findViewById(R.id.tvTenantDetailsFamilyCount);
        tvTenantDetailsNotes = findViewById(R.id.tvTenantDetailsNotes);

        tvCurrentTenancy = findViewById(R.id.tvCurrentTenancy);
        tvPreviousTenancies = findViewById(R.id.tvPreviousTenancies);

        tvDocumentCount = findViewById(R.id.tvDocumentCount);
        tvNoDocuments = findViewById(R.id.tvNoDocuments);
        recyclerDocuments = findViewById(R.id.recyclerDocuments);
        btnAddDocument = findViewById(R.id.btnAddDocument);

        btnEditTenant = findViewById(R.id.btnEditTenant);
        btnChangeStatus = findViewById(R.id.btnChangeStatus);
        btnDeleteTenant = findViewById(R.id.btnDeleteTenant);
    }

    private void setupToolbar() {
        MaterialToolbar toolbar = findViewById(R.id.toolbarTenantDetails);
        toolbar.setNavigationOnClickListener(v -> finish());
    }

    private void setupRecyclerView() {
        documentAdapter = new TenantDocumentAdapter();
        recyclerDocuments.setLayoutManager(new LinearLayoutManager(this));
        recyclerDocuments.setAdapter(documentAdapter);

        documentAdapter.setOnDocumentClickListener(new TenantDocumentAdapter.OnDocumentClickListener() {
            @Override
            public void onViewClick(TenantDocument document) {
                viewDocument(document);
            }

            @Override
            public void onDeleteClick(TenantDocument document) {
                showDeleteDocumentDialog(document);
            }
        });
    }

    private void setupListeners() {
        btnEditTenant.setOnClickListener(v -> {
            if (currentTenant != null) {
                Intent intent = new Intent(TenantDetailsActivity.this, AddTenantActivity.class);
                intent.putExtra("tenant_id", currentTenant.getId());
                startActivity(intent);
            }
        });

        btnChangeStatus.setOnClickListener(v -> {
            if (currentTenant != null) {
                showChangeStatusDialog();
            }
        });

        btnDeleteTenant.setOnClickListener(v -> {
            if (currentTenant != null) {
                showDeleteTenantDialog();
            }
        });

        tvTenantDetailsPhone.setOnClickListener(v -> {
            if (currentTenant != null && currentTenant.getPhoneNumber() != null && !currentTenant.getPhoneNumber().trim().isEmpty()) {
                Intent intent = new Intent(Intent.ACTION_DIAL);
                intent.setData(Uri.parse("tel:" + currentTenant.getPhoneNumber().trim()));
                startActivity(intent);
            }
        });

        btnAddDocument.setOnClickListener(v -> showDocumentTypePicker());
    }

    private void loadTenant() {
        if (tenantId == -1) {
            return;
        }

        repository.getTenantById(tenantId, new TenantRepository.DatabaseCallback<Tenant>() {
            @Override
            public void onSuccess(Tenant tenant) {
                if (tenant != null) {
                    currentTenant = tenant;
                    runOnUiThread(() -> displayTenantDetails(tenant));
                }
            }

            @Override
            public void onError(Exception exception) {
            }
        });
    }

    private void displayTenantDetails(Tenant tenant) {
        tvTenantDetailsName.setText(tenant.getFullName());

        String status = tenant.getStatus() != null ? tenant.getStatus().trim().toUpperCase() : "ACTIVE";
        tvTenantDetailsStatusBadge.setText(TenantAdapter.getStatusDisplay(this, status));
        applyStatusBadgeStyle(this, tvTenantDetailsStatusBadge, status);

        String noneVal = getString(R.string.none_value);
        String notSetVal = getString(R.string.not_set);

        tvTenantDetailsPhone.setText(tenant.getPhoneNumber() != null ? tenant.getPhoneNumber() : noneVal);
        tvTenantDetailsAltPhone.setText(tenant.getAlternativePhone() != null && !tenant.getAlternativePhone().isEmpty() ? tenant.getAlternativePhone() : noneVal);
        tvTenantDetailsEmail.setText(tenant.getEmail() != null && !tenant.getEmail().isEmpty() ? tenant.getEmail() : noneVal);
        tvTenantDetailsDob.setText(tenant.getDateOfBirth() != null && !tenant.getDateOfBirth().isEmpty() ? tenant.getDateOfBirth() : notSetVal);
        tvTenantDetailsNid.setText(tenant.getNidNumber() != null && !tenant.getNidNumber().isEmpty() ? tenant.getNidNumber() : notSetVal);
        tvTenantDetailsPassport.setText(tenant.getPassportNumber() != null && !tenant.getPassportNumber().isEmpty() ? tenant.getPassportNumber() : noneVal);
        tvTenantDetailsOccupation.setText(tenant.getOccupation() != null && !tenant.getOccupation().isEmpty() ? tenant.getOccupation() : noneVal);

        String emergency = "";
        if (tenant.getEmergencyContactName() != null && !tenant.getEmergencyContactName().isEmpty()) {
            emergency = tenant.getEmergencyContactName();
            if (tenant.getEmergencyContactPhone() != null && !tenant.getEmergencyContactPhone().isEmpty()) {
                emergency += " (" + tenant.getEmergencyContactPhone() + ")";
            }
        }
        tvTenantDetailsEmergency.setText(!emergency.isEmpty() ? emergency : noneVal);

        tvTenantDetailsPresentAddress.setText(tenant.getPresentAddress() != null && !tenant.getPresentAddress().isEmpty() ? tenant.getPresentAddress() : noneVal);
        tvTenantDetailsPermanentAddress.setText(tenant.getPermanentAddress() != null && !tenant.getPermanentAddress().isEmpty() ? tenant.getPermanentAddress() : noneVal);
        tvTenantDetailsFamilyCount.setText(String.valueOf(tenant.getFamilyMemberCount() > 0 ? tenant.getFamilyMemberCount() : 1));
        tvTenantDetailsNotes.setText(tenant.getNotes() != null && !tenant.getNotes().isEmpty() ? tenant.getNotes() : getString(R.string.no_notes));
    }

    private void loadTenancyHistory() {
        if (tenantId == -1) {
            return;
        }

        tenancyRepository.getTenanciesWithDetailsByTenant(tenantId, new TenancyRepository.DatabaseCallback<List<TenancyWithDetails>>() {
            @Override
            public void onSuccess(List<TenancyWithDetails> list) {
                runOnUiThread(() -> displayTenancies(list));
            }

            @Override
            public void onError(Exception exception) {
            }
        });
    }

    private void displayTenancies(List<TenancyWithDetails> list) {
        if (list == null || list.isEmpty()) {
            tvCurrentTenancy.setText(R.string.no_active_tenancy);
            tvPreviousTenancies.setText(R.string.no_previous_tenancy);
            return;
        }

        StringBuilder activeText = new StringBuilder();
        StringBuilder previousText = new StringBuilder();

        for (TenancyWithDetails item : list) {
            if (item.tenancy == null) continue;

            String unitName = item.unit != null ? getString(R.string.prefix_unit_format, item.unit.getUnitNumber()) : getString(R.string.prefix_unit_hash, item.tenancy.getUnitId());
            String rent = getString(R.string.currency_symbol) + currencyFormatter.format(item.tenancy.getMonthlyRent());
            String start = item.tenancy.getStartDate() != null ? item.tenancy.getStartDate() : getString(R.string.unknown_value);

            if ("ACTIVE".equalsIgnoreCase(item.tenancy.getStatus())) {
                activeText.append("🏢 ").append(unitName)
                        .append("\n").append(getString(R.string.prefix_rent_format, rent, getString(R.string.per_month)))
                        .append("\n").append(getString(R.string.prefix_started_format, start));

                tvCurrentTenancy.setOnClickListener(v -> {
                    Intent intent = new Intent(TenantDetailsActivity.this, TenancyDetailsActivity.class);
                    intent.putExtra("tenancy_id", item.tenancy.getId());
                    startActivity(intent);
                });
            } else {
                String end = item.tenancy.getEndDate() != null ? item.tenancy.getEndDate() : getString(R.string.status_ended);
                if (previousText.length() > 0) {
                    previousText.append("\n\n");
                }
                previousText.append("• ").append(unitName)
                        .append(" (").append(start).append(" → ").append(end).append(")")
                        .append(" [").append(TenancyAdapter.getStatusDisplay(this, item.tenancy.getStatus())).append("]");
            }
        }

        if (activeText.length() > 0) {
            tvCurrentTenancy.setText(activeText.toString());
        } else {
            tvCurrentTenancy.setText(R.string.no_active_tenancy);
        }

        if (previousText.length() > 0) {
            tvPreviousTenancies.setText(previousText.toString());
        } else {
            tvPreviousTenancies.setText(R.string.no_previous_tenancy);
        }
    }

    private void applyStatusBadgeStyle(Context context, TextView badge, String status) {
        int bgColor;
        int textColor;

        switch (status) {
            case "INACTIVE":
                bgColor = ContextCompat.getColor(context, R.color.status_maintenance_bg);
                textColor = ContextCompat.getColor(context, R.color.status_maintenance_text);
                break;
            case "ARCHIVED":
                bgColor = ContextCompat.getColor(context, R.color.status_reserved_bg);
                textColor = ContextCompat.getColor(context, R.color.status_reserved_text);
                break;
            case "ACTIVE":
            default:
                bgColor = ContextCompat.getColor(context, R.color.status_vacant_bg);
                textColor = ContextCompat.getColor(context, R.color.status_vacant_text);
                break;
        }

        GradientDrawable shape = new GradientDrawable();
        shape.setCornerRadius(24f);
        shape.setColor(bgColor);
        badge.setBackground(shape);
        badge.setTextColor(textColor);
    }

    private void loadDocuments() {
        if (tenantId == -1) {
            return;
        }

        repository.getDocumentsByTenant(tenantId, new TenantRepository.DatabaseCallback<List<TenantDocument>>() {
            @Override
            public void onSuccess(List<TenantDocument> documents) {
                runOnUiThread(() -> displayDocuments(documents));
            }

            @Override
            public void onError(Exception exception) {
                runOnUiThread(() -> displayDocuments(null));
            }
        });
    }

    private void displayDocuments(List<TenantDocument> documents) {
        int count = documents != null ? documents.size() : 0;
        tvDocumentCount.setText(count == 1 ? getString(R.string.count_documents_singular, count) : getString(R.string.count_documents_plural, count));

        if (documents == null || documents.isEmpty()) {
            tvNoDocuments.setVisibility(View.VISIBLE);
            recyclerDocuments.setVisibility(View.GONE);
            documentAdapter.setDocuments(null);
        } else {
            tvNoDocuments.setVisibility(View.GONE);
            recyclerDocuments.setVisibility(View.VISIBLE);
            documentAdapter.setDocuments(documents);
        }
    }

    private void showDocumentTypePicker() {
        String[] docTypes = {
                getString(R.string.document_type_nid),
                getString(R.string.document_type_passport),
                getString(R.string.document_type_photo),
                getString(R.string.document_type_agreement),
                getString(R.string.document_type_employment),
                getString(R.string.document_type_other)
        };

        new MaterialAlertDialogBuilder(this)
                .setTitle(R.string.select_document_type)
                .setItems(docTypes, (dialog, which) -> {
                    selectedDocType = docTypes[which];
                    filePickerLauncher.launch(new String[]{"*/*", "image/*", "application/pdf"});
                })
                .setNegativeButton(R.string.cancel, null)
                .show();
    }

    private void saveImportedDocument(Uri uri) {
        String originalName = FileUtils.getFileName(this, uri);
        String mimeType = FileUtils.getMimeType(this, uri);

        String targetFileName = "doc_" + tenantId + "_" + System.currentTimeMillis() + "_" + originalName;
        File savedFile = FileUtils.copyUriToPrivateStorage(this, uri, "tenant_documents", targetFileName);

        if (savedFile != null && savedFile.exists()) {
            TenantDocument document = new TenantDocument(
                    tenantId,
                    selectedDocType,
                    originalName,
                    savedFile.getAbsolutePath(),
                    mimeType,
                    System.currentTimeMillis(),
                    System.currentTimeMillis()
            );

            repository.insertDocument(document, new TenantRepository.DatabaseCallback<Long>() {
                @Override
                public void onSuccess(Long id) {
                    runOnUiThread(() -> {
                        Toast.makeText(TenantDetailsActivity.this, R.string.document_added_success, Toast.LENGTH_SHORT).show();
                        loadDocuments();
                    });
                }

                @Override
                public void onError(Exception exception) {
                    runOnUiThread(() -> Toast.makeText(TenantDetailsActivity.this, R.string.save_failed, Toast.LENGTH_SHORT).show());
                }
            });
        } else {
            Toast.makeText(this, R.string.save_failed, Toast.LENGTH_SHORT).show();
        }
    }

    private void viewDocument(TenantDocument document) {
        if (document == null) return;
        com.rob.houserental.utils.DocumentOpenUtils.openDocument(
                this,
                document.getFilePath(),
                document.getMimeType(),
                document.getDisplayName()
        );
    }

    private void showDeleteDocumentDialog(TenantDocument document) {
        new MaterialAlertDialogBuilder(this)
                .setTitle(R.string.delete_document)
                .setMessage(R.string.delete_document_confirm)
                .setPositiveButton(R.string.delete, (dialog, which) -> {
                    if (document.getFilePath() != null) {
                        File file = new File(document.getFilePath());
                        if (file.exists()) {
                            file.delete();
                        }
                    }

                    repository.deleteDocument(document, new TenantRepository.DatabaseCallback<Void>() {
                        @Override
                        public void onSuccess(Void result) {
                            runOnUiThread(() -> {
                                Toast.makeText(TenantDetailsActivity.this, R.string.document_deleted_success, Toast.LENGTH_SHORT).show();
                                loadDocuments();
                            });
                        }

                        @Override
                        public void onError(Exception exception) {
                        }
                    });
                })
                .setNegativeButton(R.string.cancel, null)
                .show();
    }

    private void showChangeStatusDialog() {
        String[] statuses = {"ACTIVE", "INACTIVE", "ARCHIVED"};
        int selectedIndex = 0;
        for (int i = 0; i < statuses.length; i++) {
            if (statuses[i].equalsIgnoreCase(currentTenant.getStatus())) {
                selectedIndex = i;
                break;
            }
        }

        final int[] choice = {selectedIndex};

        new MaterialAlertDialogBuilder(this)
                .setTitle(R.string.change_status)
                .setSingleChoiceItems(statuses, selectedIndex, (dialog, which) -> choice[0] = which)
                .setPositiveButton(R.string.edit, (dialog, which) -> {
                    String newStatus = statuses[choice[0]];
                    currentTenant.setStatus(newStatus);
                    currentTenant.setUpdatedAt(System.currentTimeMillis());
                    repository.update(currentTenant, new TenantRepository.DatabaseCallback<Void>() {
                        @Override
                        public void onSuccess(Void result) {
                            runOnUiThread(() -> {
                                Toast.makeText(TenantDetailsActivity.this, R.string.status_updated_success, Toast.LENGTH_SHORT).show();
                                displayTenantDetails(currentTenant);
                            });
                        }

                        @Override
                        public void onError(Exception exception) {
                            runOnUiThread(() -> Toast.makeText(TenantDetailsActivity.this, R.string.update_failed, Toast.LENGTH_SHORT).show());
                        }
                    });
                })
                .setNegativeButton(R.string.cancel, null)
                .show();
    }

    private void showDeleteTenantDialog() {
        new MaterialAlertDialogBuilder(this)
                .setTitle(R.string.delete_tenant)
                .setMessage(getString(R.string.delete_tenant_confirm, currentTenant.getFullName()))
                .setPositiveButton(R.string.delete, (dialog, which) -> {
                    repository.delete(currentTenant, new TenantRepository.DatabaseCallback<Void>() {
                        @Override
                        public void onSuccess(Void result) {
                            runOnUiThread(() -> {
                                Toast.makeText(TenantDetailsActivity.this, R.string.tenant_deleted_success, Toast.LENGTH_SHORT).show();
                                finish();
                            });
                        }

                        @Override
                        public void onError(Exception exception) {
                            runOnUiThread(() -> Toast.makeText(TenantDetailsActivity.this, R.string.save_failed, Toast.LENGTH_SHORT).show());
                        }
                    });
                })
                .setNegativeButton(R.string.cancel, null)
                .show();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (repository != null) {
            loadTenant();
            loadTenancyHistory();
            loadDocuments();
        }
    }
}

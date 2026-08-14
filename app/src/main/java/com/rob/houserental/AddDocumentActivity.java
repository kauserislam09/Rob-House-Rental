package com.rob.houserental;

import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.MaterialAutoCompleteTextView;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.rob.houserental.adapter.DocumentAdapter;
import com.rob.houserental.data.AppDatabase;
import com.rob.houserental.model.AppDocument;
import com.rob.houserental.model.Property;
import com.rob.houserental.model.Tenant;
import com.rob.houserental.repository.DocumentRepository;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class AddDocumentActivity extends AppCompatActivity {

    private TextInputLayout layoutDocDisplayName;
    private TextInputEditText etDocumentDisplayName;

    private TextInputLayout layoutDocType;
    private MaterialAutoCompleteTextView autoDocumentType;

    private TextInputLayout layoutDocProperty;
    private MaterialAutoCompleteTextView autoDocProperty;

    private TextInputLayout layoutDocTenant;
    private MaterialAutoCompleteTextView autoDocTenant;

    private TextInputLayout layoutDocCategory;
    private MaterialAutoCompleteTextView autoDocCategory;

    private MaterialButton btnChooseDocumentFile;
    private View layoutAttachedFilePreview;
    private TextView tvAttachedFileName;
    private TextView tvAttachedFileSize;
    private MaterialButton btnRemoveAttachedFile;

    private TextInputEditText etDocumentNotes;
    private MaterialButton btnSaveDocument;

    private DocumentRepository repository;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    private final List<Property> propertyList = new ArrayList<>();
    private final List<Tenant> tenantList = new ArrayList<>();

    private Property selectedProperty;
    private Tenant selectedTenant;

    private String selectedDocType = "PROPERTY";
    private String selectedCategory = "DEED";

    private String attachedFilePath;
    private String attachedFileName;
    private String attachedMimeType;
    private long attachedFileSize = 0;

    private final ActivityResultLauncher<String> filePickerLauncher =
            registerForActivityResult(new ActivityResultContracts.GetContent(), uri -> {
                if (uri != null) {
                    saveFileLocally(uri);
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_add_document);

        initializeViews();

        setupToolbar();

        repository = new DocumentRepository(getApplicationContext());

        setupDocTypeDropdown();

        setupCategoryDropdown();

        loadProperties();

        setupListeners();
    }

    private void initializeViews() {
        layoutDocDisplayName = findViewById(R.id.layoutDocDisplayName);
        etDocumentDisplayName = findViewById(R.id.etDocumentDisplayName);

        layoutDocType = findViewById(R.id.layoutDocType);
        autoDocumentType = findViewById(R.id.autoDocumentType);

        layoutDocProperty = findViewById(R.id.layoutDocProperty);
        autoDocProperty = findViewById(R.id.autoDocProperty);

        layoutDocTenant = findViewById(R.id.layoutDocTenant);
        autoDocTenant = findViewById(R.id.autoDocTenant);

        layoutDocCategory = findViewById(R.id.layoutDocCategory);
        autoDocCategory = findViewById(R.id.autoDocCategory);

        btnChooseDocumentFile = findViewById(R.id.btnChooseDocumentFile);
        layoutAttachedFilePreview = findViewById(R.id.layoutAttachedFilePreview);
        tvAttachedFileName = findViewById(R.id.tvAttachedFileName);
        tvAttachedFileSize = findViewById(R.id.tvAttachedFileSize);
        btnRemoveAttachedFile = findViewById(R.id.btnRemoveAttachedFile);

        etDocumentNotes = findViewById(R.id.etDocumentNotes);
        btnSaveDocument = findViewById(R.id.btnSaveDocument);
    }

    private void setupToolbar() {
        MaterialToolbar toolbar = findViewById(R.id.toolbarAddDocument);
        toolbar.setNavigationOnClickListener(v -> finish());
    }

    private void setupDocTypeDropdown() {
        String[] typeKeys = {
                "PROPERTY", "TENANT", "EXPENSE", "RENT_PAYMENT", "UTILITY_BILL", "GENERAL"
        };
        String[] typeLabels = {
                getString(R.string.type_property),
                getString(R.string.type_tenant),
                getString(R.string.type_expense),
                getString(R.string.type_rent),
                getString(R.string.type_utility),
                getString(R.string.type_general)
        };

        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_dropdown_item_1line,
                typeLabels
        );
        autoDocumentType.setAdapter(adapter);
        autoDocumentType.setText(typeLabels[0], false);
        selectedDocType = typeKeys[0];

        autoDocumentType.setOnItemClickListener((parent, view, position, id) -> {
            selectedDocType = typeKeys[position];
            updateFormVisibilityForType(selectedDocType);
        });
    }

    private void updateFormVisibilityForType(String docType) {
        if ("GENERAL".equalsIgnoreCase(docType)) {
            layoutDocProperty.setVisibility(View.GONE);
            layoutDocTenant.setVisibility(View.GONE);
            selectedProperty = null;
            selectedTenant = null;
        } else if ("TENANT".equalsIgnoreCase(docType) || "RENT_PAYMENT".equalsIgnoreCase(docType)) {
            layoutDocProperty.setVisibility(View.VISIBLE);
            layoutDocTenant.setVisibility(View.VISIBLE);
        } else {
            layoutDocProperty.setVisibility(View.VISIBLE);
            layoutDocTenant.setVisibility(View.GONE);
            selectedTenant = null;
        }
    }

    private void setupCategoryDropdown() {
        String[] catKeys = {
                "DEED", "TAX_RECORD", "NID", "PASSPORT", "AGREEMENT",
                "VOUCHER", "PAYMENT_SLIP", "BILL_RECEIPT", "POLICE_FORM", "OTHER"
        };
        String[] catLabels = {
                getString(R.string.doc_cat_deed),
                getString(R.string.doc_cat_tax),
                getString(R.string.doc_cat_nid),
                getString(R.string.doc_cat_passport),
                getString(R.string.doc_cat_agreement),
                getString(R.string.doc_cat_voucher),
                getString(R.string.doc_cat_rent_slip),
                getString(R.string.doc_cat_bill_copy),
                getString(R.string.doc_cat_police),
                getString(R.string.doc_cat_other)
        };

        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_dropdown_item_1line,
                catLabels
        );
        autoDocCategory.setAdapter(adapter);
        autoDocCategory.setText(catLabels[0], false);
        selectedCategory = catKeys[0];

        autoDocCategory.setOnItemClickListener((parent, view, position, id) -> {
            selectedCategory = catKeys[position];
        });
    }

    private void loadProperties() {
        executor.execute(() -> {
            try {
                AppDatabase db = AppDatabase.getInstance(getApplicationContext());
                List<Property> properties = db.propertyDao().getAllProperties();

                runOnUiThread(() -> {
                    propertyList.clear();
                    if (properties != null) {
                        propertyList.addAll(properties);
                    }

                    List<String> propertyNames = new ArrayList<>();
                    for (Property p : propertyList) {
                        propertyNames.add(p.getName());
                    }

                    ArrayAdapter<String> adapter = new ArrayAdapter<>(
                            AddDocumentActivity.this,
                            android.R.layout.simple_dropdown_item_1line,
                            propertyNames
                    );
                    autoDocProperty.setAdapter(adapter);

                    autoDocProperty.setOnItemClickListener((parent, view, position, id) -> {
                        selectedProperty = propertyList.get(position);
                        loadTenants();
                    });
                });
            } catch (Exception ignored) {
            }
        });
    }

    private void loadTenants() {
        executor.execute(() -> {
            try {
                AppDatabase db = AppDatabase.getInstance(getApplicationContext());
                List<Tenant> tenants = db.tenantDao().getActiveTenants();

                runOnUiThread(() -> {
                    tenantList.clear();
                    if (tenants != null) {
                        tenantList.addAll(tenants);
                    }

                    List<String> tenantNames = new ArrayList<>();
                    for (Tenant t : tenantList) {
                        tenantNames.add(t.getFullName() + (t.getPhoneNumber() != null ? " (" + t.getPhoneNumber() + ")" : ""));
                    }

                    ArrayAdapter<String> adapter = new ArrayAdapter<>(
                            AddDocumentActivity.this,
                            android.R.layout.simple_dropdown_item_1line,
                            tenantNames
                    );
                    autoDocTenant.setAdapter(adapter);

                    autoDocTenant.setOnItemClickListener((parent, view, position, id) -> {
                        selectedTenant = tenantList.get(position);
                    });
                });
            } catch (Exception ignored) {
            }
        });
    }

    private void setupListeners() {
        btnChooseDocumentFile.setOnClickListener(v -> filePickerLauncher.launch("*/*"));

        btnRemoveAttachedFile.setOnClickListener(v -> {
            attachedFilePath = null;
            attachedFileName = null;
            attachedMimeType = null;
            attachedFileSize = 0;
            layoutAttachedFilePreview.setVisibility(View.GONE);
        });

        btnSaveDocument.setOnClickListener(v -> saveDocument());
    }

    private void saveFileLocally(Uri uri) {
        executor.execute(() -> {
            try {
                InputStream inputStream = getContentResolver().openInputStream(uri);
                if (inputStream == null) return;

                File docsDir = new File(getFilesDir(), "documents");
                if (!docsDir.exists()) {
                    docsDir.mkdirs();
                }

                String fileName = "doc_" + System.currentTimeMillis();
                File targetFile = new File(docsDir, fileName);

                FileOutputStream outputStream = new FileOutputStream(targetFile);
                byte[] buffer = new byte[4096];
                int bytesRead;
                long totalBytes = 0;
                while ((bytesRead = inputStream.read(buffer)) != -1) {
                    outputStream.write(buffer, 0, bytesRead);
                    totalBytes += bytesRead;
                }
                outputStream.flush();
                outputStream.close();
                inputStream.close();

                attachedFilePath = targetFile.getAbsolutePath();
                attachedFileName = fileName;
                attachedMimeType = getContentResolver().getType(uri);
                attachedFileSize = totalBytes;

                runOnUiThread(() -> {
                    tvAttachedFileName.setText(fileName);
                    tvAttachedFileSize.setText(DocumentAdapter.formatFileSize(attachedFileSize) + " • " + DocumentAdapter.formatMimeType(attachedMimeType, fileName));
                    layoutAttachedFilePreview.setVisibility(View.VISIBLE);

                    if (TextUtils.isEmpty(getText(etDocumentDisplayName))) {
                        etDocumentDisplayName.setText(DocumentAdapter.getCategoryTitle(AddDocumentActivity.this, selectedCategory));
                    }
                });
            } catch (Exception e) {
                runOnUiThread(() -> Toast.makeText(AddDocumentActivity.this, R.string.file_attach_failed, Toast.LENGTH_SHORT).show());
            }
        });
    }

    private void saveDocument() {
        clearErrors();

        String displayName = getText(etDocumentDisplayName);
        if (TextUtils.isEmpty(displayName)) {
            layoutDocDisplayName.setError(getString(R.string.document_name_required));
            etDocumentDisplayName.requestFocus();
            return;
        }

        if (attachedFilePath == null || attachedFilePath.isEmpty()) {
            Toast.makeText(this, R.string.file_selection_required, Toast.LENGTH_SHORT).show();
            return;
        }

        String notes = getText(etDocumentNotes);
        long propertyId = selectedProperty != null ? selectedProperty.getId() : 0;
        long tenantId = selectedTenant != null ? selectedTenant.getId() : 0;
        long currentTime = System.currentTimeMillis();

        AppDocument document = new AppDocument(
                selectedDocType,
                selectedCategory,
                displayName,
                attachedFileName,
                attachedFilePath,
                attachedMimeType,
                attachedFileSize,
                propertyId,
                0,
                tenantId,
                0,
                notes,
                false,
                currentTime,
                currentTime
        );

        btnSaveDocument.setEnabled(false);

        repository.saveDocument(document, new DocumentRepository.DatabaseCallback<Long>() {
            @Override
            public void onSuccess(Long id) {
                runOnUiThread(() -> {
                    btnSaveDocument.setEnabled(true);
                    Toast.makeText(AddDocumentActivity.this, R.string.document_saved_success, Toast.LENGTH_SHORT).show();
                    setResult(RESULT_OK);
                    finish();
                });
            }

            @Override
            public void onError(Exception exception) {
                runOnUiThread(() -> {
                    btnSaveDocument.setEnabled(true);
                    Toast.makeText(AddDocumentActivity.this, R.string.save_failed, Toast.LENGTH_SHORT).show();
                });
            }
        });
    }

    private String getText(android.widget.TextView view) {
        if (view == null || view.getText() == null) {
            return "";
        }
        return view.getText().toString().trim();
    }

    private void clearErrors() {
        layoutDocDisplayName.setError(null);
        layoutDocProperty.setError(null);
        layoutDocTenant.setError(null);
    }
}

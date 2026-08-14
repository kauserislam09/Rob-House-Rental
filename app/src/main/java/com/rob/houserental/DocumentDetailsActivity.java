package com.rob.houserental;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.rob.houserental.adapter.DocumentAdapter;
import com.rob.houserental.model.AppDocument;
import com.rob.houserental.model.AppDocumentDisplayItem;
import com.rob.houserental.repository.DocumentRepository;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class DocumentDetailsActivity extends AppCompatActivity {

    private long documentId;
    private DocumentRepository repository;
    private AppDocumentDisplayItem currentDisplayItem;
    private AppDocument currentDocument;

    private TextView tvDocDetailsIcon;
    private TextView tvDocDetailsDisplayName;
    private TextView tvDocDetailsCategoryBadge;

    private TextView tvDocDetailsLinkedEntity;
    private TextView tvDocDetailsFileInfo;
    private TextView tvDocDetailsDate;
    private TextView tvDocDetailsNotes;

    private TextView tvDocDetailsFileName;
    private MaterialButton btnOpenDocumentFile;

    private MaterialButton btnRenameDocument;
    private MaterialButton btnArchiveDocument;
    private MaterialButton btnDeleteDocument;

    private static final SimpleDateFormat dateFormat = new SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_document_details);

        documentId = getIntent().getLongExtra("document_id", -1);
        if (documentId == -1) {
            Toast.makeText(this, R.string.invalid_document_record, Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        initializeViews();

        setupToolbar();

        repository = new DocumentRepository(getApplicationContext());

        setupListeners();

        loadDocumentDetails();
    }

    private void initializeViews() {
        tvDocDetailsIcon = findViewById(R.id.tvDocDetailsIcon);
        tvDocDetailsDisplayName = findViewById(R.id.tvDocDetailsDisplayName);
        tvDocDetailsCategoryBadge = findViewById(R.id.tvDocDetailsCategoryBadge);

        tvDocDetailsLinkedEntity = findViewById(R.id.tvDocDetailsLinkedEntity);
        tvDocDetailsFileInfo = findViewById(R.id.tvDocDetailsFileInfo);
        tvDocDetailsDate = findViewById(R.id.tvDocDetailsDate);
        tvDocDetailsNotes = findViewById(R.id.tvDocDetailsNotes);

        tvDocDetailsFileName = findViewById(R.id.tvDocDetailsFileName);
        btnOpenDocumentFile = findViewById(R.id.btnOpenDocumentFile);

        btnRenameDocument = findViewById(R.id.btnRenameDocument);
        btnArchiveDocument = findViewById(R.id.btnArchiveDocument);
        btnDeleteDocument = findViewById(R.id.btnDeleteDocument);
    }

    private void setupToolbar() {
        MaterialToolbar toolbar = findViewById(R.id.toolbarDocumentDetails);
        toolbar.setNavigationOnClickListener(v -> finish());
    }

    private void setupListeners() {
        btnOpenDocumentFile.setOnClickListener(v -> openDocumentFile());

        btnRenameDocument.setOnClickListener(v -> showRenameDialog());

        btnArchiveDocument.setOnClickListener(v -> showArchiveConfirmationDialog());

        btnDeleteDocument.setOnClickListener(v -> showDeleteConfirmationDialog());
    }

    private void loadDocumentDetails() {
        repository.getDisplayItemById(documentId, new DocumentRepository.DatabaseCallback<AppDocumentDisplayItem>() {
            @Override
            public void onSuccess(AppDocumentDisplayItem item) {
                if (item == null) {
                    runOnUiThread(() -> {
                        Toast.makeText(DocumentDetailsActivity.this, R.string.document_not_found, Toast.LENGTH_SHORT).show();
                        finish();
                    });
                    return;
                }
                currentDisplayItem = item;
                runOnUiThread(() -> populateViews(item));
            }

            @Override
            public void onError(Exception exception) {
                runOnUiThread(() -> Toast.makeText(DocumentDetailsActivity.this, R.string.document_load_failed, Toast.LENGTH_SHORT).show());
            }
        });

        repository.getDocumentById(documentId, new DocumentRepository.DatabaseCallback<AppDocument>() {
            @Override
            public void onSuccess(AppDocument doc) {
                currentDocument = doc;
            }

            @Override
            public void onError(Exception exception) {
            }
        });
    }

    private void populateViews(AppDocumentDisplayItem item) {
        tvDocDetailsIcon.setText(DocumentAdapter.getDocumentIcon(item));
        tvDocDetailsDisplayName.setText(item.displayName != null ? item.displayName : getString(R.string.type_document));
        tvDocDetailsCategoryBadge.setText(DocumentAdapter.getCategoryTitle(this, item.category));

        // Linked entity
        StringBuilder entity = new StringBuilder();
        if (item.propertyName != null && !item.propertyName.isEmpty()) {
            entity.append("").append(item.propertyName);
            if (item.unitNumber != null && !item.unitNumber.isEmpty()) {
                entity.append(" • ").append(getString(R.string.prefix_unit_format, item.unitNumber));
            }
        }
        if (item.tenantFullName != null && !item.tenantFullName.isEmpty()) {
            if (entity.length() > 0) {
                entity.append("\n");
            }
            entity.append("").append(item.tenantFullName);
            if (item.tenantPhone != null && !item.tenantPhone.isEmpty()) {
                entity.append(" (").append(item.tenantPhone).append(")");
            }
        }
        if (entity.length() == 0) {
            entity.append("").append(getString(R.string.type_general));
        }
        tvDocDetailsLinkedEntity.setText(entity.toString());

        // File info
        String sizeFormatted = DocumentAdapter.formatFileSize(item.fileSize);
        String formatFormatted = DocumentAdapter.formatMimeType(item.mimeType, item.fileName);
        tvDocDetailsFileInfo.setText(getString(R.string.prefix_size_format, sizeFormatted) + " • " + formatFormatted);

        tvDocDetailsDate.setText((item.createdAt > 0 ? dateFormat.format(new Date(item.createdAt)) : ""));

        if (item.notes != null && !item.notes.trim().isEmpty()) {
            tvDocDetailsNotes.setText(getString(R.string.prefix_notes, item.notes.trim()));
        } else {
            tvDocDetailsNotes.setText(R.string.no_notes_provided);
        }

        tvDocDetailsFileName.setText(item.fileName != null ? item.fileName : "file");
    }

    private void openDocumentFile() {
        if (currentDisplayItem == null) return;
        com.rob.houserental.utils.DocumentOpenUtils.openDocument(
                this,
                currentDisplayItem.filePath,
                currentDisplayItem.mimeType,
                currentDisplayItem.fileName != null ? currentDisplayItem.fileName : currentDisplayItem.displayName
        );
    }

    private void showRenameDialog() {
        if (currentDisplayItem == null) return;

        final EditText input = new EditText(this);
        input.setText(currentDisplayItem.displayName != null ? currentDisplayItem.displayName : "");
        input.setSelection(input.getText().length());

        new MaterialAlertDialogBuilder(this)
                .setTitle(R.string.rename_document)
                .setView(input)
                .setPositiveButton(R.string.save_unit, (dialog, which) -> {
                    String newName = input.getText().toString().trim();
                    if (!TextUtils.isEmpty(newName)) {
                        repository.renameDocument(documentId, newName, new DocumentRepository.DatabaseCallback<Void>() {
                            @Override
                            public void onSuccess(Void result) {
                                runOnUiThread(() -> {
                                    Toast.makeText(DocumentDetailsActivity.this, R.string.document_renamed_success, Toast.LENGTH_SHORT).show();
                                    loadDocumentDetails();
                                });
                            }

                            @Override
                            public void onError(Exception exception) {
                            }
                        });
                    }
                })
                .setNegativeButton(R.string.cancel, null)
                .show();
    }

    private void showArchiveConfirmationDialog() {
        new MaterialAlertDialogBuilder(this)
                .setTitle(R.string.archive_expense)
                .setMessage(R.string.archive_expense_confirm)
                .setPositiveButton(R.string.archive_expense, (dialog, which) -> {
                    repository.archiveDocument(documentId, new DocumentRepository.DatabaseCallback<Void>() {
                        @Override
                        public void onSuccess(Void result) {
                            runOnUiThread(() -> {
                                Toast.makeText(DocumentDetailsActivity.this, R.string.document_archived_success, Toast.LENGTH_SHORT).show();
                                finish();
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

    private void showDeleteConfirmationDialog() {
        if (currentDisplayItem == null) return;

        String name = currentDisplayItem.displayName != null ? currentDisplayItem.displayName : "this document";

        new MaterialAlertDialogBuilder(this)
                .setTitle(R.string.delete_document)
                .setMessage(getString(R.string.delete_document_confirm_msg, name))
                .setPositiveButton(R.string.delete_document, (dialog, which) -> {
                    if (currentDocument != null) {
                        repository.deleteDocument(currentDocument, true, new DocumentRepository.DatabaseCallback<Void>() {
                            @Override
                            public void onSuccess(Void result) {
                                runOnUiThread(() -> {
                                    Toast.makeText(DocumentDetailsActivity.this, R.string.document_deleted_success, Toast.LENGTH_SHORT).show();
                                    finish();
                                });
                            }

                            @Override
                            public void onError(Exception exception) {
                                runOnUiThread(() -> Toast.makeText(DocumentDetailsActivity.this, R.string.delete_failed, Toast.LENGTH_SHORT).show());
                            }
                        });
                    }
                })
                .setNegativeButton(R.string.cancel, null)
                .show();
    }
}

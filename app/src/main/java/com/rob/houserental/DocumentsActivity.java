package com.rob.houserental;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.PopupMenu;
import androidx.core.content.FileProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;
import com.rob.houserental.adapter.DocumentAdapter;
import com.rob.houserental.model.AppDocument;
import com.rob.houserental.model.AppDocumentDisplayItem;
import com.rob.houserental.repository.DocumentRepository;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class DocumentsActivity extends AppCompatActivity {

    private TextView tvTotalDocumentsCount;
    private TextView tvTotalStorageUsed;
    private MaterialButton btnAddDocumentHeader;

    private TextInputEditText etSearchDocuments;
    private ChipGroup chipGroupDocTypes;
    private TextView tvDocsCount;
    private RecyclerView recyclerDocuments;
    private View layoutEmptyDocuments;
    private MaterialButton btnEmptyAddDocument;
    private ExtendedFloatingActionButton fabAddDocument;

    private DocumentAdapter adapter;
    private DocumentRepository repository;

    private final List<AppDocumentDisplayItem> allDocuments = new ArrayList<>();
    private String currentSearchQuery = "";
    private String currentTypeFilter = "ALL";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_documents);

        initializeViews();

        setupToolbar();

        setupRecyclerView();

        repository = new DocumentRepository(getApplicationContext());

        setupSearchAndFilters();

        setupListeners();

        loadDocuments();
    }

    private void initializeViews() {
        tvTotalDocumentsCount = findViewById(R.id.tvTotalDocumentsCount);
        tvTotalStorageUsed = findViewById(R.id.tvTotalStorageUsed);
        btnAddDocumentHeader = findViewById(R.id.btnAddDocumentHeader);

        etSearchDocuments = findViewById(R.id.etSearchDocuments);
        chipGroupDocTypes = findViewById(R.id.chipGroupDocTypes);
        tvDocsCount = findViewById(R.id.tvDocsCount);
        recyclerDocuments = findViewById(R.id.recyclerDocuments);
        layoutEmptyDocuments = findViewById(R.id.layoutEmptyDocuments);
        btnEmptyAddDocument = findViewById(R.id.btnEmptyAddDocument);
        fabAddDocument = findViewById(R.id.fabAddDocument);
    }

    private void setupToolbar() {
        MaterialToolbar toolbar = findViewById(R.id.toolbarDocuments);
        toolbar.setNavigationOnClickListener(v -> finish());
    }

    private void setupRecyclerView() {
        adapter = new DocumentAdapter();
        recyclerDocuments.setLayoutManager(new LinearLayoutManager(this));
        recyclerDocuments.setAdapter(adapter);

        adapter.setOnDocumentClickListener(new DocumentAdapter.OnDocumentClickListener() {
            @Override
            public void onDocumentClick(AppDocumentDisplayItem item) {
                Intent intent = new Intent(DocumentsActivity.this, DocumentDetailsActivity.class);
                intent.putExtra("document_id", item.id);
                startActivity(intent);
            }

            @Override
            public void onDocumentOptionsClick(AppDocumentDisplayItem item, View anchorView) {
                showDocumentPopupMenu(item, anchorView);
            }
        });
    }

    private void setupSearchAndFilters() {
        etSearchDocuments.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int count, int after) {
                currentSearchQuery = s != null ? s.toString().trim().toLowerCase() : "";
                applyFilterAndSearch();
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        chipGroupDocTypes.setOnCheckedStateChangeListener((group, checkedIds) -> {
            if (checkedIds.isEmpty()) {
                currentTypeFilter = "ALL";
            } else {
                int checkedId = checkedIds.get(0);
                if (checkedId == R.id.chipDocProperty) {
                    currentTypeFilter = "PROPERTY";
                } else if (checkedId == R.id.chipDocTenant) {
                    currentTypeFilter = "TENANT";
                } else if (checkedId == R.id.chipDocExpense) {
                    currentTypeFilter = "EXPENSE";
                } else if (checkedId == R.id.chipDocRent) {
                    currentTypeFilter = "RENT_PAYMENT";
                } else if (checkedId == R.id.chipDocUtility) {
                    currentTypeFilter = "UTILITY_BILL";
                } else if (checkedId == R.id.chipDocGeneral) {
                    currentTypeFilter = "GENERAL";
                } else {
                    currentTypeFilter = "ALL";
                }
            }
            applyFilterAndSearch();
        });
    }

    private void setupListeners() {
        View.OnClickListener addListener = v -> {
            Intent intent = new Intent(DocumentsActivity.this, AddDocumentActivity.class);
            startActivity(intent);
        };

        btnAddDocumentHeader.setOnClickListener(addListener);
        btnEmptyAddDocument.setOnClickListener(addListener);
        fabAddDocument.setOnClickListener(addListener);
    }

    private void loadDocuments() {
        // Load Storage KPIs
        repository.getTotalDocumentCount(new DocumentRepository.DatabaseCallback<Integer>() {
            @Override
            public void onSuccess(Integer count) {
                runOnUiThread(() -> {
                    int c = count != null ? count : 0;
                    tvTotalDocumentsCount.setText(getString(R.string.total_documents_count, c));
                });
            }

            @Override
            public void onError(Exception exception) {
            }
        });

        repository.getTotalDocumentStorageBytes(new DocumentRepository.DatabaseCallback<Long>() {
            @Override
            public void onSuccess(Long bytes) {
                runOnUiThread(() -> {
                    long b = bytes != null ? bytes : 0;
                    tvTotalStorageUsed.setText(DocumentAdapter.formatFileSize(b));
                });
            }

            @Override
            public void onError(Exception exception) {
            }
        });

        // Load Documents List
        repository.getAllDisplayItems(new DocumentRepository.DatabaseCallback<List<AppDocumentDisplayItem>>() {
            @Override
            public void onSuccess(List<AppDocumentDisplayItem> list) {
                runOnUiThread(() -> {
                    allDocuments.clear();
                    if (list != null) {
                        allDocuments.addAll(list);
                    }
                    applyFilterAndSearch();
                });
            }

            @Override
            public void onError(Exception exception) {
                runOnUiThread(() -> {
                    allDocuments.clear();
                    applyFilterAndSearch();
                });
            }
        });
    }

    private void applyFilterAndSearch() {
        List<AppDocumentDisplayItem> filtered = new ArrayList<>();

        for (AppDocumentDisplayItem item : allDocuments) {
            // Type Filter
            boolean typeMatches = "ALL".equalsIgnoreCase(currentTypeFilter) ||
                    currentTypeFilter.equalsIgnoreCase(item.documentType);

            if (!typeMatches) continue;

            // Search filter
            if (TextUtils.isEmpty(currentSearchQuery)) {
                filtered.add(item);
            } else {
                boolean matches = false;
                if (item.displayName != null && item.displayName.toLowerCase().contains(currentSearchQuery)) {
                    matches = true;
                } else if (item.category != null && item.category.toLowerCase().contains(currentSearchQuery)) {
                    matches = true;
                } else if (item.propertyName != null && item.propertyName.toLowerCase().contains(currentSearchQuery)) {
                    matches = true;
                } else if (item.tenantFullName != null && item.tenantFullName.toLowerCase().contains(currentSearchQuery)) {
                    matches = true;
                } else if (item.notes != null && item.notes.toLowerCase().contains(currentSearchQuery)) {
                    matches = true;
                }

                if (matches) {
                    filtered.add(item);
                }
            }
        }

        updateList(filtered);
    }

    private void updateList(List<AppDocumentDisplayItem> list) {
        int count = list != null ? list.size() : 0;
        tvDocsCount.setText(count == 1 ? getString(R.string.count_documents_singular, count) : getString(R.string.count_documents_plural, count));

        if (count == 0) {
            recyclerDocuments.setVisibility(View.GONE);
            layoutEmptyDocuments.setVisibility(View.VISIBLE);
            adapter.setDocuments(null);
        } else {
            recyclerDocuments.setVisibility(View.VISIBLE);
            layoutEmptyDocuments.setVisibility(View.GONE);
            adapter.setDocuments(list);
        }
    }

    private void showDocumentPopupMenu(AppDocumentDisplayItem item, View anchorView) {
        PopupMenu popup = new PopupMenu(this, anchorView);
        popup.getMenu().add(0, 1, 0, R.string.view_document);
        popup.getMenu().add(0, 2, 1, R.string.rename_document);
        popup.getMenu().add(0, 3, 2, R.string.archive_expense);
        popup.getMenu().add(0, 4, 3, R.string.delete_document);

        popup.setOnMenuItemClickListener(menuItem -> {
            int id = menuItem.getItemId();
            if (id == 1) {
                openDocumentFile(item);
                return true;
            } else if (id == 2) {
                showRenameDialog(item);
                return true;
            } else if (id == 3) {
                archiveDocument(item);
                return true;
            } else if (id == 4) {
                confirmDeleteDocument(item);
                return true;
            }
            return false;
        });

        popup.show();
    }

    private void openDocumentFile(AppDocumentDisplayItem item) {
        if (item == null) return;
        com.rob.houserental.utils.DocumentOpenUtils.openDocument(
                this,
                item.filePath,
                item.mimeType,
                item.fileName != null ? item.fileName : item.displayName
        );
    }

    private void showRenameDialog(AppDocumentDisplayItem item) {
        final EditText input = new EditText(this);
        input.setText(item.displayName != null ? item.displayName : "");
        input.setSelection(input.getText().length());

        new MaterialAlertDialogBuilder(this)
                .setTitle(R.string.rename_document)
                .setView(input)
                .setPositiveButton(R.string.save_unit, (dialog, which) -> {
                    String newName = input.getText().toString().trim();
                    if (!TextUtils.isEmpty(newName)) {
                        repository.renameDocument(item.id, newName, new DocumentRepository.DatabaseCallback<Void>() {
                            @Override
                            public void onSuccess(Void result) {
                                runOnUiThread(() -> {
                                    Toast.makeText(DocumentsActivity.this, R.string.document_renamed_success, Toast.LENGTH_SHORT).show();
                                    loadDocuments();
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

    private void archiveDocument(AppDocumentDisplayItem item) {
        repository.archiveDocument(item.id, new DocumentRepository.DatabaseCallback<Void>() {
            @Override
            public void onSuccess(Void result) {
                runOnUiThread(() -> {
                    Toast.makeText(DocumentsActivity.this, R.string.document_archived_success, Toast.LENGTH_SHORT).show();
                    loadDocuments();
                });
            }

            @Override
            public void onError(Exception exception) {
            }
        });
    }

    private void confirmDeleteDocument(AppDocumentDisplayItem item) {
        String name = item.displayName != null ? item.displayName : "this document";
        new MaterialAlertDialogBuilder(this)
                .setTitle(R.string.delete_document)
                .setMessage(getString(R.string.delete_document_confirm_msg, name))
                .setPositiveButton(R.string.delete_document, (dialog, which) -> {
                    repository.getDocumentById(item.id, new DocumentRepository.DatabaseCallback<AppDocument>() {
                        @Override
                        public void onSuccess(AppDocument doc) {
                            if (doc != null) {
                                repository.deleteDocument(doc, true, new DocumentRepository.DatabaseCallback<Void>() {
                                    @Override
                                    public void onSuccess(Void result) {
                                        runOnUiThread(() -> {
                                            Toast.makeText(DocumentsActivity.this, R.string.document_deleted_success, Toast.LENGTH_SHORT).show();
                                            loadDocuments();
                                        });
                                    }

                                    @Override
                                    public void onError(Exception exception) {
                                        runOnUiThread(() -> Toast.makeText(DocumentsActivity.this, R.string.delete_failed, Toast.LENGTH_SHORT).show());
                                    }
                                });
                            }
                        }

                        @Override
                        public void onError(Exception exception) {
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
            loadDocuments();
        }
    }
}

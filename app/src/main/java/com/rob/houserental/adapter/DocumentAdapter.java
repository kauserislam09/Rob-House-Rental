package com.rob.houserental.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.rob.houserental.R;
import com.rob.houserental.model.AppDocumentDisplayItem;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class DocumentAdapter extends RecyclerView.Adapter<DocumentAdapter.DocumentViewHolder> {

    private final List<AppDocumentDisplayItem> documentList = new ArrayList<>();
    private OnDocumentClickListener listener;
    private static final SimpleDateFormat dateFormat = new SimpleDateFormat("dd MMM yyyy", Locale.getDefault());
    private static final DecimalFormat sizeFormat = new DecimalFormat("#,##0.#");

    public interface OnDocumentClickListener {
        void onDocumentClick(AppDocumentDisplayItem item);
        void onDocumentOptionsClick(AppDocumentDisplayItem item, View anchorView);
    }

    public void setOnDocumentClickListener(OnDocumentClickListener listener) {
        this.listener = listener;
    }

    public void setDocuments(List<AppDocumentDisplayItem> documents) {
        documentList.clear();
        if (documents != null) {
            documentList.addAll(documents);
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public DocumentViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_app_document, parent, false);
        return new DocumentViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull DocumentViewHolder holder, int position) {
        AppDocumentDisplayItem item = documentList.get(position);
        Context context = holder.itemView.getContext();

        holder.tvDocIcon.setText(getDocumentIcon(item));
        holder.tvDocDisplayName.setText(item.displayName != null ? item.displayName : context.getString(R.string.type_document));
        holder.tvDocCategory.setText(getCategoryTitle(context, item.category));

        // Linked Entity Label
        StringBuilder entityLabel = new StringBuilder();
        if (item.propertyName != null && !item.propertyName.isEmpty()) {
            entityLabel.append("🏢 ").append(item.propertyName);
            if (item.unitNumber != null && !item.unitNumber.isEmpty()) {
                entityLabel.append(" • ").append(context.getString(R.string.prefix_unit_format, item.unitNumber));
            }
        }
        if (item.tenantFullName != null && !item.tenantFullName.isEmpty()) {
            if (entityLabel.length() > 0) {
                entityLabel.append(" • ");
            }
            entityLabel.append("👤 ").append(item.tenantFullName);
        }
        if (entityLabel.length() == 0) {
            entityLabel.append("📂 ").append(context.getString(R.string.type_general));
        }
        holder.tvDocLinkedEntity.setText(entityLabel.toString());

        // File size and format
        holder.tvDocFileSize.setText(formatFileSize(item.fileSize));
        holder.tvDocDate.setText("📅 " + (item.createdAt > 0 ? dateFormat.format(new Date(item.createdAt)) : ""));
        holder.tvDocFormatBadge.setText(formatMimeType(item.mimeType, item.fileName));

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onDocumentClick(item);
            }
        });

        holder.btnDocOptions.setOnClickListener(v -> {
            if (listener != null) {
                listener.onDocumentOptionsClick(item, v);
            }
        });
    }

    public static String getDocumentIcon(AppDocumentDisplayItem item) {
        if (item == null) return "📄";
        if (item.mimeType != null && item.mimeType.contains("pdf")) {
            return "📕";
        }
        if (item.mimeType != null && item.mimeType.contains("image")) {
            return "🖼️";
        }
        if ("PROPERTY".equalsIgnoreCase(item.documentType)) {
            return "🏛️";
        }
        if ("TENANT".equalsIgnoreCase(item.documentType)) {
            return "🪪";
        }
        if ("EXPENSE".equalsIgnoreCase(item.documentType)) {
            return "💸";
        }
        if ("RENT_PAYMENT".equalsIgnoreCase(item.documentType) || "UTILITY_BILL".equalsIgnoreCase(item.documentType)) {
            return "🧾";
        }
        return "📄";
    }

    public static String getCategoryTitle(Context context, String category) {
        if (category == null) return context.getString(R.string.doc_cat_other);
        switch (category.toUpperCase()) {
            case "DEED":
                return context.getString(R.string.doc_cat_deed);
            case "TAX_RECORD":
                return context.getString(R.string.doc_cat_tax);
            case "NID":
                return context.getString(R.string.doc_cat_nid);
            case "PASSPORT":
                return context.getString(R.string.doc_cat_passport);
            case "AGREEMENT":
                return context.getString(R.string.doc_cat_agreement);
            case "VOUCHER":
                return context.getString(R.string.doc_cat_voucher);
            case "PAYMENT_SLIP":
                return context.getString(R.string.doc_cat_rent_slip);
            case "BILL_RECEIPT":
                return context.getString(R.string.doc_cat_bill_copy);
            case "POLICE_FORM":
                return context.getString(R.string.doc_cat_police);
            case "OTHER":
            default:
                return context.getString(R.string.doc_cat_other);
        }
    }

    public static String formatFileSize(long bytes) {
        if (bytes <= 0) return "0 KB";
        double kb = bytes / 1024.0;
        if (kb < 1000) {
            return sizeFormat.format(kb) + " KB";
        }
        double mb = kb / 1024.0;
        return sizeFormat.format(mb) + " MB";
    }

    public static String formatMimeType(String mimeType, String fileName) {
        if (mimeType != null) {
            if (mimeType.contains("pdf")) return "PDF";
            if (mimeType.contains("jpeg") || mimeType.contains("jpg")) return "JPG";
            if (mimeType.contains("png")) return "PNG";
        }
        if (fileName != null && fileName.toLowerCase().endsWith(".pdf")) return "PDF";
        if (fileName != null && (fileName.toLowerCase().endsWith(".jpg") || fileName.toLowerCase().endsWith(".jpeg"))) return "JPG";
        if (fileName != null && fileName.toLowerCase().endsWith(".png")) return "PNG";
        return "FILE";
    }

    @Override
    public int getItemCount() {
        return documentList.size();
    }

    static class DocumentViewHolder extends RecyclerView.ViewHolder {

        TextView tvDocIcon;
        TextView tvDocDisplayName;
        TextView tvDocCategory;
        TextView tvDocLinkedEntity;
        TextView tvDocFileSize;
        TextView tvDocDate;
        TextView tvDocFormatBadge;
        MaterialButton btnDocOptions;

        public DocumentViewHolder(@NonNull View itemView) {
            super(itemView);
            tvDocIcon = itemView.findViewById(R.id.tvDocIcon);
            tvDocDisplayName = itemView.findViewById(R.id.tvDocDisplayName);
            tvDocCategory = itemView.findViewById(R.id.tvDocCategory);
            tvDocLinkedEntity = itemView.findViewById(R.id.tvDocLinkedEntity);
            tvDocFileSize = itemView.findViewById(R.id.tvDocFileSize);
            tvDocDate = itemView.findViewById(R.id.tvDocDate);
            tvDocFormatBadge = itemView.findViewById(R.id.tvDocFormatBadge);
            btnDocOptions = itemView.findViewById(R.id.btnDocOptions);
        }
    }
}

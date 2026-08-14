package com.rob.houserental.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.rob.houserental.R;
import com.rob.houserental.model.TenantDocument;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class TenantDocumentAdapter extends RecyclerView.Adapter<TenantDocumentAdapter.DocumentViewHolder> {

    private final List<TenantDocument> documentList = new ArrayList<>();
    private OnDocumentClickListener listener;
    private static final SimpleDateFormat dateFormat = new SimpleDateFormat("dd MMM yyyy", Locale.getDefault());

    public interface OnDocumentClickListener {
        void onViewClick(TenantDocument document);
        void onDeleteClick(TenantDocument document);
    }

    public void setOnDocumentClickListener(OnDocumentClickListener listener) {
        this.listener = listener;
    }

    public void setDocuments(List<TenantDocument> documents) {
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
                .inflate(R.layout.item_tenant_document, parent, false);
        return new DocumentViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull DocumentViewHolder holder, int position) {
        TenantDocument document = documentList.get(position);

        holder.tvDocumentType.setText(document.getDocumentType());
        holder.tvDocumentName.setText(document.getDisplayName());

        if (document.getCreatedAt() > 0) {
            holder.tvDocumentDate.setText(dateFormat.format(new Date(document.getCreatedAt())));
            holder.tvDocumentDate.setVisibility(View.VISIBLE);
        } else {
            holder.tvDocumentDate.setVisibility(View.GONE);
        }

        holder.btnViewDocument.setOnClickListener(v -> {
            if (listener != null) {
                listener.onViewClick(document);
            }
        });

        holder.btnDeleteDocument.setOnClickListener(v -> {
            if (listener != null) {
                listener.onDeleteClick(document);
            }
        });
    }

    @Override
    public int getItemCount() {
        return documentList.size();
    }

    static class DocumentViewHolder extends RecyclerView.ViewHolder {

        TextView tvDocumentType;
        TextView tvDocumentDate;
        TextView tvDocumentName;
        MaterialButton btnViewDocument;
        MaterialButton btnDeleteDocument;

        public DocumentViewHolder(@NonNull View itemView) {
            super(itemView);
            tvDocumentType = itemView.findViewById(R.id.tvDocumentType);
            tvDocumentDate = itemView.findViewById(R.id.tvDocumentDate);
            tvDocumentName = itemView.findViewById(R.id.tvDocumentName);
            btnViewDocument = itemView.findViewById(R.id.btnViewDocument);
            btnDeleteDocument = itemView.findViewById(R.id.btnDeleteDocument);
        }
    }
}

package com.rob.houserental.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.rob.houserental.R;
import com.rob.houserental.model.AppDocument;

import java.util.ArrayList;
import java.util.List;

public class AppDocumentAdapter extends RecyclerView.Adapter<AppDocumentAdapter.DocumentViewHolder> {

    private final List<AppDocument> documentList = new ArrayList<>();
    private OnDocumentClickListener listener;

    public interface OnDocumentClickListener {
        void onDocumentClick(AppDocument doc);
        void onDeleteClick(AppDocument doc);
    }

    public void setOnDocumentClickListener(OnDocumentClickListener listener) {
        this.listener = listener;
    }

    public void setDocuments(List<AppDocument> documents) {
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
        AppDocument doc = documentList.get(position);

        String icon = "";
        if (doc.getMimeType() != null) {
            if (doc.getMimeType().contains("pdf")) icon = "";
            else if (doc.getMimeType().contains("image")) icon = "";
        }

        holder.tvIcon.setText(icon);
        holder.tvTitle.setText(doc.getDisplayName() != null ? doc.getDisplayName() : doc.getFileName());

        String details = (doc.getDocumentType() != null ? doc.getDocumentType() : "");
        holder.tvSub.setText(details);

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) listener.onDocumentClick(doc);
        });

        if (holder.btnMenu != null) {
            holder.btnMenu.setOnClickListener(v -> {
                if (listener != null) listener.onDeleteClick(doc);
            });
        }
    }

    @Override
    public int getItemCount() {
        return documentList.size();
    }

    static class DocumentViewHolder extends RecyclerView.ViewHolder {
        TextView tvIcon, tvTitle, tvSub;
        MaterialButton btnMenu;

        public DocumentViewHolder(@NonNull View itemView) {
            super(itemView);
            tvIcon = itemView.findViewById(R.id.tvDocIcon);
            tvTitle = itemView.findViewById(R.id.tvDocDisplayName);
            tvSub = itemView.findViewById(R.id.tvDocCategory);
            btnMenu = itemView.findViewById(R.id.btnDocOptions);
        }
    }
}

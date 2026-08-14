package com.rob.houserental.adapter;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.rob.houserental.R;
import com.rob.houserental.model.BackupHistory;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class BackupHistoryAdapter extends RecyclerView.Adapter<BackupHistoryAdapter.HistoryViewHolder> {

    private final List<BackupHistory> historyList = new ArrayList<>();
    private static final SimpleDateFormat dateFormat = new SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault());
    private static final DecimalFormat sizeFormat = new DecimalFormat("#,##0.#");

    public void setHistory(List<BackupHistory> history) {
        historyList.clear();
        if (history != null) {
            historyList.addAll(history);
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public HistoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_backup_history, parent, false);
        return new HistoryViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull HistoryViewHolder holder, int position) {
        BackupHistory item = historyList.get(position);
        Context context = holder.itemView.getContext();

        holder.tvBackupIcon.setText(getIconForType(item.getBackupType()));
        holder.tvBackupType.setText(getLocalizedType(context, item.getBackupType()));

        String status = item.getStatus() != null ? item.getStatus() : "SUCCESS";
        holder.tvBackupStatusBadge.setText(getLocalizedStatus(context, status));

        if ("SUCCESS".equalsIgnoreCase(status)) {
            holder.tvBackupStatusBadge.setTextColor(Color.parseColor("#2E7D32"));
        } else if ("FAILED".equalsIgnoreCase(status)) {
            holder.tvBackupStatusBadge.setTextColor(Color.parseColor("#C62828"));
        } else if ("RESTORED".equalsIgnoreCase(status)) {
            holder.tvBackupStatusBadge.setTextColor(Color.parseColor("#1565C0"));
        } else if ("LOCAL_ONLY".equalsIgnoreCase(status)) {
            holder.tvBackupStatusBadge.setTextColor(Color.parseColor("#EF6C00"));
        } else {
            holder.tvBackupStatusBadge.setTextColor(Color.parseColor("#EF6C00"));
        }

        long time = item.getCompletedAt() > 0 ? item.getCompletedAt() : item.getCreatedAt();
        holder.tvBackupDate.setText("📅 " + (time > 0 ? dateFormat.format(new Date(time)) : ""));

        holder.tvBackupSize.setText("💾 " + formatFileSize(item.getSizeBytes()));

        if (item.getErrorMessage() != null && !item.getErrorMessage().isEmpty() && "FAILED".equalsIgnoreCase(status)) {
            holder.tvBackupError.setVisibility(View.VISIBLE);
            holder.tvBackupError.setText(context.getString(R.string.prefix_error, item.getErrorMessage()));
        } else {
            holder.tvBackupError.setVisibility(View.GONE);
        }
    }

    private String getIconForType(String type) {
        if (type == null) return "☁️";
        switch (type.toUpperCase()) {
            case "AUTOMATIC":
                return "⏰";
            case "RESTORE":
                return "🔄";
            case "EXPORT":
                return "💾";
            case "IMPORT":
                return "📥";
            case "MANUAL":
            default:
                return "☁️";
        }
    }

    private String getLocalizedType(Context context, String type) {
        if (type == null) return context.getString(R.string.type_manual);
        switch (type.toUpperCase()) {
            case "AUTOMATIC":
                return context.getString(R.string.type_automatic) + " " + context.getString(R.string.backup_and_restore_title);
            case "RESTORE":
                return context.getString(R.string.type_restore);
            case "EXPORT":
                return context.getString(R.string.type_export);
            case "IMPORT":
                return context.getString(R.string.type_import);
            case "MANUAL":
            default:
                return context.getString(R.string.type_manual) + " " + context.getString(R.string.backup_and_restore_title);
        }
    }

    private String getLocalizedStatus(Context context, String status) {
        if (status == null) return context.getString(R.string.status_success);
        switch (status.toUpperCase()) {
            case "LOCAL_ONLY":
                return context.getString(R.string.status_local_only);
            case "FAILED":
                return context.getString(R.string.status_failed);
            case "RESTORED":
                return context.getString(R.string.status_restored);
            case "IN_PROGRESS":
                return context.getString(R.string.status_in_progress);
            case "SUCCESS":
            default:
                return context.getString(R.string.status_success);
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

    @Override
    public int getItemCount() {
        return historyList.size();
    }

    static class HistoryViewHolder extends RecyclerView.ViewHolder {
        TextView tvBackupIcon;
        TextView tvBackupType;
        TextView tvBackupStatusBadge;
        TextView tvBackupDate;
        TextView tvBackupSize;
        TextView tvBackupError;

        public HistoryViewHolder(@NonNull View itemView) {
            super(itemView);
            tvBackupIcon = itemView.findViewById(R.id.tvBackupIcon);
            tvBackupType = itemView.findViewById(R.id.tvBackupType);
            tvBackupStatusBadge = itemView.findViewById(R.id.tvBackupStatusBadge);
            tvBackupDate = itemView.findViewById(R.id.tvBackupDate);
            tvBackupSize = itemView.findViewById(R.id.tvBackupSize);
            tvBackupError = itemView.findViewById(R.id.tvBackupError);
        }
    }
}

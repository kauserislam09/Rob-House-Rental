package com.rob.houserental.adapter;

import android.content.Context;
import android.graphics.drawable.GradientDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.rob.houserental.R;
import com.rob.houserental.model.MaintenanceRecord;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

public class MaintenanceAdapter extends RecyclerView.Adapter<MaintenanceAdapter.ViewHolder> {

    private final List<MaintenanceRecord> items = new ArrayList<>();
    private OnMaintenanceClickListener listener;
    private static final DecimalFormat currencyFormat = new DecimalFormat("#,##,###.##");

    public interface OnMaintenanceClickListener {
        void onMaintenanceClick(MaintenanceRecord record);
    }

    public void setOnMaintenanceClickListener(OnMaintenanceClickListener listener) {
        this.listener = listener;
    }

    public void setItems(List<MaintenanceRecord> list) {
        items.clear();
        if (list != null) {
            items.addAll(list);
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_maintenance, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        MaintenanceRecord record = items.get(position);
        Context context = holder.itemView.getContext();

        holder.tvTitle.setText(record.getTitle() != null ? record.getTitle() : "");

        String catDisplay = getCategoryDisplay(context, record.getCategory());
        holder.tvCategory.setText(catDisplay);

        String priority = record.getPriority() != null ? record.getPriority().toUpperCase() : "MEDIUM";
        holder.tvPriorityBadge.setText(getPriorityDisplay(context, priority));
        applyPriorityBadgeStyle(context, holder.tvPriorityBadge, priority);

        String status = record.getStatus() != null ? record.getStatus().toUpperCase() : "OPEN";
        holder.tvStatusBadge.setText(getStatusDisplay(context, status));
        applyStatusBadgeStyle(context, holder.tvStatusBadge, status);

        if (record.getScheduledDate() != null && !record.getScheduledDate().isEmpty()) {
            holder.tvScheduledDate.setText(context.getString(R.string.scheduled_label) + ": " + record.getScheduledDate());
            holder.tvScheduledDate.setVisibility(View.VISIBLE);
        } else {
            holder.tvScheduledDate.setVisibility(View.GONE);
        }

        String curr = context.getString(R.string.currency_symbol);
        double cost = record.getActualCost() > 0 ? record.getActualCost() : record.getEstimatedCost();
        if (cost > 0) {
            holder.tvCost.setText(curr + currencyFormat.format(cost));
            holder.tvCost.setVisibility(View.VISIBLE);
        } else {
            holder.tvCost.setVisibility(View.GONE);
        }

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onMaintenanceClick(record);
            }
        });
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public static String getCategoryDisplay(Context context, String category) {
        if (category == null) return context.getString(R.string.category_other);
        switch (category.toUpperCase()) {
            case "PLUMBING": return context.getString(R.string.category_plumbing);
            case "ELECTRICAL": return context.getString(R.string.category_electrical);
            case "PAINTING": return context.getString(R.string.category_painting);
            case "AC": return context.getString(R.string.category_ac);
            case "APPLIANCE": return context.getString(R.string.category_appliance);
            case "CLEANING": return context.getString(R.string.category_cleaning);
            case "STRUCTURAL": return context.getString(R.string.category_structural);
            case "SECURITY": return context.getString(R.string.category_security);
            case "WATER": return context.getString(R.string.category_water);
            case "GAS": return context.getString(R.string.category_gas);
            default: return context.getString(R.string.category_other);
        }
    }

    public static String getPriorityDisplay(Context context, String priority) {
        if (priority == null) return context.getString(R.string.priority_medium);
        switch (priority.toUpperCase()) {
            case "LOW": return context.getString(R.string.priority_low);
            case "HIGH": return context.getString(R.string.priority_high);
            case "URGENT": return context.getString(R.string.priority_urgent);
            case "MEDIUM":
            default: return context.getString(R.string.priority_medium);
        }
    }

    public static String getStatusDisplay(Context context, String status) {
        if (status == null) return context.getString(R.string.status_maint_open);
        switch (status.toUpperCase()) {
            case "SCHEDULED": return context.getString(R.string.status_maint_scheduled);
            case "IN_PROGRESS": return context.getString(R.string.status_maint_in_progress);
            case "COMPLETED": return context.getString(R.string.status_maint_completed);
            case "CANCELLED": return context.getString(R.string.status_maint_cancelled);
            case "OPEN":
            default: return context.getString(R.string.status_maint_open);
        }
    }

    private void applyPriorityBadgeStyle(Context context, TextView badge, String priority) {
        int bgColor;
        int textColor;
        switch (priority) {
            case "URGENT":
            case "HIGH":
                bgColor = ContextCompat.getColor(context, R.color.status_overdue_bg);
                textColor = ContextCompat.getColor(context, R.color.status_overdue_text);
                break;
            case "LOW":
                bgColor = ContextCompat.getColor(context, R.color.status_vacant_bg);
                textColor = ContextCompat.getColor(context, R.color.status_vacant_text);
                break;
            case "MEDIUM":
            default:
                bgColor = ContextCompat.getColor(context, R.color.status_partial_bg);
                textColor = ContextCompat.getColor(context, R.color.status_partial_text);
                break;
        }
        GradientDrawable shape = new GradientDrawable();
        shape.setCornerRadius(20f);
        shape.setColor(bgColor);
        badge.setBackground(shape);
        badge.setTextColor(textColor);
    }

    private void applyStatusBadgeStyle(Context context, TextView badge, String status) {
        int bgColor;
        int textColor;
        switch (status) {
            case "COMPLETED":
                bgColor = ContextCompat.getColor(context, R.color.status_paid_bg);
                textColor = ContextCompat.getColor(context, R.color.status_paid_text);
                break;
            case "IN_PROGRESS":
            case "SCHEDULED":
                bgColor = ContextCompat.getColor(context, R.color.status_partial_bg);
                textColor = ContextCompat.getColor(context, R.color.status_partial_text);
                break;
            case "CANCELLED":
                bgColor = ContextCompat.getColor(context, R.color.status_unpaid_bg);
                textColor = ContextCompat.getColor(context, R.color.status_unpaid_text);
                break;
            case "OPEN":
            default:
                bgColor = ContextCompat.getColor(context, R.color.status_vacant_bg);
                textColor = ContextCompat.getColor(context, R.color.status_vacant_text);
                break;
        }
        GradientDrawable shape = new GradientDrawable();
        shape.setCornerRadius(20f);
        shape.setColor(bgColor);
        badge.setBackground(shape);
        badge.setTextColor(textColor);
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitle;
        TextView tvPriorityBadge;
        TextView tvPropertyUnit;
        TextView tvCategory;
        TextView tvStatusBadge;
        TextView tvScheduledDate;
        TextView tvCost;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tvMaintenanceTitle);
            tvPriorityBadge = itemView.findViewById(R.id.tvMaintenancePriorityBadge);
            tvPropertyUnit = itemView.findViewById(R.id.tvMaintenancePropertyUnit);
            tvCategory = itemView.findViewById(R.id.tvMaintenanceCategory);
            tvStatusBadge = itemView.findViewById(R.id.tvMaintenanceStatusBadge);
            tvScheduledDate = itemView.findViewById(R.id.tvMaintenanceScheduledDate);
            tvCost = itemView.findViewById(R.id.tvMaintenanceCost);
        }
    }
}

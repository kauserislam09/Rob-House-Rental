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
import com.rob.houserental.model.RentRecordDisplayItem;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class RentAdapter extends RecyclerView.Adapter<RentAdapter.RentViewHolder> {

    private final List<RentRecordDisplayItem> rentList = new ArrayList<>();
    private OnRentClickListener listener;
    private static final NumberFormat currencyFormatter = NumberFormat.getNumberInstance(Locale.getDefault());

    public interface OnRentClickListener {
        void onRentClick(RentRecordDisplayItem item);
    }

    public void setOnRentClickListener(OnRentClickListener listener) {
        this.listener = listener;
    }

    public void setRentRecords(List<RentRecordDisplayItem> records) {
        rentList.clear();
        if (records != null) {
            rentList.addAll(records);
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public RentViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_rent_record, parent, false);
        return new RentViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RentViewHolder holder, int position) {
        RentRecordDisplayItem item = rentList.get(position);
        Context context = holder.itemView.getContext();

        holder.tvRentTenantName.setText(item.tenantName != null ? item.tenantName : context.getString(R.string.tenant_label));

        // Status Badge
        String status = item.status != null ? item.status.trim().toUpperCase() : "UNPAID";
        holder.tvRentStatusBadge.setText(getStatusDisplay(context, status));
        applyStatusBadgeStyle(context, holder.tvRentStatusBadge, status);

        // Property & Unit
        String propName = item.propertyName != null ? item.propertyName : context.getString(R.string.property_label);
        String propUnit = item.unitNumber != null && !item.unitNumber.isEmpty()
                ? propName + " • " + context.getString(R.string.prefix_unit_format, item.unitNumber)
                : propName;
        holder.tvRentPropertyUnit.setText("🏢 " + propUnit);

        // Month
        holder.tvRentBillingMonth.setText("📅 " + (item.billingMonth != null ? item.billingMonth : ""));

        // Amounts
        String curr = context.getString(R.string.currency_symbol);
        holder.tvRentDueAmount.setText(curr + currencyFormatter.format(item.amountDue));
        holder.tvRentPaidAmount.setText(curr + currencyFormatter.format(item.amountPaid));
        holder.tvRentRemainingAmount.setText(curr + currencyFormatter.format(item.remainingAmount));

        // Footer Date
        if ("PAID".equalsIgnoreCase(status) && item.lastPaymentDate != null && !item.lastPaymentDate.isEmpty()) {
            holder.tvRentFooterDate.setText(context.getString(R.string.prefix_paid_format, item.lastPaymentDate));
        } else if (item.dueDate != null && !item.dueDate.isEmpty()) {
            holder.tvRentFooterDate.setText(context.getString(R.string.prefix_due_format, item.dueDate));
        } else {
            holder.tvRentFooterDate.setText("");
        }

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onRentClick(item);
            }
        });
    }

    private void applyStatusBadgeStyle(Context context, TextView badge, String status) {
        int bgColor;
        int textColor;

        switch (status) {
            case "PAID":
                bgColor = ContextCompat.getColor(context, R.color.status_vacant_bg);
                textColor = ContextCompat.getColor(context, R.color.status_vacant_text);
                break;
            case "PARTIAL":
                bgColor = ContextCompat.getColor(context, R.color.status_reserved_bg);
                textColor = ContextCompat.getColor(context, R.color.status_reserved_text);
                break;
            case "OVERDUE":
                bgColor = ContextCompat.getColor(context, R.color.status_maintenance_bg);
                textColor = ContextCompat.getColor(context, R.color.status_maintenance_text);
                break;
            case "WAIVED":
                bgColor = ContextCompat.getColor(context, R.color.status_reserved_bg);
                textColor = ContextCompat.getColor(context, R.color.status_reserved_text);
                break;
            case "UNPAID":
            default:
                bgColor = ContextCompat.getColor(context, R.color.status_maintenance_bg);
                textColor = ContextCompat.getColor(context, R.color.status_maintenance_text);
                break;
        }

        GradientDrawable shape = new GradientDrawable();
        shape.setCornerRadius(24f);
        shape.setColor(bgColor);
        badge.setBackground(shape);
        badge.setTextColor(textColor);
    }

    public static String getStatusDisplay(Context context, String status) {
        if (status == null) return context.getString(R.string.status_unpaid);
        switch (status.trim().toUpperCase()) {
            case "PAID":
                return context.getString(R.string.status_paid);
            case "PARTIAL":
                return context.getString(R.string.status_partial);
            case "OVERDUE":
                return context.getString(R.string.status_overdue);
            case "WAIVED":
                return context.getString(R.string.status_waived);
            case "UNPAID":
            default:
                return context.getString(R.string.status_unpaid);
        }
    }

    @Override
    public int getItemCount() {
        return rentList.size();
    }

    static class RentViewHolder extends RecyclerView.ViewHolder {

        TextView tvRentTenantName;
        TextView tvRentStatusBadge;
        TextView tvRentPropertyUnit;
        TextView tvRentBillingMonth;
        TextView tvRentDueAmount;
        TextView tvRentPaidAmount;
        TextView tvRentRemainingAmount;
        TextView tvRentFooterDate;

        public RentViewHolder(@NonNull View itemView) {
            super(itemView);
            tvRentTenantName = itemView.findViewById(R.id.tvRentTenantName);
            tvRentStatusBadge = itemView.findViewById(R.id.tvRentStatusBadge);
            tvRentPropertyUnit = itemView.findViewById(R.id.tvRentPropertyUnit);
            tvRentBillingMonth = itemView.findViewById(R.id.tvRentBillingMonth);
            tvRentDueAmount = itemView.findViewById(R.id.tvRentDueAmount);
            tvRentPaidAmount = itemView.findViewById(R.id.tvRentPaidAmount);
            tvRentRemainingAmount = itemView.findViewById(R.id.tvRentRemainingAmount);
            tvRentFooterDate = itemView.findViewById(R.id.tvRentFooterDate);
        }
    }
}

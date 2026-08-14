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
import com.rob.houserental.model.UtilityBillDisplayItem;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class UtilityBillAdapter extends RecyclerView.Adapter<UtilityBillAdapter.BillViewHolder> {

    private final List<UtilityBillDisplayItem> billList = new ArrayList<>();
    private OnBillClickListener listener;
    private static final NumberFormat currencyFormatter = NumberFormat.getNumberInstance(Locale.getDefault());

    public interface OnBillClickListener {
        void onBillClick(UtilityBillDisplayItem item);
    }

    public void setOnBillClickListener(OnBillClickListener listener) {
        this.listener = listener;
    }

    public void setBills(List<UtilityBillDisplayItem> bills) {
        billList.clear();
        if (bills != null) {
            billList.addAll(bills);
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public BillViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_utility_bill, parent, false);
        return new BillViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull BillViewHolder holder, int position) {
        UtilityBillDisplayItem item = billList.get(position);
        Context context = holder.itemView.getContext();

        // Icon and Title
        String type = item.billType != null ? item.billType : "OTHER";
        holder.tvBillTypeIcon.setText(getTypeIcon(type));
        holder.tvBillTypeTitle.setText(getTypeTitle(context, type));

        // Status Badge
        String status = item.status != null ? item.status.trim().toUpperCase() : "UNPAID";
        holder.tvBillStatusBadge.setText(getStatusDisplay(context, status));
        applyStatusBadgeStyle(context, holder.tvBillStatusBadge, status);

        // Property & Unit
        String propName = item.propertyName != null ? item.propertyName : context.getString(R.string.property_label);
        if (item.unitNumber != null && !item.unitNumber.isEmpty()) {
            holder.tvBillPropertyUnit.setText(propName + " • " + context.getString(R.string.prefix_unit_format, item.unitNumber));
        } else {
            holder.tvBillPropertyUnit.setText(propName + " (" + context.getString(R.string.whole_property_option) + ")");
        }

        // Tenant
        if (item.tenantName != null && !item.tenantName.isEmpty()) {
            holder.tvBillTenant.setText(item.tenantName);
            holder.tvBillTenant.setVisibility(View.VISIBLE);
        } else {
            holder.tvBillTenant.setVisibility(View.GONE);
        }

        // Month & Due Date
        String month = item.billingMonth != null ? item.billingMonth : "";
        String due = item.dueDate != null ? " • " + context.getString(R.string.prefix_due_format, item.dueDate) : "";
        holder.tvBillMonthAndDue.setText(month + due);

        // Readings if electricity or water
        if (item.unitsConsumed > 0 || item.currentReading > 0) {
            holder.tvBillReadingSnippet.setText(context.getString(R.string.prefix_units_summary_format, item.unitsConsumed, item.previousReading, item.currentReading));
            holder.tvBillReadingSnippet.setVisibility(View.VISIBLE);
        } else if (item.meterNumber != null && !item.meterNumber.isEmpty()) {
            holder.tvBillReadingSnippet.setText(context.getString(R.string.prefix_meter_format, item.meterNumber));
            holder.tvBillReadingSnippet.setVisibility(View.VISIBLE);
        } else {
            holder.tvBillReadingSnippet.setVisibility(View.GONE);
        }

        // Amounts
        String curr = context.getString(R.string.currency_symbol);
        holder.tvBillDueAmount.setText(curr + currencyFormatter.format(item.amountDue));
        holder.tvBillPaidAmount.setText(curr + currencyFormatter.format(item.amountPaid));
        holder.tvBillRemainingAmount.setText(curr + currencyFormatter.format(item.remainingAmount));

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onBillClick(item);
            }
        });
    }

    private String getTypeIcon(String type) {
        switch (type.toUpperCase()) {
            case "ELECTRICITY":
                return "";
            case "WATER":
                return "";
            case "GAS":
                return "";
            case "INTERNET":
                return "";
            case "SERVICE_CHARGE":
                return "";
            case "OTHER":
            default:
                return "";
        }
    }

    private String getTypeTitle(Context context, String type) {
        switch (type.toUpperCase()) {
            case "ELECTRICITY":
                return context.getString(R.string.bill_type_electricity);
            case "WATER":
                return context.getString(R.string.bill_type_water);
            case "GAS":
                return context.getString(R.string.bill_type_gas);
            case "INTERNET":
                return context.getString(R.string.bill_type_internet);
            case "SERVICE_CHARGE":
                return context.getString(R.string.bill_type_service);
            case "OTHER":
            default:
                return context.getString(R.string.bill_type_other);
        }
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
        return billList.size();
    }

    static class BillViewHolder extends RecyclerView.ViewHolder {

        TextView tvBillTypeIcon;
        TextView tvBillTypeTitle;
        TextView tvBillStatusBadge;
        TextView tvBillPropertyUnit;
        TextView tvBillTenant;
        TextView tvBillMonthAndDue;
        TextView tvBillReadingSnippet;
        TextView tvBillDueAmount;
        TextView tvBillPaidAmount;
        TextView tvBillRemainingAmount;

        public BillViewHolder(@NonNull View itemView) {
            super(itemView);
            tvBillTypeIcon = itemView.findViewById(R.id.tvBillTypeIcon);
            tvBillTypeTitle = itemView.findViewById(R.id.tvBillTypeTitle);
            tvBillStatusBadge = itemView.findViewById(R.id.tvBillStatusBadge);
            tvBillPropertyUnit = itemView.findViewById(R.id.tvBillPropertyUnit);
            tvBillTenant = itemView.findViewById(R.id.tvBillTenant);
            tvBillMonthAndDue = itemView.findViewById(R.id.tvBillMonthAndDue);
            tvBillReadingSnippet = itemView.findViewById(R.id.tvBillReadingSnippet);
            tvBillDueAmount = itemView.findViewById(R.id.tvBillDueAmount);
            tvBillPaidAmount = itemView.findViewById(R.id.tvBillPaidAmount);
            tvBillRemainingAmount = itemView.findViewById(R.id.tvBillRemainingAmount);
        }
    }
}

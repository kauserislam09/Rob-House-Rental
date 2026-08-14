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
import com.rob.houserental.model.TenancyWithDetails;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class TenancyAdapter extends RecyclerView.Adapter<TenancyAdapter.TenancyViewHolder> {

    private final List<TenancyWithDetails> tenancyList = new ArrayList<>();
    private OnTenancyClickListener listener;
    private static final NumberFormat currencyFormatter = NumberFormat.getNumberInstance(Locale.getDefault());

    public interface OnTenancyClickListener {
        void onTenancyClick(TenancyWithDetails details);
    }

    public void setOnTenancyClickListener(OnTenancyClickListener listener) {
        this.listener = listener;
    }

    public void setTenancies(List<TenancyWithDetails> tenancies) {
        tenancyList.clear();
        if (tenancies != null) {
            tenancyList.addAll(tenancies);
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public TenancyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_tenancy, parent, false);
        return new TenancyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TenancyViewHolder holder, int position) {
        TenancyWithDetails details = tenancyList.get(position);
        Context context = holder.itemView.getContext();

        // Tenant Name
        if (details.tenant != null) {
            holder.tvTenancyTenantName.setText(details.tenant.getFullName());
        } else {
            holder.tvTenancyTenantName.setText(context.getString(R.string.prefix_tenant_hash, details.tenancy.getTenantId()));
        }

        // Status Badge
        String status = details.tenancy.getStatus() != null ? details.tenancy.getStatus().trim().toUpperCase() : "ACTIVE";
        holder.tvTenancyStatusBadge.setText(getStatusDisplay(context, status));
        applyStatusBadgeStyle(context, holder.tvTenancyStatusBadge, status);

        // Property & Unit
        String unitText = context.getString(R.string.prefix_unit_hash, details.tenancy.getUnitId());
        if (details.unit != null) {
            if (details.unit.getFloor() > 0) {
                unitText = context.getString(R.string.prefix_unit_floor_format, details.unit.getUnitNumber(), details.unit.getFloor());
            } else {
                unitText = context.getString(R.string.prefix_unit_format, details.unit.getUnitNumber());
            }
        }
        holder.tvTenancyPropertyUnit.setText("🚪 " + unitText);

        // Rent
        String rentFormatted = currencyFormatter.format(details.tenancy.getMonthlyRent());
        holder.tvTenancyRent.setText(context.getString(R.string.prefix_rent_format, context.getString(R.string.currency_symbol) + rentFormatted, context.getString(R.string.per_month)));

        // Dates
        String startDate = details.tenancy.getStartDate() != null ? details.tenancy.getStartDate() : context.getString(R.string.not_set);
        String dates;
        if ("ENDED".equalsIgnoreCase(status) && details.tenancy.getEndDate() != null && !details.tenancy.getEndDate().isEmpty()) {
            dates = context.getString(R.string.prefix_started_ended_format, startDate, details.tenancy.getEndDate());
        } else {
            dates = context.getString(R.string.prefix_started_format, startDate);
        }
        holder.tvTenancyDates.setText(dates);

        // Agreement Number
        String agreement = details.tenancy.getAgreementNumber();
        if (agreement != null && !agreement.trim().isEmpty()) {
            holder.tvTenancyAgreementNumber.setText(agreement.trim());
            holder.tvTenancyAgreementNumber.setVisibility(View.VISIBLE);
        } else {
            holder.tvTenancyAgreementNumber.setVisibility(View.GONE);
        }

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onTenancyClick(details);
            }
        });
    }

    private void applyStatusBadgeStyle(Context context, TextView badge, String status) {
        int bgColor;
        int textColor;

        switch (status) {
            case "ENDED":
                bgColor = ContextCompat.getColor(context, R.color.status_reserved_bg);
                textColor = ContextCompat.getColor(context, R.color.status_reserved_text);
                break;
            case "CANCELLED":
                bgColor = ContextCompat.getColor(context, R.color.status_maintenance_bg);
                textColor = ContextCompat.getColor(context, R.color.status_maintenance_text);
                break;
            case "ACTIVE":
            default:
                bgColor = ContextCompat.getColor(context, R.color.status_vacant_bg);
                textColor = ContextCompat.getColor(context, R.color.status_vacant_text);
                break;
        }

        GradientDrawable shape = new GradientDrawable();
        shape.setCornerRadius(24f);
        shape.setColor(bgColor);
        badge.setBackground(shape);
        badge.setTextColor(textColor);
    }

    public static String getStatusDisplay(Context context, String status) {
        if (status == null) return context.getString(R.string.status_active);
        switch (status.trim().toUpperCase()) {
            case "ENDED":
                return context.getString(R.string.status_ended);
            case "CANCELLED":
                return context.getString(R.string.status_cancelled);
            case "ACTIVE":
            default:
                return context.getString(R.string.status_active);
        }
    }

    @Override
    public int getItemCount() {
        return tenancyList.size();
    }

    static class TenancyViewHolder extends RecyclerView.ViewHolder {

        TextView tvTenancyTenantName;
        TextView tvTenancyStatusBadge;
        TextView tvTenancyPropertyUnit;
        TextView tvTenancyRent;
        TextView tvTenancyDates;
        TextView tvTenancyAgreementNumber;

        public TenancyViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTenancyTenantName = itemView.findViewById(R.id.tvTenancyTenantName);
            tvTenancyStatusBadge = itemView.findViewById(R.id.tvTenancyStatusBadge);
            tvTenancyPropertyUnit = itemView.findViewById(R.id.tvTenancyPropertyUnit);
            tvTenancyRent = itemView.findViewById(R.id.tvTenancyRent);
            tvTenancyDates = itemView.findViewById(R.id.tvTenancyDates);
            tvTenancyAgreementNumber = itemView.findViewById(R.id.tvTenancyAgreementNumber);
        }
    }
}

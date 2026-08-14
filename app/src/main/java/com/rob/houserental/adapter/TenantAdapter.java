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
import com.rob.houserental.model.Tenant;

import java.util.ArrayList;
import java.util.List;

public class TenantAdapter extends RecyclerView.Adapter<TenantAdapter.TenantViewHolder> {

    private final List<Tenant> tenantList = new ArrayList<>();
    private OnTenantClickListener listener;

    public interface OnTenantClickListener {
        void onTenantClick(Tenant tenant);
    }

    public void setOnTenantClickListener(OnTenantClickListener listener) {
        this.listener = listener;
    }

    public void setTenants(List<Tenant> tenants) {
        tenantList.clear();
        if (tenants != null) {
            tenantList.addAll(tenants);
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public TenantViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_tenant, parent, false);
        return new TenantViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TenantViewHolder holder, int position) {
        Tenant tenant = tenantList.get(position);
        Context context = holder.itemView.getContext();

        holder.tvTenantName.setText(tenant.getFullName());

        // Status Badge
        String status = tenant.getStatus() != null ? tenant.getStatus().trim().toUpperCase() : "ACTIVE";
        holder.tvTenantStatusBadge.setText(getStatusDisplay(context, status));
        applyStatusBadgeStyle(context, holder.tvTenantStatusBadge, status);

        // Phone
        if (tenant.getPhoneNumber() != null && !tenant.getPhoneNumber().isEmpty()) {
            holder.tvTenantPhone.setText("📞  " + tenant.getPhoneNumber());
            holder.tvTenantPhone.setVisibility(View.VISIBLE);
        } else {
            holder.tvTenantPhone.setVisibility(View.GONE);
        }

        // Occupation
        if (tenant.getOccupation() != null && !tenant.getOccupation().trim().isEmpty()) {
            holder.tvTenantOccupation.setText(context.getString(R.string.occupation) + ": " + tenant.getOccupation().trim());
            holder.tvTenantOccupation.setVisibility(View.VISIBLE);
        } else {
            holder.tvTenantOccupation.setVisibility(View.GONE);
        }

        // NID
        String nid = tenant.getNidNumber();
        if (nid != null && !nid.trim().isEmpty()) {
            holder.tvTenantNid.setText(context.getString(R.string.nid_label) + ": " + nid.trim());
            holder.tvTenantNid.setVisibility(View.VISIBLE);
        } else {
            holder.tvTenantNid.setVisibility(View.GONE);
        }

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onTenantClick(tenant);
            }
        });
    }

    private void applyStatusBadgeStyle(Context context, TextView badge, String status) {
        int bgColor;
        int textColor;

        switch (status) {
            case "INACTIVE":
                bgColor = ContextCompat.getColor(context, R.color.status_maintenance_bg);
                textColor = ContextCompat.getColor(context, R.color.status_maintenance_text);
                break;
            case "ARCHIVED":
                bgColor = ContextCompat.getColor(context, R.color.status_reserved_bg);
                textColor = ContextCompat.getColor(context, R.color.status_reserved_text);
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
            case "INACTIVE":
                return context.getString(R.string.status_inactive);
            case "ARCHIVED":
                return context.getString(R.string.status_archived);
            case "ACTIVE":
            default:
                return context.getString(R.string.status_active);
        }
    }

    @Override
    public int getItemCount() {
        return tenantList.size();
    }

    static class TenantViewHolder extends RecyclerView.ViewHolder {

        TextView tvTenantName;
        TextView tvTenantStatusBadge;
        TextView tvTenantPhone;
        TextView tvTenantOccupation;
        TextView tvTenantNid;

        public TenantViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTenantName = itemView.findViewById(R.id.tvTenantName);
            tvTenantStatusBadge = itemView.findViewById(R.id.tvTenantStatusBadge);
            tvTenantPhone = itemView.findViewById(R.id.tvTenantPhone);
            tvTenantOccupation = itemView.findViewById(R.id.tvTenantOccupation);
            tvTenantNid = itemView.findViewById(R.id.tvTenantNid);
        }
    }
}

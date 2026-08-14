package com.rob.houserental.adapter;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.drawable.GradientDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.rob.houserental.R;
import com.rob.houserental.model.Unit;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

public class UnitAdapter extends RecyclerView.Adapter<UnitAdapter.UnitViewHolder> {

    private final List<Unit> unitList = new ArrayList<>();
    private OnUnitActionListener listener;
    private static final DecimalFormat currencyFormat = new DecimalFormat("#,##,###.##");

    public interface OnUnitActionListener {
        void onUnitClick(Unit unit);
        void onEditClick(Unit unit);
        void onStatusClick(Unit unit);
        void onDeleteClick(Unit unit);
    }

    public void setOnUnitActionListener(OnUnitActionListener listener) {
        this.listener = listener;
    }

    public void setUnits(List<Unit> units) {
        unitList.clear();
        if (units != null) {
            unitList.addAll(units);
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public UnitViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_unit, parent, false);
        return new UnitViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull UnitViewHolder holder, int position) {
        Unit unit = unitList.get(position);
        Context context = holder.itemView.getContext();

        holder.tvUnitNumber.setText(unit.getUnitNumber());

        // Floor and Type
        String floorText = unit.getFloor() > 0 ? context.getString(R.string.floor) + " " + unit.getFloor() : "";
        String typeText = getUnitTypeDisplay(context, unit.getUnitType());
        if (!floorText.isEmpty() && !typeText.isEmpty()) {
            holder.tvUnitFloorAndType.setText(floorText + "  •  " + typeText);
            holder.tvUnitFloorAndType.setVisibility(View.VISIBLE);
        } else if (!floorText.isEmpty()) {
            holder.tvUnitFloorAndType.setText(floorText);
            holder.tvUnitFloorAndType.setVisibility(View.VISIBLE);
        } else if (!typeText.isEmpty()) {
            holder.tvUnitFloorAndType.setText(typeText);
            holder.tvUnitFloorAndType.setVisibility(View.VISIBLE);
        } else {
            holder.tvUnitFloorAndType.setVisibility(View.GONE);
        }

        // Status Badge
        String status = unit.getStatus() != null ? unit.getStatus().trim().toUpperCase() : "VACANT";
        holder.tvUnitStatusBadge.setText(getStatusDisplay(context, status));
        applyStatusBadgeStyle(context, holder.tvUnitStatusBadge, status);

        // Monthly Rent
        String currencySymbol = context.getString(R.string.currency_symbol);
        String perMonth = context.getString(R.string.per_month);
        String rentFormatted = currencySymbol + " " + currencyFormat.format(unit.getMonthlyRent()) + " " + perMonth;
        holder.tvUnitRent.setText(rentFormatted);

        // Security Deposit
        if (unit.getSecurityDeposit() > 0) {
            String depositText = context.getString(R.string.prefix_deposit_format, currencySymbol + " " + currencyFormat.format(unit.getSecurityDeposit()));
            holder.tvUnitDeposit.setText(depositText);
            holder.tvUnitDeposit.setVisibility(View.VISIBLE);
        } else {
            holder.tvUnitDeposit.setVisibility(View.GONE);
        }

        // Notes
        if (unit.getNotes() != null && !unit.getNotes().trim().isEmpty()) {
            holder.tvUnitNotes.setText(unit.getNotes().trim());
            holder.tvUnitNotes.setVisibility(View.VISIBLE);
        } else {
            holder.tvUnitNotes.setVisibility(View.GONE);
        }

        // Click listeners
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onUnitClick(unit);
            }
        });

        holder.btnEditUnit.setOnClickListener(v -> {
            if (listener != null) {
                listener.onEditClick(unit);
            }
        });

        holder.btnUnitStatus.setOnClickListener(v -> {
            if (listener != null) {
                listener.onStatusClick(unit);
            }
        });

        holder.btnDeleteUnit.setOnClickListener(v -> {
            if (listener != null) {
                listener.onDeleteClick(unit);
            }
        });
    }

    private void applyStatusBadgeStyle(Context context, TextView badge, String status) {
        int bgColor;
        int textColor;

        switch (status) {
            case "OCCUPIED":
                bgColor = ContextCompat.getColor(context, R.color.status_occupied_bg);
                textColor = ContextCompat.getColor(context, R.color.status_occupied_text);
                break;
            case "RESERVED":
                bgColor = ContextCompat.getColor(context, R.color.status_reserved_bg);
                textColor = ContextCompat.getColor(context, R.color.status_reserved_text);
                break;
            case "MAINTENANCE":
                bgColor = ContextCompat.getColor(context, R.color.status_maintenance_bg);
                textColor = ContextCompat.getColor(context, R.color.status_maintenance_text);
                break;
            case "VACANT":
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
        if (status == null) return context.getString(R.string.status_vacant);
        switch (status.trim().toUpperCase()) {
            case "OCCUPIED":
                return context.getString(R.string.status_occupied);
            case "RESERVED":
                return context.getString(R.string.status_reserved);
            case "MAINTENANCE":
                return context.getString(R.string.status_maintenance);
            case "VACANT":
            default:
                return context.getString(R.string.status_vacant);
        }
    }

    public static String getUnitTypeDisplay(Context context, String type) {
        if (type == null) return "";
        String t = type.trim();
        if ("Apartment".equalsIgnoreCase(t)) return context.getString(R.string.unit_type_apartment);
        if ("Flat".equalsIgnoreCase(t)) return context.getString(R.string.unit_type_flat);
        if ("Room".equalsIgnoreCase(t)) return context.getString(R.string.unit_type_room);
        if ("Shop".equalsIgnoreCase(t)) return context.getString(R.string.unit_type_shop);
        if ("Office".equalsIgnoreCase(t)) return context.getString(R.string.unit_type_office);
        if ("Parking".equalsIgnoreCase(t)) return context.getString(R.string.unit_type_parking);
        if ("Other".equalsIgnoreCase(t)) return context.getString(R.string.unit_type_other);
        return t;
    }

    @Override
    public int getItemCount() {
        return unitList.size();
    }

    static class UnitViewHolder extends RecyclerView.ViewHolder {

        TextView tvUnitNumber;
        TextView tvUnitStatusBadge;
        TextView tvUnitFloorAndType;
        TextView tvUnitRent;
        TextView tvUnitDeposit;
        TextView tvUnitNotes;
        MaterialButton btnUnitStatus;
        MaterialButton btnEditUnit;
        MaterialButton btnDeleteUnit;

        public UnitViewHolder(@NonNull View itemView) {
            super(itemView);

            tvUnitNumber = itemView.findViewById(R.id.tvUnitNumber);
            tvUnitStatusBadge = itemView.findViewById(R.id.tvUnitStatusBadge);
            tvUnitFloorAndType = itemView.findViewById(R.id.tvUnitFloorAndType);
            tvUnitRent = itemView.findViewById(R.id.tvUnitRent);
            tvUnitDeposit = itemView.findViewById(R.id.tvUnitDeposit);
            tvUnitNotes = itemView.findViewById(R.id.tvUnitNotes);
            btnUnitStatus = itemView.findViewById(R.id.btnUnitStatus);
            btnEditUnit = itemView.findViewById(R.id.btnEditUnit);
            btnDeleteUnit = itemView.findViewById(R.id.btnDeleteUnit);
        }
    }
}

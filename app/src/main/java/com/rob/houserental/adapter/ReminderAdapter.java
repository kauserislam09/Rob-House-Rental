package com.rob.houserental.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.switchmaterial.SwitchMaterial;
import com.rob.houserental.R;
import com.rob.houserental.model.Reminder;

import java.util.ArrayList;
import java.util.List;

public class ReminderAdapter extends RecyclerView.Adapter<ReminderAdapter.ViewHolder> {

    private final List<Reminder> items = new ArrayList<>();
    private OnReminderActionListener listener;

    public interface OnReminderActionListener {
        void onReminderClick(Reminder reminder);
        void onToggleEnabled(Reminder reminder, boolean enabled);
    }

    public void setOnReminderActionListener(OnReminderActionListener listener) {
        this.listener = listener;
    }

    public void setItems(List<Reminder> list) {
        items.clear();
        if (list != null) {
            items.addAll(list);
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_reminder, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Reminder reminder = items.get(position);
        Context context = holder.itemView.getContext();

        holder.tvTitle.setText(reminder.getTitle() != null ? reminder.getTitle() : "");

        if (reminder.getDescription() != null && !reminder.getDescription().trim().isEmpty()) {
            holder.tvDescription.setText(reminder.getDescription().trim());
            holder.tvDescription.setVisibility(View.VISIBLE);
        } else {
            holder.tvDescription.setVisibility(View.GONE);
        }

        String dtText = (reminder.getReminderDate() != null ? reminder.getReminderDate() : "") +
                (reminder.getReminderTime() != null ? reminder.getReminderTime() : "");
        holder.tvDateTime.setText(dtText);

        holder.tvTypeBadge.setText(getTypeDisplay(context, reminder.getReminderType()));

        holder.switchEnabled.setOnCheckedChangeListener(null);
        holder.switchEnabled.setChecked(reminder.isEnabled());
        holder.switchEnabled.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (listener != null) {
                listener.onToggleEnabled(reminder, isChecked);
            }
        });

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onReminderClick(reminder);
            }
        });
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public static String getTypeDisplay(Context context, String type) {
        if (type == null) return context.getString(R.string.reminder_type_manual);
        switch (type.toUpperCase()) {
            case "RENT_DUE": return context.getString(R.string.reminder_type_rent);
            case "TENANCY_EXPIRY": return context.getString(R.string.reminder_type_tenancy);
            case "BILL_DUE": return context.getString(R.string.reminder_type_bill);
            case "MAINTENANCE": return context.getString(R.string.reminder_type_maintenance);
            case "DOCUMENT_EXPIRY": return context.getString(R.string.reminder_type_document);
            case "MANUAL":
            default: return context.getString(R.string.reminder_type_manual);
        }
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitle;
        TextView tvDescription;
        TextView tvDateTime;
        TextView tvTypeBadge;
        SwitchMaterial switchEnabled;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tvReminderTitle);
            tvDescription = itemView.findViewById(R.id.tvReminderDescription);
            tvDateTime = itemView.findViewById(R.id.tvReminderDateTime);
            tvTypeBadge = itemView.findViewById(R.id.tvReminderTypeBadge);
            switchEnabled = itemView.findViewById(R.id.switchReminderEnabled);
        }
    }
}

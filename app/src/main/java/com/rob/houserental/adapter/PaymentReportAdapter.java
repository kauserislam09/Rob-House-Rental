package com.rob.houserental.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.rob.houserental.R;
import com.rob.houserental.model.PaymentDisplayItem;
import com.rob.houserental.utils.PaymentMethodUtils;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class PaymentReportAdapter extends RecyclerView.Adapter<PaymentReportAdapter.ViewHolder> {

    private final List<PaymentDisplayItem> items = new ArrayList<>();
    private static final NumberFormat currencyFormatter = NumberFormat.getNumberInstance(Locale.getDefault());

    public void setItems(List<PaymentDisplayItem> list) {
        items.clear();
        if (list != null) {
            items.addAll(list);
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_payment, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        PaymentDisplayItem item = items.get(position);
        Context context = holder.itemView.getContext();

        String curr = context.getString(R.string.currency_symbol);
        holder.tvAmount.setText(curr + currencyFormatter.format(item.amount));
        holder.tvMethod.setText(PaymentMethodUtils.getDisplayName(context, item.paymentMethod));
        holder.tvDate.setText((item.paymentDate != null ? item.paymentDate : ""));

        String details = (item.tenantName != null ? item.tenantName : "") + " • " +
                (item.propertyName != null ? item.propertyName : "") + " (" + (item.unitNumber != null ? item.unitNumber : "") + ")";
        holder.tvRef.setText(details);
        holder.tvRef.setVisibility(View.VISIBLE);

        if (item.notes != null && !item.notes.trim().isEmpty()) {
            holder.tvNotes.setText(context.getString(R.string.prefix_note, item.notes.trim()));
            holder.tvNotes.setVisibility(View.VISIBLE);
        } else {
            holder.tvNotes.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        final TextView tvAmount;
        final TextView tvMethod;
        final TextView tvDate;
        final TextView tvRef;
        final TextView tvNotes;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvAmount = itemView.findViewById(R.id.tvPaymentAmount);
            tvMethod = itemView.findViewById(R.id.tvPaymentMethod);
            tvDate = itemView.findViewById(R.id.tvPaymentDate);
            tvRef = itemView.findViewById(R.id.tvPaymentReference);
            tvNotes = itemView.findViewById(R.id.tvPaymentNotes);
        }
    }
}

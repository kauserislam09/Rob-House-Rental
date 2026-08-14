package com.rob.houserental.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.rob.houserental.R;
import com.rob.houserental.model.Payment;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class PaymentAdapter extends RecyclerView.Adapter<PaymentAdapter.PaymentViewHolder> {

    private final List<Payment> paymentList = new ArrayList<>();
    private static final NumberFormat currencyFormatter = NumberFormat.getNumberInstance(Locale.getDefault());

    public void setPayments(List<Payment> payments) {
        paymentList.clear();
        if (payments != null) {
            paymentList.addAll(payments);
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public PaymentViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_payment, parent, false);
        return new PaymentViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PaymentViewHolder holder, int position) {
        Payment payment = paymentList.get(position);
        Context context = holder.itemView.getContext();

        String curr = context.getString(R.string.currency_symbol);
        holder.tvPaymentAmount.setText(curr + currencyFormatter.format(payment.getAmount()));
        holder.tvPaymentMethod.setText(getPaymentMethodDisplay(context, payment.getPaymentMethod()));
        holder.tvPaymentDate.setText("📅 " + (payment.getPaymentDate() != null ? payment.getPaymentDate() : ""));

        if (payment.getReference() != null && !payment.getReference().trim().isEmpty()) {
            holder.tvPaymentReference.setText(context.getString(R.string.prefix_ref, payment.getReference().trim()));
            holder.tvPaymentReference.setVisibility(View.VISIBLE);
        } else {
            holder.tvPaymentReference.setVisibility(View.GONE);
        }

        if (payment.getNotes() != null && !payment.getNotes().trim().isEmpty()) {
            holder.tvPaymentNotes.setText(context.getString(R.string.prefix_note, payment.getNotes().trim()));
            holder.tvPaymentNotes.setVisibility(View.VISIBLE);
        } else {
            holder.tvPaymentNotes.setVisibility(View.GONE);
        }
    }

    public static String getPaymentMethodDisplay(Context context, String method) {
        return com.rob.houserental.utils.PaymentMethodUtils.getDisplayName(context, method);
    }

    @Override
    public int getItemCount() {
        return paymentList.size();
    }

    static class PaymentViewHolder extends RecyclerView.ViewHolder {

        TextView tvPaymentAmount;
        TextView tvPaymentMethod;
        TextView tvPaymentDate;
        TextView tvPaymentReference;
        TextView tvPaymentNotes;

        public PaymentViewHolder(@NonNull View itemView) {
            super(itemView);
            tvPaymentAmount = itemView.findViewById(R.id.tvPaymentAmount);
            tvPaymentMethod = itemView.findViewById(R.id.tvPaymentMethod);
            tvPaymentDate = itemView.findViewById(R.id.tvPaymentDate);
            tvPaymentReference = itemView.findViewById(R.id.tvPaymentReference);
            tvPaymentNotes = itemView.findViewById(R.id.tvPaymentNotes);
        }
    }
}

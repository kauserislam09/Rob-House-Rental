package com.rob.houserental.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.rob.houserental.R;
import com.rob.houserental.model.BillPayment;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class BillPaymentAdapter extends RecyclerView.Adapter<BillPaymentAdapter.BillPaymentViewHolder> {

    private final List<BillPayment> paymentList = new ArrayList<>();
    private static final NumberFormat currencyFormatter = NumberFormat.getNumberInstance(Locale.getDefault());

    public void setPayments(List<BillPayment> payments) {
        paymentList.clear();
        if (payments != null) {
            paymentList.addAll(payments);
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public BillPaymentViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_bill_payment, parent, false);
        return new BillPaymentViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull BillPaymentViewHolder holder, int position) {
        BillPayment payment = paymentList.get(position);
        Context context = holder.itemView.getContext();

        String curr = context.getString(R.string.currency_symbol);
        holder.tvBillPaymentAmount.setText(curr + currencyFormatter.format(payment.getAmount()));
        holder.tvBillPaymentMethod.setText(PaymentAdapter.getPaymentMethodDisplay(context, payment.getPaymentMethod()));
        holder.tvBillPaymentDate.setText("📅 " + (payment.getPaymentDate() != null ? payment.getPaymentDate() : ""));

        if (payment.getReference() != null && !payment.getReference().trim().isEmpty()) {
            holder.tvBillPaymentReference.setText(context.getString(R.string.prefix_ref, payment.getReference().trim()));
            holder.tvBillPaymentReference.setVisibility(View.VISIBLE);
        } else {
            holder.tvBillPaymentReference.setVisibility(View.GONE);
        }

        if (payment.getNotes() != null && !payment.getNotes().trim().isEmpty()) {
            holder.tvBillPaymentNotes.setText(context.getString(R.string.prefix_note, payment.getNotes().trim()));
            holder.tvBillPaymentNotes.setVisibility(View.VISIBLE);
        } else {
            holder.tvBillPaymentNotes.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() {
        return paymentList.size();
    }

    static class BillPaymentViewHolder extends RecyclerView.ViewHolder {

        TextView tvBillPaymentAmount;
        TextView tvBillPaymentMethod;
        TextView tvBillPaymentDate;
        TextView tvBillPaymentReference;
        TextView tvBillPaymentNotes;

        public BillPaymentViewHolder(@NonNull View itemView) {
            super(itemView);
            tvBillPaymentAmount = itemView.findViewById(R.id.tvBillPaymentAmount);
            tvBillPaymentMethod = itemView.findViewById(R.id.tvBillPaymentMethod);
            tvBillPaymentDate = itemView.findViewById(R.id.tvBillPaymentDate);
            tvBillPaymentReference = itemView.findViewById(R.id.tvBillPaymentReference);
            tvBillPaymentNotes = itemView.findViewById(R.id.tvBillPaymentNotes);
        }
    }
}

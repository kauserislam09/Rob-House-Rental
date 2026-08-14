package com.rob.houserental.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.rob.houserental.R;
import com.rob.houserental.commercial.PlanConfig;
import com.rob.houserental.commercial.SubscriptionPlan;
import com.rob.houserental.model.PaymentOrder;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class PaymentOrderAdapter extends RecyclerView.Adapter<PaymentOrderAdapter.ViewHolder> {

    private final List<PaymentOrder> items = new ArrayList<>();
    private static final DecimalFormat currencyFormat = new DecimalFormat("#,##,###");
    private static final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());

    public void setItems(List<PaymentOrder> list) {
        items.clear();
        if (list != null) {
            items.addAll(list);
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_payment_order, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        PaymentOrder order = items.get(position);
        Context context = holder.itemView.getContext();

        SubscriptionPlan plan = PlanConfig.getPlan(order.getPlanCode());
        holder.tvPlanName.setText(plan.getDisplayName());

        holder.tvOrderId.setText(context.getString(R.string.order_id_label) + order.getOrderId());
        holder.tvAmount.setText(context.getString(R.string.amount_to_send) + " ৳" + currencyFormat.format(order.getAmountMinor() / 100.0));
        holder.tvMethod.setText(context.getString(R.string.select_payment_method) + ": " + getMethodDisplay(context, order.getPaymentMethod()));

        if (order.getTransactionId() != null && !order.getTransactionId().trim().isEmpty()) {
            holder.tvTxId.setText("TrxID: " + order.getTransactionId().trim());
            holder.tvTxId.setVisibility(View.VISIBLE);
        } else {
            holder.tvTxId.setVisibility(View.GONE);
        }

        holder.tvDate.setText(dateFormat.format(new Date(order.getCreatedAt())));

        String status = order.getStatus() != null ? order.getStatus().toUpperCase() : "PENDING_PAYMENT";
        holder.tvStatusBadge.setText(getStatusDisplay(context, status));
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    private String getMethodDisplay(Context context, String method) {
        if (method == null) return "bKash";
        switch (method.toUpperCase()) {
            case "NAGAD": return context.getString(R.string.payment_nagad);
            case "ROCKET": return context.getString(R.string.payment_rocket);
            case "BKASH":
            default: return context.getString(R.string.payment_bkash);
        }
    }

    private String getStatusDisplay(Context context, String status) {
        switch (status) {
            case "PAYMENT_SUBMITTED":
            case "UNDER_REVIEW":
                return context.getString(R.string.order_status_submitted);
            case "APPROVED":
                return context.getString(R.string.order_status_approved);
            case "REJECTED":
                return context.getString(R.string.order_status_rejected);
            case "EXPIRED":
                return context.getString(R.string.order_status_expired);
            case "PENDING_PAYMENT":
            default:
                return context.getString(R.string.order_status_pending);
        }
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvPlanName, tvStatusBadge, tvOrderId, tvAmount, tvMethod, tvTxId, tvDate;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvPlanName = itemView.findViewById(R.id.tvOrderPlanName);
            tvStatusBadge = itemView.findViewById(R.id.tvOrderStatusBadge);
            tvOrderId = itemView.findViewById(R.id.tvOrderIdText);
            tvAmount = itemView.findViewById(R.id.tvOrderAmount);
            tvMethod = itemView.findViewById(R.id.tvOrderMethod);
            tvTxId = itemView.findViewById(R.id.tvOrderTxId);
            tvDate = itemView.findViewById(R.id.tvOrderDate);
        }
    }
}

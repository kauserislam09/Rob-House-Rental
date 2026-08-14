package com.rob.houserental.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.rob.houserental.R;
import com.rob.houserental.model.ExpenseDisplayItem;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class ExpenseAdapter extends RecyclerView.Adapter<ExpenseAdapter.ExpenseViewHolder> {

    private final List<ExpenseDisplayItem> expenseList = new ArrayList<>();
    private OnExpenseClickListener listener;
    private static final NumberFormat currencyFormatter = NumberFormat.getNumberInstance(Locale.getDefault());

    public interface OnExpenseClickListener {
        void onExpenseClick(ExpenseDisplayItem item);
    }

    public void setOnExpenseClickListener(OnExpenseClickListener listener) {
        this.listener = listener;
    }

    public void setExpenses(List<ExpenseDisplayItem> expenses) {
        expenseList.clear();
        if (expenses != null) {
            expenseList.addAll(expenses);
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ExpenseViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_expense, parent, false);
        return new ExpenseViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ExpenseViewHolder holder, int position) {
        ExpenseDisplayItem item = expenseList.get(position);
        Context context = holder.itemView.getContext();

        String cat = item.category != null ? item.category : "OTHER";
        holder.tvExpenseCategoryIcon.setText(getCategoryIcon(cat));
        holder.tvExpenseCategoryTitle.setText(getCategoryTitle(context, cat));

        String curr = context.getString(R.string.currency_symbol);
        holder.tvExpenseAmount.setText(curr + currencyFormatter.format(item.amount));

        String propName = item.propertyName != null ? item.propertyName : context.getString(R.string.property_label);
        if (item.unitNumber != null && !item.unitNumber.isEmpty()) {
            holder.tvExpensePropertyUnit.setText("🏢 " + propName + " • " + context.getString(R.string.prefix_unit_format, item.unitNumber));
        } else {
            holder.tvExpensePropertyUnit.setText("🏢 " + propName + " (" + context.getString(R.string.whole_property_option) + ")");
        }

        holder.tvExpenseDate.setText("📅 " + (item.expenseDate != null ? item.expenseDate : ""));

        if (item.receiptPath != null && !item.receiptPath.isEmpty()) {
            holder.tvExpenseReceiptBadge.setVisibility(View.VISIBLE);
        } else {
            holder.tvExpenseReceiptBadge.setVisibility(View.GONE);
        }

        if (item.description != null && !item.description.trim().isEmpty()) {
            holder.tvExpenseDescription.setText(item.description.trim());
            holder.tvExpenseDescription.setVisibility(View.VISIBLE);
        } else {
            holder.tvExpenseDescription.setVisibility(View.GONE);
        }

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onExpenseClick(item);
            }
        });
    }

    public static String getCategoryIcon(String category) {
        if (category == null) return "📦";
        switch (category.toUpperCase()) {
            case "REPAIR":
                return "🔧";
            case "MAINTENANCE":
                return "🛠️";
            case "CLEANING":
                return "🧹";
            case "SECURITY":
                return "🛡️";
            case "GENERATOR":
                return "⚡";
            case "PLUMBING":
                return "🚰";
            case "ELECTRICAL":
                return "💡";
            case "PAINTING":
                return "🎨";
            case "RENOVATION":
                return "🏛️";
            case "PROPERTY_TAX":
                return "🏢";
            case "SERVICE_CHARGE":
                return "⚙️";
            case "OTHER":
            default:
                return "📦";
        }
    }

    public static String getCategoryTitle(Context context, String category) {
        if (category == null) return context.getString(R.string.expense_cat_other);
        switch (category.toUpperCase()) {
            case "REPAIR":
                return context.getString(R.string.expense_cat_repair);
            case "MAINTENANCE":
                return context.getString(R.string.expense_cat_maintenance);
            case "CLEANING":
                return context.getString(R.string.expense_cat_cleaning);
            case "SECURITY":
                return context.getString(R.string.expense_cat_security);
            case "GENERATOR":
                return context.getString(R.string.expense_cat_generator);
            case "PLUMBING":
                return context.getString(R.string.expense_cat_plumbing);
            case "ELECTRICAL":
                return context.getString(R.string.expense_cat_electrical);
            case "PAINTING":
                return context.getString(R.string.expense_cat_painting);
            case "RENOVATION":
                return context.getString(R.string.expense_cat_renovation);
            case "PROPERTY_TAX":
                return context.getString(R.string.expense_cat_tax);
            case "SERVICE_CHARGE":
                return context.getString(R.string.expense_cat_service);
            case "OTHER":
            default:
                return context.getString(R.string.expense_cat_other);
        }
    }

    @Override
    public int getItemCount() {
        return expenseList.size();
    }

    static class ExpenseViewHolder extends RecyclerView.ViewHolder {

        TextView tvExpenseCategoryIcon;
        TextView tvExpenseCategoryTitle;
        TextView tvExpenseAmount;
        TextView tvExpensePropertyUnit;
        TextView tvExpenseDate;
        TextView tvExpenseReceiptBadge;
        TextView tvExpenseDescription;

        public ExpenseViewHolder(@NonNull View itemView) {
            super(itemView);
            tvExpenseCategoryIcon = itemView.findViewById(R.id.tvExpenseCategoryIcon);
            tvExpenseCategoryTitle = itemView.findViewById(R.id.tvExpenseCategoryTitle);
            tvExpenseAmount = itemView.findViewById(R.id.tvExpenseAmount);
            tvExpensePropertyUnit = itemView.findViewById(R.id.tvExpensePropertyUnit);
            tvExpenseDate = itemView.findViewById(R.id.tvExpenseDate);
            tvExpenseReceiptBadge = itemView.findViewById(R.id.tvExpenseReceiptBadge);
            tvExpenseDescription = itemView.findViewById(R.id.tvExpenseDescription);
        }
    }
}

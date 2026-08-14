package com.rob.houserental.adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.rob.houserental.PropertyDetailsActivity;
import com.rob.houserental.R;
import com.rob.houserental.financial.PropertyFinancialSummary;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class PropertyPerformanceAdapter extends RecyclerView.Adapter<PropertyPerformanceAdapter.ViewHolder> {

    private final List<PropertyFinancialSummary> items = new ArrayList<>();

    public void setItems(List<PropertyFinancialSummary> list) {
        items.clear();
        if (list != null) {
            items.addAll(list);
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_property_performance, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        PropertyFinancialSummary item = items.get(position);
        holder.bind(item);
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        private final TextView tvPropName;
        private final TextView tvPropUnits;
        private final TextView tvPropExpected;
        private final TextView tvPropCollected;
        private final TextView tvPropOutstanding;
        private final TextView tvPropNet;
        private final TextView tvPropRates;
        private final NumberFormat currencyFormatter = NumberFormat.getNumberInstance(Locale.getDefault());

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvPropName = itemView.findViewById(R.id.tvPropPerfName);
            tvPropUnits = itemView.findViewById(R.id.tvPropPerfUnits);
            tvPropExpected = itemView.findViewById(R.id.tvPropPerfExpected);
            tvPropCollected = itemView.findViewById(R.id.tvPropPerfCollected);
            tvPropOutstanding = itemView.findViewById(R.id.tvPropPerfOutstanding);
            tvPropNet = itemView.findViewById(R.id.tvPropPerfNet);
            tvPropRates = itemView.findViewById(R.id.tvPropPerfRates);
        }

        public void bind(PropertyFinancialSummary item) {
            Context context = itemView.getContext();
            String curr = context.getString(R.string.currency_symbol);

            tvPropName.setText(item.getPropertyName());
            tvPropUnits.setText(context.getString(R.string.reports_total_units) + ": " + item.getTotalUnits() +
                    " | " + context.getString(R.string.reports_occupied_units) + ": " + item.getOccupiedUnits() +
                    " | " + context.getString(R.string.reports_vacant_units) + ": " + item.getVacantUnits());

            tvPropExpected.setText(context.getString(R.string.reports_expected_rent) + ": " + curr + currencyFormatter.format(item.getExpectedRent()));
            tvPropCollected.setText(context.getString(R.string.reports_collected_rent) + ": " + curr + currencyFormatter.format(item.getCollectedRent()));
            tvPropOutstanding.setText(context.getString(R.string.reports_outstanding_rent) + ": " + curr + currencyFormatter.format(item.getOutstandingRent()));
            tvPropNet.setText(context.getString(R.string.reports_net_income) + ": " + curr + currencyFormatter.format(item.getNetIncome()));

            tvPropRates.setText(context.getString(R.string.reports_collection_rate) + ": " + String.format(Locale.getDefault(), "%.1f%%", item.getCollectionRate()) +
                    " • " + context.getString(R.string.reports_occupancy_rate) + ": " + String.format(Locale.getDefault(), "%.1f%%", item.getOccupancyRate()));

            itemView.setOnClickListener(v -> {
                Intent intent = new Intent(context, PropertyDetailsActivity.class);
                intent.putExtra("property_id", item.getPropertyId());
                context.startActivity(intent);
            });
        }
    }
}

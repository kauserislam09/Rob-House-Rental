package com.rob.houserental.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.rob.houserental.R;
import com.rob.houserental.model.Property;

import java.util.ArrayList;
import java.util.List;

public class PropertyAdapter
        extends RecyclerView.Adapter<PropertyAdapter.PropertyViewHolder> {

    private final List<Property> propertyList = new ArrayList<>();

    private OnPropertyClickListener listener;

    public interface OnPropertyClickListener {
        void onPropertyClick(Property property);
    }

    public void setOnPropertyClickListener(
            OnPropertyClickListener listener
    ) {
        this.listener = listener;
    }

    public void setProperties(List<Property> properties) {

        propertyList.clear();

        if (properties != null) {
            propertyList.addAll(properties);
        }

        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public PropertyViewHolder onCreateViewHolder(
            @NonNull ViewGroup parent,
            int viewType
    ) {

        View view = LayoutInflater.from(parent.getContext())
                .inflate(
                        R.layout.item_property,
                        parent,
                        false
                );

        return new PropertyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(
            @NonNull PropertyViewHolder holder,
            int position
    ) {

        Property property =
                propertyList.get(position);

        android.content.Context context = holder.itemView.getContext();

        holder.tvPropertyName.setText(
                property.getName()
        );

        holder.tvPropertyAddress.setText(
                property.getAddress()
        );

        holder.tvPropertyType.setText(
                getPropertyTypeDisplay(context, property.getPropertyType())
        );

        holder.tvPropertyFloors.setText(
                context.getString(R.string.prefix_floors, property.getNumberOfFloors())
        );

        holder.itemView.setOnClickListener(v -> {

            if (listener != null) {
                listener.onPropertyClick(property);
            }

        });
    }

    public static String getPropertyTypeDisplay(android.content.Context context, String type) {
        if (type == null || type.trim().isEmpty()) return "";
        if (context == null) return type;
        String t = type.trim();
        if ("Apartment Building".equalsIgnoreCase(t)) return context.getString(R.string.prop_type_apartment_building);
        if ("House".equalsIgnoreCase(t)) return context.getString(R.string.prop_type_house);
        if ("Duplex".equalsIgnoreCase(t)) return context.getString(R.string.prop_type_duplex);
        if ("Commercial Building".equalsIgnoreCase(t)) return context.getString(R.string.prop_type_commercial);
        if ("Shop".equalsIgnoreCase(t)) return context.getString(R.string.prop_type_shop);
        if ("Office".equalsIgnoreCase(t)) return context.getString(R.string.prop_type_office);
        if ("Other".equalsIgnoreCase(t)) return context.getString(R.string.prop_type_other);
        return t;
    }

    @Override
    public int getItemCount() {
        return propertyList.size();
    }

    static class PropertyViewHolder
            extends RecyclerView.ViewHolder {

        TextView tvPropertyName;
        TextView tvPropertyAddress;
        TextView tvPropertyType;
        TextView tvPropertyFloors;

        public PropertyViewHolder(
                @NonNull View itemView
        ) {
            super(itemView);

            tvPropertyName =
                    itemView.findViewById(
                            R.id.tvPropertyName
                    );

            tvPropertyAddress =
                    itemView.findViewById(
                            R.id.tvPropertyAddress
                    );

            tvPropertyType =
                    itemView.findViewById(
                            R.id.tvPropertyType
                    );

            tvPropertyFloors =
                    itemView.findViewById(
                            R.id.tvPropertyFloors
                    );
        }
    }
}
package com.rob.houserental;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.appbar.MaterialToolbar;
import com.rob.houserental.adapter.PaymentOrderAdapter;
import com.rob.houserental.model.PaymentOrder;
import com.rob.houserental.repository.DatabaseCallback;
import com.rob.houserental.repository.SubscriptionRepository;

import java.util.List;

public class MyOrdersActivity extends AppCompatActivity {

    private static final String TAG = "MyOrdersActivity";

    private MaterialToolbar toolbar;
    private RecyclerView recyclerView;
    private LinearLayout layoutEmpty;
    private PaymentOrderAdapter adapter;
    private SubscriptionRepository subscriptionRepository;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_orders);

        subscriptionRepository = new SubscriptionRepository(this);

        toolbar = findViewById(R.id.toolbarMyOrders);
        recyclerView = findViewById(R.id.recyclerMyOrders);
        layoutEmpty = findViewById(R.id.layoutEmptyOrders);

        if (toolbar != null) {
            toolbar.setNavigationOnClickListener(v -> finish());
        }

        if (recyclerView != null) {
            recyclerView.setLayoutManager(new LinearLayoutManager(this));
            adapter = new PaymentOrderAdapter();
            recyclerView.setAdapter(adapter);
        }

        loadOrders();
    }

    private void loadOrders() {
        subscriptionRepository.getUserOrders(new DatabaseCallback<List<PaymentOrder>>() {
            @Override
            public void onSuccess(List<PaymentOrder> orders) {
                runOnUiThread(() -> {
                    if (orders == null || orders.isEmpty()) {
                        if (recyclerView != null) recyclerView.setVisibility(View.GONE);
                        if (layoutEmpty != null) layoutEmpty.setVisibility(View.VISIBLE);
                    } else {
                        if (layoutEmpty != null) layoutEmpty.setVisibility(View.GONE);
                        if (recyclerView != null) recyclerView.setVisibility(View.VISIBLE);
                        if (adapter != null) adapter.setItems(orders);
                    }
                });
            }

            @Override
            public void onError(Exception exception) {
                Log.e(TAG, "Error loading orders", exception);
            }
        });
    }
}

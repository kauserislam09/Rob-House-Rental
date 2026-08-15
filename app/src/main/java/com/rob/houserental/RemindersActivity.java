package com.rob.houserental;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.rob.houserental.adapter.ReminderAdapter;
import com.rob.houserental.model.Reminder;
import com.rob.houserental.repository.ReminderRepository;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class RemindersActivity extends AppCompatActivity {

    private static final String TAG = "RemindersActivity";

    private MaterialToolbar toolbar;
    private ChipGroup chipGroupState;
    private RecyclerView recyclerReminders;
    private View layoutEmptyReminders;
    private ExtendedFloatingActionButton fabAddReminder;

    private ReminderAdapter adapter;
    private ReminderRepository reminderRepository;

    private List<Reminder> allRemindersList = new ArrayList<>();
    private int selectedStateChipId = R.id.chipReminderAll;
    private static final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reminders);

        reminderRepository = new ReminderRepository(this);

        initializeViews();
        setupToolbar();
        setupRecyclerView();
        setupListeners();

        loadReminders();
    }

    private void initializeViews() {
        toolbar = findViewById(R.id.toolbarReminders);
        chipGroupState = findViewById(R.id.chipGroupReminderState);
        recyclerReminders = findViewById(R.id.recyclerReminders);
        layoutEmptyReminders = findViewById(R.id.layoutEmptyReminders);
        fabAddReminder = findViewById(R.id.fabAddReminder);
    }

    private void setupToolbar() {
        if (toolbar != null) {
            toolbar.setNavigationOnClickListener(v -> finish());
        }
    }

    private void setupRecyclerView() {
        adapter = new ReminderAdapter();
        if (recyclerReminders != null) {
            recyclerReminders.setLayoutManager(new LinearLayoutManager(this));
            recyclerReminders.setAdapter(adapter);
        }

        adapter.setOnReminderActionListener(new ReminderAdapter.OnReminderActionListener() {
            @Override
            public void onReminderClick(Reminder reminder) {
                Intent intent = new Intent(RemindersActivity.this, ReminderDetailsActivity.class);
                intent.putExtra("reminder_id", reminder.getId());
                startActivity(intent);
            }

            @Override
            public void onToggleEnabled(Reminder reminder, boolean enabled) {
                reminder.setEnabled(enabled);
                reminderRepository.update(reminder, new ReminderRepository.DatabaseCallback<Void>() {
                    @Override
                    public void onSuccess(Void result) {
                        runOnUiThread(() -> loadReminders());
                    }

                    @Override
                    public void onError(Exception exception) {
                        Log.e(TAG, "Error updating reminder enabled state", exception);
                    }
                });
            }
        });
    }

    private void setupListeners() {
        if (fabAddReminder != null) {
            fabAddReminder.setOnClickListener(v -> {
                Intent intent = new Intent(RemindersActivity.this, AddReminderActivity.class);
                startActivity(intent);
            });
        }

        if (chipGroupState != null) {
            chipGroupState.setOnCheckedChangeListener((group, checkedId) -> {
                if (checkedId != View.NO_ID) {
                    selectedStateChipId = checkedId;
                } else {
                    selectedStateChipId = R.id.chipReminderAll;
                }
                applyStateFilter();
            });
        }
    }

    private void loadReminders() {
        reminderRepository.getAll(new ReminderRepository.DatabaseCallback<List<Reminder>>() {
            @Override
            public void onSuccess(List<Reminder> list) {
                allRemindersList = list != null ? list : new ArrayList<>();
                runOnUiThread(() -> applyStateFilter());
            }

            @Override
            public void onError(Exception exception) {
                Log.e(TAG, "Error loading reminders", exception);
                runOnUiThread(() -> updateUI(new ArrayList<>()));
            }
        });
    }

    private void applyStateFilter() {
        String today = dateFormat.format(new Date());
        List<Reminder> filtered = new ArrayList<>();

        for (Reminder r : allRemindersList) {
            if (r == null) continue;

            if (selectedStateChipId == R.id.chipReminderUpcoming) {
                if (r.isEnabled() && !r.isCompleted() && r.getReminderDate() != null && r.getReminderDate().compareTo(today) >= 0) {
                    filtered.add(r);
                }
            } else if (selectedStateChipId == R.id.chipReminderToday) {
                if (r.getReminderDate() != null && r.getReminderDate().equals(today)) {
                    filtered.add(r);
                }
            } else if (selectedStateChipId == R.id.chipReminderOverdue) {
                if (r.isEnabled() && !r.isCompleted() && r.getReminderDate() != null && r.getReminderDate().compareTo(today) < 0) {
                    filtered.add(r);
                }
            } else if (selectedStateChipId == R.id.chipReminderCompleted) {
                if (r.isCompleted()) {
                    filtered.add(r);
                }
            } else {
                filtered.add(r);
            }
        }

        updateUI(filtered);
    }

    private void updateUI(List<Reminder> list) {
        if (list == null || list.isEmpty()) {
            if (layoutEmptyReminders != null) layoutEmptyReminders.setVisibility(View.VISIBLE);
            if (recyclerReminders != null) recyclerReminders.setVisibility(View.GONE);
            if (adapter != null) adapter.setItems(null);
        } else {
            if (layoutEmptyReminders != null) layoutEmptyReminders.setVisibility(View.GONE);
            if (recyclerReminders != null) recyclerReminders.setVisibility(View.VISIBLE);
            if (adapter != null) adapter.setItems(list);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadReminders();
    }
}

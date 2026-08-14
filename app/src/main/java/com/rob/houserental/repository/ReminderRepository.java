package com.rob.houserental.repository;

import android.content.Context;

import com.rob.houserental.data.AppDatabase;
import com.rob.houserental.data.ReminderDao;
import com.rob.houserental.model.Reminder;
import com.rob.houserental.notifications.ReminderSchedulerUtils;
import com.rob.houserental.utils.AppExecutors;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutorService;

public class ReminderRepository {

    private final Context context;
    private final ReminderDao reminderDao;
    private final ExecutorService executorService =
            AppExecutors.getInstance().getDatabaseExecutor();
    private static final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());

    public interface DatabaseCallback<T> extends com.rob.houserental.repository.DatabaseCallback<T> {}

    public ReminderRepository(Context context) {
        this.context = context.getApplicationContext();
        AppDatabase db = AppDatabase.getInstance(this.context);
        this.reminderDao = db.reminderDao();
    }

    public void insert(Reminder reminder, com.rob.houserental.repository.DatabaseCallback<Long> callback) {
        executorService.execute(() -> {
            try {
                long id = reminderDao.insert(reminder);
                reminder.setId(id);
                if (reminder.isEnabled() && !reminder.isCompleted()) {
                    ReminderSchedulerUtils.scheduleAlarm(context, reminder);
                }
                if (callback != null) callback.onSuccess(id);
            } catch (Exception e) {
                if (callback != null) callback.onError(e);
            }
        });
    }

    public void update(Reminder reminder, com.rob.houserental.repository.DatabaseCallback<Void> callback) {
        executorService.execute(() -> {
            try {
                reminder.setUpdatedAt(System.currentTimeMillis());
                reminderDao.update(reminder);
                if (reminder.isEnabled() && !reminder.isCompleted()) {
                    ReminderSchedulerUtils.scheduleAlarm(context, reminder);
                } else {
                    ReminderSchedulerUtils.cancelAlarm(context, reminder);
                }
                if (callback != null) callback.onSuccess(null);
            } catch (Exception e) {
                if (callback != null) callback.onError(e);
            }
        });
    }

    public void delete(Reminder reminder, com.rob.houserental.repository.DatabaseCallback<Void> callback) {
        executorService.execute(() -> {
            try {
                ReminderSchedulerUtils.cancelAlarm(context, reminder);
                reminderDao.delete(reminder);
                if (callback != null) callback.onSuccess(null);
            } catch (Exception e) {
                if (callback != null) callback.onError(e);
            }
        });
    }

    public void createOrUpdateDeterministicReminder(Reminder reminder, com.rob.houserental.repository.DatabaseCallback<Long> callback) {
        executorService.execute(() -> {
            try {
                Reminder existing = reminderDao.getByRelatedEntity(
                        reminder.getRelatedEntityType(),
                        reminder.getRelatedEntityId(),
                        reminder.getReminderType()
                );

                if (existing != null) {
                    existing.setTitle(reminder.getTitle());
                    existing.setDescription(reminder.getDescription());
                    existing.setReminderDate(reminder.getReminderDate());
                    existing.setReminderTime(reminder.getReminderTime());
                    existing.setRepeatType(reminder.getRepeatType());
                    existing.setRepeatInterval(reminder.getRepeatInterval());
                    existing.setEnabled(reminder.isEnabled());
                    existing.setCompleted(false);
                    existing.setUpdatedAt(System.currentTimeMillis());

                    reminderDao.update(existing);
                    if (existing.isEnabled()) {
                        ReminderSchedulerUtils.scheduleAlarm(context, existing);
                    }
                    if (callback != null) callback.onSuccess(existing.getId());
                } else {
                    long id = reminderDao.insert(reminder);
                    reminder.setId(id);
                    if (reminder.isEnabled()) {
                        ReminderSchedulerUtils.scheduleAlarm(context, reminder);
                    }
                    if (callback != null) callback.onSuccess(id);
                }
            } catch (Exception e) {
                if (callback != null) callback.onError(e);
            }
        });
    }

    public void getById(long id, com.rob.houserental.repository.DatabaseCallback<Reminder> callback) {
        executorService.execute(() -> {
            try {
                Reminder r = reminderDao.getById(id);
                if (callback != null) callback.onSuccess(r);
            } catch (Exception e) {
                if (callback != null) callback.onError(e);
            }
        });
    }

    public void getAll(com.rob.houserental.repository.DatabaseCallback<List<Reminder>> callback) {
        executorService.execute(() -> {
            try {
                List<Reminder> list = reminderDao.getAll();
                if (callback != null) callback.onSuccess(list);
            } catch (Exception e) {
                if (callback != null) callback.onError(e);
            }
        });
    }

    public void getUpcoming(com.rob.houserental.repository.DatabaseCallback<List<Reminder>> callback) {
        executorService.execute(() -> {
            try {
                String today = dateFormat.format(new Date());
                List<Reminder> list = reminderDao.getUpcoming(today);
                if (callback != null) callback.onSuccess(list);
            } catch (Exception e) {
                if (callback != null) callback.onError(e);
            }
        });
    }

    public void getOverdue(com.rob.houserental.repository.DatabaseCallback<List<Reminder>> callback) {
        executorService.execute(() -> {
            try {
                String today = dateFormat.format(new Date());
                List<Reminder> list = reminderDao.getOverdue(today);
                if (callback != null) callback.onSuccess(list);
            } catch (Exception e) {
                if (callback != null) callback.onError(e);
            }
        });
    }

    public void getCompleted(com.rob.houserental.repository.DatabaseCallback<List<Reminder>> callback) {
        executorService.execute(() -> {
            try {
                List<Reminder> list = reminderDao.getCompleted();
                if (callback != null) callback.onSuccess(list);
            } catch (Exception e) {
                if (callback != null) callback.onError(e);
            }
        });
    }

    public void setCompletedState(long id, boolean isCompleted, com.rob.houserental.repository.DatabaseCallback<Void> callback) {
        executorService.execute(() -> {
            try {
                Reminder r = reminderDao.getById(id);
                if (r != null) {
                    r.setCompleted(isCompleted);
                    r.setUpdatedAt(System.currentTimeMillis());
                    reminderDao.update(r);
                    if (isCompleted) {
                        ReminderSchedulerUtils.cancelAlarm(context, r);
                    } else if (r.isEnabled()) {
                        ReminderSchedulerUtils.scheduleAlarm(context, r);
                    }
                }
                if (callback != null) callback.onSuccess(null);
            } catch (Exception e) {
                if (callback != null) callback.onError(e);
            }
        });
    }
}

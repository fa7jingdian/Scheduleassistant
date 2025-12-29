package com.backmo.scheduleassistant.reminder;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import androidx.core.app.NotificationManagerCompat;
import androidx.work.Data;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;

import java.util.concurrent.TimeUnit;

public class ReminderActionReceiver extends BroadcastReceiver {
    public static final String ACTION_SNOOZE = "com.backmo.scheduleassistant.action.SNOOZE";
    public static final String ACTION_DISMISS = "com.backmo.scheduleassistant.action.DISMISS";
    public static final String EXTRA_NOTIF_ID = "notif_id";
    public static final String EXTRA_TITLE = "title";

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        int id = intent.getIntExtra(EXTRA_NOTIF_ID, 0);
        if (ACTION_DISMISS.equals(action)) {
            NotificationManagerCompat.from(context).cancel(id);
        } else if (ACTION_SNOOZE.equals(action)) {
            NotificationManagerCompat.from(context).cancel(id);
            Data input = new Data.Builder()
                    .putString(EventReminderWorker.KEY_TITLE, intent.getStringExtra(EXTRA_TITLE))
                    .putString(EventReminderWorker.KEY_TIME, "稍后提醒")
                    .putInt(EventReminderWorker.KEY_NOTIF_ID, id)
                    .build();
            OneTimeWorkRequest req = new OneTimeWorkRequest.Builder(EventReminderWorker.class)
                    .setInputData(input)
                    .setInitialDelay(10, TimeUnit.MINUTES)
                    .build();
            WorkManager.getInstance(context).enqueue(req);
        }
    }
}


package com.backmo.scheduleassistant.reminder;

import android.content.Context;

import androidx.work.Data;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;

import com.backmo.scheduleassistant.data.db.EventEntity;

import java.util.concurrent.TimeUnit;

public class ReminderScheduler {
    public static void scheduleNotification(Context ctx, EventEntity e) {
        if (!"notification".equals(e.remindChannel)) return;
        long triggerAt = e.startAt - e.remindOffsetMinutes * 60L * 1000L;
        long delayMs = Math.max(0, triggerAt - System.currentTimeMillis());
        java.text.SimpleDateFormat fmt = new java.text.SimpleDateFormat("MM-dd HH:mm", java.util.Locale.getDefault());
        int notifId = (int) (e.startAt % Integer.MAX_VALUE);
        Data input = new Data.Builder()
                .putString(EventReminderWorker.KEY_TITLE, e.title)
                .putString(EventReminderWorker.KEY_TIME, fmt.format(new java.util.Date(e.startAt)))
                .putString(EventReminderWorker.KEY_LOCATION, e.location)
                .putInt(EventReminderWorker.KEY_NOTIF_ID, notifId)
                .build();
        OneTimeWorkRequest req = new OneTimeWorkRequest.Builder(EventReminderWorker.class)
                .setInputData(input)
                .setInitialDelay(delayMs, TimeUnit.MILLISECONDS)
                .build();
        WorkManager.getInstance(ctx).enqueue(req);
    }
}

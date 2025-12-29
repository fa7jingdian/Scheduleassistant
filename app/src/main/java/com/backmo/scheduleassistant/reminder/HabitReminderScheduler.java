package com.backmo.scheduleassistant.reminder;

import android.content.Context;

import androidx.work.Data;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;

import com.backmo.scheduleassistant.data.db.HabitEntity;

import java.util.Calendar;
import java.util.concurrent.TimeUnit;

public class HabitReminderScheduler {
    public static void scheduleNext(Context ctx, HabitEntity h) {
        if (!"notification".equals(h.remindChannel)) return;
        Calendar now = Calendar.getInstance();
        Calendar time = Calendar.getInstance();
        time.setTimeInMillis(h.timeOfDay);
        Calendar next = Calendar.getInstance();
        next.set(Calendar.HOUR_OF_DAY, time.get(Calendar.HOUR_OF_DAY));
        next.set(Calendar.MINUTE, time.get(Calendar.MINUTE));
        next.set(Calendar.SECOND, 0);
        next.set(Calendar.MILLISECOND, 0);
        if (next.getTimeInMillis() <= System.currentTimeMillis()) {
            next.add(Calendar.DAY_OF_MONTH, 1);
        }
        long triggerAt = next.getTimeInMillis() - h.remindOffsetMinutes * 60L * 1000L;
        long delayMs = Math.max(0, triggerAt - System.currentTimeMillis());
        Data input = new Data.Builder()
                .putString(HabitReminderWorker.KEY_HABIT_NAME, h.name)
                .build();
        OneTimeWorkRequest req = new OneTimeWorkRequest.Builder(HabitReminderWorker.class)
                .setInputData(input)
                .setInitialDelay(delayMs, TimeUnit.MILLISECONDS)
                .build();
        WorkManager.getInstance(ctx).enqueue(req);
    }
}


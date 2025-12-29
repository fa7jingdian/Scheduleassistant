package com.backmo.scheduleassistant.reminder;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.widget.Toast;

import com.backmo.scheduleassistant.data.db.EventEntity;
import com.backmo.scheduleassistant.util.PermissionUtils;

public class AlarmReminderScheduler {
    public static void schedule(Context ctx, EventEntity e) {
        long triggerAt = e.startAt - e.remindOffsetMinutes * 60L * 1000L;
        if (triggerAt < System.currentTimeMillis()) triggerAt = System.currentTimeMillis() + 1000;
        if (!PermissionUtils.canScheduleExactAlarms(ctx)) {
            Toast.makeText(ctx, "请允许精确闹钟以启用闹钟提醒", Toast.LENGTH_LONG).show();
            PermissionUtils.requestExactAlarm(ctx);
            return;
        }
        Intent i = new Intent(ctx, AlarmReminderReceiver.class);
        i.putExtra("title", e.title);
        i.putExtra("location", e.location);
        int reqCode = (int) (e.startAt % Integer.MAX_VALUE);
        PendingIntent pi = PendingIntent.getBroadcast(ctx, reqCode, i, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        AlarmManager am = (AlarmManager) ctx.getSystemService(Context.ALARM_SERVICE);
        if (am == null) return;
        if (Build.VERSION.SDK_INT >= 23) {
            am.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAt, pi);
        } else {
            am.setExact(AlarmManager.RTC_WAKEUP, triggerAt, pi);
        }
    }
}


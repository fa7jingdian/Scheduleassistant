package com.backmo.scheduleassistant.util;

import android.app.Activity;
import android.app.AlarmManager;
import android.content.Context;
import android.os.Build;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

public class PermissionUtils {
    public static boolean hasPostNotifications(Activity a) {
        if (Build.VERSION.SDK_INT < 33) return true;
        int res = ContextCompat.checkSelfPermission(a, android.Manifest.permission.POST_NOTIFICATIONS);
        return res == android.content.pm.PackageManager.PERMISSION_GRANTED;
    }

    public static void requestPostNotifications(Activity a) {
        if (Build.VERSION.SDK_INT >= 33) {
            ActivityCompat.requestPermissions(a, new String[]{android.Manifest.permission.POST_NOTIFICATIONS}, 1001);
        }
    }

    public static boolean canScheduleExactAlarms(Context c) {
        if (Build.VERSION.SDK_INT < 31) return true;
        AlarmManager am = (AlarmManager) c.getSystemService(Context.ALARM_SERVICE);
        return am != null && am.canScheduleExactAlarms();
    }

    public static void requestExactAlarm(Context c) {
        if (Build.VERSION.SDK_INT >= 31) {
            android.content.Intent i = new android.content.Intent(android.provider.Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM);
            i.addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK);
            c.startActivity(i);
        }
    }
}


package com.backmo.scheduleassistant.widget;

import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Context;

import com.backmo.scheduleassistant.R;

public class WidgetUpdateHelper {
    public static void requestUpdate(Context context) {
        AppWidgetManager mgr = AppWidgetManager.getInstance(context);
        int[] ids = mgr.getAppWidgetIds(new ComponentName(context, TodayWidgetProvider.class));
        for (int id : ids) {
            mgr.notifyAppWidgetViewDataChanged(id, R.id.widget_list);
        }
    }
}


package com.backmo.scheduleassistant.widget;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.widget.RemoteViews;

import com.backmo.scheduleassistant.R;
import com.backmo.scheduleassistant.MainActivity;
import com.backmo.scheduleassistant.ui.event.EventDetailActivity;

public class TodayWidgetProvider extends AppWidgetProvider {
    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        for (int appWidgetId : appWidgetIds) {
            RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.widget_today);
            Intent svcIntent = new Intent(context, TodayWidgetService.class);
            svcIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
            svcIntent.setData(android.net.Uri.parse(svcIntent.toUri(Intent.URI_INTENT_SCHEME)));
            views.setRemoteAdapter(R.id.widget_list, svcIntent);
            views.setEmptyView(R.id.widget_list, R.id.widget_empty);
            Intent openApp = new Intent(context, MainActivity.class);
            PendingIntent piOpen = PendingIntent.getActivity(context, appWidgetId, openApp, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
            views.setOnClickPendingIntent(R.id.tv_header, piOpen);
            Intent detailTemplate = new Intent(context, EventDetailActivity.class);
            PendingIntent piTemplate = PendingIntent.getActivity(context, 0, detailTemplate, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
            views.setPendingIntentTemplate(R.id.widget_list, piTemplate);
            appWidgetManager.updateAppWidget(appWidgetId, views);
            appWidgetManager.notifyAppWidgetViewDataChanged(appWidgetId, R.id.widget_list);
        }
    }

    public static void requestRefresh(Context context) {
        AppWidgetManager mgr = AppWidgetManager.getInstance(context);
        int[] ids = mgr.getAppWidgetIds(new ComponentName(context, TodayWidgetProvider.class));
        for (int id : ids) {
            mgr.notifyAppWidgetViewDataChanged(id, R.id.widget_list);
        }
    }
}


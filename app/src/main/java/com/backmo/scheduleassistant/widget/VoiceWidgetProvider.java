package com.backmo.scheduleassistant.widget;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.widget.RemoteViews;

import com.backmo.scheduleassistant.R;
import com.backmo.scheduleassistant.ui.voice.VoiceQuickAddActivity;

public class VoiceWidgetProvider extends AppWidgetProvider {
    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        for (int appWidgetId : appWidgetIds) {
            RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.widget_voice);
            Intent intent = new Intent(context, VoiceQuickAddActivity.class);
            PendingIntent pi = PendingIntent.getActivity(context, appWidgetId, intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
            views.setOnClickPendingIntent(R.id.btn_voice_add, pi);
            appWidgetManager.updateAppWidget(appWidgetId, views);
        }
    }
}


package com.backmo.scheduleassistant.widget;

import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import com.backmo.scheduleassistant.R;
import com.backmo.scheduleassistant.data.db.EventEntity;
import com.backmo.scheduleassistant.data.db.AppDatabase;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class TodayWidgetService extends RemoteViewsService {
    @Override
    public RemoteViewsFactory onGetViewFactory(Intent intent) {
        return new TodayWidgetFactory(getApplicationContext(), intent);
    }

    static class TodayWidgetFactory implements RemoteViewsFactory {
        private final Context context;
        private final int appWidgetId;
        private final List<EventEntity> items = new ArrayList<>();
        private final SimpleDateFormat fmtTime = new SimpleDateFormat("HH:mm", Locale.getDefault());

        TodayWidgetFactory(Context c, Intent intent) {
            context = c;
            appWidgetId = intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID);
        }

        @Override public void onCreate() {}

        @Override
        public void onDataSetChanged() {
            items.clear();
            Calendar c = Calendar.getInstance();
            c.set(Calendar.HOUR_OF_DAY, 0);
            c.set(Calendar.MINUTE, 0);
            c.set(Calendar.SECOND, 0);
            c.set(Calendar.MILLISECOND, 0);
            long start = c.getTimeInMillis();
            c.add(Calendar.DAY_OF_MONTH, 1);
            long end = c.getTimeInMillis();
            List<EventEntity> list = AppDatabase.getInstance(context).eventDao().getEventsForDaySync(start, end);
            if (list != null) items.addAll(list);
        }

        @Override public void onDestroy() { items.clear(); }

        @Override public int getCount() { return items.size(); }

        @Override
        public RemoteViews getViewAt(int position) {
            if (position < 0 || position >= items.size()) return null;
            EventEntity e = items.get(position);
            RemoteViews rv = new RemoteViews(context.getPackageName(), R.layout.widget_today_item);
            rv.setTextViewText(R.id.tv_title, e.title);
            rv.setTextViewText(R.id.tv_time, e.allDay ? "全天" : fmtTime.format(new java.util.Date(e.startAt)));
            Intent fill = new Intent();
            fill.putExtra(com.backmo.scheduleassistant.ui.event.EventDetailActivity.EXTRA_TITLE, e.title);
            fill.putExtra(com.backmo.scheduleassistant.ui.event.EventDetailActivity.EXTRA_START, e.startAt);
            fill.putExtra(com.backmo.scheduleassistant.ui.event.EventDetailActivity.EXTRA_END, e.endAt);
            fill.putExtra(com.backmo.scheduleassistant.ui.event.EventDetailActivity.EXTRA_ALLDAY, e.allDay);
            fill.putExtra(com.backmo.scheduleassistant.ui.event.EventDetailActivity.EXTRA_LOCATION, e.location);
            rv.setOnClickFillInIntent(R.id.item_root, fill);
            return rv;
        }

        @Override public RemoteViews getLoadingView() { return null; }
        @Override public int getViewTypeCount() { return 1; }
        @Override public long getItemId(int position) { return position; }
        @Override public boolean hasStableIds() { return true; }
    }
}


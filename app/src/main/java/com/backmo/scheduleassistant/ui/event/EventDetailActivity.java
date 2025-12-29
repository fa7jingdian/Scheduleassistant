package com.backmo.scheduleassistant.ui.event;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.backmo.scheduleassistant.R;
import com.backmo.scheduleassistant.data.ScheduleRepository;
import com.backmo.scheduleassistant.data.db.EventEntity;
import com.backmo.scheduleassistant.reminder.ReminderScheduler;

import java.text.SimpleDateFormat;
import java.util.Locale;

public class EventDetailActivity extends AppCompatActivity {
    public static final String EXTRA_TITLE = "title";
    public static final String EXTRA_START = "start";
    public static final String EXTRA_END = "end";
    public static final String EXTRA_ALLDAY = "allday";
    public static final String EXTRA_LOCATION = "location";

    private ScheduleRepository repository;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event_detail);
        repository = new ScheduleRepository(this);
        TextView tvTitle = findViewById(R.id.tv_title);
        TextView tvTime = findViewById(R.id.tv_time);
        TextView tvLocation = findViewById(R.id.tv_location);
        Button btnEdit = findViewById(R.id.btn_edit);
        Button btnDelete = findViewById(R.id.btn_delete);
        Button btnSnooze = findViewById(R.id.btn_snooze);
        Button btnClose = findViewById(R.id.btn_close);
        Button btnNavigate = findViewById(R.id.btn_navigate);

        String title = getIntent().getStringExtra(EXTRA_TITLE);
        long start = getIntent().getLongExtra(EXTRA_START, System.currentTimeMillis());
        long end = getIntent().getLongExtra(EXTRA_END, start + 3600000);
        boolean allDay = getIntent().getBooleanExtra(EXTRA_ALLDAY, false);
        String location = getIntent().getStringExtra(EXTRA_LOCATION);

        tvTitle.setText(title);
        SimpleDateFormat fmt = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());
        tvTime.setText(allDay ? "全天" : (fmt.format(new java.util.Date(start)) + " - " + fmt.format(new java.util.Date(end))));
        tvLocation.setText(location == null || location.isEmpty() ? "无地点" : location);

        btnEdit.setOnClickListener(v -> {
            Intent i = new Intent(this, EventEditActivity.class);
            startActivity(i);
        });
        btnDelete.setOnClickListener(v -> {
            finish();
        });
        btnSnooze.setOnClickListener(v -> {
            EventEntity e = new EventEntity();
            e.title = title;
            e.startAt = System.currentTimeMillis() + 10 * 60 * 1000;
            e.endAt = e.startAt + 60 * 60 * 1000;
            e.remindOffsetMinutes = 0;
            e.remindChannel = "notification";
            ReminderScheduler.scheduleNotification(this, e);
        });
        btnClose.setOnClickListener(v -> finish());
        btnNavigate.setOnClickListener(v -> {
            if (location != null && !location.isEmpty()) {
                Intent mapIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("geo:0,0?q=" + Uri.encode(location)));
                mapIntent.setPackage("com.google.android.apps.maps");
                startActivity(mapIntent);
            }
        });
    }
}


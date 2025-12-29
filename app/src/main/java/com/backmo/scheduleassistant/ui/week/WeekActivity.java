package com.backmo.scheduleassistant.ui.week;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.backmo.scheduleassistant.R;
import com.backmo.scheduleassistant.data.ScheduleRepository;
import com.backmo.scheduleassistant.data.db.EventEntity;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class WeekActivity extends AppCompatActivity {
    private final List<DayBlock> days = new ArrayList<>();
    private ScheduleRepository repository;
    private WeekAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_week);
        repository = new ScheduleRepository(this);
        RecyclerView rv = findViewById(R.id.rv_week);
        rv.setLayoutManager(new LinearLayoutManager(this));
        adapter = new WeekAdapter(days);
        rv.setAdapter(adapter);
        buildWeek();
        loadEvents();
    }

    private void buildWeek() {
        days.clear();
        Calendar c = Calendar.getInstance();
        c.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
        for (int i = 0; i < 7; i++) {
            Calendar start = (Calendar) c.clone();
            start.set(Calendar.HOUR_OF_DAY, 0);
            start.set(Calendar.MINUTE, 0);
            start.set(Calendar.SECOND, 0);
            start.set(Calendar.MILLISECOND, 0);
            Calendar end = (Calendar) start.clone();
            end.add(Calendar.DAY_OF_MONTH, 1);
            days.add(new DayBlock(start.getTimeInMillis(), end.getTimeInMillis(), new ArrayList<>()));
            c.add(Calendar.DAY_OF_MONTH, 1);
        }
    }

    private void loadEvents() {
        for (DayBlock block : days) {
            repository.getEventsForDay(block.start, block.end).observe(this, events -> {
                block.items.clear();
                if (events != null) block.items.addAll(events);
                adapter.notifyDataSetChanged();
            });
        }
    }

    private static class DayBlock {
        long start;
        long end;
        List<EventEntity> items;
        DayBlock(long s, long e, List<EventEntity> i) { start = s; end = e; items = i; }
    }

    private static class WeekAdapter extends RecyclerView.Adapter<WeekViewHolder> {
        private final List<DayBlock> days;
        WeekAdapter(List<DayBlock> days) { this.days = days; }
        @NonNull
        @Override
        public WeekViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_week_day, parent, false);
            return new WeekViewHolder(v);
        }
        @Override
        public void onBindViewHolder(@NonNull WeekViewHolder holder, int position) {
            holder.bind(days.get(position));
        }
        @Override
        public int getItemCount() { return days.size(); }
    }

    private static class WeekViewHolder extends RecyclerView.ViewHolder {
        private final TextView tvHeader;
        private final TextView tvList;
        private final SimpleDateFormat fmtDay = new SimpleDateFormat("EEE MM-dd", Locale.getDefault());
        private final SimpleDateFormat fmtTime = new SimpleDateFormat("HH:mm", Locale.getDefault());
        WeekViewHolder(@NonNull View itemView) {
            super(itemView);
            tvHeader = itemView.findViewById(R.id.tv_header);
            tvList = itemView.findViewById(R.id.tv_list);
        }
        void bind(DayBlock block) {
            tvHeader.setText(fmtDay.format(new Date(block.start)));
            StringBuilder sb = new StringBuilder();
            for (EventEntity e : block.items) {
                String time = e.allDay ? "全天" : fmtTime.format(new Date(e.startAt));
                sb.append("• ").append(time).append(" ").append(e.title).append("\n");
            }
            tvList.setText(sb.length() == 0 ? "无日程" : sb.toString());
        }
    }
}


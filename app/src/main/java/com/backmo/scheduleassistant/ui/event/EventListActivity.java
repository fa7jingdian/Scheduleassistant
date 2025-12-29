package com.backmo.scheduleassistant.ui.event;

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
import java.util.List;
import java.util.Locale;

public class EventListActivity extends AppCompatActivity {
    private ScheduleRepository repository;
    private EventListAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event_list);
        repository = new ScheduleRepository(this);
        RecyclerView rv = findViewById(R.id.rv_events);
        rv.setLayoutManager(new LinearLayoutManager(this));
        adapter = new EventListAdapter(new ArrayList<>(), e -> {
            android.content.Intent i = new android.content.Intent(this, com.backmo.scheduleassistant.ui.event.EventDetailActivity.class);
            i.putExtra(com.backmo.scheduleassistant.ui.event.EventDetailActivity.EXTRA_TITLE, e.title);
            i.putExtra(com.backmo.scheduleassistant.ui.event.EventDetailActivity.EXTRA_START, e.startAt);
            i.putExtra(com.backmo.scheduleassistant.ui.event.EventDetailActivity.EXTRA_END, e.endAt);
            i.putExtra(com.backmo.scheduleassistant.ui.event.EventDetailActivity.EXTRA_ALLDAY, e.allDay);
            i.putExtra(com.backmo.scheduleassistant.ui.event.EventDetailActivity.EXTRA_LOCATION, e.location);
            startActivity(i);
        });
        rv.setAdapter(adapter);
        repository.getAllEvents().observe(this, events -> adapter.setItems(events));
    }

    private static class EventListAdapter extends RecyclerView.Adapter<EventViewHolder> {
        private final List<EventEntity> items;
        private final OnEventClick listener;
        EventListAdapter(List<EventEntity> items, OnEventClick l) { this.items = items; this.listener = l; }
        void setItems(List<EventEntity> newItems) {
            items.clear();
            if (newItems != null) items.addAll(newItems);
            notifyDataSetChanged();
        }
        @NonNull
        @Override
        public EventViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_event, parent, false);
            return new EventViewHolder(v);
        }
        @Override
        public void onBindViewHolder(@NonNull EventViewHolder holder, int position) {
            holder.bind(items.get(position), listener);
        }
        @Override
        public int getItemCount() { return items.size(); }
    }

    private static class EventViewHolder extends RecyclerView.ViewHolder {
        private final TextView title;
        private final TextView subtitle;
        private final View colorBar;
        private final TextView badge;
        private final View expandContainer;
        private final TextView tvNotes;
        private final TextView tvRepeat;
        private final View btnEdit;
        private final View btnDelete;
        private boolean expanded = false;
        private final SimpleDateFormat fmtTime = new SimpleDateFormat("HH:mm", Locale.getDefault());
        private final SimpleDateFormat fmtDay = new SimpleDateFormat("MM-dd", Locale.getDefault());
        EventViewHolder(@NonNull View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.tv_title);
            subtitle = itemView.findViewById(R.id.tv_meta);
            colorBar = itemView.findViewById(R.id.color_bar);
            badge = itemView.findViewById(R.id.tv_badge);
            expandContainer = itemView.findViewById(R.id.expand_container);
            tvNotes = itemView.findViewById(R.id.tv_notes);
            tvRepeat = itemView.findViewById(R.id.tv_repeat);
            btnEdit = itemView.findViewById(R.id.btn_edit);
            btnDelete = itemView.findViewById(R.id.btn_delete);
        }
        void bind(EventEntity e, OnEventClick listener) {
            title.setText(e.title);
            String day = fmtDay.format(new java.util.Date(e.startAt));
            String time = e.allDay ? "全天" : (fmtTime.format(new java.util.Date(e.startAt)) + " - " + fmtTime.format(new java.util.Date(e.endAt)));
            String meta = day + " · " + time + (e.location == null || e.location.isEmpty() ? "" : (" · " + e.location));
            subtitle.setText(meta);
            int color = getCategoryColor(itemView.getContext(), e.category);
            colorBar.setBackgroundColor(color);
            if (e.allDay) {
                badge.setText("全天");
                badge.setVisibility(View.VISIBLE);
            } else if (e.remindOffsetMinutes > 0) {
                badge.setText("提前" + e.remindOffsetMinutes + "分钟");
                badge.setVisibility(View.VISIBLE);
            } else {
                badge.setVisibility(View.GONE);
            }
            expandContainer.setVisibility(View.VISIBLE);
            tvNotes.setVisibility(View.GONE);
            tvRepeat.setVisibility(View.GONE);
            btnEdit.setOnClickListener(v -> {
                android.content.Intent i = new android.content.Intent(itemView.getContext(), com.backmo.scheduleassistant.ui.event.EventEditActivity.class);
                i.putExtra("id", e.id);
                i.putExtra("title", e.title);
                i.putExtra("allDay", e.allDay);
                i.putExtra("startAt", e.startAt);
                i.putExtra("endAt", e.endAt);
                i.putExtra("remindOffsetMinutes", e.remindOffsetMinutes);
                i.putExtra("remindChannel", e.remindChannel);
                i.putExtra("repeatRule", e.repeatRule);
                i.putExtra("location", e.location);
                i.putExtra("notes", e.notes);
                itemView.getContext().startActivity(i);
            });
            btnDelete.setOnClickListener(v -> {
                com.backmo.scheduleassistant.data.ScheduleRepository repo = new com.backmo.scheduleassistant.data.ScheduleRepository(itemView.getContext());
                if (e.category != null && e.category.trim().equals("习惯") && e.notes != null && e.notes.startsWith("HABIT-")) {
                    try {
                        long hid = Long.parseLong(e.notes.substring(6));
                        repo.deleteHabitById(hid);
                        android.widget.Toast.makeText(itemView.getContext(), "已删除习惯", android.widget.Toast.LENGTH_SHORT).show();
                    } catch (Exception ex) {
                        android.widget.Toast.makeText(itemView.getContext(), "删除习惯失败", android.widget.Toast.LENGTH_SHORT).show();
                    }
                } else {
                    repo.deleteEvent(e);
                    android.widget.Toast.makeText(itemView.getContext(), "已删除日程", android.widget.Toast.LENGTH_SHORT).show();
                }
            });
        }

        private int getCategoryColor(android.content.Context c, String category) {
            if (category == null) return androidx.core.content.ContextCompat.getColor(c, android.R.color.darker_gray);
            String k = category.trim();
            if (k.equals("工作")) return 0xFF4CAF50;
            if (k.equals("学习")) return 0xFF2196F3;
            if (k.equals("生活")) return 0xFFFFC107;
            if (k.equals("导入")) return 0xFF9C27B0;
            return 0xFF607D8B;
        }
        private String repeatText(String rule) {
            if (rule == null || rule.isEmpty() || "none".equals(rule)) return "无";
            switch (rule) {
                case "daily": return "每天";
                case "weekly": return "每周";
                case "monthly": return "每月";
                case "yearly": return "每年";
                default: return rule;
            }
        }
    }

    private interface OnEventClick {
        void onClick(EventEntity e);
    }
}

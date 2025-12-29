package com.backmo.scheduleassistant.ui.calendar;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.backmo.scheduleassistant.R;
import com.backmo.scheduleassistant.data.ScheduleRepository;
import com.backmo.scheduleassistant.data.db.EventEntity;

import android.text.TextUtils;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class CalendarFragment extends Fragment {
    private RecyclerView eventList;
    private TextView tvEmpty;
    private ScheduleRepository repository;
    private Calendar monthCal;
    private long selectedStart;
    private long selectedEnd;
    private int selectedDay;
    private int todayDay;
    private TextView tvMonth;
    private final java.util.HashMap<Integer, Integer> dayCounts = new java.util.HashMap<>();
    private List<com.backmo.scheduleassistant.data.db.EventEntity> currentEvents = new java.util.ArrayList<>();
    private List<com.backmo.scheduleassistant.data.db.HabitEntity> currentHabits = new java.util.ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_calendar, container, false);
        RecyclerView grid = root.findViewById(R.id.calendar_grid);
        eventList = root.findViewById(R.id.event_list);
        tvEmpty = root.findViewById(R.id.tv_empty);
        grid.setLayoutManager(new GridLayoutManager(requireContext(), 7));
        monthCal = Calendar.getInstance();
        monthCal.set(Calendar.DAY_OF_MONTH, 1);
        repository = new ScheduleRepository(requireContext());
        tvMonth = root.findViewById(R.id.tv_month);
        root.findViewById(R.id.btn_prev_month).setOnClickListener(v -> {
            monthCal.add(Calendar.MONTH, -1);
            refreshGridDays(grid);
        });
        root.findViewById(R.id.btn_next_month).setOnClickListener(v -> {
            monthCal.add(Calendar.MONTH, 1);
            refreshGridDays(grid);
        });
        DayAdapter dayAdapter = new DayAdapter(generateDays(), dayCounts, day -> {
            if (day <= 0) return;
            setSelectedDay(day);
            loadEventsForSelected();
            selectedDay = day;
            ((DayAdapter) grid.getAdapter()).setSelectedDay(day);
        });
        grid.setAdapter(dayAdapter);
        grid.addItemDecoration(new SpacingDecoration(4));
        eventList.setLayoutManager(new androidx.recyclerview.widget.LinearLayoutManager(requireContext()));
        eventList.setAdapter(new EventAdapter(new ArrayList<>(), e -> {
            android.content.Intent i = new android.content.Intent(requireContext(), com.backmo.scheduleassistant.ui.event.EventDetailActivity.class);
            i.putExtra(com.backmo.scheduleassistant.ui.event.EventDetailActivity.EXTRA_TITLE, e.title);
            i.putExtra(com.backmo.scheduleassistant.ui.event.EventDetailActivity.EXTRA_START, e.startAt);
            i.putExtra(com.backmo.scheduleassistant.ui.event.EventDetailActivity.EXTRA_END, e.endAt);
            i.putExtra(com.backmo.scheduleassistant.ui.event.EventDetailActivity.EXTRA_ALLDAY, e.allDay);
            i.putExtra(com.backmo.scheduleassistant.ui.event.EventDetailActivity.EXTRA_LOCATION, e.location);
            startActivity(i);
        }));
        repository.getAllHabits().observe(getViewLifecycleOwner(), habits -> {
            currentHabits.clear();
            if (habits != null) currentHabits.addAll(habits);
            renderCombined();
        });
        root.findViewById(R.id.btn_search_top).setOnClickListener(v -> {
            android.content.Intent i = new android.content.Intent(requireContext(), com.backmo.scheduleassistant.ui.event.EventSearchActivity.class);
            startActivity(i);
        });
        root.findViewById(R.id.btn_more_top).setOnClickListener(v -> showToolsSheet());
        setTodayAsSelected();
        updateMonthLabel();
        loadMonthIndicators(dayAdapter);
        loadEventsForSelected();
        return root;
    }

    private List<String> generateDays() {
        List<String> days = new ArrayList<>();
        Calendar cal = (Calendar) monthCal.clone();
        int firstWeekdayOfMonth = cal.get(Calendar.DAY_OF_WEEK) - 1;
        int maxDay = cal.getActualMaximum(Calendar.DAY_OF_MONTH);
        for (int i = 0; i < firstWeekdayOfMonth; i++) {
            days.add("");
        }
        for (int d = 1; d <= maxDay; d++) {
            days.add(String.valueOf(d));
        }
        return days;
    }

    private void setTodayAsSelected() {
        Calendar today = Calendar.getInstance();
        if (today.get(Calendar.MONTH) == monthCal.get(Calendar.MONTH) &&
                today.get(Calendar.YEAR) == monthCal.get(Calendar.YEAR)) {
            todayDay = today.get(Calendar.DAY_OF_MONTH);
            setSelectedDay(todayDay);
            selectedDay = todayDay;
        } else {
            todayDay = -1;
            setSelectedDay(1);
            selectedDay = 1;
        }
    }

    private void setSelectedDay(int day) {
        Calendar c = (Calendar) monthCal.clone();
        c.set(Calendar.DAY_OF_MONTH, day);
        c.set(Calendar.HOUR_OF_DAY, 0);
        c.set(Calendar.MINUTE, 0);
        c.set(Calendar.SECOND, 0);
        c.set(Calendar.MILLISECOND, 0);
        selectedStart = c.getTimeInMillis();
        c.add(Calendar.DAY_OF_MONTH, 1);
        selectedEnd = c.getTimeInMillis();
    }

    private void loadEventsForSelected() {
        repository.getEventsForDay(selectedStart, selectedEnd).observe(getViewLifecycleOwner(), events -> {
            currentEvents.clear();
            if (events != null) currentEvents.addAll(events);
            renderCombined();
        });
    }

    private void showDatePicker() {
        Calendar c = Calendar.getInstance();
        android.app.DatePickerDialog dp = new android.app.DatePickerDialog(requireContext(),
                (view, year, month, dayOfMonth) -> {
                    monthCal.set(Calendar.YEAR, year);
                    monthCal.set(Calendar.MONTH, month);
                    monthCal.set(Calendar.DAY_OF_MONTH, 1);
                    RecyclerView grid = requireView().findViewById(R.id.calendar_grid);
                    refreshGridDays(grid);
                    setSelectedDay(dayOfMonth);
                    selectedDay = dayOfMonth;
                    loadEventsForSelected();
                },
                c.get(Calendar.YEAR),
                c.get(Calendar.MONTH),
                c.get(Calendar.DAY_OF_MONTH));
        dp.show();
    }

    private void showToolsSheet() {
        com.google.android.material.bottomsheet.BottomSheetDialog dialog = new com.google.android.material.bottomsheet.BottomSheetDialog(requireContext());
        View content = LayoutInflater.from(requireContext()).inflate(R.layout.bottom_sheet_tools, null);
        dialog.setContentView(content);
        content.findViewById(R.id.btn_jump).setOnClickListener(v -> {
            dialog.dismiss();
            showDatePicker();
        });
        content.findViewById(R.id.btn_week).setOnClickListener(v -> {
            dialog.dismiss();
            android.content.Intent i = new android.content.Intent(requireContext(), com.backmo.scheduleassistant.ui.week.WeekActivity.class);
            startActivity(i);
        });
        content.findViewById(R.id.btn_search).setOnClickListener(v -> {
            dialog.dismiss();
            android.content.Intent i = new android.content.Intent(requireContext(), com.backmo.scheduleassistant.ui.event.EventSearchActivity.class);
            startActivity(i);
        });
        content.findViewById(R.id.btn_habits).setOnClickListener(v -> {
            dialog.dismiss();
            android.content.Intent i = new android.content.Intent(requireContext(), com.backmo.scheduleassistant.ui.habit.HabitListActivity.class);
            startActivity(i);
        });
        content.findViewById(R.id.btn_events).setOnClickListener(v -> {
            dialog.dismiss();
            android.content.Intent i = new android.content.Intent(requireContext(), com.backmo.scheduleassistant.ui.event.EventListActivity.class);
            startActivity(i);
        });
        dialog.show();
    }

    private void renderCombined() {
        List<com.backmo.scheduleassistant.data.db.EventEntity> merged = new java.util.ArrayList<>(currentEvents);
        for (com.backmo.scheduleassistant.data.db.HabitEntity h : currentHabits) {
            java.util.Calendar t = java.util.Calendar.getInstance();
            t.setTimeInMillis(selectedStart);
            java.util.Calendar time = java.util.Calendar.getInstance();
            time.setTimeInMillis(h.timeOfDay);
            t.set(java.util.Calendar.HOUR_OF_DAY, time.get(java.util.Calendar.HOUR_OF_DAY));
            t.set(java.util.Calendar.MINUTE, time.get(java.util.Calendar.MINUTE));
            t.set(java.util.Calendar.SECOND, 0);
            t.set(java.util.Calendar.MILLISECOND, 0);
            long start = t.getTimeInMillis();
            long end = start + 30 * 60 * 1000;
            com.backmo.scheduleassistant.data.db.EventEntity e = new com.backmo.scheduleassistant.data.db.EventEntity();
            e.title = h.name;
            e.category = "习惯";
            e.allDay = false;
            e.startAt = start;
            e.endAt = end;
            e.remindOffsetMinutes = h.remindOffsetMinutes;
            e.remindChannel = h.remindChannel;
            e.repeatRule = "daily";
            e.location = "";
            e.notes = "HABIT-" + h.id;
            merged.add(e);
        }
        merged.sort((a, b) -> java.lang.Long.compare(a.startAt, b.startAt));
        EventAdapter adapter = (EventAdapter) eventList.getAdapter();
        adapter.setItems(merged);
        boolean empty = merged.isEmpty();
        tvEmpty.setVisibility(empty ? View.VISIBLE : View.GONE);
        eventList.setVisibility(empty ? View.GONE : View.VISIBLE);
    }

    private void refreshGridDays(RecyclerView grid) {
        DayAdapter adapter = (DayAdapter) grid.getAdapter();
        adapter.days.clear();
        adapter.days.addAll(generateDays());
        updateMonthLabel();
        loadMonthIndicators(adapter);
        adapter.notifyDataSetChanged();
    }

    private void updateMonthLabel() {
        SimpleDateFormat fmt = new SimpleDateFormat("yyyy年MM月", Locale.getDefault());
        tvMonth.setText(fmt.format(monthCal.getTime()));
    }

    private void loadMonthIndicators(DayAdapter adapter) {
        Calendar start = (Calendar) monthCal.clone();
        start.set(Calendar.HOUR_OF_DAY, 0);
        start.set(Calendar.MINUTE, 0);
        start.set(Calendar.SECOND, 0);
        start.set(Calendar.MILLISECOND, 0);
        Calendar end = (Calendar) monthCal.clone();
        end.add(Calendar.MONTH, 1);
        end.set(Calendar.DAY_OF_MONTH, 1);
        repository.getEventsBetween(start.getTimeInMillis(), end.getTimeInMillis()).observe(getViewLifecycleOwner(), events -> {
            dayCounts.clear();
            if (events != null) {
                Calendar tmp = (Calendar) monthCal.clone();
                for (EventEntity e : events) {
                    tmp.setTimeInMillis(e.startAt);
                    if (tmp.get(Calendar.MONTH) == monthCal.get(Calendar.MONTH) &&
                            tmp.get(Calendar.YEAR) == monthCal.get(Calendar.YEAR)) {
                        int d = tmp.get(Calendar.DAY_OF_MONTH);
                        dayCounts.put(d, (dayCounts.containsKey(d) ? dayCounts.get(d) : 0) + 1);
                    }
                }
            }
            adapter.notifyDataSetChanged();
        });
    }

    private static class DayAdapter extends RecyclerView.Adapter<DayViewHolder> {
        private final List<String> days;
        private final OnDayClickListener listener;
        private final java.util.Map<Integer, Integer> dayCounts;
        private int selectedDay = -1;
        private int todayDay = -1;

        DayAdapter(List<String> days, java.util.Map<Integer, Integer> dayCounts, OnDayClickListener listener) {
            this.days = days;
            this.listener = listener;
            this.dayCounts = dayCounts;
        }

        @NonNull
        @Override
        public DayViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_day_cell, parent, false);
            return new DayViewHolder(v);
        }

        @Override
        public void onBindViewHolder(@NonNull DayViewHolder holder, int position) {
            holder.bind(days.get(position), listener, dayCounts, selectedDay, todayDay);
        }

        @Override
        public int getItemCount() {
            return days.size();
        }

        void setSelectedDay(int day) {
            this.selectedDay = day;
            notifyDataSetChanged();
        }

        void setTodayDay(int day) {
            this.todayDay = day;
        }
    }

    private static class DayViewHolder extends RecyclerView.ViewHolder {
        private final com.google.android.material.card.MaterialCardView card;
        private final TextView tvDay;
        private final View dot;

        DayViewHolder(@NonNull View itemView) {
            super(itemView);
            this.card = itemView.findViewById(R.id.card);
            this.tvDay = itemView.findViewById(R.id.tv_day);
            this.dot = itemView.findViewById(R.id.dot);
        }

        void bind(String dayText, OnDayClickListener listener, java.util.Map<Integer, Integer> dayCounts, int selectedDay, int todayDay) {
            if (dayText.isEmpty()) {
                tvDay.setText("");
                dot.setVisibility(View.INVISIBLE);
                card.setClickable(false);
                card.setEnabled(false);
                card.setForeground(null);
                card.setBackground(null);
            } else {
                tvDay.setText(dayText);
                int day = Integer.parseInt(dayText);
                Integer count = dayCounts.get(day);
                dot.setVisibility(count != null && count > 0 ? View.VISIBLE : View.INVISIBLE);
                if (day == selectedDay) {
                    card.setBackground(androidx.core.content.ContextCompat.getDrawable(itemView.getContext(), R.drawable.day_selected_bg));
                } else if (day == todayDay) {
                    card.setBackground(androidx.core.content.ContextCompat.getDrawable(itemView.getContext(), R.drawable.day_today_bg));
                } else {
                    card.setBackground(null);
                }
                card.setOnClickListener(v -> {
                    try {
                        listener.onDayClicked(day);
                    } catch (Exception ignored) { }
                });
            }
        }
    }

    private interface OnDayClickListener {
        void onDayClicked(int day);
    }

    private static class EventAdapter extends RecyclerView.Adapter<EventViewHolder> {
        private final List<EventEntity> items;
        private final OnEventClickListener listener;
        private final java.util.HashSet<Integer> expanded = new java.util.HashSet<>();

        EventAdapter(List<EventEntity> items, OnEventClickListener listener) {
            this.items = items;
            this.listener = listener;
        }

        void setItems(List<EventEntity> newItems) {
            this.items.clear();
            if (newItems != null) this.items.addAll(newItems);
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
            boolean isExpanded = expanded.contains(position);
            holder.bind(items.get(position), listener, isExpanded, () -> {
                if (isExpanded) {
                    expanded.remove(position);
                } else {
                    expanded.add(position);
                }
                notifyItemChanged(position);
            });
        }

        @Override
        public int getItemCount() {
            return items.size();
        }
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
        private final View btnCheckIn;
        private final SimpleDateFormat fmt = new SimpleDateFormat("HH:mm", Locale.getDefault());

        EventViewHolder(@NonNull View itemView) {
            super(itemView);
            this.title = itemView.findViewById(R.id.tv_title);
            this.subtitle = itemView.findViewById(R.id.tv_meta);
            this.colorBar = itemView.findViewById(R.id.color_bar);
            this.badge = itemView.findViewById(R.id.tv_badge);
            this.expandContainer = itemView.findViewById(R.id.expand_container);
            this.tvNotes = itemView.findViewById(R.id.tv_notes);
            this.tvRepeat = itemView.findViewById(R.id.tv_repeat);
            this.btnEdit = itemView.findViewById(R.id.btn_edit);
            this.btnDelete = itemView.findViewById(R.id.btn_delete);
            this.btnCheckIn = itemView.findViewById(R.id.btn_checkin);
        }

        void bind(EventEntity e, OnEventClickListener listener, boolean expanded, Runnable toggle) {
            title.setText(e.title);
            String time = e.allDay ? "全天" :
                    fmt.format(new java.util.Date(e.startAt)) + " - " + fmt.format(new java.util.Date(e.endAt));
            subtitle.setText(time + (TextUtils.isEmpty(e.location) ? "" : (" · " + e.location)));
            itemView.setOnClickListener(v -> toggle.run());
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
            expandContainer.setVisibility(expanded ? View.VISIBLE : View.GONE);
            if ("习惯".equals(e.category)) {
                tvNotes.setVisibility(View.GONE);
                tvRepeat.setVisibility(View.GONE);
                btnEdit.setVisibility(View.GONE);
                btnDelete.setVisibility(View.GONE);
                btnCheckIn.setVisibility(View.VISIBLE);
                btnCheckIn.setOnClickListener(v -> {
                    if (!TextUtils.isEmpty(e.notes) && e.notes.startsWith("HABIT-")) {
                        try {
                            long hid = Long.parseLong(e.notes.substring(6));
                            new com.backmo.scheduleassistant.data.ScheduleRepository(itemView.getContext()).checkInHabitById(hid);
                            android.widget.Toast.makeText(itemView.getContext(), "已打卡", android.widget.Toast.LENGTH_SHORT).show();
                        } catch (Exception ex) {
                            android.widget.Toast.makeText(itemView.getContext(), "打卡失败", android.widget.Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            } else {
                tvNotes.setVisibility(View.VISIBLE);
                tvRepeat.setVisibility(View.VISIBLE);
                btnEdit.setVisibility(View.VISIBLE);
                btnDelete.setVisibility(View.VISIBLE);
                btnCheckIn.setVisibility(View.GONE);
                tvNotes.setText(TextUtils.isEmpty(e.notes) ? "无备注" : e.notes);
                tvRepeat.setText("重复：" + repeatText(e.repeatRule));
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
            }
            btnDelete.setOnClickListener(v -> {
                com.backmo.scheduleassistant.data.ScheduleRepository repo = new com.backmo.scheduleassistant.data.ScheduleRepository(itemView.getContext());
                if ("习惯".equals(e.category) && !TextUtils.isEmpty(e.notes) && e.notes.startsWith("HABIT-")) {
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
            if (k.equals("习惯")) return 0xFFE91E63;
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

    private interface OnEventClickListener {
        void onClick(EventEntity e);
    }

    private static class SpacingDecoration extends RecyclerView.ItemDecoration {
        private final int space;
        SpacingDecoration(int dp) {
            this.space = dp;
        }
        @Override
        public void getItemOffsets(android.graphics.Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
            outRect.set(space, space, space, space);
        }
    }
}

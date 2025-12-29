package com.backmo.scheduleassistant.ui.event;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SimpleAdapter;

import androidx.appcompat.app.AppCompatActivity;

import com.backmo.scheduleassistant.R;
import com.backmo.scheduleassistant.data.ScheduleRepository;
import com.backmo.scheduleassistant.data.db.EventEntity;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class EventSearchActivity extends AppCompatActivity {
    private ScheduleRepository repository;
    private SimpleAdapter adapter;
    private List<Map<String, String>> data = new ArrayList<>();
    private final SimpleDateFormat fmt = new SimpleDateFormat("MM-dd HH:mm", Locale.getDefault());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event_search);
        repository = new ScheduleRepository(this);
        EditText et = findViewById(R.id.et_query);
        ListView lv = findViewById(R.id.lv_results);
        adapter = new SimpleAdapter(this, data, android.R.layout.simple_list_item_2,
                new String[]{"title", "sub"}, new int[]{android.R.id.text1, android.R.id.text2});
        lv.setAdapter(adapter);
        et.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}
            @Override public void afterTextChanged(Editable s) {
                String q = s.toString().trim();
                repository.searchEvents(q).observe(EventSearchActivity.this, events -> {
                    data.clear();
                    for (EventEntity e : events) {
                        Map<String, String> m = new HashMap<>();
                        m.put("title", e.title);
                        m.put("sub", fmt.format(new java.util.Date(e.startAt)));
                        data.add(m);
                    }
                    adapter.notifyDataSetChanged();
                });
            }
        });
    }
}


package com.backmo.scheduleassistant.ui.voice;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.backmo.scheduleassistant.data.ScheduleRepository;
import com.backmo.scheduleassistant.data.db.EventEntity;
import com.backmo.scheduleassistant.reminder.ReminderScheduler;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class VoiceQuickAddActivity extends AppCompatActivity {
    private static final int REQ_RECORD = 2001;
    private static final int REQ_SPEECH = 2002;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.RECORD_AUDIO}, REQ_RECORD);
        } else {
            startVoice();
        }
    }

    private void startVoice() {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, "zh-CN");
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "请说出日程，如：2025-01-01 09:00 早会");
        try {
            startActivityForResult(intent, REQ_SPEECH);
        } catch (Exception e) {
            Toast.makeText(this, "设备不支持语音输入", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQ_RECORD) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startVoice();
            } else {
                Toast.makeText(this, "需要麦克风权限以使用语音输入", Toast.LENGTH_SHORT).show();
                finish();
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQ_SPEECH && resultCode == RESULT_OK && data != null) {
            ArrayList<String> results = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
            String text = (results != null && !results.isEmpty()) ? results.get(0) : "";
            createEventFromText(text);
        } else {
            finish();
        }
    }

    private void createEventFromText(String text) {
        String title;
        long startAt;
        Pattern p = Pattern.compile("(?:(\\d{4})-(\\d{2})-(\\d{2})[ T](\\d{2}):(\\d{2}))\\s+(.*)");
        Matcher m = p.matcher(text == null ? "" : text.trim());
        if (m.matches()) {
            int y = Integer.parseInt(m.group(1));
            int mo = Integer.parseInt(m.group(2)) - 1;
            int d = Integer.parseInt(m.group(3));
            int h = Integer.parseInt(m.group(4));
            int mi = Integer.parseInt(m.group(5));
            Calendar c = Calendar.getInstance();
            c.set(y, mo, d, h, mi, 0);
            c.set(Calendar.MILLISECOND, 0);
            startAt = c.getTimeInMillis();
            title = m.group(6);
        } else {
            title = (text == null || text.isEmpty()) ? "语音新建日程" : text.trim();
            startAt = System.currentTimeMillis() + 2 * 60 * 60 * 1000;
        }
        ScheduleRepository repo = new ScheduleRepository(this);
        EventEntity e = new EventEntity();
        e.title = title;
        e.category = "语音";
        e.allDay = false;
        e.startAt = startAt;
        e.endAt = startAt + 60 * 60 * 1000;
        e.remindOffsetMinutes = 5;
        e.remindChannel = "notification";
        e.repeatRule = "none";
        e.location = "";
        e.notes = "语音快速添加";
        repo.insertEvent(e);
        ReminderScheduler.scheduleNotification(this, e);
        Toast.makeText(this, "已创建日程：" + title, Toast.LENGTH_SHORT).show();
        finish();
    }
}


package com.backmo.scheduleassistant;

import android.os.Bundle;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.backmo.scheduleassistant.ui.calendar.CalendarFragment;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import androidx.appcompat.app.AlertDialog;
import com.backmo.scheduleassistant.util.PermissionUtils;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        FragmentManager fm = getSupportFragmentManager();
        Fragment existing = fm.findFragmentById(R.id.fragment_container);
        if (existing == null) {
            FragmentTransaction ft = fm.beginTransaction();
            ft.replace(R.id.fragment_container, new CalendarFragment());
            ft.commit();
        }
        FloatingActionButton fab = findViewById(R.id.fab_add);
        fab.setOnClickListener(v -> showActionSheet());
        askNotificationPermissionIfNeeded();
    }

    private void showActionSheet() {
        BottomSheetDialog dialog = new BottomSheetDialog(this);
        android.view.View content = getLayoutInflater().inflate(R.layout.bottom_sheet_actions, null);
        dialog.setContentView(content);
        content.findViewById(R.id.btn_new_event).setOnClickListener(v -> {
            dialog.dismiss();
            android.content.Intent i = new android.content.Intent(this, com.backmo.scheduleassistant.ui.event.EventEditActivity.class);
            startActivity(i);
        });
        content.findViewById(R.id.btn_new_habit).setOnClickListener(v -> {
            dialog.dismiss();
            android.content.Intent i = new android.content.Intent(this, com.backmo.scheduleassistant.ui.habit.HabitEditActivity.class);
            startActivity(i);
        });
        content.findViewById(R.id.btn_import_clipboard).setOnClickListener(v -> {
            dialog.dismiss();
            importFromClipboard();
        });
        content.findViewById(R.id.btn_smart_plan).setOnClickListener(v -> {
            dialog.dismiss();
            android.content.Intent i = new android.content.Intent(this, com.backmo.scheduleassistant.ui.smart.SmartPlanActivity.class);
            startActivity(i);
        });
        dialog.show();
    }

    private void importFromClipboard() {
        android.content.ClipboardManager cm = (android.content.ClipboardManager) getSystemService(android.content.Context.CLIPBOARD_SERVICE);
        if (cm == null || !cm.hasPrimaryClip()) {
            android.widget.Toast.makeText(this, "剪贴板为空", android.widget.Toast.LENGTH_SHORT).show();
            return;
        }
        CharSequence cs = cm.getPrimaryClip().getItemAt(0).coerceToText(this);
        String text = cs == null ? "" : cs.toString().trim();
        if (text.isEmpty()) {
            android.widget.Toast.makeText(this, "剪贴板为空", android.widget.Toast.LENGTH_SHORT).show();
            return;
        }
        java.util.regex.Pattern p = java.util.regex.Pattern.compile("(?:(\\d{4})-(\\d{2})-(\\d{2})[ T](\\d{2}):(\\d{2}))\\s+(.*)");
        java.util.regex.Matcher m = p.matcher(text);
        long startAt;
        String title;
        if (m.matches()) {
            int y = Integer.parseInt(m.group(1));
            int mo = Integer.parseInt(m.group(2)) - 1;
            int d = Integer.parseInt(m.group(3));
            int h = Integer.parseInt(m.group(4));
            int mi = Integer.parseInt(m.group(5));
            java.util.Calendar c = java.util.Calendar.getInstance();
            c.set(y, mo, d, h, mi, 0);
            c.set(java.util.Calendar.MILLISECOND, 0);
            startAt = c.getTimeInMillis();
            title = m.group(6);
        } else {
            title = text;
            startAt = System.currentTimeMillis() + 2 * 60 * 60 * 1000;
        }
        com.backmo.scheduleassistant.data.ScheduleRepository repo = new com.backmo.scheduleassistant.data.ScheduleRepository(this);
        com.backmo.scheduleassistant.data.db.EventEntity e = new com.backmo.scheduleassistant.data.db.EventEntity();
        e.title = title;
        e.category = "导入";
        e.allDay = false;
        e.startAt = startAt;
        e.endAt = startAt + 60 * 60 * 1000;
        e.remindOffsetMinutes = 5;
        e.remindChannel = "notification";
        e.repeatRule = "none";
        e.location = "";
        e.notes = "剪贴板导入";
        repo.insertEvent(e);
        com.backmo.scheduleassistant.reminder.ReminderScheduler.scheduleNotification(this, e);
        android.widget.Toast.makeText(this, "已从剪贴板创建日程", android.widget.Toast.LENGTH_SHORT).show();
    }

    private void askNotificationPermissionIfNeeded() {
        if (!PermissionUtils.hasPostNotifications(this)) {
            new AlertDialog.Builder(this)
                    .setTitle("开启通知权限")
                    .setMessage("开启通知权限以接收日程和习惯提醒。")
                    .setPositiveButton("去开启", (d, w) -> PermissionUtils.requestPostNotifications(this))
                    .setNegativeButton("稍后", null)
                    .show();
        }
    }
}

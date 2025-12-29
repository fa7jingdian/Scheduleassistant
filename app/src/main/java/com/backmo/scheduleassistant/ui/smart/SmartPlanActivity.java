package com.backmo.scheduleassistant.ui.smart;

import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class SmartPlanActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        TextView tv = new TextView(this);
        tv.setText("智能规划占位：后续提供空闲时间推荐与自动安排。");
        setContentView(tv);
    }
}


package com.backmo.scheduleassistant.data.db;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "events")
public class EventEntity {
    @PrimaryKey(autoGenerate = true)
    public long id;
    public String title;
    public String category;
    public boolean allDay;
    public long startAt;
    public long endAt;
    public int remindOffsetMinutes;
    public String remindChannel;
    public String repeatRule;
    public String location;
    public String notes;
}


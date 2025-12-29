package com.backmo.scheduleassistant.data.db;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "habits")
public class HabitEntity {
    @PrimaryKey(autoGenerate = true)
    public long id;
    public String name;
    public String category;
    public long timeOfDay;
    public int remindOffsetMinutes;
    public String remindChannel;
    public int streak;
    public long lastCheckInDate;
}


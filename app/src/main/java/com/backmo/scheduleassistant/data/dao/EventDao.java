package com.backmo.scheduleassistant.data.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.backmo.scheduleassistant.data.db.EventEntity;

import java.util.List;

@Dao
public interface EventDao {
    @Insert
    long insert(EventEntity event);

    @Update
    int update(EventEntity event);

    @Delete
    int delete(EventEntity event);

    @Query("SELECT * FROM events WHERE startAt < :dayEnd AND endAt > :dayStart ORDER BY startAt ASC")
    LiveData<List<EventEntity>> getEventsForDay(long dayStart, long dayEnd);

    @Query("SELECT * FROM events ORDER BY startAt ASC")
    LiveData<List<EventEntity>> getAll();

    @Query("SELECT * FROM events WHERE title LIKE :pattern OR notes LIKE :pattern ORDER BY startAt ASC")
    LiveData<List<EventEntity>> search(String pattern);

    @Query("SELECT * FROM events WHERE startAt >= :start AND startAt < :end ORDER BY startAt ASC")
    LiveData<List<EventEntity>> getBetween(long start, long end);

    @Query("SELECT * FROM events WHERE startAt < :dayEnd AND endAt > :dayStart ORDER BY startAt ASC")
    List<EventEntity> getEventsForDaySync(long dayStart, long dayEnd);
}

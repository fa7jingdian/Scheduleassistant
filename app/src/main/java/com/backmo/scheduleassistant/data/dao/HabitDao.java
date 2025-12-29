package com.backmo.scheduleassistant.data.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.backmo.scheduleassistant.data.db.HabitEntity;

import java.util.List;

@Dao
public interface HabitDao {
    @Insert
    long insert(HabitEntity habit);

    @Update
    int update(HabitEntity habit);

    @Delete
    int delete(HabitEntity habit);

    @Query("SELECT * FROM habits ORDER BY id DESC")
    LiveData<List<HabitEntity>> getAll();

    @Query("DELETE FROM habits WHERE id = :id")
    int deleteById(long id);

    @Query("SELECT * FROM habits WHERE id = :id LIMIT 1")
    HabitEntity getByIdSync(long id);
}

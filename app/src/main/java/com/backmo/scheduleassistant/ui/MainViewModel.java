package com.backmo.scheduleassistant.ui;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.backmo.scheduleassistant.data.ScheduleRepository;
import com.backmo.scheduleassistant.data.db.EventEntity;

import java.util.Calendar;
import java.util.List;

public class MainViewModel extends AndroidViewModel {
    private final ScheduleRepository repository;

    public MainViewModel(@NonNull Application application) {
        super(application);
        repository = new ScheduleRepository(application);
    }

    public LiveData<List<EventEntity>> eventsForToday() {
        Calendar c = Calendar.getInstance();
        c.set(Calendar.HOUR_OF_DAY, 0);
        c.set(Calendar.MINUTE, 0);
        c.set(Calendar.SECOND, 0);
        c.set(Calendar.MILLISECOND, 0);
        long start = c.getTimeInMillis();
        c.add(Calendar.DAY_OF_MONTH, 1);
        long end = c.getTimeInMillis();
        return repository.getEventsForDay(start, end);
    }
}


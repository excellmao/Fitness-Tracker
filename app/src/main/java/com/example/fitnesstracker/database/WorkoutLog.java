package com.example.fitnesstracker.database;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "workout_logs")
public class WorkoutLog {

    @PrimaryKey(autoGenerate = true)
    public int id;

    public String date;

    public String workoutType;
    public int durationMinutes;

    public WorkoutLog(String date, String workoutType, int durationMinutes) {
        this.date = date;
        this.workoutType = workoutType;
        this.durationMinutes = durationMinutes;
    }
}

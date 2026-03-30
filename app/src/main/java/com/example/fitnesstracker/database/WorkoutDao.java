package com.example.fitnesstracker.database;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

@Dao
public interface WorkoutDao {
    @Insert
    void insertWorkout(WorkoutLog workoutLog);
    @Query("SELECT COUNT(*) FROM workout_logs WHERE date = :dateString")
    int getWorkoutCountForDate(String dateString);
}
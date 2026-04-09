package com.example.fitnesstracker.database;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

@Dao
public interface WorkoutDao {
    @Insert
    void insertWorkout(WorkoutLog workoutLog);
    @Query("SELECT COUNT(*) FROM workout_logs WHERE date = :dateString")
    int getWorkoutCountForDate(String dateString);
    @Query("SELECT COALESCE(SUM(durationMinutes), 0) FROM workout_logs WHERE date = :date")
    int getTotalDurationForDate(String date);
    @Query("SELECT * FROM workout_logs ORDER BY date DESC")
    List<WorkoutLog> getAllWorkoutLogs();
}
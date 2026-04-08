package com.example.fitnesstracker.database;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import java.util.List;
import com.example.fitnesstracker.homescreen.models.WorkoutRoutine;

@Dao
public interface RoutineDao {
    @Insert
    void insertRoutine(WorkoutRoutine routine);

    @Query("SELECT * FROM workout_routines")
    List<WorkoutRoutine> getAllRoutines();

    @Query("SELECT COUNT(*) FROM workout_routines")
    int getRoutineCount();
}
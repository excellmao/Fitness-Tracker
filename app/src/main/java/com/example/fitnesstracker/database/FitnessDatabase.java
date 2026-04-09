package com.example.fitnesstracker.database;

import android.content.Context;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

// 1. Make sure this import matches where we moved the file!
import com.example.fitnesstracker.homescreen.models.WorkoutRoutine;

@Database(entities = {WorkoutLog.class, WorkoutRoutine.class}, version = 4, exportSchema = false)
public abstract class FitnessDatabase extends RoomDatabase {

    public abstract WorkoutDao workoutDao();

    public abstract RoutineDao routineDao();

    private static volatile FitnessDatabase INSTANCE;

    public static FitnessDatabase getInstance(Context context) {
        if (INSTANCE == null) {
            synchronized (FitnessDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                                    FitnessDatabase.class, "fitness_tracker_database")
                            .fallbackToDestructiveMigration() // This safely handles the version bump
                            .build();
                }
            }
        }
        return INSTANCE;
    }
}
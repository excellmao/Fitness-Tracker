package com.example.fitnesstracker.database;

import android.content.Context;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

@Database(entities = {WorkoutLog.class}, version = 1, exportSchema = false)
public abstract class FitnessDatabase extends RoomDatabase {
    public abstract WorkoutDao workoutDao();

    public static volatile FitnessDatabase INSTANCE;

    public static FitnessDatabase getInstance(Context context) {
        if (INSTANCE == null) {
            synchronized (FitnessDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                            FitnessDatabase.class, "fitness_tracker_database")
                            .fallbackToDestructiveMigration()
                            .build();
                }
            }
        }
        return INSTANCE;
    }
}

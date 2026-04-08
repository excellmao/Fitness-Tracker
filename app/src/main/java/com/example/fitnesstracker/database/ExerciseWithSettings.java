package com.example.fitnesstracker.database;

import androidx.room.ColumnInfo;

/**
 * A POJO to hold combined data from Exercise and RoutineExercise tables.
 * This is used to display exercise details within a specific routine.
 */
public class ExerciseWithSettings {
    // Fields from Exercise table
    @ColumnInfo(name = "exercise_id")
    public int exerciseId;
    public String name;
    public String category;
    public String imageUrl;
    public String level;

    // Fields from RoutineExercise table
    public int sets;
    public int reps;
    public int restSeconds;
    public int durationSeconds; // 0 = reps-based, >0 = timed
    public int orderIndex;

    @ColumnInfo(name = "re_id")
    public int routineExerciseId;

    public ExerciseWithSettings(int exerciseId, String name, String category, String imageUrl, String level,
                                int sets, int reps, int restSeconds, int durationSeconds,
                                int orderIndex, int routineExerciseId) {
        this.exerciseId = exerciseId;
        this.name = name;
        this.category = category;
        this.imageUrl = imageUrl;
        this.level = level;
        this.sets = sets;
        this.reps = reps;
        this.restSeconds = restSeconds;
        this.durationSeconds = durationSeconds;
        this.orderIndex = orderIndex;
        this.routineExerciseId = routineExerciseId;
    }
}

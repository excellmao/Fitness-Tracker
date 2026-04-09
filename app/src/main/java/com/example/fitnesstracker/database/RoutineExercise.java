package com.example.fitnesstracker.database;

import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.PrimaryKey;

@Entity(tableName = "routine_exercises",
    foreignKeys = {
        @ForeignKey(entity = Routine.class,
            parentColumns = "id",
            childColumns = "routineId",
            onDelete = ForeignKey.CASCADE),
        @ForeignKey(entity = Exercise.class,
            parentColumns = "id",
            childColumns = "exerciseId",
            onDelete = ForeignKey.CASCADE)
    })
public class RoutineExercise {
    @PrimaryKey(autoGenerate = true)
    private int id;

    private int routineId;
    private int exerciseId;
    private int sets;
    private int reps;
    private int restSeconds;
    private int durationSeconds; // 0 = reps-based, >0 = timed (seconds per set)
    private int orderIndex; // To maintain the order of exercises in a routine

    public RoutineExercise(int routineId, int exerciseId, int sets, int reps, int restSeconds, int durationSeconds, int orderIndex) {
        this.routineId = routineId;
        this.exerciseId = exerciseId;
        this.sets = sets;
        this.reps = reps;
        this.restSeconds = restSeconds;
        this.durationSeconds = durationSeconds;
        this.orderIndex = orderIndex;
    }

    // Getters and Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public int getRoutineId() { return routineId; }
    public void setRoutineId(int routineId) { this.routineId = routineId; }
    public int getExerciseId() { return exerciseId; }
    public void setExerciseId(int exerciseId) { this.exerciseId = exerciseId; }
    public int getSets() { return sets; }
    public void setSets(int sets) { this.sets = sets; }
    public int getReps() { return reps; }
    public void setReps(int reps) { this.reps = reps; }
    public int getRestSeconds() { return restSeconds; }
    public void setRestSeconds(int restSeconds) { this.restSeconds = restSeconds; }
    public int getDurationSeconds() { return durationSeconds; }
    public void setDurationSeconds(int durationSeconds) { this.durationSeconds = durationSeconds; }
    public int getOrderIndex() { return orderIndex; }
    public void setOrderIndex(int orderIndex) { this.orderIndex = orderIndex; }
}

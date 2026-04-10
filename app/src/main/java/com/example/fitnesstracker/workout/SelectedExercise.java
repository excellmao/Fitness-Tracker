package com.example.fitnesstracker.workout;

public class SelectedExercise {
    public int exerciseId;
    public String name;
    public String category;

    // Default values for a new routine
    public int sets = 3;
    public int reps = 12;
    public int restSeconds = 60;
    public int durationSeconds = 0; // for timed exercises

    public SelectedExercise(int exerciseId, String name, String category) {
        this.exerciseId = exerciseId;
        this.name = name;
        this.category = category;
    }
}
package com.example.fitnesstracker.database;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "routines")
public class Routine {
    @PrimaryKey(autoGenerate = true)
    public int id;

    public String name;
    public String description;
    public String phase; // e.g., "PHASE 02 / STRENGTH"
    public int exerciseCount;
    public int totalMinutes;
    public int totalCalories;
    public String coverImageUrl; // For that dark aesthetic background

    public Routine(String name, String description, String phase, int exerciseCount, int totalMinutes, int totalCalories, String coverImageUrl) {
        this.name = name;
        this.description = description;
        this.phase = phase;
        this.exerciseCount = exerciseCount;
        this.totalMinutes = totalMinutes;
        this.totalCalories = totalCalories;
        this.coverImageUrl = coverImageUrl;
    }

    public int getId() { return id; }
    public String getName() { return name; }
    public String getDescription() { return description; }
    public String getPhase() { return phase; }
    public int getExerciseCount() { return exerciseCount; }
    public int getTotalMinutes() { return totalMinutes; }
    public int getTotalCalories() { return totalCalories; }
    public String getCoverImageUrl() { return coverImageUrl; }
}
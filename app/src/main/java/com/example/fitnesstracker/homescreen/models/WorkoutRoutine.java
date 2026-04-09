package com.example.fitnesstracker.homescreen.models;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "workout_routines")
public class WorkoutRoutine {

    @PrimaryKey(autoGenerate = true)
    private int id;

    private String title;
    private String level;
    private int durationMinutes;
    private int caloriesBurned;
    private int imageResource;

    public WorkoutRoutine(String title, String level, int durationMinutes, int caloriesBurned, int imageResource) {
        this.title = title;
        this.level = level;
        this.durationMinutes = durationMinutes;
        this.caloriesBurned = caloriesBurned;
        this.imageResource = imageResource;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getLevel() { return level; }
    public void setLevel(String level) { this.level = level; }

    public int getDurationMinutes() { return durationMinutes; }
    public void setDurationMinutes(int durationMinutes) { this.durationMinutes = durationMinutes; }

    public int getCaloriesBurned() { return caloriesBurned; }
    public void setCaloriesBurned(int caloriesBurned) { this.caloriesBurned = caloriesBurned; }

    public int getImageResource() { return imageResource; }
    public void setImageResource(int imageResource) { this.imageResource = imageResource; }
}
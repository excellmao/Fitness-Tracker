package com.example.fitnesstracker.database;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "routines")
public class Routine {
    @PrimaryKey(autoGenerate = true)
    private int id;

    private String name;
    private String description;
    private int exerciseCount;

    public Routine(String name, String description, int exerciseCount) {
        this.name = name;
        this.description = description;
        this.exerciseCount = exerciseCount;
    }

    // Getters and Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public int getExerciseCount() { return exerciseCount; }
    public void setExerciseCount(int exerciseCount) { this.exerciseCount = exerciseCount; }
}

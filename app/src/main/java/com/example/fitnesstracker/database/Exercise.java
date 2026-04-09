package com.example.fitnesstracker.database;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "exercises")
public class Exercise {
    @PrimaryKey(autoGenerate = true)
    private int id;
    private String name;
    private String category; // e.g., Quads & Glutes
    private String level;    // e.g., Beginner
    private String imageUrl; // Thumbnail

    public Exercise(String name, String category, String level, String imageUrl) {
        this.name = name;
        this.category = category;
        this.level = level;
        this.imageUrl = imageUrl;
    }

    // Getters and Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
    public String getLevel() { return level; }
    public void setLevel(String level) { this.level = level; }
    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
}

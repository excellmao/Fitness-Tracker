package com.example.fitnesstracker.database;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "exercises")
public class Exercise {
    @PrimaryKey(autoGenerate = true)
    public int id;

    public String name;
    public String category;
    public String level;
    public String imageUrl;
    public String gifUrl; // internet db

    public Exercise(String name, String category, String level, String imageUrl, String gifUrl) {
        this.name = name;
        this.category = category;
        this.level = level;
        this.imageUrl = imageUrl;
        this.gifUrl = gifUrl;
    }

    public int getId() { return id; }
    public String getName() { return name; }
    public String getCategory() { return category; }
    public String getLevel() { return level; }
    public String getImageUrl() { return imageUrl; }
    public String getGifUrl() { return gifUrl; }
}
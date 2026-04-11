package com.example.fitnesstracker.database;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "food_logs")
public class FoodLog {
    @PrimaryKey(autoGenerate = true)
    public int id;
    public String name;
    public int calories;
    public String mealType; // Breakfast, Lunch, Dinner, Snack
    public int quantity;
    public String date; // YYYY-MM-DD

    public FoodLog(String name, int calories, String mealType, int quantity, String date) {
        this.name = name;
        this.calories = calories;
        this.mealType = mealType;
        this.quantity = quantity;
        this.date = date;
    }
}
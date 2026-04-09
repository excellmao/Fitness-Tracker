package com.example.fitnesstracker.profile.models;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "user_metrics")
public class UserMetricLog {

    @PrimaryKey(autoGenerate = true)
    public int id;

    public float dayIndex; // To map nicely to the X-Axis of the chart
    public float weight;
    public float calories;

    public UserMetricLog(float dayIndex, float weight, float calories) {
        this.dayIndex = dayIndex;
        this.weight = weight;
        this.calories = calories;
    }
}
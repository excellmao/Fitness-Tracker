package com.example.fitnesstracker.database;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "water_logs")
public class WaterLog {
    @PrimaryKey(autoGenerate = true)
    public int id;
    public int amountMl;
    public String date; // YYYY-MM-DD

    public WaterLog(int amountMl, String date) {
        this.amountMl = amountMl;
        this.date = date;
    }
}
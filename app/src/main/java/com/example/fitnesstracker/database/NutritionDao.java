package com.example.fitnesstracker.database;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;
import androidx.room.Delete;

import java.util.List;

@Dao
public interface NutritionDao {
    @Insert
    void insertFood(FoodLog food);

    @Update
    void updateFood(FoodLog food);

    @Delete
    void deleteFood(FoodLog food);

    @Query("SELECT * FROM food_logs WHERE date = :date")
    LiveData<List<FoodLog>> getFoodLogsByDate(String date);

    @Query("SELECT * FROM food_logs WHERE date = :date")
    List<FoodLog> getFoodLogsByDateSync(String date);

    @Query("SELECT SUM(calories * quantity) FROM food_logs WHERE date = :date")
    LiveData<Integer> getTotalCaloriesByDate(String date);

    @Insert
    void insertWater(WaterLog water);

    @Query("SELECT SUM(amountMl) FROM water_logs WHERE date = :date")
    LiveData<Integer> getTotalWaterByDate(String date);

    @Query("SELECT SUM(durationMinutes * 10) FROM workout_logs WHERE date = :date")
    LiveData<Integer> getBurnedCaloriesByDate(String date);

    @Query("SELECT COUNT(*) FROM food_logs WHERE date = :date")
    int getFoodLogCountByDate(String date);

    @Query("SELECT SUM(amountMl) FROM water_logs WHERE date = :date")
    int getTotalWaterByDateSync(String date);
}
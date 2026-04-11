package com.example.fitnesstracker.database;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import java.util.List;
import com.example.fitnesstracker.profile.models.UserMetricLog;

@Dao
public interface MetricDao {
    @Insert
    void insertMetric(UserMetricLog metric);

    // This grabs the whole history to draw the chart
    @Query("SELECT * FROM user_metrics ORDER BY dayIndex ASC")
    List<UserMetricLog> getAllMetrics();

    // Your Run module will use this later to get the user's current weight for calorie math!
    @Query("SELECT weight FROM user_metrics ORDER BY dayIndex DESC LIMIT 1")
    float getLatestWeight();

    @Query("SELECT COUNT(*) FROM user_metrics")
    int getMetricCount();

    @Query("SELECT weight FROM user_metrics ORDER BY dayIndex DESC LIMIT 1")
    Float getLatestWeightSync();
}
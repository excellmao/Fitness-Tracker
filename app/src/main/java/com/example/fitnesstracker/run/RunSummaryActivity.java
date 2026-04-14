package com.example.fitnesstracker.run;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.example.fitnesstracker.MainActivity;
import com.example.fitnesstracker.R;

// Google Maps Imports
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import java.util.ArrayList;

public class RunSummaryActivity extends AppCompatActivity {

    private MapView mapLite;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_run_summary);

        Intent incomingIntent = getIntent();

        // 1. Setup the Text Views
        TextView tvSumDuration = findViewById(R.id.tvSumDuration);
        TextView tvSumDistance = findViewById(R.id.tvSumDistance);
        TextView tvSumCalories = findViewById(R.id.tvSumCalories);
        TextView tvSumPace     = findViewById(R.id.tvSumPace);

        if(tvSumDuration != null) tvSumDuration.setText(incomingIntent.getStringExtra("duration_key"));
        if(tvSumDistance != null) tvSumDistance.setText(incomingIntent.getStringExtra("distance_key"));
        if(tvSumCalories != null) tvSumCalories.setText(incomingIntent.getStringExtra("calories_key"));
        if(tvSumPace != null) tvSumPace.setText(incomingIntent.getStringExtra("pace_key"));

        // 2. Setup the Lite Map
        mapLite = findViewById(R.id.mapLite);
        mapLite.onCreate(savedInstanceState);

        // Grab the GPS coordinates we passed
        ArrayList<LatLng> route = incomingIntent.getParcelableArrayListExtra("route_points");

        mapLite.getMapAsync(map -> {
            // Make it dark mode!
            map.setMapStyle(MapStyleOptions.loadRawResourceStyle(this, R.raw.map_style_dark));

            // Draw the line and center the camera
            if (route != null && !route.isEmpty()) {
                map.addPolyline(new PolylineOptions().addAll(route).color(0xFFD4FF00).width(12f));

                // Calculate the boundaries so the camera zooms perfectly to fit the whole run
                LatLngBounds.Builder builder = new LatLngBounds.Builder();
                for (LatLng point : route) {
                    builder.include(point);
                }
                // Padding of 50 pixels so the line doesn't touch the very edges
                map.moveCamera(CameraUpdateFactory.newLatLngBounds(builder.build(), 50));
            }

            // Bonus: By default, clicking a "Lite Mode" map opens the official Google Maps app!
        });

        // 3. Setup the Done Button
        Button btnDone = findViewById(R.id.btnDone);
        btnDone.setOnClickListener(v -> {

            // 1. Extract and convert the text back into numbers
            String durStr = incomingIntent.getStringExtra("duration_key"); // e.g., "00:42:15"
            String calStr = incomingIntent.getStringExtra("calories_key"); // e.g., "420 kcal"

            int minutes = 0;
            if (durStr != null && durStr.contains(":")) {
                String[] parts = durStr.split(":");
                // Convert Hours to mins, add Mins
                minutes = (Integer.parseInt(parts[0]) * 60) + Integer.parseInt(parts[1]);
            }

            int calories = 0;
            if (calStr != null) {
                // Strips out " kcal" and leaves just the number
                calories = Integer.parseInt(calStr.replaceAll("[^0-9]", ""));
            }

            final int finalMins = minutes;
            final int finalCals = calories;

            // 2. Save to Database in the background
            new Thread(() -> {
                com.example.fitnesstracker.database.FitnessDatabase db = com.example.fitnesstracker.database.FitnessDatabase.getInstance(this);
                String todayDate = java.time.LocalDate.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd"));

                // Insert as a "Run"
                db.workoutDao().insertWorkout(new com.example.fitnesstracker.database.WorkoutLog(todayDate, "Run", finalMins, finalCals));

                // 3. Go back to Home
                runOnUiThread(() -> {
                    Intent intent = new Intent(this, MainActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                    startActivity(intent);
                    finish();
                });
            }).start();
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mapLite != null) mapLite.onResume();
    }
    @Override
    protected void onPause() {
        super.onPause();
        if (mapLite != null) mapLite.onPause();
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mapLite != null) mapLite.onDestroy();
    }
    @Override
    public void onLowMemory() {
        super.onLowMemory();
        if (mapLite != null) mapLite.onLowMemory();
    }
}
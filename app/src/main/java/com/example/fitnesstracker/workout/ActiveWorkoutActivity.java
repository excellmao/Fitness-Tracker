package com.example.fitnesstracker.workout;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.fitnesstracker.R;
import com.example.fitnesstracker.database.FitnessDatabase;
import com.example.fitnesstracker.database.Routine;
import com.example.fitnesstracker.database.WorkoutLog;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ActiveWorkoutActivity extends AppCompatActivity {

    private TextView tvActiveRoutineName, tvTimer;
    private Button btnFinishWorkout;

    private int routineId;
    private String routineName = "Custom Workout";
    private FitnessDatabase db;

    // Stopwatch logic
    private int elapsedSeconds = 0;
    private boolean isRunning = true;
    private final Handler timerHandler = new Handler(Looper.getMainLooper());
    private final Runnable timerRunnable = new Runnable() {
        @Override
        public void run() {
            if (isRunning) {
                elapsedSeconds++;
                int minutes = elapsedSeconds / 60;
                int seconds = elapsedSeconds % 60;
                // Formats the timer to look like 05:09
                tvTimer.setText(String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds));
                timerHandler.postDelayed(this, 1000); // Run again in 1 second
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_active_workout);

        tvActiveRoutineName = findViewById(R.id.tvActiveRoutineName);
        tvTimer = findViewById(R.id.tvTimer);
        btnFinishWorkout = findViewById(R.id.btnFinishWorkout);

        db = FitnessDatabase.getInstance(this);
        routineId = getIntent().getIntExtra("routine_id", -1);

        loadRoutineData();

        // Start the stopwatch
        timerHandler.postDelayed(timerRunnable, 1000);

        btnFinishWorkout.setOnClickListener(v -> finishAndLogWorkout());
    }

    private void loadRoutineData() {
        if (routineId == -1) return;

        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.execute(() -> {
            List<Routine> routines = db.routineDao().getAllRoutines();
            for (Routine r : routines) {
                if (r.getId() == routineId) {
                    routineName = r.getName();
                    break;
                }
            }
            runOnUiThread(() -> tvActiveRoutineName.setText(routineName));
        });
    }

    private void finishAndLogWorkout() {
        // 1. Stop the clock
        isRunning = false;
        timerHandler.removeCallbacks(timerRunnable);

        // 2. Do the math
        int durationMinutes = Math.max(1, elapsedSeconds / 60); // Minimum 1 min
        int estimatedCalories = durationMinutes * 8; // Roughly 8 kcal per minute of lifting

        String todayDate = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());

        // 3. THE MAGIC BRIDGE: Save it to your History database!
        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.execute(() -> {
            db.workoutDao().insertWorkout(
                    new WorkoutLog(todayDate, routineName, durationMinutes, estimatedCalories)
            );

            runOnUiThread(() -> {
                Toast.makeText(this, "Workout saved to History!", Toast.LENGTH_SHORT).show();
                finish(); // This closes the activity and sends them back to the app
            });
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        isRunning = false;
        timerHandler.removeCallbacks(timerRunnable);
    }
}
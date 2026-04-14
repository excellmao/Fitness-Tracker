package com.example.fitnesstracker.workout;

import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.bumptech.glide.Glide;
import com.example.fitnesstracker.R;
import com.example.fitnesstracker.database.ExerciseWithSettings;
import com.example.fitnesstracker.database.FitnessDatabase;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class ActiveWorkoutActivity extends AppCompatActivity {

    // UI Containers
    private ConstraintLayout clActive, clRest, clSummary;

    // Active State UI
    private ImageView ivActiveGif;
    private TextView tvActiveName, tvActiveSetInfo, tvActiveNextUp, tvActiveTimer;
    private ImageButton btnPause;

    // Rest State UI
    private TextView tvRestTimer, tvRestNextName, tvRestPhase;
    private ImageView ivRestNextThumb;

    // Summary State UI
    private TextView tvSummaryTime, tvSummaryKcal;

    // Workout Data
    private List<ExerciseWithSettings> exercises = new ArrayList<>();
    private int currentIndex = 0;
    private int currentSet = 1;
    private int routineId;
    private String routineName = "Custom Workout";

    // Timers & Stats
    private CountDownTimer currentTimer;
    private long timeLeftInMillis;
    private boolean isTimerRunning = false;
    private long totalSessionStartTime; // To calculate total workout time

    // Enums for clarity
    private enum State { ACTIVE, REST, SUMMARY }
    private State currentState;
    private float userWeightKg = 70f;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_active_workout);

        routineId = getIntent().getIntExtra("routine_id", -1);
        totalSessionStartTime = System.currentTimeMillis();

        initViews();
        setupClickListeners();
        fetchWorkoutData();

        new Thread(() -> {
            Float realWeight = com.example.fitnesstracker.database.FitnessDatabase.getInstance(this)
                    .metricDao().getLatestWeightSync();

            if (realWeight != null && realWeight > 0) {
                userWeightKg = realWeight;
            }
        }).start();
    }

    private void initViews() {
        // Containers
        clActive = findViewById(R.id.clStateActive);
        clRest = findViewById(R.id.clStateRest);
        clSummary = findViewById(R.id.clStateSummary);

        // Active
        ivActiveGif = findViewById(R.id.ivActiveGif);
        tvActiveName = findViewById(R.id.tvActiveName);
        tvActiveSetInfo = findViewById(R.id.tvActiveSetInfo);
        tvActiveNextUp = findViewById(R.id.tvActiveNextUp);
        tvActiveTimer = findViewById(R.id.tvActiveTimer);
        btnPause = findViewById(R.id.btnActivePause);

        // Rest
        tvRestTimer = findViewById(R.id.tvRestTimer);
        tvRestNextName = findViewById(R.id.tvRestNextName);
        tvRestPhase = findViewById(R.id.tvRestPhase);
        ivRestNextThumb = findViewById(R.id.ivRestNextThumb);

        // Summary
        tvSummaryTime = findViewById(R.id.tvSummaryTime);
        tvSummaryKcal = findViewById(R.id.tvSummaryKcal);
    }

    private void setupClickListeners() {
        // Global Close Button
        findViewById(R.id.ibCloseSession).setOnClickListener(v -> finish());

        // Skip buttons (Both Active and Rest use the same logic: go to next step)
        findViewById(R.id.btnActiveSkip).setOnClickListener(v -> moveToNextStep());
        findViewById(R.id.btnRestSkip).setOnClickListener(v -> moveToNextStep());

        // Pause Button (Bonus: simple pause/resume logic)
        btnPause.setOnClickListener(v -> {
            if (isTimerRunning) {
                pauseTimer();
                btnPause.setImageResource(R.drawable.ic_play); // Assuming you have a play icon!
            } else {
                startTimer(timeLeftInMillis);
                btnPause.setImageResource(R.drawable.ic_pause);
            }
        });
    }

    private void fetchWorkoutData() {
        new Thread(() -> {
            FitnessDatabase db = FitnessDatabase.getInstance(this);

            com.example.fitnesstracker.database.Routine currentRoutine = db.routineDao().getRoutineById(routineId);
            if (currentRoutine != null) {
                routineName = currentRoutine.name;
            }

            exercises = db.routineDao().getExercisesForRoutine(routineId);

            if (!exercises.isEmpty()) {
                runOnUiThread(() -> startActiveExercise());
            } else {
                runOnUiThread(this::finish); // Failsafe if empty
            }
        }).start();
    }

    // ==========================================
    // THE STATE MACHINE
    // ==========================================

    private void switchState(State newState) {
        currentState = newState;
        clActive.setVisibility(newState == State.ACTIVE ? View.VISIBLE : View.GONE);
        clRest.setVisibility(newState == State.REST ? View.VISIBLE : View.GONE);
        clSummary.setVisibility(newState == State.SUMMARY ? View.VISIBLE : View.GONE);

        // Update the header text depending on state
        TextView tvHeader = findViewById(R.id.tvHeaderStatus);
        if (newState == State.SUMMARY) {
            tvHeader.setText("WORKOUT COMPLETE");
        } else {
            tvHeader.setText("WORKOUT IN PROGRESS");
        }
    }

    private void startActiveExercise() {
        switchState(State.ACTIVE);
        ExerciseWithSettings currentEx = exercises.get(currentIndex);

        // 1. Update Text
        tvActiveName.setText(currentEx.name.toUpperCase());
        tvActiveSetInfo.setText("Set " + currentSet + " of " + currentEx.sets);

        // 2. Determine "Next Up"
        if (currentSet < currentEx.sets) {
            tvActiveNextUp.setText(currentEx.name + " (Set " + (currentSet + 1) + ")");
        } else if (currentIndex < exercises.size() - 1) {
            tvActiveNextUp.setText(exercises.get(currentIndex + 1).name);
        } else {
            tvActiveNextUp.setText("Workout Complete!");
        }

        // 3. Load GIF from Assets
        if (currentEx.gifUrl != null && !currentEx.gifUrl.isEmpty()) {
            Glide.with(this).asGif().load(currentEx.gifUrl).centerCrop().into(ivActiveGif);
        }

        // 4. Start Timer (Fallback to 45s if it's a rep-only exercise)
        long duration = currentEx.durationSeconds > 0 ? currentEx.durationSeconds * 1000L : 45000L;
        startTimer(duration);
    }

    private void startRestPeriod() {
        switchState(State.REST);
        ExerciseWithSettings nextEx = exercises.get(currentIndex);

        // 1. Update Text
        tvRestPhase.setText("PREPARING FOR SET " + currentSet);
        tvRestNextName.setText(nextEx.name);

        // 2. Load Static Image for Next Up (Glide automatically freezes GIFs into static images!)
        if (nextEx.gifUrl != null && !nextEx.gifUrl.isEmpty()) {
            Glide.with(this).load(nextEx.gifUrl).centerCrop().into(ivRestNextThumb);
        }

        // 3. Start Rest Timer
        long restTime = nextEx.restSeconds > 0 ? nextEx.restSeconds * 1000L : 60000L;
        startTimer(restTime);
    }

    private void showSummary() {
        switchState(State.SUMMARY);
        cancelTimer();

        long totalMillis = System.currentTimeMillis() - totalSessionStartTime;
        int totalMinutes = (int) (totalMillis / 1000) / 60;
        int totalSeconds = (int) (totalMillis / 1000) % 60;

        // REAL WORKOUT MATH: 6.0 METs * Weight(kg) * Time(hours)
        float timeInHours = totalMinutes / 60f;

        // Uses the dynamic weight we fetched in onCreate!
        int burnedKcal = (int) (6.0 * userWeightKg * timeInHours);

        tvSummaryTime.setText(String.format(java.util.Locale.getDefault(), "%02d:%02d", totalMinutes, totalSeconds));
        tvSummaryKcal.setText(String.valueOf(Math.max(1, burnedKcal)));
        findViewById(R.id.btnSummaryDone).setOnClickListener(v -> {
            new Thread(() -> {
                com.example.fitnesstracker.database.FitnessDatabase db = com.example.fitnesstracker.database.FitnessDatabase.getInstance(this);
                String todayDate = java.time.LocalDate.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd"));

                // Save the Workout Log using the math from above
                db.workoutDao().insertWorkout(new com.example.fitnesstracker.database.WorkoutLog(
                        todayDate,
                        routineName,
                        totalMinutes,
                        Math.max(1, burnedKcal)
                ));

                // Close the workout screen and return to Home
                runOnUiThread(this::finish);
            }).start();
        });
    }


    private void moveToNextStep() {
        cancelTimer();
        ExerciseWithSettings currentEx = exercises.get(currentIndex);

        if (currentState == State.ACTIVE) {
            // Did we finish the last set of the exercise?
            if (currentSet < currentEx.sets) {
                currentSet++;
                startRestPeriod(); // Rest between sets
            } else {
                // Move to the next exercise entirely
                currentIndex++;
                if (currentIndex < exercises.size()) {
                    currentSet = 1;
                    startRestPeriod(); // Rest between exercises
                } else {
                    showSummary(); // No more exercises left!
                }
            }
        } else if (currentState == State.REST) {
            // Rest is over, start the actual exercise
            startActiveExercise();
        }
    }

    // ==========================================
    // TIMER UTILS
    // ==========================================

    private void startTimer(long millis) {
        cancelTimer();
        timeLeftInMillis = millis;
        isTimerRunning = true;

        currentTimer = new CountDownTimer(timeLeftInMillis, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                timeLeftInMillis = millisUntilFinished;
                updateTimerText();
            }

            @Override
            public void onFinish() {
                isTimerRunning = false;
                moveToNextStep(); // Automatically transition when timer hits 0!
            }
        }.start();
    }

    private void pauseTimer() {
        cancelTimer();
        isTimerRunning = false;
    }

    private void cancelTimer() {
        if (currentTimer != null) {
            currentTimer.cancel();
        }
    }

    private void updateTimerText() {
        int minutes = (int) (timeLeftInMillis / 1000) / 60;
        int seconds = (int) (timeLeftInMillis / 1000) % 60;
        String timeFormatted = String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds);

        if (currentState == State.ACTIVE) {
            tvActiveTimer.setText(timeFormatted);
        } else if (currentState == State.REST) {
            tvRestTimer.setText(timeFormatted);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        cancelTimer(); // Prevent crashes if the user closes the app while timer is ticking!
    }
}
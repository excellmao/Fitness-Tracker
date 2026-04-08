package com.example.fitnesstracker.homescreen;

import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.ImageView;
import androidx.appcompat.app.AppCompatActivity;

import com.example.fitnesstracker.R;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.DayOfWeek;
import java.time.temporal.TemporalAdjusters;

import com.example.fitnesstracker.database.FitnessDatabase;
import com.example.fitnesstracker.database.WorkoutDao;
import com.example.fitnesstracker.database.WorkoutLog;

public class HomeScreenActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        //avatar
        ImageView avatar = findViewById(R.id.ivAvatar);
        avatar.setOnClickListener( v -> {
            // TODO: navigate to profile module
            // Intent intent = new Intent(this, ProfileActivity.class);
            // startActivity(intent);
        });

        //bottom nav
        ImageView navHome = findViewById(R.id.navHome);
        ImageView navRun = findViewById(R.id.navRun);
        ImageView navWorkout = findViewById(R.id.navWorkout);
        ImageView navMeals = findViewById(R.id.navMeals);
        ImageView navProfile = findViewById(R.id.navProfile);

        navHome.setOnClickListener(v -> {
            //NestedScrollView scrollView = findViewById(R.id.mainScrollView);
            //scrollView.smoothScrollTo(0, 0);
        });

        navRun.setOnClickListener( v -> {
            // TODO: navigate to run module
            // Intent intent = new Intent(this, RunActivity.class);
            // startActivity(intent);
        });

        navWorkout.setOnClickListener( v -> {
            // TODO: navigate to workout module
            // Intent intent = new Intent(this, WorkoutActivity.class);
            // startActivity(intent);
        });

        navMeals.setOnClickListener( v -> {
            // TODO: navigate to meals module
            // Intent intent = new Intent(this, MealsActivity.class);
            // startActivity(intent);
        });

        navProfile.setOnClickListener( v -> {
            // TODO: navigate to profile module
            // Intent intent = new Intent(this, ProfileActivity.class);
            // startActivity(intent);
        });

        // Test only, commented when running on actual devices
        seedDatabase();

        // Real database call
        // loadWeeklyChartData();
    }

    private void seedDatabase() {
        ExecutorService executor = Executors.newSingleThreadExecutor();

        executor.execute(() -> {
            FitnessDatabase db = FitnessDatabase.getInstance(this);
            WorkoutDao dao = db.workoutDao();

            LocalDate today = LocalDate.now();
            LocalDate monday = today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

            String dateMon = monday.format(formatter);
            String dateWed = monday.plusDays(2).format(formatter);
            String dateFri = monday.plusDays(4).format(formatter);
            String dateSat = monday.plusDays(5).format(formatter);

            if (dao.getWorkoutCountForDate(dateMon) == 0) {

                // 3. Insert dummy workouts using the dynamic date strings
                dao.insertWorkout(new WorkoutLog(dateMon, "RUN", 45, 360));
                dao.insertWorkout(new WorkoutLog(dateWed, "LIFT", 60, 480));
                dao.insertWorkout(new WorkoutLog(dateWed, "RUN", 30, 240));
                dao.insertWorkout(new WorkoutLog(dateFri, "YOGA", 45, 360));
                dao.insertWorkout(new WorkoutLog(dateSat, "SWIM", 30, 240));
            }

            loadWeeklyChartData();
        });
    }

    private void loadWeeklyChartData() {
        ExecutorService executor = Executors.newSingleThreadExecutor();

        executor.execute(() -> {
            FitnessDatabase db = FitnessDatabase.getInstance(this);
            WorkoutDao dao = db.workoutDao();

            LocalDate today = LocalDate.now();
            LocalDate monday = today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));

            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

            String dateMon = monday.format(formatter);
            String dateTue = monday.plusDays(1).format(formatter);
            String dateWed = monday.plusDays(2).format(formatter);
            String dateThu = monday.plusDays(3).format(formatter);
            String dateFri = monday.plusDays(4).format(formatter);
            String dateSat = monday.plusDays(5).format(formatter);
            String dateSun = monday.plusDays(6).format(formatter);

            int monCount = dao.getWorkoutCountForDate(dateMon);
            int tueCount = dao.getWorkoutCountForDate(dateTue);
            int wedCount = dao.getWorkoutCountForDate(dateWed);
            int thuCount = dao.getWorkoutCountForDate(dateThu);
            int friCount = dao.getWorkoutCountForDate(dateFri);
            int satCount = dao.getWorkoutCountForDate(dateSat);
            int sunCount = dao.getWorkoutCountForDate(dateSun);

            int maxWorkoutThisWeek = 1;
            int[] allCounts = {monCount, tueCount, wedCount, thuCount, friCount, satCount, sunCount};
            for (int count : allCounts) {
                if (count > maxWorkoutThisWeek){
                    maxWorkoutThisWeek = count;
                }
            }

            final int chartScaleMax = maxWorkoutThisWeek;

            runOnUiThread(() -> {
                setBarData(findViewById(R.id.barMon), "MON", monCount, chartScaleMax);
                setBarData(findViewById(R.id.barTue), "TUE", tueCount, chartScaleMax);
                setBarData(findViewById(R.id.barWed), "WED", wedCount, chartScaleMax);
                setBarData(findViewById(R.id.barThu), "THU", thuCount, chartScaleMax);
                setBarData(findViewById(R.id.barFri), "FRI", friCount, chartScaleMax);
                setBarData(findViewById(R.id.barSat), "SAT", satCount, chartScaleMax);
                setBarData(findViewById(R.id.barSun), "SUN", sunCount, chartScaleMax);
            });
        });
    }

    private void setBarData(View barLayout, String dayLabel, int workoutCount, int chartScaleMax) {
        TextView tvDay = barLayout.findViewById(R.id.tvDayLabel);
        tvDay.setText(dayLabel);

        int percentage = (int) (((float) workoutCount / chartScaleMax) * 100);
        if (percentage > 100) percentage = 100;

        View viewSpacer = barLayout.findViewById(R.id.viewSpacer);
        View viewFill = barLayout.findViewById(R.id.viewFill);

        LinearLayout.LayoutParams spacerParams = (LinearLayout.LayoutParams) viewSpacer.getLayoutParams();
        spacerParams.weight = 100 - percentage;
        viewSpacer.setLayoutParams(spacerParams);

        LinearLayout.LayoutParams fillParams = (LinearLayout.LayoutParams) viewFill.getLayoutParams();
        fillParams.weight = percentage;
        viewFill.setLayoutParams(fillParams);
    }
}

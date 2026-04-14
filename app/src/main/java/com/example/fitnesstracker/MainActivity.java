package com.example.fitnesstracker;

import android.graphics.Color;
import android.os.Bundle;
import android.widget.ImageView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.view.WindowInsetsControllerCompat;

import com.example.fitnesstracker.homescreen.HomeFragment;
import com.example.fitnesstracker.nutrition.NutritionFragment;
import com.example.fitnesstracker.profile.ProfileFragment;
import com.example.fitnesstracker.workout.WorkoutListFragment;
import com.example.fitnesstracker.run.RunPrepFragment; // ADDED FROM RUN BRANCH!

import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;
import androidx.work.ExistingPeriodicWorkPolicy;
import java.util.concurrent.TimeUnit;

public class MainActivity extends AppCompatActivity {

    private ImageView navHome, navRun, navWorkout, navMeal, navProfile;

    private final int COLOR_NEON = Color.parseColor("#D4FF00");
    private final int COLOR_INACTIVE = Color.parseColor("#808080");
    private final androidx.activity.result.ActivityResultLauncher<String[]> permissionLauncher =
            registerForActivityResult(new androidx.activity.result.contract.ActivityResultContracts.RequestMultiplePermissions(), result -> {
                // Android gives us a map of which permissions were granted or denied
                Boolean fineLocation = result.getOrDefault(android.Manifest.permission.ACCESS_FINE_LOCATION, false);
                Boolean notifications = result.getOrDefault(android.Manifest.permission.POST_NOTIFICATIONS, false);

                if (!fineLocation) {
                    android.widget.Toast.makeText(this, "Location needed to draw run map!", android.widget.Toast.LENGTH_SHORT).show();
                }
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU && !notifications) {
                    android.widget.Toast.makeText(this, "Notifications needed for background tracking!", android.widget.Toast.LENGTH_SHORT).show();
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // 1. KEEP YOUR FULLSCREEN LOGIC
        WindowInsetsControllerCompat windowInsetsController =
                WindowCompat.getInsetsController(getWindow(), getWindow().getDecorView());
        windowInsetsController.setSystemBarsBehavior(
                WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        );
        windowInsetsController.hide(WindowInsetsCompat.Type.systemBars());

        // 2. INITIALIZE YOUR CUSTOM NAV ICONS
        navHome = findViewById(R.id.navHome);
        navRun = findViewById(R.id.navRun);
        navWorkout = findViewById(R.id.navWorkout);
        navMeal = findViewById(R.id.navMeal);
        navProfile = findViewById(R.id.navProfile);

        // ALWAYS START ON HOME SCREEN
        if (savedInstanceState == null) {
            loadFragment(new HomeFragment());
            updateNavUI(navHome);
        }

        // ==========================================
        // NAVIGATION CLICK LISTENERS
        // ==========================================
        navHome.setOnClickListener( v -> {
            loadFragment(new HomeFragment());
            updateNavUI(navHome);
        });

        // ADDED: Hook up the Run branch to the Run icon!
        navRun.setOnClickListener(v -> {
            loadFragment(new RunPrepFragment());
            updateNavUI(navRun);
        });

        navWorkout.setOnClickListener(v -> {
            loadFragment(new WorkoutListFragment());
            updateNavUI(navWorkout);
        });

        navMeal.setOnClickListener(v -> {
            loadFragment(new NutritionFragment());
            updateNavUI(navMeal);
        });

        navProfile.setOnClickListener(v -> {
            loadFragment(new ProfileFragment());
            updateNavUI(navProfile);
        });

        ImageView ivAvatar = findViewById(R.id.ivAvatar);
        if (ivAvatar != null) {
            ivAvatar.setOnClickListener(v -> {
                loadFragment(new ProfileFragment());
                updateNavUI(navProfile);
            });
        }

        PeriodicWorkRequest waterRequest = new PeriodicWorkRequest.Builder(
                com.example.fitnesstracker.nutrition.WaterReminderWorker.class,
                2, TimeUnit.HOURS)
                .build();

        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
                "WaterReminderTask",
                ExistingPeriodicWorkPolicy.KEEP,
                waterRequest
        );

        checkAndRequestPermissions();
    }

    private void loadFragment(Fragment fragment) {
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragmentContainer, fragment)
                .commit();
    }

    private void updateNavUI(ImageView selectedNav) {
        navHome.setColorFilter(COLOR_INACTIVE);
        navRun.setColorFilter(COLOR_INACTIVE);
        navWorkout.setColorFilter(COLOR_INACTIVE);
        navMeal.setColorFilter(COLOR_INACTIVE);
        navProfile.setColorFilter(COLOR_INACTIVE);

        selectedNav.setColorFilter(COLOR_NEON);
    }
    private void checkAndRequestPermissions() {
        java.util.List<String> permissionsNeeded = new java.util.ArrayList<>();

        if (androidx.core.content.ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION)
                != android.content.pm.PackageManager.PERMISSION_GRANTED) {
            permissionsNeeded.add(android.Manifest.permission.ACCESS_FINE_LOCATION);
            permissionsNeeded.add(android.Manifest.permission.ACCESS_COARSE_LOCATION);
        }

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            if (androidx.core.content.ContextCompat.checkSelfPermission(this, android.Manifest.permission.POST_NOTIFICATIONS)
                    != android.content.pm.PackageManager.PERMISSION_GRANTED) {
                permissionsNeeded.add(android.Manifest.permission.POST_NOTIFICATIONS);
            }
        }

        if (!permissionsNeeded.isEmpty()) {
            permissionLauncher.launch(permissionsNeeded.toArray(new String[0]));
        }
    }
}
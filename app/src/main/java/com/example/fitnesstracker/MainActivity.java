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
import com.example.fitnesstracker.profile.ProfileFragment;
import com.example.fitnesstracker.workout.WorkoutListFragment; // IMPORT THE NEW MODULE!

public class MainActivity extends AppCompatActivity {

    private ImageView navHome, navRun, navWorkout, navMeal, navProfile;

    private final int COLOR_NEON = Color.parseColor("#D4FF00");
    private final int COLOR_INACTIVE = Color.parseColor("#808080");

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

        // 3. SET DEFAULT FRAGMENT
        if (savedInstanceState == null) {
            loadFragment(new HomeFragment());
            updateNavUI(navHome);
        }

        // 4. WIRE UP THE CLICK LISTENERS
        navHome.setOnClickListener(v -> {
            loadFragment(new HomeFragment());
            updateNavUI(navHome);
        });

        navWorkout.setOnClickListener(v -> {
            // FINALLY CONNECTING THE WORKOUT MODULE!
            loadFragment(new WorkoutListFragment());
            updateNavUI(navWorkout);
        });

        navProfile.setOnClickListener(v -> {
            loadFragment(new ProfileFragment());
            updateNavUI(navProfile);
        });

        // Placeholder for Run and Meal
        navRun.setOnClickListener(v -> updateNavUI(navRun));
        navMeal.setOnClickListener(v -> updateNavUI(navMeal));
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
}
package com.example.fitnesstracker.homescreen;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.fitnesstracker.R;
import com.example.fitnesstracker.database.FitnessDatabase;
import com.example.fitnesstracker.database.Routine;
import com.example.fitnesstracker.database.RoutineDao;
import com.example.fitnesstracker.database.WorkoutDao;
import com.example.fitnesstracker.database.WorkoutLog;
import com.example.fitnesstracker.homescreen.adapters.RecommendedWorkoutAdapter;
import com.example.fitnesstracker.workout.WorkoutListFragment;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAdjusters;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class HomeFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // KEEP: Use your fragment_home layout
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        loadWeeklyChartData(view);
        loadRecommendedRoutines(view);

        return view;
    }

    private void loadWeeklyChartData(View view) {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.execute(() -> {
            FitnessDatabase db = FitnessDatabase.getInstance(requireContext());
            WorkoutDao dao = db.workoutDao();

            LocalDate today = LocalDate.now();
            LocalDate monday = today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

            // 1. SEED DATA (Updated with calorie parameter)
            seedDummyDataIfNeeded(dao, monday, formatter);

            // 2. READ MINUTES
            int monMins = dao.getTotalDurationForDate(monday.format(formatter));
            int tueMins = dao.getTotalDurationForDate(monday.plusDays(1).format(formatter));
            int wedMins = dao.getTotalDurationForDate(monday.plusDays(2).format(formatter));
            int thuMins = dao.getTotalDurationForDate(monday.plusDays(3).format(formatter));
            int friMins = dao.getTotalDurationForDate(monday.plusDays(4).format(formatter));
            int satMins = dao.getTotalDurationForDate(monday.plusDays(5).format(formatter));
            int sunMins = dao.getTotalDurationForDate(monday.plusDays(6).format(formatter));

            int[] allMins = {monMins, tueMins, wedMins, thuMins, friMins, satMins, sunMins};
            int maxMins = 1;
            for (int m : allMins) if (m > maxMins) maxMins = m;

            final int chartScaleMax = maxMins;

            // 3. UPDATE BARS
            requireActivity().runOnUiThread(() -> {
                setBarData(view.findViewById(R.id.barMon), "MON", monMins, chartScaleMax);
                setBarData(view.findViewById(R.id.barTue), "TUE", tueMins, chartScaleMax);
                setBarData(view.findViewById(R.id.barWed), "WED", wedMins, chartScaleMax);
                setBarData(view.findViewById(R.id.barThu), "THU", thuMins, chartScaleMax);
                setBarData(view.findViewById(R.id.barFri), "FRI", friMins, chartScaleMax);
                setBarData(view.findViewById(R.id.barSat), "SAT", satMins, chartScaleMax);
                setBarData(view.findViewById(R.id.barSun), "SUN", sunMins, chartScaleMax);
            });
        });
    }

    private void loadRecommendedRoutines(View view) {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.execute(() -> {
            FitnessDatabase db = FitnessDatabase.getInstance(requireContext());
            // Now using the real Routine table from the workout branch
            List<Routine> dbRoutines = db.routineDao().getAllRoutines();

            requireActivity().runOnUiThread(() -> {
                RecyclerView rvRecommended = view.findViewById(R.id.rvRecommended);
                rvRecommended.setLayoutManager(new LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false));

                RecommendedWorkoutAdapter adapter = new RecommendedWorkoutAdapter(dbRoutines, routineId -> {
                    // This is the missing Click Listener!
                    com.example.fitnesstracker.workout.WorkoutDetailFragment detailFragment = new com.example.fitnesstracker.workout.WorkoutDetailFragment();

                    android.os.Bundle args = new android.os.Bundle();
                    args.putInt("routine_id", routineId);
                    detailFragment.setArguments(args);

                    requireActivity().getSupportFragmentManager().beginTransaction()
                            .replace(R.id.fragmentContainer, detailFragment)
                            .addToBackStack(null)
                            .commit();
                });
                rvRecommended.setAdapter(adapter);

                TextView tvViewAll = view.findViewById(R.id.tvViewAll); // Make sure this ID matches your XML
                tvViewAll.setOnClickListener(v -> {
                    // Navigate to the list of all routines
                    requireActivity().getSupportFragmentManager().beginTransaction()
                            .replace(R.id.fragmentContainer, new WorkoutListFragment()) // Your fragment name from the project tree!
                            .addToBackStack(null)
                            .commit();
                });
            });
        });
    }

    private void setBarData(View barLayout, String dayLabel, int value, int max) {
        if (barLayout == null) return;
        TextView tvDay = barLayout.findViewById(R.id.tvDayLabel);
        tvDay.setText(dayLabel);

        int percentage = (int) (((float) value / max) * 100);
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

    private void seedDummyDataIfNeeded(WorkoutDao dao, LocalDate monday, DateTimeFormatter formatter) {
        if (dao.getWorkoutCountForDate(monday.format(formatter)) == 0) {
            // Updated with the new 4th parameter: Calories
            dao.insertWorkout(new WorkoutLog(monday.format(formatter), "Morning Sprint", 30, 250));
            dao.insertWorkout(new WorkoutLog(monday.format(formatter), "PeePee", 30, 150));
            dao.insertWorkout(new WorkoutLog(monday.plusDays(2).format(formatter), "Leg Day", 45, 400));
            dao.insertWorkout(new WorkoutLog(monday.plusDays(4).format(formatter), "Zen Flow", 20, 100));
        }
    }
}
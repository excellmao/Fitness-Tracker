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
import com.example.fitnesstracker.database.RoutineDao;
import com.example.fitnesstracker.database.WorkoutDao;
import com.example.fitnesstracker.database.WorkoutLog;
import com.example.fitnesstracker.homescreen.adapters.RecommendedWorkoutAdapter;
import com.example.fitnesstracker.homescreen.models.WorkoutRoutine;

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

            // SEED DATA
            seedDummyDataIfNeeded(dao, monday, formatter);

            // READ ACTUAL DATA
            int monMins = dao.getTotalDurationForDate(monday.format(formatter));      // Will be 60 mins (30+30)
            int tueMins = dao.getTotalDurationForDate(monday.plusDays(1).format(formatter)); // 0 mins
            int wedMins = dao.getTotalDurationForDate(monday.plusDays(2).format(formatter)); // 45 mins
            int thuMins = dao.getTotalDurationForDate(monday.plusDays(3).format(formatter)); // 0 mins
            int friMins = dao.getTotalDurationForDate(monday.plusDays(4).format(formatter)); // 20 mins
            int satMins = dao.getTotalDurationForDate(monday.plusDays(5).format(formatter)); // 0 mins
            int sunMins = dao.getTotalDurationForDate(monday.plusDays(6).format(formatter)); // 0 mins

            int maxMinsThisWeek = 1;
            int[] allMins = {monMins, tueMins, wedMins, thuMins, friMins, satMins, sunMins};
            for (int mins : allMins) {
                if (mins > maxMinsThisWeek) {
                    maxMinsThisWeek = mins;
                }
            }

            final int chartScaleMax = maxMinsThisWeek;

            // 3. UPDATE UI
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
            RoutineDao dao = db.routineDao();

            if (dao.getRoutineCount() == 0) {
                dao.insertRoutine(new WorkoutRoutine("NEON\nSTRENGTH", "PRO LEVEL", 45, 420, R.drawable.avatar));
                dao.insertRoutine(new WorkoutRoutine("ZEN\nFLOW", "RECOVERY", 20, 150, R.drawable.avatar));
                dao.insertRoutine(new WorkoutRoutine("HIIT\nBURN", "INTERMEDIATE", 30, 350, R.drawable.avatar));
            }

            List<WorkoutRoutine> dbRoutines = dao.getAllRoutines();

            requireActivity().runOnUiThread(() -> {
                RecyclerView rvRecommended = view.findViewById(R.id.rvRecommended);
                rvRecommended.setLayoutManager(new LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false));

                RecommendedWorkoutAdapter adapter = new RecommendedWorkoutAdapter(dbRoutines);
                rvRecommended.setAdapter(adapter);
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

     //TODO: Delete or comment out the call to this method before final production release!
     // Dummy data
    private void seedDummyDataIfNeeded(WorkoutDao dao, LocalDate monday, DateTimeFormatter formatter) {
        if (dao.getWorkoutCountForDate(monday.format(formatter)) == 0) {
            dao.insertWorkout(new WorkoutLog(monday.format(formatter), "Morning Sprint", 30));
            dao.insertWorkout(new WorkoutLog(monday.format(formatter), "PeePee", 30));
            dao.insertWorkout(new WorkoutLog(monday.plusDays(2).format(formatter), "Leg Day", 45));
            dao.insertWorkout(new WorkoutLog(monday.plusDays(4).format(formatter), "Zen Flow", 20));
        }
    }
}
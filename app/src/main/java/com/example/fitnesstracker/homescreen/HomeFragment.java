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
import com.example.fitnesstracker.R;
import com.example.fitnesstracker.database.FitnessDatabase;
import com.example.fitnesstracker.database.WorkoutDao;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAdjusters;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class HomeFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // Rule 2: Inflate and find view
        View view = inflater.inflate(R.layout.activity_home, container, false);
        
        // Ẩn thanh Bottom Nav thủ công nếu nó nằm trong layout này (tránh bị lặp)
        View manualNav = view.findViewById(R.id.navHome);
        if (manualNav != null && manualNav.getParent() instanceof View) {
            ((View)manualNav.getParent()).setVisibility(View.GONE);
        }

        loadWeeklyChartData(view);
        return view;
    }

    private void loadWeeklyChartData(View rootView) {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.execute(() -> {
            // Rule 2: Use requireContext()
            FitnessDatabase db = FitnessDatabase.getInstance(requireContext());
            WorkoutDao dao = db.workoutDao();

            LocalDate today = LocalDate.now();
            LocalDate monday = today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

            int[] counts = new int[7];
            int maxWorkout = 1;

            for (int i = 0; i < 7; i++) {
                counts[i] = dao.getWorkoutCountForDate(monday.plusDays(i).format(formatter));
                if (counts[i] > maxWorkout) maxWorkout = counts[i];
            }

            final int chartScaleMax = maxWorkout;
            requireActivity().runOnUiThread(() -> {
                String[] labels = {"MON", "TUE", "WED", "THU", "FRI", "SAT", "SUN"};
                int[] ids = {R.id.barMon, R.id.barTue, R.id.barWed, R.id.barThu, R.id.barFri, R.id.barSat, R.id.barSun};
                
                for (int i = 0; i < 7; i++) {
                    View bar = rootView.findViewById(ids[i]);
                    if (bar != null) setBarData(bar, labels[i], counts[i], chartScaleMax);
                }
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

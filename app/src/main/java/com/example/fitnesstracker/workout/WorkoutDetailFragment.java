package com.example.fitnesstracker.workout;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatButton;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.fitnesstracker.R;
import com.example.fitnesstracker.database.ExerciseWithSettings;
import com.example.fitnesstracker.database.FitnessDatabase;
import com.example.fitnesstracker.database.Routine;
import java.util.ArrayList;
import java.util.List;

public class WorkoutDetailFragment extends Fragment {

    private int routineId;
    private ExerciseDetailAdapter adapter;
    private FitnessDatabase db;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_workout_detail, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        routineId = getArguments() != null ? getArguments().getInt("routine_id", -1) : -1;
        db = FitnessDatabase.getInstance(requireContext());

        ImageButton ibBack         = view.findViewById(R.id.ibDetailBack);
        TextView tvRoutineName     = view.findViewById(R.id.tvDetailRoutineName);
        TextView tvExerciseCount   = view.findViewById(R.id.tvDetailExerciseCount);
        TextView tvStatMinutes     = view.findViewById(R.id.tvStatMinutes);
        TextView tvStatKcal        = view.findViewById(R.id.tvStatKcal);
        RecyclerView rvExercises   = view.findViewById(R.id.rvDetailExercises);
        AppCompatButton btnStart   = view.findViewById(R.id.btnStartSession);

        adapter = new ExerciseDetailAdapter(new ArrayList<>());
        rvExercises.setLayoutManager(new LinearLayoutManager(requireContext()));
        rvExercises.setAdapter(adapter);

        ibBack.setOnClickListener(v ->
                requireActivity().getSupportFragmentManager().popBackStack());

        btnStart.setOnClickListener(v -> {
            Intent intent = new Intent(requireActivity(), ActiveWorkoutActivity.class);
            intent.putExtra("routine_id", routineId);
            startActivity(intent);
        });

        if (routineId != -1) {
            loadData(tvRoutineName, tvExerciseCount, tvStatMinutes, tvStatKcal);
        }
    }

    private void loadData(TextView tvName, TextView tvCount, TextView tvMinutes, TextView tvKcal) {
        new Thread(() -> {
            List<ExerciseWithSettings> exercises = db.routineDao().getExercisesForRoutine(routineId);

            // Estimate session duration: multiply sets × ~1.5 min/set per exercise + rest
            int totalMinutes = 0;
            for (ExerciseWithSettings ex : exercises) {
                int workTime = (ex.durationSeconds > 0)
                        ? ex.sets * ex.durationSeconds
                        : ex.sets * 45; // estimate 45s per reps set
                int restTime = ex.sets * ex.restSeconds;
                totalMinutes += (workTime + restTime) / 60;
            }
            final int minutes = Math.max(totalMinutes, 1);

            // Get routine name
            List<Routine> routines = db.routineDao().getAllRoutines();
            String name = "Routine";
            for (Routine r : routines) {
                if (r.getId() == routineId) { name = r.getName(); break; }
            }
            final String routineName = name;

            final int kcal = minutes * 8;
            requireActivity().runOnUiThread(() -> {
                tvName.setText(routineName);
                tvCount.setText(exercises.size() + " Exercises");
                tvMinutes.setText(String.valueOf(minutes));
                tvKcal.setText(String.valueOf(kcal));
                adapter.setExercises(exercises);
            });
        }).start();
    }
}

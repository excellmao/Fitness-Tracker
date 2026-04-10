package com.example.fitnesstracker.workout;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatButton;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.fitnesstracker.R;
import com.example.fitnesstracker.database.ExerciseWithSettings;
import com.example.fitnesstracker.database.FitnessDatabase;
import com.example.fitnesstracker.database.Routine;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.ArrayList;
import java.util.List;

public class WorkoutDetailFragment extends Fragment {

    private int routineId;
    private ExerciseDetailAdapter adapter;
    private FitnessDatabase db;

    // UI Elements
    private ImageView ivDetailCover;
    private TextView tvPhase; // For "PHASE 02 / STRENGTH"

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

        // NEW: Grab the ImageView for the Cover and the Phase text
        ivDetailCover = view.findViewById(R.id.ivDetailCover);
        tvPhase       = view.findViewById(R.id.tvDetailPhase);

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
            List<Routine> routines = db.routineDao().getAllRoutines();

            Routine targetRoutine = null;
            for (Routine r : routines) {
                if (r.getId() == routineId) {
                    targetRoutine = r;
                    break;
                }
            }

            if (targetRoutine != null) {
                final Routine finalRoutine = targetRoutine;

                requireActivity().runOnUiThread(() -> {
                    // 1. Set the exact Text from the Database
                    tvName.setText(finalRoutine.getName());
                    if (tvPhase != null) tvPhase.setText(finalRoutine.getPhase());
                    tvCount.setText(exercises.size() + " Exercises");
                    tvMinutes.setText(String.valueOf(finalRoutine.getTotalMinutes()));
                    tvKcal.setText(String.valueOf(finalRoutine.getTotalCalories()));

                    // 2. Load the GIF/Image from your Assets using Glide
                    String coverUrl = finalRoutine.getCoverImageUrl();
                    if (ivDetailCover != null && coverUrl != null && !coverUrl.isEmpty()) {
                        Glide.with(requireContext())
                                .load(coverUrl) // This handles "file:///android_asset/..." perfectly
                                .centerCrop()
                                .into(ivDetailCover);
                    }

                    adapter.setExercises(exercises);
                });
            }
        }).start();
    }

    @Override
    public void onResume() {
        super.onResume();
        // Changed from BottomNavigationView to generic View
        View navBar = requireActivity().findViewById(R.id.bottomNavContainer);
        if (navBar != null) navBar.setVisibility(View.GONE);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        // Changed from BottomNavigationView to generic View
        View navBar = requireActivity().findViewById(R.id.bottomNavContainer);
        if (navBar != null) navBar.setVisibility(View.VISIBLE);
    }
}
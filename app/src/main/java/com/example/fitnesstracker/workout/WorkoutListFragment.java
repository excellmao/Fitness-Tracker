package com.example.fitnesstracker.workout;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.fitnesstracker.R;
import com.example.fitnesstracker.database.FitnessDatabase;
import com.example.fitnesstracker.database.Routine;
import java.util.ArrayList;
import java.util.List;

public class WorkoutListFragment extends Fragment {

    private RecyclerView rvRoutines;
    private RoutineAdapter adapter;
    private FitnessDatabase db;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_workout_list, container, false);

        rvRoutines = view.findViewById(R.id.rvRoutines);
        Button btnCreateRoutine = view.findViewById(R.id.btnCreateRoutine);

        db = FitnessDatabase.getInstance(requireContext());

        setupRecyclerView();

        // CHANGED: Open EditRoutineFragment in "Create Mode" (ID = -1)
        btnCreateRoutine.setOnClickListener(v -> {
            EditRoutineFragment createFragment = new EditRoutineFragment();
            Bundle args = new Bundle();
            args.putInt("routine_id", -1); // -1 tells it to start with a blank slate
            createFragment.setArguments(args);

            requireActivity().getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragmentContainer, createFragment)
                    .addToBackStack(null)
                    .commit();
        });

        loadRoutines();

        return view;
    }

    private void setupRecyclerView() {
        rvRoutines.setLayoutManager(new LinearLayoutManager(requireContext()));
        adapter = new RoutineAdapter(
                new ArrayList<>(),
                // onRoutineClick → open detail
                routine -> {
                    WorkoutDetailFragment detailFragment = new WorkoutDetailFragment();
                    Bundle bundle = new Bundle();
                    bundle.putInt("routine_id", routine.getId());
                    detailFragment.setArguments(bundle);
                    requireActivity().getSupportFragmentManager().beginTransaction()
                            .replace(R.id.fragmentContainer, detailFragment)
                            .addToBackStack(null)
                            .commit();
                },

                // CHANGED: Open EditRoutineFragment in "Edit Mode" (Passes actual ID)
                routine -> {
                    EditRoutineFragment editFragment = new EditRoutineFragment();
                    Bundle args = new Bundle();
                    args.putInt("routine_id", routine.getId()); // Passes the real ID to load existing data
                    editFragment.setArguments(args);

                    requireActivity().getSupportFragmentManager().beginTransaction()
                            .replace(R.id.fragmentContainer, editFragment)
                            .addToBackStack(null)
                            .commit();
                },

                // onDelete → confirm dialog then delete
                routine -> new AlertDialog.Builder(requireContext())
                        .setTitle("Delete routine")
                        .setMessage("Do you want to delete \"" + routine.getName() + "\"?")
                        .setPositiveButton("Xóa", (d, w) -> deleteRoutine(routine))
                        .setNegativeButton("Hủy", null)
                        .show()
        );
        rvRoutines.setAdapter(adapter);
    }

    private void deleteRoutine(Routine routine) {
        new Thread(() -> {
            db.routineDao().deleteRoutineExercisesByRoutineId(routine.getId());
            db.routineDao().deleteRoutine(routine.getId());
            requireActivity().runOnUiThread(this::loadRoutines);
        }).start();
    }

    private void loadRoutines() {
        new Thread(() -> {
            List<Routine> routines = db.routineDao().getAllRoutines();
            requireActivity().runOnUiThread(() -> adapter.setRoutines(routines));
        }).start();
    }

    @Override
    public void onResume() {
        super.onResume();
        loadRoutines();
    }
}
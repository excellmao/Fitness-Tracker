package com.example.fitnesstracker.workout;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.fitnesstracker.R;
import com.example.fitnesstracker.database.Exercise;
import com.example.fitnesstracker.database.ExerciseWithSettings;
import com.example.fitnesstracker.database.FitnessDatabase;
import com.example.fitnesstracker.database.Routine;
import com.example.fitnesstracker.database.RoutineExercise;
import java.util.ArrayList;
import java.util.List;

public class CreateRoutineFragment extends Fragment {

    private static final String ARG_ROUTINE_ID = "routine_id";

    /** Use this factory method when opening in Edit mode. */
    public static CreateRoutineFragment newEditInstance(int routineId) {
        CreateRoutineFragment f = new CreateRoutineFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_ROUTINE_ID, routineId);
        f.setArguments(args);
        return f;
    }

    private int editRoutineId = -1; // -1 = create mode
    private final List<SelectedExercise> selectedExercises = new ArrayList<>();
    private SelectedExerciseAdapter adapter;
    private TextView tvExerciseCount;
    private FitnessDatabase db;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_create_routine, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        db = FitnessDatabase.getInstance(requireContext());

        editRoutineId = getArguments() != null ? getArguments().getInt(ARG_ROUTINE_ID, -1) : -1;

        EditText etRoutineName   = view.findViewById(R.id.etRoutineName);
        tvExerciseCount          = view.findViewById(R.id.tvExerciseCount);
        RecyclerView rvExercises = view.findViewById(R.id.rvSelectedExercises);
        ImageButton ibBack       = view.findViewById(R.id.ibBack);
        TextView tvSave          = view.findViewById(R.id.tvSave);
        View btnAddExercise      = view.findViewById(R.id.btnAddExercise);

        // Setup RecyclerView
        adapter = new SelectedExerciseAdapter(selectedExercises, position -> {
            selectedExercises.remove(position);
            adapter.notifyItemRemoved(position);
            updateExerciseCount();
        });
        rvExercises.setLayoutManager(new LinearLayoutManager(requireContext()));
        rvExercises.setAdapter(adapter);

        // Receive selected exercises back from AddExerciseFragment
        getParentFragmentManager().setFragmentResultListener(
                "add_exercise_result",
                getViewLifecycleOwner(),
                (requestKey, bundle) -> {
                    int[] ids = bundle.getIntArray("selected_ids");
                    if (ids == null || ids.length == 0) return;
                    loadExercisesByIds(ids);
                }
        );

        // If edit mode, update title and pre-fill existing data
        TextView tvTitle = view.findViewById(R.id.tvTitle);
        if (editRoutineId != -1) {
            tvTitle.setText("Edit Routine");
            preLoadRoutineData(etRoutineName);
        }

        ibBack.setOnClickListener(v ->
                requireActivity().getSupportFragmentManager().popBackStack());

        btnAddExercise.setOnClickListener(v ->
                requireActivity().getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragmentContainer, new AddExerciseFragment())
                        .addToBackStack(null)
                        .commit()
        );

        tvSave.setOnClickListener(v -> {
            String name = etRoutineName.getText().toString().trim();
            if (name.isEmpty()) {
                Toast.makeText(requireContext(), "Enter a routine name", Toast.LENGTH_SHORT).show();
                return;
            }
            if (selectedExercises.isEmpty()) {
                Toast.makeText(requireContext(), "Add at least one exercise", Toast.LENGTH_SHORT).show();
                return;
            }
            saveRoutine(name);
        });
    }

    /** Pre-fills the form from DB when opening in Edit mode. */
    private void preLoadRoutineData(EditText etRoutineName) {
        new Thread(() -> {
            // Get routine name
            List<Routine> all = db.routineDao().getAllRoutines();
            String existingName = "";
            for (Routine r : all) {
                if (r.getId() == editRoutineId) { existingName = r.getName(); break; }
            }
            final String name = existingName;

            // Get existing exercises for this routine
            List<ExerciseWithSettings> existing = db.routineDao().getExercisesForRoutine(editRoutineId);
            List<SelectedExercise> preSelected = new ArrayList<>();
            for (ExerciseWithSettings ews : existing) {
                SelectedExercise se = new SelectedExercise(ews.exerciseId, ews.name, ews.category);
                se.sets            = ews.sets;
                se.reps            = ews.reps;
                se.restSeconds     = ews.restSeconds;
                se.durationSeconds = ews.durationSeconds;
                preSelected.add(se);
            }

            requireActivity().runOnUiThread(() -> {
                etRoutineName.setText(name);
                selectedExercises.clear();
                selectedExercises.addAll(preSelected);
                adapter.notifyDataSetChanged();
                updateExerciseCount();
            });
        }).start();
    }

    /** Fetch selected Exercise objects from DB and add as SelectedExercise if not already in list. */
    private void loadExercisesByIds(int[] ids) {
        new Thread(() -> {
            // Build id -> Exercise map to preserve order
            List<Exercise> allExercises = db.routineDao().getAllExercises();
            List<SelectedExercise> toAdd = new ArrayList<>();
            for (int id : ids) {
                // Skip duplicates already in list
                boolean alreadyAdded = false;
                for (SelectedExercise se : selectedExercises) {
                    if (se.exerciseId == id) { alreadyAdded = true; break; }
                }
                if (alreadyAdded) continue;
                for (Exercise e : allExercises) {
                    if (e.getId() == id) {
                        toAdd.add(new SelectedExercise(e.getId(), e.getName(), e.getCategory()));
                        break;
                    }
                }
            }
            requireActivity().runOnUiThread(() -> {
                int insertStart = selectedExercises.size();
                selectedExercises.addAll(toAdd);
                adapter.notifyItemRangeInserted(insertStart, toAdd.size());
                updateExerciseCount();
            });
        }).start();
    }

    private void updateExerciseCount() {
        int count = selectedExercises.size();
        tvExerciseCount.setText(count + " EXERCISE" + (count == 1 ? "" : "S"));
    }

    private void saveRoutine(String name) {
        List<SelectedExercise> snapshot = new ArrayList<>(selectedExercises);
        new Thread(() -> {
            List<RoutineExercise> rows = new ArrayList<>();

            if (editRoutineId == -1) {
                // CREATE mode: insert new routine
                Routine routine = new Routine(name, "", snapshot.size());
                long routineId = db.routineDao().insertRoutine(routine);
                for (int i = 0; i < snapshot.size(); i++) {
                    SelectedExercise se = snapshot.get(i);
                    rows.add(new RoutineExercise((int) routineId, se.exerciseId,
                            se.sets, se.reps, se.restSeconds, se.durationSeconds, i));
                }
                db.routineDao().insertRoutineExercises(rows);
            } else {
                // EDIT mode: update routine row + replace all exercises
                db.routineDao().updateRoutine(editRoutineId, name, snapshot.size());
                db.routineDao().deleteRoutineExercisesByRoutineId(editRoutineId);
                for (int i = 0; i < snapshot.size(); i++) {
                    SelectedExercise se = snapshot.get(i);
                    rows.add(new RoutineExercise(editRoutineId, se.exerciseId,
                            se.sets, se.reps, se.restSeconds, se.durationSeconds, i));
                }
                db.routineDao().insertRoutineExercises(rows);
            }

            requireActivity().runOnUiThread(() -> {
                Toast.makeText(requireContext(),
                        editRoutineId == -1 ? "Routine saved!" : "Routine updated!",
                        Toast.LENGTH_SHORT).show();
                requireActivity().getSupportFragmentManager().popBackStack();
            });
        }).start();
    }
}

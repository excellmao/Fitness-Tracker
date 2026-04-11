package com.example.fitnesstracker.workout;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
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

public class EditRoutineFragment extends Fragment {

    private int routineId;
    private FitnessDatabase db;

    // UI Containers for the State Machine
    private ConstraintLayout clStateEdit, clStateSelect;

    // Adapters
    private EditRoutineAdapter editAdapter;
    private SelectExerciseAdapter selectAdapter;

    // Data Lists
    private List<ExerciseWithSettings> activeExercises = new ArrayList<>();
    private List<Exercise> allDatabaseExercises = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_edit_routine, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        routineId = getArguments() != null ? getArguments().getInt("routine_id", -1) : -1;
        db = FitnessDatabase.getInstance(requireContext());

        // 1. Initialize Views
        clStateEdit = view.findViewById(R.id.clStateEdit);
        clStateSelect = view.findViewById(R.id.clStateSelect);
        TextView tvRoutineName = view.findViewById(R.id.tvEditRoutineName);

        // 2. Setup Recycler Views
        RecyclerView rvEdit = view.findViewById(R.id.rvEditExercises);
        rvEdit.setLayoutManager(new LinearLayoutManager(requireContext()));
        editAdapter = new EditRoutineAdapter(activeExercises, position -> {
            activeExercises.remove(position);
            editAdapter.notifyItemRemoved(position);
        });
        rvEdit.setAdapter(editAdapter);

        RecyclerView rvSelect = view.findViewById(R.id.rvDatabaseExercises);
        rvSelect.setLayoutManager(new LinearLayoutManager(requireContext()));
        selectAdapter = new SelectExerciseAdapter(allDatabaseExercises, exercise -> {
            // When user clicks the + button on a database exercise:
            ExerciseWithSettings newEx = new ExerciseWithSettings();
            newEx.exerciseId = exercise.getId();
            newEx.name = exercise.getName();
            newEx.sets = 3;             // Default Values
            newEx.durationSeconds = 10; // Default Reps
            newEx.restSeconds = 60;     // Default Rest

            activeExercises.add(newEx);
            editAdapter.notifyItemInserted(activeExercises.size() - 1);

            // Go back to Edit screen automatically
            switchState(false);
            Toast.makeText(requireContext(), exercise.getName() + " Added", Toast.LENGTH_SHORT).show();
        });
        rvSelect.setAdapter(selectAdapter);

        // 3. Setup Click Listeners
        view.findViewById(R.id.ibEditBack).setOnClickListener(v -> requireActivity().getSupportFragmentManager().popBackStack());
        view.findViewById(R.id.ibSelectBack).setOnClickListener(v -> switchState(false));
        view.findViewById(R.id.btnAddNewExercise).setOnClickListener(v -> switchState(true));

        // SAVE BUTTON LOGIC
        view.findViewById(R.id.tvSaveRoutine).setOnClickListener(v -> {
            // Clear focus from EditTexts to trigger the final save
            view.clearFocus();
            saveRoutineToDatabase();
        });

        // 4. Load Initial Data
        if (routineId != -1) loadData(tvRoutineName);

        // Setup our category filters
        setupCategoryChips(view);

        // ALWAYS load data, regardless if it's new or editing!
        loadData(tvRoutineName);
    }

    private void switchState(boolean showSelectScreen) {
        clStateEdit.setVisibility(showSelectScreen ? View.GONE : View.VISIBLE);
        clStateSelect.setVisibility(showSelectScreen ? View.VISIBLE : View.GONE);
    }

    private void loadData(TextView tvName) {
        new Thread(() -> {
            // ALWAYS fetch the master list from the database
            allDatabaseExercises = db.routineDao().getAllExercises();

            // ONLY fetch existing routine data if we are editing
            if (routineId != -1) {
                activeExercises = db.routineDao().getExercisesForRoutine(routineId);
                Routine routine = db.routineDao().getRoutineById(routineId);

                requireActivity().runOnUiThread(() -> {
                    if (routine != null) tvName.setText(routine.getName());
                    editAdapter.setExercises(activeExercises);
                });
            }

            // Push the master list to the UI
            requireActivity().runOnUiThread(() -> {
                selectAdapter.setExercises(allDatabaseExercises);
            });
        }).start();
    }

    private void saveRoutineToDatabase() {
        new Thread(() -> {
            List<ExerciseWithSettings> updatedList = editAdapter.getUpdatedExercises();
            int currentRoutineId = routineId; // Track the ID we are working with

            // ==========================================
            // STEP 1: Handle Creating vs. Editing
            // ==========================================
            if (currentRoutineId == -1) {
                // CREATE MODE: We must create the Routine first!
                // Grab the name the user typed, or give it a default
                TextView tvName = requireView().findViewById(R.id.tvEditRoutineName);
                String routineName = tvName.getText().toString();
                if (routineName.isEmpty()) routineName = "Custom Routine";

                int estimatedMins = updatedList.size() * 8;
                int estimatedKcal = estimatedMins * 10;

                // Create the routine object
                Routine newRoutine = new Routine(
                        routineName,
                        "Custom Workout",
                        "PHASE 01 / CUSTOM",
                        updatedList.size(),
                        estimatedMins,
                        estimatedKcal,
                        "" // Empty cover image for custom routines
                );

                // Insert it and grab the brand new ID!
                currentRoutineId = (int) db.routineDao().insertRoutine(newRoutine);

            } else {
                // EDIT MODE: Update existing routine stats and clear old exercises
                Routine existingRoutine = db.routineDao().getRoutineById(currentRoutineId);
                if (existingRoutine != null) {
                    existingRoutine.exerciseCount = updatedList.size();
                    existingRoutine.totalMinutes = updatedList.size() * 8;
                    existingRoutine.totalCalories = existingRoutine.totalMinutes * 10;
                    db.routineDao().updateRoutine(existingRoutine);
                }

                // FIX: Make sure this ONLY deletes the exercises, NOT the routine itself!
                db.routineDao().deleteRoutineExercisesByRoutineId(currentRoutineId);
            }

            // ==========================================
            // STEP 2: Save the Exercises
            // ==========================================
            List<RoutineExercise> newDbList = new ArrayList<>();
            for (int i = 0; i < updatedList.size(); i++) {
                ExerciseWithSettings ex = updatedList.get(i);
                // Use the currentRoutineId (which is either the existing one, or the brand new one!)
                newDbList.add(new RoutineExercise(currentRoutineId, ex.exerciseId, ex.sets, ex.durationSeconds, ex.restSeconds, 0, i));
            }

            db.routineDao().insertRoutineExercises(newDbList);

            // ==========================================
            // STEP 3: Return to Previous Screen
            // ==========================================
            requireActivity().runOnUiThread(() -> {
                Toast.makeText(requireContext(), "Routine Saved!", Toast.LENGTH_SHORT).show();
                requireActivity().getSupportFragmentManager().popBackStack();
            });
        }).start();
    }

    private void setupCategoryChips(View view) {
        TextView chipAll = view.findViewById(R.id.chipAll);
        TextView chipChest = view.findViewById(R.id.chipChest);
        TextView chipBack = view.findViewById(R.id.chipBack);
        TextView chipLegs = view.findViewById(R.id.chipLegs);

        TextView[] chips = {chipAll, chipChest, chipBack, chipLegs};
        String[] categories = {"ALL", "Chest", "Back", "Legs"};

        for (int i = 0; i < chips.length; i++) {
            final int index = i;
            chips[i].setOnClickListener(v -> {
                // 1. Reset all chips to dark theme
                for (TextView c : chips) {
                    c.setBackgroundResource(R.drawable.bg_pill_dark);
                    c.setTextColor(getResources().getColor(R.color.text_gray, null));
                }
                // 2. Set the tapped chip to Yellow
                chips[index].setBackgroundResource(R.drawable.bg_pill_yellow);
                chips[index].setTextColor(0xFF000000); // Solid Black text

                // 3. Filter the list!
                filterDatabaseList(categories[index]);
            });
        }
    }

    private void filterDatabaseList(String category) {
        if (category.equals("ALL")) {
            selectAdapter.setExercises(allDatabaseExercises);
            return;
        }

        List<Exercise> filteredList = new ArrayList<>();
        for (Exercise ex : allDatabaseExercises) {
            // Check if the exercise's target muscle matches the pill we tapped
            if (ex.getCategory() != null && ex.getCategory().equalsIgnoreCase(category)) {
                filteredList.add(ex);
            }
        }
        selectAdapter.setExercises(filteredList);
    }

    // --- FULL SCREEN HACKS ---
    @Override
    public void onResume() {
        super.onResume();
        View navBar = requireActivity().findViewById(R.id.bottomNavContainer);
        if (navBar != null) navBar.setVisibility(View.GONE);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        View navBar = requireActivity().findViewById(R.id.bottomNavContainer);
        if (navBar != null) navBar.setVisibility(View.VISIBLE);
    }
}
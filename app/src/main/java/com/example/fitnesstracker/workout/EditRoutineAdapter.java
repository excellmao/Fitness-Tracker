package com.example.fitnesstracker.workout;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.fitnesstracker.R;
import com.example.fitnesstracker.database.ExerciseWithSettings;
import java.util.List;

public class EditRoutineAdapter extends RecyclerView.Adapter<EditRoutineAdapter.ViewHolder> {

    private List<ExerciseWithSettings> activeExercises;
    private OnExerciseDeleteListener deleteListener;

    public interface OnExerciseDeleteListener {
        void onDeleteClicked(int position);
    }

    public EditRoutineAdapter(List<ExerciseWithSettings> activeExercises, OnExerciseDeleteListener deleteListener) {
        this.activeExercises = activeExercises;
        this.deleteListener = deleteListener;
    }

    public void setExercises(List<ExerciseWithSettings> exercises) {
        this.activeExercises = exercises;
        notifyDataSetChanged();
    }

    // This lets our Fragment grab the updated data when the user clicks "SAVE"
    public List<ExerciseWithSettings> getUpdatedExercises() {
        return activeExercises;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_edit_exercise, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ExerciseWithSettings currentEx = activeExercises.get(position);

        holder.tvName.setText(currentEx.name);
        holder.etSets.setText(String.valueOf(currentEx.sets));
        holder.etReps.setText(String.valueOf(currentEx.durationSeconds)); // Using duration as reps
        holder.etRest.setText(String.valueOf(currentEx.restSeconds));

        // Save typed data when they tap away from the textbox
        holder.etSets.setOnFocusChangeListener((v, hasFocus) -> {
            if (!hasFocus) currentEx.sets = parseInput(holder.etSets.getText().toString());
        });
        holder.etReps.setOnFocusChangeListener((v, hasFocus) -> {
            if (!hasFocus) currentEx.durationSeconds = parseInput(holder.etReps.getText().toString());
        });
        holder.etRest.setOnFocusChangeListener((v, hasFocus) -> {
            if (!hasFocus) currentEx.restSeconds = parseInput(holder.etRest.getText().toString());
        });

        holder.btnDelete.setOnClickListener(v -> {
            if (deleteListener != null) deleteListener.onDeleteClicked(holder.getAdapterPosition());
        });
    }

    private int parseInput(String val) {
        try { return Integer.parseInt(val.replaceAll("[^0-9]", "")); }
        catch (NumberFormatException e) { return 0; }
    }

    @Override
    public int getItemCount() { return activeExercises.size(); }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvName;
        EditText etSets, etReps, etRest;
        ImageButton btnDelete;

        ViewHolder(View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvEditItemName);
            etSets = itemView.findViewById(R.id.etEditSets);
            etReps = itemView.findViewById(R.id.etEditReps);
            etRest = itemView.findViewById(R.id.etEditRest);
            btnDelete = itemView.findViewById(R.id.btnDeleteExercise);
        }
    }
}
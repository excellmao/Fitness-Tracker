package com.example.fitnesstracker.workout;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.fitnesstracker.R;
import com.example.fitnesstracker.database.ExerciseWithSettings;
import java.util.List;

public class ExerciseDetailAdapter extends RecyclerView.Adapter<ExerciseDetailAdapter.ViewHolder> {

    private List<ExerciseWithSettings> exercises;

    public ExerciseDetailAdapter(List<ExerciseWithSettings> exercises) {
        this.exercises = exercises;
    }

    public void setExercises(List<ExerciseWithSettings> exercises) {
        this.exercises = exercises;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_exercise_detail, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ExerciseWithSettings ex = exercises.get(position);
        holder.tvName.setText(ex.name);
        // Displays something like "3 Sets x 12 Reps"
        holder.tvStats.setText(ex.sets + " Sets x " + ex.reps + " Reps");
    }

    @Override
    public int getItemCount() {
        return exercises.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvStats;
        ViewHolder(View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvDetailExName);
            tvStats = itemView.findViewById(R.id.tvDetailExStats);
        }
    }
}
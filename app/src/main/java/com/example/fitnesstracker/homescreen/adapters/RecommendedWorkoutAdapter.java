package com.example.fitnesstracker.homescreen.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.fitnesstracker.R;
import com.example.fitnesstracker.database.Routine;
import com.example.fitnesstracker.homescreen.models.WorkoutRoutine;

import java.util.List;

public class RecommendedWorkoutAdapter extends RecyclerView.Adapter<RecommendedWorkoutAdapter.WorkoutViewHolder> {

    private List<WorkoutRoutine> workoutList;

    public RecommendedWorkoutAdapter(List<Routine> workoutList) {
        this.workoutList = workoutList;
    }

    @NonNull
    @Override
    public WorkoutViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_recommended_workout, parent, false);
        return new WorkoutViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull WorkoutViewHolder holder, int position) {
        WorkoutRoutine routine = workoutList.get(position);

        holder.tvTitle.setText(routine.getTitle());
        holder.tvLevel.setText(routine.getLevel());
        holder.tvDuration.setText("⏱ " + routine.getDurationMinutes() + " MIN");
        holder.tvCalories.setText("🔥 " + routine.getCaloriesBurned() + " CAL");
        holder.ivImage.setImageResource(routine.getImageResource());
    }

    @Override
    public int getItemCount() {
        return workoutList.size();
    }

    static class WorkoutViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitle, tvLevel, tvDuration, tvCalories;
        ImageView ivImage;

        public WorkoutViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tvWorkoutTitle);
            tvLevel = itemView.findViewById(R.id.tvLevelBadge);
            tvDuration = itemView.findViewById(R.id.tvDuration);
            tvCalories = itemView.findViewById(R.id.tvCalories);
            ivImage = itemView.findViewById(R.id.ivWorkoutImage);
        }
    }
}
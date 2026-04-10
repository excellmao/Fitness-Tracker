package com.example.fitnesstracker.homescreen.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.fitnesstracker.R;
import com.example.fitnesstracker.database.Routine;

import java.util.List;

public class RecommendedWorkoutAdapter extends RecyclerView.Adapter<RecommendedWorkoutAdapter.WorkoutViewHolder> {

    public interface OnRoutineClickListener {
        void onRoutineClick(int routineId);
    }

    private List<Routine> workoutList;
    private OnRoutineClickListener listener;
    public RecommendedWorkoutAdapter(List<Routine> workoutList, OnRoutineClickListener listener) {
        this.workoutList = workoutList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public WorkoutViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_recommended_workout, parent, false);
        return new WorkoutViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull WorkoutViewHolder holder, int position) {
        Routine routine = workoutList.get(position);

        holder.tvTitle.setText(routine.getName());
        holder.tvLevel.setText(routine.getPhase());
        holder.tvDuration.setText("⏱ " + routine.getTotalMinutes() + " MIN");
        holder.tvCalories.setText("🔥 " + routine.getTotalCalories() + " KCAL");

        String imageUrl = routine.getCoverImageUrl();

        if (imageUrl != null && !imageUrl.isEmpty()) {
            Glide.with(holder.itemView.getContext())
                    .load(imageUrl)
                    .centerCrop() // Makes it fill the rounded card nicely
                    .into(holder.ivImage);
        } else {
            // Fallback if the database URL is empty
            holder.ivImage.setImageResource(R.drawable.bg_card_dark);
        }

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onRoutineClick(routine.getId());
            }
        });
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
package com.example.fitnesstracker.workout;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.example.fitnesstracker.R;
import com.example.fitnesstracker.database.Exercise;
import java.util.List;

public class SelectExerciseAdapter extends RecyclerView.Adapter<SelectExerciseAdapter.ViewHolder> {

    private List<Exercise> exerciseList;
    private OnExerciseAddListener listener;

    public interface OnExerciseAddListener {
        void onAddClicked(Exercise exercise);
    }

    public SelectExerciseAdapter(List<Exercise> exerciseList, OnExerciseAddListener listener) {
        this.exerciseList = exerciseList;
        this.listener = listener;
    }

    public void setExercises(List<Exercise> exercises) {
        this.exerciseList = exercises;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_select_exercise, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Exercise exercise = exerciseList.get(position);
        holder.tvName.setText(exercise.getName());
        holder.tvDetails.setText(exercise.getCategory().toUpperCase() + " • " + exercise.getLevel().toUpperCase());

        if (exercise.getGifUrl() != null && !exercise.getGifUrl().isEmpty()) {
            Glide.with(holder.itemView.getContext()).load(exercise.getGifUrl()).centerCrop().into(holder.ivThumb);
        }

        holder.btnAdd.setOnClickListener(v -> {
            if (listener != null) listener.onAddClicked(exercise);
        });
    }

    @Override
    public int getItemCount() { return exerciseList.size(); }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvDetails;
        ImageView ivThumb;
        ImageButton btnAdd;

        ViewHolder(View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvSelectName);
            tvDetails = itemView.findViewById(R.id.tvSelectDetails);
            ivThumb = itemView.findViewById(R.id.ivSelectThumb);
            btnAdd = itemView.findViewById(R.id.btnAddExercise);
        }
    }
}
package com.example.fitnesstracker.profile.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.fitnesstracker.R;
import com.example.fitnesstracker.database.WorkoutLog; // Adjust package as needed
import java.util.ArrayList;
import java.util.List;

public class HistoryAdapter extends RecyclerView.Adapter<HistoryAdapter.HistoryViewHolder> {

    private List<WorkoutLog> logsList = new ArrayList<>();

    public void setLogs(List<WorkoutLog> logsList) {
        this.logsList = logsList;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public HistoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_archive_record, parent, false); // <-- Change this line!
        return new HistoryViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull HistoryViewHolder holder, int position) {
        WorkoutLog currentLog = logsList.get(position);

        // Bind the data to the UI!
        holder.tvDate.setText(currentLog.date);
        holder.tvWorkoutType.setText(currentLog.workoutType);
        holder.tvDuration.setText(currentLog.durationMinutes + " min");
    }

    @Override
    public int getItemCount() {
        return logsList.size();
    }

    static class HistoryViewHolder extends RecyclerView.ViewHolder {
        TextView tvDate, tvWorkoutType, tvDuration;

        public HistoryViewHolder(@NonNull View itemView) {
            super(itemView);
            tvDate = itemView.findViewById(R.id.tvDate);
            tvWorkoutType = itemView.findViewById(R.id.tvWorkoutType);
            tvDuration = itemView.findViewById(R.id.tvDuration);
        }
    }
}
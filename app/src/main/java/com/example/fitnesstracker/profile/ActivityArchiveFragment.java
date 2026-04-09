package com.example.fitnesstracker.profile;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.fitnesstracker.R;
import com.example.fitnesstracker.database.FitnessDatabase;
import com.example.fitnesstracker.database.WorkoutLog;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ActivityArchiveFragment extends Fragment {

    private RecyclerView rvActivityHistory;
    private ArchiveAdapter adapter;

    public ActivityArchiveFragment() {}

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_activity_archive, container, false);

        rvActivityHistory = view.findViewById(R.id.rvActivityHistory);

        // BACK BUTTON LOGIC
        View btnBack = view.findViewById(R.id.btnBack);
        btnBack.setOnClickListener(v -> {
            requireActivity().getSupportFragmentManager().popBackStack();
        });

        setupRecyclerView();
        loadHistoryFromDatabase();

        return view;
    }

    private void setupRecyclerView() {
        rvActivityHistory.setLayoutManager(new LinearLayoutManager(requireContext()));
        adapter = new ArchiveAdapter(new ArrayList<>());
        rvActivityHistory.setAdapter(adapter);
    }

    private void loadHistoryFromDatabase() {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.execute(() -> {
            FitnessDatabase db = FitnessDatabase.getInstance(requireContext());

            // Grab history from the Home Screen DB entries
            List<WorkoutLog> realHistory = db.workoutDao().getAllWorkoutLogs();

            requireActivity().runOnUiThread(() -> {
                adapter.setLogs(realHistory);
            });
        });
    }

    // =========================================================================
    // THE ADAPTER
    // =========================================================================

    private static class ArchiveAdapter extends RecyclerView.Adapter<ArchiveAdapter.ViewHolder> {
        private List<WorkoutLog> data;

        ArchiveAdapter(List<WorkoutLog> data) {
            this.data = data;
        }

        public void setLogs(List<WorkoutLog> newData) {
            this.data = newData;
            notifyDataSetChanged();
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_archive_record, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            WorkoutLog item = data.get(position);

            // Map to the correct updated variables
            holder.tvWorkoutType.setText(item.workoutType);
            holder.tvDate.setText(item.date);
            holder.tvDuration.setText(item.durationMinutes + " mins");
        }

        @Override
        public int getItemCount() { return data.size(); }

        static class ViewHolder extends RecyclerView.ViewHolder {
            TextView tvWorkoutType, tvDate, tvDuration;

            ViewHolder(View itemView) {
                super(itemView);
                tvWorkoutType = itemView.findViewById(R.id.tvWorkoutType);
                tvDate = itemView.findViewById(R.id.tvDate);
                tvDuration = itemView.findViewById(R.id.tvDuration);
            }
        }
    }
}
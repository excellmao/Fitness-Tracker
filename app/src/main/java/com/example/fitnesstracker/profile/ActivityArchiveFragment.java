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

import java.util.ArrayList;
import java.util.List;

public class ActivityArchiveFragment extends Fragment {

    private RecyclerView rvActivityHistory;

    public ActivityArchiveFragment() {}

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_activity_archive, container, false);

        rvActivityHistory = view.findViewById(R.id.rvActivityHistory);

        // --- THÊM 3 DÒNG NÀY ĐỂ XỬ LÝ NÚT QUAY LẠI ---
        View btnBack = view.findViewById(R.id.btnBack);
        btnBack.setOnClickListener(v -> {
            requireActivity().getSupportFragmentManager().popBackStack();
        });
        // ---------------------------------------------

        setupRecyclerView();
        return view;
    }

    private void setupRecyclerView() {
        rvActivityHistory.setLayoutManager(new LinearLayoutManager(requireContext()));

        List<MockArchiveModel> dummyData = new ArrayList<>();
        dummyData.add(new MockArchiveModel("Leg Day Annihilation", "05/04/2026", "450 kcal | 60 mins"));
        dummyData.add(new MockArchiveModel("Upper Body Pump", "03/04/2026", "380 kcal | 45 mins"));
        dummyData.add(new MockArchiveModel("Morning 5K Run", "01/04/2026", "520 kcal | 28 mins"));
        dummyData.add(new MockArchiveModel("Core & Abs", "30/03/2026", "200 kcal | 20 mins"));

        ArchiveAdapter adapter = new ArchiveAdapter(dummyData);
        rvActivityHistory.setAdapter(adapter);
    }

    private static class MockArchiveModel {
        String title, date, stats;
        MockArchiveModel(String title, String date, String stats) {
            this.title = title; this.date = date; this.stats = stats;
        }
    }

    private static class ArchiveAdapter extends RecyclerView.Adapter<ArchiveAdapter.ViewHolder> {
        private final List<MockArchiveModel> data;

        ArchiveAdapter(List<MockArchiveModel> data) { this.data = data; }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_archive_record, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            MockArchiveModel item = data.get(position);
            holder.tvRoutineName.setText(item.title);
            holder.tvDate.setText(item.date);
            holder.tvStats.setText(item.stats);
        }

        @Override
        public int getItemCount() { return data.size(); }

        static class ViewHolder extends RecyclerView.ViewHolder {
            TextView tvRoutineName, tvDate, tvStats;
            ViewHolder(View itemView) {
                super(itemView);
                tvRoutineName = itemView.findViewById(R.id.tvRoutineName);
                tvDate = itemView.findViewById(R.id.tvDate);
                tvStats = itemView.findViewById(R.id.tvStats);
            }
        }
    }
}
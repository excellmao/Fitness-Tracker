package com.example.fitnesstracker.profile;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.fitnesstracker.R;
import com.example.fitnesstracker.database.FitnessDatabase;
import com.example.fitnesstracker.database.MetricDao;
import com.example.fitnesstracker.profile.models.UserMetricLog;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ProfileFragment extends Fragment {

    private RadioGroup rgGender, rgChartToggle;
    private EditText etHeight, etWeight;
    private Button btnSaveMetrics, btnViewArchive;
    private LineChart chartProgress;

    private SharedPreferences prefs;

    // Chart Data
    private List<Entry> weightEntries;
    private List<Entry> caloEntries;
    private float currentDayIndex = 5f;

    public ProfileFragment() {}

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        rgGender = view.findViewById(R.id.rgGender);
        rgChartToggle = view.findViewById(R.id.rgChartToggle);
        etHeight = view.findViewById(R.id.etHeight);
        etWeight = view.findViewById(R.id.etWeight);
        btnSaveMetrics = view.findViewById(R.id.btnSaveMetrics);
        chartProgress = view.findViewById(R.id.chartWeightProgress);
        btnViewArchive = view.findViewById(R.id.btnViewArchive);

        prefs = requireContext().getSharedPreferences("KineticProfile", Context.MODE_PRIVATE);

        loadSavedMetrics();
        setupClickListeners();

        weightEntries = new ArrayList<>();
        caloEntries = new ArrayList<>();

        // Fetch live data and draw chart
        loadDatabaseMetrics();

        return view;
    }

    private void loadSavedMetrics() {
        int genderId = prefs.getInt("genderId", R.id.rbMale);
        rgGender.check(genderId);
        etHeight.setText(prefs.getString("height", ""));
        etWeight.setText(prefs.getString("weight", ""));
    }

    private void setupClickListeners() {
        // SAVE BUTTON
        btnSaveMetrics.setOnClickListener(v -> {
            String weightStr = etWeight.getText().toString().trim();
            String heightStr = etHeight.getText().toString().trim();

            if (weightStr.isEmpty()) {
                Toast.makeText(requireContext(), "Vui lòng nhập cân nặng", Toast.LENGTH_SHORT).show();
                return;
            }

            prefs.edit()
                    .putInt("genderId", rgGender.getCheckedRadioButtonId())
                    .putString("height", heightStr)
                    .putString("weight", weightStr)
                    .apply();

            float newWeight = Float.parseFloat(weightStr);
            currentDayIndex += 1f;

            // Background thread to save to Room DB
            ExecutorService executor = Executors.newSingleThreadExecutor();
            executor.execute(() -> {
                FitnessDatabase db = FitnessDatabase.getInstance(requireContext());
                // Assuming 2000 cal average for newly entered weights
                db.metricDao().insertMetric(new UserMetricLog(currentDayIndex, newWeight, 2000f));

                requireActivity().runOnUiThread(() -> {
                    weightEntries.add(new Entry(currentDayIndex, newWeight));
                    if (rgChartToggle.getCheckedRadioButtonId() == R.id.rbChartWeight) {
                        drawChart(weightEntries, "Cân nặng (kg)", "#CCFF00");
                    }
                    Toast.makeText(requireContext(), "Đã cập nhật biểu đồ!", Toast.LENGTH_SHORT).show();
                });
            });
        });

        // TOGGLE CHART
        rgChartToggle.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.rbChartWeight) {
                drawChart(weightEntries, "Cân nặng (kg)", "#CCFF00");
            } else if (checkedId == R.id.rbChartCalo) {
                drawChart(caloEntries, "Calo tiêu thụ (kcal)", "#FF5722");
            }
        });

        // VIEW ARCHIVE
        btnViewArchive.setOnClickListener(v -> {
            requireActivity().getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragmentContainer, new ActivityArchiveFragment())
                    .addToBackStack(null)
                    .commit();
        });
    }

    private void loadDatabaseMetrics() {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.execute(() -> {
            FitnessDatabase db = FitnessDatabase.getInstance(requireContext());
            MetricDao dao = db.metricDao();

            // Seed initial data if empty
            if (dao.getMetricCount() == 0) {
                dao.insertMetric(new UserMetricLog(1f, 75.0f, 2200f));
                dao.insertMetric(new UserMetricLog(2f, 74.5f, 1800f));
                dao.insertMetric(new UserMetricLog(3f, 74.2f, 2500f));
                dao.insertMetric(new UserMetricLog(4f, 73.8f, 2100f));
                dao.insertMetric(new UserMetricLog(5f, 73.0f, 2000f));
            }

            List<UserMetricLog> logs = dao.getAllMetrics();
            float maxIndex = 0;

            for (UserMetricLog log : logs) {
                weightEntries.add(new Entry(log.dayIndex, log.weight));
                caloEntries.add(new Entry(log.dayIndex, log.calories));
                if (log.dayIndex > maxIndex) maxIndex = log.dayIndex;
            }
            currentDayIndex = maxIndex;

            requireActivity().runOnUiThread(() -> {
                if (rgChartToggle.getCheckedRadioButtonId() == R.id.rbChartWeight) {
                    drawChart(weightEntries, "Cân nặng (kg)", "#CCFF00");
                } else {
                    drawChart(caloEntries, "Calo tiêu thụ (kcal)", "#FF5722");
                }
            });
        });
    }

    private void drawChart(List<Entry> entries, String label, String colorHex) {
        LineDataSet dataSet = new LineDataSet(entries, label);

        int chartColor = android.graphics.Color.parseColor(colorHex);
        int whiteColor = getResources().getColor(android.R.color.white, requireContext().getTheme());

        dataSet.setColor(chartColor);
        dataSet.setCircleColor(chartColor);
        dataSet.setValueTextColor(whiteColor);
        dataSet.setLineWidth(2.5f);
        dataSet.setCircleRadius(5f);

        LineData lineData = new LineData(dataSet);
        chartProgress.setData(lineData);

        chartProgress.getDescription().setEnabled(false);
        chartProgress.getAxisRight().setEnabled(false);
        chartProgress.getAxisLeft().setTextColor(whiteColor);
        chartProgress.getXAxis().setTextColor(whiteColor);
        chartProgress.getXAxis().setPosition(XAxis.XAxisPosition.BOTTOM);
        chartProgress.getLegend().setTextColor(whiteColor);

        chartProgress.invalidate();
    }
}
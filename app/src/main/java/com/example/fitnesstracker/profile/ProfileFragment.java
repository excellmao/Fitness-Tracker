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
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;

import java.util.ArrayList;
import java.util.List;

public class ProfileFragment extends Fragment {

    private RadioGroup rgGender, rgChartToggle;
    private EditText etHeight, etWeight;
    private Button btnSaveMetrics, btnViewArchive;
    private LineChart chartProgress;

    private SharedPreferences prefs;

    // Dữ liệu biểu đồ động
    private List<Entry> weightEntries;
    private List<Entry> caloEntries;
    private float currentDayIndex = 5f; // Trục X hiện tại

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

        initMockData();
        loadSavedMetrics();
        setupClickListeners();

        // Vẽ biểu đồ Cân nặng mặc định khi mở app
        drawChart(weightEntries, "Cân nặng (kg)", "#CCFF00");

        return view;
    }

    private void initMockData() {
        weightEntries = new ArrayList<>();
        weightEntries.add(new Entry(1f, 75.0f));
        weightEntries.add(new Entry(2f, 74.5f));
        weightEntries.add(new Entry(3f, 74.2f));
        weightEntries.add(new Entry(4f, 73.8f));
        weightEntries.add(new Entry(5f, 73.0f));

        caloEntries = new ArrayList<>();
        caloEntries.add(new Entry(1f, 2200f));
        caloEntries.add(new Entry(2f, 1800f));
        caloEntries.add(new Entry(3f, 2500f));
        caloEntries.add(new Entry(4f, 2100f));
        caloEntries.add(new Entry(5f, 2000f));
    }

    private void loadSavedMetrics() {
        int genderId = prefs.getInt("genderId", R.id.rbMale);
        rgGender.check(genderId);
        etHeight.setText(prefs.getString("height", ""));
        etWeight.setText(prefs.getString("weight", ""));
    }

    private void setupClickListeners() {
        // NÚT LƯU: Cập nhật chỉ số và vẽ thêm vào biểu đồ
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

            // Cập nhật điểm mới lên biểu đồ cân nặng
            float newWeight = Float.parseFloat(weightStr);
            currentDayIndex += 1f;
            weightEntries.add(new Entry(currentDayIndex, newWeight));

            // Nếu đang ở tab Cân nặng thì vẽ lại luôn
            if (rgChartToggle.getCheckedRadioButtonId() == R.id.rbChartWeight) {
                drawChart(weightEntries, "Cân nặng (kg)", "#CCFF00");
            }

            Toast.makeText(requireContext(), "Đã cập nhật biểu đồ!", Toast.LENGTH_SHORT).show();
        });

        // CHUYỂN ĐỔI BIỂU ĐỒ: Giữa Cân Nặng và Calo
        rgChartToggle.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.rbChartWeight) {
                drawChart(weightEntries, "Cân nặng (kg)", "#CCFF00"); // Màu neon
            } else if (checkedId == R.id.rbChartCalo) {
                drawChart(caloEntries, "Calo tiêu thụ (kcal)", "#FF5722"); // Màu cam cho dễ phân biệt
            }
        });

        btnViewArchive.setOnClickListener(v -> {
            requireActivity().getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragmentContainer, new ActivityArchiveFragment())
                    .addToBackStack(null)
                    .commit();
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

        chartProgress.invalidate(); // Refresh để vẽ lại
    }
}
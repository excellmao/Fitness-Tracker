package com.example.fitnesstracker.run;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.fitnesstracker.R;
import com.google.android.material.button.MaterialButton;

public class RunPrepFragment extends Fragment {

    public RunPrepFragment() {
        // Required empty public constructor
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // Inflate layout cho màn hình Start Run
        View view = inflater.inflate(R.layout.fragment_run_prep, container, false);

        MaterialButton btnStartNow = view.findViewById(R.id.btnStartNow);
        btnStartNow.setOnClickListener(v -> {
            // Khi nhấn START NOW, chuyển sang ActiveRunActivity
            Intent intent = new Intent(requireActivity(), ActiveRunActivity.class);
            startActivity(intent);
        });

        return view;
    }
}
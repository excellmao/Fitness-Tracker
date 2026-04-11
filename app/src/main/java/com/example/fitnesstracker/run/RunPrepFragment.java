package com.example.fitnesstracker.run;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

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
        TextView tvCountdown = view.findViewById(R.id.tvCountdown);

        btnStartNow.setOnClickListener(v -> {
            // 1. Disable and fade out the button so they can't double-click
            btnStartNow.setEnabled(false);
            btnStartNow.animate().alpha(0f).setDuration(300);

            // 2. Start a 3-second countdown (3000 milliseconds)
            new android.os.CountDownTimer(3000, 1000) {
                @Override
                public void onTick(long millisUntilFinished) {
                    // Update the big yellow number every second
                    int secondsLeft = (int) (millisUntilFinished / 1000) + 1;
                    tvCountdown.setText(String.valueOf(secondsLeft));
                }

                @Override
                public void onFinish() {
                    // 3. Yell GO! and launch the map screen
                    tvCountdown.setText("GO!");
                    Intent intent = new Intent(requireActivity(), ActiveRunActivity.class);
                    startActivity(intent);

                    // 4. Quietly reset the UI behind the scenes for the next time they run
                    new android.os.Handler().postDelayed(() -> {
                        tvCountdown.setText("3");
                        btnStartNow.setEnabled(true);
                        btnStartNow.setAlpha(1f);
                    }, 1000);
                }
            }.start();
        });

        return view;
    }
}
package com.example.fitnesstracker.run;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.fitnesstracker.R;

public class RunSummaryFragment extends Fragment {

    public RunSummaryFragment() {
        // Constructor trống bắt buộc
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // LUẬT 2: Sử dụng view.findViewById và requireActivity()
        View view = inflater.inflate(R.layout.fragment_run_summary, container, false);

        Button btnDone = view.findViewById(R.id.btnDone);

        // Xử lý sự kiện khi nhấn nút DONE
        btnDone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Chuyển sang HomeScreenActivity bằng Intent
                // Sử dụng đường dẫn đầy đủ để đảm bảo compiler tìm thấy class
                Intent intent = new Intent(requireActivity(), com.example.fitnesstracker.homescreen.HomeScreenActivity.class);

                // Flag này giúp dọn dẹp các màn hình trung gian, quay về Home sạch sẽ
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);

                startActivity(intent);

                // Đóng Activity hiện tại (ActiveRunActivity) để giải phóng bộ nhớ
                requireActivity().finish();
            }
        });

        return view;
    }
}
package com.example.fitnesstracker;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import com.example.fitnesstracker.run.RunPrepFragment; // Đảm bảo import đúng package

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // KHI MỞ APP: Hiển thị màn hình Start Run (Fragment) ngay lập tức
        if (savedInstanceState == null) {
            loadFragment(new RunPrepFragment());
        }

        // (Tùy chọn) Xử lý khi nhấn vào các icon trên Bottom Navigation
        // setupBottomNavigation();
    }

    private void loadFragment(Fragment fragment) {
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .commit();
    }
}
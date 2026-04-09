package com.example.fitnesstracker.homescreen;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.ImageView;
import androidx.appcompat.app.AppCompatActivity;

// Import màn hình MainActivity để chuyển sang RunPrepFragment
import com.example.fitnesstracker.MainActivity;
import com.example.fitnesstracker.R;
import com.example.fitnesstracker.database.FitnessDatabase;
import com.example.fitnesstracker.database.WorkoutDao;
import com.example.fitnesstracker.database.WorkoutLog;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.DayOfWeek;
import java.time.temporal.TemporalAdjusters;

public class HomeScreenActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        // 1. Ánh xạ các nút bấm trên thanh điều hướng (Bottom Nav)
        ImageView navHome = findViewById(R.id.navHome);
        ImageView navRun = findViewById(R.id.navRun);
        ImageView navWorkout = findViewById(R.id.navWorkout);
        ImageView navMeals = findViewById(R.id.navMeals);
        ImageView navProfile = findViewById(R.id.navProfile);
        ImageView ivAvatar = findViewById(R.id.ivAvatar);

        // 2. XỬ LÝ SỰ KIỆN CLICK NÚT CHẠY (RUN) - CHUYỂN SANG MÀN HÌNH NEON
        navRun.setOnClickListener(v -> {
            // Chuyển từ Home sang MainActivity (Nơi chứa RunPrepFragment)
            Intent intent = new Intent(HomeScreenActivity.this, MainActivity.class);
            startActivity(intent);
        });

        // 3. Các sự kiện click khác (Tạm thời để trống hoặc cuộn trang)
        navHome.setOnClickListener(v -> {
            // Đang ở Home rồi
        });

        ivAvatar.setOnClickListener(v -> {
            // Code mở ProfileActivity sau này
        });

        // 4. Khởi tạo dữ liệu mẫu và vẽ biểu đồ tuần
        seedDatabaseAndLoadChart();
    }

    /**
     * Phương thức khởi tạo dữ liệu ảo và tải biểu đồ
     */
    private void seedDatabaseAndLoadChart() {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.execute(() -> {
            FitnessDatabase db = FitnessDatabase.getInstance(this);
            WorkoutDao dao = db.workoutDao();

            // Lấy ngày Thứ Hai của tuần hiện tại
            LocalDate today = LocalDate.now();
            LocalDate monday = today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

            // Tạo dữ liệu mẫu nếu Database đang trống
            String dateMon = monday.format(formatter);
            if (dao.getWorkoutCountForDate(dateMon) == 0) {
                dao.insertWorkout(new WorkoutLog(dateMon, "RUN", 45));
                dao.insertWorkout(new WorkoutLog(monday.plusDays(2).format(formatter), "LIFT", 60));
                dao.insertWorkout(new WorkoutLog(monday.plusDays(4).format(formatter), "YOGA", 30));
                dao.insertWorkout(new WorkoutLog(monday.plusDays(5).format(formatter), "SWIM", 30));
            }

            // Lấy dữ liệu 7 ngày để vẽ biểu đồ
            int[] weekData = new int[7];
            int maxCount = 1;
            for (int i = 0; i < 7; i++) {
                String dateStr = monday.plusDays(i).format(formatter);
                weekData[i] = dao.getWorkoutCountForDate(dateStr);
                if (weekData[i] > maxCount) maxCount = weekData[i];
            }

            final int chartMax = maxCount;

            // Cập nhật giao diện biểu đồ trên luồng chính (UI Thread)
            runOnUiThread(() -> {
                updateAllBars(weekData, chartMax);
            });
        });
    }

    private void updateAllBars(int[] weekData, int chartMax) {
        String[] labels = {"MON", "TUE", "WED", "THU", "FRI", "SAT", "SUN"};
        int[] barLayoutIds = {R.id.barMon, R.id.barTue, R.id.barWed, R.id.barThu, R.id.barFri, R.id.barSat, R.id.barSun};

        for (int i = 0; i < 7; i++) {
            setBarData(findViewById(barLayoutIds[i]), labels[i], weekData[i], chartMax);
        }
    }

    private void setBarData(View barLayout, String dayLabel, int workoutCount, int chartScaleMax) {
        if (barLayout == null) return;

        TextView tvDay = barLayout.findViewById(R.id.tvDayLabel);
        tvDay.setText(dayLabel);

        // Tính toán tỷ lệ phần trăm để co giãn thanh biểu đồ (weight)
        int percentage = (int) (((float) workoutCount / chartScaleMax) * 100);
        if (percentage > 100) percentage = 100;
        if (workoutCount > 0 && percentage < 10) percentage = 15; // Đảm bảo cột vẫn hiện nếu có dữ liệu nhỏ

        View viewSpacer = barLayout.findViewById(R.id.viewSpacer);
        View viewFill = barLayout.findViewById(R.id.viewFill);

        // Điều chỉnh độ cao bằng cách thay đổi weight của LinearLayout
        LinearLayout.LayoutParams spacerParams = (LinearLayout.LayoutParams) viewSpacer.getLayoutParams();
        spacerParams.weight = 100 - percentage;
        viewSpacer.setLayoutParams(spacerParams);

        LinearLayout.LayoutParams fillParams = (LinearLayout.LayoutParams) viewFill.getLayoutParams();
        fillParams.weight = percentage;
        viewFill.setLayoutParams(fillParams);
    }
}
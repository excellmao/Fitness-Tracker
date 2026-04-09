package com.example.fitnesstracker.run;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.util.Log;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.example.fitnesstracker.R;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class ActiveRunActivity extends AppCompatActivity implements OnMapReadyCallback {

    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1001;
    private static final int NEON_YELLOW = 0xFFCCFF00;

    // UI Views
    private TextView tvDuration, tvDistance, tvCalories, tvAvgPace, tvGpsStatus;
    private FrameLayout btnPauseResume;
    private ImageView ivPauseResumeIcon;
    private Button btnStop;

    // Map & Location
    private GoogleMap googleMap;
    private FusedLocationProviderClient fusedLocationClient;
    private LocationCallback locationCallback;
    private LocationRequest locationRequest;
    private final List<LatLng> routePoints = new ArrayList<>();
    private Polyline routePolyline;

    // State
    private boolean isRunning = false;
    private boolean isPaused  = false;
    private float totalDistanceMeters = 0f;
    private Location lastLocation = null;

    // Timer
    private final Handler timerHandler = new Handler();
    private long startTimeMillis = 0L;
    private long elapsedMillis   = 0L;

    private final Runnable timerRunnable = new Runnable() {
        @Override
        public void run() {
            if (isRunning && !isPaused) {
                elapsedMillis = SystemClock.elapsedRealtime() - startTimeMillis;
                updateTimerUI(elapsedMillis);
                timerHandler.postDelayed(this, 1000);
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_active_run);

        initViews();
        setupMap();
        setupLocationServices();

        btnPauseResume.setOnClickListener(v -> togglePauseResume());
        btnStop.setOnClickListener(v -> stopRun());
    }

    private void initViews() {
        tvDuration    = findViewById(R.id.tvDuration);
        tvDistance    = findViewById(R.id.tvDistance);
        tvCalories    = findViewById(R.id.tvCalories);
        tvAvgPace     = findViewById(R.id.tvAvgPace);
        tvGpsStatus   = findViewById(R.id.tvGpsStatus);
        btnPauseResume = findViewById(R.id.btnPauseResume);
        ivPauseResumeIcon = findViewById(R.id.ivPauseResumeIcon);
        btnStop        = findViewById(R.id.btnStop);
    }

    private void setupMap() {
        SupportMapFragment mapFragment = (SupportMapFragment)
                getSupportFragmentManager().findFragmentById(R.id.mapFragment);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }
    }

    @Override
    public void onMapReady(@NonNull GoogleMap map) {
        googleMap = map;
        try {
            boolean success = googleMap.setMapStyle(
                    MapStyleOptions.loadRawResourceStyle(this, R.raw.map_style_dark));
            if (!success) Log.e("MAP", "Style parsing failed.");
        } catch (Exception e) {
            e.printStackTrace();
        }

        googleMap.getUiSettings().setZoomControlsEnabled(false);
        checkLocationPermissionAndStart();
    }

    private void setupLocationServices() {
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        locationRequest = new LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 3000)
                .setMinUpdateIntervalMillis(2000)
                .build();

        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(@NonNull LocationResult result) {
                if (!isRunning || isPaused) return;
                Location location = result.getLastLocation();
                if (location == null) return;

                LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
                routePoints.add(latLng);

                if (lastLocation != null) {
                    totalDistanceMeters += lastLocation.distanceTo(location);
                    updateMetricsUI();
                }
                lastLocation = location;

                if (routePolyline != null) {
                    routePolyline.setPoints(routePoints);
                } else {
                    routePolyline = googleMap.addPolyline(new PolylineOptions()
                            .addAll(routePoints).color(NEON_YELLOW).width(12f));
                }
                googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 17f));
                tvGpsStatus.setText("GPS ACTIVE");
            }
        };
    }

    private void togglePauseResume() {
        if (!isRunning) return;

        if (!isPaused) {
            isPaused = true;
            elapsedMillis = SystemClock.elapsedRealtime() - startTimeMillis;
            timerHandler.removeCallbacks(timerRunnable);
            fusedLocationClient.removeLocationUpdates(locationCallback);
            ivPauseResumeIcon.setImageResource(android.R.drawable.ic_media_play);
        } else {
            isPaused = false;
            startTimeMillis = SystemClock.elapsedRealtime() - elapsedMillis;
            timerHandler.post(timerRunnable);
            startLocationUpdates();
            ivPauseResumeIcon.setImageResource(android.R.drawable.ic_media_pause);
        }
    }

    private void startRun() {
        isRunning = true;
        startTimeMillis = SystemClock.elapsedRealtime();
        timerHandler.post(timerRunnable);
        startLocationUpdates();
    }

    private void startLocationUpdates() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, null);
            googleMap.setMyLocationEnabled(true);
        }
    }

    /**
     * CẬP NHẬT: Thay vì finish(), hàm này sẽ chuyển sang RunSummaryFragment
     */
    private void stopRun() {
        // 1. Dừng mọi trạng thái chạy và GPS
        isRunning = false;
        timerHandler.removeCallbacks(timerRunnable);
        if (fusedLocationClient != null) {
            fusedLocationClient.removeLocationUpdates(locationCallback);
        }

        // 2. Thu thập dữ liệu cuối cùng từ UI
        String finalDuration = tvDuration.getText().toString();
        String finalDistance = tvDistance.getText().toString();
        String finalCalories = tvCalories.getText().toString();
        String finalPace     = tvAvgPace.getText().toString();

        // 3. Khởi tạo Fragment tổng kết
        RunSummaryFragment summaryFragment = new RunSummaryFragment();

        // 4. Đóng gói dữ liệu để gửi sang Fragment
        Bundle args = new Bundle();
        args.putString("duration_key", finalDuration);
        args.putString("distance_key", finalDistance);
        args.putString("calories_key", finalCalories);
        args.putString("pace_key", finalPace);
        summaryFragment.setArguments(args);

        // 5. Thực hiện chuyển đổi màn hình (đè lên toàn bộ Activity hiện tại)
        getSupportFragmentManager().beginTransaction()
                .setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out)
                .replace(android.R.id.content, summaryFragment)
                .addToBackStack(null) // Cho phép ấn back để xem lại map nếu cần
                .commit();
    }

    private void updateTimerUI(long millis) {
        int sec = (int) (millis / 1000) % 60;
        int min = (int) (millis / (1000 * 60)) % 60;
        int hrs = (int) (millis / (1000 * 60 * 60));
        tvDuration.setText(String.format(Locale.getDefault(), "%02d:%02d:%02d", hrs, min, sec));
    }

    private void updateMetricsUI() {
        float km = totalDistanceMeters / 1000f;
        tvDistance.setText(String.format(Locale.getDefault(), "%.2f km", km));
        tvCalories.setText(String.format(Locale.getDefault(), "%d kcal", (int)(km * 60)));

        if (km > 0.01f) {
            float paceSecs = (elapsedMillis / 1000f) / km;
            tvAvgPace.setText(String.format(Locale.getDefault(), "%d'%02d\"/km", (int)paceSecs/60, (int)paceSecs%60));
        }
    }

    private void checkLocationPermissionAndStart() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_PERMISSION_REQUEST_CODE);
        } else {
            startRun();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        timerHandler.removeCallbacks(timerRunnable);
        if (fusedLocationClient != null) fusedLocationClient.removeLocationUpdates(locationCallback);
    }
}
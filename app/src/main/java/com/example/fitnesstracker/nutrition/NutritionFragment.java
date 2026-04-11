package com.example.fitnesstracker.nutrition;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.core.app.NotificationCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.fitnesstracker.R;
import com.example.fitnesstracker.database.FitnessDatabase;
import com.example.fitnesstracker.database.FoodLog;
import com.example.fitnesstracker.database.WaterLog;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class NutritionFragment extends Fragment {

    private TextView tvCaloriesLeft, tvCaloriesIn, tvCaloriesOut, tvWaterCount;
    private ProgressBar pbCalories, pbWater;
    private RecyclerView rvFoodLogs;
    private Button btnLogWater;
    private TextView tvViewAll;
    private LinearLayout llWaterDrops;
    private CardView cvWaterAlert;
    
    private String todayDate;
    private ExecutorService executor = Executors.newSingleThreadExecutor();
    private FoodAdapter adapter;

    private static final String CHANNEL_ID = "nutrition_channel";
    private int totalIn = 0;
    private int totalOut = 0;
    private int currentGlasses = 0;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_nutrition, container, false);
        
        tvCaloriesLeft = view.findViewById(R.id.tvCaloriesLeft);
        tvCaloriesIn = view.findViewById(R.id.tvCaloriesIn);
        tvCaloriesOut = view.findViewById(R.id.tvCaloriesOut);
        tvWaterCount = view.findViewById(R.id.tvWaterCount);
        pbCalories = view.findViewById(R.id.pbCalories);
        pbWater = view.findViewById(R.id.pbWater);
        rvFoodLogs = view.findViewById(R.id.rvFoodLogs);
        btnLogWater = view.findViewById(R.id.btnLogWater);
        tvViewAll = view.findViewById(R.id.tvViewAllFood);
        llWaterDrops = view.findViewById(R.id.llWaterDrops);
        cvWaterAlert = view.findViewById(R.id.cvWaterAlert);

        todayDate = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));

        createNotificationChannel();
        setupRecyclerView();
        observeData();

        btnLogWater.setOnClickListener(v -> {
            if (currentGlasses < 8) {
                logWater(250);
            } else {
                Toast.makeText(requireContext(), "You've reached your daily goal of 8 glasses!", Toast.LENGTH_SHORT).show();
            }
        });

        tvViewAll.setOnClickListener(v -> {
            Intent intent = new Intent(requireContext(), LogFoodActivity.class);
            startActivity(intent);
        });

        return view;
    }

    private void setupRecyclerView() {
        adapter = new FoodAdapter(new ArrayList<>());
        rvFoodLogs.setLayoutManager(new LinearLayoutManager(requireContext()));
        rvFoodLogs.setAdapter(adapter);
    }

    private void observeData() {
        FitnessDatabase db = FitnessDatabase.getInstance(requireContext());
        
        db.nutritionDao().getFoodLogsByDate(todayDate).observe(getViewLifecycleOwner(), logs -> {
            if (logs != null && !logs.isEmpty()) {
                List<FoodLog> displayList = logs.size() > 2 ? logs.subList(logs.size() - 2, logs.size()) : logs;
                adapter.setLogs(displayList);
            } else {
                adapter.setLogs(new ArrayList<>());
            }
        });

        db.nutritionDao().getTotalCaloriesByDate(todayDate).observe(getViewLifecycleOwner(), total -> {
            totalIn = (total != null) ? total : 0;
            updateCalorieUI();
        });

        db.nutritionDao().getBurnedCaloriesByDate(todayDate).observe(getViewLifecycleOwner(), total -> {
            totalOut = (total != null) ? total : 0;
            updateCalorieUI();
        });

        db.nutritionDao().getTotalWaterByDate(todayDate).observe(getViewLifecycleOwner(), total -> {
            int totalWater = (total != null) ? total : 0;
            currentGlasses = totalWater / 250;
            tvWaterCount.setText(String.format(Locale.getDefault(), "%d/8", currentGlasses));
            pbWater.setProgress(currentGlasses);
            updateWaterDropsUI(currentGlasses);
        });
    }

    private void updateCalorieUI() {
        int left = totalIn - totalOut;
        
        tvCaloriesIn.setText(String.format(Locale.getDefault(), "%,d", totalIn));
        tvCaloriesOut.setText(String.format(Locale.getDefault(), "%,d", totalOut));
        tvCaloriesLeft.setText(String.valueOf(left));
        
        if (totalIn > 0) {
            pbCalories.setMax(totalIn);
            pbCalories.setProgress(Math.max(0, left));
        } else {
            pbCalories.setMax(100);
            pbCalories.setProgress(0);
        }
        
        showCalorieNotification(totalIn);
    }

    private void updateWaterDropsUI(int glasses) {
        int orangeColor = getResources().getColor(R.color.quick_action_meal);
        int grayColor = getResources().getColor(R.color.text_gray);

        for (int i = 0; i < llWaterDrops.getChildCount(); i++) {
            View child = llWaterDrops.getChildAt(i);
            if (child instanceof ImageView) {
                ImageView drop = (ImageView) child;
                if (i < glasses) {
                    drop.setImageTintList(ColorStateList.valueOf(orangeColor));
                    drop.setAlpha(1.0f);
                } else {
                    drop.setImageTintList(ColorStateList.valueOf(grayColor));
                    drop.setAlpha(0.3f);
                }
            }
        }

        // Ẩn thông báo nếu đã uống đủ 8 cốc
        if (glasses >= 8) {
            cvWaterAlert.setVisibility(View.GONE);
        } else {
            cvWaterAlert.setVisibility(View.VISIBLE);
        }
    }

    private void logWater(int amount) {
        executor.execute(() -> {
            FitnessDatabase.getInstance(requireContext()).nutritionDao().insertWater(new WaterLog(amount, todayDate));
            showWaterReminder();
        });
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "Nutrition Tracker";
            String description = "Notifications for calories and hydration";
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);
            NotificationManager notificationManager = requireContext().getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }

    private void showCalorieNotification(int total) {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(requireContext(), CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_meal)
                .setContentTitle("Daily Calorie Intake")
                .setContentText("Total calories today: " + total + " kcal")
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setOngoing(true);

        NotificationManager notificationManager = (NotificationManager) requireContext().getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(1, builder.build());
    }

    private void showWaterReminder() {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(requireContext(), CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_water)
                .setContentTitle("Stay Hydrated!")
                .setContentText("Don't forget to drink water regularly.")
                .setPriority(NotificationCompat.PRIORITY_DEFAULT);

        NotificationManager notificationManager = (NotificationManager) requireContext().getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(2, builder.build());
    }

    class FoodAdapter extends RecyclerView.Adapter<FoodAdapter.FoodViewHolder> {
        private List<FoodLog> logs;

        public FoodAdapter(List<FoodLog> logs) {
            this.logs = logs;
        }

        public void setLogs(List<FoodLog> logs) {
            this.logs = logs;
            notifyDataSetChanged();
        }

        @NonNull
        @Override
        public FoodViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_food, parent, false);
            return new FoodViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull FoodViewHolder holder, int position) {
            FoodLog log = logs.get(position);
            holder.tvName.setText(log.name);
            holder.tvDetails.setText(String.format(Locale.getDefault(), "%d kcal • %s", log.calories, log.mealType));
            holder.tvQuantity.setText(String.valueOf(log.quantity));

            holder.btnAdd.setOnClickListener(v -> updateQuantity(log, 1));
            holder.btnMinus.setOnClickListener(v -> {
                if (log.quantity > 1) {
                    updateQuantity(log, -1);
                } else {
                    deleteFood(log);
                }
            });
        }

        @Override
        public int getItemCount() {
            return logs.size();
        }

        private void updateQuantity(FoodLog log, int delta) {
            log.quantity += delta;
            executor.execute(() -> FitnessDatabase.getInstance(requireContext()).nutritionDao().updateFood(log));
        }

        private void deleteFood(FoodLog log) {
            executor.execute(() -> FitnessDatabase.getInstance(requireContext()).nutritionDao().deleteFood(log));
        }

        class FoodViewHolder extends RecyclerView.ViewHolder {
            TextView tvName, tvDetails, tvQuantity;
            ImageButton btnAdd, btnMinus;

            public FoodViewHolder(@NonNull View itemView) {
                super(itemView);
                tvName = itemView.findViewById(R.id.tvFoodName);
                tvDetails = itemView.findViewById(R.id.tvFoodDetails);
                tvQuantity = itemView.findViewById(R.id.tvQuantity);
                btnAdd = itemView.findViewById(R.id.btnAdd);
                btnMinus = itemView.findViewById(R.id.btnMinus);
            }
        }
    }
}
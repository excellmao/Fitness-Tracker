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

    private boolean isShowingAllLogs = false;
    private List<FoodLog> allCurrentLogs = new ArrayList<>();
    private TextView tvDailyKcal, tvDailyPercent;
    private ProgressBar pbDailyCalories;
    private int dailyGoal = 2500;
    private TextView tvDailyGoal;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_nutrition, container, false);

        tvDailyGoal = view.findViewById(R.id.tvDailyGoal);

        // Load the saved goal from SharedPreferences (Defaults to 2500 if not set)
        android.content.SharedPreferences prefs = requireContext().getSharedPreferences("FitnessPrefs", Context.MODE_PRIVATE);
        dailyGoal = prefs.getInt("daily_calorie_goal", 2500);
        tvDailyGoal.setText(String.format(Locale.getDefault(), "%,d goal ✎", dailyGoal));

        // Listen for clicks on the goal text
        tvDailyGoal.setOnClickListener(v -> showEditGoalDialog());

        tvWaterCount = view.findViewById(R.id.tvWaterCount);
        pbWater = view.findViewById(R.id.pbWater);
        rvFoodLogs = view.findViewById(R.id.rvFoodLogs);
        btnLogWater = view.findViewById(R.id.btnLogWater);
        tvViewAll = view.findViewById(R.id.tvViewAllFood);
        llWaterDrops = view.findViewById(R.id.llWaterDrops);
        LinearLayout btnAddFood = view.findViewById(R.id.btnAddFood);
        tvDailyKcal = view.findViewById(R.id.tvDailyKcal);
        tvDailyPercent = view.findViewById(R.id.tvDailyPercent);
        pbDailyCalories = view.findViewById(R.id.pbDailyCalories);

        todayDate = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
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
            isShowingAllLogs = !isShowingAllLogs;
            updateFoodListUI(); // We will create this method below!
        });

        // btnAddFood still goes to LogFoodActivity!
        btnAddFood.setOnClickListener(v -> {
            Intent intent = new Intent(requireContext(), LogFoodActivity.class);
            startActivity(intent);
        });

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (requireContext().checkSelfPermission(android.Manifest.permission.POST_NOTIFICATIONS) != android.content.pm.PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{android.Manifest.permission.POST_NOTIFICATIONS}, 101);
            }
        }

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
            allCurrentLogs = (logs != null) ? logs : new ArrayList<>();
            updateFoodListUI();
        });

        db.nutritionDao().getTotalCaloriesByDate(todayDate).observe(getViewLifecycleOwner(), total -> {
            totalIn = (total != null) ? total : 0;

            tvDailyKcal.setText(String.format(Locale.getDefault(), "%,d", totalIn));

            // Update Max AND Progress
            pbDailyCalories.setMax(dailyGoal);
            pbDailyCalories.setProgress(totalIn);

            int percent = (int) (((float) totalIn / dailyGoal) * 100);
            tvDailyPercent.setText(percent + "% of daily goal");
        });

        db.nutritionDao().getTotalWaterByDate(todayDate).observe(getViewLifecycleOwner(), total -> {
            int totalWater = (total != null) ? total : 0;
            currentGlasses = totalWater / 250;
            tvWaterCount.setText(String.valueOf(currentGlasses));
            pbWater.setProgress(currentGlasses);
            updateWaterDropsUI(currentGlasses);
            WaterLogReceiver.updateNotification(requireContext(), currentGlasses);
        });
    }


    private void updateWaterDropsUI(int glasses) {
        int blueColor = android.graphics.Color.parseColor("#29B6F6");
        int grayColor = getResources().getColor(R.color.text_gray, null);

        for (int i = 0; i < llWaterDrops.getChildCount(); i++) {
            View child = llWaterDrops.getChildAt(i);
            if (child instanceof ImageView) {
                ImageView drop = (ImageView) child;
                if (i < glasses) {
                    drop.setImageTintList(ColorStateList.valueOf(blueColor)); // Apply the blue!
                    drop.setAlpha(1.0f);
                } else {
                    drop.setImageTintList(ColorStateList.valueOf(grayColor));
                    drop.setAlpha(0.3f);
                }
            }
        }
    }

    private void logWater(int amount) {
        executor.execute(() -> {
            FitnessDatabase.getInstance(requireContext()).nutritionDao().insertWater(new WaterLog(amount, todayDate));
        });
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

            setFoodIcon(holder.ivFoodImage, log.name);
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
            ImageView ivFoodImage;

            public FoodViewHolder(@NonNull View itemView) {
                super(itemView);
                tvName = itemView.findViewById(R.id.tvFoodName);
                tvDetails = itemView.findViewById(R.id.tvFoodDetails);
                tvQuantity = itemView.findViewById(R.id.tvQuantity);
                btnAdd = itemView.findViewById(R.id.btnAdd);
                btnMinus = itemView.findViewById(R.id.btnMinus);

                ivFoodImage = itemView.findViewById(R.id.ivFoodImage);
            }
        }
    }

    private void updateFoodListUI() {
        if (allCurrentLogs.isEmpty()) {
            adapter.setLogs(new ArrayList<>());
            tvViewAll.setVisibility(View.GONE);
            return;
        }

        tvViewAll.setVisibility(View.VISIBLE);

        if (isShowingAllLogs) {
            adapter.setLogs(allCurrentLogs);
            tvViewAll.setText("SHOW LESS");
        } else {
            // Show only the latest 2 items
            List<FoodLog> displayList = allCurrentLogs.size() > 2
                    ? allCurrentLogs.subList(allCurrentLogs.size() - 2, allCurrentLogs.size())
                    : allCurrentLogs;
            adapter.setLogs(displayList);
            tvViewAll.setText("VIEW ALL");
        }
    }

    private void setFoodIcon(ImageView imageView, String name) {
        // Compare the food name and set the matching picture
        switch (name.toLowerCase()) {
            case "protein bowl":
                imageView.setImageResource(R.drawable.img_protein_bowl);
                break;
            case "avocado toast":
                imageView.setImageResource(R.drawable.img_toast);
                break;
            case "grilled salmon":
                imageView.setImageResource(R.drawable.img_salmon);
                break;
            case "greek yogurt":
                imageView.setImageResource(R.drawable.img_yogurt);
                break;
            case "garden salad":
                imageView.setImageResource(R.drawable.img_salad);
                break;
            case "chicken breast":
                imageView.setImageResource(R.drawable.img_chicken);
                break;
            case "oatmeal":
                imageView.setImageResource(R.drawable.img_oatmeal);
                break;
            case "banana":
                imageView.setImageResource(R.drawable.img_banana);
                break;
            case "beef steak":
                imageView.setImageResource(R.drawable.img_steak);
                break;
            case "scrambled eggs":
                imageView.setImageResource(R.drawable.img_eggs);
                break;
            default:
                imageView.setImageResource(R.drawable.ic_meal);
                break;
        }
    }

    private void showEditGoalDialog() {
        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(requireContext());
        builder.setTitle("Set Daily Calorie Goal");

        // Create an input field
        final android.widget.EditText input = new android.widget.EditText(requireContext());
        input.setInputType(android.text.InputType.TYPE_CLASS_NUMBER);
        input.setText(String.valueOf(dailyGoal));
        builder.setView(input);

        // Setup the Save button
        builder.setPositiveButton("Save", (dialog, which) -> {
            String newGoalStr = input.getText().toString();
            if (!newGoalStr.isEmpty()) {
                dailyGoal = Integer.parseInt(newGoalStr);

                // Save it permanently to SharedPreferences
                requireContext().getSharedPreferences("FitnessPrefs", Context.MODE_PRIVATE)
                        .edit()
                        .putInt("daily_calorie_goal", dailyGoal)
                        .apply();

                // Update the UI text
                tvDailyGoal.setText(String.format(Locale.getDefault(), "%,d goal ✎", dailyGoal));

                // Force the math to recalculate
                pbDailyCalories.setMax(dailyGoal);
                int percent = (int) (((float) totalIn / dailyGoal) * 100);
                tvDailyPercent.setText(percent + "% of daily goal");
            }
        });

        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());
        builder.show();
    }
}
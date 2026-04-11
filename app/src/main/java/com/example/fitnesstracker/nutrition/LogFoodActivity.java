package com.example.fitnesstracker.nutrition;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.fitnesstracker.R;
import com.example.fitnesstracker.database.FitnessDatabase;
import com.example.fitnesstracker.database.FoodLog;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class LogFoodActivity extends AppCompatActivity {

    private TextView tvTotalCalories, tvPercentGoal, tvDate;
    private ProgressBar pbDailyGoal;
    private RecyclerView rvFoodLogsDetail;
    private Button btnDone;
    private ImageButton btnBack;

    private String todayDate;
    private ExecutorService executor = Executors.newSingleThreadExecutor();
    private FoodDetailAdapter adapter;
    private List<FoodLog> localFoodList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_log_food);

        tvTotalCalories = findViewById(R.id.tvTotalCalories);
        tvPercentGoal = findViewById(R.id.tvPercentGoal);
        tvDate = findViewById(R.id.tvDate);
        pbDailyGoal = findViewById(R.id.pbDailyGoal);
        rvFoodLogsDetail = findViewById(R.id.rvFoodLogsDetail);
        btnDone = findViewById(R.id.btnDone);
        btnBack = findViewById(R.id.btnBack);

        todayDate = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        tvDate.setText(LocalDate.now().format(DateTimeFormatter.ofPattern("MMMM dd")));

        setupRecyclerView();
        loadFoodData();

        // Nút Back quay lại màn hình trước đó
        btnBack.setOnClickListener(v -> finish());
        btnDone.setOnClickListener(v -> saveLogsAndFinish());
    }

    private void setupRecyclerView() {
        adapter = new FoodDetailAdapter(localFoodList);
        rvFoodLogsDetail.setLayoutManager(new LinearLayoutManager(this));
        rvFoodLogsDetail.setAdapter(adapter);
    }

    private void loadFoodData() {
        // Danh sách gốc cố định
        List<FoodLog> masterList = new ArrayList<>();
        masterList.add(new FoodLog("Protein Bowl", 520, "Lunch", 0, todayDate));
        masterList.add(new FoodLog("Avocado Toast", 340, "Breakfast", 0, todayDate));
        masterList.add(new FoodLog("Greek Yogurt", 150, "Snack", 0, todayDate));
        masterList.add(new FoodLog("Grilled Salmon", 410, "Dinner", 0, todayDate));
        masterList.add(new FoodLog("Garden Salad", 200, "Lunch", 0, todayDate));
        masterList.add(new FoodLog("Chicken Breast", 165, "Dinner", 0, todayDate));
        masterList.add(new FoodLog("Oatmeal", 150, "Breakfast", 0, todayDate));
        masterList.add(new FoodLog("Banana", 105, "Snack", 0, todayDate));
        masterList.add(new FoodLog("Beef Steak", 600, "Dinner", 0, todayDate));
        masterList.add(new FoodLog("Scrambled Eggs", 140, "Breakfast", 0, todayDate));

        executor.execute(() -> {
            FitnessDatabase db = FitnessDatabase.getInstance(this);
            // Lấy dữ liệu thực tế từ DB một lần duy nhất
            List<FoodLog> savedLogs = db.nutritionDao().getFoodLogsByDateSync(todayDate);
            
            runOnUiThread(() -> {
                localFoodList.clear();
                for (FoodLog master : masterList) {
                    FoodLog found = null;
                    if (savedLogs != null) {
                        for (FoodLog saved : savedLogs) {
                            if (saved.name.equals(master.name)) {
                                found = saved;
                                break;
                            }
                        }
                    }
                    if (found != null) {
                        localFoodList.add(found);
                    } else {
                        localFoodList.add(master);
                    }
                }
                adapter.notifyDataSetChanged();
                updateCaloriesUI();
            });
        });
    }

    private void updateCaloriesUI() {
        int total = 0;
        for (FoodLog item : localFoodList) {
            total += (item.calories * item.quantity);
        }
        
        tvTotalCalories.setText(String.format(Locale.getDefault(), "%,d", total));
        pbDailyGoal.setProgress(total);
        
        int percent = (int) ((total / 2500.0) * 100);
        tvPercentGoal.setText(String.format(Locale.getDefault(), "%d%% of daily goal", percent));
    }

    private void saveLogsAndFinish() {
        executor.execute(() -> {
            FitnessDatabase db = FitnessDatabase.getInstance(this);
            for (FoodLog log : localFoodList) {
                if (log.id != 0) {
                    db.nutritionDao().updateFood(log);
                } else if (log.quantity > 0) {
                    db.nutritionDao().insertFood(log);
                }
            }
            runOnUiThread(() -> {
                Toast.makeText(this, "Logs updated successfully!", Toast.LENGTH_SHORT).show();
                finish();
            });
        });
    }

    class FoodDetailAdapter extends RecyclerView.Adapter<FoodDetailAdapter.FoodViewHolder> {
        private List<FoodLog> logs;

        public FoodDetailAdapter(List<FoodLog> logs) {
            this.logs = logs;
        }

        @NonNull
        @Override
        public FoodViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_food_detail, parent, false);
            return new FoodViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull FoodViewHolder holder, int position) {
            FoodLog log = logs.get(position);
            holder.tvName.setText(log.name);
            holder.tvMealType.setText(log.mealType.toUpperCase());
            holder.tvKcalPerUnit.setText(String.format(Locale.getDefault(), "%d kcal per unit", log.calories));
            holder.tvQuantity.setText(String.valueOf(log.quantity));

            setFoodIcon(holder.ivFoodImage, log.name);

            holder.btnAdd.setOnClickListener(v -> {
                log.quantity++;
                notifyItemChanged(position);
                updateCaloriesUI();
            });

            holder.btnMinus.setOnClickListener(v -> {
                if (log.quantity > 0) {
                    log.quantity--;
                    notifyItemChanged(position);
                    updateCaloriesUI();
                }
            });
        }

        private void setFoodIcon(ImageView imageView, String name) {
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

        @Override
        public int getItemCount() {
            return logs.size();
        }

        class FoodViewHolder extends RecyclerView.ViewHolder {
            TextView tvName, tvMealType, tvKcalPerUnit, tvQuantity;
            ImageButton btnAdd, btnMinus;
            ImageView ivFoodImage;

            public FoodViewHolder(@NonNull View itemView) {
                super(itemView);
                tvName = itemView.findViewById(R.id.tvFoodName);
                tvMealType = itemView.findViewById(R.id.tvMealType);
                tvKcalPerUnit = itemView.findViewById(R.id.tvKcalPerUnit);
                tvQuantity = itemView.findViewById(R.id.tvQuantity);
                btnAdd = itemView.findViewById(R.id.btnAdd);
                btnMinus = itemView.findViewById(R.id.btnMinus);
                ivFoodImage = itemView.findViewById(R.id.ivFoodImage);
            }
        }
    }
}
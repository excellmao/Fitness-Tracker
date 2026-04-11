package com.example.fitnesstracker.nutrition;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.os.Build;
import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.example.fitnesstracker.R;

public class WaterReminderWorker extends Worker {

    private static final String CHANNEL_ID = "nutrition_channel";

    public WaterReminderWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    @NonNull
    @Override
    public Result doWork() {
        // 1. Ensure the channel exists (required for Android 8+)
        createNotificationChannel();

        // 2. Build the Notification
        NotificationCompat.Builder builder = new NotificationCompat.Builder(getApplicationContext(), CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_water) // Make sure you have this icon!
                .setContentTitle("Hydration Check! 💧")
                .setContentText("It's been a while. Time to drink a glass of water to hit your daily goal!")
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setAutoCancel(true);

        // 3. Fire the Notification
        NotificationManager manager = (NotificationManager) getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);
        if (manager != null) {
            // Using a random ID so they don't overwrite each other if multiple fire
            manager.notify((int) System.currentTimeMillis(), builder.build());
        }

        return Result.success();
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    "Nutrition Tracker",
                    NotificationManager.IMPORTANCE_DEFAULT
            );
            channel.setDescription("Notifications for hydration reminders");
            NotificationManager manager = getApplicationContext().getSystemService(NotificationManager.class);
            if (manager != null) {
                manager.createNotificationChannel(channel);
            }
        }
    }
}
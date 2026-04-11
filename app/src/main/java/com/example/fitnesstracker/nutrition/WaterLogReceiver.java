package com.example.fitnesstracker.nutrition;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.RemoteViews;
import androidx.core.app.NotificationCompat;
import com.example.fitnesstracker.R;
import com.example.fitnesstracker.database.FitnessDatabase;
import com.example.fitnesstracker.database.WaterLog;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.Executors;

public class WaterLogReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        if ("ADD_WATER".equals(intent.getAction())) {
            // Run database work on a background thread so it doesn't freeze!
            Executors.newSingleThreadExecutor().execute(() -> {
                FitnessDatabase db = FitnessDatabase.getInstance(context);
                String todayDate = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));

                // Add 250ml
                db.nutritionDao().insertWater(new WaterLog(250, todayDate));

                // Get the new total and update the notification
                Integer totalWater = db.nutritionDao().getTotalWaterByDateSync(todayDate);
                int glasses = (totalWater != null ? totalWater : 0) / 250;

                updateNotification(context, glasses);
            });
        }
    }

    // This method rebuilds the notification with the new number
    public static void updateNotification(Context context, int currentGlasses) {
        // 1. We must use a NEW ID to escape the old noisy channel!
        String SILENT_CHANNEL_ID = "water_tracker_silent";

        // 2. Create the Silent Channel (Android 8+)
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            android.app.NotificationChannel channel = new android.app.NotificationChannel(
                    SILENT_CHANNEL_ID,
                    "Silent Water Tracker",
                    android.app.NotificationManager.IMPORTANCE_LOW // <-- THE MAGIC FIX: No sound, no popup!
            );
            channel.setDescription("Ongoing silent widget for water tracking");
            android.app.NotificationManager manager = context.getSystemService(android.app.NotificationManager.class);
            if (manager != null) {
                manager.createNotificationChannel(channel);
            }
        }

        // 3. Build the Custom View
        RemoteViews remoteViews = new RemoteViews(context.getPackageName(), R.layout.notification_water);
        remoteViews.setTextViewText(R.id.tvNotifWaterCount, currentGlasses + "/8 Glasses");

        Intent addIntent = new Intent(context, WaterLogReceiver.class);
        addIntent.setAction("ADD_WATER");
        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                context, 0, addIntent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        remoteViews.setOnClickPendingIntent(R.id.btnNotifAddWater, pendingIntent);

        // 4. Build the Notification using the SILENT channel
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, SILENT_CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_water)
                .setCustomContentView(remoteViews)
                .setStyle(new NotificationCompat.DecoratedCustomViewStyle())
                .setOngoing(true) // Keeps it pinned
                .setOnlyAlertOnce(true); // Ensures it never buzzes on updates

        // 5. Fire it!
        android.app.NotificationManager manager = (android.app.NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        if (manager != null) {
            manager.notify(999, builder.build());
        }
    }
}
package com.example.fitnesstracker.run;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;
import android.widget.RemoteViews;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import com.example.fitnesstracker.R;

public class RunForegroundService extends Service {

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // If the activity tells us to stop, kill the service
        if (intent != null && "STOP_SERVICE".equals(intent.getAction())) {
            stopForeground(true);
            stopSelf();
            return START_NOT_STICKY;
        }

        // Grab the live data sent from the Activity
        String duration = intent != null ? intent.getStringExtra("duration") : "00:00:00";
        String metrics = intent != null ? intent.getStringExtra("metrics") : "0.00 km • 0'00\"/km";
        boolean isPaused = intent != null && intent.getBooleanExtra("isPaused", false);

        createNotificationChannel();

        // Build the Custom Layout
        RemoteViews remoteViews = new RemoteViews(getPackageName(), R.layout.notification_run);
        remoteViews.setTextViewText(R.id.tvNotifDuration, duration);
        remoteViews.setTextViewText(R.id.tvNotifMetrics, metrics);
        remoteViews.setImageViewResource(R.id.btnNotifToggle, isPaused ? android.R.drawable.ic_media_play : android.R.drawable.ic_media_pause);

        // Hook up the Pause/Play button broadcast
        Intent toggleIntent = new Intent("TOGGLE_RUN");
        toggleIntent.setPackage(getPackageName());
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, 0, toggleIntent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        remoteViews.setOnClickPendingIntent(R.id.btnNotifToggle, pendingIntent);

        // Build the Notification
        Notification notification = new NotificationCompat.Builder(this, "run_channel")
                .setSmallIcon(R.drawable.ic_run) // Make sure this icon exists!
                .setCustomContentView(remoteViews)
                .setStyle(new NotificationCompat.DecoratedCustomViewStyle())
                .setOngoing(true)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC) // Shows on lock screen!
                .setOnlyAlertOnce(true)
                .build();

        // MAGIC TRICK: This tells Android to keep your app alive in the background!
        startForeground(888, notification);
        return START_STICKY;
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel("run_channel", "Active Run", NotificationManager.IMPORTANCE_LOW);
            getSystemService(NotificationManager.class).createNotificationChannel(channel);
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) { return null; }
}
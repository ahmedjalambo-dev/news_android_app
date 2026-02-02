package com.example.newsapp.utils;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.os.Build;

import androidx.core.app.NotificationCompat;

public class NotificationHelper {

    private static final String CHANNEL_ID = "news_channel";
    private static final String CHANNEL_NAME = "News Updates";
    private static final int NOTIFICATION_ID = 1;

    /**
     * Shows a system notification.
     * Logic extracted from MainActivity to separate concerns.
     */
    public static void show(Context context, String title, String message) {
        NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    CHANNEL_NAME,
                    NotificationManager.IMPORTANCE_DEFAULT
            );
            manager.createNotificationChannel(channel);
        }

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(android.R.drawable.ic_dialog_info) // Kept original icon
                .setContentTitle(title)
                .setContentText(message)
                .setAutoCancel(true);

        manager.notify(NOTIFICATION_ID, builder.build());
    }
}
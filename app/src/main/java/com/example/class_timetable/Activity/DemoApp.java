package com.example.class_timetable.Activity;

import android.app.Application;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.os.Build;

public class DemoApp extends Application {
   // public static String CHANNEL_ID = "ChannelID";

    private static final int NOTIFICATION_ID = 0;

    private static final String PRIMARY_CHANNEL_ID =
            "primary_notification_channel";

    @Override
    public void onCreate() {
        super.onCreate();
        int importance = NotificationManager.IMPORTANCE_DEFAULT;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel notificationChannel = new NotificationChannel(PRIMARY_CHANNEL_ID,
                    "primary", NotificationManager.IMPORTANCE_HIGH);
            NotificationManager mNotificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
            if (mNotificationManager != null) {
                mNotificationManager.createNotificationChannel(notificationChannel);
            }
        }
    }
}

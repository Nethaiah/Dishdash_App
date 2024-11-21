package com.example.dishdash;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import androidx.core.app.NotificationCompat;

public class NotificationAlarmReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        String mealType = intent.getStringExtra("mealType");
        String mealTime = intent.getStringExtra("mealTime");
        sendNotification(context, mealType, mealTime);
    }

    private void sendNotification(Context context, String mealType, String mealTime) {
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        String channelId = "meal_notifications";

        // Create notification channel for Android O and above
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(channelId, "Meal Notifications", NotificationManager.IMPORTANCE_HIGH);
            notificationManager.createNotificationChannel(channel);
        }

        // Create an intent for the notification action
        Intent notificationIntent = new Intent(context, home.class);
        notificationIntent.putExtra("showMealPlanner", true); // Add an extra to indicate which fragment to show
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        // Build the notification
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, channelId)
                .setSmallIcon(R.drawable.dish_dash)
                .setContentTitle("Meal Reminder")
                .setContentText("It's time to prepare/eat your " + mealType + " at " + mealTime + "!")
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setContentIntent(pendingIntent)
                .setDefaults(NotificationCompat.DEFAULT_ALL)
                .setAutoCancel(true);

        // Send the notification
        notificationManager.notify((int) System.currentTimeMillis(), builder.build());
    }
}

package com.example.beta1;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;

public class NotificationReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        // Get the notification ID, title, and message from the intent
        int notificationId = intent.getIntExtra("notificationId", 0);
        String title = intent.getStringExtra("title");
        String message = intent.getStringExtra("message");

        // Create a new intent to launch the app's main activity
        Intent mainIntent = new Intent(context, Navi.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, mainIntent, 0);

        // Create a new notification
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        String channelId = "my_channel_id";
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(channelId, "My Channel", NotificationManager.IMPORTANCE_DEFAULT);
            channel.enableLights(true);
            channel.setLightColor(Color.BLUE);
            notificationManager.createNotificationChannel(channel);
        }

        Notification.Builder builder = null;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            builder = new Notification.Builder(context, channelId)
                    .setSmallIcon(R.drawable.noticon)
                    .setContentTitle(title)
                    .setContentText(message)
                    .setContentIntent(pendingIntent)
                    .setAutoCancel(true);
        }

        Notification notification = builder.build();

        // Display the notification
        notificationManager.notify(notificationId, notification);
    }
}


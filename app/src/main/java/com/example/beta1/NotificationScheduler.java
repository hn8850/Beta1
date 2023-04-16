package com.example.beta1;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

/**
 * @author Harel Navon harelnavon2710@gmail.com
 * @version 1.2
 * @since 14/4/2023
 * Used to schedule notifications regarding order updates for the user.
 */

public class NotificationScheduler {

    public static void scheduleNotification(Context context, String title, String message, String dateString, String timeString, int notificationId) {
        // Get the current time and date in the device's time zone
        Calendar calendar = Calendar.getInstance(TimeZone.getDefault());

        // Parse the date and time strings into a Date object in the device's time zone
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());
        dateFormat.setTimeZone(TimeZone.getDefault());
        timeFormat.setTimeZone(TimeZone.getDefault());
        Date date = null;
        Date time = null;
        try {
            date = dateFormat.parse(dateString);
            time = timeFormat.parse(timeString);
        } catch (ParseException e) {
            e.printStackTrace();
        }

        // Set the date and time of the notification in the device's time zone
        calendar.setTime(date);
        calendar.set(Calendar.HOUR_OF_DAY, time.getHours());
        calendar.set(Calendar.MINUTE, time.getMinutes());
        calendar.set(Calendar.SECOND, 0);

        // Check if the scheduled time has already passed
        if (calendar.getTimeInMillis() <= System.currentTimeMillis()) {
            // If the scheduled time has already passed, add one day to the calendar object
            calendar.add(Calendar.DAY_OF_MONTH, 1);
        }

        // Create a new intent for the notification
        Intent notificationIntent = new Intent(context, NotificationReceiver.class);
        notificationIntent.putExtra("notificationId", notificationId);
        notificationIntent.putExtra("title", title);
        notificationIntent.putExtra("message", message);

        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, notificationId, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        // Create the notification
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        String channelId = "my_channel_id";
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(channelId, "My Channel", NotificationManager.IMPORTANCE_DEFAULT);
            channel.enableLights(true);
            channel.setLightColor(Color.BLUE);
            notificationManager.createNotificationChannel(channel);
        }

        Notification.Builder builder = new Notification.Builder(context, channelId)
                .setSmallIcon(R.drawable.noticon)
                .setContentTitle(title)
                .setContentText(message)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)
                .setWhen(calendar.getTimeInMillis());

        Notification notification = builder.build();

        // Schedule the notification
        notificationManager.notify(notificationId, notification);
    }

}



package edu.cuhk.csci3310.cuhk_all_in_one;

import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.os.Build;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

public class TodoNotificationService extends IntentService {
    private static final String CHANNEL_ID = "edu.cuhk.csci3310.cuhk_all_in_one";

    private static final int NOTIFICATION_ID = 3;

    public TodoNotificationService(){
        super("TodoNotificationService");
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        Log.v("Title", intent.getStringExtra("Title"));
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            Notification.Builder builder = new Notification.Builder(this, CHANNEL_ID);
            builder.setContentTitle(intent.getStringExtra("Title") + " due soon!");
            builder.setContentText("The todo due today!");
            builder.setSmallIcon(R.drawable.ic_baseline_error_24);
            builder.setAutoCancel(true);
            Intent notifyIntent = new Intent(this, MainActivity.class);
            PendingIntent pendingIntent = PendingIntent.getActivity(this, 2, notifyIntent, PendingIntent.FLAG_UPDATE_CURRENT);
            builder.setContentIntent(pendingIntent);
            Notification notificationCompat = builder.build();
            NotificationManagerCompat managerCompat = NotificationManagerCompat.from(this);
            managerCompat.notify(NOTIFICATION_ID, notificationCompat);
        }else{
            Notification.Builder builder = new Notification.Builder(this);
            builder.setContentTitle(intent.getStringExtra("Title") + " due soon!");
            builder.setContentText("The todo due today!");
            builder.setSmallIcon(R.drawable.ic_baseline_error_24);
            builder.setPriority(Notification.PRIORITY_DEFAULT);
            builder.setAutoCancel(true);
            Intent notifyIntent = new Intent(this, MainActivity.class);
            PendingIntent pendingIntent = PendingIntent.getActivity(this, 2, notifyIntent, PendingIntent.FLAG_UPDATE_CURRENT);
            builder.setContentIntent(pendingIntent);
            Notification notificationCompat = builder.build();
            NotificationManagerCompat managerCompat = NotificationManagerCompat.from(this);
            managerCompat.notify(NOTIFICATION_ID, notificationCompat);
        }
    }
}

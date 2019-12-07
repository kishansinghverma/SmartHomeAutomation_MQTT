package com.example.smarthomeautomation_mqtt;


import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.graphics.BitmapFactory;
import android.os.Build;
import androidx.core.app.NotificationCompat;

public class NotificationHandler{

    public NotificationHandler(NotificationManager notificationManager) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "Smart Home";
            String description = "Smart Home Automation Notification";
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel("SHA", name, importance);
            channel.setDescription(description);
            notificationManager.createNotificationChannel(channel);
        }
    }

    public NotificationCompat.Builder createGasNotification(Context context){
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, "SHA")
                .setSmallIcon(R.drawable.small_home)
                .setContentTitle("Alert!")
                .setLargeIcon(BitmapFactory.decodeResource(context.getResources(), R.drawable.critical))
                .setContentText("Gas detected in the House!")
                .setPriority(NotificationCompat.PRIORITY_DEFAULT);

        return builder;
    }

    public NotificationCompat.Builder createWaterNotification(Context context){

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, "SHA")
                .setContentTitle("Alert!")
                .setContentText("Water tank is full...")
                .setSmallIcon(R.drawable.small_home)
                .setLargeIcon(BitmapFactory.decodeResource(context.getResources(), R.drawable.alert))
                .setPriority(NotificationCompat.PRIORITY_DEFAULT);

        return builder;
    }
}

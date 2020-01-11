package com.example.recommendmeaplace;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SyncRequest;
import android.os.IBinder;
import android.os.SystemClock;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

public class SyncService extends Service {

    public static final String TAG = "Synchronization Service";
    public static Context context;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        new RemoteDataBaseHelper(SyncService.this.getApplication(), true).synchronize();
//        NotificationManager mNotificationManager =
//                (NotificationManager) getSystemService(SyncService.this.NOTIFICATION_SERVICE);
//        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
//            NotificationChannel channel = new NotificationChannel("YOUR_CHANNEL_ID",
//                    "YOUR_CHANNEL_NAME",
//                    NotificationManager.IMPORTANCE_DEFAULT);
//            channel.setDescription("YOUR_NOTIFICATION_CHANNEL_DISCRIPTION");
//            mNotificationManager.createNotificationChannel(channel);
//        }
//        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(SyncService.this, "YOUR_CHANNEL_ID")
//                .setSmallIcon(R.mipmap.ic_launcher_round)
//                .setContentTitle("Recommend Me A Place")
//                .setContentText("Synchronization has completed successfully !")
//                .setPriority(NotificationCompat.PRIORITY_DEFAULT);
//
//
//        mNotificationManager.notify(0, mBuilder.build());
        return START_NOT_STICKY;
    }
}

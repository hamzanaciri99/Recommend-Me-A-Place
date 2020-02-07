package com.example.recommendmeaplace;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.app.job.JobInfo;
import android.app.job.JobParameters;
import android.app.job.JobScheduler;
import android.app.job.JobService;
import android.app.job.JobWorkItem;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.os.SystemClock;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import java.util.List;


public class SyncService extends Service {

    public static final String CHANNEL_ID = "RECOMMEND_ME_A_PLACE";
    public static final String CHANNEL_NAME = "Recommend Me a Place";
    public static final long ONE_DAY_INTERVAL = 24 * 60 * 60 * 1000L; // 1 Day
    public static final long HALF_DAY_INTERVAL = 12 * 60 * 60 * 1000L; // 1/2 Day
    public static final long TEN_SECONDS_INTERVAL = 10 * 1000L; // 10 Seconds


    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        new Thread() {
            @Override
            public void run() {
                super.run();

                new RemoteDataBaseHelper(SyncService.this.getApplication(), true).synchronize();

                NotificationManager mNotificationManager =
                        (NotificationManager) getSystemService(SyncService.this.NOTIFICATION_SERVICE);
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                    NotificationChannel channel = new NotificationChannel(CHANNEL_ID,
                            CHANNEL_NAME,
                            NotificationManager.IMPORTANCE_DEFAULT);
                    channel.setDescription(CHANNEL_NAME);
                    mNotificationManager.createNotificationChannel(channel);
                }
                NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(SyncService.this, CHANNEL_ID)
                        .setSmallIcon(R.mipmap.ic_launcher_round)
                        .setContentText("Locations have been updated successfully !")
                        .setPriority(NotificationCompat.PRIORITY_DEFAULT);

                mNotificationManager.notify(0, mBuilder.build());
            }
        }.start();
        return START_NOT_STICKY;
    }
}
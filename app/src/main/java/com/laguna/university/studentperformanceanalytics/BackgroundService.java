package com.laguna.university.studentperformanceanalytics;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v7.app.NotificationCompat;

/**
 * Created by W10 on 27/10/2017.
 */

public class BackgroundService extends Service {

    SQLiteDBcontroller db = new SQLiteDBcontroller(this);

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        notifyThis();
        return START_STICKY;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
    }

    @Override
    public void onDestroy() {
    }

    public void notifyThis() {
        String name = "";
        int notifCount = 0;
        if(MainActivity.active){
            db.login(MainActivity.user, MainActivity.pass);
        }
        if(db.loginID.size() > 0){
            db.getNotification(db.loginID.get(0));
            for(int i=0;i<db.notificationId.size();i++){
                if(db.notificationStatus.get(i).equals("UNREAD")){
                    notifCount++;
                }
            }
            name = db.loginFirstName(db.loginID.get(0));
        }
        NotificationManager nm = (NotificationManager) this.getSystemService(Context.NOTIFICATION_SERVICE);
        if(notifCount > 0) {
            NotificationCompat.Builder notif = new NotificationCompat.Builder(this);
            PendingIntent contentIntent = PendingIntent.getActivity(this, 0, new Intent(this, Login.class), PendingIntent.FLAG_UPDATE_CURRENT);

            notif.setAutoCancel(false)
                    .setSmallIcon(R.drawable.ic_chart_bar)
                    .setTicker(name+", You have " + notifCount + " notifications.")
                    .setContentTitle("Student performance Analytics")
                    .setContentText(name+", You have " + notifCount + " notifications. Please see immediately.")
                    .setContentInfo(String.valueOf(notifCount))
                    .setContentIntent(contentIntent)
                    .setOngoing(true);

            nm.notify(1, notif.build());
        } else {
            nm.cancelAll();
        }
    }
}
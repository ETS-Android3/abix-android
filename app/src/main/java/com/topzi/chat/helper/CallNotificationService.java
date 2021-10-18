package com.topzi.chat.helper;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;

import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;
import android.util.Log;

import com.topzi.chat.activity.CallActivity;
import com.topzi.chat.R;

/**
 * Created on 14/9/18.
 */

public class CallNotificationService extends Service {

    static DatabaseHandler dbhelper;

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d("CallNotificationService", "Service Started");
        dbhelper = DatabaseHandler.getInstance(this);
        setCallNotification(this);
        return START_NOT_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d("CallNotificationService", "Service Destroyed");
    }

    @Override
    public void onTaskRemoved(Intent rootIntent) {
        Log.e("CallNotificationService", "END");
        //Code here
        SocketConnection socketConnection = SocketConnection.getInstance(this);
        socketConnection.disconnect();
        stopSelf();
        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        if (notificationManager != null) {
            notificationManager.cancel("abixcall", 0);
        }
    }

    public static void setCallNotification(Context context){
        String appName = context.getString(R.string.app_name);
        long when = System.currentTimeMillis();

        Intent intent = new Intent(context, CallActivity.class);
        intent.putExtra("notification", "true");
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
        PendingIntent resultPendingIntent =
                PendingIntent.getActivity(context, 0, intent, 0);

        Intent intent2 = new Intent(context, CallActivity.class);
        intent2.putExtra("_ACTION_", "endcall");
        intent2.setAction("End Call");
        intent2.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
        PendingIntent endIntent = PendingIntent.getActivity(context, 0, intent2, 0);

        String channelId = context.getString(R.string.notification_channel_id);
        CharSequence channelName = context.getString(R.string.app_name);
        int importance = NotificationManager.IMPORTANCE_HIGH;

        NotificationManager mNotifyManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel notificationChannel = new NotificationChannel(channelId, channelName, importance);
            mNotifyManager.createNotificationChannel(notificationChannel);
        }
        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(context, channelId);
        mBuilder.setContentTitle(appName)
                .setContentText("Ongoing call").setTicker(appName).setWhen(when)
                .setStyle(new NotificationCompat.BigTextStyle().bigText("Ongoing call"))
                .setContentIntent(resultPendingIntent)
                .setSmallIcon(R.drawable.temp)
                .setColor(ContextCompat.getColor(context, R.color.colorAccent))
                .addAction(R.drawable.call_cancel, "End call", endIntent)
                .setOngoing(true)
                .setAutoCancel(true);
        mNotifyManager.notify("abixcall", 0, mBuilder.build());
    }
}

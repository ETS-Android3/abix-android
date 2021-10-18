package com.topzi.chat.helper;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.media.AudioAttributes;
import android.media.AudioManager;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import com.topzi.chat.R;
import com.topzi.chat.utils.Constants;

import java.util.Objects;

public class HeadsUpNotificationService extends Service {
    private String CHANNEL_ID = "AbixCall";
    private String CHANNEL_NAME = "Call Channel";
    DatabaseHandler dbhelper;
    private String userID = "";

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        dbhelper = DatabaseHandler.getInstance(this);
        Bundle data = null;
        if (intent != null && intent.getExtras() != null) {
            data = intent.getExtras();
            userID = data.getString("user_id");
            Log.e("LLLL_Call: ",dbhelper.getCallTone(userID));
        }
        try {
            Intent receiveCallAction = new Intent(getApplicationContext(), HeadsUpNotificationActionReceiver.class);
            receiveCallAction.putExtra(Constants.CALL_RESPONSE_ACTION_KEY, Constants.CALL_RECEIVE_ACTION);
            receiveCallAction.putExtra(Constants.FCM_DATA_KEY, data);
            receiveCallAction.setAction("RECEIVE_CALL");

//            Intent cancelCallAction = new Intent(getApplicationContext(), HeadsUpNotificationActionReceiver.class);
//            cancelCallAction.putExtra(Constants.CALL_RESPONSE_ACTION_KEY, Constants.CALL_CANCEL_ACTION);
//            cancelCallAction.putExtra(Constants.FCM_DATA_KEY, data);
//            cancelCallAction.setAction("CANCEL_CALL");

            PendingIntent receiveCallPendingIntent = PendingIntent.getBroadcast(getApplicationContext(), 1200, receiveCallAction, PendingIntent.FLAG_UPDATE_CURRENT);
//            PendingIntent cancelCallPendingIntent = PendingIntent.getBroadcast(getApplicationContext(), 1201, cancelCallAction, PendingIntent.FLAG_UPDATE_CURRENT);

            createChannel();
            NotificationCompat.Builder notificationBuilder = null;
            if (data != null) {
                notificationBuilder = new NotificationCompat.Builder(this, CHANNEL_ID)
                        .setContentText(data.getString("user_name"))
                        .setContentTitle("Incoming " + data.getString("type") + " Call")
                        .setSmallIcon(R.drawable.call_atten)
                        .setPriority(NotificationCompat.PRIORITY_HIGH)
                        .setCategory(NotificationCompat.CATEGORY_CALL)
                        .setChannelId(CHANNEL_ID)
//                        .addAction(R.drawable.call_atten, "Receive Call", receiveCallPendingIntent)
//                        .addAction(R.drawable.call_cancel, "Cancel call", cancelCallPendingIntent)
                        .setAutoCancel(true)
                        .setVibrate(new long[]{0, 100, 1000, 300, 200, 100, 500, 200, 100})
                        .setSound(Uri.parse(dbhelper.getCallTone(userID)))
                        .setFullScreenIntent(receiveCallPendingIntent, true);
            }

            Notification incomingCallNotification = null;
            if (notificationBuilder != null) {
                incomingCallNotification = notificationBuilder.build();
            }
            startForeground(120, incomingCallNotification);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return START_STICKY;
    }

    /*
    Create noticiation channel if OS version is greater than or eqaul to Oreo
    */
    public void createChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, CHANNEL_NAME, NotificationManager.IMPORTANCE_HIGH);
            channel.setDescription("Call Notifications");
            channel.setVibrationPattern(new long[]{0, 100, 1000, 300, 200, 100, 500, 200, 100});
            channel.setSound(Uri.parse(dbhelper.getCallTone(userID)),
                    new AudioAttributes.Builder().setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                            .setLegacyStreamType(AudioManager.STREAM_RING)
                            .setUsage(AudioAttributes.USAGE_VOICE_COMMUNICATION).build());
            Objects.requireNonNull(getApplicationContext().getSystemService(NotificationManager.class)).createNotificationChannel(channel);
        }
    }
}

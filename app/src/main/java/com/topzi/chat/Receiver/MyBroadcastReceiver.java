package com.topzi.chat.Receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.widget.Toast;
import com.topzi.chat.Backup.RemoteBackup;
import com.topzi.chat.activity.SplashActivity;
import com.topzi.chat.sLock.UnHideActivity;
import com.topzi.chat.utils.ConstMethod;

import static android.content.Context.MODE_PRIVATE;

public class MyBroadcastReceiver extends BroadcastReceiver {

    private RemoteBackup remoteBackup;
    SplashActivity activity;

    SharedPreferences pref;
    SharedPreferences.Editor editor;

    @Override
    public void onReceive(Context context, Intent intent) {
        if(context instanceof SplashActivity) {
            activity = (SplashActivity) context;
        }
        Toast.makeText(context, "Alarm....", Toast.LENGTH_LONG).show();

        pref = activity.getSharedPreferences("SavedPref", MODE_PRIVATE);
        editor = pref.edit();

        editor.putLong("bakupTime", System.currentTimeMillis());
        editor.commit();
        remoteBackup = new RemoteBackup(activity);

        if (pref.getString("backupOver","Wi-Fi").equals("Wi-Fi")) {
            if (ConstMethod.getNetworkType(context).equals("WIFI")) {
                remoteBackup.connectToDrive(true);
            }
        } else {
            remoteBackup.connectToDrive(true);
        }

    }
}

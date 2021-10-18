package com.topzi.chat.Receiver;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;

import com.topzi.chat.activity.SplashActivity;

public class DialPadListener extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {

                String ourCode = "*1234#";
                String dialedNumber = getResultData();

                if (dialedNumber.equals(ourCode)){

                        PackageManager packageManager = context.getPackageManager();
                        ComponentName componentName = new ComponentName(context, SplashActivity.class);
                        packageManager.setComponentEnabledSetting(componentName,PackageManager.COMPONENT_ENABLED_STATE_ENABLED, PackageManager.DONT_KILL_APP);

                        // My app will bring up, so cancel the dialer broadcast
                        setResultData(null);

                        //Intent to launch MainActivity
                        Intent intent_to_mainActivity = new Intent(context, SplashActivity.class);
                        intent_to_mainActivity.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        context.startActivity(intent_to_mainActivity);

                }
        }
}

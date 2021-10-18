package com.topzi.chat.sLock.service;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.topzi.chat.sLock.UnHideActivity;

public class LocalBroadcastReceiver extends BroadcastReceiver {
    private static final String TAG = "LocalBroadcastReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        String mAction = intent.getAction();

        Log.e(TAG, "The Intent Action is: "+mAction);

        if (mAction.equals(Intent.ACTION_SCREEN_ON)) {
            if (!UnHideActivity.isShown) {
                UnHideActivity.isShown = true;
                Intent jmpSLA = new Intent();
                jmpSLA.setClass(context, UnHideActivity.class);
                jmpSLA.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(jmpSLA);
            }
        }
    }

}


package com.topzi.chat.helper;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import com.topzi.chat.activity.CallActivity;
import com.topzi.chat.utils.Constants;


public class HeadsUpNotificationActionReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent != null && intent.getExtras() != null) {
            String action = intent.getStringExtra(Constants.CALL_RESPONSE_ACTION_KEY);
            Bundle data = intent.getBundleExtra(Constants.FCM_DATA_KEY);

//            if (action != null) {
                performClickAction(context, action, data);
//            }

            // Close the notification after the click action is performed.

            Intent it = new Intent(Intent.ACTION_CLOSE_SYSTEM_DIALOGS);
            context.sendBroadcast(it);
            context.stopService(new Intent(context, HeadsUpNotificationService.class));
        }
    }

    private void performClickAction(Context context, String action, Bundle data) {
//        if (action.equals(Constants.CALL_RECEIVE_ACTION) && data != null && data.get("type").equals("voip")) {
            Intent openIntent;
                openIntent = new Intent(context, CallActivity.class);
                        openIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        openIntent.putExtra("type", data.getString("type"));
        openIntent.putExtra("from", "receive");
        openIntent.putExtra("user_id", data.getString("user_id"));
        openIntent.putExtra("call_id", data.getString("call_id"));
        openIntent.putExtra("user_name", data.getString("user_name"));
                context.startActivity(openIntent);
//        } else if (action.equals(Constants.CALL_RECEIVE_ACTION) && data != null && data.get("type").equals("video")) {
//            Intent openIntent;
//                openIntent = new Intent(context, CallActivity.class);
//                        openIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//                context.startActivity(openIntent);
//        } else if (action.equals(Constants.CALL_CANCEL_ACTION)) {
//            context.stopService(new Intent(context, HeadsUpNotificationService.class));
//            Intent it = new Intent(Intent.ACTION_CLOSE_SYSTEM_DIALOGS);
//            context.sendBroadcast(it);
//        }
    }
}
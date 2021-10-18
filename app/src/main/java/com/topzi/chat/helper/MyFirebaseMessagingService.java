package com.topzi.chat.helper;


import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.Color;
import android.media.AudioAttributes;
import android.media.AudioManager;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.TrafficStats;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;

import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;

import android.os.VibrationEffect;
import android.os.Vibrator;
import android.telephony.TelephonyManager;
import android.util.Log;

import com.quickblox.users.model.QBUser;
import com.topzi.chat.model.DataStorageModel;
import com.topzi.chat.model.UserProfileModel;
import com.topzi.chat.service.LoginService;
import com.topzi.chat.utils.ObjectSerializer;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.google.gson.Gson;
import com.topzi.chat.activity.ApplicationClass;
import com.topzi.chat.activity.CallActivity;
import com.topzi.chat.activity.ChannelChatActivity;
import com.topzi.chat.activity.ChatActivity;
import com.topzi.chat.activity.GroupChatActivity;
import com.topzi.chat.activity.MainActivity;
import com.topzi.chat.R;
import com.topzi.chat.model.ChannelMessage;
import com.topzi.chat.model.ChannelResult;
import com.topzi.chat.model.ContactsData;
import com.topzi.chat.model.GroupData;
import com.topzi.chat.model.GroupMessage;
import com.topzi.chat.model.MessagesData;
import com.topzi.chat.utils.ApiClient;
import com.topzi.chat.utils.ApiInterface;
import com.topzi.chat.utils.Constants;
import com.topzi.chat.utils.GetSet;
import com.topzi.chat.utils.SharedPrefsHelper;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.text.CharacterIterator;
import java.text.StringCharacterIterator;
import java.util.ArrayList;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Random;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static com.topzi.chat.utils.Constants.TAG_ADMIN_ID;
import static com.topzi.chat.utils.Constants.TAG_CHANNEL_ID;
import static com.topzi.chat.utils.Constants.TAG_CHANNEL_NAME;
import static com.topzi.chat.utils.Constants.TAG_GROUP_ADMIN_ID;
import static com.topzi.chat.utils.Constants.TAG_GROUP_ID;
import static com.topzi.chat.utils.Constants.TAG_GROUP_NAME;
import static com.topzi.chat.utils.Constants.TAG_MEMBER_ID;

/**
 * Created on 03/11/16.
 */

public class MyFirebaseMessagingService extends FirebaseMessagingService {

    private static final String TAG = "MyFirebaseMsgService";
    private SharedPreferences pref;
    private SharedPreferences.Editor editor;
    DatabaseHandler dbhelper;

    private long rxMesBytesFinal;
    protected SharedPrefsHelper sharedPrefsHelper;

    public static void start(Context context) {
        Intent startServiceIntent = new Intent(context, MyFirebaseInstanceIDService.class);
        context.startService(startServiceIntent);

        Intent notificationServiceIntent = new Intent(context, MyFirebaseMessagingService.class);
        context.startService(notificationServiceIntent);

    }

    public Long getUidRxBytes(int uid) {
        BufferedReader reader;
        Long rxBytes = 0L;
        try {
            reader = new BufferedReader(new FileReader("/proc/uid_stat/" + uid
                    + "/tcp_rcv"));
            rxBytes = Long.parseLong(reader.readLine());
            reader.close();
        } catch (FileNotFoundException e) {
            rxBytes = TrafficStats.getUidRxBytes(uid);
            //e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return rxBytes;
    }

    public Long getUidTxBytes(int uid) {
        BufferedReader reader;
        Long txBytes = 0L;
        try {
            reader = new BufferedReader(new FileReader("/proc/uid_stat/" + uid
                    + "/tcp_snd"));
            txBytes = Long.parseLong(reader.readLine());
            reader.close();
        } catch (FileNotFoundException e) {
            txBytes = TrafficStats.getUidTxBytes(uid);
            //e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return txBytes;
    }

    public static String humanReadableByteCountSI(long bytes) {
        if (-1000 < bytes && bytes < 1000) {
            return bytes + " B";
        }
        CharacterIterator ci = new StringCharacterIterator("kMGTPE");
        while (bytes <= -999_950 || bytes >= 999_950) {
            bytes /= 1000;
            ci.next();
        }
        return String.format("%.1f %cB", bytes / 1000.0, ci.current());
    }




    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {

        pref = getApplicationContext().getSharedPreferences(Constants.NETWORK_USAGE, MODE_PRIVATE);
        editor = pref.edit();
        dbhelper = DatabaseHandler.getInstance(getApplicationContext());
        sharedPrefsHelper = SharedPrefsHelper.getInstance();
        if (pref.getBoolean("isLogged", false)) {
            GetSet.setUserId(pref.getString("userId", null));
            GetSet.setToken(pref.getString("token", null));
        }

        if (remoteMessage.getData().size() > 0) {
            if (remoteMessage.getNotification() != null)
//                Log.e("LLLLL_Notification111 ", "Data Payload: " + remoteMessage.getNotification().getBody());
                Log.e("LLLLL_Notification ", "Data Payload: " + remoteMessage.getData().toString());
            long sendTime = (long) remoteMessage.getSentTime();
            try {
                String strData = remoteMessage.getData().toString().replace("messsage_data=", "\"message_data\":");
                JSONObject remdata = new JSONObject(strData);
                Log.e(TAG, "Data Payload: strData" + remdata);
                JSONObject data = remdata.getJSONObject("message_data");
                String chatType = data.optString(Constants.TAG_CHAT_TYPE, "");
                if (chatType.equals(Constants.TAG_SINGLE)) {

                    long count = pref.getLong("receiveMesCount", 0);
                    editor.putLong("receiveMesCount", count + 1);
                    editor.apply();

                    DataStorageModel dataStorageModel = dbhelper.getRecord(data.optString("sender_id", ""));

                    dbhelper.addDataStorage(data.optString("sender_id", ""),
                            String.valueOf(Long.parseLong(dataStorageModel.getMessage_count()) + 1),
                            dataStorageModel.getSent_contact(),
                            dataStorageModel.getSent_location(),
                            dataStorageModel.getSent_photos(),
                            dataStorageModel.getSent_videos(),
                            dataStorageModel.getSent_aud(),
                            dataStorageModel.getSent_doc(),
                            dataStorageModel.getSent_photos_size(),
                            dataStorageModel.getSent_videos_size(),
                            dataStorageModel.getSent_aud_size(),
                            dataStorageModel.getSent_doc_size());

                    int UID = android.os.Process.myUid();

                    Long rxBytes = pref.getLong("MesReceiveTotal", getUidRxBytes(UID));

                    rxMesBytesFinal = getUidRxBytes(UID) - rxBytes;

                    Log.e("LLLLL_Mes_Size1: ", humanReadableByteCountSI(rxMesBytesFinal));

                    editor.putLong("MesReceiveTotal", getUidRxBytes(UID));
                    editor.apply();
                    editor.commit();

                    long totalrx = pref.getLong("MesSent", 0) + rxMesBytesFinal;
                    editor.putLong("MesReceive", totalrx);
                    editor.apply();
                    Log.e("LLLLL_MesReceive: ", humanReadableByteCountSI(totalrx));
                    editor.commit();

                    String sender_id = data.optString("sender_id", "");
                    if (!ApplicationClass.onAppForegrounded) {
                        MessagesData mdata = getMessagesByType(new JSONObject().put("message_data", data));
                        chatReceived(mdata);
                    }
                    if (dbhelper.isUserExist(sender_id)) {

                        ContactsData.Result results = dbhelper.getContactDetail(sender_id);
                        if (results != null && !results.mute_notification.equals("true")) {//User Not Blocked Notifications
                            if (pref.getString("mutenotification", "").equals("")) {
                                if (ChatActivity.tempUserId != null && !ChatActivity.tempUserId.equalsIgnoreCase(sender_id)) {
                                    showSmallNotification(data);
                                }
                            } else {
                                if (ChatActivity.tempUserId != null && !ChatActivity.tempUserId.equalsIgnoreCase(sender_id))
                                    muteSmallNotification(data);
                            }
                        }
                    }
                } else if (chatType.equals(Constants.TAG_GROUP)) {
                    String groupId = data.optString(Constants.TAG_GROUP_ID, "");
                    String memberId = data.optString(Constants.TAG_MEMBER_ID, "");
                    String msgType = data.optString(Constants.TAG_MESSAGE_TYPE, "");
                    if (!ApplicationClass.onAppForegrounded && dbhelper.isGroupExist(groupId)) {
                        getGroupMessagesByType(new JSONObject().put("message_data", data));
                    }
                    if (!memberId.equals(GetSet.getUserId()) && dbhelper.isGroupExist(groupId) &&
                            !msgType.equals("admin") && !msgType.equals("subject") && !msgType.equals("group_image") &&
                            !msgType.equals("add_member") && !msgType.equals("remove_member") && !msgType.equals("left")) {
                        GroupData results = dbhelper.getGroupData(getApplicationContext(), groupId);
                        if (results != null && !results.muteNotification.equals("true")) {//Group Not Blocked Notifications
                            if (GroupChatActivity.tempGroupId != null && !GroupChatActivity.tempGroupId.equalsIgnoreCase(groupId))
                                showSmallNotification(data);
                        }
                    }
                } else if (chatType.equals(Constants.TAG_CALL)) {
                    String userId = data.optString("caller_id", "");
                    if (dbhelper.isUserExist(userId)) {
                        ContactsData.Result results = dbhelper.getContactDetail(userId);
                        String userName = ApplicationClass.getContactName(this, results.phone_no);
                        onCallReceive(data, sendTime, userName);
                    } else {
                        getUserInfo(userId, data, sendTime);
                    }
                } else if (chatType.equals(Constants.TAG_CHANNEL)) {
                    String channelId = data.optString(Constants.TAG_CHANNEL_ID, "");
                    String msgType = data.optString(Constants.TAG_MESSAGE_TYPE, "");
                    if (!ApplicationClass.onAppForegrounded) {
                        getChannelMessagesByType(new JSONObject().put("message_data", data));
                    }
                    if (dbhelper.isChannelExist(channelId) && !msgType.equals("subject") && !msgType.equals("channel_image") &&
                            !msgType.equals("channel_des")) {
                        ChannelResult.Result results = dbhelper.getChannelInfo(channelId);
                        data.put(Constants.TAG_CHANNEL_NAME, "");
                        if (results != null && !results.muteNotification.equals("true")) {
                            data.put(Constants.TAG_CHANNEL_NAME, results.channelName);
                            if (ChannelChatActivity.tempChannelId != null && !ChannelChatActivity.tempChannelId.equalsIgnoreCase(channelId))
                                showSmallNotification(data);
                        }
                    }
                } else if (chatType.equalsIgnoreCase(Constants.TAG_GROUP_INVITATION)) {
                    String adminId = data.optString(Constants.TAG_ADMIN_ID, "");
                    if (!adminId.equalsIgnoreCase(GetSet.getUserId()))
                        showSmallNotification(data);
                } else if (chatType.equalsIgnoreCase(Constants.TAG_CHANNEL_INVITATION)) {
                    showSmallNotification(data);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    public MessagesData getMessagesByType(JSONObject data) {
        MessagesData mdata = new MessagesData();
        try {
            JSONObject jobj = data.getJSONObject("message_data");
            mdata.user_id = jobj.optString(Constants.TAG_SENDER_ID, "");
            mdata.message_type = jobj.optString(Constants.TAG_MESSAGE_TYPE, "");
            mdata.message = jobj.optString(Constants.TAG_MESSAGE, "");
            mdata.message_id = jobj.optString(Constants.TAG_MESSAGE_ID, "");
            mdata.attachment = jobj.optString(Constants.TAG_ATTACHMENT, "");
            mdata.chat_time = jobj.optString(Constants.TAG_CHAT_TIME, "");
            mdata.receiver_id = jobj.optString(Constants.TAG_RECEIVER_ID, "");
            mdata.sender_id = jobj.optString(Constants.TAG_SENDER_ID, "");
            mdata.lat = jobj.optString(Constants.TAG_LAT, "");
            mdata.lon = jobj.optString(Constants.TAG_LON, "");
            mdata.contact_name = jobj.optString(Constants.TAG_CONTACT_NAME, "");
            mdata.contact_phone_no = jobj.optString(Constants.TAG_CONTACT_PHONE_NO, "");
            mdata.contact_country_code = jobj.optString(Constants.TAG_CONTACT_COUNTRY_CODE, "");
            mdata.thumbnail = jobj.optString(Constants.TAG_THUMBNAIL, "");

            dbhelper.addMessageDatas(GetSet.getUserId() + mdata.user_id, mdata.message_id, mdata.user_id, "",
                    mdata.message_type, mdata.message, mdata.attachment, mdata.lat, mdata.lon, mdata.contact_name, mdata.contact_phone_no,
                    mdata.contact_country_code, mdata.chat_time, GetSet.getUserId(), mdata.user_id, "sent", mdata.thumbnail, mdata.reply_to, mdata.groupId);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return mdata;
    }

    void chatReceived(final MessagesData mdata) {
//        Map<String, String> params = new HashMap<>();
//        params.put("user_id", GetSet.getUserId());
//        params.put("sender_id", mdata.sender_id);
//        params.put("receiver_id", mdata.receiver_id);
//        params.put("message_id", mdata.message_id);
//        params.put("chat_id", mdata.receiver_id + mdata.sender_id);
        ApiInterface apiInterface = ApiClient.getClient().create(ApiInterface.class);
        Call<Map<String, String>> call3 = apiInterface.chatreceived(GetSet.getToken(), GetSet.getUserId(), mdata.sender_id, mdata.message_id);
        call3.enqueue(new Callback<Map<String, String>>() {
            @Override
            public void onResponse(Call<Map<String, String>> call, Response<Map<String, String>> response) {
                try {
                    Log.v("chatReceived", "response=" + new Gson().toJson(response));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(Call<Map<String, String>> call, Throwable t) {
                Log.v("chatReceived", "TEST" + t.getMessage());
                call.cancel();
            }
        });
    }

    private void getUserInfo(String memberId, JSONObject data, long sendTime) {
        ApiInterface apiInterface = ApiClient.getClient().create(ApiInterface.class);
        Call<UserProfileModel> call3 = apiInterface.getuserprofile(memberId);
        call3.enqueue(new Callback<UserProfileModel>() {
            @Override
            public void onResponse(Call<UserProfileModel> call, Response<UserProfileModel> response) {
                try {
                    Log.v(TAG, "getUserInfo: " + new Gson().toJson(response));
                    UserProfileModel userdata = response.body();
                    if (userdata != null && userdata.getSTATUS().equals("true")) {
                        dbhelper.addContactDetails(userdata.getRESULT().getId(), userdata.getRESULT().getUserName(), userdata.getRESULT().getPhoneNo(),
                                userdata.getRESULT().getCountryCode(), userdata.getRESULT().getUserImage(), userdata.getRESULT().getPrivacyAbout(),
                                userdata.getRESULT().getPrivacyLastSeen(), userdata.getRESULT().getPrivacyProfileImage(), userdata.getRESULT().getAbout(), "true");

                        onCallReceive(data, sendTime, userdata.getRESULT().getPhoneNo());
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(Call<UserProfileModel> call, Throwable t) {
                Log.v("getUserInfo Failed", "TEST" + t.getMessage());
                call.cancel();
            }
        });
    }

    private void onCallReceive(JSONObject data, long sendTime, String userName) {
        String userId = data.optString("caller_id", "");
        String type = data.optString(Constants.TAG_TYPE, "");
        String callId = data.optString("call_id", "");
        String unixStamp = data.optString("created_at", "");
        String call_type = data.optString("call_type", "");
        long diffInMs = System.currentTimeMillis() - sendTime;
        long diffSeconds = diffInMs / 1000;
        Log.e("LLL_FCM", "sendTime=" + sendTime);
        Log.e("LLL_FCM", "diffInSec=" + diffSeconds);
        Log.e("LLLL_FCM", "now=" + System.currentTimeMillis());

        if (call_type.equalsIgnoreCase("created")) {
            TelephonyManager telephony = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
            int isPhoneCallOn = telephony.getCallState();

            dbhelper.addRecentCall(callId, userId, type, "incoming", unixStamp);

            if (sharedPrefsHelper.hasQbUser()) {
                QBUser qbUser = sharedPrefsHelper.getQbUser();
                Log.d(TAG, "App has logged user" + qbUser.getId());
                LoginService.start(this, qbUser);
            }

//            if (diffSeconds < 30 && !CallActivity.isInCall && isPhoneCallOn == 0 && !ApplicationClass.onAppForegrounded) {
//                CallActivity.isInCall = true;
//                Intent intent = new Intent(getApplicationContext(), HeadsUpNotificationService.class);
//                Bundle bundle = new Bundle();
////                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//                bundle.putString("type", type);
//                bundle.putString("from", "receive");
//                bundle.putString("user_id", userId);
//                bundle.putString("call_id", callId);
//                bundle.putString("user_name", userName);
//                intent.putExtras(bundle);
////                startActivity(intent);
//                startService(intent);
//            }
        } else if (call_type.equalsIgnoreCase("ended")) {
//            CallActivity.isInCall = false;
//            stopService(new Intent(getBaseContext(), HeadsUpNotificationService.class));
//            if (CallActivity.callActivity != null)
//                CallActivity.callActivity.finish();
        }
    }

    public void showSmallNotification(JSONObject jsonObject) throws JSONException {
        try {
            Intent intent = null;
            String message = jsonObject.optString("message", "");
            String userName = "Topzi", userId = "", group_id = "";

            if (jsonObject.get(Constants.TAG_CHAT_TYPE).equals(Constants.TAG_SINGLE)) {
                editor.putString("popup", "always");
                editor.commit();
                String sender_id = jsonObject.optString("sender_id", "");
                String mes_type = jsonObject.optString("message_type", "");
                userId = sender_id;

                if (mes_type.equalsIgnoreCase("contact")) {
                    DataStorageModel dataStorageModel = dbhelper.getRecord(userId);

                    dbhelper.addDataStorage(userId,
                            dataStorageModel.getMessage_count(),
                            String.valueOf(Long.parseLong(dataStorageModel.getSent_contact()) + 1),
                            dataStorageModel.getSent_location(),
                            dataStorageModel.getSent_photos(),
                            dataStorageModel.getSent_videos(),
                            dataStorageModel.getSent_aud(),
                            dataStorageModel.getSent_doc(),
                            dataStorageModel.getSent_photos_size(),
                            dataStorageModel.getSent_videos_size(),
                            dataStorageModel.getSent_aud_size(),
                            dataStorageModel.getSent_doc_size());

                }
                intent = new Intent(getApplicationContext(), ChatActivity.class);
                intent.putExtra("user_id", userId);
            } else if (jsonObject.get(Constants.TAG_CHAT_TYPE).equals(Constants.TAG_GROUP)) {
                group_id = jsonObject.optString(Constants.TAG_GROUP_ID, "");
                String phone_no = jsonObject.optString(Constants.TAG_MEMBER_NO, "");
                userName = jsonObject.optString(Constants.TAG_GROUP_NAME, "");
                String name = ApplicationClass.getContactName(this, phone_no);
                message = name + " : " + message;

                DataStorageModel dataStorageModel = dbhelper.getRecord(userId);

                dbhelper.addDataStorage(group_id,
                        dataStorageModel.getMessage_count(),
                        String.valueOf(Long.parseLong(dataStorageModel.getSent_contact()) + 1),
                        dataStorageModel.getSent_location(),
                        dataStorageModel.getSent_photos(),
                        dataStorageModel.getSent_videos(),
                        dataStorageModel.getSent_aud(),
                        dataStorageModel.getSent_doc(),
                        dataStorageModel.getSent_photos_size(),
                        dataStorageModel.getSent_videos_size(),
                        dataStorageModel.getSent_aud_size(),
                        dataStorageModel.getSent_doc_size());

                intent = new Intent(getApplicationContext(), GroupChatActivity.class);
                intent.putExtra("group_id", group_id);
            }

            if (jsonObject.get(Constants.TAG_CHAT_TYPE).equals(Constants.TAG_SINGLE)) {
                if (dbhelper.getPopup(userId).equals("Only when screen ON") && !ApplicationClass.onAppForegrounded) {
                    Log.e("LLLL_Mes: ", pref.getString("popup", ""));
                    stopService(new Intent(getApplicationContext(), PopUpService.class));
                    Intent intent1 = new Intent(getApplicationContext(), PopUpService.class);
                    Bundle bundle = new Bundle();
                    bundle.putString("message", jsonObject.toString());
                    bundle.putString("type", "screen ON");
                    intent1.putExtras(bundle);
                    startService(intent1);
                } else if (dbhelper.getPopup(userId).equals("Only when screen OFF")) {
                    Log.e("LLLL_Mes: ", pref.getString("popup", ""));
                    stopService(new Intent(getApplicationContext(), PopUpService.class));
                    Intent intent1 = new Intent(getApplicationContext(), PopUpService.class);
                    Bundle bundle = new Bundle();
                    bundle.putString("message", jsonObject.toString());
                    bundle.putString("type", "screen OFF");
                    intent1.putExtras(bundle);
                    startService(intent1);
                } else if (dbhelper.getPopup(userId).equals("Always show popup") && !ApplicationClass.onAppForegrounded) {
                    Log.e("LLLL_Mes: ", pref.getString("popup", ""));
                    stopService(new Intent(getApplicationContext(), PopUpService.class));
                    Intent intent1 = new Intent(getApplicationContext(), PopUpService.class);
                    Bundle bundle = new Bundle();
                    bundle.putString("message", jsonObject.toString());
                    bundle.putString("type", "always");
                    intent1.putExtras(bundle);
                    startService(intent1);
                }
            } else if (jsonObject.get(Constants.TAG_CHAT_TYPE).equals(Constants.TAG_GROUP)) {
                if (dbhelper.getPopup(group_id).equals("Only when screen ON") && !ApplicationClass.onAppForegrounded) {
                    Log.e("LLLL_Mes: ", pref.getString("popup", ""));
                    stopService(new Intent(getApplicationContext(), PopUpService.class));
                    Intent intent1 = new Intent(getApplicationContext(), PopUpService.class);
                    Bundle bundle = new Bundle();
                    bundle.putString("message", jsonObject.toString());
                    bundle.putString("type", "screen ON");
                    intent1.putExtras(bundle);
                    startService(intent1);
                } else if (dbhelper.getPopup(group_id).equals("Only when screen OFF")) {
                    Log.e("LLLL_Mes: ", pref.getString("popup", ""));
                    stopService(new Intent(getApplicationContext(), PopUpService.class));
                    Intent intent1 = new Intent(getApplicationContext(), PopUpService.class);
                    Bundle bundle = new Bundle();
                    bundle.putString("message", jsonObject.toString());
                    bundle.putString("type", "screen OFF");
                    intent1.putExtras(bundle);
                    startService(intent1);
                } else if (dbhelper.getPopup(group_id).equals("Always show popup") && !ApplicationClass.onAppForegrounded) {
                    Log.e("LLLL_Mes: ", pref.getString("popup", ""));
                    stopService(new Intent(getApplicationContext(), PopUpService.class));
                    Intent intent1 = new Intent(getApplicationContext(), PopUpService.class);
                    Bundle bundle = new Bundle();
                    bundle.putString("message", jsonObject.toString());
                    bundle.putString("type", "always");
                    intent1.putExtras(bundle);
                    startService(intent1);
                }
            }

            if (jsonObject.get(Constants.TAG_CHAT_TYPE).equals(Constants.TAG_SINGLE)) {
                editor.putString("popup", "always");
                editor.commit();
                String sender_id = jsonObject.optString("sender_id", "");
                String mes_type = jsonObject.optString("message_type", "");
                userId = sender_id;
                intent = new Intent(getApplicationContext(), ChatActivity.class);
                intent.putExtra("user_id", userId);
            } else if (jsonObject.get(Constants.TAG_CHAT_TYPE).equals(Constants.TAG_GROUP)) {
                group_id = jsonObject.optString(Constants.TAG_GROUP_ID, "");
                String phone_no = jsonObject.optString(Constants.TAG_MEMBER_NO, "");
                userName = jsonObject.optString(Constants.TAG_GROUP_NAME, "");
                String name = ApplicationClass.getContactName(this, phone_no);
                message = name + " : " + message;

                intent = new Intent(getApplicationContext(), GroupChatActivity.class);
                intent.putExtra("group_id", group_id);
            } else if (jsonObject.get(Constants.TAG_CHAT_TYPE).equals(Constants.TAG_CHANNEL)) {
                userName = jsonObject.optString(Constants.TAG_CHANNEL_NAME, "");
                String channel_id = jsonObject.optString(Constants.TAG_CHANNEL_ID, "");
                intent = new Intent(getApplicationContext(), ChannelChatActivity.class);
                intent.putExtra("channel_id", channel_id);
            } else if (jsonObject.get(Constants.TAG_CHAT_TYPE).equals(Constants.TAG_GROUP_INVITATION)) {
                userName = getLocaleString(R.string.new_group);
                group_id = jsonObject.optString(Constants.ID, "");
                message = getLocaleString(R.string.you_added_in_group) + " " + jsonObject.optString(Constants.TAG_TITLE, "");
                intent = new Intent(getApplicationContext(), MainActivity.class);
                intent.putExtra("group_id", group_id);
                intent.putExtra(Constants.IS_FROM, "group");
            } else if (jsonObject.get(Constants.TAG_CHAT_TYPE).equals(Constants.TAG_CHANNEL_INVITATION)) {
                userName = getLocaleString(R.string.new_channel_received);
                String channel_id = jsonObject.optString(Constants.ID, "");
                message = getLocaleString(R.string.invitation_received);
                intent = new Intent(getApplicationContext(), MainActivity.class);
                intent.putExtra("channel_id", channel_id);
                intent.putExtra(Constants.IS_FROM, "channel");
            }

            String appName = getString(R.string.app_name);
            Random random = new Random();
            int m = random.nextInt(9999 - 1000) + 1000;
            int uniqueInt = (int) (System.currentTimeMillis() & 0xfffffff);
            long when = System.currentTimeMillis();

            intent.putExtra("notification", "true");
            PendingIntent resultPendingIntent =
                    PendingIntent.getActivity(this, uniqueInt, intent, PendingIntent.FLAG_UPDATE_CURRENT);

            String channelId = getString(R.string.notification_channel_id);
            CharSequence channelName = getString(R.string.app_name);
            Vibrator v = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);

            if (dbhelper.isNotification(userId) || dbhelper.isNotification(group_id)) {
                if (dbhelper.getVibratType(userId).equals("Default") ||
                        dbhelper.getVibratType(group_id).equals("Default")) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        v.vibrate(VibrationEffect.createOneShot(300, VibrationEffect.DEFAULT_AMPLITUDE));
                    } else {
                        //deprecated in API 26
                        v.vibrate(300);
                    }
                } else if (dbhelper.getVibratType(userId).equals("Off") ||
                        dbhelper.getVibratType(group_id).equals("Off")) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        v.vibrate(VibrationEffect.createOneShot(0, VibrationEffect.DEFAULT_AMPLITUDE));
                    } else {
                        //deprecated in API 26
                        v.vibrate(0);
                    }
                } else if (dbhelper.getVibratType(userId).equals("Short") ||
                        dbhelper.getVibratType(group_id).equals("Short")) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        v.vibrate(VibrationEffect.createOneShot(100, VibrationEffect.DEFAULT_AMPLITUDE));
                    } else {
                        //deprecated in API 26
                        v.vibrate(100);
                    }
                } else if (dbhelper.getVibratType(userId).equals("Long") ||
                        dbhelper.getVibratType(group_id).equals("Long")) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        v.vibrate(VibrationEffect.createOneShot(500, VibrationEffect.DEFAULT_AMPLITUDE));
                    } else {
                        //deprecated in API 26
                        v.vibrate(500);
                    }
                }

                String LightColor = dbhelper.getLightColor(userId);
                Log.e("LLLLLL_LightColor: ", LightColor);
                Log.e("LLLLLL_Notifi_Sound: ", dbhelper.getNotificationTone(userId));

                NotificationManager mNotifyManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    int importance = NotificationManager.IMPORTANCE_HIGH;
                    NotificationChannel notificationChannel = new NotificationChannel(channelId, channelName, importance);

                    notificationChannel.setSound(Uri.parse(dbhelper.getNotificationTone(userId)), new AudioAttributes.Builder().setContentType(AudioAttributes.CONTENT_TYPE_UNKNOWN)
                            .setUsage(AudioAttributes.USAGE_NOTIFICATION_COMMUNICATION_INSTANT)
                            .build());

                    if (LightColor.equals("None")) {
                        notificationChannel.setLightColor(Color.TRANSPARENT);
                    } else if (LightColor.equals("White")) {
                        notificationChannel.setLightColor(Color.WHITE);
                    } else if (LightColor.equals("Red")) {
                        notificationChannel.setLightColor(Color.RED);
                    } else if (LightColor.equals("Yellow")) {
                        notificationChannel.setLightColor(Color.YELLOW);
                    } else if (LightColor.equals("Green")) {
                        notificationChannel.setLightColor(Color.GREEN);
                    } else if (LightColor.equals("Cyan")) {
                        notificationChannel.setLightColor(Color.CYAN);
                    } else if (LightColor.equals("Blue")) {
                        notificationChannel.setLightColor(Color.BLUE);
                    } else if (LightColor.equals("Purple")) {
                        notificationChannel.setLightColor(Color.MAGENTA);
                    }
                    notificationChannel.enableLights(true);
                    notificationChannel.enableVibration(true);

                    mNotifyManager.createNotificationChannel(notificationChannel);
                }
                Ringtone r = RingtoneManager.getRingtone(getApplicationContext(), Uri.parse(dbhelper.getNotificationTone(userId)));
                r.play();
                NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(this, channelId);
                Log.e("LLLL_Data: ", dbhelper.getNotificationTone(userId));
                mBuilder.setContentTitle(userName)
                        .setDefaults(Notification.FLAG_SHOW_LIGHTS)
                        .setChannelId(channelId)
                        .setSound(Uri.parse(dbhelper.getNotificationTone(userId)), AudioManager.STREAM_NOTIFICATION)
                        .setContentText(message).setTicker(appName).setWhen(when)
                        .setStyle(new NotificationCompat.BigTextStyle().bigText(message))
                        .setContentIntent(resultPendingIntent)
                        .setSmallIcon(R.drawable.app_icon)
                        .setColor(ContextCompat.getColor(this, R.color.colorAccent))
                        .setAutoCancel(true);

                if (LightColor.equals("None")) {
                    mBuilder.setLights(Color.TRANSPARENT, 500, 500);
                } else if (LightColor.equals("White")) {
                    mBuilder.setLights(Color.WHITE, 500, 500);
                } else if (LightColor.equals("Red")) {
                    mBuilder.setLights(Color.RED, 500, 500);
                } else if (LightColor.equals("Yellow")) {
                    mBuilder.setLights(Color.YELLOW, 500, 500);
                } else if (LightColor.equals("Green")) {
                    mBuilder.setLights(Color.GREEN, 500, 500);
                } else if (LightColor.equals("Cyan")) {
                    mBuilder.setLights(Color.CYAN, 500, 500);
                } else if (LightColor.equals("Blue")) {
                    mBuilder.setLights(Color.BLUE, 500, 500);
                } else if (LightColor.equals("Purple")) {
                    mBuilder.setLights(Color.MAGENTA, 500, 500);
                }
            } else {
                if (pref.getString("vibrateType", "Default").equals("Default")) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        v.vibrate(VibrationEffect.createOneShot(300, VibrationEffect.DEFAULT_AMPLITUDE));
                    } else {
                        //deprecated in API 26
                        v.vibrate(300);
                    }
                } else if (pref.getString("vibrateType", "Default").equals("Off")) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        v.vibrate(VibrationEffect.createOneShot(0, VibrationEffect.DEFAULT_AMPLITUDE));
                    } else {
                        //deprecated in API 26
                        v.vibrate(0);
                    }
                } else if (pref.getString("vibrateType", "Default").equals("Short")) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        v.vibrate(VibrationEffect.createOneShot(100, VibrationEffect.DEFAULT_AMPLITUDE));
                    } else {
                        //deprecated in API 26
                        v.vibrate(100);
                    }
                } else if (pref.getString("vibrateType", "Default").equals("Long")) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        v.vibrate(VibrationEffect.createOneShot(500, VibrationEffect.DEFAULT_AMPLITUDE));
                    } else {
                        //deprecated in API 26
                        v.vibrate(500);
                    }
                }
                String LightColor = "White";
                String callTune = "";

                if (!userId.equals("")) {
                    LightColor = pref.getString("appLightColor", "White");
                    callTune = pref.getString("appToneName", "");
                } else if (!group_id.equals("")) {
                    LightColor = pref.getString("appGrpLightColor", "White");
                    callTune = pref.getString("grpCallToneName", "");
                } else
                    LightColor = "White";

                Log.e("LLLLLL_LightColor: ", LightColor);

                NotificationManager mNotifyManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    int importance = NotificationManager.IMPORTANCE_HIGH;
                    NotificationChannel notificationChannel = new NotificationChannel(channelId, channelName, importance);

                    notificationChannel.setSound(Uri.parse(dbhelper.getNotificationTone(userId)), new AudioAttributes.Builder().setContentType(AudioAttributes.CONTENT_TYPE_UNKNOWN)
                            .setUsage(AudioAttributes.USAGE_NOTIFICATION_COMMUNICATION_INSTANT)
                            .build());

                    if (LightColor.equals("None")) {
                        notificationChannel.setLightColor(Color.TRANSPARENT);
                    } else if (LightColor.equals("White")) {
                        notificationChannel.setLightColor(Color.WHITE);
                    } else if (LightColor.equals("Red")) {
                        notificationChannel.setLightColor(Color.RED);
                    } else if (LightColor.equals("Yellow")) {
                        notificationChannel.setLightColor(Color.YELLOW);
                    } else if (LightColor.equals("Green")) {
                        notificationChannel.setLightColor(Color.GREEN);
                    } else if (LightColor.equals("Cyan")) {
                        notificationChannel.setLightColor(Color.CYAN);
                    } else if (LightColor.equals("Blue")) {
                        notificationChannel.setLightColor(Color.BLUE);
                    } else if (LightColor.equals("Purple")) {
                        notificationChannel.setLightColor(Color.MAGENTA);
                    }
                    notificationChannel.enableLights(true);
                    notificationChannel.enableVibration(true);

                    mNotifyManager.createNotificationChannel(notificationChannel);
                }
                Ringtone r = RingtoneManager.getRingtone(getApplicationContext(), Uri.parse(dbhelper.getNotificationTone(userId)));
                r.play();
                NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(this, channelId);
                Log.e("LLLL_Data: ", dbhelper.getNotificationTone(userId));
                mBuilder.setContentTitle(userName)
                        .setDefaults(Notification.FLAG_SHOW_LIGHTS)
                        .setChannelId(channelId)
                        .setSound(Uri.parse(callTune))
                        .setContentText(message).setTicker(appName).setWhen(when)
                        .setStyle(new NotificationCompat.BigTextStyle().bigText(message))
                        .setContentIntent(resultPendingIntent)
                        .setSmallIcon(R.drawable.app_icon)
                        .setColor(ContextCompat.getColor(this, R.color.colorAccent))
                        .setAutoCancel(true);

                if (LightColor.equals("None")) {
                    mBuilder.setLights(Color.TRANSPARENT, 500, 500);
                } else if (LightColor.equals("White")) {
                    mBuilder.setLights(Color.WHITE, 500, 500);
                } else if (LightColor.equals("Red")) {
                    mBuilder.setLights(Color.RED, 500, 500);
                } else if (LightColor.equals("Yellow")) {
                    mBuilder.setLights(Color.YELLOW, 500, 500);
                } else if (LightColor.equals("Green")) {
                    mBuilder.setLights(Color.GREEN, 500, 500);
                } else if (LightColor.equals("Cyan")) {
                    mBuilder.setLights(Color.CYAN, 500, 500);
                } else if (LightColor.equals("Blue")) {
                    mBuilder.setLights(Color.BLUE, 500, 500);
                } else if (LightColor.equals("Purple")) {
                    mBuilder.setLights(Color.MAGENTA, 500, 500);
                }
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private ArrayList<String> getHideChat() {

        ArrayList<String> hideChatID = new ArrayList<>();
        if (null == hideChatID) {
            hideChatID = new ArrayList<>();
        }

        try {
            hideChatID = (ArrayList<String>) ObjectSerializer.deserialize(pref.getString("hideId", ObjectSerializer.serialize(new ArrayList<String>())));
            Log.e("LLLLL_Hide: ", String.valueOf(hideChatID));
            return hideChatID;
        } catch (IOException e) {
            Log.e("LLLLLLL_EX11: ", Objects.requireNonNull(e.getMessage()));
            e.printStackTrace();
            return new ArrayList<>();
        }
    }


    public void muteSmallNotification(JSONObject jsonObject) {
        try {
            Intent intent = null;
            String message = jsonObject.optString("message", "");
            String userName = "Topzi", userId = "";
            if (jsonObject.get(Constants.TAG_CHAT_TYPE).equals(Constants.TAG_SINGLE)) {
               /* CryptLib cryptLib = null;
                try {
                    cryptLib = new CryptLib();
                    message = cryptLib.decryptCipherTextWithRandomIV(message, "123");
                } catch (NoSuchAlgorithmException e) {
                    e.printStackTrace();
                } catch (NoSuchPaddingException e) {
                    e.printStackTrace();
                } catch (Exception e) {
                    e.printStackTrace();
                }*/
                String sender_id = jsonObject.optString("sender_id", "");
                userId = sender_id;
                if (dbhelper.isUserExist(sender_id)) {
                    ContactsData.Result results = dbhelper.getContactDetail(sender_id);
                    //  String phone_no = jsonObject.optString("phone_no", "");
                    userName = ApplicationClass.getContactName(this, results.phone_no);
                } else {
                    //userName = phone_no;
                }

                intent = new Intent(getApplicationContext(), ChatActivity.class);
                intent.putExtra("user_id", userId);
            } else if (jsonObject.get(Constants.TAG_CHAT_TYPE).equals(Constants.TAG_GROUP)) {
                String group_id = jsonObject.optString(Constants.TAG_GROUP_ID, "");
                String phone_no = jsonObject.optString(Constants.TAG_MEMBER_NO, "");
                userName = jsonObject.optString(Constants.TAG_GROUP_NAME, "");
                String name = ApplicationClass.getContactName(this, phone_no);
                message = name + " : " + message;

                intent = new Intent(getApplicationContext(), GroupChatActivity.class);
                intent.putExtra("group_id", group_id);
            } else if (jsonObject.get(Constants.TAG_CHAT_TYPE).equals(Constants.TAG_CHANNEL)) {
                userName = jsonObject.optString(Constants.TAG_CHANNEL_NAME, "");
                String channel_id = jsonObject.optString(Constants.TAG_CHANNEL_ID, "");

                intent = new Intent(getApplicationContext(), ChannelChatActivity.class);
                intent.putExtra("channel_id", channel_id);
            } else if (jsonObject.get(Constants.TAG_CHAT_TYPE).equals(Constants.TAG_GROUP_INVITATION)) {
                userName = getLocaleString(R.string.new_group);
                String group_id = jsonObject.optString(Constants.ID, "");
                message = getLocaleString(R.string.you_added_in_group) + " " + jsonObject.optString(Constants.TAG_TITLE, "");
                intent = new Intent(getApplicationContext(), MainActivity.class);
                intent.putExtra("group_id", group_id);
                intent.putExtra(Constants.IS_FROM, "group");
            } else if (jsonObject.get(Constants.TAG_CHAT_TYPE).equals(Constants.TAG_CHANNEL_INVITATION)) {
                userName = getLocaleString(R.string.new_channel_received);
                String channel_id = jsonObject.optString(Constants.ID, "");
                message = getLocaleString(R.string.invitation_received);
                intent = new Intent(getApplicationContext(), MainActivity.class);
                intent.putExtra("channel_id", channel_id);
                intent.putExtra(Constants.IS_FROM, "channel");
            }

            String appName = getString(R.string.app_name);
            Random random = new Random();
            int m = random.nextInt(9999 - 1000) + 1000;
            int uniqueInt = (int) (System.currentTimeMillis() & 0xfffffff);
            long when = System.currentTimeMillis();

            intent.putExtra("notification", "true");
            PendingIntent resultPendingIntent =
                    PendingIntent.getActivity(this, uniqueInt, intent, PendingIntent.FLAG_UPDATE_CURRENT);

            String channelId = getString(R.string.notification_channel_id);
            CharSequence channelName = getString(R.string.app_name);

            Vibrator v = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                v.vibrate(VibrationEffect.createOneShot(0, VibrationEffect.DEFAULT_AMPLITUDE));
            } else {
                //deprecated in API 26
                v.vibrate(0);
            }

            NotificationManager mNotifyManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                int importance = NotificationManager.IMPORTANCE_HIGH;
                NotificationChannel notificationChannel = new NotificationChannel(channelId, channelName, importance);
                notificationChannel.setSound(null, null);
                mNotifyManager.createNotificationChannel(notificationChannel);
            }
            NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(this, channelId);
            mBuilder.setContentTitle(userName)
                    .setChannelId(channelId)
                    .setContentText(message).setTicker(appName).setWhen(when)
                    .setStyle(new NotificationCompat.BigTextStyle().bigText(message))
                    .setContentIntent(resultPendingIntent)
                    .setSmallIcon(R.drawable.app_icon)
                    .setColor(ContextCompat.getColor(this, R.color.colorAccent))
                    .setAutoCancel(true);
            mNotifyManager.notify(userName, 0, mBuilder.build());
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public String getLocaleString(int stringId) {
        Configuration configuration = new Configuration(getApplicationContext().getResources().getConfiguration());
        configuration.setLocale(new Locale(pref.getString(Constants.TAG_LANGUAGE_CODE, Constants.TAG_DEFAULT_LANGUAGE_CODE)));
        return getApplicationContext().createConfigurationContext(configuration).getResources().getString(stringId);
    }

    private GroupMessage getGroupMessagesByType(JSONObject data) {
        GroupMessage mdata = new GroupMessage();
        try {
            JSONObject jobj = data.getJSONObject("message_data");
            mdata.groupId = jobj.optString(TAG_GROUP_ID, "");
            mdata.groupName = jobj.optString(TAG_GROUP_NAME, "");
            mdata.memberId = jobj.optString(Constants.TAG_MEMBER_ID, "");
            mdata.memberName = jobj.optString(Constants.TAG_MEMBER_NAME, "");
            mdata.memberNo = jobj.optString(Constants.TAG_MEMBER_NO, "");
            mdata.messageType = jobj.optString(Constants.TAG_MESSAGE_TYPE, "");
            mdata.message = jobj.optString(Constants.TAG_MESSAGE, "");
            mdata.messageId = jobj.optString(Constants.TAG_MESSAGE_ID, "");
            mdata.chatTime = jobj.optString(Constants.TAG_CHAT_TIME, "");
            mdata.attachment = jobj.optString(Constants.TAG_ATTACHMENT, "");
            mdata.thumbnail = jobj.optString(Constants.TAG_THUMBNAIL, "");
            mdata.lat = jobj.optString(Constants.TAG_LAT, "");
            mdata.lon = jobj.optString(Constants.TAG_LON, "");
            mdata.contactName = jobj.optString(Constants.TAG_CONTACT_NAME, "");
            mdata.contactPhoneNo = jobj.optString(Constants.TAG_CONTACT_PHONE_NO, "");
            mdata.contactCountryCode = jobj.optString(Constants.TAG_CONTACT_COUNTRY_CODE, "");
            mdata.groupAdminId = jobj.optString(TAG_GROUP_ADMIN_ID, "");

            switch (mdata.messageType) {
                case "subject":
                    dbhelper.updateGroupData(mdata.groupId, Constants.TAG_GROUP_NAME, mdata.groupName);
                    break;
                case "group_image":
                    dbhelper.updateGroupData(mdata.groupId, Constants.TAG_GROUP_IMAGE, mdata.attachment);
                    break;
                case "add_member":
                    if (!mdata.attachment.equals("")) {
                        JSONArray jsonArray = new JSONArray(mdata.attachment);
                        for (int i = 0; i < jsonArray.length(); i++) {
                            JSONObject jsonObject = jsonArray.getJSONObject(i);
                            String memberId = jsonObject.getString(Constants.TAG_MEMBER_ID);
                            String memberRole = jsonObject.getString(Constants.TAG_MEMBER_ROLE);
                            if (!dbhelper.isUserExist(memberId)) {
                                String memberKey = mdata.groupId + jsonObject.getString(TAG_MEMBER_ID);
                                getUserData(memberKey, mdata.groupId, memberId, memberRole);
                            } else {
                                String memberKey = mdata.groupId + jsonObject.getString(TAG_MEMBER_ID);
                                dbhelper.updateGroupMembers(memberKey, mdata.groupId, memberId, memberRole);
                            }
                        }
                    }
                    break;
                case "left":
                case "remove_member":
                    if (dbhelper.isUserExist(mdata.memberId))
                        dbhelper.deleteFromGroup(mdata.groupId, mdata.memberId);
                    break;
                case "admin":
                    if (!dbhelper.isUserExist(mdata.memberId)) {
                        String memberKey = mdata.groupId + mdata.memberId;
                        getUserData(memberKey, mdata.groupId, mdata.memberId, mdata.attachment);
                    } else {
                        String memberKey = mdata.groupId + mdata.memberId;
                        dbhelper.updateGroupMembers(memberKey, mdata.groupId, mdata.memberId, mdata.attachment);
                    }
                    break;
                case "change_number":
                    dbhelper.updateContactInfo(mdata.memberId, Constants.TAG_COUNTRY_CODE, mdata.contactCountryCode);
                    dbhelper.updateContactInfo(mdata.memberId, Constants.TAG_PHONE_NUMBER, mdata.contactPhoneNo);
                    break;
                default:
                    dbhelper.addGroupMessages(mdata.messageId, mdata.groupId, mdata.memberId, mdata.groupAdminId, mdata.messageType,
                            mdata.message, mdata.attachment, mdata.lat, mdata.lon, mdata.contactName, mdata.contactPhoneNo,
                            mdata.contactCountryCode, mdata.chatTime, mdata.thumbnail, "", mdata.reply_to);

                    int unseenCount = dbhelper.getUnseenGroupMessagesCount(mdata.groupId);
                    dbhelper.addGroupRecentMsgs(mdata.groupId, mdata.messageId,
                            mdata.memberId, mdata.chatTime, String.valueOf(unseenCount));
                    break;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return mdata;
    }

    private void getUserData(String memberKey, String groupId, String memberId, String
            memberRole) {
        ApiInterface apiInterface = ApiClient.getClient().create(ApiInterface.class);
        Call<UserProfileModel> call3 = apiInterface.getuserprofile(memberId);
        call3.enqueue(new Callback<UserProfileModel>() {
            @Override
            public void onResponse(Call<UserProfileModel> call, Response<UserProfileModel> response) {
                try {
                    Log.v(TAG, "getUserData: " + new Gson().toJson(response));
                    UserProfileModel userdata = response.body();
                    if (userdata != null && userdata.getSTATUS().equals("true")) {
                        dbhelper.addContactDetails(userdata.getRESULT().getId(), userdata.getRESULT().getUserName(), userdata.getRESULT().getPhoneNo(),
                                userdata.getRESULT().getCountryCode(), userdata.getRESULT().getUserImage(), userdata.getRESULT().getPrivacyAbout(),
                                userdata.getRESULT().getPrivacyLastSeen(), userdata.getRESULT().getPrivacyProfileImage(), userdata.getRESULT().getAbout(), "true");
                        dbhelper.createGroupMembers(memberKey, groupId, memberId, memberRole);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(Call<UserProfileModel> call, Throwable t) {
                Log.v(TAG, "getUserData Failed" + t.getMessage());
                call.cancel();
            }
        });
    }

    private ChannelMessage getChannelMessagesByType(JSONObject data) {
        ChannelMessage mdata = new ChannelMessage();
        try {
            JSONObject jobj = data.getJSONObject("message_data");
            mdata.channelId = jobj.optString(TAG_CHANNEL_ID, "");
            mdata.channelAdminId = jobj.optString(TAG_ADMIN_ID, "");
            mdata.channelName = jobj.optString(TAG_CHANNEL_NAME, "");
            mdata.messageType = jobj.optString(Constants.TAG_MESSAGE_TYPE, "");
            mdata.chatType = jobj.optString(Constants.TAG_CHAT_TYPE, "");
            mdata.message = jobj.optString(Constants.TAG_MESSAGE, "");
            mdata.messageId = jobj.optString(Constants.TAG_MESSAGE_ID, "");
            mdata.chatTime = jobj.optString(Constants.TAG_CHAT_TIME, "");
            mdata.attachment = jobj.optString(Constants.TAG_ATTACHMENT, "");
            mdata.thumbnail = jobj.optString(Constants.TAG_THUMBNAIL, "");
            mdata.lat = jobj.optString(Constants.TAG_LAT, "");
            mdata.lon = jobj.optString(Constants.TAG_LON, "");
            mdata.contactName = jobj.optString(Constants.TAG_CONTACT_NAME, "");
            mdata.contactPhoneNo = jobj.optString(Constants.TAG_CONTACT_PHONE_NO, "");
            mdata.contactCountryCode = jobj.optString(Constants.TAG_CONTACT_COUNTRY_CODE, "");

            if (!dbhelper.isChannelExist(mdata.channelId)) {
                getChannelInfo(mdata.channelId);
            }

            switch (mdata.messageType) {
                case "subject":
                    dbhelper.updateChannelData(mdata.channelId, Constants.TAG_CHANNEL_NAME, mdata.channelName);
                    break;
                case "channel_image":
                    dbhelper.updateChannelData(mdata.channelId, Constants.TAG_CHANNEL_IMAGE, mdata.attachment);
                    break;
                case "channel_des":
                    dbhelper.updateChannelData(mdata.channelId, Constants.TAG_CHANNEL_DES, mdata.message);
                    break;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return mdata;
    }

    private void getChannelInfo(String channelId) {
        JSONArray jsonArray = new JSONArray();
        jsonArray.put(channelId);
        ApiInterface apiInterface = ApiClient.getClient().create(ApiInterface.class);
//        Log.i(TAG, "initChannel: " + jsonArray);
        Call<ChannelResult> call = apiInterface.getChannelInfo(GetSet.getToken(), jsonArray);
        call.enqueue(new Callback<ChannelResult>() {
            @Override
            public void onResponse(Call<ChannelResult> call, Response<ChannelResult> response) {
                Log.i(TAG, "getChannelInfo: " + new Gson().toJson(response));
                if (response.body().status != null && response.body().status.equalsIgnoreCase(Constants.TRUE)) {
                    for (ChannelResult.Result result : response.body().result) {
                        dbhelper.addChannel(result.channelId, result.channelName, result.channelDes, result.channelImage,
                                result.channelType, result.channelAdminId, result.channelAdminName, result.totalSubscribers, result.createdAt, Constants.TAG_USER_CHANNEL, "", result.blockStatus);
                    }
                }
            }

            @Override
            public void onFailure(Call<ChannelResult> call, Throwable t) {
                Log.e(TAG, "getChannelInfo: " + t.getMessage());
                call.cancel();
            }
        });
    }
}
package com.topzi.chat.helper;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.IBinder;
import android.provider.ContactsContract;

import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;

import android.util.Log;

import com.topzi.chat.model.UserProfileModel;
import com.google.gson.Gson;
import com.google.i18n.phonenumbers.NumberParseException;
import com.google.i18n.phonenumbers.PhoneNumberUtil;
import com.google.i18n.phonenumbers.Phonenumber;
import com.topzi.chat.external.RandomString;
import com.topzi.chat.R;
import com.topzi.chat.activity.WelcomeActivity;
import com.topzi.chat.model.AdminChannel;
import com.topzi.chat.model.AdminChannelMsg;
import com.topzi.chat.model.BlocksData;
import com.topzi.chat.model.CallData;
import com.topzi.chat.model.ChannelChatResult;
import com.topzi.chat.model.ChannelMessage;
import com.topzi.chat.model.ChannelResult;
import com.topzi.chat.model.ContactsData;
import com.topzi.chat.model.GroupChatResult;
import com.topzi.chat.model.GroupData;
import com.topzi.chat.model.GroupInvite;
import com.topzi.chat.model.GroupMessage;
import com.topzi.chat.model.MessagesData;
import com.topzi.chat.model.RecentsData;
import com.topzi.chat.model.SaveMyContacts;
import com.topzi.chat.model.contacts.ContactsModel;
import com.topzi.chat.utils.ApiClient;
import com.topzi.chat.utils.ApiInterface;
import com.topzi.chat.utils.Constants;
import com.topzi.chat.utils.GetSet;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static android.Manifest.permission.READ_CONTACTS;
import static com.topzi.chat.utils.Constants.TAG_MEMBER_ID;

/**
 * Created on 5/7/18.
 */

public class ForegroundService extends Service {

    private static final String LOG_TAG = "ForegroundService";
    public static boolean IS_SERVICE_RUNNING = false;
    Thread recentChatThread, getBlockThread, checkDevice, groupInvitesThread, recenGroupChatThread, saveContacts,
            recentCallThread, recentChannelChatThread, adminChannelThread, channelInvitesThread;
    ApiInterface apiInterface;
    DatabaseHandler dbhelper;
    SocketConnection socketConnection;
    NotificationManager mNotifyManager;
    NotificationCompat.Builder mBuilder;
    int count = 10;
    Context context = this;
    List<String> myContacts = new ArrayList<>();

    @Override
    public void onCreate() {
        Log.v(LOG_TAG, "onCreate");
        super.onCreate();
        mNotifyManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        String channelId = getString(R.string.notification_channel_foreground_service);
        CharSequence channelName = getString(R.string.app_name);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel notificationChannel = new NotificationChannel(channelId, channelName, NotificationManager.IMPORTANCE_DEFAULT);
            notificationChannel.enableVibration(false);
            notificationChannel.setSound(null, null);
            mNotifyManager.createNotificationChannel(notificationChannel);
        }
        mBuilder = new NotificationCompat.Builder(this, channelId);
        mBuilder.setContentTitle(getString(R.string.app_name))
                .setContentText("Checking new messages")
                .setSmallIcon(R.drawable.change_camera);
        startForeground(1, mBuilder.build());
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null && intent.getAction().equals("start")) {
            IS_SERVICE_RUNNING = true;
            Log.v(LOG_TAG, "Received Start Foreground Intent ");
            //Toast.makeText(this, "Service Started!", Toast.LENGTH_SHORT).show();
            apiInterface = ApiClient.getClient().create(ApiInterface.class);
            dbhelper = DatabaseHandler.getInstance(this);
            socketConnection = SocketConnection.getInstance(this);

            getBlockThread = new Thread(new Runnable() {
                @Override
                public void run() {
                    getblockstatus();
                }
            });
            getBlockThread.start();

            saveContacts = new Thread(new Runnable() {
                @Override
                public void run() {
                    if (checkCallPermission()) {
                        saveMyContacts();
                    }
                }
            });
            saveContacts.start();

            checkDevice = new Thread(new Runnable() {
                @Override
                public void run() {
                    checkDeviceInfo();
                }
            });
            checkDevice.start();

            recentChatThread = new Thread(new Runnable() {
                @Override
                public void run() {
                    recentchats();
                }
            });
            recentChatThread.start();

            groupInvitesThread = new Thread(new Runnable() {
                @Override
                public void run() {
                    getGroupInvites();
                }
            });
            groupInvitesThread.start();

            recenGroupChatThread = new Thread(new Runnable() {
                @Override
                public void run() {
                    recentgroupchats();
                }
            });
            recenGroupChatThread.start();

            recentCallThread = new Thread(new Runnable() {
                @Override
                public void run() {
                    recentCalls();
                }
            });
            recentCallThread.start();

        } else if (intent == null || intent.getAction().equals("stop")) {
            IS_SERVICE_RUNNING = false;
            Log.i(LOG_TAG, "Received Stop Foreground Intent");
            stopForeground(true);
            stopSelf();
        }
        return START_STICKY;
    }

    private void stopService() {
        count--;
        Log.v("service", "count=" + count);
        if (count == 0) {
            IS_SERVICE_RUNNING = false;
            stopForeground(true);
            stopSelf();
        }
    }

    void recentchats() {
        Call<RecentsData> call3 = apiInterface.recentchats(GetSet.getToken(), GetSet.getUserId());
        call3.enqueue(new Callback<RecentsData>() {
            @Override
            public void onResponse(Call<RecentsData> call, Response<RecentsData> response) {
                try {
                    Log.v("response", "response=" + new Gson().toJson(response));
                    RecentsData data = response.body();
                    if (data != null && data.status.equals("true")) {
                        ArrayList<MessagesData> result = data.result;
                        for (int i = 0; i < result.size(); i++) {
                            MessagesData mdata = result.get(i);
                            if (mdata.user_id != null) {
                                dbhelper.addMessageDatas(GetSet.getUserId() + mdata.user_id, mdata.message_id, mdata.user_id, "",
                                        mdata.message_type, mdata.message, mdata.attachment, mdata.lat, mdata.lon, mdata.contact_name, mdata.contact_phone_no,
                                        mdata.contact_country_code, mdata.chat_time, GetSet.getUserId(), mdata.user_id, "sent", mdata.thumbnail, mdata.reply_to, mdata.groupId);

                                if (!dbhelper.isUserExist(mdata.user_id)) {
                                    getuserprofile(mdata);
                                } else {
                                    setMessagesnListener(mdata);
                                }
                            }
                        }
                        socketConnection.setRecentListener();
                    } else {

                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                stopService();
            }

            @Override
            public void onFailure(Call<RecentsData> call, Throwable t) {
                Log.v("Contacts Failed", "TEST" + t.getMessage());
                call.cancel();
                stopService();
            }
        });
    }

    private void setMessagesnListener(MessagesData mdata) {
        try {
            int unseenCount = dbhelper.getUnseenMessagesCount(mdata.user_id);
            Log.v("unseenCount", "unseenCount=" + unseenCount);
            dbhelper.addRecentMessages(GetSet.getUserId() + mdata.user_id, mdata.user_id, mdata.message_id, mdata.chat_time, String.valueOf(unseenCount));

            // To acknowledge the message has been delivered
            JSONObject jsonObject = new JSONObject();
            jsonObject.put(Constants.TAG_FRIENDID, mdata.user_id);
            jsonObject.put(Constants.TAG_USER_ID, GetSet.getUserId());
            jsonObject.put(Constants.TAG_MESSAGE_ID, mdata.message_id);
            Log.v("chatreceivedFore", "=" + jsonObject);
            socketConnection.chatReceived(jsonObject);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    void getuserprofile(final MessagesData mdata) {

        ApiInterface apiInterface = ApiClient.getClient().create(ApiInterface.class);
        Call<UserProfileModel> call3 = apiInterface.getuserprofile(mdata.user_id);
        call3.enqueue(new Callback<UserProfileModel>() {
            @Override
            public void onResponse(Call<UserProfileModel> call, Response<UserProfileModel> response) {
                try {
                    Log.v("response", "response=" + new Gson().toJson(response));
                    UserProfileModel userdata = response.body();
                    if (userdata != null && userdata.getSTATUS().equals("true")) {
                        dbhelper.addContactDetails(userdata.getRESULT().getId(), userdata.getRESULT().getUserName(), userdata.getRESULT().getPhoneNo(),
                                userdata.getRESULT().getCountryCode(), userdata.getRESULT().getUserImage(), userdata.getRESULT().getPrivacyAbout(),
                                userdata.getRESULT().getPrivacyLastSeen(), userdata.getRESULT().getPrivacyProfileImage(), userdata.getRESULT().getAbout(), "true");

                        setMessagesnListener(mdata);
                    } else {

                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(Call<UserProfileModel> call, Throwable t) {
                Log.v("Contacts Failed", "TEST" + t.getMessage());
                call.cancel();
            }
        });

    }

    void saveMyContacts() {
        myContacts = getMyContacts();
        HashMap<String, String> map = new HashMap<>();
        map.put(Constants.TAG_USER_ID, GetSet.getUserId());
        map.put(Constants.TAG_CONTACTS, "" + myContacts);
        Log.v("ContactsSave", "saveMyContacts=" + myContacts);
        Call<SaveMyContacts> call = apiInterface.saveMyContacts(GetSet.getToken(), map);
        call.enqueue(new Callback<SaveMyContacts>() {
            @Override
            public void onResponse(Call<SaveMyContacts> call, Response<SaveMyContacts> response) {
                updatemycontacts();
            }

            @Override
            public void onFailure(Call<SaveMyContacts> call, Throwable t) {
                Log.e(LOG_TAG, "saveMyContacts: " + t.getMessage());
                call.cancel();
                stopService();
            }
        });

    }

    public List<String> getMyContacts() {
        List<String> contactsNum = new ArrayList<>();
        Uri uri = null;
        uri = ContactsContract.CommonDataKinds.Contactables.CONTENT_URI;
        ContentResolver cr = getContentResolver();
        Cursor cur = cr.query(uri, Constants.PROJECTION, Constants.SELECTION, Constants.SELECTION_ARGS, null);

        if (cur != null) {
            try {
                final int nameIndex = cur.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME);
                final int numberIndex = cur.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER);

                while (cur.moveToNext()) {
                    String phoneNo = cur.getString(numberIndex).replace(" ", "");
                    String name = cur.getString(nameIndex);

                    try {
                        PhoneNumberUtil phoneUtil = PhoneNumberUtil.getInstance();
                        Phonenumber.PhoneNumber numberProto = phoneUtil.parse(phoneNo, Locale.getDefault().getCountry());
                        if (phoneNo != null && !phoneNo.equals("") && phoneNo.length() > 6 && phoneUtil.isPossibleNumberForType(numberProto, PhoneNumberUtil.PhoneNumberType.MOBILE)) {
                            String tempNo = ("" + numberProto.getNationalNumber());
                            if (tempNo.startsWith("0")) {
                                tempNo = tempNo.replaceFirst("^0+(?!$)", "");
                            }
                            contactsNum.add(tempNo.replaceAll("[^0-9]", ""));
                        }
                    } catch (NumberParseException e) {
                        if (isValidPhoneNumber(phoneNo)) {
                            if (phoneNo.startsWith("0")) {
                                phoneNo = phoneNo.replaceFirst("^0+(?!$)", "");
                            }
//                            Log.e("LLLL_Name", "excep name=" + name);
                            contactsNum.add(phoneNo.replaceAll("[^0-9]", ""));
                        }
                    }
                }
            } finally {
                cur.close();
            }
        }

        /*ContentResolver cr = getContentResolver();
        Cursor cur = cr.query(ContactsContract.Contacts.CONTENT_URI,
                null, null, null, null);

        if ((cur != null ? cur.getCount() : 0) > 0) {
            while (cur.moveToNext()) {
                String id = cur.getString(
                        cur.getColumnIndex(ContactsContract.Contacts._ID));
                if (cur.getInt(cur.getColumnIndex(
                        ContactsContract.Contacts.HAS_PHONE_NUMBER)) > 0) {
                    Cursor pCur = cr.query(
                            ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                            null,
                            ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = ?",
                            new String[]{id}, null);
                    while (pCur.moveToNext()) {
                        String phoneNo = pCur.getString(pCur.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)).replace(" ", "");
                        try {
                            PhoneNumberUtil phoneUtil = PhoneNumberUtil.getInstance();
                            Phonenumber.PhoneNumber numberProto = phoneUtil.parse(phoneNo, Locale.getDefault().getCountry());
                            if (phoneNo != null && !phoneNo.equals("") && phoneNo.length() > 6 && phoneUtil.isPossibleNumberForType(numberProto, PhoneNumberUtil.PhoneNumberType.MOBILE)) {
                                contactsNum.add("" + numberProto.getNationalNumber());
                            }
                        } catch (NumberParseException e) {
                            if (isValidPhoneNumber(phoneNo)) {
                                contactsNum.add(phoneNo);
                            }
                        }
                    }

                    pCur.close();
                }
            }
//            Log.i(LOG_TAG, "getMyContacts: " + contactsNum);
        }
        if (cur != null) {
            cur.close();
        }*/

        Log.i(LOG_TAG, "getMyContacts: " + contactsNum.size());
        return contactsNum;
    }

    public boolean isValidPhoneNumber(CharSequence target) {
        if (target.length() < 7 || target.length() > 15) {
            return false;
        } else {
            return android.util.Patterns.PHONE.matcher(target).matches();
        }
    }

    void updatemycontacts() {
        List<String> contacts = new ArrayList<>();
        List<ContactsModel> contactsNum = new ArrayList<>();
        contacts = dbhelper.getAllContactsNumber(this);
        for (String contact : contacts) {
            if (!myContacts.contains(contact)) {
                ContactsModel contactsModel = new ContactsModel();
                contactsModel.setNumber(contact.replaceAll("[^0-9]", ""));
                contactsNum.add(contactsModel);
//                myContacts.add(contact.replaceAll("[^0-9]", ""));
            }
        }
//        HashMap<String, String> map = new HashMap<>();
//        map.put(Constants.TAG_USER_ID, GetSet.getUserId());
//        map.put(Constants.TAG_CONTACTS, "" + myContacts);
//        map.put(Constants.TAG_PHONE_NUMBER, GetSet.getphonenumber());
        Gson gson = new Gson();
        String contact = gson.toJson(contactsNum);

        Log.e("LLLLLL_Contact: ", "updateMyContacts: " + contact);
        Call<ContactsData> call3 = apiInterface.updatemycontacts(GetSet.getToken(), GetSet.getUserId(), GetSet.getphonenumber(), contact);
        call3.enqueue(new Callback<ContactsData>() {
            @Override
            public void onResponse(Call<ContactsData> call, Response<ContactsData> response) {
                try {
                    Log.i(LOG_TAG, "updateMyContacts: " + new Gson().toJson(response));
                    ContactsData data = response.body();
                    if (data.status.equals("true")) {
                        for (ContactsData.Result result : data.result) {
//                            Log.e("LLLLL_user: ", String.valueOf(result));
                            dbhelper.addContactDetails(result.user_id, result.user_name, result.phone_no, result.country_code, result.user_image, result.privacy_about,
                                    result.privacy_last_scene, result.privacy_profile_image, result.about, result.contactstatus);
                        }
                        socketConnection.setRecentListener();
                    }
                } catch (Exception e) {
                    Log.e("LLLLL_Exception: ", e.getMessage());
                    e.printStackTrace();
                }
                stopService();
            }

            @Override
            public void onFailure(Call<ContactsData> call, Throwable t) {
//                Log.e(LOG_TAG, "updatemycontacts: " + t.getMessage());
                call.cancel();
                stopService();
            }
        });

    }

    void getblockstatus() {
        Call<BlocksData> call3 = apiInterface.getblockstatus(GetSet.getUserId());
        call3.enqueue(new Callback<BlocksData>() {
            @Override
            public void onResponse(Call<BlocksData> call, Response<BlocksData> response) {
                try {
                    Log.v("response", "response=" + new Gson().toJson(response));
                    BlocksData data = response.body();
                    if (data != null && data.getSTATUS() != null && data.getSTATUS().equals("true")) {
                        List<BlocksData.BlockedMe> blockedme = data.getRESULT().getBlockedMe();
                        if (blockedme.size() == 0) {
                            dbhelper.resetAllBlockStatus(Constants.TAG_BLOCKED_ME);
                        } else {
                            for (int i = 0; i < blockedme.size(); i++) {
                                BlocksData.BlockedMe block = blockedme.get(i);
                                dbhelper.updateBlockStatus(block.getUserId(), Constants.TAG_BLOCKED_ME, "block");
                            }
                        }

                        List<BlocksData.BlockedByMe> blockedbyme = data.getRESULT().getBlockedByMe();
                        if (blockedbyme.size() == 0) {
                            dbhelper.resetAllBlockStatus(Constants.TAG_BLOCKED_BYME);
                        } else {
                            for (int i = 0; i < blockedbyme.size(); i++) {
                                BlocksData.BlockedByMe block = blockedbyme.get(i);
                                dbhelper.updateBlockStatus(block.getUserId(), Constants.TAG_BLOCKED_BYME, "block");
                            }
                        }
                    } else {

                    }

                } catch (Exception e) {
                    e.printStackTrace();
                }

                stopService();
            }

            @Override
            public void onFailure(Call<BlocksData> call, Throwable t) {
                Log.v("Contacts Failed", "TEST" + t.getMessage());
                call.cancel();
                stopService();
            }
        });
    }

    void checkDeviceInfo() {
        final String deviceId = android.provider.Settings.Secure.getString(getApplicationContext().getContentResolver(),
                android.provider.Settings.Secure.ANDROID_ID);

        Map<String, String> map = new HashMap<>();
        map.put("user_id", GetSet.getUserId());
        map.put("device_id", deviceId);
        Log.v("checkDeviceInfo", "Params- " + map);
        Call<Map<String, String>> call3 = apiInterface.deviceinfo(GetSet.getToken(), map);
        call3.enqueue(new Callback<Map<String, String>>() {
            @Override
            public void onResponse(Call<Map<String, String>> call, Response<Map<String, String>> response) {
                Map<String, String> data = response.body();
                Log.v("checkDeviceInfo:", "response- " + data);
                if (data != null && data.get(Constants.TAG_STATUS).equals("false")) {
                    GetSet.logout();
                    SharedPreferences settings = getSharedPreferences("SavedPref", Context.MODE_PRIVATE);
                    settings.edit().clear().commit();
                    Intent logout = new Intent(getApplicationContext(), WelcomeActivity.class);
                    logout.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(logout);
                }
                stopService();
            }

            @Override
            public void onFailure(Call<Map<String, String>> call, Throwable t) {
                call.cancel();
                stopService();
            }
        });

    }

    private void getGroupInvites() {
        final ApiInterface apiInterface = ApiClient.getClient().create(ApiInterface.class);
        Call<GroupInvite> call3 = apiInterface.getGroupInvites(GetSet.getToken(), GetSet.getUserId());
        call3.enqueue(new Callback<GroupInvite>() {
            @Override
            public void onResponse(Call<GroupInvite> call, Response<GroupInvite> response) {
                try {
                    Log.v("GroupInvite", "GroupInvite=" + new Gson().toJson(response));
                    GroupInvite userdata = response.body();
                    if (userdata.status.equalsIgnoreCase(Constants.TRUE)) {

                        for (GroupData groupData : userdata.result) {
                            if (!dbhelper.isGroupExist(groupData.groupId)) {
                                dbhelper.createGroup(groupData.groupId, groupData.groupAdminId,
                                        groupData.groupName, groupData.createdAt, groupData.groupImage);

                                for (GroupData.GroupMembers groupMember : groupData.groupMembers) {
                                    if (!dbhelper.isUserExist(groupMember.memberId)) {
                                        String memberKey = groupData.groupId + groupMember.memberId;
                                        getUserData(memberKey, groupData.groupId, groupMember.memberId, groupMember.memberRole);
                                    } else {
                                        String memberKey = groupData.groupId + groupMember.memberId;
                                        dbhelper.createGroupMembers(memberKey, groupData.groupId, groupMember.memberId,
                                                groupMember.memberRole);
                                    }
                                }

                                String unixStamp = String.valueOf(System.currentTimeMillis() / 1000L);
                                RandomString randomString = new RandomString(10);
                                String messageId = groupData.groupId + randomString.nextString();

                                dbhelper.addGroupMessages(messageId, groupData.groupId, GetSet.getUserId(), groupData.groupAdminId, "create_group",
                                        "", "", "", "", "", "", "",
                                        groupData.createdAt, "", "", "");
                                int unseenCount = dbhelper.getUnseenGroupMessagesCount(groupData.groupId);
                                dbhelper.addGroupRecentMsgs(groupData.groupId, messageId, GetSet.getUserId(), unixStamp, "" + unseenCount);

                                if (!groupData.groupAdminId.equals(GetSet.getUserId())) {
                                    String unixStamp2 = String.valueOf(System.currentTimeMillis() / 1000L);
                                    String messageId2 = groupData.groupId + randomString.nextString();
                                    dbhelper.addGroupMessages(messageId2, groupData.groupId, GetSet.getUserId(), groupData.groupAdminId, "add_member",
                                            "", "", "", "",
                                            "", "", "", groupData.createdAt, "", "", "");
                                    unseenCount = dbhelper.getUnseenGroupMessagesCount(groupData.groupId);
                                    dbhelper.addGroupRecentMsgs(groupData.groupId, messageId, GetSet.getUserId(), unixStamp, "" + unseenCount);
                                }
                            }
                            try {
                                JSONObject jobj = new JSONObject();
                                jobj.put(Constants.TAG_GROUP_ID, groupData.groupId);
                                jobj.put(Constants.TAG_MEMBER_ID, GetSet.getUserId());
                                socketConnection.joinGroup(jobj);
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                        socketConnection.setRecentGroupListener();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                stopService();
            }

            @Override
            public void onFailure(Call<GroupInvite> call, Throwable t) {
                Log.e(LOG_TAG, "getGroupInvites " + t.getMessage());
                call.cancel();
                stopService();
            }
        });
    }

    void recentgroupchats() {
        Call<GroupChatResult> call3 = apiInterface.getRecentGroupChats(GetSet.getToken(), GetSet.getUserId());
        call3.enqueue(new Callback<GroupChatResult>() {
            @Override
            public void onResponse(Call<GroupChatResult> call, Response<GroupChatResult> response) {
                try {
                    Log.v(LOG_TAG, "recentGroupChats=" + new Gson().toJson(response));
                    GroupChatResult data = response.body();
                    if (data.status.equals("true")) {
                        List<GroupMessage> result = data.result;
                        for (int g = 0; g < result.size(); g++) {
                            GroupMessage mdata = result.get(g);
                            if (mdata.memberId != null) {

                                switch (mdata.messageType) {
                                    case "subject":
                                        dbhelper.updateGroupData(mdata.groupId, Constants.TAG_GROUP_NAME, mdata.groupName);
                                        socketConnection.updateGroupInfo(mdata);
                                        break;
                                    case "group_image":
                                        dbhelper.updateGroupData(mdata.groupId, Constants.TAG_GROUP_IMAGE, mdata.attachment);
                                        socketConnection.updateGroupInfo(mdata);
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
                                        socketConnection.updateGroupInfo(mdata);
                                        break;
                                    case "left":
                                    case "remove_member":
                                        if (dbhelper.isUserExist(mdata.memberId))
                                            dbhelper.deleteFromGroup(mdata.groupId, mdata.memberId);
                                        socketConnection.updateGroupInfo(mdata);
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
                                }

                                if (mdata.memberId.equalsIgnoreCase(GetSet.getUserId()) && (mdata.messageType.equals("text") || mdata.messageType.equals("image") ||
                                        mdata.messageType.equals("audio") || mdata.messageType.equals("voice") || mdata.messageType.equals("video") || mdata.messageType.equals("document") ||
                                        mdata.messageType.equals("location") || mdata.messageType.equals("contact"))) {


                                } else if (!mdata.memberId.equalsIgnoreCase(GetSet.getUserId()) && mdata.messageType.equals("admin")) {

                                } else if (mdata.messageType.equalsIgnoreCase("remove_member") && (!dbhelper.isUserExist(mdata.memberId))) {

                                } else {

                                    dbhelper.addGroupMessages(mdata.messageId, mdata.groupId, mdata.memberId, mdata.groupAdminId, mdata.messageType,
                                            mdata.message, mdata.attachment, mdata.lat, mdata.lon,
                                            mdata.contactName, mdata.contactPhoneNo, mdata.contactCountryCode, mdata.chatTime, mdata.thumbnail, "", mdata.reply_to);

                                    int unseenCount = dbhelper.getUnseenGroupMessagesCount(mdata.groupId);
                                    dbhelper.addGroupRecentMsgs(mdata.groupId, mdata.messageId,
                                            mdata.memberId, mdata.chatTime, String.valueOf(unseenCount));
                                }

                                try {
                                    JSONObject json = new JSONObject();
                                    json.put("user_id", GetSet.getUserId());
                                    json.put("group_id", mdata.groupId);
                                    json.put("chat_id", mdata.chatId);
                                    socketConnection.groupChatReceived(json);
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }

                            }
                        }
                        socketConnection.setRecentGroupListener();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                stopService();
            }

            @Override
            public void onFailure(Call<GroupChatResult> call, Throwable t) {
                Log.v("Contacts Failed", "TEST" + t.getMessage());
                call.cancel();
                stopService();
            }
        });
    }

    void recentCalls() {
        Call<CallData> call3 = apiInterface.recentcalls(GetSet.getUserId());
        call3.enqueue(new Callback<CallData>() {
            @Override
            public void onResponse(Call<CallData> call, Response<CallData> response) {
                try {
                    Log.v("response", "response=" + new Gson().toJson(response));
                    CallData data = response.body();
                    if (data.status.equals("true")) {
                        List<CallData.Result> result = data.result;
                        for (int i = 0; i < result.size(); i++) {
                            CallData.Result mdata = result.get(i);
                            if (mdata.callerId != null) {
                                dbhelper.addRecentCall(mdata.callId, mdata.callerId, mdata.type, mdata.callStatus, mdata.createdAt);

                                if (!dbhelper.isUserExist(mdata.callerId)) {
                                    getUserInfo(mdata.callerId);
                                }
                            }
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                stopService();
            }

            @Override
            public void onFailure(Call<CallData> call, Throwable t) {
                Log.v("Contacts Failed", "TEST" + t.getMessage());
                call.cancel();
                stopService();
            }
        });
    }

    private void getUserData(String memberKey, String groupId, String memberId, String memberRole) {
        ApiInterface apiInterface = ApiClient.getClient().create(ApiInterface.class);
        Call<UserProfileModel> call3 = apiInterface.getuserprofile(memberId);
        call3.enqueue(new Callback<UserProfileModel>() {
            @Override
            public void onResponse(Call<UserProfileModel> call, Response<UserProfileModel> response) {
                try {
                    Log.v(LOG_TAG, "getUserData: " + new Gson().toJson(response));
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
                Log.v(LOG_TAG, "getUserData Failed" + t.getMessage());
                call.cancel();
            }
        });
    }

    private void getUserInfo(String memberId) {
        ApiInterface apiInterface = ApiClient.getClient().create(ApiInterface.class);
        Call<UserProfileModel> call3 = apiInterface.getuserprofile(memberId);
        call3.enqueue(new Callback<UserProfileModel>() {
            @Override
            public void onResponse(Call<UserProfileModel> call, Response<UserProfileModel> response) {
                try {
                    Log.v(LOG_TAG, "getUserInfo: " + new Gson().toJson(response));
                    UserProfileModel userdata = response.body();
                    if (userdata != null && userdata.getSTATUS().equals("true")) {
                        dbhelper.addContactDetails(userdata.getRESULT().getId(), userdata.getRESULT().getUserName(), userdata.getRESULT().getPhoneNo(),
                                userdata.getRESULT().getCountryCode(), userdata.getRESULT().getUserImage(), userdata.getRESULT().getPrivacyAbout(),
                                userdata.getRESULT().getPrivacyLastSeen(), userdata.getRESULT().getPrivacyProfileImage(), userdata.getRESULT().getAbout(), "true");

                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(Call<UserProfileModel> call, Throwable t) {
                Log.v("Contacts Failed", "TEST" + t.getMessage());
                call.cancel();
            }
        });
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.v(LOG_TAG, "In onDestroy");
    }

    @Override
    public IBinder onBind(Intent intent) {
        // Used only in case if services are bound (Bound Services).
        return null;
    }

    private boolean checkCallPermission() {
        int permissionContacts = ContextCompat.checkSelfPermission(context,
                READ_CONTACTS);
        return permissionContacts == PackageManager.PERMISSION_GRANTED;
    }
}

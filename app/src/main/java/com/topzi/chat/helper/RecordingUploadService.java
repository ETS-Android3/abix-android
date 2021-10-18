package com.topzi.chat.helper;

import android.app.IntentService;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.topzi.chat.model.ChannelMessage;
import com.topzi.chat.model.GroupImageModel;
import com.topzi.chat.model.GroupMessage;
import com.topzi.chat.model.MessagesData;
import com.topzi.chat.model.UpMyChatModel;
import com.topzi.chat.utils.ApiClient;
import com.topzi.chat.utils.ApiInterface;
import com.topzi.chat.utils.Constants;
import com.topzi.chat.utils.GetSet;
import com.google.gson.Gson;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.Map;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RecordingUploadService extends IntentService implements ProgressRequestBody.UploadCallbacks {

    private static final String LOG_TAG = "RecordingUploadService";
    DatabaseHandler dbhelper;
    SocketConnection socketConnection;
    StorageManager storageManager;
    MessagesData mdata;
    GroupMessage gdata;
    ChannelMessage chdata;
    String filepath, chatType;
    long startTime;
    long elapsedTime = 0L;

    public RecordingUploadService() {
        super("RecordingUploadService");
    }

    @Override
    public void onCreate() {
        Log.v(LOG_TAG, "onCreate");
        super.onCreate();
        /*mNotifyManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        String channelId = getString(R.string.notification_channel_foreground_service);
        CharSequence channelName = getString(R.string.app_name);
        int importance = NotificationManager.IMPORTANCE_HIGH;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel notificationChannel = new NotificationChannel(channelId, channelName, importance);
            mNotifyManager.createNotificationChannel(notificationChannel);
        }
        mBuilder = new NotificationCompat.Builder(this, channelId);
        mBuilder.setContentTitle(getString(R.string.app_name))
                .setContentText("Uploading..")
                .setSmallIcon(R.drawable.temp)
                .setOnlyAlertOnce(true);
        mNotifyManager.notify(2, mBuilder.build());*/
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Log.v(LOG_TAG, "onHandleIntent");
        dbhelper = DatabaseHandler.getInstance(this);
        socketConnection = SocketConnection.getInstance(this);
        storageManager = StorageManager.getInstance(this);

        Bundle bundle = intent.getExtras();
        filepath = bundle.getString("filepath");
        chatType = bundle.getString("chatType");

        if (chatType.equals("chat") || chatType.equals(Constants.status)) {
            mdata = (MessagesData) bundle.getSerializable("mdata");

            ProgressRequestBody fileBody = new ProgressRequestBody(new File(filepath), this);
            MultipartBody.Part filePart = MultipartBody.Part.createFormData("attachment", new File(filepath).getName(), fileBody);

            ApiInterface apiInterface = ApiClient.getClient().create(ApiInterface.class);
            RequestBody userid = RequestBody.create(MediaType.parse("multipart/form-data"), GetSet.getUserId());
            Call<UpMyChatModel> call3 = apiInterface.upmychat(GetSet.getToken(), filePart, userid);
//            try {
            call3.enqueue(new Callback<UpMyChatModel>() {
                @Override
                public void onResponse(Call<UpMyChatModel> call, Response<UpMyChatModel> response) {
                    if (response.isSuccessful() && response.body() != null && response.body().getStatus().equals("true")) {
                        try {
                            Log.v("uploadzz", "response=" + response.body());
                            Log.v("uploadzz", "filepath=" + filepath);
                            UpMyChatModel userdata = response.body();
                            if (userdata.getStatus().equals("true")) {
                                try {
                                    Log.v("mdata", "mdata=" + mdata.message_type);
                                    boolean fileStatus = storageManager.moveFilesToSentPath(RecordingUploadService.this, mdata.message_type, filepath, userdata.getResult().getImage());
                                    if (fileStatus) {
//                                    JSONObject jobj = new JSONObject();
                                        JSONObject message = new JSONObject();
                                        message.put(Constants.TAG_USER_ID, mdata.user_id);
                                        message.put(Constants.TAG_USER_NAME, mdata.user_name);
                                        message.put(Constants.TAG_MESSAGE_TYPE, mdata.message_type);
                                        message.put(Constants.TAG_ATTACHMENT, userdata.getResult().getImage());
                                        message.put(Constants.TAG_THUMBNAIL, mdata.thumbnail);
                                        message.put(Constants.TAG_MESSAGE, mdata.message);
                                        message.put(Constants.TAG_CHAT_TIME, String.valueOf(System.currentTimeMillis() / 1000L));

                                        if (!chatType.equals(Constants.status)) {
                                            message.put(Constants.TAG_CHAT_ID, mdata.chat_id);
                                            message.put(Constants.TAG_MESSAGE_ID, mdata.message_id);
                                            message.put(Constants.TAG_FRIENDID, mdata.receiver_id);
                                            message.put(Constants.TAG_SENDER_ID, mdata.user_id);
                                            message.put(Constants.TAG_CHAT_TYPE, Constants.TAG_SINGLE);
                                            Log.v("checkChat", "startchat=" + message);
                                            socketConnection.startChat(message);

                                            dbhelper.updateMessageData(mdata.message_id, Constants.TAG_ATTACHMENT, userdata.getResult().getImage());
                                            dbhelper.updateMessageData(mdata.message_id, Constants.TAG_PROGRESS, "completed");

                                            /*mBuilder.setProgress(0, 0, false);
                                            mBuilder.setContentText("File uploaded");
                                            mNotifyManager.notify(2, mBuilder.build());*/

                                            socketConnection.setUploadingListen(chatType, mdata.message_id, userdata.getResult().getImage(), "completed");
                                        } else {
                                            message.put(Constants.TAG_STATUS_ID, mdata.message_id);
                                            Log.v("checkChat", "startchat=" + message);
                                            socketConnection.statusAdd(message);

                                            dbhelper.updateStatusData(mdata.message_id, Constants.TAG_ATTACHMENT, userdata.getResult().getImage());
                                            dbhelper.updateStatusData(mdata.message_id, Constants.TAG_PROGRESS, "completed");

                                            /*mBuilder.setProgress(0, 0, false);
                                            mBuilder.setContentText("File uploaded");
                                            mNotifyManager.notify(2, mBuilder.build());*/

                                            socketConnection.setUploadingListen(chatType, mdata.message_id, userdata.getResult().getImage(), "completed");
                                        }
                                    }
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                    setErrorUpload();
                                }
                            } else {
                                setErrorUpload();
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                            setErrorUpload();
                        }
                    } else {
                        setErrorUpload();
                    }
                }

                @Override
                public void onFailure(Call<UpMyChatModel> call, Throwable t) {
                    setErrorUpload();
                }
            });
//            } catch (IOException e) {
//                e.printStackTrace();
//                setErrorUpload();
//            }
        } else if (chatType.equals("group")) {
            gdata = (GroupMessage) bundle.getSerializable("mdata");

            ProgressRequestBody fileBody = new ProgressRequestBody(new File(filepath), this);
            MultipartBody.Part filePart = MultipartBody.Part.createFormData("group_attachment", new File(filepath).getName(), fileBody);

            ApiInterface apiInterface = ApiClient.getClient().create(ApiInterface.class);
            RequestBody userid = RequestBody.create(MediaType.parse("multipart/form-data"), GetSet.getUserId());
            Call<GroupImageModel> call3 = apiInterface.upMyGroupChat(filePart, userid, null);
            call3.enqueue(new Callback<GroupImageModel>() {
                @Override
                public void onResponse(Call<GroupImageModel> call, Response<GroupImageModel> response) {
                    GroupImageModel data = response.body();
                    if (data != null && data.getSTATUS().equalsIgnoreCase(Constants.TRUE)) {
                        try {
                            Log.v("gdata", "gdata=" + gdata.messageType);
                            boolean fileStatus = storageManager.moveFilesToSentPath(RecordingUploadService.this, gdata.messageType, filepath, data.getRESULT().getUserImage());
                            if (fileStatus) {
                                JSONObject message = new JSONObject();
                                message.put(Constants.TAG_GROUP_ID, gdata.groupId);
                                message.put(Constants.TAG_GROUP_NAME, gdata.groupName);
                                message.put(Constants.TAG_CHAT_TYPE, Constants.TAG_GROUP);
                                message.put(Constants.TAG_MESSAGE_TYPE, gdata.messageType);
                                message.put(Constants.TAG_ATTACHMENT, data.getRESULT().getUserImage());
                                message.put(Constants.TAG_THUMBNAIL, gdata.thumbnail);
                                message.put(Constants.TAG_MESSAGE, gdata.message);
                                message.put(Constants.TAG_CHAT_TIME, String.valueOf(System.currentTimeMillis() / 1000L));
                                message.put(Constants.TAG_MESSAGE_ID, gdata.messageId);
                                message.put(Constants.TAG_MEMBER_ID, gdata.memberId);
                                message.put(Constants.TAG_MEMBER_NAME, gdata.memberName);
                                message.put(Constants.TAG_MEMBER_NO, gdata.memberNo);
                                Log.v("checkChat", "startchat=" + message);
                                socketConnection.startGroupChat(message);

                                dbhelper.updateGroupMessageData(gdata.messageId, Constants.TAG_ATTACHMENT, data.getRESULT().getUserImage());
                                dbhelper.updateGroupMessageData(gdata.messageId, Constants.TAG_PROGRESS, "completed");

                                /*mBuilder.setProgress(0, 0, false);
                                mBuilder.setContentText("File uploaded");
                                mNotifyManager.notify(2, mBuilder.build());*/

                                socketConnection.setUploadingListen(chatType, gdata.messageId, data.getRESULT().getUserImage(), "completed");
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                            setGroupErrorUpload();
                        }
                    } else {
                        setGroupErrorUpload();
                    }
                }

                @Override
                public void onFailure(Call<GroupImageModel> call, Throwable t) {
                    setGroupErrorUpload();
                }
            });
//                } else {
//                    setGroupErrorUpload();
//                }
//            } catch (IOException e) {
//                e.printStackTrace();
//                setGroupErrorUpload();
//            }
        } else if (chatType.equals(Constants.TAG_CHANNEL)) {
            chdata = (ChannelMessage) bundle.getSerializable("mdata");

            ProgressRequestBody fileBody = new ProgressRequestBody(new File(filepath), this);
            MultipartBody.Part filePart = MultipartBody.Part.createFormData("channel_attachment", new File(filepath).getName(), fileBody);

            RequestBody channelid = RequestBody.create(MediaType.parse("multipart/form-data"), chdata.channelId);
            ApiInterface apiInterface = ApiClient.getClient().create(ApiInterface.class);
            RequestBody userid = RequestBody.create(MediaType.parse("multipart/form-data"), GetSet.getUserId());
            Call<Map<String, String>> call3 = apiInterface.upChannelChat(GetSet.getToken(), filePart, channelid, userid);
            try {
                Response<Map<String, String>> response = call3.execute();
                if (response.isSuccessful()) {
                    try {
                        Log.v(LOG_TAG, "upChannelChat " + response.body());
                        Log.v(LOG_TAG, "filepath=" + filepath);
                        Map<String, String> userdata = response.body();
                        if (userdata.get(Constants.TAG_STATUS).equals("true")) {
                            try {
                                Log.e(LOG_TAG, "upChannelChat " + new Gson().toJson(chdata));
                                boolean fileStatus = storageManager.moveFilesToSentPath(RecordingUploadService.this, chdata.messageType, filepath, userdata.get(Constants.TAG_USER_IMAGE));
                                if (fileStatus) {
                                    JSONObject message = new JSONObject();
                                    message.put(Constants.TAG_CHANNEL_ID, chdata.channelId);
                                    message.put(Constants.TAG_ADMIN_ID, chdata.channelAdminId);
                                    message.put(Constants.TAG_CHANNEL_NAME, chdata.channelName);
                                    message.put(Constants.TAG_CHAT_TYPE, Constants.TAG_CHANNEL);
                                    message.put(Constants.TAG_MESSAGE_TYPE, chdata.messageType);
                                    message.put(Constants.TAG_ATTACHMENT, userdata.get(Constants.TAG_USER_IMAGE));
                                    message.put(Constants.TAG_THUMBNAIL, chdata.thumbnail);
                                    message.put(Constants.TAG_MESSAGE, chdata.message);
                                    message.put(Constants.TAG_CHAT_TIME, String.valueOf(System.currentTimeMillis() / 1000L));
                                    message.put(Constants.TAG_MESSAGE_ID, chdata.messageId);
                                    socketConnection.startChannelChat(message);

                                    dbhelper.updateChannelMessageData(chdata.messageId, Constants.TAG_ATTACHMENT, userdata.get(Constants.TAG_USER_IMAGE));
                                    dbhelper.updateChannelMessageData(chdata.messageId, Constants.TAG_PROGRESS, "completed");

                                    /*mBuilder.setProgress(0, 0, false);
                                    mBuilder.setContentText("File uploaded");
                                    mNotifyManager.notify(2, mBuilder.build());*/

                                    socketConnection.setUploadingListen(chatType, chdata.messageId, userdata.get(Constants.TAG_USER_IMAGE), "completed");
                                }
                            } catch (JSONException e) {
                                e.printStackTrace();
                                setChannelErrorUpload();
                            }
                        } else {
                            setChannelErrorUpload();
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        setChannelErrorUpload();
                    }
                } else {
                    setChannelErrorUpload();
                }
            } catch (IOException e) {
                e.printStackTrace();
                setChannelErrorUpload();
            }
        }

        Log.v(LOG_TAG, "onHandleIntent END");
    }

    private void setErrorUpload() {
        dbhelper.updateMessageData(mdata.message_id, Constants.TAG_PROGRESS, "error");
        socketConnection.setUploadingListen(chatType, mdata.message_id, filepath, "error");
        /*mBuilder.setProgress(0, 0, false);
        mBuilder.setContentText("File upload error");
        mNotifyManager.notify(2, mBuilder.build());*/
    }

    private void setGroupErrorUpload() {
        dbhelper.updateGroupMessageData(gdata.messageId, Constants.TAG_PROGRESS, "error");
        socketConnection.setUploadingListen(chatType, gdata.messageId, filepath, "error");
        /*mBuilder.setProgress(0, 0, false);
        mBuilder.setContentText("File upload error");
        mNotifyManager.notify(2, mBuilder.build());*/
    }

    private void setChannelErrorUpload() {
        dbhelper.updateChannelMessageData(chdata.messageId, Constants.TAG_PROGRESS, "error");
        socketConnection.setUploadingListen(chatType, chdata.messageId, filepath, "error");
        /*mBuilder.setProgress(0, 0, false);
        mBuilder.setContentText("File upload error");
        mNotifyManager.notify(2, mBuilder.build());*/
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.v(LOG_TAG, "In onDestroy");
    }

    @Override
    public void onProgressUpdate(final int percentage) {
        if (elapsedTime > 500) {
            new Handler(Looper.getMainLooper()).post(new Runnable() {
                @Override
                public void run() {
                    /*mBuilder.setProgress(100, percentage, false);
                    mBuilder.setContentText("Uploading..");
                    mNotifyManager.notify(2, mBuilder.build());*/

                    startTime = System.currentTimeMillis();
                    elapsedTime = 0;
                }
            });
            Log.v(LOG_TAG, "onProgressUpdate=" + percentage);
        } else
            elapsedTime = new Date().getTime() - startTime;
    }

    @Override
    public void onError() {
        Log.v(LOG_TAG, "onError");
        if (chatType.equals("chat")) {
            setErrorUpload();
        } else if (chatType.equals("group")) {
            setGroupErrorUpload();
        }
    }
}
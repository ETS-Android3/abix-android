package com.topzi.chat.helper;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.telephony.TelephonyManager;
import android.util.Log;

import com.quickblox.chat.QBChatService;
import com.quickblox.users.model.QBUser;
import com.quickblox.videochat.webrtc.QBRTCClient;
import com.quickblox.videochat.webrtc.QBRTCSession;
import com.quickblox.videochat.webrtc.QBRTCTypes;
import com.topzi.chat.R;
import com.topzi.chat.activity.ApplicationClass;
import com.topzi.chat.model.UserProfileModel;
import com.google.gson.Gson;
import com.topzi.chat.external.RandomString;
import com.topzi.chat.activity.CallActivity;
import com.topzi.chat.model.AdminChannel;
import com.topzi.chat.model.AdminChannelMsg;
import com.topzi.chat.model.ChannelMessage;
import com.topzi.chat.model.ChannelResult;
import com.topzi.chat.model.GroupData;
import com.topzi.chat.model.GroupMessage;
import com.topzi.chat.model.GroupResult;
import com.topzi.chat.model.MessagesData;
import com.topzi.chat.service.LoginService;
import com.topzi.chat.utils.ApiClient;
import com.topzi.chat.utils.ApiInterface;
import com.topzi.chat.utils.CollectionsUtils;
import com.topzi.chat.utils.Constants;
import com.topzi.chat.utils.GetSet;
import com.topzi.chat.utils.PushNotificationSender;
import com.topzi.chat.utils.SharedPrefsHelper;
import com.topzi.chat.utils.ToastUtils;
import com.topzi.chat.utils.WebRtcSessionManager;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.webrtc.IceCandidate;
import org.webrtc.SessionDescription;

import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

import io.socket.client.IO;
import io.socket.client.Socket;
import io.socket.emitter.Emitter;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static android.content.Context.MODE_PRIVATE;
import static com.topzi.chat.utils.Constants.TAG_ADMIN_ID;
import static com.topzi.chat.utils.Constants.TAG_CHANNEL_ID;
import static com.topzi.chat.utils.Constants.TAG_CHANNEL_NAME;
import static com.topzi.chat.utils.Constants.TAG_CHAT_ID;
import static com.topzi.chat.utils.Constants.TAG_GROUP_ADMIN_ID;
import static com.topzi.chat.utils.Constants.TAG_GROUP_ID;
import static com.topzi.chat.utils.Constants.TAG_GROUP_NAME;
import static com.topzi.chat.utils.Constants.TAG_ID;
import static com.topzi.chat.utils.Constants.TAG_MEMBER_ID;
import static com.topzi.chat.utils.Constants.TAG_MEMBER_ROLE;
import static com.topzi.chat.utils.Constants.TAG_PRIVACY_ABOUT;
import static com.topzi.chat.utils.Constants.TAG_PRIVACY_LAST_SEEN;
import static com.topzi.chat.utils.Constants.TAG_PRIVACY_PROFILE;
import static com.topzi.chat.utils.Constants.TAG_USER_ID;

/**
 * Created on 28/6/18.
 */

public class SocketConnection extends Thread {

    private final String TAG = this.getClass().getSimpleName();
    private static SocketConnection mInstance;
    private static Context mCtx;
    private static Socket mSocket;
    DatabaseHandler dbhelper;
    public static RecentChatReceivedListener recentChatReceivedListener;
    public static RecentStatusReceivedListener recentStatusReceivedListener;
    public static ChatCallbackListener chatCallbackListener;
    public static StatusCallbackListener statusCallbackListener;
    public static GroupChatCallbackListener groupChatCallbackListener;
    public static GroupRecentReceivedListener groupRecentReceivedListener;
    private static NewAdminCreatedListener newAdminCreatedListener;
    private static OnGroupCreatedListener onGroupCreatedListener;
    private static ChannelCallbackListener channelCallbackListener;
    private static SignalingInterface signalingInterface;
    private static SelectContactListener selectContactListener;
    private static UserProfileListener userProfileListener;
    public static ChannelChatCallbackListener channelChatCallbackListener;
    public static ChannelRecentReceivedListener channelRecentReceivedListener;
    public static OnUpdateTabIndication onUpdateTabIndication;
    private SharedPreferences pref;
    private SharedPreferences.Editor editor;
    protected SharedPrefsHelper sharedPrefsHelper;

    private SocketConnection(Context context) {
        mCtx = context;
        dbhelper = DatabaseHandler.getInstance(context);
        pref = mCtx.getSharedPreferences("SavedPref", MODE_PRIVATE);
        editor = pref.edit();
        sharedPrefsHelper = SharedPrefsHelper.getInstance();
        start();
    }

    @Override
    public void run() {
        try {

            /*If the Server is SSL enabled please uncomment the below lines*/

            /*SSLContext mySSLContext = SSLContext.getInstance("TLS");
            TrustManager[] trustAllCerts = new TrustManager[]{new X509TrustManager() {
                public X509Certificate[] getAcceptedIssuers() {
                    return new X509Certificate[]{};
                }

                public void checkClientTrusted(X509Certificate[] chain,
                                               String authType) throws CertificateException {
                }

                public void checkServerTrusted(X509Certificate[] chain,
                                               String authType) throws CertificateException {
                }
            }};

            X509TrustManager myX509TrustManager = new X509TrustManager() {
                public X509Certificate[] getAcceptedIssuers() {
                    return new X509Certificate[]{};
                }

                public void checkClientTrusted(X509Certificate[] chain,
                                               String authType) throws CertificateException {
                }

                public void checkServerTrusted(X509Certificate[] chain,
                                               String authType) throws CertificateException {
                }
            };

            mySSLContext.init(null, trustAllCerts, null);

            HostnameVerifier myHostnameVerifier = new HostnameVerifier() {
                @Override
                public boolean verify(String hostname, SSLSession session) {
                    return true;
                }
            };

            OkHttpClient okHttpClient = new OkHttpClient.Builder()
                    .hostnameVerifier(myHostnameVerifier)
                    .sslSocketFactory(mySSLContext.getSocketFactory(), myX509TrustManager)
                    .build();

            // default settings for all sockets
            IO.setDefaultOkHttpWebSocketFactory(okHttpClient);
            IO.setDefaultOkHttpCallFactory(okHttpClient);

            // set as an option
            IO.Options opts = new IO.Options();
            opts.callFactory = okHttpClient;
            opts.webSocketFactory = okHttpClient;
             mSocket = IO.socket(Constants.SOCKETURL, opts);*/

            /*Comment the next one line if the server is SSL Enabled*/
            mSocket = IO.socket(Constants.BASE_URL);
            mSocket.on(Socket.EVENT_CONNECT, new Emitter.Listener() {
                @Override
                public void call(Object... args) {
                    Log.v("EVENT_CONNECT", "EVENT_CONNECT");
                    JSONObject join = new JSONObject();
                    try {
                        join.put(Constants.TAG_USER_ID, GetSet.getUserId());
                        join.put(Constants.TAG_FRIENDID, GetSet.getUserId());
                        join.put("timestamp", String.valueOf(System.currentTimeMillis() / 1000L));
                        mSocket.emit("in-chat-box", join);
                        joinGroups();
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }).on(Socket.EVENT_DISCONNECT, new Emitter.Listener() {
                @Override
                public void call(Object... args) {
                    Log.v("EVENT_DISCONNECT", "EVENT_DISCONNECT");
                }
            });
//            mSocket.on("pong", pong);
            mSocket.on("receive-chat", receiveChat);
            mSocket.on("end-chat", endChat);
            mSocket.on("viewchat", viewChat);
            mSocket.on("onlinestatus", onlineStatus);
            mSocket.on("listen-typing", listenTyping);
            mSocket.on("blockstatus", blockStatus);
            mSocket.on("offlinereadstatus", offlineReadStatus);
            mSocket.on("offlinedeliverystatus", offlineDeliveryStatus);
            mSocket.on("receive-my-status", receiveStatus);
            mSocket.on("groupInvitation", groupInvitation);
            mSocket.on("changeuserimage", userImageChange);
            mSocket.on("msgFromGroup", msgFromGroup);
            mSocket.on("listenGroupTyping", listenGroupTyping);
            mSocket.on("memberExited", memberExited);
            mSocket.on("groupModified", groupModified);
            mSocket.on("makeprivate", makeprivate);
            mSocket.on("Channelcreated", Channelcreated);
            mSocket.on("msgFromChannel", msgFromChannel);
            mSocket.on("msgfromadminchannels", msgfromadminchannels);
            mSocket.on("deletechannel", deletechannel);
            mSocket.on("receiveChannelInvitation", receiveChannelInvitation);
//            mSocket.on("channelblocked", channelblocked);
//            mSocket.on("newAdmin", newAdmin);
            mSocket.on(Socket.EVENT_CONNECT_ERROR, new Emitter.Listener() {
                @Override
                public void call(Object... args) {
                    Log.v("EVENT_CONNECT_ERROR", "EVENT_CONNECT_ERROR");
                }
            });
            mSocket.on(Socket.EVENT_CONNECT_TIMEOUT, new Emitter.Listener() {
                @Override
                public void call(Object... args) {
                    Log.v("EVENT_CONNECT_TIMEOUT", "EVENT_CONNECT_TIMEOUT");
                }
            });
            mSocket.on(Socket.EVENT_ERROR, new Emitter.Listener() {
                @Override
                public void call(Object... args) {
                    Log.v("EVENT_ERROR", "EVENT_ERROR");
                }
            });
            mSocket.on("newAdmin", newAdmin);
            mSocket.on("groupDeleted", groupDeleted);
            mSocket.on("created", onCreated);
            mSocket.on("full", onFull);
            mSocket.on("join", onJoin);
            mSocket.on("joined", onJoined);
            mSocket.on("bye", onBye);
            mSocket.on("rtcmessage", onRtcMessage);
            mSocket.on("callCreated", callCreated);
            mSocket.connect();
            Log.v("EVENT_SOCKET_CONNECT", "EVENT_SOCKET_CONNECT");
        } catch (URISyntaxException e) {
            e.printStackTrace();
        } /*catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (KeyManagementException e) {
            e.printStackTrace();
        }*/
    }

    public static synchronized SocketConnection getInstance(Context context) {
        if (mInstance == null) {
            mInstance = new SocketConnection(context);
        }
        return mInstance;
    }

    public void disconnect() {
        mSocket.disconnect();
        mSocket.off("receive-chat", receiveChat);
        mSocket.off("end-chat", endChat);
        mSocket.off("viewchat", viewChat);
        mSocket.off("onlinestatus", onlineStatus);
        mSocket.off("listen-typing", listenTyping);
        mSocket.off("blockstatus", blockStatus);
        mSocket.off("offlinereadstatus", offlineReadStatus);
        mSocket.off("offlinedeliverystatus", offlineDeliveryStatus);
        mSocket.off("groupInvitation", groupInvitation);
        mSocket.off("changeuserimage", userImageChange);
        mSocket.off("msgFromGroup", msgFromGroup);
        mSocket.off("listenGroupTyping", listenGroupTyping);
        mSocket.off("memberExited", memberExited);
        mSocket.off("groupModified", groupModified);
        mSocket.off("makeprivate", makeprivate);
        mSocket.off("Channelcreated", Channelcreated);
        mSocket.off("receiveChannelInvitation", receiveChannelInvitation);
        mSocket.off("msgfromadminchannels", msgfromadminchannels);
//        mSocket.off("channelblocked", channelblocked);
//        mSocket.off("pong", pong);
        mSocket.off(Socket.EVENT_CONNECT);
        mSocket.off(Socket.EVENT_DISCONNECT);
        mInstance = null;
        Log.v("disconnect", "disconnect");
    }

    private void joinGroups() {
        List<GroupData> groupList = dbhelper.getGroups();
        for (GroupData groupData : groupList) {
            try {
                JSONObject jobj = new JSONObject();
                jobj.put(Constants.TAG_GROUP_ID, groupData.groupId);
                jobj.put(Constants.TAG_MEMBER_ID, GetSet.getUserId());
                joinGroup(jobj);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

    }

    public void chatBox(String friendId) {
        JSONObject join = new JSONObject();
        try {
            join.put(Constants.TAG_USER_ID, GetSet.getUserId());
            join.put(Constants.TAG_FRIENDID, friendId);
            join.put("timestamp", String.valueOf(System.currentTimeMillis() / 1000L));
            mSocket.emit("in-chat-box", join);
            joinGroups();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void outChatBox(String friendId) {
        JSONObject join = new JSONObject();
        try {
            join.put(Constants.TAG_USER_ID, GetSet.getUserId());
            join.put(Constants.TAG_FRIENDID, friendId);
            join.put("timestamp", String.valueOf(System.currentTimeMillis() / 1000L));
            mSocket.emit("out-chat-box", join);
            joinGroups();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void startChat(JSONObject jsonObject) {
        mSocket.emit("start-chat", jsonObject);
    }

    public void chatReceived(JSONObject jsonObject) {
        mSocket.emit("chat-received", jsonObject);
    }

    public void chatViewed(JSONObject jsonObject) {
        mSocket.emit("chatviewed", jsonObject);
    }

    public void online(JSONObject jsonObject) {
        mSocket.emit("i-am-online", jsonObject);
    }

    public void typing(JSONObject jsonObject) {
        mSocket.emit("typing", jsonObject);
    }

    public void block(JSONObject jsonObject) {
        mSocket.emit("block", jsonObject);
    }

    public void createCall(JSONObject jsonObject) {
        mSocket.emit("createCall", jsonObject);
    }


    public void statusAdd(JSONObject jsonObject) {
        mSocket.emit("add-my-status", jsonObject);
    }

    public void statusReceived(JSONObject jsonObject) {
        mSocket.emit("status-received", jsonObject);
    }

    public void statusViewed(JSONObject jsonObject) {
        mSocket.emit("status-viewed", jsonObject);
    }


    public void startGroupChat(JSONObject jobj) {
        Log.e(TAG, "startGroupChat: " + jobj);
        mSocket.emit("msgToGroup", jobj);
    }

    public void groupChatReceived(JSONObject jobj) {
        Log.e(TAG, "groupChatReceived: " + jobj);
        mSocket.emit("groupchatreceived", jobj);
    }

    //    private Emitter.Listener pong = new Emitter.Listener() {
//        @Override
//        public void call(final Object... args) {
//
//        }
//    };
    private Emitter.Listener receiveChat = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            Log.v("onMessage", "onMessage=" + args[0]);
            try {
                JSONObject data = (JSONObject) args[0];
                MessagesData mdata = getMessagesByType(data);

                // checking the user already in contacts table
                if (dbhelper.isUserExist(mdata.user_id)) {
                    setMessagesnListener(mdata);
                } else {
                    updatemycontacts(mdata, 1);
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    };

    private Emitter.Listener receiveStatus = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            Log.v("onMessage", "onMessage=" + args[0]);
            try {
                JSONObject data = (JSONObject) args[0];
                MessagesData mdata = getStatusByType(data);

                // checking the user already in contacts table
                if (dbhelper.isUserExist(mdata.user_id)) {
                    setStatusListener(mdata);
                } else {
                    updatemycontacts(mdata, 2);
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    };

    public MessagesData getMessagesByType(JSONObject jobj) {
        MessagesData mdata = new MessagesData();
//        try {
//            JSONObject jobj = data;
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
        mdata.reply_to = jobj.optString(Constants.TAG_REPLY_TO, "");
        mdata.groupId = jobj.optString(TAG_GROUP_ID, "");

        if (mdata.message_type.equalsIgnoreCase("image") && mdata.message.equalsIgnoreCase("Voice Note")) {
            mdata.message_type = "voice";
        }

        dbhelper.addMessageDatas(GetSet.getUserId() + mdata.user_id, mdata.message_id, mdata.user_id, "",
                mdata.message_type, mdata.message, mdata.attachment, mdata.lat, mdata.lon, mdata.contact_name, mdata.contact_phone_no,
                mdata.contact_country_code, mdata.chat_time, GetSet.getUserId(), mdata.user_id, "sent", mdata.thumbnail, mdata.reply_to, mdata.groupId);
//        } catch (JSONException e) {
//            e.printStackTrace();
//        }
        return mdata;
    }

    public MessagesData getStatusByType(JSONObject jobj) {
        MessagesData mdata = new MessagesData();
//        try {
//            JSONObject jobj = data;
        mdata.user_id = jobj.optString(Constants.TAG_USER_ID, "");
        mdata.message_type = jobj.optString(Constants.TAG_MESSAGE_TYPE, "");
        mdata.message = jobj.optString(Constants.TAG_MESSAGE, "");
        mdata.message_id = jobj.optString(Constants.TAG_STATUS_ID, "");
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

        dbhelper.addStatusData(mdata.message_id, mdata.user_id, "",
                mdata.message_type, mdata.message, mdata.attachment, mdata.lat, mdata.lon, mdata.contact_name, mdata.contact_phone_no,
                mdata.contact_country_code, mdata.chat_time, GetSet.getUserId(), mdata.user_id, "sent", mdata.thumbnail);
//        } catch (JSONException e) {
//            e.printStackTrace();
//        }
        return mdata;
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
            Log.v("chatreceived", "=" + jsonObject);
            chatReceived(jsonObject);

            if (chatCallbackListener != null) {
                chatCallbackListener.onReceiveChat(mdata);
            }
            if (recentChatReceivedListener != null) {
                recentChatReceivedListener.onRecentChatReceived();
            }
            if (onUpdateTabIndication != null) {
                onUpdateTabIndication.updateIndication();
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void setStatusListener(MessagesData mdata) {
        try {
            int unseenCount = dbhelper.getUnseenStatusCount(mdata.user_id);
            Log.v("unseenCount", "unseenCount=" + unseenCount);
            dbhelper.addRecentStatus(mdata.user_id, mdata.message_id, mdata.chat_time, String.valueOf(unseenCount));

            // To acknowledge the message has been delivered
            JSONObject jsonObject = new JSONObject();
            jsonObject.put(Constants.TAG_FRIENDID, mdata.user_id);
            jsonObject.put(Constants.TAG_USER_ID, GetSet.getUserId());
            jsonObject.put(Constants.TAG_STATUS_ID, mdata.message_id);
            Log.v("chatreceived", "=" + jsonObject);
            statusReceived(jsonObject);

            if (statusCallbackListener != null) {
                statusCallbackListener.onReceiveStatus(mdata);
            }
            if (recentStatusReceivedListener != null) {
                recentStatusReceivedListener.onRecentStatusReceived();
            }
//            if (onUpdateTabIndication != null) {
//                onUpdateTabIndication.updateIndication();
//            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void setRecentListener() {
        if (recentChatReceivedListener != null) {
            recentChatReceivedListener.onRecentChatReceived();
        }
        if (chatCallbackListener != null) {
            chatCallbackListener.onGetUpdateFromDB();
        }
        if (onUpdateTabIndication != null) {
            onUpdateTabIndication.updateIndication();
        }
    }

    public void setUploadingListen(String chatType, String message_id, String attachment, String progress) {
        if (chatType.equals("chat")) {
            if (chatCallbackListener != null) {
                chatCallbackListener.onUploadListen(message_id, attachment, progress);
            }
        } else if (chatType.equals("group")) {
            if (groupChatCallbackListener != null) {
                groupChatCallbackListener.onUploadListen(message_id, attachment, progress);
            }
        } else if (chatType.equals(Constants.TAG_CHANNEL)) {
            if (channelChatCallbackListener != null) {
                channelChatCallbackListener.onUploadListen(message_id, attachment, progress);
            }
        }
    }

    private Emitter.Listener endChat = new Emitter.Listener() {
        @Override
        public void call(final Object... args) {
            Log.v("onEndChat", "=" + args[0]);
            try {
                JSONObject data = (JSONObject) args[0];
                String messageId = data.getString(Constants.TAG_MESSAGE_ID);
                String senderId = data.getString(Constants.TAG_FRIENDID);
                String receiverId = data.getString(TAG_USER_ID);
                dbhelper.updateMessageDeliverStatus(messageId);
                if (chatCallbackListener != null) {
                    chatCallbackListener.onEndChat(messageId, senderId, receiverId);
                }
                if (recentChatReceivedListener != null) {
                    Log.v("chatee", "sender=" + senderId + "&receiver=" + receiverId);
                    recentChatReceivedListener.onUpdateChatStatus(receiverId);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    };

    private Emitter.Listener viewChat = new Emitter.Listener() {
        @Override
        public void call(final Object... args) {
            Log.v("onViewChat", "=" + args[0]);
            try {
                JSONObject data = (JSONObject) args[0];
                String chatId = data.getString(Constants.TAG_CHAT_ID);
                String senderId = data.getString(Constants.TAG_SENDER_ID);
                String receiverId = data.getString(Constants.TAG_RECEIVER_ID);
                dbhelper.updateMessageReadStatus(chatId, receiverId);
                if (chatCallbackListener != null) {
                    chatCallbackListener.onViewChat(chatId, senderId, receiverId);
                }
                if (recentChatReceivedListener != null) {
                    Log.v("chatvv", "sender=" + senderId + "&receiver=" + receiverId);
                    recentChatReceivedListener.onUpdateChatStatus(receiverId);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    };

    private Emitter.Listener offlineReadStatus = new Emitter.Listener() {
        @Override
        public void call(final Object... args) {
            Log.v("offlinereadstatus", "=" + args[0]);
            try {
                JSONObject jobj = (JSONObject) args[0];
                JSONArray chats = jobj.getJSONArray("chats");
                for (int i = 0; i < chats.length(); i++) {
                    JSONObject data = chats.getJSONObject(i);
                    String chatId = data.getString(Constants.TAG_CHAT_ID);
                    String senderId = data.getString(Constants.TAG_SENDER_ID);
                    String receiverId = data.getString(Constants.TAG_RECEIVER_ID);
                    dbhelper.updateMessageReadStatus(chatId, receiverId);
                    if (chatCallbackListener != null) {
                        chatCallbackListener.onViewChat(chatId, senderId, receiverId);
                    }
                    if (recentChatReceivedListener != null) {
                        Log.v("chatvv", "sender=" + senderId + "&receiver=" + receiverId);
                        recentChatReceivedListener.onUpdateChatStatus(receiverId);
                    }
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    };

    private Emitter.Listener offlineDeliveryStatus = new Emitter.Listener() {
        @Override
        public void call(final Object... args) {
            Log.v("offlinedeliverystatus", "=" + args[0]);
            try {
                JSONObject jobj = (JSONObject) args[0];
                JSONArray messages = jobj.getJSONArray("messages");
                for (int i = 0; i < messages.length(); i++) {
                    JSONObject data = messages.getJSONObject(i);
                    String messageId = data.getString(Constants.TAG_MESSAGE_ID);
                    String senderId = data.getString(Constants.TAG_SENDER_ID);
                    String receiverId = data.getString(Constants.TAG_RECEIVER_ID);
                    dbhelper.updateMessageDeliverStatus(messageId);
                    if (chatCallbackListener != null) {
                        chatCallbackListener.onEndChat(messageId, senderId, receiverId);
                    }
                    if (recentChatReceivedListener != null) {
                        Log.v("chatee", "sender=" + senderId + "&receiver=" + receiverId);
                        recentChatReceivedListener.onUpdateChatStatus(receiverId);
                    }
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    };

    private Emitter.Listener onlineStatus = new Emitter.Listener() {
        @Override
        public void call(final Object... args) {
            Log.v("onlineStatus", "=" + args[0]);
            try {
                if (chatCallbackListener != null) {
                    JSONObject data = (JSONObject) args[0];
                    chatCallbackListener.onlineStatus(data);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    };

    private Emitter.Listener listenTyping = new Emitter.Listener() {
        @Override
        public void call(final Object... args) {
            Log.v("listenTyping", "=" + args[0]);
            try {
                JSONObject data = (JSONObject) args[0];
                if (chatCallbackListener != null) {
                    chatCallbackListener.onListenTyping(data);
                }
                if (recentChatReceivedListener != null) {
                    recentChatReceivedListener.onListenTyping(data);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    };

    public void groupTyping(JSONObject jsonObject) {
        Log.i(TAG, "groupTyping: " + jsonObject.toString());
        mSocket.emit("groupTyping", jsonObject);
    }

    private Emitter.Listener listenGroupTyping = new Emitter.Listener() {
        @Override
        public void call(final Object... args) {
            Log.v("listenGroupTyping", "=" + args[0]);
            try {
                JSONObject data = (JSONObject) args[0];
                if (groupChatCallbackListener != null) {
                    groupChatCallbackListener.onListenGroupTyping(data);
                }
                if (groupRecentReceivedListener != null) {
                    groupRecentReceivedListener.onListenGroupTyping(data);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    };

    private Emitter.Listener blockStatus = new Emitter.Listener() {
        @Override
        public void call(final Object... args) {
            Log.v("blockStatus", "=" + args[0]);
            try {
                JSONObject data = (JSONObject) args[0];
                String senderId = data.getString(Constants.TAG_SENDER_ID);
                String receiverId = data.getString(Constants.TAG_RECEIVER_ID);
                String type = data.getString(Constants.TAG_TYPE);
                dbhelper.updateBlockStatus(senderId, Constants.TAG_BLOCKED_ME, type);
                if (chatCallbackListener != null) {
                    chatCallbackListener.onBlockStatus(data);
                }
                if (recentChatReceivedListener != null) {
                    recentChatReceivedListener.onBlockStatus(data);
                }
                if (selectContactListener != null) {
                    selectContactListener.onBlockStatus(data);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    };

    private Emitter.Listener userImageChange = new Emitter.Listener() {
        @Override
        public void call(final Object... args) {
            Log.v("blockStatus", "=" + args[0]);
            try {
                JSONObject data = (JSONObject) args[0];
                String user_id = data.getString(Constants.TAG_USER_ID);
                String user_image = data.getString(Constants.TAG_USER_IMAGE);
                dbhelper.updateBlockStatus(user_id, Constants.TAG_USER_IMAGE, user_image);
                if (chatCallbackListener != null) {
                    chatCallbackListener.onUserImageChange(user_id, user_image);
                }
                if (recentChatReceivedListener != null) {
                    recentChatReceivedListener.onUserImageChange(user_id, user_image);
                }
                if (selectContactListener != null) {
                    selectContactListener.onUserImageChange(user_id, user_image);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    };

    private Emitter.Listener groupInvitation = new Emitter.Listener() {
        @Override
        public void call(final Object... args) {
            Log.e(TAG, "groupInvitation= " + args[0]);
            try {
                JSONObject data = (JSONObject) args[0];
                String groupId = data.getString(TAG_ID);
                String groupAdminId = data.getString(Constants.TAG_GROUP_ADMIN_ID);
                String groupName = data.getString(Constants.TAG_GROUP_NAME);
                String groupImage = data.getString(Constants.TAG_GROUP_IMAGE);
                String createdAt = data.getString(Constants.TAG_CREATED_AT);
                dbhelper.createGroup(groupId, groupAdminId, groupName, createdAt, groupImage);

                JSONArray membersArray = new JSONArray(data.getString(Constants.TAG_GROUP_MEMBERS));

                for (int i = 0; i < membersArray.length(); i++) {
                    JSONObject jsonObject = new JSONObject();
                    try {
                        jsonObject = membersArray.getJSONObject(i);
                        String memberId = jsonObject.getString(Constants.TAG_MEMBER_ID);
                        String memberRole = jsonObject.getString(Constants.TAG_MEMBER_ROLE);
                        if (!dbhelper.isUserExist(memberId)) {
                            String memberKey = groupId + memberId;
                            getUserData(memberKey, groupId, memberId, memberRole);
                        } else {
                            String memberKey = groupId + memberId;
                            dbhelper.createGroupMembers(memberKey, groupId, memberId, memberRole);
                        }

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }

                String unixStamp = String.valueOf(System.currentTimeMillis() / 1000L);
                RandomString randomString = new RandomString(10);
                String messageId = groupId + randomString.nextString();

                if (!groupAdminId.equals(GetSet.getUserId())) {
                    String unixStamp2 = String.valueOf(System.currentTimeMillis() / 1000L);
                    String messageId2 = groupId + randomString.nextString();
                    dbhelper.addGroupMessages(messageId2, groupId, GetSet.getUserId(), groupAdminId, "add_member",
                            "", "", "", "",
                            "", "", "", createdAt, "", "", "");
                }

                dbhelper.addGroupMessages(messageId, groupId, GetSet.getUserId(), groupAdminId, "create_group",
                        "", "", "", "",
                        "", "", "", createdAt, "", "", "");

                int unseenCount = dbhelper.getUnseenGroupMessagesCount(groupId);
                dbhelper.addGroupRecentMsgs(groupId, messageId, GetSet.getUserId(), unixStamp, "" + unseenCount);

                if (groupRecentReceivedListener != null) {
                    groupRecentReceivedListener.onGroupCreated();
                }

                if (onGroupCreatedListener != null) {
                    onGroupCreatedListener.onGroupCreated(data);
                }

                if (onUpdateTabIndication != null) {
                    onUpdateTabIndication.updateIndication();
                }

                try {
                    JSONObject jobj = new JSONObject();
                    jobj.put(Constants.TAG_GROUP_ID, groupId);
                    jobj.put(Constants.TAG_MEMBER_ID, GetSet.getUserId());
                    jobj.put("acknowledged", "1");
                    joinGroup(jobj);
                } catch (JSONException e) {
                    e.printStackTrace();
                }

            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    };

    private void getUserInfo(String memberId) {
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

    private void getUserData(String memberKey, String groupId, String memberId, String memberRole) {
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

    private void getGroupInfo(JSONObject data, String groupId) {
        JSONArray jsonArray = new JSONArray();
        jsonArray.put(groupId);
        ApiInterface apiInterface = ApiClient.getClient().create(ApiInterface.class);
        Call<GroupResult> call3 = apiInterface.getGroupInfo(GetSet.getUserId(), jsonArray.toString());
        call3.enqueue(new Callback<GroupResult>() {
            @Override
            public void onResponse(Call<GroupResult> call, Response<GroupResult> response) {
                try {
                    Log.v(TAG, "getGroupInfo: " + new Gson().toJson(response));
                    GroupResult userdata = response.body();
                    if (userdata.status.equalsIgnoreCase(Constants.TRUE)) {
                        for (GroupData groupData : userdata.result) {
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
                                String messageId2 = groupId + randomString.nextString();
                                dbhelper.addGroupMessages(messageId2, groupId, GetSet.getUserId(), groupData.groupAdminId, "add_member",
                                        "", "", "", "",
                                        "", "", "", groupData.createdAt, "", "", "");
                                unseenCount = dbhelper.getUnseenGroupMessagesCount(groupData.groupId);
                                dbhelper.addGroupRecentMsgs(groupData.groupId, messageId, GetSet.getUserId(), unixStamp, "" + unseenCount);
                            }

                            if (groupRecentReceivedListener != null) {
                                groupRecentReceivedListener.onGroupCreated();
                            }
                        }
                        setGroupMsgListener(data);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(Call<GroupResult> call, Throwable t) {
                Log.v("getGroupInfo Failed", "TEST" + t.getMessage());
                call.cancel();
            }
        });
    }

    private Emitter.Listener msgFromGroup = new Emitter.Listener() {
        @Override
        public void call(final Object... args) {
            Log.e(TAG, "msgFromGroup " + args[0]);
            try {
                JSONObject data = (JSONObject) args[0];
                if (!dbhelper.isGroupExist(data.getString(Constants.TAG_GROUP_ID))) {
                    getGroupInfo(data, data.getString(Constants.TAG_GROUP_ID));
                } else {
                    setGroupMsgListener(data);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    };

    private void setGroupMsgListener(JSONObject jsonObject) {

        GroupMessage mdata = getGroupMessagesByType(jsonObject);

        if (mdata.memberId.equalsIgnoreCase(GetSet.getUserId()) && (mdata.messageType.equals("text") || mdata.messageType.equals("image") ||
                mdata.messageType.equals("audio") || mdata.messageType.equals("video") || mdata.messageType.equals("document") ||
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

            if (groupChatCallbackListener != null) {
                groupChatCallbackListener.onGroupChatReceive(mdata);
            }
            if (groupRecentReceivedListener != null) {
                groupRecentReceivedListener.onGroupRecentReceived();
            }
            if (onUpdateTabIndication != null) {
                onUpdateTabIndication.updateIndication();
            }
        }

        try {
            JSONObject json = new JSONObject();
            json.put("user_id", GetSet.getUserId());
            json.put("group_id", mdata.groupId);
            json.put("chat_id", mdata.chatId);
            groupChatReceived(json);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private Emitter.Listener newAdmin = new Emitter.Listener() {
        @Override
        public void call(final Object... args) {
            JSONObject jsonObject = (JSONObject) args[0];
            Log.i(TAG, "newAdmin: " + jsonObject.toString());

            try {
                String memberKey = jsonObject.getString(TAG_GROUP_ID) + jsonObject.getString(TAG_MEMBER_ID);
                dbhelper.updateGroupMembers(memberKey, jsonObject.getString(TAG_GROUP_ID), jsonObject.getString(TAG_MEMBER_ID),
                        jsonObject.getString(TAG_MEMBER_ROLE));

                if (newAdminCreatedListener != null) {
                    newAdminCreatedListener.onNewAdminCreated();
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    };

    private Emitter.Listener memberExited = new Emitter.Listener() {
        @Override
        public void call(final Object... args) {
            try {
                JSONObject data = (JSONObject) args[0];
                Log.i(TAG, "memberExited: " + data.toString());
                /*String groupId = data.getString(Constants.TAG_GROUP_ID);
                String memberId = data.getString(Constants.TAG_MEMBER_ID);
                dbhelper.deleteFromGroup(groupId, memberId);*/
                if (groupChatCallbackListener != null) {
                    groupChatCallbackListener.onMemberExited(data);
                }
                if (groupRecentReceivedListener != null) {
                    groupRecentReceivedListener.onMemberExited(data);
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    };

    private Emitter.Listener groupModified = new Emitter.Listener() {
        @Override
        public void call(final Object... args) {
            JSONObject data = (JSONObject) args[0];
            Log.i(TAG, "groupModified: " + data);

            /*try {
                dbhelper.updateGroup(data.getString(TAG_ID),
                        data.getString(Constants.TAG_GROUP_ADMIN_ID),
                        data.getString(Constants.TAG_GROUP_NAME),
                        data.getString(Constants.TAG_CREATED_AT),
                        data.has(Constants.TAG_GROUP_IMAGE) ? data.getString(Constants.TAG_GROUP_IMAGE) : "");

                JSONArray jsonArray = new JSONArray(data.getString(Constants.TAG_GROUP_MEMBERS));
                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject jsonObject = jsonArray.getJSONObject(i);
                    String memberId = jsonObject.getString(Constants.TAG_MEMBER_ID);
                    String memberRole = jsonObject.getString(Constants.TAG_MEMBER_ROLE);
                    if (!dbhelper.isUserExist(memberId)) {
                        getUserInfo(memberId);
                    }
                    String memberKey = data.getString(TAG_ID) + jsonObject.getString(TAG_MEMBER_ID);
                    dbhelper.updateGroupMembers(memberKey, data.getString(Constants.TAG_ID), memberId, memberRole);
                }

                if (groupRecentReceivedListener != null) {
                    groupRecentReceivedListener.onGroupModified(data);
                }
            } catch (JSONException e) {
                Log.e(TAG, "groupModified: " + e.getMessage());
                e.printStackTrace();
            }*/

        }
    };

    private Emitter.Listener makeprivate = new Emitter.Listener() {
        @Override
        public void call(final Object... args) {
            JSONObject data = (JSONObject) args[0];
            Log.i(TAG, "makeprivate: " + data);
            try {
                if (data.getString(TAG_USER_ID).equalsIgnoreCase(GetSet.getUserId())) {
                    editor.putString("privacyprofileimage", data.getString(TAG_PRIVACY_PROFILE));
                    editor.putString("privacylastseen", data.getString(TAG_PRIVACY_LAST_SEEN));
                    editor.putString("privacyabout", data.getString(TAG_PRIVACY_ABOUT));
                    editor.commit();

                    GetSet.setPrivacyprofileimage(pref.getString("privacyprofileimage", Constants.TAG_EVERYONE));
                    GetSet.setPrivacylastseen(pref.getString("privacylastseen", Constants.TAG_EVERYONE));
                    GetSet.setPrivacyabout(pref.getString("privacyabout", Constants.TAG_EVERYONE));
                } else {
                    dbhelper.updateUserPrivacy(data);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
            if (recentChatReceivedListener != null) {
                recentChatReceivedListener.onPrivacyChanged(data);
            }
            if (chatCallbackListener != null) {
                chatCallbackListener.onPrivacyChanged(data);
            }
            if (selectContactListener != null) {
                selectContactListener.onPrivacyChanged(data);
            }
            if (userProfileListener != null) {
                userProfileListener.onPrivacyChanged(data);
            }
        }
    };

    private GroupMessage getGroupMessagesByType(JSONObject data) {
        GroupMessage mdata = new GroupMessage();
        try {
            mdata.groupId = data.optString(TAG_GROUP_ID, "");
            mdata.groupName = data.optString(TAG_GROUP_NAME, "");
            mdata.memberId = data.optString(Constants.TAG_MEMBER_ID, "");
            mdata.memberName = data.optString(Constants.TAG_MEMBER_NAME, "");
            mdata.memberNo = data.optString(Constants.TAG_MEMBER_NO, "");
            mdata.messageType = data.optString(Constants.TAG_MESSAGE_TYPE, "");
            mdata.message = data.optString(Constants.TAG_MESSAGE, "");
            mdata.messageId = data.optString(Constants.TAG_MESSAGE_ID, "");
            mdata.chatTime = data.optString(Constants.TAG_CHAT_TIME, "");
            mdata.attachment = data.optString(Constants.TAG_ATTACHMENT, "");
            mdata.thumbnail = data.optString(Constants.TAG_THUMBNAIL, "");
            mdata.lat = data.optString(Constants.TAG_LAT, "");
            mdata.lon = data.optString(Constants.TAG_LON, "");
            mdata.contactName = data.optString(Constants.TAG_CONTACT_NAME, "");
            mdata.contactPhoneNo = data.optString(Constants.TAG_CONTACT_PHONE_NO, "");
            mdata.contactCountryCode = data.optString(Constants.TAG_CONTACT_COUNTRY_CODE, "");
            mdata.groupAdminId = data.optString(TAG_GROUP_ADMIN_ID, "");
            mdata.chatId = data.optString(TAG_CHAT_ID, "");

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
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return mdata;
    }

    private Emitter.Listener Channelcreated = new Emitter.Listener() {
        @Override
        public void call(final Object... args) {
            JSONObject jsonObject = (JSONObject) args[0];
            Log.i(TAG, "Channelcreated: " + jsonObject);
            try {
                String channelId = jsonObject.getString(TAG_ID);
                String channelName = jsonObject.getString(Constants.TAG_CHANNEL_NAME);
                String channelDes = jsonObject.getString(Constants.TAG_CHANNEL_DES);
                String channelImage = jsonObject.getString(Constants.TAG_CHANNEL_IMAGE);
                String channelType = jsonObject.getString(Constants.TAG_CHANNEL_TYPE);
                String adminId = jsonObject.getString(Constants.TAG_CHANNEL_ADMIN_ID);
                String totalSubscriber = jsonObject.getString(Constants.TAG_TOTAL_SUBSCRIBERS);
                String createdAt = jsonObject.getString(Constants.TAG_CREATED_TIME);

                dbhelper.addChannel(channelId, channelName, channelDes, channelImage, channelType,
                        adminId, "", totalSubscriber, createdAt, Constants.TAG_USER_CHANNEL, "", "");

                String unixStamp = String.valueOf(System.currentTimeMillis() / 1000L);
                RandomString randomString = new RandomString(10);
                String messageId = channelId + randomString.nextString();

                String deliveryStatus = "";
                int unreadCount = 0;
                if (adminId.equalsIgnoreCase(GetSet.getUserId())) {
                    deliveryStatus = "read";
                    unreadCount = 0;
                } else {
                    unreadCount = dbhelper.getUnseenChannelMessagesCount(channelId);
                }

                if (!dbhelper.isChannelIdExistInMessages(channelId)) {
                    dbhelper.addChannelMessages(channelId, Constants.TAG_CHANNEL, messageId, "create_channel",
                            "", "", "", "", "", "", "", unixStamp, "", deliveryStatus);

                    dbhelper.addChannelRecentMsgs(channelId, messageId, unixStamp, "" + unreadCount);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
            if (channelCallbackListener != null) {
                channelCallbackListener.onChannelCreated(jsonObject);
            }

            if (channelRecentReceivedListener != null) {
                channelRecentReceivedListener.onChannelCreated(jsonObject);
            }
        }
    };

    private Emitter.Listener receiveChannelInvitation = new Emitter.Listener() {
        @Override
        public void call(final Object... args) {
            JSONObject jsonObject = (JSONObject) args[0];
            Log.i(TAG, "receiveChannelInvitation: " + jsonObject);
            try {
                dbhelper.addChannel(jsonObject.getString(Constants.TAG_ID),
                        jsonObject.getString(Constants.TAG_CHANNEL_NAME), jsonObject.getString(Constants.TAG_CHANNEL_DES),
                        jsonObject.getString(Constants.TAG_CHANNEL_IMAGE), jsonObject.getString(Constants.TAG_CHANNEL_TYPE),
                        jsonObject.getString(Constants.TAG_CHANNEL_ADMIN_ID), "", jsonObject.getString(Constants.TAG_TOTAL_SUBSCRIBERS),
                        jsonObject.getString(Constants.TAG_CREATED_TIME), Constants.TAG_USER_CHANNEL, "", "");

                String unixStamp = String.valueOf(System.currentTimeMillis() / 1000L);
                RandomString randomString = new RandomString(10);
                String messageId = jsonObject.getString(Constants.TAG_ID) + randomString.nextString();

                dbhelper.addChannelMessages(jsonObject.getString(Constants.TAG_ID), Constants.TAG_CHANNEL,
                        messageId, "create_channel", "", "", "", "",
                        "", "", "", unixStamp, "", "");

                int unseenCount = dbhelper.getUnseenChannelMessagesCount(jsonObject.getString(Constants.TAG_ID));
                dbhelper.addChannelRecentMsgs(jsonObject.getString(Constants.TAG_ID), messageId, unixStamp, String.valueOf(unseenCount));


            } catch (JSONException e) {
                e.printStackTrace();
            }
            if (channelRecentReceivedListener != null) {
                channelRecentReceivedListener.onChannelInviteReceived(jsonObject);
            }
        }
    };

    private Emitter.Listener msgfromadminchannels = new Emitter.Listener() {
        @Override
        public void call(final Object... args) {

            Log.i(TAG, "msgFromAdminChannels: " + args[0]);
            JSONObject jObj = (JSONObject) args[0];
            try {
                if (!dbhelper.isChannelExist(jObj.getString(Constants.TAG_CHANNEL_ID))) {
                    getAdminChannels(jObj);
                } else {
                    setAdminChannelMessages(jObj);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    };

    private Emitter.Listener channelblocked = new Emitter.Listener() {
        @Override
        public void call(final Object... args) {

            Log.i(TAG, "channelBlocked: " + args[0]);
            JSONObject jObj = (JSONObject) args[0];
            try {
                if (dbhelper.isChannelExist(jObj.getString(Constants.TAG_CHANNEL_ID))) {
                    dbhelper.updateChannelData(jObj.getString(Constants.TAG_CHANNEL_ID), Constants.TAG_BLOCK_STATUS, "1");

//                    if(channelChatCallbackListener != null) {
//                        channelChatCallbackListener.onChannelBlocked(jObj.getString(Constants.TAG_CHANNEL_ID));
//                    }
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    };

    private void setAdminChannelMessages(JSONObject jObj) {
        AdminChannelMsg.Result adminMessage = getAdminMessagesByType(jObj);

        if (dbhelper.isChannelExist(adminMessage.channelId)) {
            dbhelper.addChannelMessages(adminMessage.channelId, adminMessage.chatType, adminMessage.messageId, adminMessage.messageType,
                    adminMessage.message, adminMessage.attachment, "", "", "", "", "", adminMessage.chatTime, adminMessage.thumbnail, "");

            int unseenCount = dbhelper.getUnseenChannelMessagesCount(adminMessage.channelId);
            dbhelper.addChannelRecentMsgs(adminMessage.channelId, adminMessage.messageId, adminMessage.chatTime, "" + unseenCount);
        }

        if (channelChatCallbackListener != null) {
            channelChatCallbackListener.onAdminChatReceive(adminMessage);
        }

        if (channelRecentReceivedListener != null) {
            channelRecentReceivedListener.onAdminChatReceive();
        }

        if (onUpdateTabIndication != null) {
            onUpdateTabIndication.updateIndication();
        }
    }

    private void getAdminChannels(JSONObject jObj) {
        ApiInterface apiInterface = ApiClient.getClient().create(ApiInterface.class);
        Call<AdminChannel> call3 = apiInterface.getAdminChannels(GetSet.getToken(), GetSet.getUserId());
        call3.enqueue(new Callback<AdminChannel>() {
            @Override
            public void onResponse(Call<AdminChannel> call, Response<AdminChannel> response) {
                Log.v(TAG, "getAdminChannels: " + new Gson().toJson(response));
                if (response.body().status.equalsIgnoreCase(Constants.TRUE)) {
                    /*Get Channels from Admin*/
                    for (AdminChannel.Result result : response.body().result) {
                        if (!dbhelper.isChannelExist(result.channelId)) {
                            dbhelper.addChannel(result.channelId, result.channelName, result.channelDes,
                                    "", Constants.TAG_PUBLIC, "", "", "", result.createdTime, Constants.TAG_ADMIN_CHANNEL, "", "");
                            if (!dbhelper.isChannelIdExistInMessages(result.channelId)) {
                                String unixStamp = String.valueOf(System.currentTimeMillis() / 1000L);
                                RandomString randomString = new RandomString(10);
                                String messageId = result.channelId + randomString.nextString();
                                dbhelper.addChannelMessages(result.channelId, Constants.TAG_ADMIN_CHANNEL, messageId, "create_channel",
                                        "", "", "", "", "", "", "",
                                        result.createdTime, "", "");

                                int unseenCount = dbhelper.getUnseenChannelMessagesCount(result.channelId);
                                dbhelper.addChannelRecentMsgs(result.channelId, messageId, unixStamp, "" + unseenCount);
                            }
                        }
                    }
                    setAdminChannelMessages(jObj);
                }
            }

            @Override
            public void onFailure(Call<AdminChannel> call, Throwable t) {
                Log.e(TAG, "getAdminChannels: " + t.getMessage());
                call.cancel();
            }
        });
    }

    private Emitter.Listener deletechannel = new Emitter.Listener() {
        @Override
        public void call(final Object... args) {

            Log.i(TAG, "deletechannel: " + args[0]);
            String channelId = "" + args[0];

            dbhelper.deleteChannel(channelId);
            dbhelper.deleteChannelMessages(channelId);
            dbhelper.deleteChannelRecentMessages(channelId);
            if (channelChatCallbackListener != null) {
                channelChatCallbackListener.onChannelDeleted();
            }

            if (channelRecentReceivedListener != null) {
                channelRecentReceivedListener.onChannelDeleted();
            }


        }
    };

    public void setRecentGroupListener() {
        if (groupRecentReceivedListener != null) {
            groupRecentReceivedListener.onGroupRecentReceived();
        }
        if (groupChatCallbackListener != null) {
            groupChatCallbackListener.onGetUpdateFromDB();
        }
        if (onUpdateTabIndication != null) {
            onUpdateTabIndication.updateIndication();
        }
    }

    public void updateGroupInfo(GroupMessage groupMessage) {
        if (groupChatCallbackListener != null) {
            groupChatCallbackListener.onUpdateGroupInfo(groupMessage);
        }
    }

    public void setRecentChannelChatListener() {
        if (channelChatCallbackListener != null) {
            channelChatCallbackListener.onGetUpdateFromDB();
        }

        if (channelRecentReceivedListener != null) {
            channelRecentReceivedListener.onChannelRecentReceived();
        }

        if (onUpdateTabIndication != null) {
            onUpdateTabIndication.updateIndication();
        }
    }

    void updatemycontacts(final MessagesData mdata, int i) {

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
                        if (i == 1)
                            setMessagesnListener(mdata);
                        else setStatusListener(mdata);
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

    private Emitter.Listener msgFromChannel = new Emitter.Listener() {
        @Override
        public void call(final Object... args) {
            Log.e(TAG, "msgFromChannel " + args[0]);
            try {
                JSONObject data = (JSONObject) args[0];
                setChannelMsgListener(data);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    };

    private void setChannelMsgListener(JSONObject jsonObject) {

        ChannelMessage mdata = getChannelMessagesByType(jsonObject);

        if (mdata.chatType.equalsIgnoreCase(Constants.TAG_CHANNEL)) {
//            ChannelResult.Result channelData = dbhelper.getChannelInfo(mdata.channelId);
//            if (!channelData.channelAdminId.equalsIgnoreCase(GetSet.getUserId())) {
            if (mdata.channelAdminId != null && !mdata.channelAdminId.equalsIgnoreCase(GetSet.getUserId())) {
                insertChannelMessages(mdata);
            }
        }
    }

    private void insertChannelMessages(ChannelMessage result) {
        dbhelper.addChannelMessages(result.channelId, result.chatType,
                result.messageId, result.messageType, result.message, result.attachment, result.lat, result.lon,
                result.contactName, result.contactPhoneNo, result.contactCountryCode, result.chatTime, result.thumbnail, "");

        int unseenCount = dbhelper.getUnseenChannelMessagesCount(result.channelId);
        dbhelper.addChannelRecentMsgs(result.channelId, result.messageId, result.chatTime, String.valueOf(unseenCount));

        if (channelChatCallbackListener != null) {
            channelChatCallbackListener.onChannelChatReceive(result);
        }
        if (channelRecentReceivedListener != null) {
            channelRecentReceivedListener.onChannelRecentReceived();
        }

        if (onUpdateTabIndication != null) {
            onUpdateTabIndication.updateIndication();
        }
    }

    private ChannelMessage getChannelMessagesByType(JSONObject data) {
        ChannelMessage mdata = new ChannelMessage();
        try {
            mdata.channelId = data.optString(TAG_CHANNEL_ID, "");
            mdata.channelAdminId = data.optString(TAG_ADMIN_ID, "");
            mdata.channelName = data.optString(TAG_CHANNEL_NAME, "");
            mdata.messageType = data.optString(Constants.TAG_MESSAGE_TYPE, "");
            mdata.chatType = data.optString(Constants.TAG_CHAT_TYPE, "");
            mdata.message = data.optString(Constants.TAG_MESSAGE, "");
            mdata.messageId = data.optString(Constants.TAG_MESSAGE_ID, "");
            mdata.chatTime = data.optString(Constants.TAG_CHAT_TIME, "");
            mdata.attachment = data.optString(Constants.TAG_ATTACHMENT, "");
            mdata.thumbnail = data.optString(Constants.TAG_THUMBNAIL, "");
            mdata.lat = data.optString(Constants.TAG_LAT, "");
            mdata.lon = data.optString(Constants.TAG_LON, "");
            mdata.contactName = data.optString(Constants.TAG_CONTACT_NAME, "");
            mdata.contactPhoneNo = data.optString(Constants.TAG_CONTACT_PHONE_NO, "");
            mdata.contactCountryCode = data.optString(Constants.TAG_CONTACT_COUNTRY_CODE, "");

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

    private AdminChannelMsg.Result getAdminMessagesByType(JSONObject data) {
        AdminChannelMsg.Result mdata = new AdminChannelMsg().new Result();
        try {
            mdata.channelId = data.optString(TAG_CHANNEL_ID, "");
            mdata.messageType = data.optString(Constants.TAG_MESSAGE_TYPE, "");
            mdata.message = data.optString(Constants.TAG_MESSAGE, "");
            mdata.messageId = data.optString(Constants.TAG_ID, "");
            mdata.messageDate = data.optString("message_date", "");
            mdata.chatTime = data.optString("message_at", "");
            mdata.channelId = data.optString(Constants.TAG_CHANNEL_ID, "");
            mdata.attachment = data.optString(Constants.TAG_ATTACHMENT, "");
            mdata.thumbnail = data.optString(Constants.TAG_THUMBNAIL, "");
            mdata.chatType = data.optString(Constants.TAG_CHAT_TYPE, "");
            mdata.progress = data.optString(Constants.TAG_PROGRESS, "");

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

    public void createGroup(JSONObject jobj) {
        Log.i(TAG, "createGroup: " + jobj);
        mSocket.emit("createGroup", jobj);
    }

    public void setRecentChatReceivedListener(RecentChatReceivedListener listener) {
        recentChatReceivedListener = listener;
    }

    public void setChatCallbackListener(ChatCallbackListener listener) {
        chatCallbackListener = listener;
    }

    public void setStatusCallbackListener(StatusCallbackListener listener) {
        statusCallbackListener = listener;
    }

    public void setRecentStatusReceivedListener(RecentStatusReceivedListener listener) {
        recentStatusReceivedListener = listener;
    }

    public void setGroupChatCallbackListener(GroupChatCallbackListener listener) {
        groupChatCallbackListener = listener;
    }

    public void setGroupRecentCallbackListener(GroupRecentReceivedListener listener) {
        groupRecentReceivedListener = listener;
    }

    public void setOnGroupCreatedListener(OnGroupCreatedListener listener) {
        onGroupCreatedListener = listener;
    }

    public void setSelectContactListener(SelectContactListener listener) {
        selectContactListener = listener;
    }

    public void setUserProfileListener(UserProfileListener listener) {
        userProfileListener = listener;
    }

    public void setCallListener(SignalingInterface listener) {
        signalingInterface = listener;
    }

    public void joinGroup(JSONObject jobj) {
        Log.i(TAG, "joinGroup: " + jobj.toString());
        mSocket.emit("joinGroup", jobj);
    }

    public void modifyGroupInfo(JSONObject jsonObject) {
        Log.i(TAG, "modifyGroupInfo: " + jsonObject);
        mSocket.emit("modifyGroupinfo", jsonObject);
    }

    public void setNewAdminCreatedListener(NewAdminCreatedListener listener) {
        newAdminCreatedListener = listener;
    }

    public void deleteGroup(JSONObject jsonObject) {
        Log.i(TAG, "deleteGroup: " + jsonObject);
        mSocket.emit("trashGroup", jsonObject);
    }

    private Emitter.Listener groupDeleted = new Emitter.Listener() {
        @Override
        public void call(final Object... args) {
            JSONObject jsonObject = (JSONObject) args[0];
            Log.i(TAG, "groupDeleted: " + jsonObject);
            try {
                String groupId = jsonObject.getString(TAG_GROUP_ID);
                dbhelper.deleteGroup(groupId);
                dbhelper.deleteMembers(groupId);
                dbhelper.deleteGroupMessages(groupId);
                dbhelper.deleteGroupRecentChats(groupId);
            } catch (JSONException e) {
                e.printStackTrace();
            }

            if (groupRecentReceivedListener != null) {
                groupRecentReceivedListener.onGroupDeleted(jsonObject);
            }
        }
    };

    public void exitFromGroup(JSONObject jsonObject) {
        Log.i(TAG, "exitFromGroup: " + jsonObject);
        mSocket.emit("exitFromGroup", jsonObject);
    }

    public void emitIceCandidate(IceCandidate iceCandidate, String roomID, String friendID) {
        try {
            JSONObject msg = new JSONObject();
            msg.put("type", "candidate");
            msg.put("label", iceCandidate.sdpMLineIndex);
            msg.put("id", iceCandidate.sdpMid);
            msg.put("candidate", iceCandidate.sdp);
            JSONObject object = new JSONObject();
            object.put("room", roomID);
            object.put("message", msg);
            object.put(Constants.TAG_USER_ID, GetSet.getUserId());
            object.put(Constants.TAG_FRIENDID, friendID);
            mSocket.emit("rtcmessage", object);
            Log.v(TAG, "emitIceCandidate " + object.toString());
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public void emitMessage(SessionDescription message, String roomID, String friendID) {
        try {
            Log.v(TAG, "emitMessage() called with: message = [" + message + "]");
            JSONObject msg = new JSONObject();
            msg.put("type", message.type.canonicalForm());
            msg.put("sdp", message.description);
            JSONObject obj = new JSONObject();
            obj.put("room", roomID);
            obj.put("message", msg);
            obj.put(Constants.TAG_USER_ID, GetSet.getUserId());
            obj.put(Constants.TAG_FRIENDID, friendID);
            Log.v(TAG, obj.toString());
            mSocket.emit("rtcmessage", obj);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void emitMessage(String message, String roomID, String friendID) {
        try {
            Log.v(TAG, "emitMessage() called with: message = [" + message + "]");
            JSONObject obj = new JSONObject();
            obj.put("room", roomID);
            obj.put("message", message);
            obj.put(Constants.TAG_USER_ID, GetSet.getUserId());
            obj.put(Constants.TAG_FRIENDID, friendID);
            Log.v(TAG, obj.toString());
            mSocket.emit("rtcmessage", obj);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void createOrJoin(JSONObject message) {
        Log.v(TAG, "emitInitStatement() called with: event = [" + "create or join" + "], message = [" + message + "]");
        mSocket.emit("create or join", message);
    }

    public void close(String roomID) {
        Log.v(TAG, "close() called with: event");
        mSocket.emit("bye", roomID);
    }

    //callCreated created event.
    private Emitter.Listener callCreated = new Emitter.Listener() {
        @Override
        public void call(final Object... args) {
            Log.i(TAG, "callCreated: " + args[0]);
            try {
                JSONObject data = (JSONObject) args[0];
                String userId = data.optString("caller_id", "");
                if (dbhelper.isUserExist(userId)) {
                    onCallReceive(data);
                } else {
                    getUserInfo(userId, data);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    };

    //room created event.
    private Emitter.Listener onCreated = new Emitter.Listener() {
        @Override
        public void call(final Object... args) {
            Log.i(TAG, "onCreated: " + args[0]);
            if (signalingInterface != null) {
                signalingInterface.onCreatedRoom();
            }
        }
    };

    //room is full event
    private Emitter.Listener onFull = new Emitter.Listener() {
        @Override
        public void call(final Object... args) {
            Log.i(TAG, "onFull: " + args[0]);
        }
    };

    //peer joined event
    private Emitter.Listener onJoin = new Emitter.Listener() {
        @Override
        public void call(final Object... args) {
            Log.i(TAG, "onJoin: " + args[0]);
            if (signalingInterface != null) {
                signalingInterface.onNewPeerJoined();
            }
        }
    };

    //when you joined a chat room successfully
    private Emitter.Listener onJoined = new Emitter.Listener() {
        @Override
        public void call(final Object... args) {
            Log.i(TAG, "onJoined: " + args[0]);
            if (signalingInterface != null) {
                signalingInterface.onJoinedRoom();
            }
        }
    };

    //when you joined a chat room successfully
    private Emitter.Listener onBye = new Emitter.Listener() {
        @Override
        public void call(final Object... args) {
            Log.i(TAG, "onBye: " + args[0]);
            if (signalingInterface != null) {
                signalingInterface.onRemoteHangUp((String) args[0]);
            }
        }
    };

    //messages - SDP and ICE candidates are transferred through this
    private Emitter.Listener onRtcMessage = new Emitter.Listener() {
        @Override
        public void call(final Object... args) {
            Log.i(TAG, "onRtcMessage: " + args[0]);
            if (args[0] instanceof String) {
                Log.v(TAG, "String received :: " + args[0]);
                String data = (String) args[0];
                if (data.equalsIgnoreCase("got user media") && signalingInterface != null) {
                    signalingInterface.onTryToStart();
                }
                if (data.equalsIgnoreCase("bye") && signalingInterface != null) {
                    signalingInterface.onRemoteHangUp(data);
                }
            } else if (args[0] instanceof JSONObject) {
                try {

                    JSONObject data = (JSONObject) args[0];
                    Log.v(TAG, "Json Received :: " + data.toString());
                    String type = data.optString("type", "got user media");
                    if (type.equalsIgnoreCase("offer")) {
                        signalingInterface.onOfferReceived(data);
                    } else if (type.equalsIgnoreCase("answer")) {
                        signalingInterface.onAnswerReceived(data);
                    } else if (type.equalsIgnoreCase("candidate")) {
                        signalingInterface.onIceCandidateReceived(data);
                    } else if (type.equalsIgnoreCase("got user media")) {
                        signalingInterface.onTryToStart();
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    };

    private void getUserInfo(String memberId, JSONObject data) {
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

                        onCallReceive(data);
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

    private void onCallReceive(JSONObject data) {
        String type = data.optString(Constants.TAG_TYPE, "");
        String userId = data.optString("caller_id", "");
        String callId = data.optString("call_id", "");
        String unixStamp = data.optString("created_at", "");
        String call_type = data.optString("call_type", "");

        if (call_type.equalsIgnoreCase("created")) {
            TelephonyManager telephony = (TelephonyManager) mCtx.getSystemService(Context.TELEPHONY_SERVICE);
            int isPhoneCallOn = telephony.getCallState();
            dbhelper.addRecentCall(callId, userId, type, "incoming", unixStamp);
            if (checkIsLoggedInChat()) {
                startCall(true);
            }
//            if (Constants.qbUsersList.size()!=0 && isPhoneCallOn == 0) {
//
//                Intent intent = new Intent(mCtx, CallActivity.class);
//                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//                intent.putExtra("type", type);
//                intent.putExtra("from", "receive");
//                intent.putExtra("user_id", userId);
//                intent.putExtra("call_id", callId);
//                mCtx.startActivity(intent);
//            }
        } else if (call_type.equalsIgnoreCase("ended")) {

        }
    }

    private boolean checkIsLoggedInChat() {
        if (!QBChatService.getInstance().isLoggedIn()) {
            startLoginService();
            ToastUtils.shortToast(R.string.dlg_relogin_wait);
            return false;
        }
        return true;
    }

    private void startLoginService() {
        if (sharedPrefsHelper.hasQbUser()) {
            QBUser qbUser = sharedPrefsHelper.getQbUser();
            LoginService.start(ApplicationClass.getInstance(), qbUser);
        }
    }


    private void startCall(boolean isVideoCall) {
        Log.d(TAG, "Starting Call");

        if (Constants.qbUsersList.size() > Constants.MAX_OPPONENTS_COUNT) {
            ToastUtils.longToast(String.format(ApplicationClass.getInstance().getString(R.string.error_max_opponents_count),
                    Constants.MAX_OPPONENTS_COUNT));
            return;
        }

        ArrayList<Integer> opponentsList = CollectionsUtils.getIdsSelectedOpponents(Constants.qbUsersList);
        QBRTCTypes.QBConferenceType conferenceType = isVideoCall
                ? QBRTCTypes.QBConferenceType.QB_CONFERENCE_TYPE_VIDEO
                : QBRTCTypes.QBConferenceType.QB_CONFERENCE_TYPE_AUDIO;
        Log.d(TAG, "conferenceType = " + conferenceType);

        QBRTCClient qbrtcClient = QBRTCClient.getInstance(ApplicationClass.getInstance());
        QBRTCSession newQbRtcSession = qbrtcClient.createNewSessionWithOpponents(opponentsList, conferenceType);
        WebRtcSessionManager.getInstance(ApplicationClass.getInstance()).setCurrentSession(newQbRtcSession);
        PushNotificationSender.sendPushMessage(opponentsList, GetSet.getUserName());
        CallActivity.start(ApplicationClass.getInstance(), false);
    }

    public void setChannelCallbackListener(ChannelCallbackListener listener) {
        channelCallbackListener = listener;
    }

    public void setChannelChatCallbackListener(ChannelChatCallbackListener listener) {
        channelChatCallbackListener = listener;
    }

    public void setChannelRecentReceivedListener(ChannelRecentReceivedListener listener) {
        channelRecentReceivedListener = listener;
    }

    public void setOnUpdateTabIndication(OnUpdateTabIndication listener) {
        onUpdateTabIndication = listener;
    }

    public void createChannel(JSONObject jsonObject) {
        Log.i(TAG, "createChannel: " + jsonObject);
        mSocket.emit("createChannel", jsonObject);
    }

    public void sendInvitesToSubscribers(JSONObject jObject) {
        Log.i(TAG, "sendChannelInvitation: " + jObject);
        mSocket.emit("sendChannelInvitation", jObject);
    }

    public void subscribeChannel(JSONObject jsonObject) {
        Log.i(TAG, "subscribeChannel: " + jsonObject);
        mSocket.emit("subscribeChannel", jsonObject);
    }

    public void unsubscribeChannel(JSONObject jsonObject, String channelId, String totalSubscribers) {
        Log.i(TAG, "unsubscribeChannel: " + jsonObject);
        mSocket.emit("unsubscribeChannel", jsonObject);
        dbhelper.deleteChannelRecentMessages(channelId);
        dbhelper.deleteChannelMessages(channelId);
        dbhelper.deleteChannel(channelId);
    }

    public void startChannelChat(JSONObject jsonObject) {
        Log.i(TAG, "msgToChannel: " + jsonObject);
        mSocket.emit("msgToChannel", jsonObject);
    }

    public void runTimerTask(String ping) {
//        Log.i(TAG, "runTimerTask: " + ping);
        if (mSocket != null)
            mSocket.emit("ping", ping);
    }

    public void leaveChannel(JSONObject jsonObject, String channelId) {
        Log.e(TAG, "leaveChannel: " + jsonObject);
        mSocket.emit("leaveChannel", jsonObject);
        dbhelper.deleteChannel(channelId);

        if (channelChatCallbackListener != null) {
            channelChatCallbackListener.onChannelDeleted();
        }

        if (channelRecentReceivedListener != null) {
            channelRecentReceivedListener.onChannelDeleted();
        }
    }

    public interface RecentChatReceivedListener {
        void onRecentChatReceived();

        void onUserImageChange(String user_id, String user_image);

        void onBlockStatus(JSONObject data);

        void onUpdateChatStatus(String user_id);

        void onListenTyping(JSONObject data);

        void onPrivacyChanged(JSONObject jsonObject);
    }

    public interface ChatCallbackListener {
        void onReceiveChat(MessagesData mdata);

        void onEndChat(String message_id, String sender_id, String receiverId);

        void onViewChat(String chat_id, String sender_id, String receiverId);

        void onlineStatus(JSONObject data);

        void onListenTyping(JSONObject data);

        void onBlockStatus(JSONObject data);

        void onUserImageChange(String user_id, String user_image);

        void onGetUpdateFromDB();

        void onUploadListen(String message_id, String attachment, String progress);

        void onPrivacyChanged(JSONObject jsonObject);
    }

    public interface GroupChatCallbackListener {

        void onGroupChatReceive(GroupMessage mdata);

        void onListenGroupTyping(JSONObject data);

        void onMemberExited(JSONObject data);

        void onUploadListen(String message_id, String attachment, String progress);

        void onGetUpdateFromDB();

        void onUpdateGroupInfo(GroupMessage groupMessage);
    }

    public interface StatusCallbackListener {
        void onReceiveStatus(MessagesData mdata);

        void onEndStatus(String status_id, String sender_id, String receiverId);

        void onViewStatus(String status_id, String user_id);

        void onBlockStatus(JSONObject data);

        void onUserImageChange(String user_id, String user_image);

        void onGetUpdateFromDB();

        void onUploadListen(String status_id, String attachment, String progress);

        void onPrivacyChanged(JSONObject jsonObject);
    }

    public interface RecentStatusReceivedListener {
        void onRecentStatusReceived();

        void onUserImageChange(String user_id, String user_image);

        void onBlockStatus(JSONObject data);

        void onUpdateStatus(String user_id);

        void onPrivacyChanged(JSONObject jsonObject);
    }

    public interface GroupRecentReceivedListener {

        void onGroupCreated();

        void onGroupRecentReceived();

        void onGroupModified(JSONObject data);

        void onUserImageChange(String user_id, String user_image);

        void onMemberExited(JSONObject data);

        void onUpdateChatStatus(String user_id);

        void onListenGroupTyping(JSONObject data);

        void onGroupDeleted(JSONObject jsonObject);

    }

    public interface NewAdminCreatedListener {
        void onNewAdminCreated();
    }

    public interface OnGroupCreatedListener {
        void onGroupCreated(JSONObject data);
    }

    public interface SignalingInterface {
        void onRemoteHangUp(String msg);

        void onOfferReceived(JSONObject data);

        void onAnswerReceived(JSONObject data);

        void onIceCandidateReceived(JSONObject data);

        void onTryToStart();

        void onCreatedRoom();

        void onJoinedRoom();

        void onNewPeerJoined();
    }

    public interface SelectContactListener {
        void onUserImageChange(String user_id, String user_image);

        void onBlockStatus(JSONObject data);

        void onPrivacyChanged(JSONObject jsonObject);
    }

    public interface UserProfileListener {
        void onPrivacyChanged(JSONObject jsonObject);
    }

    public interface ChannelCallbackListener {
        void onChannelCreated(JSONObject jsonObject);
    }

    public interface ChannelChatCallbackListener {
        void onChannelChatReceive(ChannelMessage mdata);

        void onAdminChatReceive(AdminChannelMsg.Result adminMessage);

        void onUploadListen(String message_id, String attachment, String progress);

        void onChannelDeleted();

        void onGetUpdateFromDB();

//        void onChannelBlocked(String channelId);
    }

    public interface ChannelRecentReceivedListener {

        void onChannelCreated(JSONObject jsonObject);

        void onChannelRecentReceived();

        void onChannelDeleted();

        void onAdminChatReceive();

        void onChannelInviteReceived(JSONObject jsonObject);
    }

    public interface OnUpdateTabIndication {
        void updateIndication();
    }

    public interface CallReceivedFromSocket {
        void onCallReceived(Object args);
    }
}

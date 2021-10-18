package com.topzi.chat.helper;

import android.app.Dialog;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.droidninja.imageeditengine.views.imagezoom.easing.Linear;
import com.google.android.material.snackbar.Snackbar;
import com.topzi.chat.R;
import com.topzi.chat.activity.ApplicationClass;
import com.topzi.chat.activity.ChatActivity;
import com.topzi.chat.activity.DialogActivity;
import com.topzi.chat.activity.SelectContact;
import com.topzi.chat.external.RandomString;
import com.topzi.chat.model.ContactsData;
import com.topzi.chat.model.DataStorageModel;
import com.topzi.chat.model.MessagesData;
import com.topzi.chat.utils.Constants;
import com.topzi.chat.utils.GetSet;

import org.json.JSONException;
import org.json.JSONObject;

import de.hdodenhof.circleimageview.CircleImageView;

import static android.net.TrafficStats.getUidTxBytes;
import static com.topzi.chat.helper.MyFirebaseMessagingService.humanReadableByteCountSI;
import static com.topzi.chat.helper.NetworkUtil.NOT_CONNECT;
import static com.topzi.chat.utils.Constants.TAG_FRIENDID;

public class PopUpService extends Service {

    private static final String TAG = PopUpService.class.getSimpleName();
    WindowManager mWindowManager;
    View mView;
    Animation mAnimation;
    Bundle data;
    String message1 = "";
    String type = "";
    ContactsData.Result results;
    DatabaseHandler dbhelper;
    SharedPreferences prefData;
    SharedPreferences.Editor editorData;
    private long txMesBytesFinal;
    Handler handler = new Handler();
    Runnable runnable;
    boolean  meTyping;
    SocketConnection socketConnection;
    public static boolean isAdd = false;
    LinearLayout dialog;
    CircleImageView userImg;
    TextView imageButton;
    TextView yes;
    ImageView send;
    EditText editText;
    TextView username;
    TextView online;
    TextView message;

    public PopUpService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        data = null;
        if (intent != null && intent.getExtras() != null) {
            data = intent.getExtras();
            message1 = data.getString("message");
            type = data.getString("type");
            Log.e("LLLL_Mes11: ",message1);
        }

        registerOverlayReceiver();
        showDialog();

        return super.onStartCommand(intent, flags, startId);
    }

    private void showDialog(){

        mWindowManager = (WindowManager) getSystemService(WINDOW_SERVICE);

        mView = View.inflate(getApplicationContext(), R.layout.dialoge_pop_up, null);
        mView.setTag(TAG);

        mView.setVisibility(View.VISIBLE);
        dbhelper = DatabaseHandler.getInstance(getApplicationContext());
        prefData = PopUpService.this.getSharedPreferences(Constants.NETWORK_USAGE, MODE_PRIVATE);
        editorData = prefData.edit();
        socketConnection = SocketConnection.getInstance(this);
        dialog = (LinearLayout) mView.findViewById(R.id.dialog);
        userImg = mView.findViewById(R.id.userImg);
        imageButton = (TextView) mView.findViewById(R.id.no);
        yes = (TextView) mView.findViewById(R.id.yes);
        send = (ImageView) mView.findViewById(R.id.send);
        editText = mView.findViewById(R.id.editText);

        imageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isAdd = false;
                mView.setVisibility(View.GONE);
            }
        });

        username = (TextView) mView.findViewById(R.id.userName);
        online = (TextView) mView.findViewById(R.id.online);
        message = (TextView) mView.findViewById(R.id.message);

        try {
            JSONObject jsonObject = new JSONObject(message1);

            String sender_id = jsonObject.optString("sender_id", "");
            String mes_type = jsonObject.optString("message_type", "");
            String mes = jsonObject.optString("message", "");
            results = dbhelper.getContactDetail(sender_id);
            username.setText(ApplicationClass.getContactName(this, results.phone_no));

            message.setText(mes);

            if (results.blockedme == null || !results.blockedme.equals("block")) {
                DialogActivity.setProfileImage(dbhelper.getContactDetail(sender_id), userImg, this);
                online.setVisibility(View.VISIBLE);
            } else {
                Glide.with(getApplicationContext())
                        .load(R.drawable.person)
                        .apply(RequestOptions.circleCropTransform()
                                .placeholder(R.drawable.person)
                                .error(R.drawable.person))
                        .into(userImg);
                online.setVisibility(View.GONE);
            }

            yes.setOnClickListener(v -> {
                mView.setVisibility(View.GONE);
                isAdd =false;
                Intent i = new Intent(PopUpService.this, ChatActivity.class);
                i.putExtra("user_id", sender_id);
                i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(i);
            });

            editText.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                }

                @Override
                public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                    if (charSequence.length() > 0) {
                        send.setVisibility(View.VISIBLE);
                    } else {
                        send.setVisibility(View.GONE);
                    }
                    if ((results.blockedme == null || !results.blockedme.equals("block")) && (results.blockedbyme == null || !results.blockedbyme.equals("block"))) {
                        if (runnable != null)
                            handler.removeCallbacks(runnable);
                        if (!meTyping) {
                            meTyping = true;
                        }
                        try {
                            JSONObject jsonObject = new JSONObject();
                            jsonObject.put(Constants.TAG_SENDER_ID, GetSet.getUserId());
                            jsonObject.put(Constants.TAG_RECEIVER_ID, sender_id);
                            jsonObject.put(Constants.TAG_CHAT_ID, sender_id + GetSet.getUserId());
                            jsonObject.put("type", "typing");
                            socketConnection.typing(jsonObject);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }

                @Override
                public void afterTextChanged(Editable editable) {
                    if ((results.blockedme == null || !results.blockedme.equals("block")) && (results.blockedbyme == null || !results.blockedbyme.equals("block"))) {
                        runnable = new Runnable() {
                            public void run() {
                                meTyping = false;
                                try {
                                    JSONObject jsonObject = new JSONObject();
                                    jsonObject.put(Constants.TAG_SENDER_ID, GetSet.getUserId());
                                    jsonObject.put(Constants.TAG_RECEIVER_ID, sender_id);
                                    jsonObject.put(Constants.TAG_CHAT_ID, sender_id + GetSet.getUserId());
                                    jsonObject.put("type", "untyping");
                                    socketConnection.typing(jsonObject);
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            }
                        };
                        handler.postDelayed(runnable, 1000);
                    }
                }
            });

            send.setOnClickListener(v -> {
                if (isNetworkConnected().equals(NOT_CONNECT)) {
                    Toast.makeText(PopUpService.this, getString(R.string.network_failure), Toast.LENGTH_SHORT).show();
                }  else {
                    if (editText.getText().toString().trim().length() > 0) {
                        int UID = android.os.Process.myUid();

                        Long txBytes = prefData.getLong("MesSentTotal", getUidTxBytes(UID));

                        txMesBytesFinal = getUidTxBytes(UID) - txBytes;

                        Log.e("LLLLL_Mes_Size: ", "      " + humanReadableByteCountSI(txMesBytesFinal));

                        DataStorageModel dataStorageModel = dbhelper.getRecord(sender_id);
                        if (dataStorageModel.getData_id() != null)
                            dbhelper.addDataStorage(sender_id,
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

                        String unixStamp = String.valueOf(System.currentTimeMillis() / 1000L);
                        String textMsg = editText.getText().toString().trim();
                    /*String encryptedMsg = "";
                    try {
                        CryptLib cryptLib = new CryptLib();
                        encryptedMsg = cryptLib.encryptPlainTextWithRandomIV(textMsg,"123");
                    } catch (NoSuchAlgorithmException | NoSuchPaddingException e) {
                        Log.e(TAG, "onClick: "+e.getMessage());
                        e.printStackTrace();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }*/
                        String chatId = GetSet.getUserId() + sender_id;
                        RandomString randomString = new RandomString(10);
                        String messageId = GetSet.getUserId() + randomString.nextString();
                        try {
                            if (results.blockedme == null || !results.blockedme.equals("block")) {
//                                JSONObject jobj = new JSONObject();
                                JSONObject message2 = new JSONObject();
                                message2.put(Constants.TAG_USER_ID, GetSet.getUserId());
                                message2.put(Constants.TAG_USER_NAME, GetSet.getUserName());
                                message2.put(Constants.TAG_MESSAGE_TYPE, "text");
                                message2.put(Constants.TAG_MESSAGE, textMsg);
                                message2.put(Constants.TAG_CHAT_TIME, unixStamp);
                                message2.put(Constants.TAG_CHAT_ID, chatId);
                                message2.put(Constants.TAG_MESSAGE_ID, messageId);
                                message2.put(TAG_FRIENDID, sender_id);
                                message2.put(Constants.TAG_SENDER_ID, GetSet.getUserId());
                                message2.put(Constants.TAG_CHAT_TYPE, Constants.TAG_SINGLE);
//                                jobj.put(Constants.TAG_SENDER_ID, GetSet.getUserId());
//                                jobj.put(Constants.TAG_RECEIVER_ID, userId);
//                                jobj.put("message_data", message);

                                socketConnection.startChat(message2);

                                Log.v("startchat", "startchat=" + message2);
                            }

                            editorData.putLong("MesSentTotal", getUidTxBytes(UID));
                            editorData.apply();
                            editorData.commit();

                            long totalrx = prefData.getLong("MesSent", 0) + txMesBytesFinal;
                            editorData.putLong("MesSent", totalrx);
                            editorData.apply();
                            long count = prefData.getLong("sentMesCount", 0);
                            editorData.putLong("sentMesCount", count + 1);
                            editorData.apply();

                            Log.e("LLLLL_MesSent: ", humanReadableByteCountSI(totalrx));
                            editorData.commit();

                            dbhelper.addMessageDatas(chatId, messageId, GetSet.getUserId(), GetSet.getUserName(),
                                    "text", textMsg, "", "", "", "", "",
                                    "", unixStamp, sender_id, GetSet.getUserId(), "", "", "", "");

                            dbhelper.addRecentMessages(chatId, sender_id, messageId, unixStamp, "0");
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                        MessagesData data = new MessagesData();
                        data.user_id = GetSet.getUserId();
                        data.message_type = "text";
                        data.message = textMsg;
                        data.message_id = messageId;
                        data.chat_time = unixStamp;
                        data.delivery_status = "";

                        editText.setText("");
                    } else {
                        editText.setError(getString(R.string.please_enter_your_message));
                    }
                }
            });

        } catch (JSONException e) {
            e.printStackTrace();
        }

        int LAYOUT_FLAG;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            LAYOUT_FLAG = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
        } else {
            LAYOUT_FLAG = WindowManager.LayoutParams.TYPE_PHONE;
        }

        final WindowManager.LayoutParams mLayoutParams = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.MATCH_PARENT,
                LAYOUT_FLAG,
                WindowManager.LayoutParams.FLAG_FULLSCREEN,
                PixelFormat.TRANSLUCENT);

        mView.setVisibility(View.VISIBLE);
        mAnimation = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.fade_in);
        mView.startAnimation(mAnimation);

        isAdd = true;
        mWindowManager.addView(mView, mLayoutParams);

    }

    private void hideDialog(){
        if(mView != null && mWindowManager != null){
            mWindowManager.removeView(mView);
            mView = null;
        }
    }

    @Override
    public void onDestroy() {
        unregisterOverlayReceiver();
        super.onDestroy();
    }

    private void registerOverlayReceiver() {
        IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_SCREEN_ON);
        filter.addAction(Intent.ACTION_SCREEN_OFF);
        filter.addAction(Intent.ACTION_USER_PRESENT);
        registerReceiver(overlayReceiver, filter);
    }

    private void unregisterOverlayReceiver() {
        hideDialog();
    }

    private BroadcastReceiver overlayReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            Log.e("LLLL_Mes: ", "[onReceive]" + action);
            showDialog();
            if (action.equals(Intent.ACTION_SCREEN_ON)) {
                showDialog();
            }
            else if (action.equals(Intent.ACTION_USER_PRESENT)) {
                hideDialog();
            }
            else if (action.equals(Intent.ACTION_SCREEN_OFF)) {
                hideDialog();
            }
        }
    };

    private String isNetworkConnected() {
        return NetworkUtil.getConnectivityStatusString(this);
    }

}

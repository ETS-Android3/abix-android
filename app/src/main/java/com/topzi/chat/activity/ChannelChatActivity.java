package com.topzi.chat.activity;

import android.app.Activity;
import android.app.Dialog;
import android.app.NotificationManager;
import android.content.ActivityNotFoundException;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.ContactsContract;
import android.provider.MediaStore;

import androidx.annotation.NonNull;

import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.snackbar.Snackbar;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.appcompat.widget.Toolbar;
import androidx.transition.TransitionManager;

import android.text.Editable;
import android.text.Html;
import android.text.TextWatcher;
import android.text.format.DateFormat;
import android.text.util.Linkify;
import android.util.Log;
import android.view.Display;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.AnimationUtils;
import android.webkit.MimeTypeMap;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.topzi.chat.BuildConfig;
import com.topzi.chat.R;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.Target;
import com.google.gson.Gson;
import com.topzi.chat.external.EndlessRecyclerOnScrollListener;
import com.topzi.chat.external.ImagePicker;
import com.topzi.chat.external.ImageUtils;
import com.topzi.chat.external.ProgressWheel;
import com.topzi.chat.external.RandomString;
import com.topzi.chat.external.RecyclerItemClickListener;
import com.topzi.chat.helper.DatabaseHandler;
import com.topzi.chat.helper.DownloadFiles;
import com.topzi.chat.helper.FileUploadService;
import com.topzi.chat.helper.ImageCompression;
import com.topzi.chat.helper.ImageDownloader;
import com.topzi.chat.helper.NetworkReceiver;
import com.topzi.chat.helper.NetworkUtil;
import com.topzi.chat.helper.SocketConnection;
import com.topzi.chat.helper.StorageManager;
import com.topzi.chat.helper.Utils;
import com.topzi.chat.model.AdminChannel;
import com.topzi.chat.model.AdminChannelMsg;
import com.topzi.chat.model.ChannelMessage;
import com.topzi.chat.model.ChannelResult;
import com.topzi.chat.utils.ApiClient;
import com.topzi.chat.utils.ApiInterface;
import com.topzi.chat.utils.Constants;
import com.topzi.chat.utils.GetSet;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;
import droidninja.filepicker.FilePickerBuilder;
import droidninja.filepicker.FilePickerConst;
import droidninja.filepicker.models.sort.SortingTypes;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static android.Manifest.permission.CAMERA;
import static android.Manifest.permission.RECORD_AUDIO;
import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;
import static com.topzi.chat.helper.NetworkUtil.NOT_CONNECT;
import static com.topzi.chat.activity.ChannelChatActivity.MessageListAdapter.VIEW_TYPE_DATE;
import static com.topzi.chat.utils.Constants.TAG_GROUP_ID;
import static com.topzi.chat.utils.Constants.TAG_MEMBER_ID;
import static com.topzi.chat.utils.Constants.TRUE;

public class ChannelChatActivity extends BaseActivity implements View.OnClickListener, TextWatcher, SocketConnection.ChannelChatCallbackListener {
    EditText editText;
    String channelId;
    List<ChannelMessage> messagesList = new ArrayList<>();
    String TAG = this.getClass().getSimpleName();
    RecyclerView recyclerView;
    TextView username, online, txtMembers, txtBlocked;
    RelativeLayout chatUserLay, mainLay, attachmentsLay, imageViewLay, bottomLay, forwordLay;
    ImageView attachbtn, optionbtn, backbtn, send, audioCallBtn, videoCallBtn, cameraBtn, closeBtn,
            galleryBtn, fileBtn, audioBtn, locationBtn, contactBtn, imageView, forwordBtn, copyBtn, deleteBtn;
    CircleImageView userimage;
    ImageView imgChatBg;
    Display display;
    boolean visible, stopLoading = false, meTyping, chatLongPressed = false;
    int totalMsg;
    SocketConnection socketConnection;
    LinearLayoutManager linearLayoutManager;
    MessageListAdapter messageListAdapter;
    DatabaseHandler dbhelper;
    StorageManager storageManager;
    ApiInterface apiInterface;
    BottomSheetBehavior bottomSheetBehavior;
    ArrayList<String> pathsAry = new ArrayList<>();
    Handler handler = new Handler();
    Runnable runnable;
    EndlessRecyclerOnScrollListener endlessRecyclerOnScrollListener;
    public static String tempChannelId = "";
    ChannelResult.Result channelData;
    ArrayList<ChannelMessage> selectedChatPos = new ArrayList<>();
    private boolean isFromNotification;
    String channelAdminId = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_chat);
        if (getIntent().getStringExtra("notification") != null) {
            Constants.isChannelChatOpened = true;
            isFromNotification = true;
            NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            if (notificationManager != null) {
                notificationManager.cancelAll();
            }
        }
        if (Constants.channelContext != null && Constants.isChannelChatOpened) {
            ((Activity) Constants.channelContext).finish();
        }
        Constants.channelContext = this;
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        imgChatBg = findViewById(R.id.img_chat_bg);
        recyclerView = findViewById(R.id.recyclerView);
        send = findViewById(R.id.send);
        editText = findViewById(R.id.editText);
        chatUserLay = findViewById(R.id.chatUserLay);
        userimage = findViewById(R.id.userImg);
        username = findViewById(R.id.userName);
        txtMembers = findViewById(R.id.txtMembers);
        online = findViewById(R.id.online);
        attachbtn = findViewById(R.id.attachbtn);
        audioCallBtn = findViewById(R.id.audioCallBtn);
        videoCallBtn = findViewById(R.id.videoCallBtn);
        optionbtn = findViewById(R.id.optionbtn);
        backbtn = findViewById(R.id.backbtn);
        bottomLay = findViewById(R.id.bottom);
        mainLay = findViewById(R.id.mainLay);
        attachmentsLay = findViewById(R.id.attachmentsLay);
        cameraBtn = findViewById(R.id.cameraBtn);
        galleryBtn = findViewById(R.id.galleryBtn);
        fileBtn = findViewById(R.id.fileBtn);
        audioBtn = findViewById(R.id.audioBtn);
        locationBtn = findViewById(R.id.locationBtn);
        contactBtn = findViewById(R.id.contactBtn);
        imageViewLay = findViewById(R.id.imageViewLay);
        imageView = findViewById(R.id.imageView);
        closeBtn = findViewById(R.id.closeBtn);
        forwordLay = findViewById(R.id.forwordLay);
        forwordBtn = findViewById(R.id.forwordBtn);
        copyBtn = findViewById(R.id.copyBtn);
        deleteBtn = findViewById(R.id.deleteBtn);
        txtBlocked = findViewById(R.id.txtBlocked);

        Glide.with(ChannelChatActivity.this)
                .load(R.drawable.chat_bg)
                .into(imgChatBg);

        socketConnection = SocketConnection.getInstance(this);
        SocketConnection.getInstance(this).setChannelChatCallbackListener(this);
        bottomSheetBehavior = BottomSheetBehavior.from(imageViewLay);
        apiInterface = ApiClient.getClient().create(ApiInterface.class);
        dbhelper = DatabaseHandler.getInstance(this);
        storageManager = StorageManager.getInstance(this);
        display = getWindowManager().getDefaultDisplay();

        channelId = getIntent().getStringExtra(Constants.TAG_CHANNEL_ID);
        channelData = dbhelper.getChannelInfo(channelId);

        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        if (notificationManager != null) {
            notificationManager.cancel(channelData.channelName, 0);
            notificationManager.cancel("New Channel", 0);
        }

        backbtn.setColorFilter(ContextCompat.getColor(getApplicationContext(), R.color.primarytext));
        // set visibility status
        chatUserLay.setVisibility(View.VISIBLE);
        backbtn.setVisibility(View.VISIBLE);
        audioCallBtn.setVisibility(View.GONE);
        videoCallBtn.setVisibility(View.GONE);
        optionbtn.setVisibility(View.VISIBLE);
        txtMembers.setVisibility(View.VISIBLE);

        totalMsg = dbhelper.getChannelMessagesCount(channelId);
        Log.v("totalMsg", "totalMsg=" + totalMsg);

        messagesList.addAll(getMessagesAry(dbhelper.getChannelMessages(channelId, "0", "20"), null));
        linearLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        linearLayoutManager.setReverseLayout(true);
        linearLayoutManager.setStackFromEnd(true);
        recyclerView.setLayoutManager(linearLayoutManager);

        messageListAdapter = new MessageListAdapter(this, messagesList);
        recyclerView.setAdapter(messageListAdapter);

        recyclerView.post(new Runnable() {
            @Override
            public void run() {
                recyclerView.scrollToPosition(0);
            }
        });

        DividerItemDecoration divider = new DividerItemDecoration(recyclerView.getContext(),
                linearLayoutManager.getOrientation());
        divider.setDrawable(getResources().getDrawable(R.drawable.emptychat_divider));
        recyclerView.addItemDecoration(divider);

        send.setOnClickListener(this);
        backbtn.setOnClickListener(this);
        attachbtn.setOnClickListener(this);
        optionbtn.setOnClickListener(this);
        userimage.setOnClickListener(this);
        audioCallBtn.setOnClickListener(this);
        videoCallBtn.setOnClickListener(this);
        cameraBtn.setOnClickListener(this);
        galleryBtn.setOnClickListener(this);
        fileBtn.setOnClickListener(this);
        audioBtn.setOnClickListener(this);
        locationBtn.setOnClickListener(this);
        contactBtn.setOnClickListener(this);
        editText.addTextChangedListener(this);
        chatUserLay.setOnClickListener(this);
        closeBtn.setOnClickListener(this);
        copyBtn.setOnClickListener(this);
        forwordBtn.setOnClickListener(this);
        deleteBtn.setOnClickListener(this);

        whileViewChat();

        bottomSheetBehavior.setBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {
            @Override
            public void onStateChanged(@NonNull View bottomSheet, int newState) {

            }

            @Override
            public void onSlide(@NonNull View bottomSheet, float slideOffset) {
                Log.v("slideOffset", "slideOffset=" + slideOffset);
                ImageView imgBg = bottomSheet.findViewById(R.id.imgBg);
                imgBg.setAlpha(slideOffset);
            }
        });

        endlessRecyclerOnScrollListener = new EndlessRecyclerOnScrollListener(linearLayoutManager) {
            @Override
            public void onLoadMore(int page, int totalItemsCount, RecyclerView view) {
                Log.v("current_page", "current_page=" + page + "&totalItems=" + totalItemsCount);
                final List<ChannelMessage> tmpList = new ArrayList<>(dbhelper.getChannelMessages(channelId, String.valueOf(page * 20), "20"));
                if (tmpList.size() == 0 && !stopLoading) {
                    stopLoading = true;
                    messagesList.addAll(getMessagesAry(tmpList, messagesList.get(messagesList.size() - 1)));
                } else {
                    messagesList.addAll(getMessagesAry(tmpList, null));
                }
                Log.v("current_page", "messagesList=" + messagesList.size());
                recyclerView.post(new Runnable() {
                    public void run() {
                        messageListAdapter.notifyDataSetChanged();
                    }
                });
            }
        };
        recyclerView.addOnScrollListener(endlessRecyclerOnScrollListener);

        recyclerView.addOnItemTouchListener(chatItemClick(this, recyclerView));
    }

    private void getChannelInfo(String channelId) {
        channelData = dbhelper.getChannelInfo(channelId);
        setUI(channelData);
        if (NetworkReceiver.isConnected()) {
            JSONArray jsonArray = new JSONArray();
            jsonArray.put(channelId);
            Call<ChannelResult> call = apiInterface.getChannelInfo(GetSet.getToken(), jsonArray);
            call.enqueue(new Callback<ChannelResult>() {
                @Override
                public void onResponse(Call<ChannelResult> call, Response<ChannelResult> response) {
                    Log.i(TAG, "getChannelInfo: " + new Gson().toJson(response));
                    if (response.body().status.equalsIgnoreCase(Constants.TRUE)) {
                        for (ChannelResult.Result result : response.body().result) {
                            dbhelper.updateChannelInfo(result.channelId, result.channelName, result.channelDes, result.channelImage,
                                    result.channelType != null ? result.channelType : Constants.TAG_PUBLIC, result.channelAdminId != null ? result.channelAdminId : "", result.channelAdminName, result.totalSubscribers, result.blockStatus);
                            if (channelId.equalsIgnoreCase(result.channelId)) {
                                channelData = dbhelper.getChannelInfo(result.channelId);
                                setUI(channelData);
                            }
                        }
                    }
                }

                @Override
                public void onFailure(Call<ChannelResult> call, Throwable t) {
                    Log.e(TAG, "getChannelInfo: " + t.getMessage());
                    call.cancel();
                    channelData = dbhelper.getChannelInfo(channelId);
                    setUI(channelData);
                }
            });
        }
    }

    @Override
    public void onNetworkChange(boolean isConnected) {
//        if (isConnected) {
//            online.setVisibility(View.VISIBLE);
//        } else {
//            online.setVisibility(View.GONE);
//        }
    }

    private List<ChannelMessage> getMessagesAry(List<ChannelMessage> tmpList, ChannelMessage lastData) {
        List<ChannelMessage> msgList = new ArrayList<>();
        if (tmpList.size() == 0 && lastData != null) {
            ChannelMessage channelMessage = new ChannelMessage();
            channelMessage.messageType = "date";
            channelMessage.chatTime = lastData.chatTime;
            msgList.add(channelMessage);
            Log.v("diff", "diff pos=ss" + "&msg=" + lastData.message);
        } else {
            for (int i = 0; i < tmpList.size(); i++) {
                Calendar cal1 = Calendar.getInstance();
                cal1.setTimeInMillis(Long.parseLong(tmpList.get(i).chatTime) * 1000L);

                if (i + 1 < tmpList.size()) {
                    Calendar cal2 = Calendar.getInstance();
                    cal2.setTimeInMillis(Long.parseLong(tmpList.get(i + 1).chatTime) * 1000L);

                    boolean sameDay = cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
                            cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR);

                    if (sameDay) {
                        msgList.add(tmpList.get(i));
                        Log.v("diff", "same pos=" + i + "&msg=" + tmpList.get(i).message);
                    } else {
                        msgList.add(tmpList.get(i));
                        ChannelMessage channelMessage = new ChannelMessage();
                        channelMessage.messageType = "date";
                        channelMessage.chatTime = tmpList.get(i).chatTime;
                        msgList.add(channelMessage);
                        Log.v("diff", "diff pos=" + i + "&msg=" + tmpList.get(i).message);
                    }
                } else {
                    msgList.add(tmpList.get(i));
                }
            }
        }
        return msgList;
    }

    private void whileViewChat() {
        dbhelper.updateChannelMessageReadStatus(channelId);
        dbhelper.resetUnseenChannelMessagesCount(channelId);
        dbhelper.updateChannelReadData(channelId, Constants.TAG_DELIVERY_STATUS, "read");
    }

    @Override
    public void onChannelChatReceive(ChannelMessage result) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Log.i(TAG, "onChannelChatReceive: " + new Gson().toJson(result));
                if (result.channelId.equalsIgnoreCase(channelId)) {
                    messagesList.add(0, result);
                    if (messageListAdapter != null) {
                        messageListAdapter.notifyItemInserted(0);
                        recyclerView.smoothScrollToPosition(0);
                    }

                    if (result.messageType.equalsIgnoreCase("subject") || result.messageType.equalsIgnoreCase("channel_image")) {
                        username.setText(result.channelName);
                        Glide.with(ChannelChatActivity.this).load(Constants.CHANNEL_IMG_PATH + result.attachment)
                                .apply(RequestOptions.circleCropTransform().placeholder(R.drawable.change_camera).error(R.drawable.change_camera))
                                .into(userimage);
                    }

                    whileViewChat();
                }
            }
        });
    }

    @Override
    public void onAdminChatReceive(AdminChannelMsg.Result result) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {

                if (result.channelId.equalsIgnoreCase(channelId)) {
                    ChannelMessage channelMessage = new ChannelMessage();
                    channelMessage.channelId = result.channelId;
                    channelMessage.channelName = channelData.channelName != null ? channelData.channelName : "";
                    channelMessage.chatTime = result.chatTime;
                    channelMessage.message = result.message;
                    channelMessage.messageId = result.messageId;
                    channelMessage.messageType = result.messageType;
                    channelMessage.thumbnail = result.thumbnail;
                    channelMessage.attachment = result.attachment;
                    messagesList.add(0, channelMessage);
                    if (messageListAdapter != null) {
                        messageListAdapter.notifyItemInserted(0);
                        recyclerView.smoothScrollToPosition(0);
                    }

                    whileViewChat();
                }

            }
        });
    }

    @Override
    public void onUploadListen(final String message_id, final String attachment, final String progress) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                for (int i = 0; i < messagesList.size(); i++) {
                    if (message_id.equals(messagesList.get(i).messageId)) {
                        Log.v("checkChat", "onPostExecute");
                        messagesList.get(i).attachment = attachment;
                        messagesList.get(i).progress = progress;
                        messageListAdapter.notifyItemChanged(i);
                        break;
                    }
                }
            }
        });
    }

    @Override
    public void onGetUpdateFromDB() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                int currentCount = dbhelper.getChannelMessagesCount(channelId);
                if (totalMsg != currentCount) {
                    messagesList.clear();
                    if (endlessRecyclerOnScrollListener != null) {
                        endlessRecyclerOnScrollListener.resetState();
                    }
                    messagesList.addAll(getMessagesAry(dbhelper.getChannelMessages(channelId, "0", "20"), null));
                    messageListAdapter.notifyDataSetChanged();
                    recyclerView.post(new Runnable() {
                        @Override
                        public void run() {
                            recyclerView.scrollToPosition(0);
                        }
                    });

                    whileViewChat();
                } else if (isFromNotification) {
                    messagesList.clear();
                    if (endlessRecyclerOnScrollListener != null) {
                        endlessRecyclerOnScrollListener.resetState();
                    }
                    messagesList.addAll(getMessagesAry(dbhelper.getChannelMessages(channelId, "0", "20"), null));
                    messageListAdapter.notifyDataSetChanged();
                    recyclerView.post(new Runnable() {
                        @Override
                        public void run() {
                            recyclerView.scrollToPosition(0);
                        }
                    });

                    whileViewChat();

                }
            }
        });
    }

//    @Override
//    public void onChannelBlocked(String channelId) {
//        runOnUiThread(new Runnable() {
//            @Override
//            public void run() {
//                channelData = dbhelper.getChannelInfo(channelId);
//                setUI(channelData);
//            }
//        });
//    }

    @Override
    public void onChannelDeleted() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                finish();
            }
        });
    }

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
        if (runnable != null)
            handler.removeCallbacks(runnable);
        if (!meTyping) {
            meTyping = true;
        }
    }

    @Override
    public void afterTextChanged(Editable editable) {
        runnable = new Runnable() {
            public void run() {
                meTyping = false;
            }
        };
        handler.postDelayed(runnable, 500);
    }

    @Override
    public void onPointerCaptureChanged(boolean hasCapture) {

    }

    private void getMsgFromAdminChannels() {
        String timeStamp;
        if (messagesList.size() > 0) {
            timeStamp = messagesList.get(0).chatTime;
        } else {
            timeStamp = "" + (System.currentTimeMillis() / 1000);
        }
        Log.i(TAG, "timeStamp: " + timeStamp);
        Call<AdminChannelMsg> call3 = apiInterface.getMsgFromAdminChannels(GetSet.getToken(), timeStamp);
        call3.enqueue(new Callback<AdminChannelMsg>() {
            @Override
            public void onResponse(Call<AdminChannelMsg> call, Response<AdminChannelMsg> response) {
                if (response.body().status.equalsIgnoreCase(Constants.TRUE)) {
                    for (AdminChannelMsg.Result result : response.body().result) {
                        Log.e(TAG, "getMsgFromAdminChannels: " + result.messageId);
                        ChannelMessage channelMessage = new ChannelMessage();
                        channelMessage.channelId = result.channelId;
                        channelMessage.channelName = channelData.channelName != null ? channelData.channelName : "";
                        channelMessage.chatTime = result.chatTime;
                        channelMessage.message = result.message;
                        channelMessage.messageId = result.messageId;
                        channelMessage.messageType = result.messageType;
                        messagesList.add(0, channelMessage);
                        if (messageListAdapter != null) {
                            messageListAdapter.notifyItemInserted(0);
                            recyclerView.smoothScrollToPosition(0);
                        }
                    }
                }
            }

            @Override
            public void onFailure(Call<AdminChannelMsg> call, Throwable t) {
                Log.e(TAG, "getMsgFromAdminChannels: " + t.getMessage());
                call.cancel();
            }
        });
    }

    public class MessageListAdapter extends RecyclerView.Adapter {
        private static final int VIEW_TYPE_MESSAGE_SENT = 1;
        private static final int VIEW_TYPE_MESSAGE_RECEIVED = 2;
        private static final int VIEW_TYPE_IMAGE_SENT = 3;
        private static final int VIEW_TYPE_IMAGE_RECEIVED = 4;
        private static final int VIEW_TYPE_CONTACT_SENT = 5;
        private static final int VIEW_TYPE_CONTACT_RECEIVED = 6;
        private static final int VIEW_TYPE_FILE_SENT = 7;
        private static final int VIEW_TYPE_FILE_RECEIVED = 8;
        public static final int VIEW_TYPE_DATE = 9;

        private Context mContext;
        private List<ChannelMessage> mMessageList;

        public MessageListAdapter(Context context, List<ChannelMessage> messageList) {
            mContext = context;
            mMessageList = messageList;
        }

        @Override
        public int getItemCount() {
            return mMessageList.size();
        }

        // Determines the appropriate ViewType according to the sender of the message.
        @Override
        public int getItemViewType(int position) {
            final ChannelMessage message = mMessageList.get(position);

            if (channelData.channelAdminId != null && channelData.channelAdminId.equalsIgnoreCase(GetSet.getUserId())) {
                switch (message.messageType) {
                    case "text":
                        return VIEW_TYPE_MESSAGE_SENT;
                    case "image":
                    case "video":
                    case "location":
                        return VIEW_TYPE_IMAGE_SENT;
                    case "contact":
                        return VIEW_TYPE_CONTACT_SENT;
                    case "date":
                    case "subject":
                    case "create_channel":
                    case "channel_image":
                    case "channel_des":
                        return VIEW_TYPE_DATE;
                    default:
                        return VIEW_TYPE_FILE_SENT;
                }
            } else {
                switch (message.messageType) {
                    case "text":
                        return VIEW_TYPE_MESSAGE_RECEIVED;
                    case "image":
                    case "video":
                    case "location":
                        return VIEW_TYPE_IMAGE_RECEIVED;
                    case "contact":
                        return VIEW_TYPE_CONTACT_RECEIVED;
                    case "date":
                    case "subject":
                    case "create_channel":
                    case "channel_image":
                    case "channel_des":
                        return VIEW_TYPE_DATE;
                    default:
                        return VIEW_TYPE_FILE_RECEIVED;
                }
            }
        }

        // Inflates the appropriate layout according to the ViewType.
        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view;

            switch (viewType) {
                case VIEW_TYPE_MESSAGE_SENT:
                    view = LayoutInflater.from(parent.getContext())
                            .inflate(R.layout.chat_text_bubble_sent, parent, false);
                    return new SentMessageHolder(view);
                case VIEW_TYPE_MESSAGE_RECEIVED:
                    view = LayoutInflater.from(parent.getContext())
                            .inflate(R.layout.chat_text_bubble_receive, parent, false);
                    return new ReceivedMessageHolder(view);
                case VIEW_TYPE_IMAGE_SENT:
                    view = LayoutInflater.from(parent.getContext())
                            .inflate(R.layout.chat_image_bubble_sent, parent, false);
                    return new SentImageHolder(view);
                case VIEW_TYPE_IMAGE_RECEIVED:
                    view = LayoutInflater.from(parent.getContext())
                            .inflate(R.layout.chat_image_bubble_receive, parent, false);
                    return new ReceivedImageHolder(view);
                case VIEW_TYPE_CONTACT_SENT:
                    view = LayoutInflater.from(parent.getContext())
                            .inflate(R.layout.chat_contact_bubble_sent, parent, false);
                    return new SentContactHolder(view);
                case VIEW_TYPE_CONTACT_RECEIVED:
                    view = LayoutInflater.from(parent.getContext())
                            .inflate(R.layout.chat_contact_bubble_receive, parent, false);
                    return new ReceivedContactHolder(view);
                case VIEW_TYPE_FILE_SENT:
                    view = LayoutInflater.from(parent.getContext())
                            .inflate(R.layout.chat_file_bubble_sent, parent, false);
                    return new SentFileHolder(view);
                case VIEW_TYPE_FILE_RECEIVED:
                    view = LayoutInflater.from(parent.getContext())
                            .inflate(R.layout.chat_file_bubble_received, parent, false);
                    return new ReceivedFileHolder(view);
                case VIEW_TYPE_DATE:
                    view = LayoutInflater.from(parent.getContext())
                            .inflate(R.layout.chat_date_layout, parent, false);
                    return new DateHolder(view);
            }


            return null;
        }

        // Passes the message object to a ViewHolder so that the contents can be bound to UI.
        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
            final ChannelMessage message = mMessageList.get(position);
            switch (holder.getItemViewType()) {
                case VIEW_TYPE_MESSAGE_SENT:
                    ((MessageListAdapter.SentMessageHolder) holder).bind(message);
                    break;
                case VIEW_TYPE_MESSAGE_RECEIVED:
                    ((MessageListAdapter.ReceivedMessageHolder) holder).bind(message);
                    break;
                case VIEW_TYPE_IMAGE_SENT:
                    ((MessageListAdapter.SentImageHolder) holder).bind(message);
                    break;
                case VIEW_TYPE_IMAGE_RECEIVED:
                    ((MessageListAdapter.ReceivedImageHolder) holder).bind(message);
                    break;
                case VIEW_TYPE_FILE_SENT:
                    ((MessageListAdapter.SentFileHolder) holder).bind(message);
                    break;
                case VIEW_TYPE_FILE_RECEIVED:
                    ((MessageListAdapter.ReceivedFileHolder) holder).bind(message);
                    break;
                case VIEW_TYPE_CONTACT_SENT:
                    ((MessageListAdapter.SentContactHolder) holder).bind(message);
                    break;
                case VIEW_TYPE_CONTACT_RECEIVED:
                    ((MessageListAdapter.ReceivedContactHolder) holder).bind(message);
                    break;
                case VIEW_TYPE_DATE:
                    ((MessageListAdapter.DateHolder) holder).bind(message);
                    break;
            }
        }

        private class SentMessageHolder extends RecyclerView.ViewHolder {
            TextView messageText, timeText;

            SentMessageHolder(View itemView) {
                super(itemView);

                messageText = itemView.findViewById(R.id.text_message_body);
                timeText = itemView.findViewById(R.id.text_message_time);
            }

            void bind(final ChannelMessage message) {
                messageText.setText(message.message
                        + Html.fromHtml(" &#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;"));
                Linkify.addLinks(messageText, Linkify.EMAIL_ADDRESSES | Linkify.PHONE_NUMBERS | Linkify.WEB_URLS);
                timeText.setText(ApplicationClass.getTime(Long.parseLong(message.chatTime)));
                if (selectedChatPos.contains(message)) {
                    itemView.setSelected(true);
                } else {
                    itemView.setSelected(false);
                }
            }
        }

        private class ReceivedMessageHolder extends RecyclerView.ViewHolder {
            TextView messageText, timeText, nameText;

            ReceivedMessageHolder(View itemView) {
                super(itemView);

                nameText = (TextView) itemView.findViewById(R.id.text_message_sender);
                messageText = (TextView) itemView.findViewById(R.id.text_message_body);
                timeText = (TextView) itemView.findViewById(R.id.text_message_time);
            }

            void bind(ChannelMessage message) {
                nameText.setVisibility(View.GONE);
                if (message.messageType.equals("create_channel") || message.messageType.equals("new_invite")) {
                    messageText.setText(Html.fromHtml("Welcome"
                            + " &#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;"));
                } else {
                    messageText.setText(message.message
                            + Html.fromHtml(" &#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;"));
                    Linkify.addLinks(messageText, Linkify.EMAIL_ADDRESSES | Linkify.PHONE_NUMBERS | Linkify.WEB_URLS);
                }
                timeText.setText(ApplicationClass.getTime(Long.parseLong(message.chatTime.replace(".0", ""))));
                if (selectedChatPos.contains(message)) {
                    itemView.setSelected(true);
                } else {
                    itemView.setSelected(false);
                }
            }
        }

        private class SentImageHolder extends RecyclerView.ViewHolder {
            TextView timeText;
            ImageView uploadimage, downloadicon;
            RelativeLayout progresslay;
            ProgressWheel progressbar;

            SentImageHolder(View itemView) {
                super(itemView);

                uploadimage = itemView.findViewById(R.id.uploadimage);
                timeText = itemView.findViewById(R.id.text_message_time);
                progresslay = itemView.findViewById(R.id.progresslay);
                progressbar = itemView.findViewById(R.id.progressbar);
                downloadicon = itemView.findViewById(R.id.downloadicon);
            }

            void bind(final ChannelMessage message) {
                timeText.setText(ApplicationClass.getTime(Long.parseLong(message.chatTime)));
                if (selectedChatPos.contains(message)) {
                    itemView.setSelected(true);
                } else {
                    itemView.setSelected(false);
                }
                switch (message.messageType) {
                    case "image":
                        downloadicon.setImageResource(R.drawable.upload);
                        switch (message.progress) {
                            case "": {
                                progresslay.setVisibility(View.VISIBLE);
                                progressbar.setVisibility(View.VISIBLE);
                                progressbar.spin();
                                File file = storageManager.getImage("sent", getFileName(message.attachment));
                                if (file != null) {
                                    Log.v(TAG, "checkChat=" + file.getAbsolutePath());
                                    Glide.with(mContext).load(Uri.fromFile(file)).thumbnail(0.5f)
                                            .into(uploadimage);
                                }
                                break;
                            }
                            case "completed": {
                                progresslay.setVisibility(View.GONE);
                                progressbar.setVisibility(View.GONE);
                                progressbar.stopSpinning();
                                File file = storageManager.getImage("thumb", message.attachment);
                                if (file != null) {
                                    Log.v(TAG, "checkChat=" + file.getAbsolutePath());
                                    Glide.with(mContext).load(Uri.fromFile(file)).thumbnail(0.5f)
                                            .into(uploadimage);
                                }
                                break;
                            }
                            case "error": {
                                progresslay.setVisibility(View.VISIBLE);
                                progressbar.setVisibility(View.VISIBLE);
                                progressbar.stopSpinning();
                                File file = storageManager.getImage("sent", getFileName(message.attachment));
                                if (file != null) {
                                    Log.v(TAG, "checkChat=" + file.getAbsolutePath());
                                    Glide.with(mContext).load(Uri.fromFile(file)).thumbnail(0.5f)
                                            .into(uploadimage);
                                }
                                break;
                            }
                        }

                        uploadimage.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                if (message.progress.equals("error")) {
                                    if (isNetworkConnected().equals(NOT_CONNECT)) {
                                        networkSnack();
                                    } else {
                                        try {
                                            progressbar.setVisibility(View.VISIBLE);
                                            progressbar.spin();
                                            dbhelper.updateChannelMessageData(message.messageId, Constants.TAG_PROGRESS, "");
                                            message.progress = "";
                                            byte[] bytes = FileUtils.readFileToByteArray(new File(message.attachment));
                                            uploadImage(bytes, message.attachment, message, "");
                                        } catch (IOException ex) {
                                            ex.printStackTrace();
                                        }
                                    }
                                } else if (message.progress.equals("completed")) {
                                    if (storageManager.checkifImageExists("sent", message.attachment)) {
                                        File file = storageManager.getImage("sent", message.attachment);
                                        if (file != null) {
                                            Log.v(TAG, "file=" + file.getAbsolutePath());
                                            bottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
                                            Glide.with(mContext).load(Uri.fromFile(file)).thumbnail(0.5f)
                                                    .transition(new DrawableTransitionOptions().crossFade())
                                                    .into(imageView);
                                        }
                                    } else {
                                        Toast.makeText(ChannelChatActivity.this, getString(R.string.no_media), Toast.LENGTH_SHORT).show();
                                    }
                                }
                            }
                        });
                        break;
                    case "location":
                        progresslay.setVisibility(View.GONE);
                        int size = ApplicationClass.dpToPx(mContext, 170);
                        String url = "http://maps.google.com/maps/api/staticmap?center=" + message.lat + "," + message.lon + "&zoom=18&size=" + size + "x" + size + "&sensor=false" + "&key=" + Constants.GOOGLE_MAPS_KEY;
                        Glide.with(mContext).load(url).thumbnail(0.5f)
                                .into(uploadimage);

                        uploadimage.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                Intent i = new Intent(ChannelChatActivity.this, LocationActivity.class);
                                i.putExtra("from", "view");
                                i.putExtra("lat", message.lat);
                                i.putExtra("lon", message.lon);
                                startActivity(i);
                            }
                        });
                        break;
                    case "video":
                        progresslay.setVisibility(View.VISIBLE);
                        switch (message.progress) {
                            case "": {
                                progressbar.setVisibility(View.VISIBLE);
                                progressbar.spin();
                                downloadicon.setImageResource(R.drawable.upload);
                                File file = storageManager.getImage("sent", getFileName(message.thumbnail));
                                if (file != null) {
                                    Log.v(TAG, "file=" + file.getAbsolutePath());
                                    Glide.with(mContext).load(Uri.fromFile(file)).thumbnail(0.5f)
                                            .into(uploadimage);
                                }
                                break;
                            }
                            case "completed": {
                                progressbar.setVisibility(View.GONE);
                                progressbar.stopSpinning();
                                downloadicon.setImageResource(R.drawable.play);
                                File file = storageManager.getImage("sent", message.thumbnail);
                                if (file != null) {
                                    Log.v(TAG, "file=" + file.getAbsolutePath());
                                    Glide.with(mContext).load(Uri.fromFile(file)).thumbnail(0.5f)
                                            .into(uploadimage);
                                }
                                break;
                            }
                            case "error": {
                                progressbar.setVisibility(View.VISIBLE);
                                progressbar.stopSpinning();
                                downloadicon.setImageResource(R.drawable.upload);
                                File file = storageManager.getImage("sent", getFileName(message.thumbnail));
                                if (file != null) {
                                    Log.v(TAG, "file=" + file.getAbsolutePath());
                                    Glide.with(mContext).load(Uri.fromFile(file)).thumbnail(0.5f)
                                            .into(uploadimage);
                                }
                                break;
                            }
                            default:

                                break;
                        }

                        uploadimage.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                if (message.progress.equals("error")) {
                                    if (isNetworkConnected().equals(NOT_CONNECT)) {
                                        networkSnack();
                                    } else {
                                        try {
                                            Bitmap thumb = ThumbnailUtils.createVideoThumbnail(message.attachment, MediaStore.Video.Thumbnails.MINI_KIND);
                                            if (thumb != null) {
                                                progressbar.setVisibility(View.VISIBLE);
                                                progressbar.spin();
                                                dbhelper.updateChannelMessageData(message.messageId, Constants.TAG_PROGRESS, "");
                                                message.progress = "";
                                                String timestamp = String.valueOf(System.currentTimeMillis() / 1000L);
                                                String imageStatus = storageManager.saveToSdCard(getApplicationContext(), thumb, "sent", timestamp + ".jpg");
                                                if (imageStatus.equals("success")) {
                                                    File file = storageManager.getImage("sent", timestamp + ".jpg");
                                                    String imagePath = file.getAbsolutePath();
                                                    byte[] bytes = FileUtils.readFileToByteArray(new File(imagePath));
                                                    uploadImage(bytes, imagePath, message, message.attachment);
                                                }
                                            }
                                        } catch (IOException ex) {
                                            ex.printStackTrace();
                                            Toast.makeText(ChannelChatActivity.this, getString(R.string.something_wrong), Toast.LENGTH_SHORT).show();
                                        }
                                    }
                                } else if (message.progress.equals("completed")) {
                                    if (storageManager.checkifFileExists(message.attachment, message.messageType, "sent")) {
                                        try {
                                            Intent intent = new Intent();
                                            intent.setAction(Intent.ACTION_VIEW);
                                            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                                            File file = storageManager.getFile(message.attachment, message.messageType, "sent");
                                            Uri photoURI = FileProvider.getUriForFile(mContext,
                                                    BuildConfig.APPLICATION_ID + ".provider", file);

                                            MimeTypeMap mime = MimeTypeMap.getSingleton();
                                            String ext = file.getName().substring(file.getName().indexOf(".") + 1);
                                            String type = mime.getMimeTypeFromExtension(ext);

                                            intent.setDataAndType(photoURI, type);

                                            startActivity(intent);
                                        } catch (ActivityNotFoundException e) {
                                            Toast.makeText(ChannelChatActivity.this, getString(R.string.no_application), Toast.LENGTH_SHORT).show();
                                            e.printStackTrace();
                                        }
                                    } else {
                                        Toast.makeText(ChannelChatActivity.this, getString(R.string.no_media), Toast.LENGTH_SHORT).show();
                                    }
                                }
                            }
                        });
                        break;
                }
            }
        }

        private class ReceivedImageHolder extends RecyclerView.ViewHolder {
            TextView timeText, nameText;
            ImageView uploadimage, downloadicon;
            RelativeLayout progresslay, videoprogresslay;
            ProgressWheel progressbar, videoprogressbar;

            ReceivedImageHolder(View itemView) {
                super(itemView);

                uploadimage = itemView.findViewById(R.id.uploadimage);
                progresslay = itemView.findViewById(R.id.progresslay);
                timeText = itemView.findViewById(R.id.text_message_time);
                progressbar = itemView.findViewById(R.id.progressbar);
                downloadicon = itemView.findViewById(R.id.downloadicon);
                nameText = itemView.findViewById(R.id.text_message_sender);
                videoprogresslay = itemView.findViewById(R.id.videoprogresslay);
                videoprogressbar = itemView.findViewById(R.id.videoprogressbar);
            }

            void bind(final ChannelMessage message) {
                nameText.setVisibility(View.GONE);
                if (selectedChatPos.contains(message)) {
                    itemView.setSelected(true);
                } else {
                    itemView.setSelected(false);
                }
                switch (message.messageType) {
                    case "image":
                        if (storageManager.checkifImageExists("receive", message.attachment)) {
                            progresslay.setVisibility(View.GONE);
                            videoprogresslay.setVisibility(View.GONE);
                            File file = storageManager.getImage("thumb", message.attachment);
                            if (file != null) {
                                Glide.with(mContext).load(file).thumbnail(0.5f)
                                        .into(uploadimage);
                            }
                        } else {
                            progresslay.setVisibility(View.VISIBLE);
                            progressbar.setVisibility(View.VISIBLE);
                            progressbar.stopSpinning();
                            Glide.with(mContext).load(Constants.CHANNEL_IMG_PATH + message.attachment).thumbnail(0.5f)
                                    .apply(RequestOptions.overrideOf(18, 18))
                                    .into(uploadimage);
                        }
                        timeText.setText(ApplicationClass.getTime(Long.parseLong(message.chatTime)));

                        uploadimage.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                if (storageManager.checkifImageExists("receive", message.attachment)) {
                                    File file = storageManager.getImage("receive", message.attachment);
                                    if (file != null) {
                                        videoprogresslay.setVisibility(View.GONE);
                                        Log.v(TAG, "file=" + file.getAbsolutePath());
                                        bottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
                                        Glide.with(mContext).load(file).thumbnail(0.5f)
                                                .transition(new DrawableTransitionOptions().crossFade())
                                                .into(imageView);
                                    }
                                } else {
                                    if (ContextCompat.checkSelfPermission(ChannelChatActivity.this, WRITE_EXTERNAL_STORAGE)
                                            != PackageManager.PERMISSION_GRANTED) {
                                        ActivityCompat.requestPermissions(ChannelChatActivity.this, new String[]{WRITE_EXTERNAL_STORAGE}, 100);
                                    } else {
                                        if (isNetworkConnected().equals(NOT_CONNECT)) {
                                            networkSnack();
                                        } else {
                                            ImageDownloader imageDownloader = new ImageDownloader(ChannelChatActivity.this) {
                                                @Override
                                                protected void onPostExecute(Bitmap imgBitmap) {
                                                    if (imgBitmap == null) {
                                                        Log.e(TAG, "bitmapFailed");
                                                        Toast.makeText(mContext, getString(R.string.something_wrong), Toast.LENGTH_SHORT).show();
                                                    } else {
                                                        Log.v(TAG, "onBitmapLoaded");
                                                        try {
                                                            String status = storageManager.saveThumbNail(imgBitmap, message.attachment);
                                                            if (status.equals("success")) {
                                                                File thumbFile = storageManager.getImage("thumb", message.attachment);
                                                                if (thumbFile != null) {
                                                                    Log.v("file", "file=" + thumbFile.getAbsolutePath());
                                                                    Glide.with(mContext).load(thumbFile).thumbnail(0.5f)
                                                                            .into(uploadimage);
                                                                    progresslay.setVisibility(View.GONE);
                                                                    progressbar.stopSpinning();
                                                                    videoprogresslay.setVisibility(View.GONE);
                                                                }
                                                            } else {
                                                                Toast.makeText(mContext, getString(R.string.something_wrong), Toast.LENGTH_SHORT).show();
                                                            }
                                                        } catch (NullPointerException e) {
                                                            e.printStackTrace();
                                                        }
                                                    }
                                                }

                                                @Override
                                                protected void onProgressUpdate(String... progress) {
                                                    // progressbar.setProgress(Integer.parseInt(progress[0]));
                                                }
                                            };
                                            imageDownloader.execute(Constants.CHANNEL_IMG_PATH + message.attachment, "receive");
                                            progressbar.setVisibility(View.VISIBLE);
                                            progressbar.spin();
                                        }
                                    }
                                }
                            }
                        });
                        break;
                    case "location":
                        progresslay.setVisibility(View.GONE);
                        int size = ApplicationClass.dpToPx(mContext, 170);
                        String url = "http://maps.google.com/maps/api/staticmap?center=" + message.lat + "," + message.lon + "&zoom=18&size=" + size + "x" + size + "&sensor=false" + "&key=" + Constants.GOOGLE_MAPS_KEY;
                        Glide.with(mContext).load(url).thumbnail(0.5f)
                                .into(uploadimage);
                        timeText.setText(ApplicationClass.getTime(Long.parseLong(message.chatTime)));
                        uploadimage.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                Intent i = new Intent(ChannelChatActivity.this, LocationActivity.class);
                                i.putExtra("from", "view");
                                i.putExtra("lat", message.lat);
                                i.putExtra("lon", message.lon);
                                startActivity(i);
                            }
                        });
                        break;
                    case "video":
                        progresslay.setVisibility(View.VISIBLE);
                        progressbar.setVisibility(View.GONE);
                        downloadicon.setImageResource(R.drawable.play);
                        timeText.setText(ApplicationClass.getTime(Long.parseLong(message.chatTime)));
                        if (storageManager.checkifFileExists(message.attachment, message.messageType, "receive") &&
                                storageManager.checkifImageExists("thumb", message.thumbnail)) {
                            Log.v(TAG, "video-if");
                            videoprogresslay.setVisibility(View.GONE);
                            File file = storageManager.getImage("thumb", message.thumbnail);
                            if (file != null) {
                                Log.v(TAG, "file=" + file.getAbsolutePath());
                                Glide.with(mContext).load(Uri.fromFile(file)).thumbnail(0.5f)
                                        .into(uploadimage);
                            }
                        } else {
                            Glide.with(mContext).load(Constants.CHANNEL_IMG_PATH + message.thumbnail)
                                    .listener(new RequestListener<Drawable>() {
                                                  @Override
                                                  public boolean onLoadFailed( GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {

                                                      return false;
                                                  }

                                                  @Override
                                                  public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                                                      uploadimage.setImageDrawable(resource);
                                                      return true;
                                                  }
                                              }
                                    ).into(uploadimage);
                            videoprogresslay.setVisibility(View.VISIBLE);
                            videoprogressbar.setVisibility(View.VISIBLE);
                            videoprogressbar.stopSpinning();
                        }
                        uploadimage.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                if (storageManager.checkifFileExists(message.attachment, message.messageType, "receive") &&
                                        storageManager.checkifImageExists("thumb", message.thumbnail)) {
                                    try {
                                        Intent intent = new Intent();
                                        intent.setAction(Intent.ACTION_VIEW);
                                        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                                        File file = storageManager.getFile(message.attachment, message.messageType, "receive");
                                        Uri photoURI = FileProvider.getUriForFile(mContext,
                                                BuildConfig.APPLICATION_ID + ".provider", file);

                                        MimeTypeMap mime = MimeTypeMap.getSingleton();
                                        String ext = file.getName().substring(file.getName().indexOf(".") + 1);
                                        String type = mime.getMimeTypeFromExtension(ext);

                                        intent.setDataAndType(photoURI, type);

                                        startActivity(intent);
                                    } catch (ActivityNotFoundException e) {
                                        Toast.makeText(ChannelChatActivity.this, getString(R.string.no_application), Toast.LENGTH_SHORT).show();
                                        e.printStackTrace();
                                    }
                                } else {
                                    if (isNetworkConnected().equals(NOT_CONNECT)) {
                                        networkSnack();
                                    } else {
                                        ImageDownloader imageDownloader = new ImageDownloader(ChannelChatActivity.this) {
                                            @Override
                                            protected void onPostExecute(Bitmap imgBitmap) {
                                                if (imgBitmap == null) {
                                                    Log.v("bitmapFailed", "bitmapFailed");
                                                    Toast.makeText(mContext, getString(R.string.something_wrong), Toast.LENGTH_SHORT).show();
                                                    videoprogresslay.setVisibility(View.GONE);
                                                    videoprogressbar.setVisibility(View.GONE);
                                                    videoprogressbar.stopSpinning();
                                                } else {
                                                    Log.v("onBitmapLoaded", "onBitmapLoaded");
                                                    try {
//                                                        String status = storageManager.saveThumbNail(imgBitmap, message.thumbnail);
//                                                        if (status.equals("success")) {
                                                        final File thumbFile = storageManager.getImage("thumb", message.thumbnail);
                                                        if (thumbFile != null) {
                                                            Log.v("file", "file=" + thumbFile.getAbsolutePath());

                                                            DownloadFiles downloadFiles = new DownloadFiles(ChannelChatActivity.this) {
                                                                @Override
                                                                protected void onPostExecute(String downPath) {
                                                                    videoprogresslay.setVisibility(View.GONE);
                                                                    videoprogressbar.setVisibility(View.GONE);
                                                                    videoprogressbar.stopSpinning();
                                                                    if (downPath == null) {
                                                                        Log.v("Download Failed", "Download Failed");
                                                                        Toast.makeText(mContext, getString(R.string.something_wrong), Toast.LENGTH_SHORT).show();
                                                                    } else {
                                                                        Glide.with(mContext).load(Uri.fromFile(thumbFile)).thumbnail(0.5f)
                                                                                .into(uploadimage);
                                                                        //  Toast.makeText(mContext, getString(R.string.downloaded), Toast.LENGTH_SHORT).show();
                                                                    }
                                                                }
                                                            };
                                                            downloadFiles.execute(Constants.CHANNEL_IMG_PATH + message.attachment, message.messageType);
                                                        }
//                                                        } else {
//                                                            Toast.makeText(mContext, getString(R.string.something_wrong), Toast.LENGTH_SHORT).show();
//                                                            videoprogresslay.setVisibility(View.VISIBLE);
//                                                            videoprogressbar.setVisibility(View.VISIBLE);
//                                                            videoprogressbar.stopSpinning();
//                                                        }
                                                    } catch (NullPointerException e) {
                                                        e.printStackTrace();
                                                    }
                                                }
                                            }

                                            @Override
                                            protected void onProgressUpdate(String... progress) {
                                                // progressbar.setProgress(Integer.parseInt(progress[0]));
                                            }
                                        };
                                        imageDownloader.execute(Constants.CHANNEL_IMG_PATH + message.thumbnail, "thumb");
                                        videoprogresslay.setVisibility(View.VISIBLE);
                                        videoprogressbar.setVisibility(View.VISIBLE);
                                        videoprogressbar.spin();
                                    }
                                }
                            }
                        });
                        break;
                }
            }
        }

        private class SentFileHolder extends RecyclerView.ViewHolder {
            TextView filename, timeText, file_type_tv;
            ImageView icon, uploadicon;
            RelativeLayout file_body_lay;
            ProgressWheel progressbar;

            SentFileHolder(View itemView) {
                super(itemView);

                filename = itemView.findViewById(R.id.filename);
                timeText = itemView.findViewById(R.id.text_message_time);
                icon = itemView.findViewById(R.id.icon);
                file_body_lay = itemView.findViewById(R.id.file_body_lay);
                progressbar = itemView.findViewById(R.id.progressbar);
                uploadicon = itemView.findViewById(R.id.uploadicon);
                file_type_tv = itemView.findViewById(R.id.file_type_tv);
            }

            void bind(final ChannelMessage message) {
                timeText.setText(ApplicationClass.getTime(Long.parseLong(message.chatTime)));
                if (selectedChatPos.contains(message)) {
                    itemView.setSelected(true);
                } else {
                    itemView.setSelected(false);
                }
                if (message.messageType.equals("file")) {
                    icon.setImageResource(R.drawable.icon_file_unknown);
                    file_type_tv.setVisibility(View.VISIBLE);
                    file_type_tv.setText(firstThree(FilenameUtils.getExtension(message.attachment)));
                } else if (message.messageType.equals("audio")) {
                    icon.setImageResource(R.drawable.mp3);
                    file_type_tv.setVisibility(View.GONE);
                }

                switch (message.progress) {
                    case "":
                        progressbar.setVisibility(View.VISIBLE);
                        progressbar.spin();
                        uploadicon.setVisibility(View.VISIBLE);
                        filename.setText(getString(R.string.uploading));
                        break;
                    case "completed":
                        progressbar.setVisibility(View.GONE);
                        progressbar.stopSpinning();
                        uploadicon.setVisibility(View.GONE);
                        filename.setText(message.message);
                        break;
                    case "error":
                        progressbar.setVisibility(View.VISIBLE);
                        progressbar.stopSpinning();
                        uploadicon.setVisibility(View.VISIBLE);
                        filename.setText(getString(R.string.retry));
                        break;
                }

                file_body_lay.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if (message.progress.equals("error")) {
                            if (isNetworkConnected().equals(NOT_CONNECT)) {
                                networkSnack();
                            } else {
                                try {
                                    progressbar.setVisibility(View.VISIBLE);
                                    progressbar.spin();
                                    uploadicon.setVisibility(View.VISIBLE);
                                    filename.setText("Uploading..");
                                    dbhelper.updateGroupMessageData(message.messageId, Constants.TAG_PROGRESS, "");
                                    message.progress = "";
                                    Intent service = new Intent(ChannelChatActivity.this, FileUploadService.class);
                                    Bundle b = new Bundle();
                                    b.putSerializable("mdata", message);
                                    b.putString("filepath", message.attachment);
                                    b.putString("chatType", Constants.TAG_CHANNEL);
                                    service.putExtras(b);
                                    startService(service);
                                } catch (Exception ex) {
                                    ex.printStackTrace();
                                }
                            }
                        } else if (message.progress.equals("completed")) {
                            if (storageManager.checkifFileExists(message.attachment, message.messageType, "sent")) {
                                try {
                                    Intent intent = new Intent();
                                    intent.setAction(android.content.Intent.ACTION_VIEW);
                                    intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                                    File file = storageManager.getFile(message.attachment, message.messageType, "sent");
                                    Uri photoURI = FileProvider.getUriForFile(mContext,
                                            BuildConfig.APPLICATION_ID + ".provider", file);

                                    MimeTypeMap mime = MimeTypeMap.getSingleton();
                                    String ext = file.getName().substring(file.getName().indexOf(".") + 1);
                                    String type = mime.getMimeTypeFromExtension(ext);

                                    intent.setDataAndType(photoURI, type);

                                    startActivity(intent);
                                } catch (ActivityNotFoundException e) {
                                    makeToast(getString(R.string.no_application));
                                    e.printStackTrace();
                                }
                            } else {
                                makeToast(getString(R.string.no_media));
                            }
                        }
                    }
                });
            }
        }

        private class ReceivedFileHolder extends RecyclerView.ViewHolder {
            TextView filename, timeText, nameText, file_type_tv;
            ImageView icon, downloadicon;
            RelativeLayout file_body_lay;
            ProgressWheel progressbar;

            ReceivedFileHolder(View itemView) {
                super(itemView);

                filename = itemView.findViewById(R.id.filename);
                timeText = itemView.findViewById(R.id.text_message_time);
                icon = itemView.findViewById(R.id.icon);
                file_body_lay = itemView.findViewById(R.id.file_body_lay);
                downloadicon = itemView.findViewById(R.id.downloadicon);
                progressbar = itemView.findViewById(R.id.progressbar);
                nameText = itemView.findViewById(R.id.text_message_sender);
                file_type_tv = itemView.findViewById(R.id.file_type_tv);
            }

            void bind(final ChannelMessage message) {
                filename.setText(message.message);
                timeText.setText(ApplicationClass.getTime(Long.parseLong(message.chatTime)));
                nameText.setVisibility(View.GONE);
                if (selectedChatPos.contains(message)) {
                    itemView.setSelected(true);
                } else {
                    itemView.setSelected(false);
                }
                if (message.messageType.equals("document")) {
                    icon.setImageResource(R.drawable.icon_file_unknown);
                    file_type_tv.setVisibility(View.VISIBLE);
                    file_type_tv.setText(firstThree(FilenameUtils.getExtension(message.attachment)));
                } else if (message.messageType.equals("audio")) {
                    file_type_tv.setVisibility(View.GONE);
                    icon.setImageResource(R.drawable.mp3);
                }

                if (storageManager.checkifFileExists(message.attachment, message.messageType, "receive")) {
                    downloadicon.setVisibility(View.GONE);
                    progressbar.setVisibility(View.GONE);
                } else {
                    downloadicon.setVisibility(View.VISIBLE);
                    progressbar.setVisibility(View.VISIBLE);
                    progressbar.stopSpinning();
                }

                file_body_lay.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if (storageManager.checkifFileExists(message.attachment, message.messageType, "receive")) {
                            try {
                                Intent intent = new Intent();
                                intent.setAction(Intent.ACTION_VIEW);
                                intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                                File file = storageManager.getFile(message.attachment, message.messageType, "receive");
                                Uri photoURI = FileProvider.getUriForFile(mContext,
                                        BuildConfig.APPLICATION_ID + ".provider", file);

                                MimeTypeMap mime = MimeTypeMap.getSingleton();
                                String ext = file.getName().substring(file.getName().indexOf(".") + 1);
                                String type = mime.getMimeTypeFromExtension(ext);

                                intent.setDataAndType(photoURI, type);

                                startActivity(intent);
                            } catch (ActivityNotFoundException e) {
                                Toast.makeText(ChannelChatActivity.this, getString(R.string.no_application), Toast.LENGTH_SHORT).show();
                                e.printStackTrace();
                            }
                        } else {
                            if (isNetworkConnected().equals(NOT_CONNECT)) {
                                networkSnack();
                            } else {
                                DownloadFiles downloadFiles = new DownloadFiles(ChannelChatActivity.this) {
                                    @Override
                                    protected void onPostExecute(String downPath) {
                                        progressbar.setVisibility(View.GONE);
                                        progressbar.stopSpinning();
                                        downloadicon.setVisibility(View.GONE);
                                        if (downPath == null) {
                                            Log.v("Download Failed", "Download Failed");
                                            Toast.makeText(mContext, getString(R.string.something_wrong), Toast.LENGTH_SHORT).show();
                                        } else {
                                            //Toast.makeText(mContext, getString(R.string.downloaded), Toast.LENGTH_SHORT).show();
                                        }
                                    }
                                };
                                downloadFiles.execute(Constants.CHANNEL_IMG_PATH + message.attachment, message.messageType);
                                progressbar.setVisibility(View.VISIBLE);
                                progressbar.spin();
                                downloadicon.setVisibility(View.VISIBLE);
                            }
                        }
                    }
                });
            }
        }

        private class SentContactHolder extends RecyclerView.ViewHolder {
            TextView username, phoneno, timeText;

            SentContactHolder(View itemView) {
                super(itemView);
                username = itemView.findViewById(R.id.username);
                phoneno = itemView.findViewById(R.id.phoneno);
                timeText = itemView.findViewById(R.id.text_message_time);
            }

            void bind(ChannelMessage message) {
                username.setText(message.contactName);
                phoneno.setText(message.contactPhoneNo);
                timeText.setText(ApplicationClass.getTime(Long.parseLong(message.chatTime)));
                if (selectedChatPos.contains(message)) {
                    itemView.setSelected(true);
                } else {
                    itemView.setSelected(false);
                }
            }
        }

        private class ReceivedContactHolder extends RecyclerView.ViewHolder {
            TextView username, phoneno, timeText, addcontact, nameText;

            ReceivedContactHolder(View itemView) {
                super(itemView);
                username = itemView.findViewById(R.id.username);
                phoneno = itemView.findViewById(R.id.phoneno);
                timeText = itemView.findViewById(R.id.text_message_time);
                addcontact = itemView.findViewById(R.id.addcontact);
                nameText = itemView.findViewById(R.id.text_message_sender);
            }

            void bind(final ChannelMessage message) {
                username.setText(message.contactName);
                phoneno.setText(message.contactPhoneNo);
                timeText.setText(ApplicationClass.getTime(Long.parseLong(message.chatTime)));
                nameText.setVisibility(View.GONE);
                nameText.setText("");
                if (selectedChatPos.contains(message)) {
                    itemView.setSelected(true);
                } else {
                    itemView.setSelected(false);
                }
                addcontact.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent intent = new Intent(ContactsContract.Intents.Insert.ACTION);
                        intent.setType(ContactsContract.RawContacts.CONTENT_TYPE);
                        intent.putExtra(ContactsContract.Intents.Insert.PHONE, message.contactPhoneNo);
                        intent.putExtra(ContactsContract.Intents.Insert.NAME, message.contactName);
                        startActivity(intent);
                    }
                });
            }
        }

        private class DateHolder extends RecyclerView.ViewHolder {
            TextView timeText;

            DateHolder(View itemView) {
                super(itemView);
                timeText = itemView.findViewById(R.id.text_message_time);
            }

            void bind(final ChannelMessage message) {
                if (message.messageType.equalsIgnoreCase("create_channel")) {
                    if (Utils.isUserAdminInChannel(channelData)) {
                        timeText.setText(R.string.you_created_this_channel);
                    } else {
                        timeText.setText(R.string.you_added_in_to_this_channel);
                    }
                } else if (message.messageType.equalsIgnoreCase("subject")) {
                    timeText.setText(R.string.channel_subject_changed);
                } else if (message.messageType.equalsIgnoreCase("channel_image")) {
                    timeText.setText(R.string.channel_image_changed);
                } else if (message.messageType.equalsIgnoreCase("channel_des")) {
                    timeText.setText(R.string.channel_info_changed);
                } else {
                    timeText.setText(getFormattedDate(mContext, Long.parseLong(message.chatTime)));
                }
            }
        }

    }

    public RecyclerItemClickListener chatItemClick(Context mContext, final RecyclerView recyclerView) {
        return new RecyclerItemClickListener(mContext, recyclerView, new RecyclerItemClickListener.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                if (chatLongPressed) {
                    String messageType = messagesList.get(position).messageType;
                    int chatType = recyclerView.getAdapter().getItemViewType(position);
                    if (chatType != VIEW_TYPE_DATE && isForwardable(messagesList.get(position))) {
                        if (selectedChatPos.contains(messagesList.get(position))) {
                            selectedChatPos.remove(messagesList.get(position));
                            chatUserLay.setVisibility(View.VISIBLE);
                            forwordLay.setVisibility(View.GONE);
                            chatLongPressed = false;
                        } else {
                            selectedChatPos.clear();
                            selectedChatPos.add(messagesList.get(position));
                            chatUserLay.setVisibility(View.GONE);
                            forwordLay.setVisibility(View.VISIBLE);
                            if (messageType.equals("text")) {
                                copyBtn.setVisibility(View.VISIBLE);
                            } else {
                                copyBtn.setVisibility(View.GONE);
                            }
                        }
                        messageListAdapter.notifyDataSetChanged();
                    }
                }
            }

            @Override
            public void onItemLongClick(View view, int position) {
                if (!chatLongPressed) {
                    String messageType = messagesList.get(position).messageType;
                    int chatType = recyclerView.getAdapter().getItemViewType(position);
                    if (chatType != VIEW_TYPE_DATE && isForwardable(messagesList.get(position))) {
                        chatLongPressed = true;
                        if (selectedChatPos.contains(messagesList.get(position))) {
                            selectedChatPos.remove(messagesList.get(position));
                            chatUserLay.setVisibility(View.VISIBLE);
                            forwordLay.setVisibility(View.GONE);
                            chatLongPressed = false;
                        } else {
                            selectedChatPos.clear();
                            selectedChatPos.add(messagesList.get(position));
                            chatUserLay.setVisibility(View.GONE);
                            forwordLay.setVisibility(View.VISIBLE);
                            if (messageType.equals("text")) {
                                copyBtn.setVisibility(View.VISIBLE);
                            } else {
                                copyBtn.setVisibility(View.GONE);
                            }
                        }
                        messageListAdapter.notifyDataSetChanged();
                    }
                }
            }
        });
    }

    private boolean isForwardable(ChannelMessage mData) {
        if ((mData.messageType.equals("video") || mData.messageType.equals("file") ||
                mData.messageType.equals("audio"))) {
            if (channelAdminId != null && channelAdminId.equals(GetSet.getUserId()) && !mData.progress.equals("completed")) {
                return false;
            } else if (channelAdminId != null && !channelAdminId.equals(GetSet.getUserId()) && !storageManager.checkifFileExists(mData.attachment, mData.messageType, "receive")) {
                return false;
            }
            return true;
        } else if (mData.messageType.equals("image") && !mData.progress.equals("completed")) {
            if (channelAdminId.equals(GetSet.getUserId()) && !mData.progress.equals("completed")) {
                return false;
            } else if (channelAdminId != null && !channelAdminId.equals(GetSet.getUserId()) && !storageManager.checkifImageExists("receive", mData.attachment)) {
                return false;
            }
            return true;
        } else {
            return true;
        }
    }

    private void emitImage(ChannelMessage mdata) {
        try {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put(Constants.TAG_CHANNEL_ID, channelId);
            jsonObject.put(Constants.TAG_CHANNEL_NAME, channelData.channelName);
            jsonObject.put(Constants.TAG_CHAT_TYPE, Constants.TAG_CHANNEL);
            jsonObject.put(Constants.TAG_MESSAGE_ID, mdata.messageId);
            jsonObject.put(Constants.TAG_MESSAGE_TYPE, mdata.messageType);
            jsonObject.put(Constants.TAG_MESSAGE, mdata.message);
            jsonObject.put(Constants.TAG_ATTACHMENT, mdata.attachment);
            jsonObject.put(Constants.TAG_CHAT_TIME, mdata.chatTime);
            jsonObject.put(Constants.TAG_ADMIN_ID, channelData.channelAdminId);
            socketConnection.startChannelChat(jsonObject);
        } catch (JSONException e) {
            e.printStackTrace();
        }

    }

    private void emitLocation(String type, String lat, String lon) {
        String unixStamp = String.valueOf(System.currentTimeMillis() / 1000L);
        RandomString randomString = new RandomString(10);
        String messageId = channelId + randomString.nextString();
        try {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put(Constants.TAG_CHANNEL_ID, channelId);
            jsonObject.put(Constants.TAG_CHANNEL_NAME, channelData.channelName);
            jsonObject.put(Constants.TAG_CHAT_TYPE, Constants.TAG_CHANNEL);
            jsonObject.put(Constants.TAG_MESSAGE_ID, messageId);
            jsonObject.put(Constants.TAG_MESSAGE_TYPE, type);
            jsonObject.put(Constants.TAG_MESSAGE, "Location");
            jsonObject.put(Constants.TAG_LAT, lat);
            jsonObject.put(Constants.TAG_LON, lon);
            jsonObject.put(Constants.TAG_CHAT_TIME, unixStamp);
            jsonObject.put(Constants.TAG_ADMIN_ID, channelData.channelAdminId);
            socketConnection.startChannelChat(jsonObject);

            dbhelper.addChannelMessages(channelId, Constants.TAG_CHANNEL, messageId, type,
                    getString(R.string.location), "", lat, lon,
                    "", "", "",
                    unixStamp, "", "read");

            dbhelper.addChannelRecentMsgs(channelId, messageId, unixStamp, "0");

        } catch (JSONException e) {
            e.printStackTrace();
        }

        ChannelMessage data = new ChannelMessage();
        data.messageId = messageId;
        data.channelId = channelId;
        data.messageType = type;
        data.message = getString(R.string.location);
        data.lat = lat;
        data.lon = lon;
        data.chatTime = unixStamp;
        data.deliveryStatus = "";
        messagesList.add(0, data);
        messageListAdapter.notifyItemInserted(0);
        recyclerView.smoothScrollToPosition(0);
    }

    private void emitContact(String type, String name, String phone, String countrycode) {
        String unixStamp = String.valueOf(System.currentTimeMillis() / 1000L);
        RandomString randomString = new RandomString(10);
        String messageId = channelId + randomString.nextString();
        try {

            JSONObject jsonObject = new JSONObject();
            jsonObject.put(Constants.TAG_CHANNEL_ID, channelId);
            jsonObject.put(Constants.TAG_CHANNEL_NAME, channelData.channelName);
            jsonObject.put(Constants.TAG_CHAT_TYPE, Constants.TAG_CHANNEL);
            jsonObject.put(Constants.TAG_MESSAGE_ID, messageId);
            jsonObject.put(Constants.TAG_MESSAGE_TYPE, type);
            jsonObject.put(Constants.TAG_MESSAGE, getString(R.string.contact));
            jsonObject.put(Constants.TAG_CONTACT_NAME, name);
            jsonObject.put(Constants.TAG_CONTACT_PHONE_NO, phone);
            jsonObject.put(Constants.TAG_CONTACT_COUNTRY_CODE, countrycode);
            jsonObject.put(Constants.TAG_CHAT_TIME, unixStamp);
            jsonObject.put(Constants.TAG_ADMIN_ID, channelData.channelAdminId);
            socketConnection.startChannelChat(jsonObject);
            dbhelper.addChannelMessages(channelId, Constants.TAG_CHANNEL, messageId, type,
                    getString(R.string.contact), "", "", "", name, phone, countrycode,
                    unixStamp, "", "read");

            dbhelper.addChannelRecentMsgs(channelId, messageId, unixStamp, "0");

        } catch (JSONException e) {
            e.printStackTrace();
        }

        ChannelMessage data = new ChannelMessage();
        data.messageType = type;
        data.message = getString(R.string.contact);
        data.contactName = name;
        data.contactPhoneNo = phone;
        data.contactCountryCode = countrycode;
        data.messageId = messageId;
        data.chatTime = unixStamp;
        data.deliveryStatus = "";
        messagesList.add(0, data);
        messageListAdapter.notifyItemInserted(0);
        recyclerView.smoothScrollToPosition(0);
    }

    private void deleteChatConfirmDialog() {
        final Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.setContentView(R.layout.default_popup);
        dialog.getWindow().setLayout(getResources().getDisplayMetrics().widthPixels * 90 / 100, ViewGroup.LayoutParams.WRAP_CONTENT);
        dialog.setCancelable(true);
        dialog.setCanceledOnTouchOutside(true);

        TextView title = dialog.findViewById(R.id.title);
        TextView yes = dialog.findViewById(R.id.yes);
        TextView no = dialog.findViewById(R.id.no);
        yes.setText(getString(R.string.im_sure));
        no.setText(getString(R.string.nope));
        title.setText(R.string.really_delete_chat_history);
        no.setVisibility(View.VISIBLE);

        yes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
                dbhelper.deleteChannelMessages(channelId);
                messagesList.clear();
                messageListAdapter.notifyDataSetChanged();
            }
        });

        no.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });

        dialog.show();
    }

    private void exitConfirmDialog() {
        final Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.setContentView(R.layout.default_popup);
        dialog.getWindow().setLayout(getResources().getDisplayMetrics().widthPixels * 90 / 100, ViewGroup.LayoutParams.WRAP_CONTENT);
        dialog.setCancelable(true);
        dialog.setCanceledOnTouchOutside(true);

        TextView title = dialog.findViewById(R.id.title);
        TextView yes = dialog.findViewById(R.id.yes);
        TextView no = dialog.findViewById(R.id.no);
        yes.setText(getString(R.string.im_sure));
        no.setText(getString(R.string.nope));
        title.setText(R.string.really_exit_group);
        no.setVisibility(View.VISIBLE);

        yes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
                try {
                    String unixStamp = String.valueOf(System.currentTimeMillis() / 1000L);
                    RandomString randomString = new RandomString(10);
                    String messageId = channelId + randomString.nextString();

                    JSONObject message = new JSONObject();
                    message.put(Constants.TAG_CHANNEL_ID, channelId);
                    message.put(Constants.TAG_CHANNEL_NAME, channelData.channelName);
                    message.put(Constants.TAG_CHAT_TYPE, Constants.TAG_CHANNEL);
                    message.put(Constants.TAG_CHAT_TIME, unixStamp);
                    message.put(Constants.TAG_MESSAGE_ID, messageId);
                    message.put(Constants.TAG_MEMBER_ID, GetSet.getUserId());
                    message.put(Constants.TAG_MEMBER_NAME, GetSet.getUserName());
                    message.put(Constants.TAG_MEMBER_NO, GetSet.getphonenumber());
                    message.put(Constants.TAG_MESSAGE_TYPE, getString(R.string.left));
                    message.put(Constants.TAG_MESSAGE, getString(R.string.one_participant_left));
                    message.put(Constants.TAG_ADMIN_ID, GetSet.getUserId());
                    socketConnection.startGroupChat(message);

                    JSONObject jsonObject = new JSONObject();
                    jsonObject.put(TAG_GROUP_ID, channelId);
                    jsonObject.put(TAG_MEMBER_ID, GetSet.getUserId());
                    socketConnection.exitFromGroup(jsonObject);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                bottomLay.setVisibility(View.GONE);
            }
        });

        no.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });

        dialog.show();
    }

    private void deleteMessageConfirmDialog(ChannelMessage mData) {
        final Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        dialog.setContentView(R.layout.default_popup);
        dialog.getWindow().setLayout(getResources().getDisplayMetrics().widthPixels * 90 / 100, ViewGroup.LayoutParams.WRAP_CONTENT);
        dialog.setCancelable(true);
        dialog.setCanceledOnTouchOutside(true);

        TextView title = dialog.findViewById(R.id.title);
        TextView yes = dialog.findViewById(R.id.yes);
        TextView no = dialog.findViewById(R.id.no);
        yes.setText(getString(R.string.im_sure));
        no.setText(getString(R.string.nope));
        title.setText(R.string.really_delete_msg);
        no.setVisibility(View.VISIBLE);

        yes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
                dbhelper.deleteChannelMessageFromId(mData.messageId);
                messagesList.remove(mData);
                Toast.makeText(ChannelChatActivity.this, getString(R.string.message_deleted), Toast.LENGTH_SHORT).show();
                selectedChatPos.clear();
                messageListAdapter.notifyDataSetChanged();
                chatUserLay.setVisibility(View.VISIBLE);
                forwordLay.setVisibility(View.GONE);
                chatLongPressed = false;
            }
        });

        no.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });

        dialog.show();
    }

    public String getFormattedDate(Context context, long smsTimeInMilis) {
        Calendar smsTime = Calendar.getInstance();
        smsTime.setTimeInMillis(smsTimeInMilis * 1000L);

        Calendar now = Calendar.getInstance();

        final String dateTimeFormatString = "d MMMM yyyy";
        if (now.get(Calendar.DATE) == smsTime.get(Calendar.DATE)) {
            return getString(R.string.today);
        } else if (now.get(Calendar.DATE) - smsTime.get(Calendar.DATE) == 1) {
            return getString(R.string.yesterday);
        } else {
            return DateFormat.format(dateTimeFormatString, smsTime).toString();
        }
    }

    private void uploadImage(byte[] imageBytes, final String imagePath, final ChannelMessage mdata, final String filePath) {
        RequestBody requestFile = RequestBody.create(MediaType.parse("image/*"), imageBytes);
        MultipartBody.Part body = MultipartBody.Part.createFormData("channel_attachment", "image.jpg", requestFile);

        RequestBody channelid = RequestBody.create(MediaType.parse("multipart/form-data"), channelId);
        RequestBody user_id = RequestBody.create(MediaType.parse("multipart/form-data"), GetSet.getUserId());
        Call<HashMap<String, String>> call3 = apiInterface.uploadChannelChat(GetSet.getToken(), body, channelid, user_id);
        call3.enqueue(new Callback<HashMap<String, String>>() {
            @Override
            public void onResponse(Call<HashMap<String, String>> call, Response<HashMap<String, String>> response) {
                HashMap<String, String> data = response.body();
                Log.i(TAG, "uploadChannelChat " + data);
                if (data.get(Constants.TAG_STATUS).equals(TRUE)) {
                    //File dir = new File(getExternalFilesDir(null) + "/" + getString(R.string.app_name) + "/" + getString(R.string.app_name) + "Images/Sent");
                    File dir = new File(StorageManager.getDataRoot() + "/" + getString(R.string.app_name) + "/" + getString(R.string.app_name) + "Images/Sent");

                    if (dir.exists()) {
                        File from = new File(imagePath);
                        File to = new File(dir + "/" + data.get(Constants.TAG_USER_IMAGE));
                        if (from.exists()) {
                            try {
                                FileUtils.copyFile(from, to);
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }

                        File file = storageManager.getImage("sent", data.get(Constants.TAG_USER_IMAGE));

                        final int imgSize = ApplicationClass.dpToPx(ChannelChatActivity.this, 170);
                        Log.v("file path", "file path=" + file.getAbsolutePath());

                        Bitmap bitmap = ImageUtils.compressImage(file.getAbsolutePath(), imgSize, imgSize);
                        String imgstatus = storageManager.saveThumbNail(bitmap, data.get(Constants.TAG_USER_IMAGE));
                        if (mdata.messageType.equals("image")) {
                            if (imgstatus.equals("success")) {
                                dbhelper.updateChannelMessageData(mdata.messageId, Constants.TAG_ATTACHMENT, data.get(Constants.TAG_USER_IMAGE));
                                dbhelper.updateChannelMessageData(mdata.messageId, Constants.TAG_PROGRESS, "completed");
                                if (messageListAdapter != null) {
                                    for (int i = 0; i < messagesList.size(); i++) {
                                        if (mdata.messageId.equals(messagesList.get(i).messageId)) {
                                            messagesList.get(i).attachment = data.get(Constants.TAG_USER_IMAGE);
                                            messagesList.get(i).progress = "completed";
                                            messageListAdapter.notifyItemChanged(i);
                                            break;
                                        }
                                    }
                                }
                            }
                            mdata.attachment = data.get(Constants.TAG_USER_IMAGE);
                            emitImage(mdata);
                        } else if (mdata.messageType.equals("video")) {
                            Log.v("checkChat", "uploadImage-video");
                            if (imgstatus.equals("success")) {
                                mdata.thumbnail = data.get(Constants.TAG_USER_IMAGE);
                                dbhelper.updateChannelMessageData(mdata.messageId, Constants.TAG_THUMBNAIL, data.get(Constants.TAG_USER_IMAGE));
                                if (messageListAdapter != null) {
                                    for (int i = 0; i < messagesList.size(); i++) {
                                        if (mdata.messageId.equals(messagesList.get(i).messageId)) {
                                            messagesList.get(i).thumbnail = mdata.thumbnail;
                                            messageListAdapter.notifyItemChanged(i);
                                            break;
                                        }
                                    }
                                }
                            }
                            Intent service = new Intent(ChannelChatActivity.this, FileUploadService.class);
                            Bundle b = new Bundle();
                            b.putSerializable("mdata", mdata);
                            b.putString("filepath", filePath);
                            b.putString("chatType", Constants.TAG_CHANNEL);
                            service.putExtras(b);
                            startService(service);
                        }
                    }
                } else {
                    dbhelper.updateChannelMessageData(mdata.messageId, Constants.TAG_PROGRESS, "error");
                    if (messageListAdapter != null) {
                        for (int i = 0; i < messagesList.size(); i++) {
                            if (mdata.messageId.equals(messagesList.get(i).messageId)) {
                                messagesList.get(i).progress = "error";
                                messageListAdapter.notifyItemChanged(i);
                                break;
                            }
                        }
                    }
                }
            }

            @Override
            public void onFailure(Call<HashMap<String, String>> call, Throwable t) {
                Log.e(TAG, "uploadChannelChat " + t.getMessage());
                call.cancel();
            }
        });
    }

    private void networkSnack() {
        Snackbar snackbar = Snackbar
                .make(mainLay, getString(R.string.network_failure), Snackbar.LENGTH_SHORT);
        View sbView = snackbar.getView();
        TextView textView = sbView.findViewById(com.google.android.material.R.id.snackbar_text);
        textView.setTextColor(Color.WHITE);
        snackbar.show();
    }

    private String isNetworkConnected() {
        return NetworkUtil.getConnectivityStatusString(this);
    }

    private ChannelMessage updateDBList(String type, String imagePath, String filePath) {
        String unixStamp = String.valueOf(System.currentTimeMillis() / 1000L);
        RandomString randomString = new RandomString(10);
        String messageId = channelId + randomString.nextString();

        String msg = "";
        if (type.equals("image")) {
            msg = getString(R.string.image);
        } else if (type.equals("audio")) {
            msg = getFileName(filePath);
        } else if (type.equals("video")) {
            msg = getString(R.string.video);
        } else if (type.equals("document")) {
            msg = getFileName(filePath);
        }

        ChannelMessage channelMessage = new ChannelMessage();
        channelMessage.channelId = channelId;
        channelMessage.channelName = channelData.channelName;
        channelMessage.channelAdminId = channelData.channelAdminId != null ? channelData.channelAdminId : channelData.adminId;
        channelMessage.chatType = channelData.channelCategory;
        channelMessage.messageType = type;
        channelMessage.message = msg;
        channelMessage.messageId = messageId;
        channelMessage.chatTime = unixStamp;
        channelMessage.deliveryStatus = "";
        channelMessage.progress = "";

        if (type.equals("video")) {
            channelMessage.thumbnail = imagePath;
            channelMessage.attachment = filePath;
            dbhelper.addChannelMessages(channelId, Constants.TAG_CHANNEL, messageId, type,
                    msg, filePath, "", "", "", "", "", unixStamp, imagePath, "read");

        } else if (type.equals("image")) {
            channelMessage.thumbnail = "";
            channelMessage.attachment = imagePath;
            dbhelper.addChannelMessages(channelId, Constants.TAG_CHANNEL, messageId, type,
                    msg, filePath, "", "", "", "", "", unixStamp, imagePath, "read");
        } else {
            channelMessage.thumbnail = "";
            channelMessage.attachment = filePath;
            dbhelper.addChannelMessages(channelId, Constants.TAG_CHANNEL, messageId, type,
                    msg, filePath, "", "", "", "", "", unixStamp, imagePath, "read");
        }

        dbhelper.addChannelRecentMsgs(channelId, messageId, unixStamp, "0");

        messagesList.add(0, channelMessage);
        messageListAdapter.notifyItemInserted(0);
        recyclerView.smoothScrollToPosition(0);

        return channelMessage;
    }

    public static boolean isVideoFile(String fileUrl) {
        String mimeType = getMimeType(fileUrl.replaceAll("\\s+", ""));
        return isVideo(mimeType);
    }

    public static String getMimeType(String fileUrl) {
        String extension = MimeTypeMap.getFileExtensionFromUrl(fileUrl);
        return MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension);
    }

    public static boolean isVideo(String mimeType) {
        Log.v("mimeType", "mimeType=" + mimeType);
        return mimeType != null && mimeType.startsWith("video");
    }

    private String getFileName(String url) {
        String imgSplit = url;
        int endIndex = imgSplit.lastIndexOf("/");
        if (endIndex != -1) {
            imgSplit = imgSplit.substring(endIndex + 1, imgSplit.length());
        }
        return imgSplit;
    }

    /**
     * Function for Sent a message to Socket
     */

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 100) {
            int permissionCamera = ContextCompat.checkSelfPermission(ChannelChatActivity.this,
                    CAMERA);
            int permissionAudio = ContextCompat.checkSelfPermission(ChannelChatActivity.this,
                    RECORD_AUDIO);

            if (permissionCamera == PackageManager.PERMISSION_GRANTED &&
                    permissionAudio == PackageManager.PERMISSION_GRANTED) {
              /*  Intent video = new Intent(ChatActivity.this, CallActivity.class);
                video.putExtra("from", "send");
                video.putExtra("type", "audio");
                video.putExtra("data", data);
                startActivity(video);*/
            }
        } else if (requestCode == 101) {
            int permissionCamera = ContextCompat.checkSelfPermission(ChannelChatActivity.this,
                    CAMERA);
            int permissionAudio = ContextCompat.checkSelfPermission(ChannelChatActivity.this,
                    RECORD_AUDIO);

            if (permissionCamera == PackageManager.PERMISSION_GRANTED &&
                    permissionAudio == PackageManager.PERMISSION_GRANTED) {
               /* Intent video = new Intent(ChatActivity.this, CallActivity.class);
                video.putExtra("from", "send");
                video.putExtra("type", "video");
                video.putExtra("data", data);
                startActivity(video);*/
            }
        } else if (requestCode == 102) {
            int permissionStorage = ContextCompat.checkSelfPermission(ChannelChatActivity.this, WRITE_EXTERNAL_STORAGE);

            if (permissionStorage == PackageManager.PERMISSION_GRANTED) {
                ImagePicker.pickImage(this, getString(R.string.select_your_image));
            }
        }
    }

    @Override
    protected void onActivityResult(final int requestCode, final int resultCode, final Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.v(TAG, "onActivityResult");
        if (resultCode == -1 && requestCode == 234) {
            if (isNetworkConnected().equals(NOT_CONNECT)) {
                networkSnack();
            } else {
                Log.v(TAG, "camera");
                Bitmap bitmap = ImagePicker.getImageFromResult(this, requestCode, resultCode, data);
                String timestamp = String.valueOf(System.currentTimeMillis() / 1000L);
                String imageStatus = storageManager.saveToSdCard(getApplicationContext(), bitmap, "sent", timestamp + ".jpg");
                if (imageStatus.equals("success")) {
                    File file = storageManager.getImage("sent", timestamp + ".jpg");
                    String filepath = file.getAbsolutePath();
                    Log.i(TAG, "selectedImageFile: " + filepath);
                    ImageCompression imageCompression = new ImageCompression(ChannelChatActivity.this) {
                        @Override
                        protected void onPostExecute(String imagePath) {
                            try {
                                ChannelMessage mdata = updateDBList("image", imagePath, "");
                                byte[] bytes = FileUtils.readFileToByteArray(new File(imagePath));
                                uploadImage(bytes, imagePath, mdata, "");
                            } catch (IOException ex) {
                                ex.printStackTrace();
                            }
                        }
                    };
                    imageCompression.execute(filepath);
                } else {
                    Toast.makeText(this, getString(R.string.something_wrong), Toast.LENGTH_SHORT).show();
                }
            }
        } else if (resultCode == -1 && requestCode == 150) {
            if (isNetworkConnected().equals(NOT_CONNECT)) {
                networkSnack();
            } else {
                pathsAry = new ArrayList<>();
                pathsAry.addAll(data.getStringArrayListExtra(FilePickerConst.KEY_SELECTED_MEDIA));
                if (pathsAry.size() > 0) {
                    Log.v(TAG, "File");
                    String filepath = pathsAry.get(0);
                    Log.i(TAG, "selectedFile: " + filepath);
                    if (isVideoFile(filepath)) {
                        try {
                            Log.v("checkChat", "videopath=" + filepath);
                            Bitmap thumb = ThumbnailUtils.createVideoThumbnail(filepath, MediaStore.Video.Thumbnails.MINI_KIND);
                            if (thumb != null) {
                                String timestamp = String.valueOf(System.currentTimeMillis() / 1000L);
                                String imageStatus = storageManager.saveToSdCard(getApplicationContext(), thumb, "sent", timestamp + ".jpg");
                                if (imageStatus.equals("success")) {
                                    File file = storageManager.getImage("sent", timestamp + ".jpg");
                                    String imagePath = file.getAbsolutePath();
                                    ChannelMessage mdata = updateDBList("video", imagePath, filepath);
                                    byte[] bytes = FileUtils.readFileToByteArray(new File(imagePath));
                                    uploadImage(bytes, imagePath, mdata, filepath);
                                }
                            }

                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    } else {
                        ImageCompression imageCompression = new ImageCompression(ChannelChatActivity.this) {
                            @Override
                            protected void onPostExecute(String imagePath) {
                                try {
                                    Log.v("checkChat", "imagepath=" + imagePath);
                                    ChannelMessage mdata = updateDBList("image", imagePath, "");
                                    byte[] bytes = org.apache.commons.io.FileUtils.readFileToByteArray(new File(imagePath));
                                    Log.e(TAG, "onActivityResult: " + imagePath);
                                    uploadImage(bytes, imagePath, mdata, "");
                                } catch (IOException ex) {
                                    ex.printStackTrace();
                                }
                            }
                        };
                        imageCompression.execute(filepath);
                    }
                } else {
                    Toast.makeText(this, getString(R.string.something_wrong), Toast.LENGTH_SHORT).show();
                }
            }
        } else if (resultCode == -1 && requestCode == 151) {
            if (isNetworkConnected().equals(NOT_CONNECT)) {
                networkSnack();
            } else {
                pathsAry = new ArrayList<>();
                pathsAry.addAll(data.getStringArrayListExtra(FilePickerConst.KEY_SELECTED_DOCS));
                if (pathsAry.size() > 0) {
                    Log.v(TAG, "File");
                    String filepath = pathsAry.get(0);
                    Log.i(TAG, "selectedImageFile: " + filepath);
                    try {
                        ChannelMessage mdata = updateDBList("document", "", filepath);
                        Intent service = new Intent(ChannelChatActivity.this, FileUploadService.class);
                        Bundle b = new Bundle();
                        b.putSerializable("mdata", mdata);
                        b.putString("filepath", filepath);
                        b.putString("chatType", Constants.TAG_CHANNEL);
                        service.putExtras(b);
                        startService(service);
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                } else {
                    Toast.makeText(this, getString(R.string.something_wrong), Toast.LENGTH_SHORT).show();
                }
            }
        } else if (resultCode == -1 && requestCode == 152) {
            if (isNetworkConnected().equals(NOT_CONNECT)) {
                networkSnack();
            } else {
                pathsAry = new ArrayList<>();
                pathsAry.addAll(data.getStringArrayListExtra(FilePickerConst.KEY_SELECTED_DOCS));
                if (pathsAry.size() > 0) {
                    Log.v(TAG, "Audio");
                    String filepath = pathsAry.get(0);
                    Log.i(TAG, "selectedImageFile: " + filepath);
                    try {
                        ChannelMessage mdata = updateDBList("audio", "", filepath);
                        Intent service = new Intent(ChannelChatActivity.this, FileUploadService.class);
                        Bundle b = new Bundle();
                        b.putSerializable("mdata", mdata);
                        b.putString("filepath", filepath);
                        b.putString("chatType", Constants.TAG_CHANNEL);
                        service.putExtras(b);
                        startService(service);
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                } else {
                    Toast.makeText(this, getString(R.string.something_wrong), Toast.LENGTH_SHORT).show();
                }
            }
        } else if (resultCode == -1 && requestCode == 200) {
            String lat = data.getStringExtra("lat");
            String lon = data.getStringExtra("lon");
            if (isNetworkConnected().equals(NOT_CONNECT)) {
                networkSnack();
            } else {
                emitLocation("location", lat, lon);
            }
        } else if (resultCode == -1 && requestCode == 13) {
            try {
                if (isNetworkConnected().equals(NOT_CONNECT)) {
                    networkSnack();
                } else {
                    Uri uri = data.getData();
                    Cursor cursor = getContentResolver().query(uri, null, null, null, null);
                    if (cursor.moveToFirst()) {
                        int phoneIndex = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER);
                        int nameIndex = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME);
                        String phoneNo = cursor.getString(phoneIndex);
                        String name = cursor.getString(nameIndex);

                        Log.v("Name & Contact", name + "," + phoneNo);

                        emitContact("contact", name, phoneNo, "");
                    }
                    cursor.close();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else if (resultCode == RESULT_OK && requestCode == 556) {
            username.setText(channelData.channelName);
        } else if (resultCode == RESULT_OK && requestCode == 222) {
            selectedChatPos.clear();
            messageListAdapter.notifyDataSetChanged();
            chatUserLay.setVisibility(View.VISIBLE);
            forwordLay.setVisibility(View.GONE);
            chatLongPressed = false;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        tempChannelId = channelId;
        if (!("" + channelData.channelAdminId).equalsIgnoreCase(GetSet.getUserId())) {
            bottomLay.setVisibility(View.GONE);
        }
        ApplicationClass.onShareExternal = false;
        if (channelData.channelCategory.equalsIgnoreCase(Constants.TAG_ADMIN_CHANNEL)) {
            AdminChannel.Result adminData = dbhelper.getAdminChannelInfo(channelId);
            Glide.with(ChannelChatActivity.this).load(Constants.CHANNEL_IMG_PATH + adminData.channelImage)
                    .apply(RequestOptions.circleCropTransform().placeholder(R.drawable.profile_square).error(R.drawable.profile_square))
                    .into(userimage);
            username.setText(adminData.channelName);
            txtMembers.setText("" + adminData.channelDes);
        } else {
            getChannelInfo(channelId);
        }
    }

    public String firstThree(String str) {
        return str.length() < 3 ? str : str.substring(0, 3);
    }

    private void setUI(ChannelResult.Result channelData) {
        if (channelData.channelCategory != null && channelData.channelCategory.equalsIgnoreCase(Constants.TAG_ADMIN_CHANNEL)) {
            Glide.with(getApplicationContext()).load(Constants.CHANNEL_IMG_PATH + channelData.channelImage)
                    .apply(RequestOptions.circleCropTransform().placeholder(R.drawable.change_camera).error(R.drawable.change_camera))
                    .into(userimage);
        } else {
            if (Utils.isChannelAdmin(channelData, GetSet.getUserId())) {
                bottomLay.setVisibility(View.VISIBLE);
            } else {
                bottomLay.setVisibility(View.GONE);
            }
//            if(channelData.blockStatus != null && channelData.blockStatus.equalsIgnoreCase("1")) {
//                txtBlocked.setVisibility(View.VISIBLE);
//                chatUserLay.setEnabled(false);
//            } else {
//                chatUserLay.setEnabled(true);
//                txtBlocked.setVisibility(View.GONE);
//            }
            Glide.with(getApplicationContext()).load(Constants.CHANNEL_IMG_PATH + channelData.channelImage)
                    .apply(RequestOptions.circleCropTransform().placeholder(R.drawable.ic_channel_square).error(R.drawable.ic_channel_square))
                    .into(userimage);
            channelAdminId = channelData.channelAdminId;
        }
        username.setText(channelData.channelName);
        txtMembers.setText("" + channelData.channelDes);
    }

    @Override
    public void onPause() {
        tempChannelId = "";
        editText.setError(null);
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.v("onDestroy", "onDestroy");
        if (Constants.isChannelChatOpened) {
            Constants.isChannelChatOpened = false;
        }
        SocketConnection.getInstance(this).setChannelChatCallbackListener(null);
    }

    @Override
    public void onBackPressed() {
        if (selectedChatPos.size() > 0) {
            selectedChatPos.clear();
            messageListAdapter.notifyDataSetChanged();
            chatUserLay.setVisibility(View.VISIBLE);
            forwordLay.setVisibility(View.GONE);
            chatLongPressed = false;
        } else {
            if (bottomSheetBehavior != null && bottomSheetBehavior.getState() == BottomSheetBehavior.STATE_EXPANDED) {
                bottomSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);
            } else {
                if (isFromNotification) {
                    Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                    intent.putExtra(Constants.IS_FROM, "channel");
                    startActivity(intent);
                    finish();
                } else {
                    finish();
                }
            }
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.send:
                if (isNetworkConnected().equals(NOT_CONNECT)) {
                    networkSnack();
                } else if (editText.getText().toString().trim().length() > 0) {
                    String unixStamp = String.valueOf(System.currentTimeMillis() / 1000L);
                    String textMsg = editText.getText().toString().trim();
                    RandomString randomString = new RandomString(10);
                    String messageId = channelId + randomString.nextString();
                    try {
                        String type = "text";
                        JSONObject jsonObject = new JSONObject();
                        jsonObject.put(Constants.TAG_CHANNEL_ID, channelId);
                        jsonObject.put(Constants.TAG_CHANNEL_NAME, channelData.channelName);
                        jsonObject.put(Constants.TAG_CHAT_TYPE, Constants.TAG_CHANNEL);
                        jsonObject.put(Constants.TAG_MESSAGE_ID, messageId);
                        jsonObject.put(Constants.TAG_MESSAGE_TYPE, type);
                        jsonObject.put(Constants.TAG_MESSAGE, textMsg);
                        jsonObject.put(Constants.TAG_CHAT_TIME, unixStamp);
                        jsonObject.put(Constants.TAG_ADMIN_ID, GetSet.getUserId());
                        socketConnection.startChannelChat(jsonObject);

                        dbhelper.addChannelMessages(channelId, Constants.TAG_CHANNEL, messageId, type,
                                textMsg, "", "", "", "", "",
                                "", unixStamp, "", "read");

                        dbhelper.addChannelRecentMsgs(channelId, messageId, unixStamp, "0");
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                    ChannelMessage channelMessage = new ChannelMessage();
                    channelMessage.chatType = channelData.channelCategory;
                    channelMessage.messageType = "text";
                    channelMessage.message = textMsg;
                    channelMessage.messageId = messageId;
                    channelMessage.chatTime = unixStamp;
                    channelMessage.deliveryStatus = "";
                    messagesList.add(0, channelMessage);
                    messageListAdapter.notifyItemInserted(0);
                    recyclerView.smoothScrollToPosition(0);
                    editText.setText("");
                } else {
                    editText.setError(getString(R.string.please_enter_your_message));
                }
                break;
            case R.id.backbtn:
                onBackPressed();
                break;
            case R.id.optionbtn:
                Display display = this.getWindowManager().getDefaultDisplay();
                final ArrayList<String> values = new ArrayList<>();
                ChannelResult.Result results = dbhelper.getChannelInfo(channelId);
                if (Utils.isUserAdminInChannel(results)) {
                    if (results.blockStatus == null || !results.blockStatus.equals("1")) {
                        values.add(getString(R.string.clear_chat));
                        values.add(getString(R.string.invite_subscribers));
                    }
                    values.add(getString(R.string.delete_channel));
                } else {
                    if (results.blockStatus == null || !results.blockStatus.equals("1")) {
                        if (results.muteNotification.equals("true")) {
                            values.add(getString(R.string.unmute_notification));
                        } else {
                            values.add(getString(R.string.mute_notification));
                        }
                        values.add(getString(R.string.clear_chat));

                        if (results.channelCategory.equalsIgnoreCase(Constants.TAG_USER_CHANNEL)) {
                            if (results.channelType.equalsIgnoreCase(Constants.TAG_PUBLIC)) {
                                values.add(getString(R.string.invite_subscribers));
                            }

                            values.add(getString(R.string.unsubscribe_channel));
//                            values.add(getString(R.string.report));
                        }
                    } else {
                        values.add(getString(R.string.unsubscribe_channel));
                    }
                }

                final ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
                        R.layout.option_item, android.R.id.text1, values);
                LayoutInflater layoutInflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                View layout = layoutInflater.inflate(R.layout.option_layout, null);
                layout.setAnimation(AnimationUtils.loadAnimation(this, R.anim.grow_from_topright_to_bottomleft));
                final PopupWindow popup = new PopupWindow(ChannelChatActivity.this);
                popup.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                popup.setContentView(layout);
                popup.setWidth(display.getWidth() * 60 / 100);
                popup.setHeight(ViewGroup.LayoutParams.WRAP_CONTENT);
                popup.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_NOTHING);
                popup.setFocusable(true);
                popup.showAtLocation(mainLay, Gravity.TOP | Gravity.RIGHT, ApplicationClass.dpToPx(this, 10), ApplicationClass.dpToPx(this, 63));

                final ListView lv = layout.findViewById(R.id.listView);
                lv.setAdapter(adapter);
                popup.showAsDropDown(v);

                lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {

                    @Override
                    public void onItemClick(AdapterView<?> parent, View view,
                                            int position, long id) {
                        popup.dismiss();
                        if (values.get(position).equalsIgnoreCase(getString(R.string.mute_notification))) {
                            dbhelper.updateChannelData(channelId, Constants.TAG_MUTE_NOTIFICATION, "true");
                        } else if (values.get(position).equalsIgnoreCase(getString(R.string.unmute_notification))) {
                            dbhelper.updateChannelData(channelId, Constants.TAG_MUTE_NOTIFICATION, "");
                        } else if (values.get(position).equalsIgnoreCase(getString(R.string.clear_chat))) {
                            deleteChatConfirmDialog();
                        } else if (values.get(position).equalsIgnoreCase(getString(R.string.invite_subscribers))) {
                            Intent subscribers = new Intent(getApplicationContext(), NewChannelActivity.class);
                            subscribers.putExtra(Constants.IS_EDIT, true);
                            subscribers.putExtra(Constants.TAG_CHANNEL_ID, channelId);
                            startActivity(subscribers);
                        } else if (values.get(position).equalsIgnoreCase(getString(R.string.unsubscribe_channel))) {
                            if (isNetworkConnected().equals(NOT_CONNECT)) {
                                networkSnack();
                            } else {
                                JSONObject jsonObject = new JSONObject();
                                try {
                                    jsonObject.put(Constants.TAG_USER_ID, GetSet.getUserId());
                                    jsonObject.put(Constants.TAG_CHANNEL_ID, results.channelId);
                                    socketConnection.unsubscribeChannel(jsonObject, results.channelId, results.totalSubscribers);
                                    finish();
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            }
                        } else if (values.get(position).equalsIgnoreCase(getString(R.string.delete_channel))) {
                            if (isNetworkConnected().equals(NOT_CONNECT)) {
                                networkSnack();
                            } else {
                                JSONObject jsonObject = new JSONObject();
                                try {
                                    jsonObject.put(Constants.TAG_CHANNEL_ID, channelId);
                                    jsonObject.put(Constants.TAG_USER_ID, GetSet.getUserId());
                                    socketConnection.leaveChannel(jsonObject, channelId);
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            }
                        } else if (values.get(position).equalsIgnoreCase(getString(R.string.report))) {
                            openReportDialog();
                        }
                    }
                });
                break;
            case R.id.closeBtn:
                if (bottomSheetBehavior != null) {
                    bottomSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);
                }
                break;
            case R.id.attachbtn:
                TransitionManager.beginDelayedTransition(mainLay);
                visible = !visible;
                attachmentsLay.setVisibility(visible ? View.VISIBLE : View.GONE);
                break;
            case R.id.userImg:
                break;
            case R.id.cameraBtn:
                if (ContextCompat.checkSelfPermission(this, CAMERA) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(this, new String[]{CAMERA}, 100);
                } else if (ContextCompat.checkSelfPermission(this, WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(this, new String[]{WRITE_EXTERNAL_STORAGE}, 100);
                } else if (isNetworkConnected().equals(NOT_CONNECT)) {
                    networkSnack();
                } else {
                    ApplicationClass.onShareExternal = true;
                    ImagePicker.pickImageCameraOnly(this, 234);
                }
                break;
            case R.id.galleryBtn:
                if (ContextCompat.checkSelfPermission(this, WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(this, new String[]{WRITE_EXTERNAL_STORAGE}, 100);
                } else if (isNetworkConnected().equals(NOT_CONNECT)) {
                    networkSnack();
                } else {
                    FilePickerBuilder.getInstance()
                            .setMaxCount(1)
                            .setActivityTheme(R.style.MainTheme)
                            .setActivityTitle(getString(R.string.please_select_media))
                            .enableVideoPicker(true)
                            .enableImagePicker(true)
                            .enableCameraSupport(false)
                            .showGifs(false)
                            .showFolderView(false)
                            .enableSelectAll(false)
//                            .withOrientation(Orientation.UNSPECIFIED)
                            .pickPhoto(this, 150);
                }
                break;
            case R.id.fileBtn:
                if (isNetworkConnected().equals(NOT_CONNECT)) {
                    networkSnack();
                } else {
                    FilePickerBuilder.getInstance()
                            .setMaxCount(1)
                            .enableDocSupport(true)
                            .setActivityTitle(getString(R.string.please_select_document))
//                            .showTabLayout(true)
                            .setActivityTheme(R.style.MainTheme)
                            .pickFile(this, 151);
                }
                break;
            case R.id.audioBtn:
                if (isNetworkConnected().equals(NOT_CONNECT)) {
                    networkSnack();
                } else {
                    String[] aud = {".mp3", ".wav", ".flac", ".3gp", ".ogg"};
                    FilePickerBuilder.getInstance()
                            .setMaxCount(1)
                            .setActivityTheme(R.style.MainTheme)
                            .setActivityTitle(getString(R.string.please_select_audio))
                            .addFileSupport("MP3", aud)
                            .enableDocSupport(false)
                            .enableSelectAll(true)
//                            .showTabLayout(false)
                            .sortDocumentsBy(SortingTypes.name)
//                            .withOrientation(Orientation.UNSPECIFIED)
                            .pickFile(this, 152);

                }
                break;
            case R.id.locationBtn:
                if (isNetworkConnected().equals(NOT_CONNECT)) {
                    networkSnack();
                } else {
                    Intent location = new Intent(this, LocationActivity.class);
                    location.putExtra("from", "share");
                    startActivityForResult(location, 200);
                }
                break;
            case R.id.contactBtn:
                if (isNetworkConnected().equals(NOT_CONNECT)) {
                    networkSnack();
                } else {
                    ApplicationClass.onShareExternal = true;
                    Intent intentc = new Intent(Intent.ACTION_PICK, ContactsContract.CommonDataKinds.Phone.CONTENT_URI);
                    startActivityForResult(intentc, 13);
                }
                break;
            case R.id.chatUserLay:
                Intent profile = new Intent(ChannelChatActivity.this, ChannelInfoActivity.class);
                profile.putExtra(Constants.TAG_CHANNEL_ID, channelId);
                startActivity(profile);
                break;
            case R.id.forwordBtn:
                Intent f = new Intent(ChannelChatActivity.this, ForwardActivity.class);
                f.putExtra("from", "channel");
                f.putExtra("data", selectedChatPos.get(0));
                startActivityForResult(f, 222);
                break;
            case R.id.copyBtn:
                if (selectedChatPos.size() > 0) {
                    String msg = selectedChatPos.get(0).message;
                    ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                    ClipData clip = ClipData.newPlainText("Copied Message", msg);
                    clipboard.setPrimaryClip(clip);
                    Toast.makeText(this, getString(R.string.message_copied), Toast.LENGTH_SHORT).show();
                    selectedChatPos.clear();
                    messageListAdapter.notifyDataSetChanged();
                    chatUserLay.setVisibility(View.VISIBLE);
                    forwordLay.setVisibility(View.GONE);
                    chatLongPressed = false;
                }
                break;
            case R.id.deleteBtn:
                deleteMessageConfirmDialog(selectedChatPos.get(0));
                break;
        }

    }

    private void openReportDialog() {
        Dialog reportDialog = new Dialog(ChannelChatActivity.this);
        reportDialog.setCancelable(true);
        if (reportDialog.getWindow() != null) {
            reportDialog.getWindow().requestFeature(Window.FEATURE_NO_TITLE);
            reportDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        }
        reportDialog.setContentView(R.layout.dialog_report);

        TextView btnSpam, btnOther;
        btnSpam = reportDialog.findViewById(R.id.btnSpam);
        btnOther = reportDialog.findViewById(R.id.btnOther);

        btnSpam.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                reportChannel(channelId, "spam");
                reportDialog.dismiss();
            }
        });

        btnOther.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent report = new Intent(ChannelChatActivity.this, ReportActivity.class);
                report.putExtra(Constants.TAG_CHANNEL_ID, channelId);
                startActivity(report);
                reportDialog.dismiss();
            }
        });

        reportDialog.show();
    }

    private void reportChannel(String channelId, String description) {
        HashMap<String, String> hashMap = new HashMap<>();
        hashMap.put(Constants.TAG_USER_ID, GetSet.getUserId());
        hashMap.put(Constants.TAG_CHANNEL_ID, channelId);
        hashMap.put(Constants.TAG_REPORT, description);

        Call<HashMap<String, String>> call = apiInterface.reportChannel(GetSet.getToken(), hashMap);
        call.enqueue(new Callback<HashMap<String, String>>() {
            @Override
            public void onResponse(Call<HashMap<String, String>> call, Response<HashMap<String, String>> response) {
                Log.i(TAG, "ReportChannel Response: " + response.body());
                makeToast(getString(R.string.channel_reported_successfully));
//                if (response.body().get(Constants.TAG_STATUS).equalsIgnoreCase(Constants.TRUE)) {
//                }
            }

            @Override
            public void onFailure(Call<HashMap<String, String>> call, Throwable t) {
                call.cancel();
                Log.e(TAG, "Report Channel onFailure: " + t.getMessage());
            }
        });
    }
}

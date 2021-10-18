package com.topzi.chat.activity;

import android.Manifest;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.Dialog;
import android.app.NotificationManager;
import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.ColorDrawable;
import android.media.ExifInterface;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.media.RingtoneManager;
import android.media.ThumbnailUtils;
import android.net.TrafficStats;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Environment;
import android.os.Handler;
import android.provider.ContactsContract;
import android.provider.MediaStore;
import android.provider.Settings;
import android.text.Editable;
import android.text.Html;
import android.text.TextWatcher;
import android.text.format.DateFormat;
import android.text.util.Linkify;
import android.util.Log;
import android.util.TypedValue;
import android.view.Display;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.EditorInfo;
import android.webkit.MimeTypeMap;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.MediaController;
import android.widget.PopupWindow;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.Toolbar;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.transition.TransitionManager;

import com.androidnetworking.AndroidNetworking;
import com.androidnetworking.common.Priority;
import com.androidnetworking.error.ANError;
import com.androidnetworking.interfaces.JSONObjectRequestListener;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.gson.Gson;
import com.quickblox.chat.QBChatService;
import com.quickblox.core.Consts;
import com.quickblox.core.QBEntityCallback;
import com.quickblox.core.exception.QBResponseException;
import com.quickblox.core.request.GenericQueryRule;
import com.quickblox.core.request.QBPagedRequestBuilder;
import com.quickblox.users.model.QBUser;
import com.quickblox.videochat.webrtc.QBRTCClient;
import com.quickblox.videochat.webrtc.QBRTCSession;
import com.quickblox.videochat.webrtc.QBRTCTypes;
import com.shain.messenger.MessageSwipeController;
import com.shain.messenger.SwipeControllerActions;
import com.topzi.chat.BuildConfig;
import com.topzi.chat.R;
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
import com.topzi.chat.helper.NetworkUtil;
import com.topzi.chat.helper.QbUsersDbManager;
import com.topzi.chat.helper.RecordingUploadService;
import com.topzi.chat.helper.SocketConnection;
import com.topzi.chat.helper.StorageManager;
import com.topzi.chat.model.ContactsData;
import com.topzi.chat.model.DataStorageModel;
import com.topzi.chat.model.GroupData;
import com.topzi.chat.model.GroupMessage;
import com.topzi.chat.model.MediaDown.MobileData;
import com.topzi.chat.model.MessagesData;
import com.topzi.chat.model.UpMyChatModel;
import com.topzi.chat.service.CallService;
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
import com.vanniktech.emoji.EmojiPopup;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URISyntaxException;
import java.text.CharacterIterator;
import java.text.StringCharacterIterator;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.TimeZone;
import java.util.Timer;
import java.util.TimerTask;

import de.hdodenhof.circleimageview.CircleImageView;
import droidninja.filepicker.FilePickerBuilder;
import droidninja.filepicker.FilePickerConst;
import droidninja.filepicker.models.sort.SortingTypes;
import droidninja.filepicker.utils.ContentUriUtils;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static android.Manifest.permission.CAMERA;
import static android.Manifest.permission.READ_CONTACTS;
import static android.Manifest.permission.READ_PHONE_STATE;
import static android.Manifest.permission.RECORD_AUDIO;
import static android.Manifest.permission.WAKE_LOCK;
import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;
import static com.topzi.chat.activity.ChatActivity.MessageListAdapter.VIEW_TYPE_DATE;
import static com.topzi.chat.helper.NetworkUtil.NOT_CONNECT;
import static com.topzi.chat.utils.Constants.API_VERSION;
import static com.topzi.chat.utils.Constants.BASE_URL;
import static com.topzi.chat.utils.Constants.MUTE_NOTI;
import static com.topzi.chat.utils.Constants.REPORT_USER;
import static com.topzi.chat.utils.Constants.STAR_MSG;
import static com.topzi.chat.utils.Constants.TAG_FRIENDID;
import static com.topzi.chat.utils.Constants.TAG_GROUP_ID;
import static com.topzi.chat.utils.Constants.TAG_MY_CONTACTS;
import static com.topzi.chat.utils.Constants.TAG_NOBODY;
import static com.topzi.chat.utils.Constants.TAG_REPLY_TO;
import static com.topzi.chat.utils.Constants.TAG_USER_ID;
import static com.topzi.chat.utils.Constants.TRUE;
import static com.topzi.chat.utils.Constants.USER_ID;
import static com.topzi.chat.utils.Constants.getConnectionType;
import static com.topzi.chat.utils.Constants.isDataRoamingEnabled;
import static com.topzi.chat.utils.Constants.setStatusBarGradiant;

public class ChatActivity extends BaseActivity implements View.OnClickListener, SocketConnection.ChatCallbackListener, TextWatcher {

    EditText editText;
    String userId, msgReplyTo = null, groupReply = null;
    List<MessagesData> messagesList = new ArrayList<>();
    String TAG = "ChatActivity";
    RecyclerView recyclerView;
    TextView username, online;
    LinearLayout llWallpaper;
    TextView tvGallary, tvSolidColor, tvWallpaper;
    RelativeLayout chatUserLay, mainLay, attachmentsLay, imageViewLay, bottomLay, forwordLay;
    ImageView attachbtn, optionbtn, backbtn, send, audioCallBtn, videoCallBtn, cameraBtn,
            galleryBtn, fileBtn, audioBtn, locationBtn, contactBtn, imageView, closeBtn, forwordBtn, copyBtn, deleteBtn, starBtn;
    CircleImageView userimage;
    ImageView imgChatBg, imgEmoji, imgReply;
    VideoView videoView;
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
    ArrayList<Uri> pathsAry = new ArrayList<>();
    Timer onlineTimer = new Timer();
    Handler handler = new Handler();
    Runnable runnable;
    ContactsData.Result results;
    EndlessRecyclerOnScrollListener endlessRecyclerOnScrollListener;
    public static String tempUserId = "";
    SharedPreferences pref;
    SharedPreferences.Editor editor;
    SharedPreferences prefData;
    SharedPreferences.Editor editorData;
    ArrayList<MessagesData> selectedChatPos = new ArrayList<>();
    private boolean isFromNotification = false;
    private Dialog permissionDialog;
    FloatingActionButton btnRecord;
    TextView textRecordTimer, txtReplyTo, txtMsgReply;
    LinearLayout layoutTypeMessage;
    EmojiPopup emojiPopup;
    public static ArrayList<Integer> solidWallpaper = new ArrayList<>();
    private SolidColorAdapter solidColorAdapter;
    ConstraintLayout replyContainer, lytReply;
    ImageButton btnCancelReply;
    BottomSheetBehavior behavior;
    Dialog dialog;
    Dialog dialog1;

    private MediaRecorder recorder = null;
    CountDownTimer countDownTimer;
    int second = -1, minute, hour;

    File recording = null;

    int fontSize = 20;
    Handler recordingHandler;
    Runnable recordingCallback;

    String editImagePath = "";
    MediaController mediacontroller;

    private long rxBytesFinal;
    private long txBytesFinal;

    private long rxMesBytesFinal;
    private long txMesBytesFinal;

    MobileData Mobiledata1;
    MobileData WifiData;
    MobileData RoamingData;

    private QbUsersDbManager dbManager;
    List<QBUser> currentOpponentsList = new ArrayList<>();
    private static final int PER_PAGE_SIZE_100 = 100;
    private static final String ORDER_RULE = "order";
    private static final String ORDER_DESC_UPDATED = "desc date updated_at";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStatusBarGradiant(ChatActivity.this);
        setContentView(R.layout.activity_main_chat);

        AndroidNetworking.initialize(ChatActivity.this);
        dbManager = QbUsersDbManager.getInstance(getApplicationContext());

        if (getIntent().getStringExtra("notification") != null) {
            Constants.isChatOpened = true;
            isFromNotification = true;
            NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            if (notificationManager != null) {
                notificationManager.cancelAll();
            }
        }
        if (Constants.chatContext != null && Constants.isChatOpened) {
            ((Activity) Constants.chatContext).finish();
        }
        mediacontroller = new MediaController(this);
        mediacontroller.setAnchorView(videoView);
        solidWallpaper.clear();
        solidWallpaper.add(R.color.solid_1);
        solidWallpaper.add(R.color.solid_2);
        solidWallpaper.add(R.color.solid_3);
        solidWallpaper.add(R.color.solid_4);
        solidWallpaper.add(R.color.solid_5);
        solidWallpaper.add(R.color.solid_6);
        solidWallpaper.add(R.color.solid_7);
        solidWallpaper.add(R.color.solid_8);
        solidWallpaper.add(R.color.solid_9);
        solidWallpaper.add(R.color.solid_10);
        solidWallpaper.add(R.color.solid_11);
        solidWallpaper.add(R.color.solid_12);
        solidWallpaper.add(R.color.solid_13);
        solidWallpaper.add(R.color.solid_14);
        solidWallpaper.add(R.color.solid_15);
        solidWallpaper.add(R.color.solid_16);
        solidWallpaper.add(R.color.solid_17);
        solidWallpaper.add(R.color.solid_18);
        solidWallpaper.add(R.color.solid_19);
        solidWallpaper.add(R.color.solid_20);
        solidWallpaper.add(R.color.solid_21);

        solidColorAdapter = new SolidColorAdapter();

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        pref = ChatActivity.this.getSharedPreferences(Constants.PREFERENCE_NAME, MODE_PRIVATE);
        editor = pref.edit();
        prefData = ChatActivity.this.getSharedPreferences(Constants.NETWORK_USAGE, MODE_PRIVATE);
        editorData = prefData.edit();
        Constants.chatContext = this;

        if (!prefData.getString("MobileData", "").equals("") &&
                prefData.getString("MobileData", "") != null) {
            Gson gson1 = new Gson();
            String json1 = prefData.getString("MobileData", "");
            Mobiledata1 = gson1.fromJson(json1, MobileData.class);
        } else {
            Mobiledata1 = new MobileData();
            Mobiledata1.setPhotos(true);
            Mobiledata1.setAudio(false);
            Mobiledata1.setVideo(false);
            Mobiledata1.setDoc(false);

            Gson gson = new Gson();
            String json = gson.toJson(Mobiledata1);
            editorData.putString("MobileData", json);
            editorData.apply();
            editorData.commit();
        }

        if (!prefData.getString("WifiData", "").equals("") &&
                prefData.getString("WifiData", "") != null) {
            Gson gson2 = new Gson();
            String json2 = prefData.getString("WifiData", "");
            WifiData = gson2.fromJson(json2, MobileData.class);
        } else {
            WifiData = new MobileData();
            WifiData.setPhotos(true);
            WifiData.setAudio(true);
            WifiData.setVideo(true);
            WifiData.setDoc(true);

            Gson gson = new Gson();
            String json = gson.toJson(WifiData);
            editorData.putString("WifiData", json);
            editorData.apply();
            editorData.commit();
        }

        if (!prefData.getString("RoamingData", "").equals("") &&
                prefData.getString("RoamingData", "") != null) {
            Gson gson3 = new Gson();
            String json3 = prefData.getString("RoamingData", "");
            RoamingData = gson3.fromJson(json3, MobileData.class);
        } else {
            RoamingData = new MobileData();
            RoamingData.setPhotos(false);
            RoamingData.setAudio(false);
            RoamingData.setVideo(false);
            RoamingData.setDoc(false);

            Gson gson = new Gson();
            String json = gson.toJson(RoamingData);
            editorData.putString("RoamingData", json);
            editorData.apply();
            editorData.commit();
        }

        imgChatBg = findViewById(R.id.img_chat_bg);
        imgEmoji = findViewById(R.id.img_emoji);
        llWallpaper = findViewById(R.id.ll_wallpaper);
        tvGallary = findViewById(R.id.tv_gallary);
        tvSolidColor = findViewById(R.id.tv_solid_color);
        tvWallpaper = findViewById(R.id.tv_wallpaper);
        recyclerView = findViewById(R.id.recyclerView);
        send = findViewById(R.id.send);
        editText = findViewById(R.id.editText);
        chatUserLay = findViewById(R.id.chatUserLay);
        userimage = findViewById(R.id.userImg);
        username = findViewById(R.id.userName);
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
        videoView = findViewById(R.id.videoView);
        closeBtn = findViewById(R.id.closeBtn);
        forwordLay = findViewById(R.id.forwordLay);
        forwordBtn = findViewById(R.id.forwordBtn);
        copyBtn = findViewById(R.id.copyBtn);
        deleteBtn = findViewById(R.id.deleteBtn);
        starBtn = findViewById(R.id.starBtn);
        btnRecord = findViewById(R.id.btnRecord);
        textRecordTimer = findViewById(R.id.textRecordTimer);
        layoutTypeMessage = findViewById(R.id.layoutTypeMessage);
        txtReplyTo = findViewById(R.id.txt_user_name);
        txtMsgReply = findViewById(R.id.txt_message);
        imgReply = findViewById(R.id.img_reply);
        replyContainer = findViewById(R.id.reply_layout);
        lytReply = findViewById(R.id.lyt_reply);
        btnCancelReply = findViewById(R.id.btn_cancel_reply);
        emojiPopup = EmojiPopup.Builder.fromRootView(mainLay).build(editText);
        behavior = BottomSheetBehavior.from(llWallpaper);
        behavior.setBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {
            @Override
            public void onStateChanged(@NonNull View bottomSheet, int newState) {
                // React to state change
            }

            @Override
            public void onSlide(@NonNull View bottomSheet, float slideOffset) {
                // React to dragging events
            }
        });

        socketConnection = SocketConnection.getInstance(this);
        SocketConnection.getInstance(this).setChatCallbackListener(this);
        bottomSheetBehavior = BottomSheetBehavior.from(imageViewLay);
        apiInterface = ApiClient.getClient().create(ApiInterface.class);
        dbhelper = DatabaseHandler.getInstance(this);
        storageManager = StorageManager.getInstance(this);
        display = getWindowManager().getDefaultDisplay();

        if (getIntent().getStringExtra("EXTRA_EDITED_PATH") != null) {
            editImagePath = getIntent().getStringExtra("EXTRA_EDITED_PATH");
            userId = pref.getString("sendImageUSerID", "");
            tempUserId = userId;
            if (isNetworkConnected().equals(NOT_CONNECT)) {
                networkSnack();
            } else {
                if (pathsAry.size() > 0) {
                    Log.v(TAG, "File");
                    String filepath = null;
                    filepath = editImagePath;
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
                                    MessagesData mdata = updateDBList("video", imagePath, filepath);
                                    byte[] bytes = org.apache.commons.io.FileUtils.readFileToByteArray(new File(imagePath));
                                    uploadImage(bytes, imagePath, mdata, filepath);
                                }
                            }
                        } catch (IOException ex) {
                            ex.printStackTrace();
                            Log.e("LLLLLL16: ", Objects.requireNonNull(ex.getMessage()));
                            Toast.makeText(ChatActivity.this, "16:" + getString(R.string.something_wrong), Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        ImageCompression imageCompression = new ImageCompression(ChatActivity.this) {
                            @Override
                            protected void onPostExecute(String imagePath) {
                                try {
                                    Log.v("checkChat", "imagepath=" + imagePath);
                                    MessagesData mdata = updateDBList("image", imagePath, "");
                                    byte[] bytes = org.apache.commons.io.FileUtils.readFileToByteArray(new File(imagePath));
                                    uploadImage(bytes, imagePath, mdata, "");
                                } catch (IOException ex) {
                                    ex.printStackTrace();
                                }
                            }
                        };
                        imageCompression.execute(filepath);
                    }
                } else {
                    Toast.makeText(this, "17:" + getString(R.string.something_wrong), Toast.LENGTH_SHORT).show();
                }
            }
        } else {
            userId = getIntent().getExtras().getString(USER_ID);
            groupReply = getIntent().getExtras().getString(TAG_GROUP_ID);
            msgReplyTo = getIntent().getExtras().getString(TAG_REPLY_TO);
            tempUserId = userId;

            if (groupReply != null && !groupReply.equals("")) {
                showGroupReplyTo();
            }
        }

        // set visibility status
        chatUserLay.setVisibility(View.VISIBLE);
        backbtn.setVisibility(View.VISIBLE);
        audioCallBtn.setVisibility(View.VISIBLE);
        videoCallBtn.setVisibility(View.VISIBLE);
        optionbtn.setVisibility(View.VISIBLE);

        Log.v("userId", "userId=" + userId);
        results = dbhelper.getContactDetail(userId);

        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        if (notificationManager != null) {
            notificationManager.cancel(ApplicationClass.getContactName(this, results.phone_no), 0);
        }

        username.setText(ApplicationClass.getContactName(this, results.phone_no));
        if (results.blockedme == null || !results.blockedme.equals("block")) {
            DialogActivity.setProfileImage(dbhelper.getContactDetail(userId), userimage, this);
            online.setVisibility(View.VISIBLE);
        } else {
            Glide.with(ChatActivity.this)
                    .load(R.drawable.person)
                    .apply(RequestOptions.circleCropTransform()
                            .placeholder(R.drawable.person)
                            .error(R.drawable.person))
                    .into(userimage);
            online.setVisibility(View.GONE);
        }
        if (results.blockedbyme != null && results.blockedbyme.equals("block")) {
            online.setVisibility(View.GONE);
        }

        totalMsg = dbhelper.getMessagesCount(GetSet.getUserId() + userId);
        Log.v("totalMsg", "totalMsg=" + totalMsg);

        try {
            messagesList.addAll(getMessagesAry(dbhelper.getMessages(GetSet.getUserId() + userId, "0", "20"), null));
        } catch (Exception e) {
            e.printStackTrace();
        }
        linearLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        linearLayoutManager.setReverseLayout(true);
        recyclerView.setLayoutManager(linearLayoutManager);

        messageListAdapter = new MessageListAdapter(this, messagesList);
        recyclerView.setAdapter(messageListAdapter);

        DividerItemDecoration divider = new DividerItemDecoration(recyclerView.getContext(),
                linearLayoutManager.getOrientation());
        divider.setDrawable(getResources().getDrawable(R.drawable.emptychat_divider));
        recyclerView.addItemDecoration(divider);

        send.setOnClickListener(this);
        backbtn.setOnClickListener(this);
        attachbtn.setOnClickListener(this);
        optionbtn.setOnClickListener(this);
        tvGallary.setOnClickListener(this);
        tvSolidColor.setOnClickListener(this);
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
        starBtn.setOnClickListener(this);

        if (!pref.getString("newWallpaper", "").equals("")) {
            File file = storageManager.getImage("wallpaper", pref.getString("newWallpaper", ""));
            Uri uri = Uri.fromFile(file);
            Glide.with(ChatActivity.this)
                    .load(uri)
                    .into(imgChatBg);
        } else if (!pref.getString("oldWallpaper", "").equals("")) {
            Uri uri = Uri.fromFile(new File(pref.getString("oldWallpaper", "")));
            Glide.with(ChatActivity.this)
                    .load(uri)
                    .into(imgChatBg);
        } else if (pref.getInt("solidColorPos", 0) != 0) {
            imgChatBg.setBackgroundColor(getResources().getColor(solidWallpaper.get(pref.getInt("solidColorPos", 0))));
        } else {
            imgChatBg.setImageDrawable(getResources().getDrawable(R.drawable.chat_bg));
        }

        onlineTimer.scheduleAtFixedRate(new TimerTask() {
            public void run() {
                //Function call every second
                if ((results.blockedme == null || !results.blockedme.equals("block")) && (results.blockedbyme == null || !results.blockedbyme.equals("block"))) {
                    try {
                        JSONObject jsonObject = new JSONObject();
                        jsonObject.put(Constants.TAG_USER_ID, GetSet.getUserId());
                        jsonObject.put(Constants.TAG_CONTACT_ID, userId);
                        Log.v("online", "online=" + jsonObject);
                        socketConnection.online(jsonObject);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }
        }, 0, 2000);

        if (pref.getBoolean("readReciept", true))
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
                final List<MessagesData> tmpList = new ArrayList<>();
                try {
                    tmpList.addAll(dbhelper.getMessages(GetSet.getUserId() + userId, String.valueOf(page * 20), "20"));
                } catch (Exception e) {
                    e.printStackTrace();
                }
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

        if (SocketConnection.onUpdateTabIndication != null) {
            SocketConnection.onUpdateTabIndication.updateIndication();
        }

        recyclerView.addOnItemTouchListener(chatItemClick(this, recyclerView));


        btnRecord.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                if (motionEvent.getAction() == MotionEvent.ACTION_DOWN) {
                    if (ActivityCompat.checkSelfPermission(ChatActivity.this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {

                        ActivityCompat.requestPermissions(ChatActivity.this, new String[]{Manifest.permission.RECORD_AUDIO},
                                123);

                    } else {
                        recordingCallback = new Runnable() {
                            @Override
                            public void run() {
                                layoutTypeMessage.setVisibility(View.INVISIBLE);
                                textRecordTimer.setVisibility(View.VISIBLE);
                                startRecording();
                            }
                        };

                        recordingHandler = new Handler();
                        recordingHandler.postDelayed(recordingCallback, 300);

                    }
                    return true;
                } else if (motionEvent.getAction() == MotionEvent.ACTION_UP) {
                    if (ActivityCompat.checkSelfPermission(ChatActivity.this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {

                        ActivityCompat.requestPermissions(ChatActivity.this, new String[]{Manifest.permission.RECORD_AUDIO},
                                123);

                    } else {
                        recordingHandler.removeCallbacks(recordingCallback);
                        stopRecording();
                    }
                    textRecordTimer.setVisibility(View.INVISIBLE);
                    layoutTypeMessage.setVisibility(View.VISIBLE);
                    return true;
                }
                return true;
            }
        });

        imgEmoji.setOnClickListener(v -> emojiPopup.toggle());

        MessageSwipeController messageSwipeController = new MessageSwipeController(this, new SwipeControllerActions() {
            @Override
            public void showReplyUI(int position) {
                showReplyTo(messagesList.get(position));
            }
        });

        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(messageSwipeController);
        itemTouchHelper.attachToRecyclerView(recyclerView);

        btnCancelReply.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                msgReplyTo = null;
                replyContainer.setVisibility(View.GONE);
            }
        });

        startLoginService();
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (pref.getBoolean(Constants.PREF_ENTER_IS_SEND, false)) {
            editText.setSingleLine(true);
            editText.setImeOptions(EditorInfo.IME_ACTION_SEND);
            editText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
                @Override
                public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                    if (actionId == EditorInfo.IME_ACTION_SEND) {
                        //onClick(send);
                        sendMessage();
                        return true;
                    }
                    return false;
                }
            });
        } else {
            editText.setSingleLine(false);
            editText.setImeOptions(EditorInfo.IME_ACTION_NONE);
        }

        switch (pref.getInt(Constants.PREF_FONT_SIZE, Constants.FONT_SIZE_MEDIUM)) {
            case Constants.FONT_SIZE_SMALL:
                fontSize = 15;
                break;
            case Constants.FONT_SIZE_MEDIUM:
                fontSize = 20;
                break;
            case Constants.FONT_SIZE_LARGE:
                fontSize = 25;
                break;
        }
    }

    private void sendMessage() {
        if (isNetworkConnected().equals(NOT_CONNECT)) {
            networkSnack();
        } else if (results.blockedbyme == null || results.blockedbyme.equals("block")) {
            blockChatConfirmDialog("unblock", "sent");
        } else {
            if (editText.getText().toString().trim().length() > 0) {
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
                String chatId = GetSet.getUserId() + userId;
                RandomString randomString = new RandomString(10);
                String messageId = GetSet.getUserId() + randomString.nextString();
                try {
                    if (results.blockedme == null || !results.blockedme.equals("block")) {
//                                JSONObject jobj = new JSONObject();
                        JSONObject message = new JSONObject();
                        message.put(Constants.TAG_USER_ID, GetSet.getUserId());
                        message.put(Constants.TAG_USER_NAME, GetSet.getUserName());
                        message.put(Constants.TAG_MESSAGE_TYPE, "text");
                        message.put(Constants.TAG_MESSAGE, textMsg);
                        message.put(Constants.TAG_REPLY_TO, msgReplyTo);
                        message.put(Constants.TAG_CHAT_TIME, unixStamp);
                        message.put(Constants.TAG_CHAT_ID, chatId);
                        message.put(Constants.TAG_MESSAGE_ID, messageId);
                        message.put(TAG_FRIENDID, userId);
                        message.put(Constants.TAG_SENDER_ID, GetSet.getUserId());
                        message.put(Constants.TAG_CHAT_TYPE, Constants.TAG_SINGLE);
                        message.put(Constants.TAG_REPLY_TO, msgReplyTo);
                        message.put(Constants.TAG_TO_GROUP_ID, groupReply);
//                                jobj.put(Constants.TAG_SENDER_ID, GetSet.getUserId());
//                                jobj.put(Constants.TAG_RECEIVER_ID, userId);
//                                jobj.put("message_data", message);
                        Log.v("startchat", "startchat=" + message);
                        socketConnection.startChat(message);
                    }

                    dbhelper.addMessageDatas(chatId, messageId, GetSet.getUserId(), GetSet.getUserName(),
                            "text", textMsg, "", "", "", "", "",
                            "", unixStamp, userId, GetSet.getUserId(), "", "", msgReplyTo, groupReply);

                    dbhelper.addRecentMessages(chatId, userId, messageId, unixStamp, "0");
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                MessagesData data = new MessagesData();
                data.user_id = GetSet.getUserId();
                data.message_type = "text";
                data.message = textMsg;
                data.message_id = messageId;
                data.chat_time = unixStamp;
                data.reply_to = msgReplyTo;
                data.groupId = groupReply;
                data.delivery_status = "";
                messagesList.add(0, data);
                messageListAdapter.notifyItemInserted(0);
                recyclerView.smoothScrollToPosition(0);
                editText.setText("");
                btnCancelReply.callOnClick();
            } else {
                editText.setError(getString(R.string.please_enter_your_message));
            }
        }
    }

    private void startRecording() {
        String fileName = "/REC" + new Date().getTime() + ".3gpp";
        recorder = new MediaRecorder();
        recorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        recorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
        recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
        // File outputFolder = new File(getExternalFilesDir(null).getAbsolutePath() + "/" + getString(R.string.app_name) + "/" + getString(R.string.app_name) + "/Voice/Sent/");
        File outputFolder = new File(StorageManager.getDataRoot().getAbsolutePath() + "/" + getString(R.string.app_name) + "/" + getString(R.string.app_name) + "/Voice/Sent/");
        Log.i(TAG, "startRecording: creating output file " + outputFolder.mkdirs());
        recording = new File(outputFolder.getAbsolutePath() + fileName);

        recorder.setOutputFile(recording.getAbsolutePath());
        recorder.setMaxDuration((1000 * 60 * 30));

        try {
            recorder.prepare();
        } catch (IOException e) {
            Log.e(TAG, "prepare() failed");
        }

        recorder.start();
        showTimer();
    }

    private void stopRecording() {
        if (recorder == null)
            return;

        recorder.stop();
        recorder.release();
        recorder = null;

        second = -1;
        minute = 0;
        hour = 0;
        textRecordTimer.setText("00:00:00");

        countDownTimer.cancel();

        if (isNetworkConnected().equals(NOT_CONNECT)) {
            networkSnack();
        } else if (recording != null) {
            String filepath = recording.getAbsolutePath();
            /*try {
                filepath = ContentUriUtils.INSTANCE.getFilePath(getApplicationContext(), pathsAry.get(0));
            } catch (URISyntaxException e) {
                e.printStackTrace();
            }*/
            Log.i(TAG, "selectedImageFile: " + filepath);
            try {
                MessagesData mdata = updateDBList("voice", "", filepath);
                Intent service = new Intent(ChatActivity.this, RecordingUploadService.class);
                Bundle b = new Bundle();
                b.putSerializable("mdata", mdata);
                b.putString("filepath", filepath);
                b.putString("chatType", "chat");
                service.putExtras(b);
                startService(service);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }

        recording = null;
    }

    public void showTimer() {
        countDownTimer = new CountDownTimer(Long.MAX_VALUE, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                second++;
                textRecordTimer.setText(recorderTime());
            }

            public void onFinish() {

            }
        };
        countDownTimer.start();
    }

    //recorder time
    public String recorderTime() {
        if (second == 60) {
            minute++;
            second = 0;
        }
        if (minute == 60) {
            hour++;
            minute = 0;
        }
        return String.format("%02d:%02d:%02d", hour, minute, second);
    }

    private void showReplyTo(MessagesData messagesData) {
        msgReplyTo = messagesData.getMessage_id();
        if (msgReplyTo != null && !msgReplyTo.equals("")) {
            replyContainer.setVisibility(View.VISIBLE);
            lytReply.setVisibility(View.VISIBLE);
            ContactsData.Result contactDetail = dbhelper.getContactDetail(messagesData.user_id);
            if (contactDetail != null && contactDetail.phone_no.equals(GetSet.getphonenumber()))
                txtReplyTo.setText(Constants.YOU);
            else
                txtReplyTo.setText(ApplicationClass.getContactName(ChatActivity.this, dbhelper.getContactPhone(messagesData.user_id)));
            if (messagesData.message != null)
                txtMsgReply.setText(messagesData.message);
        }
//        imgReply = findViewById(R.id.img_reply);
    }

    private void showGroupReplyTo() {
        GroupMessage msgGrpReplyTo = dbhelper.getSingleGroupMessage(groupReply, msgReplyTo);
        replyContainer.setVisibility(View.VISIBLE);
        lytReply.setVisibility(View.VISIBLE);
        if (msgGrpReplyTo.contactName != null)
            txtReplyTo.setText(msgGrpReplyTo.contactName + " . " + msgGrpReplyTo.groupName);
        if (msgGrpReplyTo.message != null)
            txtMsgReply.setText(msgGrpReplyTo.message);
//        imgReply = findViewById(R.id.img_reply);
    }


    @Override
    public void onNetworkChange(boolean isConnected) {
        Log.v("onNetwork", "chat=" + isConnected);
        if (online != null) {
            if (isConnected) {
                online.setVisibility(View.VISIBLE);
            } else {
                online.setVisibility(View.GONE);
            }
        }
    }

    private List<MessagesData> getMessagesAry(List<MessagesData> tmpList, MessagesData lastData) {
        List<MessagesData> msgList = new ArrayList<>();
        if (tmpList.size() == 0 && lastData != null) {
            MessagesData mdata = new MessagesData();
            mdata.isPlaying = false;
            mdata.playProgress = 0;
            mdata.message_type = "date";
            mdata.chat_time = lastData.chat_time;
            msgList.add(mdata);
            Log.v("diff", "diff pos=ss" + "&msg=" + lastData.message);
        } else {
            for (int i = 0; i < tmpList.size(); i++) {
                Calendar cal1 = Calendar.getInstance();
                if (!tmpList.get(i).chat_time.equals(""))
                    cal1.setTimeInMillis(Long.parseLong(tmpList.get(i).chat_time) * 1000L);

                if (i + 1 < tmpList.size()) {
                    Calendar cal2 = Calendar.getInstance();
                    if (!tmpList.get(i + 1).chat_time.equals("")) {
                        cal2.setTimeInMillis(Long.parseLong(tmpList.get(i + 1).chat_time) * 1000L);
                    }

                    boolean sameDay = cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
                            cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR);

                    if (sameDay) {
                        msgList.add(tmpList.get(i));
                        Log.v("diff", "same pos=" + i + "&msg=" + tmpList.get(i).message);
                    } else {
                        msgList.add(tmpList.get(i));
                        MessagesData mdata = new MessagesData();
                        mdata.message_type = "date";
                        mdata.chat_time = tmpList.get(i).chat_time;
                        msgList.add(mdata);
                        Log.v("diff", "diff pos=" + i + "&msg=" + tmpList.get(i).message);
                    }
                } else {
                    msgList.add(tmpList.get(i));
                }
            }
        }
        return msgList;
    }

    private List<MessagesData> getPlayingList() {
        ArrayList<MessagesData> playingList = new ArrayList<>();
        for (int i = 0; i < messagesList.size(); i++) {
            if (messagesList.get(i).isPlaying) {
                playingList.add(messagesList.get(i));
            }
        }
        return playingList;
    }

    private int getMessagePosition(MessagesData data) {
        for (int i = 0; i < messagesList.size(); i++) {
            if (messagesList.get(i).message_id == data.message_id) {
                return i;
            }
        }
        return 0;
    }

    @Override
    public void onReceiveChat(final MessagesData mdata) {
        runOnUiThread(new Runnable() {
            public void run() {
                Log.v("onGroupChatReceive", "onGroupChatReceive");
                /*if(mdata.message_type.equalsIgnoreCase("text")){
                    try {
                        CryptLib cryptLib = new CryptLib();
                        mdata.message = cryptLib.decryptCipherTextWithRandomIV(mdata.message,"123");
                    } catch (NoSuchAlgorithmException e) {
                        e.printStackTrace();
                    } catch (NoSuchPaddingException e) {
                        e.printStackTrace();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }*/
                if (mdata.user_id.equals(userId)) {
                    messagesList.add(0, mdata);
                    messageListAdapter.notifyItemInserted(0);
                    recyclerView.smoothScrollToPosition(0);
                    if (pref.getBoolean("readReciept", true))
                        whileViewChat();
                } else if (mdata.receiver_id.equals(userId)) {
                    messagesList.add(0, mdata);
                    messageListAdapter.notifyItemInserted(0);
                    recyclerView.smoothScrollToPosition(0);
                    if (pref.getBoolean("readReciept", true))
                        whileViewChat();
                }
            }
        });
    }

    @Override
    public void onEndChat(final String message_id, final String sender_id, final String receiverId) {
        runOnUiThread(new Runnable() {
            public void run() {
                Log.v("onEndChat", "onEndChat");
                for (int i = 0; i < messagesList.size(); i++) {
                    if (messagesList.get(i).message_id != null &&
                            messagesList.get(i).message_id.equals(message_id)) {
                        messagesList.get(i).delivery_status = "sent";
                        break;
                    }
                }
                messageListAdapter.notifyDataSetChanged();
            }
        });
    }


    private void whileViewChat() {
        Log.e(TAG, "LLL_whileViewChat: " + GetSet.getUserId() + userId);
        dbhelper.updateMessageReadStatus(GetSet.getUserId() + userId, GetSet.getUserId());
        dbhelper.resetUnseenMessagesCount(userId);

        runOnUiThread(new Thread(new Runnable() {
            public void run() {
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            JSONObject jsonObject = new JSONObject();
                            jsonObject.put(TAG_USER_ID, userId);
                            jsonObject.put(TAG_FRIENDID, GetSet.getUserId());
                            jsonObject.put(Constants.TAG_CHAT_ID, userId + GetSet.getUserId());
                            Log.v(TAG, "chatViewed: " + jsonObject);
                            socketConnection.chatViewed(jsonObject);
                            messageListAdapter.notifyDataSetChanged();
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }, 1000);

            }
        }));

        // viewChat();
    }

    private void viewChat() {
        Log.e(TAG, "viewChat: " + GetSet.getUserId() + userId);
        dbhelper.updateMessageReadStatus(GetSet.getUserId() + userId, GetSet.getUserId());
        dbhelper.resetUnseenMessagesCount(userId);

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                try {
                    JSONObject jsonObject = new JSONObject();
                    jsonObject.put(Constants.TAG_CHAT_ID, userId + GetSet.getUserId());
                    jsonObject.put(Constants.TAG_SENDER_ID, GetSet.getUserId());
                    jsonObject.put(Constants.TAG_RECEIVER_ID, userId);
                    Log.v(TAG, "viewChat: " + jsonObject);
                    //socketConnection.chatViewedByUser(jsonObject);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, 1000);
    }

    @Override
    public void onViewChat(final String chat_id, final String sender_id, final String receiverId) {
        runOnUiThread(new Runnable() {
            public void run() {
                Log.v("onViewChat", "onViewChat");
                if (chat_id.equals(GetSet.getUserId() + userId)) {
                    for (int i = 0; i < messagesList.size(); i++) {
                        if (messagesList.get(i).delivery_status != null && messagesList.get(i).delivery_status.equals("sent")) {
                            messagesList.get(i).delivery_status = "read";
                        }
                    }
                    messageListAdapter.notifyDataSetChanged();
                }
            }
        });
    }


    @Override
    public void onlineStatus(final JSONObject data) {
        runOnUiThread(new Runnable() {
            public void run() {
                //Log.v("onlineStatus", "onlineStatus="+data);
                try {
                    String contactId = data.getString(TAG_FRIENDID);
                    if (contactId.equals(userId)) {
                        if ((results.blockedme == null || !results.blockedme.equals("block")) && (results.blockedbyme == null || !results.blockedbyme.equals("block"))) {
                            online.setVisibility(View.VISIBLE);
                            if (data.get("online").equals("true")) {
                                online.setText(getString(R.string.online));
                                online.setSelected(false);
                            } else if (data.get("online").equals("false")) {
                                ContactsData.Result result = dbhelper.getContactDetail(contactId);
                                if (result != null) {
                                    if (result.privacy_last_seen != null && result.privacy_last_seen.equalsIgnoreCase(TAG_MY_CONTACTS)) {
                                        if (result.contactstatus.equalsIgnoreCase(TRUE)) {
                                            online.setText(getString(R.string.last_seen) + " " + getFormattedDateTime(ChatActivity.this, data.getLong("lastSeen")));
                                            online.setSelected(true);
                                        } else {
                                            online.setText("");
                                            online.setSelected(false);
                                        }
                                    } else if (result.privacy_last_seen != null && result.privacy_last_seen.equalsIgnoreCase(TAG_NOBODY)) {
                                        online.setText("");
                                        online.setSelected(false);
                                    } else {
                                        online.setText(getString(R.string.last_seen) + " " + getFormattedDateTime(ChatActivity.this, Long.parseLong(data.getString("lastseen"))));
                                        online.setSelected(true);
                                    }
                                }
                            }
                        } else {
                            online.setVisibility(View.GONE);
                        }
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    @Override
    public void onListenTyping(final JSONObject data) {
        runOnUiThread(new Runnable() {
            public void run() {
                try {
                    String chatId = data.getString(Constants.TAG_CHAT_ID);
                    if (chatId.equals(GetSet.getUserId() + userId)) {
                        if (data.get("type").equals("typing")) {
                            online.setText(getString(R.string.typing));
                        }

                        /*
                        roke
                         */
                        if (data.get("type").equals("untyping")) {
                            online.setText(getString(R.string.online));
                        }
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    @Override
    public void onBlockStatus(final JSONObject data) {
        runOnUiThread(new Runnable() {
            public void run() {
                try {
                    String senderId = data.getString(Constants.TAG_SENDER_ID);
                    String receiverId = data.getString(Constants.TAG_RECEIVER_ID);
                    String type = data.getString(Constants.TAG_TYPE);
                    if (senderId.equals(userId)) {
                        results = dbhelper.getContactDetail(userId);
                        if (results.blockedme == null || !results.blockedme.equals("block")) {
                            DialogActivity.setProfileImage(dbhelper.getContactDetail(userId), userimage, ChatActivity.this);
                            online.setVisibility(View.VISIBLE);
                        } else {
                            Glide.with(ChatActivity.this).load(R.drawable.person)
                                    .apply(RequestOptions.circleCropTransform()
                                            .placeholder(R.drawable.person)
                                            .error(R.drawable.person)
                                            .override(ApplicationClass.dpToPx(ChatActivity.this, 70)))
                                    .into(userimage);
                            online.setVisibility(View.GONE);
                        }
                        if (results.blockedbyme == null || results.blockedbyme.equals("block")) {
                            online.setVisibility(View.GONE);
                        }
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    @Override
    public void onUserImageChange(final String user_id, final String user_image) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (user_id.equals(userId)) {
                    ContactsData.Result results = dbhelper.getContactDetail(userId);
                    if ((results.blockedme == null || !results.blockedme.equals("block")) && (results.blockedbyme == null || !results.blockedbyme.equals("block"))) {
                        DialogActivity.setProfileImage(dbhelper.getContactDetail(results.user_id), userimage, ChatActivity.this);
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
                int currentCount = dbhelper.getMessagesCount(GetSet.getUserId() + userId);
                if (totalMsg != currentCount) {
                    messagesList.clear();
                    if (endlessRecyclerOnScrollListener != null) {
                        endlessRecyclerOnScrollListener.resetState();
                    }
                    try {
                        messagesList.addAll(getMessagesAry(dbhelper.getMessages(GetSet.getUserId() + userId, "0", "20"), null));
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    messageListAdapter.notifyDataSetChanged();
                    recyclerView.post(new Runnable() {
                        @Override
                        public void run() {
                            recyclerView.scrollToPosition(0);
                        }
                    });
                    if (pref.getBoolean("readReciept", true))
                        whileViewChat();
                } else if (isFromNotification) {
                    messagesList.clear();
                    if (endlessRecyclerOnScrollListener != null) {
                        endlessRecyclerOnScrollListener.resetState();
                    }
                    try {
                        messagesList.addAll(getMessagesAry(dbhelper.getMessages(GetSet.getUserId() + userId, "0", "20"), null));
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    messageListAdapter.notifyDataSetChanged();
                    recyclerView.post(new Runnable() {
                        @Override
                        public void run() {
                            recyclerView.scrollToPosition(0);
                        }
                    });

                    if (pref.getBoolean("readReciept", true))
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
                    if (message_id.equals(messagesList.get(i).message_id)) {
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
    public void onPrivacyChanged(final JSONObject jsonObject) {
//        Log.i(TAG, "onPrivacyChanged: " + jsonObject);
        try {
            if (jsonObject.getString(TAG_USER_ID).equalsIgnoreCase(userId)) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            DialogActivity.setProfileImage(dbhelper.getContactDetail(jsonObject.getString(TAG_USER_ID)), userimage, ChatActivity.this);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                });
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

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
        if ((results.blockedme == null || !results.blockedme.equals("block")) && (results.blockedbyme == null || !results.blockedbyme.equals("block"))) {
            if (runnable != null)
                handler.removeCallbacks(runnable);
            if (!meTyping) {
                meTyping = true;
            }
            try {
                JSONObject jsonObject = new JSONObject();
                jsonObject.put(Constants.TAG_SENDER_ID, GetSet.getUserId());
                jsonObject.put(Constants.TAG_RECEIVER_ID, userId);
                jsonObject.put(Constants.TAG_CHAT_ID, userId + GetSet.getUserId());
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
                        jsonObject.put(Constants.TAG_RECEIVER_ID, userId);
                        jsonObject.put(Constants.TAG_CHAT_ID, userId + GetSet.getUserId());
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

    public class MessageListAdapter extends RecyclerView.Adapter {
        public static final int VIEW_TYPE_MESSAGE_SENT = 1;
        public static final int VIEW_TYPE_MESSAGE_RECEIVED = 2;
        public static final int VIEW_TYPE_IMAGE_SENT = 3;
        public static final int VIEW_TYPE_IMAGE_RECEIVED = 4;
        public static final int VIEW_TYPE_CONTACT_SENT = 5;
        public static final int VIEW_TYPE_CONTACT_RECEIVED = 6;
        public static final int VIEW_TYPE_FILE_SENT = 7;
        public static final int VIEW_TYPE_FILE_RECEIVED = 8;
        public static final int VIEW_TYPE_DATE = 9;
        public static final int VIEW_TYPE_VOICE_SENT = 10;
        public static final int VIEW_TYPE_VOICE_RECEIVED = 11;

        private Context mContext;
        private List<MessagesData> mMessageList;
        private List<MessagesData> mSentMessageList;
        private List<MessagesData> mReceiveMessageList;

        private MediaPlayer mediaPlayer;
        private int currentPlayingPosition;
        private SeekBarUpdater seekBarUpdater;
        private RecyclerView.ViewHolder playingHolder;

        public MessageListAdapter(Context context, List<MessagesData> messageList) {
            mContext = context;
            mMessageList = messageList;
            this.currentPlayingPosition = -1;
            seekBarUpdater = new SeekBarUpdater();
        }

        @Override
        public int getItemCount() {
            return mMessageList.size();
        }

        // Determines the appropriate ViewType according to the sender of the message.
        @Override
        public int getItemViewType(int position) {
            MessagesData message = mMessageList.get(position);
            if (message.user_id != null && message.user_id.equals(GetSet.getUserId())) {
//                mSentMessageList.add(message);
//                editor.putLong("sentMesTotal",mSentMessageList.size());
//                editor.apply();
//                editor.commit();
                switch (message.message_type) {
                    case "text":
                        return VIEW_TYPE_MESSAGE_SENT;
                    case "image":
                    case "video":
                    case "location":
                        return VIEW_TYPE_IMAGE_SENT;
                    case "contact":
                        return VIEW_TYPE_CONTACT_SENT;
                    case "date":
                        return VIEW_TYPE_DATE;
                    case "voice":
                        return VIEW_TYPE_VOICE_SENT;
                    default:
                        return VIEW_TYPE_FILE_SENT;
                }
            } else {
                switch (message.message_type) {
                    case "text":
                        return VIEW_TYPE_MESSAGE_RECEIVED;
                    case "image":
                    case "video":
                    case "location":
                        return VIEW_TYPE_IMAGE_RECEIVED;
                    case "contact":
                        return VIEW_TYPE_CONTACT_RECEIVED;
                    case "date":
                        return VIEW_TYPE_DATE;
                    case "voice":
                        return VIEW_TYPE_VOICE_RECEIVED;
                    default:
                        return VIEW_TYPE_FILE_RECEIVED;
                }
            }
        }

        // Inflates the appropriate layout according to the ViewType.
        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view;

            if (viewType == VIEW_TYPE_MESSAGE_SENT) {
                view = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.chat_text_bubble_sent, parent, false);
                return new SentMessageHolder(view);
            } else if (viewType == VIEW_TYPE_MESSAGE_RECEIVED) {
                view = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.chat_text_bubble_receive, parent, false);
                return new ReceivedMessageHolder(view);
            } else if (viewType == VIEW_TYPE_IMAGE_SENT) {
                view = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.chat_image_bubble_sent, parent, false);
                return new SentImageHolder(view);
            } else if (viewType == VIEW_TYPE_IMAGE_RECEIVED) {
                view = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.chat_image_bubble_receive, parent, false);
                return new ReceivedImageHolder(view);
            } else if (viewType == VIEW_TYPE_CONTACT_SENT) {
                view = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.chat_contact_bubble_sent, parent, false);
                return new SentContactHolder(view);
            } else if (viewType == VIEW_TYPE_CONTACT_RECEIVED) {
                view = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.chat_contact_bubble_receive, parent, false);
                return new ReceivedContactHolder(view);
            } else if (viewType == VIEW_TYPE_FILE_SENT) {
                view = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.chat_file_bubble_sent, parent, false);
                return new SentFileHolder(view);
            } else if (viewType == VIEW_TYPE_FILE_RECEIVED) {
                view = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.chat_file_bubble_received, parent, false);
                return new ReceivedFileHolder(view);
            } else if (viewType == VIEW_TYPE_VOICE_SENT) {
                view = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.chat_voice_bubble_sent, parent, false);
                return new SentVoiceHolder(view);
            } else if (viewType == VIEW_TYPE_VOICE_RECEIVED) {
                view = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.chat_voice_bubble_received, parent, false);
                return new ReceivedVoiceHolder(view);
            } else if (viewType == VIEW_TYPE_DATE) {
                view = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.chat_date_layout, parent, false);
                return new DateHolder(view);
            }

            return null;
        }

        // Passes the message object to a ViewHolder so that the contents can be bound to UI.
        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
            MessagesData message = mMessageList.get(position);

            switch (holder.getItemViewType()) {
                case VIEW_TYPE_MESSAGE_SENT:
                    ((SentMessageHolder) holder).bind(message);
                    break;
                case VIEW_TYPE_MESSAGE_RECEIVED:
                    ((ReceivedMessageHolder) holder).bind(message);
                    break;
                case VIEW_TYPE_IMAGE_SENT:
                    ((SentImageHolder) holder).bind(message);
                    break;
                case VIEW_TYPE_IMAGE_RECEIVED:
                    ((ReceivedImageHolder) holder).bind(message);
                    break;
                case VIEW_TYPE_FILE_SENT:
                    ((SentFileHolder) holder).bind(message);
                    break;
                case VIEW_TYPE_FILE_RECEIVED:
                    ((ReceivedFileHolder) holder).bind(message);
                    break;
                case VIEW_TYPE_VOICE_SENT:
                    ((SentVoiceHolder) holder).bind(message);
                    if (position == currentPlayingPosition) {
                        playingHolder = holder;
                        //updatePlayingView();
                    } else {
                        // updateNonPlayingView(holder);
                    }
                    break;
                case VIEW_TYPE_VOICE_RECEIVED:
                    ((ReceivedVoiceHolder) holder).bind(message);
                    if (position == currentPlayingPosition) {
                        playingHolder = holder;
                        //updatePlayingView();
                    } else {
                        // updateNonPlayingView(holder);
                    }
                    break;
                case VIEW_TYPE_CONTACT_SENT:
                    ((SentContactHolder) holder).bind(message);
                    break;
                case VIEW_TYPE_CONTACT_RECEIVED:
                    ((ReceivedContactHolder) holder).bind(message);
                    break;
                case VIEW_TYPE_DATE:
                    ((DateHolder) holder).bind(message);
                    break;
            }
        }

        private void showGroupReply(String groupId, String reply_to, ConstraintLayout replyLayout, TextView txtReplyUserName, TextView txtReplyMsg, ImageView imgReply) {
            if (groupId != null && !groupId.equals("") && reply_to != null && !reply_to.equals("")) {

                GroupMessage repliedToMsg = dbhelper.getSingleGroupMessage(groupId, reply_to);
                if (repliedToMsg == null || repliedToMsg.memberId == null) {
                    replyLayout.setVisibility(View.GONE);
                    return;
                }
                replyLayout.setVisibility(View.VISIBLE);
                ContactsData.Result contactDetail = dbhelper.getContactDetail(repliedToMsg.memberId);
                GroupData groupDetail = dbhelper.getGroupData(ChatActivity.this, repliedToMsg.groupId);
                if (contactDetail.phone_no.equals(GetSet.getphonenumber()))
                    txtReplyTo.setText(Constants.YOU);
                if (repliedToMsg.contactName != null)
                    txtReplyUserName.setText(username.getText().toString() + " . " + groupDetail.groupName);
                if (repliedToMsg.message != null)
                    txtReplyMsg.setText(repliedToMsg.message);
            } else replyLayout.setVisibility(View.GONE);
        }

        private void showReply(MessagesData message, ConstraintLayout replyLayout, TextView txtReplyUserName, TextView txtReplyMsg, ImageView imgReply) {
            if (message.reply_to != null && !message.reply_to.equals("")) {

                MessagesData repliedToMsg = dbhelper.getSingleMessage(message.reply_to);
                if (repliedToMsg == null || repliedToMsg.user_id == null) {
                    replyLayout.setVisibility(View.GONE);
                    return;
                }
                replyLayout.setVisibility(View.VISIBLE);
                ContactsData.Result contactDetail = dbhelper.getContactDetail(repliedToMsg.user_id);
                if (contactDetail.phone_no.equals(GetSet.getphonenumber()))
                    txtReplyTo.setText(Constants.YOU);
                else
                    txtReplyUserName.setText(username.getText().toString());
                if (repliedToMsg.message != null)
                    txtReplyMsg.setText(repliedToMsg.message);
//                imgReply.set;
            } else replyLayout.setVisibility(View.GONE);
        }

        private class SentMessageHolder extends RecyclerView.ViewHolder {
            TextView messageText, timeText;
            ImageView tickimage;
            ConstraintLayout replyLayout;
            TextView txtReplyUserName, txtReplyMsg;
            ImageView imgReply;

            SentMessageHolder(View itemView) {
                super(itemView);

                messageText = itemView.findViewById(R.id.text_message_body);
                timeText = itemView.findViewById(R.id.text_message_time);
                tickimage = itemView.findViewById(R.id.tickimage);
                replyLayout = itemView.findViewById(R.id.lyt_reply);
                txtReplyUserName = itemView.findViewById(R.id.txt_user_name);
                txtReplyMsg = itemView.findViewById(R.id.txt_message);
                imgReply = itemView.findViewById(R.id.img_reply);
            }

            void bind(MessagesData message) {

                messageText.setTextSize(TypedValue.COMPLEX_UNIT_SP, fontSize);
                messageText.setText(message.message + Html.fromHtml(" &#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;"));
                Linkify.addLinks(messageText, Linkify.EMAIL_ADDRESSES | Linkify.PHONE_NUMBERS | Linkify.WEB_URLS);
                timeText.setText(ApplicationClass.getTime(Long.parseLong(message.chat_time)));
                if (message.groupId != null && !message.groupId.equals(""))
                    showGroupReply(message.groupId, message.reply_to, replyLayout, txtReplyUserName, txtReplyMsg, imgReply);
                else
                    showReply(message, replyLayout, txtReplyUserName, txtReplyMsg, imgReply);
//                Log.e("LLLLL_Msg_Type: ", message.getDelivery_status());
                switch (message.delivery_status) {
                    case "read":
                        tickimage.setVisibility(View.VISIBLE);
                        tickimage.setImageResource(R.drawable.double_tick);
                        break;
                    case "sent":
                        tickimage.setVisibility(View.VISIBLE);
                        tickimage.setImageResource(R.drawable.double_tick_unseen);
                        break;
                    default:
                        tickimage.setVisibility(View.VISIBLE);
                        tickimage.setImageResource(R.drawable.single_tick);
                        break;
                }

                if (selectedChatPos.contains(message)) {
                    itemView.setSelected(true);
                } else {
                    itemView.setSelected(false);
                }
            }
        }

        private class ReceivedMessageHolder extends RecyclerView.ViewHolder {
            TextView messageText, timeText;
            ConstraintLayout replyLayout;
            TextView txtReplyUserName, txtReplyMsg;
            ImageView imgReply;

            ReceivedMessageHolder(View itemView) {
                super(itemView);

                messageText = (TextView) itemView.findViewById(R.id.text_message_body);
                timeText = (TextView) itemView.findViewById(R.id.text_message_time);
                replyLayout = itemView.findViewById(R.id.lyt_reply);
                txtReplyUserName = itemView.findViewById(R.id.txt_user_name);
                txtReplyMsg = itemView.findViewById(R.id.txt_message);
                imgReply = itemView.findViewById(R.id.img_reply);
            }

            void bind(MessagesData message) {
                messageText.setTextSize(TypedValue.COMPLEX_UNIT_SP, fontSize);
                messageText.setText(message.message + Html.fromHtml(
                        " &#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;"));
                Linkify.addLinks(messageText, Linkify.EMAIL_ADDRESSES | Linkify.PHONE_NUMBERS | Linkify.WEB_URLS);
                timeText.setText(ApplicationClass.getTime(Long.parseLong(message.chat_time.replace(".0", ""))));
                if (message.groupId != null && !message.groupId.equals(""))
                    showGroupReply(message.groupId, message.reply_to, replyLayout, txtReplyUserName, txtReplyMsg, imgReply);
                else
                    showReply(message, replyLayout, txtReplyUserName, txtReplyMsg, imgReply);
                if (selectedChatPos.contains(message)) {
                    itemView.setSelected(true);
                } else {
                    itemView.setSelected(false);
                }
            }
        }

        private class SentImageHolder extends RecyclerView.ViewHolder {
            TextView timeText;
            ImageView tickimage, uploadimage, downloadicon;
            RelativeLayout progresslay;
            ProgressWheel progressbar;
            ConstraintLayout replyLayout;
            TextView txtReplyUserName, txtReplyMsg;
            ImageView imgReply;

            SentImageHolder(View itemView) {
                super(itemView);

                uploadimage = itemView.findViewById(R.id.uploadimage);
                timeText = itemView.findViewById(R.id.text_message_time);
                tickimage = itemView.findViewById(R.id.tickimage);
                progresslay = itemView.findViewById(R.id.progresslay);
                progressbar = itemView.findViewById(R.id.progressbar);
                downloadicon = itemView.findViewById(R.id.downloadicon);
                replyLayout = itemView.findViewById(R.id.lyt_reply);
                txtReplyUserName = itemView.findViewById(R.id.txt_user_name);
                txtReplyMsg = itemView.findViewById(R.id.txt_message);
                imgReply = itemView.findViewById(R.id.img_reply);
            }

            void bind(final MessagesData message) {
                timeText.setText(ApplicationClass.getTime(Long.parseLong(message.chat_time)));
                if (selectedChatPos.contains(message)) {
                    itemView.setSelected(true);
                } else {
                    itemView.setSelected(false);
                }
                if (message.groupId != null && !message.groupId.equals(""))
                    showGroupReply(message.groupId, message.reply_to, replyLayout, txtReplyUserName, txtReplyMsg, imgReply);
                else
                    showReply(message, replyLayout, txtReplyUserName, txtReplyMsg, imgReply);
                if (message.message_type.equals("image")) {
                    if (message.delivery_status.equals("read")) {
                        tickimage.setVisibility(View.VISIBLE);
                        tickimage.setImageResource(R.drawable.double_tick);
                        tickimage.setColorFilter(ContextCompat.getColor(mContext, R.color.colorAccent), android.graphics.PorterDuff.Mode.MULTIPLY);
                    } else if (message.delivery_status.equals("sent")) {
                        tickimage.setVisibility(View.VISIBLE);
                        tickimage.setImageResource(R.drawable.double_tick_unseen);
                        tickimage.setColorFilter(ContextCompat.getColor(mContext, R.color.white), android.graphics.PorterDuff.Mode.MULTIPLY);
                    } else if (message.progress.equals("completed")) {
                        tickimage.setVisibility(View.VISIBLE);
                        tickimage.setImageResource(R.drawable.single_tick);
                        tickimage.setColorFilter(ContextCompat.getColor(mContext, R.color.white), android.graphics.PorterDuff.Mode.MULTIPLY);
                    } else {
                        tickimage.setVisibility(View.GONE);
                    }

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
                            File file = storageManager.getImage("sent", message.attachment);
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
                                        dbhelper.updateMessageData(message.message_id, Constants.TAG_PROGRESS, "");
                                        message.progress = "";
                                        byte[] bytes = org.apache.commons.io.FileUtils.readFileToByteArray(new File(message.attachment));
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
                                        imageView.setVisibility(View.VISIBLE);
                                        videoView.setVisibility(View.GONE);
                                        Glide.with(mContext).load(Uri.fromFile(file)).thumbnail(0.5f)
                                                .transition(new DrawableTransitionOptions().crossFade())
                                                .into(imageView);
                                    }
                                } else {
                                    Toast.makeText(ChatActivity.this, getString(R.string.no_media), Toast.LENGTH_SHORT).show();
                                }
                            }
                        }
                    });
                } else if (message.message_type.equals("location")) {
                    switch (message.delivery_status) {
                        case "read":
                            tickimage.setVisibility(View.VISIBLE);
                            tickimage.setImageResource(R.drawable.double_tick);
                            tickimage.setColorFilter(ContextCompat.getColor(mContext, R.color.colorAccent), android.graphics.PorterDuff.Mode.MULTIPLY);
                            break;
                        case "sent":
                            tickimage.setVisibility(View.VISIBLE);
                            tickimage.setImageResource(R.drawable.double_tick_unseen);
                            tickimage.setColorFilter(ContextCompat.getColor(mContext, R.color.white), android.graphics.PorterDuff.Mode.MULTIPLY);
                            break;
                        default:
                            tickimage.setVisibility(View.VISIBLE);
                            tickimage.setImageResource(R.drawable.single_tick);
                            tickimage.setColorFilter(ContextCompat.getColor(mContext, R.color.white), android.graphics.PorterDuff.Mode.MULTIPLY);
                            break;
                    }
                    progresslay.setVisibility(View.GONE);
                    int size = ApplicationClass.dpToPx(mContext, 170);
                    String url = "http://maps.google.com/maps/api/staticmap?center=" + message.lat + "," + message.lon + "&zoom=18&size=" + size + "x" + size + "&sensor=false" + "&key=" + Constants.GOOGLE_MAPS_KEY;
                    Glide.with(mContext).load(url).thumbnail(0.5f)
                            .into(uploadimage);

                    uploadimage.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            Intent i = new Intent(ChatActivity.this, LocationActivity.class);
                            i.putExtra("from", "view");
                            i.putExtra("lat", message.lat);
                            i.putExtra("lon", message.lon);
                            startActivity(i);
                        }
                    });
                } else if (message.message_type.equals("video")) {
                    if (message.delivery_status.equals("read")) {
                        tickimage.setVisibility(View.VISIBLE);
                        tickimage.setImageResource(R.drawable.double_tick);
                        tickimage.setColorFilter(ContextCompat.getColor(mContext, R.color.colorAccent), android.graphics.PorterDuff.Mode.MULTIPLY);
                    } else if (message.delivery_status.equals("sent")) {
                        tickimage.setVisibility(View.VISIBLE);
                        tickimage.setImageResource(R.drawable.double_tick_unseen);
                        tickimage.setColorFilter(ContextCompat.getColor(mContext, R.color.white), android.graphics.PorterDuff.Mode.MULTIPLY);
                    } else if (message.progress.equals("completed")) {
                        tickimage.setVisibility(View.VISIBLE);
                        tickimage.setImageResource(R.drawable.single_tick);
                        tickimage.setColorFilter(ContextCompat.getColor(mContext, R.color.white), android.graphics.PorterDuff.Mode.MULTIPLY);
                    } else {
                        tickimage.setVisibility(View.GONE);
                    }

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
                                            dbhelper.updateMessageData(message.message_id, Constants.TAG_PROGRESS, "");
                                            message.progress = "";
                                            String timestamp = String.valueOf(System.currentTimeMillis() / 1000L);
                                            String imageStatus = storageManager.saveToSdCard(getApplicationContext(), thumb, "sent", timestamp + ".jpg");
                                            if (imageStatus.equals("success")) {
                                                File file = storageManager.getImage("sent", timestamp + ".jpg");
                                                String imagePath = file.getAbsolutePath();
                                                byte[] bytes = org.apache.commons.io.FileUtils.readFileToByteArray(new File(imagePath));
                                                uploadImage(bytes, imagePath, message, message.attachment);
                                            }
                                        }
                                    } catch (IOException ex) {
                                        ex.printStackTrace();
                                        Toast.makeText(ChatActivity.this, "1:" + getString(R.string.something_wrong), Toast.LENGTH_SHORT).show();
                                    }
                                }
                            } else if (message.progress.equals("completed")) {
                                if (storageManager.checkifFileExists(message.attachment, message.message_type, "sent")) {
                                    bottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
                                    imageView.setVisibility(View.GONE);
                                    videoView.setVisibility(View.VISIBLE);
                                    File file = storageManager.getFile(message.attachment, message.message_type, "sent");
                                    Uri photoURI = FileProvider.getUriForFile(mContext,
                                            BuildConfig.APPLICATION_ID + ".provider", file);
                                    videoView.setMediaController(mediacontroller);
                                    videoView.requestFocus();
                                    videoView.setVideoURI(photoURI);
                                    videoView.start();
                                    videoView.setOnPreparedListener(mp -> {
                                        mp.setVolume(0f, 0f);
                                        mp.setLooping(true);
                                        if (!videoView.isPlaying()) {
                                            videoView.start();
                                        } else
                                            videoView.pause();
                                    });

                                    videoView.setOnErrorListener(new MediaPlayer.OnErrorListener() {
                                        @Override
                                        public boolean onError(MediaPlayer mp, int what, int extra) {
                                            videoView.setVisibility(View.GONE);
                                            imageView.setVisibility(View.VISIBLE);
                                            Log.d("video", "setOnErrorListener ");
                                            return true;
                                        }
                                    });
                                } else {
                                    Toast.makeText(ChatActivity.this, getString(R.string.no_media), Toast.LENGTH_SHORT).show();
                                }
                            }
                        }
                    });
                }
            }
        }

        private class ReceivedImageHolder extends RecyclerView.ViewHolder {
            TextView timeText;
            ImageView uploadimage, downloadicon;
            RelativeLayout progresslay, videoprogresslay;
            ProgressWheel progressbar, videoprogressbar;
            ConstraintLayout replyLayout;
            TextView txtReplyUserName, txtReplyMsg;
            ImageView imgReply;

            ReceivedImageHolder(View itemView) {
                super(itemView);

                uploadimage = itemView.findViewById(R.id.uploadimage);
                progresslay = itemView.findViewById(R.id.progresslay);
                timeText = itemView.findViewById(R.id.text_message_time);
                progressbar = itemView.findViewById(R.id.progressbar);
                downloadicon = itemView.findViewById(R.id.downloadicon);
                videoprogresslay = itemView.findViewById(R.id.videoprogresslay);
                videoprogressbar = itemView.findViewById(R.id.videoprogressbar);
                replyLayout = itemView.findViewById(R.id.lyt_reply);
                txtReplyUserName = itemView.findViewById(R.id.txt_user_name);
                txtReplyMsg = itemView.findViewById(R.id.txt_message);
                imgReply = itemView.findViewById(R.id.img_reply);
            }

            void bind(final MessagesData message) {
                if (selectedChatPos.contains(message)) {
                    itemView.setSelected(true);
                } else {
                    itemView.setSelected(false);
                }
                if (message.groupId != null && !message.groupId.equals(""))
                    showGroupReply(message.groupId, message.reply_to, replyLayout, txtReplyUserName, txtReplyMsg, imgReply);
                else
                    showReply(message, replyLayout, txtReplyUserName, txtReplyMsg, imgReply);
                switch (message.message_type) {
                    case "image":
                        videoprogresslay.setVisibility(View.GONE);
                        downloadicon.setImageResource(R.drawable.download);
                        if (storageManager.checkifImageExists("thumb", message.attachment)) {
                            progresslay.setVisibility(View.GONE);
                            File file = storageManager.getImage("thumb", message.attachment);
                            ExifInterface exif = null;
                            int orientation = 0;
                            if (file != null) {
                                Glide.with(mContext).load(file).thumbnail(0.5f)
                                        .into(uploadimage);
                            }
                        } else {

                            Log.e("LLLLLL_Mobile: ", getConnectionType(ChatActivity.this));
                            Log.e("LLLLLL_Roaming: ", String.valueOf(isDataRoamingEnabled(ChatActivity.this)));

                            if (isDataRoamingEnabled(ChatActivity.this)) {
                                if (RoamingData.isPhotos()) {
                                    if (ContextCompat.checkSelfPermission(ChatActivity.this, WRITE_EXTERNAL_STORAGE)
                                            != PackageManager.PERMISSION_GRANTED) {
                                        ActivityCompat.requestPermissions(ChatActivity.this, new String[]{WRITE_EXTERNAL_STORAGE}, 100);
                                    } else {
                                        if (isNetworkConnected().equals(NOT_CONNECT)) {
                                            networkSnack();
                                        } else {
                                            ImageDownloader imageDownloader = new ImageDownloader(ChatActivity.this) {
                                                @Override
                                                protected void onPostExecute(Bitmap imgBitmap) {
                                                    if (imgBitmap == null) {
                                                        Log.v("bitmapFailed", "bitmapFailed");
                                                        Toast.makeText(mContext, "2:" + getString(R.string.something_wrong), Toast.LENGTH_SHORT).show();
                                                    } else {
                                                        Log.v("onBitmapLoaded", "onBitmapLoaded");
                                                        try {
//                                                            String[] fileName = message.attachment.split("/");
                                                            String status = storageManager.saveThumbNail(imgBitmap, message.attachment);
                                                            if (status.equals("success")) {
                                                                File thumbFile = storageManager.getImage("thumb", message.attachment);
                                                                Glide.with(mContext).load(thumbFile).thumbnail(0.5f)
                                                                        .into(uploadimage);
                                                                dbhelper.updateMessageData(message.message_id, Constants.TAG_PROGRESS, "completed");
                                                                long size = thumbFile.length();

                                                                if (prefData.getLong("mediaDownload", 0) == 0) {
                                                                    editorData.putLong("mediaDownload", size);
                                                                } else {
                                                                    editorData.putLong("mediaDownload", prefData.getLong("mediaDownload", 0) + size);
                                                                }
                                                                editorData.apply();
                                                                editorData.commit();
                                                                Log.e("LLLLL_MediaDown: ", humanReadableByteCountSI(prefData.getLong("mediaDownload", 0)));

                                                                DataStorageModel dataStorageModel = dbhelper.getRecord(userId);
                                                                String bitsi = dataStorageModel.getSent_photos();
                                                                Log.e("LLLLLL_!!: ", String.valueOf(bitsi + 1));
                                                                dbhelper.addDataStorage(userId,
                                                                        dataStorageModel.getMessage_count(),
                                                                        dataStorageModel.getSent_contact(),
                                                                        dataStorageModel.getSent_location(),
                                                                        String.valueOf(Long.parseLong(dataStorageModel.getSent_photos()) + 1),
                                                                        dataStorageModel.getSent_videos(),
                                                                        dataStorageModel.getSent_aud(),
                                                                        dataStorageModel.getSent_doc(),
                                                                        String.valueOf(Long.parseLong(dataStorageModel.getSent_photos_size()) + size),
                                                                        dataStorageModel.getSent_videos_size(),
                                                                        dataStorageModel.getSent_aud_size(),
                                                                        dataStorageModel.getSent_doc_size());

                                                                progresslay.setVisibility(View.GONE);
                                                                progressbar.stopSpinning();
                                                                videoprogresslay.setVisibility(View.GONE);
//                                                                }
                                                            } else {
                                                                Toast.makeText(mContext, "3:" + getString(R.string.something_wrong), Toast.LENGTH_SHORT).show();
                                                            }
                                                        } catch (NullPointerException e) {
                                                            e.printStackTrace();
                                                        }
                                                    }
                                                }

                                                @Override
                                                protected void onProgressUpdate(String... progress) {
                                                    //progressbar.setProgress(Integer.parseInt(progress[0]));
                                                }
                                            };
                                            imageDownloader.execute(Constants.CHAT_IMG_PATH + message.attachment, "receive");
                                            progressbar.setVisibility(View.VISIBLE);
                                            progressbar.spin();
                                        }
                                    }
                                } else {
                                    progresslay.setVisibility(View.VISIBLE);
                                    progressbar.setVisibility(View.VISIBLE);
                                    progressbar.stopSpinning();
                                    Glide.with(mContext).load(Constants.CHAT_IMG_PATH + message.attachment).thumbnail(0.5f)
                                            .apply(RequestOptions.overrideOf(18, 18))
                                            .into(uploadimage);
                                }
                            } else {
                                if (getConnectionType(ChatActivity.this).equals("wifiData")) {
                                    if (WifiData.isPhotos()) {
                                        if (ContextCompat.checkSelfPermission(ChatActivity.this, WRITE_EXTERNAL_STORAGE)
                                                != PackageManager.PERMISSION_GRANTED) {
                                            ActivityCompat.requestPermissions(ChatActivity.this, new String[]{WRITE_EXTERNAL_STORAGE}, 100);
                                        } else {
                                            if (isNetworkConnected().equals(NOT_CONNECT)) {
                                                networkSnack();
                                            } else {
                                                ImageDownloader imageDownloader = new ImageDownloader(ChatActivity.this) {
                                                    @Override
                                                    protected void onPostExecute(Bitmap imgBitmap) {
                                                        if (imgBitmap == null) {
                                                            Log.v("bitmapFailed", "bitmapFailed");
                                                            Toast.makeText(mContext, "2:" + getString(R.string.something_wrong), Toast.LENGTH_SHORT).show();
                                                        } else {
                                                            Log.v("onBitmapLoaded", "onBitmapLoaded");
                                                            try {
//                                                            String[] fileName = message.attachment.split("/");
                                                                String status = storageManager.saveThumbNail(imgBitmap, message.attachment);
                                                                if (status.equals("success")) {
                                                                    File thumbFile = storageManager.getImage("thumb", message.attachment);
                                                                    Glide.with(mContext).load(thumbFile).thumbnail(0.5f)
                                                                            .into(uploadimage);

                                                                    long size = thumbFile.length();

                                                                    if (prefData.getLong("mediaDownload", 0) == 0) {
                                                                        editorData.putLong("mediaDownload", size);
                                                                    } else {
                                                                        editorData.putLong("mediaDownload", prefData.getLong("mediaDownload", 0) + size);
                                                                    }
                                                                    editorData.apply();
                                                                    editorData.commit();
                                                                    Log.e("LLLLL_MediaDown: ", humanReadableByteCountSI(prefData.getLong("mediaDownload", 0)));

                                                                    DataStorageModel dataStorageModel = dbhelper.getRecord(userId);
                                                                    String bitsi = dataStorageModel.getSent_photos();
                                                                    Log.e("LLLLLL_!!: ", String.valueOf(bitsi + 1));
                                                                    dbhelper.addDataStorage(userId,
                                                                            dataStorageModel.getMessage_count(),
                                                                            dataStorageModel.getSent_contact(),
                                                                            dataStorageModel.getSent_location(),
                                                                            String.valueOf(Long.parseLong(dataStorageModel.getSent_photos()) + 1),
                                                                            dataStorageModel.getSent_videos(),
                                                                            dataStorageModel.getSent_aud(),
                                                                            dataStorageModel.getSent_doc(),
                                                                            String.valueOf(Long.parseLong(dataStorageModel.getSent_photos_size()) + size),
                                                                            dataStorageModel.getSent_videos_size(),
                                                                            dataStorageModel.getSent_aud_size(),
                                                                            dataStorageModel.getSent_doc_size());

                                                                    progresslay.setVisibility(View.GONE);
                                                                    progressbar.stopSpinning();
                                                                    videoprogresslay.setVisibility(View.GONE);
//                                                                }
                                                                } else {
                                                                    Toast.makeText(mContext, "3:" + getString(R.string.something_wrong), Toast.LENGTH_SHORT).show();
                                                                }
                                                            } catch (NullPointerException e) {
                                                                e.printStackTrace();
                                                            }
                                                        }
                                                    }

                                                    @Override
                                                    protected void onProgressUpdate(String... progress) {
                                                        //progressbar.setProgress(Integer.parseInt(progress[0]));
                                                    }
                                                };
                                                imageDownloader.execute(Constants.CHAT_IMG_PATH + message.attachment, "receive");
                                                progressbar.setVisibility(View.VISIBLE);
                                                progressbar.spin();
                                            }
                                        }
                                    } else {
                                        progresslay.setVisibility(View.VISIBLE);
                                        progressbar.setVisibility(View.VISIBLE);
                                        progressbar.stopSpinning();
                                        Glide.with(mContext).load(Constants.CHAT_IMG_PATH + message.attachment).thumbnail(0.5f)
                                                .apply(RequestOptions.overrideOf(18, 18))
                                                .into(uploadimage);
                                    }
                                } else if (getConnectionType(ChatActivity.this).equals("mobileData")) {
                                    if (Mobiledata1.isPhotos()) {
                                        if (ContextCompat.checkSelfPermission(ChatActivity.this, WRITE_EXTERNAL_STORAGE)
                                                != PackageManager.PERMISSION_GRANTED) {
                                            ActivityCompat.requestPermissions(ChatActivity.this, new String[]{WRITE_EXTERNAL_STORAGE}, 100);
                                        } else {
                                            if (isNetworkConnected().equals(NOT_CONNECT)) {
                                                networkSnack();
                                            } else {
                                                ImageDownloader imageDownloader = new ImageDownloader(ChatActivity.this) {
                                                    @Override
                                                    protected void onPostExecute(Bitmap imgBitmap) {
                                                        if (imgBitmap == null) {
                                                            Log.v("bitmapFailed", "bitmapFailed");
                                                            Toast.makeText(mContext, "2:" + getString(R.string.something_wrong), Toast.LENGTH_SHORT).show();
                                                        } else {
                                                            Log.v("onBitmapLoaded", "onBitmapLoaded");
                                                            try {
//                                                            String[] fileName = message.attachment.split("/");
                                                                String status = storageManager.saveThumbNail(imgBitmap, message.attachment);
                                                                if (status.equals("success")) {
                                                                    File thumbFile = storageManager.getImage("thumb", message.attachment);
                                                                    Glide.with(mContext).load(thumbFile).thumbnail(0.5f)
                                                                            .into(uploadimage);

                                                                    long size = thumbFile.length();

                                                                    if (prefData.getLong("mediaDownload", 0) == 0) {
                                                                        editorData.putLong("mediaDownload", size);
                                                                    } else {
                                                                        editorData.putLong("mediaDownload", prefData.getLong("mediaDownload", 0) + size);
                                                                    }
                                                                    editorData.apply();
                                                                    editorData.commit();
                                                                    Log.e("LLLLL_MediaDown: ", humanReadableByteCountSI(prefData.getLong("mediaDownload", 0)));

                                                                    DataStorageModel dataStorageModel = dbhelper.getRecord(userId);
                                                                    String bitsi = dataStorageModel.getSent_photos();
                                                                    Log.e("LLLLLL_!!: ", String.valueOf(bitsi + 1));
                                                                    dbhelper.addDataStorage(userId,
                                                                            dataStorageModel.getMessage_count(),
                                                                            dataStorageModel.getSent_contact(),
                                                                            dataStorageModel.getSent_location(),
                                                                            String.valueOf(Long.parseLong(dataStorageModel.getSent_photos()) + 1),
                                                                            dataStorageModel.getSent_videos(),
                                                                            dataStorageModel.getSent_aud(),
                                                                            dataStorageModel.getSent_doc(),
                                                                            String.valueOf(Long.parseLong(dataStorageModel.getSent_photos_size()) + size),
                                                                            dataStorageModel.getSent_videos_size(),
                                                                            dataStorageModel.getSent_aud_size(),
                                                                            dataStorageModel.getSent_doc_size());

                                                                    progresslay.setVisibility(View.GONE);
                                                                    progressbar.stopSpinning();
                                                                    videoprogresslay.setVisibility(View.GONE);
//                                                                }
                                                                } else {
                                                                    Toast.makeText(mContext, "3:" + getString(R.string.something_wrong), Toast.LENGTH_SHORT).show();
                                                                }
                                                            } catch (NullPointerException e) {
                                                                e.printStackTrace();
                                                            }
                                                        }
                                                    }

                                                    @Override
                                                    protected void onProgressUpdate(String... progress) {
                                                        //progressbar.setProgress(Integer.parseInt(progress[0]));
                                                    }
                                                };
                                                imageDownloader.execute(Constants.CHAT_IMG_PATH + message.attachment, "receive");
                                                progressbar.setVisibility(View.VISIBLE);
                                                progressbar.spin();
                                            }
                                        }
                                    } else {
                                        progresslay.setVisibility(View.VISIBLE);
                                        progressbar.setVisibility(View.VISIBLE);
                                        progressbar.stopSpinning();
                                        Glide.with(mContext).load(Constants.CHAT_IMG_PATH + message.attachment).thumbnail(0.5f)
                                                .apply(RequestOptions.overrideOf(18, 18))
                                                .into(uploadimage);
                                    }
                                } else {
                                    progresslay.setVisibility(View.VISIBLE);
                                    progressbar.setVisibility(View.VISIBLE);
                                    progressbar.stopSpinning();
                                    Glide.with(mContext).load(Constants.CHAT_IMG_PATH + message.attachment).thumbnail(0.5f)
                                            .apply(RequestOptions.overrideOf(18, 18))
                                            .into(uploadimage);
                                }
                            }
                        }

                        timeText.setText(ApplicationClass.getTime(Long.parseLong(message.chat_time)));
                        //timeText.setText(message.message_type);
                        uploadimage.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                if (storageManager.checkifImageExists("thumb", message.attachment)) {
                                    File file = storageManager.getImage("thumb", message.attachment);
                                    if (file != null) {
                                        Log.v(TAG, "file=" + file.getAbsolutePath());
                                        bottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
                                        videoprogresslay.setVisibility(View.GONE);
                                        imageView.setVisibility(View.VISIBLE);
                                        videoView.setVisibility(View.GONE);
                                        Glide.with(mContext).load(file).thumbnail(0.5f)
                                                .transition(new DrawableTransitionOptions().crossFade())
                                                .into(imageView);
                                    }
                                } else {
                                    if (ContextCompat.checkSelfPermission(ChatActivity.this, WRITE_EXTERNAL_STORAGE)
                                            != PackageManager.PERMISSION_GRANTED) {
                                        ActivityCompat.requestPermissions(ChatActivity.this, new String[]{WRITE_EXTERNAL_STORAGE}, 100);
                                    } else {
                                        if (isNetworkConnected().equals(NOT_CONNECT)) {
                                            networkSnack();
                                        } else {
                                            ImageDownloader imageDownloader = new ImageDownloader(ChatActivity.this) {
                                                @Override
                                                protected void onPostExecute(Bitmap imgBitmap) {
                                                    if (imgBitmap == null) {
                                                        Log.v("bitmapFailed", "bitmapFailed");
                                                        Toast.makeText(mContext, "2:" + getString(R.string.something_wrong), Toast.LENGTH_SHORT).show();
                                                    } else {
                                                        Log.v("onBitmapLoaded", "onBitmapLoaded");
                                                        try {
//                                                            String[] fileName = message.attachment.split("/");
                                                            String status = storageManager.saveThumbNail(imgBitmap, message.attachment);
                                                            if (status.equals("success")) {
                                                                File thumbFile = storageManager.getImage("thumb", message.attachment);
                                                                Glide.with(mContext).load(thumbFile).thumbnail(0.5f)
                                                                        .into(uploadimage);

                                                                long size = thumbFile.length();

                                                                if (prefData.getLong("mediaDownload", 0) == 0) {
                                                                    editorData.putLong("mediaDownload", size);
                                                                } else {
                                                                    editorData.putLong("mediaDownload", prefData.getLong("mediaDownload", 0) + size);
                                                                }
                                                                editorData.apply();
                                                                editorData.commit();
                                                                Log.e("LLLLL_MediaDown: ", humanReadableByteCountSI(prefData.getLong("mediaDownload", 0)));

                                                                DataStorageModel dataStorageModel = dbhelper.getRecord(userId);
                                                                String bitsi = dataStorageModel.getSent_photos();
                                                                Log.e("LLLLLL_!!: ", String.valueOf(bitsi + 1));
                                                                dbhelper.addDataStorage(userId,
                                                                        dataStorageModel.getMessage_count(),
                                                                        dataStorageModel.getSent_contact(),
                                                                        dataStorageModel.getSent_location(),
                                                                        String.valueOf(Long.parseLong(dataStorageModel.getSent_photos()) + 1),
                                                                        dataStorageModel.getSent_videos(),
                                                                        dataStorageModel.getSent_aud(),
                                                                        dataStorageModel.getSent_doc(),
                                                                        String.valueOf(Long.parseLong(dataStorageModel.getSent_photos_size()) + size),
                                                                        dataStorageModel.getSent_videos_size(),
                                                                        dataStorageModel.getSent_aud_size(),
                                                                        dataStorageModel.getSent_doc_size());

                                                                progresslay.setVisibility(View.GONE);
                                                                progressbar.stopSpinning();
                                                                videoprogresslay.setVisibility(View.GONE);
//                                                                }
                                                            } else {
                                                                Toast.makeText(mContext, "3:" + getString(R.string.something_wrong), Toast.LENGTH_SHORT).show();
                                                            }
                                                        } catch (NullPointerException e) {
                                                            e.printStackTrace();
                                                        }
                                                    }
                                                }

                                                @Override
                                                protected void onProgressUpdate(String... progress) {
                                                    //progressbar.setProgress(Integer.parseInt(progress[0]));
                                                }
                                            };
                                            imageDownloader.execute(Constants.CHAT_IMG_PATH + message.attachment, "receive");
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
                        videoprogresslay.setVisibility(View.GONE);
                        int size = ApplicationClass.dpToPx(mContext, 170);

                        String url = "http://maps.google.com/maps/api/staticmap?center=" + message.lat + "," + message.lon + "&zoom=18&size=" + size + "x" + size + "&sensor=false" + "&key=" + Constants.GOOGLE_MAPS_KEY;

                        DataStorageModel dataStorageModel = dbhelper.getRecord(userId);

                        dbhelper.addDataStorage(userId,
                                dataStorageModel.getMessage_count(),
                                dataStorageModel.getSent_contact(),
                                String.valueOf(Long.parseLong(dataStorageModel.getSent_location()) + 1),
                                dataStorageModel.getSent_photos(),
                                dataStorageModel.getSent_videos(),
                                dataStorageModel.getSent_aud(),
                                dataStorageModel.getSent_doc(),
                                dataStorageModel.getSent_photos_size(),
                                dataStorageModel.getSent_videos_size(),
                                dataStorageModel.getSent_aud_size(),
                                dataStorageModel.getSent_doc_size());

                        Log.e("LLLLL_Url: ", url);
                        Glide.with(mContext)
                                .load(url)
                                .into(uploadimage);
                        timeText.setText(ApplicationClass.getTime(Long.parseLong(message.chat_time)));
                        uploadimage.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                Intent i = new Intent(ChatActivity.this, LocationActivity.class);
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
                        timeText.setText(ApplicationClass.getTime(Long.parseLong(message.chat_time)));
                        if (storageManager.checkifFileExists(message.attachment, message.message_type, "receive") &&
                                storageManager.checkifImageExists("thumb", message.thumbnail)) {
                            Log.v("dddd", "video-if");
                            videoprogresslay.setVisibility(View.GONE);
                            File file = storageManager.getImage("thumb", message.thumbnail);
                            if (file != null) {
                                Log.v(TAG, "file=" + file.getAbsolutePath());
                                Glide.with(mContext).load(Uri.fromFile(file)).thumbnail(0.5f)
                                        .into(uploadimage);
                            }
                        } else {

                            Log.e("LLLLLL_Mobile: ", getConnectionType(ChatActivity.this));
                            Log.e("LLLLLL_Roaming: ", String.valueOf(isDataRoamingEnabled(ChatActivity.this)));

                            if (isDataRoamingEnabled(ChatActivity.this)) {
                                if (RoamingData.isVideo()) {
                                    if (isNetworkConnected().equals(NOT_CONNECT)) {
                                        networkSnack();
                                    } else {
                                        ImageDownloader imageDownloader = new ImageDownloader(ChatActivity.this) {
                                            @Override
                                            protected void onPostExecute(Bitmap imgBitmap) {
                                                if (imgBitmap == null) {
                                                    Log.v("bitmapFailed", "bitmapFailed");
                                                    Toast.makeText(mContext, "4:" + getString(R.string.something_wrong), Toast.LENGTH_SHORT).show();
                                                    videoprogresslay.setVisibility(View.GONE);
                                                    videoprogressbar.setVisibility(View.GONE);
                                                    videoprogressbar.stopSpinning();
                                                } else {
                                                    Log.v("onBitmapLoaded", "onBitmapLoaded");
                                                    try {
                                                        String status = storageManager.saveThumbNail(imgBitmap, message.thumbnail);
                                                        if (status.equals("success")) {
                                                            final File thumbFile = storageManager.getImage("thumb", message.thumbnail);
                                                            if (thumbFile != null) {
                                                                Log.v("file", "file=" + thumbFile.getAbsolutePath());

                                                                DownloadFiles downloadFiles = new DownloadFiles(ChatActivity.this) {
                                                                    @Override
                                                                    protected void onPostExecute(String downPath) {
                                                                        videoprogresslay.setVisibility(View.GONE);
                                                                        videoprogressbar.setVisibility(View.GONE);

                                                                        long size = downPath.length();

                                                                        if (prefData.getLong("mediaDownload", 0) == 0) {
                                                                            editorData.putLong("mediaDownload", size);
                                                                        } else {
                                                                            editorData.putLong("mediaDownload", prefData.getLong("mediaDownload", 0) + size);
                                                                        }
                                                                        editorData.apply();
                                                                        editorData.commit();

                                                                        DataStorageModel dataStorageModel = dbhelper.getRecord(userId);

                                                                        dbhelper.addDataStorage(userId,
                                                                                dataStorageModel.getMessage_count(),
                                                                                dataStorageModel.getSent_contact(),
                                                                                dataStorageModel.getSent_location(),
                                                                                dataStorageModel.getSent_photos(),
                                                                                String.valueOf(Long.parseLong(dataStorageModel.getSent_videos()) + 1),
                                                                                dataStorageModel.getSent_aud(),
                                                                                dataStorageModel.getSent_doc(),
                                                                                dataStorageModel.getSent_photos_size(),
                                                                                String.valueOf(Long.parseLong(dataStorageModel.getSent_videos_size()) + size),
                                                                                dataStorageModel.getSent_aud_size(),
                                                                                dataStorageModel.getSent_doc_size());

                                                                        videoprogressbar.stopSpinning();
                                                                        if (downPath == null) {
                                                                            Log.v("Download Failed", "Download Failed");
                                                                            Toast.makeText(mContext, "5:" + getString(R.string.something_wrong), Toast.LENGTH_SHORT).show();
                                                                        } else {
                                                                            Glide.with(mContext).load(Uri.fromFile(thumbFile)).thumbnail(0.5f)
                                                                                    .into(uploadimage);
                                                                            //  Toast.makeText(mContext, getString(R.string.downloaded), Toast.LENGTH_SHORT).show();
                                                                        }
                                                                    }
                                                                };
                                                                downloadFiles.execute(Constants.CHAT_IMG_PATH + message.attachment, message.message_type);
                                                            }
                                                        } else {
                                                            Toast.makeText(mContext, "6:" + getString(R.string.something_wrong), Toast.LENGTH_SHORT).show();
                                                            videoprogresslay.setVisibility(View.GONE);
                                                            videoprogressbar.setVisibility(View.GONE);
                                                            videoprogressbar.stopSpinning();
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
                                        imageDownloader.execute(Constants.CHAT_IMG_PATH + message.thumbnail, "thumb");
                                        videoprogresslay.setVisibility(View.VISIBLE);
                                        videoprogressbar.setVisibility(View.VISIBLE);
                                        videoprogressbar.spin();
                                    }
                                }
                            } else {
                                if (getConnectionType(ChatActivity.this).equals("wifiData")) {
                                    if (WifiData.isVideo()) {
                                        if (isNetworkConnected().equals(NOT_CONNECT)) {
                                            networkSnack();
                                        } else {
                                            ImageDownloader imageDownloader = new ImageDownloader(ChatActivity.this) {
                                                @Override
                                                protected void onPostExecute(Bitmap imgBitmap) {
                                                    if (imgBitmap == null) {
                                                        Log.v("bitmapFailed", "bitmapFailed");
                                                        Toast.makeText(mContext, "4:" + getString(R.string.something_wrong), Toast.LENGTH_SHORT).show();
                                                        videoprogresslay.setVisibility(View.GONE);
                                                        videoprogressbar.setVisibility(View.GONE);
                                                        videoprogressbar.stopSpinning();
                                                    } else {
                                                        Log.v("onBitmapLoaded", "onBitmapLoaded");
                                                        try {
                                                            String status = storageManager.saveThumbNail(imgBitmap, message.thumbnail);
                                                            if (status.equals("success")) {
                                                                final File thumbFile = storageManager.getImage("thumb", message.thumbnail);
                                                                if (thumbFile != null) {
                                                                    Log.v("file", "file=" + thumbFile.getAbsolutePath());

                                                                    DownloadFiles downloadFiles = new DownloadFiles(ChatActivity.this) {
                                                                        @Override
                                                                        protected void onPostExecute(String downPath) {
                                                                            videoprogresslay.setVisibility(View.GONE);
                                                                            videoprogressbar.setVisibility(View.GONE);

                                                                            long size = downPath.length();

                                                                            if (prefData.getLong("mediaDownload", 0) == 0) {
                                                                                editorData.putLong("mediaDownload", size);
                                                                            } else {
                                                                                editorData.putLong("mediaDownload", prefData.getLong("mediaDownload", 0) + size);
                                                                            }
                                                                            editorData.apply();
                                                                            editorData.commit();

                                                                            DataStorageModel dataStorageModel = dbhelper.getRecord(userId);

                                                                            dbhelper.addDataStorage(userId,
                                                                                    dataStorageModel.getMessage_count(),
                                                                                    dataStorageModel.getSent_contact(),
                                                                                    dataStorageModel.getSent_location(),
                                                                                    dataStorageModel.getSent_photos(),
                                                                                    String.valueOf(Long.parseLong(dataStorageModel.getSent_videos()) + 1),
                                                                                    dataStorageModel.getSent_aud(),
                                                                                    dataStorageModel.getSent_doc(),
                                                                                    dataStorageModel.getSent_photos_size(),
                                                                                    String.valueOf(Long.parseLong(dataStorageModel.getSent_videos_size()) + size),
                                                                                    dataStorageModel.getSent_aud_size(),
                                                                                    dataStorageModel.getSent_doc_size());

                                                                            videoprogressbar.stopSpinning();
                                                                            if (downPath == null) {
                                                                                Log.v("Download Failed", "Download Failed");
                                                                                Toast.makeText(mContext, "5:" + getString(R.string.something_wrong), Toast.LENGTH_SHORT).show();
                                                                            } else {
                                                                                Glide.with(mContext).load(Uri.fromFile(thumbFile)).thumbnail(0.5f)
                                                                                        .into(uploadimage);
                                                                                //  Toast.makeText(mContext, getString(R.string.downloaded), Toast.LENGTH_SHORT).show();
                                                                            }
                                                                        }
                                                                    };
                                                                    downloadFiles.execute(Constants.CHAT_IMG_PATH + message.attachment, message.message_type);
                                                                }
                                                            } else {
                                                                Toast.makeText(mContext, "6:" + getString(R.string.something_wrong), Toast.LENGTH_SHORT).show();
                                                                videoprogresslay.setVisibility(View.GONE);
                                                                videoprogressbar.setVisibility(View.GONE);
                                                                videoprogressbar.stopSpinning();
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
                                            imageDownloader.execute(Constants.CHAT_IMG_PATH + message.thumbnail, "thumb");
                                            videoprogresslay.setVisibility(View.VISIBLE);
                                            videoprogressbar.setVisibility(View.VISIBLE);
                                            videoprogressbar.spin();
                                        }
                                    } else {
                                        Log.v("dddd", "video-else=" + message.thumbnail);
                                        Glide.with(mContext).load(Constants.CHAT_IMG_PATH + message.thumbnail).thumbnail(0.5f)
                                                .apply(RequestOptions.overrideOf(18, 18))
                                                .into(uploadimage);
                                        videoprogresslay.setVisibility(View.VISIBLE);
                                        videoprogressbar.setVisibility(View.VISIBLE);
                                        videoprogressbar.stopSpinning();
                                    }
                                } else if (getConnectionType(ChatActivity.this).equals("mobileData")) {
                                    if (Mobiledata1.isVideo()) {
                                        if (isNetworkConnected().equals(NOT_CONNECT)) {
                                            networkSnack();
                                        } else {
                                            ImageDownloader imageDownloader = new ImageDownloader(ChatActivity.this) {
                                                @Override
                                                protected void onPostExecute(Bitmap imgBitmap) {
                                                    if (imgBitmap == null) {
                                                        Log.v("bitmapFailed", "bitmapFailed");
                                                        Toast.makeText(mContext, "4:" + getString(R.string.something_wrong), Toast.LENGTH_SHORT).show();
                                                        videoprogresslay.setVisibility(View.GONE);
                                                        videoprogressbar.setVisibility(View.GONE);
                                                        videoprogressbar.stopSpinning();
                                                    } else {
                                                        Log.v("onBitmapLoaded", "onBitmapLoaded");
                                                        try {
                                                            String status = storageManager.saveThumbNail(imgBitmap, message.thumbnail);
                                                            if (status.equals("success")) {
                                                                final File thumbFile = storageManager.getImage("thumb", message.thumbnail);
                                                                if (thumbFile != null) {
                                                                    Log.v("file", "file=" + thumbFile.getAbsolutePath());

                                                                    DownloadFiles downloadFiles = new DownloadFiles(ChatActivity.this) {
                                                                        @Override
                                                                        protected void onPostExecute(String downPath) {
                                                                            videoprogresslay.setVisibility(View.GONE);
                                                                            videoprogressbar.setVisibility(View.GONE);

                                                                            long size = downPath.length();

                                                                            if (prefData.getLong("mediaDownload", 0) == 0) {
                                                                                editorData.putLong("mediaDownload", size);
                                                                            } else {
                                                                                editorData.putLong("mediaDownload", prefData.getLong("mediaDownload", 0) + size);
                                                                            }
                                                                            editorData.apply();
                                                                            editorData.commit();

                                                                            DataStorageModel dataStorageModel = dbhelper.getRecord(userId);

                                                                            dbhelper.addDataStorage(userId,
                                                                                    dataStorageModel.getMessage_count(),
                                                                                    dataStorageModel.getSent_contact(),
                                                                                    dataStorageModel.getSent_location(),
                                                                                    dataStorageModel.getSent_photos(),
                                                                                    String.valueOf(Long.parseLong(dataStorageModel.getSent_videos()) + 1),
                                                                                    dataStorageModel.getSent_aud(),
                                                                                    dataStorageModel.getSent_doc(),
                                                                                    dataStorageModel.getSent_photos_size(),
                                                                                    String.valueOf(Long.parseLong(dataStorageModel.getSent_videos_size()) + size),
                                                                                    dataStorageModel.getSent_aud_size(),
                                                                                    dataStorageModel.getSent_doc_size());

                                                                            videoprogressbar.stopSpinning();
                                                                            if (downPath == null) {
                                                                                Log.v("Download Failed", "Download Failed");
                                                                                Toast.makeText(mContext, "5:" + getString(R.string.something_wrong), Toast.LENGTH_SHORT).show();
                                                                            } else {
                                                                                Glide.with(mContext).load(Uri.fromFile(thumbFile)).thumbnail(0.5f)
                                                                                        .into(uploadimage);
                                                                                //  Toast.makeText(mContext, getString(R.string.downloaded), Toast.LENGTH_SHORT).show();
                                                                            }
                                                                        }
                                                                    };
                                                                    downloadFiles.execute(Constants.CHAT_IMG_PATH + message.attachment, message.message_type);
                                                                }
                                                            } else {
                                                                Toast.makeText(mContext, "6:" + getString(R.string.something_wrong), Toast.LENGTH_SHORT).show();
                                                                videoprogresslay.setVisibility(View.GONE);
                                                                videoprogressbar.setVisibility(View.GONE);
                                                                videoprogressbar.stopSpinning();
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
                                            imageDownloader.execute(Constants.CHAT_IMG_PATH + message.thumbnail, "thumb");
                                            videoprogresslay.setVisibility(View.VISIBLE);
                                            videoprogressbar.setVisibility(View.VISIBLE);
                                            videoprogressbar.spin();
                                        }
                                    } else {
                                        Log.v("dddd", "video-else=" + message.thumbnail);
                                        Glide.with(mContext).load(Constants.CHAT_IMG_PATH + message.thumbnail).thumbnail(0.5f)
                                                .apply(RequestOptions.overrideOf(18, 18))
                                                .into(uploadimage);
                                        videoprogresslay.setVisibility(View.VISIBLE);
                                        videoprogressbar.setVisibility(View.VISIBLE);
                                        videoprogressbar.stopSpinning();
                                    }
                                } else {
                                    Log.v("dddd", "video-else=" + message.thumbnail);
                                    Glide.with(mContext).load(Constants.CHAT_IMG_PATH + message.thumbnail).thumbnail(0.5f)
                                            .apply(RequestOptions.overrideOf(18, 18))
                                            .into(uploadimage);
                                    videoprogresslay.setVisibility(View.VISIBLE);
                                    videoprogressbar.setVisibility(View.VISIBLE);
                                    videoprogressbar.stopSpinning();
                                }
                            }

                        }

                        uploadimage.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                if (storageManager.checkifFileExists(message.attachment, message.message_type, "receive") &&
                                        storageManager.checkifImageExists("thumb", message.thumbnail)) {
                                    File file = storageManager.getFile(message.attachment, message.message_type, "receive");
                                    Uri photoURI = FileProvider.getUriForFile(mContext,
                                            BuildConfig.APPLICATION_ID + ".provider", file);
                                    bottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
                                    imageView.setVisibility(View.GONE);
                                    videoView.setVisibility(View.VISIBLE);
                                    videoView.setMediaController(mediacontroller);
                                    videoView.requestFocus();
                                    videoView.setVideoURI(photoURI);
                                    videoView.start();
                                    videoView.setOnPreparedListener(mp -> {
                                        mp.setVolume(0f, 0f);
                                        mp.setLooping(true);
                                        if (!videoView.isPlaying()) {
                                            videoView.start();
                                        } else
                                            videoView.pause();
                                    });

                                    videoView.setOnErrorListener(new MediaPlayer.OnErrorListener() {
                                        @Override
                                        public boolean onError(MediaPlayer mp, int what, int extra) {
                                            videoView.setVisibility(View.GONE);
                                            imageView.setVisibility(View.VISIBLE);
                                            Log.d("video", "setOnErrorListener ");
                                            return true;
                                        }
                                    });

                                } else {
                                    if (isNetworkConnected().equals(NOT_CONNECT)) {
                                        networkSnack();
                                    } else {
                                        ImageDownloader imageDownloader = new ImageDownloader(ChatActivity.this) {
                                            @Override
                                            protected void onPostExecute(Bitmap imgBitmap) {
                                                if (imgBitmap == null) {
                                                    Log.v("bitmapFailed", "bitmapFailed");
                                                    Toast.makeText(mContext, "4:" + getString(R.string.something_wrong), Toast.LENGTH_SHORT).show();
                                                    videoprogresslay.setVisibility(View.GONE);
                                                    videoprogressbar.setVisibility(View.GONE);
                                                    videoprogressbar.stopSpinning();
                                                } else {
                                                    Log.v("onBitmapLoaded", "onBitmapLoaded");
                                                    try {
                                                        String status = storageManager.saveThumbNail(imgBitmap, message.thumbnail);
                                                        if (status.equals("success")) {
                                                            final File thumbFile = storageManager.getImage("thumb", message.thumbnail);
                                                            if (thumbFile != null) {
                                                                Log.v("file", "file=" + thumbFile.getAbsolutePath());

                                                                DownloadFiles downloadFiles = new DownloadFiles(ChatActivity.this) {
                                                                    @Override
                                                                    protected void onPostExecute(String downPath) {
                                                                        videoprogresslay.setVisibility(View.GONE);
                                                                        videoprogressbar.setVisibility(View.GONE);

                                                                        long size = downPath.length();

                                                                        if (prefData.getLong("mediaDownload", 0) == 0) {
                                                                            editorData.putLong("mediaDownload", size);
                                                                        } else {
                                                                            editorData.putLong("mediaDownload", prefData.getLong("mediaDownload", 0) + size);
                                                                        }
                                                                        editorData.apply();
                                                                        editorData.commit();

                                                                        DataStorageModel dataStorageModel = dbhelper.getRecord(userId);

                                                                        dbhelper.addDataStorage(userId,
                                                                                dataStorageModel.getMessage_count(),
                                                                                dataStorageModel.getSent_contact(),
                                                                                dataStorageModel.getSent_location(),
                                                                                dataStorageModel.getSent_photos(),
                                                                                String.valueOf(Long.parseLong(dataStorageModel.getSent_videos()) + 1),
                                                                                dataStorageModel.getSent_aud(),
                                                                                dataStorageModel.getSent_doc(),
                                                                                dataStorageModel.getSent_photos_size(),
                                                                                String.valueOf(Long.parseLong(dataStorageModel.getSent_videos_size()) + size),
                                                                                dataStorageModel.getSent_aud_size(),
                                                                                dataStorageModel.getSent_doc_size());

                                                                        videoprogressbar.stopSpinning();
                                                                        if (downPath == null) {
                                                                            Log.v("Download Failed", "Download Failed");
                                                                            Toast.makeText(mContext, "5:" + getString(R.string.something_wrong), Toast.LENGTH_SHORT).show();
                                                                        } else {
                                                                            Glide.with(mContext).load(Uri.fromFile(thumbFile)).thumbnail(0.5f)
                                                                                    .into(uploadimage);
                                                                            //  Toast.makeText(mContext, getString(R.string.downloaded), Toast.LENGTH_SHORT).show();
                                                                        }
                                                                    }
                                                                };
                                                                downloadFiles.execute(Constants.CHAT_IMG_PATH + message.attachment, message.message_type);
                                                            }
                                                        } else {
                                                            Toast.makeText(mContext, "6:" + getString(R.string.something_wrong), Toast.LENGTH_SHORT).show();
                                                            videoprogresslay.setVisibility(View.GONE);
                                                            videoprogressbar.setVisibility(View.GONE);
                                                            videoprogressbar.stopSpinning();
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
                                        imageDownloader.execute(Constants.CHAT_IMG_PATH + message.thumbnail, "thumb");
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
            ImageView tickimage, icon, uploadicon;
            RelativeLayout file_body_lay;
            ProgressWheel progressbar;
            ConstraintLayout replyLayout;
            TextView txtReplyUserName, txtReplyMsg;
            ImageView imgReply;

            SentFileHolder(View itemView) {
                super(itemView);

                filename = itemView.findViewById(R.id.filename);
                timeText = itemView.findViewById(R.id.text_message_time);
                tickimage = itemView.findViewById(R.id.tickimage);
                icon = itemView.findViewById(R.id.icon);
                file_body_lay = itemView.findViewById(R.id.file_body_lay);
                progressbar = itemView.findViewById(R.id.progressbar);
                uploadicon = itemView.findViewById(R.id.uploadicon);
                file_type_tv = itemView.findViewById(R.id.file_type_tv);
                replyLayout = itemView.findViewById(R.id.lyt_reply);
                txtReplyUserName = itemView.findViewById(R.id.txt_user_name);
                txtReplyMsg = itemView.findViewById(R.id.txt_message);
                imgReply = itemView.findViewById(R.id.img_reply);
            }

            void bind(final MessagesData message) {
                filename.setTextSize(TypedValue.COMPLEX_UNIT_SP, fontSize);
                if (selectedChatPos.contains(message)) {
                    itemView.setSelected(true);
                } else {
                    itemView.setSelected(false);
                }
                if (message.groupId != null && !message.groupId.equals(""))
                    showGroupReply(message.groupId, message.reply_to, replyLayout, txtReplyUserName, txtReplyMsg, imgReply);
                else
                    showReply(message, replyLayout, txtReplyUserName, txtReplyMsg, imgReply);
                timeText.setText(ApplicationClass.getTime(Long.parseLong(message.chat_time)));
                if (message.delivery_status.equals("read")) {
                    tickimage.setVisibility(View.VISIBLE);
                    tickimage.setImageResource(R.drawable.double_tick);
                } else if (message.delivery_status.equals("sent")) {
                    tickimage.setVisibility(View.VISIBLE);
                    tickimage.setImageResource(R.drawable.double_tick_unseen);
                } else if (message.progress.equals("completed")) {
                    tickimage.setVisibility(View.VISIBLE);
                    tickimage.setImageResource(R.drawable.single_tick);
                } else {
                    tickimage.setVisibility(View.GONE);
                }

                if (message.message_type.equals("document")) {
                    icon.setImageResource(R.drawable.icon_file_unknown);
                    file_type_tv.setVisibility(View.VISIBLE);
                    file_type_tv.setText(firstThree(FilenameUtils.getExtension(message.attachment)));
                } else if (message.message_type.equals("audio")) {
                    icon.setImageResource(R.drawable.mp3);
                    file_type_tv.setVisibility(View.GONE);
                }

                switch (message.progress) {
                    case "":
                        progressbar.setVisibility(View.VISIBLE);
                        progressbar.spin();
                        uploadicon.setVisibility(View.VISIBLE);
                        filename.setText(R.string.uploading);
                        break;
                    case "completed":
                        progressbar.setVisibility(View.GONE);
                        progressbar.stopSpinning();
                        uploadicon.setVisibility(View.GONE);
                        if (message.message_type.equals("voice"))
                            filename.setText("VoiceNote");
                        else
                            filename.setText(message.message);
                        break;
                    case "error":
                        progressbar.setVisibility(View.VISIBLE);
                        progressbar.stopSpinning();
                        uploadicon.setVisibility(View.VISIBLE);
                        filename.setText(R.string.retry);
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
                                    filename.setText(getString(R.string.uploading));
                                    dbhelper.updateMessageData(message.message_id, Constants.TAG_PROGRESS, "");
                                    message.progress = "";
                                    Intent service = new Intent(ChatActivity.this, FileUploadService.class);
                                    Bundle b = new Bundle();
                                    b.putSerializable("mdata", message);
                                    b.putString("filepath", message.attachment);
                                    b.putString("chatType", "chat");
                                    service.putExtras(b);
                                    startService(service);
                                } catch (Exception ex) {
                                    ex.printStackTrace();
                                }
                            }
                        } else if (message.progress.equals("completed")) {
                            if (storageManager.checkifFileExists(message.attachment, message.message_type, "sent")) {
                                try {
                                    Intent intent = new Intent();
                                    intent.setAction(android.content.Intent.ACTION_VIEW);
                                    intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                                    File file = storageManager.getFile(message.attachment, message.message_type, "sent");
                                    Uri photoURI = FileProvider.getUriForFile(mContext,
                                            BuildConfig.APPLICATION_ID + ".provider", file);

                                    MimeTypeMap mime = MimeTypeMap.getSingleton();
                                    String ext = file.getName().substring(file.getName().indexOf(".") + 1);
                                    String type = mime.getMimeTypeFromExtension(ext);


                                    intent.setDataAndType(photoURI, type);

                                    startActivity(intent);

                                } catch (ActivityNotFoundException e) {
                                    Toast.makeText(ChatActivity.this, getString(R.string.no_application), Toast.LENGTH_SHORT).show();
                                    e.printStackTrace();
                                }
                            } else {
                                Toast.makeText(ChatActivity.this, getString(R.string.no_media), Toast.LENGTH_SHORT).show();
                            }
                        }
                    }
                });
            }
        }

        private class ReceivedFileHolder extends RecyclerView.ViewHolder {
            TextView filename, timeText, file_type_tv;
            ImageView icon, downloadicon;
            RelativeLayout file_body_lay;
            ProgressWheel progressbar;
            ConstraintLayout replyLayout;
            TextView txtReplyUserName, txtReplyMsg;
            ImageView imgReply;

            ReceivedFileHolder(View itemView) {
                super(itemView);

                filename = itemView.findViewById(R.id.filename);
                timeText = itemView.findViewById(R.id.text_message_time);
                icon = itemView.findViewById(R.id.icon);
                file_body_lay = itemView.findViewById(R.id.file_body_lay);
                downloadicon = itemView.findViewById(R.id.downloadicon);
                progressbar = itemView.findViewById(R.id.progressbar);
                file_type_tv = itemView.findViewById(R.id.file_type_tv);
                replyLayout = itemView.findViewById(R.id.lyt_reply);
                txtReplyUserName = itemView.findViewById(R.id.txt_user_name);
                txtReplyMsg = itemView.findViewById(R.id.txt_message);
                imgReply = itemView.findViewById(R.id.img_reply);
            }

            void bind(final MessagesData message) {
                filename.setTextSize(TypedValue.COMPLEX_UNIT_SP, fontSize);
                filename.setText(message.message);
                timeText.setText(ApplicationClass.getTime(Long.parseLong(message.chat_time)));
                //timeText.setText(message.message_type);
                if (selectedChatPos.contains(message)) {
                    itemView.setSelected(true);
                } else {
                    itemView.setSelected(false);
                }
                if (message.groupId != null && !message.groupId.equals(""))
                    showGroupReply(message.groupId, message.reply_to, replyLayout, txtReplyUserName, txtReplyMsg, imgReply);
                else
                    showReply(message, replyLayout, txtReplyUserName, txtReplyMsg, imgReply);
                if (message.groupId != null && !message.groupId.equals(""))
                    showGroupReply(message.groupId, message.reply_to, replyLayout, txtReplyUserName, txtReplyMsg, imgReply);
                else
                    showReply(message, replyLayout, txtReplyUserName, txtReplyMsg, imgReply);
                if (message.message_type.equals("document")) {
                    icon.setImageResource(R.drawable.icon_file_unknown);
                    file_type_tv.setVisibility(View.VISIBLE);
                    file_type_tv.setText(firstThree(FilenameUtils.getExtension(message.attachment)));
                } else if (message.message_type.equals("audio")) {
                    file_type_tv.setVisibility(View.GONE);
                    icon.setImageResource(R.drawable.mp3);
                }

                if (storageManager.checkifFileExists(message.attachment, message.message_type, "receive")) {
                    downloadicon.setVisibility(View.GONE);
                    progressbar.setVisibility(View.GONE);
                } else {

                    if (isDataRoamingEnabled(ChatActivity.this)) {
                        if (RoamingData.isDoc()) {
                            if (storageManager.checkifFileExists(message.attachment, message.message_type, "receive")) {
                                try {
                                    Intent intent = new Intent();
                                    intent.setAction(android.content.Intent.ACTION_VIEW);
                                    intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                                    File file = storageManager.getFile(message.attachment, message.message_type, "receive");
                                    Uri photoURI = FileProvider.getUriForFile(mContext,
                                            BuildConfig.APPLICATION_ID + ".provider", file);

                                    MimeTypeMap mime = MimeTypeMap.getSingleton();
                                    String ext = file.getName().substring(file.getName().indexOf(".") + 1);
                                    String type = mime.getMimeTypeFromExtension(ext);

                                    intent.setDataAndType(photoURI, type);

                                    startActivity(intent);
                                } catch (ActivityNotFoundException e) {
                                    Toast.makeText(ChatActivity.this, getString(R.string.no_application), Toast.LENGTH_SHORT).show();
                                    e.printStackTrace();
                                }
                            } else {
                                if (isNetworkConnected().equals(NOT_CONNECT)) {
                                    networkSnack();
                                } else {
                                    DownloadFiles downloadFiles = new DownloadFiles(ChatActivity.this) {
                                        @Override
                                        protected void onPostExecute(String downPath) {
                                            progressbar.setVisibility(View.GONE);
                                            progressbar.stopSpinning();
                                            downloadicon.setVisibility(View.GONE);
                                            if (downPath == null) {
                                                Log.v("Download Failed", "Download Failed");
                                                Toast.makeText(mContext, "7:" + getString(R.string.something_wrong), Toast.LENGTH_SHORT).show();

                                            } else {

                                                File file = new File(downPath);
                                                long size = file.length();

                                                Toast.makeText(ChatActivity.this, humanReadableByteCountSI(size), Toast.LENGTH_LONG).show();
                                                Log.e("LLLLL_DownSize: ", humanReadableByteCountSI(size));

                                                if (prefData.getLong("mediaDownload", 0) == 0) {
                                                    editorData.putLong("mediaDownload", size);
                                                } else {
                                                    editorData.putLong("mediaDownload", prefData.getLong("mediaDownload", 0) + size);
                                                }
                                                editorData.apply();
                                                editorData.commit();

                                                DataStorageModel dataStorageModel = dbhelper.getRecord(userId);

                                                dbhelper.addDataStorage(userId,
                                                        dataStorageModel.getMessage_count(),
                                                        dataStorageModel.getSent_contact(),
                                                        dataStorageModel.getSent_location(),
                                                        dataStorageModel.getSent_photos(),
                                                        dataStorageModel.getSent_videos(),
                                                        dataStorageModel.getSent_aud(),
                                                        String.valueOf(Long.parseLong(dataStorageModel.getSent_doc()) + 1),
                                                        dataStorageModel.getSent_photos_size(),
                                                        dataStorageModel.getSent_videos_size(),
                                                        dataStorageModel.getSent_aud_size(),
                                                        String.valueOf(Long.parseLong(dataStorageModel.getSent_doc_size()) + size));

                                                //Toast.makeText(mContext, getString(R.string.downloaded), Toast.LENGTH_SHORT).show();
                                            }
                                        }
                                    };
                                    downloadFiles.execute(Constants.CHAT_IMG_PATH + message.attachment, message.message_type);
                                    progressbar.setVisibility(View.VISIBLE);
                                    progressbar.spin();
                                    downloadicon.setVisibility(View.VISIBLE);
                                }
                            }
                        }
                    } else {
                        if (getConnectionType(ChatActivity.this).equals("wifiData")) {
                            if (WifiData.isDoc()) {
                                if (storageManager.checkifFileExists(message.attachment, message.message_type, "receive")) {
                                    try {
                                        Intent intent = new Intent();
                                        intent.setAction(android.content.Intent.ACTION_VIEW);
                                        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                                        File file = storageManager.getFile(message.attachment, message.message_type, "receive");
                                        Uri photoURI = FileProvider.getUriForFile(mContext,
                                                BuildConfig.APPLICATION_ID + ".provider", file);

                                        MimeTypeMap mime = MimeTypeMap.getSingleton();
                                        String ext = file.getName().substring(file.getName().indexOf(".") + 1);
                                        String type = mime.getMimeTypeFromExtension(ext);

                                        intent.setDataAndType(photoURI, type);

                                        startActivity(intent);
                                    } catch (ActivityNotFoundException e) {
                                        Toast.makeText(ChatActivity.this, getString(R.string.no_application), Toast.LENGTH_SHORT).show();
                                        e.printStackTrace();
                                    }
                                } else {
                                    if (isNetworkConnected().equals(NOT_CONNECT)) {
                                        networkSnack();
                                    } else {
                                        DownloadFiles downloadFiles = new DownloadFiles(ChatActivity.this) {
                                            @Override
                                            protected void onPostExecute(String downPath) {
                                                progressbar.setVisibility(View.GONE);
                                                progressbar.stopSpinning();
                                                downloadicon.setVisibility(View.GONE);
                                                if (downPath == null) {
                                                    Log.v("Download Failed", "Download Failed");
                                                    Toast.makeText(mContext, "7:" + getString(R.string.something_wrong), Toast.LENGTH_SHORT).show();

                                                } else {

                                                    File file = new File(downPath);
                                                    long size = file.length();

                                                    Toast.makeText(ChatActivity.this, humanReadableByteCountSI(size), Toast.LENGTH_LONG).show();
                                                    Log.e("LLLLL_DownSize: ", humanReadableByteCountSI(size));

                                                    if (prefData.getLong("mediaDownload", 0) == 0) {
                                                        editorData.putLong("mediaDownload", size);
                                                    } else {
                                                        editorData.putLong("mediaDownload", prefData.getLong("mediaDownload", 0) + size);
                                                    }
                                                    editorData.apply();
                                                    editorData.commit();

                                                    DataStorageModel dataStorageModel = dbhelper.getRecord(userId);

                                                    dbhelper.addDataStorage(userId,
                                                            dataStorageModel.getMessage_count(),
                                                            dataStorageModel.getSent_contact(),
                                                            dataStorageModel.getSent_location(),
                                                            dataStorageModel.getSent_photos(),
                                                            dataStorageModel.getSent_videos(),
                                                            dataStorageModel.getSent_aud(),
                                                            String.valueOf(Long.parseLong(dataStorageModel.getSent_doc()) + 1),
                                                            dataStorageModel.getSent_photos_size(),
                                                            dataStorageModel.getSent_videos_size(),
                                                            dataStorageModel.getSent_aud_size(),
                                                            String.valueOf(Long.parseLong(dataStorageModel.getSent_doc_size()) + size));

                                                    //Toast.makeText(mContext, getString(R.string.downloaded), Toast.LENGTH_SHORT).show();
                                                }
                                            }
                                        };
                                        downloadFiles.execute(Constants.CHAT_IMG_PATH + message.attachment, message.message_type);
                                        progressbar.setVisibility(View.VISIBLE);
                                        progressbar.spin();
                                        downloadicon.setVisibility(View.VISIBLE);
                                    }
                                }
                            } else {
                                downloadicon.setVisibility(View.VISIBLE);
                                progressbar.setVisibility(View.VISIBLE);
                                progressbar.stopSpinning();

                            }
                        } else if (getConnectionType(ChatActivity.this).equals("mobileData")) {
                            if (Mobiledata1.isDoc()) {
                                if (storageManager.checkifFileExists(message.attachment, message.message_type, "receive")) {
                                    try {
                                        Intent intent = new Intent();
                                        intent.setAction(android.content.Intent.ACTION_VIEW);
                                        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                                        File file = storageManager.getFile(message.attachment, message.message_type, "receive");
                                        Uri photoURI = FileProvider.getUriForFile(mContext,
                                                BuildConfig.APPLICATION_ID + ".provider", file);

                                        MimeTypeMap mime = MimeTypeMap.getSingleton();
                                        String ext = file.getName().substring(file.getName().indexOf(".") + 1);
                                        String type = mime.getMimeTypeFromExtension(ext);

                                        intent.setDataAndType(photoURI, type);

                                        startActivity(intent);
                                    } catch (ActivityNotFoundException e) {
                                        Toast.makeText(ChatActivity.this, getString(R.string.no_application), Toast.LENGTH_SHORT).show();
                                        e.printStackTrace();
                                    }
                                } else {
                                    if (isNetworkConnected().equals(NOT_CONNECT)) {
                                        networkSnack();
                                    } else {
                                        DownloadFiles downloadFiles = new DownloadFiles(ChatActivity.this) {
                                            @Override
                                            protected void onPostExecute(String downPath) {
                                                progressbar.setVisibility(View.GONE);
                                                progressbar.stopSpinning();
                                                downloadicon.setVisibility(View.GONE);
                                                if (downPath == null) {
                                                    Log.v("Download Failed", "Download Failed");
                                                    Toast.makeText(mContext, "7:" + getString(R.string.something_wrong), Toast.LENGTH_SHORT).show();

                                                } else {

                                                    File file = new File(downPath);
                                                    long size = file.length();

                                                    Toast.makeText(ChatActivity.this, humanReadableByteCountSI(size), Toast.LENGTH_LONG).show();
                                                    Log.e("LLLLL_DownSize: ", humanReadableByteCountSI(size));

                                                    if (prefData.getLong("mediaDownload", 0) == 0) {
                                                        editorData.putLong("mediaDownload", size);
                                                    } else {
                                                        editorData.putLong("mediaDownload", prefData.getLong("mediaDownload", 0) + size);
                                                    }
                                                    editorData.apply();
                                                    editorData.commit();

                                                    DataStorageModel dataStorageModel = dbhelper.getRecord(userId);

                                                    dbhelper.addDataStorage(userId,
                                                            dataStorageModel.getMessage_count(),
                                                            dataStorageModel.getSent_contact(),
                                                            dataStorageModel.getSent_location(),
                                                            dataStorageModel.getSent_photos(),
                                                            dataStorageModel.getSent_videos(),
                                                            dataStorageModel.getSent_aud(),
                                                            String.valueOf(Long.parseLong(dataStorageModel.getSent_doc()) + 1),
                                                            dataStorageModel.getSent_photos_size(),
                                                            dataStorageModel.getSent_videos_size(),
                                                            dataStorageModel.getSent_aud_size(),
                                                            String.valueOf(Long.parseLong(dataStorageModel.getSent_doc_size()) + size));

                                                    //Toast.makeText(mContext, getString(R.string.downloaded), Toast.LENGTH_SHORT).show();
                                                }
                                            }
                                        };
                                        downloadFiles.execute(Constants.CHAT_IMG_PATH + message.attachment, message.message_type);
                                        progressbar.setVisibility(View.VISIBLE);
                                        progressbar.spin();
                                        downloadicon.setVisibility(View.VISIBLE);
                                    }
                                }
                            } else {
                                downloadicon.setVisibility(View.VISIBLE);
                                progressbar.setVisibility(View.VISIBLE);
                                progressbar.stopSpinning();

                            }
                        } else {
                            downloadicon.setVisibility(View.VISIBLE);
                            progressbar.setVisibility(View.VISIBLE);
                            progressbar.stopSpinning();

                        }
                    }

                }
                file_body_lay.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if (storageManager.checkifFileExists(message.attachment, message.message_type, "receive")) {
                            try {
                                Intent intent = new Intent();
                                intent.setAction(android.content.Intent.ACTION_VIEW);
                                intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                                File file = storageManager.getFile(message.attachment, message.message_type, "receive");
                                Uri photoURI = FileProvider.getUriForFile(mContext,
                                        BuildConfig.APPLICATION_ID + ".provider", file);

                                MimeTypeMap mime = MimeTypeMap.getSingleton();
                                String ext = file.getName().substring(file.getName().indexOf(".") + 1);
                                String type = mime.getMimeTypeFromExtension(ext);

                                intent.setDataAndType(photoURI, type);

                                startActivity(intent);
                            } catch (ActivityNotFoundException e) {
                                Toast.makeText(ChatActivity.this, getString(R.string.no_application), Toast.LENGTH_SHORT).show();
                                e.printStackTrace();
                            }
                        } else {
                            if (isNetworkConnected().equals(NOT_CONNECT)) {
                                networkSnack();
                            } else {
                                DownloadFiles downloadFiles = new DownloadFiles(ChatActivity.this) {
                                    @Override
                                    protected void onPostExecute(String downPath) {
                                        progressbar.setVisibility(View.GONE);
                                        progressbar.stopSpinning();
                                        downloadicon.setVisibility(View.GONE);
                                        if (downPath == null) {
                                            Log.v("Download Failed", "Download Failed");
                                            Toast.makeText(mContext, "7:" + getString(R.string.something_wrong), Toast.LENGTH_SHORT).show();

                                        } else {

                                            File file = new File(downPath);
                                            long size = file.length();

                                            Toast.makeText(ChatActivity.this, humanReadableByteCountSI(size), Toast.LENGTH_LONG).show();
                                            Log.e("LLLLL_DownSize: ", humanReadableByteCountSI(size));

                                            if (prefData.getLong("mediaDownload", 0) == 0) {
                                                editorData.putLong("mediaDownload", size);
                                            } else {
                                                editorData.putLong("mediaDownload", prefData.getLong("mediaDownload", 0) + size);
                                            }
                                            editorData.apply();
                                            editorData.commit();

                                            DataStorageModel dataStorageModel = dbhelper.getRecord(userId);

                                            dbhelper.addDataStorage(userId,
                                                    dataStorageModel.getMessage_count(),
                                                    dataStorageModel.getSent_contact(),
                                                    dataStorageModel.getSent_location(),
                                                    dataStorageModel.getSent_photos(),
                                                    dataStorageModel.getSent_videos(),
                                                    dataStorageModel.getSent_aud(),
                                                    String.valueOf(Long.parseLong(dataStorageModel.getSent_doc()) + 1),
                                                    dataStorageModel.getSent_photos_size(),
                                                    dataStorageModel.getSent_videos_size(),
                                                    dataStorageModel.getSent_aud_size(),
                                                    String.valueOf(Long.parseLong(dataStorageModel.getSent_doc_size()) + size));

                                            //Toast.makeText(mContext, getString(R.string.downloaded), Toast.LENGTH_SHORT).show();
                                        }
                                    }
                                };
                                downloadFiles.execute(Constants.CHAT_IMG_PATH + message.attachment, message.message_type);
                                progressbar.setVisibility(View.VISIBLE);
                                progressbar.spin();
                                downloadicon.setVisibility(View.VISIBLE);
                            }
                        }
                    }
                });
            }
        }

        private class SentVoiceHolder extends RecyclerView.ViewHolder {
            TextView filename, timeText, file_type_tv;
            ImageView tickimage, playIcon, uploadicon;
            RelativeLayout file_body_lay;
            ProgressWheel progressbar;
            SeekBar seekBar;
            ConstraintLayout replyLayout;
            TextView txtReplyUserName, txtReplyMsg;
            ImageView imgReply;

            SentVoiceHolder(View itemView) {
                super(itemView);

                filename = itemView.findViewById(R.id.filename);
                timeText = itemView.findViewById(R.id.text_message_time);
                tickimage = itemView.findViewById(R.id.tickimage);
                playIcon = itemView.findViewById(R.id.playIcon);
                file_body_lay = itemView.findViewById(R.id.file_body_lay);
                progressbar = itemView.findViewById(R.id.progressbar);
                uploadicon = itemView.findViewById(R.id.uploadicon);
                file_type_tv = itemView.findViewById(R.id.file_type_tv);
                seekBar = itemView.findViewById(R.id.seekBar);
                replyLayout = itemView.findViewById(R.id.lyt_reply);
                txtReplyUserName = itemView.findViewById(R.id.txt_user_name);
                txtReplyMsg = itemView.findViewById(R.id.txt_message);
                imgReply = itemView.findViewById(R.id.img_reply);
            }

            void bind(final MessagesData message) {
                seekBar.setEnabled(false);
                filename.setTextSize(TypedValue.COMPLEX_UNIT_SP, fontSize);

                if (selectedChatPos.contains(message)) {
                    itemView.setSelected(true);
                } else {
                    itemView.setSelected(false);
                }
                if (message.groupId != null && !message.groupId.equals(""))
                    showGroupReply(message.groupId, message.reply_to, replyLayout, txtReplyUserName, txtReplyMsg, imgReply);
                else
                    showReply(message, replyLayout, txtReplyUserName, txtReplyMsg, imgReply);
                timeText.setText(ApplicationClass.getTime(Long.parseLong(message.chat_time)));
                if (message.delivery_status.equals("read")) {
                    tickimage.setVisibility(View.VISIBLE);
                    tickimage.setImageResource(R.drawable.double_tick);
                } else if (message.delivery_status.equals("sent")) {
                    tickimage.setVisibility(View.VISIBLE);
                    tickimage.setImageResource(R.drawable.double_tick_unseen);
                } else if (message.progress.equals("completed")) {
                    tickimage.setVisibility(View.VISIBLE);
                    tickimage.setImageResource(R.drawable.single_tick);
                } else {
                    tickimage.setVisibility(View.GONE);
                }

                /*if (message.message_type.equals("document")) {
                    icon.setImageResource(R.drawable.icon_file_unknown);
                    file_type_tv.setVisibility(View.VISIBLE);
                    file_type_tv.setText(firstThree(FilenameUtils.getExtension(message.attachment)));
                } else if (message.message_type.equals("audio")) {
                    icon.setImageResource(R.drawable.mp3);
                    file_type_tv.setVisibility(View.GONE);
                }*/

                switch (message.progress) {
                    case "":
                        progressbar.setVisibility(View.VISIBLE);
                        progressbar.spin();
                        uploadicon.setVisibility(View.VISIBLE);
                        filename.setText(R.string.uploading);
                        break;
                    case "completed":
                        progressbar.setVisibility(View.GONE);
                        progressbar.stopSpinning();
                        uploadicon.setVisibility(View.GONE);

                        try {
                            File file = storageManager.getFile(message.attachment, message.message_type, "sent");
                            MediaMetadataRetriever mmr = new MediaMetadataRetriever();
                            mmr.setDataSource(file.getAbsolutePath());
                            String duration = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
                            mmr.release();

                            long dur = Long.parseLong(duration);
                            long seconds = dur / 1000;
                            long minute = seconds / 60;
                            seconds = seconds % 60;
                            duration = minute + ":" + seconds;

                            filename.setText(duration);
                        } catch (Exception e) {
                            filename.setText("00:00");
                        }
                        break;
                    case "error":
                        progressbar.setVisibility(View.VISIBLE);
                        progressbar.stopSpinning();
                        uploadicon.setVisibility(View.VISIBLE);
                        filename.setText(R.string.retry);
                        break;
                }

                playIcon.setOnClickListener(new View.OnClickListener() {
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
                                    filename.setText(getString(R.string.uploading));
                                    dbhelper.updateMessageData(message.message_id, Constants.TAG_PROGRESS, "");
                                    message.progress = "";
                                    Intent service = new Intent(ChatActivity.this, FileUploadService.class);
                                    Bundle b = new Bundle();
                                    b.putSerializable("mdata", message);
                                    b.putString("filepath", message.attachment);
                                    b.putString("chatType", "chat");
                                    service.putExtras(b);
                                    startService(service);
                                } catch (Exception ex) {
                                    ex.printStackTrace();
                                }
                            }
                        } else if (message.progress.equals("completed")) {
                            if (storageManager.checkifFileExists(message.attachment, message.message_type, "sent")) {
                                try {
                                    if (message.message_type.equals("voice")) {
                                        if (getAdapterPosition() == currentPlayingPosition) {
                                            if (mediaPlayer.isPlaying()) {
                                                mediaPlayer.pause();
                                            } else {
                                                mediaPlayer.start();
                                            }
                                        } else {
                                            currentPlayingPosition = getAdapterPosition();
                                            if (mediaPlayer != null) {
                                                if (null != playingHolder) {
                                                    updateNonPlayingView(playingHolder);
                                                }
                                                mediaPlayer.release();
                                            }
                                            playingHolder = SentVoiceHolder.this;
                                            startMediaPlayer(message, "sent");
                                        }
                                        updatePlayingView();
                                    }

                                } catch (ActivityNotFoundException e) {
                                    Toast.makeText(ChatActivity.this, getString(R.string.no_application), Toast.LENGTH_SHORT).show();
                                    e.printStackTrace();
                                }
                            } else {
                                Toast.makeText(ChatActivity.this, getString(R.string.no_media), Toast.LENGTH_SHORT).show();
                            }
                        }
                    }
                });

                seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                    @Override
                    public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                        if (b) {
                            mediaPlayer.seekTo(i);
                        }
                    }

                    @Override
                    public void onStartTrackingTouch(SeekBar seekBar) {

                    }

                    @Override
                    public void onStopTrackingTouch(SeekBar seekBar) {

                    }
                });
            }
        }

        private class ReceivedVoiceHolder extends RecyclerView.ViewHolder {
            TextView filename, timeText, file_type_tv;
            ImageView playIcon, downloadicon;
            RelativeLayout file_body_lay;
            ProgressWheel progressbar;
            SeekBar seekBar;
            ConstraintLayout replyLayout;
            TextView txtReplyUserName, txtReplyMsg;
            ImageView imgReply;

            ReceivedVoiceHolder(View itemView) {
                super(itemView);

                filename = itemView.findViewById(R.id.filename);
                timeText = itemView.findViewById(R.id.text_message_time);
                playIcon = itemView.findViewById(R.id.playIcon);
                file_body_lay = itemView.findViewById(R.id.file_body_lay);
                downloadicon = itemView.findViewById(R.id.downloadicon);
                progressbar = itemView.findViewById(R.id.progressbar);
                file_type_tv = itemView.findViewById(R.id.file_type_tv);
                seekBar = itemView.findViewById(R.id.seekBar);
                replyLayout = itemView.findViewById(R.id.lyt_reply);
                txtReplyUserName = itemView.findViewById(R.id.txt_user_name);
                txtReplyMsg = itemView.findViewById(R.id.txt_message);
                imgReply = itemView.findViewById(R.id.img_reply);
            }

            void bind(final MessagesData message) {
                seekBar.setEnabled(false);
                filename.setTextSize(TypedValue.COMPLEX_UNIT_SP, fontSize);
                //filename.setText(message.message);
                timeText.setText(ApplicationClass.getTime(Long.parseLong(message.chat_time)));
                if (selectedChatPos.contains(message)) {
                    itemView.setSelected(true);
                } else {
                    itemView.setSelected(false);
                }
                if (message.groupId != null && !message.groupId.equals(""))
                    showGroupReply(message.groupId, message.reply_to, replyLayout, txtReplyUserName, txtReplyMsg, imgReply);
                else
                    showReply(message, replyLayout, txtReplyUserName, txtReplyMsg, imgReply);
                /*if (message.message_type.equals("document")) {
                    icon.setImageResource(R.drawable.icon_file_unknown);
                    file_type_tv.setVisibility(View.VISIBLE);
                    file_type_tv.setText(firstThree(FilenameUtils.getExtension(message.attachment)));
                } else if (message.message_type.equals("audio")) {
                    file_type_tv.setVisibility(View.GONE);
                    icon.setImageResource(R.drawable.mp3);
                }*/

                if (storageManager.checkifFileExists(message.attachment, message.message_type, "receive")) {
                    downloadicon.setVisibility(View.GONE);
                    progressbar.setVisibility(View.GONE);
                } else {

                    if (isDataRoamingEnabled(ChatActivity.this)) {
                        if (RoamingData.isAudio()) {
                            if (!storageManager.checkifFileExists(message.attachment, message.message_type, "receive")) {
                                if (isNetworkConnected().equals(NOT_CONNECT)) {
                                    networkSnack();
                                } else {
                                    DownloadFiles downloadFiles = new DownloadFiles(ChatActivity.this) {
                                        @Override
                                        protected void onPostExecute(String downPath) {
                                            progressbar.setVisibility(View.GONE);
                                            progressbar.stopSpinning();
                                            downloadicon.setVisibility(View.GONE);
                                            if (downPath == null) {
                                                Log.v("Download Failed", "Download Failed");
                                                Toast.makeText(mContext, "7:" + getString(R.string.something_wrong), Toast.LENGTH_SHORT).show();
                                            } else {
                                                if (storageManager.checkifFileExists(message.attachment, message.message_type, "receive")) {
                                                    File file = storageManager.getFile(message.attachment, message.message_type, "receive");
                                                    MediaMetadataRetriever mmr = new MediaMetadataRetriever();
                                                    mmr.setDataSource(file.getAbsolutePath());
                                                    String duration = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
                                                    mmr.release();

                                                    long size = file.length();

                                                    if (prefData.getLong("mediaDownload", 0) == 0) {
                                                        editorData.putLong("mediaDownload", size);
                                                    } else {
                                                        editorData.putLong("mediaDownload", prefData.getLong("mediaDownload", 0) + size);
                                                    }
                                                    editorData.apply();
                                                    editorData.commit();

                                                    DataStorageModel dataStorageModel = dbhelper.getRecord(userId);

                                                    dbhelper.addDataStorage(userId,
                                                            dataStorageModel.getMessage_count(),
                                                            dataStorageModel.getSent_contact(),
                                                            dataStorageModel.getSent_location(),
                                                            dataStorageModel.getSent_photos(),
                                                            dataStorageModel.getSent_videos(),
                                                            String.valueOf(Long.parseLong(dataStorageModel.getSent_aud()) + 1),
                                                            dataStorageModel.getSent_doc(),
                                                            dataStorageModel.getSent_photos_size(),
                                                            dataStorageModel.getSent_videos_size(),
                                                            String.valueOf(Long.parseLong(dataStorageModel.getSent_aud_size()) + size),
                                                            dataStorageModel.getSent_doc_size());


                                                    long dur = Long.parseLong(duration);
                                                    long seconds = dur / 1000;
                                                    long minute = seconds / 60;
                                                    seconds = seconds % 60;
                                                    duration = minute + ":" + seconds;

                                                    filename.setText(duration);
                                                }
                                                //Toast.makeText(mContext, getString(R.string.downloaded), Toast.LENGTH_SHORT).show();
                                            }
                                        }
                                    };
                                    downloadFiles.execute(Constants.CHAT_IMG_PATH + message.attachment, message.message_type);
                                    progressbar.setVisibility(View.VISIBLE);
                                    progressbar.spin();
                                    downloadicon.setVisibility(View.VISIBLE);
                                }
                            }
                        } else {
                            downloadicon.setVisibility(View.VISIBLE);
                            progressbar.setVisibility(View.VISIBLE);
                            progressbar.stopSpinning();
                        }
                    } else {
                        if (getConnectionType(ChatActivity.this).equals("wifiData")) {
                            if (WifiData.isAudio()) {
                                if (!storageManager.checkifFileExists(message.attachment, message.message_type, "receive")) {
                                    if (isNetworkConnected().equals(NOT_CONNECT)) {
                                        networkSnack();
                                    } else {
                                        DownloadFiles downloadFiles = new DownloadFiles(ChatActivity.this) {
                                            @Override
                                            protected void onPostExecute(String downPath) {
                                                progressbar.setVisibility(View.GONE);
                                                progressbar.stopSpinning();
                                                downloadicon.setVisibility(View.GONE);
                                                if (downPath == null) {
                                                    Log.v("Download Failed", "Download Failed");
                                                    Toast.makeText(mContext, "7:" + getString(R.string.something_wrong), Toast.LENGTH_SHORT).show();
                                                } else {
                                                    if (storageManager.checkifFileExists(message.attachment, message.message_type, "receive")) {
                                                        File file = storageManager.getFile(message.attachment, message.message_type, "receive");
                                                        MediaMetadataRetriever mmr = new MediaMetadataRetriever();
                                                        mmr.setDataSource(file.getAbsolutePath());
                                                        String duration = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
                                                        mmr.release();

                                                        long size = file.length();

                                                        if (prefData.getLong("mediaDownload", 0) == 0) {
                                                            editorData.putLong("mediaDownload", size);
                                                        } else {
                                                            editorData.putLong("mediaDownload", prefData.getLong("mediaDownload", 0) + size);
                                                        }
                                                        editorData.apply();
                                                        editorData.commit();

                                                        DataStorageModel dataStorageModel = dbhelper.getRecord(userId);

                                                        dbhelper.addDataStorage(userId,
                                                                dataStorageModel.getMessage_count(),
                                                                dataStorageModel.getSent_contact(),
                                                                dataStorageModel.getSent_location(),
                                                                dataStorageModel.getSent_photos(),
                                                                dataStorageModel.getSent_videos(),
                                                                String.valueOf(Long.parseLong(dataStorageModel.getSent_aud()) + 1),
                                                                dataStorageModel.getSent_doc(),
                                                                dataStorageModel.getSent_photos_size(),
                                                                dataStorageModel.getSent_videos_size(),
                                                                String.valueOf(Long.parseLong(dataStorageModel.getSent_aud_size()) + size),
                                                                dataStorageModel.getSent_doc_size());


                                                        long dur = Long.parseLong(duration);
                                                        long seconds = dur / 1000;
                                                        long minute = seconds / 60;
                                                        seconds = seconds % 60;
                                                        duration = minute + ":" + seconds;

                                                        filename.setText(duration);
                                                    }
                                                    //Toast.makeText(mContext, getString(R.string.downloaded), Toast.LENGTH_SHORT).show();
                                                }
                                            }
                                        };
                                        downloadFiles.execute(Constants.CHAT_IMG_PATH + message.attachment, message.message_type);
                                        progressbar.setVisibility(View.VISIBLE);
                                        progressbar.spin();
                                        downloadicon.setVisibility(View.VISIBLE);
                                    }
                                }
                            } else {
                                downloadicon.setVisibility(View.VISIBLE);
                                progressbar.setVisibility(View.VISIBLE);
                                progressbar.stopSpinning();
                            }
                        } else if (getConnectionType(ChatActivity.this).equals("mobileData")) {
                            if (Mobiledata1.isAudio()) {
                                if (!storageManager.checkifFileExists(message.attachment, message.message_type, "receive")) {
                                    if (isNetworkConnected().equals(NOT_CONNECT)) {
                                        networkSnack();
                                    } else {
                                        DownloadFiles downloadFiles = new DownloadFiles(ChatActivity.this) {
                                            @Override
                                            protected void onPostExecute(String downPath) {
                                                progressbar.setVisibility(View.GONE);
                                                progressbar.stopSpinning();
                                                downloadicon.setVisibility(View.GONE);
                                                if (downPath == null) {
                                                    Log.v("Download Failed", "Download Failed");
                                                    Toast.makeText(mContext, "7:" + getString(R.string.something_wrong), Toast.LENGTH_SHORT).show();
                                                } else {
                                                    if (storageManager.checkifFileExists(message.attachment, message.message_type, "receive")) {
                                                        File file = storageManager.getFile(message.attachment, message.message_type, "receive");
                                                        MediaMetadataRetriever mmr = new MediaMetadataRetriever();
                                                        mmr.setDataSource(file.getAbsolutePath());
                                                        String duration = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
                                                        mmr.release();

                                                        long size = file.length();

                                                        if (prefData.getLong("mediaDownload", 0) == 0) {
                                                            editorData.putLong("mediaDownload", size);
                                                        } else {
                                                            editorData.putLong("mediaDownload", prefData.getLong("mediaDownload", 0) + size);
                                                        }
                                                        editorData.apply();
                                                        editorData.commit();

                                                        DataStorageModel dataStorageModel = dbhelper.getRecord(userId);

                                                        dbhelper.addDataStorage(userId,
                                                                dataStorageModel.getMessage_count(),
                                                                dataStorageModel.getSent_contact(),
                                                                dataStorageModel.getSent_location(),
                                                                dataStorageModel.getSent_photos(),
                                                                dataStorageModel.getSent_videos(),
                                                                String.valueOf(Long.parseLong(dataStorageModel.getSent_aud()) + 1),
                                                                dataStorageModel.getSent_doc(),
                                                                dataStorageModel.getSent_photos_size(),
                                                                dataStorageModel.getSent_videos_size(),
                                                                String.valueOf(Long.parseLong(dataStorageModel.getSent_aud_size()) + size),
                                                                dataStorageModel.getSent_doc_size());


                                                        long dur = Long.parseLong(duration);
                                                        long seconds = dur / 1000;
                                                        long minute = seconds / 60;
                                                        seconds = seconds % 60;
                                                        duration = minute + ":" + seconds;

                                                        filename.setText(duration);
                                                    }
                                                    //Toast.makeText(mContext, getString(R.string.downloaded), Toast.LENGTH_SHORT).show();
                                                }
                                            }
                                        };
                                        downloadFiles.execute(Constants.CHAT_IMG_PATH + message.attachment, message.message_type);
                                        progressbar.setVisibility(View.VISIBLE);
                                        progressbar.spin();
                                        downloadicon.setVisibility(View.VISIBLE);
                                    }
                                }
                            } else {
                                downloadicon.setVisibility(View.VISIBLE);
                                progressbar.setVisibility(View.VISIBLE);
                                progressbar.stopSpinning();
                            }
                        } else {
                            downloadicon.setVisibility(View.VISIBLE);
                            progressbar.setVisibility(View.VISIBLE);
                            progressbar.stopSpinning();
                        }
                    }

                }
                if (storageManager.checkifFileExists(message.attachment, message.message_type, "receive")) {
                    File file = storageManager.getFile(message.attachment, message.message_type, "receive");
                    MediaMetadataRetriever mmr = new MediaMetadataRetriever();
                    mmr.setDataSource(file.getAbsolutePath());
                    String duration = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
                    mmr.release();

                    long dur = Long.parseLong(duration);
                    long seconds = dur / 1000;
                    long minute = seconds / 60;
                    seconds = seconds % 60;
                    duration = minute + ":" + seconds;

                    filename.setText(duration);
                } else {
                    file_body_lay.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            if (!storageManager.checkifFileExists(message.attachment, message.message_type, "receive")) {
                                if (isNetworkConnected().equals(NOT_CONNECT)) {
                                    networkSnack();
                                } else {
                                    DownloadFiles downloadFiles = new DownloadFiles(ChatActivity.this) {
                                        @Override
                                        protected void onPostExecute(String downPath) {
                                            progressbar.setVisibility(View.GONE);
                                            progressbar.stopSpinning();
                                            downloadicon.setVisibility(View.GONE);
                                            if (downPath == null) {
                                                Log.v("Download Failed", "Download Failed");
                                                Toast.makeText(mContext, "7:" + getString(R.string.something_wrong), Toast.LENGTH_SHORT).show();
                                            } else {
                                                if (storageManager.checkifFileExists(message.attachment, message.message_type, "receive")) {
                                                    File file = storageManager.getFile(message.attachment, message.message_type, "receive");
                                                    MediaMetadataRetriever mmr = new MediaMetadataRetriever();
                                                    mmr.setDataSource(file.getAbsolutePath());
                                                    String duration = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
                                                    mmr.release();

                                                    long size = file.length();

                                                    if (prefData.getLong("mediaDownload", 0) == 0) {
                                                        editorData.putLong("mediaDownload", size);
                                                    } else {
                                                        editorData.putLong("mediaDownload", prefData.getLong("mediaDownload", 0) + size);
                                                    }
                                                    editorData.apply();
                                                    editorData.commit();

                                                    DataStorageModel dataStorageModel = dbhelper.getRecord(userId);

                                                    dbhelper.addDataStorage(userId,
                                                            dataStorageModel.getMessage_count(),
                                                            dataStorageModel.getSent_contact(),
                                                            dataStorageModel.getSent_location(),
                                                            dataStorageModel.getSent_photos(),
                                                            dataStorageModel.getSent_videos(),
                                                            String.valueOf(Long.parseLong(dataStorageModel.getSent_aud()) + 1),
                                                            dataStorageModel.getSent_doc(),
                                                            dataStorageModel.getSent_photos_size(),
                                                            dataStorageModel.getSent_videos_size(),
                                                            String.valueOf(Long.parseLong(dataStorageModel.getSent_aud_size()) + size),
                                                            dataStorageModel.getSent_doc_size());


                                                    long dur = Long.parseLong(duration);
                                                    long seconds = dur / 1000;
                                                    long minute = seconds / 60;
                                                    seconds = seconds % 60;
                                                    duration = minute + ":" + seconds;

                                                    filename.setText(duration);
                                                }
                                                //Toast.makeText(mContext, getString(R.string.downloaded), Toast.LENGTH_SHORT).show();
                                            }
                                        }
                                    };
                                    downloadFiles.execute(Constants.CHAT_IMG_PATH + message.attachment, message.message_type);
                                    progressbar.setVisibility(View.VISIBLE);
                                    progressbar.spin();
                                    downloadicon.setVisibility(View.VISIBLE);
                                }
                            }
                        }
                    });
                    filename.setText(message.message);
                }

                playIcon.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if (storageManager.checkifFileExists(message.attachment, message.message_type, "receive")) {
                            try {
                                if (message.message_type.equals("voice")) {
                                    if (getAdapterPosition() == currentPlayingPosition) {
                                        if (mediaPlayer.isPlaying()) {
                                            mediaPlayer.pause();
                                        } else {
                                            mediaPlayer.start();
                                        }
                                    } else {
                                        currentPlayingPosition = getAdapterPosition();
                                        if (mediaPlayer != null) {
                                            if (null != playingHolder) {
                                                updateNonPlayingView(playingHolder);
                                            }
                                            mediaPlayer.release();
                                        }
                                        playingHolder = ReceivedVoiceHolder.this;
                                        startMediaPlayer(message, "receive");
                                    }
                                    updatePlayingView();
                                }

                            } catch (ActivityNotFoundException e) {
                                Toast.makeText(ChatActivity.this, getString(R.string.no_application), Toast.LENGTH_SHORT).show();
                                e.printStackTrace();
                            }
                        } else {
                            if (isNetworkConnected().equals(NOT_CONNECT)) {
                                networkSnack();
                            } else {
                                DownloadFiles downloadFiles = new DownloadFiles(ChatActivity.this) {
                                    @Override
                                    protected void onPostExecute(String downPath) {
                                        progressbar.setVisibility(View.GONE);
                                        progressbar.stopSpinning();
                                        downloadicon.setVisibility(View.GONE);
                                        if (downPath == null) {
                                            Log.v("Download Failed", "Download Failed");
                                            Toast.makeText(mContext, "7:" + getString(R.string.something_wrong), Toast.LENGTH_SHORT).show();
                                        } else {
                                            if (storageManager.checkifFileExists(message.attachment, message.message_type, "receive")) {
                                                File file = storageManager.getFile(message.attachment, message.message_type, "receive");
                                                MediaMetadataRetriever mmr = new MediaMetadataRetriever();
                                                mmr.setDataSource(file.getAbsolutePath());
                                                String duration = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
                                                mmr.release();

                                                long size = file.length();

                                                if (prefData.getLong("mediaDownload", 0) == 0) {
                                                    editorData.putLong("mediaDownload", size);
                                                } else {
                                                    editorData.putLong("mediaDownload", prefData.getLong("mediaDownload", 0) + size);
                                                }
                                                editorData.apply();
                                                editorData.commit();

                                                DataStorageModel dataStorageModel = dbhelper.getRecord(userId);

                                                dbhelper.addDataStorage(userId,
                                                        dataStorageModel.getMessage_count(),
                                                        dataStorageModel.getSent_contact(),
                                                        dataStorageModel.getSent_location(),
                                                        dataStorageModel.getSent_photos(),
                                                        dataStorageModel.getSent_videos(),
                                                        String.valueOf(Long.parseLong(dataStorageModel.getSent_aud()) + 1),
                                                        dataStorageModel.getSent_doc(),
                                                        dataStorageModel.getSent_photos_size(),
                                                        dataStorageModel.getSent_videos_size(),
                                                        String.valueOf(Long.parseLong(dataStorageModel.getSent_aud_size()) + size),
                                                        dataStorageModel.getSent_doc_size());


                                                long dur = Long.parseLong(duration);
                                                long seconds = dur / 1000;
                                                long minute = seconds / 60;
                                                seconds = seconds % 60;
                                                duration = minute + ":" + seconds;

                                                filename.setText(duration);
                                            }
                                            //Toast.makeText(mContext, getString(R.string.downloaded), Toast.LENGTH_SHORT).show();
                                        }
                                    }
                                };
                                downloadFiles.execute(Constants.CHAT_IMG_PATH + message.attachment, message.message_type);
                                progressbar.setVisibility(View.VISIBLE);
                                progressbar.spin();
                                downloadicon.setVisibility(View.VISIBLE);
                            }
                        }
                    }
                });

                seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                    @Override
                    public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                        if (b) {
                            mediaPlayer.seekTo(i);
                        }
                    }

                    @Override
                    public void onStartTrackingTouch(SeekBar seekBar) {

                    }

                    @Override
                    public void onStopTrackingTouch(SeekBar seekBar) {

                    }
                });
            }
        }

        private void updateNonPlayingView(RecyclerView.ViewHolder holder) {
            if (holder instanceof SentVoiceHolder) {
                ((SentVoiceHolder) holder).seekBar.removeCallbacks(seekBarUpdater);
                ((SentVoiceHolder) holder).seekBar.setEnabled(false);
                ((SentVoiceHolder) holder).seekBar.setProgress(0);
                ((SentVoiceHolder) holder).playIcon.setImageResource(R.drawable.ic_play);
            } else if (holder instanceof ReceivedVoiceHolder) {
                ((ReceivedVoiceHolder) holder).seekBar.removeCallbacks(seekBarUpdater);
                ((ReceivedVoiceHolder) holder).seekBar.setEnabled(false);
                ((ReceivedVoiceHolder) holder).seekBar.setProgress(0);
                ((ReceivedVoiceHolder) holder).playIcon.setImageResource(R.drawable.ic_play);

            }
        }

        private void updatePlayingView() {
            if (playingHolder instanceof SentVoiceHolder) {
                ((SentVoiceHolder) playingHolder).seekBar.setMax(mediaPlayer.getDuration());
                ((SentVoiceHolder) playingHolder).seekBar.setProgress(mediaPlayer.getCurrentPosition());
                ((SentVoiceHolder) playingHolder).seekBar.setEnabled(true);
                if (mediaPlayer.isPlaying()) {
                    ((SentVoiceHolder) playingHolder).seekBar.postDelayed(seekBarUpdater, 100);
                    ((SentVoiceHolder) playingHolder).playIcon.setImageResource(R.drawable.ic_pause);
                } else {
                    ((SentVoiceHolder) playingHolder).seekBar.removeCallbacks(seekBarUpdater);
                    ((SentVoiceHolder) playingHolder).playIcon.setImageResource(R.drawable.ic_play);
                }
            } else if (playingHolder instanceof ReceivedVoiceHolder) {
                ((ReceivedVoiceHolder) playingHolder).seekBar.setMax(mediaPlayer.getDuration());
                ((ReceivedVoiceHolder) playingHolder).seekBar.setProgress(mediaPlayer.getCurrentPosition());
                ((ReceivedVoiceHolder) playingHolder).seekBar.setEnabled(true);
                if (mediaPlayer.isPlaying()) {
                    ((ReceivedVoiceHolder) playingHolder).seekBar.postDelayed(seekBarUpdater, 100);
                    ((ReceivedVoiceHolder) playingHolder).playIcon.setImageResource(R.drawable.ic_pause);
                } else {
                    ((ReceivedVoiceHolder) playingHolder).seekBar.removeCallbacks(seekBarUpdater);
                    ((ReceivedVoiceHolder) playingHolder).playIcon.setImageResource(R.drawable.ic_play);
                }

            }

        }

        void stopPlayer() {
            if (null != mediaPlayer) {
                releaseMediaPlayer();
            }
        }

        private void startMediaPlayer(MessagesData message, String from) {
            try {
                mediaPlayer = new MediaPlayer();
                File file = storageManager.getFile(message.attachment, message.message_type, from);
                mediaPlayer.setDataSource(file.getAbsolutePath());
                //mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
                mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                    @Override
                    public void onCompletion(MediaPlayer mp) {
                        releaseMediaPlayer();
                    }
                });
                mediaPlayer.prepare();
                mediaPlayer.setLooping(false);
                mediaPlayer.start();
            } catch (Exception e) {
                e.printStackTrace();
            }

        }

        private void releaseMediaPlayer() {
            if (null != playingHolder) {
                updateNonPlayingView(playingHolder);
            }
            mediaPlayer.release();
            mediaPlayer = null;
            currentPlayingPosition = -1;
        }

        private class SeekBarUpdater implements Runnable {
            @Override
            public void run() {
                if (null != playingHolder && mediaPlayer != null && mediaPlayer.isPlaying()) {
                    if (playingHolder instanceof SentVoiceHolder) {
                        ((SentVoiceHolder) playingHolder).seekBar.setProgress(mediaPlayer.getCurrentPosition());
                        ((SentVoiceHolder) playingHolder).seekBar.postDelayed(this, 100);
                    } else if (playingHolder instanceof ReceivedVoiceHolder) {
                        ((ReceivedVoiceHolder) playingHolder).seekBar.setProgress(mediaPlayer.getCurrentPosition());
                        ((ReceivedVoiceHolder) playingHolder).seekBar.postDelayed(this, 100);
                    }
                }
            }
        }

        @Override
        public void onViewRecycled(@NonNull RecyclerView.ViewHolder holder) {
            super.onViewRecycled(holder);
            if (currentPlayingPosition == holder.getAdapterPosition()) {
                updateNonPlayingView(playingHolder);
                playingHolder = null;
            }
        }

        private class SentContactHolder extends RecyclerView.ViewHolder {
            TextView username, phoneno, timeText;
            ImageView tickimage;
            ConstraintLayout replyLayout;
            TextView txtReplyUserName, txtReplyMsg;
            ImageView imgReply;

            SentContactHolder(View itemView) {
                super(itemView);
                username = itemView.findViewById(R.id.username);
                phoneno = itemView.findViewById(R.id.phoneno);
                tickimage = itemView.findViewById(R.id.tickimage);
                timeText = itemView.findViewById(R.id.text_message_time);
                replyLayout = itemView.findViewById(R.id.lyt_reply);
                txtReplyUserName = itemView.findViewById(R.id.txt_user_name);
                txtReplyMsg = itemView.findViewById(R.id.txt_message);
                imgReply = itemView.findViewById(R.id.img_reply);
            }

            void bind(MessagesData message) {
                username.setTextSize(TypedValue.COMPLEX_UNIT_SP, fontSize);
                phoneno.setTextSize(TypedValue.COMPLEX_UNIT_SP, fontSize);
                username.setText(message.contact_name);
                phoneno.setText(message.contact_phone_no);
                timeText.setText(ApplicationClass.getTime(Long.parseLong(message.chat_time)));
                if (selectedChatPos.contains(message)) {
                    itemView.setSelected(true);
                } else {
                    itemView.setSelected(false);
                }
                if (message.groupId != null && !message.groupId.equals(""))
                    showGroupReply(message.groupId, message.reply_to, replyLayout, txtReplyUserName, txtReplyMsg, imgReply);
                else
                    showReply(message, replyLayout, txtReplyUserName, txtReplyMsg, imgReply);
                switch (message.delivery_status) {
                    case "read":
                        tickimage.setVisibility(View.VISIBLE);
                        tickimage.setImageResource(R.drawable.double_tick);
                        break;
                    case "sent":
                        tickimage.setVisibility(View.VISIBLE);
                        tickimage.setImageResource(R.drawable.double_tick_unseen);
                        break;
                    default:
                        tickimage.setVisibility(View.VISIBLE);
                        tickimage.setImageResource(R.drawable.single_tick);
                        break;
                }
            }
        }

        private class ReceivedContactHolder extends RecyclerView.ViewHolder {
            TextView username, phoneno, timeText, addcontact;
            ConstraintLayout replyLayout;
            TextView txtReplyUserName, txtReplyMsg;
            ImageView imgReply;

            ReceivedContactHolder(View itemView) {
                super(itemView);
                username = itemView.findViewById(R.id.username);
                phoneno = itemView.findViewById(R.id.phoneno);
                timeText = itemView.findViewById(R.id.text_message_time);
                addcontact = itemView.findViewById(R.id.addcontact);
                replyLayout = itemView.findViewById(R.id.lyt_reply);
                txtReplyUserName = itemView.findViewById(R.id.txt_user_name);
                txtReplyMsg = itemView.findViewById(R.id.txt_message);
                imgReply = itemView.findViewById(R.id.img_reply);
            }

            void bind(final MessagesData message) {
                username.setTextSize(TypedValue.COMPLEX_UNIT_SP, fontSize);
                phoneno.setTextSize(TypedValue.COMPLEX_UNIT_SP, fontSize);
                username.setText(message.contact_name);
                phoneno.setText(message.contact_phone_no);
                timeText.setText(ApplicationClass.getTime(Long.parseLong(message.chat_time)));
                if (selectedChatPos.contains(message)) {
                    itemView.setSelected(true);
                } else {
                    itemView.setSelected(false);
                }
                if (message.groupId != null && !message.groupId.equals(""))
                    showGroupReply(message.groupId, message.reply_to, replyLayout, txtReplyUserName, txtReplyMsg, imgReply);
                else
                    showReply(message, replyLayout, txtReplyUserName, txtReplyMsg, imgReply);
                addcontact.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent intent = new Intent(ContactsContract.Intents.Insert.ACTION);
                        intent.setType(ContactsContract.RawContacts.CONTENT_TYPE);
                        intent.putExtra(ContactsContract.Intents.Insert.PHONE, message.contact_phone_no);
                        intent.putExtra(ContactsContract.Intents.Insert.NAME, message.contact_name);
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

            void bind(final MessagesData message) {
                timeText.setText(getFormattedDate(mContext, Long.parseLong(message.chat_time)));
            }
        }
    }

    public RecyclerItemClickListener chatItemClick(Context mContext, final RecyclerView recyclerView) {
        return new RecyclerItemClickListener(mContext, recyclerView, new RecyclerItemClickListener.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                if (chatLongPressed) {
                    String messageType = messagesList.get(position).message_type;
                    int chatType = recyclerView.getAdapter().getItemViewType(position);
                    if (chatType != VIEW_TYPE_DATE && isForwardable(messagesList.get(position))) {
                        if (selectedChatPos.contains(messagesList.get(position))) {
                            selectedChatPos.remove(messagesList.get(position));
                            if (selectedChatPos.size() == 0) {
                                chatUserLay.setVisibility(View.VISIBLE);
                                forwordLay.setVisibility(View.GONE);
                                chatLongPressed = false;
                            }
                        } else {
//                            selectedChatPos.clear();
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
                    String messageType = messagesList.get(position).message_type;
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

    private boolean isForwardable(MessagesData mData) {
        switch (mData.message_type) {
            case "video":
            case "document":
            case "audio":
                if (!mData.progress.equals("completed")) {
                    return false;
                } else if (!mData.user_id.equals(GetSet.getUserId()) && !storageManager.checkifFileExists(mData.attachment, mData.message_type, "receive")) {
                    return false;
                }
                return true;
            case "image":
                if (!mData.progress.equals("completed")) {
                    return false;
                } else if (!mData.user_id.equals(GetSet.getUserId()) && !storageManager.checkifImageExists("receive", mData.attachment)) {
                    return false;
                }
                return true;
            default:
                return true;
        }
    }

    private void emitImage(MessagesData mdata) {
        try {
//            JSONObject jobj = new JSONObject();
            JSONObject message = new JSONObject();
            message.put(Constants.TAG_USER_ID, GetSet.getUserId());
            message.put(Constants.TAG_USER_NAME, GetSet.getUserName());
            message.put(Constants.TAG_MESSAGE_TYPE, mdata.message_type);
            message.put(Constants.TAG_ATTACHMENT, mdata.attachment);
            message.put(Constants.TAG_MESSAGE, mdata.message);
            message.put(Constants.TAG_CHAT_TIME, mdata.chat_time);
            message.put(Constants.TAG_CHAT_ID, mdata.chat_id);
            message.put(Constants.TAG_MESSAGE_ID, mdata.message_id);
            message.put(TAG_FRIENDID, userId);
            message.put(Constants.TAG_REPLY_TO, msgReplyTo);
            message.put(Constants.TAG_SENDER_ID, GetSet.getUserId());
            message.put(Constants.TAG_CHAT_TYPE, Constants.TAG_SINGLE);
            message.put(Constants.TAG_REPLY_TO, msgReplyTo);
            message.put(Constants.TAG_TO_GROUP_ID, groupReply);
//            jobj.put(Constants.TAG_SENDER_ID, GetSet.getUserId());
//            jobj.put(Constants.TAG_RECEIVER_ID, userId);
//            jobj.put("message_data", message);
            Log.v("startchat", "startchat=" + message);
            socketConnection.startChat(message);
            btnCancelReply.callOnClick();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private MessagesData updateDBList(String type, String imagePath, String filePath) {
        String unixStamp = String.valueOf(System.currentTimeMillis() / 1000L);
        String chatId = GetSet.getUserId() + userId;
        RandomString randomString = new RandomString(10);
        String messageId = GetSet.getUserId() + randomString.nextString();

        String msg = "";
        if (type.equals("image")) {
            msg = getString(R.string.image);
        } else if (type.equals("audio")) {
            msg = getFileName(filePath);
        } else if (type.equals("voice")) {
            msg = "Voice Note";
        } else if (type.equals("video")) {
            msg = getString(R.string.video);
        } else if (type.equals("document")) {
            msg = getFileName(filePath);
        }

        MessagesData data = new MessagesData();
        data.user_id = GetSet.getUserId();
        data.message_type = type;
        data.message = msg;
        data.message_id = messageId;
        data.chat_time = unixStamp;
        data.delivery_status = "";
        data.progress = "";
        data.receiver_id = userId;

        switch (type) {
            case "video":
                data.thumbnail = imagePath;
                data.attachment = filePath;
                dbhelper.addMessageDatas(chatId, messageId, GetSet.getUserId(), GetSet.getUserName(),
                        type, msg, filePath, "", "", "", "",
                        "", unixStamp, userId, GetSet.getUserId(), "", imagePath, msgReplyTo, groupReply);
                break;
            case "image":
                data.thumbnail = "";
                data.attachment = imagePath;
                dbhelper.addMessageDatas(chatId, messageId, GetSet.getUserId(), GetSet.getUserName(),
                        type, msg, imagePath, "", "", "", "",
                        "", unixStamp, userId, GetSet.getUserId(), "", "", msgReplyTo, groupReply);
                break;
            default:
                data.thumbnail = "";
                data.attachment = filePath;
                dbhelper.addMessageDatas(chatId, messageId, GetSet.getUserId(), GetSet.getUserName(),
                        type, msg, filePath, "", "", "", "",
                        "", unixStamp, userId, GetSet.getUserId(), "", "", msgReplyTo, groupReply);
                break;
        }
        dbhelper.addRecentMessages(chatId, userId, messageId, unixStamp, "0");

        messagesList.add(0, data);
        messageListAdapter.notifyItemInserted(0);
        recyclerView.smoothScrollToPosition(0);

        return data;
    }

    private void emitLocation(String type, String lat, String lon) {
        String unixStamp = String.valueOf(System.currentTimeMillis() / 1000L);
        String chatId = GetSet.getUserId() + userId;
        RandomString randomString = new RandomString(10);
        String messageId = GetSet.getUserId() + randomString.nextString();
        try {
            if (results.blockedme == null || !results.blockedme.equals("block")) {
//                JSONObject jobj = new JSONObject();
                JSONObject message = new JSONObject();
                message.put(Constants.TAG_USER_ID, GetSet.getUserId());
                message.put(Constants.TAG_USER_NAME, GetSet.getUserName());
                message.put(Constants.TAG_MESSAGE_TYPE, type);
                message.put(Constants.TAG_MESSAGE, "Location");
                message.put(Constants.TAG_CHAT_TIME, unixStamp);
                message.put(Constants.TAG_CHAT_ID, chatId);
                message.put(Constants.TAG_LAT, lat);
                message.put(Constants.TAG_LON, lon);
                message.put(Constants.TAG_MESSAGE_ID, messageId);
                message.put(TAG_FRIENDID, userId);
                message.put(Constants.TAG_SENDER_ID, GetSet.getUserId());
                message.put(Constants.TAG_CHAT_TYPE, Constants.TAG_SINGLE);
                message.put(Constants.TAG_REPLY_TO, msgReplyTo);
                message.put(Constants.TAG_TO_GROUP_ID, groupReply);
//                jobj.put(Constants.TAG_SENDER_ID, GetSet.getUserId());
//                jobj.put(Constants.TAG_RECEIVER_ID, userId);
//                jobj.put("message_data", message);
                Log.v("startchat", "startchat=" + message);
                socketConnection.startChat(message);
            }

            dbhelper.addMessageDatas(chatId, messageId, GetSet.getUserId(), GetSet.getUserName(),
                    type, "Location", "", lat, lon, "", "",
                    "", unixStamp, userId, GetSet.getUserId(), "", "", msgReplyTo, groupReply);

            dbhelper.addRecentMessages(chatId, userId, messageId, unixStamp, "0");
        } catch (JSONException e) {
            e.printStackTrace();
        }

        MessagesData data = new MessagesData();
        data.user_id = GetSet.getUserId();
        data.message_type = type;
        data.message = "Location";
        data.lat = lat;
        data.lon = lon;
        data.message_id = messageId;
        data.chat_time = unixStamp;
        data.delivery_status = "";
        data.reply_to = msgReplyTo;
        data.groupId = groupReply;
        messagesList.add(0, data);
        messageListAdapter.notifyItemInserted(0);
        recyclerView.smoothScrollToPosition(0);
        btnCancelReply.callOnClick();
    }

    private void emitContact(String type, String name, String phone, String countrycode) {
        String unixStamp = String.valueOf(System.currentTimeMillis() / 1000L);
        String chatId = GetSet.getUserId() + userId;
        RandomString randomString = new RandomString(10);
        String messageId = GetSet.getUserId() + randomString.nextString();
        try {
            if (results.blockedme == null || !results.blockedme.equals("block")) {
//                JSONObject jobj = new JSONObject();
                JSONObject message = new JSONObject();
                message.put(Constants.TAG_USER_ID, GetSet.getUserId());
                message.put(Constants.TAG_USER_NAME, GetSet.getUserName());
                message.put(Constants.TAG_MESSAGE_TYPE, type);
                message.put(Constants.TAG_MESSAGE, "Contact");
                message.put(Constants.TAG_CHAT_TIME, unixStamp);
                message.put(Constants.TAG_CHAT_ID, chatId);
                message.put(Constants.TAG_CONTACT_NAME, name);
                message.put(Constants.TAG_CONTACT_PHONE_NO, phone);
                message.put(Constants.TAG_CONTACT_COUNTRY_CODE, countrycode);
                message.put(Constants.TAG_MESSAGE_ID, messageId);
                message.put(TAG_FRIENDID, userId);
                message.put(Constants.TAG_SENDER_ID, GetSet.getUserId());
                message.put(Constants.TAG_CHAT_TYPE, Constants.TAG_SINGLE);
                message.put(Constants.TAG_REPLY_TO, msgReplyTo);
                message.put(Constants.TAG_TO_GROUP_ID, groupReply);
//                jobj.put(Constants.TAG_SENDER_ID, GetSet.getUserId());
//                jobj.put(Constants.TAG_RECEIVER_ID, userId);
//                jobj.put("message_data", message);
                Log.v("startchat", "startchat=" + message);
                socketConnection.startChat(message);
            }

            dbhelper.addMessageDatas(chatId, messageId, GetSet.getUserId(), GetSet.getUserName(),
                    type, "Contact", "", "", "", name, phone,
                    countrycode, unixStamp, userId, GetSet.getUserId(), "", "", msgReplyTo, groupReply);

            dbhelper.addRecentMessages(chatId, userId, messageId, unixStamp, "0");
        } catch (JSONException e) {
            e.printStackTrace();
        }

        MessagesData data = new MessagesData();
        data.user_id = GetSet.getUserId();
        data.message_type = type;
        data.message = "Contact";
        data.contact_name = name;
        data.contact_phone_no = phone;
        data.contact_country_code = countrycode;
        data.message_id = messageId;
        data.chat_time = unixStamp;
        data.reply_to = msgReplyTo;
        data.groupId = groupReply;
        data.delivery_status = "";
        messagesList.add(0, data);
        messageListAdapter.notifyItemInserted(0);
        recyclerView.smoothScrollToPosition(0);
        btnCancelReply.callOnClick();
    }

    private void deleteChatConfirmDialog() {
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
        title.setText(R.string.really_delete_chat_history);
        no.setVisibility(View.VISIBLE);

        yes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
                dbhelper.deleteAllChats(GetSet.getUserId() + userId);
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

    private void deleteMessageConfirmDialog(ArrayList<MessagesData> mData) {
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

                dbhelper.deleteMessageFromId(mData);
                for (int i = 0; i < mData.size(); i++)
                    messagesList.remove(mData.get(i));
                Toast.makeText(ChatActivity.this, getString(R.string.message_deleted), Toast.LENGTH_SHORT).show();
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

    private void blockChatConfirmDialog(final String type, String from) {
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

        if (from.equals("popup")) {
            yes.setText(getString(R.string.im_sure));
            no.setText(getString(R.string.nope));
            if (type.equals(Constants.TAG_BLOCK)) {
                title.setText(R.string.really_block_chat);
            } else {
                title.setText(R.string.really_unblock_chat);
            }
        } else {
            yes.setText(getString(R.string.unblock));
            no.setText(getString(R.string.cancel));
            title.setText(R.string.unblock_message);
        }

        no.setVisibility(View.VISIBLE);

        yes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
                try {
                    JSONObject jsonObject = new JSONObject();
                    jsonObject.put(Constants.TAG_SENDER_ID, GetSet.getUserId());
                    jsonObject.put(Constants.TAG_RECEIVER_ID, userId);
                    jsonObject.put(Constants.TAG_TYPE, type);
                    Log.v("block", "block=" + jsonObject);
                    socketConnection.block(jsonObject);
                    dbhelper.updateBlockStatus(userId, Constants.TAG_BLOCKED_BYME, type);
                    results = dbhelper.getContactDetail(userId);
                    online.setVisibility(View.GONE);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
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

    public String getFormattedDateTime(Context context, long smsTimeInMilis) {
        Calendar smsTime = Calendar.getInstance(TimeZone.getTimeZone("GMT+5:30"));
        smsTime.setTimeInMillis(smsTimeInMilis * 1000L);

        Calendar now = Calendar.getInstance();

        final String timeFormatString = "h:mm aa";
        final String dateTimeFormatString = "EEEE, MMMM d, h:mm aa";
        final long HOURS = 60 * 60 * 60;
        if (now.get(Calendar.DATE) == smsTime.get(Calendar.DATE)) {
            return getString(R.string.today) + " " + DateFormat.format(timeFormatString, smsTime);
        } else if (now.get(Calendar.DATE) - smsTime.get(Calendar.DATE) == 1) {
            return getString(R.string.yesterday) + " " + DateFormat.format(timeFormatString, smsTime);
        } else if (now.get(Calendar.YEAR) == smsTime.get(Calendar.YEAR)) {
            return DateFormat.format(dateTimeFormatString, smsTime).toString();
        } else {
            return DateFormat.format("MMMM dd yyyy, h:mm aa", smsTime).toString();
        }
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

    private void uploadImage(byte[] imageBytes, final String imagePath, final MessagesData mdata, final String filePath) {
        RequestBody requestFile = RequestBody.create(MediaType.parse("image/*"), imageBytes);
        MultipartBody.Part body = MultipartBody.Part.createFormData("attachment", "image.jpg", requestFile);

        RequestBody userid = RequestBody.create(MediaType.parse("multipart/form-data"), GetSet.getUserId());
        Call<UpMyChatModel> call3 = apiInterface.upmychat(GetSet.getToken(), body, userid);
        call3.enqueue(new Callback<UpMyChatModel>() {
            @Override
            public void onResponse(Call<UpMyChatModel> call, Response<UpMyChatModel> response) {
                UpMyChatModel data = response.body();
                Log.v(TAG, "uploadImageresponse=" + data);
                if (data.getStatus().equals("true")) {
                    //File dir = new File(getExternalFilesDir(null) + "/" + getString(R.string.app_name) + "/" + getString(R.string.app_name) + "Images/Sent");
                    File dir = new File(StorageManager.getDataRoot() + "/" + getString(R.string.app_name) + "/" + getString(R.string.app_name) + "Images/Sent");

                    if (dir.exists()) {
                        File from = new File(imagePath);
                        File to = new File(dir + "/" + data.getResult().getImage());
                        if (from.exists()) {
                            try {
                                FileUtils.copyFile(from, to);
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }

                        File file = storageManager.getImage("sent", data.getResult().getImage());

                        final int imgSize = ApplicationClass.dpToPx(ChatActivity.this, 170);
                        Log.v("file path", "file path=" + file.getAbsolutePath());

                        Bitmap bitmap = ImageUtils.compressImage(file.getAbsolutePath(), imgSize, imgSize);
                        String imgstatus = storageManager.saveThumbNail(bitmap, data.getResult().getImage());

                        if (mdata.message_type.equals("image")) {
                            if (imgstatus.equals("success")) {
                                dbhelper.updateMessageData(mdata.message_id, Constants.TAG_ATTACHMENT, data.getResult().getImage());
                                dbhelper.updateMessageData(mdata.message_id, Constants.TAG_PROGRESS, "completed");
                                if (messageListAdapter != null) {
                                    for (int i = 0; i < messagesList.size(); i++) {
                                        if (mdata.message_id.equals(messagesList.get(i).message_id)) {
                                            messagesList.get(i).attachment = data.getResult().getImage();
                                            messagesList.get(i).progress = "completed";
                                            messageListAdapter.notifyItemChanged(i);
                                            break;
                                        }
                                    }
                                }
                            }
                            mdata.attachment = data.getResult().getImage();
                            if (results.blockedme == null || !results.blockedme.equals("block")) {
                                emitImage(mdata);
                                // Toast.makeText(ChatActivity.this, data.get(Constants.TAG_MESSAGE), Toast.LENGTH_SHORT).show();
                            }
                        } else if (mdata.message_type.equals("video")) {
                            Log.v("checkChat", "uploadImage-video");
                            if (imgstatus.equals("success")) {
                                mdata.thumbnail = data.getResult().getImage();
                                dbhelper.updateMessageData(mdata.message_id, Constants.TAG_THUMBNAIL, data.getResult().getImage());
                                if (messageListAdapter != null) {
                                    for (int i = 0; i < messagesList.size(); i++) {
                                        if (mdata.message_id.equals(messagesList.get(i).message_id)) {
                                            messagesList.get(i).thumbnail = mdata.thumbnail;
                                            messageListAdapter.notifyItemChanged(i);
                                            break;
                                        }
                                    }
                                }
                            }
                            Intent service = new Intent(ChatActivity.this, FileUploadService.class);
                            Bundle b = new Bundle();
                            b.putSerializable("mdata", mdata);
                            b.putString("filepath", filePath);
                            b.putString("chatType", "chat");
                            service.putExtras(b);
                            startService(service);
                        }
                    }
                } else {
                    dbhelper.updateMessageData(mdata.message_id, Constants.TAG_PROGRESS, "error");
                    if (messageListAdapter != null) {
                        for (int i = 0; i < messagesList.size(); i++) {
                            if (mdata.message_id.equals(messagesList.get(i).message_id)) {
                                messagesList.get(i).progress = "error";
                                messageListAdapter.notifyItemChanged(i);
                                break;
                            }
                        }
                    }
                }
            }

            @Override
            public void onFailure(Call<UpMyChatModel> call, Throwable t) {
                Log.v(TAG, "onFailure=" + "onFailure");
                call.cancel();
                dbhelper.updateMessageData(mdata.message_id, Constants.TAG_PROGRESS, "error");
                if (messageListAdapter != null) {
                    for (int i = 0; i < messagesList.size(); i++) {
                        if (mdata.message_id.equals(messagesList.get(i).message_id)) {
                            messagesList.get(i).progress = "error";
                            messageListAdapter.notifyItemChanged(i);
                            break;
                        }
                    }
                }
            }
        });
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

    public String firstThree(String str) {
        return str.length() < 3 ? str : str.substring(0, 3);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 100) {
            int permissionCamera = ContextCompat.checkSelfPermission(ChatActivity.this,
                    CAMERA);
            int permissionAudio = ContextCompat.checkSelfPermission(ChatActivity.this,
                    RECORD_AUDIO);
            int permissionPhoneState = ContextCompat.checkSelfPermission(ChatActivity.this,
                    READ_PHONE_STATE);
            int permissionWakeLock = ContextCompat.checkSelfPermission(ChatActivity.this,
                    WAKE_LOCK);

            if (permissionCamera == PackageManager.PERMISSION_GRANTED &&
                    permissionAudio == PackageManager.PERMISSION_GRANTED &&
                    permissionWakeLock == PackageManager.PERMISSION_GRANTED &&
                    permissionPhoneState == PackageManager.PERMISSION_GRANTED) {
              /*  Intent video = new Intent(ChatActivity.this, CallActivity.class);
                video.putExtra("from", "send");
                video.putExtra("type", "audio");
                video.putExtra("data", data);
                startActivity(video);*/
            } else {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    if (shouldShowRequestPermissionRationale(CAMERA) &&
                            shouldShowRequestPermissionRationale(WAKE_LOCK) &&
                            shouldShowRequestPermissionRationale(READ_PHONE_STATE) &&
                            shouldShowRequestPermissionRationale(RECORD_AUDIO)) {
                        requestPermission(new String[]{CAMERA, RECORD_AUDIO, WAKE_LOCK, READ_PHONE_STATE}, 100);
                    } else {
//                        openPermissionDialog("Camera, Record Audio");
                        makeToast(getString(R.string.call_permission_error));
                    }
                }
            }
        } else if (requestCode == 101) {
            int permissionCamera = ContextCompat.checkSelfPermission(ChatActivity.this,
                    CAMERA);
            int permissionAudio = ContextCompat.checkSelfPermission(ChatActivity.this,
                    RECORD_AUDIO);
            int permissionPhoneState = ContextCompat.checkSelfPermission(ChatActivity.this,
                    READ_PHONE_STATE);
            int permissionWakeLock = ContextCompat.checkSelfPermission(ChatActivity.this,
                    WAKE_LOCK);

            if (permissionCamera == PackageManager.PERMISSION_GRANTED &&
                    permissionAudio == PackageManager.PERMISSION_GRANTED &&
                    permissionWakeLock == PackageManager.PERMISSION_GRANTED &&
                    permissionPhoneState == PackageManager.PERMISSION_GRANTED) {
               /* Intent video = new Intent(ChatActivity.this, CallActivity.class);
                video.putExtra("from", "send");
                video.putExtra("type", "video");
                video.putExtra("data", data);
                startActivity(video);*/
            } else {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    if (shouldShowRequestPermissionRationale(CAMERA) &&
                            shouldShowRequestPermissionRationale(WAKE_LOCK) &&
                            shouldShowRequestPermissionRationale(READ_PHONE_STATE) &&
                            shouldShowRequestPermissionRationale(RECORD_AUDIO)) {
                        requestPermission(new String[]{CAMERA, RECORD_AUDIO, WAKE_LOCK, READ_PHONE_STATE}, 101);
                    } else {
//                        openPermissionDialog("Camera, Record Audio,Phone State");
                        makeToast(getString(R.string.call_permission_error));
                    }
                }
            }
        } else if (requestCode == 102) {
            int permissionStorage = ContextCompat.checkSelfPermission(ChatActivity.this, WRITE_EXTERNAL_STORAGE);

            if (permissionStorage == PackageManager.PERMISSION_GRANTED) {
                ImagePicker.pickImage(this, getString(R.string.select_your_image));
            } else {
                makeToast(getString(R.string.storage_permission_error));
            }
        } else if (requestCode == 106) {
            int permissionCamera = ContextCompat.checkSelfPermission(ChatActivity.this,
                    CAMERA);
            int permissionStorage = ContextCompat.checkSelfPermission(ChatActivity.this,
                    WRITE_EXTERNAL_STORAGE);

            if (permissionCamera == PackageManager.PERMISSION_GRANTED &&
                    permissionStorage == PackageManager.PERMISSION_GRANTED) {
                if (isNetworkConnected().equals(NOT_CONNECT)) {
                    networkSnack();
                } else if (results.blockedbyme == null || results.blockedbyme.equals("block")) {
                    blockChatConfirmDialog("unblock", "sent");
                } else {
                    ApplicationClass.onShareExternal = true;
                    ImagePicker.pickImageCameraOnly(this, 231);
                }
            } else {
                makeToast(getString(R.string.storage_permission_error));
            }
        } else if (requestCode == 107) {
            int permissionCamera = ContextCompat.checkSelfPermission(ChatActivity.this,
                    CAMERA);
            int permissionStorage = ContextCompat.checkSelfPermission(ChatActivity.this,
                    WRITE_EXTERNAL_STORAGE);

            if (permissionCamera == PackageManager.PERMISSION_GRANTED &&
                    permissionStorage == PackageManager.PERMISSION_GRANTED) {
                if (isNetworkConnected().equals(NOT_CONNECT)) {
                    networkSnack();
                } else if (results.blockedbyme == null || results.blockedbyme.equals("block")) {
                    blockChatConfirmDialog("unblock", "sent");
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
            } else {
                makeToast(getString(R.string.storage_permission_error));
            }
        } else if (requestCode == 108) {
            int permissionStorage = ContextCompat.checkSelfPermission(ChatActivity.this,
                    WRITE_EXTERNAL_STORAGE);

            if (permissionStorage == PackageManager.PERMISSION_GRANTED) {
                if (isNetworkConnected().equals(NOT_CONNECT)) {
                    networkSnack();
                } else if (results.blockedbyme == null || results.blockedbyme.equals("block")) {
                    blockChatConfirmDialog("unblock", "sent");
                } else {
                    FilePickerBuilder.getInstance()
                            .setMaxCount(1)
                            .enableDocSupport(true)
                            .setActivityTitle("Please select document")
//                            .showTabLayout(true)
                            .setActivityTheme(R.style.MainTheme)
                            .pickFile(this, 151);
                }
            } else {
                makeToast(getString(R.string.storage_permission_error));
            }
        } else if (requestCode == 109) {
            int permissionStorage = ContextCompat.checkSelfPermission(ChatActivity.this,
                    WRITE_EXTERNAL_STORAGE);

            if (permissionStorage == PackageManager.PERMISSION_GRANTED) {
                if (isNetworkConnected().equals(NOT_CONNECT)) {
                    networkSnack();
                } else if (results.blockedbyme == null || results.blockedbyme.equals("block")) {
                    blockChatConfirmDialog("unblock", "sent");
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
            } else {
                makeToast(getString(R.string.storage_permission_error));
            }
        } else if (requestCode == 110) {
            int permissionContacts = ContextCompat.checkSelfPermission(ChatActivity.this,
                    READ_CONTACTS);

            if (permissionContacts == PackageManager.PERMISSION_GRANTED) {
                if (isNetworkConnected().equals(NOT_CONNECT)) {
                    networkSnack();
                } else if (results.blockedbyme == null || results.blockedbyme.equals("block")) {
                    blockChatConfirmDialog("unblock", "sent");
                } else {
                    ApplicationClass.onShareExternal = true;
                    Intent intentc = new Intent(Intent.ACTION_PICK, ContactsContract.CommonDataKinds.Phone.CONTENT_URI);
                    startActivityForResult(intentc, 13);
                }
            }
        }
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
    protected void onActivityResult(final int requestCode, final int resultCode, final Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.v(TAG, "onActivityResult");
        if (resultCode == -1 && requestCode == 231) {
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
                    ImageCompression imageCompression = new ImageCompression(ChatActivity.this) {
                        @Override
                        protected void onPostExecute(String imagePath) {
                            try {
                                MessagesData mdata = updateDBList("image", imagePath, "");
                                byte[] bytes = org.apache.commons.io.FileUtils.readFileToByteArray(new File(imagePath));
                                long size = file.length();
                                if (prefData.getLong("mediaUpload", 0) == 0) {
                                    editorData.putLong("mediaUpload", size);
                                } else {
                                    editorData.putLong("mediaUpload", prefData.getLong("mediaUpload", 0) + size);
                                }
                                editorData.apply();
                                editorData.commit();

                                long count = prefData.getLong("sentMesCount", 0);
                                editorData.putLong("sentMesCount", count + 1);
                                editorData.apply();

                                DataStorageModel dataStorageModel = dbhelper.getRecord(userId);

                                dbhelper.addDataStorage(userId,
                                        dataStorageModel.getMessage_count(),
                                        dataStorageModel.getSent_contact(),
                                        dataStorageModel.getSent_location(),
                                        String.valueOf(Long.parseLong(dataStorageModel.getSent_photos()) + 1),
                                        dataStorageModel.getSent_videos(),
                                        dataStorageModel.getSent_aud(),
                                        dataStorageModel.getSent_doc(),
                                        String.valueOf(Long.parseLong(dataStorageModel.getSent_photos_size()) + size),
                                        dataStorageModel.getSent_videos_size(),
                                        dataStorageModel.getSent_aud_size(),
                                        dataStorageModel.getSent_doc_size());

                                uploadImage(bytes, imagePath, mdata, "");
                            } catch (IOException ex) {
                                ex.printStackTrace();
                            }
                        }
                    };
                    imageCompression.execute(filepath);
                } else {
                    Toast.makeText(this, "8:" + getString(R.string.something_wrong), Toast.LENGTH_SHORT).show();
                }
            }
        } else if (resultCode == -1 && requestCode == 150) {
            if (isNetworkConnected().equals(NOT_CONNECT)) {
                networkSnack();
            } else {
                long size = 0;
                int UID = android.os.Process.myUid();

                Long rxBytes = prefData.getLong("callReceiveTotal", getUidRxBytes(UID));
                Long txBytes = prefData.getLong("callSentTotal", getUidRxBytes(UID));

                rxBytesFinal = getUidRxBytes(UID) - rxBytes;
                txBytesFinal = getUidTxBytes(UID) - txBytes;

                pathsAry = new ArrayList<>();
                pathsAry.addAll(data.getParcelableArrayListExtra(FilePickerConst.KEY_SELECTED_MEDIA));
                if (pathsAry.size() > 0) {
                    Log.v(TAG, "File");
                    String filepath = null;
                    try {
                        filepath = ContentUriUtils.INSTANCE.getFilePath(getApplicationContext(), pathsAry.get(0));
                        File file = new File(filepath);
                        size = file.length();
                        if (prefData.getLong("mediaUpload", 0) == 0) {
                            editorData.putLong("mediaUpload", size);
                        } else {
                            editorData.putLong("mediaUpload", prefData.getLong("mediaUpload", 0) + size);
                        }
                        editorData.apply();

                        Log.e("LLLLLL_fileSize: ", humanReadableByteCountSI(prefData.getLong("mediaUpload", 0)));
                    } catch (URISyntaxException e) {
                        e.printStackTrace();
                    }
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
                                    MessagesData mdata = updateDBList("video", imagePath, filepath);
                                    byte[] bytes = org.apache.commons.io.FileUtils.readFileToByteArray(new File(imagePath));

                                    DataStorageModel dataStorageModel = dbhelper.getRecord(userId);

                                    dbhelper.addDataStorage(userId,
                                            dataStorageModel.getMessage_count(),
                                            dataStorageModel.getSent_contact(),
                                            dataStorageModel.getSent_location(),
                                            dataStorageModel.getSent_photos(),
                                            String.valueOf(Long.parseLong(dataStorageModel.getSent_videos()) + 1),
                                            dataStorageModel.getSent_aud(),
                                            dataStorageModel.getSent_doc(),
                                            dataStorageModel.getSent_photos_size(),
                                            String.valueOf(Long.parseLong(dataStorageModel.getSent_videos_size()) + size),
                                            dataStorageModel.getSent_aud_size(),
                                            dataStorageModel.getSent_doc_size());

                                    long count = prefData.getLong("sentMesCount", 0);
                                    editorData.putLong("sentMesCount", count + 1);
                                    editorData.apply();
                                    editorData.commit();

                                    uploadImage(bytes, imagePath, mdata, filepath);
                                }
                            }
                        } catch (IOException ex) {
                            ex.printStackTrace();
                            Toast.makeText(ChatActivity.this, "9:" + getString(R.string.something_wrong), Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        long finalSize = size;
                        ImageCompression imageCompression = new ImageCompression(ChatActivity.this) {
                            @Override
                            protected void onPostExecute(String imagePath) {
                                try {
                                    Log.v("checkChat", "imagepath=" + imagePath);
                                    MessagesData mdata = updateDBList("image", imagePath, "");
                                    byte[] bytes = org.apache.commons.io.FileUtils.readFileToByteArray(new File(imagePath));

                                    long count = prefData.getLong("sentMesCount", 0);
                                    editorData.putLong("sentMesCount", count + 1);
                                    editorData.apply();

                                    DataStorageModel dataStorageModel = dbhelper.getRecord(userId);

                                    dbhelper.addDataStorage(userId,
                                            dataStorageModel.getMessage_count(),
                                            dataStorageModel.getSent_contact(),
                                            dataStorageModel.getSent_location(),
                                            String.valueOf(Long.parseLong(dataStorageModel.getSent_photos()) + 1),
                                            dataStorageModel.getSent_videos(),
                                            dataStorageModel.getSent_aud(),
                                            dataStorageModel.getSent_doc(),
                                            String.valueOf(Long.parseLong(dataStorageModel.getSent_photos_size()) + finalSize),
                                            dataStorageModel.getSent_videos_size(),
                                            dataStorageModel.getSent_aud_size(),
                                            dataStorageModel.getSent_doc_size());

                                    uploadImage(bytes, imagePath, mdata, "");
                                } catch (IOException ex) {
                                    ex.printStackTrace();
                                }
                            }
                        };
                        imageCompression.execute(filepath);
                    }
                } else {
                    Toast.makeText(this, "10:" + getString(R.string.something_wrong), Toast.LENGTH_SHORT).show();
                }
            }
        } else if (resultCode == -1 && requestCode == 151) {
            if (isNetworkConnected().equals(NOT_CONNECT)) {
                networkSnack();
            } else {
                pathsAry = new ArrayList<>();
                pathsAry.addAll(data.getParcelableArrayListExtra(FilePickerConst.KEY_SELECTED_DOCS));
                if (pathsAry.size() > 0) {
                    Log.v(TAG, "File");
                    String filepath = null;
                    try {
                        filepath = ContentUriUtils.INSTANCE.getFilePath(getApplicationContext(), pathsAry.get(0));
                    } catch (URISyntaxException e) {
                        e.printStackTrace();
                    }
                    Log.i(TAG, "selectedImageFile: " + filepath);
                    try {
                        MessagesData mdata = updateDBList("document", "", filepath);
                        Intent service = new Intent(ChatActivity.this, FileUploadService.class);
                        Bundle b = new Bundle();
                        b.putSerializable("mdata", mdata);
                        b.putString("filepath", filepath);
                        b.putString("chatType", "chat");
                        service.putExtras(b);
                        startService(service);

                        File file = new File(filepath);
                        long size = file.length();
                        if (prefData.getLong("mediaUpload", 0) == 0) {
                            editorData.putLong("mediaUpload", size);
                        } else {
                            editorData.putLong("mediaUpload", prefData.getLong("mediaUpload", 0) + size);
                        }
                        Log.e("LLLL_FilePath: ", file.getAbsolutePath());
                        editorData.apply();

                        long count = prefData.getLong("sentMesCount", 0);
                        editorData.putLong("sentMesCount", count + 1);
                        editorData.apply();

                        editorData.commit();

                        DataStorageModel dataStorageModel = dbhelper.getRecord(userId);

                        dbhelper.addDataStorage(userId,
                                dataStorageModel.getMessage_count(),
                                dataStorageModel.getSent_contact(),
                                dataStorageModel.getSent_location(),
                                dataStorageModel.getSent_photos(),
                                dataStorageModel.getSent_videos(),
                                dataStorageModel.getSent_aud(),
                                String.valueOf(Long.parseLong(dataStorageModel.getSent_doc()) + 1),
                                dataStorageModel.getSent_photos_size(),
                                dataStorageModel.getSent_videos_size(),
                                dataStorageModel.getSent_aud_size(),
                                String.valueOf(Long.parseLong(dataStorageModel.getSent_doc_size()) + size));

                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                } else {
                    Toast.makeText(this, "11:" + getString(R.string.something_wrong), Toast.LENGTH_SHORT).show();
                }
            }
        } else if (resultCode == -1 && requestCode == 152) {
            if (isNetworkConnected().equals(NOT_CONNECT)) {
                networkSnack();
            } else {
                pathsAry = new ArrayList<>();
                pathsAry.addAll(data.getParcelableArrayListExtra(FilePickerConst.KEY_SELECTED_DOCS));
                if (pathsAry.size() > 0) {
                    Log.v(TAG, "Audio");
                    String filepath = null;
                    try {
                        filepath = ContentUriUtils.INSTANCE.getFilePath(getApplicationContext(), pathsAry.get(0));
                    } catch (URISyntaxException e) {
                        e.printStackTrace();
                    }
                    Log.i(TAG, "selectedImageFile: " + filepath);
                    try {
                        MessagesData mdata = updateDBList("audio", "", filepath);
                        File file = new File(filepath);
                        long size = file.length();
                        if (prefData.getLong("mediaUpload", 0) == 0) {
                            editorData.putLong("mediaUpload", size);
                        } else {
                            editorData.putLong("mediaUpload", prefData.getLong("mediaUpload", 0) + size);
                        }
                        editorData.apply();

                        long count = prefData.getLong("sentMesCount", 0);
                        editorData.putLong("sentMesCount", count + 1);
                        editorData.apply();

                        editorData.commit();

                        Intent service = new Intent(ChatActivity.this, FileUploadService.class);
                        Bundle b = new Bundle();
                        b.putSerializable("mdata", mdata);
                        b.putString("filepath", filepath);
                        b.putString("chatType", "chat");
                        service.putExtras(b);
                        startService(service);

                        DataStorageModel dataStorageModel = dbhelper.getRecord(userId);

                        dbhelper.addDataStorage(userId,
                                dataStorageModel.getMessage_count(),
                                dataStorageModel.getSent_contact(),
                                dataStorageModel.getSent_location(),
                                dataStorageModel.getSent_photos(),
                                dataStorageModel.getSent_videos(),
                                String.valueOf(Long.parseLong(dataStorageModel.getSent_aud()) + 1),
                                dataStorageModel.getSent_doc(),
                                dataStorageModel.getSent_photos_size(),
                                dataStorageModel.getSent_videos_size(),
                                String.valueOf(Long.parseLong(dataStorageModel.getSent_aud_size()) + size),
                                dataStorageModel.getSent_doc_size());


                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                } else {
                    Toast.makeText(this, "12:" + getString(R.string.something_wrong), Toast.LENGTH_SHORT).show();
                }
            }
        } else if (resultCode == -1 && requestCode == 200) {
            String lat = data.getStringExtra("lat");
            String lon = data.getStringExtra("lon");
            if (isNetworkConnected().equals(NOT_CONNECT)) {
                networkSnack();
            } else {
                DataStorageModel dataStorageModel = dbhelper.getRecord(userId);

                dbhelper.addDataStorage(userId,
                        dataStorageModel.getMessage_count(),
                        dataStorageModel.getSent_contact(),
                        String.valueOf(Long.parseLong(dataStorageModel.getSent_location()) + 1),
                        dataStorageModel.getSent_photos(),
                        dataStorageModel.getSent_videos(),
                        dataStorageModel.getSent_aud(),
                        dataStorageModel.getSent_doc(),
                        dataStorageModel.getSent_photos_size(),
                        dataStorageModel.getSent_videos_size(),
                        dataStorageModel.getSent_aud_size(),
                        dataStorageModel.getSent_doc_size());

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

                        emitContact("contact", name, phoneNo, "");
                    }
                    cursor.close();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else if (resultCode == -1 && requestCode == 234) {
            try {
                Uri uri = data.getData();
                String picturePath = getPath(ChatActivity.this, uri);
                Log.e("LLLLLL_Folder: ", picturePath);

                editor.putString("oldWallpaper", picturePath);
                editor.commit();

                Bitmap bitmap = ImagePicker.getImageFromResult(this, requestCode, resultCode, data);
                String timestamp = String.valueOf(System.currentTimeMillis() / 1000L);
                String imageStatus = storageManager.saveToSdCard(getApplicationContext(), bitmap, "wallpaper", timestamp + ".jpg");
                if (imageStatus.equals("success")) {
                    File file = storageManager.getImage("wallpaper", timestamp + ".jpg");
                    String filepath = file.getAbsolutePath();
                    editor.putString("newWallpaper", timestamp + ".jpg");
                    editor.putInt("solidColorPos", 0);
                    editor.commit();
                    Log.i(TAG, "selectedImageFile: " + filepath);
                    behavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
                    ImageCompression imageCompression = new ImageCompression(ChatActivity.this) {
                        @Override
                        protected void onPostExecute(String imagePath) {
                            try {
                                Uri imageUri = Uri.fromFile(file);
                                Glide.with(ChatActivity.this)
                                        .load(imageUri)
                                        .placeholder(R.drawable.chat_bg)
                                        .into(imgChatBg);
                                byte[] bytes = org.apache.commons.io.FileUtils.readFileToByteArray(new File(imagePath));
//                                uploadImage(bytes);
//                                    uploadImage(new File(imagePath));
                            } catch (IOException ex) {
                                ex.printStackTrace();
                            }
                        }
                    };
                    imageCompression.execute(filepath);
                } else {
                    Toast.makeText(this, "13:" + getString(R.string.something_wrong), Toast.LENGTH_SHORT).show();
                }
            } catch (Exception e) {
                Log.e(TAG, "onActivityResult: " + e.getMessage());
                e.printStackTrace();
            }
        } else if (resultCode == RESULT_OK && requestCode == 556) {
            username.setText(ApplicationClass.getContactName(this, results.phone_no));
        } else if (resultCode == RESULT_OK && requestCode == 222) {
            selectedChatPos.clear();
            messageListAdapter.notifyDataSetChanged();
            chatUserLay.setVisibility(View.VISIBLE);
            forwordLay.setVisibility(View.GONE);
            chatLongPressed = false;
        }
    }

    public static String getPath(Context context, Uri uri) {
        String result = null;
        String[] proj = {MediaStore.Images.Media.DATA};
        Cursor cursor = context.getContentResolver().query(uri, proj, null, null, null);
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                int column_index = cursor.getColumnIndexOrThrow(proj[0]);
                result = cursor.getString(column_index);
            }
            cursor.close();
        }
        if (result == null) {
            result = "Not found";
        }
        return result;
    }

    private boolean isCallServiceRunning(Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }

    @Override
    protected void onResume() {
        super.onResume();
        ApplicationClass.onShareExternal = false;
        audioCallBtn.setOnClickListener(this);
        videoCallBtn.setOnClickListener(this);

        boolean isIncomingCall = SharedPrefsHelper.getInstance().get(Constants.EXTRA_IS_INCOMING_CALL, false);
        if (isCallServiceRunning(CallService.class)) {
            Log.d(TAG, "CallService is running now");
            CallActivity.start(this, isIncomingCall);
        }
        loadUsers();

        if (pref.getBoolean("readReciept", true))
            whileViewChat();
        if (getIntent().getStringExtra("EXTRA_EDITED_PATH") != null) {
            editImagePath = getIntent().getStringExtra("EXTRA_EDITED_PATH");
            userId = pref.getString("sendImageUSerID", "");
            tempUserId = userId;
            if (isNetworkConnected().equals(NOT_CONNECT)) {
                networkSnack();
            } else {
                Log.v(TAG, "File");
                String filepath = null;
                filepath = editImagePath;
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
                                MessagesData mdata = updateDBList("video", imagePath, filepath);
                                byte[] bytes = org.apache.commons.io.FileUtils.readFileToByteArray(new File(imagePath));
                                uploadImage(bytes, imagePath, mdata, filepath);
                            }
                        }
                    } catch (IOException ex) {
                        ex.printStackTrace();
                        Toast.makeText(ChatActivity.this, "14:" + getString(R.string.something_wrong), Toast.LENGTH_SHORT).show();
                    }
                } else {
                    ImageCompression imageCompression = new ImageCompression(ChatActivity.this) {
                        @Override
                        protected void onPostExecute(String imagePath) {
                            try {
                                Log.v("checkChat", "imagepath=" + imagePath);
                                MessagesData mdata = updateDBList("image", imagePath, "");
                                byte[] bytes = org.apache.commons.io.FileUtils.readFileToByteArray(new File(imagePath));
                                uploadImage(bytes, imagePath, mdata, "");
                            } catch (IOException ex) {
                                ex.printStackTrace();
                            }
                        }
                    };
                    imageCompression.execute(filepath);
                }
            }
        } else {
            userId = getIntent().getExtras().getString("user_id");
            tempUserId = userId;
        }
        results = dbhelper.getContactDetail(userId);
        username.setText(ApplicationClass.getContactName(this, results.phone_no));
        if (results.blockedme == null || !results.blockedme.equals("block")) {
            DialogActivity.setProfileImage(dbhelper.getContactDetail(userId), userimage, ChatActivity.this);
            online.setVisibility(View.VISIBLE);
        } else {
            Glide.with(ChatActivity.this).load(R.drawable.person)
                    .apply(RequestOptions.circleCropTransform()
                            .placeholder(R.drawable.person).error(R.drawable.person))
                    .into(userimage);
            online.setVisibility(View.GONE);
        }
        if (results.blockedbyme == null || results.blockedbyme.equals("block")) {
            online.setVisibility(View.GONE);
        }
        SocketConnection.getInstance(this).chatBox(userId);
    }

    @Override
    public void onPause() {
        socketConnection.outChatBox(userId);
        tempUserId = "";
        editText.setError(null);
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (Constants.isChatOpened) {
            Constants.isChatOpened = false;
        }
        SocketConnection.getInstance(this).setChatCallbackListener(null);
//        if (onlineTimer != null) {
//            onlineTimer.cancel();
//        }
    }

    @Override
    public void onBackPressed() {
        if (dialog != null && dialog.isShowing()) {
            dialog.dismiss();
        } else if (dialog1 != null && dialog1.isShowing()) {
            dialog1.dismiss();
        } else if (selectedChatPos.size() > 0) {
            selectedChatPos.clear();
            messageListAdapter.notifyDataSetChanged();
            chatUserLay.setVisibility(View.VISIBLE);
            forwordLay.setVisibility(View.GONE);
            chatLongPressed = false;
        } else {
            if (bottomSheetBehavior != null && bottomSheetBehavior.getState() == BottomSheetBehavior.STATE_EXPANDED) {
                bottomSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);
            } else if (behavior != null && behavior.getState() == BottomSheetBehavior.STATE_EXPANDED) {
                behavior.setState(BottomSheetBehavior.STATE_HIDDEN);
            } else {
                if (isFromNotification) {
                    Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                }
                finish();
            }
        }
    }

    public Map<String, String> getNotifications() {
        RingtoneManager manager = new RingtoneManager(this);
        manager.setType(RingtoneManager.TYPE_NOTIFICATION);
        Cursor cursor = manager.getCursor();

        Map<String, String> list = new HashMap<>();
        while (cursor.moveToNext()) {
            String notificationTitle = cursor.getString(RingtoneManager.TITLE_COLUMN_INDEX);
            String notificationUri = cursor.getString(RingtoneManager.URI_COLUMN_INDEX);

            list.put(notificationTitle, notificationUri);
        }

        return list;
    }

    public ArrayList<String> getNotificationSounds() {
        RingtoneManager manager = new RingtoneManager(this);
        manager.setType(RingtoneManager.TYPE_NOTIFICATION);
        Cursor cursor = manager.getCursor();

        ArrayList<String> list = new ArrayList<>();
        while (cursor.moveToNext()) {
            String id = cursor.getString(RingtoneManager.ID_COLUMN_INDEX);
            String uri = cursor.getString(RingtoneManager.URI_COLUMN_INDEX);
            String name = cursor.getString(RingtoneManager.TITLE_COLUMN_INDEX);

            list.add(uri + "/" + id + "     " + name);
        }

        return list;
    }

    private void getMuteNotification() {
        String[] backup_time = {"8 hours", "1 week", "1 year"};

        dialog = new Dialog(ChatActivity.this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        dialog.setContentView(R.layout.dialoge_mute_noti);
        dialog.getWindow().setLayout(getResources().getDisplayMetrics().widthPixels * 90 / 100, ViewGroup.LayoutParams.WRAP_CONTENT);
        dialog.setCancelable(true);
        dialog.setCanceledOnTouchOutside(true);

        RadioGroup radioGrp = dialog.findViewById(R.id.radioGroup);
        TextView tv_cancel = dialog.findViewById(R.id.tv_cancel);
        TextView tv_ok = dialog.findViewById(R.id.tv_ok);
        TextView title = dialog.findViewById(R.id.title);

        tv_ok.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isNetworkConnected().equals(NOT_CONNECT)) {
                    networkSnack();
                    dialog.dismiss();
                } else {
                    int checkedRadioButtonId = radioGrp.getCheckedRadioButtonId();
                    dbhelper.updateMuteUser(userId, "true");
                    RadioButton radioBtn = (RadioButton) dialog.findViewById(checkedRadioButtonId);
                    editor.putString("mutenotification", String.valueOf(radioBtn.getText()));
                    editor.commit();

                    JSONObject jsonObject = new JSONObject();
                    JSONArray jsonArray = new JSONArray();
                    jsonArray.put(Integer.parseInt(userId));
                    JSONArray jsonArray1 = new JSONArray();
                    try {
                        jsonObject.put("userId", pref.getString("userId", ""));
                        jsonObject.put("friendIds", jsonArray);
                        jsonObject.put("groupIds", jsonArray1);
                        jsonObject.put("mute", 1);
                        jsonObject.put("duration", String.valueOf(radioBtn.getText()));

                        Log.e("LLLLLL_JSONOBJ: ", jsonObject.toString());
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                    AndroidNetworking.post(BASE_URL + API_VERSION + MUTE_NOTI)
                            .addJSONObjectBody(jsonObject)
                            .setPriority(Priority.IMMEDIATE)
                            .build()
                            .getAsJSONObject(new JSONObjectRequestListener() {
                                @Override
                                public void onResponse(JSONObject response) {
                                    Log.e("LLLLL_Notifi: ", response.toString());
                                    try {
                                        if (response.getBoolean("status")) {
                                            Toast.makeText(ChatActivity.this, response.getString("msg"), Toast.LENGTH_SHORT).show();
                                        }
                                    } catch (JSONException e) {
                                        Log.e("LLLLLL_JSONEX: ", e.toString());
                                        e.printStackTrace();
                                    }
                                }

                                @Override
                                public void onError(ANError anError) {
                                    Log.e("LLLLL_API: ", anError.getErrorBody());
                                }
                            });

                    dialog.dismiss();
                }
            }
        });

        title.setText("Mute notification for...");

        tv_cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });

        for (int i = 0; i < backup_time.length; i++) {
            RadioButton radioButton = new RadioButton(this);
            radioButton.setPadding(30, 30, 7, 30);
            radioButton.setText(backup_time[i]);
            radioButton.setId(i);
            radioGrp.addView(radioButton);

            if (backup_time[i].equals(pref.getString("mutenotification", ""))) {
                radioButton.setChecked(true);
            } else {
                if (radioButton.getId() == 0) {
                    radioButton.setChecked(true);
                }
            }
        }

        //set listener to radio button group
        radioGrp.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                Toast.makeText(ChatActivity.this, pref.getString("mutenotification", ""), Toast.LENGTH_SHORT).show();
            }
        });

        dialog.show();

    }

    private void getSolidColorDialoge() {
        dialog1 = new Dialog(ChatActivity.this, android.R.style.Theme_Light_NoTitleBar_Fullscreen);
        dialog1.setContentView(R.layout.dialoge_solid_color);
        dialog1.getWindow().setBackgroundDrawableResource(android.R.color.white);
        dialog1.getWindow().setLayout(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);
        dialog1.show();

        ImageView imgBack = dialog1.findViewById(R.id.img_back);
        RecyclerView rvSolidColor = dialog1.findViewById(R.id.rv_solid_color);

        rvSolidColor.setLayoutManager(new GridLayoutManager(ChatActivity.this, 3));
        rvSolidColor.setAdapter(solidColorAdapter);

        imgBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (dialog1.isShowing() && dialog1 != null)
                    dialog1.dismiss();
            }
        });
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.send:
                if (isNetworkConnected().equals(NOT_CONNECT)) {
                    networkSnack();
                } else if (results.blockedbyme == null || results.blockedbyme.equals("block")) {
                    blockChatConfirmDialog("unblock", "sent");
                } else {
                    if (editText.getText().toString().trim().length() > 0) {
                        int UID = android.os.Process.myUid();

                        Long txBytes = prefData.getLong("MesSentTotal", getUidTxBytes(UID));

                        txMesBytesFinal = getUidTxBytes(UID) - txBytes;

                        Log.e("LLLLL_Mes_Size: ", "      " + humanReadableByteCountSI(txMesBytesFinal));

                        DataStorageModel dataStorageModel = dbhelper.getRecord(userId);
                        if (dataStorageModel.getData_id() != null)
                            dbhelper.addDataStorage(userId,
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
                        String chatId = GetSet.getUserId() + userId;
                        RandomString randomString = new RandomString(10);
                        String messageId = GetSet.getUserId() + randomString.nextString();
                        try {
                            if (results.blockedme == null || !results.blockedme.equals("block")) {
//                                JSONObject jobj = new JSONObject();
                                JSONObject message = new JSONObject();
                                message.put(Constants.TAG_USER_ID, GetSet.getUserId());
                                message.put(Constants.TAG_USER_NAME, GetSet.getUserName());
                                message.put(Constants.TAG_MESSAGE_TYPE, "text");
                                message.put(Constants.TAG_MESSAGE, textMsg);
                                message.put(Constants.TAG_CHAT_TIME, unixStamp);
                                message.put(Constants.TAG_CHAT_ID, chatId);
                                message.put(Constants.TAG_MESSAGE_ID, messageId);
                                message.put(TAG_FRIENDID, userId);
                                message.put(Constants.TAG_SENDER_ID, GetSet.getUserId());
                                message.put(Constants.TAG_CHAT_TYPE, Constants.TAG_SINGLE);
                                message.put(Constants.TAG_REPLY_TO, msgReplyTo);
                                message.put(Constants.TAG_TO_GROUP_ID, groupReply);
//                                jobj.put(Constants.TAG_SENDER_ID, GetSet.getUserId());
//                                jobj.put(Constants.TAG_RECEIVER_ID, userId);
//                                jobj.put("message_data", message);
                                Log.v("startchat", "startchat=" + message);
                                socketConnection.startChat(message);
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
                                    "", unixStamp, userId, GetSet.getUserId(), "", "", msgReplyTo, groupReply);

                            dbhelper.addRecentMessages(chatId, userId, messageId, unixStamp, "0");
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
                        data.reply_to = msgReplyTo;
                        data.groupId = groupReply;
                        messagesList.add(0, data);
                        messageListAdapter.notifyItemInserted(0);
                        recyclerView.smoothScrollToPosition(0);
                        editText.setText("");
                        btnCancelReply.callOnClick();
                    } else {
                        editText.setError(getString(R.string.please_enter_your_message));
                    }
                }
                break;
            case R.id.backbtn:
                if (dialog1 != null && dialog1.isShowing())
                    dialog1.dismiss();
                else
                    onBackPressed();
                break;
            case R.id.tv_gallary:
                if (ContextCompat.checkSelfPermission(this, WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(this, new String[]{WRITE_EXTERNAL_STORAGE}, 101);
                } else {
                    ImagePicker.pickImage(this, getString(R.string.select_your_image));
                }
                break;
            case R.id.tv_solid_color:
                if (behavior != null) {
                    behavior.setState(BottomSheetBehavior.STATE_HIDDEN);
                }
                getSolidColorDialoge();
                break;
            case R.id.optionbtn:
                results = dbhelper.getContactDetail(userId);
                HashMap<String, String> map = ApplicationClass.getContactrNot(this, results.phone_no);
                Display display = this.getWindowManager().getDefaultDisplay();
                final ArrayList<String> values = new ArrayList<>();
                if (results.mute_notification.equals("true")) {
                    values.add(getString(R.string.unmute_notification));
                } else {
                    values.add(getString(R.string.mute_notification));
                }
                if (results.blockedbyme == null || results.blockedbyme.equals("block")) {
                    values.add(getString(R.string.unblock));
                } else {
                    values.add(getString(R.string.block));
                }
                values.add(getString(R.string.clear_chat));
                if (results.favourited.equals("true")) {
                    values.add(getString(R.string.remove_favourite));
                } else {
                    values.add(getString(R.string.mark_favourite));
                }
                values.add("Export Chat");
                values.add("Wallpaper");
                values.add("Report");
                if (map.get("isAlready").equals("false")) {
                    values.add(getString(R.string.add_contact));
                }

                final ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
                        R.layout.option_item, android.R.id.text1, values);
                LayoutInflater layoutInflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                View layout = layoutInflater.inflate(R.layout.option_layout, null);
                layout.setAnimation(AnimationUtils.loadAnimation(this, R.anim.grow_from_topright_to_bottomleft));
                final PopupWindow popup = new PopupWindow(ChatActivity.this);
                popup.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                popup.setContentView(layout);
                popup.setWidth(display.getWidth() * 50 / 100);
                popup.setHeight(ViewGroup.LayoutParams.WRAP_CONTENT);
                popup.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_NOTHING);
                popup.setFocusable(true);
                popup.showAtLocation(mainLay, Gravity.TOP | Gravity.RIGHT, ApplicationClass.dpToPx(this, 10), ApplicationClass.dpToPx(this, 60));

                final ListView lv = layout.findViewById(R.id.listView);
                lv.setAdapter(adapter);
                popup.showAsDropDown(v);

                lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {

                    @Override
                    public void onItemClick(AdapterView<?> parent, View view,
                                            int position, long id) {
                        popup.dismiss();
                        if (position == 0) {
                            if (isNetworkConnected().equals(NOT_CONNECT)) {
                                networkSnack();
                            } else {
                                if (values.get(position).equalsIgnoreCase(getString(R.string.mute_notification))) {
                                    getMuteNotification();
                                    Log.e("LLLLLLL_Nostif_Sound: ", getNotificationSounds().toString());
                                    values.set(position, getString(R.string.unmute_notification));
                                } else {
                                    if (isNetworkConnected().equals(NOT_CONNECT)) {
                                        networkSnack();
                                        dialog.dismiss();
                                    } else {
                                        JSONObject jsonObject = new JSONObject();
                                        try {
                                            JSONArray jsonArray = new JSONArray();
                                            jsonArray.put(Integer.parseInt(userId));
                                            JSONArray jsonArray1 = new JSONArray();
                                            jsonObject.put("userId", pref.getString("userId", ""));
                                            jsonObject.put("friendIds", jsonArray);
                                            jsonObject.put("groupIds", jsonArray1);
                                            jsonObject.put("mute", 0);
                                            jsonObject.put("duration", "");

                                            Log.e("LLLLLL_JSONOBJ: ", jsonObject.toString());

                                            AndroidNetworking.post(BASE_URL + API_VERSION + MUTE_NOTI)
                                                    .addJSONObjectBody(jsonObject)
                                                    .setPriority(Priority.IMMEDIATE)
                                                    .build()
                                                    .getAsJSONObject(new JSONObjectRequestListener() {
                                                        @Override
                                                        public void onResponse(JSONObject response) {
                                                            Log.e("LLLLL_Notifi: ", response.toString());
                                                            try {
                                                                if (response.getBoolean("status")) {
                                                                    Toast.makeText(ChatActivity.this, response.getString("msg"), Toast.LENGTH_SHORT).show();
                                                                }
                                                            } catch (JSONException e) {
                                                                Log.e("LLLLLL_JSONEX: ", e.toString());
                                                                e.printStackTrace();
                                                            }
                                                        }

                                                        @Override
                                                        public void onError(ANError anError) {
                                                            Log.e("LLLLL_API1: ", anError.getErrorBody());
                                                        }
                                                    });

                                        } catch (JSONException e) {
                                            e.printStackTrace();
                                        }
                                        dbhelper.updateMuteUser(userId, "");
                                        values.set(position, getString(R.string.mute_notification));
                                    }
                                }
                                results = dbhelper.getContactDetail(userId);
                                adapter.notifyDataSetChanged();
                            }
                        } else if (position == 1) {
                            if (isNetworkConnected().equals(NOT_CONNECT)) {
                                networkSnack();
                            } else {
                                String type = "";
                                if (results.blockedbyme == null || results.blockedbyme.equals("block")) {
                                    type = "unblock";
                                } else {
                                    type = "block";
                                }
                                blockChatConfirmDialog(type, "popup");
                            }
                        } else if (position == 2) {
                            deleteChatConfirmDialog();
                        } else if (position == 3) {
                            if (results.favourited.equals("true")) {
                                dbhelper.updateFavUser(userId, "false");
                                Toast.makeText(ChatActivity.this, getString(R.string.removed_favourites), Toast.LENGTH_SHORT).show();
                            } else {
                                dbhelper.updateFavUser(userId, "true");
                                Toast.makeText(ChatActivity.this, getString(R.string.marked_favourite), Toast.LENGTH_SHORT).show();
                            }
                        } else if (position == 4) {
                            new LongOperation().execute();
                        } else if (position == 5) {
                            File prefsdir = new File(getApplicationInfo().dataDir, "shared_prefs/SavedPref.xml");
                            Log.e("LLLLLL_XML: ", prefsdir.getAbsolutePath());
                            behavior.setState(BottomSheetBehavior.STATE_EXPANDED);
                        } else if (position == 6) {
                            reportUser(userId);
                        } else if (position == 7) {
                            Intent intent = new Intent(Intent.ACTION_INSERT, ContactsContract.Contacts.CONTENT_URI);
                            intent.setType(ContactsContract.RawContacts.CONTENT_TYPE);
                            intent.putExtra("finishActivityOnSaveCompleted", true);
                            intent.putExtra(ContactsContract.Intents.Insert.PHONE, results.phone_no);
                            intent.putExtra(ContactsContract.Intents.Insert.NAME, "");
                            startActivityForResult(intent, 556);
                        }
                    }
                });
                break;
            case R.id.attachbtn:
                TransitionManager.beginDelayedTransition(bottomLay);
                visible = !visible;
                attachmentsLay.setVisibility(visible ? View.VISIBLE : View.GONE);
                break;
            case R.id.userImg:
                break;
            case R.id.cameraBtn:
                if (ContextCompat.checkSelfPermission(this, WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED ||
                        ContextCompat.checkSelfPermission(this, CAMERA) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(this, new String[]{WRITE_EXTERNAL_STORAGE, CAMERA}, 107);
                } else if (isNetworkConnected().equals(NOT_CONNECT)) {
                    networkSnack();
                } else if (results.blockedbyme == null || results.blockedbyme.equals("block")) {
                    blockChatConfirmDialog("unblock", "sent");
                } else {
                    ApplicationClass.onShareExternal = true;
                    ImagePicker.pickImageCameraOnly(this, 231);
                }
                break;
            case R.id.galleryBtn:
                if (ContextCompat.checkSelfPermission(this, WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED ||
                        ContextCompat.checkSelfPermission(this, CAMERA) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(this, new String[]{WRITE_EXTERNAL_STORAGE, CAMERA}, 107);
                } else if (isNetworkConnected().equals(NOT_CONNECT)) {
                    networkSnack();
                } else if (results.blockedbyme == null || results.blockedbyme.equals("block")) {
                    blockChatConfirmDialog("unblock", "sent");
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
                if (ContextCompat.checkSelfPermission(this, WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(this, new String[]{WRITE_EXTERNAL_STORAGE}, 108);
                } else {
                    if (isNetworkConnected().equals(NOT_CONNECT)) {
                        networkSnack();
                    } else if (results.blockedbyme == null || results.blockedbyme.equals("block")) {
                        blockChatConfirmDialog("unblock", "sent");
                    } else {
                        FilePickerBuilder.getInstance()
                                .setMaxCount(1)
                                .enableDocSupport(true)
                                .setActivityTitle(getString(R.string.please_select_document))
//                                .showTabLayout(true)
                                .setActivityTheme(R.style.MainTheme)
                                .pickFile(this, 151);
                    }
                }
                break;
            case R.id.audioBtn:
                if (ContextCompat.checkSelfPermission(this, WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(this, new String[]{WRITE_EXTERNAL_STORAGE}, 109);
                } else {
                    Intent intent_upload = new Intent();
                    intent_upload.setType("audio/*");
                    intent_upload.setAction(Intent.ACTION_GET_CONTENT);
                    startActivityForResult(intent_upload, 1);
                    if (isNetworkConnected().equals(NOT_CONNECT)) {
                        networkSnack();
                    } else if (results.blockedbyme == null || results.blockedbyme.equals("block")) {
                        blockChatConfirmDialog("unblock", "sent");
                    } else {
                        String[] aud = {".mp3", ".wav", ".flac", ".3gp", ".ogg"};
                        FilePickerBuilder.getInstance()
                                .setMaxCount(1)
                                .setActivityTheme(R.style.MainTheme)
                                .setActivityTitle(getString(R.string.please_select_audio))
                                .addFileSupport("MP3", aud)
                                .enableDocSupport(false)
                                .enableSelectAll(true)
//                                .showTabLayout(false)
                                .sortDocumentsBy(SortingTypes.name)
//                                .withOrientation(Orientation.UNSPECIFIED)
                                .pickFile(this, 152);
                    }
                }
                break;
            case R.id.locationBtn:
                if (isNetworkConnected().equals(NOT_CONNECT)) {
                    networkSnack();
                } else if (results.blockedbyme == null || results.blockedbyme.equals("block")) {
                    blockChatConfirmDialog("unblock", "sent");
                } else {
                    Intent location = new Intent(this, LocationActivity.class);
                    location.putExtra("from", "share");
                    startActivityForResult(location, 200);
                }
                break;
            case R.id.contactBtn:
                if (ContextCompat.checkSelfPermission(this, READ_CONTACTS) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(this, new String[]{READ_CONTACTS}, 110);
                } else {
                    if (isNetworkConnected().equals(NOT_CONNECT)) {
                        networkSnack();
                    } else if (results.blockedbyme == null || results.blockedbyme.equals("block")) {
                        blockChatConfirmDialog("unblock", "sent");
                    } else {
                        ApplicationClass.onShareExternal = true;
                        Intent intentc = new Intent(Intent.ACTION_PICK, ContactsContract.CommonDataKinds.Phone.CONTENT_URI);
                        startActivityForResult(intentc, 13);
                    }
                }
                break;
            case R.id.closeBtn:
                if (bottomSheetBehavior != null) {
                    bottomSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);
                }
                break;
            case R.id.audioCallBtn:
                if (ContextCompat.checkSelfPermission(ChatActivity.this, CAMERA) != PackageManager.PERMISSION_GRANTED ||
                        ContextCompat.checkSelfPermission(ChatActivity.this, RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED ||
                        ContextCompat.checkSelfPermission(ChatActivity.this, WAKE_LOCK) != PackageManager.PERMISSION_GRANTED ||
                        ContextCompat.checkSelfPermission(ChatActivity.this, READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(ChatActivity.this, new String[]{CAMERA, RECORD_AUDIO, WAKE_LOCK, READ_PHONE_STATE}, 100);
                } else if (results.blockedbyme == null || results.blockedbyme.equals("block")) {
                    blockChatConfirmDialog("unblock", "sent");
                } else {
                    if (checkIsLoggedInChat()) {
                        startCall(false);
                    }
                }
                break;
            case R.id.videoCallBtn:
                if (ContextCompat.checkSelfPermission(ChatActivity.this, CAMERA) != PackageManager.PERMISSION_GRANTED ||
                        ContextCompat.checkSelfPermission(ChatActivity.this, RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED ||
                        ContextCompat.checkSelfPermission(ChatActivity.this, WAKE_LOCK) != PackageManager.PERMISSION_GRANTED ||
                        ContextCompat.checkSelfPermission(ChatActivity.this, READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(ChatActivity.this, new String[]{CAMERA, RECORD_AUDIO, WAKE_LOCK, READ_PHONE_STATE}, 101);
                } else if (results.blockedbyme == null || results.blockedbyme.equals("block")) {
                    blockChatConfirmDialog("unblock", "sent");
                } else {
                    if (checkIsLoggedInChat()) {
                        startCall(true);
                    }
                }
                break;
            case R.id.chatUserLay:
                Intent profile = new Intent(ChatActivity.this, ProfileActivity.class);
                profile.putExtra(Constants.TAG_USER_ID, userId);
                startActivity(profile);
                break;
            case R.id.forwordBtn:
                if (isNetworkConnected().equals(NOT_CONNECT)) {
                    networkSnack();
                } else {
                    Intent f = new Intent(ChatActivity.this, ForwardActivity.class);
                    f.putExtra("from", "chat");
                    f.putExtra("data", selectedChatPos);
                    startActivityForResult(f, 222);
                }
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
                deleteMessageConfirmDialog(selectedChatPos);
                break;
            case R.id.starBtn:
                Log.e("LLLLL_CHatID: ", selectedChatPos.get(0).getChat_id());
                Log.e("LLLLL_MessID: ", selectedChatPos.get(0).getMessage_id());
                Log.e("LLLLL_Mess: ", selectedChatPos.get(0).getMessage());
                addStarMes(selectedChatPos.get(0).getMessage_id());
                break;
        }
    }

    private void reportUser(String report_id) {
        dialog = new Dialog(ChatActivity.this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        dialog.setContentView(R.layout.dialoge_mute_noti);
        dialog.getWindow().setLayout(getResources().getDisplayMetrics().widthPixels * 90 / 100, ViewGroup.LayoutParams.WRAP_CONTENT);
        dialog.setCancelable(true);
        dialog.setCanceledOnTouchOutside(true);

        RadioGroup radioGrp = dialog.findViewById(R.id.radioGroup);
        TextView tv_cancel = dialog.findViewById(R.id.tv_cancel);
        TextView tv_ok = dialog.findViewById(R.id.tv_ok);
        TextView title = dialog.findViewById(R.id.title);
        TextView tv_check = dialog.findViewById(R.id.tv_check);
        CheckBox checkbox = dialog.findViewById(R.id.checkbox);

        title.setText("Report this contact to " + getString(R.string.app_name));
        tv_check.setText("Block contact and delete this chat's messages");

        radioGrp.setVisibility(View.GONE);

        tv_ok.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isNetworkConnected().equals(NOT_CONNECT)) {
                    networkSnack();
                    dialog.dismiss();
                } else {
                    Log.e("LLLLL_PAram: ", pref.getString("userId", "") + "    " + report_id);
                    AndroidNetworking.get(BASE_URL + API_VERSION + REPORT_USER)
                            .addQueryParameter("user_id", pref.getString("userId", ""))
                            .addQueryParameter("report_id", report_id)
                            .setPriority(Priority.IMMEDIATE)
                            .build()
                            .getAsJSONObject(new JSONObjectRequestListener() {
                                @Override
                                public void onResponse(JSONObject response) {
                                    Log.e("LLLLL_Notifi: ", response.toString());
                                    try {
                                        if (response.getBoolean("status")) {
                                            Toast.makeText(ChatActivity.this, response.getString("msg"), Toast.LENGTH_SHORT).show();
                                            if (checkbox.isChecked()) {
                                                try {
                                                    JSONObject jsonObject = new JSONObject();
                                                    jsonObject.put(Constants.TAG_SENDER_ID, GetSet.getUserId());
                                                    jsonObject.put(Constants.TAG_RECEIVER_ID, userId);
                                                    jsonObject.put(Constants.TAG_TYPE, "block");
                                                    Log.v("block", "block=" + jsonObject);
                                                    socketConnection.block(jsonObject);
                                                    dbhelper.updateBlockStatus(userId, Constants.TAG_BLOCKED_BYME, "block");
                                                    results = dbhelper.getContactDetail(userId);
                                                    online.setVisibility(View.GONE);

                                                    dbhelper.deleteAllChats(GetSet.getUserId() + userId);
                                                    messagesList.clear();
                                                    messageListAdapter.notifyDataSetChanged();

                                                } catch (JSONException e) {
                                                    e.printStackTrace();
                                                }
                                            }
                                        }
                                    } catch (JSONException e) {
                                        Log.e("LLLLLL_JSONEX: ", e.toString());
                                        e.printStackTrace();
                                    }
                                }

                                @Override
                                public void onError(ANError anError) {
                                    Log.e("LLLLL_API: ", anError.getErrorBody());
                                }
                            });

                    dialog.dismiss();
                }
            }
        });

        title.setText("Mute notification for...");

        tv_cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });


        dialog.show();

    }

    private void requestPermission(String[] permissions, int requestCode) {
        ActivityCompat.requestPermissions(ChatActivity.this, permissions, requestCode);
    }

    public void generateNoteOnSD(Context context) {
        try {
            File root = new File(Environment.getExternalStorageDirectory(), "topzi/topzi doccuments");
            if (!root.exists()) {
                root.mkdirs();
            }
            String File_name = ApplicationClass.getContactName(this, results.phone_no) + ".txt";

            File gpxfile = new File(root, "Topzi chat with " + File_name);
            FileWriter writer = new FileWriter(gpxfile);

            ArrayList<MessagesData> mesgData = new ArrayList<>(messagesList);
            Collections.reverse(mesgData);
            for (int i = 0; i < mesgData.size(); i++) {
                MessagesData messagesData = mesgData.get(i);
                if (messagesData.message != null) {
                    Log.e("LLLLL_Name: ", messagesData.contact_name + "    " + messagesData.contact_phone_no);
                    if (dbhelper.getContactPhone(messagesData.sender_id).equals(""))
                        writer.append(ApplicationClass.getDateTime(Long.parseLong(messagesData.getChat_time()))).append(" - ").append(ApplicationClass.getContactName(this, pref.getString("phoneNumber", null))).append(": ").append(messagesData.getMessage()).append("\n");
                    else {
                        if (!messagesData.sender_id.equals(pref.getString("userId", "")))
                            writer.append(ApplicationClass.getDateTime(Long.parseLong(messagesData.getChat_time()))).append(" - ").append(ApplicationClass.getContactName(this, dbhelper.getContactPhone(messagesData.sender_id))).append(": ").append(messagesData.getMessage()).append("\n");
                        else
                            writer.append(ApplicationClass.getDateTime(Long.parseLong(messagesData.getChat_time()))).append(" - ").append("You").append(": ").append(messagesData.getMessage()).append("\n");
                    }
                }
            }

            writer.flush();
            writer.close();

            final Uri data = FileProvider.getUriForFile(ChatActivity.this, "com.topzi.chat.provider", gpxfile);
            Intent sharingIntent = new Intent(Intent.ACTION_SEND);
            sharingIntent.setType("text/*");
            sharingIntent.putExtra(Intent.EXTRA_STREAM, data);
            startActivity(Intent.createChooser(sharingIntent, "share file with"));

        } catch (IOException e) {
            Log.e("LLLLLL_Error: ", Objects.requireNonNull(e.getMessage()));
            e.printStackTrace();
        }
    }

    private final class LongOperation extends AsyncTask<Void, Void, String> {

        ProgressDialog progressDialog;

        public LongOperation() {
            this.progressDialog = new ProgressDialog(ChatActivity.this);
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressDialog.setMessage("Please Wait...");
            progressDialog.show();
        }

        @Override
        protected String doInBackground(Void... params) {
            generateNoteOnSD(ChatActivity.this);
            return "Executed";
        }

        @Override
        protected void onPostExecute(String result) {
            if (progressDialog.isShowing() && progressDialog != null)
                progressDialog.dismiss();

        }
    }

    private void openPermissionDialog(String permissionList) {
        permissionDialog = new Dialog(ChatActivity.this);
        permissionDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        permissionDialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        permissionDialog.setContentView(R.layout.default_popup);
        permissionDialog.getWindow().setLayout(getResources().getDisplayMetrics().widthPixels * 85 / 100, ViewGroup.LayoutParams.WRAP_CONTENT);
        permissionDialog.setCancelable(false);
        permissionDialog.setCanceledOnTouchOutside(false);

        TextView title = permissionDialog.findViewById(R.id.title);
        TextView yes = permissionDialog.findViewById(R.id.yes);
        TextView no = permissionDialog.findViewById(R.id.no);
        title.setText("This app requires " + permissionList + " permissions to access the features. Please turn on");
        yes.setText(R.string.grant);
        no.setText(R.string.nope);

        yes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                permissionDialog.dismiss();
                Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                Uri uri = Uri.fromParts("package", getPackageName(), null);
                intent.setData(uri);
                startActivityForResult(intent, 100);
            }
        });

        no.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (permissionDialog.isShowing())
                    permissionDialog.dismiss();
            }
        });
        permissionDialog.show();
    }

    public class SolidColorAdapter extends RecyclerView.Adapter<SolidColorAdapter.MyClassView> {

        @NonNull
        @Override
        public SolidColorAdapter.MyClassView onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_solid_color, parent, false);
            return new MyClassView(view);
        }

        @Override
        public void onBindViewHolder(@NonNull SolidColorAdapter.MyClassView holder, int position) {

            Integer str = solidWallpaper.get(position);
            Log.e("LLLLLL_solid", String.valueOf(str));
            holder.imgSolidColor.setColorFilter(getResources().getColor(str), PorterDuff.Mode.SRC_IN);
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    editor.putInt("solidColorPos", position + 1);
                    editor.putString("oldWallpaper", "");
                    editor.putString("newWallpaper", "");
                    editor.commit();
                    imgChatBg.setImageDrawable(null);
                    imgChatBg.setBackgroundColor(getResources().getColor(str));
                    if (dialog1.isShowing() && dialog1 != null)
                        dialog1.dismiss();
                }
            });
        }

        @Override
        public int getItemCount() {
            return solidWallpaper.size();
        }

        public class MyClassView extends RecyclerView.ViewHolder {
            ImageView imgSolidColor;

            public MyClassView(@NonNull View itemView) {
                super(itemView);

                imgSolidColor = itemView.findViewById(R.id.img_solid_Color);
            }
        }
    }

    private void addStarMes(String chatID) {
        Log.e("LLLLL_ID: ", pref.getString("userId", ""));
        AndroidNetworking.post(BASE_URL + API_VERSION + STAR_MSG)
                .addBodyParameter("userId", pref.getString("userId", ""))
                .addBodyParameter("chatId", chatID)
                .addBodyParameter("star", String.valueOf(0))
                .setPriority(Priority.IMMEDIATE)
                .build()
                .getAsJSONObject(new JSONObjectRequestListener() {
                    @Override
                    public void onResponse(JSONObject response) {
                        Log.v("LLLLL_Star: ", response.toString());
                    }

                    @Override
                    public void onError(ANError anError) {
                        Log.e("LLLLLL_Error_Stra: ", anError.getErrorBody());
                    }
                });
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
            LoginService.start(this, qbUser);
        }
    }

    private void startCall(boolean isVideoCall) {
        Log.d(TAG, "Starting Call");

        if (Constants.qbUsersList.size() > Constants.MAX_OPPONENTS_COUNT) {
            ToastUtils.longToast(String.format(getString(R.string.error_max_opponents_count),
                    Constants.MAX_OPPONENTS_COUNT));
            return;
        }

        ArrayList<Integer> opponentsList = CollectionsUtils.getIdsSelectedOpponents(Constants.qbUsersList);
        QBRTCTypes.QBConferenceType conferenceType = isVideoCall
                ? QBRTCTypes.QBConferenceType.QB_CONFERENCE_TYPE_VIDEO
                : QBRTCTypes.QBConferenceType.QB_CONFERENCE_TYPE_AUDIO;
        Log.d(TAG, "conferenceType = " + conferenceType);

        QBRTCClient qbrtcClient = QBRTCClient.getInstance(getApplicationContext());
        QBRTCSession newQbRtcSession = qbrtcClient.createNewSessionWithOpponents(opponentsList, conferenceType);
        WebRtcSessionManager.getInstance(this).setCurrentSession(newQbRtcSession);
        PushNotificationSender.sendPushMessage(opponentsList, GetSet.getUserName());
        CallActivity.start(this, false);
    }

    private void loadUsers() {
        ArrayList<GenericQueryRule> rules = new ArrayList<>();
        rules.add(new GenericQueryRule(ORDER_RULE, ORDER_DESC_UPDATED));

        QBPagedRequestBuilder qbPagedRequestBuilder = new QBPagedRequestBuilder();
        qbPagedRequestBuilder.setRules(rules);
        qbPagedRequestBuilder.setPerPage(PER_PAGE_SIZE_100);

        requestExecutor.loadLastUpdatedUsers(qbPagedRequestBuilder, new QBEntityCallback<ArrayList<QBUser>>() {
            @Override
            public void onSuccess(ArrayList<QBUser> qbUsers, Bundle bundle) {
                Log.e("LLLLLL_Quickbox: ", "Successfully loaded Last 100 created users");
                dbManager.saveAllUsers(qbUsers, true);
                currentOpponentsList = dbManager.getAllUsers();

                Constants.qbUsersList.clear();
                for (int i = 0; i < currentOpponentsList.size(); i++) {
                    QBUser qbUser = currentOpponentsList.get(i);
                    if (qbUser.getLogin().equals(userId))
                        Constants.qbUsersList.add(qbUser);
                }

            }

            @Override
            public void onError(QBResponseException e) {
                Log.d(TAG, "Error load users" + e.getMessage());
            }
        });
    }
}

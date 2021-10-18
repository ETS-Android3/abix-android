package com.topzi.chat.activity;

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
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.provider.ContactsContract;
import android.provider.MediaStore;

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
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.androidnetworking.AndroidNetworking;
import com.androidnetworking.common.Priority;
import com.androidnetworking.error.ANError;
import com.androidnetworking.interfaces.JSONObjectRequestListener;
import com.quickblox.chat.QBChatService;
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
import com.topzi.chat.helper.QbUsersDbManager;
import com.topzi.chat.model.GroupImageModel;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.snackbar.Snackbar;
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
import com.topzi.chat.helper.NetworkUtil;
import com.topzi.chat.helper.SocketConnection;
import com.topzi.chat.helper.StorageManager;
import com.topzi.chat.model.ContactsData;
import com.topzi.chat.model.GroupData;
import com.topzi.chat.model.GroupMessage;
import com.topzi.chat.model.GroupUpdateResult;
import com.topzi.chat.model.MessagesData;
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
import com.topzi.chat.utils.UiUtils;
import com.topzi.chat.utils.WebRtcSessionManager;
import com.vanniktech.emoji.EmojiPopup;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

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
import static com.topzi.chat.activity.ChatActivity.getPath;
import static com.topzi.chat.helper.NetworkUtil.NOT_CONNECT;
import static com.topzi.chat.activity.GroupChatActivity.MessageListAdapter.VIEW_TYPE_DATE;
import static com.topzi.chat.utils.Constants.API_VERSION;
import static com.topzi.chat.utils.Constants.BASE_URL;
import static com.topzi.chat.utils.Constants.MUTE_NOTI;
import static com.topzi.chat.utils.Constants.TAG_ADMIN;
import static com.topzi.chat.utils.Constants.TAG_GROUP;
import static com.topzi.chat.utils.Constants.TAG_GROUP_ID;
import static com.topzi.chat.utils.Constants.TAG_MEMBER;
import static com.topzi.chat.utils.Constants.TAG_MEMBER_ID;
import static com.topzi.chat.utils.Constants.TAG_MEMBER_NO;
import static com.topzi.chat.utils.Constants.TAG_MEMBER_ROLE;
import static com.topzi.chat.utils.Constants.TAG_REPLY_TO;
import static com.topzi.chat.utils.Constants.TRUE;
import static com.topzi.chat.utils.Constants.USER_ID;

public class GroupChatActivity extends BaseActivity implements View.OnClickListener, SocketConnection.GroupChatCallbackListener, TextWatcher {
    EditText editText;
    String groupId, groupName, msgReplyTo = null;
    List<GroupMessage> messagesList = new ArrayList<>();
    GroupMessage replyPrivateTo = null;
    String TAG = this.getClass().getSimpleName();
    RecyclerView recyclerView;
    LinearLayout llWallpaper;
    TextView tvGallary, tvSolidColor, tvWallpaper;
    TextView username, online, txtMembers, txtReplyTo, txtMsgReply;
    RelativeLayout chatUserLay, mainLay, attachmentsLay, imageViewLay, bottomLay, forwordLay;
    ImageView attachbtn, optionbtn, backbtn, send, audioCallBtn, videoCallBtn, cameraBtn,
            galleryBtn, fileBtn, audioBtn, locationBtn, contactBtn, imageView, forwordBtn, copyBtn, closeBtn, deleteBtn, optionBtnForward;
    CircleImageView userimage;
    ImageView imgChatBg, imgEmoji, imgReply;

    RelativeLayout rlLast;
    RecyclerView rvSelected, rvUserList;
    LinearLayout buttonLayout;
    ImageView audioCallBtn1,videoCallBtn1;

    EmojiPopup emojiPopup;
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
    Handler handler = new Handler();
    Runnable runnable;
    EndlessRecyclerOnScrollListener endlessRecyclerOnScrollListener;
    public static String tempGroupId = "";
    GroupData groupData;
    ArrayList<GroupMessage> selectedChatPos = new ArrayList<>();
    private boolean isFromNotification;
    ContactsData.Result results;
    SharedPreferences pref;
    SharedPreferences.Editor editor;
    Dialog dialog;
    String editImagePath = "";
    Dialog dialog1;
    ConstraintLayout replyContainer, lytReply;
    ImageButton btnCancelReply;

    public static ArrayList<Integer> solidWallpaper = new ArrayList<>();
    private SolidColorAdapter solidColorAdapter;
    BottomSheetBehavior behavior;
    List<GroupData.GroupMembers> membersList = new ArrayList<>();

    private QbUsersDbManager dbManager;
    List<QBUser> currentOpponentsList = new ArrayList<>();
    private static final int PER_PAGE_SIZE_100 = 100;
    private static final String ORDER_RULE = "order";
    private static final String ORDER_DESC_UPDATED = "desc date updated_at";

    List<ContactsData.Result> contactList = new ArrayList<>();
    List<ContactsData.Result> filterList = new ArrayList<>();
    List<ContactsData.Result> selectedUser = new ArrayList<>();

    List<QBUser> selectedOppList = new ArrayList<>();
    private UsersAdapter usersAdapter;
    private SelectedUsersAdapter selectedUsersAdapter;

    BottomSheetBehavior newBehavior;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_chat);

        if (getIntent().getStringExtra("notification") != null) {
            Constants.isGroupChatOpened = true;
            isFromNotification = true;
        }
        if (Constants.groupContext != null && Constants.isGroupChatOpened) {
            ((Activity) Constants.groupContext).finish();
        }
        pref = GroupChatActivity.this.getSharedPreferences(Constants.PREFERENCE_NAME, MODE_PRIVATE);
        editor = pref.edit();
        Constants.groupContext = this;
        dbManager = QbUsersDbManager.getInstance(getApplicationContext());

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
        txtMembers = findViewById(R.id.txtMembers);
        online = findViewById(R.id.online);
        attachbtn = findViewById(R.id.attachbtn);
        audioCallBtn = findViewById(R.id.audioCallBtn);
        videoCallBtn = findViewById(R.id.videoCallBtn);
        optionbtn = findViewById(R.id.optionbtn);
        optionBtnForward = findViewById(R.id.option_btn_forward);
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
        closeBtn = imageViewLay.findViewById(R.id.closeBtn);
        imageView = findViewById(R.id.imageView);
        forwordLay = findViewById(R.id.forwordLay);
        forwordBtn = findViewById(R.id.forwordBtn);
        copyBtn = findViewById(R.id.copyBtn);
        deleteBtn = findViewById(R.id.deleteBtn);
        txtReplyTo = findViewById(R.id.txt_user_name);
        txtMsgReply = findViewById(R.id.txt_message);
        imgReply = findViewById(R.id.img_reply);
        replyContainer = findViewById(R.id.reply_layout);
        lytReply = findViewById(R.id.lyt_reply);
        btnCancelReply = findViewById(R.id.btn_cancel_reply);

        rvSelected = findViewById(R.id.rvSelected);
        rvUserList = findViewById(R.id.rvUserList);
        buttonLayout = findViewById(R.id.buttonLayout);
        videoCallBtn1 = findViewById(R.id.videoCallBtn1);
        audioCallBtn1 = findViewById(R.id.audioCallBtn1);
        rlLast = findViewById(R.id.rlLast);

        behavior = BottomSheetBehavior.from(llWallpaper);
        emojiPopup = EmojiPopup.Builder.fromRootView(mainLay).build(editText);
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
        SocketConnection.getInstance(this).setGroupChatCallbackListener(this);
        bottomSheetBehavior = BottomSheetBehavior.from(imageViewLay);
        apiInterface = ApiClient.getClient().create(ApiInterface.class);
        dbhelper = DatabaseHandler.getInstance(this);
        storageManager = StorageManager.getInstance(this);
        display = getWindowManager().getDefaultDisplay();


        if (getIntent().getStringExtra("EXTRA_EDITED_PATH") != null) {
            editImagePath = getIntent().getStringExtra("EXTRA_EDITED_PATH");
            groupId = pref.getString("sendGrpImage", "");
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
                                GroupMessage mdata = updateDBList("video", imagePath, filepath);
                                byte[] bytes = org.apache.commons.io.FileUtils.readFileToByteArray(new File(imagePath));
                                uploadImage(bytes, imagePath, mdata, filepath);
                            }
                        }

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                } else {
                    ImageCompression imageCompression = new ImageCompression(GroupChatActivity.this) {
                        @Override
                        protected void onPostExecute(String imagePath) {
                            try {
                                Log.v("checkChat", "imagepath=" + imagePath);
                                GroupMessage mdata = updateDBList("image", imagePath, "");
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
            }
        } else {
            groupId = getIntent().getStringExtra(TAG_GROUP_ID);
        }

        if (dbhelper.getGroupData(this, groupId) != null) {
            groupData = dbhelper.getGroupData(this, groupId);
            groupName = groupData.groupName;
            tempGroupId = groupId;

            NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            if (notificationManager != null) {
                notificationManager.cancel(groupData.groupName, 0);
                notificationManager.cancel("New Group", 0);
            }

        } else {
            finish();
        }

        backbtn.setColorFilter(ContextCompat.getColor(getApplicationContext(), R.color.primarytext));
        // set visibility status
        chatUserLay.setVisibility(View.VISIBLE);
        backbtn.setVisibility(View.VISIBLE);
        audioCallBtn.setVisibility(View.VISIBLE);
        videoCallBtn.setVisibility(View.VISIBLE);
        optionbtn.setVisibility(View.VISIBLE);
        txtMembers.setVisibility(View.VISIBLE);

        username.setText(groupData.groupName);
        setGroupMembers(groupId);
        Glide.with(GroupChatActivity.this).load(Constants.GROUP_IMG_PATH + groupData.groupImage)
                .apply(RequestOptions.circleCropTransform().placeholder(R.drawable.create_group).error(R.drawable.create_group))
                .into(userimage);

        totalMsg = dbhelper.getGroupMessagesCount(groupId);
        Log.v("totalMsg", "totalMsg=" + totalMsg);

        messagesList.addAll(getMessagesAry(dbhelper.getGroupMessages(groupId, "0", "20", getApplicationContext()), null));
        linearLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        linearLayoutManager.setReverseLayout(true);
//        linearLayoutManager.setStackFromEnd(true);
        recyclerView.setNestedScrollingEnabled(false);
        recyclerView.setLayoutManager(linearLayoutManager);
//        recyclerView.setHasFixedSize(true);
//        recyclerView.setItemViewCacheSize(100);
//        recyclerView.setDrawingCacheEnabled(true);
//        recyclerView.setDrawingCacheQuality(View.DRAWING_CACHE_QUALITY_HIGH);

        endlessRecyclerOnScrollListener = new EndlessRecyclerOnScrollListener(linearLayoutManager) {
            @Override
            public void onLoadMore(int page, int totalItemsCount, RecyclerView view) {
                Log.v("current_page", "current_page=" + page + "&totalItems=" + totalItemsCount);
                final List<GroupMessage> tmpList = new ArrayList<>(dbhelper.getGroupMessages(groupId, String.valueOf(page * 20), "20", getApplicationContext()));
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

        DividerItemDecoration divider = new DividerItemDecoration(recyclerView.getContext(),
                linearLayoutManager.getOrientation());
        divider.setDrawable(getResources().getDrawable(R.drawable.emptychat_divider));
        recyclerView.addItemDecoration(divider);

        messageListAdapter = new MessageListAdapter(this, messagesList);
        recyclerView.setAdapter(messageListAdapter);

        tvGallary.setOnClickListener(this);
        tvSolidColor.setOnClickListener(this);
        send.setOnClickListener(this);
        backbtn.setOnClickListener(this);
        attachbtn.setOnClickListener(this);
        optionbtn.setOnClickListener(this);
        optionBtnForward.setOnClickListener(this);
        userimage.setOnClickListener(this);
        audioCallBtn.setOnClickListener(this);
        videoCallBtn.setOnClickListener(this);
        audioCallBtn1.setOnClickListener(this);
        videoCallBtn1.setOnClickListener(this);
        cameraBtn.setOnClickListener(this);
        galleryBtn.setOnClickListener(this);
        fileBtn.setOnClickListener(this);
        audioBtn.setOnClickListener(this);
        locationBtn.setOnClickListener(this);
        contactBtn.setOnClickListener(this);
        editText.addTextChangedListener(this);
        chatUserLay.setOnClickListener(this);
        copyBtn.setOnClickListener(this);
        forwordBtn.setOnClickListener(this);
        closeBtn.setOnClickListener(this);
        deleteBtn.setOnClickListener(this);

        whileViewChat();

        if (!dbhelper.isMemberExist(GetSet.getUserId(), groupId)) {
            bottomLay.setVisibility(View.GONE);
        }

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

        if (SocketConnection.onUpdateTabIndication != null) {
            SocketConnection.onUpdateTabIndication.updateIndication();
        }

        recyclerView.addOnItemTouchListener(chatItemClick(this, recyclerView));

        if (!pref.getString("newWallpaper", "").equals("")) {
            File file = storageManager.getImage("wallpaper", pref.getString("newWallpaper", ""));
            Uri uri = Uri.fromFile(file);
            Glide.with(GroupChatActivity.this)
                    .load(uri)
                    .into(imgChatBg);
        } else if (!pref.getString("oldWallpaper", "").equals("")) {
            Uri uri = Uri.fromFile(new File(pref.getString("oldWallpaper", "")));
            Glide.with(GroupChatActivity.this)
                    .load(uri)
                    .into(imgChatBg);
        } else if (pref.getInt("solidColorPos", 0) != 0) {
            imgChatBg.setBackgroundColor(getResources().getColor(solidWallpaper.get(pref.getInt("solidColorPos", 0))));
        } else {
            imgChatBg.setImageDrawable(getResources().getDrawable(R.drawable.chat_bg));
        }

        imgEmoji.setOnClickListener(v -> emojiPopup.toggle());

        MessageSwipeController messageSwipeController = new MessageSwipeController(this, new SwipeControllerActions() {
            @Override
            public void showReplyUI(int position) {
                showReplyTo(messagesList.get(position), false);
            }
        });

        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(messageSwipeController);
        itemTouchHelper.attachToRecyclerView(recyclerView);

        btnCancelReply.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                msgReplyTo = null;
                replyPrivateTo = null;
                replyContainer.setVisibility(View.GONE);
            }
        });

        startLoginService();

        newBehavior = BottomSheetBehavior.from(rlLast);
        newBehavior.setBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {
            @Override
            public void onStateChanged(@NonNull View bottomSheet, int newState) {
                // React to state change
            }

            @Override
            public void onSlide(@NonNull View bottomSheet, float slideOffset) {
                // React to dragging events
            }
        });

        membersList.clear();
        membersList.addAll(dbhelper.getGroupMembers(getApplicationContext(), groupId));

        List<QBUser> currentOpponentsList = new ArrayList<>();
        contactList.addAll(dbhelper.getStoredContacts(GroupChatActivity.this));
        for (int i = 0; i < contactList.size(); i++) {
            ContactsData.Result result = contactList.get(i);
            for (int j = 0; j < dbManager.getAllUsers().size(); j++) {
                QBUser qbUser = dbManager.getAllUsers().get(j);
                if (result.user_id.equals(qbUser.getLogin())) {
                    filterList.add(result);
                    currentOpponentsList.add(qbUser);
                }
            }
        }

        currentOpponentsList.remove(sharedPrefsHelper.getQbUser());
        Log.e("LLLL_QbUser: ", dbhelper.getStoredContacts(GroupChatActivity.this).size() + "     " + dbManager.getAllUsers().size());
        if (usersAdapter == null) {
            Constants.qbUsersList.clear();
            usersAdapter = new UsersAdapter(GroupChatActivity.this, currentOpponentsList, filterList);
            rvUserList.setLayoutManager(new LinearLayoutManager(GroupChatActivity.this));
            rvUserList.setAdapter(usersAdapter);
        } else {
            usersAdapter.updateUsersList(currentOpponentsList);
        }

        Log.e("LLLL_QbUser: ", dbhelper.getStoredContacts(GroupChatActivity.this).size() + "     " + dbManager.getAllUsers().size());
        if (selectedUsersAdapter == null) {
            Constants.qbUsersList.clear();
            selectedUsersAdapter = new SelectedUsersAdapter(GroupChatActivity.this, selectedOppList, selectedUser);
            rvSelected.setLayoutManager(new LinearLayoutManager(GroupChatActivity.this,RecyclerView.HORIZONTAL,false));
            rvSelected.setAdapter(selectedUsersAdapter);
        } else {
            selectedUsersAdapter.updateUsersList(currentOpponentsList);
        }

    }

    private void showReplyTo(GroupMessage messagesData, boolean isReplyPrivate) {
        msgReplyTo = messagesData.messageId;
        if (msgReplyTo != null && !msgReplyTo.equals("")) {
            replyContainer.setVisibility(View.VISIBLE);
            lytReply.setVisibility(View.VISIBLE);

            ContactsData.Result contactDetail = dbhelper.getContactDetail(messagesData.memberId);
            if (contactDetail != null && contactDetail.phone_no.equals(GetSet.getphonenumber()))
                txtReplyTo.setText(Constants.YOU);
            else
                txtReplyTo.setText(ApplicationClass.getContactName(GroupChatActivity.this, dbhelper.getContactPhone(messagesData.memberId)));

            if (messagesData.message != null)
                txtMsgReply.setText(messagesData.message);
        }
//        imgReply = findViewById(R.id.img_reply);
    }

    public class SolidColorAdapter extends RecyclerView.Adapter<SolidColorAdapter.MyClassView> {

        @NonNull
        @Override
        public SolidColorAdapter.MyClassView onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_solid_color, parent, false);
            return new SolidColorAdapter.MyClassView(view);
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


    private void setGroupMembers(String groupId) {
        List<GroupData.GroupMembers> memberList = dbhelper.getThreeMembers(this, groupId);
        StringBuilder members = new StringBuilder();
        String prefix = "";
        for (GroupData.GroupMembers groupMembers : memberList) {
            members.append(prefix);
            prefix = ", ";
            if (groupMembers.memberId.equalsIgnoreCase(GetSet.getUserId())) {
                members.append(getString(R.string.you));
            } else {
                ContactsData.Result user = dbhelper.getContactDetail(groupMembers.memberId);
                members.append(ApplicationClass.getContactName(this, user.phone_no));
            }
        }
        txtMembers.setText("" + members);

    }

    @Override
    public void onNetworkChange(boolean isConnected) {
        Log.v("onNetwork", "GroupChat=" + isConnected);
//        if (isConnected) {
//            online.setVisibility(View.VISIBLE);
//        } else {
//            online.setVisibility(View.GONE);
//        }
    }

    private List<GroupMessage> getMessagesAry(List<GroupMessage> tmpList, GroupMessage lastData) {
        List<GroupMessage> msgList = new ArrayList<>();
        if (tmpList.size() == 0 && lastData != null) {
            GroupMessage groupMessage = new GroupMessage();
            groupMessage.messageType = "date";
            groupMessage.message = getFormattedDate(this, Long.parseLong(lastData.chatTime));
            msgList.add(groupMessage);
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
                        GroupMessage groupMessage = new GroupMessage();
                        groupMessage.messageType = "date";
                        groupMessage.message = getFormattedDate(this, Long.parseLong(tmpList.get(i).chatTime));
                        msgList.add(groupMessage);
                        Log.v("diff", "diff pos=" + i + "&msg=" + tmpList.get(i).message);
                    }
                } else {
                    msgList.add(tmpList.get(i));
                }
            }
        }
        return msgList;
    }

    @Override
    public void onGroupChatReceive(final GroupMessage mdata) {
        Log.i(TAG, "onGroupChatReceive: " + mdata.groupId);
        runOnUiThread(new Runnable() {
            public void run() {
                if (mdata.groupId.equalsIgnoreCase(groupId)) {
                    GroupMessage groupMessage = mdata;
                    groupMessage = GroupChatActivity.getMessages(dbhelper, getApplicationContext(), mdata);
                    messagesList.add(0, groupMessage);
                    messageListAdapter.notifyItemInserted(0);
                    recyclerView.smoothScrollToPosition(0);

                    switch (mdata.messageType) {
                        case "group_image":
                        case "subject":
                            groupData = dbhelper.getGroupData(GroupChatActivity.this, groupId);
                            username.setText(groupData.groupName);
                            Glide.with(GroupChatActivity.this).load(Constants.GROUP_IMG_PATH + groupData.groupImage)
                                    .apply(RequestOptions.circleCropTransform().placeholder(R.drawable.change_camera).error(R.drawable.change_camera))
                                    .into(userimage);
                            break;
                        case "remove_member":
                        case "left":
                            if (mdata.memberId.equals(GetSet.getUserId())) {
                                bottomLay.setVisibility(View.GONE);
                            } else {
                                bottomLay.setVisibility(View.VISIBLE);
                            }
                            setGroupMembers(groupId);
                            break;
                        case "add_member":
                            try {
                                JSONArray jsonArray = new JSONArray(mdata.attachment);
                                for (int i = 0; i < jsonArray.length(); i++) {
                                    JSONObject jsonObject = jsonArray.getJSONObject(i);
                                    if (jsonObject.getString(TAG_MEMBER_ID).equals(GetSet.getUserId())) {
                                        bottomLay.setVisibility(View.VISIBLE);
                                        setGroupMembers(groupId);
                                        recyclerView.post(new Runnable() {
                                            @Override
                                            public void run() {
                                                recyclerView.scrollToPosition(0);
                                            }
                                        });
                                        break;
                                    }
                                }
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                            break;
                    }

                    whileViewChat();

                }
            }
        });
    }

//    private void makeAdmin(String groupId) {
//        GroupData groupData = dbhelper.getGroupData(getApplicationContext(), groupId);
//        if (dbhelper.isGroupHaveAdmin(groupData.groupId) == 1 && groupData.groupAdminId.equalsIgnoreCase(GetSet.getUserId())) {
//            List<GroupData.GroupMembers> membersData = dbhelper.getGroupMembers(getApplicationContext(), groupData.groupId);
//            for (GroupData.GroupMembers groupMember : membersData) {
//                if (!groupMember.memberId.equals(GetSet.getUserId())) {
//                    JSONArray jsonArray = new JSONArray();
//                    try {
//                        JSONObject jsonObject = new JSONObject();
//                        jsonObject.put(TAG_MEMBER_ID, groupMember.memberId);
//                        jsonObject.put(TAG_MEMBER_ROLE, TAG_ADMIN);
//                        jsonArray.put(jsonObject);
//                    } catch (JSONException e) {
//                        e.printStackTrace();
//                    }
//                    String unixStamp = String.valueOf(System.currentTimeMillis() / 1000L);
//                    RandomString randomString = new RandomString(10);
//                    String messageId = groupId + randomString.nextString();
//
//                    JSONObject message = new JSONObject();
//                    try {
//                        message.put(Constants.TAG_GROUP_ADMIN_ID, groupMember.memberId);
//                        message.put(Constants.TAG_GROUP_ID, groupId);
//                        message.put(Constants.TAG_GROUP_NAME, groupData.groupName);
//                        message.put(Constants.TAG_CHAT_TYPE, Constants.TAG_GROUP);
//                        message.put(Constants.TAG_CHAT_TIME, unixStamp);
//                        message.put(Constants.TAG_MESSAGE_ID, messageId);
//                        message.put(Constants.TAG_ATTACHMENT, Constants.TAG_ADMIN);
//                        message.put(Constants.TAG_MEMBER_ID, groupMember.memberId);
//                        message.put(Constants.TAG_MEMBER_NAME, groupMember.memberName);
//                        message.put(Constants.TAG_MEMBER_NO, groupMember.memberNo);
//                        message.put(Constants.TAG_MESSAGE_TYPE, "admin");
//                        message.put(Constants.TAG_MESSAGE, "Admin");
//                    } catch (JSONException e) {
//                        e.printStackTrace();
//                    }
//                    socketConnection.startGroupChat(message);
//                    updateGroupData(jsonArray);
//                    break;
//                }
//            }
//        }
//    }

    private void updateGroupData(JSONArray jsonArray) {
        ApiInterface apiInterface = ApiClient.getClient().create(ApiInterface.class);
        Call<GroupUpdateResult> call3 = apiInterface.updateGroup(GetSet.getToken(), groupId, jsonArray);
        call3.enqueue(new Callback<GroupUpdateResult>() {
            @Override
            public void onResponse(Call<GroupUpdateResult> call, Response<GroupUpdateResult> response) {
                try {
                    Log.i(TAG, "updateGroup: " + new Gson().toJson(response));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(Call<GroupUpdateResult> call, Throwable t) {
                Log.e(TAG, "updateGroup: " + t.getMessage());
                call.cancel();
            }
        });
    }

    private void whileViewChat() {
        dbhelper.updateGroupMessageReadStatus(groupId);
        dbhelper.resetUnseenGroupMessagesCount(groupId);
    }

    @Override
    public void onListenGroupTyping(final JSONObject data) {
        runOnUiThread(new Runnable() {
            public void run() {
                if (dbhelper.isMemberExist(GetSet.getUserId(), groupId)) {
                    try {
                        Log.e(TAG, "onListenGroupTyping: " + data.toString());
                        String memberId = data.getString(Constants.TAG_MEMBER_ID);
                        String group_id = data.getString(Constants.TAG_GROUP_ID);
                        if (!memberId.equalsIgnoreCase(GetSet.getUserId()) &&
                                group_id.equalsIgnoreCase(groupId))
                            if (data.get("type").equals("typing")) {
                                txtMembers.setText(ApplicationClass.getContactName(GroupChatActivity.this, dbhelper.getContactDetail(memberId).phone_no) + " is " + getString(R.string.typing));
                            }

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            setGroupMembers(groupId);
                        }
                    }, 1000);
                }
            }
        });
    }

    @Override
    public void onMemberExited(final JSONObject data) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                try {
                    setGroupMembers(data.getString(Constants.TAG_GROUP_ID));
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            setGroupMembers(groupId);
                        }
                    }, 1000);
                } catch (JSONException e) {
                    e.printStackTrace();
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

                int currentCount = dbhelper.getGroupMessagesCount(groupId);
                if (totalMsg != currentCount) {
                    messagesList.clear();
                    if (endlessRecyclerOnScrollListener != null) {
                        endlessRecyclerOnScrollListener.resetState();
                    }
                    messagesList.addAll(getMessagesAry(dbhelper.getGroupMessages(groupId, "0", "20", getApplicationContext()), null));
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

    @Override
    public void onUpdateGroupInfo(GroupMessage groupMessage) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (GroupChatActivity.this.groupId.equals(groupMessage.groupId)) {
                    switch (groupMessage.messageType) {
                        case "group_image":
                        case "subject":
                            groupData = dbhelper.getGroupData(GroupChatActivity.this, groupMessage.groupId);
                            username.setText(groupData.groupName);
                            Glide.with(GroupChatActivity.this).load(Constants.GROUP_IMG_PATH + groupData.groupImage)
                                    .apply(RequestOptions.circleCropTransform().placeholder(R.drawable.change_camera).error(R.drawable.change_camera))
                                    .into(userimage);
                            break;
                        case "remove_member":
                        case "left":
                            if (groupMessage.memberId.equals(GetSet.getUserId())) {
                                bottomLay.setVisibility(View.GONE);
                            } else {
                                bottomLay.setVisibility(View.VISIBLE);
                            }
                            setGroupMembers(groupMessage.groupId);
                            break;
                        case "add_member":
                            try {
                                JSONArray jsonArray = new JSONArray(groupMessage.attachment);
                                for (int i = 0; i < jsonArray.length(); i++) {
                                    JSONObject jsonObject = jsonArray.getJSONObject(i);
                                    if (jsonObject.getString(TAG_MEMBER_ID).equals(GetSet.getUserId())) {
                                        bottomLay.setVisibility(View.VISIBLE);
                                        setGroupMembers(groupMessage.groupId);
                                        recyclerView.post(new Runnable() {
                                            @Override
                                            public void run() {
                                                recyclerView.scrollToPosition(0);
                                            }
                                        });
                                        break;
                                    }
                                }
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                            break;
                    }
                }
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
        try {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put(Constants.TAG_GROUP_ID, groupId);
            jsonObject.put(Constants.TAG_MEMBER_ID, GetSet.getUserId());
            jsonObject.put("type", "typing");
            socketConnection.groupTyping(jsonObject);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void afterTextChanged(Editable editable) {
        runnable = new Runnable() {
            public void run() {
                meTyping = false;
                try {
                    JSONObject jsonObject = new JSONObject();
                    jsonObject.put(Constants.TAG_GROUP_ID, groupId);
                    jsonObject.put(Constants.TAG_MEMBER_ID, GetSet.getUserId());
                    jsonObject.put("type", "untyping");
                    socketConnection.groupTyping(jsonObject);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        };
        handler.postDelayed(runnable, 500);
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
        private List<GroupMessage> mMessageList;

        public MessageListAdapter(Context context, List<GroupMessage> messageList) {
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
            final GroupMessage message = mMessageList.get(position);

            if (message.memberId != null && message.memberId.equals(GetSet.getUserId())) {
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
                    case "create_group":
                    case "add_member":
                    case "group_image":
                    case "subject":
                    case "left":
                    case "remove_member":
                    case "admin":
                    case "change_number":
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
                    case "create_group":
                    case "add_member":
                    case "group_image":
                    case "subject":
                    case "left":
                    case "remove_member":
                    case "admin":
                    case "change_number":
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
            final GroupMessage message = mMessageList.get(position);
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

        private void showReply(GroupMessage message, ConstraintLayout replyLayout, TextView txtReplyUserName, TextView txtReplyMsg, ImageView imgReply) {
            if (message.reply_to != null && !message.reply_to.equals("")) {
                GroupMessage repliedToMsg = dbhelper.getSingleGroupMessage(groupId, message.reply_to);
                if (repliedToMsg == null || repliedToMsg.memberId == null) {
                    replyLayout.setVisibility(View.GONE);
                    return;
                }
                replyLayout.setVisibility(View.VISIBLE);
                ContactsData.Result contactDetail = dbhelper.getContactDetail(repliedToMsg.memberId);
                if (contactDetail != null && contactDetail.phone_no.equals(GetSet.getphonenumber()))
                    txtReplyUserName.setText(Constants.YOU);
                else
                    txtReplyUserName.setText(ApplicationClass.getContactName(mContext, dbhelper.getContactPhone(repliedToMsg.memberId)));
                if (repliedToMsg.message != null)
                    txtReplyMsg.setText(repliedToMsg.message);
//                imgReply.set;
            } else replyLayout.setVisibility(View.GONE);
        }

        private class SentMessageHolder extends RecyclerView.ViewHolder {
            TextView messageText, timeText;
            ConstraintLayout replyLayout;
            TextView txtReplyUserName, txtReplyMsg;
            ImageView imgReply;

            SentMessageHolder(View itemView) {
                super(itemView);

                messageText = itemView.findViewById(R.id.text_message_body);
                timeText = itemView.findViewById(R.id.text_message_time);
                replyLayout = itemView.findViewById(R.id.lyt_reply);
                txtReplyUserName = itemView.findViewById(R.id.txt_user_name);
                txtReplyMsg = itemView.findViewById(R.id.txt_message);
                imgReply = itemView.findViewById(R.id.img_reply);
            }

            void bind(final GroupMessage message) {
                messageText.setText(message.message
                        + Html.fromHtml(" &#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;"));
                Linkify.addLinks(messageText, Linkify.EMAIL_ADDRESSES | Linkify.PHONE_NUMBERS | Linkify.WEB_URLS);
                timeText.setText(ApplicationClass.getTime(Long.parseLong(message.chatTime)));

                if (selectedChatPos.contains(message)) {
                    itemView.setSelected(true);
                } else {
                    itemView.setSelected(false);
                }
                showReply(message, replyLayout, txtReplyUserName, txtReplyMsg, imgReply);
            }
        }

        private class ReceivedMessageHolder extends RecyclerView.ViewHolder {
            TextView messageText, timeText, nameText;
            ConstraintLayout replyLayout;
            TextView txtReplyUserName, txtReplyMsg;
            ImageView imgReply;

            ReceivedMessageHolder(View itemView) {
                super(itemView);

                nameText = (TextView) itemView.findViewById(R.id.text_message_sender);
                messageText = (TextView) itemView.findViewById(R.id.text_message_body);
                timeText = (TextView) itemView.findViewById(R.id.text_message_time);
                replyLayout = itemView.findViewById(R.id.lyt_reply);
                txtReplyUserName = itemView.findViewById(R.id.txt_user_name);
                txtReplyMsg = itemView.findViewById(R.id.txt_message);
                imgReply = itemView.findViewById(R.id.img_reply);
            }

            void bind(GroupMessage message) {
                nameText.setVisibility(View.VISIBLE);
                nameText.setText(ApplicationClass.getContactName(mContext, dbhelper.getContactPhone(message.memberId)));
                messageText.setText(message.message
                        + Html.fromHtml(" &#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;"));
                Linkify.addLinks(messageText, Linkify.EMAIL_ADDRESSES | Linkify.PHONE_NUMBERS | Linkify.WEB_URLS);
                timeText.setText(ApplicationClass.getTime(Long.parseLong(message.chatTime.replace(".0", ""))));

                if (selectedChatPos.contains(message)) {
                    itemView.setSelected(true);
                } else {
                    itemView.setSelected(false);
                }
                showReply(message, replyLayout, txtReplyUserName, txtReplyMsg, imgReply);
            }
        }

        private class SentImageHolder extends RecyclerView.ViewHolder {
            TextView timeText;
            ImageView uploadimage, downloadicon;
            RelativeLayout progresslay;
            ProgressWheel progressbar;
            ConstraintLayout replyLayout;
            TextView txtReplyUserName, txtReplyMsg;
            ImageView imgReply;

            SentImageHolder(View itemView) {
                super(itemView);

                uploadimage = itemView.findViewById(R.id.uploadimage);
                timeText = itemView.findViewById(R.id.text_message_time);
                progresslay = itemView.findViewById(R.id.progresslay);
                progressbar = itemView.findViewById(R.id.progressbar);
                downloadicon = itemView.findViewById(R.id.downloadicon);
                replyLayout = itemView.findViewById(R.id.lyt_reply);
                txtReplyUserName = itemView.findViewById(R.id.txt_user_name);
                txtReplyMsg = itemView.findViewById(R.id.txt_message);
                imgReply = itemView.findViewById(R.id.img_reply);
            }

            void bind(final GroupMessage message) {
                timeText.setText(ApplicationClass.getTime(Long.parseLong(message.chatTime)));
                if (selectedChatPos.contains(message)) {
                    itemView.setSelected(true);
                } else {
                    itemView.setSelected(false);
                }
                showReply(message, replyLayout, txtReplyUserName, txtReplyMsg, imgReply);
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
                                            dbhelper.updateGroupMessageData(message.messageId, Constants.TAG_PROGRESS, "");
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
                                        Toast.makeText(GroupChatActivity.this, getString(R.string.no_media), Toast.LENGTH_SHORT).show();
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
                                Intent i = new Intent(GroupChatActivity.this, LocationActivity.class);
                                i.putExtra("from", "view");
                                i.putExtra("lat", message.lat);
                                i.putExtra("lon", message.lon);
                                startActivity(i);
                            }
                        });
                        break;
                    case "video":
                        progresslay.setVisibility(View.VISIBLE);
                        if (message.progress.equals("")) {
                            progressbar.setVisibility(View.VISIBLE);
                            progressbar.spin();
                            downloadicon.setImageResource(R.drawable.upload);
                            File file = storageManager.getImage("sent", getFileName(message.thumbnail));
                            if (file != null) {
                                Log.v(TAG, "file=" + file.getAbsolutePath());
                                Glide.with(mContext).load(Uri.fromFile(file)).thumbnail(0.5f)
                                        .into(uploadimage);
                            }
                        } else if (message.progress.equals("completed")) {
                            progressbar.setVisibility(View.GONE);
                            progressbar.stopSpinning();
                            downloadicon.setImageResource(R.drawable.play);
                            File file = storageManager.getImage("sent", message.thumbnail);
                            if (file != null) {
                                Log.v(TAG, "file=" + file.getAbsolutePath());
                                Glide.with(mContext).load(Uri.fromFile(file)).thumbnail(0.5f)
                                        .into(uploadimage);
                            }
                        } else if (message.progress.equals("error")) {
                            progressbar.setVisibility(View.VISIBLE);
                            progressbar.stopSpinning();
                            downloadicon.setImageResource(R.drawable.upload);
                            File file = storageManager.getImage("sent", getFileName(message.thumbnail));
                            if (file != null) {
                                Log.v(TAG, "file=" + file.getAbsolutePath());
                                Glide.with(mContext).load(Uri.fromFile(file)).thumbnail(0.5f)
                                        .into(uploadimage);
                            }
                        } else {

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
                                                dbhelper.updateGroupMessageData(message.messageId, Constants.TAG_PROGRESS, "");
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
                                            Toast.makeText(GroupChatActivity.this, getString(R.string.something_wrong), Toast.LENGTH_SHORT).show();
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
                                            Toast.makeText(GroupChatActivity.this, getString(R.string.no_application), Toast.LENGTH_SHORT).show();
                                            e.printStackTrace();
                                        }
                                    } else {
                                        Toast.makeText(GroupChatActivity.this, getString(R.string.no_media), Toast.LENGTH_SHORT).show();
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
                nameText = itemView.findViewById(R.id.text_message_sender);
                videoprogresslay = itemView.findViewById(R.id.videoprogresslay);
                videoprogressbar = itemView.findViewById(R.id.videoprogressbar);
                replyLayout = itemView.findViewById(R.id.lyt_reply);
                txtReplyUserName = itemView.findViewById(R.id.txt_user_name);
                txtReplyMsg = itemView.findViewById(R.id.txt_message);
                imgReply = itemView.findViewById(R.id.img_reply);
            }

            void bind(final GroupMessage message) {
                nameText.setVisibility(View.VISIBLE);
                nameText.setText(ApplicationClass.getContactName(mContext, dbhelper.getContactPhone(message.memberId)));
                if (selectedChatPos.contains(message)) {
                    itemView.setSelected(true);
                } else {
                    itemView.setSelected(false);
                }
                showReply(message, replyLayout, txtReplyUserName, txtReplyMsg, imgReply);
                switch (message.messageType) {
                    case "image":
                        if (storageManager.checkifImageExists("thumb", message.attachment)) {
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
                            Glide.with(mContext).load(Constants.GROUP_IMG_PATH + message.attachment).thumbnail(0.5f)
                                    .apply(RequestOptions.overrideOf(18, 18))
                                    .into(uploadimage);
                        }
                        timeText.setText(ApplicationClass.getTime(Long.parseLong(message.chatTime)));

                        uploadimage.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                String[] fileName = message.attachment.split("/");
                                if (storageManager.checkifImageExists("receive", fileName[fileName.length - 1])) {
                                    File file = storageManager.getImage("receive", fileName[fileName.length - 1]);
                                    if (file != null) {
                                        Log.v(TAG, "file=" + file.getAbsolutePath());
                                        bottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
                                        Glide.with(mContext).load(file).thumbnail(0.5f)
                                                .transition(new DrawableTransitionOptions().crossFade())
                                                .into(imageView);
                                    }
                                } else {
                                    if (ContextCompat.checkSelfPermission(GroupChatActivity.this, WRITE_EXTERNAL_STORAGE)
                                            != PackageManager.PERMISSION_GRANTED) {
                                        ActivityCompat.requestPermissions(GroupChatActivity.this, new String[]{WRITE_EXTERNAL_STORAGE}, 100);
                                    } else {
                                        if (isNetworkConnected().equals(NOT_CONNECT)) {
                                            networkSnack();
                                        } else {
                                            ImageDownloader imageDownloader = new ImageDownloader(GroupChatActivity.this) {
                                                @Override
                                                protected void onPostExecute(Bitmap imgBitmap) {
                                                    if (imgBitmap == null) {
                                                        Log.v("bitmapFailed", "bitmapFailed");
                                                        Toast.makeText(mContext, getString(R.string.something_wrong), Toast.LENGTH_SHORT).show();
                                                    } else {
                                                        Log.v("onBitmapLoaded", "onBitmapLoaded");
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
                                            imageDownloader.execute(Constants.GROUP_IMG_PATH + message.attachment, "receive");
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
                                Intent i = new Intent(GroupChatActivity.this, LocationActivity.class);
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
                            Log.v("dddd", "video-if");
                            videoprogresslay.setVisibility(View.GONE);
                            File file = storageManager.getImage("thumb", message.thumbnail);
                            if (file != null) {
                                Log.v(TAG, "file=" + file.getAbsolutePath());
                                Glide.with(mContext).load(Uri.fromFile(file)).thumbnail(0.5f)
                                        .into(uploadimage);
                            }
                        } else {
                            Log.v("dddd", "video-else=" + message.thumbnail);
                            Glide.with(mContext).load(Constants.GROUP_IMG_PATH + message.thumbnail).thumbnail(0.5f)
                                    .apply(RequestOptions.overrideOf(18, 18))
                                    .into(uploadimage);
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
                                        Toast.makeText(GroupChatActivity.this, getString(R.string.no_application), Toast.LENGTH_SHORT).show();
                                        e.printStackTrace();
                                    }
                                } else {
                                    if (isNetworkConnected().equals(NOT_CONNECT)) {
                                        networkSnack();
                                    } else {
                                        ImageDownloader imageDownloader = new ImageDownloader(GroupChatActivity.this) {
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

                                                            DownloadFiles downloadFiles = new DownloadFiles(GroupChatActivity.this) {
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
                                                            downloadFiles.execute(Constants.GROUP_IMG_PATH + message.attachment, message.messageType);
                                                        }
//                                                        } else {
//                                                            Toast.makeText(mContext, getString(R.string.something_wrong), Toast.LENGTH_SHORT).show();
//                                                            videoprogresslay.setVisibility(View.GONE);
//                                                            videoprogressbar.setVisibility(View.GONE);
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
                                        imageDownloader.execute(Constants.GROUP_IMG_PATH + message.thumbnail, "thumb");
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
            ConstraintLayout replyLayout;
            TextView txtReplyUserName, txtReplyMsg;
            ImageView imgReply;

            SentFileHolder(View itemView) {
                super(itemView);

                filename = itemView.findViewById(R.id.filename);
                timeText = itemView.findViewById(R.id.text_message_time);
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

            void bind(final GroupMessage message) {
                timeText.setText(ApplicationClass.getTime(Long.parseLong(message.chatTime)));
                if (selectedChatPos.contains(message)) {
                    itemView.setSelected(true);
                } else {
                    itemView.setSelected(false);
                }
                showReply(message, replyLayout, txtReplyUserName, txtReplyMsg, imgReply);
                if (message.messageType.equals("document")) {
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
                                    filename.setText(getString(R.string.uploading));
                                    dbhelper.updateGroupMessageData(message.messageId, Constants.TAG_PROGRESS, "");
                                    message.progress = "";
                                    Intent service = new Intent(GroupChatActivity.this, FileUploadService.class);
                                    Bundle b = new Bundle();
                                    b.putSerializable("mdata", message);
                                    b.putString("filepath", message.attachment);
                                    b.putString("chatType", "group");
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
                                    Toast.makeText(GroupChatActivity.this, getString(R.string.no_application), Toast.LENGTH_SHORT).show();
                                    e.printStackTrace();
                                }
                            } else {
                                Toast.makeText(GroupChatActivity.this, getString(R.string.no_media), Toast.LENGTH_SHORT).show();
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
                nameText = itemView.findViewById(R.id.text_message_sender);
                file_type_tv = itemView.findViewById(R.id.file_type_tv);
                replyLayout = itemView.findViewById(R.id.lyt_reply);
                txtReplyUserName = itemView.findViewById(R.id.txt_user_name);
                txtReplyMsg = itemView.findViewById(R.id.txt_message);
                imgReply = itemView.findViewById(R.id.img_reply);
            }

            void bind(final GroupMessage message) {
                filename.setText(message.message);
                timeText.setText(ApplicationClass.getTime(Long.parseLong(message.chatTime)));
                nameText.setVisibility(View.VISIBLE);
                nameText.setText(ApplicationClass.getContactName(mContext, dbhelper.getContactPhone(message.memberId)));
                if (selectedChatPos.contains(message)) {
                    itemView.setSelected(true);
                } else {
                    itemView.setSelected(false);
                }
                showReply(message, replyLayout, txtReplyUserName, txtReplyMsg, imgReply);
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
                                intent.setAction(android.content.Intent.ACTION_VIEW);
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
                                Toast.makeText(GroupChatActivity.this, getString(R.string.no_application), Toast.LENGTH_SHORT).show();
                                e.printStackTrace();
                            }
                        } else {

                            DownloadFiles downloadFiles = new DownloadFiles(GroupChatActivity.this) {
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
                            downloadFiles.execute(Constants.GROUP_IMG_PATH + message.attachment, message.messageType);
                            progressbar.setVisibility(View.VISIBLE);
                            progressbar.spin();
                            downloadicon.setVisibility(View.VISIBLE);
                        }
                    }
                });
            }
        }

        private class SentContactHolder extends RecyclerView.ViewHolder {
            TextView username, phoneno, timeText;
            ConstraintLayout replyLayout;
            TextView txtReplyUserName, txtReplyMsg;
            ImageView imgReply;

            SentContactHolder(View itemView) {
                super(itemView);
                username = itemView.findViewById(R.id.username);
                phoneno = itemView.findViewById(R.id.phoneno);
                timeText = itemView.findViewById(R.id.text_message_time);
                replyLayout = itemView.findViewById(R.id.lyt_reply);
                txtReplyUserName = itemView.findViewById(R.id.txt_user_name);
                txtReplyMsg = itemView.findViewById(R.id.txt_message);
                imgReply = itemView.findViewById(R.id.img_reply);
            }

            void bind(GroupMessage message) {
                username.setText(message.contactName);
                phoneno.setText(message.contactPhoneNo);
                timeText.setText(ApplicationClass.getTime(Long.parseLong(message.chatTime)));
                showReply(message, replyLayout, txtReplyUserName, txtReplyMsg, imgReply);
            }
        }

        private class ReceivedContactHolder extends RecyclerView.ViewHolder {
            TextView username, phoneno, timeText, addcontact, nameText;
            ConstraintLayout replyLayout;
            TextView txtReplyUserName, txtReplyMsg;
            ImageView imgReply;

            ReceivedContactHolder(View itemView) {
                super(itemView);
                username = itemView.findViewById(R.id.username);
                phoneno = itemView.findViewById(R.id.phoneno);
                timeText = itemView.findViewById(R.id.text_message_time);
                addcontact = itemView.findViewById(R.id.addcontact);
                nameText = itemView.findViewById(R.id.text_message_sender);
                replyLayout = itemView.findViewById(R.id.lyt_reply);
                txtReplyUserName = itemView.findViewById(R.id.txt_user_name);
                txtReplyMsg = itemView.findViewById(R.id.txt_message);
                imgReply = itemView.findViewById(R.id.img_reply);
            }

            void bind(final GroupMessage message) {
                username.setText(message.contactName);
                phoneno.setText(message.contactPhoneNo);
                timeText.setText(ApplicationClass.getTime(Long.parseLong(message.chatTime)));
                nameText.setVisibility(View.VISIBLE);
                nameText.setText(ApplicationClass.getContactName(mContext, dbhelper.getContactPhone(message.memberId)));
                if (selectedChatPos.contains(message)) {
                    itemView.setSelected(true);
                } else {
                    itemView.setSelected(false);
                }
                showReply(message, replyLayout, txtReplyUserName, txtReplyMsg, imgReply);
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

            void bind(final GroupMessage message) {
                setSectionMessage(mContext, message, timeText);
            }
        }

    }

    private void setSectionMessage(Context mContext, GroupMessage message, TextView timeText) {
        timeText.setText(message.message);
        if (message.messageType.equalsIgnoreCase("change_number")) {

            timeText.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    HashMap<String, String> map = ApplicationClass.getContactrNot(getApplicationContext(), message.contactPhoneNo);
                    if (map.get("isAlready").equals("false")) {
                        showAlertDialog(message);
                    } else {
                        makeToast(getString(R.string.contact_already_exists));
                    }
                }
            });
        } else {
            timeText.setOnClickListener(null);
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
                            if (selectedChatPos.size() == 0) {
                                chatUserLay.setVisibility(View.VISIBLE);
                                forwordLay.setVisibility(View.GONE);
                                chatLongPressed = false;
                            }
                            if (selectedChatPos.size() > 1)
                                optionBtnForward.setVisibility(View.GONE);
                            else if (selectedChatPos.size() == 1 && !selectedChatPos.get(0).memberId.equals(GetSet.getUserId())) {
                                optionBtnForward.setVisibility(View.VISIBLE);
                                replyPrivateTo = selectedChatPos.get(0);
                            } else {
                                replyPrivateTo = null;
                            }
                        } else {
//                            selectedChatPos.clear();
                            optionBtnForward.setVisibility(View.GONE);
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
                            if (selectedChatPos.size() == 1 && !selectedChatPos.get(0).memberId.equals(GetSet.getUserId())) {
                                optionBtnForward.setVisibility(View.VISIBLE);
                                replyPrivateTo = selectedChatPos.get(0);
                            } else {
                                replyPrivateTo = null;
                            }
                        }
                        messageListAdapter.notifyDataSetChanged();
                    }
                }
            }
        });
    }

    public void showAlertDialog(GroupMessage message) {
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
        title.setText(R.string.do_you_want_to_add_contact);
        no.setVisibility(View.VISIBLE);

        yes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
                Intent intent = new Intent(ContactsContract.Intents.Insert.ACTION);
                intent.setType(ContactsContract.RawContacts.CONTENT_TYPE);
                intent.putExtra(ContactsContract.Intents.Insert.PHONE, message.contactPhoneNo);
                intent.putExtra(ContactsContract.Intents.Insert.NAME, message.contactName);
                startActivity(intent);
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

    private boolean isForwardable(GroupMessage mData) {
        if ((mData.messageType.equals("video") || mData.messageType.equals("document") ||
                mData.messageType.equals("audio"))) {
            if (!mData.progress.equals("completed")) {
                return false;
            } else if (!mData.memberId.equals(GetSet.getUserId()) && !storageManager.checkifFileExists(mData.attachment, mData.messageType, "receive")) {
                return false;
            }
            return true;
        } else if (mData.messageType.equals("image") && !mData.progress.equals("completed")) {
            if (!mData.progress.equals("completed")) {
                return false;
            } else if (!mData.memberId.equals(GetSet.getUserId()) && !storageManager.checkifImageExists("receive", mData.attachment)) {
                return false;
            }
            return true;
        } else {
            return true;
        }
    }

    private void emitImage(GroupMessage mdata) {
        try {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put(Constants.TAG_GROUP_ID, groupId);
            jsonObject.put(Constants.TAG_GROUP_NAME, groupName);
            jsonObject.put(Constants.TAG_CHAT_TYPE, TAG_GROUP);
            jsonObject.put(Constants.TAG_MEMBER_ID, GetSet.getUserId());
            jsonObject.put(Constants.TAG_MEMBER_NAME, GetSet.getUserName());
            jsonObject.put(Constants.TAG_MEMBER_NO, GetSet.getphonenumber());
            jsonObject.put(Constants.TAG_MESSAGE_ID, mdata.messageId);
            jsonObject.put(Constants.TAG_MESSAGE_TYPE, mdata.messageType);
            jsonObject.put(Constants.TAG_MESSAGE, mdata.message);
            jsonObject.put(Constants.TAG_ATTACHMENT, mdata.attachment);
            jsonObject.put(Constants.TAG_CHAT_TIME, mdata.chatTime);
            jsonObject.put(Constants.TAG_REPLY_TO, msgReplyTo);

            socketConnection.startGroupChat(jsonObject);
            btnCancelReply.callOnClick();
        } catch (JSONException e) {
            e.printStackTrace();
        }

    }

    private void emitLocation(String type, String lat, String lon) {
        String unixStamp = String.valueOf(System.currentTimeMillis() / 1000L);
        RandomString randomString = new RandomString(10);
        String messageId = groupId + randomString.nextString();
        try {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put(Constants.TAG_GROUP_ID, groupId);
            jsonObject.put(Constants.TAG_GROUP_NAME, groupName);
            jsonObject.put(Constants.TAG_CHAT_TYPE, TAG_GROUP);
            jsonObject.put(Constants.TAG_MEMBER_ID, GetSet.getUserId());
            jsonObject.put(Constants.TAG_MEMBER_NAME, GetSet.getUserName());
            jsonObject.put(Constants.TAG_MEMBER_NO, GetSet.getphonenumber());
            jsonObject.put(Constants.TAG_MESSAGE_ID, messageId);
            jsonObject.put(Constants.TAG_MESSAGE_TYPE, type);
            jsonObject.put(Constants.TAG_MESSAGE, getString(R.string.location));
            jsonObject.put(Constants.TAG_LAT, lat);
            jsonObject.put(Constants.TAG_LON, lon);
            jsonObject.put(Constants.TAG_CHAT_TIME, unixStamp);
            jsonObject.put(Constants.TAG_REPLY_TO, msgReplyTo);
            socketConnection.startGroupChat(jsonObject);

            dbhelper.addGroupMessages(messageId, groupId, GetSet.getUserId(), "", type,
                    getString(R.string.location), "", lat, lon,
                    "", "", "",
                    unixStamp, "", "read", msgReplyTo);

            dbhelper.addGroupRecentMsgs(groupId, messageId, GetSet.getUserId(), unixStamp, "0");
        } catch (JSONException e) {
            e.printStackTrace();
        }

        GroupMessage data = new GroupMessage();
        data.messageId = messageId;
        data.groupId = groupId;
        data.memberId = GetSet.getUserId();
        data.messageType = type;
        data.message = getString(R.string.location);
        data.lat = lat;
        data.lon = lon;
        data.chatTime = unixStamp;
        data.deliveryStatus = "";
        data.reply_to = msgReplyTo;
        messagesList.add(0, data);
        messageListAdapter.notifyItemInserted(0);
        recyclerView.smoothScrollToPosition(0);
        btnCancelReply.callOnClick();
    }

    public String firstThree(String str) {
        return str.length() < 3 ? str : str.substring(0, 3);
    }

    private void emitContact(String type, String name, String phone, String countrycode) {
        String unixStamp = String.valueOf(System.currentTimeMillis() / 1000L);
        RandomString randomString = new RandomString(10);
        String messageId = groupId + randomString.nextString();
        try {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put(Constants.TAG_GROUP_ID, groupId);
            jsonObject.put(Constants.TAG_GROUP_NAME, groupName);
            jsonObject.put(Constants.TAG_CHAT_TYPE, TAG_GROUP);
            jsonObject.put(Constants.TAG_MEMBER_ID, GetSet.getUserId());
            jsonObject.put(Constants.TAG_MEMBER_NAME, GetSet.getUserName());
            jsonObject.put(Constants.TAG_MEMBER_NO, GetSet.getphonenumber());
            jsonObject.put(Constants.TAG_MESSAGE_ID, messageId);
            jsonObject.put(Constants.TAG_MESSAGE_TYPE, type);
            jsonObject.put(Constants.TAG_MESSAGE, getString(R.string.contact));
            jsonObject.put(Constants.TAG_CONTACT_NAME, name);
            jsonObject.put(Constants.TAG_CONTACT_PHONE_NO, phone);
            jsonObject.put(Constants.TAG_CONTACT_COUNTRY_CODE, countrycode);
            jsonObject.put(Constants.TAG_CHAT_TIME, unixStamp);
            jsonObject.put(Constants.TAG_REPLY_TO, msgReplyTo);

            socketConnection.startGroupChat(jsonObject);

            dbhelper.addGroupMessages(messageId, groupId, GetSet.getUserId(), "", type,
                    getString(R.string.contact), "", "", "", name, phone, countrycode,
                    unixStamp, "", "read", msgReplyTo);

            dbhelper.addGroupRecentMsgs(groupId, messageId, GetSet.getUserId(), unixStamp, "0");
        } catch (JSONException e) {
            e.printStackTrace();
        }

        GroupMessage data = new GroupMessage();
        data.memberId = GetSet.getUserId();
        data.messageType = type;
        data.message = getString(R.string.contact);
        data.contactName = name;
        data.contactPhoneNo = phone;
        data.contactCountryCode = countrycode;
        data.messageId = messageId;
        data.chatTime = unixStamp;
        data.deliveryStatus = "";
        data.reply_to = msgReplyTo;
        messagesList.add(0, data);
        messageListAdapter.notifyItemInserted(0);
        recyclerView.smoothScrollToPosition(0);
        btnCancelReply.callOnClick();
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
                dbhelper.deleteGroupMessages(groupId);
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

    private void deleteMessageConfirmDialog(ArrayList<GroupMessage> mData) {
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
                dbhelper.deleteGroupMessageFromId(mData);
                for (int i = 0; i < mData.size(); i++)
                    messagesList.remove(mData.get(i));
                Toast.makeText(GroupChatActivity.this, getString(R.string.message_deleted), Toast.LENGTH_SHORT).show();
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

                if (dbhelper.isGroupHaveAdmin(groupId) == 1) {
                    GroupData.GroupMembers memberData = dbhelper.getAdminFromMembers(groupId);
                    if (memberData.memberId.equalsIgnoreCase(GetSet.getUserId())) {
                        Log.v(TAG, "No admin");
                        for (GroupData.GroupMembers members : dbhelper.getGroupMembers(getApplicationContext(), groupId)) {
                            if (!members.memberId.equals(GetSet.getUserId())) {
                                JSONArray jsonArray = new JSONArray();
                                try {
                                    JSONObject jsonObject = new JSONObject();
                                    jsonObject.put(TAG_MEMBER_ID, members.memberId);
                                    jsonObject.put(TAG_MEMBER_ROLE, TAG_ADMIN);
                                    jsonArray.put(jsonObject);
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }

                                try {
                                    String unixStamp = String.valueOf(System.currentTimeMillis() / 1000L);
                                    RandomString randomString = new RandomString(10);
                                    String messageId = groupId + randomString.nextString();

                                    JSONObject message = new JSONObject();
                                    message.put(Constants.TAG_GROUP_ID, groupId);
                                    message.put(Constants.TAG_GROUP_NAME, groupName);
                                    message.put(Constants.TAG_CHAT_TYPE, Constants.TAG_GROUP);
                                    message.put(Constants.TAG_CHAT_TIME, unixStamp);
                                    message.put(Constants.TAG_MESSAGE_ID, messageId);
                                    message.put(Constants.TAG_ATTACHMENT, "1");
                                    message.put(Constants.TAG_MEMBER_ID, members.memberId);
                                    message.put(Constants.TAG_MEMBER_NAME, "");
                                    message.put(Constants.TAG_MEMBER_NO, members.memberNo);
                                    message.put(Constants.TAG_MESSAGE_TYPE, "admin");
                                    message.put(Constants.TAG_MESSAGE, getString(R.string.admin));
                                    message.put(Constants.TAG_GROUP_ADMIN_ID, GetSet.getUserId());
                                    message.put(Constants.TAG_REPLY_TO, msgReplyTo);
                                    socketConnection.startGroupChat(message);
                                    btnCancelReply.callOnClick();
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                                updateGroupData(jsonArray);
                                break;
                            }
                        }
                    }
                }

                try {
                    String unixStamp = String.valueOf(System.currentTimeMillis() / 1000L);
                    RandomString randomString = new RandomString(10);
                    String messageId = groupId + randomString.nextString();

                    JSONObject message = new JSONObject();
                    message.put(Constants.TAG_GROUP_ID, groupId);
                    message.put(Constants.TAG_GROUP_NAME, groupName);
                    message.put(Constants.TAG_CHAT_TYPE, Constants.TAG_GROUP);
                    message.put(Constants.TAG_CHAT_TIME, unixStamp);
                    message.put(Constants.TAG_MESSAGE_ID, messageId);
                    message.put(Constants.TAG_MEMBER_ID, GetSet.getUserId());
                    message.put(Constants.TAG_MEMBER_NAME, GetSet.getUserName());
                    message.put(Constants.TAG_MEMBER_NO, GetSet.getphonenumber());
                    message.put(Constants.TAG_MESSAGE_TYPE, getString(R.string.left));
                    message.put(Constants.TAG_MESSAGE, getString(R.string.one_participant_left));
                    message.put(Constants.TAG_GROUP_ADMIN_ID, groupData.groupAdminId);
                    message.put(Constants.TAG_REPLY_TO, msgReplyTo);
                    socketConnection.startGroupChat(message);

                    JSONObject jsonObject = new JSONObject();
                    jsonObject.put(TAG_GROUP_ID, groupId);
                    jsonObject.put(TAG_MEMBER_ID, GetSet.getUserId());
                    socketConnection.exitFromGroup(jsonObject);
                    btnCancelReply.callOnClick();
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

    public static String getFormattedDate(Context context, long smsTimeInMilis) {
        Calendar smsTime = Calendar.getInstance();
        smsTime.setTimeInMillis(smsTimeInMilis * 1000L);

        Calendar now = Calendar.getInstance();

        final String dateTimeFormatString = "d MMMM yyyy";
        if (now.get(Calendar.DATE) == smsTime.get(Calendar.DATE)) {
            return context.getString(R.string.today);
        } else if (now.get(Calendar.DATE) - smsTime.get(Calendar.DATE) == 1) {
            return context.getString(R.string.yesterday);
        } else {
            return DateFormat.format(dateTimeFormatString, smsTime).toString();
        }
    }

    private void uploadImage(byte[] imageBytes, final String imagePath, final GroupMessage mdata, final String filePath) {
        RequestBody requestFile = RequestBody.create(MediaType.parse("image/*"), imageBytes);
        MultipartBody.Part body = MultipartBody.Part.createFormData("group_attachment", "image.jpg", requestFile);

        RequestBody userid = RequestBody.create(MediaType.parse("multipart/form-data"), GetSet.getUserId());
        Call<GroupImageModel> call3 = apiInterface.upMyGroupChat(body, userid, null);
        call3.enqueue(new Callback<GroupImageModel>() {
            @Override
            public void onResponse(Call<GroupImageModel> call, Response<GroupImageModel> response) {
                GroupImageModel data = response.body();
                Log.v(TAG, "uploadImageresponse=" + data);
                if (data != null && data.getSTATUS().equals(TRUE)) {
                    //File dir = new File(getExternalFilesDir(null) + "/" + getString(R.string.app_name) + "/" + getString(R.string.app_name) + "Images/Sent");
                    File dir = new File(StorageManager.getDataRoot() + "/" + getString(R.string.app_name) + "/" + getString(R.string.app_name) + "Images/Sent");

                    if (dir.exists()) {
                        File from = new File(imagePath);
                        File to = new File(dir + "/" + data.getRESULT().getUserImage());
                        if (from.exists()) {
                            try {
                                FileUtils.copyFile(from, to);
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }

                        File file = storageManager.getImage("sent", data.getRESULT().getUserImage());

                        final int imgSize = ApplicationClass.dpToPx(GroupChatActivity.this, 170);
                        Log.v("file path", "file path=" + file.getAbsolutePath());

                        Bitmap bitmap = ImageUtils.compressImage(file.getAbsolutePath(), imgSize, imgSize);
                        String imgstatus = storageManager.saveThumbNail(bitmap, data.getRESULT().getUserImage());
                        if (mdata.messageType.equals("image")) {
                            if (imgstatus.equals("success")) {
                                dbhelper.updateGroupMessageData(mdata.messageId, Constants.TAG_ATTACHMENT, data.getRESULT().getUserImage());
                                dbhelper.updateGroupMessageData(mdata.messageId, Constants.TAG_PROGRESS, "completed");
                                if (messageListAdapter != null) {
                                    for (int i = 0; i < messagesList.size(); i++) {
                                        if (mdata.messageId.equals(messagesList.get(i).messageId)) {
                                            messagesList.get(i).attachment = data.getRESULT().getUserImage();
                                            messagesList.get(i).progress = "completed";
                                            messageListAdapter.notifyItemChanged(i);
                                            break;
                                        }
                                    }
                                }
                            }
                            mdata.attachment = data.getRESULT().getUserImage();
                            emitImage(mdata);
                        } else if (mdata.messageType.equals("video")) {
                            Log.v("checkChat", "uploadImage-video");
                            if (imgstatus.equals("success")) {
                                mdata.thumbnail = data.getRESULT().getUserImage();
                                dbhelper.updateGroupMessageData(mdata.messageId, Constants.TAG_THUMBNAIL, data.getRESULT().getUserImage());
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
                            Intent service = new Intent(GroupChatActivity.this, FileUploadService.class);
                            Bundle b = new Bundle();
                            b.putSerializable("mdata", mdata);
                            b.putString("filepath", filePath);
                            b.putString("chatType", "group");
                            service.putExtras(b);
                            startService(service);
                        }
                    }
                } else {
                    dbhelper.updateGroupMessageData(mdata.messageId, Constants.TAG_PROGRESS, "error");
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
            public void onFailure(Call<GroupImageModel> call, Throwable t) {
                Log.v(TAG, "onFailure=" + "onFailure");
                dbhelper.updateGroupMessageData(mdata.messageId, Constants.TAG_PROGRESS, "error");
                if (messageListAdapter != null) {
                    for (int i = 0; i < messagesList.size(); i++) {
                        if (mdata.messageId.equals(messagesList.get(i).messageId)) {
                            messagesList.get(i).progress = "error";
                            messageListAdapter.notifyItemChanged(i);
                            break;
                        }
                    }
                }
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

    private GroupMessage updateDBList(String type, String imagePath, String filePath) {
        String unixStamp = String.valueOf(System.currentTimeMillis() / 1000L);
        RandomString randomString = new RandomString(10);
        String messageId = groupId + randomString.nextString();

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

        GroupMessage groupMessage = new GroupMessage();
        groupMessage.groupId = groupId;
        groupMessage.groupName = groupName;
        groupMessage.memberId = GetSet.getUserId();
        groupMessage.memberName = GetSet.getUserName();
        groupMessage.memberNo = GetSet.getphonenumber();
        groupMessage.messageType = type;
        groupMessage.message = msg;
        groupMessage.messageId = messageId;
        groupMessage.chatTime = unixStamp;
        groupMessage.deliveryStatus = "";
        groupMessage.progress = "";

        if (type.equals("video")) {
            groupMessage.thumbnail = imagePath;
            groupMessage.attachment = filePath;
            dbhelper.addGroupMessages(messageId, groupId, GetSet.getUserId(), "",
                    type, msg, filePath, "", "", "", "",
                    "", unixStamp, imagePath, "read", msgReplyTo);
        } else if (type.equals("image")) {
            groupMessage.thumbnail = "";
            groupMessage.attachment = imagePath;
            dbhelper.addGroupMessages(messageId, groupId, GetSet.getUserId(), "",
                    type, msg, imagePath, "", "", "", "",
                    "", unixStamp, "", "read", msgReplyTo);
        } else {
            groupMessage.thumbnail = "";
            groupMessage.attachment = filePath;
            dbhelper.addGroupMessages(messageId, groupId, GetSet.getUserId(), "",
                    type, msg, filePath, "", "", "", "",
                    "", unixStamp, "", "read", msgReplyTo);
        }

        dbhelper.addGroupRecentMsgs(groupId, messageId, GetSet.getUserId(), unixStamp, "0");

        messagesList.add(0, groupMessage);
        messageListAdapter.notifyItemInserted(0);
        recyclerView.smoothScrollToPosition(0);

        return groupMessage;
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
            int permissionCamera = ContextCompat.checkSelfPermission(GroupChatActivity.this,
                    CAMERA);
            int permissionAudio = ContextCompat.checkSelfPermission(GroupChatActivity.this,
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
            int permissionCamera = ContextCompat.checkSelfPermission(GroupChatActivity.this,
                    CAMERA);
            int permissionAudio = ContextCompat.checkSelfPermission(GroupChatActivity.this,
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
            int permissionStorage = ContextCompat.checkSelfPermission(GroupChatActivity.this, WRITE_EXTERNAL_STORAGE);

            if (permissionStorage == PackageManager.PERMISSION_GRANTED) {
                ImagePicker.pickImage(this, getString(R.string.select_your_image));
            }
        } else if (requestCode == 106) {
            int permissionCamera = ContextCompat.checkSelfPermission(GroupChatActivity.this,
                    CAMERA);
            int permissionStorage = ContextCompat.checkSelfPermission(GroupChatActivity.this,
                    WRITE_EXTERNAL_STORAGE);

            if (permissionCamera == PackageManager.PERMISSION_GRANTED &&
                    permissionStorage == PackageManager.PERMISSION_GRANTED) {
                if (isNetworkConnected().equals(NOT_CONNECT)) {
                    networkSnack();
                } else {
                    ApplicationClass.onShareExternal = true;
                    ImagePicker.pickImageCameraOnly(this, 104);
                }
            }
        } else if (requestCode == 107) {
            int permissionCamera = ContextCompat.checkSelfPermission(GroupChatActivity.this,
                    CAMERA);
            int permissionStorage = ContextCompat.checkSelfPermission(GroupChatActivity.this,
                    WRITE_EXTERNAL_STORAGE);

            if (permissionCamera == PackageManager.PERMISSION_GRANTED &&
                    permissionStorage == PackageManager.PERMISSION_GRANTED) {
                if (isNetworkConnected().equals(NOT_CONNECT)) {
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
            } else {
                makeToast(getString(R.string.storage_permission_error));
            }
        } else if (requestCode == 108) {
            int permissionStorage = ContextCompat.checkSelfPermission(GroupChatActivity.this,
                    WRITE_EXTERNAL_STORAGE);

            if (permissionStorage == PackageManager.PERMISSION_GRANTED) {
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
            } else {
                makeToast(getString(R.string.storage_permission_error));
            }
        } else if (requestCode == 109) {
            int permissionStorage = ContextCompat.checkSelfPermission(GroupChatActivity.this,
                    WRITE_EXTERNAL_STORAGE);

            if (permissionStorage == PackageManager.PERMISSION_GRANTED) {
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
            } else {
                makeToast(getString(R.string.storage_permission_error));
            }
        } else if (requestCode == 110) {
            int permissionContacts = ContextCompat.checkSelfPermission(GroupChatActivity.this,
                    READ_CONTACTS);

            if (permissionContacts == PackageManager.PERMISSION_GRANTED) {
                if (isNetworkConnected().equals(NOT_CONNECT)) {
                    networkSnack();
                } else {
                    ApplicationClass.onShareExternal = true;
                    Intent intentc = new Intent(Intent.ACTION_PICK, ContactsContract.CommonDataKinds.Phone.CONTENT_URI);
                    startActivityForResult(intentc, 13);
                }
            } else {
                makeToast(getString(R.string.storage_permission_error));
            }
        }
    }

    @Override
    protected void onActivityResult(final int requestCode, final int resultCode, final Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.v(TAG, "onActivityResult");
        if (resultCode == -1 && requestCode == 104) {
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
                    ImageCompression imageCompression = new ImageCompression(GroupChatActivity.this) {
                        @Override
                        protected void onPostExecute(String imagePath) {
                            try {
                                GroupMessage mdata = updateDBList("image", imagePath, "");
                                byte[] bytes = org.apache.commons.io.FileUtils.readFileToByteArray(new File(imagePath));
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
        } else if (resultCode == -1 && requestCode == 234) {
            try {
                Uri uri = data.getData();
                String picturePath = getPath(GroupChatActivity.this, uri);
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
                    ImageCompression imageCompression = new ImageCompression(GroupChatActivity.this) {
                        @Override
                        protected void onPostExecute(String imagePath) {
                            try {
                                Uri imageUri = Uri.fromFile(file);
                                Glide.with(GroupChatActivity.this)
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
        } else if (data != null && resultCode == -1 && requestCode == 150) {
            if (isNetworkConnected().equals(NOT_CONNECT)) {
                networkSnack();
            } else {
                pathsAry = new ArrayList<>();
                pathsAry.addAll(data.getParcelableArrayListExtra(FilePickerConst.KEY_SELECTED_MEDIA));
                if (pathsAry.size() > 0) {
                    Log.v(TAG, "File");
                    String filepath = null;
                    try {
                        filepath = ContentUriUtils.INSTANCE.getFilePath(getApplicationContext(), pathsAry.get(0));
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
                                    GroupMessage mdata = updateDBList("video", imagePath, filepath);
                                    byte[] bytes = org.apache.commons.io.FileUtils.readFileToByteArray(new File(imagePath));
                                    uploadImage(bytes, imagePath, mdata, filepath);
                                }
                            }

                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    } else {
                        ImageCompression imageCompression = new ImageCompression(GroupChatActivity.this) {
                            @Override
                            protected void onPostExecute(String imagePath) {
                                try {
                                    Log.v("checkChat", "imagepath=" + imagePath);
                                    GroupMessage mdata = updateDBList("image", imagePath, "");
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
                        GroupMessage mdata = updateDBList("document", "", filepath);
                        Intent service = new Intent(GroupChatActivity.this, FileUploadService.class);
                        Bundle b = new Bundle();
                        b.putSerializable("mdata", mdata);
                        b.putString("filepath", filepath);
                        b.putString("chatType", "group");
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
                        GroupMessage mdata = updateDBList("audio", "", filepath);
                        Intent service = new Intent(GroupChatActivity.this, FileUploadService.class);
                        Bundle b = new Bundle();
                        b.putSerializable("mdata", mdata);
                        b.putString("filepath", filepath);
                        b.putString("chatType", "group");
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
            username.setText(groupName);
        } else if (resultCode == RESULT_OK && requestCode == 222) {
            selectedChatPos.clear();
            messageListAdapter.notifyDataSetChanged();
            chatUserLay.setVisibility(View.VISIBLE);
            forwordLay.setVisibility(View.GONE);
            chatLongPressed = false;
        }
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
        if (getIntent().getStringExtra(TAG_GROUP_ID) != null)
            groupId = getIntent().getStringExtra(TAG_GROUP_ID);
        tempGroupId = groupId;

        boolean isIncomingCall = SharedPrefsHelper.getInstance().get(Constants.EXTRA_IS_INCOMING_CALL, false);
        if (isCallServiceRunning(CallService.class)) {
            Log.d(TAG, "CallService is running now");
            CallActivity.start(this, isIncomingCall);
        }
        loadUsers();

        if (dbhelper.getGroupData(this, groupId) != null) {
            groupData = dbhelper.getGroupData(this, groupId);
            groupName = groupData.groupName;
            username.setText(groupData.groupName);
            Glide.with(GroupChatActivity.this).load(Constants.GROUP_IMG_PATH + groupData.groupImage)
                    .apply(RequestOptions.circleCropTransform().placeholder(R.drawable.create_group).error(R.drawable.create_group))
                    .into(userimage);
            setGroupMembers(groupId);
        } else {
            finish();
        }

        if (getIntent().getStringExtra("EXTRA_EDITED_PATH") != null) {
            editImagePath = getIntent().getStringExtra("EXTRA_EDITED_PATH");
            groupId = pref.getString("sendGrpImage", "");
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
                                GroupMessage mdata = updateDBList("video", imagePath, filepath);
                                byte[] bytes = org.apache.commons.io.FileUtils.readFileToByteArray(new File(imagePath));
                                uploadImage(bytes, imagePath, mdata, filepath);
                            }
                        }

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                } else {
                    ImageCompression imageCompression = new ImageCompression(GroupChatActivity.this) {
                        @Override
                        protected void onPostExecute(String imagePath) {
                            try {
                                Log.v("checkChat", "imagepath=" + imagePath);
                                GroupMessage mdata = updateDBList("image", imagePath, "");
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
            }
        } else {
            groupId = getIntent().getStringExtra(TAG_GROUP_ID);
        }


        ApplicationClass.onShareExternal = false;
    }

    @Override
    public void onPause() {
        tempGroupId = "";
        editText.setError(null);
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.v("onDestroy", "onDestroy");
        if (Constants.isGroupChatOpened) {
            Constants.isGroupChatOpened = false;
        }
        SocketConnection.getInstance(this).setGroupChatCallbackListener(null);
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
            } else if (behavior != null && behavior.getState() == BottomSheetBehavior.STATE_EXPANDED) {
                behavior.setState(BottomSheetBehavior.STATE_HIDDEN);
            } else if (newBehavior != null && newBehavior.getState() == BottomSheetBehavior.STATE_EXPANDED) {
                newBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);
            } else {
                if (isFromNotification) {
                    startActivity(new Intent(getApplicationContext(), MainActivity.class));
                }
                finish();
            }
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.send:
                if (isNetworkConnected().equals(NOT_CONNECT)) {
                    networkSnack();
                } else if (dbhelper.isMemberExist(GetSet.getUserId(), groupId)) {
                    if (editText.getText().toString().trim().length() > 0) {
                        String unixStamp = String.valueOf(System.currentTimeMillis() / 1000L);
                        String textMsg = editText.getText().toString().trim();
                        RandomString randomString = new RandomString(10);
                        String messageId = groupId + randomString.nextString();
                        try {
                            JSONObject jsonObject = new JSONObject();
                            jsonObject.put(Constants.TAG_GROUP_ID, groupId);
                            jsonObject.put(Constants.TAG_GROUP_NAME, groupName);
                            jsonObject.put(Constants.TAG_CHAT_TYPE, TAG_GROUP);
                            jsonObject.put(Constants.TAG_MEMBER_ID, GetSet.getUserId());
                            jsonObject.put(Constants.TAG_MEMBER_NAME, GetSet.getUserName());
                            jsonObject.put(Constants.TAG_MEMBER_NO, GetSet.getphonenumber());
                            jsonObject.put(Constants.TAG_MESSAGE_ID, messageId);
                            jsonObject.put(Constants.TAG_MESSAGE_TYPE, "text");
                            jsonObject.put(Constants.TAG_MESSAGE, textMsg);
                            jsonObject.put(Constants.TAG_CHAT_TIME, unixStamp);
                            jsonObject.put(Constants.TAG_REPLY_TO, msgReplyTo);

                            socketConnection.startGroupChat(jsonObject);

                            dbhelper.addGroupMessages(messageId, groupId, GetSet.getUserId(), "", "text",
                                    textMsg, "", "", "", "", "",
                                    "", unixStamp, "", "read", msgReplyTo);

                            dbhelper.addGroupRecentMsgs(groupId, messageId, GetSet.getUserId(), unixStamp, "0");
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                        GroupMessage groupMessage = new GroupMessage();
                        groupMessage.memberId = GetSet.getUserId();
                        groupMessage.messageType = "text";
                        groupMessage.message = textMsg;
                        groupMessage.messageId = messageId;
                        groupMessage.chatTime = unixStamp;
                        groupMessage.reply_to = msgReplyTo;
                        groupMessage.deliveryStatus = "";
                        messagesList.add(0, groupMessage);
                        messageListAdapter.notifyItemInserted(0);
                        recyclerView.smoothScrollToPosition(0);
                        editText.setText("");
                        btnCancelReply.callOnClick();
                    } else {
                        editText.setError(getString(R.string.please_enter_your_message));
                    }
                } else {
                    makeToast(getString(R.string.you_are_no_longer_member_in_this_group));
                }
                break;
            case R.id.backbtn:
                if (dialog1 != null && dialog1.isShowing())
                    dialog1.dismiss();
                else
                    onBackPressed();
                break;
            case R.id.videoCallBtn:
            case R.id.audioCallBtn:
                Constants.qbUsersList.clear();
                selectedOppList.clear();
                selectedUser.clear();
                usersAdapter.notifyDataSetChanged();
                selectedUsersAdapter.notifyDataSetChanged();
                newBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
                break;
            case R.id.videoCallBtn1:
                newBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);
                if (checkIsLoggedInChat()) {
                    startCall(true);
                }
                break;
            case R.id.audioCallBtn1:
                newBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);
                if (checkIsLoggedInChat()) {
                    startCall(false);
                }
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
            case R.id.option_btn_forward:
                Display display1 = this.getWindowManager().getDefaultDisplay();
                final ArrayList<String> values1 = new ArrayList<>();
                values1.add(getString(R.string.reply_privatly));
                final ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
                        R.layout.option_item, android.R.id.text1, values1);
                LayoutInflater layoutInflater1 = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                View layout1 = layoutInflater1.inflate(R.layout.option_layout, null);
                layout1.setAnimation(AnimationUtils.loadAnimation(this, R.anim.grow_from_topright_to_bottomleft));
                final PopupWindow popup = new PopupWindow(GroupChatActivity.this);
                popup.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                popup.setContentView(layout1);
                popup.setWidth(display1.getWidth() * 60 / 100);
                popup.setHeight(ViewGroup.LayoutParams.WRAP_CONTENT);
                popup.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_NOTHING);
                popup.setFocusable(true);
                popup.showAtLocation(mainLay, Gravity.TOP | Gravity.RIGHT, ApplicationClass.dpToPx(this, 10), ApplicationClass.dpToPx(this, 63));

                final ListView lv = layout1.findViewById(R.id.listView);
                lv.setAdapter(adapter);
                popup.showAsDropDown(v);

                lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {

                    @Override
                    public void onItemClick(AdapterView<?> parent, View view,
                                            int position, long id) {
                        popup.dismiss();
                        if (position == 0) {
                            Intent intent = new Intent(GroupChatActivity.this, ChatActivity.class);
                            intent.putExtra(USER_ID, replyPrivateTo.memberId);
                            intent.putExtra(TAG_REPLY_TO, replyPrivateTo.messageId);
                            intent.putExtra(TAG_GROUP_ID, groupId);
                            startActivity(intent);
                            finish();

                        }
                    }
                });
                break;
            case R.id.optionbtn:
                Display display = this.getWindowManager().getDefaultDisplay();
                final ArrayList<String> values = new ArrayList<>();
                GroupData results = dbhelper.getGroupData(GroupChatActivity.this, groupId);

                if (dbhelper.isMemberExist(GetSet.getUserId(), groupId)) {
                    if (results.muteNotification.equals("true")) {
                        values.add(getString(R.string.unmute_notification));
                    } else {
                        values.add(getString(R.string.mute_notification));
                    }
                    values.add(getString(R.string.clear_chat));
                    values.add(getString(R.string.exit_group));
                    values.add("Export Chat");
                    values.add("Wallpaper");
                } else {
                    values.add(getString(R.string.delete_group));
                }

                final ArrayAdapter<String> adapter1 = new ArrayAdapter<String>(this,
                        R.layout.option_item, android.R.id.text1, values);
                LayoutInflater layoutInflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                View layout = layoutInflater.inflate(R.layout.option_layout, null);
                layout.setAnimation(AnimationUtils.loadAnimation(this, R.anim.grow_from_topright_to_bottomleft));
                final PopupWindow popup1 = new PopupWindow(GroupChatActivity.this);
                popup1.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                popup1.setContentView(layout);
                popup1.setWidth(display.getWidth() * 60 / 100);
                popup1.setHeight(ViewGroup.LayoutParams.WRAP_CONTENT);
                popup1.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_NOTHING);
                popup1.setFocusable(true);
                popup1.showAtLocation(mainLay, Gravity.TOP | Gravity.RIGHT, ApplicationClass.dpToPx(this, 10), ApplicationClass.dpToPx(this, 63));

                final ListView lv1 = layout.findViewById(R.id.listView);
                lv1.setAdapter(adapter1);
                popup1.showAsDropDown(v);

                lv1.setOnItemClickListener(new AdapterView.OnItemClickListener() {

                    @Override
                    public void onItemClick(AdapterView<?> parent, View view,
                                            int position, long id) {
                        popup1.dismiss();
                        if (position == 0) {
                            if (dbhelper.isMemberExist(GetSet.getUserId(), groupId)) {
                                if (isNetworkConnected().equals(NOT_CONNECT)) {
                                    networkSnack();
                                } else {
                                    if (values.get(position).equalsIgnoreCase(getString(R.string.mute_notification))) {
                                        getMuteNotification();
                                        dbhelper.updateMuteGroup(groupId, "true");
                                        values.set(position, getString(R.string.unmute_notification));
                                    } else {
                                        JSONObject jsonObject = new JSONObject();
                                        try {
                                            JSONArray jsonArray = new JSONArray();
                                            JSONArray jsonArray1 = new JSONArray();
                                            jsonArray1.put(Integer.parseInt(groupId));
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
                                                                    Toast.makeText(GroupChatActivity.this, response.getString("msg"), Toast.LENGTH_SHORT).show();
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
                                        dbhelper.updateMuteGroup(groupId, "");
                                        values.set(position, getString(R.string.mute_notification));
                                    }
                                    adapter1.notifyDataSetChanged();
                                }
                            } else {
                                dbhelper.deleteMembers(groupId);
                                dbhelper.deleteGroupMessages(groupId);
                                dbhelper.deleteGroup(groupId);
                                finish();
                            }
                        } else if (position == 1) {
                            deleteChatConfirmDialog();
                        } else if (position == 2) {
                            if (isNetworkConnected().equals(NOT_CONNECT)) {
                                networkSnack();
                            } else {
                                exitConfirmDialog();
                            }
                        } else if (position == 3) {
                            new LongOperation().execute();
                        } else if (position == 4) {
                            File prefsdir = new File(getApplicationInfo().dataDir, "shared_prefs/SavedPref.xml");
                            Log.e("LLLLLL_XML: ", prefsdir.getAbsolutePath());
                            behavior.setState(BottomSheetBehavior.STATE_EXPANDED);
                        }
                    }
                });
                break;
            case R.id.attachbtn:
                TransitionManager.beginDelayedTransition(mainLay);
                visible = !visible;
                attachmentsLay.setVisibility(visible ? View.VISIBLE : View.GONE);
                break;
            case R.id.userImg:
                break;
            case R.id.cameraBtn:
                if (ContextCompat.checkSelfPermission(this, WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED ||
                        ContextCompat.checkSelfPermission(this, CAMERA) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(this, new String[]{WRITE_EXTERNAL_STORAGE, CAMERA}, 106);
                } else if (isNetworkConnected().equals(NOT_CONNECT)) {
                    networkSnack();
                } else {
                    ApplicationClass.onShareExternal = true;
                    ImagePicker.pickImageCameraOnly(this, 104);
                }
                break;
            case R.id.galleryBtn:
                if (ContextCompat.checkSelfPermission(this, WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED ||
                        ContextCompat.checkSelfPermission(this, CAMERA) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(this, new String[]{WRITE_EXTERNAL_STORAGE, CAMERA}, 107);
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
                if (ContextCompat.checkSelfPermission(this, WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(this, new String[]{WRITE_EXTERNAL_STORAGE}, 108);
                } else {
                    if (isNetworkConnected().equals(NOT_CONNECT)) {
                        networkSnack();
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
                    } else {
                        ApplicationClass.onShareExternal = true;
                        Intent intentc = new Intent(Intent.ACTION_PICK, ContactsContract.CommonDataKinds.Phone.CONTENT_URI);
                        startActivityForResult(intentc, 13);
                    }
                }
                break;
            case R.id.chatUserLay:
                Intent profile = new Intent(GroupChatActivity.this, GroupInfoActivity.class);
                profile.putExtra(Constants.TAG_GROUP_ID, groupId);
                startActivity(profile);
                break;
            case R.id.forwordBtn:
                Intent f = new Intent(GroupChatActivity.this, ForwardActivity.class);
                f.putExtra("from", "group");
                f.putExtra("data", selectedChatPos);
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
            case R.id.closeBtn:
                onBackPressed();
                break;
            case R.id.deleteBtn:
                deleteMessageConfirmDialog(selectedChatPos);
                break;
        }
    }

    private void getSolidColorDialoge() {
        dialog1 = new Dialog(GroupChatActivity.this, android.R.style.Theme_Light_NoTitleBar_Fullscreen);
        dialog1.setContentView(R.layout.dialoge_solid_color);
        dialog1.getWindow().setBackgroundDrawableResource(android.R.color.white);
        dialog1.getWindow().setLayout(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);
        dialog1.show();

        ImageView imgBack = dialog1.findViewById(R.id.img_back);
        RecyclerView rvSolidColor = dialog1.findViewById(R.id.rv_solid_color);

        rvSolidColor.setLayoutManager(new GridLayoutManager(GroupChatActivity.this, 3));
        rvSolidColor.setAdapter(solidColorAdapter);

        imgBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (dialog1.isShowing() && dialog1 != null)
                    dialog1.dismiss();
            }
        });
    }

    private final class LongOperation extends AsyncTask<Void, Void, String> {

        ProgressDialog progressDialog;

        public LongOperation() {
            this.progressDialog = new ProgressDialog(GroupChatActivity.this);
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressDialog.setMessage("Please Wait...");
            progressDialog.show();
        }

        @Override
        protected String doInBackground(Void... params) {
            generateNoteOnSD(GroupChatActivity.this);
            return "Executed";
        }

        @Override
        protected void onPostExecute(String result) {
            if (progressDialog.isShowing() && progressDialog != null)
                progressDialog.dismiss();

        }
    }

    public void generateNoteOnSD(Context context) {
        try {
            File root = new File(Environment.getExternalStorageDirectory(), "topzi/topzi doccuments");
            if (!root.exists()) {
                root.mkdirs();
            }
            results = dbhelper.getContactDetail(pref.getString("userId", ""));
            String File_name = ApplicationClass.getContactName(this, groupName) + ".txt";

            File gpxfile = new File(root, "Topzi Group chat with " + File_name);
            FileWriter writer = new FileWriter(gpxfile);

            ArrayList<GroupMessage> mesgData = new ArrayList<>(messagesList);
            Collections.reverse(mesgData);
            for (int i = 0; i < mesgData.size(); i++) {
                GroupMessage messagesData = mesgData.get(i);
                if (messagesData.message != null) {
                    Log.e("LLLLL_Name: ", messagesData.contactName + "    " + messagesData.memberId);
                    if (messagesData.memberId != null) {
                        if (dbhelper.getContactPhone(messagesData.memberId).equals("")) {
                            writer.append(ApplicationClass.getDateTime(Long.parseLong(messagesData.chatTime))).append(" - ").append(ApplicationClass.getContactName(this, messagesData.memberId)).append(": ").append(messagesData.message).append("\n");
                        } else {
                            if (!messagesData.memberId.equals(pref.getString("userId", "")))
                                writer.append(ApplicationClass.getDateTime(Long.parseLong(messagesData.chatTime))).append(" - ").append(ApplicationClass.getContactName(this, dbhelper.getContactPhone(messagesData.memberId))).append(": ").append(messagesData.message).append("\n");
                            else
                                writer.append(ApplicationClass.getDateTime(Long.parseLong(messagesData.chatTime))).append(" - ").append("You").append(": ").append(messagesData.message).append("\n");
                        }
                    }
                }
            }

            writer.flush();
            writer.close();

            final Uri data = FileProvider.getUriForFile(GroupChatActivity.this, "com.topzi.chat.provider", gpxfile);
            Intent sharingIntent = new Intent(Intent.ACTION_SEND);
            sharingIntent.setType("text/*");
            sharingIntent.putExtra(Intent.EXTRA_STREAM, data);
            startActivity(Intent.createChooser(sharingIntent, "share file with"));

        } catch (IOException e) {
            Log.e("LLLLLL_Error: ", Objects.requireNonNull(e.getMessage()));
            e.printStackTrace();
        }
    }

    private void getMuteNotification() {
        String[] backup_time = {"8 hours", "1 week", "1 year"};
        final int[] check = {0};
        dialog = new Dialog(GroupChatActivity.this);
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
        CheckBox checkbox = dialog.findViewById(R.id.checkbox);

        checkbox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (checkbox.isChecked()) {
                    check[0] = 1;
                } else {
                    check[0] = 0;
                }
            }
        });

        tv_ok.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isNetworkConnected().equals(NOT_CONNECT)) {
                    networkSnack();
                    dialog.dismiss();
                } else {
                    int checkedRadioButtonId = radioGrp.getCheckedRadioButtonId();
                    dbhelper.updateMuteUser(groupId, "true");
                    RadioButton radioBtn = (RadioButton) dialog.findViewById(checkedRadioButtonId);
                    editor.putString("mutenotification", String.valueOf(radioBtn.getText()));
                    editor.commit();

                    if (checkbox.isChecked()) {
                        check[0] = 1;
                    } else {
                        check[0] = 0;
                    }

                    JSONObject jsonObject = new JSONObject();
                    JSONArray jsonArray = new JSONArray();
                    JSONArray jsonArray1 = new JSONArray();
                    jsonArray1.put(Integer.parseInt(groupId));
                    try {
                        jsonObject.put("userId", pref.getString("userId", ""));
                        jsonObject.put("friendIds", jsonArray);
                        jsonObject.put("groupIds", jsonArray1);
                        jsonObject.put("mute", check[0]);
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
                                            Toast.makeText(GroupChatActivity.this, response.getString("msg"), Toast.LENGTH_SHORT).show();
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
                Toast.makeText(GroupChatActivity.this, pref.getString("mutenotification", ""), Toast.LENGTH_SHORT).show();
            }
        });

        dialog.show();

    }


    private boolean isAdmin() {
        boolean isAdmin = false;
        for (GroupData.GroupMembers members : groupData.groupMembers) {
            if (members.memberId.equalsIgnoreCase(GetSet.getUserId())) {
                isAdmin = members.memberRole.equalsIgnoreCase(TAG_ADMIN);
                break;
            }
        }
        return isAdmin;
    }

    public static GroupMessage getMessages(DatabaseHandler dbhelper, Context mContext, GroupMessage groupMessage) {
        if (groupMessage.messageType != null) {
            switch (groupMessage.messageType) {
                case "text":
                case "image":
                case "video":
                case "file":
                case "location":
                case "contact":
                case "audio":
                    groupMessage.message = groupMessage.message != null ? groupMessage.message : "";
                    break;
                case "create_group":
                    if (groupMessage.groupAdminId.equals(GetSet.getUserId())) {
                        groupMessage.message = mContext.getString(R.string.you_created_the_group);
                    } else {
                        if (dbhelper.isUserExist(groupMessage.groupAdminId)) {
                            groupMessage.message = ApplicationClass.getContactName(mContext, dbhelper.getContactPhone(groupMessage.groupAdminId)) + " " + mContext.getString(R.string.created_the_group);
                        } else {
                            groupMessage.message = mContext.getString(R.string.group_created);
                        }
                    }
                    break;
                case "add_member":
                    if (groupMessage.attachment.equals("")) {
                        if (dbhelper.isUserExist(groupMessage.groupAdminId)) {
                            groupMessage.message = ApplicationClass.getContactName(mContext, dbhelper.getContactPhone(groupMessage.groupAdminId)) + " " + mContext.getString(R.string.added_you);
                        } else {
                            groupMessage.message = mContext.getString(R.string.you_were_added);
                        }
                    } else {
                        try {
                            JSONArray jsonArray = new JSONArray(groupMessage.attachment);
                            ArrayList<String> members = new ArrayList<>();
                            for (int i = 0; i < jsonArray.length(); i++) {
                                JSONObject jsonObject = jsonArray.getJSONObject(i);
                                if (jsonObject.getString(TAG_MEMBER_ID).equals(GetSet.getUserId())) {
                                    members.add(mContext.getString(R.string.you));
                                } else if (dbhelper.isUserExist(jsonObject.getString(Constants.TAG_MEMBER_ID))) {
                                    members.add(ApplicationClass.getContactName(mContext, jsonObject.getString(TAG_MEMBER_NO)));
                                }
                            }
                            String memberstr = members.toString().replaceAll("[\\[\\]]|(?<=,)\\s+", "");
                            if (groupMessage.memberId.equals(GetSet.getUserId())) {
                                groupMessage.message = mContext.getString(R.string.you_added) + " " + memberstr;
                            } else {
                                groupMessage.message = ApplicationClass.getContactName(mContext, dbhelper.getContactPhone(groupMessage.groupAdminId)) + " " + mContext.getString(R.string.added) + " " + memberstr;
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                    break;
                case "group_image":
                    if (groupMessage.memberId.equalsIgnoreCase(GetSet.getUserId())) {
                        groupMessage.message = mContext.getString(R.string.you) + " " + groupMessage.message;
                    } else {
                        groupMessage.message = ApplicationClass.getContactName(mContext, dbhelper.getContactPhone(groupMessage.memberId)) + " " + groupMessage.message;
                    }
                    break;
                case "subject":
                    if (groupMessage.memberId.equalsIgnoreCase(GetSet.getUserId())) {
                        groupMessage.message = mContext.getString(R.string.you) + " " + groupMessage.message;
                    } else {
                        groupMessage.message = ApplicationClass.getContactName(mContext, dbhelper.getContactPhone(groupMessage.memberId)) + " " + groupMessage.message;
                    }
                    break;
                case "left":
                    if (groupMessage.memberId.equalsIgnoreCase(GetSet.getUserId())) {
                        groupMessage.message = mContext.getString(R.string.you_left);
                    } else {
                        groupMessage.message = ApplicationClass.getContactName(mContext, dbhelper.getContactPhone(groupMessage.memberId)) + " " + mContext.getString(R.string.left);
                    }
                    break;
                case "remove_member":
                    if (groupMessage.groupAdminId.equals(GetSet.getUserId())) {
                        groupMessage.message = mContext.getString(R.string.you_removed) + " " + ApplicationClass.getContactName(mContext, dbhelper.getContactPhone(groupMessage.memberId));
                    } else {
                        if (groupMessage.memberId.equals(GetSet.getUserId())) {
                            groupMessage.message = ApplicationClass.getContactName(mContext, dbhelper.getContactPhone(groupMessage.groupAdminId)) + " " + mContext.getString(R.string.removed_you);
                        } else {
                            groupMessage.message = ApplicationClass.getContactName(mContext, dbhelper.getContactPhone(groupMessage.groupAdminId)) + " " + mContext.getString(R.string.removed) + " " +
                                    ApplicationClass.getContactName(mContext, dbhelper.getContactPhone(groupMessage.memberId));
                        }
                    }
                    break;
                case "admin":
                    if (groupMessage.attachment.equals(TAG_MEMBER)) {
                        groupMessage.message = mContext.getString(R.string.you_are_no_longer_as_admin);
                    } else {
                        groupMessage.message = mContext.getString(R.string.you_are_now_an_admin);
                    }
                    break;
                case "date":
                    groupMessage.message = getFormattedDate(mContext, Long.parseLong(groupMessage.chatTime));
                    break;
                case "change_number":
                    if (!groupMessage.memberId.equals(GetSet.getUserId())) {
                        groupMessage.message = ApplicationClass.getContactName(mContext, groupMessage.attachment) + " " + groupMessage.message;
                    }
                    break;
            }
        } else {
            groupMessage.message = "";
        }

        return groupMessage;
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
                    for (int j = 0; j < membersList.size(); j++) {
                        final GroupData.GroupMembers memberResult = membersList.get(j);
                        final ContactsData.Result memberData = dbhelper.getContactDetail(memberResult.memberId);
                        if (qbUser.getLogin().equals(memberData.user_id))
                            Constants.qbUsersList.add(qbUser);
                    }
                }

            }

            @Override
            public void onError(QBResponseException e) {
                Log.d(TAG, "Error load users" + e.getMessage());
            }
        });
    }

    public class UsersAdapter extends RecyclerView.Adapter<UsersAdapter.ViewHolder> {

        private Context context;
        List<QBUser> usersList;
        List<ContactsData.Result> filterList;


        public UsersAdapter(Context context, List<QBUser> usersList, List<ContactsData.Result> filterList) {
            this.context = context;
            this.usersList = usersList;
            this.filterList = filterList;
        }

        @NonNull
        @Override
        public UsersAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            return new UsersAdapter.ViewHolder(LayoutInflater.from(context).inflate(R.layout.item_opponents_list, parent, false));
        }

        @Override
        public void onBindViewHolder(@NonNull UsersAdapter.ViewHolder holder, int position) {
            QBUser user = usersList.get(position);
            ContactsData.Result result = filterList.get(position);

            Log.e("LLLLL_Name: ",user.getFullName()+"   "+result.user_name);

            holder.opponentName.setText(result.user_name);
            if (Constants.qbUsersList.contains(user)) {
                holder.imgSelecte.setVisibility(View.VISIBLE);
//                holder.rootLayout.setBackgroundResource(R.color.background_color_selected_user_item);
                holder.opponentIcon.setBackgroundDrawable(
                        UiUtils.getColoredCircleDrawable(context.getResources().getColor(R.color.icon_background_color_selected_user)));
                holder.opponentIcon.setImageResource(R.drawable.ic_checkmark);
            } else {
                holder.imgSelecte.setVisibility(View.GONE);
//                holder.rootLayout.setBackgroundResource(R.color.background_color_normal_user_item);
                holder.opponentIcon.setBackgroundDrawable(UiUtils.getColorCircleDrawable(user.getId()));
                holder.opponentIcon.setImageResource(R.drawable.ic_person);
            }

            if (filterList.get(position).blockedme.equals("block")) {
                Glide.with(context).load(R.drawable.change_camera)
                        .apply(RequestOptions.circleCropTransform().placeholder(R.drawable.change_camera).error(R.drawable.change_camera).override(ApplicationClass.dpToPx(context, 70)))
                        .into(holder.opponentIcon);
            } else {
                DialogActivity.setProfileImage(dbhelper.getContactDetail(filterList.get(position).user_id), holder.opponentIcon, context);
            }

            holder.rootLayout.setOnClickListener(v -> toggleSelection(user, result));
        }

        @Override
        public int getItemCount() {
            return usersList.size();
        }

        public void updateUsersList(List<QBUser> usersList) {
            this.usersList = usersList;
            notifyDataSetChanged();
        }

        private void toggleSelection(QBUser qbUser, ContactsData.Result result) {

            if (Constants.qbUsersList.contains(qbUser)) {
                selectedUser.remove(result);
                Constants.qbUsersList.remove(qbUser);
                selectedOppList.remove(qbUser);
            } else {
                selectedUser.add(result);
                Constants.qbUsersList.add(qbUser);
                selectedOppList.add(qbUser);
            }

            selectedUsersAdapter.notifyDataSetChanged();
            notifyDataSetChanged();
        }

        public class ViewHolder extends RecyclerView.ViewHolder {
            ImageView opponentIcon;
            ImageView imgSelecte;
            TextView opponentName;
            LinearLayout rootLayout;

            public ViewHolder(@NonNull View view) {
                super(view);
                opponentIcon = view.findViewById(R.id.image_opponent_icon);
                imgSelecte = view.findViewById(R.id.imgSelecte);
                opponentName = view.findViewById(R.id.opponents_name);
                rootLayout = view.findViewById(R.id.root_layout);
            }
        }

    }

    public class SelectedUsersAdapter extends RecyclerView.Adapter<SelectedUsersAdapter.ViewHolder> {

        private Context context;
        List<QBUser> usersList;
        List<ContactsData.Result> filterList;


        public SelectedUsersAdapter(Context context, List<QBUser> usersList, List<ContactsData.Result> filterList) {
            this.context = context;
            this.usersList = usersList;
            this.filterList = filterList;
        }

        @NonNull
        @Override
        public SelectedUsersAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            return new SelectedUsersAdapter.ViewHolder(LayoutInflater.from(context).inflate(R.layout.selected_lay, parent, false));
        }

        @Override
        public void onBindViewHolder(@NonNull SelectedUsersAdapter.ViewHolder holder, int position) {
            QBUser user = usersList.get(position);
            ContactsData.Result result = filterList.get(position);

            Log.e("LLLLL_Name: ",user.getFullName()+"   "+result.user_name);

            if (Constants.qbUsersList.contains(user)) {
//                holder.rootLayout.setBackgroundResource(R.color.background_color_selected_user_item);
                holder.opponentIcon.setBackgroundDrawable(
                        UiUtils.getColoredCircleDrawable(context.getResources().getColor(R.color.icon_background_color_selected_user)));
                holder.opponentIcon.setImageResource(R.drawable.ic_checkmark);
            } else {
//                holder.rootLayout.setBackgroundResource(R.color.background_color_normal_user_item);
                holder.opponentIcon.setBackgroundDrawable(UiUtils.getColorCircleDrawable(user.getId()));
                holder.opponentIcon.setImageResource(R.drawable.ic_person);
            }

            if (filterList.get(position).blockedme.equals("block")) {
                Glide.with(context).load(R.drawable.change_camera)
                        .apply(RequestOptions.circleCropTransform().placeholder(R.drawable.change_camera).error(R.drawable.change_camera).override(ApplicationClass.dpToPx(context, 70)))
                        .into(holder.opponentIcon);
            } else {
                DialogActivity.setProfileImage(dbhelper.getContactDetail(filterList.get(position).user_id), holder.opponentIcon, context);
            }

            holder.rootLayout.setOnClickListener(v -> toggleSelection(user, result));
        }

        @Override
        public int getItemCount() {
            return selectedUser.size();
        }

        public void updateUsersList(List<QBUser> usersList) {
            this.usersList = usersList;
            notifyDataSetChanged();
        }

        private void toggleSelection(QBUser qbUser, ContactsData.Result result) {

            if (Constants.qbUsersList.contains(qbUser)) {
                selectedUser.remove(result);
                Constants.qbUsersList.remove(qbUser);
                selectedOppList.remove(qbUser);
            } else {
                selectedUser.add(result);
                Constants.qbUsersList.add(qbUser);
                selectedOppList.add(qbUser);
            }

            usersAdapter.notifyDataSetChanged();
            notifyDataSetChanged();
        }

        public class ViewHolder extends RecyclerView.ViewHolder {
            ImageView opponentIcon;
            RelativeLayout rootLayout;

            public ViewHolder(@NonNull View view) {
                super(view);
                opponentIcon = view.findViewById(R.id.image_opponent_icon);
                rootLayout = view.findViewById(R.id.root_layout);
            }
        }

    }

}

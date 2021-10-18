package com.topzi.chat.activity;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Build;
import androidx.annotation.NonNull;
import androidx.appcompat.widget.SwitchCompat;
import androidx.coordinatorlayout.widget.CoordinatorLayout;

import com.topzi.chat.adapters.ImageViewPagerAdapter;
import com.topzi.chat.adapters.RecentMediaAdapter;
import com.topzi.chat.helper.StorageManager;
import com.topzi.chat.model.MessagesData;
import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.appbar.CollapsingToolbarLayout;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.snackbar.Snackbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager.widget.ViewPager;

import android.os.Bundle;
import android.util.Log;
import android.view.Display;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.topzi.chat.R;
import com.topzi.chat.model.UserProfileModel;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.bumptech.glide.request.RequestOptions;
import com.google.gson.Gson;
import com.topzi.chat.helper.DatabaseHandler;
import com.topzi.chat.helper.NetworkUtil;
import com.topzi.chat.helper.SocketConnection;
import com.topzi.chat.helper.Utils;
import com.topzi.chat.model.ContactsData;
import com.topzi.chat.utils.ApiClient;
import com.topzi.chat.utils.ApiInterface;
import com.topzi.chat.utils.Constants;
import com.topzi.chat.utils.GetSet;
import com.topzi.chat.utils.ObjectSerializer;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static android.Manifest.permission.CAMERA;
import static android.Manifest.permission.READ_PHONE_STATE;
import static android.Manifest.permission.RECORD_AUDIO;
import static android.Manifest.permission.WAKE_LOCK;
import static com.topzi.chat.helper.NetworkUtil.NOT_CONNECT;
import static com.topzi.chat.utils.Constants.TAG_USER_ID;

public class ProfileActivity extends BaseActivity implements View.OnClickListener, SocketConnection.UserProfileListener {

    private String TAG = this.getClass().getSimpleName();
    LinearLayout llCustnoti;
    TextView userName, about, txtMobileNumber, txtNumberType;
    ImageView userImage, backbtn, editbtn, btnMenu, closeBtn;
    CollapsingToolbarLayout collapse_toolbar;
    AppBarLayout appBarLayout;
    CoordinatorLayout mainLay;
    DatabaseHandler dbhelper;
    private String userId = "";
    private SwitchCompat btnMute;
    private Switch switch_media_visibility;
    private ImageView btnMessage, btnCall, btnVideo;
    private ImageView imageView;
    SocketConnection socketConnection;
    SharedPreferences pref;
    SharedPreferences.Editor editor;
    ContactsData.Result results;
    LinearLayout aboutLay, muteLay;
    RelativeLayout mobileLay, imageViewLay;
    BottomSheetBehavior bottomSheetBehavior;
    RecyclerView mediaRecycler;

    RecentMediaAdapter recentMediaAdapter;
    ArrayList<MessagesData> recentMediaList;

    StorageManager storageManager;

    ViewPager viewPager;
    ImageViewPagerAdapter pagerAdapter;
    RelativeLayout imageLayout;
    private ArrayList<String> mediaVisibilty = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        pref = ProfileActivity.this.getSharedPreferences("SavedPref", MODE_PRIVATE);
        editor = pref.edit();

        dbhelper = DatabaseHandler.getInstance(this);
        llCustnoti = findViewById(R.id.ll_custnoti);
        userName = findViewById(R.id.userName);
        about = findViewById(R.id.about);
        userImage = findViewById(R.id.userImage);
        collapse_toolbar = findViewById(R.id.collapse_toolbar);
        appBarLayout = findViewById(R.id.appbar);
        backbtn = findViewById(R.id.backbtn);
        editbtn = findViewById(R.id.editbtn);
        btnMenu = findViewById(R.id.btnMenu);
        txtMobileNumber = findViewById(R.id.txtMobileNumber);
        txtNumberType = findViewById(R.id.txtNumberType);
        btnMute = findViewById(R.id.btnMute);
        btnMessage = findViewById(R.id.btnMessage);
        btnCall = findViewById(R.id.btnCall);
        btnVideo = findViewById(R.id.btnVideo);
        mainLay = findViewById(R.id.mainLay);
        aboutLay = findViewById(R.id.aboutLay);
        muteLay = findViewById(R.id.muteLay);
        mobileLay = findViewById(R.id.mobileLay);
        imageViewLay = findViewById(R.id.imageViewLay);
        closeBtn = findViewById(R.id.closeBtn);
        imageView = findViewById(R.id.imageView);
        mediaRecycler = findViewById(R.id.mediaRecycler);
        viewPager = findViewById(R.id.pager);
        imageLayout = findViewById(R.id.imageLayout);


        storageManager = StorageManager.getInstance(this);

        recentMediaList = new ArrayList<MessagesData>();

        recentMediaAdapter = new RecentMediaAdapter(this, recentMediaList);

        mediaRecycler.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        mediaRecycler.setAdapter(recentMediaAdapter);

        recentMediaAdapter.setAdapterItemClickListener(new RecentMediaAdapter.AdapterItemClickListener() {
            @Override
            public void showImageClicked(MessagesData messagesData, int pos) {
                imageLayout.setVisibility(View.GONE);
                viewPager.setVisibility(View.VISIBLE);
                pagerAdapter = new ImageViewPagerAdapter(ProfileActivity.this, recentMediaList);
                viewPager.setAdapter(pagerAdapter);
                if (messagesData.user_id != null && messagesData.user_id.equals(GetSet.getUserId())) {
                    if (messagesData.progress.equals("completed")) {
                        if (storageManager.checkifImageExists("sent", messagesData.attachment)) {
                            File file = storageManager.getImage("sent", messagesData.attachment);
                            if (file != null) {
                                Log.v(TAG, "file=" + file.getAbsolutePath());
                                viewPager.setCurrentItem(pos);
                                bottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
                            /*Glide.with(this).load(Uri.fromFile(file)).thumbnail(0.5f)
                                    .transition(new DrawableTransitionOptions().crossFade())
                                    .into(imageView);*/
                            }
                        } else {
                            Toast.makeText(ProfileActivity.this, getString(R.string.no_media), Toast.LENGTH_SHORT).show();
                        }
                    }
                }
                else{
                    File file = storageManager.getImage("thumb", messagesData.attachment);
                    if (file != null) {
                        Log.v(TAG, "file=" + file.getAbsolutePath());
                        viewPager.setCurrentItem(pos);
                        bottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
                    /*Glide.with(this).load(file).thumbnail(0.5f)
                            .transition(new DrawableTransitionOptions().crossFade())
                            .into(imageView);*/
                    }
                }
            }
        });



        bottomSheetBehavior = BottomSheetBehavior.from(imageViewLay);

        socketConnection = SocketConnection.getInstance(this);
        SocketConnection.getInstance(this).setUserProfileListener(this);
        if (getIntent().getStringExtra(Constants.TAG_USER_ID) != null) {
            userId = getIntent().getStringExtra(Constants.TAG_USER_ID);
        }

        if (userId.equalsIgnoreCase(GetSet.getUserId())) {
            editbtn.setVisibility(View.VISIBLE);
            btnMenu.setVisibility(View.GONE);
            muteLay.setVisibility(View.GONE);
            mobileLay.setVisibility(View.GONE);
            aboutLay.setVisibility(View.VISIBLE);

            userName.setText(GetSet.getUserName());
            if (!userId.equalsIgnoreCase(GetSet.getUserId())) {
                if (Utils.isProfileEnabled(dbhelper.getContactDetail(userId))) {
                    Glide.with(getApplicationContext()).load(Constants.USER_IMG_PATH + results.user_image).thumbnail(0.5f)
                            .transition(new DrawableTransitionOptions().crossFade())
                            .into(userImage);
                } else {
                    Glide.with(getApplicationContext()).load(R.drawable.profile_banner).thumbnail(0.5f)
                            .transition(new DrawableTransitionOptions().crossFade())
                            .into(userImage);
                }
                if (Utils.isAboutEnabled(dbhelper.getContactDetail(userId))) {
                    about.setText(GetSet.getAbout());
                }else about.setText("");

            } else {
                Glide.with(getApplicationContext()).load(Constants.USER_IMG_PATH + GetSet.getImageUrl()).thumbnail(0.5f)
                        .transition(new DrawableTransitionOptions().crossFade())
                        .into(userImage);
            }
        } else {
            setOtherUserProfile();
            getuserprofile();


//            switch_media_visibility.setChecked(pref.getBoolean(Constants.PREF_MEDIA_VISIBILITY, true));
//
//            switch_media_visibility.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
//                @Override
//                public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
//                    editor.putBoolean(Constants.PREF_MEDIA_VISIBILITY, b).commit();
//                }
//            });
        }

        collapse_toolbar.getLayoutParams().height = (getResources().getDisplayMetrics().heightPixels * 60 / 100);

        appBarLayout.addOnOffsetChangedListener(new AppBarLayout.OnOffsetChangedListener() {
            @Override
            public void onOffsetChanged(AppBarLayout appBarLayout, int verticalOffset) {
                if (Math.abs(verticalOffset) == appBarLayout.getTotalScrollRange()) {
                    // Collapsed
                    backbtn.setColorFilter(ContextCompat.getColor(ProfileActivity.this, R.color.primarytext));
                    editbtn.setColorFilter(ContextCompat.getColor(ProfileActivity.this, R.color.primarytext));
                    btnMenu.setColorFilter(ContextCompat.getColor(ProfileActivity.this, R.color.primarytext));
                } else if (verticalOffset == 0) {
                    // Expanded
                    backbtn.setColorFilter(ContextCompat.getColor(ProfileActivity.this, R.color.white));
                    editbtn.setColorFilter(ContextCompat.getColor(ProfileActivity.this, R.color.white));
                    btnMenu.setColorFilter(ContextCompat.getColor(ProfileActivity.this, R.color.white));
                } else {
                    // Somewhere in between
                }
            }
        });

        backbtn.setOnClickListener(this);
        editbtn.setOnClickListener(this);
        btnMenu.setOnClickListener(this);
        btnMute.setOnClickListener(this);
        btnCall.setOnClickListener(this);
        btnVideo.setOnClickListener(this);
        btnMessage.setOnClickListener(this);
        userImage.setOnClickListener(this);
        closeBtn.setOnClickListener(this);
        llCustnoti.setOnClickListener(this);

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

        try {
            List<MessagesData> data = dbhelper.getMessagesByType(GetSet.getUserId() + userId, "image", "0", "200");
            Log.d("Size","Size"+data.size());

            filterAndAddAvailableData(data);

            mediaRecycler.notify();

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    void filterAndAddAvailableData(List<MessagesData> list){
        for(int i = 0; i < list.size(); i++){
            MessagesData data = list.get(i);
            if (storageManager.checkifImageExists("thumb", data.attachment)) {
                recentMediaList.add(data);
            }
        }
    }

    private void setOtherUserProfile() {
        btnMenu.setVisibility(View.VISIBLE);
        editbtn.setVisibility(View.GONE);
        muteLay.setVisibility(View.VISIBLE);
        mobileLay.setVisibility(View.VISIBLE);
        aboutLay.setVisibility(View.VISIBLE);

        results = dbhelper.getContactDetail(userId);
        userName.setText(ApplicationClass.getContactName(this, results.phone_no));

        if (results.mute_notification.equals("true")) {
            btnMute.setChecked(true);
        } else {
            btnMute.setChecked(false);
        }
        if (results.blockedme.equals("block")) {
            ContactsData.Result result = dbhelper.getContactDetail(userId);
            DialogActivity.setProfileBanner(result, userImage, this);
            if (Utils.isAboutEnabled(result)) {
                aboutLay.setVisibility(View.VISIBLE);
                about.setText(results.about);
            } else {
                aboutLay.setVisibility(View.GONE);
                about.setText("");
            }
        } else {
            if (!userId.equalsIgnoreCase(GetSet.getUserId())) {
                if (Utils.isProfileEnabled(dbhelper.getContactDetail(userId))) {
                    Glide.with(getApplicationContext()).load(Constants.USER_IMG_PATH + results.user_image).thumbnail(0.5f)
                            .transition(new DrawableTransitionOptions().crossFade())
                            .into(userImage);
                } else {
                    Glide.with(getApplicationContext()).load(R.drawable.person).thumbnail(0.5f)
                            .transition(new DrawableTransitionOptions().crossFade())
                            .into(userImage);
                }
                if (Utils.isAboutEnabled(dbhelper.getContactDetail(userId))) {
                    about.setText(GetSet.getAbout());
                }else about.setText("");

            } else {
                Glide.with(getApplicationContext()).load(Constants.USER_IMG_PATH + GetSet.getImageUrl()).thumbnail(0.5f)
                        .transition(new DrawableTransitionOptions().crossFade())
                        .into(userImage);
            }
        }
        txtMobileNumber.setText(results.country_code + " - " + results.phone_no);
        txtNumberType.setText(R.string.mobile);
    }

    void getuserprofile() {

        ApiInterface apiInterface = ApiClient.getClient().create(ApiInterface.class);
        Call<UserProfileModel> call3 = apiInterface.getuserprofile(userId);
        call3.enqueue(new Callback<UserProfileModel>() {
            @Override
            public void onResponse(Call<UserProfileModel> call, Response<UserProfileModel> response) {
                try {
                    UserProfileModel userdata = response.body();
                    if (userdata != null && userdata.getSTATUS().equals("true")) {
                        if (dbhelper.isUserExist(userdata.getRESULT().getId())) {
                            ContactsData.Result results = dbhelper.getContactDetail(userId);
                            dbhelper.addContactDetails(userdata.getRESULT().getId(), userdata.getRESULT().getUserName(), userdata.getRESULT().getPhoneNo(),
                                    userdata.getRESULT().getCountryCode(), userdata.getRESULT().getUserImage(), userdata.getRESULT().getPrivacyAbout(),
                                    userdata.getRESULT().getPrivacyLastSeen(), userdata.getRESULT().getPrivacyProfileImage(), userdata.getRESULT().getAbout(), results.contactstatus);
                        } else {
                            dbhelper.addContactDetails(userdata.getRESULT().getId(), userdata.getRESULT().getUserName(), userdata.getRESULT().getPhoneNo(), userdata.getRESULT().getCountryCode(), userdata.getRESULT().getUserImage(),
                                    userdata.getRESULT().getPrivacyAbout(), userdata.getRESULT().getPrivacyLastSeen(), userdata.getRESULT().getPrivacyProfileImage(), userdata.getRESULT().getAbout(), "false");
                        }
                        setOtherUserProfile();
                    } else {

                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(Call<UserProfileModel> call, Throwable t) {
                Log.v("getuserprofile Failed", "TEST" + t.getMessage());
                call.cancel();
            }
        });

    }

    @Override
    public void onNetworkChange(boolean isConnected) {

    }

    private void mediaVisibility(String userID){
        ArrayList<String> mediaVisibility = new ArrayList<>();
        mediaVisibility.clear();
        for (int i = 0; i < getMediaVisibility().size(); i++) {
            String archive = getMediaVisibility().get(i);
            if (!userID.equalsIgnoreCase(archive)){
                mediaVisibility.add(archive);
            }
        }

        Log.e("LLLLL_Hide2211: ", String.valueOf(mediaVisibility));
        try {
            editor.putString("mediaVisibiltyID", ObjectSerializer.serialize(mediaVisibility));
        } catch (IOException e) {
            Log.e("LLLLLLL_Hide2EX11: ", Objects.requireNonNull(e.getMessage()));
            e.printStackTrace();
        }
        editor.commit();
    }

    private ArrayList<String> getMediaVisibility() {
        if (null == mediaVisibilty) {
            mediaVisibilty = new ArrayList<>();
        }

        try {
            mediaVisibilty = (ArrayList<String>) ObjectSerializer.deserialize(pref.getString("mediaVisibiltyID", ObjectSerializer.serialize(new ArrayList<String>())));
            Log.e("LLLLL_Hide: ", String.valueOf(mediaVisibilty));
            return mediaVisibilty;
        } catch (IOException e) {
            Log.e("LLLLLLL_EX11: ", Objects.requireNonNull(e.getMessage()));
            e.printStackTrace();
            return new ArrayList<>();
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

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.backbtn:
                onBackPressed();
                break;
            case R.id.editbtn:
                Intent i = new Intent(ProfileActivity.this, ProfileInfo.class);
                i.putExtra("from", "edit");
                startActivity(i);
                break;
            case R.id.btnMenu:
                Display display = this.getWindowManager().getDefaultDisplay();
                ArrayList<String> values = new ArrayList<>();
                results = dbhelper.getContactDetail(userId);
                if (results.blockedbyme.equals("block")) {
                    values.add(getString(R.string.unblock));
                } else {
                    values.add(getString(R.string.block));
                }

                ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
                        R.layout.option_item, android.R.id.text1, values);
                LayoutInflater layoutInflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                View layout = layoutInflater.inflate(R.layout.option_layout, null);
                layout.setAnimation(AnimationUtils.loadAnimation(this, R.anim.grow_from_topright_to_bottomleft));
                final PopupWindow popup = new PopupWindow(ProfileActivity.this);
                popup.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                popup.setContentView(layout);
                popup.setWidth(display.getWidth() * 50 / 100);
                popup.setHeight(ViewGroup.LayoutParams.WRAP_CONTENT);
                popup.setFocusable(true);
                popup.showAtLocation(mainLay, Gravity.TOP | Gravity.RIGHT, ApplicationClass.dpToPx(this, 10), ApplicationClass.dpToPx(this, 63));

                final ListView lv = layout.findViewById(R.id.listView);
                lv.setAdapter(adapter);
                popup.showAsDropDown(view);

                lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {

                    @Override
                    public void onItemClick(AdapterView<?> parent, View view,
                                            int position, long id) {
                        popup.dismiss();
                        if (position == 0) {
                            if (isNetworkConnected().equals(NOT_CONNECT)) {
                                networkSnack();
                            } else {
                                String type = "";
                                if (results.blockedbyme.equals("block")) {
                                    type = "unblock";
                                } else {
                                    type = "block";
                                }
                                blockChatConfirmDialog(type, "popup");
                            }
                        }
                    }
                });
                break;
            case R.id.btnMute:
                if (btnMute.isChecked()) {
                    dbhelper.updateMuteUser(userId, "true");
                } else {
                    dbhelper.updateMuteUser(userId, "");
                }
                break;
            case R.id.ll_custnoti:
                Intent intent = new Intent(ProfileActivity.this,CustomeNotification.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
                intent.putExtra("user_id", userId);
                startActivity(intent);
                break;
            case R.id.btnMessage:
                finish();
                Intent ch = new Intent(this, ChatActivity.class);
                ch.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
                ch.putExtra("user_id", userId);
                startActivity(ch);
                break;
            case R.id.btnCall:
                if (ContextCompat.checkSelfPermission(ProfileActivity.this, CAMERA) != PackageManager.PERMISSION_GRANTED
                        || ContextCompat.checkSelfPermission(ProfileActivity.this, RECORD_AUDIO)
                        != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(ProfileActivity.this, new String[]{CAMERA, RECORD_AUDIO}, 100);
                } else if (results.blockedbyme.equals("block")) {
                    blockChatConfirmDialog("unblock", "sent");
                } else {
                    btnCall.setOnClickListener(null);
                    Intent video = new Intent(ProfileActivity.this, CallActivity.class);
                    video.putExtra("from", "send");
                    video.putExtra("type", "audio");
                    video.putExtra("user_id", userId);
                    startActivity(video);
                }
                break;
            case R.id.btnVideo:
                if (ContextCompat.checkSelfPermission(ProfileActivity.this, CAMERA) != PackageManager.PERMISSION_GRANTED
                        || ContextCompat.checkSelfPermission(ProfileActivity.this, RECORD_AUDIO)
                        != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(ProfileActivity.this, new String[]{CAMERA, RECORD_AUDIO}, 101);
                } else if (results.blockedbyme.equals("block")) {
                    blockChatConfirmDialog("unblock", "sent");
                } else {
                    btnVideo.setOnClickListener(null);
                    Intent video = new Intent(ProfileActivity.this, CallActivity.class);
                    video.putExtra("from", "send");
                    video.putExtra("type", "video");
                    video.putExtra("user_id", userId);
                    startActivity(video);
                }
                break;
            case R.id.userImage:

                imageLayout.setVisibility(View.VISIBLE);
                viewPager.setVisibility(View.GONE);
                bottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
                if (!userId.equalsIgnoreCase(GetSet.getUserId())) {
                    if (Utils.isProfileEnabled(dbhelper.getContactDetail(userId))) {
                        Glide.with(getApplicationContext()).load(Constants.USER_IMG_PATH + results.user_image).thumbnail(0.5f)
                                .transition(new DrawableTransitionOptions().crossFade())
                                .into(imageView);
                    } else {
                        Glide.with(getApplicationContext()).load(R.drawable.profile_banner).thumbnail(0.5f)
                                .transition(new DrawableTransitionOptions().crossFade())
                                .into(imageView);
                    }
                } else {
                    Glide.with(getApplicationContext()).load(Constants.USER_IMG_PATH + GetSet.getImageUrl()).thumbnail(0.5f)
                            .transition(new DrawableTransitionOptions().crossFade())
                            .into(imageView);
                }

                break;
            case R.id.closeBtn:
                onBackPressed();
                break;
        }
    }

    private void openCustNotiDialoge(){

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
                    setOtherUserProfile();
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

    @Override
    protected void onResume() {
        super.onResume();
        btnCall.setOnClickListener(this);
        btnVideo.setOnClickListener(this);
        if (userId.equalsIgnoreCase(GetSet.getUserId())) {
//            Glide.with(ProfileActivity.this).load(Constants.USER_IMG_PATH + GetSet.getImageUrl()).thumbnail(0.5f)
//                    .apply(new RequestOptions().placeholder(R.drawable.change_camera).error(R.drawable.change_camera))
//                    .transition(new DrawableTransitionOptions().crossFade())
//                    .into(userImage);
//            userName.setText(GetSet.getUserName());



            if (!userId.equalsIgnoreCase(GetSet.getUserId())) {
                if (Utils.isProfileEnabled(dbhelper.getContactDetail(userId))) {
                    Glide.with(getApplicationContext()).load(Constants.USER_IMG_PATH + results.user_image).thumbnail(0.5f)
                            .transition(new DrawableTransitionOptions().crossFade())
                            .into(userImage);
                } else {
                    Glide.with(getApplicationContext()).load(R.drawable.profile_banner).thumbnail(0.5f)
                            .transition(new DrawableTransitionOptions().crossFade())
                            .into(userImage);
                }
                if (Utils.isAboutEnabled(dbhelper.getContactDetail(userId))) {
                    about.setText(GetSet.getAbout());
                }else about.setText("");

            } else {
                Glide.with(getApplicationContext()).load(Constants.USER_IMG_PATH + GetSet.getImageUrl()).thumbnail(0.5f)
                        .transition(new DrawableTransitionOptions().crossFade())
                        .into(userImage);
            }
        }
    }

    @Override
    public void onPrivacyChanged(final JSONObject jsonObject) {
        try {

            if (jsonObject.getString(TAG_USER_ID).equalsIgnoreCase(userId)) {
                runOnUiThread(new Runnable() {
                    public void run() {
                        try {
                            ContactsData.Result result = dbhelper.getContactDetail(jsonObject.getString(Constants.TAG_USER_ID));
                            if (!result.user_id.equalsIgnoreCase(GetSet.getUserId())) {
                                if (Utils.isAboutEnabled(result)) {
                                    aboutLay.setVisibility(View.VISIBLE);
                                    about.setText(results.about);
                                } else {
                                    aboutLay.setVisibility(View.GONE);
                                    about.setText("");
                                }
                                DialogActivity.setProfileBanner(dbhelper.getContactDetail(jsonObject.getString(Constants.TAG_USER_ID)), userImage, getApplicationContext());
                            }
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
    protected void onDestroy() {
        SocketConnection.getInstance(this).setUserProfileListener(null);
        super.onDestroy();
    }

    @Override
    public void onBackPressed() {
        if (bottomSheetBehavior != null && bottomSheetBehavior.getState() == BottomSheetBehavior.STATE_EXPANDED) {
            bottomSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 100) {
            int permissionCamera = ContextCompat.checkSelfPermission(ProfileActivity.this,
                    CAMERA);
            int permissionAudio = ContextCompat.checkSelfPermission(ProfileActivity.this,
                    RECORD_AUDIO);
            int permissionPhoneState = ContextCompat.checkSelfPermission(ProfileActivity.this,
                    READ_PHONE_STATE);
            int permissionWakeLock = ContextCompat.checkSelfPermission(ProfileActivity.this,
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
            int permissionCamera = ContextCompat.checkSelfPermission(ProfileActivity.this,
                    CAMERA);
            int permissionAudio = ContextCompat.checkSelfPermission(ProfileActivity.this,
                    RECORD_AUDIO);
            int permissionPhoneState = ContextCompat.checkSelfPermission(ProfileActivity.this,
                    READ_PHONE_STATE);
            int permissionWakeLock = ContextCompat.checkSelfPermission(ProfileActivity.this,
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
        }
    }

    private void requestPermission(String[] permissions, int requestCode) {
        ActivityCompat.requestPermissions(ProfileActivity.this, permissions, requestCode);
    }
}

package com.topzi.chat.activity;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.view.ViewCompat;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.topzi.chat.R;
import com.topzi.chat.model.ContactsData;
import com.topzi.chat.utils.Constants;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.bumptech.glide.request.RequestOptions;
import com.makeramen.roundedimageview.RoundedImageView;

import static android.Manifest.permission.CAMERA;
import static android.Manifest.permission.READ_CONTACTS;
import static android.Manifest.permission.RECORD_AUDIO;
import static com.topzi.chat.utils.Constants.TAG_MY_CONTACTS;
import static com.topzi.chat.utils.Constants.TAG_NOBODY;
import static com.topzi.chat.utils.Constants.TRUE;

public class DialogActivity extends BaseActivity implements View.OnClickListener {

    private final String TAG = this.getClass().getSimpleName();
    private TextView txtUserName;
    private RoundedImageView userImageView;
    private ImageView btnMessage, btnCall, btnVideo, btnInfo;
    private RelativeLayout imageLay, mainLay, messageLay, callLay, videoLay, infoLay;
    private String userName;
    private String userId;
    private String userImage, blockedme;
    ContactsData.Result result;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_user);
        if (getSupportActionBar() != null) getSupportActionBar().hide();
        txtUserName = findViewById(R.id.txtUserName);
        userImageView = findViewById(R.id.userImage);
        btnMessage = findViewById(R.id.btnMessage);
        btnCall = findViewById(R.id.btnCall);
        btnVideo = findViewById(R.id.btnVideo);
        btnInfo = findViewById(R.id.btnInfo);
        imageLay = findViewById(R.id.imageLay);
        mainLay = findViewById(R.id.mainLay);
        messageLay = findViewById(R.id.messageLayout);
        callLay = findViewById(R.id.callLayout);
        videoLay = findViewById(R.id.videoLayout);
        infoLay = findViewById(R.id.infoLayout);


        ViewCompat.setTransitionName(mainLay, getURLForResource(R.drawable.person));
        if (getIntent() != null) {
            if (getIntent().getStringExtra(Constants.TAG_USER_ID) != null) {
                userId = getIntent().getStringExtra(Constants.TAG_USER_ID);
                result = dbhelper.getContactDetail(userId);
                if (ContextCompat.checkSelfPermission(DialogActivity.this, READ_CONTACTS) == PackageManager.PERMISSION_GRANTED) {
                    userName = ApplicationClass.getContactName(this, result.phone_no);
                } else {
                    userName = result.phone_no;
                }

                userImage = result.user_image;
                blockedme = result.blockedme;
                txtUserName.setText(userName);
                if (!blockedme.equals("block")) {
                    if (result.privacy_profile_image != null && result.privacy_profile_image.equalsIgnoreCase(TAG_MY_CONTACTS)) {
                        if (result.contactstatus != null && result.contactstatus.equalsIgnoreCase(TRUE)) {
                            Glide.with(DialogActivity.this).load(Constants.USER_IMG_PATH + result.user_image)
                                    .apply(new RequestOptions().placeholder(R.drawable.person).error(R.drawable.person))
                                    .into(userImageView);
                        } else {
                            Glide.with(DialogActivity.this).load(R.drawable.person)
                                    .apply(new RequestOptions().placeholder(R.drawable.person).error(R.drawable.person))
                                    .into(userImageView);
                        }

                    } else if (result.privacy_profile_image != null && result.privacy_profile_image.equalsIgnoreCase(TAG_NOBODY)) {
                        Glide.with(DialogActivity.this).load(R.drawable.person)
                                .apply(new RequestOptions().placeholder(R.drawable.person).error(R.drawable.person))
                                .into(userImageView);
                    } else {

                        Glide.with(DialogActivity.this).load(Constants.USER_IMG_PATH + result.user_image)
                                .apply(new RequestOptions().placeholder(R.drawable.person).error(R.drawable.person))
                                .into(userImageView);
                    }

                } else {
                    Glide.with(DialogActivity.this).load(R.drawable.person)
                            .apply(new RequestOptions().placeholder(R.drawable.person).error(R.drawable.person))
                            .into(userImageView);
                }

                btnMessage.setOnClickListener(this);
                btnCall.setOnClickListener(this);
                btnVideo.setOnClickListener(this);
                btnInfo.setOnClickListener(this);
                imageLay.setOnClickListener(this);

            } else if (getIntent().getStringExtra(Constants.TAG_GROUP_ID) != null) {
                userId = getIntent().getStringExtra(Constants.TAG_GROUP_ID);
                userName = getIntent().getStringExtra(Constants.TAG_GROUP_NAME);
                userImage = getIntent().getStringExtra(Constants.TAG_GROUP_IMAGE);

                txtUserName.setText(userName);
                if (userImage == null || userImage.equalsIgnoreCase("")) {
                    Glide.with(DialogActivity.this).load(R.drawable.person)
                            .apply(new RequestOptions().placeholder(R.drawable.person).error(R.drawable.person))
                            .into(userImageView);
                } else {
                    Glide.with(DialogActivity.this).load(Constants.GROUP_IMG_PATH + userImage)
                            .apply(new RequestOptions().placeholder(R.drawable.person).error(R.drawable.person))
                            .into(userImageView);
                }

                callLay.setVisibility(View.GONE);
                videoLay.setVisibility(View.GONE);

                btnMessage.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        finish();
                        Intent i = new Intent(DialogActivity.this, GroupChatActivity.class);
                        i.putExtra(Constants.TAG_GROUP_ID, userId);
                        startActivity(i);
                    }
                });

                btnInfo.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        finish();
                        Intent profile = new Intent(DialogActivity.this, GroupInfoActivity.class);
                        profile.putExtra(Constants.TAG_GROUP_ID, userId);
                        startActivity(profile);
                    }
                });

                imageLay.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        finish();
                        Intent profile = new Intent(DialogActivity.this, GroupInfoActivity.class);
                        profile.putExtra(Constants.TAG_GROUP_ID, userId);
                        startActivity(profile);
                    }
                });
            }
        }
    }

    @Override
    public void onNetworkChange(boolean isConnected) {

    }

    public static String getURLForResource(int resourceId) {
        return Uri.parse("android.resource://com.topzi.chat/" + resourceId).toString();
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.imageLay:
                finish();
                Intent profile = new Intent(DialogActivity.this, ProfileActivity.class);
                profile.putExtra(Constants.TAG_USER_ID, userId);
                startActivity(profile);
                break;

            case R.id.btnMessage:
                finish();
                Intent i = new Intent(DialogActivity.this, ChatActivity.class);
                i.putExtra(Constants.TAG_USER_ID, userId);
                startActivity(i);
                break;

            case R.id.btnCall:
                if (ContextCompat.checkSelfPermission(DialogActivity.this, CAMERA) != PackageManager.PERMISSION_GRANTED
                        || ContextCompat.checkSelfPermission(DialogActivity.this, RECORD_AUDIO)
                        != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(DialogActivity.this, new String[]{CAMERA, RECORD_AUDIO}, 100);
                } else if (result.blockedbyme.equals("block")) {
                    makeToast(getString(R.string.unblock_message));
                } else {
                    Intent video = new Intent(DialogActivity.this, CallActivity.class);
                    video.putExtra("from", "send");
                    video.putExtra("type", "audio");
                    video.putExtra("user_id", userId);
                    startActivity(video);
                }
                break;
            case R.id.btnVideo:
                if (ContextCompat.checkSelfPermission(DialogActivity.this, CAMERA) != PackageManager.PERMISSION_GRANTED
                        || ContextCompat.checkSelfPermission(DialogActivity.this, RECORD_AUDIO)
                        != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(DialogActivity.this, new String[]{CAMERA, RECORD_AUDIO}, 101);
                } else if (result.blockedbyme.equals("block")) {
                    makeToast(getString(R.string.unblock_message));
                } else {
                    Intent video = new Intent(DialogActivity.this, CallActivity.class);
                    video.putExtra("from", "send");
                    video.putExtra("type", "video");
                    video.putExtra("user_id", userId);
                    startActivity(video);
                }
                break;
            case R.id.btnInfo:
                finish();
                Intent info = new Intent(DialogActivity.this, ProfileActivity.class);
                info.putExtra(Constants.TAG_USER_ID, userId);
                startActivity(info);
                break;
        }
    }

    public static void setProfileImage(ContactsData.Result result, ImageView profileImage, Context context) {
        try {
            if (result.privacy_profile_image.equalsIgnoreCase(TAG_MY_CONTACTS)) {
                if (result.contactstatus != null && result.contactstatus.equalsIgnoreCase(TRUE)) {
                    Glide.with(context).load(Constants.USER_IMG_PATH + result.user_image).thumbnail(0.5f)
                            .apply(RequestOptions.circleCropTransform()
                                    .placeholder(R.drawable.person)
                                    .error(R.drawable.person)
                                    .override(ApplicationClass.dpToPx(context, 70)))
                            .into(profileImage);
                } else {
                    Glide.with(context).load(R.drawable.person)
                            .thumbnail(0.5f)
                            .apply(RequestOptions.circleCropTransform()
                                    .placeholder(R.drawable.person)
                                    .error(R.drawable.person)
                                    .override(ApplicationClass.dpToPx(context, 70)))
                            .into(profileImage);
                }

            } else if (result.privacy_profile_image.equalsIgnoreCase(TAG_NOBODY)) {
                Glide.with(context).load(R.drawable.person).thumbnail(0.5f)
                        .apply(RequestOptions.circleCropTransform()
                                .placeholder(R.drawable.person)
                                .error(R.drawable.person)
                                .override(ApplicationClass.dpToPx(context, 70)))
                        .into(profileImage);
            } else {

                Glide.with(context).load(Constants.USER_IMG_PATH + result.user_image).thumbnail(0.5f)
                        .apply(RequestOptions.circleCropTransform().placeholder(R.drawable.person).error(R.drawable.person).override(ApplicationClass.dpToPx(context, 70)))
                        .into(profileImage);
            }
        } catch (NullPointerException e) {
            profileImage.setImageDrawable(context.getResources().getDrawable(R.drawable.person));
            e.printStackTrace();
        }
    }

    public static void setProfileBanner(ContactsData.Result result, ImageView profileImage, Context context) {
        try {
            if (result != null && result.privacy_profile_image.equalsIgnoreCase(TAG_MY_CONTACTS)) {
                if (result.contactstatus != null && result.contactstatus.equalsIgnoreCase(TRUE)) {
                    Glide.with(context).load(Constants.USER_IMG_PATH + result.user_image)
                            .apply(new RequestOptions().placeholder(R.drawable.person).error(R.drawable.person))
                            .transition(new DrawableTransitionOptions().crossFade())
                            .into(profileImage);
                } else {
                    Glide.with(context).load(R.drawable.person)
                            .apply(new RequestOptions().placeholder(R.drawable.person).error(R.drawable.person))
                            .transition(new DrawableTransitionOptions().crossFade())
                            .into(profileImage);
                }

            } else if (result != null && result.privacy_profile_image.equalsIgnoreCase(TAG_NOBODY)) {
                Glide.with(context).load(R.drawable.person)
                        .apply(new RequestOptions().placeholder(R.drawable.person).error(R.drawable.person))
                        .transition(new DrawableTransitionOptions().crossFade())
                        .into(profileImage);
            } else {

                Glide.with(context).load(Constants.USER_IMG_PATH + result.user_image)
                        .apply(new RequestOptions().placeholder(R.drawable.person).error(R.drawable.person))
                        .transition(new DrawableTransitionOptions().crossFade())
                        .into(profileImage);
            }
        } catch (NullPointerException e) {
            e.printStackTrace();
        }
    }

    public static void setAboutUs(ContactsData.Result result, TextView txtAbout) {
        try {
            if (result.privacy_about.equalsIgnoreCase(TAG_MY_CONTACTS)) {
                if (result.contactstatus != null && result.contactstatus.equalsIgnoreCase(TRUE)) {
                    txtAbout.setText("" + result.about);
                } else {
                    txtAbout.setText("");
                }
            } else if (result.privacy_about.equalsIgnoreCase(TAG_NOBODY)) {
                txtAbout.setText("");
            } else {
                txtAbout.setText(result.about);
            }
        } catch (NullPointerException e) {
            e.printStackTrace();
        }
    }

}
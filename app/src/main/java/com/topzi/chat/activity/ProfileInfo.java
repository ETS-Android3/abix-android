package com.topzi.chat.activity;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.annotation.SuppressLint;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.ContactsContract;

import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.coordinatorlayout.widget.CoordinatorLayout;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.text.TextUtils;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.quickblox.core.QBEntityCallback;
import com.quickblox.core.QBEntityCallbackImpl;
import com.quickblox.core.exception.QBResponseException;
import com.quickblox.users.QBUsers;
import com.quickblox.users.model.QBUser;
import com.topzi.chat.R;
import com.topzi.chat.model.UserProfileModel;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.Target;
import com.google.android.material.snackbar.Snackbar;
import com.google.gson.Gson;
import com.google.i18n.phonenumbers.NumberParseException;
import com.google.i18n.phonenumbers.PhoneNumberUtil;
import com.google.i18n.phonenumbers.Phonenumber;
import com.topzi.chat.external.ImagePicker;
import com.topzi.chat.external.RandomString;
import com.topzi.chat.helper.DatabaseHandler;
import com.topzi.chat.helper.ForegroundService;
import com.topzi.chat.helper.ImageCompression;
import com.topzi.chat.helper.NetworkReceiver;
import com.topzi.chat.helper.SharedPrefManager;
import com.topzi.chat.helper.SocketConnection;
import com.topzi.chat.helper.StorageManager;
import com.topzi.chat.model.ChannelResult;
import com.topzi.chat.model.ContactsData;
import com.topzi.chat.model.DeviceDataModel;
import com.topzi.chat.model.GroupData;
import com.topzi.chat.model.GroupResult;
import com.topzi.chat.model.LoginModel;
import com.topzi.chat.model.ProfileUpdatResModel;
import com.topzi.chat.model.SigninResponse;
import com.topzi.chat.model.UpdateProfileModel;
import com.topzi.chat.model.contacts.ContactsModel;
import com.topzi.chat.service.LoginService;
import com.topzi.chat.utils.ApiClient;
import com.topzi.chat.utils.ApiInterface;
import com.topzi.chat.utils.Constants;
import com.topzi.chat.utils.GetSet;
import com.topzi.chat.utils.QBResRequestExecutor;
import com.topzi.chat.utils.SharedPrefsHelper;
import com.topzi.chat.utils.ToastUtils;
import com.topzi.chat.utils.ValidationUtils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;

import de.hdodenhof.circleimageview.CircleImageView;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static android.Manifest.permission.READ_CONTACTS;
import static android.Manifest.permission.RECEIVE_SMS;
import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;

public class ProfileInfo extends BaseActivity implements View.OnClickListener {
    static ApiInterface apiInterface;
    ProgressDialog progressDialog;
    ProgressBar progressbar;
    EditText name, about;
    TextView detail;
    CoordinatorLayout mainLay;
    CircleImageView userImage, noimage;
    ImageView backbtn, fab;
    RelativeLayout btnNext;
    SharedPreferences pref;
    SharedPreferences.Editor editor;
    String TAG = "PROFILE INFO", from = "";
    DatabaseHandler dbhelper;
    StorageManager storageManager;

    protected SharedPrefsHelper sharedPrefsHelper;
    protected QBResRequestExecutor requestExecutor;
    private QBUser userForSave;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile_info);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        requestExecutor = ApplicationClass.getInstance().getQbResRequestExecutor();
        sharedPrefsHelper = SharedPrefsHelper.getInstance();

        userImage = findViewById(R.id.userImage);
        noimage = findViewById(R.id.noimage);
        name = findViewById(R.id.name);
        about = findViewById(R.id.about);
        detail = findViewById(R.id.detail);
        btnNext = findViewById(R.id.btnNext);
        backbtn = findViewById(R.id.backbtn);
        fab = findViewById(R.id.fab);
        mainLay = findViewById(R.id.mainLay);
        progressbar = findViewById(R.id.progressbar);
        progressbar.setIndeterminate(true);
        progressbar.getIndeterminateDrawable().setColorFilter(Color.WHITE, PorterDuff.Mode.SRC_IN);

        from = getIntent().getExtras().getString("from");
        apiInterface = ApiClient.getClient().create(ApiInterface.class);
        pref = ProfileInfo.this.getSharedPreferences("SavedPref", MODE_PRIVATE);
        editor = pref.edit();
        dbhelper = DatabaseHandler.getInstance(this);
        storageManager = StorageManager.getInstance(this);

        progressDialog = new ProgressDialog(ProfileInfo.this);
        progressDialog.setMessage(getResources().getString(R.string.pleasewait));
        progressDialog.setCancelable(false);

        if (GetSet.getUserName() != null) {
            name.setText(GetSet.getUserName());
        }

        if (GetSet.getImageUrl() != null) {
            Glide.with(ProfileInfo.this).load(Constants.USER_IMG_PATH + GetSet.getImageUrl())
                    .apply(RequestOptions.circleCropTransform().placeholder(R.drawable.change_camera).error(R.drawable.change_camera))
                    .listener(new RequestListener<Drawable>() {
                        @Override
                        public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                            noimage.setVisibility(View.VISIBLE);
                            userImage.setVisibility(View.INVISIBLE);
                            return false;
                        }

                        @Override
                        public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                            noimage.setVisibility(View.GONE);
                            userImage.setVisibility(View.VISIBLE);
                            return false;
                        }
                    }).into(userImage);
        }

        if (from.equals("edit")) {
            hideLoading();
            detail.setText(R.string.editprofileinfo);
            about.setVisibility(View.VISIBLE);
            if (GetSet.getAbout() != null) {
                about.setText(GetSet.getAbout());
            }
        } else {
            if (NetworkReceiver.isConnected()) {
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        getMyGroups();
                    }
                }, 1000);
            }
            detail.setText(R.string.profileinfodetail);
            about.setVisibility(View.GONE);
        }
        backbtn.setColorFilter(ContextCompat.getColor(getApplicationContext(), R.color.primarytext));

        userImage.setOnClickListener(this);
        noimage.setOnClickListener(this);
        backbtn.setOnClickListener(this);
        btnNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (ContextCompat.checkSelfPermission(ProfileInfo.this, READ_CONTACTS) != PackageManager.PERMISSION_GRANTED
                        || ContextCompat.checkSelfPermission(ProfileInfo.this, WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED
                        || ContextCompat.checkSelfPermission(ProfileInfo.this, RECEIVE_SMS) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(ProfileInfo.this, new String[]{READ_CONTACTS, WRITE_EXTERNAL_STORAGE, RECEIVE_SMS}, 100);
                } else {
                    if (NetworkReceiver.isConnected()) {
                        updateProfile();
                    } else {
                        Snackbar.make(mainLay, getString(R.string.no_internet_connection), Snackbar.LENGTH_LONG).show();
                    }
                }
            }
        });
    }

//    private void getMyChannels() {
//        ApiInterface apiInterface = ApiClient.getClient().create(ApiInterface.class);
//        Call<ChannelResult> call3 = apiInterface.getMyChannels(GetSet.getToken(), GetSet.getUserId());
//        call3.enqueue(new Callback<ChannelResult>() {
//            @Override
//            public void onResponse(Call<ChannelResult> call, Response<ChannelResult> response) {
//                try {
////                    Log.i(TAG, "getMyChannels: " + new Gson().toJson(response));
//                    if (response.body().status.equalsIgnoreCase(Constants.TRUE)) {
//                        new InsertMyChannelTask(response.body().result).execute();
//                    } else {
//                        getSubscribedChannels();
//                    }
//                } catch (Exception e) {
//                    e.printStackTrace();
//                    getSubscribedChannels();
//                }
//            }
//
//            @Override
//            public void onFailure(Call<ChannelResult> call, Throwable t) {
//                Log.e(TAG, "getMyChannels" + t.getMessage());
//                call.cancel();
//                getSubscribedChannels();
//            }
//        });
//    }

//    public void getSubscribedChannels() {
//        ApiInterface apiInterface = ApiClient.getClient().create(ApiInterface.class);
//        Call<ChannelResult> call3 = apiInterface.getMySubscribedChannels(GetSet.getToken(), GetSet.getUserId());
//        call3.enqueue(new Callback<ChannelResult>() {
//            @Override
//            public void onResponse(Call<ChannelResult> call, Response<ChannelResult> response) {
//                try {
////                    Log.i(TAG, "getMySubscribedChannels: " + new Gson().toJson(response));
//                    if (response.body().status.equalsIgnoreCase(Constants.TRUE)) {
//
//                        new InsertSubscribedChannelTask(response.body().result).execute();
//                    } else {
//                        hideLoading();
//                    }
//                } catch (Exception e) {
//                    e.printStackTrace();
//                    hideLoading();
//                }
//            }
//
//            @Override
//            public void onFailure(Call<ChannelResult> call, Throwable t) {
//                Log.e(TAG, "getMySubscribedChannels" + t.getMessage());
//                call.cancel();
//                hideLoading();
//            }
//        });
//    }

    private void getMyGroups() {
        showLoading();
        ApiInterface apiInterface = ApiClient.getClient().create(ApiInterface.class);
        Call<GroupResult> call3 = apiInterface.getMyGroups(GetSet.getUserId());
        call3.enqueue(new Callback<GroupResult>() {
            @Override
            public void onResponse(Call<GroupResult> call, Response<GroupResult> response) {
                try {
//                    Log.i(TAG, "getMyGroups: " + new Gson().toJson(response));
                    if (response.body().status.equalsIgnoreCase(Constants.TRUE)) {
                        new InsertMyGroupTask(response.body().result).execute();
                    } else {
//                        getMyChannels();
                        hideLoading();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
//                    getMyChannels();
                    hideLoading();
                }
            }

            @Override
            public void onFailure(Call<GroupResult> call, Throwable t) {
                Log.e(TAG, "getMyGroups" + t.getMessage());
                call.cancel();
                hideLoading();
//                getMyChannels();
            }
        });
    }

    private void getUserInfo(GroupData groupData, GroupData.GroupMembers groupMember) {
        ApiInterface apiInterface = ApiClient.getClient().create(ApiInterface.class);
        Call<UserProfileModel> call3 = apiInterface.getuserprofile(groupMember.memberId);
        call3.enqueue(new Callback<UserProfileModel>() {
            @Override
            public void onResponse(Call<UserProfileModel> call, Response<UserProfileModel> response) {
                try {
                    Log.i(TAG, "getUserInfo: " + new Gson().toJson(response));
                    UserProfileModel userdata = response.body();
                    if (userdata != null && userdata.getSTATUS().equals("true")) {
                        new InsertGroupMemberTask(groupData, groupMember, userdata).execute();
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

    private void updateProfile() {
        if (name.getText().toString().trim().length() == 0) {
            Snackbar.make(mainLay, getString(R.string.profileinfodetail), Snackbar.LENGTH_LONG).show();
        } else {
            if (from.equals("welcome")) {
                Signin(GetSet.getphonenumber(), GetSet.getcountrycode(), name.getText().toString());
            } else {
                if (about.getText().toString().trim().length() == 0) {
                    Snackbar.make(mainLay, getString(R.string.about_blank), Snackbar.LENGTH_LONG).show();
                } else {
                    updateMyprofile();
                }
            }
        }
    }

    @Override
    public void onNetworkChange(boolean isConnected) {

    }

    void Signin(String number, String code, String name) {
        showLoading();
        LoginModel loginModel = new LoginModel();
        loginModel.setCountryCode(code);
        loginModel.setPhone(number);
        Gson gson = new Gson();
        String login = gson.toJson(loginModel);
        Call<SigninResponse> call3 = apiInterface.signin(login);
        call3.enqueue(new Callback<SigninResponse>() {
            @Override
            public void onResponse(Call<SigninResponse> call, Response<SigninResponse> response) {
                try {
                    SigninResponse userdata = response.body();
//                    Log.e("LLLLLLL_DataL: ", "Signin=" + userdata.toString());

                    if (userdata != null && userdata.getSTATUS()) {

                        Pattern emailPattern = Patterns.EMAIL_ADDRESS; // API level 8+
                        Account[] accounts = AccountManager.get(ProfileInfo.this).getAccounts();

                        editor.putBoolean("isLogged", true);
                        editor.putString("userId", userdata.getRESULT().getId() + "");
                        editor.putString("userName", userdata.getRESULT().getUserName());
                        editor.putString("userImage", userdata.getRESULT().getUserImage());
                        editor.putString("phoneNumber", userdata.getRESULT().getPhoneNo());
                        editor.putString("countryCode", userdata.getRESULT().getCountryCode());
                        editor.putString("token", userdata.getRESULT().getUserToken());
                        editor.putString("about", userdata.getRESULT().getAbout());
                        editor.putString("oldWallpaper", "");
                        editor.putString("newWallpaper", "");
                        editor.putString("vibrateType", "Default");
                        editor.putString("callVibrateType", "Default");
                        editor.putString("privacyprofileimage", userdata.getRESULT().getPrivacyProfileImage());
                        editor.putString("privacylastseen", userdata.getRESULT().getPrivacyLastSeen());
                        editor.putString("privacyabout", userdata.getRESULT().getPrivacyAbout());
                        if (accounts != null) {
                            if (emailPattern.matcher(accounts[0].name).matches()) {
                                editor.putString("backupEmail", accounts[0].name);
                            }
                        }
                        editor.putBoolean("archiveAll", false);
                        editor.putBoolean("isPriorieNotification", true);
                        editor.putBoolean("isUseCustNotification", false);
                        editor.putString("backupTime", "Never");
                        editor.putString("backupOver", "Wi-Fi");
                        editor.putString("mutenotification", "");
                        editor.commit();


                        GetSet.setLogged(true);
                        GetSet.setUserId(pref.getString("userId", null));
                        GetSet.setUserName(pref.getString("userName", null));
                        GetSet.setphonenumber(pref.getString("phoneNumber", null));
                        GetSet.setcountrycode(pref.getString("countryCode", null));
                        GetSet.setImageUrl(pref.getString("userImage", null));
                        GetSet.setToken(pref.getString("token", null));
                        GetSet.setAbout(pref.getString("about", null));
                        GetSet.setPrivacyprofileimage(pref.getString("privacyprofileimage", Constants.TAG_EVERYONE));
                        GetSet.setPrivacylastseen(pref.getString("privacylastseen", Constants.TAG_EVERYONE));
                        GetSet.setPrivacyabout(pref.getString("privacyabout", Constants.TAG_EVERYONE));

                        userForSave = createUserWithEnteredData(String.valueOf(userdata.getRESULT().getId()),name);
                        startSignUpNewUser(userForSave);

//                        if (ValidationUtils.isLoginValid(ProfileInfo.this, userdata.getRESULT().getUserName()) &&
//                                ValidationUtils.isFoolNameValid(ProfileInfo.this, userdata.getRESULT().getUserName())) {
//
//                        }


                        addDeviceId(ProfileInfo.this);
                        new GetContactTask().execute();

                    } else if (userdata != null && !userdata.getSTATUS()) {
                        hideLoading();
                        Toast.makeText(getApplicationContext(), userdata.getMSG(), Toast.LENGTH_SHORT).show();
                    }
                } catch (Exception e) {
                    hideLoading();
                    Log.e("LLLLL_Pofile_Error: ", e.getMessage());
                    e.printStackTrace();
                }

            }

            @Override
            public void onFailure(Call<SigninResponse> call, Throwable t) {
                Log.e("LLLLLL_Log_Error :", t.getMessage());
                Log.v(TAG, "TEST");
                call.cancel();
                hideLoading();
            }
        });

    }

    private void addDeviceId(final Context context) {

        final String token = SharedPrefManager.getInstance(context).getDeviceToken();
        final String deviceId = android.provider.Settings.Secure.getString(context.getContentResolver(),
                android.provider.Settings.Secure.ANDROID_ID);

        DeviceDataModel deviceDataModel = new DeviceDataModel();
        deviceDataModel.setDeviceId(deviceId);
        deviceDataModel.setDeviceToken(token);
        deviceDataModel.setType("Android");
        deviceDataModel.setUserId(GetSet.getUserId());

        Gson gson = new Gson();
        String data = gson.toJson(deviceDataModel);
        Call<SigninResponse> call3 = apiInterface.pushsignin(GetSet.getUserId(), "Android", deviceId, token);
        call3.enqueue(new Callback<SigninResponse>() {
            @Override
            public void onResponse(Call<SigninResponse> call, Response<SigninResponse> response) {
                if (response != null) {
                    SigninResponse data = response.body();
                    Log.v("addDeviceId:", "response- " + data);
                }

            }

            @Override
            public void onFailure(Call<SigninResponse> call, Throwable t) {
                call.cancel();
            }
        });
    }

    void updateMyprofile() {
        showLoading();

        Pattern emailPattern = Patterns.EMAIL_ADDRESS; // API level 8+
        Account[] accounts = AccountManager.get(ProfileInfo.this).getAccounts();

        UpdateProfileModel updateProfileModel = new UpdateProfileModel();
        updateProfileModel.setUserId(GetSet.getUserId());
        updateProfileModel.setAbout(about.getText().toString().trim());
        updateProfileModel.setUserName(name.getText().toString());
        updateProfileModel.setPhone(GetSet.getphonenumber());
        updateProfileModel.setCountryCode(GetSet.getcountrycode());
        Gson gson = new Gson();
        String profile = gson.toJson(updateProfileModel);

        Call<ProfileUpdatResModel> call3 = apiInterface.updatemyprofile(profile);
        call3.enqueue(new Callback<ProfileUpdatResModel>() {
            @Override
            public void onResponse(Call<ProfileUpdatResModel> call, Response<ProfileUpdatResModel> response) {
                try {
                    ProfileUpdatResModel userdata = response.body();
                    Log.v("response", "response=" + userdata.toString());

                    if (userdata.getSTATUS()) {
                        editor.putBoolean("isLogged", true);
                        editor.putString("userId", userdata.getRESULT().getId());
                        editor.putString("userName", userdata.getRESULT().getUserName());
                        editor.putString("userImage", userdata.getRESULT().getUserImage());
                        editor.putString("phoneNumber", userdata.getRESULT().getPhoneNo());
                        editor.putString("countryCode", userdata.getRESULT().getCountryCode());
                        editor.putString("token", "" + GetSet.getToken());
                        editor.putString("about", userdata.getRESULT().getAbout());
                        editor.putString("privacyprofileimage", userdata.getRESULT().getPrivacyProfileImage());
                        editor.putString("privacylastseen", userdata.getRESULT().getPrivacyLastSeen());
                        editor.putString("privacyabout", userdata.getRESULT().getPrivacyAbout());

                        editor.putString("oldWallpaper", "");
                        editor.putString("newWallpaper", "");
                        editor.putString("vibrateType", "Default");
                        editor.putString("callVibrateType", "Default");
                        editor.putString("privacyprofileimage", userdata.getRESULT().getPrivacyProfileImage());
                        editor.putString("privacylastseen", userdata.getRESULT().getPrivacyLastSeen());
                        editor.putString("privacyabout", userdata.getRESULT().getPrivacyAbout());
                        editor.putBoolean("archiveAll", false);
                        if (accounts != null) {
                            if (emailPattern.matcher(accounts[0].name).matches()) {
                                editor.putString("backupEmail", accounts[0].name);
                            }
                        }
                        editor.putBoolean("isPriorieNotification", true);
                        editor.putBoolean("isUseCustNotification", false);
                        editor.putBoolean("isAPPPriorieNotification", true);
                        editor.putBoolean("isAPPGrpPriorieNotification", true);
                        editor.putString("backupTime", "Never");
                        editor.putString("backupOver", "Wi-Fi");
                        editor.putString("mutenotification", "");

                        editor.commit();

                        GetSet.setLogged(true);
                        GetSet.setUserId(pref.getString("userId", null));
                        GetSet.setUserName(pref.getString("userName", null));
                        GetSet.setphonenumber(pref.getString("phoneNumber", null));
                        GetSet.setcountrycode(pref.getString("countryCode", null));
                        GetSet.setImageUrl(pref.getString("userImage", null));
                        GetSet.setToken(pref.getString("token", null));
                        GetSet.setAbout(pref.getString("about", null));
                        GetSet.setPrivacyprofileimage(pref.getString("privacyprofileimage", Constants.TAG_EVERYONE));
                        GetSet.setPrivacylastseen(pref.getString("privacylastseen", Constants.TAG_EVERYONE));
                        GetSet.setPrivacyabout(pref.getString("privacyabout", Constants.TAG_EVERYONE));

                        Toast.makeText(getApplicationContext(), getString(R.string.updated_successfully), Toast.LENGTH_SHORT).show();
                        hideLoading();
                        finish();

                        userForSave = createUserWithEnteredData(String.valueOf(userdata.getRESULT().getId()),userdata.getRESULT().getUserName());
                        startSignUpNewUser(userForSave);

//                        if (ValidationUtils.isLoginValid(ProfileInfo.this, userdata.getRESULT().getUserName()) &&
//                                ValidationUtils.isFoolNameValid(ProfileInfo.this, userdata.getRESULT().getUserName())) {
//                            userForSave = createUserWithEnteredData(String.valueOf(userdata.getRESULT().getId()),userdata.getRESULT().getUserName());
//                            startSignUpNewUser(userForSave);
//                        }

                    } else if (!userdata.getSTATUS()) {
                        hideLoading();
                        Toast.makeText(getApplicationContext(), userdata.getMSG(), Toast.LENGTH_SHORT).show();
                    }
                } catch (Exception e) {
                    hideLoading();
                    e.printStackTrace();
                }

            }

            @Override
            public void onFailure(Call<ProfileUpdatResModel> call, Throwable t) {
                Log.e(TAG, "updateMyProfile: " + t.getMessage());
                call.cancel();
                hideLoading();
            }
        });

    }

    public void saveMyContacts(List<ContactsModel> contacts) {
        HashMap<String, String> map = new HashMap<>();
        map.put(Constants.TAG_USER_ID, GetSet.getUserId());
        map.put(Constants.TAG_CONTACTS, "" + contacts);
//        Log.v(TAG, "saveMyContacts=" + contacts);
        for (ContactsModel s : contacts) {
            if (s.getNumber().contains("+")) {
                Log.e(TAG, "saveMyContacts: " + s);
            }
        }
//        Call<SaveMyContacts> call = apiInterface.saveMyContacts(GetSet.getToken(), map);
//        call.enqueue(new Callback<SaveMyContacts>() {
//            @Override
//            public void onResponse(Call<SaveMyContacts> call, Response<SaveMyContacts> response) {
//                Log.v(TAG, "saveMyContacts=" + new Gson().toJson(response));
        updatemycontacts(contacts);
//            }
//
//            @Override
//            public void onFailure(Call<SaveMyContacts> call, Throwable t) {
//                Log.e(TAG, "saveMyContacts: " + t.getMessage());
//                call.cancel();
//                hideLoading();
//            }
//        });
    }

    void updatemycontacts(List<ContactsModel> contactsNum) {
//        ContactsModel contactsModel = new ContactsModel();
//        contactsModel.setNumber(contactsNum);
//        contactsModel.setPhone(GetSet.getphonenumber());
//        contactsModel.setUserId(GetSet.getUserId());
//        contactsModel.setUserToken(GetSet.getToken());
        Gson gson = new Gson();
        String contacts = gson.toJson(contactsNum);

//        Log.v(TAG, "updateMyContacts: " + map);
//        Call<ContactsData> call3 = apiInterface.updatemycontacts(GetSet.getToken(), GetSet.getUserId(), GetSet.getphonenumber(), "[\n" +
//                "\t\t{\n" +
//                "\t\t\t\"number\":\"111111\"\n" +
//                "\t\t}\n" +
//                "\t]");

        Call<ContactsData> call3 = apiInterface.updatemycontacts(GetSet.getToken(), GetSet.getUserId(), GetSet.getphonenumber(), contacts);
        call3.enqueue(new Callback<ContactsData>() {
            @Override
            public void onResponse(Call<ContactsData> call, Response<ContactsData> response) {
                try {
                    Log.v("updateMyContacts", "response=" + new Gson().toJson(response));
                    ContactsData data = response.body();
                    if (data != null && data.status.equals("true")) {
                        new UpdateContactTask(data).execute();
                    } else if (data != null && data.status.equals("false")) {
                        socketConnection.disconnect();
                        socketConnection = SocketConnection.getInstance(ProfileInfo.this);
                        Intent service = new Intent(ProfileInfo.this, ForegroundService.class);
                        service.setAction("start");
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                            startForegroundService(service);
                        } else {
                            startService(service);
                        }
                        new Handler().postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                hideLoading();
                                finish();
                                Intent in = new Intent(ProfileInfo.this, MainActivity.class);
                                startActivity(in);
                            }
                        }, 500);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    hideLoading();
                }
            }

            @Override
            public void onFailure(Call<ContactsData> call, Throwable t) {
                Log.e(TAG, "updateMyContacts: " + t.getMessage());
                call.cancel();
                hideLoading();
            }
        });

    }

    @SuppressLint("StaticFieldLeak")
    private class GetContactTask extends AsyncTask<Void, Integer, Void> {
        List<ContactsModel> contactsNum = new ArrayList<>();

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            super.onProgressUpdate(values);
        }

        @Override
        protected Void doInBackground(Void... voids) {
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
                                ContactsModel contact = new ContactsModel();
                                contact.setNumber(tempNo.replaceAll("[^0-9]", ""));
                                contactsNum.add(contact);
//                                Log.v("Name", "name=" + name + " num="+tempNo.replaceAll("[^0-9]", ""));
                            }
                        } catch (NumberParseException e) {
                            if (isValidPhoneNumber(phoneNo)) {
                                if (phoneNo.startsWith("0")) {
                                    phoneNo = phoneNo.replaceFirst("^0+(?!$)", "");
                                }
//                                Log.v("Name", "excep name=" + name + " num="+phoneNo.replaceAll("[^0-9]", ""));
                                ContactsModel contact = new ContactsModel();
                                contact.setNumber(phoneNo.replaceAll("[^0-9]", ""));
                                contactsNum.add(contact);
                            }
                        }
                    }
                } finally {
                    cur.close();
                }
            }
            Log.e(TAG, "getContactList: " + contactsNum.size());
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
//            List<ContactsModel> contactsNum = new ArrayList<>();
//            ContactsModel contact = new ContactsModel();
//            contact.setNumber("8122484752");
//            contactsNum.add(contact);
//            contact.setNumber("9961109842");
//            contactsNum.add(contact);
//            contact.setNumber("7736595629");
//            contactsNum.add(contact);
//            contact.setNumber("111111");
//            contactsNum.add(contact);

            saveMyContacts(contactsNum);
        }
    }

    public boolean isValidPhoneNumber(CharSequence target) {
        if (target.length() < 7 || target.length() > 15) {
            return false;
        } else {
            return android.util.Patterns.PHONE.matcher(target).matches();
        }
    }

    private void uploadImage(File file) {
        progressDialog.show();
        RequestBody requestFile = RequestBody.create(MediaType.parse("multipart/form-data"), file);
        MultipartBody.Part body = MultipartBody.Part.createFormData("file", file.getName(), requestFile);

        RequestBody userid = RequestBody.create(MediaType.parse("multipart/form-data"), GetSet.getUserId());
        Call<ProfileUpdatResModel> call3 = apiInterface.upmyprofile(body, userid);
        call3.enqueue(new Callback<ProfileUpdatResModel>() {
            @Override
            public void onResponse(Call<ProfileUpdatResModel> call, Response<ProfileUpdatResModel> response) {
                ProfileUpdatResModel data = response.body();
                Log.v(TAG, "uploadImageresponse=" + data);

                if (data != null && data.getRESULT() != null) {
                    GetSet.setImageUrl(data.getRESULT().getUserImage());
                    GetSet.setAbout(data.getRESULT().getAbout());
                    editor.putString("userImage", data.getRESULT().getUserImage());
                    editor.commit();

                    if (data.getRESULT().getUserImage() != null) {
                        Glide.with(ProfileInfo.this).load(Constants.USER_IMG_PATH + data.getRESULT().getUserImage())
                                .apply(RequestOptions.circleCropTransform().placeholder(R.drawable.temp).error(R.drawable.temp))
                                .listener(new RequestListener<Drawable>() {
                                    @Override
                                    public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                                        noimage.setVisibility(View.VISIBLE);
                                        userImage.setVisibility(View.GONE);
                                        return false;
                                    }

                                    @Override
                                    public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                                        noimage.setVisibility(View.GONE);
                                        userImage.setVisibility(View.VISIBLE);
                                        return false;
                                    }
                                }).into(userImage);
                    }

                    Toast.makeText(getApplicationContext(), getString(R.string.updated_successfully), Toast.LENGTH_SHORT).show();
                }
                if (progressDialog.isShowing())
                    progressDialog.dismiss();
            }

            @Override
            public void onFailure(Call<ProfileUpdatResModel> call, Throwable t) {
                Log.v(TAG, "uploadImage=" + t.getMessage());
                if (progressDialog.isShowing())
                    progressDialog.dismiss();
                call.cancel();
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == -1 && requestCode == 234) {
            try {
                Bitmap bitmap = ImagePicker.getImageFromResult(this, requestCode, resultCode, data);
                String timestamp = String.valueOf(System.currentTimeMillis() / 1000L);
                String imageStatus = storageManager.saveToSdCard(getApplicationContext(), bitmap, "profile", timestamp + ".jpg");
                if (imageStatus.equals("success")) {
                    File file = storageManager.getImage("profile", timestamp + ".jpg");
                    String filepath = file.getAbsolutePath();
                    Log.i(TAG, "selectedImageFile: " + filepath);
                    ImageCompression imageCompression = new ImageCompression(ProfileInfo.this) {
                        @Override
                        protected void onPostExecute(String imagePath) {
                            try {
                                byte[] bytes = org.apache.commons.io.FileUtils.readFileToByteArray(new File(imagePath));
//                                uploadImage(bytes);
                                uploadImage(new File(imagePath));
                            } catch (IOException ex) {
                                ex.printStackTrace();
                            }
                        }
                    };
                    imageCompression.execute(filepath);
                } else {
                    Toast.makeText(this, getString(R.string.something_wrong), Toast.LENGTH_SHORT).show();
                }
            } catch (Exception e) {
                Log.e(TAG, "onActivityResult: " + e.getMessage());
                e.printStackTrace();
            }
        }
        if (resultCode == Constants.EXTRA_LOGIN_RESULT_CODE) {
            boolean isLoginSuccess = data.getBooleanExtra(Constants.EXTRA_LOGIN_RESULT, false);
            String errorMessage = data.getStringExtra(Constants.EXTRA_LOGIN_ERROR_MESSAGE);

            if (isLoginSuccess) {
                saveUserData(userForSave);
                signInCreatedUser(userForSave);
            } else {
                ToastUtils.longToast(getString(R.string.login_chat_login_error) + errorMessage);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        Log.v("requestCode", "requestCode=" + requestCode);
        switch (requestCode) {
            case 100:
                int permissionContacts = ContextCompat.checkSelfPermission(ProfileInfo.this,
                        READ_CONTACTS);
                int permissionStorage = ContextCompat.checkSelfPermission(ProfileInfo.this,
                        WRITE_EXTERNAL_STORAGE);
                int permissionSms = ContextCompat.checkSelfPermission(ProfileInfo.this,
                        RECEIVE_SMS);

                if (permissionContacts == PackageManager.PERMISSION_GRANTED && permissionStorage == PackageManager.PERMISSION_GRANTED &&
                        permissionSms == PackageManager.PERMISSION_GRANTED) {
                    if (NetworkReceiver.isConnected()) {
                        updateProfile();
                    } else {
                        Snackbar.make(mainLay, getString(R.string.no_internet_connection), Snackbar.LENGTH_LONG).show();
                    }
                }
                break;
            case 101:
                int permStorage = ContextCompat.checkSelfPermission(ProfileInfo.this,
                        WRITE_EXTERNAL_STORAGE);
                if (permStorage == PackageManager.PERMISSION_GRANTED) {
                    ImagePicker.pickImage(this, "Select your image:");
                }
                break;
        }
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.userImage:
            case R.id.noimage:
                if (ContextCompat.checkSelfPermission(this, WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(this, new String[]{WRITE_EXTERNAL_STORAGE}, 101);
                } else {
                    ImagePicker.pickImage(this, getString(R.string.select_your_image));
                }
                break;

            case R.id.backbtn:
                finish();
                break;
        }
    }

    public void showLoading() {
        progressbar.setVisibility(View.VISIBLE);
        fab.setVisibility(View.GONE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
                WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
        btnNext.setEnabled(false);
//        if (progressDialog != null && !progressDialog.isShowing()){
//            progressDialog.show();
//        }

    }

    public void hideLoading() {
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
        progressbar.setVisibility(View.GONE);
        fab.setVisibility(View.VISIBLE);
        btnNext.setEnabled(true);
//        if(progressDialog != null && progressDialog.isShowing()){
//            progressDialog.dismiss();
//        }
    }


//    @SuppressLint("StaticFieldLeak")
//    private class InsertMyChannelTask extends AsyncTask<Void, Integer, Void> {
//        List<ChannelResult.Result> result = new ArrayList<>();
//
//        @Override
//        protected void onPreExecute() {
//            super.onPreExecute();
//        }
//
//        public InsertMyChannelTask(List<ChannelResult.Result> result) {
//            this.result = result;
//        }
//
//        @Override
//        protected void onProgressUpdate(Integer... values) {
//            super.onProgressUpdate(values);
//        }
//
//        @Override
//        protected Void doInBackground(Void... voids) {
//            for (ChannelResult.Result result : result) {
//                dbhelper.addChannel(result.channelId, result.channelName, result.channelDes, result.channelImage, result.channelType,
//                        result.adminId, GetSet.getUserName(), result.totalSubscribers, result.createdTime, Constants.TAG_USER_CHANNEL, "", result.blockStatus);
//
//                if (!dbhelper.isChannelIdExistInMessages(result.channelId)) {
//                    String unixStamp = String.valueOf(System.currentTimeMillis() / 1000L);
//                    RandomString randomString = new RandomString(10);
//                    String messageId = result.channelId + randomString.nextString();
//                    dbhelper.addChannelMessages(result.channelId, Constants.TAG_CHANNEL, messageId, "create_channel",
//                            "", "", "", "", "", "", "",
//                            unixStamp, "", "");
//
//                    int unseenCount = dbhelper.getUnseenChannelMessagesCount(result.channelId);
//                    dbhelper.addChannelRecentMsgs(result.channelId, messageId, unixStamp, "" + unseenCount);
//                }
//            }
//            return null;
//        }
//
//        @Override
//        protected void onPostExecute(Void aVoid) {
//            super.onPostExecute(aVoid);
////            getSubscribedChannels();
//        }
//    }

//    @SuppressLint("StaticFieldLeak")
//    private class InsertSubscribedChannelTask extends AsyncTask<Void, Integer, Void> {
//        List<ChannelResult.Result> result = new ArrayList<>();
//
//        public InsertSubscribedChannelTask(List<ChannelResult.Result> result) {
//            this.result = result;
//        }
//
//        @Override
//        protected void onPreExecute() {
//            super.onPreExecute();
//        }
//
//        @Override
//        protected void onProgressUpdate(Integer... values) {
//            super.onProgressUpdate(values);
//        }
//
//        @Override
//        protected Void doInBackground(Void... voids) {
//            for (ChannelResult.Result result : result) {
//
//                if (dbhelper.isChannelExist(result.channelId)) {
//                    dbhelper.updateChannelWithoutAdminName(result.channelId, result.channelName, result.channelDes, result.channelImage,
//                            result.channelType != null ? result.channelType : Constants.TAG_PUBLIC, result.adminId != null ? result.adminId : "", result.totalSubscribers);
//                } else {
//                    dbhelper.addChannel(result.channelId, result.channelName, result.channelDes, result.channelImage, result.channelType,
//                            result.adminId, "", result.totalSubscribers, result.createdTime, Constants.TAG_USER_CHANNEL, Constants.TRUE, result.blockStatus);
//                }
//                if (!dbhelper.isChannelIdExistInMessages(result.channelId)) {
//                    String unixStamp = String.valueOf(System.currentTimeMillis() / 1000L);
//                    RandomString randomString = new RandomString(10);
//                    String messageId = result.channelId + randomString.nextString();
//                    dbhelper.addChannelMessages(result.channelId, Constants.TAG_CHANNEL, messageId, "create_channel",
//                            "", "", "", "", "", "", "",
//                            unixStamp, "", "");
//
//                    int unseenCount = dbhelper.getUnseenChannelMessagesCount(result.channelId);
//                    dbhelper.addChannelRecentMsgs(result.channelId, messageId, unixStamp, "" + unseenCount);
//                }
//            }
//            return null;
//        }
//
//        @Override
//        protected void onPostExecute(Void aVoid) {
//            super.onPostExecute(aVoid);
//            hideLoading();
//        }
//    }

    @SuppressLint("StaticFieldLeak")
    private class InsertMyGroupTask extends AsyncTask<Void, Integer, Void> {
        List<GroupData> result = new ArrayList<>();

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        public InsertMyGroupTask(List<GroupData> result) {
            this.result = result;
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            super.onProgressUpdate(values);
        }

        @Override
        protected Void doInBackground(Void... voids) {
            for (GroupData groupData : result) {
                dbhelper.createGroup(groupData.groupId, groupData.groupAdminId, groupData.groupName, groupData.createdAt, groupData.groupImage);

                for (GroupData.GroupMembers groupMember : groupData.groupMembers) {
                    getUserInfo(groupData, groupMember);
                }

                String unixStamp = String.valueOf(System.currentTimeMillis() / 1000L);
                RandomString randomString = new RandomString(10);
                String messageId = groupData.groupId + randomString.nextString();

                if (!groupData.groupAdminId.equals(GetSet.getUserId())) {
                    String unixStamp2 = String.valueOf(System.currentTimeMillis() / 1000L);
                    String messageId2 = groupData.groupId + randomString.nextString();
                    dbhelper.addGroupMessages(messageId2, groupData.groupId, GetSet.getUserId(), groupData.groupAdminId, "add_member",
                            "", "", "", "",
                            "", "", "", unixStamp2, "", "", "");
                }

                dbhelper.addGroupMessages(messageId, groupData.groupId, GetSet.getUserId(), groupData.groupAdminId, "create_group",
                        "", "", "", "",
                        "", "", "", unixStamp, "", "", "");

                int unseenCount = dbhelper.getUnseenGroupMessagesCount(groupData.groupId);
                dbhelper.addGroupRecentMsgs(groupData.groupId, messageId, GetSet.getUserId(), unixStamp, "" + unseenCount);
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            hideLoading();
//            getMyChannels();
        }
    }

    @SuppressLint("StaticFieldLeak")
    private class InsertGroupMemberTask extends AsyncTask<Void, Integer, Void> {
        UserProfileModel userdata;
        GroupData groupData = new GroupData();
        GroupData.GroupMembers groupMember = new GroupData().new GroupMembers();

        public InsertGroupMemberTask(GroupData groupData, GroupData.GroupMembers groupMember, UserProfileModel userdata) {
            this.groupData = groupData;
            this.userdata = userdata;
            this.groupMember = groupMember;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            super.onProgressUpdate(values);
        }

        @Override
        protected Void doInBackground(Void... voids) {
            dbhelper.addContactDetails(userdata.getRESULT().getId(), userdata.getRESULT().getUserName(), userdata.getRESULT().getPhoneNo(),
                    userdata.getRESULT().getCountryCode(), userdata.getRESULT().getUserImage(), userdata.getRESULT().getPrivacyAbout(),
                    userdata.getRESULT().getPrivacyLastSeen(), userdata.getRESULT().getPrivacyProfileImage(), userdata.getRESULT().getAbout(), "true");

            String memberKey = groupData.groupId + groupMember.memberId;
            dbhelper.createGroupMembers(memberKey, groupData.groupId, groupMember.memberId, groupMember.memberRole);
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
        }
    }

    @SuppressLint("StaticFieldLeak")
    private class UpdateContactTask extends AsyncTask<Void, Integer, Void> {
        ContactsData data = new ContactsData();

        public UpdateContactTask(ContactsData data) {
            this.data = data;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            super.onProgressUpdate(values);
        }

        @Override
        protected Void doInBackground(Void... voids) {
            for (ContactsData.Result result : data.result) {
                dbhelper.addContactDetails(result.user_id, result.user_name, result.phone_no, result.country_code, result.user_image, result.privacy_about,
                        result.privacy_last_scene, result.privacy_profile_image, result.about, result.contactstatus);
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            socketConnection.disconnect();
            socketConnection = SocketConnection.getInstance(ProfileInfo.this);
            Intent service = new Intent(ProfileInfo.this, ForegroundService.class);
            service.setAction("start");
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                startForegroundService(service);
            } else {
                startService(service);
            }

        }
    }

    private QBUser createUserWithEnteredData(String username, String passowrd) {
        return createQBUserWithCurrentData(username,
                passowrd);
    }

    private QBUser createQBUserWithCurrentData(String userLogin, String userFullName) {
        QBUser qbUser = null;
        if (!TextUtils.isEmpty(userLogin) && !TextUtils.isEmpty(userFullName)) {
            qbUser = new QBUser();
            qbUser.setLogin(userLogin);
            qbUser.setFullName(userFullName);
            qbUser.setPassword(ApplicationClass.USER_DEFAULT_PASSWORD);
        }
        return qbUser;
    }

    private void startSignUpNewUser(final QBUser newUser) {
        Log.e("LLLLL_Quickbox_Login1:", "SignUp New User");
        requestExecutor.signUpNewUser(newUser, new QBEntityCallback<QBUser>() {
                    @Override
                    public void onSuccess(QBUser result, Bundle params) {
                        Log.e("LLLLL_Quickbox_Login3: ", "SignUp Successful");
                        saveUserData(newUser);
                        loginToChat(result);
                    }

                    @Override
                    public void onError(QBResponseException e) {
                        Log.e("LLLLL_Quickbox_Login2: ", "Error SignUp" + e.getMessage());
                        if (e.getHttpStatusCode() == Constants.ERR_LOGIN_ALREADY_TAKEN_HTTP_STATUS) {
                            signInCreatedUser(newUser);
                        } else {
                            Toast.makeText(ProfileInfo.this, R.string.sign_up_error, Toast.LENGTH_LONG).show();
                        }
                    }
                }
        );
    }

    private void loginToChat(final QBUser qbUser) {
        qbUser.setPassword(ApplicationClass.USER_DEFAULT_PASSWORD);
        userForSave = qbUser;
        startLoginService(qbUser);
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                Intent in = new Intent(ProfileInfo.this, MainActivity.class);
                startActivity(in);
                hideLoading();
                finish();
            }
        }, 1000);
    }

    private void startLoginService(QBUser qbUser) {
        Intent tempIntent = new Intent(this, LoginService.class);
        PendingIntent pendingIntent = createPendingResult(Constants.EXTRA_LOGIN_RESULT_CODE, tempIntent, 0);
        LoginService.start(this, qbUser, pendingIntent);
    }

    private void signInCreatedUser(final QBUser qbUser) {
        Log.e("LLLLL_Quickbox_Login4: ", "SignIn Started");
        requestExecutor.signInUser(qbUser, new QBEntityCallbackImpl<QBUser>() {
            @Override
            public void onSuccess(QBUser user, Bundle params) {
                Log.e("LLLLL_Quickbox_Login5: ", "SignIn Successful");
                sharedPrefsHelper.saveQbUser(user);
                updateUserOnServer(qbUser);
            }

            @Override
            public void onError(QBResponseException responseException) {
                Log.e("LLLLL_Quickbox_Login6: ", "Error SignIn" + responseException.getMessage());
                Toast.makeText(ProfileInfo.this, R.string.sign_in_error, Toast.LENGTH_LONG).show();
            }
        });
    }

    private void saveUserData(QBUser qbUser) {
        sharedPrefsHelper.saveQbUser(qbUser);
    }

//    private QBUser createUserWithEnteredData(String login, String Username) {
//        return createQBUserWithCurrentData(login, Username);
//    }

    private void updateUserOnServer(QBUser user) {
        user.setPassword(null);
        QBUsers.updateUser(user).performAsync(new QBEntityCallback<QBUser>() {
            @Override
            public void onSuccess(QBUser qbUser, Bundle bundle) {
              finish();
            }

            @Override
            public void onError(QBResponseException e) {
                Toast.makeText(ProfileInfo.this, R.string.update_user_error, Toast.LENGTH_LONG).show();
            }
        });
    }
}
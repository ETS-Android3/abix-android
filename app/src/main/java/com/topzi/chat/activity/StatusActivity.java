package com.topzi.chat.activity;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Typeface;
import android.media.ThumbnailUtils;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.snackbar.Snackbar;
import com.topzi.chat.R;
import com.topzi.chat.external.ImageUtils;
import com.topzi.chat.external.RandomString;
import com.topzi.chat.helper.DatabaseHandler;
import com.topzi.chat.helper.FileUploadService;
import com.topzi.chat.helper.ImageCompression;
import com.topzi.chat.helper.NetworkUtil;
import com.topzi.chat.helper.SocketConnection;
import com.topzi.chat.helper.StorageManager;
import com.topzi.chat.model.MessagesData;
import com.topzi.chat.model.UpMyChatModel;
import com.topzi.chat.utils.ApiClient;
import com.topzi.chat.utils.ApiInterface;
import com.topzi.chat.utils.Constants;
import com.topzi.chat.utils.GetSet;

import org.apache.commons.io.FileUtils;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collections;

import butterknife.BindView;
import butterknife.ButterKnife;
import droidninja.filepicker.FilePickerConst;
import droidninja.filepicker.utils.ContentUriUtils;
import hani.momanii.supernova_emoji_library.Actions.EmojIconActions;
import hani.momanii.supernova_emoji_library.Helper.EmojiconEditText;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static com.topzi.chat.activity.ChatActivity.isVideoFile;
import static com.topzi.chat.helper.NetworkUtil.NOT_CONNECT;


public class StatusActivity extends AppCompatActivity implements View.OnClickListener, SocketConnection.StatusCallbackListener, SocketConnection.RecentStatusReceivedListener {

    @BindView(R.id.drawLay)
    RelativeLayout drawLay;
    @BindView(R.id.imgBgColor)
    ImageView imgBgColor;
    @BindView(R.id.imgSetEmoji)
    ImageView imgSetEmoji;
    @BindView(R.id.etStatus)
    EmojiconEditText etStatus;
    @BindView(R.id.tvFontChange)
    TextView tvFontChange;
    @BindView(R.id.mainLay)
    RelativeLayout mainLay;
    @BindView(R.id.btnSend)
    RelativeLayout btnSend;

    public static ArrayList<Integer> solidWallpaper = new ArrayList<>();
    public static ArrayList<Integer> solidWallpaper1 = new ArrayList<>();
    private static int position = 0;
    private static int textPosition = 0;
    ArrayList<String> font_data = new ArrayList();
    EmojIconActions emojIcon;
    DatabaseHandler dbhelper;
    StorageManager storageManager;
    ApiInterface apiInterface;
    SocketConnection socketConnection;
    File saveFile;

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStatusBarStatus(StatusActivity.this);
        setContentView(R.layout.activity_status);

        ButterKnife.bind(StatusActivity.this);
        storageManager = StorageManager.getInstance(StatusActivity.this);
        dbhelper = DatabaseHandler.getInstance(StatusActivity.this);
        apiInterface = ApiClient.getClient().create(ApiInterface.class);
        socketConnection = SocketConnection.getInstance(StatusActivity.this);

        solidWallpaper.clear();
        solidWallpaper.add(getResources().getColor(R.color.status_1));
        solidWallpaper.add(getResources().getColor(R.color.status_2));
        solidWallpaper.add(getResources().getColor(R.color.status_3));
        solidWallpaper.add(getResources().getColor(R.color.status_4));
        solidWallpaper.add(getResources().getColor(R.color.status_5));
        solidWallpaper.add(getResources().getColor(R.color.status_6));
        solidWallpaper.add(getResources().getColor(R.color.status_7));

        solidWallpaper1.clear();
        solidWallpaper1.add(R.color.stat_1);
        solidWallpaper1.add(R.color.stat_2);
        solidWallpaper1.add(R.color.stat_3);
        solidWallpaper1.add(R.color.stat_4);
        solidWallpaper1.add(R.color.stat_5);
        solidWallpaper1.add(R.color.stat_6);
        solidWallpaper1.add(R.color.stat_7);

        drawLay.setBackgroundColor(solidWallpaper.get(position));
        Window window = StatusActivity.this.getWindow();
        window.setBackgroundDrawableResource(solidWallpaper1.get(position));
        window.setNavigationBarColor(getResources().getColor(solidWallpaper1.get(position)));

        font_data.clear();
        Collections.addAll(font_data, getResources().getStringArray(R.array.FontFamily));
        Typeface face = Typeface.createFromAsset(getAssets(), font_data.get(0));
        etStatus.setTypeface(face);
        tvFontChange.setTypeface(face);

        imgBgColor.setOnClickListener(this);
        tvFontChange.setOnClickListener(this);
        imgSetEmoji.setOnClickListener(this);
        btnSend.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.imgBgColor:
                if (position + 1 < solidWallpaper.size())
                    position = position + 1;
                else
                    position = 0;

                runOnUiThread(() -> {
                    drawLay.setBackgroundColor(solidWallpaper.get(position));
                    Window window = StatusActivity.this.getWindow();
                    window.setBackgroundDrawableResource(solidWallpaper1.get(position));
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        window.setNavigationBarColor(getResources().getColor(solidWallpaper1.get(position)));
                    }
                });

                break;

            case R.id.tvFontChange:
                if (textPosition + 1 < font_data.size())
                    textPosition = textPosition + 1;
                else
                    textPosition = 0;

                runOnUiThread(() -> {
                    Typeface face = Typeface.createFromAsset(getAssets(), font_data.get(textPosition));
                    etStatus.setTypeface(face);
                    tvFontChange.setTypeface(face);
                });

                break;
            case R.id.imgSetEmoji:
                emojIcon = new EmojIconActions(StatusActivity.this, getWindow().getDecorView().findViewById(android.R.id.content), etStatus, imgSetEmoji);
                emojIcon.setUseSystemEmoji(true);
                emojIcon.setUseSystemEmoji(true);
                break;
            case R.id.btnSend:
                getTextBitmap();
                break;
        }
    }

    private void getTextBitmap() {
        saveFile = new File(getFilename());
        drawLay.setDrawingCacheEnabled(true);
        drawLay.layout(0, 0, drawLay.getMeasuredWidth(), drawLay.getMeasuredHeight());
        try {
            Bitmap bitmap = drawLay.getDrawingCache(true);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, new FileOutputStream(this.saveFile));
        } catch (FileNotFoundException e) {
            Log.e("LLLL_Err: ", e.getMessage());
        }
        System.out.println("Sagartext_path:" + saveFile.getAbsolutePath());
        addGallaryStatus();
    }

    public String getFilename() {
        File mediaStorageDir = new File(getExternalFilesDir(null) + "/" + getString(R.string.app_name) + "/" + getString(R.string.app_name) + "Images/Sent");
        // Create the storage directory if it does not exist
        if (!mediaStorageDir.exists()) {
            mediaStorageDir.mkdirs();
        }
        String mImageName = "TEMP_" + System.currentTimeMillis() + ".jpg";
        String uriString = (mediaStorageDir.getAbsolutePath() + "/" + mImageName);

        return uriString;
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public static void setStatusBarStatus(Activity activity) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = activity.getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(activity.getResources().getColor(R.color.nav));
            window.setNavigationBarColor(activity.getResources().getColor(R.color.nav));
        }
    }

    public void addGallaryStatus() {
        if (isNetworkConnected().equals(NOT_CONNECT)) {
            networkSnack();
        } else {

            Log.v("LLLLL_Status_Upload", "File");
            String filepath = null;

            filepath = saveFile.getAbsolutePath();

            Log.i("LLLLL_Status_Upload", "selectedFile: " + filepath);
            try {
                Log.v("checkChat", "imagepath=" + saveFile.getAbsolutePath());
                MessagesData mdata = updateDBList("image", saveFile.getAbsolutePath(), "");
                byte[] bytes = org.apache.commons.io.FileUtils.readFileToByteArray(new File(saveFile.getAbsolutePath()));
                uploadImage(bytes, saveFile.getAbsolutePath(), mdata, "");
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }

    private MessagesData updateDBList(String type, String imagePath, String filePath) {
        String unixStamp = String.valueOf(System.currentTimeMillis() / 1000L);
//        String chatId = GetSet.getUserId() + userId;
        RandomString randomString = new RandomString(10);
        String statusId = GetSet.getUserId() + randomString.nextString();

        String msg = "";
        switch (type) {
            case "image":
                msg = getString(R.string.image);
                break;
            case "audio":
                msg = getFileName(filePath);
                break;
            case "video":
                msg = getString(R.string.video);
                break;
            case "document":
                msg = getFileName(filePath);
                break;
        }

        MessagesData data = new MessagesData();
        data.user_id = GetSet.getUserId();
        data.message_type = type;
        data.message = msg;
        data.message_id = statusId;
        data.chat_time = unixStamp;
        data.delivery_status = "";
        data.progress = "";

        switch (type) {
            case "video":
                data.thumbnail = imagePath;
                data.attachment = filePath;
                dbhelper.addStatusData(statusId, GetSet.getUserId(), GetSet.getUserName(),
                        type, msg, filePath, "", "", "", "",
                        "", unixStamp, GetSet.getUserId(), GetSet.getUserId(), "", imagePath);
                break;
            case "image":
                data.thumbnail = "";
                data.attachment = imagePath;
                dbhelper.addStatusData(statusId, GetSet.getUserId(), GetSet.getUserName(),
                        type, msg, imagePath, "", "", "", "",
                        "", unixStamp, GetSet.getUserId(), GetSet.getUserId(), "", "");
                break;
            default:
                data.thumbnail = "";
                data.attachment = filePath;
                dbhelper.addStatusData(statusId, GetSet.getUserId(), GetSet.getUserName(),
                        type, msg, filePath, "", "", "", "",
                        "", unixStamp, GetSet.getUserId(), GetSet.getUserId(), "", "");
                break;
        }
        dbhelper.addRecentStatus(GetSet.getUserId(), statusId, unixStamp, "0");

        return data;
    }


    private String isNetworkConnected() {
        return NetworkUtil.getConnectivityStatusString(StatusActivity.this);
    }

    private void networkSnack() {
        Snackbar snackbar = Snackbar
                .make(mainLay, getString(R.string.network_failure), Snackbar.LENGTH_SHORT);
        View sbView = snackbar.getView();
        TextView textView = sbView.findViewById(com.google.android.material.R.id.snackbar_text);
        textView.setTextColor(Color.WHITE);
        snackbar.show();
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
                Log.v("TAG", "uploadImageresponse=" + data);
                if (data.getStatus().equals("true")) {
                    File dir = new File(StatusActivity.this.getExternalFilesDir(null) + "/" + getString(R.string.app_name) + "/" + getString(R.string.app_name) + "Images/Sent");

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

                        final int imgSize = ApplicationClass.dpToPx(StatusActivity.this, 170);
                        Log.v("file path", "file path=" + file.getAbsolutePath());

                        Bitmap bitmap = ImageUtils.compressImage(file.getAbsolutePath(), imgSize, imgSize);
                        String imgstatus = storageManager.saveThumbNail(bitmap, data.getResult().getImage());

                        if (mdata.message_type.equals("image")) {
                            if (imgstatus.equals("success")) {
                                dbhelper.updateStatusData(mdata.message_id, Constants.TAG_ATTACHMENT, data.getResult().getImage());
                                dbhelper.updateStatusData(mdata.message_id, Constants.TAG_PROGRESS, "completed");
                            }
                            mdata.attachment = data.getResult().getImage();
                            emitImage(mdata);
                        }
                    }
                } else {
                    dbhelper.updateStatusData(mdata.message_id, Constants.TAG_PROGRESS, "error");
                }
            }

            @Override
            public void onFailure(Call<UpMyChatModel> call, Throwable t) {
                Log.v("LLLLL_Status_Upload", "onFailure=" + "onFailure");
                call.cancel();
                dbhelper.updateStatusData(mdata.message_id, Constants.TAG_PROGRESS, "error");
//                if (messageListAdapter != null) {
//                    for (int i = 0; i < messagesList.size(); i++) {
//                        if (mdata.message_id.equals(messagesList.get(i).message_id)) {
//                            messagesList.get(i).progress = "error";
//                            messageListAdapter.notifyItemChanged(i);
//                            break;
//                        }
//                    }
//                }
            }
        });
    }

    private void emitImage(MessagesData mdata) {
        try {
//            JSONObject jobj = new JSONObject();
            JSONObject message = new JSONObject();
            message.put(Constants.TAG_USER_ID, GetSet.getUserId());
            message.put(Constants.TAG_USER_NAME, GetSet.getUserName());
            message.put(Constants.TAG_MESSAGE_TYPE, mdata.message_type);
            message.put(Constants.TAG_ATTACHMENT, mdata.attachment);
            message.put(Constants.TAG_COMMENT, mdata.message);
            message.put(Constants.TAG_CHAT_TIME, mdata.chat_time);
            message.put(Constants.TAG_STATUS_ID, mdata.message_id);
//            jobj.put(Constants.TAG_SENDER_ID, GetSet.getUserId());
//            jobj.put(Constants.TAG_RECEIVER_ID, userId);
//            jobj.put("message_data", message);
            Log.v("startchat", "startchat=" + message);
            socketConnection.statusAdd(message);
            finish();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private String getFileName(String url) {
        String imgSplit = url;
        int endIndex = imgSplit.lastIndexOf("/");
        if (endIndex != -1) {
            imgSplit = imgSplit.substring(endIndex + 1, imgSplit.length());
        }
        return imgSplit;
    }


    @Override
    public void onReceiveStatus(MessagesData mdata) {

    }

    @Override
    public void onEndStatus(String status_id, String sender_id, String receiverId) {

    }

    @Override
    public void onViewStatus(String status_id, String user_id) {

    }

    @Override
    public void onBlockStatus(JSONObject data) {

    }

    @Override
    public void onUpdateStatus(String user_id) {

    }

    @Override
    public void onRecentStatusReceived() {

    }

    @Override
    public void onUserImageChange(String user_id, String user_image) {

    }

    @Override
    public void onGetUpdateFromDB() {

    }

    @Override
    public void onUploadListen(String status_id, String attachment, String progress) {

    }

    @Override
    public void onPrivacyChanged(JSONObject jsonObject) {

    }
}
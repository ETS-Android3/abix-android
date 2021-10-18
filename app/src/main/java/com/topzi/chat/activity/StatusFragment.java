package com.topzi.chat.activity;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.MimeTypeMap;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.topzi.chat.BuildConfig;
import com.topzi.chat.R;
import com.topzi.chat.external.ImagePicker;
import com.topzi.chat.external.ImageUtils;
import com.topzi.chat.external.RandomString;
import com.topzi.chat.helper.DownloadFiles;
import com.topzi.chat.helper.FileUploadService;
import com.topzi.chat.helper.ImageCompression;
import com.topzi.chat.helper.ImageDownloader;
import com.topzi.chat.helper.NetworkUtil;
import com.topzi.chat.helper.StorageManager;
import com.topzi.chat.model.MessagesData;
import com.topzi.chat.model.UpMyChatModel;
import com.topzi.chat.utils.ApiClient;
import com.topzi.chat.utils.ApiInterface;
import com.topzi.chat.utils.GetSet;
import com.topzi.chat.helper.DatabaseHandler;
import com.topzi.chat.helper.SocketConnection;
import com.topzi.chat.utils.Constants;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.snackbar.Snackbar;

import org.apache.commons.io.FileUtils;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URISyntaxException;
import java.text.CharacterIterator;
import java.text.StringCharacterIterator;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

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
import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;
import static android.content.Context.MODE_PRIVATE;
import static com.topzi.chat.activity.ChatActivity.isVideoFile;
import static com.topzi.chat.helper.NetworkUtil.NOT_CONNECT;

public class StatusFragment extends Fragment implements SocketConnection.StatusCallbackListener, SocketConnection.RecentStatusReceivedListener, View.OnClickListener {

    private final String TAG = this.getClass().getSimpleName();
    RecyclerViewAdapter statusAdapter;
    RecyclerView recyclerView;
    RelativeLayout imageViewLay, mainLay, lytAttachment, bottomLay;
    List<MessagesData> allStatus;
    int currentStatusPosition = 0;
    int currentUserPosition = 0;
    Handler handler;
    ProgressBar progressBar;
    MessagesData message;
    //    LinearLayout nullLay;
    //    , headerLayout;
//    TextView nullText;
    //    , btnAllChannel;
    ImageView cameraBtn, galleryBtn, audioBtn, closeBtn, forwardBtn, backwardBtn, playBtn, drawBtn;
    LinearLayoutManager linearLayoutManager;
    DatabaseHandler dbhelper;
    StorageManager storageManager;
    ApiInterface apiInterface;
    ArrayList<Uri> pathsAry = new ArrayList<>();
    private ArrayList<HashMap<String, String>> statusList = new ArrayList<>();
    List<MessagesData> messagesList = new ArrayList<>();
    SocketConnection socketConnection;
    BottomSheetBehavior bottomSheetBehavior;
    ImageView imageView;
    boolean visible;

    SharedPreferences prefData;
    SharedPreferences.Editor editorData;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_status, container, false);

        prefData = getActivity().getSharedPreferences(Constants.NETWORK_USAGE, MODE_PRIVATE);
        editorData = prefData.edit();

//        SocketConnection.getInstance(getActivity()).setChannelRecentReceivedListener(this);
//        progressLay = view.findViewById(R.id.progress);
//        nullLay = view.findViewById(R.id.nullLay);
//        nullText = view.findViewById(R.id.nullText);
        lytAttachment = view.findViewById(R.id.attachmentsLay);
//        btnAllChannel = view.findViewById(R.id.btnAllChannel);
        closeBtn = view.findViewById(R.id.closeBtn);
        mainLay = view.findViewById(R.id.mainLay);
        recyclerView = view.findViewById(R.id.recyclerView);
        cameraBtn = view.findViewById(R.id.cameraBtn);
        galleryBtn = view.findViewById(R.id.galleryBtn);
        audioBtn = view.findViewById(R.id.audioBtn);
        imageViewLay = view.findViewById(R.id.imageViewLay);
        bottomSheetBehavior = BottomSheetBehavior.from(imageViewLay);
        imageView = view.findViewById(R.id.imageView);
        bottomLay = view.findViewById(R.id.bottom);
        forwardBtn = view.findViewById(R.id.img_forward);
        backwardBtn = view.findViewById(R.id.img_backward);
        progressBar = view.findViewById(R.id.progress);
        playBtn = view.findViewById(R.id.imgPlay);
        drawBtn = view.findViewById(R.id.drawBtn);
//        headerLayout.setVisibility(View.VISIBLE);
        storageManager = StorageManager.getInstance(getActivity());
        dbhelper = DatabaseHandler.getInstance(getActivity());
        linearLayoutManager = new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false);
        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.setHasFixedSize(true);

//        btnAllChannel.setOnClickListener(this);
        statusList = dbhelper.getRecentStatus(getActivity());
        Log.e("LLLL_Status list : ", String.valueOf(statusList.size()));
        statusAdapter = new RecyclerViewAdapter(getActivity(), statusList);
        recyclerView.setAdapter(statusAdapter);
//        statusAdapter.notifyDataSetChanged();
        cameraBtn.setOnClickListener(this);
        galleryBtn.setOnClickListener(this);
        audioBtn.setOnClickListener(this);
        closeBtn.setOnClickListener(this);
        forwardBtn.setOnClickListener(this);
        backwardBtn.setOnClickListener(this);
        playBtn.setOnClickListener(this);
        drawBtn.setOnClickListener(this);
        apiInterface = ApiClient.getClient().create(ApiInterface.class);
        socketConnection = SocketConnection.getInstance(getActivity());
        SocketConnection.getInstance(getActivity()).setStatusCallbackListener(this);
        SocketConnection.getInstance(getActivity()).setRecentStatusReceivedListener(this);
//        nullText.setText(R.string.no_channels_yet_buddy);
//        if (statusList.size() == 0) {
//            nullLay.setVisibility(View.VISIBLE);
//        } else {
//            nullLay.setVisibility(View.GONE);
//        }

        if (getActivity().getIntent().getStringExtra("EXTRA_EDITED_PATH")!=null){
            handleStatusSendImage();
        }

        return view;
    }

    @Override
    public void onResume() {
//        SocketConnection.getInstance(getActivity()).setChannelRecentReceivedListener(this);
        SocketConnection.getInstance(getActivity()).setRecentStatusReceivedListener(this);
        if (statusAdapter != null) {
            statusList.clear();
            statusList.addAll(dbhelper.getRecentStatus(getActivity()));
            statusAdapter.notifyDataSetChanged();
        }
//        if (statusList.size() == 0) {
//            nullLay.setVisibility(View.VISIBLE);
//        } else {
//            nullLay.setVisibility(View.GONE);
//        }
        super.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        SocketConnection.getInstance(getActivity()).setChannelRecentReceivedListener(null);
    }

//    @Override
//    public void onAdminChatReceive() {
//        if (getActivity() != null) {
//            getActivity().runOnUiThread(new Runnable() {
//                @Override
//                public void run() {
//                    if (statusAdapter != null) {
//                        statusList.clear();
//                        statusList.addAll(dbhelper.getChannelRecentMessages());
//                        statusAdapter.notifyDataSetChanged();
//                    }
//                }
//            });
//        }
//    }
//
//    @Override
//    public void onChannelInviteReceived(JSONObject jsonObject) {
//        getActivity().runOnUiThread(new Runnable() {
//            @Override
//            public void run() {
//                if (statusAdapter != null) {
//                    statusList.clear();
//                    statusList.addAll(dbhelper.getChannelRecentMessages());
//                    statusAdapter.notifyDataSetChanged();
//                }
//            }
//        });
//    }
//
//    @Override
//    public void onChannelDeleted() {
//        getActivity().runOnUiThread(new Runnable() {
//            @Override
//            public void run() {
//                if (statusAdapter != null) {
//                    statusList.clear();
//                    statusList.addAll(dbhelper.getChannelRecentMessages());
//                    statusAdapter.notifyDataSetChanged();
//
////                    if (statusList.size() == 0) {
////                        nullLay.setVisibility(View.GONE);
////                    }
//                }
//            }
//        });
//    }

//    @Override
//    public void onChannelCreated(JSONObject jsonObject) {
//        getActivity().runOnUiThread(new Runnable() {
//            @Override
//            public void run() {
//                if (statusAdapter != null) {
//                    statusList.clear();
//                    statusList.addAll(dbhelper.getChannelRecentMessages());
//                    statusAdapter.notifyDataSetChanged();
//                }
//            }
//        });
//    }
//
//    @Override
//    public void onChannelRecentReceived() {
//        if (getActivity() != null) {
//            getActivity().runOnUiThread(new Runnable() {
//                @Override
//                public void run() {
//                    if (statusAdapter != null) {
//                        statusList.clear();
//                        statusList.addAll(dbhelper.getChannelRecentMessages());
//                        statusAdapter.notifyDataSetChanged();
//                    }
//                }
//            });
//        }
//    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.cameraBtn:
                if (ContextCompat.checkSelfPermission(getActivity(), WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED ||
                        ContextCompat.checkSelfPermission(getActivity(), CAMERA) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(getActivity(), new String[]{WRITE_EXTERNAL_STORAGE, CAMERA}, Constants.statusCameraImage);
                } else if (isNetworkConnected().equals(NOT_CONNECT)) {
                    networkSnack();
                }
//                else if (results.blockedbyme.equals("block")) {
//                    blockChatConfirmDialog("unblock", "sent");
//                }
                else {
                    ApplicationClass.onShareExternal = true;
                    ImagePicker.pickImageCameraOnly(getActivity(), Constants.statusCameraImage);
                }
                break;
            case R.id.drawBtn:
                showCreteStatus();
                Intent intent = new Intent(getActivity(), StatusActivity.class);
                startActivity(intent);
                break;
            case R.id.galleryBtn:
                if (ContextCompat.checkSelfPermission(getActivity(), WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED ||
                        ContextCompat.checkSelfPermission(getActivity(), CAMERA) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(getActivity(), new String[]{WRITE_EXTERNAL_STORAGE, CAMERA}, Constants.statusGallery);
                } else if (isNetworkConnected().equals(NOT_CONNECT)) {
                    networkSnack();
                }
//                else if (results.blockedbyme.equals("block")) {
//                    blockChatConfirmDialog("unblock", "sent");
//                }
                else {
                    showCreteStatus();
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
                            .pickPhoto(this, Constants.statusGallery);
                }
                break;
            case R.id.audioBtn:
                if (ContextCompat.checkSelfPermission(getActivity(), WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(getActivity(), new String[]{WRITE_EXTERNAL_STORAGE}, Constants.statusAudio);
                } else {
                    if (isNetworkConnected().equals(NOT_CONNECT)) {
                        networkSnack();
                    }
//                    else if (results.blockedbyme.equals("block")) {
//                        blockChatConfirmDialog("unblock", "sent");
//                    }
                    else {
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
                                .pickFile(this, Constants.statusAudio);
                    }
                }
                break;
            case R.id.closeBtn:
                if (bottomSheetBehavior != null) {
                    bottomSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);
                }
                break;
            case R.id.img_forward:
                if (handler != null)
                    handler.removeCallbacksAndMessages(null);
                changeStatus(true);
                break;
            case R.id.img_backward:
                if (handler != null)
                    handler.removeCallbacksAndMessages(null);
                changeStatus(false);
                break;

            case R.id.imgPlay:
                playVideo(message);
                break;
        }
    }

    private String isNetworkConnected() {
        return NetworkUtil.getConnectivityStatusString(getActivity());
    }

    private void networkSnack() {
        Snackbar snackbar = Snackbar
                .make(mainLay, getString(R.string.network_failure), Snackbar.LENGTH_SHORT);
        View sbView = snackbar.getView();
        TextView textView = sbView.findViewById(com.google.android.material.R.id.snackbar_text);
        textView.setTextColor(Color.WHITE);
        snackbar.show();
    }

    public void showCreteStatus() {
//        TransitionManager.beginDelayedTransition(bottomLay);
        visible = !visible;
        lytAttachment.setVisibility(visible ? View.VISIBLE : View.GONE);
//        lytAttachment.setVisibility(View.VISIBLE);
    }

    public boolean showCreteStatus1() {
//        TransitionManager.beginDelayedTransition(bottomLay);
        visible = !visible;
        return lytAttachment.getVisibility() == View.VISIBLE;

//        lytAttachment.setVisibility(View.VISIBLE);
    }

    public void addCameraImageStatus(Intent data, int requestCode, int resultCode) {
        if (data != null) {
            if (isNetworkConnected().equals(NOT_CONNECT)) {
                networkSnack();
            } else {
                Log.v(TAG, "camera");
                Bitmap bitmap = ImagePicker.getImageFromResult(getActivity(), requestCode, resultCode, data);
                String timestamp = String.valueOf(System.currentTimeMillis() / 1000L);
                String imageStatus = storageManager.saveToSdCard(getActivity(), bitmap, "sent", timestamp + ".jpg");
                if (imageStatus.equals("success")) {
                    File file = storageManager.getImage("sent", timestamp + ".jpg");
                    String filepath = file.getAbsolutePath();

                    long size = file.length();
                    if (prefData.getLong("statusUpload", 0) == 0) {
                        editorData.putLong("statusUpload", size);
                    } else {
                        editorData.putLong("statusUpload", prefData.getLong("statusUpload", 0) + size);
                    }
                    editorData.apply();

                    long count = prefData.getLong("statusUpMesCount", 0);
                    editorData.putLong("statusUpMesCount", count + 1);
                    editorData.apply();
                    editorData.commit();

                    Log.i(TAG, "selectedImageFile: " + filepath);
                    ImageCompression imageCompression = new ImageCompression(getActivity()) {
                        @Override
                        protected void onPostExecute(String imagePath) {
                            try {
                                MessagesData mdata = updateDBList("image", imagePath, "");
                                byte[] bytes = org.apache.commons.io.FileUtils.readFileToByteArray(new File(imagePath));
                                uploadImage(bytes, imagePath, mdata, "");
                            } catch (IOException ex) {
                                ex.printStackTrace();
                            }
                        }
                    };
                    imageCompression.execute(filepath);
                } else {
                    Toast.makeText(getActivity(), getString(R.string.something_wrong), Toast.LENGTH_SHORT).show();
                }
            }
        } else
            Toast.makeText(getActivity(), getString(R.string.something_wrong), Toast.LENGTH_SHORT).show();
    }

    public void addGallaryStatus(Intent data) {
        if (data != null) {
            if (isNetworkConnected().equals(NOT_CONNECT)) {
                networkSnack();
            } else {
                pathsAry = new ArrayList<>();
                pathsAry.addAll(data.getParcelableArrayListExtra(FilePickerConst.KEY_SELECTED_MEDIA));
                if (pathsAry.size() > 0) {
                    Log.v(TAG, "File");
                    String filepath = null;
                    try {
                        filepath = ContentUriUtils.INSTANCE.getFilePath(getActivity(), pathsAry.get(0));

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
                                String imageStatus = storageManager.saveToSdCard(getActivity(), thumb, "sent", timestamp + ".jpg");
                                if (imageStatus.equals("success")) {
                                    File file = storageManager.getImage("sent", timestamp + ".jpg");

                                    long size = file.length();
                                    if (prefData.getLong("statusUpload", 0) == 0) {
                                        editorData.putLong("statusUpload", size);
                                    } else {
                                        editorData.putLong("statusUpload", prefData.getLong("statusUpload", 0) + size);
                                    }
                                    editorData.apply();

                                    long count = prefData.getLong("statusUpMesCount", 0);
                                    editorData.putLong("statusUpMesCount", count + 1);
                                    editorData.apply();
                                    editorData.commit();

                                    String imagePath = file.getAbsolutePath();
                                    MessagesData mdata = updateDBList("video", imagePath, filepath);
                                    byte[] bytes = org.apache.commons.io.FileUtils.readFileToByteArray(new File(imagePath));
                                    uploadImage(bytes, imagePath, mdata, filepath);
                                }
                            }
                        } catch (IOException ex) {
                            ex.printStackTrace();
                            Toast.makeText(getActivity(), getString(R.string.something_wrong), Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        ImageCompression imageCompression = new ImageCompression(getActivity()) {
                            @Override
                            protected void onPostExecute(String imagePath) {
                                try {
                                    Log.v("checkChat", "imagepath=" + imagePath);
                                    MessagesData mdata = updateDBList("image", imagePath, "");
                                    byte[] bytes = org.apache.commons.io.FileUtils.readFileToByteArray(new File(imagePath));

                                    File file = new File(imagePath);

                                    long size = file.length();
                                    if (prefData.getLong("statusUpload", 0) == 0) {
                                        editorData.putLong("statusUpload", size);
                                    } else {
                                        editorData.putLong("statusUpload", prefData.getLong("statusUpload", 0) + size);
                                    }
                                    editorData.apply();

                                    long count = prefData.getLong("statusUpMesCount", 0);
                                    editorData.putLong("statusUpMesCount", count + 1);
                                    editorData.apply();
                                    editorData.commit();

                                    uploadImage(bytes, imagePath, mdata, "");
                                } catch (IOException ex) {
                                    ex.printStackTrace();
                                }
                            }
                        };
                        imageCompression.execute(filepath);
                    }
                } else {
                    Toast.makeText(getActivity(), getString(R.string.something_wrong), Toast.LENGTH_SHORT).show();
                }
            }
        } else
            Toast.makeText(getActivity(), getString(R.string.something_wrong), Toast.LENGTH_SHORT).show();
    }

    public void handleStatusSendImage() {
        if (isNetworkConnected().equals(NOT_CONNECT)) {
            networkSnack();
        } else {
            Log.v(TAG, "File");
            String filepath = null;
            filepath = getActivity().getIntent().getStringExtra("EXTRA_EDITED_PATH");

            Log.i(TAG, "selectedFile: " + filepath);
            if (isVideoFile(filepath)) {
                try {
                    Log.v("checkChat", "videopath=" + filepath);
                    Bitmap thumb = ThumbnailUtils.createVideoThumbnail(filepath, MediaStore.Video.Thumbnails.MINI_KIND);
                    if (thumb != null) {
                        String timestamp = String.valueOf(System.currentTimeMillis() / 1000L);
                        String imageStatus = storageManager.saveToSdCard(getActivity(), thumb, "sent", timestamp + ".jpg");
                        if (imageStatus.equals("success")) {
                            File file = storageManager.getImage("sent", timestamp + ".jpg");

                            long size = file.length();
                            if (prefData.getLong("statusUpload", 0) == 0) {
                                editorData.putLong("statusUpload", size);
                            } else {
                                editorData.putLong("statusUpload", prefData.getLong("statusUpload", 0) + size);
                            }
                            editorData.apply();

                            long count = prefData.getLong("statusUpMesCount", 0);
                            editorData.putLong("statusUpMesCount", count + 1);
                            editorData.apply();
                            editorData.commit();

                            String imagePath = file.getAbsolutePath();
                            MessagesData mdata = updateDBList("video", imagePath, filepath);
                            byte[] bytes = org.apache.commons.io.FileUtils.readFileToByteArray(new File(imagePath));
                            uploadImage(bytes, imagePath, mdata, filepath);
                        }
                    }
                } catch (IOException ex) {
                    ex.printStackTrace();
                    Toast.makeText(getActivity(), getString(R.string.something_wrong), Toast.LENGTH_SHORT).show();
                }
            } else {
                ImageCompression imageCompression = new ImageCompression(getActivity()) {
                    @Override
                    protected void onPostExecute(String imagePath) {
                        try {

                            File file = new File(imagePath);

                            long size = file.length();
                            if (prefData.getLong("statusUpload", 0) == 0) {
                                editorData.putLong("statusUpload", size);
                            } else {
                                editorData.putLong("statusUpload", prefData.getLong("statusUpload", 0) + size);
                            }
                            editorData.apply();

                            long count = prefData.getLong("statusUpMesCount", 0);
                            editorData.putLong("statusUpMesCount", count + 1);
                            editorData.apply();
                            editorData.commit();

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
    }

    public void setFromGallaryStatus() {
        if (isNetworkConnected().equals(NOT_CONNECT)) {
            networkSnack();
        } else {
            Log.v(TAG, "File");
            String filepath = null;
            try {
                filepath = ContentUriUtils.INSTANCE.getFilePath(getActivity(), pathsAry.get(0));
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
                        String imageStatus = storageManager.saveToSdCard(getActivity(), thumb, "sent", timestamp + ".jpg");
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
                    Toast.makeText(getActivity(), getString(R.string.something_wrong), Toast.LENGTH_SHORT).show();
                }
            } else {
                ImageCompression imageCompression = new ImageCompression(getActivity()) {
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

    }

    public void addAudioStatus(Intent data, int requestCode, int resultCode) {
        if (data != null) {
            if (isNetworkConnected().equals(NOT_CONNECT)) {
                networkSnack();
            } else {
                pathsAry = new ArrayList<>();
                pathsAry.addAll(data.getParcelableArrayListExtra(FilePickerConst.KEY_SELECTED_DOCS));
                if (pathsAry.size() > 0) {
                    Log.v(TAG, "Audio");
                    String filepath = null;
                    try {
                        filepath = ContentUriUtils.INSTANCE.getFilePath(getActivity(), pathsAry.get(0));

                        File file = new File(filepath);

                        long size = file.length();
                        if (prefData.getLong("statusUpload", 0) == 0) {
                            editorData.putLong("statusUpload", size);
                        } else {
                            editorData.putLong("statusUpload", prefData.getLong("statusUpload", 0) + size);
                        }
                        editorData.apply();

                        long count = prefData.getLong("statusUpMesCount", 0);
                        editorData.putLong("statusUpMesCount", count + 1);
                        editorData.apply();
                        editorData.commit();

                    } catch (URISyntaxException e) {
                        e.printStackTrace();
                    }
                    Log.i(TAG, "selectedImageFile: " + filepath);
                    try {
                        MessagesData mdata = updateDBList("audio", "", filepath);
                        Intent service = new Intent(getActivity(), FileUploadService.class);
                        Bundle b = new Bundle();
                        b.putSerializable("mdata", mdata);
                        b.putString("filepath", filepath);
                        b.putString("chatType", Constants.status);
                        service.putExtras(b);
                        getActivity().startService(service);
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                } else {
                    Toast.makeText(getActivity(), getString(R.string.something_wrong), Toast.LENGTH_SHORT).show();
                }
            }
        } else
            Toast.makeText(getActivity(), getString(R.string.something_wrong), Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onReceiveStatus(MessagesData mdata) {
        if (statusAdapter != null) {
            statusList.clear();
            statusList.addAll(dbhelper.getRecentStatus(getActivity()));
            statusAdapter.notifyDataSetChanged();
        }
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

    public class RecyclerViewAdapter extends RecyclerView.Adapter<RecyclerViewAdapter.MyViewHolder> {

        ArrayList<HashMap<String, String>> statusList = new ArrayList<>();
        Context context;

        public RecyclerViewAdapter(Context context, ArrayList<HashMap<String, String>> statusList) {
            this.statusList = statusList;
            this.context = context;
        }

        @Override
        public RecyclerViewAdapter.MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View itemView = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.chat_item, parent, false);

            return new RecyclerViewAdapter.MyViewHolder(itemView);
        }

        @Override
        public void onBindViewHolder(final MyViewHolder holder, int position) {

            final HashMap<String, String> hashMap = statusList.get(position);

            holder.name.setText(hashMap.get(Constants.TAG_USER_NAME));

//            if (hashMap.get(Constants.TAG_CHANNEL_TYPE).equalsIgnoreCase(Constants.TAG_PRIVATE)) {
//                holder.privateImage.setVisibility(View.VISIBLE);
//            } else {
//                holder.privateImage.setVisibility(View.GONE);
//            }

            int unreadCount = dbhelper.getUnseenStatusCount(hashMap.get(Constants.TAG_SENDER_ID));
            if (unreadCount > 0 && !hashMap.get(Constants.TAG_SENDER_ID).equals(GetSet.getUserId())) {
                holder.unseenLay.setVisibility(View.VISIBLE);
                holder.unseenCount.setText("" + unreadCount);
            } else { holder.name.setText(Constants.myStatus);
                holder.unseenLay.setVisibility(View.GONE);
            }
            holder.message.setText(ApplicationClass.getTime(Long.parseLong(hashMap.get(Constants.TAG_CHAT_TIME).replace(".0", ""))));

            if (hashMap.get(Constants.TAG_BLOCKED_ME).equals("block")) {
                Glide.with(context).load(R.drawable.change_camera).thumbnail(0.5f)
                        .apply(RequestOptions.circleCropTransform().placeholder(R.drawable.change_camera).error(R.drawable.change_camera).override(ApplicationClass.dpToPx(context, 70)))
                        .into(holder.profileimage);
            } else {
                DialogActivity.setProfileImage(dbhelper.getContactDetail(hashMap.get(Constants.TAG_USER_ID)), holder.profileimage, context);
            }

            //            if (hashMap.get(Constants.TAG_CHANNEL_CATEGORY).equalsIgnoreCase(Constants.TAG_ADMIN_CHANNEL)) {
//                Glide.with(context).load(Constants.CHANNEL_IMG_PATH + hashMap.get(Constants.TAG_CHANNEL_IMAGE)).thumbnail(0.5f)
//                        .apply(RequestOptions.circleCropTransform().placeholder(R.drawable.change_camera).error(R.drawable.change_camera).override(ApplicationClass.dpToPx(context, 70)))
//                        .into(holder.profileimage);
//
//                holder.message.setText((hashMap.get(Constants.TAG_MESSAGE) != null && !hashMap.get(Constants.TAG_MESSAGE).equals("")) ? Utils.fromHtml(hashMap.get(Constants.TAG_MESSAGE)) : Utils.fromHtml(hashMap.get(Constants.TAG_CHANNEL_DES)));
//            } else {
//                Glide.with(context).load(Constants.CHANNEL_IMG_PATH + hashMap.get(Constants.TAG_CHANNEL_IMAGE)).thumbnail(0.5f)
//                        .apply(RequestOptions.circleCropTransform().placeholder(R.drawable.ic_channel_square).error(R.drawable.ic_channel_square).override(ApplicationClass.dpToPx(context, 70)))
//                        .into(holder.profileimage);
//
//                if (hashMap.get(Constants.TAG_MESSAGE_TYPE) == null) {
//                    holder.message.setText(hashMap.get(Constants.TAG_CHANNEL_DES));
//                } else if (hashMap.get(Constants.TAG_MESSAGE_TYPE).equals("create_channel") || hashMap.get(Constants.TAG_MESSAGE_TYPE).equals("new_invite")) {
//                    holder.message.setText(hashMap.get(Constants.TAG_MESSAGE) != null ? Utils.fromHtml(hashMap.get(Constants.TAG_CHANNEL_DES)) : "");
//                } else {
//                    holder.message.setText(hashMap.get(Constants.TAG_MESSAGE) != null ? Utils.fromHtml(hashMap.get(Constants.TAG_MESSAGE)) : Utils.fromHtml(hashMap.get(Constants.TAG_CHANNEL_DES)));
//                }
//            }

            if (hashMap.get(Constants.TAG_MUTE_NOTIFICATION).equals("true")) {
                holder.mute.setVisibility(View.VISIBLE);
            } else {
                holder.mute.setVisibility(View.GONE);
            }
        }

        @Override
        public int getItemCount() {
            return statusList.size();
        }

        public class MyViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

            LinearLayout parentlay, messageLay;
            RelativeLayout unseenLay;
            TextView name, message, time, unseenCount, typing;
            ImageView tickimage, typeicon, mute, privateImage;
            CircleImageView profileimage;
            View profileview;

            public MyViewHolder(View view) {
                super(view);

                parentlay = view.findViewById(R.id.parentlay);
                message = view.findViewById(R.id.message);
                time = view.findViewById(R.id.time);
                name = view.findViewById(R.id.name);
                profileimage = view.findViewById(R.id.profileimage);
                tickimage = view.findViewById(R.id.tickimage);
                typeicon = view.findViewById(R.id.typeicon);
                unseenLay = view.findViewById(R.id.unseenLay);
                unseenCount = view.findViewById(R.id.unseenCount);
                profileview = view.findViewById(R.id.profileview);
                typing = view.findViewById(R.id.typing);
                messageLay = view.findViewById(R.id.messageLay);
                mute = view.findViewById(R.id.mute);
                privateImage = view.findViewById(R.id.privateImage);

                parentlay.setOnClickListener(this);
            }

            @Override
            public void onClick(View view) {
                switch (view.getId()) {
                    case R.id.parentlay:
                        try {
                            currentUserPosition = getAdapterPosition();
                            currentStatusPosition = 0;
                            allStatus = dbhelper.getAllStatus(statusList.get(getAdapterPosition()).get(Constants.TAG_SENDER_ID), "0");
                            if (allStatus.get(currentStatusPosition).message_type.equalsIgnoreCase(Constants.image))
                                showStatusImage(allStatus.get(currentStatusPosition));
                            else if (allStatus.get(currentStatusPosition).message_type.equalsIgnoreCase(Constants.video)) {
                                showStatusVideo(allStatus.get(currentStatusPosition));
                            } else if (allStatus.get(currentStatusPosition).message_type.equalsIgnoreCase(Constants.audio)) {
                                showStatusAudio(allStatus.get(currentStatusPosition));
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
//                        if (hashMap.get(Constants.TAG_CHANNEL_CATEGORY).equalsIgnoreCase(Constants.TAG_USER_CHANNEL)) {
//                            if (hashMap.get(Constants.TAG_SUBSCRIBE_STATUS).equalsIgnoreCase("")) {
//                                i = new Intent(context, ChannelRequestActivity.class);
//                            } else if (hashMap.get(Constants.TAG_SUBSCRIBE_STATUS).equalsIgnoreCase(Constants.TRUE)) {
//                                i = new Intent(context, ChannelChatActivity.class);
//                            }
//                        } else {
//                            i = new Intent(context, ChannelChatActivity.class);
//                        }
//                        i.putExtra(Constants.TAG_CHANNEL_ID, hashMap.get(Constants.TAG_CHANNEL_ID));
//                        startActivity(i);
                        break;
                }
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

        messagesList.add(0, data);
//        messageListAdapter.notifyItemInserted(0);
        recyclerView.smoothScrollToPosition(0);

        return data;
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
                    //File dir = new File(getActivity().getExternalFilesDir(null) + "/" + getString(R.string.app_name) + "/" + getString(R.string.app_name) + "Images/Sent");
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

                        final int imgSize = ApplicationClass.dpToPx(getActivity(), 170);
                        Log.v("file path", "file path=" + file.getAbsolutePath());

                        Bitmap bitmap = ImageUtils.compressImage(file.getAbsolutePath(), imgSize, imgSize);
                        String imgstatus = storageManager.saveThumbNail(bitmap, data.getResult().getImage());

                        if (mdata.message_type.equals("image")) {
                            if (imgstatus.equals("success")) {
                                dbhelper.updateStatusData(mdata.message_id, Constants.TAG_ATTACHMENT, data.getResult().getImage());
                                dbhelper.updateStatusData(mdata.message_id, Constants.TAG_PROGRESS, "completed");
//                                if (messageListAdapter != null) {
//                                    for (int i = 0; i < messagesList.size(); i++) {
//                                        if (mdata.message_id.equals(messagesList.get(i).message_id)) {
//                                            messagesList.get(i).attachment = data.getResult().getImage();
//                                            messagesList.get(i).progress = "completed";
//                                            messageListAdapter.notifyItemChanged(i);
//                                            break;
//                                        }
//                                    }
//                                }
                            }
                            mdata.attachment = data.getResult().getImage();
//                            if (!results.blockedme.equals("block")) {
                            emitImage(mdata);
                            // Toast.makeText(ChatActivity.this, data.get(Constants.TAG_MESSAGE), Toast.LENGTH_SHORT).show();
//                            }
                        } else if (mdata.message_type.equals("video")) {
                            Log.v("checkChat", "uploadImage-video");
                            if (imgstatus.equals("success")) {
                                mdata.thumbnail = data.getResult().getImage();
                                dbhelper.updateStatusData(mdata.message_id, Constants.TAG_THUMBNAIL, data.getResult().getImage());
//                                if (messageListAdapter != null) {
//                                    for (int i = 0; i < messagesList.size(); i++) {
//                                        if (mdata.message_id.equals(messagesList.get(i).message_id)) {
//                                            messagesList.get(i).thumbnail = mdata.thumbnail;
//                                            messageListAdapter.notifyItemChanged(i);
//                                            break;
//                                        }
//                                    }
//                                }
                            }
                            Intent service = new Intent(getActivity(), FileUploadService.class);
                            Bundle b = new Bundle();
                            b.putSerializable("mdata", mdata);
                            b.putString("filepath", filePath);
                            b.putString("chatType", Constants.status);
                            service.putExtras(b);
                            getActivity().startService(service);
                        }
                    }
                } else {
                    dbhelper.updateStatusData(mdata.message_id, Constants.TAG_PROGRESS, "error");
//                    if (messageListAdapter != null) {
//                        for (int i = 0; i < messagesList.size(); i++) {
//                            if (mdata.message_id.equals(messagesList.get(i).message_id)) {
//                                messagesList.get(i).progress = "error";
//                                messageListAdapter.notifyItemChanged(i);
//                                break;
//                            }
//                        }
//                    }
                }
            }

            @Override
            public void onFailure(Call<UpMyChatModel> call, Throwable t) {
                Log.v(TAG, "onFailure=" + "onFailure");
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

    private String getFileName(String url) {
        String imgSplit = url;
        int endIndex = imgSplit.lastIndexOf("/");
        if (endIndex != -1) {
            imgSplit = imgSplit.substring(endIndex + 1, imgSplit.length());
        }
        return imgSplit;
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
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == -1 && requestCode == Constants.statusCameraImage) {
            addCameraImageStatus(data, requestCode, resultCode);
        } else if (resultCode == -1 && requestCode == Constants.statusGallery) {
            addGallaryStatus(data);
        } else if (resultCode == -1 && requestCode == Constants.statusAudio) {
            addAudioStatus(data, requestCode, resultCode);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        SocketConnection.getInstance(getActivity()).setStatusCallbackListener(null);
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

    private void showStatusImage(MessagesData message) {
        this.message = message;
        bottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
        playBtn.setVisibility(View.GONE);
        if (storageManager.checkifImageExists("thumb", message.attachment)) {
            File file = storageManager.getImage("thumb", message.attachment);

            long size = file.length();

            if (prefData.getLong("statusDownload", 0) == 0) {
                editorData.putLong("statusDownload", size);
            } else {
                editorData.putLong("statusDownload", prefData.getLong("statusDownload", 0) + size);
            }
            editorData.apply();

            editorData.commit();

            Log.e("LLLLL_statusDown: ", humanReadableByteCountSI(prefData.getLong("statusDownMesCount", 0)));

            if (file != null) {
                Log.v(TAG, "file=" + file.getAbsolutePath());
               Bitmap bitmap = getBitmap(file.getAbsolutePath());
                Glide.with(getActivity())
                        .asBitmap()
                        .load(bitmap)
                        .thumbnail(0.5f)
                        .into(imageView);

//                videoprogresslay.setVisibility(View.GONE);
//                Glide.with(getActivity()).load(file).thumbnail(0.5f)
//                        .transition(new DrawableTransitionOptions().crossFade())
//                        .into(imageView);
                showNextStatus(true);
            }
        } else {
            if (ContextCompat.checkSelfPermission(getActivity(), WRITE_EXTERNAL_STORAGE)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(getActivity(), new String[]{WRITE_EXTERNAL_STORAGE}, 100);
            } else {
                if (isNetworkConnected().equals(NOT_CONNECT)) {
                    networkSnack();
                } else {
                    ImageDownloader imageDownloader = new ImageDownloader(getActivity()) {
                        @Override
                        protected void onPostExecute(Bitmap imgBitmap) {
                            if (imgBitmap == null) {
                                Log.e("LLLLL_bitmapFailed", "bitmapFailed");
                                Toast.makeText(getActivity(), getString(R.string.something_wrong), Toast.LENGTH_SHORT).show();
                            } else {
                                Log.v("onBitmapLoaded", "onBitmapLoaded");
                                try {
//                                                            String[] fileName = message.attachment.split("/");
                                    String status = storageManager.saveThumbNail(imgBitmap, message.attachment);
                                    if (status.equals("success")) {
//                                        File thumbFile = storageManager.getImage("thumb", message.attachment);
//                                        Glide.with(getActivity()).load(thumbFile).thumbnail(0.5f)
//                                                .into(uploadimage);
//                                        progresslay.setVisibility(View.GONE);
//                                        progressbar.stopSpinning();
//                                        videoprogresslay.setVisibility(View.GONE);

                                        File file = storageManager.getImage("thumb", message.attachment);

                                        long count = prefData.getLong("statusDownMesCount", 0);
                                        editorData.putLong("statusDownMesCount", count + 1);
                                        editorData.apply();

                                        if (file != null) {
                                            Log.v(TAG, "file=" + file.getAbsolutePath());
                                            bottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
                                            progressBar.setVisibility(View.GONE);
                                            Glide.with(getActivity()).load(file).thumbnail(0.5f)
                                                    .transition(new DrawableTransitionOptions().crossFade())
                                                    .into(imageView);
                                        }

//                                               }
                                    } else {
                                        Log.e("LLLLL_bitmapFailed1: ","Fild..");
                                        Toast.makeText(getActivity(), getString(R.string.something_wrong), Toast.LENGTH_SHORT).show();
                                    }
                                } catch (NullPointerException e) {
                                    e.printStackTrace();
                                }
                            }
                            showNextStatus(true);
                            dbhelper.updateStatusData(message.message_id, Constants.TAG_DELIVERY_STATUS, Constants.read);
                            statusAdapter.notifyDataSetChanged();
// dbhelper.resetUnseenStatusCount(message.receiver_id);
                        }

                        @Override
                        protected void onProgressUpdate(String... progress) {
                            progressBar.setProgress(Integer.parseInt(progress[0]));
                        }
                    };
                    imageDownloader.execute(Constants.CHAT_IMG_PATH + message.attachment, "receive");
                    progressBar.setVisibility(View.VISIBLE);
//                    progressbar.spin();
                }
            }
        }
    }

    public Bitmap getBitmap(String path) {
        Bitmap bitmap=null;
        try {
            File f= new File(path);
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inPreferredConfig = Bitmap.Config.ARGB_8888;
            bitmap = BitmapFactory.decodeStream(new FileInputStream(f), null, options);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return bitmap ;
    }

    private void showStatusVideo(MessagesData message) {
        this.message = message;
        bottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
        if (storageManager.checkifFileExists(message.attachment, message.message_type, "receive") &&
                storageManager.checkifImageExists("thumb", message.thumbnail)) {

//            File file = storageManager.getImage("thumb", message.attachment);
//            if (file != null) {
//                Log.v(TAG, "file=" + file.getAbsolutePath());
//                videoprogresslay.setVisibility(View.GONE);
            String[] fileName = message.attachment.split("/");
            playBtn.setVisibility(View.VISIBLE);
            Glide.with(getActivity()).asBitmap().load(Uri.fromFile(new File("/storage/emulated/0/Topzi/AbixVideos/uploads/chatFiles/" + fileName[fileName.length - 1])))
//                        .transition(new DrawableTransitionOptions().crossFade())
                    .into(imageView);
            showNextStatus(true);
//            }


        } else {
            if (isNetworkConnected().equals(NOT_CONNECT)) {
                networkSnack();
            } else {
                ImageDownloader imageDownloader = new ImageDownloader(getActivity()) {
                    @Override
                    protected void onPostExecute(Bitmap imgBitmap) {
                        if (imgBitmap == null) {
                            Log.e("LLLLL_bitmapFailed3: ","Fild..");
                            Toast.makeText(getActivity(), getString(R.string.something_wrong), Toast.LENGTH_SHORT).show();
//                            videoprogresslay.setVisibility(View.GONE);
//                            videoprogressbar.setVisibility(View.GONE);
//                            videoprogressbar.stopSpinning();
                        } else {
                            Log.v("onBitmapLoaded", "onBitmapLoaded");
                            try {
                                String status = storageManager.saveThumbNail(imgBitmap, message.thumbnail);
                                if (status.equals("success")) {
                                    final File thumbFile = storageManager.getImage("thumb", message.thumbnail);
                                    if (thumbFile != null) {
                                        Log.v("file", "file=" + thumbFile.getAbsolutePath());

                                        long size = thumbFile.length();

                                        if (prefData.getLong("statusDownload", 0) == 0) {
                                            editorData.putLong("statusDownload", size);
                                        } else {
                                            editorData.putLong("statusDownload", prefData.getLong("statusDownload", 0) + size);
                                        }
                                        editorData.apply();

                                        long count = prefData.getLong("statusDownMesCount", 0);
                                        editorData.putLong("statusDownMesCount", count + 1);
                                        editorData.apply();
                                        editorData.commit();


                                        DownloadFiles downloadFiles = new DownloadFiles(getActivity()) {
                                            @Override
                                            protected void onPostExecute(String downPath) {
                                                progressBar.setVisibility(View.GONE);
//                                                videoprogressbar.setVisibility(View.GONE);
//                                                videoprogressbar.stopSpinning();
                                                if (downPath == null) {
                                                    Log.e("LLLLL_bitmapFailed5: ","Fild..");
                                                    Log.v("Download Failed", "Download Failed");
                                                    Toast.makeText(getActivity(), getString(R.string.something_wrong), Toast.LENGTH_SHORT).show();
                                                } else {
                                                    playBtn.setVisibility(View.VISIBLE);
                                                    Glide.with(getActivity()).load(Uri.fromFile(thumbFile)).thumbnail(0.5f)
                                                            .into(imageView);
                                                }
                                            }
                                        };
                                        downloadFiles.execute(Constants.CHAT_IMG_PATH + message.attachment, message.message_type);
                                    }
                                } else {
                                    Log.e("LLLLL_bitmapFailed6: ","Fild..");
                                    Toast.makeText(getActivity(), getString(R.string.something_wrong), Toast.LENGTH_SHORT).show();
//                                    videoprogresslay.setVisibility(View.GONE);
//                                    videoprogressbar.setVisibility(View.GONE);
//                                    videoprogressbar.stopSpinning();
                                }
                            } catch (NullPointerException e) {
                                e.printStackTrace();
                            }
                        }
                        showNextStatus(true);
                    }

                    @Override
                    protected void onProgressUpdate(String... progress) {
                        progressBar.setProgress(Integer.parseInt(progress[0]));
                    }
                };
                imageDownloader.execute(Constants.CHAT_IMG_PATH + message.thumbnail, "thumb");
                progressBar.setVisibility(View.VISIBLE);
//                videoprogressbar.setVisibility(View.VISIBLE);
//                videoprogressbar.spin();
            }
        }
    }

    private void playVideo(MessagesData message) {
        try {
            Intent intent = new Intent();
            intent.setAction(Intent.ACTION_VIEW);
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            File file = storageManager.getFile(message.attachment, message.message_type, "receive");
            Uri photoURI = FileProvider.getUriForFile(getActivity(),
                    BuildConfig.APPLICATION_ID + ".provider", file);

            MimeTypeMap mime = MimeTypeMap.getSingleton();
            String ext = file.getName().substring(file.getName().indexOf(".") + 1);
            String type = mime.getMimeTypeFromExtension(ext);

            intent.setDataAndType(photoURI, type);

            startActivity(intent);
        } catch (
                ActivityNotFoundException e) {
            Toast.makeText(getActivity(), getString(R.string.no_application), Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }

    }

    private void showStatusAudio(MessagesData message) {
        this.message = message;
        bottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
        imageView.setImageDrawable(getActivity().getResources().getDrawable(R.drawable.mp3));
        if (storageManager.checkifFileExists(message.attachment, message.message_type, "receive")) {
            playAudio(message);
        } else {
            if (isNetworkConnected().equals(NOT_CONNECT)) {
                networkSnack();
            } else {
                DownloadFiles downloadFiles = new DownloadFiles(getActivity()) {
                    @Override
                    protected void onPostExecute(String downPath) {

                        File file = new File(downPath);

                        long size = file.length();

                        if (prefData.getLong("statusDownload", 0) == 0) {
                            editorData.putLong("statusDownload", size);
                        } else {
                            editorData.putLong("statusDownload", prefData.getLong("statusDownload", 0) + size);
                        }
                        editorData.apply();

                        long count = prefData.getLong("statusDownMesCount", 0);
                        editorData.putLong("statusDownMesCount", count + 1);
                        editorData.apply();
                        editorData.commit();


                        progressBar.setVisibility(View.GONE);
                        if (downPath == null) {
                            Log.e("LLLLL_bitmapFailed7: ","Fild..");
                            Log.v("Download Failed", "Download Failed");
                            Toast.makeText(getActivity(), getString(R.string.something_wrong), Toast.LENGTH_SHORT).show();
                        } else {
//                            Toast.makeText(getActivity(), getString(R.string.downloaded), Toast.LENGTH_SHORT).show();
                            playAudio(message);
                        }
                    }
                };
                downloadFiles.execute(Constants.CHAT_IMG_PATH + message.attachment, message.message_type);
                progressBar.setVisibility(View.VISIBLE);
            }
        }

    }

    private void playAudio(MessagesData message) {
        try {
            Intent intent = new Intent();
            intent.setAction(android.content.Intent.ACTION_VIEW);
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            File file = storageManager.getFile(message.attachment, message.message_type, "receive");
            Uri photoURI = FileProvider.getUriForFile(getActivity(),
                    BuildConfig.APPLICATION_ID + ".provider", file);

            MimeTypeMap mime = MimeTypeMap.getSingleton();
            String ext = file.getName().substring(file.getName().indexOf(".") + 1);
            String type = mime.getMimeTypeFromExtension(ext);

            intent.setDataAndType(photoURI, type);

            startActivity(intent);
        } catch (ActivityNotFoundException e) {
            Toast.makeText(getActivity(), getString(R.string.no_application), Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
//        showNextStatus(true);
    }


    private void showNextStatus(boolean isNext) {
        handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (bottomSheetBehavior.getState() == BottomSheetBehavior.STATE_HIDDEN) {
                    return;
                }
                changeStatus(isNext);
            }
        }, 5000);
    }

    private void changeStatus(boolean isNext) {
        if (isNext) {
            if (currentStatusPosition + 1 < allStatus.size()) {
                if (allStatus.get(currentStatusPosition).message_type.equalsIgnoreCase(Constants.image))
                    showStatusImage(allStatus.get(currentStatusPosition + 1));
                else if (allStatus.get(currentStatusPosition).message_type.equalsIgnoreCase(Constants.video)) {
                    showStatusVideo(allStatus.get(currentStatusPosition));
                } else if (allStatus.get(currentStatusPosition).message_type.equalsIgnoreCase(Constants.audio)) {
                    showStatusAudio(allStatus.get(currentStatusPosition));
                }
                currentStatusPosition = currentStatusPosition + 1;
            } else if (currentUserPosition + 1 < statusList.size()) {
                try {
                    currentStatusPosition = 0;
                    allStatus = dbhelper.getAllStatus(statusList.get(currentUserPosition + 1).get(Constants.TAG_SENDER_ID), "0");
                    if (allStatus.get(currentStatusPosition).message_type.equalsIgnoreCase(Constants.image))
                        showStatusImage(allStatus.get(currentStatusPosition));
                    else if (allStatus.get(currentStatusPosition).message_type.equalsIgnoreCase(Constants.video)) {
                        showStatusVideo(allStatus.get(currentStatusPosition));
                    } else if (allStatus.get(currentStatusPosition).message_type.equalsIgnoreCase(Constants.audio)) {
                        showStatusAudio(allStatus.get(currentStatusPosition));
                    }
                    currentUserPosition = currentUserPosition + 1;
                } catch (Exception e) {
                    e.printStackTrace();
                }

            } else {
                bottomSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);
            }
        } else {
            if (currentStatusPosition - 1 >= 0) {
                currentStatusPosition = currentStatusPosition - 1;
                if (allStatus.get(currentStatusPosition).message_type.equalsIgnoreCase(Constants.image))
                    showStatusImage(allStatus.get(currentStatusPosition));
                else if (allStatus.get(currentStatusPosition).message_type.equalsIgnoreCase(Constants.video)) {
                    showStatusVideo(allStatus.get(currentStatusPosition));
                } else if (allStatus.get(currentStatusPosition).message_type.equalsIgnoreCase(Constants.audio)) {
                    showStatusAudio(allStatus.get(currentStatusPosition));
                }
            } else if (currentUserPosition - 1 >= statusList.size()) {
                currentUserPosition = currentUserPosition - 1;
                try {
                    currentStatusPosition = statusList.size();
                    allStatus = dbhelper.getAllStatus(statusList.get(currentUserPosition - 1).get(Constants.TAG_SENDER_ID), "0");
                    if (allStatus.get(currentStatusPosition).message_type.equalsIgnoreCase(Constants.image))
                        showStatusImage(allStatus.get(currentStatusPosition));
                    else if (allStatus.get(currentStatusPosition).message_type.equalsIgnoreCase(Constants.video)) {
                        showStatusVideo(allStatus.get(currentStatusPosition));
                    } else if (allStatus.get(currentStatusPosition).message_type.equalsIgnoreCase(Constants.audio)) {
                        showStatusAudio(allStatus.get(currentStatusPosition));
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }
}

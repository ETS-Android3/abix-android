package com.topzi.chat.activity;

import android.app.Dialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.snackbar.Snackbar;
import com.google.gson.Gson;
import com.topzi.chat.R;
import com.topzi.chat.helper.NetworkUtil;
import com.topzi.chat.model.MediaDown.MobileData;
import com.topzi.chat.utils.Constants;

import java.text.CharacterIterator;
import java.text.StringCharacterIterator;
import java.util.ArrayList;
import java.util.Arrays;

import butterknife.BindView;
import butterknife.ButterKnife;

import static com.topzi.chat.helper.NetworkUtil.NOT_CONNECT;
import static com.topzi.chat.utils.Constants.setStatusBarGradiant;

public class DataStorage extends AppCompatActivity implements View.OnClickListener {

    @BindView(R.id.mainLay)
    LinearLayout mainLay;
    @BindView(R.id.llData)
    LinearLayout llData;
    @BindView(R.id.llStorage)
    LinearLayout llStorage;
    @BindView(R.id.llMobileData)
    LinearLayout llMobileData;
    @BindView(R.id.llWifi)
    LinearLayout llWifi;
    @BindView(R.id.llRoaming)
    LinearLayout llRoaming;
    @BindView(R.id.img_back)
    ImageView img_back;
    @BindView(R.id.tvSent)
    TextView tvSent;
    @BindView(R.id.tvRecv)
    TextView tvRecv;
    @BindView(R.id.tvStorage)
    TextView tvStorage;
    @BindView(R.id.tvMobileData)
    TextView tvMobileData;
    @BindView(R.id.tvWfi)
    TextView tvWfi;
    @BindView(R.id.tvRoaming)
    TextView tvRoaming;

    Dialog dialog;
    SharedPreferences prefData;
    SharedPreferences.Editor editorData;

    MobileData Mobiledata1;
    MobileData WifiData;
    MobileData RoamingData;

    ArrayList<String> mobileData = new ArrayList<>();
    ArrayList<String> wifiData = new ArrayList<>();
    ArrayList<String> roamingData = new ArrayList<>();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStatusBarGradiant(DataStorage.this);
        setContentView(R.layout.activity_data_storage);

        ButterKnife.bind(DataStorage.this);
        prefData = DataStorage.this.getSharedPreferences(Constants.NETWORK_USAGE, MODE_PRIVATE);
        editorData = prefData.edit();

        setData();

        llData.setOnClickListener(this);
        llStorage.setOnClickListener(this);
        llMobileData.setOnClickListener(this);
        llWifi.setOnClickListener(this);
        llRoaming.setOnClickListener(this);
        llMobileData.setOnClickListener(this);
        img_back.setOnClickListener(this);
    }

    private void setData() {
        long totalSentUsage = prefData.getLong("callSent", 0) +
                prefData.getLong("mediaUpload", 0) +
                prefData.getLong("MesSent", 0) +
                prefData.getLong("statusUpload", 0);

        tvSent.setText(humanReadableByteCountSI(totalSentUsage));

        long totalRecvUsage = prefData.getLong("callReceive", 0) +
                prefData.getLong("mediaDownload", 0) +
                prefData.getLong("MesReceive", 0) +
                prefData.getLong("statusDownload", 0);

        tvRecv.setText(humanReadableByteCountSI(totalRecvUsage));

        long totalUsage = prefData.getLong("callSent", 0) +
                prefData.getLong("callReceive", 0) +
                prefData.getLong("mediaDownload", 0) +
                prefData.getLong("mediaUpload", 0) +
                prefData.getLong("MesReceive", 0) +
                prefData.getLong("MesSent", 0);

        long mediaTotal = (prefData.getLong("mediaDownload", 0) +
                prefData.getLong("mediaUpload", 0));

        tvStorage.setText(humanReadableByteCountSI(mediaTotal));

        if (!prefData.getString("MobileData", "").equals("")&&
                prefData.getString("MobileData", "")!=null) {
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

        if (!prefData.getString("WifiData", "").equals("")&&
                prefData.getString("WifiData", "")!=null) {
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

        if (!prefData.getString("RoamingData", "").equals("")&&
                prefData.getString("RoamingData", "")!=null) {
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

        if (Mobiledata1.isPhotos())
            mobileData.add(getString(R.string.Photos1));
        if (Mobiledata1.isAudio())
            mobileData.add(getString(R.string.Audios));
        if (Mobiledata1.isVideo())
            mobileData.add(getString(R.string.videos));
        if (Mobiledata1.isDoc())
            mobileData.add(getString(R.string.Doc));
        if (mobileData.isEmpty())
            mobileData.add("No Media");

        StringBuilder sb = new StringBuilder();
        for (String s : mobileData) {
            if (s != null) {
                sb.append(s).append(',');
            }
        }
        String mobileDa = sb.toString();

        if (WifiData.isPhotos())
            wifiData.add(getString(R.string.Photos1));
        if (WifiData.isAudio())
            wifiData.add(getString(R.string.Audios));
        if (WifiData.isVideo())
            wifiData.add(getString(R.string.videos));
        if (WifiData.isDoc())
            wifiData.add(getString(R.string.Doc));
        if (wifiData.isEmpty())
            wifiData.add("No Media");

        StringBuilder sb1 = new StringBuilder();
        for (String s : wifiData) {
            if (s != null) {
                sb1.append(s).append(',');
            }
        }
        String wifiDa = sb.toString();

        if (RoamingData.isPhotos())
            roamingData.add(getString(R.string.Photos1));
        if (RoamingData.isAudio())
            roamingData.add(getString(R.string.Audios));
        if (RoamingData.isVideo())
            roamingData.add(getString(R.string.videos));
        if (RoamingData.isDoc())
            roamingData.add(getString(R.string.Doc));
        if (roamingData.isEmpty())
            roamingData.add("No Media");

        StringBuilder sb2 = new StringBuilder();
        for (String s : roamingData) {
            if (s != null) {
                sb2.append(s).append(',');
            }
        }
        String roamingDa = sb.toString();

        Log.e("LLLL_Data: ", wifiData.toString() +"       "+wifiDa);
        tvMobileData.setText(mobileData.toString().replace("[","").replace("]",""));
        tvWfi.setText(wifiData.toString().replace("[","").replace("]",""));
        tvRoaming.setText(roamingData.toString().replace("[","").replace("]",""));
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.llData:
                Intent intent = new Intent(DataStorage.this, NetworkUsage.class);
                startActivity(intent);
                finish();
                break;
            case R.id.llStorage:
                Intent intent1 = new Intent(DataStorage.this, DetailStorageActivity.class);
                startActivity(intent1);
                finish();
                break;
            case R.id.llMobileData:
                setMobileData();
                break;
            case R.id.llWifi:
                setWifiData();
                break;
            case R.id.llRoaming:
                setRoamingData();
                break;
            case R.id.img_back:
                onBackPressed();
                break;
        }
    }

    private void setMobileData() {

        dialog = new Dialog(DataStorage.this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        dialog.setContentView(R.layout.dialoge_media_down);
        dialog.getWindow().setLayout(getResources().getDisplayMetrics().widthPixels * 90 / 100, ViewGroup.LayoutParams.WRAP_CONTENT);
        dialog.setCancelable(true);
        dialog.setCanceledOnTouchOutside(true);

        TextView tv_cancel = dialog.findViewById(R.id.tv_cancel);
        TextView tv_ok = dialog.findViewById(R.id.tv_ok);
        TextView title = dialog.findViewById(R.id.title);

        title.setText(getResources().getString(R.string.mobile_data_active));

        CheckBox checkboxPhoto = dialog.findViewById(R.id.checkboxPhoto);
        CheckBox checkboxAudio = dialog.findViewById(R.id.checkboxAudio);
        CheckBox checkboxVideo = dialog.findViewById(R.id.checkboxVideo);
        CheckBox checkboxDoc = dialog.findViewById(R.id.checkboxDoc);


        if (!prefData.getString("MobileData", "").equals("")) {
            Gson gson1 = new Gson();
            String json1 = prefData.getString("MobileData", "");
            MobileData mobileData1 = gson1.fromJson(json1, MobileData.class);

            checkboxPhoto.setChecked(mobileData1.isPhotos());
            checkboxAudio.setChecked(mobileData1.isAudio());
            checkboxVideo.setChecked(mobileData1.isVideo());
            checkboxDoc.setChecked(mobileData1.isDoc());
        } else {
            checkboxPhoto.setChecked(true);
            checkboxAudio.setChecked(false);
            checkboxVideo.setChecked(false);
            checkboxDoc.setChecked(false);

            MobileData mobileData = new MobileData();
            mobileData.setPhotos(checkboxPhoto.isChecked());
            mobileData.setAudio(checkboxAudio.isChecked());
            mobileData.setVideo(checkboxVideo.isChecked());
            mobileData.setDoc(checkboxDoc.isChecked());

            Gson gson = new Gson();
            String json = gson.toJson(mobileData);
            editorData.putString("MobileData", json);
            editorData.apply();
            editorData.commit();
        }

        tv_ok.setOnClickListener(v -> {
            if (isNetworkConnected().equals(NOT_CONNECT)) {
                networkSnack();
                dialog.dismiss();
            } else {
                MobileData mobileData = new MobileData();
                mobileData.setPhotos(checkboxPhoto.isChecked());
                mobileData.setAudio(checkboxAudio.isChecked());
                mobileData.setVideo(checkboxVideo.isChecked());
                mobileData.setDoc(checkboxDoc.isChecked());
                Gson gson = new Gson();
                String json = gson.toJson(mobileData);
                editorData.putString("MobileData", json);
                editorData.apply();
                editorData.commit();
                dialog.dismiss();
            }
        });


        tv_cancel.setOnClickListener(v -> dialog.dismiss());

        dialog.show();

    }

    private void setWifiData() {

        dialog = new Dialog(DataStorage.this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        dialog.setContentView(R.layout.dialoge_media_down);
        dialog.getWindow().setLayout(getResources().getDisplayMetrics().widthPixels * 90 / 100, ViewGroup.LayoutParams.WRAP_CONTENT);
        dialog.setCancelable(true);
        dialog.setCanceledOnTouchOutside(true);

        TextView tv_cancel = dialog.findViewById(R.id.tv_cancel);
        TextView tv_ok = dialog.findViewById(R.id.tv_ok);
        TextView title = dialog.findViewById(R.id.title);

        title.setText(getResources().getString(R.string.wifi_data_active));

        CheckBox checkboxPhoto = dialog.findViewById(R.id.checkboxPhoto);
        CheckBox checkboxAudio = dialog.findViewById(R.id.checkboxAudio);
        CheckBox checkboxVideo = dialog.findViewById(R.id.checkboxVideo);
        CheckBox checkboxDoc = dialog.findViewById(R.id.checkboxDoc);


        if (!prefData.getString("WifiData", "").equals("")) {
            Gson gson1 = new Gson();
            String json1 = prefData.getString("WifiData", "");
            MobileData mobileData1 = gson1.fromJson(json1, MobileData.class);

            checkboxPhoto.setChecked(mobileData1.isPhotos());
            checkboxAudio.setChecked(mobileData1.isAudio());
            checkboxVideo.setChecked(mobileData1.isVideo());
            checkboxDoc.setChecked(mobileData1.isDoc());
        } else {
            MobileData mobileData = new MobileData();

            checkboxPhoto.setChecked(true);
            checkboxAudio.setChecked(true);
            checkboxVideo.setChecked(true);
            checkboxDoc.setChecked(true);

            mobileData.setPhotos(checkboxPhoto.isChecked());
            mobileData.setAudio(checkboxAudio.isChecked());
            mobileData.setVideo(checkboxVideo.isChecked());
            mobileData.setDoc(checkboxDoc.isChecked());
            Gson gson = new Gson();
            String json = gson.toJson(mobileData);
            editorData.putString("WifiData", json);
            editorData.apply();
            editorData.commit();
        }

        tv_ok.setOnClickListener(v -> {
            if (isNetworkConnected().equals(NOT_CONNECT)) {
                networkSnack();
                dialog.dismiss();
            } else {
                MobileData mobileData = new MobileData();
                mobileData.setPhotos(checkboxPhoto.isChecked());
                mobileData.setAudio(checkboxAudio.isChecked());
                mobileData.setVideo(checkboxVideo.isChecked());
                mobileData.setDoc(checkboxDoc.isChecked());
                Gson gson = new Gson();
                String json = gson.toJson(mobileData);
                editorData.putString("WifiData", json);
                editorData.apply();
                editorData.commit();
                dialog.dismiss();
            }
        });


        tv_cancel.setOnClickListener(v -> dialog.dismiss());

        dialog.show();

    }

    private void setRoamingData() {

        dialog = new Dialog(DataStorage.this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        dialog.setContentView(R.layout.dialoge_media_down);
        dialog.getWindow().setLayout(getResources().getDisplayMetrics().widthPixels * 90 / 100, ViewGroup.LayoutParams.WRAP_CONTENT);
        dialog.setCancelable(true);
        dialog.setCanceledOnTouchOutside(true);

        TextView tv_cancel = dialog.findViewById(R.id.tv_cancel);
        TextView tv_ok = dialog.findViewById(R.id.tv_ok);
        TextView title = dialog.findViewById(R.id.title);

        title.setText(getResources().getString(R.string.data_roaming));

        CheckBox checkboxPhoto = dialog.findViewById(R.id.checkboxPhoto);
        CheckBox checkboxAudio = dialog.findViewById(R.id.checkboxAudio);
        CheckBox checkboxVideo = dialog.findViewById(R.id.checkboxVideo);
        CheckBox checkboxDoc = dialog.findViewById(R.id.checkboxDoc);


        if (!prefData.getString("RoamingData", "").equals("")) {
            Gson gson1 = new Gson();
            String json1 = prefData.getString("RoamingData", "");
            MobileData mobileData1 = gson1.fromJson(json1, MobileData.class);

            checkboxPhoto.setChecked(mobileData1.isPhotos());
            checkboxAudio.setChecked(mobileData1.isAudio());
            checkboxVideo.setChecked(mobileData1.isVideo());
            checkboxDoc.setChecked(mobileData1.isDoc());
        } else {
            MobileData mobileData = new MobileData();

            checkboxPhoto.setChecked(false);
            checkboxAudio.setChecked(false);
            checkboxVideo.setChecked(false);
            checkboxDoc.setChecked(false);

            mobileData.setPhotos(checkboxPhoto.isChecked());
            mobileData.setAudio(checkboxAudio.isChecked());
            mobileData.setVideo(checkboxVideo.isChecked());
            mobileData.setDoc(checkboxDoc.isChecked());
            Gson gson = new Gson();
            String json = gson.toJson(mobileData);
            editorData.putString("RoamingData", json);
            editorData.apply();
            editorData.commit();
        }

        tv_ok.setOnClickListener(v -> {
            if (isNetworkConnected().equals(NOT_CONNECT)) {
                networkSnack();
                dialog.dismiss();
            } else {
                MobileData mobileData = new MobileData();
                mobileData.setPhotos(checkboxPhoto.isChecked());
                mobileData.setAudio(checkboxAudio.isChecked());
                mobileData.setVideo(checkboxVideo.isChecked());
                mobileData.setDoc(checkboxDoc.isChecked());
                Gson gson = new Gson();
                String json = gson.toJson(mobileData);
                editorData.putString("RoamingData", json);
                editorData.apply();
                editorData.commit();
                dialog.dismiss();
            }
        });


        tv_cancel.setOnClickListener(v -> dialog.dismiss());

        dialog.show();

    }

    private String isNetworkConnected() {
        return NetworkUtil.getConnectivityStatusString(this);
    }

    private void networkSnack() {
        Snackbar snackbar = Snackbar
                .make(mainLay, getString(R.string.network_failure), Snackbar.LENGTH_SHORT);
        View sbView = snackbar.getView();
        TextView textView = sbView.findViewById(com.google.android.material.R.id.snackbar_text);
        textView.setTextColor(Color.WHITE);
        snackbar.show();
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
    public void onBackPressed() {
        super.onBackPressed();
    }
}
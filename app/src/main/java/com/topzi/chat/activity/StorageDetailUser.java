package com.topzi.chat.activity;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.topzi.chat.R;
import com.topzi.chat.helper.DatabaseHandler;
import com.topzi.chat.model.ContactsData;
import com.topzi.chat.model.DataStorageModel;

import java.text.CharacterIterator;
import java.text.StringCharacterIterator;
import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import de.hdodenhof.circleimageview.CircleImageView;

import static com.topzi.chat.utils.Constants.setStatusBarGradiant;

public class StorageDetailUser extends AppCompatActivity implements View.OnClickListener {

    @BindView(R.id.img_back)
    ImageView img_back;
    @BindView(R.id.userImage)
    CircleImageView userImage;
    @BindView(R.id.tvUsername)
    TextView tvUsername;
    @BindView(R.id.tvSize)
    TextView tvSize;
    @BindView(R.id.tvMsgCount)
    TextView tvMsgCount;
    @BindView(R.id.tvContCount)
    TextView tvContCount;
    @BindView(R.id.tvLocCount)
    TextView tvLocCount;
    @BindView(R.id.tvPhotoCount)
    TextView tvPhotoCount;
    @BindView(R.id.tvVidCount)
    TextView tvVidCount;
    @BindView(R.id.tvAudCount)
    TextView tvAudCount;
    @BindView(R.id.tvDocCount)
    TextView tvDocCount;
    @BindView(R.id.tvPhotoSize)
    TextView tvPhotoSize;
    @BindView(R.id.tvVideoSize)
    TextView tvVideoSize;
    @BindView(R.id.tvAudSize)
    TextView tvAudSize;
    @BindView(R.id.tvDocSize)
    TextView tvDocSize;

    ContactsData.Result results;
    DatabaseHandler dbhelper;
    DataStorageModel storageModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStatusBarGradiant(StorageDetailUser.this);
        setContentView(R.layout.activity_storage_detail_user);

        String userID = getIntent().getStringExtra("UserID");
        ButterKnife.bind(StorageDetailUser.this);
        dbhelper = DatabaseHandler.getInstance(this);

        storageModel = dbhelper.getRecord(userID);

        results = dbhelper.getContactDetail(storageModel.getUserId());
        tvUsername.setText(ApplicationClass.getContactName(StorageDetailUser.this, results.phone_no));
        DialogActivity.setProfileImage(dbhelper.getContactDetail(storageModel.getUserId()), userImage, StorageDetailUser.this);

        long total = Long.parseLong(storageModel.getSent_photos_size()) +
                Long.parseLong(storageModel.getSent_videos_size()) +
                Long.parseLong(storageModel.getSent_aud_size()) +
                Long.parseLong(storageModel.getSent_doc_size());

        tvSize.setText(humanReadableByteCountSI(total));
        setData();

        img_back.setOnClickListener(this);

    }

    private void setData(){
        tvMsgCount.setText(storageModel.getMessage_count());
        tvContCount.setText(storageModel.getSent_contact());
        tvLocCount.setText(storageModel.getSent_location());
        tvPhotoCount.setText(storageModel.getSent_photos());
        tvVidCount.setText(storageModel.getSent_videos());
        tvAudCount.setText(storageModel.getSent_aud());
        tvDocCount.setText(storageModel.getSent_doc());
        tvPhotoSize.setText(humanReadableByteCountSI(Long.parseLong(storageModel.getSent_photos_size())));
        tvVideoSize.setText(humanReadableByteCountSI(Long.parseLong(storageModel.getSent_videos_size())));
        tvAudSize.setText(humanReadableByteCountSI(Long.parseLong(storageModel.getSent_aud_size())));
        tvDocSize.setText(humanReadableByteCountSI(Long.parseLong(storageModel.getSent_doc_size())));
    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent(StorageDetailUser.this,DetailStorageActivity.class);
        startActivity(intent);
        finish();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.img_back:
                onBackPressed();
                break;
        }
    }

    @SuppressLint("DefaultLocale")
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
}
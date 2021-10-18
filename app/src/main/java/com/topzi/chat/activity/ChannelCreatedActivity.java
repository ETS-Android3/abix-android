package com.topzi.chat.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.topzi.chat.R;
import com.topzi.chat.helper.DatabaseHandler;
import com.topzi.chat.model.ChannelResult;
import com.topzi.chat.utils.Constants;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;

import de.hdodenhof.circleimageview.CircleImageView;

public class ChannelCreatedActivity extends BaseActivity implements View.OnClickListener {

    private String TAG = this.getClass().getSimpleName();
    ImageView backbtn, searchbtn, optionbtn, cancelbtn;
    CircleImageView channelImageView;
    TextView txtChannelName, txtChannelDes, btnInvite;
    ChannelResult.Result channelData = new ChannelResult().new Result();
    String channelId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_channel_created);
        dbhelper = DatabaseHandler.getInstance(this);

        if (getIntent().getStringExtra(Constants.TAG_CHANNEL_ID) != null) {
            channelId = getIntent().getStringExtra(Constants.TAG_CHANNEL_ID);
            channelData = dbhelper.getChannelInfo(channelId);
        }
        channelImageView = findViewById(R.id.channelImageView);
        txtChannelName = findViewById(R.id.txtChannelName);
        txtChannelDes = findViewById(R.id.txtChannelDes);
        btnInvite = findViewById(R.id.btnInvite);
        backbtn = findViewById(R.id.backbtn);

        btnInvite.setOnClickListener(this);
        backbtn.setOnClickListener(this);

        Glide.with(getApplicationContext()).load(Constants.CHANNEL_IMG_PATH + channelData.channelImage).thumbnail(0.5f)
                .apply(RequestOptions.circleCropTransform().placeholder(R.drawable.change_camera).error(R.drawable.change_camera).override(ApplicationClass.dpToPx(getApplicationContext(), 70)))
                .into(channelImageView);
        txtChannelName.setText("" + channelData.channelName);
        txtChannelDes.setText("" + channelData.channelDes);
    }

    @Override
    public void onNetworkChange(boolean isConnected) {

    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.backbtn:
                finish();
                break;
            case R.id.btnInvite:
                finish();
                Intent intent = new Intent(getApplicationContext(), NewChannelActivity.class);
                intent.putExtra(Constants.IS_EDIT, false);
                intent.putExtra(Constants.TAG_CHANNEL_ID, channelId);
                startActivity(intent);
                break;
        }
    }
}

package com.topzi.chat.activity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.skydoves.progressview.ProgressView;
import com.topzi.chat.R;
import com.topzi.chat.utils.Constants;

import java.text.CharacterIterator;
import java.text.StringCharacterIterator;

import butterknife.BindView;
import butterknife.ButterKnife;

import static com.topzi.chat.utils.Constants.setStatusBarGradiant;

public class NetworkUsage extends AppCompatActivity implements View.OnClickListener {

    @BindView(R.id.img_back)
    ImageView img_back;
    @BindView(R.id.tvSentCall)
    TextView tvSentCall;
    @BindView(R.id.tvReceiveCall)
    TextView tvReceiveCall;
    @BindView(R.id.tvUploadM)
    TextView tvUploadM;
    @BindView(R.id.tvDownM)
    TextView tvDownM;
    @BindView(R.id.tvMessUp)
    TextView tvMessUp;
    @BindView(R.id.tvMessDown)
    TextView tvMessDown;
    @BindView(R.id.tvSentMessageCount)
    TextView tvSentMessageCount;
    @BindView(R.id.tvRecvMessageCount)
    TextView tvRecvMessageCount;
    @BindView(R.id.tvTotalUsage)
    TextView tvTotalUsage;
    @BindView(R.id.tvSentUsage)
    TextView tvSentUsage;
    @BindView(R.id.tvRecvUsage)
    TextView tvRecvUsage;
    @BindView(R.id.tvSentStatus)
    TextView tvSentStatus;
    @BindView(R.id.tvRecvStatus)
    TextView tvRecvStatus;
    @BindView(R.id.tvSentStatusCount)
    TextView tvSentStatusCount;
    @BindView(R.id.tvRecvStatusCount)
    TextView tvRecvStatusCount;
    @BindView(R.id.tvCallCount)
    TextView tvCallCount;
    @BindView(R.id.tvCallCountRecv)
    TextView tvCallCountRecv;
    @BindView(R.id.progressView1)
    ProgressView progressView1;
    @BindView(R.id.progressView2)
    ProgressView progressView2;
    @BindView(R.id.progressView3)
    ProgressView progressView3;
    @BindView(R.id.progressView4)
    ProgressView progressView4;
    @BindView(R.id.progressView5)
    ProgressView progressView5;
    @BindView(R.id.progressView6)
    ProgressView progressView6;

    SharedPreferences prefData;
    SharedPreferences.Editor editorData;
    long callTotal = 0, mediaTotal = 0, messageTotal = 0, statusTotal = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStatusBarGradiant(NetworkUsage.this);
        setContentView(R.layout.activity_network_usage);

        ButterKnife.bind(NetworkUsage.this);
        prefData = NetworkUsage.this.getSharedPreferences(Constants.NETWORK_USAGE, MODE_PRIVATE);
        editorData = prefData.edit();

        setData();

        img_back.setOnClickListener(this);
    }

    private void setData() {

        tvSentCall.setText(humanReadableByteCountSI(prefData.getLong("callSent", 0)));
        tvReceiveCall.setText(humanReadableByteCountSI(prefData.getLong("callReceive", 0)));
        tvDownM.setText(humanReadableByteCountSI(prefData.getLong("mediaDownload", 0)));
        tvUploadM.setText(humanReadableByteCountSI(prefData.getLong("mediaUpload", 0)));
        tvMessDown.setText(humanReadableByteCountSI(prefData.getLong("MesReceive", 0)));
        tvMessUp.setText(humanReadableByteCountSI(prefData.getLong("MesSent", 0)));
        tvSentStatus.setText(humanReadableByteCountSI(prefData.getLong("statusUpload", 0)));
        tvRecvStatus.setText(humanReadableByteCountSI(prefData.getLong("statusDownload", 0)));
        tvSentMessageCount.setText(prefData.getLong("sentMesCount", 0) + " Sent");
        tvRecvMessageCount.setText(prefData.getLong("receiveMesCount", 0) + " Receive");
        tvSentStatusCount.setText(prefData.getLong("statusUpMesCount", 0) + " Sent");
        tvRecvStatusCount.setText(prefData.getLong("statusDownMesCount", 0) + " Receive");
        tvCallCountRecv.setText(prefData.getLong("callReceiveCount", 0) + " incoming");
        tvCallCount.setText(prefData.getLong("callSentCount", 0) + " outgoing");

        long totalUsage = prefData.getLong("callSent", 0) +
                prefData.getLong("callReceive", 0) +
                prefData.getLong("mediaDownload", 0) +
                prefData.getLong("mediaUpload", 0) +
                prefData.getLong("MesReceive", 0) +
                prefData.getLong("MesSent", 0) +
                prefData.getLong("statusUpload", 0) +
                prefData.getLong("statusDownload", 0);

        tvTotalUsage.setText(humanReadableByteCountSI(totalUsage));

        long totalSentUsage = prefData.getLong("callSent", 0) +
                prefData.getLong("mediaUpload", 0) +
                prefData.getLong("MesSent", 0) +
                prefData.getLong("statusUpload", 0);

        tvSentUsage.setText(humanReadableByteCountSI(totalSentUsage));

        long totalRecvUsage = prefData.getLong("callReceive", 0) +
                prefData.getLong("mediaDownload", 0) +
                prefData.getLong("MesReceive", 0) +
                prefData.getLong("statusDownload", 0);

        tvRecvUsage.setText(humanReadableByteCountSI(totalRecvUsage));

        if (prefData.getLong("callSent", 0) > 0 &&
                prefData.getLong("callReceive", 0) > 0) {
            callTotal = (((prefData.getLong("callSent", 0) +
                    prefData.getLong("callReceive", 0)) * 100) / totalUsage);
        }
        if (prefData.getLong("mediaDownload", 0) > 0 &&
                prefData.getLong("mediaUpload", 0) > 0) {
            mediaTotal = (((prefData.getLong("mediaDownload", 0) +
                    prefData.getLong("mediaUpload", 0)) * 100) / totalUsage);
        }

        if (prefData.getLong("MesReceive", 0) > 0 &&
                prefData.getLong("MesSent", 0) > 0) {
            messageTotal = (((prefData.getLong("MesReceive", 0) +
                    prefData.getLong("MesSent", 0)) * 100) / totalUsage);
        }

        if (prefData.getLong("statusDownload", 0) > 0 &&
                prefData.getLong("statusUpload", 0) > 0) {
            statusTotal = (((prefData.getLong("statusDownload", 0) +
                    prefData.getLong("statusUpload", 0)) * 100) / totalUsage);
        }

        Log.e("LLLL_TotalCall: ", callTotal + "       " + humanReadableByteCountSI(callTotal));

        progressView1.setAutoAnimate(true);
        progressView1.setMax(100f);
        progressView1.setProgress(callTotal);

        progressView2.setAutoAnimate(true);
        progressView2.setMax(100f);
        progressView2.setProgress(mediaTotal);

        progressView3.setAutoAnimate(true);
        progressView3.setMax(100f);
        progressView3.setProgress(0f);

        progressView4.setAutoAnimate(true);
        progressView4.setMax(100f);
        progressView4.setProgress(messageTotal);

        progressView5.setAutoAnimate(true);
        progressView5.setMax(100f);
        progressView5.setProgress(statusTotal);

        progressView6.setAutoAnimate(true);
        progressView6.setMax(100f);
        progressView6.setProgress(0f);

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.img_back:
                onBackPressed();
                break;
        }
    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent(NetworkUsage.this, DataStorage.class);
        startActivity(intent);
        finish();
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
}
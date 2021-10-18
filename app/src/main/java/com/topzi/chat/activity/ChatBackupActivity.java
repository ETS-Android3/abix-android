package com.topzi.chat.activity;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.Dialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.topzi.chat.Backup.RemoteBackup;
import com.topzi.chat.R;
import com.topzi.chat.helper.DatabaseHandler;
import com.google.android.gms.drive.DriveId;
import com.google.android.gms.drive.OpenFileActivityOptions;

import java.util.regex.Pattern;


public class ChatBackupActivity extends AppCompatActivity implements View.OnClickListener {

    LinearLayout llGDrive,llGAcc,llBackupTime;
    RelativeLayout llIncludeVid;
    TextView tvBackupGDrive,tvGAcc,tvBackupTime;
    Switch switchVid;
    Toolbar toolbar;
    ImageView btnBack;
    TextView txtTitle;

    SharedPreferences pref;
    SharedPreferences.Editor editor;
    DatabaseHandler dbhelper;
    Dialog dialog;
    private RemoteBackup remoteBackup;

    public static final int REQUEST_CODE_SIGN_IN = 0;
    public static final int REQUEST_CODE_OPENING = 1;
    public static final int REQUEST_CODE_CREATION = 2;

    private boolean isBackup = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_backup);

        pref = ChatBackupActivity.this.getSharedPreferences("SavedPref", MODE_PRIVATE);
        editor = pref.edit();
        dbhelper = DatabaseHandler.getInstance(this);
        remoteBackup = new RemoteBackup(ChatBackupActivity.this);

        llGDrive = findViewById(R.id.ll_g_drive);
        llGAcc = findViewById(R.id.ll_g_acc);
        llBackupTime = findViewById(R.id.ll_backup_time);
        llIncludeVid = findViewById(R.id.ll_include_vid);

        tvBackupGDrive = findViewById(R.id.tv_backup_g_drive);
        tvGAcc = findViewById(R.id.tv_g_acc);
        tvBackupTime = findViewById(R.id.tv_backup_time);
        switchVid = findViewById(R.id.switch_vid);

        toolbar = findViewById(R.id.actionbar);
        btnBack = toolbar.findViewById(R.id.backbtn);
        txtTitle = toolbar.findViewById(R.id.title);

        remoteBackup.connectToDrive(isBackup);

        if (!pref.getString("backupEmail","").equals("")){
            tvGAcc.setText(pref.getString("backupEmail",""));
        }
        tvBackupGDrive.setText(pref.getString("backupTime","Never"));

        llGAcc.setOnClickListener(this);
        llGDrive.setOnClickListener(this);
        llBackupTime.setOnClickListener(this);

        initToolBar();
    }

    private void initToolBar() {
        txtTitle.setVisibility(View.VISIBLE);
        btnBack.setVisibility(View.VISIBLE);
        txtTitle.setText(R.string.chat_backup);
        btnBack.setColorFilter(ContextCompat.getColor(getApplicationContext(), R.color.primarytext));
        btnBack.setOnClickListener(view -> onBackPressed());
    }

    private void getAccounts(){
        Pattern emailPattern = Patterns.EMAIL_ADDRESS; // API level 8+
        Account[] accounts = AccountManager.get(ChatBackupActivity.this).getAccounts();

        dialog = new Dialog(ChatBackupActivity.this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        dialog.setContentView(R.layout.dialoge_g_account);
        dialog.getWindow().setLayout(getResources().getDisplayMetrics().widthPixels * 90 / 100, ViewGroup.LayoutParams.WRAP_CONTENT);
        dialog.setCancelable(true);
        dialog.setCanceledOnTouchOutside(true);

        RadioGroup radioGrp = dialog.findViewById(R.id.radioGroup);
        TextView tv_cancel = dialog.findViewById(R.id.tv_cancel);

        tv_cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });

        for (int i = 0; i < accounts.length; i++) {
            Account account = accounts[i];
            if (emailPattern.matcher(account.name).matches()) {
                String possibleEmail = account.name;
                Log.e("LLLLL_Account_ID: ",possibleEmail);
                RadioButton radioButton = new RadioButton(this);
                radioButton.setPadding(30,30,7,30);
                radioButton.setText(possibleEmail);
                radioButton.setId(i);
                radioGrp.addView(radioButton);
                if (possibleEmail.equals(pref.getString("backupEmail",""))){
                    radioButton.setChecked(true);
                }
            }
        }

        //set listener to radio button group
        radioGrp.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                int checkedRadioButtonId = group.getCheckedRadioButtonId();
                RadioButton radioBtn = (RadioButton) dialog.findViewById(checkedRadioButtonId);
                tvGAcc.setText(radioBtn.getText());
                editor.putString("backupEmail", String.valueOf(radioBtn.getText()));
                editor.commit();
                dialog.dismiss();
            }
        });

        dialog.show();

    }

    private void getGBackupTime(){
        String[] backup_time = {"Never","Daily","Weekly","Monthly"};

        dialog = new Dialog(ChatBackupActivity.this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        dialog.setContentView(R.layout.dialoge_g_account);
        dialog.getWindow().setLayout(getResources().getDisplayMetrics().widthPixels * 90 / 100, ViewGroup.LayoutParams.WRAP_CONTENT);
        dialog.setCancelable(true);
        dialog.setCanceledOnTouchOutside(true);

        RadioGroup radioGrp = dialog.findViewById(R.id.radioGroup);
        TextView tv_cancel = dialog.findViewById(R.id.tv_cancel);
        TextView title = dialog.findViewById(R.id.title);

        title.setText("Back up to Google Drive");

        tv_cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });

        for (int i = 0; i < backup_time.length; i++) {
                String account = backup_time[i];
                String possibleEmail = account;
                RadioButton radioButton = new RadioButton(this);
                radioButton.setPadding(30,30,7,30);
                radioButton.setText(possibleEmail);
                radioButton.setId(i);
                radioGrp.addView(radioButton);
                if (possibleEmail.equals(pref.getString("backupTime","Never"))){
                    radioButton.setChecked(true);
                }
        }

        //set listener to radio button group
        radioGrp.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                int checkedRadioButtonId = group.getCheckedRadioButtonId();
                RadioButton radioBtn = (RadioButton) dialog.findViewById(checkedRadioButtonId);
                tvBackupGDrive.setText(radioBtn.getText());
                editor.putString("backupTime", String.valueOf(radioBtn.getText()));
                editor.commit();
                Toast.makeText(ChatBackupActivity.this, pref.getString("backupTime","Never"), Toast.LENGTH_SHORT).show();
                dialog.dismiss();
            }
        });

        dialog.show();

    }

    private void getGBackupOver(){
        String[] backup_time = {"Wi-Fi","Wi-Fi or Cellular"};

        dialog = new Dialog(ChatBackupActivity.this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        dialog.setContentView(R.layout.dialoge_g_account);
        dialog.getWindow().setLayout(getResources().getDisplayMetrics().widthPixels * 90 / 100, ViewGroup.LayoutParams.WRAP_CONTENT);
        dialog.setCancelable(true);
        dialog.setCanceledOnTouchOutside(true);

        RadioGroup radioGrp = dialog.findViewById(R.id.radioGroup);
        TextView tv_cancel = dialog.findViewById(R.id.tv_cancel);
        TextView title = dialog.findViewById(R.id.title);

        title.setText("Back up Over");

        tv_cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });

        for (int i = 0; i < backup_time.length; i++) {
                String account = backup_time[i];
                String possibleEmail = account;
                RadioButton radioButton = new RadioButton(this);
                radioButton.setPadding(30,30,7,30);
                radioButton.setText(possibleEmail);
                radioButton.setId(i);
                radioGrp.addView(radioButton);
                if (possibleEmail.equals(pref.getString("backupOver","Wi-Fi"))){
                    radioButton.setChecked(true);
                }
        }

        //set listener to radio button group
        radioGrp.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                int checkedRadioButtonId = group.getCheckedRadioButtonId();
                RadioButton radioBtn = (RadioButton) dialog.findViewById(checkedRadioButtonId);
                tvBackupGDrive.setText(radioBtn.getText());
                editor.putString("backupOver", String.valueOf(radioBtn.getText()));
                editor.commit();
                Toast.makeText(ChatBackupActivity.this, pref.getString("backupOver","Wi-Fi"), Toast.LENGTH_SHORT).show();
                dialog.dismiss();
            }
        });

        dialog.show();

    }



    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.ll_g_acc){
            getAccounts();
        } else if (v.getId() == R.id.ll_g_drive){
            getGBackupTime();
        } else if (v.getId() == R.id.ll_backup_time){
            getGBackupOver();
        }
    }


    @Override
    protected void onActivityResult(final int requestCode, final int resultCode, final Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {

            case REQUEST_CODE_SIGN_IN:
                // Called after user is signed in.
                if (resultCode == RESULT_OK) {
                    Log.i("LLLLLL_Drive", "Sign in request code");
                    remoteBackup.connectToDrive(isBackup);
                }
                break;

            case REQUEST_CODE_CREATION:
                // Called after a file is saved to Drive.
                if (resultCode == RESULT_OK) {
                    Log.i("LLLLLL_Drive", "Backup successfully saved.");
                    Toast.makeText(this, "Backup successufly loaded!", Toast.LENGTH_SHORT).show();
                }
                break;

            case REQUEST_CODE_OPENING:
                if (resultCode == RESULT_OK) {
                    DriveId driveId = data.getParcelableExtra(
                            OpenFileActivityOptions.EXTRA_RESPONSE_DRIVE_ID);
                    remoteBackup.mOpenItemTaskSource.setResult(driveId);
                } else {
                    remoteBackup.mOpenItemTaskSource.setException(new RuntimeException("Unable to open file"));
                }

        }
    }



}

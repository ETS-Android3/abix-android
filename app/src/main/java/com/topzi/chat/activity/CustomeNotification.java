package com.topzi.chat.activity;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.ColorDrawable;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.topzi.chat.R;
import com.topzi.chat.helper.DatabaseHandler;

import java.io.File;

import static com.topzi.chat.utils.Constants.setStatusBarGradiant;

public class CustomeNotification extends AppCompatActivity implements View.OnClickListener {

    private String userId = "";
    RelativeLayout rlCustNoti,rlNotificationTone,rlVibrateSet,rlPopUp,rlLightColor,rlPriority,rlCallTone,rlCallVibrate;
    TextView tvToneName,tvVibrate1,tvPopUp1,tvLight1,tvCallToneName,tvCallVibrate1;
    CheckBox chechkboxPrior,checkboxCustNoti;
    ImageView imgBack;
    private String chosenRingtone = "";
    private String chosenRingtoneName = "";
    private String callTone = "";
    private String callToneName = "";
    private String vibrationType = "Default";
    private String callVibrationType = "Default";
    private String popUpa = "No popup";
    private String lightColor = "White";
    private Dialog dialog;
    SharedPreferences pref;
    SharedPreferences.Editor editor;
    DatabaseHandler dbhelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStatusBarGradiant(CustomeNotification.this);
        setContentView(R.layout.activity_custome_notification);

        userId = getIntent().getStringExtra("user_id");
        pref = CustomeNotification.this.getSharedPreferences("SavedPref", MODE_PRIVATE);
        editor = pref.edit();
        dbhelper = DatabaseHandler.getInstance(this);

        rlCustNoti = findViewById(R.id.rl_cust_noti);
        rlNotificationTone = findViewById(R.id.rl_notificationTone);
        rlVibrateSet = findViewById(R.id.rl_vibrate_Set);
        rlPopUp = findViewById(R.id.rl_pop_up);
        rlLightColor = findViewById(R.id.rl_lightColor);
        rlPriority = findViewById(R.id.rl_priority);
        rlCallTone = findViewById(R.id.rl_call_tone);
        rlCallVibrate = findViewById(R.id.rl_call_vibrate);

        checkboxCustNoti = findViewById(R.id.checkbox_cust_noti);
        chechkboxPrior = findViewById(R.id.chechkbox_prior);
        tvToneName = findViewById(R.id.tv_tone_name);
        tvVibrate1 = findViewById(R.id.tv_vibrate1);
        tvLight1 = findViewById(R.id.tv_light1);
        tvPopUp1 = findViewById(R.id.tv_pop_up1);
        tvCallVibrate1 = findViewById(R.id.tv_call_vibrate1);
        tvCallToneName = findViewById(R.id.tv_call_tone_name);
        imgBack = findViewById(R.id.img_back);

        if (dbhelper.getNitiPrio(userId)) {
            chechkboxPrior.setChecked(true);
        }

        Log.e("LLLLL_Tag: ", String.valueOf(pref.getBoolean("isUseCustNotification",false)));

        chosenRingtone = dbhelper.getNotificationTone(userId);
        chosenRingtoneName = dbhelper.getNotificationToneName(userId);
        callTone = dbhelper.getCallTone(userId);
        callToneName = dbhelper.getCallToneName(userId);
        vibrationType = dbhelper.getVibratType(userId);
        callVibrationType = dbhelper.getCallVibratType(userId);
        popUpa = dbhelper.getPopup(userId);
        lightColor = dbhelper.getLightColor(userId);

        if (dbhelper.getCustONOFF(userId)) {
            checkboxCustNoti.setChecked(true);

            tvCallVibrate1.setText(dbhelper.getCallVibratType(userId));
            tvVibrate1.setText(dbhelper.getVibratType(userId));
            tvToneName.setText(dbhelper.getNotificationToneName(userId));
            tvCallToneName.setText(dbhelper.getCallToneName(userId));
            tvLight1.setText(dbhelper.getLightColor(userId));
            tvPopUp1.setText(dbhelper.getPopup(userId));

            rlNotificationTone.setOnClickListener(this);
            rlVibrateSet.setOnClickListener(this);
            rlPopUp.setOnClickListener(this);
            rlLightColor.setOnClickListener(this);
            rlPriority.setOnClickListener(this);
            rlCallTone.setOnClickListener(this);
            rlCallVibrate.setOnClickListener(this);

            rlNotificationTone.setEnabled(true);
            rlVibrateSet.setEnabled(true);
            rlLightColor.setEnabled(true);
            rlPriority.setEnabled(true);
            rlCallTone.setEnabled(true);
            rlCallVibrate.setEnabled(true);
        } else {
            checkboxCustNoti.setChecked(false);

            Uri notification = RingtoneManager.getActualDefaultRingtoneUri(this, RingtoneManager.TYPE_NOTIFICATION);
            Ringtone ringtone = RingtoneManager.getRingtone(this, notification);
            String title = ringtone.getTitle(this);

            tvToneName.setText(title);

            Uri notification1 = RingtoneManager.getActualDefaultRingtoneUri(this, RingtoneManager.TYPE_RINGTONE);
            Ringtone ringtone1 = RingtoneManager.getRingtone(this, notification1);
            String title1 = ringtone1.getTitle(this);

            tvCallToneName.setText(title1);

            tvCallVibrate1.setText("Default");
            tvVibrate1.setText("Default");
            tvLight1.setText("White");
            tvPopUp1.setText("No popup");

            rlNotificationTone.setOnClickListener(null);
            rlVibrateSet.setOnClickListener(null);
            rlPopUp.setOnClickListener(null);
            rlLightColor.setOnClickListener(null);
            rlPriority.setOnClickListener(null);
            rlCallTone.setOnClickListener(null);
            rlCallVibrate.setOnClickListener(null);

            rlNotificationTone.setEnabled(false);
            rlVibrateSet.setEnabled(false);
            rlPopUp.setEnabled(false);
            rlLightColor.setEnabled(false);
            rlPriority.setEnabled(false);
            rlCallTone.setEnabled(false);
            rlCallVibrate.setEnabled(false);
        }

        rlCustNoti.setOnClickListener(this);
        imgBack.setOnClickListener(this);

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.rl_cust_noti:
                if (dbhelper.getCustONOFF(userId)) {
                    Log.e("LLLLL_Tag1: ", String.valueOf(pref.getBoolean("isUseCustNotification",false)));
                    checkboxCustNoti.setChecked(false);

                    Uri notification = RingtoneManager.getActualDefaultRingtoneUri(this, RingtoneManager.TYPE_NOTIFICATION);
                    Ringtone ringtone = RingtoneManager.getRingtone(this, notification);
                    String title = ringtone.getTitle(this);

                    tvToneName.setText(title);

                    Uri notification1 = RingtoneManager.getActualDefaultRingtoneUri(this, RingtoneManager.TYPE_RINGTONE);
                    Ringtone ringtone1 = RingtoneManager.getRingtone(this, notification1);
                    String title1 = ringtone1.getTitle(this);

                    tvCallToneName.setText(title1);

                    tvCallVibrate1.setText("Default");
                    tvVibrate1.setText("Default");
                    tvLight1.setText("White");

                    rlNotificationTone.setOnClickListener(null);
                    rlVibrateSet.setOnClickListener(null);
                    rlPopUp.setOnClickListener(null);
                    rlLightColor.setOnClickListener(null);
                    rlPriority.setOnClickListener(null);
                    rlCallTone.setOnClickListener(null);
                    rlCallVibrate.setOnClickListener(null);

                    rlNotificationTone.setEnabled(false);
                    rlVibrateSet.setEnabled(false);
                    rlPopUp.setEnabled(false);
                    rlLightColor.setEnabled(false);
                    rlPriority.setEnabled(false);
                    rlCallTone.setEnabled(false);
                    rlCallVibrate.setEnabled(false);

                    String string1 = String.valueOf(checkboxCustNoti.isChecked());
                    Log.e("LLLLLL_dara: ",string1);
                    dbhelper.addCustNotiDetails(userId,
                            notification.toString(),
                            "Default",
                            "No popup",
                            "White",
                            "false",
                            notification1.toString(),
                            "Default",
                            string1,
                            title,
                            title1);
                } else {
                    Log.e("LLLLL_Tag1: ", String.valueOf(pref.getBoolean("isUseCustNotification",false)));
                    checkboxCustNoti.setChecked(true);

                    rlNotificationTone.setOnClickListener(this);
                    rlVibrateSet.setOnClickListener(this);
                    rlPopUp.setOnClickListener(this);
                    rlLightColor.setOnClickListener(this);
                    rlPriority.setOnClickListener(this);
                    rlCallTone.setOnClickListener(this);
                    rlCallVibrate.setOnClickListener(this);

                    rlNotificationTone.setEnabled(true);
                    rlVibrateSet.setEnabled(true);
                    rlPopUp.setEnabled(true);
                    rlLightColor.setEnabled(true);
                    rlPriority.setEnabled(true);
                    rlCallTone.setEnabled(true);
                    rlCallVibrate.setEnabled(true);

                    String string = String.valueOf(chechkboxPrior.isChecked());
                    String string1 = String.valueOf(checkboxCustNoti.isChecked());
                    dbhelper.addCustNotiDetails(userId,
                            this.chosenRingtone,
                            this.vibrationType,
                            this.popUpa,
                            lightColor,
                            string,
                            this.callTone,
                            this.callVibrationType,
                            string1,
                            chosenRingtoneName,
                            callToneName);
                }
                break;
            case R.id.rl_notificationTone:
                Intent intent = new Intent(RingtoneManager.ACTION_RINGTONE_PICKER);
                intent.putExtra(RingtoneManager.EXTRA_RINGTONE_TYPE, RingtoneManager.TYPE_NOTIFICATION);
                intent.putExtra(RingtoneManager.EXTRA_RINGTONE_TITLE, "Select Tone");
                intent.putExtra(RingtoneManager.EXTRA_RINGTONE_EXISTING_URI, (Uri) null);
                this.startActivityForResult(intent, 5);
                break;
            case R.id.rl_vibrate_Set:
                getVibrateType();
                break;
            case R.id.rl_pop_up:
                getPopUp();
                break;
            case R.id.rl_lightColor:
                getLightColor();
                break;
            case R.id.rl_priority:
                if (pref.getBoolean("isPriorieNotification",true)) {
                    chechkboxPrior.setChecked(false);
                    editor.putBoolean("isPriorieNotification",false);
                    editor.commit();
                } else {
                    chechkboxPrior.setChecked(true);
                    editor.putBoolean("isPriorieNotification",true);
                    editor.commit();
                }
                break;
            case R.id.rl_call_tone:
                Intent callIntent = new Intent(RingtoneManager.ACTION_RINGTONE_PICKER);
                callIntent.putExtra(RingtoneManager.EXTRA_RINGTONE_TYPE, RingtoneManager.TYPE_RINGTONE);
                callIntent.putExtra(RingtoneManager.EXTRA_RINGTONE_TITLE, "Select Tone");
                callIntent.putExtra(RingtoneManager.EXTRA_RINGTONE_EXISTING_URI, (Uri) null);
                this.startActivityForResult(callIntent, 6);
                break;
            case  R.id.rl_call_vibrate:
                getCallVibrateType();
                break;
            case R.id.img_back:
                onBackPressed();
                break;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onActivityResult(final int requestCode, final int resultCode, final Intent intent)
    {
        if (resultCode == Activity.RESULT_OK && requestCode == 5) {
            Uri uri = intent.getParcelableExtra(RingtoneManager.EXTRA_RINGTONE_PICKED_URI);
            Ringtone ringtone = RingtoneManager.getRingtone(CustomeNotification.this, uri);
            String title = ringtone.getTitle(CustomeNotification.this);
            if (uri != null) {
                chosenRingtone = String.valueOf(uri);
                chosenRingtoneName = title;
                Log.e("LLLL_Select_ringtone: ", String.valueOf(uri));
                tvToneName.setText(title);
            } else {
                Uri notification = RingtoneManager.getActualDefaultRingtoneUri(this, RingtoneManager.TYPE_NOTIFICATION);
                Ringtone ringtone1 = RingtoneManager.getRingtone(CustomeNotification.this, notification);
                String title1 = ringtone1.getTitle(CustomeNotification.this);
                chosenRingtone = String.valueOf(notification);
                chosenRingtoneName = title1;
                tvToneName.setText(title1);

            }
        } else if (resultCode == Activity.RESULT_OK && requestCode == 6) {
            Uri uri = intent.getParcelableExtra(RingtoneManager.EXTRA_RINGTONE_PICKED_URI);
            Ringtone ringtone = RingtoneManager.getRingtone(CustomeNotification.this, uri);
            String title = ringtone.getTitle(CustomeNotification.this);
            if (uri != null) {
                callTone = String.valueOf(uri);
                callToneName = title;
                Log.e("LLLL_Select_ringtone: ",title);
                tvCallToneName.setText(title);
            } else {
                Uri notification1 = RingtoneManager.getActualDefaultRingtoneUri(CustomeNotification.this, RingtoneManager.TYPE_RINGTONE);
                Ringtone ringtone1 = RingtoneManager.getRingtone(CustomeNotification.this, notification1);
                String title1 = ringtone1.getTitle(CustomeNotification.this);

                callTone = String.valueOf(notification1);
                callToneName = title1;
                Log.e("LLLL_Select_ringtone: ",title1);
                tvCallToneName.setText(title1);
            }
        }
    }

    private void getVibrateType(){
        String[] vibration_type = {"Off","Default","Short","Long"};

        dialog = new Dialog(CustomeNotification.this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        dialog.setContentView(R.layout.dialoge_g_account);
        dialog.getWindow().setLayout(getResources().getDisplayMetrics().widthPixels * 90 / 100, ViewGroup.LayoutParams.WRAP_CONTENT);
        dialog.setCancelable(true);
        dialog.setCanceledOnTouchOutside(true);
        dialog.show();

        RadioGroup radioGrp = dialog.findViewById(R.id.radioGroup);
        TextView tv_cancel = dialog.findViewById(R.id.tv_cancel);
        TextView title = dialog.findViewById(R.id.title);

        title.setText("Vibrate");

        tv_cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });

        for (int i = 0; i < vibration_type.length; i++) {
            String account = vibration_type[i];
            String possibleEmail = account;
            RadioButton radioButton = new RadioButton(this);
            radioButton.setPadding(30,30,7,30);
            radioButton.setText(possibleEmail);
            radioButton.setId(i);
            radioGrp.addView(radioButton);
            if (possibleEmail.equals(pref.getString("vibrateType","Default"))){
                radioButton.setChecked(true);
                vibrationType = "Default";
            }
        }

        //set listener to radio button group
        radioGrp.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                int checkedRadioButtonId = group.getCheckedRadioButtonId();
                RadioButton radioBtn = (RadioButton) dialog.findViewById(checkedRadioButtonId);

                vibrationType = String.valueOf(radioBtn.getText());
                editor.putString("vibrateType",String.valueOf(radioBtn.getText()));
                editor.commit();
                tvVibrate1.setText(pref.getString("vibrateType","Default"));
                dialog.dismiss();
            }
        });

    }

    private void getPopUp(){
        String[] vibration_type = {"No popup","Only when screen ON","Only when screen OFF","Always show popup"};

        dialog = new Dialog(CustomeNotification.this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        dialog.setContentView(R.layout.dialoge_g_account);
        dialog.getWindow().setLayout(getResources().getDisplayMetrics().widthPixels * 90 / 100, ViewGroup.LayoutParams.WRAP_CONTENT);
        dialog.setCancelable(true);
        dialog.setCanceledOnTouchOutside(true);
        dialog.show();

        RadioGroup radioGrp = dialog.findViewById(R.id.radioGroup);
        TextView tv_cancel = dialog.findViewById(R.id.tv_cancel);
        TextView title = dialog.findViewById(R.id.title);

        title.setText("Popup notification");

        tv_cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });

        for (int i = 0; i < vibration_type.length; i++) {
            String account = vibration_type[i];
            String possibleEmail = account;
            RadioButton radioButton = new RadioButton(this);
            radioButton.setPadding(30,30,7,30);
            radioButton.setText(possibleEmail);
            radioButton.setId(i);
            radioGrp.addView(radioButton);
            if (possibleEmail.equals(pref.getString("popupType","No popup"))){
                radioButton.setChecked(true);
                popUpa = "No popup";
            }
        }

        //set listener to radio button group
        radioGrp.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                int checkedRadioButtonId = group.getCheckedRadioButtonId();
                RadioButton radioBtn = (RadioButton) dialog.findViewById(checkedRadioButtonId);

                popUpa = String.valueOf(radioBtn.getText());
                tvPopUp1.setText(pref.getString("popupType","No popup"));
                dialog.dismiss();
            }
        });

    }

    private void getCallVibrateType(){
        String[] vibration_type = {"Off","Default","Short","Long"};

        dialog = new Dialog(CustomeNotification.this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        dialog.setContentView(R.layout.dialoge_g_account);
        dialog.getWindow().setLayout(getResources().getDisplayMetrics().widthPixels * 90 / 100, ViewGroup.LayoutParams.WRAP_CONTENT);
        dialog.setCancelable(true);
        dialog.setCanceledOnTouchOutside(true);
        dialog.show();

        RadioGroup radioGrp = dialog.findViewById(R.id.radioGroup);
        TextView tv_cancel = dialog.findViewById(R.id.tv_cancel);
        TextView title = dialog.findViewById(R.id.title);

        title.setText("Vibrate");

        tv_cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });

        for (int i = 0; i < vibration_type.length; i++) {
            String account = vibration_type[i];
            String possibleEmail = account;
            RadioButton radioButton = new RadioButton(this);
            radioButton.setPadding(30,30,7,30);
            radioButton.setText(possibleEmail);
            radioButton.setId(i);
            radioGrp.addView(radioButton);
            if (possibleEmail.equals(pref.getString("callVibrateType","Default"))){
                radioButton.setChecked(true);
                callVibrationType = "Default";
            }
        }

        //set listener to radio button group
        radioGrp.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                int checkedRadioButtonId = group.getCheckedRadioButtonId();
                RadioButton radioBtn = (RadioButton) dialog.findViewById(checkedRadioButtonId);

                callVibrationType = String.valueOf(radioBtn.getText());
                editor.putString("callVibrateType",String.valueOf(radioBtn.getText()));
                editor.commit();
                tvVibrate1.setText(pref.getString("callVibrateType","Default"));
                dialog.dismiss();
            }
        });

    }

    private void getLightColor(){
        String[] light_color = {"None","White","Red","Yellow","Green","Cyan","Blue","Purple"};

        dialog = new Dialog(CustomeNotification.this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        dialog.setContentView(R.layout.dialoge_g_account);
        dialog.getWindow().setLayout(getResources().getDisplayMetrics().widthPixels * 90 / 100, ViewGroup.LayoutParams.WRAP_CONTENT);
        dialog.setCancelable(true);
        dialog.setCanceledOnTouchOutside(true);
        dialog.show();

        RadioGroup radioGrp = dialog.findViewById(R.id.radioGroup);
        TextView tv_cancel = dialog.findViewById(R.id.tv_cancel);
        TextView title = dialog.findViewById(R.id.title);

        title.setText("Light");

        tv_cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });

        for (int i = 0; i < light_color.length; i++) {
            String account = light_color[i];
            String possibleEmail = account;
            RadioButton radioButton = new RadioButton(this);
            radioButton.setPadding(30,30,7,30);
            radioButton.setText(possibleEmail);
            radioButton.setId(i);
            radioGrp.addView(radioButton);
            if (possibleEmail.equals(pref.getString("lightColor","White"))){
                radioButton.setChecked(true);
                lightColor = "White";
            }
        }

        //set listener to radio button group
        radioGrp.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                int checkedRadioButtonId = group.getCheckedRadioButtonId();
                RadioButton radioBtn = (RadioButton) dialog.findViewById(checkedRadioButtonId);

                lightColor = String.valueOf(radioBtn.getText());
                editor.putString("lightColor",String.valueOf(radioBtn.getText()));
                editor.commit();
                tvLight1.setText(pref.getString("lightColor","White"));
                dialog.dismiss();
            }
        });
    }

    @Override
    public void onBackPressed() {
        String string = String.valueOf(chechkboxPrior.isChecked());
        String string1 = String.valueOf(checkboxCustNoti.isChecked());
        if (checkboxCustNoti.isChecked()){
            dbhelper.addCustNotiDetails(userId,
                    chosenRingtone,
                    vibrationType,
                    popUpa,
                    lightColor,
                    string,
                    callTone,
                    callVibrationType,
                    string1,
                    chosenRingtoneName,
                    callToneName);
        }
        super.onBackPressed();
    }
}

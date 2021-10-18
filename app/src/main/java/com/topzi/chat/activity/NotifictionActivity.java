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
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.Switch;
import android.widget.TextView;

import com.topzi.chat.R;
import com.topzi.chat.helper.DatabaseHandler;

import static com.topzi.chat.utils.Constants.setStatusBarGradiant;

public class NotifictionActivity extends BaseActivity implements View.OnClickListener {

    RelativeLayout rlCustNoti,rlNotificationTone,rlVibrateSet,rlPopUp,rlLightColor,rlPriority,rlCallTone,rlCallVibrate;
    RelativeLayout rlGrpNotificationTone,rlGrpVibrateSet,rlGrpLightColor,rlGrpPriority;
    TextView tvToneName,tvVibrate1,tvPopUp1,tvLight1,tvCallToneName,tvCallVibrate1,tvGrpToneName,tvGrpLight1;
    Switch switchPrior,switchGrptNoti,switchConversation;
    ImageView imgBack;
    private Dialog dialog;
    SharedPreferences pref;
    SharedPreferences.Editor editor;
    DatabaseHandler dbhelper;

    private String chosenRingtone = "";
    private String chosenRingtoneName = "";
    private String callTone = "";
    private String callToneName = "";
    private String grpCallTone = "";
    private String grpCallToneName = "";
    private String vibrationType = "Default";
    private String callVibrationType = "Default";
    private String grpVibrationType = "Default";
    private String popUpa = "No popup";
    private String lightColor = "White";
    private String grplightColor = "White";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStatusBarGradiant(NotifictionActivity.this);
        setContentView(R.layout.activity_notifiction);

        pref = NotifictionActivity.this.getSharedPreferences("SavedPref", MODE_PRIVATE);
        editor = pref.edit();
        dbhelper = DatabaseHandler.getInstance(this);

        rlCustNoti = findViewById(R.id.rl_cust_noti);
        rlNotificationTone = findViewById(R.id.rl_notificationTone);
        rlVibrateSet = findViewById(R.id.rl_vibrate_Set);
        rlPopUp = findViewById(R.id.rl_pop_up);
        rlLightColor = findViewById(R.id.rl_lightColor);
        rlPriority = findViewById(R.id.rl_priority);
        rlGrpNotificationTone = findViewById(R.id.rl_grp_notificationTone);
        rlGrpVibrateSet = findViewById(R.id.rl_grp_vibrate_Set);
        rlGrpLightColor = findViewById(R.id.rl_grp_lightColor);
        rlGrpPriority = findViewById(R.id.rl_grp_priority);
        rlCallTone = findViewById(R.id.rl_call_tone);
        rlCallVibrate = findViewById(R.id.rl_call_vibrate);

        switchConversation = findViewById(R.id.switch_conversation);
        switchGrptNoti = findViewById(R.id.switch_grp_prior);
        switchPrior = findViewById(R.id.switch_prior);
        tvToneName = findViewById(R.id.tv_tone_name);
        tvVibrate1 = findViewById(R.id.tv_vibrate1);
        tvPopUp1 = findViewById(R.id.tv_pop_up1);
        tvLight1 = findViewById(R.id.tv_light1);
        tvCallVibrate1 = findViewById(R.id.tv_call_vibrate1);
        tvCallToneName = findViewById(R.id.tv_call_tone_name);
        tvGrpToneName = findViewById(R.id.tv_grp_tone_name);
        tvGrpLight1 = findViewById(R.id.tv_grp_light1);
        imgBack = findViewById(R.id.img_back);


        if (pref.getBoolean("isAPPPriorieNotification",true))
            switchPrior.setChecked(true);

        if (pref.getBoolean("isAPPGrpPriorieNotification",true))
            switchGrptNoti.setChecked(true);

        rlNotificationTone.setOnClickListener(NotifictionActivity.this);
        rlVibrateSet.setOnClickListener(NotifictionActivity.this);
        rlPopUp.setOnClickListener(NotifictionActivity.this);
        rlLightColor.setOnClickListener(NotifictionActivity.this);
        rlPriority.setOnClickListener(NotifictionActivity.this);
        rlGrpNotificationTone.setOnClickListener(NotifictionActivity.this);
        rlGrpVibrateSet.setOnClickListener(NotifictionActivity.this);
        rlGrpLightColor.setOnClickListener(NotifictionActivity.this);
        rlGrpPriority.setOnClickListener(NotifictionActivity.this);
        rlCallTone.setOnClickListener(NotifictionActivity.this);
        rlCallVibrate.setOnClickListener(NotifictionActivity.this);

        if (pref.getBoolean("custOn",false)) {
            switchConversation.setChecked(true);

            chosenRingtone = pref.getString("appToneName",chosenRingtone);
            chosenRingtoneName = pref.getString("appToneName",chosenRingtoneName);
            callTone = pref.getString("appCallToneName",callTone);
            callToneName = pref.getString("appCallToneName",chosenRingtoneName);
            grpCallToneName = pref.getString("grpCallToneName",grpCallToneName);
            vibrationType = pref.getString("appVibrateType","Default");
            callVibrationType = pref.getString("appCallVibrateType","Default");
            grpVibrationType = pref.getString("appGrpVibrateType","Default");
            popUpa = pref.getString("popup","Default");
            lightColor = pref.getString("appLightColor","White");
            grplightColor = pref.getString("appGrpLightColor","White");

            tvCallVibrate1.setText(callVibrationType);
            tvVibrate1.setText(vibrationType);
            tvToneName.setText(chosenRingtoneName);
            tvCallToneName.setText(callToneName);
            tvGrpToneName.setText(grpCallToneName);
            tvLight1.setText(lightColor);
            tvPopUp1.setText(popUpa);
            tvGrpLight1.setText(grplightColor);

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
            switchConversation.setChecked(false);

            Uri notification = RingtoneManager.getActualDefaultRingtoneUri(this, RingtoneManager.TYPE_NOTIFICATION);
            Ringtone ringtone = RingtoneManager.getRingtone(this, notification);
            String title = ringtone.getTitle(this);

            tvToneName.setText(title);

            Uri notification1 = RingtoneManager.getActualDefaultRingtoneUri(this, RingtoneManager.TYPE_RINGTONE);
            Ringtone ringtone1 = RingtoneManager.getRingtone(this, notification1);
            String title1 = ringtone1.getTitle(this);

            tvCallToneName.setText(title1);

            chosenRingtone = pref.getString("appCallToneName", String.valueOf(notification));
            chosenRingtoneName = pref.getString("appCallToneName",title);
            callTone = pref.getString("appCallToneName", String.valueOf(notification1));
            callToneName = pref.getString("appCallToneName",title1);
            vibrationType = pref.getString("appVibrateType","Default");
            callVibrationType = pref.getString("appCallVibrateType","Default");
            grpVibrationType = pref.getString("appGrpVibrateType","Default");
            popUpa = pref.getString("popup","Default");
            lightColor = pref.getString("appLightColor","White");
            grplightColor = pref.getString("appGrpLightColor","White");

            tvCallVibrate1.setText("Default");
            tvVibrate1.setText("Default");
            tvLight1.setText("White");
            tvGrpLight1.setText("White");
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
    public void onNetworkChange(boolean isConnected) {

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.rl_cust_noti:
                if (pref.getBoolean("custOn",false)) {
                    Log.e("LLLLL_Tag1: ", String.valueOf(pref.getBoolean("isUseCustNotification",false)));
                    switchConversation.setChecked(false);

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

                    chosenRingtone = pref.getString("appCallToneName", String.valueOf(notification));
                    chosenRingtoneName = pref.getString("appCallToneName",title);
                    callTone = pref.getString("appCallToneName", String.valueOf(notification1));
                    callToneName = pref.getString("appCallToneName",title1);
                    vibrationType = pref.getString("appVibrateType","Default");
                    callVibrationType = pref.getString("appCallVibrateType","Default");
                    grpVibrationType = pref.getString("appGrpVibrateType","Default");
                    popUpa = pref.getString("popup","Default");
                    lightColor = pref.getString("appLightColor","White");
                    grplightColor = pref.getString("appGrpLightColor","White");

                    editor.putString("appVibrateType",vibrationType);
                    editor.apply();
                    editor.putString("appGrpVibrateType",grpVibrationType);
                    editor.apply();
                    editor.putString("appCallVibrateType",callVibrationType);
                    editor.apply();
                    editor.putString("appGrpLightColor",grplightColor);
                    editor.apply();
                    editor.putString("appToneName",chosenRingtoneName);
                    editor.apply();
                    editor.putString("appCallToneName",callToneName);
                    editor.apply();
                    editor.putString("grpCallToneName",grpCallToneName);
                    editor.apply();
                    editor.putString("appTone",chosenRingtone);
                    editor.apply();
                    editor.putString("appCallTone",callTone);
                    editor.apply();
                    editor.putString("grpCallTone",grpCallTone);
                    editor.apply();
                    editor.putBoolean("notiPriority",switchPrior.isChecked());
                    editor.apply();
                    editor.putBoolean("grpNotiPriority",switchGrptNoti.isChecked());
                    editor.apply();
                    editor.putBoolean("custOn",switchConversation.isChecked());
                    editor.apply();
                    editor.putString("popup",popUpa);
                    editor.apply();
                    editor.commit();

                } else {
                    Log.e("LLLLL_Tag1: ", String.valueOf(pref.getBoolean("isUseCustNotification",false)));
                    switchConversation.setChecked(true);

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

                    editor.putString("appVibrateType",vibrationType);
                    editor.apply();
                    editor.putString("appGrpVibrateType",grpVibrationType);
                    editor.apply();
                    editor.putString("appCallVibrateType",callVibrationType);
                    editor.apply();
                    editor.putString("appGrpLightColor",grplightColor);
                    editor.apply();
                    editor.putString("appToneName",chosenRingtoneName);
                    editor.apply();
                    editor.putString("appCallToneName",callToneName);
                    editor.apply();
                    editor.putString("grpCallToneName",grpCallToneName);
                    editor.apply();
                    editor.putString("appTone",chosenRingtone);
                    editor.apply();
                    editor.putString("appCallTone",callTone);
                    editor.apply();
                    editor.putString("grpCallTone",grpCallTone);
                    editor.apply();
                    editor.putBoolean("notiPriority",switchPrior.isChecked());
                    editor.apply();
                    editor.putBoolean("grpNotiPriority",switchGrptNoti.isChecked());
                    editor.apply();
                    editor.putBoolean("custOn",switchConversation.isChecked());
                    editor.apply();
                    editor.putString("popup",popUpa);
                    editor.apply();
                    editor.commit();
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
                if (pref.getBoolean("isAPPPriorieNotification",true)) {
                    switchPrior.setChecked(false);
                    editor.putBoolean("isAPPPriorieNotification",false);
                    editor.commit();
                } else {
                    switchPrior.setChecked(true);
                    editor.putBoolean("isAPPPriorieNotification",true);
                    editor.commit();
                }
                break;
            case R.id.rl_grp_notificationTone:
                Intent grpintent = new Intent(RingtoneManager.ACTION_RINGTONE_PICKER);
                grpintent.putExtra(RingtoneManager.EXTRA_RINGTONE_TYPE, RingtoneManager.TYPE_NOTIFICATION);
                grpintent.putExtra(RingtoneManager.EXTRA_RINGTONE_TITLE, "Select Tone");
                grpintent.putExtra(RingtoneManager.EXTRA_RINGTONE_EXISTING_URI, (Uri) null);
                this.startActivityForResult(grpintent, 7);
                break;
            case R.id.rl_grp_vibrate_Set:
                getGroupVibrateType();
                break;
            case R.id.rl_grp_lightColor:
                getGrpCallVibrateType();
                break;
            case R.id.rl_grp_priority:
                if (pref.getBoolean("isAPPGrpPriorieNotification",true)) {
                    switchGrptNoti.setChecked(false);
                    editor.putBoolean("isAPPGrpPriorieNotification",false);
                    editor.commit();
                } else {
                    switchGrptNoti.setChecked(true);
                    editor.putBoolean("isAPPGrpPriorieNotification",true);
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
    protected void onActivityResult(final int requestCode, final int resultCode, final Intent intent) {
        if (resultCode == Activity.RESULT_OK && requestCode == 5) {
            Uri uri = intent.getParcelableExtra(RingtoneManager.EXTRA_RINGTONE_PICKED_URI);
            Ringtone ringtone = RingtoneManager.getRingtone(NotifictionActivity.this, uri);
            String title = ringtone.getTitle(NotifictionActivity.this);
            if (uri != null) {
                chosenRingtone = String.valueOf(uri);
                chosenRingtoneName = title;
                Log.e("LLLL_Select_ringtone: ", String.valueOf(uri));
                tvToneName.setText(title);
            } else {
                Uri notification = RingtoneManager.getActualDefaultRingtoneUri(this, RingtoneManager.TYPE_NOTIFICATION);
                Ringtone ringtone1 = RingtoneManager.getRingtone(NotifictionActivity.this, notification);
                String title1 = ringtone1.getTitle(NotifictionActivity.this);
                chosenRingtone = String.valueOf(notification);
                chosenRingtoneName = title1;
                tvToneName.setText(title1);

            }
        } else if (resultCode == Activity.RESULT_OK && requestCode == 6) {
            Uri uri = intent.getParcelableExtra(RingtoneManager.EXTRA_RINGTONE_PICKED_URI);
            Ringtone ringtone = RingtoneManager.getRingtone(NotifictionActivity.this, uri);
            String title = ringtone.getTitle(NotifictionActivity.this);
            if (uri != null) {
                callTone = String.valueOf(uri);
                callToneName = title;
                Log.e("LLLL_Select_ringtone: ",title);
                tvCallToneName.setText(title);
            } else {
                Uri notification1 = RingtoneManager.getActualDefaultRingtoneUri(NotifictionActivity.this, RingtoneManager.TYPE_RINGTONE);
                Ringtone ringtone1 = RingtoneManager.getRingtone(NotifictionActivity.this, notification1);
                String title1 = ringtone1.getTitle(NotifictionActivity.this);

                callTone = String.valueOf(notification1);
                callToneName = title1;
                Log.e("LLLL_Select_ringtone: ",title1);
                tvCallToneName.setText(title1);
            }
        } else if (resultCode == Activity.RESULT_OK && requestCode == 7) {
            Uri uri = intent.getParcelableExtra(RingtoneManager.EXTRA_RINGTONE_PICKED_URI);
            Ringtone ringtone = RingtoneManager.getRingtone(NotifictionActivity.this, uri);
            String title = ringtone.getTitle(NotifictionActivity.this);
            if (uri != null) {
                grpCallTone = String.valueOf(uri);
                grpCallToneName = title;
                Log.e("LLLL_Select_ringtone: ",title);
                tvGrpToneName.setText(title);
            } else {
                Uri notification1 = RingtoneManager.getActualDefaultRingtoneUri(NotifictionActivity.this, RingtoneManager.TYPE_RINGTONE);
                Ringtone ringtone1 = RingtoneManager.getRingtone(NotifictionActivity.this, notification1);
                String title1 = ringtone1.getTitle(NotifictionActivity.this);

                grpCallTone = String.valueOf(notification1);
                grpCallToneName = title1;
                Log.e("LLLL_Select_ringtone: ",title1);
                tvGrpToneName.setText(title1);
            }
        }
    }

    private void getVibrateType(){
        String[] vibration_type = {"Off","Default","Short","Long"};

        dialog = new Dialog(NotifictionActivity.this);
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
            if (possibleEmail.equals(pref.getString("appVibrateType","Default"))){
                radioButton.setChecked(true);
            }
        }

        //set listener to radio button group
        radioGrp.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                int checkedRadioButtonId = group.getCheckedRadioButtonId();
                RadioButton radioBtn = (RadioButton) dialog.findViewById(checkedRadioButtonId);
                vibrationType = String.valueOf(radioBtn.getText());
                tvVibrate1.setText(String.valueOf(radioBtn.getText()));
                dialog.dismiss();
            }
        });

    }

    private void getPopUp(){
        String[] vibration_type = {"No popup","Only when screen ON","Only when screen OFF","Always show popup"};

        dialog = new Dialog(NotifictionActivity.this);
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
            if (possibleEmail.equals(pref.getString("popup","No popup"))){
                radioButton.setChecked(true);
            }
        }

        //set listener to radio button group
        radioGrp.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                int checkedRadioButtonId = group.getCheckedRadioButtonId();
                RadioButton radioBtn = (RadioButton) dialog.findViewById(checkedRadioButtonId);

                popUpa = String.valueOf(radioBtn.getText());

                tvVibrate1.setText(pref.getString("popup","No popup"));
                dialog.dismiss();
            }
        });

    }

    private void getGroupVibrateType(){
        String[] vibration_type = {"Off","Default","Short","Long"};

        dialog = new Dialog(NotifictionActivity.this);
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
            if (possibleEmail.equals(pref.getString("appGrpVibrateType","Default"))){
                radioButton.setChecked(true);
            }
        }

        //set listener to radio button group
        radioGrp.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                int checkedRadioButtonId = group.getCheckedRadioButtonId();
                RadioButton radioBtn = (RadioButton) dialog.findViewById(checkedRadioButtonId);
                grpVibrationType = String.valueOf(radioBtn.getText());
                tvVibrate1.setText(grpVibrationType);
                dialog.dismiss();
            }
        });

    }

    private void getCallVibrateType(){
        String[] vibration_type = {"Off","Default","Short","Long"};

        dialog = new Dialog(NotifictionActivity.this);
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
            if (possibleEmail.equals(pref.getString("appCallVibrateType","Default"))){
                radioButton.setChecked(true);
            }
        }

        //set listener to radio button group
        radioGrp.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                int checkedRadioButtonId = group.getCheckedRadioButtonId();
                RadioButton radioBtn = (RadioButton) dialog.findViewById(checkedRadioButtonId);

                callVibrationType = String.valueOf(radioBtn.getText());
                tvVibrate1.setText(callVibrationType);
                dialog.dismiss();
            }
        });

    }

    private void getGrpCallVibrateType(){
        String[] light_color = {"None","White","Red","Yellow","Green","Cyan","Blue","Purple"};

        dialog = new Dialog(NotifictionActivity.this);
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
            if (possibleEmail.equals(pref.getString("appGrpLightColor","White"))){
                radioButton.setChecked(true);
            }
        }

        //set listener to radio button group
        radioGrp.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                int checkedRadioButtonId = group.getCheckedRadioButtonId();
                RadioButton radioBtn = (RadioButton) dialog.findViewById(checkedRadioButtonId);
                grplightColor = String.valueOf(radioBtn.getText());

                tvLight1.setText(grplightColor);
                dialog.dismiss();
            }
        });

    }

    private void getLightColor(){
        String[] light_color = {"None","White","Red","Yellow","Green","Cyan","Blue","Purple"};

        dialog = new Dialog(NotifictionActivity.this);
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
            if (possibleEmail.equals(pref.getString("appLightColor","White"))){
                radioButton.setChecked(true);
            }
        }

        //set listener to radio button group
        radioGrp.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                int checkedRadioButtonId = group.getCheckedRadioButtonId();
                RadioButton radioBtn = (RadioButton) dialog.findViewById(checkedRadioButtonId);

                editor.putString("appLightColor",String.valueOf(radioBtn.getText()));
                editor.commit();
                tvLight1.setText(pref.getString("appLightColor","White"));
                dialog.dismiss();
            }
        });
    }

    @Override
    public void onBackPressed() {
        if (switchConversation.isChecked()){
            editor.putString("appVibrateType",vibrationType);
            editor.apply();
            editor.putString("appGrpVibrateType",grpVibrationType);
            editor.apply();
            editor.putString("appCallVibrateType",callVibrationType);
            editor.apply();
            editor.putString("appGrpLightColor",grplightColor);
            editor.apply();
            editor.putString("appToneName",chosenRingtoneName);
            editor.apply();
            editor.putString("appCallToneName",callToneName);
            editor.apply();
            editor.putString("grpCallToneName",grpCallToneName);
            editor.apply();
            editor.putString("appTone",chosenRingtone);
            editor.apply();
            editor.putString("appCallTone",callTone);
            editor.apply();
            editor.putString("grpCallTone",grpCallTone);
            editor.apply();
            editor.putBoolean("notiPriority",switchPrior.isChecked());
            editor.apply();
            editor.putBoolean("grpNotiPriority",switchGrptNoti.isChecked());
            editor.apply();
            editor.putBoolean("custOn",switchConversation.isChecked());
            editor.apply();
            editor.putString("popup",popUpa);
            editor.apply();
            editor.commit();
        }
        super.onBackPressed();
    }
    
}

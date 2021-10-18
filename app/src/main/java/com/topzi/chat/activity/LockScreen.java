package com.topzi.chat.activity;

import android.app.KeyguardManager;
import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.topzi.chat.utils.Constants;


public class LockScreen extends AppCompatActivity {

//    private Button lock, disable, enable;
//    public static final int RESULT_ENABLE = 11;
//    private DevicePolicyManager devicePolicyManager;
//    private ComponentName compName;
//
//    @Override
//    public void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_lock_screen);
//        devicePolicyManager = (DevicePolicyManager) getSystemService(DEVICE_POLICY_SERVICE);
//        compName = new ComponentName(this, MyAdmin.class);
//
//        lock = (Button) findViewById(R.id.lock);
//        enable = (Button) findViewById(R.id.enableBtn);
//        disable = (Button) findViewById(R.id.disableBtn);
//        lock.setOnClickListener(this);
//        enable.setOnClickListener(this);
//        disable.setOnClickListener(this);
//    }
//
//    @Override
//    protected void onResume() {
//        super.onResume();
//        boolean isActive = devicePolicyManager.isAdminActive(compName);
//        disable.setVisibility(isActive ? View.VISIBLE : View.GONE);
//        enable.setVisibility(isActive ? View.GONE : View.VISIBLE);
//    }
//
//    @Override
//    public void onClick(View view) {
//        if (view == lock) {
//            boolean active = devicePolicyManager.isAdminActive(compName);
//
//            if (active) {
//                devicePolicyManager.lockNow();
//            } else {
//                Toast.makeText(this, "You need to enable the Admin Device Features", Toast.LENGTH_SHORT).show();
//            }
//
//        } else if (view == enable) {
//            Intent intent = new Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN);
//            intent.putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN, compName);
//            intent.putExtra(DevicePolicyManager.EXTRA_ADD_EXPLANATION, "Additional text explaining why we need this permission");
//            startActivityForResult(intent, RESULT_ENABLE);
//
//        } else if (view == disable) {
//            devicePolicyManager.removeActiveAdmin(compName);
//            disable.setVisibility(View.GONE);
//            enable.setVisibility(View.VISIBLE);
//        }
//    }
//
//    @Override
//    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
//        switch(requestCode) {
//            case RESULT_ENABLE :
//                if (resultCode == Activity.RESULT_OK) {
//                    Toast.makeText(LockScreenSample.this, "You have enabled the Admin Device features", Toast.LENGTH_SHORT).show();
//                } else {
//                    Toast.makeText(LockScreenSample.this, "Problem to enable the Admin Device features", Toast.LENGTH_SHORT).show();
//                }
//                break;
//        }
//
//        super.onActivityResult(requestCode, resultCode, data);
//    }
//}

    private static int CODE_AUTHENTICATION_VERIFICATION = 241;
    String from = "0";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_lock_screen);
        Intent intent = getIntent();
        if (intent != null)
            from = intent.getStringExtra(Constants.from);

        KeyguardManager km = (KeyguardManager) getSystemService(KEYGUARD_SERVICE);
        if (km.isKeyguardSecure()) {

            Intent i = km.createConfirmDeviceCredentialIntent("Authentication required", "Phone Lock");
            startActivityForResult(i, CODE_AUTHENTICATION_VERIFICATION);
        }
//        else
//            Toast.makeText(this, "No any security setup done by user(pattern or password or pin or fingerprint", Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && requestCode == CODE_AUTHENTICATION_VERIFICATION) {
            BaseActivity.isUnlocked = true;
            if (from == null || from.equals("0")) {
                Intent intent = new Intent(LockScreen.this, SplashActivity.class);
                startActivity(intent);
            }
            finish();
        } else {
            finish();
        }
    }
}
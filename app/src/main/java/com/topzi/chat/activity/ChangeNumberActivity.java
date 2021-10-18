package com.topzi.chat.activity;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.topzi.chat.R;
//import com.facebook.accountkit.AccessToken;
//import com.facebook.accountkit.Account;
//import com.facebook.accountkit.AccountKit;
//import com.facebook.accountkit.AccountKitCallback;
//import com.facebook.accountkit.AccountKitError;
//import com.facebook.accountkit.PhoneNumber;
//import com.facebook.accountkit.ui.AccountKitActivity;
//import com.facebook.accountkit.ui.AccountKitConfiguration;
//import com.facebook.accountkit.ui.LoginType;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;
import com.topzi.chat.external.RandomString;
import com.topzi.chat.helper.DatabaseHandler;
import com.topzi.chat.model.ChangeNumberResult;
import com.topzi.chat.model.GroupData;
import com.topzi.chat.utils.ApiClient;
import com.topzi.chat.utils.ApiInterface;
import com.topzi.chat.utils.Constants;
import com.topzi.chat.utils.GetSet;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ChangeNumberActivity extends BaseActivity implements View.OnClickListener {

    private final String TAG = this.getClass().getSimpleName();
    private static final int APP_REQUEST_CODE = 9002;
    static ApiInterface apiInterface;
    ProgressDialog progressDialog;
    DatabaseHandler dbhelper;
    EditText edtCountryCode, edtPhoneNumber, edtOtp;
    LinearLayout btnNext;
    TextView txtTitle;
    ImageView btnBack;
    String newCountryCode, newPhoneNumber, otp;
    Button btnVerify;
    private String verificationCode;
    FirebaseAuth auth;
    PhoneAuthProvider.OnVerificationStateChangedCallbacks mCallback;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_number);

        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage(getResources().getString(R.string.pleasewait));
        progressDialog.setCancelable(false);

        apiInterface = ApiClient.getClient().create(ApiInterface.class);

        dbhelper = DatabaseHandler.getInstance(this);
        btnBack = findViewById(R.id.backbtn);
        txtTitle = findViewById(R.id.title);
        btnNext = findViewById(R.id.btnNext);
        edtOtp = findViewById(R.id.et_otp);
        btnVerify = findViewById(R.id.btn_sign_in);
        edtCountryCode = findViewById(R.id.edtCountryCode);
        edtPhoneNumber = findViewById(R.id.edtPhoneNumber);

        btnBack.setColorFilter(ContextCompat.getColor(getApplicationContext(), R.color.primarytext));
        txtTitle.setText(getString(R.string.change_number));

        btnBack.setOnClickListener(this);
        btnNext.setOnClickListener(this);
        btnVerify.setOnClickListener(this);
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
            case R.id.btnNext:
                if (TextUtils.isEmpty("" + edtCountryCode.getText())) {
                    makeToast(getString(R.string.enter_country_code));
                } else if (TextUtils.isEmpty("" + edtPhoneNumber.getText())) {
                    makeToast(getString(R.string.enter_phone_number));
                } else {
                    String countryCode = "" + edtCountryCode.getText();
                    if (countryCode.contains("+")) {
                        countryCode = countryCode.replaceAll("\\+", "");
                    }

                    checkAvailability(countryCode, "" + edtPhoneNumber.getText());
                }
                break;

            case R.id.btn_sign_in:
                otp = edtOtp.getText().toString();
                if (!otp.equals("")) {
                    PhoneAuthCredential credential = PhoneAuthProvider.getCredential(verificationCode, otp);
                    SigninWithPhone(credential);
                }
                break;
        }
    }

    public void checkAvailability(String countryCode, String phoneNumber) {
        Call<Map<String, String>> call = apiInterface.verifyNewNumber(phoneNumber);
        call.enqueue(new Callback<Map<String, String>>() {
            @Override
            public void onResponse(Call<Map<String, String>> call, Response<Map<String, String>> response) {
//                Log.i(TAG, "checkAvailability: " + response.body());
                Map<String, String> map = response.body();
                if (map.get(Constants.STATUS).equalsIgnoreCase(Constants.TRUE)) {
                    newCountryCode = countryCode;
                    newPhoneNumber = phoneNumber;
                    final Dialog dialog = new Dialog(ChangeNumberActivity.this);
                    dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                    dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                    dialog.setContentView(R.layout.default_popup);
                    dialog.getWindow().setLayout(getResources().getDisplayMetrics().widthPixels * 90 / 100, ViewGroup.LayoutParams.WRAP_CONTENT);
                    dialog.setCancelable(true);
                    dialog.setCanceledOnTouchOutside(true);

                    TextView title = dialog.findViewById(R.id.title);
                    TextView yes = dialog.findViewById(R.id.yes);
                    TextView no = dialog.findViewById(R.id.no);
                    yes.setText(getString(R.string.im_sure));
                    no.setText(getString(R.string.nope));
                    title.setText(R.string.verify_old_number);
                    no.setVisibility(View.VISIBLE);

                    yes.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            dialog.dismiss();
                            edtOtp.setVisibility(View.VISIBLE);
                            btnVerify.setVisibility(View.VISIBLE);
                            verifyNumber();
                            PhoneAuthProvider.getInstance().verifyPhoneNumber(
                                    "+" + countryCode.trim() + phoneNumber.trim(),                     // Phone number to verify
                                    60,                           // Timeout duration
                                    TimeUnit.SECONDS,                // Unit of timeout
                                    ChangeNumberActivity.this,        // Activity (for callback binding)
                                    mCallback);
                        }
                    });

                    no.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            dialog.dismiss();
                        }
                    });

                    dialog.show();
                } else {
                    makeToast(getString(R.string.account_already_exists_with_this_number));
                }
            }

            @Override
            public void onFailure(Call<Map<String, String>> call, Throwable t) {
//                Log.e(TAG, "checkAvailability: " + t.getMessage());
                call.cancel();
            }
        });
    }

    private void verifyNumber() {
        auth = FirebaseAuth.getInstance();
        mCallback = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
            @Override
            public void onVerificationCompleted(PhoneAuthCredential phoneAuthCredential) {
                Toast.makeText(ChangeNumberActivity.this, "verification completed", Toast.LENGTH_SHORT).show();
                SigninWithPhone(phoneAuthCredential);
            }

            @Override
            public void onVerificationFailed(FirebaseException e) {
                Toast.makeText(ChangeNumberActivity.this, "verification failed", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onCodeSent(String s, PhoneAuthProvider.ForceResendingToken forceResendingToken) {
                super.onCodeSent(s, forceResendingToken);
                verificationCode = s;
                Toast.makeText(ChangeNumberActivity.this, "Code sent", Toast.LENGTH_SHORT).show();
            }
        };
    }

    private void SigninWithPhone(PhoneAuthCredential credential) {
        auth.signInWithCredential(credential)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
//                            Intent intent = new Intent(SigninActivity.this, WelcomeActivity.class);
//                            intent.putExtra(Constants.phone, phoneNumber.substring(3));
//                            intent.putExtra(Constants.countryCode, phoneNumber.substring(0, 3));
//                            startActivity(intent);
//                            finish();
                            changeMyNumber(GetSet.getUserId(), newPhoneNumber, newCountryCode);
                        } else {
                            Toast.makeText(ChangeNumberActivity.this, "Incorrect OTP", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    private void changeMyNumber(String userId, String phoneNumber, String countryCode) {
        Call<ChangeNumberResult> call = apiInterface.changeMyNumber(userId, phoneNumber, countryCode);
        call.enqueue(new Callback<ChangeNumberResult>() {
            @Override
            public void onResponse(Call<ChangeNumberResult> call, Response<ChangeNumberResult> response) {
//                Log.i(TAG, "changeMyNumber: " + new Gson().toJson(response));
                ChangeNumberResult result = response.body();
                if (result != null && result.getSTATUS().equalsIgnoreCase(Constants.TRUE)) {
                    sendMessageToGroup(result);
                } else {
                    makeToast(getString(R.string.there_is_some_error));
                }
            }

            @Override
            public void onFailure(Call<ChangeNumberResult> call, Throwable t) {
//                Log.e(TAG, "changeMyNumber: " + t.getMessage());
                call.cancel();
            }
        });
    }

    private void sendMessageToGroup(ChangeNumberResult result) {
        if (progressDialog != null && !progressDialog.isShowing())
            progressDialog.show();
        List<GroupData> groupData = dbhelper.getGroups();

        for (GroupData groupDatum : groupData) {
            try {
                String unixStamp = String.valueOf(System.currentTimeMillis() / 1000L);
                RandomString randomString = new RandomString(10);
                String messageId = groupDatum.groupId + randomString.nextString();

                JSONObject message = new JSONObject();
                message.put(Constants.TAG_GROUP_ID, groupDatum.groupId);
                message.put(Constants.TAG_GROUP_NAME, groupDatum.groupName);
                message.put(Constants.TAG_CHAT_TYPE, Constants.TAG_GROUP);
                message.put(Constants.TAG_CHAT_TIME, unixStamp);
                message.put(Constants.TAG_MESSAGE_ID, messageId);
                message.put(Constants.TAG_MEMBER_ID, GetSet.getUserId());
                message.put(Constants.TAG_MEMBER_NAME, GetSet.getUserName());
                message.put(Constants.TAG_MEMBER_NO, GetSet.getphonenumber());
                message.put(Constants.TAG_MESSAGE_TYPE, "change_number");
                String tempMsg = getString(R.string.changed_therir_number);
                message.put(Constants.TAG_MESSAGE, tempMsg);
                message.put(Constants.TAG_ATTACHMENT, GetSet.getphonenumber());
                message.put(Constants.TAG_CONTACT_COUNTRY_CODE, result.getRESULT().getCountryCode());
                message.put(Constants.TAG_CONTACT_PHONE_NO, result.getRESULT().getPhone());
                message.put(Constants.TAG_CONTACT_NAME, result.getRESULT().getUsername());
                message.put(Constants.TAG_GROUP_ADMIN_ID, groupDatum.groupAdminId);
                socketConnection.startGroupChat(message);

            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        updateMyNumber(result);
        if (progressDialog != null && progressDialog.isShowing())
            progressDialog.dismiss();
    }

    private void updateMyNumber(ChangeNumberResult result) {
        GetSet.setphonenumber(result.getRESULT().getPhone());
        GetSet.setcountrycode(result.getRESULT().getCountryCode());
        finish();
    }

}

package com.topzi.chat.activity;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;

import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.quickblox.core.QBEntityCallback;
import com.quickblox.core.QBEntityCallbackImpl;
import com.quickblox.core.exception.QBResponseException;
import com.quickblox.users.model.QBUser;
import com.topzi.chat.R;
import com.topzi.chat.model.LoginModel;
import com.topzi.chat.model.SigninResponse;
import com.topzi.chat.utils.ApiClient;
import com.topzi.chat.utils.ApiInterface;
import com.topzi.chat.utils.Constants;
import com.topzi.chat.utils.GetSet;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.gson.Gson;
import com.topzi.chat.utils.QBResRequestExecutor;

import java.util.Objects;
import java.util.concurrent.TimeUnit;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SigninActivity extends BaseActivity {
    Button btnGenerateOTP, btnSignIn;
    EditText etPhoneNumber, etOTP;
    String phoneNumber, otp;
    FirebaseAuth auth;
    PhoneAuthProvider.OnVerificationStateChangedCallbacks mCallback;
    private String verificationCode;
    ApiInterface apiInterface;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signin);
        apiInterface = ApiClient.getClient().create(ApiInterface.class);
        findViews();
        StartFirebaseLogin();
        btnGenerateOTP.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                phoneNumber = etPhoneNumber.getText().toString();
                if (!phoneNumber.equals(""))
                    Signin(phoneNumber.substring(3),phoneNumber.substring(0, 3));
//                    PhoneAuthProvider.getInstance().verifyPhoneNumber(
//                            phoneNumber,                     // Phone number to verify
//                            60,                           // Timeout duration
//                            TimeUnit.SECONDS,                // Unit of timeout
//                            SigninActivity.this,        // Activity (for callback binding)
//                            mCallback);                      // OnVerificationStateChangedCallbacks
            }
        });
        btnSignIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                otp = etOTP.getText().toString();
                if (!otp.equals("")) {
                    PhoneAuthCredential credential = PhoneAuthProvider.getCredential(verificationCode, otp);
                    SigninWithPhone(credential);
                }
            }
        });

    }

    @Override
    public void onNetworkChange(boolean isConnected) {

    }

    private void SigninWithPhone(PhoneAuthCredential credential) {
        auth.signInWithCredential(credential)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
//                        Log.e("LLLLL_Task: ", Objects.requireNonNull(task.getException().getMessage()));
//                        Log.e("LLLLL_Task`1: ", task.getResult().getAdditionalUserInfo().getUsername());
                        if (task.isSuccessful()) {
//                            Intent intent = new Intent(SigninActivity.this, WelcomeActivity.class);
//                            intent.putExtra(Constants.phone, phoneNumber.substring(3));
//                            intent.putExtra(Constants.countryCode, phoneNumber.substring(0, 3));
//                            startActivity(intent);
//                            finish();
                            Signin(phoneNumber.substring(3), phoneNumber.substring(0, 3));
                        } else {
                            Toast.makeText(SigninActivity.this, "Incorrect OTP", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    private void findViews() {
        btnGenerateOTP = findViewById(R.id.btn_generate_otp);
        btnSignIn = findViewById(R.id.btn_sign_in);
        etPhoneNumber = findViewById(R.id.et_phone_number);
        etOTP = findViewById(R.id.et_otp);
    }

    private void StartFirebaseLogin() {
        auth = FirebaseAuth.getInstance();
        mCallback = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
            @Override
            public void onVerificationCompleted(PhoneAuthCredential phoneAuthCredential) {
                Toast.makeText(SigninActivity.this, "verification completed", Toast.LENGTH_SHORT).show();
                SigninWithPhone(phoneAuthCredential);
            }

            @Override
            public void onVerificationFailed(FirebaseException e) {
//                Intent i = new Intent(SigninActivity.this, ProfileInfo.class);
//                i.putExtra("from", "welcome");
//                startActivity(i);
//                finish();
                Log.e("LLLLLL_Error: ",e.getMessage());
                Toast.makeText(SigninActivity.this, "verification failed", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onCodeSent(String s, PhoneAuthProvider.ForceResendingToken forceResendingToken) {
                super.onCodeSent(s, forceResendingToken);
                verificationCode = s;
                Toast.makeText(SigninActivity.this, "Code sent", Toast.LENGTH_SHORT).show();
            }
        };
    }

    void Signin(String number, String code) {
        number = number.replaceAll("[^0-9]", "");
        if (number.startsWith("0")) {
            number = number.replaceFirst("^0+(?!$)", "");
        }
        LoginModel loginModel = new LoginModel();
        loginModel.setCountryCode(code);
        loginModel.setPhone(number);
        Gson gson = new Gson();
        String login = gson.toJson(loginModel);
        Call<SigninResponse> call3 = apiInterface.signin(login);
        call3.enqueue(new Callback<SigninResponse>() {
            @Override
            public void onResponse(Call<SigninResponse> call, Response<SigninResponse> response) {
                try {

                    dbhelper.clearDB(getApplicationContext());
                    SigninResponse userdata = response.body();

                    if (userdata != null && userdata.getSTATUS()) {

                        GetSet.setToken(userdata.getRESULT().getUserToken());
                        GetSet.setUserId(userdata.getRESULT().getId() + "");
                        GetSet.setUserName(userdata.getRESULT().getUserName());
                        GetSet.setImageUrl(userdata.getRESULT().getUserImage());
                        GetSet.setphonenumber(userdata.getRESULT().getPhoneNo());
                        GetSet.setcountrycode(userdata.getRESULT().getCountryCode());

                        Intent i = new Intent(SigninActivity.this, ProfileInfo.class);
                        i.putExtra("from", "welcome");
                        startActivity(i);
                        finish();

                    } else if (userdata != null && !userdata.getSTATUS()) {
                        Toast.makeText(getApplicationContext(), userdata.getMSG(), Toast.LENGTH_SHORT).show();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }

            }

            @Override
            public void onFailure(Call<SigninResponse> call, Throwable t) {
                Log.v("LOGIN Failed", "TEST " + t.getMessage());
                call.cancel();

            }
        });
    }

}

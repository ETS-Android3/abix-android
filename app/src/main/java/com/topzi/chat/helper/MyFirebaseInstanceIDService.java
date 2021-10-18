package com.topzi.chat.helper;

import android.util.Log;

import com.google.firebase.iid.FirebaseInstanceIdService;
import com.topzi.chat.model.SigninResponse;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.gson.Gson;
import com.topzi.chat.activity.ApplicationClass;
import com.topzi.chat.model.DeviceDataModel;
import com.topzi.chat.utils.ApiClient;
import com.topzi.chat.utils.ApiInterface;
import com.topzi.chat.utils.GetSet;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created on 12/03/18.
 */


public class MyFirebaseInstanceIDService extends FirebaseInstanceIdService {

    private static final String TAG = "MyFirebaseIIDService";
    ApiInterface apiInterface;

    @Override
    public void onTokenRefresh() {
        apiInterface = ApiClient.getClient().create(ApiInterface.class);
        String refreshedToken = FirebaseInstanceId.getInstance().getToken();
        Log.d(TAG, "Refreshed token: " + refreshedToken);
        storeToken(refreshedToken);
    }

    private void storeToken(String token) {
        //saving the token on shared preferences
        SharedPrefManager.getInstance(getApplicationContext()).saveDeviceToken(token);

        //get the logined user details from preference
        ApplicationClass.pref = getApplicationContext().getSharedPreferences("SavedPref", MODE_PRIVATE);
        ApplicationClass.editor = ApplicationClass.pref.edit();

        if (ApplicationClass.pref.getBoolean("isLogged", false)) {
            GetSet.setLogged(true);
            GetSet.setUserId(ApplicationClass.pref.getString("userId", null));
            addDeviceId();
        }
    }

    private void addDeviceId() {
        final String token = SharedPrefManager.getInstance(getApplicationContext()).getDeviceToken();
        final String deviceId = android.provider.Settings.Secure.getString(getApplicationContext().getContentResolver(),
                android.provider.Settings.Secure.ANDROID_ID);

        DeviceDataModel deviceDataModel = new DeviceDataModel();
        deviceDataModel.setDeviceId(deviceId);
        deviceDataModel.setDeviceToken(token);
        deviceDataModel.setType("Android");
        deviceDataModel.setUserId(GetSet.getUserId());

        Gson gson = new Gson();
        String data = gson.toJson(deviceDataModel);
        Call<SigninResponse> call3 = apiInterface.pushsignin(GetSet.getUserId(), "Android", deviceId, token);
        call3.enqueue(new Callback<SigninResponse>() {
            @Override
            public void onResponse(Call<SigninResponse> call, Response<SigninResponse> response) {
                if (response != null) {
                    SigninResponse data = response.body();
                    Log.v("addDeviceId:", "response- " + data);
                }

            }

            @Override
            public void onFailure(Call<SigninResponse> call, Throwable t) {
                call.cancel();

            }
        });

    }


}
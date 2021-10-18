package com.topzi.chat.fragment;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.androidnetworking.AndroidNetworking;
import com.androidnetworking.common.Priority;
import com.androidnetworking.error.ANError;
import com.androidnetworking.interfaces.JSONObjectRequestListener;
import com.topzi.chat.R;
import com.topzi.chat.activity.NotifictionActivity;

import org.json.JSONObject;

import static android.content.Context.MODE_PRIVATE;
import static com.topzi.chat.fragment.PinVerification1.pinNext;
import static com.topzi.chat.utils.Constants.API_VERSION;
import static com.topzi.chat.utils.Constants.BASE_URL;
import static com.topzi.chat.utils.Constants.TWO_STEP;

public class PinVerification2 extends Fragment {

    private EditText etPin;
    private TextView tvError;
    SharedPreferences pref;
    SharedPreferences.Editor editor;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_two_step_verification_2, container, false);

        AndroidNetworking.initialize(getContext());
        etPin = view.findViewById(R.id.etPin);
        tvError = view.findViewById(R.id.tvError);

        pref = getActivity().getSharedPreferences("SavedPref", MODE_PRIVATE);
        editor = pref.edit();

        etPin.setMaxEms(6);

        etPin.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (etPin.getText().toString().trim().length() == 6) {
                    if (pinNext.equals(etPin.getText().toString().trim())) {
                        tvError.setVisibility(View.GONE);
                        setPin();
                    } else {
                        tvError.setVisibility(View.VISIBLE);
                    }
                } else {
                    tvError.setVisibility(View.GONE);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
                if (pinNext.equals(etPin.getText().toString().trim())) {
                    tvError.setVisibility(View.GONE);
                } else {
                    tvError.setVisibility(View.VISIBLE);
                }
            }
        });

        return view;
    }

    private void setPin() {
        AndroidNetworking.post(BASE_URL + API_VERSION + TWO_STEP)
                .addBodyParameter("userId",pref.getString("userId",""))
                .addBodyParameter("pin",etPin.getText().toString().trim())
                .addBodyParameter("recoverEmail","")
                .setPriority(Priority.IMMEDIATE)
                .build()
                .getAsJSONObject(new JSONObjectRequestListener() {
                    @Override
                    public void onResponse(JSONObject response) {
                        Log.e("LLLLLL_Pin ",response.toString());
                        Toast.makeText(getContext(), "Pin set successfully...", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onError(ANError anError) {
                        Log.e("LLLLLL_Pin_EX: ",anError.getErrorBody());
                    }
                });
    }
}

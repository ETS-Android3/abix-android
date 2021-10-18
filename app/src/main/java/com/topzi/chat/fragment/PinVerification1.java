package com.topzi.chat.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.topzi.chat.R;
import com.topzi.chat.View.PlaceHolderEditText;
import com.topzi.chat.activity.PinVerification;
import com.topzi.chat.activity.TwoStepVerification;

public class PinVerification1 extends Fragment {

   private EditText etPin;
   private Button btnNext;
   public static String pinNext = "";
   StringBuilder str;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_two_step_verification_1, container, false);

        etPin = view.findViewById(R.id.etPin);
        btnNext = view.findViewById(R.id.btnNext);

//        etPin.setMaxEms(6);
        etPin.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

//                int inputlength = etPin.getText().toString().length();
//
//                if (count <= inputlength && inputlength == 3){
//
//                    etPin.setText(etPin.getText().toString() + "   ");
//
//                    int pos = etPin.getText().length();
//                    etPin.setSelection(pos);
//
//                } else if (count >= inputlength && (inputlength == 3)) {
//                    etPin.setText(etPin.getText().toString()
//                            .substring(0, etPin.getText()
//                                    .toString().length() - 3));
//
//                    int pos = etPin.getText().length();
//                    etPin.setSelection(pos);
//                }
//                count = etPin.getText().toString().length();
                if (etPin.getText().toString().length() == 6) {
                    pinNext = etPin.getText().toString().trim();
                    Intent intent = new Intent(getContext(), PinVerification.class);
                    intent.putExtra("from","main1");
                    startActivity(intent);
                    getActivity().finish();
                } else {
//                    for(int i = 0; i < etPin.getText().toString().length(); i ++){
//                        if (i==3)
//                            etPin.setText(etPin.getText().toString()+"   ");
//                        else
//                            etPin.setText(etPin.getText().toString()+" ");
//                    }
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        btnNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getContext(), PinVerification.class);
                intent.putExtra("from","main1");
                startActivity(intent);
                getActivity().finish();
            }
        });

        return view;
    }


}

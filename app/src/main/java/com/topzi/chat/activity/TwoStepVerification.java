package com.topzi.chat.activity;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;

import com.topzi.chat.R;

import static com.topzi.chat.utils.Constants.setStatusBarGradiant;

public class TwoStepVerification extends AppCompatActivity implements View.OnClickListener {

    private TextView tvDisable,tvChangePin,tvChangeEmail;
    private ImageView img_back;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_two_step_verification);

        tvDisable = findViewById(R.id.tvDisable);
        tvChangePin = findViewById(R.id.tvChangePin);
        tvChangeEmail = findViewById(R.id.tvChangeEmail);
        img_back = findViewById(R.id.img_back);

        img_back.setOnClickListener(this);
        tvChangePin.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
       switch (v.getId()) {
           case R.id.img_back:
               onBackPressed();
           case R.id.tvDisable:
               break;
           case R.id.tvChangePin:
               Intent intent = new Intent(TwoStepVerification.this,PinVerification.class);
               intent.putExtra("from","main");
               startActivity(intent);
               finish();
               break;
           case R.id.tvChangeEmail:
               break;
       }
    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent(TwoStepVerification.this,AccountActivity.class);
        startActivity(intent);
        finish();
    }
}
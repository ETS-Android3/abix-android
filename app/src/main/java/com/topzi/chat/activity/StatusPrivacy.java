package com.topzi.chat.activity;

import androidx.appcompat.app.AppCompatActivity;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import com.topzi.chat.R;

import butterknife.BindView;
import butterknife.ButterKnife;

public class StatusPrivacy extends AppCompatActivity {

    @BindView(R.id.radioGrp)
    RadioGroup radioGrp;
    @BindView(R.id.img_back)
    ImageView img_back;
    @BindView(R.id.btn_done)
    Button btn_done;

    private String[] privacy_type;

    SharedPreferences pref;
    SharedPreferences.Editor editor;
    private int checkedRadioButtonId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_status_privacy);

        ButterKnife.bind(StatusPrivacy.this);
        pref = StatusPrivacy.this.getSharedPreferences("SavedPref", MODE_PRIVATE);
        editor = pref.edit();
        privacy_type = new String[]{getString(R.string.privacy1),
                getString(R.string.privacy2),
                getString(R.string.privacy3)};
        img_back.setOnClickListener(v -> onBackPressed());

        for (int i = 0; i < privacy_type.length; i++) {
            String possibleEmail;
            possibleEmail = privacy_type[i];
            RadioButton radioButton = new RadioButton(this);
            radioButton.setPadding(30,30,7,30);
            radioButton.setText(possibleEmail);
            radioButton.setId(i);
            radioGrp.addView(radioButton);
            if (possibleEmail.equals(pref.getString("privacy_status",getString(R.string.privacy1)))){
                radioButton.setChecked(true);
            }
        }

        //set listener to radio button group
        radioGrp.setOnCheckedChangeListener((group, checkedId) -> {
            checkedRadioButtonId = group.getCheckedRadioButtonId();
            RadioButton radioBtn = findViewById(checkedRadioButtonId);

            editor.putString("privacy_status",String.valueOf(radioBtn.getText()));
            editor.apply();
        });

        btn_done.setOnClickListener(v -> {
            editor.commit();
            onBackPressed();
        });

    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }
}

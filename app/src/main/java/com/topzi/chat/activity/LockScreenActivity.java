package com.topzi.chat.activity;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import com.topzi.chat.R;
import com.andrognito.patternlockview.PatternLockView;
import com.andrognito.patternlockview.listener.PatternLockViewListener;
import com.andrognito.patternlockview.utils.PatternLockUtils;
import com.andrognito.patternlockview.utils.ResourceUtils;

import java.util.List;
import java.util.Objects;

import static com.topzi.chat.utils.Constants.setStatusBarGradiant;

public class LockScreenActivity extends AppCompatActivity {

    TextView tvTitle;
    PatternLockView patternLockView;

    SharedPreferences pref;
    SharedPreferences.Editor editor;
    String pattern1;
    boolean isChange=false;

    private PatternLockViewListener mPatternLockViewListener = new PatternLockViewListener() {
        @Override
        public void onStarted() {
            Log.d(getClass().getName(), "Pattern drawing started");
        }

        @Override
        public void onProgress(List<PatternLockView.Dot> progressPattern) {
            Log.d(getClass().getName(), "Pattern progress: " +
                    PatternLockUtils.patternToString(patternLockView, progressPattern));
        }

        @Override
        public void onComplete(List<PatternLockView.Dot> pattern) {
            if (isChange){
                if (tvTitle.getText().toString().trim().equals("Set Pattern")) {
                    pattern1 = PatternLockUtils.patternToString(patternLockView, pattern);
                    tvTitle.setText("Confiorm Pattern");
                    patternLockView.clearPattern();
                } else {
                    Log.e("LLLLL_Hide2211: ", String.valueOf(pattern));
                    if (PatternLockUtils.patternToString(patternLockView, pattern).equals(pattern1)) {
                        try {
                            editor.putString("patternLock", PatternLockUtils.patternToString(patternLockView, pattern));
                            editor.putBoolean("patternType", true);
                        } catch (Exception e) {
                            Log.e("LLLLLLL_Hide2EX11: ", Objects.requireNonNull(e.getMessage()));
                            e.printStackTrace();
                        }
                        editor.commit();
                        Intent intent = new Intent(LockScreenActivity.this, MainActivity.class);
                        startActivity(intent);
                        finish();
                        Toast.makeText(LockScreenActivity.this, "Pattern Set Successfully...", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(LockScreenActivity.this, "PAttern didn't match", Toast.LENGTH_SHORT).show();
                        patternLockView.setViewMode(PatternLockView.PatternViewMode.WRONG);
                        patternLockView.setWrongStateColor(ResourceUtils.getColor(LockScreenActivity.this, R.color.call_red));
                        patternLockView.clearPattern();
                    }
                }
            } else {
                if (pref.getString("patternLock", "").equals(PatternLockUtils.patternToString(patternLockView, pattern))) {
                    patternLockView.setViewMode(PatternLockView.PatternViewMode.CORRECT);
                    Intent intent = new Intent(LockScreenActivity.this, MainActivity.class);
                    startActivity(intent);
                    finish();
                } else {
                    if (!pref.getString("patternLock", "").equals("")) {
                        patternLockView.setViewMode(PatternLockView.PatternViewMode.WRONG);
                    } else {
                        if (tvTitle.getText().toString().trim().equals("Set Pattern")) {
                            pattern1 = PatternLockUtils.patternToString(patternLockView, pattern);
                            tvTitle.setText("Confiorm Pattern");
                            patternLockView.clearPattern();
                        } else {
                            Log.e("LLLLL_Hide2211: ", String.valueOf(pattern));
                            if (PatternLockUtils.patternToString(patternLockView, pattern).equals(pattern1)) {
                                try {
                                    editor.putString("patternLock", PatternLockUtils.patternToString(patternLockView, pattern));
                                    editor.putBoolean("patternType", true);
                                } catch (Exception e) {
                                    Log.e("LLLLLLL_Hide2EX11: ", Objects.requireNonNull(e.getMessage()));
                                    e.printStackTrace();
                                }
                                editor.commit();
                                Intent intent = new Intent(LockScreenActivity.this, MainActivity.class);
                                startActivity(intent);
                                finish();
                                Toast.makeText(LockScreenActivity.this, "Pattern Set Successfully...", Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(LockScreenActivity.this, "PAttern didn't match", Toast.LENGTH_SHORT).show();
                                patternLockView.setViewMode(PatternLockView.PatternViewMode.WRONG);
                                patternLockView.setWrongStateColor(ResourceUtils.getColor(LockScreenActivity.this, R.color.call_red));
                                patternLockView.clearPattern();
                            }

                        }
                    }
                }
            }
            Log.e("LLLLLLL_Right_P", "Pattern complete: " +
                    PatternLockUtils.patternToString(patternLockView, pattern));
        }

        @Override
        public void onCleared() {
            Log.d(getClass().getName(), "Pattern has been cleared");
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStatusBarGradiant(LockScreenActivity.this);
        setContentView(R.layout.activity_lock_screen);

        pref = getSharedPreferences("SavedPref", MODE_PRIVATE);
        editor = pref.edit();

        isChange = getIntent().getBooleanExtra("isChange",false);
        tvTitle = findViewById(R.id.tvTitle);
        patternLockView = findViewById(R.id.pattern_lock_view);

        patternLockView.setDotCount(3);
        patternLockView.setDotNormalSize((int) ResourceUtils.getDimensionInPx(this, R.dimen.pattern_lock_dot_size));
        patternLockView.setDotSelectedSize((int) ResourceUtils.getDimensionInPx(this, R.dimen.pattern_lock_dot_selected_size));
        patternLockView.setPathWidth((int) ResourceUtils.getDimensionInPx(this, R.dimen.pattern_lock_path_width));
        patternLockView.setAspectRatioEnabled(true);
        patternLockView.setAspectRatio(PatternLockView.AspectRatio.ASPECT_RATIO_HEIGHT_BIAS);
        patternLockView.setViewMode(PatternLockView.PatternViewMode.CORRECT);
        patternLockView.setDotAnimationDuration(150);
        patternLockView.setPathEndAnimationDuration(100);
        patternLockView.setCorrectStateColor(ResourceUtils.getColor(this, R.color.solid_15));
        patternLockView.setWrongStateColor(ResourceUtils.getColor(this,R.color.call_red));
        patternLockView.setNormalStateColor(ResourceUtils.getColor(this,R.color.white));
        patternLockView.setInStealthMode(false);
        patternLockView.setTactileFeedbackEnabled(true);
        patternLockView.setInputEnabled(true);
        patternLockView.addPatternLockListener(mPatternLockViewListener);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }
}

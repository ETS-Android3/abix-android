package com.topzi.chat.activity;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.databinding.DataBindingUtil;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.topzi.chat.R;
import com.topzi.chat.databinding.ActivityChatSettingsBinding;
import com.topzi.chat.external.AutoCloseBottomSheetBehavior;
import com.topzi.chat.helper.LocaleManager;
import com.topzi.chat.utils.Constants;

public class ChatSettingsActivity extends BaseActivity implements View.OnClickListener {


    private ActivityChatSettingsBinding mActivityBinding;

    TextView tvBackup,tvChatHistory;
    Toolbar toolbar;
    ImageView btnBack;
    TextView txtTitle;
    private AutoCloseBottomSheetBehavior<View> bottomDrawerBehavior;

    SharedPreferences pref;
    SharedPreferences.Editor editor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mActivityBinding = DataBindingUtil.setContentView(this, R.layout.activity_chat_settings);
        mActivityBinding.setView(this);

        pref = ChatSettingsActivity.this.getSharedPreferences(Constants.PREFERENCE_NAME, MODE_PRIVATE);
        editor = pref.edit();

        toolbar = findViewById(R.id.actionbar);
        btnBack = toolbar.findViewById(R.id.backbtn);
        txtTitle = toolbar.findViewById(R.id.title);
        tvBackup = findViewById(R.id.tv_backup);
        tvChatHistory = findViewById(R.id.tv_chat_history);

        tvChatHistory.setOnClickListener(this);
        tvBackup.setOnClickListener(this);

        setUpBottomDrawer();

        mActivityBinding.tvWallpaper.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                bottomDrawerBehavior.setState(BottomSheetBehavior.STATE_HALF_EXPANDED);
            }
        });

        mActivityBinding.switchEnterIsSend.setChecked(pref.getBoolean(Constants.PREF_ENTER_IS_SEND, false));

        mActivityBinding.switchEnterIsSend.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                editor.putBoolean(Constants.PREF_ENTER_IS_SEND, b).commit();
            }
        });

        mActivityBinding.switchMediaVisibility.setChecked(pref.getBoolean(Constants.PREF_MEDIA_VISIBILITY, true));

        mActivityBinding.switchMediaVisibility.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                editor.putBoolean(Constants.PREF_MEDIA_VISIBILITY, b).commit();
            }
        });

        mActivityBinding.layoutFontSize.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                selectFontSize();
            }
        });

        mActivityBinding.layoutAppLanguage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                selectAppLanguage();
            }
        });


        int fontSize = pref.getInt(Constants.PREF_FONT_SIZE, Constants.FONT_SIZE_MEDIUM);

        switch (fontSize) {
            case Constants.FONT_SIZE_SMALL:
                mActivityBinding.textFontSize.setText(getString(R.string.small));
                break;
            case Constants.FONT_SIZE_MEDIUM:
                mActivityBinding.textFontSize.setText(getString(R.string.medium));
                break;
            case Constants.FONT_SIZE_LARGE:
                mActivityBinding.textFontSize.setText(getString(R.string.large));
                break;
        }

        String appLanguage = pref.getString(Constants.TAG_LANGUAGE_CODE, Constants.LANGUAGE_ENGLISH);


        switch (appLanguage) {
            case Constants.LANGUAGE_ENGLISH:
                mActivityBinding.textAppLanguage.setText("English");
                break;
            case Constants.LANGUAGE_HINDI:
                mActivityBinding.textAppLanguage.setText("हिन्दी");
                break;
            case Constants.LANGUAGE_MALAYALAM:
                mActivityBinding.textAppLanguage.setText("മലയാളം");
                break;
            case Constants.LANGUAGE_TAMIL:
                mActivityBinding.textAppLanguage.setText("தமிழ்");
                break;
        }

        mActivityBinding.bottomDrawer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });

        mActivityBinding.layoutNoWallpaper.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });

        mActivityBinding.layoutGallery.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });
        mActivityBinding.layoutSolidColor.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });
        mActivityBinding.layoutRestoreDefault.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });
        initToolBar();
    }

    @Override
    public void onNetworkChange(boolean isConnected) {

    }

    private void initToolBar() {
        txtTitle.setVisibility(View.VISIBLE);
        btnBack.setVisibility(View.VISIBLE);
        txtTitle.setText(R.string.chatsettings);
        btnBack.setColorFilter(ContextCompat.getColor(getApplicationContext(), R.color.primarytext));
        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
    }

    @Override
    public void onClick(View v) {
        if (v.getId()==R.id.tv_chat_history){
            Intent chathistory = new Intent(ChatSettingsActivity.this,ChatHistoryActivity.class);
            startActivity(chathistory);
            finish();
        } else if (v.getId() == R.id.tv_backup) {
            Intent chatbackup = new Intent(ChatSettingsActivity.this, ChatBackupActivity.class);
            startActivity(chatbackup);
        }
    }

    private void selectFontSize() {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);
        alertDialog.setTitle(getString(R.string.font_size));
        String[] items = {getString(R.string.small),getString(R.string.medium),getString(R.string.large)};
        int checkedItem = pref.getInt(Constants.PREF_FONT_SIZE, Constants.FONT_SIZE_MEDIUM);
        alertDialog.setSingleChoiceItems(items, checkedItem, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which) {
                    case Constants.FONT_SIZE_SMALL:
                        mActivityBinding.textFontSize.setText(getString(R.string.small));
                        editor.putInt(Constants.PREF_FONT_SIZE, Constants.FONT_SIZE_SMALL).commit();
                        dialog.dismiss();
                        break;
                    case Constants.FONT_SIZE_MEDIUM:
                        mActivityBinding.textFontSize.setText(getString(R.string.medium));
                        editor.putInt(Constants.PREF_FONT_SIZE, Constants.FONT_SIZE_MEDIUM).commit();
                        dialog.dismiss();
                        break;
                    case Constants.FONT_SIZE_LARGE:
                        mActivityBinding.textFontSize.setText(getString(R.string.large));
                        editor.putInt(Constants.PREF_FONT_SIZE, Constants.FONT_SIZE_LARGE).commit();
                        dialog.dismiss();
                        break;
                }
            }
        });
        AlertDialog alert = alertDialog.create();
        alert.setCanceledOnTouchOutside(true);
        alert.show();
    }

    private void selectAppLanguage() {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);
        alertDialog.setTitle(getString(R.string.app_language));
        String[] items = {"English","हिन्दी","മലയാളം","தமிழ்"};


        String appLanguage = pref.getString(Constants.TAG_LANGUAGE_CODE, Constants.LANGUAGE_ENGLISH);

        int checkedItem = 0;
        switch (appLanguage) {
            case Constants.LANGUAGE_ENGLISH:
                checkedItem = 0;
                break;
            case Constants.LANGUAGE_HINDI:
                checkedItem = 1;
                break;
            case Constants.LANGUAGE_MALAYALAM:
                checkedItem = 2;
                break;
            case Constants.LANGUAGE_TAMIL:
                checkedItem = 3;
                break;
        }

        alertDialog.setSingleChoiceItems(items, checkedItem, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which) {
                    case 0:
                        mActivityBinding.textAppLanguage.setText("English");
                        setNewLocale(Constants.LANGUAGE_ENGLISH, false);
                        dialog.dismiss();
                        break;
                    case 1:
                        mActivityBinding.textAppLanguage.setText("हिन्दी");
                        setNewLocale(Constants.LANGUAGE_HINDI, false);
                        dialog.dismiss();
                        break;
                    case 2:
                        mActivityBinding.textAppLanguage.setText("മലയാളം");
                        setNewLocale(Constants.LANGUAGE_MALAYALAM, false);
                        dialog.dismiss();
                        break;
                    case 3:
                        mActivityBinding.textAppLanguage.setText("தமிழ்");
                        setNewLocale(Constants.LANGUAGE_TAMIL, false);
                        dialog.dismiss();
                        break;
                }
            }
        });
        AlertDialog alert = alertDialog.create();
        alert.setCanceledOnTouchOutside(true);
        alert.show();
    }

    private boolean setNewLocale(String languageCode, boolean restartProcess) {
        LocaleManager.setNewLocale(this, languageCode);
        editor.putString(Constants.TAG_LANGUAGE_CODE, languageCode).commit();

        LanguageActivity.languageChanged = true;

        recreate();

        if (restartProcess) {
            System.exit(0);
        } else {
//                Toast.makeText(mContext, "Activity restarted", Toast.LENGTH_SHORT).show();
        }
        return true;
    }

    protected void setUpBottomDrawer() {
        View bottomDrawer = mActivityBinding.coordinatorLayout.findViewById(R.id.bottom_drawer);
        bottomDrawerBehavior = (AutoCloseBottomSheetBehavior<View>) AutoCloseBottomSheetBehavior.from(bottomDrawer);
        bottomDrawerBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);

    }

    @Override
    public void onBackPressed() {
        if (bottomDrawerBehavior.getState() != BottomSheetBehavior.STATE_HIDDEN) {
            bottomDrawerBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);
        }
        else {
            Intent intent = new Intent(ChatSettingsActivity.this,MainActivity.class);
            startActivity(intent);
            finish();
        }
    }

}

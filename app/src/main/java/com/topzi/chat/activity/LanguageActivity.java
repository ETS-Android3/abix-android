package com.topzi.chat.activity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.topzi.chat.R;
import com.topzi.chat.helper.LocaleManager;
import com.topzi.chat.model.LanguageData;
import com.topzi.chat.utils.ApiClient;
import com.topzi.chat.utils.ApiInterface;
import com.topzi.chat.utils.Constants;

import java.util.ArrayList;
import java.util.List;

public class LanguageActivity extends BaseActivity {

    private final String TAG = this.getClass().getSimpleName();
    Toolbar toolbar;
    ImageView btnBack;
    TextView txtTitle;
    RecyclerView recyclerView;
    List<LanguageData> languageList = new ArrayList<>();
    LanguageAdapter adapter;
    SharedPreferences pref;
    SharedPreferences.Editor editor;
    static ApiInterface apiInterface;
    public static boolean languageChanged = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_language);

        apiInterface = ApiClient.getClient().create(ApiInterface.class);
        pref = LanguageActivity.this.getSharedPreferences("SavedPref", MODE_PRIVATE);
        editor = pref.edit();

        toolbar = findViewById(R.id.actionbar);
        btnBack = toolbar.findViewById(R.id.backbtn);
        txtTitle = toolbar.findViewById(R.id.title);
        recyclerView = findViewById(R.id.recyclerView);

        initToolBar();
        getLanguage();

    }

    @Override
    public void onNetworkChange(boolean isConnected) {

    }

    private void initToolBar() {
        txtTitle.setVisibility(View.VISIBLE);
        btnBack.setVisibility(View.VISIBLE);
        txtTitle.setText(R.string.app_language);
        btnBack.setColorFilter(ContextCompat.getColor(getApplicationContext(), R.color.primarytext));
        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
    }

    public void getLanguage() {
        LanguageData data = new LanguageData();
        data = new LanguageData();
        data.languageId = "1";
        data.language = getString(R.string.english);
        data.languageCode = Constants.LANGUAGE_ENGLISH;
        data.isSelected = data.languageCode.equalsIgnoreCase(pref.getString(Constants.TAG_LANGUAGE_CODE, Constants.TAG_DEFAULT_LANGUAGE_CODE));
        languageList.add(data);
        data = new LanguageData();
        data.languageId = "2";
        data.language = getString(R.string.french);
        data.languageCode = "fr";
        data.isSelected = data.languageCode.equalsIgnoreCase(pref.getString(Constants.TAG_LANGUAGE_CODE, Constants.TAG_DEFAULT_LANGUAGE_CODE));
        languageList.add(data);

        adapter = new LanguageAdapter(languageList, getApplicationContext());
        recyclerView.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
        recyclerView.setAdapter(adapter);
    }

    private class LanguageAdapter extends RecyclerView.Adapter<LanguageAdapter.LanguageViewHolder> {

        List<LanguageData> languageList = new ArrayList<>();
        Context context;

        LanguageAdapter(List<LanguageData> languageList, Context context) {
            this.languageList = languageList;
            this.context = context;
        }

        @NonNull
        @Override
        public LanguageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.language_item, parent, false);
            return new LanguageViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull final LanguageViewHolder holder, final int position) {
            final LanguageData data = languageList.get(position);

            holder.txtLanguage.setText(data.language);
            if (data.isSelected) {
                holder.btnLanguage.setChecked(true);
            } else {
                holder.btnLanguage.setChecked(false);
            }

            holder.mainLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (!holder.btnLanguage.isChecked())
                        setNewLocale(data, context, false);
                }
            });

            holder.btnLanguage.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    setNewLocale(data, context, false);
                }
            });
        }

        private boolean setNewLocale(LanguageData languageData, Context mContext, boolean restartProcess) {
            LocaleManager.setNewLocale(mContext, languageData.languageCode);

            for (LanguageData data : languageList) {
                data.isSelected = data.languageCode.equalsIgnoreCase(languageData.languageCode);
            }
            adapter.notifyDataSetChanged();
            editor.putString(Constants.TAG_LANGUAGE_CODE, languageData.languageCode).commit();

            languageChanged = true;
            finish();
            AccountActivity.activity.finish();
            startActivity(new Intent(getApplicationContext(), AccountActivity.class));

            if (restartProcess) {
                System.exit(0);
            } else {
//                Toast.makeText(mContext, "Activity restarted", Toast.LENGTH_SHORT).show();
            }
            return true;
        }

        @Override
        public int getItemCount() {
            return languageList.size();
        }

        public class LanguageViewHolder extends RecyclerView.ViewHolder {
            private RelativeLayout mainLayout;
            private TextView txtLanguage;
            private RadioButton btnLanguage;

            public LanguageViewHolder(View itemView) {
                super(itemView);
                mainLayout = itemView.findViewById(R.id.mainLayout);
                txtLanguage = itemView.findViewById(R.id.txtLanguage);
                btnLanguage = itemView.findViewById(R.id.btnLanguage);
            }
        }
    }

}

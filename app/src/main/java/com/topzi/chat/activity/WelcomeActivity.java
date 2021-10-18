package com.topzi.chat.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Parcelable;

import androidx.viewpager.widget.PagerAdapter;
import androidx.viewpager.widget.ViewPager;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.topzi.chat.R;
import com.topzi.chat.external.CirclePageIndicator;
import com.topzi.chat.helper.NetworkReceiver;
import com.topzi.chat.utils.Constants;
//import com.facebook.accountkit.ui.AccountKitActivity;
//import com.facebook.accountkit.ui.AccountKitConfiguration;
//import com.facebook.accountkit.ui.LoginType;

public class WelcomeActivity extends BaseActivity implements View.OnClickListener {
    private static final int APP_REQUEST_CODE = 9002;
    static ViewPager desPager;
    TextView agree;
    CirclePageIndicator pagerIndicator;
    String mobNum;
    String countryCode;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);
        agree = findViewById(R.id.agree);
        pagerIndicator = findViewById(R.id.pagerIndicator);
        desPager = findViewById(R.id.desPager);
        agree.setOnClickListener(this);
        String[] names = {getString(R.string.welcome_des2), getString(R.string.welcome_des3)};

        DesPagerAdapter desPagerAdapter = new DesPagerAdapter(WelcomeActivity.this, names);
        desPager.setAdapter(desPagerAdapter);
        pagerIndicator.setViewPager(desPager);
        Intent intent = getIntent();
        mobNum = intent.getStringExtra(Constants.phone);
        countryCode = intent.getStringExtra(Constants.countryCode);

    }

    @Override
    public void onNetworkChange(boolean isConnected) {

    }

//    @Override
//    public void onActivityResult(int requestCode, int resultCode, Intent data) {
//        super.onActivityResult(requestCode, resultCode, data);
//        if (requestCode == APP_REQUEST_CODE) {
//            AccessToken accessToken = AccountKit.getCurrentAccessToken();
//            if (accessToken != null) {
//                Handle Returning User
//                AccountKit.getCurrentAccount(new AccountKitCallback<Account>() {
//                    @Override
//                    public void onSuccess(Account account) {
//                        PhoneNumber phNumber = account.getPhoneNumber();
//                        if (phNumber != null) {
//
//                            String phoneNumber = phNumber.getPhoneNumber();
//                            String countryCode = phNumber.getCountryCode();
//
//                            Signin(phoneNumber, countryCode);
//                        }
//                    }
//
//                    @Override
//                    public void onError(AccountKitError accountKitError) {
//                    }
//                });
//            }
//        }
//    }

//    public void verifyMobileNo() {
//        final Intent intent = new Intent(WelcomeActivity.this, AccountKitActivity.class);
//        AccountKitConfiguration.AccountKitConfigurationBuilder configurationBuilder =
//                new AccountKitConfiguration.AccountKitConfigurationBuilder(LoginType.PHONE,
//                        AccountKitActivity.ResponseType.TOKEN);
//        configurationBuilder.setReadPhoneStateEnabled(true);
//        configurationBuilder.setReceiveSMS(true);
//        intent.putExtra(
//                AccountKitActivity.ACCOUNT_KIT_ACTIVITY_CONFIGURATION,
//                configurationBuilder.build());
//        startActivityForResult(intent, APP_REQUEST_CODE);
//
////        Signin("8122484752", "12345");
//    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.agree:
                if (NetworkReceiver.isConnected()) {
                    Intent i = new Intent(WelcomeActivity.this, SigninActivity.class);
                    startActivity(i);
                    finish();

                } else {
                    makeToast(getString(R.string.no_internet_connection));
                }
                break;
        }
    }

    class DesPagerAdapter extends PagerAdapter {

        Context context;
        LayoutInflater inflater;
        String[] names;

        public DesPagerAdapter(Context act, String[] names) {
            this.names = names;
            this.context = act;
            inflater = (LayoutInflater) context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }

        @Override
        public int getCount() {
            return names.length;
        }

        @Override
        public Object instantiateItem(ViewGroup collection, final int position) {
            ViewGroup itemView = (ViewGroup) inflater.inflate(R.layout.welcome_des_text,
                    collection, false);

            TextView name = itemView.findViewById(R.id.name);
            TextView title = itemView.findViewById(R.id.title);
            ImageView image = itemView.findViewById(R.id.image);
//            if (position == 0) {
//                image.setImageDrawable(getResources().getDrawable(R.drawable.introscreen_01));
//                title.setText(getString(R.string.welcome_title1));
//            } else
            if (position == 0) {
                image.setImageDrawable(getResources().getDrawable(R.drawable.introscreen_02));
                title.setText(getString(R.string.favourites));
            } else if (position == 1) {
                image.setImageDrawable(getResources().getDrawable(R.drawable.introscreen_03));
                title.setText(getString(R.string.welcome_title3));
            }
            name.setText(names[position]);

            collection.addView(itemView, 0);
            return itemView;
        }

        @Override
        public void destroyItem(ViewGroup collection, int position, Object view) {
            collection.removeView((ViewGroup) view);
        }

        @Override
        public boolean isViewFromObject(View view, Object object) {
            return view == object;
        }

        @Override
        public Parcelable saveState() {
            return null;
        }
    }
}

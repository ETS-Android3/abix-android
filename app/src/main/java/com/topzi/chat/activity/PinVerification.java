package com.topzi.chat.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import com.topzi.chat.R;
import com.topzi.chat.View.NonSwipeableViewPager;
import com.topzi.chat.fragment.PinVerification1;
import com.topzi.chat.fragment.PinVerification2;

import java.util.ArrayList;
import java.util.List;

import static com.topzi.chat.utils.Constants.setStatusBarGradiant;

public class PinVerification extends AppCompatActivity {

    private ImageView img_back;
    private NonSwipeableViewPager nonSwiableViewrPager;
    private String from = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStatusBarGradiant(PinVerification.this);
        setContentView(R.layout.activity_pin_verification);

        nonSwiableViewrPager = findViewById(R.id.nonSwiableViewrPager);
        img_back = findViewById(R.id.img_back);
        setupViewPager();

        img_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
    }

    private void setupViewPager() {
        from = getIntent().getStringExtra("from");
        ViewPagerAdapter adapter = new ViewPagerAdapter(getSupportFragmentManager());
        adapter.addFragment((new PinVerification1()));
        adapter.addFragment((new PinVerification2()));
        nonSwiableViewrPager.setAdapter(adapter);
        if (from.equals("main"))
            nonSwiableViewrPager.setCurrentItem(0);
        else if (from.equals("main1"))
            nonSwiableViewrPager.setCurrentItem(1);

    }

    class ViewPagerAdapter extends FragmentStatePagerAdapter {
        private final List<Fragment> mFragmentList = new ArrayList<>();

        ViewPagerAdapter(FragmentManager manager) {
            super(manager);
        }

        @Override
        @NonNull
        public Fragment getItem(int position) {
            return mFragmentList.get(position);
        }

        @Override
        public int getCount() {
            return mFragmentList.size();
        }

        void addFragment(Fragment fragment) {
            mFragmentList.add(fragment);
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return "";
        }

        @Override
        public int getItemPosition(@NonNull Object object) {
            return POSITION_NONE;
        }
    }

    @Override
    public void onBackPressed() {
        if (nonSwiableViewrPager.getCurrentItem()==1){
            nonSwiableViewrPager.setCurrentItem(0);
        } else {
            Intent intent = new Intent(PinVerification.this,TwoStepVerification.class);
            startActivity(intent);
            finish();
        }
    }
}
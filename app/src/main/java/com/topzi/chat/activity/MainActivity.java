package com.topzi.chat.activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.res.Resources;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.widget.AppCompatCheckBox;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.appcompat.widget.Toolbar;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.ViewPager;

import android.provider.Settings;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.droidninja.imageeditengine.ImageEditor;
import com.topzi.chat.R;
import com.topzi.chat.helper.SharedPrefManager;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.topzi.chat.helper.DatabaseHandler;
import com.topzi.chat.helper.SocketConnection;
import com.topzi.chat.utils.Constants;
import com.topzi.chat.utils.GetSet;
import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.tabs.TabLayout;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import de.hdodenhof.circleimageview.CircleImageView;

import static android.Manifest.permission.READ_CONTACTS;

public class MainActivity extends BaseActivity implements NavigationView.OnNavigationItemSelectedListener, View.OnClickListener, TabLayout.OnTabSelectedListener, SocketConnection.OnUpdateTabIndication {

    private static final String TAG = "MainActivity";
    public CircleImageView userImage;
    Toolbar toolbar;
    AppBarLayout appBarLayout;
    ViewPagerAdapter adapter;
    TabLayout tabLayout;
    ViewPager viewPager;
    ImageView navBtn, fab, searchBtn;
    DatabaseHandler dbhelper;
    DrawerLayout drawer;
    NavigationView navigationView;
    LinearLayout usrLayout;
    TextView userName;
    StatusFragment statusFragment;
    ChatFragment chatFragment;
    Timer onlineTimer = new Timer();

    private SharedPreferences pref;
    private SharedPreferences.Editor editor;
    private String pin = "";
    public static int ACTION_MANAGE_OVERLAY_PERMISSION_REQUEST_CODE = 5469;

    public static List<Intent> POWERMANAGER_INTENTS = Arrays.asList(
            new Intent().setComponent(new ComponentName("com.miui.securitycenter", "com.miui.permcenter.autostart.AutoStartManagementActivity")),
            new Intent().setComponent(new ComponentName("com.letv.android.letvsafe", "com.letv.android.letvsafe.AutobootManageActivity")),
            new Intent().setComponent(new ComponentName("com.huawei.systemmanager", "com.huawei.systemmanager.optimize.process.ProtectActivity")),
            new Intent().setComponent(new ComponentName("com.huawei.systemmanager", "com.huawei.systemmanager.appcontrol.activity.StartupAppControlActivity")),
            new Intent().setComponent(new ComponentName("com.coloros.safecenter", "com.coloros.safecenter.permission.startup.StartupAppListActivity")),
            new Intent().setComponent(new ComponentName("com.coloros.safecenter", "com.coloros.safecenter.startupapp.StartupAppListActivity")),
            new Intent().setComponent(new ComponentName("com.coloros.safecenter", "com.coloros.safecenter.sysfloatwindow.FloatWindowListActivity")),
            new Intent().setComponent(new ComponentName("com.coloros.safecenter", "com.coloros.safecenter.permission.floatwindow.FloatWindowListActivity")),
            new Intent().setComponent(new ComponentName("com.oppo.safe", "com.oppo.safe.permission.startup.StartupAppListActivity")),
            new Intent().setComponent(new ComponentName("com.oppo.safe", "com.oppo.safe.permission.floatwindow.FloatWindowListActivity")),
            new Intent().setComponent(new ComponentName("com.iqoo.secure", "com.iqoo.secure.ui.phoneoptimize.AddWhiteListActivity")),
            new Intent().setComponent(new ComponentName("com.iqoo.secure", "com.iqoo.secure.ui.phoneoptimize.BgStartUpManager")),
            new Intent().setComponent(new ComponentName("com.vivo.permissionmanager", "com.vivo.permissionmanager.activity.BgStartUpManagerActivity")),
            new Intent().setComponent(new ComponentName("com.htc.pitroad", "com.htc.pitroad.landingpage.activity.LandingPageActivity")),
            new Intent().setComponent(new ComponentName("com.asus.mobilemanager", "com.asus.mobilemanager.entry.FunctionActivity")).setData(android.net.Uri.parse("mobilemanager://function/entry/AutoStart"))
    );

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        /*if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            Window w = getWindow();
            //w.addFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
            w.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            w.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            w.addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
        }*/
        /*if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            Window w = getWindow(); // in Activity's onCreate() for instance
            w.setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS, WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
        }*/
        super.onCreate(savedInstanceState);
        //setStatusBarGradient(this);
        setContentView(R.layout.activity_main);
        Log.v(TAG, "onCreate");

        pref = MainActivity.this.getSharedPreferences("SavedPref", MODE_PRIVATE);
        editor = pref.edit();

        if(Build.VERSION.SDK_INT >= 23) {
            if (!Settings.canDrawOverlays(MainActivity.this)) {
                Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                        Uri.parse("package:" + getPackageName()));
                startActivityForResult(intent, 1234);
            }
        }

        toolbar = findViewById(R.id.toolbar);
        viewPager = findViewById(R.id.viewpager);
        navBtn = findViewById(R.id.navBtn);
        drawer = findViewById(R.id.drawer_layout);
        tabLayout = findViewById(R.id.tabs);
        navigationView = findViewById(R.id.nav_view);
        fab = findViewById(R.id.fab);
        searchBtn = findViewById(R.id.searchBtn);
        String dev = SharedPrefManager.getInstance(MainActivity.this).getDeviceToken();
        System.out.println("token " + dev);
        setupViewPager(viewPager);

        if (getIntent().getStringArrayExtra(ImageEditor.EXTRA_EDITED_PATH) != null) {
            viewPager.setCurrentItem(2);
        } else {
            if (viewPager != null && getIntent().getStringExtra(Constants.IS_FROM) != null) {
                if (getIntent().getStringExtra(Constants.IS_FROM).equalsIgnoreCase("group")) {
                    viewPager.setCurrentItem(1);
                } else if (getIntent().getStringExtra(Constants.IS_FROM).equalsIgnoreCase("channel")) {
                    viewPager.setCurrentItem(2);
                }
            }
        }

        viewPager.setOffscreenPageLimit(3);
        View header = navigationView.getHeaderView(0);
        dbhelper = DatabaseHandler.getInstance(this);
        SocketConnection.getInstance(this).setOnUpdateTabIndication(this);

        userImage = header.findViewById(R.id.userImage);
        usrLayout = header.findViewById(R.id.usrLayout);
        userName = header.findViewById(R.id.userName);

        tabLayout.addOnTabSelectedListener(this);
        navigationView.setNavigationItemSelectedListener(this);
        navBtn.setOnClickListener(this);
        searchBtn.setOnClickListener(this);
        usrLayout.setOnClickListener(this);

        updateTabIndication();

        final ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawer,
                null, R.string.navigation_drawer_open, R.string.navigation_drawer_close) {
            @Override
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
                Log.v("Drawer", "Drawer Opened");
            }

            @Override
            public void onDrawerClosed(View drawerView) {
                super.onDrawerClosed(drawerView);
                Log.v("Drawer", "Drawer Closed");
            }
        };
        drawer.addDrawerListener(toggle);
        drawer.post(new Runnable() {
            @Override
            public void run() {
                toggle.syncState();
            }
        });

        fab.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                if (pref.getString("hideChatPin", "").equals("")) {
                    setHideChatsPin();
                } else {
                    if (!Constants.isShow)
                        checkHideChatsPin();
                    else
                        chatFragment.removeHideData();
                }
                return false;
            }
        });

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (tabLayout != null && tabLayout.getSelectedTabPosition() == 0) {
                    if (ContextCompat.checkSelfPermission(MainActivity.this, READ_CONTACTS) != PackageManager.PERMISSION_GRANTED) {
                        ActivityCompat.requestPermissions(MainActivity.this, new String[]{READ_CONTACTS}, 101);
                    } else {
                        Intent s = new Intent(getApplicationContext(), SelectContact.class);
                        s.putExtra(Constants.TAG_USER_ID, GetSet.getUserId());
                        startActivity(s);
                    }
                }
                /*Intent intent = new Intent();
                String packageName = getPackageName();
                PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
                if (pm.isIgnoringBatteryOptimizations(packageName))
                    intent.setAction(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS);
                else {
                    intent.setAction(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS);
                    intent.setData(Uri.parse("package:" + packageName));
                }
                startActivity(intent);*/
            }
        });

        navigationView.post(new Runnable() {
            @Override
            public void run() {
                Resources r = getResources();
                DisplayMetrics metrics = new DisplayMetrics();
                getWindowManager().getDefaultDisplay().getMetrics(metrics);
                int width = metrics.widthPixels;

                float screenWidth = width / r.getDisplayMetrics().density;
                float navWidth = screenWidth - 56;

                navWidth = Math.min(navWidth, 320);

                int newWidth = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, navWidth, r.getDisplayMetrics());

                DrawerLayout.LayoutParams params = (DrawerLayout.LayoutParams) navigationView.getLayoutParams();
                params.width = newWidth;
                navigationView.setLayoutParams(params);
            }
        });


//        enableAutoStart();
        startPowerSaverIntent(this);
        startTimer();
//        AutoStartPermissionHelper.getInstance().getAutoStartPermission(MainActivity.this);
    }

    private void setHideChatsPin() {

        final Dialog dialog = new Dialog(MainActivity.this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        dialog.setContentView(R.layout.dialoge_set_pin);
        dialog.getWindow().setLayout(getResources().getDisplayMetrics().widthPixels * 90 / 100, ViewGroup.LayoutParams.WRAP_CONTENT);
        dialog.setCancelable(true);
        dialog.setCanceledOnTouchOutside(true);

        TextView title = dialog.findViewById(R.id.title);
        EditText etPin = dialog.findViewById(R.id.etPin);
        TextView yes = dialog.findViewById(R.id.yes);
        TextView no = dialog.findViewById(R.id.no);


        etPin.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (etPin.getText().length() == 4) {
                    if (yes.getText().equals("Confiorm")) {
                        if (!etPin.getText().toString().trim().equals(pin)) {
                            etPin.setError("Invalid Pin");
                        }
                    }
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        no.setVisibility(View.VISIBLE);

        yes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Log.e("LLLL_Confiorm: ", String.valueOf(etPin.getText().length()));
                if (etPin.getText().length() == 4) {
                    if (yes.getText().equals("Confiorm")) {
                        if (!etPin.getText().toString().trim().equals(pin)) {
                            etPin.setError("Invalid Pin");
                        } else {
                            editor.putString("hideChatPin", pin);
                            editor.commit();
                            dialog.dismiss();
                        }
                    } else {
                        pin = etPin.getText().toString().trim();
                        etPin.setText("");
                        title.setText("Confiorm pin for hide chat");
                        yes.setText("Confiorm");
                    }
                } else {
                    etPin.setError("Please Enter 4Digit Pin");
                }
            }
        });

        no.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });

        dialog.show();

    }

    private void checkHideChatsPin() {

        final Dialog dialog = new Dialog(MainActivity.this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        dialog.setContentView(R.layout.dialoge_set_pin);
        dialog.getWindow().setLayout(getResources().getDisplayMetrics().widthPixels * 90 / 100, ViewGroup.LayoutParams.WRAP_CONTENT);
        dialog.setCancelable(true);
        dialog.setCanceledOnTouchOutside(true);

        EditText etPin = dialog.findViewById(R.id.etPin);
        TextView title = dialog.findViewById(R.id.title);
        TextView yes = dialog.findViewById(R.id.yes);
        TextView no = dialog.findViewById(R.id.no);

        title.setText("Enter the pin");

        no.setVisibility(View.VISIBLE);

        yes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!etPin.getText().toString().trim().equals(pref.getString("hideChatPin", ""))) {
                    etPin.setError("Invalid Pin");
                } else {
                    dialog.dismiss();
                    chatFragment.addHideData();
//                    Intent intent = new Intent(MainActivity.this, HideChatActivity.class);
//                    startActivity(intent);
//                    finish();
                }
            }
        });

        no.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });

        dialog.show();

    }

    private void startTimer() {
        onlineTimer.scheduleAtFixedRate(new TimerTask() {
            public void run() {
                //Function call every second
//                if (!results.blockedme.equals("block") && !results.blockedbyme.equals("block")) {
                try {
                    JSONObject jsonObject = new JSONObject();
                    jsonObject.put(Constants.TAG_USER_ID, GetSet.getUserId());
                    jsonObject.put(Constants.TAG_CONTACT_ID, GetSet.getUserId());
                    Log.v("online", "online=" + jsonObject);
                    socketConnection.online(jsonObject);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
//                }
            }
        }, 0, 2000);
    }

    public static void startPowerSaverIntent(Context context) {
        SharedPreferences settings = context.getSharedPreferences("ProtectedApps", Context.MODE_PRIVATE);
        boolean skipMessage = settings.getBoolean("skipProtectedAppCheck", false);
        if (!skipMessage) {
            final SharedPreferences.Editor editor = settings.edit();
            boolean foundCorrectIntent = false;
            for (Intent intent : POWERMANAGER_INTENTS) {
                if (isCallable(context, intent)) {
                    foundCorrectIntent = true;
                    final AppCompatCheckBox dontShowAgain = new AppCompatCheckBox(context);
                    dontShowAgain.setText("Do not show again");
                    dontShowAgain.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                        @Override
                        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                            editor.putBoolean("skipProtectedAppCheck", isChecked);
                            editor.apply();
                        }
                    });

                    new AlertDialog.Builder(context)
                            .setTitle(Build.MANUFACTURER + " Protected Apps")
                            .setMessage(String.format("%s requires to be enabled in 'Protected Apps' to function properly.%n", context.getString(R.string.app_name)))
                            .setView(dontShowAgain)
                            .setPositiveButton("Go to settings", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    try {
                                        context.startActivity(intent);
                                    } catch (SecurityException se) {
                                        Log.e(TAG, "startPowerSaverIntent: " + se.getMessage());
                                    }
                                }
                            })
                            .setNegativeButton(android.R.string.cancel, null)
                            .show();
                    break;
                }
            }
            if (!foundCorrectIntent) {
                editor.putBoolean("skipProtectedAppCheck", true);
                editor.apply();
            }
        }
    }

    private static boolean isCallable(Context context, Intent intent) {
        List<ResolveInfo> list = context.getPackageManager().queryIntentActivities(intent,
                PackageManager.MATCH_DEFAULT_ONLY);
        return list.size() > 0;
    }

    private void enableAutoStart() {
        if (Build.BRAND.equalsIgnoreCase("xiaomi")) {
            new AlertDialog.Builder(MainActivity.this).setTitle("Enable AutoStart")
                    .setMessage(
                            "Please allow AppName to always run in the background,else our services can't be accessed.")
                    .setPositiveButton("ALLOW", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(@NonNull DialogInterface dialog, @NonNull int which) {

                            Intent intent = new Intent();
                            intent.setComponent(new ComponentName("com.miui.securitycenter",
                                    "com.miui.permcenter.autostart.AutoStartManagementActivity"));
                            startActivity(intent);
                        }
                    })
                    .show();
        } else if (Build.BRAND.equalsIgnoreCase("Letv")) {
            new AlertDialog.Builder(MainActivity.this).setTitle("Enable AutoStart")
                    .setMessage(
                            "Please allow AppName to always run in the background,else our services can't be accessed.")
                    .setPositiveButton("ALLOW", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(@NonNull DialogInterface dialog, @NonNull int which) {

                            Intent intent = new Intent();
                            intent.setComponent(new ComponentName("com.letv.android.letvsafe",
                                    "com.letv.android.letvsafe.AutobootManageActivity"));
                            startActivity(intent);
                        }
                    })
                    .show();
        } else if (Build.BRAND.equalsIgnoreCase("Honor")) {
            new AlertDialog.Builder(MainActivity.this).setTitle("Enable AutoStart")
                    .setMessage(
                            "Please allow AppName to always run in the background,else our services can't be accessed.")
                    .setPositiveButton("ALLOW", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(@NonNull DialogInterface dialog, @NonNull int which) {
                            Intent intent = new Intent();
                            intent.setComponent(new ComponentName("com.huawei.systemmanager",
                                    "com.huawei.systemmanager.optimize.process.ProtectActivity"));
                            startActivity(intent);
                        }
                    })
                    .show();
        } else if (Build.MANUFACTURER.equalsIgnoreCase("oppo")) {
            new AlertDialog.Builder(MainActivity.this).setTitle("Enable AutoStart")
                    .setMessage(
                            "Please allow AppName to always run in the background,else our services can't be accessed.")
                    .setPositiveButton("ALLOW", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(@NonNull DialogInterface dialog, @NonNull int which) {
                            try {
                                Intent intent = new Intent();
                                intent.setClassName("com.coloros.safecenter",
                                        "com.coloros.safecenter.permission.startup.StartupAppListActivity");
                                startActivity(intent);
                            } catch (Exception e) {
                                try {
                                    Intent intent = new Intent();
                                    intent.setClassName("com.oppo.safe",
                                            "com.oppo.safe.permission.startup.StartupAppListActivity");
                                    startActivity(intent);
                                } catch (Exception ex) {
                                    try {
                                        Intent intent = new Intent();
                                        intent.setClassName("com.coloros.safecenter",
                                                "com.coloros.safecenter.startupapp.StartupAppListActivity");
                                        startActivity(intent);
                                    } catch (Exception exx) {

                                    }
                                }
                            }
                        }
                    })
                    .show();
        } else if (Build.MANUFACTURER.contains("vivo")) {
            new AlertDialog.Builder(MainActivity.this).setTitle("Enable AutoStart")
                    .setMessage("Please allow AppName to always run in the background.Our app runs in background else our services can't be accesed.")
                    .setPositiveButton("ALLOW", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(@NonNull DialogInterface dialog, @NonNull int which) {
                            try {
                                Intent intent = new Intent();
                                intent.setComponent(new ComponentName("com.iqoo.secure",
                                        "com.iqoo.secure.ui.phoneoptimize.AddWhiteListActivity"));
                                startActivity(intent);
                            } catch (Exception e) {
                                try {
                                    Intent intent = new Intent();
                                    intent.setComponent(new ComponentName("com.vivo.permissionmanager",
                                            "com.vivo.permissionmanager.activity.BgStartUpManagerActivity"));
                                    startActivity(intent);
                                } catch (Exception ex) {
                                    try {
                                        Intent intent = new Intent();
                                        intent.setClassName("com.iqoo.secure",
                                                "com.iqoo.secure.ui.phoneoptimize.BgStartUpManager");
                                        startActivity(intent);
                                    } catch (Exception exx) {
                                        ex.printStackTrace();
                                    }
                                }
                            }
                        }
                    })
                    .show();
        }
    }

    private void updateTabIndication() {
        for (int i = 0; i < tabLayout.getTabCount(); i++) {
            TabLayout.Tab tab = tabLayout.getTabAt(i);
            View selected = tab.getCustomView();
            ImageView indication = selected.findViewById(R.id.indication);

            if (i == 0) {
                if (dbhelper.isRecentChatIndicationExist()) {
                    indication.setVisibility(View.VISIBLE);
                } else {
                    indication.setVisibility(View.GONE);
                }
            } else if (i == 1) {
                if (dbhelper.isRecentGroupIndicationExist()) {
                    indication.setVisibility(View.VISIBLE);
                } else {
                    indication.setVisibility(View.GONE);
                }
            } else if (i == 2) {
                if (dbhelper.isRecentChannelIndicationExist()) {
                    indication.setVisibility(View.VISIBLE);
                } else {
                    indication.setVisibility(View.GONE);
                }
            }
        }
    }

    @Override
    public void onNetworkChange(boolean isConnected) {

    }

    public static void setStatusBarGradient(Activity activity) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = activity.getWindow();
            Drawable background = activity.getResources().getDrawable(R.drawable.gradient);
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(activity.getResources().getColor(android.R.color.transparent));
            window.setNavigationBarColor(activity.getResources().getColor(android.R.color.transparent));
            window.setBackgroundDrawable(background);
        }
    }

    private void setupViewPager(ViewPager viewPager) {
        adapter = new ViewPagerAdapter(getSupportFragmentManager(), MainActivity.this);
        chatFragment = new ChatFragment();
        adapter.addFragment(chatFragment, getString(R.string.chat));
        adapter.addFragment(new GroupFragment(), getString(R.string.group));
        statusFragment = new StatusFragment();
        adapter.addFragment(statusFragment, getString(R.string.status));
        adapter.addFragment(new CallFragment(), getString(R.string.calls));
        viewPager.setAdapter(adapter);
        tabLayout.setupWithViewPager(viewPager);
        // Iterate over all tabs and set the custom view
        for (int i = 0; i < tabLayout.getTabCount(); i++) {
            TabLayout.Tab tab = tabLayout.getTabAt(i);
            tab.setCustomView(adapter.getTabView(i, this));
        }
        if (getIntent().getStringArrayExtra(ImageEditor.EXTRA_EDITED_PATH) != null) {
            adapter.setOnSelectView(this, tabLayout, 2);
        } else
            adapter.setOnSelectView(this, tabLayout, 0);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        Log.v("requestCode", "requestCode=" + requestCode);
        switch (requestCode) {
            case 101:
                int permContacts = ContextCompat.checkSelfPermission(MainActivity.this,
                        READ_CONTACTS);
                if (permContacts == PackageManager.PERMISSION_GRANTED) {
                    Intent s = new Intent(this, SelectContact.class);
                    s.putExtra(Constants.TAG_USER_ID, GetSet.getUserId());
                    startActivity(s);
                }
                break;
            case Constants.statusCameraImage:
                break;
            case Constants.statusGallery:
                break;
            case Constants.statusAudio:
                break;
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.navBtn:
                drawer.openDrawer(Gravity.LEFT);
                break;
            case R.id.searchBtn:
                startActivity(new Intent(this, SearchActivity.class));
                break;
            case R.id.usrLayout:
                Intent p = new Intent(this, ProfileActivity.class);
                p.putExtra(Constants.TAG_USER_ID, GetSet.getUserId());
                startActivity(p);
                drawer.closeDrawer(Gravity.LEFT);
                break;

        }
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        Log.v("onNavigation", "=" + item.getTitle());
        int id = item.getItemId();
        switch (id) {
//            case R.id.yourrides_menu:
//                Intent channel = new Intent(MainActivity.this, MyChannelsActivity.class);
//                startActivity(channel);
//                break;
            case R.id.wallet_menu:
                Intent account = new Intent(MainActivity.this, AccountActivity.class);
                startActivity(account);
                break;
            case R.id.chat_menu:
                Intent chatsettings = new Intent(MainActivity.this, ChatSettingsActivity.class);
                startActivity(chatsettings);
                finish();
                break;
            case R.id.chat_notificaton:
                Intent notificationin = new Intent(MainActivity.this, NotifictionActivity.class);
                startActivity(notificationin);
                break;
            case R.id.data_auto:
                Intent dataIntent = new Intent(MainActivity.this, DataStorage.class);
                startActivity(dataIntent);
                break;
//            case R.id.hide_app:
//                Intent intent = new Intent(MainActivity.this,SignatureVerification.class);
//                startActivity(intent);
////                PackageManager p = getPackageManager();
////                ComponentName componentName = new ComponentName(MainActivity.this, BuildConfig.APPLICATION_ID+".activity.SplashActivity");
////                p.setComponentEnabledSetting(componentName , PackageManager.COMPONENT_ENABLED_STATE_DISABLED, PackageManager.DONT_KILL_APP);
//                break;
            case R.id.invite_menu:
                Intent g = new Intent(Intent.ACTION_SEND);
                g.setType("text/plain");
                g.putExtra(Intent.EXTRA_TEXT, getString(R.string.invite_message) + "https://play.google.com/store/apps/details?id=" +
                        getApplicationContext().getPackageName());
                startActivity(Intent.createChooser(g, "Share"));
                break;
            case R.id.help_menu:
                Intent help = new Intent(MainActivity.this, HelpActivity.class);
                startActivity(help);
                break;
            case R.id.web_signin:
                Intent websign = new Intent(MainActivity.this, ScanQrCodeActivity.class);
                startActivity(websign);
                break;
        }
        //  switchActivityByNavigation(id, item);
        drawer.closeDrawer(Gravity.LEFT);
        return false;
    }

    @Override
    public void onTabSelected(TabLayout.Tab tab) {
        adapter.setOnSelectView(this, tabLayout, tab.getPosition());
        if (tab.getPosition() == 0) {
            fab.setImageDrawable(ContextCompat.getDrawable(getApplicationContext(), R.drawable.home_page_chat));
            fab.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (drawer != null) {
                        drawer.closeDrawer(Gravity.LEFT);
                    }
                    if (ContextCompat.checkSelfPermission(MainActivity.this, READ_CONTACTS) != PackageManager.PERMISSION_GRANTED) {
                        ActivityCompat.requestPermissions(MainActivity.this, new String[]{READ_CONTACTS}, 101);
                    } else {
                        Intent s = new Intent(getApplicationContext(), SelectContact.class);
                        s.putExtra(Constants.TAG_USER_ID, GetSet.getUserId());
                        startActivity(s);
                    }
                }
            });
        } else if (tab.getPosition() == 1) {
            fab.setImageDrawable(ContextCompat.getDrawable(getApplicationContext(), R.drawable.floating_group));
            fab.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (drawer != null) {
                        drawer.closeDrawer(Gravity.LEFT);
                    }
                    Intent s = new Intent(getApplicationContext(), NewGroupActivity.class);
                    s.putExtra(Constants.TAG_USER_ID, GetSet.getUserId());
                    startActivity(s);
                }
            });
        } else if (tab.getPosition() == 2) {
            fab.setImageDrawable(ContextCompat.getDrawable(getApplicationContext(), R.drawable.floating_channel));
            fab.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (drawer != null) {
                        drawer.closeDrawer(Gravity.LEFT);
                    }
//                    Intent s = new Intent(getApplicationContext(), CreateChannelActivity.class);
//                    s.putExtra(Constants.TAG_USER_ID, GetSet.getUserId());
//                    startActivity(s);
//                    Toast.makeText(MainActivity.this, "Coming Soon", Toast.LENGTH_SHORT).show();
                    statusFragment.showCreteStatus();
                }
            });
        } else if (tab.getPosition() == 3) {
            fab.setImageDrawable(ContextCompat.getDrawable(getApplicationContext(), R.drawable.floating_call));
            fab.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (drawer != null) {
                        drawer.closeDrawer(Gravity.LEFT);
                    }
                    Intent s = new Intent(getApplicationContext(), CallContactActivity.class);
                    startActivity(s);
                }
            });
        }
    }

    @Override
    public void onBackPressed() {
        if (statusFragment.showCreteStatus1())
            statusFragment.showCreteStatus();
        else
            super.onBackPressed();
    }

    @Override
    public void onTabUnselected(TabLayout.Tab tab) {
        adapter.setUnSelectView(this, tabLayout, tab.getPosition());
    }

    @Override
    public void onTabReselected(TabLayout.Tab tab) {

    }

    @Override
    public void updateIndication() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                updateTabIndication();
            }
        });
    }

    class ViewPagerAdapter extends FragmentPagerAdapter {
        private final List<Fragment> mFragmentList = new ArrayList<>();
        private final List<String> mFragmentTitleList = new ArrayList<>();
        Context context;

        public ViewPagerAdapter(FragmentManager manager, Context context) {
            super(manager);
            this.context = context;
        }

        public View getTabView(int position, Context context) {
            // Given you have a custom layout in `res/layout/custom_tab.xml` with a TextView and ImageView
            View v = LayoutInflater.from(context).inflate(R.layout.tab_layout, null);
            TextView tabName = (TextView) v.findViewById(R.id.tabName);
            tabName.setText(mFragmentTitleList.get(position));
            // ImageView indication = (ImageView) v.findViewById(R.id.indication);
            return v;
        }

        public void setOnSelectView(Context mContext, TabLayout tabLayout, int position) {
            TabLayout.Tab tab = tabLayout.getTabAt(position);
            View selected = tab.getCustomView();
            TextView tabName = selected.findViewById(R.id.tabName);
            tabName.setTextColor(mContext.getResources().getColor(R.color.primarytext));
        }

        public void setUnSelectView(Context mContext, TabLayout tabLayout, int position) {
            TabLayout.Tab tab = tabLayout.getTabAt(position);
            View selected = tab.getCustomView();
            TextView iv_text = selected.findViewById(R.id.tabName);
            iv_text.setTextColor(mContext.getResources().getColor(R.color.secondarytext));
        }

        @Override
        public Fragment getItem(int position) {
            return mFragmentList.get(position);
        }

        @Override
        public int getCount() {
            return mFragmentList.size();
        }

        public void addFragment(Fragment fragment, String title) {
            mFragmentList.add(fragment);
            mFragmentTitleList.add(title);
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return mFragmentTitleList.get(position);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.v(TAG, "onResume");

        if (LanguageActivity.languageChanged) {
            LanguageActivity.languageChanged = false;
            recreate();
        }
        Log.e(TAG, "onResume: " + GetSet.getUserName());
        SocketConnection.getInstance(this).setOnUpdateTabIndication(this);
        updateTabIndication();
        userName.setText(GetSet.getUserName());
        Glide.with(MainActivity.this).load(Constants.USER_IMG_PATH + GetSet.getImageUrl())
                .apply(new RequestOptions().placeholder(R.drawable.person).error(R.drawable.person))
                .into(userImage);

    }

    @Override
    public void onPause() {
        super.onPause();
        Log.v(TAG, "onPause");
        SocketConnection.getInstance(this).setOnUpdateTabIndication(null);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        SocketConnection.getInstance(this).setChatCallbackListener(null);
        if (onlineTimer != null) {
            onlineTimer.cancel();
        }
    }
}

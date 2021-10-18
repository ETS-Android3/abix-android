package com.topzi.chat.activity;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.net.ConnectivityManager;
import android.os.Build;
import android.os.Bundle;
import android.os.PowerManager;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.topzi.chat.helper.DatabaseHandler;
import com.topzi.chat.helper.ForegroundService;
import com.topzi.chat.helper.LocaleManager;
import com.topzi.chat.helper.NetworkReceiver;
import com.topzi.chat.helper.SocketConnection;
import com.topzi.chat.utils.Constants;
import com.topzi.chat.utils.GetSet;
import com.topzi.chat.utils.QBResRequestExecutor;
import com.topzi.chat.utils.SharedPrefsHelper;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Timer;
import java.util.TimerTask;

public abstract class BaseActivity extends AppCompatActivity implements NetworkReceiver.ConnectivityReceiverListener {

    private static final String TAG = "BaseActivity";
    NetworkReceiver networkReceiver;
    private boolean IS_NETWORK_CHANGED = false;
    DatabaseHandler dbhelper;
    SocketConnection socketConnection;
    PowerManager.WakeLock wl;
    public static boolean isUnlocked = false;
    Timer onlineTimer;
    protected QBResRequestExecutor requestExecutor;
    protected SharedPrefsHelper sharedPrefsHelper;

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(LocaleManager.setLocale(base));
        Log.d(TAG, "attachBaseContext");
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // register connection status listener
        ApplicationClass.getInstance().setConnectivityListener(this);

        dbhelper = DatabaseHandler.getInstance(this);
        socketConnection = SocketConnection.getInstance(this);
        requestExecutor = ApplicationClass.getInstance().getQbResRequestExecutor();
        sharedPrefsHelper = SharedPrefsHelper.getInstance();

//        Timer timer = new Timer();
//        TimerTask timerTask = new TimerTask() {
//            @Override
//            public void run() {
//                // your code here...
//                if (socketConnection != null)
//                    socketConnection.runTimerTask("ping");
//            }
//        };
//        timer.schedule(timerTask, 0L, 10000);

        networkReceiver = new NetworkReceiver();
        registerReceiver(networkReceiver, new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));

        PowerManager pm = (PowerManager) getApplicationContext().getSystemService(
                Context.POWER_SERVICE);
        this.wl = pm.newWakeLock(
                PowerManager.PARTIAL_WAKE_LOCK
                        | PowerManager.ON_AFTER_RELEASE,
                TAG);
        wl.acquire();
    }

    @Override
    public void onNetworkConnectionChanged(boolean isConnected) {
        Log.v("onNetwork", "base=" + isConnected);
        if (isConnected && !ForegroundService.IS_SERVICE_RUNNING && IS_NETWORK_CHANGED) {
            IS_NETWORK_CHANGED = false;
            Log.v("onNetwork", "service start");
            socketConnection = SocketConnection.getInstance(this);
            Intent service = new Intent(this, ForegroundService.class);
            service.setAction("start");
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                startForegroundService(service);
            } else {
                startService(service);
            }
        } else {
            IS_NETWORK_CHANGED = true;
        }

        onNetworkChange(isConnected);
    }

    public abstract void onNetworkChange(boolean isConnected);

    private void startTimer() {
        onlineTimer = new Timer();
        onlineTimer.scheduleAtFixedRate(new TimerTask() {
            public void run() {
                //Function call every second
//                if (!results.blockedme.equals("block") && !results.blockedbyme.equals("block")) {
                try {
                    JSONObject jsonObject = new JSONObject();
                    jsonObject.put(Constants.TAG_USER_ID, GetSet.getUserId());
                    Log.v("online", "online=" + jsonObject);
                    if (socketConnection != null)
                        socketConnection.online(jsonObject);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
//                }
            }
        }, 0, 5000);
    }

    @Override
    protected void onResume() {
        super.onResume();
//        if(!isUnlocked) {
//            Intent intent = new Intent(BaseActivity.this, LockScreen.class);
//            intent.putExtra(Constants.from, "1");
//            startActivity(intent);
//        }
        dbhelper = DatabaseHandler.getInstance(this);
        startTimer();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        wl.release();
        if (networkReceiver != null) {
            unregisterReceiver(networkReceiver);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (onlineTimer != null) {
            onlineTimer.cancel();
        }
    }

    public void makeToast(String message) {
        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();

    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }

    @Override
    protected void onStop() {
        super.onStop();
//        PackageManager packageManager = getPackageManager();
//        ComponentName componentName = new ComponentName(BaseActivity.this, SplashActivity.class);
//        packageManager.setComponentEnabledSetting(componentName,PackageManager.COMPONENT_ENABLED_STATE_DISABLED, PackageManager.DONT_KILL_APP);
    }
}

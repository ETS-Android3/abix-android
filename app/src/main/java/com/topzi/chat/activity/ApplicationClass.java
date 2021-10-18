package com.topzi.chat.activity;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.Application;
import android.app.PendingIntent;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.provider.ContactsContract;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleObserver;
import androidx.lifecycle.OnLifecycleEvent;
import androidx.lifecycle.ProcessLifecycleOwner;
import androidx.multidex.MultiDex;

import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.TextView;
import android.widget.Toast;

import com.quickblox.BuildConfig;
import com.quickblox.auth.session.QBSettings;
import com.topzi.chat.R;
import com.facebook.stetho.Stetho;
import com.topzi.chat.external.FontsOverride;
import com.topzi.chat.helper.AlarmReceiver;
import com.topzi.chat.helper.CallNotificationService;
import com.topzi.chat.helper.DatabaseHandler;
import com.topzi.chat.helper.ForegroundService;
import com.topzi.chat.helper.LocaleManager;
import com.topzi.chat.helper.NetworkReceiver;
import com.topzi.chat.helper.NetworkUtil;
import com.topzi.chat.helper.PhoneStateReceiver;
import com.topzi.chat.helper.SocketConnection;
import com.topzi.chat.utils.Constants;
import com.topzi.chat.utils.GetSet;
import com.google.android.material.snackbar.Snackbar;
import com.topzi.chat.utils.QBResRequestExecutor;
import com.vanniktech.emoji.EmojiManager;
import com.vanniktech.emoji.ios.IosEmojiProvider;

import org.acra.ACRA;
import org.acra.annotation.ReportsCrashes;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;

import static android.Manifest.permission.READ_CONTACTS;
import static com.topzi.chat.helper.NetworkUtil.NOT_CONNECT;

/**
 * Created on 24/1/18.
 */

@ReportsCrashes(mailTo = "abixdeveloper@gmail.com")
public class ApplicationClass extends Application implements LifecycleObserver {
    public static SharedPreferences pref;
    public static SharedPreferences.Editor editor;
    private static Snackbar snackbar = null;
    private static ApplicationClass mInstance;
    private static Toast toast = null;
    private static SocketConnection socketConnection;
    public static boolean onShareExternal = false, onAppForegrounded = false;
    private static DatabaseHandler dbhelper;
    private Locale locale;

    public static final String APPLICATION_ID = "84636";
    public static final String AUTH_KEY = "mRAThTrbGMNdyy8";
    public static final String AUTH_SECRET = "k2bXRHgE5HEMRTQ";
    public static final String ACCOUNT_KEY = "LBcttVDJrfXnGgnFRRF_";
    public static final String USER_DEFAULT_PASSWORD = "quickblox";

    private static ApplicationClass instance;
    private QBResRequestExecutor qbResRequestExecutor;

    // Showing network status in Snackbar
    public static void showSnack(final Context context, View view, boolean isConnected) {
        if (snackbar == null) {
            snackbar = Snackbar
                    .make(view, context.getString(R.string.network_failure), Snackbar.LENGTH_INDEFINITE)
                    .setAction("SETTINGS", new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            Intent intent = new Intent(android.provider.Settings.ACTION_SETTINGS);
                            context.startActivity(intent);
                        }
                    });
            View sbView = snackbar.getView();
            TextView textView = sbView.findViewById(com.google.android.material.R.id.snackbar_text);
            textView.setTextColor(Color.WHITE);
        }

        if (!isConnected && !snackbar.isShown()) {
            snackbar.show();
        } else {
            snackbar.dismiss();
            snackbar = null;
        }
    }

    public void setPhoneStateListener(PhoneStateReceiver.PhoneState listener) {
        PhoneStateReceiver.phoneState = listener;
    }

    public static void showToast(final Context context, String text, int duration) {

        if (toast == null || toast.getView().getWindowVisibility() != View.VISIBLE) {
            toast = Toast.makeText(context, text, duration);
            toast.show();
        } else toast.cancel();
    }

    /**
     * To convert the given dp value to pixel
     **/
    public static int dpToPx(Context context, int dp) {
        DisplayMetrics displayMetrics = context.getResources().getDisplayMetrics();
        int px = Math.round(dp * (displayMetrics.xdpi / DisplayMetrics.DENSITY_DEFAULT));
        return px;
    }

    public static void hideSoftKeyboard(Activity context, View view) {
        try {
            InputMethodManager inputMethodManager = (InputMethodManager) context.getSystemService(Activity.INPUT_METHOD_SERVICE);
            inputMethodManager.hideSoftInputFromWindow(view.getWindowToken(), 0);
        } catch (NullPointerException npe) {
        } catch (Exception e) {
        }
    }

    public static void showKeyboard(Activity context, View view) {
        view.requestFocus();
        InputMethodManager keyboard = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
        keyboard.showSoftInput(view, 0);
    }

    public static float pxToDp(Context context, float px) {
        Resources resources = context.getResources();
        DisplayMetrics metrics = resources.getDisplayMetrics();
        float dp = px / ((float) metrics.densityDpi / DisplayMetrics.DENSITY_DEFAULT);
        return dp;
    }

    public static void requestFocus(Activity activity, View view, boolean isEnabled) {
        if (view.requestFocus()) {
            if (isEnabled)
                activity.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
            else
                activity.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
        }
    }

    public static String getContactName(Context context, String phoneNumber) {
        if (ContextCompat.checkSelfPermission(context, READ_CONTACTS) == PackageManager.PERMISSION_GRANTED) {
            ContentResolver cr = context.getContentResolver();
            Uri uri = Uri.withAppendedPath(ContactsContract.PhoneLookup.CONTENT_FILTER_URI, Uri.encode(phoneNumber));
            Cursor cursor = cr.query(uri, new String[]{ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME}, null, null, null);
            if (cursor == null) {
                return null;
            }
//            else if(cursor.moveToFirst()) {
//                String id = c.getString(c.getColumnIndexOrThrow(ContactsContract.Contacts._ID));
//            }
            String contactName = phoneNumber;
            if (cursor.moveToFirst()) {
                contactName = cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME));
            }

            if (cursor != null && !cursor.isClosed()) {
                cursor.close();
            }

            return contactName;
        } else {
            return phoneNumber;
        }
    }

    public static HashMap<String, String> getContactrNot(Context context, String phoneNumber) {
        HashMap<String, String> map = new HashMap<>();
        map.put(Constants.TAG_USER_NAME, phoneNumber);
        map.put("isAlready", "false");
        ContentResolver cr = context.getContentResolver();
        Uri uri = Uri.withAppendedPath(ContactsContract.PhoneLookup.CONTENT_FILTER_URI, Uri.encode(phoneNumber));
        Cursor cursor = cr.query(uri, new String[]{ContactsContract.PhoneLookup.DISPLAY_NAME}, null, null, null);
        if (cursor == null) {
            return map;
        }
        String contactName = phoneNumber;
        if (cursor.moveToFirst()) {
            contactName = cursor.getString(cursor.getColumnIndex(ContactsContract.PhoneLookup.DISPLAY_NAME));
            map.put(Constants.TAG_USER_NAME, contactName);
            map.put("isAlready", "true");
        }

        if (cursor != null && !cursor.isClosed()) {
            cursor.close();
        }
        return map;
    }

    /**
     * To convert timestamp to Time
     **/

    public static String getTime(long timeStamp) {
        try {
            DateFormat sdf = new SimpleDateFormat("hh:mm a");
            Date netDate = (new Date(timeStamp * 1000));
            return sdf.format(netDate);
        } catch (Exception ex) {
            ex.printStackTrace();
            return "xx";
        }
    }

    public static String getDateTime(long timeStamp) {
        try {
            DateFormat sdf = new SimpleDateFormat("dd/MM/yyyy hh:mm a");
            Date netDate = (new Date(timeStamp * 1000));
            return sdf.format(netDate);
        } catch (Exception ex) {
            ex.printStackTrace();
            return "xx";
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.v("ApplicationClass", "onCreate");
        /*ComponentName receiver = new ComponentName(this, AlarmReceiver.class);
        PackageManager pm = this.getPackageManager();

        pm.setComponentEnabledSetting(receiver,
                PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
                PackageManager.DONT_KILL_APP);*/

//        setAlarm(this);
        pref = getApplicationContext().getSharedPreferences("SavedPref", MODE_PRIVATE);
        editor = pref.edit();
        instance = this;
        checkAppCredentials();
        initCredentials();
//        changeLocale(getBaseContext());
        if (pref.getBoolean("isLogged", false)) {
            GetSet.setLogged(true);
            GetSet.setUserId(pref.getString("userId", null));
            GetSet.setUserName(pref.getString("userName", null));
            GetSet.setphonenumber(pref.getString("phoneNumber", null));
            GetSet.setcountrycode(pref.getString("countryCode", null));
            GetSet.setImageUrl(pref.getString("userImage", null));
            GetSet.setToken(pref.getString("token", null));
            GetSet.setAbout(pref.getString("about", null));
            GetSet.setPrivacyprofileimage(pref.getString("privacyprofileimage", null));
            GetSet.setPrivacylastseen(pref.getString("privacylastseen", null));
            GetSet.setPrivacyabout(pref.getString("privacyabout", null));
        }
        ProcessLifecycleOwner.get().getLifecycle().addObserver(this);
        mInstance = this;
        ACRA.init(this);
        FontsOverride.setDefaultFont(this, "MONOSPACE", "font_regular.ttf");
        Stetho.initializeWithDefaults(this);
        dbhelper = DatabaseHandler.getInstance(this);
        if (GetSet.isLogged()) {
            socketConnection = SocketConnection.getInstance(this);
        }

        EmojiManager.install(new IosEmojiProvider());
    }

    public static void setAlarm(Context context) {
        // Intent to start the Broadcast Receiver
        Intent broadcastIntent = new Intent(context, AlarmReceiver.class);

        // The Pending Intent to pass in AlarmManager
        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                context,
                0,
                broadcastIntent,
                PendingIntent.FLAG_UPDATE_CURRENT
        );

        // Setting up AlarmManager
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        int SDK_INT = Build.VERSION.SDK_INT;
        long when = System.currentTimeMillis() + 5000;
        if (SDK_INT < Build.VERSION_CODES.KITKAT)
            alarmManager.set(AlarmManager.RTC_WAKEUP, when, pendingIntent);
        else if (SDK_INT < Build.VERSION_CODES.M)
            alarmManager.setExact(AlarmManager.RTC_WAKEUP, when, pendingIntent);
        else {
            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, when, pendingIntent);
        }
//        alarmManager.set(AlarmManager.RTC_WAKEUP, when, pendingIntent);

    }

    @OnLifecycleEvent(Lifecycle.Event.ON_STOP)
    private void onAppBackgrounded() {
        Log.d("MyApp", "App in background");
        onAppForegrounded = false;
        if (!onShareExternal) {
            if (socketConnection != null) {
                socketConnection.disconnect();
            }
            Intent service = new Intent(this, ForegroundService.class);
            service.setAction("stop");
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                startForegroundService(service);
            } else {
                startService(service);
            }

            if (dbhelper != null) {
                dbhelper.close();
            }
        }

//        if (isInCall) {
//            startService(new Intent(getBaseContext(), CallNotificationService.class));
//        }
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_START)
    private void onAppForegrounded() {
        Log.d("MyApp", "App in foreground");
        onAppForegrounded = true;
        dbhelper = DatabaseHandler.getInstance(this);
        if (GetSet.isLogged() && !isNetworkConnected().equals(NOT_CONNECT)) {
            socketConnection = SocketConnection.getInstance(this);
            Intent service = new Intent(this, ForegroundService.class);
            service.setAction("start");
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                startForegroundService(service);
            } else {
                startService(service);
            }
        }
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_ANY)
    private void onAppAny() {
        Log.d("MyApp", "App in onAppAny");
    }

    private String isNetworkConnected() {
        return NetworkUtil.getConnectivityStatusString(this);
    }

    @Override
    protected void attachBaseContext(Context context) {
        super.attachBaseContext(LocaleManager.setLocale(context));
        MultiDex.install(this);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        LocaleManager.setLocale(this);
    }

    public void setConnectivityListener(NetworkReceiver.ConnectivityReceiverListener listener) {
        NetworkReceiver.connectivityReceiverListener = listener;
    }

    public void changeLocale(Context context) {
        Configuration config = context.getResources().getConfiguration();
        String lang = pref
                .getString(Constants.TAG_LANGUAGE_CODE, Constants.TAG_DEFAULT_LANGUAGE_CODE);
        if (!(config.locale.getLanguage().equals(lang))) {
            locale = new Locale(lang);
            Locale.setDefault(locale);
            config.locale = locale;
            Log.e("ApplicationClass: ", lang);
            context.getResources().updateConfiguration(config, context.getResources().getDisplayMetrics());
        }
    }



    private void checkAppCredentials() {
        if (APPLICATION_ID.isEmpty() || AUTH_KEY.isEmpty() || AUTH_SECRET.isEmpty() || ACCOUNT_KEY.isEmpty()) {
            throw new AssertionError(getString(R.string.error_credentials_empty));
        }
    }

    private void initCredentials() {
        QBSettings.getInstance().init(getApplicationContext(), APPLICATION_ID, AUTH_KEY, AUTH_SECRET);
        QBSettings.getInstance().setAccountKey(ACCOUNT_KEY);

        // Uncomment and put your Api and Chat servers endpoints if you want to point the sample
        // against your own server.
        //
        // QBSettings.getInstance().setEndpoints("https://your_api_endpoint.com", "your_chat_endpoint", ServiceZone.PRODUCTION);
        // QBSettings.getInstance().setZone(ServiceZone.PRODUCTION);
    }

    public synchronized QBResRequestExecutor getQbResRequestExecutor() {
        return qbResRequestExecutor == null
                ? qbResRequestExecutor = new QBResRequestExecutor()
                : qbResRequestExecutor;
    }

    public static ApplicationClass getInstance() {
        return instance;
    }

}

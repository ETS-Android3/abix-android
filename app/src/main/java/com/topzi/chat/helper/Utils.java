package com.topzi.chat.helper;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Build;
import android.text.Html;
import android.text.Spanned;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import androidx.coordinatorlayout.widget.CoordinatorLayout;

import com.topzi.chat.R;
import com.topzi.chat.model.ChannelResult;
import com.topzi.chat.model.ContactsData;
import com.topzi.chat.utils.GetSet;
import com.google.android.material.snackbar.Snackbar;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Calendar;

import static com.topzi.chat.utils.Constants.TAG_MY_CONTACTS;
import static com.topzi.chat.utils.Constants.TAG_NOBODY;
import static com.topzi.chat.utils.Constants.TRUE;

public class Utils {

    private static String TAG = "Utils";

    public static String getURLForResource(int resourceId) {
        return Uri.parse("android.resource://com.topzi.chat/" + resourceId).toString();
    }

    public static String getFormattedDate(Context context, long smsTimeInMilis) {
        Calendar smsTime = Calendar.getInstance();
        smsTime.setTimeInMillis(smsTimeInMilis * 1000L);

        Calendar now = Calendar.getInstance();

        final String timeFormatString = "h:mm aa";
        final String dateTimeFormatString = "EEE, MMM d";
        final long HOURS = 60 * 60 * 60;
        if (now.get(Calendar.DATE) == smsTime.get(Calendar.DATE)) {
            return String.valueOf(DateFormat.format(timeFormatString, smsTime));
        } else if (now.get(Calendar.DATE) - smsTime.get(Calendar.DATE) == 1) {
            return context.getString(R.string.yesterday);
        } else if (now.get(Calendar.YEAR) == smsTime.get(Calendar.YEAR)) {
            return DateFormat.format(dateTimeFormatString, smsTime).toString();
        } else {
            return DateFormat.format("MMM dd yyyy", smsTime).toString();
        }
    }

    public static String getCreatedFormatDate(Context context, long smsTimeInMilis) {
        Calendar smsTime = Calendar.getInstance();
        smsTime.setTimeInMillis(smsTimeInMilis * 1000L);

        Calendar now = Calendar.getInstance();

        final String timeFormatString = "h:mm aa";
        final String dateTimeFormatString = "EEE, MMM d";
        final long HOURS = 60 * 60 * 60;
        if (now.get(Calendar.DATE) == smsTime.get(Calendar.DATE)) {
            return String.valueOf(context.getString(R.string.today) + " " +
                    DateFormat.format(timeFormatString, smsTime));
        } else if (now.get(Calendar.DATE) - smsTime.get(Calendar.DATE) == 1) {
            return context.getString(R.string.yesterday);
        } else if (now.get(Calendar.YEAR) == smsTime.get(Calendar.YEAR)) {
            return DateFormat.format(dateTimeFormatString, smsTime).toString();
        } else {
            return DateFormat.format("MMM dd yyyy", smsTime).toString();
        }
    }

    public byte[] getBytes(InputStream is) throws IOException {
        ByteArrayOutputStream byteBuff = new ByteArrayOutputStream();
        int buffSize = 1024;
        byte[] buff = new byte[buffSize];
        int len = 0;
        while ((len = is.read(buff)) != -1) {
            byteBuff.write(buff, 0, len);
        }
        return byteBuff.toByteArray();
    }

    public static String isNetworkConnected(Context context) {
        return NetworkUtil.getConnectivityStatusString(context);
    }

    public static void networkSnack(CoordinatorLayout mainLay, Context context) {
        Snackbar snackbar = Snackbar
                .make(mainLay, context.getString(R.string.network_failure), Snackbar.LENGTH_SHORT);
        View sbView = snackbar.getView();
        TextView textView = sbView.findViewById(com.google.android.material.R.id.snackbar_text);
        textView.setTextColor(Color.WHITE);
        snackbar.show();
    }

    public static Spanned fromHtml(String html) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            return Html.fromHtml(html, Html.FROM_HTML_MODE_COMPACT);
        } else {
            return Html.fromHtml(html);
        }
    }

    public static boolean isUserAdminInChannel(ChannelResult.Result channelData) {
        if (channelData.channelAdminId != null && channelData.channelAdminId.equalsIgnoreCase(GetSet.getUserId())) {
            return true;
        } else {
            return false;
        }
    }

    public static boolean isChannelAdmin(ChannelResult.Result channelData, String userId) {
        return channelData.channelAdminId != null && channelData.channelAdminId.equalsIgnoreCase(userId);
    }

    public static boolean isProfileEnabled(ContactsData.Result result) {
        if (result.privacy_profile_image.equalsIgnoreCase(TAG_MY_CONTACTS)) {
            return result.contactstatus != null && result.contactstatus.equalsIgnoreCase(TRUE);
        } else return !result.privacy_profile_image.equalsIgnoreCase(TAG_NOBODY);
    }

    public static boolean isLastSeenEnabled(ContactsData.Result result) {
        if (result.privacy_last_seen.equalsIgnoreCase(TAG_MY_CONTACTS)) {
            return result.contactstatus != null && result.contactstatus.equalsIgnoreCase(TRUE);
        } else return !result.privacy_last_seen.equalsIgnoreCase(TAG_NOBODY);
    }

    public static boolean isAboutEnabled(ContactsData.Result result) {
        try {
            if (result.privacy_about.equalsIgnoreCase(TAG_MY_CONTACTS)) {
                return result.contactstatus != null && result.contactstatus.equalsIgnoreCase(TRUE);
            } else return !result.privacy_about.equalsIgnoreCase(TAG_NOBODY);
        } catch (NullPointerException e) {
            return false;
        }
    }

    public static void refreshGallery(String TAG, Context context, File file) {

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                final Intent scanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
                final Uri contentUri = Uri.fromFile(file);
                scanIntent.setData(contentUri);
                context.sendBroadcast(scanIntent);
                MediaScannerConnection.scanFile(context, new String[]{file.getAbsolutePath()}, null, new MediaScannerConnection.OnScanCompletedListener() {
                    @Override
                    public void onScanCompleted(String path, Uri uri) {
                        Log.e(TAG, "Finished scanning " + file.getAbsolutePath());
                    }
                });
            } else {
                context.sendBroadcast(new Intent(Intent.ACTION_MEDIA_MOUNTED, Uri.parse("file://"
                        + StorageManager.getDataRoot())));

            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

package com.topzi.chat.helper;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;

import com.topzi.chat.R;
import com.topzi.chat.activity.ChatSettingsActivity;
import com.topzi.chat.utils.Constants;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;

import static android.content.Context.MODE_PRIVATE;

/**
 * Created on 7/3/17.
 */

public class StorageManager {

    private static Context context;
    private static StorageManager mInstance;
    private final String TAG = this.getClass().getSimpleName();
    private boolean mediaVisibility = true;
    SharedPreferences pref;

    public StorageManager(Context context) {
        this.context = context;
        pref = context.getSharedPreferences(Constants.PREFERENCE_NAME, MODE_PRIVATE);
    }

    public static synchronized StorageManager getInstance(Context context) {
        if (mInstance == null) {
            mInstance = new StorageManager(context);
        }
        return mInstance;
    }

    public String saveToSdCard(Context context, Bitmap bitmap, String from, String filename) {
        mediaVisibility = pref.getBoolean(Constants.PREF_MEDIA_VISIBILITY, true);
        String stored = "";
        //File sdcard = context.getExternalFilesDir(null);
        File sdcard = getDataRoot();
        boolean showInGallery = false;

        String path = "";
        if (from.equals("sent")) {
            path = "/" + context.getString(R.string.app_name) + "/" + context.getString(R.string.app_name) + "Images/Sent/";
        } else if (from.equals("profile")) {
            path = "/" + context.getString(R.string.app_name) + "/" + context.getString(R.string.app_name) + "Images/Profile/";
            showInGallery = true;
        } else if (from.equals("thumb")) {
            path = "/" + context.getString(R.string.app_name) + "/" + context.getString(R.string.app_name) + "Images/.thumbnails/";
        } else if (from.equals("wallpaper")){
            path = "/" + context.getString(R.string.app_name) + "/" + context.getString(R.string.app_name) + "Images/wallpaper/";
            showInGallery = true;
        }
        else if(mediaVisibility){
            path = "/" + context.getString(R.string.app_name) + "/" + context.getString(R.string.app_name) + "Images/";
            showInGallery = true;
        }
        else {
            path = "/" + context.getString(R.string.app_name) + "/" + context.getString(R.string.app_name) + "Images/.Private/";
        }

        File folder = new File(sdcard.getAbsoluteFile(), path);
        if (from.equals("sent")) {
            folder.mkdirs();
            File nomediaFile = new File(folder.getAbsoluteFile(), ".nomedia");
            try {
                if (!nomediaFile.exists()) {
                    nomediaFile.createNewFile();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else if (from.equals("profile")) {
            folder.mkdirs();
        } else if (from.equals("wallpaper")) {
            folder.mkdirs();
        } else {
            folder.mkdirs();
        }

        File file = new File(folder.getAbsoluteFile(), filename);
        if (file.exists())
            file.delete();

        try {
            FileOutputStream out = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 90, out);
            out.flush();
            out.close();
            stored = "success";

            //Utils.refreshGallery(TAG, context, file);
            if(showInGallery && mediaVisibility){
                addImageToGallery(context, file);
            }
        } catch (Exception e) {
            Log.e("LLLLLL_Exc: ",e.getMessage());
            e.printStackTrace();
        }
        return stored;
    }

    public boolean saveImageToSentPath(String imagePath, String imageName) {
        //String path = context.getExternalFilesDir(null) + "/" + context.getString(R.string.app_name) + "/" + context.getString(R.string.app_name) + "Images/Sent";
        String path = getDataRoot() + "/" + context.getString(R.string.app_name) + "/" + context.getString(R.string.app_name) + "Images/Sent";
        File folder = new File(path);
        if (!folder.exists()) {
            folder.mkdirs();
            File nomediaFile = new File(folder.getAbsoluteFile(), ".nomedia");
            try {
                if (!nomediaFile.exists()) {
                    nomediaFile.createNewFile();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        File from = new File(imagePath);
        File to = new File(folder + "/" + imageName);
        Log.v("uploadzz", "from=" + from);
        Log.v("uploadzz", "to=" + to);
        if (from.exists()) {
            try {
                FileUtils.copyFile(from, to);
                Utils.refreshGallery(TAG, context, to);
            } catch (IOException e) {
                e.printStackTrace();
            }
            Log.v("uploadzz", "moved");
        }
        return true;
    }

    public String saveThumbNail(Bitmap bitmap, String filename) {
        String stored = "";

        StringBuilder path = new StringBuilder("/" + context.getString(R.string.app_name) + "/" + context.getString(R.string.app_name) + "Images/.thumbnails");
        String[] addPath = filename.split("/");
        if (addPath.length > 0) {
            for (int i = 0; i < addPath.length - 1; i++)
                path.append("/").append(addPath[i]);
        }
        //File sdcard = context.getExternalFilesDir(null);
        File sdcard = getDataRoot();
        File folder = new File(sdcard.getAbsoluteFile(), path.toString());
        folder.mkdirs();

        File nomediaFile = new File(folder.getAbsoluteFile(), ".nomedia");
        try {
            if (!nomediaFile.exists()) {
                nomediaFile.createNewFile();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        String[] fileName = filename.split("/");
        File file = new File(folder.getAbsoluteFile(), fileName[fileName.length - 1]);
        if (file.exists())
            return "success";

        try {
            FileOutputStream out = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 80, out);
            out.flush();
            out.close();
            stored = "success";
        } catch (Exception e) {
            e.printStackTrace();
        }
        return stored;
    }

    public File getImage(String from, String imagename) {
        mediaVisibility = pref.getBoolean(Constants.PREF_MEDIA_VISIBILITY, true);
        File mediaImage = null;
        try {
            //String root = context.getExternalFilesDir(null).toString();
            String root = getDataRoot().toString();
            File myDir = new File(root);
            if (!myDir.exists())
                return null;

            String path = "";
            if (from.equals("sent")) {
                path = "/" + context.getString(R.string.app_name) + "/" + context.getString(R.string.app_name) + "Images/Sent/";
            } else if (from.equals("profile")) {
                path = "/" + context.getString(R.string.app_name) + "/" + context.getString(R.string.app_name) + "Images/Profile/";
            } else if (from.equals("thumb")) {
                path = "/" + context.getString(R.string.app_name) + "/" + context.getString(R.string.app_name) + "Images/.thumbnails/";
            } else if (from.equals("wallpaper")) {
                path = "/" + context.getString(R.string.app_name) + "/" + context.getString(R.string.app_name) + "Images/wallpaper/";
            }
            else if(mediaVisibility){
                path = "/" + context.getString(R.string.app_name) + "/" + context.getString(R.string.app_name) + "Images/";
            }
            else {
                path = "/" + context.getString(R.string.app_name) + "/" + context.getString(R.string.app_name) + "Images/.Private";
            }
            mediaImage = new File(myDir.getPath() + path + imagename);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return mediaImage;
    }

    /*public static boolean FetchFiles(String from,Context ac, String file_name) {

        File mediaImage = null;
            //String root = context.getExternalFilesDir(null).toString();
            String root = getDataRoot().toString();
            File myDir = new File(root);

            String path = "";
            if (from.equals("sent")) {
                path = "/" + context.getString(R.string.app_name) + "/" + context.getString(R.string.app_name) + "Images/Sent/";
            } else if (from.equals("profile")) {
                path = "/" + context.getString(R.string.app_name) + "/" + context.getString(R.string.app_name) + "Images/Profile/";
            } else if (from.equals("thumb")) {
                path = "/" + context.getString(R.string.app_name) + "/" + context.getString(R.string.app_name) + "Images/.thumbnails/";
            } else if (from.equals("wallpaper")) {
                path = "/" + context.getString(R.string.app_name) + "/" + context.getString(R.string.app_name) + "Images/wallpaper/";
            } else {
                path = "/" + context.getString(R.string.app_name) + "/" + context.getString(R.string.app_name) + "Images/";
            }

        String Storage_path = myDir.getPath() + path;
        ArrayList<String> filenames = new ArrayList<String>();
        File[] files = new File[0];

        File directory = new File(Storage_path);
        if (!directory.exists()) {
            directory.mkdirs();
        }
        files = directory.listFiles();
        for (File file : files) {
            String name = file.getName();
            filenames.add(name);
        }
        return filenames.contains(file_name);
    }*/

    public boolean checkifImageExists(String from, String imagename) {
        File file = getImage(from, imagename);
        Log.v("dir", "dir=" + file);
        if (file.exists())
            return true;
        else
            return false;
    }

    public boolean moveFilesToSentPath(Context context, String type, String filePath, String fileName) {
        if (type.equals("audio")) {
            type = "Audios";
        } else if (type.equals("video")) {
            type = "Videos";
        } else {
            type = "Files";
        }
        //File dir = new File(context.getExternalFilesDir(null) + "/" + context.getString(R.string.app_name) + "/" + context.getString(R.string.app_name) + type + "/Sent");
        File dir = new File(getDataRoot() + "/" + context.getString(R.string.app_name) + "/" + context.getString(R.string.app_name) + type + "/Sent");
        if (!dir.exists()) {
            dir.mkdirs();
            File nomediaFile = new File(dir.getAbsoluteFile(), ".nomedia");
            try {
                if (!nomediaFile.exists()) {
                    nomediaFile.createNewFile();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        File from = new File(filePath);
        File to = new File(dir + "/" + fileName);
        Log.v("uploadzz", "from=" + from);
        Log.v("uploadzz", "to=" + to);
        if (from.exists()) {
            try {
                FileUtils.copyFile(from, to);
            } catch (IOException e) {
                e.printStackTrace();
            }
            Log.v("uploadzz", "moved");
        }
        return true;
    }

    public boolean checkifFileExists(String fileName, String fileType, String from) {
        File dir = getFile(fileName, fileType, from);
        Log.v("dir", "dir=" + dir);
        if (dir.exists()) {
            return true;
        }
        return false;
    }

    public File getFile(String fileName, String fileType, String from) {
        //String root = context.getExternalFilesDir(null).toString();
        String root = getDataRoot().toString();
        File myDir = new File(root);
        if (!myDir.exists())
            return null;

        if (fileType.equals("audio")) {
            fileType = "Audios";
        } else if (fileType.equals("video")) {
            fileType = "Videos";
        } else if (fileType.equals("voice")) {
            fileType = "Files";
        } else {
            fileType = "Files";
        }

        if (from.equals("sent")) {
            from = "Sent/";
        } else {
            from = "";
        }

        File dir = new File(myDir.getPath() + "/" + context.getString(R.string.app_name) + "/" + context.getString(R.string.app_name) + fileType + "/" + from + fileName);

        return dir;
    }

    public static File getDataRoot() {
        try {
            if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED) ||
                    (Environment.getExternalStorageDirectory().exists() && Environment.getExternalStorageDirectory().canWrite())) {
                return Environment.getExternalStorageDirectory();
            } else {
                return Environment.getDataDirectory();
            }
        } catch (Exception e) {
            return null;
        }
    }

    public static void refreshAndroidGallery(Context context, File file) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            Intent mediaScanIntent = new Intent(
                    Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
            mediaScanIntent.setData(Uri.fromFile(file));
            context.sendBroadcast(mediaScanIntent);
        } else {
            context.sendBroadcast(new Intent(
                    Intent.ACTION_MEDIA_MOUNTED,
                    Uri.parse("file://" + Environment.getExternalStorageDirectory())));
        }
    }

    public static void addImageToGallery(Context context, File file) {
        ContentValues values = new ContentValues();
        values.put(MediaStore.Images.Media.DATA, file.getAbsolutePath());
        values.put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg");
        context.getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
    }
}

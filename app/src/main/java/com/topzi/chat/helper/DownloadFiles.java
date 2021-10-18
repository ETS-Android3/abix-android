package com.topzi.chat.helper;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.topzi.chat.R;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created on 30/6/18.
 */

public abstract class DownloadFiles extends AsyncTask<String, Void, String> {
    private Context context;
    private static final String TAG = "DownloadFiles";

    public DownloadFiles(Context context) {
        this.context = context;
    }

    @Override
    protected String doInBackground(String... strings) {
        try {
            String filename = getFileName(strings[0]);
            URL url = new URL(strings[0]);//Create Download URl
            HttpURLConnection c = (HttpURLConnection) url.openConnection();//Open Url Connection
            c.setRequestMethod("GET");//Set Request Method to "GET" since we are grtting data
            c.connect();//connect the URL Connection

            //If Connection response is not OK then show Logs
            if (c.getResponseCode() != HttpURLConnection.HTTP_OK) {
                Log.e(TAG, "Server returned HTTP " + c.getResponseCode()
                        + " " + c.getResponseMessage());
            }

            String type = null;
            if (strings[1].equals("audio")) {
                type = "Audios";
            } else if (strings[1].equals("video")) {
                type = "Videos";
            } else {
                type = "Files";
            }
            StringBuilder path = new StringBuilder(StorageManager.getDataRoot() + "/" + context.getString(R.string.app_name) + "/" + context.getString(R.string.app_name) + type);
            String[] addPath = strings[0].split("/");
            if (addPath.length > 0) {
                for (int i = 3; i < addPath.length - 1; i++)
                    path.append("/").append(addPath[i]);
            }

            File mediaStorageDir = new File(path.toString());

            // Create the storage directory if it does not exist
            if (!mediaStorageDir.exists()) {
                mediaStorageDir.mkdirs();
            }

            File outputFile = new File(mediaStorageDir, filename);//Create Output file in Main File

            //Create New File if not present
            if (!outputFile.exists()) {
                outputFile.createNewFile();
                Log.e(TAG, "File Created");
            }

            FileOutputStream fos = new FileOutputStream(outputFile);//Get OutputStream for NewFile Location

            InputStream is = c.getInputStream();//Get InputStream for connection

            byte[] buffer = new byte[1024];//Set buffer type
            int len1 = 0;//init length
            while ((len1 = is.read(buffer)) != -1) {
                fos.write(buffer, 0, len1);//Write new file
            }

            //Close all connection after doing task
//            fos.flush();
//            fos.getFD().sync();
            fos.close();
            is.close();


            Utils.refreshGallery(TAG, context, outputFile);
            return outputFile.getAbsolutePath();
        } catch (Exception e) {
            //Read exception if something went wrong
            e.printStackTrace();
            Log.e(TAG, "Download Error Exception " + e.getMessage());
        }
        return null;
    }

    private String getFileName(String url) {
        String imgSplit = url;
        int endIndex = imgSplit.lastIndexOf("/");
        if (endIndex != -1) {
            imgSplit = imgSplit.substring(endIndex + 1, imgSplit.length());
        }
        return imgSplit;
    }

    protected abstract void onPostExecute(String downPath);
}

package com.droidninja.imageeditengine;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.graphics.RectF;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;

import androidx.annotation.Nullable;

import com.droidninja.imageeditengine.utils.FragmentUtil;

import java.io.File;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import static com.droidninja.imageeditengine.ImageEditor.EXTRA_IMAGE_FROM;
import static com.droidninja.imageeditengine.ImageEditor.EXTRA_IMAGE_PATH;

public class ImageEditActivity extends BaseImageEditActivity
    implements PhotoEditorFragment.OnFragmentInteractionListener,
    Crop1Fragment.OnFragmentInteractionListener {

  public static String imagePath = "";
  public static String from = "";
  private Rect cropRect;

  //private View touchView;

  @Override protected void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_image_edit);

    imagePath = getIntent().getStringExtra(EXTRA_IMAGE_PATH);
    from = getIntent().getStringExtra(EXTRA_IMAGE_FROM);
    if (imagePath != null) {
      FragmentUtil.addFragment(ImageEditActivity.this, R.id.fragment_container,
          PhotoEditorFragment.newInstance(imagePath));
    }
  }

  @Override public void onCropClicked(Bitmap bitmap) {
    FragmentUtil.replaceFragment(this, R.id.fragment_container,
        Crop1Fragment.newInstance(bitmap));
  }

  @Override public void onDoneClicked(String imagePath) {
    Log.e("LLLLLL_FilePath: ",imagePath);
    Intent intent = null;
    try {
      if (from.equals("Chat")) {
        intent = new Intent(ImageEditActivity.this, Class.forName("com.topzi.chat.activity.ChatActivity"));
        intent.putExtra(ImageEditor.EXTRA_EDITED_PATH, imagePath);
        startActivity(intent);
        finish();
      } else if (from.equals("Group")) {
        intent = new Intent(ImageEditActivity.this, Class.forName("com.topzi.chat.activity.GroupChatActivity"));
        intent.putExtra(ImageEditor.EXTRA_EDITED_PATH, imagePath);
        startActivity(intent);
        finish();
      } else if (from.equals("Status")) {
        intent = new Intent(ImageEditActivity.this, Class.forName("com.topzi.chat.activity.MainActivity"));
        intent.putExtra(ImageEditor.EXTRA_EDITED_PATH, imagePath);
        startActivity(intent);
        finish();
      }
    } catch (ClassNotFoundException e) {
      e.printStackTrace();
    }
  }

  @Override public void onImageCropped(Bitmap bitmap) {
    SaveImage(bitmap);
//    PhotoEditorFragment photoEditorFragment =
//        (PhotoEditorFragment) FragmentUtil.getFragmentByTag(this,
//            PhotoEditorFragment.class.getSimpleName());
//    if (photoEditorFragment != null) {
//      photoEditorFragment.reset();
//      FragmentUtil.removeFragment(this,
//          (BaseFragment) FragmentUtil.getFragmentByTag(this, Crop1Fragment.class.getSimpleName()));
//    }
  }

  @Override public void onCancelCrop() {
    if (imagePath != null) {
      FragmentUtil.addFragment(ImageEditActivity.this, R.id.fragment_container,
              PhotoEditorFragment.newInstance(imagePath));
    }
  }

  @Override public void onBackPressed() {
    super.onBackPressed();
  }

  private void SaveImage(Bitmap finalBitmap) {
    String root = Environment.getExternalStorageDirectory().toString();
    File myDir = new File(root + "/Halloween Photo Frame");
    File mySubDir = new File(myDir.getAbsolutePath() + "/temp");
    String path = myDir.getAbsolutePath();
    Log.d("LLLLL_Data","data: "+path);
    myDir.mkdirs();
    mySubDir.mkdirs();

    String currentDateAndTime = getCurrentDateAndTime();

    if(!myDir.exists()) {
      myDir.mkdirs();
    }

    if(!mySubDir.exists())
    {
      mySubDir.mkdirs();
    }

    File file = new File(mySubDir, "image_" + currentDateAndTime + ".jpg");

    if (file.exists ()) file.delete ();
    try {
      FileOutputStream out = new FileOutputStream(file);
      finalBitmap.compress(Bitmap.CompressFormat.JPEG, 100, out);
      out.flush();
      out.close();

      FragmentUtil.addFragment(ImageEditActivity.this, R.id.fragment_container,
              PhotoEditorFragment.newInstance(file.getAbsolutePath()));

    } catch (Exception e) {
      e.printStackTrace();
    }
//        galleryAddPic(file);
       /* sendBroadcast(new Intent(
                Intent.ACTION_MEDIA_MOUNTED,
                Uri.parse("file://" + Environment.getExternalStorageDirectory())));*/
  }

  private String getCurrentDateAndTime() {
    Calendar calendar = Calendar.getInstance();
    // Setting format of the time
    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss");
    // Getting the formated date as a string
    String formattedDate = dateFormat.format(calendar.getTime());

    return formattedDate;
  }

}

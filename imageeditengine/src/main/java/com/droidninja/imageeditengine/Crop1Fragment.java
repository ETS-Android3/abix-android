package com.droidninja.imageeditengine;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Rect;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.isseiaoki.simplecropview.CropImageView;


public class Crop1Fragment extends BaseFragment implements View.OnClickListener{

    private Crop1Fragment.OnFragmentInteractionListener mListener;
    private CropImageView cropImageView;
    private int currentAngle;
    public static Bitmap GalleryBitmap;
    public static int GalleryBitmapHeigt;
    public static int GalleryBitmapWidth;

    public Crop1Fragment() {
        // Required empty public constructor
    }

    @Override public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_crop1, container, false);
    }

    @Override public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof Crop1Fragment.OnFragmentInteractionListener) {
            mListener = (Crop1Fragment.OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString() + " must implement OnFragmentInteractionListener");
        }
    }

    @Override public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    public static Crop1Fragment newInstance(Bitmap bitmap) {
        Crop1Fragment Crop1Fragment =  new Crop1Fragment();
        Bundle bundle = new Bundle();
        bundle.putParcelable(ImageEditor.EXTRA_ORIGINAL,bitmap);
        Crop1Fragment.setArguments(bundle);

        return Crop1Fragment;
    }

    public void setImageBitmap(Bitmap bitmap) {
        cropImageView.setImageBitmap(bitmap);
    }

    public interface OnFragmentInteractionListener {
        void onImageCropped(Bitmap bitmap);
        void onCancelCrop();
    }

    @Override protected void initView(View view) {
        cropImageView = view.findViewById(R.id.cropImageView);
        view.findViewById(R.id.btn_cancel).setOnClickListener(this);
//        view.findViewById(R.id.back_iv).setOnClickListener(this);
        view.findViewById(R.id.rotate_left).setOnClickListener(this);
        view.findViewById(R.id.done).setOnClickListener(this);
        if(getArguments()!=null) {
            final Bitmap bitmapimage = getArguments().getParcelable(ImageEditor.EXTRA_ORIGINAL);
            if(bitmapimage!=null){
                cropImageView.setImageBitmap(bitmapimage);
                cropImageView.setCropMode(CropImageView.CropMode.FREE);
            }
        }
    }

    @Override public void onClick(View view) {
        if(view.getId()==R.id.rotate_left){
            cropImageView.rotateImage(CropImageView.RotateDegrees.ROTATE_M90D);
        }
        else if(view.getId()==R.id.btn_cancel){
            mListener.onCancelCrop();
        }
        else if(view.getId()==R.id.done){
            mListener.onImageCropped(cropImageView.getCroppedBitmap());
//            getActivity().onBackPressed();
        } 
    }
}

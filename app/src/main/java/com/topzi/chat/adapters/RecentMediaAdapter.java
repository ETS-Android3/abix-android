package com.topzi.chat.adapters;

import android.content.Context;
import android.media.ExifInterface;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.topzi.chat.R;
import com.topzi.chat.helper.StorageManager;
import com.topzi.chat.model.MessagesData;
import com.topzi.chat.utils.Constants;
import com.topzi.chat.utils.GetSet;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;

import java.io.File;
import java.util.ArrayList;

public class RecentMediaAdapter extends RecyclerView.Adapter<RecentMediaAdapter.RecentMediaViewHolder> {
    Context context;
    ArrayList<MessagesData> list;
    StorageManager storageManager;
    AdapterItemClickListener listener;
    public RecentMediaAdapter(Context context, ArrayList<MessagesData> data){
        this.context = context;
        this.list = data;
        storageManager = StorageManager.getInstance(context);
    }
    @NonNull
    @Override
    public RecentMediaViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_recent_media, parent, false);
        return new RecentMediaViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull RecentMediaViewHolder holder, int position) {
        MessagesData message = list.get(position);
        if (message.user_id != null && message.user_id.equals(GetSet.getUserId())) {
            switch (message.progress) {
                case "":
                case "error": {
                    File file = storageManager.getImage("sent", getFileName(message.attachment));
                    if (file != null) {
                        Log.v("TAG", "checkChat=" + file.getAbsolutePath());
                        Glide.with(context).load(Uri.fromFile(file)).thumbnail(0.5f)
                                .into(holder.imageView);
                    }
                    break;
                }
                case "completed": {
                    File file = storageManager.getImage("sent", message.attachment);
                    if (file != null) {
                        Log.v("TAG", "checkChat=" + file.getAbsolutePath());
                        Glide.with(context).load(Uri.fromFile(file)).thumbnail(0.5f)
                                .into(holder.imageView);
                    }
                    break;
                }
            }
        }
        else{
            if (storageManager.checkifImageExists("thumb", message.attachment)) {
                File file = storageManager.getImage("thumb", message.attachment);
                ExifInterface exif = null;
                int orientation = 0;
                if (file != null) {
                    Glide.with(context).load(file).thumbnail(0.5f)
                            .into(holder.imageView);
                }
            } else {
                Glide.with(context).load(Constants.CHAT_IMG_PATH + message.attachment).thumbnail(0.5f)
                        .apply(RequestOptions.overrideOf(18, 18))
                        .into(holder.imageView);
            }
        }

        holder.imageView.setOnClickListener(view -> {
            if(listener != null){
                listener.showImageClicked(message, position);
            }
        });

    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    class RecentMediaViewHolder extends RecyclerView.ViewHolder {
        ImageView imageView;
        public RecentMediaViewHolder(@NonNull View itemView) {
            super(itemView);
            this.imageView = itemView.findViewById(R.id.uploadimage);
        }
    }

    private String getFileName(String url) {
        String imgSplit = url;
        int endIndex = imgSplit.lastIndexOf("/");
        if (endIndex != -1) {
            imgSplit = imgSplit.substring(endIndex + 1, imgSplit.length());
        }
        return imgSplit;
    }


    public void setAdapterItemClickListener(AdapterItemClickListener listener){
        this.listener = listener;
    }
    public interface AdapterItemClickListener{
        void showImageClicked(MessagesData messagesData, int pos);
    }
}

package com.topzi.chat.adapters

import android.content.Context
import android.graphics.drawable.Drawable
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.load.resource.bitmap.FitCenter
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.topzi.chat.R
import com.topzi.chat.model.ImageDataDto
import kotlinx.android.synthetic.main.item_image_upload.view.*

class ScreenShotsAdapter(
    val context: Context,
    val imageList: ArrayList<ImageDataDto>,
    var mListener: OnUploadImageListener
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val v = LayoutInflater.from(context)
            .inflate(R.layout.item_image_upload, null)
        return CompaniesViewHolder(v)
    }

    override fun getItemCount(): Int {
        return imageList.size
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is CompaniesViewHolder) {
            val imageDto: ImageDataDto = imageList[position]
            if (imageDto.imgUrlList != null) {
                Glide.with(context)
                    .load(imageDto.imgUrlList as Uri?)
                    .listener(object : RequestListener<Drawable?> {
                        override fun onLoadFailed(
                            e: GlideException?,
                            model: Any,
                            target: Target<Drawable?>,
                            isFirstResource: Boolean
                        ): Boolean { //avLoadingIndicatorView.setVisibility(View.GONE);
                            return false
                        }

                        override fun onResourceReady(
                            resource: Drawable?,
                            model: Any,
                            target: Target<Drawable?>,
                            dataSource: DataSource,
                            isFirstResource: Boolean
                        ): Boolean { //avLoadingIndicatorView.setVisibility(View.GONE);
                            return false
                        }
                    })
                    .transform(FitCenter())
                    .into(holder.iv_uploading_img)
                holder.iv_add.visibility = View.GONE
                holder.iv_close.visibility = View.VISIBLE
                holder.iv_uploading_img.visibility = View.VISIBLE
                holder.iv_close.setOnClickListener {
                    mListener.onCloseItemListener(imageDto, position)
                }
            } else {
                holder.iv_add.visibility = View.VISIBLE
                holder.iv_close.visibility = View.GONE
                holder.iv_uploading_img.visibility = View.GONE
                holder.iv_add.setOnClickListener {
                    mListener.onOpenNSetImageListener(position)
                }
            }
        }
    }

    class CompaniesViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val iv_uploading_img = view.iv_uploading_img
        val iv_close = view.iv_close
        val iv_add = view.iv_add
    }

    interface OnUploadImageListener {
        fun onCloseItemListener(imageDto: ImageDataDto?, position: Int)
        fun onOpenNSetImageListener(position: Int)
    }
}
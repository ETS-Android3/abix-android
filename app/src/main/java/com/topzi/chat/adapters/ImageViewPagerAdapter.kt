package com.topzi.chat.adapters

import android.content.Context
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.Toast
import androidx.viewpager.widget.PagerAdapter
import androidx.viewpager.widget.ViewPager
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.topzi.chat.R
import com.topzi.chat.model.MessagesData
import com.topzi.chat.utils.GetSet
import java.io.File


class ImageViewPagerAdapter(val context: Context, val imageList: ArrayList<MessagesData>) :
    PagerAdapter() {
    val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
    val storageManager = com.topzi.chat.helper.StorageManager.getInstance(context)
    override fun isViewFromObject(view: View, `object`: Any): Boolean {
        return view === `object` as RelativeLayout
    }

    override fun getCount(): Int {
        return imageList.size
    }

    override fun instantiateItem(container: ViewGroup, position: Int): Any {
        val viewLayout: View = inflater.inflate(
            R.layout.image_view_adapter_layout, container,
            false
        )
        val imageView = viewLayout.findViewById<ImageView>(R.id.imageView)

        val messagesData = imageList[position]

        if (messagesData.user_id != null && messagesData.user_id == GetSet.getUserId()) {
            if (messagesData.progress == "completed") {
                if (storageManager.checkifImageExists("sent", messagesData.attachment)) {
                    val file: File = storageManager.getImage("sent", messagesData.attachment)
                    if (file != null) {
                        Glide.with(context).load(Uri.fromFile(file)).thumbnail(0.5f)
                            .transition(DrawableTransitionOptions().crossFade())
                            .into(imageView)
                    }
                } else {
                    Toast.makeText(
                        context,
                        context.getString(R.string.no_media),
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        } else {
            val file: File = storageManager.getImage("thumb", messagesData.attachment)
            if (file != null) {
                Glide.with(context).load(file).thumbnail(0.5f)
                    .transition(DrawableTransitionOptions().crossFade())
                    .into(imageView)
            }
        }

        (container as ViewPager).addView(viewLayout)

        return viewLayout
    }

    override fun destroyItem(container: ViewGroup, position: Int, `object`: Any) {
        (container as ViewPager).removeView(`object` as RelativeLayout)
    }
}
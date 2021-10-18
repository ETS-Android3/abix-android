package com.abix.chat.fragments

import android.app.Activity
import android.app.ProgressDialog
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import com.google.android.material.snackbar.Snackbar
import com.topzi.chat.R
import com.topzi.chat.adapters.ScreenShotsAdapter
import com.topzi.chat.model.ContactUsDto
import com.topzi.chat.model.ImageDataDto
import com.topzi.chat.utils.*
import id.zelory.compressor.Compressor
import kotlinx.android.synthetic.main.fragment_contact_us.*
import kotlinx.android.synthetic.main.fragment_contact_us.view.*
import okhttp3.MediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File
import java.io.FileNotFoundException
import java.io.IOException
import java.util.*
import kotlin.collections.ArrayList

class ContactUsFragment : Fragment(), ScreenShotsAdapter.OnUploadImageListener {
    private val IMG_REQUEST = 111
    var imageList: ArrayList<ImageDataDto> = java.util.ArrayList()
    var madapter: ScreenShotsAdapter? = null
    var selectedposition: Int = -1
    private var marshMallowPermission: MarshMallowPermission? = null
    private var apiInterface: ApiInterface? = null
    var progressDialog: ProgressDialog? = null
    var fileList: ArrayList<File> = java.util.ArrayList()
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val rootview = inflater.inflate(R.layout.fragment_contact_us, container, false)
        marshMallowPermission = MarshMallowPermission(activity)
        apiInterface = ApiClient.getClient().create(ApiInterface::class.java)
        progressDialog = ProgressDialog(context)
        progressDialog?.setMessage(resources.getString(R.string.pleasewait))
        progressDialog?.setCancelable(false)
        initImgArray()
        setListeners(rootview)
        setAdapter(rootview)
        return rootview
    }

    fun initImgArray() {
        imageList.add(ImageDataDto())
        imageList.add(ImageDataDto())
        imageList.add(ImageDataDto())
    }

    fun setListeners(view: View) {
        view.et_problem.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                if (s.length > 0) {
                    view.btn_next.setBackgroundResource(R.drawable.bg_button_green)
                } else {
                    view.btn_next.setBackgroundResource(R.drawable.bg_button_gray)
                }
            }

            override fun afterTextChanged(s: Editable) {}
        });
        view.btn_next.setOnClickListener {
            val message: String = view.et_problem.text.toString()
            if (message.length > 0)
                submitCountactUs(message)
            else
                Toast.makeText(context, "Please write something", Toast.LENGTH_SHORT)
        }
    }

    fun setAdapter(view: View) {
        var gridLayoutManager = GridLayoutManager(context, 3)
        madapter = context?.let { ScreenShotsAdapter(it, imageList, this) }
        view.rv_images.setLayoutManager(gridLayoutManager)
        view.rv_images.adapter = madapter
    }

    override fun onCloseItemListener(imageDto: ImageDataDto?, position: Int) {
        imageList.set(position, ImageDataDto())
        madapter?.notifyDataSetChanged()
    }

    override fun onOpenNSetImageListener(position: Int) {
        selectedposition = position
        if (!marshMallowPermission?.checkPermissionForExternalStorage()!! and !marshMallowPermission?.checkPermissionForReadStorage()!!)
            marshMallowPermission?.requestPermissionForExternalStorage()
        else
            selectimage()

    }

    fun selectimage() {
        val imgintent = Intent()
        imgintent.type = "image/*"
        imgintent.action = Intent.ACTION_PICK
        startActivityForResult(imgintent, IMG_REQUEST)
    }

    private fun networkSnack() {
        val snackbar = Snackbar
            .make(cl_main, getString(R.string.network_failure), Snackbar.LENGTH_SHORT)
        val sbView = snackbar.view
        val textView = sbView.findViewById<TextView>(com.google.android.material.R.id.snackbar_text)
        textView.setTextColor(Color.WHITE)
        snackbar.show()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == IMG_REQUEST && resultCode == Activity.RESULT_OK && data != null) {
            var imgUri: Uri? = data.data
            var imageFeed = ImageDataDto()
            var compressedFile: File? = null
            try {
                val actualFile: File = FileUtil.from(context, imgUri)
                compressedFile = Compressor(context)
                    .setMaxWidth(640)
                    .setMaxHeight(480)
                    .setQuality(75)
                    .setCompressFormat(Bitmap.CompressFormat.JPEG)
                    .compressToFile(actualFile)
                val path: String = compressedFile.getPath()
                //imageSize = getDropboxIMGSize(Uri.fromFile(File(path)))
            } catch (e: FileNotFoundException) {
                e.printStackTrace()
            } catch (e: IOException) {
                e.printStackTrace()
            }
            imageFeed.imgUrlList = imgUri
            imageFeed.imagefile = compressedFile
            imageList.set(selectedposition, imageFeed)
            madapter?.notifyDataSetChanged()

        }
    }


    fun submitCountactUs(message: String) {
        if (progressDialog != null && !progressDialog?.isShowing()!!) {
            progressDialog?.show()
        }
        fileList.clear()
        for (i in 0 until imageList.size) {
            if (imageList.get(i).imagefile != null) {
                fileList.add(imageList.get(i).imagefile)
            }
        }

        val contactImagesParts: ArrayList<MultipartBody.Part> = ArrayList()
        for (x in 0 until fileList.size) {
            val requestFile = RequestBody.create(MediaType.parse("image/*"), fileList.get(x))
            val body =
                MultipartBody.Part.createFormData("imageFiles", fileList.get(x).name, requestFile)
            contactImagesParts.add(body)
        }
        val contactcall = apiInterface?.supportContactus(
            GetSet.getToken(),
            contactImagesParts,
            message,
            GetSet.getUserId()
        )
        contactcall?.enqueue(object : Callback<ContactUsDto> {
            override fun onFailure(call: Call<ContactUsDto>, t: Throwable) {
                call.cancel()
                if (progressDialog != null && progressDialog?.isShowing()!!)
                    progressDialog?.dismiss()

            }

            override fun onResponse(call: Call<ContactUsDto>, response: Response<ContactUsDto>) {
                if (progressDialog != null && progressDialog?.isShowing()!!)
                    progressDialog?.dismiss()

                var data: ContactUsDto? = response.body()
                if (data?.status!!)
                    activity?.finish()
            }

        })
    }
}

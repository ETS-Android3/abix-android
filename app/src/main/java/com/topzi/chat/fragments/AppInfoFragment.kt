package com.abix.chat.fragments

import android.os.Build
import android.os.Build.VERSION_CODES
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.topzi.chat.R
import kotlinx.android.synthetic.main.fragment_app_info.view.*
import java.lang.reflect.Field


class AppInfoFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val rootview = inflater.inflate(R.layout.fragment_app_info, container, false)
        intView(rootview)
        return rootview
    }

    fun intView(rootview: View) {
        val builder = StringBuilder()
        builder.append("android : ").append(Build.VERSION.RELEASE)

        val fields: Array<Field> = VERSION_CODES::class.java.fields
        for (field in fields) {
            val fieldName: String = field.getName()
            var fieldValue = -1
            try {
                fieldValue = field.getInt(Any())
            } catch (e: IllegalArgumentException) {
                e.printStackTrace()
            } catch (e: IllegalAccessException) {
                e.printStackTrace()
            } catch (e: NullPointerException) {
                e.printStackTrace()
            }
            if (fieldValue == Build.VERSION.SDK_INT) {
                builder.append(" : ").append(fieldName).append(" : ")
                builder.append("sdk=").append(fieldValue)
            }
        }
        rootview.tv_version.text = builder.toString()
        Log.d("LOG_TAG", "OS: $builder")
    }


}

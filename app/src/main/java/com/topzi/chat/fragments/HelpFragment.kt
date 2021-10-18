package com.abix.chat.fragments

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.topzi.chat.R
import com.topzi.chat.activity.BaseHelpActivity
import kotlinx.android.synthetic.main.fragment_help.view.*

class HelpFragment : Fragment(), View.OnClickListener {


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val rootview = inflater.inflate(R.layout.fragment_help, container, false)
        setListeners(rootview)
        return rootview
    }

    fun setListeners(view: View) {
        view.ll_faq.setOnClickListener(this)
        view.ll_contact_us.setOnClickListener(this)
        view.ll_privacy.setOnClickListener(this)
        view.ll_appinfo.setOnClickListener(this)
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.ll_faq -> {

            }
            R.id.ll_contact_us -> {
                val help = Intent(context, BaseHelpActivity::class.java)
                help.putExtra("fragment_tag", "tag_contactus")
                startActivity(help)
            }
            R.id.ll_privacy -> {

            }
            R.id.ll_appinfo -> {
                val help = Intent(context, BaseHelpActivity::class.java)
                help.putExtra("fragment_tag", "tag_appinfo")
                startActivity(help)
            }
        }
    }

}

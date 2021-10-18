package com.topzi.chat.activity

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.abix.chat.fragments.AppInfoFragment
import com.abix.chat.fragments.ContactUsFragment
import com.abix.chat.fragments.HelpFragment
import com.topzi.chat.R
import kotlinx.android.synthetic.main.activity_base_help.*
import kotlinx.android.synthetic.main.activity_toolbar.view.*

class BaseHelpActivity : AppCompatActivity() {
    val TAG_HELP = "tag_help"
    val TAG_CONTACTUS = "tag_contactus"
    val TAG_APPINFO = "tag_appinfo"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_base_help)
        actionbar.backbtn.setOnClickListener {
            onBackPressed()
        }
        val intent = intent
        var tag: String? = TAG_HELP
        tag = intent.getStringExtra("fragment_tag")
        val bundle = Bundle()
        updateFragment(tag, bundle)
    }

    fun updateFragment(tag: String, bundle: Bundle) {
        val fm = supportFragmentManager
        val ft = fm.beginTransaction()
        ft.setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out)
        var fragment: Fragment? = null
        when (tag) {
            TAG_HELP -> {
                fragment = HelpFragment()
            }
            TAG_CONTACTUS -> {
                fragment = ContactUsFragment()
            }
            TAG_APPINFO -> {
                actionbar.visibility = View.GONE
                fragment = AppInfoFragment()
            }
        }
        ft.replace(R.id.fl_Container, fragment!!, tag)
        ft.commit()
    }

    override fun onBackPressed() {
        super.onBackPressed()
    }

}

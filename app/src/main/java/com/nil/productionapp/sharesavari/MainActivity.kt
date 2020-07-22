package com.nil.productionapp.sharesavari

import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.LinearLayout
import com.nil.productionapp.sharesavari.SearchScreen.SearchFragment
import com.productionapp.amhimemekar.CommonUtils.Utils
import io.github.inflationx.viewpump.ViewPumpContextWrapper

class MainActivity : AppCompatActivity() {

    lateinit var frameContainer : FrameLayout
    lateinit var llSearch : LinearLayout

    override fun attachBaseContext(newBase: Context) {
        super.attachBaseContext(ViewPumpContextWrapper.wrap(newBase))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)


        frameContainer = findViewById<FrameLayout>(R.id.frame)
        llSearch = findViewById<LinearLayout>(R.id.llSearch)


        Utils.checkConnection(this@MainActivity,frameContainer)

        llSearch.setOnClickListener {
            loadSearchFrag(fragHome = SearchFragment())
        }

    }



    fun loadSearchFrag(fragHome : SearchFragment) {

        val fm = supportFragmentManager.beginTransaction()
        fm.setCustomAnimations(android.R.anim.slide_in_left,
            android.R.anim.slide_out_right)
        fm.replace(R.id.frame,fragHome)
        fm.commit()
    }
}
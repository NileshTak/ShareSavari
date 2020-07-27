package com.nil.productionapp.sharesavari

import android.Manifest
import android.content.Context
import android.os.Bundle
import android.view.View
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.github.florent37.runtimepermission.kotlin.askPermission
import com.nil.productionapp.sharesavari.CommonUtils.Utils
import com.nil.productionapp.sharesavari.HomeScreen.HomeScreen
import com.nil.productionapp.sharesavari.SearchScreen.SearchFragment
import io.github.inflationx.viewpump.ViewPumpContextWrapper
import kotlinx.android.synthetic.main.activity_main.*
import org.greenrobot.eventbus.EventBus

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
        askGalleryPermissionLocation()

        llSearch.setOnClickListener {
            loadSearchFrag(fragHome = SearchFragment())
        }

        llHome.setOnClickListener {
            loadHomeFrag(fragHome = HomeScreen())
        }

    }


    private fun askGalleryPermissionLocation() {
        askPermission(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ) {

        }.onDeclined { e ->
            if (e.hasDenied()) {
                //the list of denied permissions
                e.denied.forEach {
                }

                AlertDialog.Builder(this@MainActivity)
                    .setMessage("Please accept our permissions.. Otherwise you will not be able to use some of our Important Features.")
                    .setPositiveButton("yes") { _, _ ->
                        e.askAgain()
                    } //ask again
                    .setNegativeButton("no") { dialog, _ ->
                        dialog.dismiss()
                    }
                    .show()
            }

            if (e.hasForeverDenied()) {
                //the list of forever denied permissions, user has check 'never ask again'
                e.foreverDenied.forEach {
                }
                // you need to open setting manually if you really need it
                e.goToSettings();
            }
        }
    }




    fun loadSearchFrag(fragHome : SearchFragment) {
        llLogin.visibility = View.GONE
        val fm = supportFragmentManager.beginTransaction()

        fm.setCustomAnimations(android.R.anim.fade_in,
            android.R.anim.fade_out)

        changeIconColor(ivSearch,tvSearch,"Search")

        fm.replace(R.id.frame,fragHome)
        fm.commit()
    }



    fun loadHomeFrag(fragHome : HomeScreen) {
        llLogin.visibility = View.VISIBLE
        val fm = supportFragmentManager.beginTransaction()

        fm.setCustomAnimations(android.R.anim.fade_in,
            android.R.anim.fade_out)

        changeIconColor(ivHome,tvHome,"Home")

        fm.replace(R.id.frame,fragHome)
        fm.commit()
    }

    private fun changeIconColor(
        image: ImageView?,
        text: TextView?,
        s: String
    ) {
        image!!.setColorFilter(resources.getColor(R.color.colorPrimary))
        text!!.setTextColor(resources.getColor(R.color.colorPrimary))

        when(s) {
            "Search" -> {
                ivHome!!.setColorFilter(resources.getColor(R.color.bottomNavText))
                tvHome!!.setTextColor(resources.getColor(R.color.bottomNavText))
            }

            "Home" -> {
                ivSearch!!.setColorFilter(resources.getColor(R.color.bottomNavText))
                tvSearch!!.setTextColor(resources.getColor(R.color.bottomNavText))
            }
        }


    }
}
package com.ddsio.productionapp.sharesavari.HomeScreen

import android.Manifest
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.viewpager.widget.ViewPager
import com.ddsio.productionapp.sharesavari.CommonUtils.Utils
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.tabs.TabLayout
import com.ddsio.productionapp.sharesavari.InboxScreen.Fragments.MessagesFrag
import com.ddsio.productionapp.sharesavari.InboxScreen.Fragments.NotificationFrag
import com.ddsio.productionapp.sharesavari.R
import com.github.florent37.runtimepermission.kotlin.askPermission
import com.nilprojects.androiduidesign.Adapter.TabAdapter
import com.onesignal.OSSubscriptionObserver
import com.onesignal.OSSubscriptionStateChanges
import com.productionapp.amhimemekar.CommonUtils.Configure

class HomeScreenParent : Fragment() , OSSubscriptionObserver {

    lateinit var viewPagerInbox : ViewPager
    lateinit var tabLayoutInbox : TabLayout
    lateinit var adapter: TabAdapter
    var player_id = ""

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_homeparent_screen, container, false)

        tabLayoutInbox = view.findViewById<TabLayout>(R.id.tabLayoutHome)

        view.findViewById<TextView>(R.id.tvTitleToolHome).text = "Rides"

        viewPagerInbox = view.findViewById<ViewPager>(R.id.viewPagerHome)


        adapter = TabAdapter(activity!!.supportFragmentManager)
        adapter.addFragment(HomeScreen(), "OFFERED Rides" )
        adapter.addFragment(NotificationFrag(), "BOOKED Rides")

        viewPagerInbox.setAdapter(adapter);
        tabLayoutInbox.setupWithViewPager(viewPagerInbox);

        return view
    }

    override fun onOSSubscriptionChanged(stateChanges: OSSubscriptionStateChanges?) {
        if (!stateChanges!!.getFrom().getSubscribed() &&
            stateChanges.getTo().getSubscribed()) {

            Log.d("ONESIGNALIS",stateChanges.to.userId)
            player_id = stateChanges.to.userId
            Utils.writeStringToPreferences(Configure.PLAYER_ID, stateChanges.to.userId.toString(), activity)
        }
    }
}
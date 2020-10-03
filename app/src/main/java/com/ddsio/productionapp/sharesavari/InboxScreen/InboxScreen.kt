package com.ddsio.productionapp.sharesavari.InboxScreen

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.viewpager.widget.ViewPager
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.tabs.TabLayout
import com.ddsio.productionapp.sharesavari.InboxScreen.Fragments.MessagesFrag
import com.ddsio.productionapp.sharesavari.InboxScreen.Fragments.NotificationFrag
import com.ddsio.productionapp.sharesavari.R
import com.nilprojects.androiduidesign.Adapter.TabAdapter

class InboxScreen : Fragment() {

    lateinit var viewPagerInbox : ViewPager
    lateinit var tabLayoutInbox : TabLayout
    lateinit var adapter: TabAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_inbox_screen, container, false)

        tabLayoutInbox = view.findViewById<TabLayout>(R.id.tabLayoutInbox)

        viewPagerInbox = view.findViewById<ViewPager>(R.id.viewPagerInbox)


        adapter = TabAdapter(activity!!.supportFragmentManager)
        adapter.addFragment(MessagesFrag(), "MESSAGES" )
        adapter.addFragment(NotificationFrag(), "BOOKED Rides")

        viewPagerInbox.setAdapter(adapter);
        tabLayoutInbox.setupWithViewPager(viewPagerInbox);

        return view
    }

}
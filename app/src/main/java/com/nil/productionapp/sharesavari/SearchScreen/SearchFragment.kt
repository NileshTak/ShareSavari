package com.nil.productionapp.sharesavari.SearchScreen

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.core.app.ActivityOptionsCompat
import androidx.fragment.app.Fragment
import com.nil.productionapp.sharesavari.R
import com.nil.productionapp.sharesavari.ShowMap.ShowMapActivity
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import java.text.SimpleDateFormat
import java.util.*

class SearchFragment : Fragment() {

    lateinit var cvFromLocation : CardView
    lateinit var tvFromAdd : TextView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_search, container, false)

        cvFromLocation = view.findViewById(R.id.cvFromLocation)
        tvFromAdd = view.findViewById<TextView>(R.id.tvFromAdd)

        cvFromLocation.setOnClickListener {

            val bundle = ActivityOptionsCompat.makeCustomAnimation(
                activity!!.applicationContext,
               R.anim.slide_up,  R.anim.slide_down
            ).toBundle()
            startActivity(Intent(activity, ShowMapActivity::class.java), bundle)

        }

        return view
    }


    @Subscribe
    fun OnAddSelected(add : String?) {
        tvFromAdd.text = add
    }

    override fun onStart() {
        super.onStart()
        if (!EventBus.getDefault().isRegistered(this)) EventBus.getDefault().register(this)
    }

    override fun onDestroy() {
        super.onDestroy()
        EventBus.getDefault().unregister(this)
    }

}
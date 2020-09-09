package com.ddsio.productionapp.sharesavari.SearchScreen.child

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import com.bumptech.glide.Glide
import com.ddsio.productionapp.sharesavari.R
import com.productionapp.amhimemekar.CommonUtils.BookRidesPojoItem
import kotlinx.android.synthetic.main.activity_driver_profile.*
import kotlinx.android.synthetic.main.activity_driver_profile.ivCloseScreen
import kotlinx.android.synthetic.main.activity_ride_detail.*

class DriverProfile : AppCompatActivity() {

    lateinit var pojoWithData : BookRidesPojoItem

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_driver_profile)

        val bundle: Bundle? = intent.extras
        pojoWithData = bundle!!.get("pojoWithData") as BookRidesPojoItem


        ivCloseScreen.setOnClickListener {
            onBackPressed()
        }

        Log.d("hhjbh", pojoWithData.image.toString())

        if (pojoWithData.image != null ) {
            Glide.with(this).load(pojoWithData.image).into(cvProf)
        }

        tvName.text = pojoWithData.username

    }
}
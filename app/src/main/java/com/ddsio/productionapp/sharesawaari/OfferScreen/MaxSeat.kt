package com.ddsio.productionapp.sharesawaari.OfferScreen

import android.app.ProgressDialog
import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.RadioButton
import android.widget.Toast
import androidx.core.app.ActivityOptionsCompat
import com.ddsio.productionapp.sharesawaari.CommonUtils.Utils
import com.ddsio.productionapp.sharesawaari.R
import com.productionapp.amhimemekar.CommonUtils.BookRidesPojoItem
import com.productionapp.amhimemekar.CommonUtils.Configure
import com.productionapp.amhimemekar.CommonUtils.offerRideModel
import io.github.inflationx.viewpump.ViewPumpContextWrapper
import kotlinx.android.synthetic.main.activity_max_seat.*

class MaxSeat : AppCompatActivity() {

    lateinit var pojoWithData : offerRideModel
    lateinit var previousPojo : BookRidesPojoItem
    var LOGIN_TOKEN = ""
    lateinit var progressDialog: ProgressDialog
    var USER_UPDATE_ID = ""
    lateinit var USER_ID_KEY : String
    var maxSeatCount = ""
    var stoppointReturn = ""

    override fun attachBaseContext(newBase: Context) {
        super.attachBaseContext(ViewPumpContextWrapper.wrap(newBase))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_max_seat)

        LOGIN_TOKEN = Utils.getStringFromPreferences(Configure.LOGIN_KEY,"",this)!!
        USER_UPDATE_ID = Utils.getStringFromPreferences(Configure.USER_UPDATE_ID,"",this)!!
        USER_ID_KEY = Utils.getStringFromPreferences(Configure.USER_ID_KEY,"",this)!!

        val bundle: Bundle? = intent.extras
        pojoWithData = bundle!!.get("pojoWithData") as offerRideModel
        stoppointReturn = bundle!!.get("stoppointReturn") as String


        Log.d("jhhhhhhhhhjh",pojoWithData.date+"   "+pojoWithData.rdate+"   "+pojoWithData.brdate+"   "+pojoWithData.tddate)

        rbMax2SeatF.setOnClickListener {
            radio_button_click_seat(rbMax2SeatF)
        }

        ivBacR.setOnClickListener {
            onBackPressed()
        }

        rbMax3SeatF.setOnClickListener {
            radio_button_click_seat(rbMax3SeatF)
        }

        fabNextMax.setOnClickListener {
            checkFields()
        }

    }

    fun radio_button_click_seat(view: View){
        // Get the clicked radio button instance
        val radio: RadioButton = findViewById(radio_groupSeatF.checkedRadioButtonId)
        maxSeatCount = radio.text.toString()
    }


    fun checkFields() {
        if (maxSeatCount.isEmpty() ||maxSeatCount == "") {
            Toast.makeText(this,"Please select valid Back Seat count ",
                Toast.LENGTH_LONG).show()
        } else {

            if (maxSeatCount == "Max 2 seat in back") {
                pojoWithData.max_back_2 = true
                pojoWithData.max_back_3 = false
            } else {
                pojoWithData.max_back_3 = true
                pojoWithData.max_back_2 = false
            }

            var int = Intent(this,
                PassengerCount::class.java)
            val bundle =
                ActivityOptionsCompat.makeCustomAnimation(
                    this ,
                    R.anim.fade_in, R.anim.fade_out
                ).toBundle()
            int.putExtra("pojoWithData",pojoWithData)
            int.putExtra("stoppointReturn", stoppointReturn)
            startActivity(int,bundle)
        }
    }
}
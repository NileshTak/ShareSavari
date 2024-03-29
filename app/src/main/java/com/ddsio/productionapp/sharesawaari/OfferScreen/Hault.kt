package com.ddsio.productionapp.sharesawaari.OfferScreen

import android.app.ProgressDialog
import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.core.app.ActivityOptionsCompat
import com.ddsio.productionapp.sharesawaari.CommonUtils.Utils
import com.ddsio.productionapp.sharesawaari.R
import com.productionapp.amhimemekar.CommonUtils.BookRidesPojoItem
import com.productionapp.amhimemekar.CommonUtils.Configure
import com.productionapp.amhimemekar.CommonUtils.offerRideModel
import io.github.inflationx.viewpump.ViewPumpContextWrapper
import kotlinx.android.synthetic.main.activity_hault.*

class Hault : AppCompatActivity() {

    lateinit var pojoWithData : offerRideModel
    lateinit var previousPojo : BookRidesPojoItem
    var LOGIN_TOKEN = ""

    lateinit var progressDialog: ProgressDialog
    var USER_UPDATE_ID = ""
    lateinit var USER_ID_KEY : String
    var stoppointReturn = ""


    override fun attachBaseContext(newBase: Context) {
        super.attachBaseContext(ViewPumpContextWrapper.wrap(newBase))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_hault)

        LOGIN_TOKEN = Utils.getStringFromPreferences(Configure.LOGIN_KEY,"",this)!!
        USER_UPDATE_ID = Utils.getStringFromPreferences(Configure.USER_UPDATE_ID,"",this)!!
        USER_ID_KEY = Utils.getStringFromPreferences(Configure.USER_ID_KEY,"",this)!!

        val bundle: Bundle? = intent.extras
        pojoWithData = bundle!!.get("pojoWithData") as offerRideModel
        stoppointReturn = bundle!!.get("stoppointReturn") as String

        ivBacH.setOnClickListener {
            onBackPressed()
        }

        fabNextH.setOnClickListener {
            checkFields()
        }
    }


    fun checkFields() {
        if (etCar.text.toString().isEmpty() || etCar.text.toString() == "") {
            Toast.makeText(this,"Please enter valid Car Name ",
                Toast.LENGTH_LONG).show()
        } else if (etCarColor.text.toString().isEmpty() || etCarColor.text.toString() == "") {
            Toast.makeText(this,"Please enter valid car color ",
                Toast.LENGTH_LONG).show()
        }   else  {

            pojoWithData.carname = etCar.text.toString()
            pojoWithData.carcolor = etCarColor.text.toString()


            var int = Intent(this,
                SmokingandBooking::class.java)
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
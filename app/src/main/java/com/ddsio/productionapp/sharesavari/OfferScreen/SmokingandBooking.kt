package com.ddsio.productionapp.sharesavari.OfferScreen

import android.app.ProgressDialog
import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.RadioButton
import android.widget.Toast
import androidx.core.app.ActivityOptionsCompat
import com.ddsio.productionapp.sharesavari.CommonUtils.Utils
import com.ddsio.productionapp.sharesavari.R
import com.productionapp.amhimemekar.CommonUtils.BookRidesPojoItem
import com.productionapp.amhimemekar.CommonUtils.Configure
import com.productionapp.amhimemekar.CommonUtils.offerRideModel
import io.github.inflationx.viewpump.ViewPumpContextWrapper
import kotlinx.android.synthetic.main.activity_hault.*
import kotlinx.android.synthetic.main.activity_hault.ivBacH
import kotlinx.android.synthetic.main.activity_smokingand_booking.*

class SmokingandBooking : AppCompatActivity() {


    lateinit var pojoWithData : offerRideModel
    var LOGIN_TOKEN = ""

    var cbBookInstant = ""
    lateinit var progressDialog: ProgressDialog
    var USER_UPDATE_ID = ""
    lateinit var USER_ID_KEY : String


    override fun attachBaseContext(newBase: Context) {
        super.attachBaseContext(ViewPumpContextWrapper.wrap(newBase))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_smokingand_booking)

        LOGIN_TOKEN = Utils.getStringFromPreferences(Configure.LOGIN_KEY,"",this)!!
        USER_UPDATE_ID = Utils.getStringFromPreferences(Configure.USER_UPDATE_ID,"",this)!!
        USER_ID_KEY = Utils.getStringFromPreferences(Configure.USER_ID_KEY,"",this)!!

        val bundle: Bundle? = intent.extras
        pojoWithData = bundle!!.get("pojoWithData") as offerRideModel

        rbBookInstantly.setOnClickListener {
            radio_button_click(rbBookInstantly)
        }

        rbMyself.setOnClickListener {
            radio_button_click(rbMyself)
        }


        ivBacS.setOnClickListener {
            onBackPressed()
        }

        fabNextS.setOnClickListener {
            checkFields()
        }
    }

    fun radio_button_click(view: View){
        // Get the clicked radio button instance
        val radio: RadioButton = findViewById(radio_group.checkedRadioButtonId)
        cbBookInstant = radio.text.toString()
    }


    fun checkFields() {
        if (cbBookInstant.isEmpty() ||cbBookInstant == "") {
            Toast.makeText(this,"Please select valid Booking Process ",
                Toast.LENGTH_LONG).show()
        } else  {

            if (cbPets.isChecked) {
                pojoWithData.pets = true
            } else {
                pojoWithData.pets = false
            }

            if (cbSmoking.isChecked) {
                pojoWithData.smoking = true
            } else {
                pojoWithData.smoking = false
            }

            if (cbBookInstant == "Book Instantly") {
                pojoWithData.is_direct = true
            } else {
                pojoWithData.is_direct = false
            }

            var int = Intent(this,
                NumberOfPassenersToTake::class.java)
            val bundle =
                ActivityOptionsCompat.makeCustomAnimation(
                    this ,
                    R.anim.fade_in, R.anim.fade_out
                ).toBundle()
            int.putExtra("pojoWithData",pojoWithData)
            startActivity(int,bundle)
        }
    }

}
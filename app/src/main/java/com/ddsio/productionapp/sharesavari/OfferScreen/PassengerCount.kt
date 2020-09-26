package com.ddsio.productionapp.sharesavari.OfferScreen

import android.Manifest
import android.app.ProgressDialog
import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.cardview.widget.CardView
import androidx.core.app.ActivityOptionsCompat
import com.ddsio.productionapp.sharesavari.CommonUtils.Utils
import com.ddsio.productionapp.sharesavari.R
import com.github.florent37.runtimepermission.kotlin.askPermission
import com.productionapp.amhimemekar.CommonUtils.BookRidesPojoItem
import com.productionapp.amhimemekar.CommonUtils.Configure
import com.productionapp.amhimemekar.CommonUtils.offerRideModel
import io.github.inflationx.viewpump.ViewPumpContextWrapper
import kotlinx.android.synthetic.main.activity_max_seat.*
import kotlinx.android.synthetic.main.activity_max_seat.fabNextMax
import kotlinx.android.synthetic.main.activity_passenger_count.*

class PassengerCount : AppCompatActivity() {

    lateinit var pojoWithData : offerRideModel
    lateinit var previousPojo : BookRidesPojoItem
    var LOGIN_TOKEN = ""
    var count = 1
    lateinit var progressDialog: ProgressDialog
    var USER_UPDATE_ID = ""
    lateinit var USER_ID_KEY : String
    var totalAmt = 0

    override fun attachBaseContext(newBase: Context) {
        super.attachBaseContext(ViewPumpContextWrapper.wrap(newBase))
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_passenger_count)
        var cv = findViewById<CardView>(R.id.cvMinus)

        LOGIN_TOKEN = Utils.getStringFromPreferences(Configure.LOGIN_KEY,"",this)!!
        USER_UPDATE_ID = Utils.getStringFromPreferences(Configure.USER_UPDATE_ID,"",this)!!
        USER_ID_KEY = Utils.getStringFromPreferences(Configure.USER_ID_KEY,"",this)!!

        val bundle: Bundle? = intent.extras
        pojoWithData = bundle!!.get("pojoWithData") as offerRideModel

        askGalleryPermissionLocation()
        Utils.checkConnection(this@PassengerCount,cv)
        if (!Utils.CheckGpsStatus(this@PassengerCount)) {
            Utils.enableGPS(this@PassengerCount)
        }

        tvTotalPrice.text =  "0₹"

        tvCount.setText(count.toString())

        ivBaP.setOnClickListener {
            onBackPressed()
        }


        cvMinus.setOnClickListener {
            if (count != 0) {
                count--
                tvCount.setText(count.toString())

                if (etPrice.text.toString() != null && etPrice.text.toString() != "") {
                    totalAmt = etPrice.text.toString().toInt() * tvCount.text.toString().toInt()
                    tvTotalPrice.text = totalAmt.toString() + "₹"
                }

            }
        }


        cvAdd.setOnClickListener {
            count++
            tvCount.setText(count.toString())

            if (etPrice.text.toString() != null && etPrice.text.toString() != "") {
                totalAmt = etPrice.text.toString().toInt() * tvCount.text.toString().toInt()
                tvTotalPrice.text = totalAmt.toString() + "₹"
            }
        }

        etPrice.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                if (etPrice.text.toString() != null && etPrice.text.toString() != "") {
                    totalAmt = etPrice.text.toString().toInt() * tvCount.text.toString().toInt()
                    tvTotalPrice.text = totalAmt.toString() + "₹"
                }
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if (etPrice.text.toString() != null && etPrice.text.toString() != "") {
                    totalAmt = etPrice.text.toString().toInt() * tvCount.text.toString().toInt()
                    tvTotalPrice.text = totalAmt.toString() + "₹"
                }

            }

        })

        fabNextP.setOnClickListener {
            checkFields()
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

                AlertDialog.Builder(this)
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

    fun checkFields() {
        if (etPrice.text.toString().isEmpty() || etPrice.text.toString() == "") {
            Toast.makeText(this,"Please Select Correct Riding Price for per Passenger ",
                Toast.LENGTH_LONG).show()
        } else {

            pojoWithData.price = etPrice.text.toString()
            pojoWithData.passenger = tvCount.text.toString()


            var int = Intent(this,
                Hault::class.java)
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
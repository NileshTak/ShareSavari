package com.ddsio.productionapp.sharesavari.OfferScreen

import android.Manifest
import android.app.ProgressDialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.RadioButton
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.core.app.ActivityOptionsCompat
import com.android.volley.*
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.ddsio.productionapp.sharesavari.CommonUtils.Utils
import com.ddsio.productionapp.sharesavari.MainActivity
import com.ddsio.productionapp.sharesavari.R
import com.ddsio.productionapp.sharesavari.ShowMap.ShowMapActivity
import com.github.florent37.runtimepermission.kotlin.askPermission
import com.productionapp.amhimemekar.CommonUtils.BookRideScreenFetchCity
import com.productionapp.amhimemekar.CommonUtils.Configure
import com.productionapp.amhimemekar.CommonUtils.offerRideModel
import io.github.inflationx.viewpump.ViewPumpContextWrapper
import kotlinx.android.synthetic.main.activity_going_date_and_time.*
import kotlinx.android.synthetic.main.activity_number_of_passeners_to_take.*
import kotlinx.android.synthetic.main.fragment_search.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe

class NumberOfPassenersToTake : AppCompatActivity() {

    var count = 1
    var request: RequestQueue? = null
    lateinit var pojoWithData : offerRideModel
    var totalAmt = 0

    var LOGIN_TOKEN = ""
    lateinit var progressDialog: ProgressDialog

    override fun attachBaseContext(newBase: Context) {
        super.attachBaseContext(ViewPumpContextWrapper.wrap(newBase))
    }


    var cbBookInstant = ""


    var maxSeatCount = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_number_of_passeners_to_take)
        var cv = findViewById<CardView>(R.id.cvMinus)



        LOGIN_TOKEN = Utils.getStringFromPreferences(Configure.LOGIN_KEY,"",this)!!
        val bundle: Bundle? = intent.extras
        pojoWithData = bundle!!.get("pojoWithData") as offerRideModel
        request= Volley.newRequestQueue(this);


//        cvStopPoint.setOnClickListener {
//            val intent =  (Intent(this, ShowMapActivity::class.java))
//            intent.putExtra("typeis","stop")
//            startActivity(intent)
//        }

        rbBookInstantly.setOnClickListener {
            radio_button_click(rbBookInstantly)
        }

        rbMyself.setOnClickListener {
            radio_button_click(rbMyself)
        }




        rbMax2Seat.setOnClickListener {
            radio_button_click_seat(rbBookInstantly)
        }

        rbMax3Seat.setOnClickListener {
            radio_button_click_seat(rbMyself)
        }


        askGalleryPermissionLocation()
            Utils.checkConnection(this@NumberOfPassenersToTake,cv)
            if (!Utils.CheckGpsStatus(this@NumberOfPassenersToTake)) {
                Utils.enableGPS(this@NumberOfPassenersToTake)
            }



        tvTotalPrice.text =  "0₹"

        tvCount.setText(count.toString())


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


        ivBacknumber.setOnClickListener {
            onBackPressed()
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

        fabFinish.setOnClickListener {

          checkFields()
        }
    }

    @Subscribe
    fun OnAddSelected(add : BookRideScreenFetchCity?) {

        Log.d("CITYIS",add!!.city.toString() +"jubhjbjbj")
        if (add!!.type == "stop") {
            tvStop.text = add!!.city
            pojoWithData.stitle = add!!.city
            pojoWithData.slat = add.lat
            pojoWithData.slog = add.long

        }
    }


    fun radio_button_click(view: View){
        // Get the clicked radio button instance
        val radio: RadioButton = findViewById(radio_group.checkedRadioButtonId)

        cbBookInstant = radio.text.toString()

    }


    fun radio_button_click_seat(view: View){
        // Get the clicked radio button instance
        val radio: RadioButton = findViewById(radio_groupSeat.checkedRadioButtonId)

        maxSeatCount = radio.text.toString()

    }


    override fun onStart() {
        super.onStart()
        if (!EventBus.getDefault().isRegistered(this@NumberOfPassenersToTake)) EventBus.getDefault().register(this)
    }

    override fun onDestroy() {
        super.onDestroy()
        EventBus.getDefault().unregister(this@NumberOfPassenersToTake)
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


    private fun checkFields() {

        if (etPrice.text.toString().isEmpty() || etPrice.text.toString() == "") {
            Toast.makeText(this,"Please Select Correct Riding Price for per Passenger ",
                Toast.LENGTH_LONG).show()
        } else if (etCar.text.toString().isEmpty() || etCar.text.toString() == "") {
            Toast.makeText(this,"Please enter valid Car Name ",
                Toast.LENGTH_LONG).show()
        } else if (cbBookInstant.isEmpty() ||cbBookInstant == "") {
            Toast.makeText(this,"Please select valid Booking Process ",
                Toast.LENGTH_LONG).show()
        }else if (maxSeatCount.isEmpty() ||maxSeatCount == "") {
            Toast.makeText(this,"Please select valid Back Seat count ",
                Toast.LENGTH_LONG).show()
        } else if (etCarColor.text.toString().isEmpty() || etCarColor.text.toString() == "") {
            Toast.makeText(this,"Please enter valid car color ",
                Toast.LENGTH_LONG).show()
        } else {
            pojoWithData.price = etPrice.text.toString()
            pojoWithData.passenger = tvCount.text.toString()
            pojoWithData.carname = etCar.text.toString()
            pojoWithData.slog = "0"
            pojoWithData.slat = "0"
            pojoWithData.carcolor = etCarColor.text.toString()
                pojoWithData.stitle = etStopPoint.text.toString()

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


            hitOfferRideAPI()
        }
    }




    private fun hitOfferRideAPI() {

        if (cbBookInstant == "Book Instantly") {
            pojoWithData.is_direct = true
        } else {
            pojoWithData.is_direct = false
        }



        if (maxSeatCount == "Max 2 seat in back") {
            pojoWithData.max_back_2 = true
            pojoWithData.max_back_3 = false
        } else {
            pojoWithData.max_back_3 = true
            pojoWithData.max_back_2 = false
        }


        progressDialog = ProgressDialog(this)
        progressDialog.setMessage("Wait a Sec....Creating your Ride")
        progressDialog.setCancelable(false)
        progressDialog.show()

        pojoWithData.id =  ""
        pojoWithData.username =  ""
        pojoWithData.url =  ""
        pojoWithData.image =  ""
        pojoWithData.comment =  ""

//        if ( pojoWithData.rdate == null || pojoWithData.rdate!!.isEmpty()) {
//            pojoWithData.is_return = "false"
//        } else {
//            pojoWithData.is_return = "true"
//        }

        val url = Configure.BASE_URL + Configure.OFFER_RIDE_URL
        Log.d("jukjbkj", url.toString())

        val jsonObjRequest: StringRequest = object : StringRequest(
            Method.POST,
            url,
            object : Response.Listener<String?> {
                override fun onResponse(response: String?) {
                    Log.d("jukjbkj", response.toString())
                    Toast.makeText(this@NumberOfPassenersToTake,"Ride Offered Successfully...",
                        Toast.LENGTH_LONG).show()
                    val mainActivity =
                        Intent(applicationContext, MainActivity::class.java)

                    mainActivity.putExtra("type", "RideBooked")
                    progressDialog.dismiss()
                    startActivity(mainActivity)
                    finish()



                }
            }, object : Response.ErrorListener {
                override fun onErrorResponse(error: VolleyError) {
                    VolleyLog.d("volley", "Error: " + error.message)
                    error.printStackTrace()
                    Log.e("jukjbkj",  "Error: " + error.message)

                    Toast.makeText(this@NumberOfPassenersToTake,"Something Went Wrong ! Please try after some time",
                        Toast.LENGTH_LONG).show()

                    progressDialog.dismiss()
                }
            }) {


            override fun getHeaders(): MutableMap<String, String> {

                Log.d("jukjbkj", LOGIN_TOKEN.toString())

                var params = java.util.HashMap<String, String>()
//                params.put("Content-Type", "application/json; charset=UTF-8");
                params.put("Authorization", "Token "+LOGIN_TOKEN!!);
                return params;
            }

            @Throws(AuthFailureError::class)
            override fun getParams(): Map<String, String> {
                val params: MutableMap<String, String> =
                    HashMap()
                params["id"] =  pojoWithData.id.toString()
                params["url"] = pojoWithData.url.toString()
                params["username"] = pojoWithData.username.toString()
                params["user"] =  pojoWithData.user.toString()
                params["image"] = pojoWithData.image.toString()
                params["leaving"] = pojoWithData.leaving.toString()
                params["lline"] =  pojoWithData.lline.toString()
                params["lcity"] = pojoWithData.lcity.toString()
                params["llat"] = pojoWithData.llat.toString()
                params["llog"] =  pojoWithData.llog.toString()
                params["going"] = pojoWithData.going.toString()
                params["glog"] = pojoWithData.glog.toString()
                params["glat"] = pojoWithData.glat.toString()
                params["gcity"] = pojoWithData.gcity.toString()
                params["gline"] = pojoWithData.gline.toString()
                params["date"] =  pojoWithData.date.toString()
                params["time"] = pojoWithData.time.toString()
                params["rdate"] = pojoWithData.rdate.toString()
                params["rtime"] =  pojoWithData.rtime.toString()
                params["price"] = pojoWithData.price.toString()
                params["passenger"] = pojoWithData.passenger.toString()
                params["comment"] = pojoWithData.comment.toString()
                params["is_return"] = pojoWithData.is_return.toString()
                params["tddate"] = pojoWithData.tddate.toString()
                params["tdtime"] = pojoWithData.tdtime.toString()
                params["is_direct"] = pojoWithData.is_direct.toString()
                params["carname"] = pojoWithData.carname.toString()
                params["carcolor"] = pojoWithData.carcolor.toString()
                params["stitle"] = pojoWithData.stitle.toString()
                params["slat"] = pojoWithData.slat.toString()
                params["slog"] = pojoWithData.slog.toString()
                params["pets"] = pojoWithData.pets.toString()
                params["smoking"] = pojoWithData.smoking.toString()
                params["max_back_2"] = pojoWithData.max_back_2.toString()
                params["max_back_3"] = pojoWithData.max_back_3.toString()
                return params
            }
        }
        request!!.add(jsonObjRequest)

    }

}
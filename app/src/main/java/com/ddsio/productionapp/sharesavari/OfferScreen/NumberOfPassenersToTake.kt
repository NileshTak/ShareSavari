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
import com.ddsio.productionapp.sharesavari.HomeScreen.Child.EditRide
import com.ddsio.productionapp.sharesavari.MainActivity
import com.ddsio.productionapp.sharesavari.R
import com.ddsio.productionapp.sharesavari.ShowMap.ShowMapActivity
import com.github.florent37.runtimepermission.kotlin.askPermission
import com.google.gson.Gson
import com.productionapp.amhimemekar.CommonUtils.*
import com.productionapp.amhimemekar.CommonUtils.Configure.BASE_URL
import com.productionapp.amhimemekar.CommonUtils.Configure.GET_USER_DETAILS
import io.github.inflationx.viewpump.ViewPumpContextWrapper
import kotlinx.android.synthetic.main.activity_going_date_and_time.*
import kotlinx.android.synthetic.main.activity_number_of_passeners_to_take.*
import kotlinx.android.synthetic.main.fragment_search.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe

class NumberOfPassenersToTake : AppCompatActivity() {


    var request: RequestQueue? = null
    lateinit var pojoWithData : offerRideModel
    lateinit var previousPojo : BookRidesPojoItem


    var LOGIN_TOKEN = ""
    lateinit var progressDialog: ProgressDialog

    override fun attachBaseContext(newBase: Context) {
        super.attachBaseContext(ViewPumpContextWrapper.wrap(newBase))
    }

    var USER_UPDATE_ID = ""
    lateinit var USER_ID_KEY : String
//    var maxSeatCount = ""

//    var pets = ""
//    var smoking = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_number_of_passeners_to_take)


        LOGIN_TOKEN = Utils.getStringFromPreferences(Configure.LOGIN_KEY,"",this)!!
        USER_UPDATE_ID = Utils.getStringFromPreferences(Configure.USER_UPDATE_ID,"",this)!!
        USER_ID_KEY = Utils.getStringFromPreferences(Configure.USER_ID_KEY,"",this)!!
        val bundle: Bundle? = intent.extras
        pojoWithData = bundle!!.get("pojoWithData") as offerRideModel
        request= Volley.newRequestQueue(this);
        previousPojo = BookRidesPojoItem()

//        cvStopPoint.setOnClickListener {
//            val intent =  (Intent(this, ShowMapActivity::class.java))
//            intent.putExtra("typeis","stop")
//            startActivity(intent)
//        }

        cvRecheck.setOnClickListener {
            checkFieldsRecheck()
        }

        askGalleryPermissionLocation()
            Utils.checkConnection(this@NumberOfPassenersToTake,cvRecheck)
            if (!Utils.CheckGpsStatus(this@NumberOfPassenersToTake)) {
                Utils.enableGPS(this@NumberOfPassenersToTake)
            }


        ivBacknumber.setOnClickListener {
            onBackPressed()
        }

        fabFinish.setOnClickListener {

          checkFields()
        }
//        getUserData()
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

            pojoWithData.comment = etComment.text.toString()

            hitOfferRideAPI()

    }


    private fun checkFieldsRecheck() {

            pojoWithData.comment = etComment.text.toString()


         moveToRecheck()

    }


    private fun moveToRecheck() {


//        if (maxSeatCount == "Max 2 seat in back") {
//            pojoWithData.max_back_2 = true
//            pojoWithData.max_back_3 = false
//        } else {
//            pojoWithData.max_back_3 = true
//            pojoWithData.max_back_2 = false
//        }

        pojoWithData.id =  ""
        pojoWithData.username =  ""
        pojoWithData.url =  ""
        pojoWithData.image =  ""
        pojoWithData.comment =  etComment.text.toString()


        previousPojo.comment = pojoWithData.comment
        previousPojo.date= pojoWithData.date
        previousPojo.gcity=  pojoWithData.gcity
        previousPojo.glat= pojoWithData.glat
        previousPojo.gline= pojoWithData.gline
        previousPojo.glog = pojoWithData.glog
        previousPojo.going= pojoWithData.going
        previousPojo.id = ""
        previousPojo.image= pojoWithData.image
        previousPojo.is_return= pojoWithData.is_return!!.toBoolean()
        previousPojo.lcity= pojoWithData.lcity
        previousPojo.leaving= pojoWithData.leaving
        previousPojo.llat= pojoWithData.llat
        previousPojo.lline= pojoWithData.lline
        previousPojo.llog= pojoWithData.llog
        previousPojo.passenger=  pojoWithData.passenger
        previousPojo.price= pojoWithData.price!!.toInt()
        previousPojo.rdate= pojoWithData.rdate
        previousPojo.rtime= pojoWithData.rtime
        previousPojo.time=  pojoWithData.time
        previousPojo.url= pojoWithData.url
        previousPojo.user=  pojoWithData.user!!.toInt()
        previousPojo.username=  pojoWithData.username
        previousPojo.tddate= pojoWithData.tddate
        previousPojo.tdtime= pojoWithData.tdtime
        previousPojo.is_direct=   pojoWithData.is_direct
        previousPojo.carcolor=   pojoWithData.carcolor
        previousPojo.carname=   pojoWithData.carname
        previousPojo.stitle=   pojoWithData.stitle
        previousPojo.pets=   pojoWithData.pets
        previousPojo.smoking=   pojoWithData.smoking
        previousPojo.max_back_2=   pojoWithData.max_back_2
        previousPojo.max_back_3=   pojoWithData.max_back_3
        previousPojo.brtime=   pojoWithData.brtime
        previousPojo.brdate = pojoWithData.brdate


        var int = Intent(this,
            EditRide::class.java)
        val bundle =
            ActivityOptionsCompat.makeCustomAnimation(
                this ,
                R.anim.fade_in, R.anim.fade_out
            ).toBundle()
        int.putExtra("pojoWithData",previousPojo)
        startActivity(int,bundle)
    }


    private fun hitOfferRideAPI() {

        progressDialog = ProgressDialog(this)
        progressDialog.setMessage("Wait a Sec....Creating your Ride")
        progressDialog.setCancelable(false)
        progressDialog.show()

        pojoWithData.id =  ""
        pojoWithData.username =  ""
        pojoWithData.url =  ""
        pojoWithData.image =  ""
        pojoWithData.comment =  ""

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
                    progressDialog.dismiss()
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

                    progressDialog.dismiss()

                    Toast.makeText(this@NumberOfPassenersToTake,"Something Went Wrong ! Please try after some time",
                        Toast.LENGTH_LONG).show()

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
                params["comment"] = etComment.text.toString()
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
                params["brdate"] = pojoWithData.brdate.toString()
                params["brtime"] = pojoWithData.brtime.toString()
                return params
            }
        }
        request!!.add(jsonObjRequest)

    }

}
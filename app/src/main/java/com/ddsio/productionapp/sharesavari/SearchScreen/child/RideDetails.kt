package com.ddsio.productionapp.sharesavari.SearchScreen.child

import android.Manifest
import android.annotation.SuppressLint
import android.app.ProgressDialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.location.LocationListener
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.View
import android.view.animation.AnimationUtils
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.app.ActivityOptionsCompat
import com.android.volley.*
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.bumptech.glide.Glide
import com.ddsio.productionapp.sharesavari.CommonUtils.Utils
import com.ddsio.productionapp.sharesavari.HomeScreen.Child.EditRide
import com.ddsio.productionapp.sharesavari.InboxScreen.Child.ChatLogActivity
import com.ddsio.productionapp.sharesavari.InboxScreen.Child.NewMessageActivity
import com.ddsio.productionapp.sharesavari.Intro.IntroActivity
import com.ddsio.productionapp.sharesavari.MainActivity
import com.ddsio.productionapp.sharesavari.R
import com.github.florent37.runtimepermission.kotlin.askPermission
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.MapView
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.Task
import com.google.firebase.database.FirebaseDatabase
import com.google.gson.Gson
import com.letsbuildthatapp.kotlinmessenger.models.ChatMessage
import com.letsbuildthatapp.kotlinmessenger.models.User
import com.productionapp.amhimemekar.CommonUtils.*
import com.productionapp.amhimemekar.CommonUtils.Configure.BASE_URL
import com.productionapp.amhimemekar.CommonUtils.Configure.RATING
import io.github.inflationx.viewpump.ViewPumpContextWrapper
import kotlinx.android.synthetic.main.activity_chat_log.*
import kotlinx.android.synthetic.main.activity_driver_profile.*
import kotlinx.android.synthetic.main.activity_ride_detail.*
import kotlinx.android.synthetic.main.activity_ride_detail.ivCloseScreen
import kotlinx.android.synthetic.main.activity_show_map.*
import kotlinx.android.synthetic.main.custom_show_list_rides.view.*
import java.io.IOException
import java.util.*
import kotlin.collections.ArrayList

class RideDetails : AppCompatActivity(), OnMapReadyCallback,
    com.google.android.gms.location.LocationListener, GoogleMap.OnCameraMoveListener, GoogleMap.OnCameraMoveStartedListener, GoogleMap.OnCameraIdleListener {

    lateinit var progressDialog: ProgressDialog
    var LOGIN_TOKEN = ""
    var USER_UPDATE_ID = ""
    lateinit var USER_ID_KEY : String


    var lat = 0.0
    var log = 0.0

    var request: RequestQueue? = null


    private var fusedLocationProviderClient: FusedLocationProviderClient? = null
    private var mMap: GoogleMap? = null
    private val DEFAULT_ZOOM = 15f

    lateinit var mapView: MapView

    private val MAP_VIEW_BUNDLE_KEY = "MapViewBundleKey"

    lateinit var pojoWithData : BookRidesPojoItem

    var IDToCancel = "0"


    override fun onMapReady(googleMap: GoogleMap) {

        mapView.onResume()

        mMap = googleMap
        getCurrentLocation()
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }
        mMap!!.setMyLocationEnabled(true)
        //        mMap.setMapStyle( MapStyleOptions.loadRawResourceStyle(getActivity(), R.raw.style));

        mMap!!.setOnCameraMoveListener(this)
        mMap!!.setOnCameraMoveStartedListener(this)
        mMap!!.setOnCameraIdleListener(this)

    }

    override fun attachBaseContext(newBase: Context) {
        super.attachBaseContext(ViewPumpContextWrapper.wrap(newBase))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_ride_detail)

        val bundle: Bundle? = intent.extras
        pojoWithData = bundle!!.get("pojoWithData") as BookRidesPojoItem
        var screen  = bundle!!.get("screen") as String
        IDToCancel  = bundle!!.get("IDToCancel") as String

        if (screen == "home") {
            rlBottom.visibility = View.GONE
            rlBottomDelete.visibility = View.VISIBLE
            rlBottomEdit.visibility = View.VISIBLE
            rlBottomCancel.visibility = View.GONE
        } else if (screen == "Booked"){
            rlBottom.visibility = View.GONE
            rlBottomCancel.visibility = View.VISIBLE
            rlBottomDelete.visibility = View.GONE
            rlBottomEdit.visibility = View.GONE
        } else {
            rlBottomDelete.visibility = View.GONE
            rlBottomEdit.visibility = View.GONE
            rlBottom.visibility = View.VISIBLE
            rlBottomCancel.visibility = View.GONE
        }

        LOGIN_TOKEN = Utils.getStringFromPreferences(Configure.LOGIN_KEY,"",this)!!
        USER_UPDATE_ID = Utils.getStringFromPreferences(Configure.USER_UPDATE_ID,"",this)!!
        USER_ID_KEY = Utils.getStringFromPreferences(Configure.USER_ID_KEY,"",this)!!
        request= Volley.newRequestQueue(this)

        askGalleryPermissionLocation()

       tvFromAdd.text = pojoWithData.lline+", "+pojoWithData.lcity
     tvToAdd.text = pojoWithData.gline+", "+pojoWithData.gcity


        var dateFormat = pojoWithData.date
        val separated =
            dateFormat!!.split("-".toRegex()).toTypedArray()
        var yearFrom = separated[0]
        var monthFrom =  separated[1]
        var dateFrom =  separated[2]
        var dateFFRom =  dateFrom +"-" +monthFrom +"-"+yearFrom
        tvDate.text = dateFFRom
        tvLTime.text = dateFFRom +" " +
                "(${pojoWithData.time})"



        tvFromFullAdd.text = "("+ pojoWithData.leaving+")"
        tvToFullAdd.text = "("+ pojoWithData.going+")"
        tvCarColor.text = "("+ pojoWithData.carcolor+")"
        tvCar.text = pojoWithData.carname
        if (pojoWithData.stitle == null || pojoWithData.stitle == "") {
            llStopPoint.visibility = View.GONE
        } else {
            tvStopPoint.text = pojoWithData.stitle
        }


        if (pojoWithData.max_back_2 == true) {
            tvBackSeat.text = "Max. 2 in the back seats."
        } else {
            tvBackSeat.text = "Max. 3 in the back seats."
        }


        if (pojoWithData.pets == true) {
            llPets.visibility = View.VISIBLE
        } else {
            llPets.visibility = View.GONE
        }



        if (pojoWithData.smoking == true) {
            llSmoking.visibility = View.VISIBLE
        } else {

            llSmoking.visibility = View.GONE
        }



        if (pojoWithData.is_direct == true) {
            llAutoApp.visibility = View.VISIBLE
        } else {
            llAutoApp.visibility = View.GONE
        }

        Log.d("jknj",pojoWithData.is_return.toString())




        var dateRFormat = pojoWithData.tddate
        val separatedR =
            dateRFormat!!.split("-".toRegex()).toTypedArray()
        var yearTo = separatedR[0]
        var monthTo =  separatedR[1]
        var dateTo =  separatedR[2]
        var dateRTo =  dateTo +"-" +monthTo +"-"+yearTo
        tvRTime.text = dateRTo +" " +
                "(${pojoWithData.tdtime})"
//        tvRTime.text = pojoWithData.tddate +" " +
//                "(${pojoWithData.tdtime})"


        tvOfferedby.text = pojoWithData.username
         tvPrice.text = "₹ "+pojoWithData.price.toString()

        if (pojoWithData.image != null ) {
            Glide.with(this).load(pojoWithData.image).into(ivprof)
        }



        askGalleryPermissionLocation()

        var mapViewBundle: Bundle? = null
        if (savedInstanceState != null) {
            askGalleryPermissionLocation()
            mapViewBundle = savedInstanceState.getBundle(MAP_VIEW_BUNDLE_KEY)
        }

        mapView = findViewById(R.id.mapRide)
        mapView.onCreate(mapViewBundle)
        mapView.getMapAsync(this)


        fetchLatLog()
        getRating()

        ivCloseScreen.setOnClickListener {
            onBackPressed()
        }


        rlProfile.setOnClickListener {
            var int = Intent(this,
                DriverProfile::class.java)
            val bundle =
                ActivityOptionsCompat.makeCustomAnimation(
                    this ,
                    R.anim.fade_in, R.anim.fade_out
                ).toBundle()
            int.putExtra("pojoWithData",pojoWithData)
            startActivity(int,bundle)
        }

        btnBook.setOnClickListener {
                bookRideAPI(pojoWithData)
            }

        btnDelete.setOnClickListener {
            deleteRideAPI(pojoWithData)
        }



        btnCancel.setOnClickListener {
            CancelRide()
        }


        btnEdit.setOnClickListener {
            var int = Intent(this,
                EditRide::class.java)
            val bundle =
                ActivityOptionsCompat.makeCustomAnimation(
                    this ,
                    R.anim.fade_in, R.anim.fade_out
                ).toBundle()
            int.putExtra("pojoWithData",pojoWithData)
            startActivity(int,bundle)
        }

    }



    private fun getRating() {
        progressDialog = ProgressDialog(this)
        progressDialog.setMessage("Wait a Sec.... ")
        progressDialog.setCancelable(false)
        progressDialog.show()


        var rating = 0
        var userRatedCount = 0

        val url = BASE_URL+ RATING
        val jsonObjRequest: StringRequest = object : StringRequest(
            Method.GET,
            url,
            object : Response.Listener<String?> {
                override fun onResponse(response: String?) {
                    Log.d("driverprofRate", response.toString())
                    val gson = Gson()

                    val userArray: ArrayList<RatingModelItem> =
                        gson.fromJson(response, RatingModel ::class.java)

                    if (userArray != null) {

                        for (i in 0..userArray.size-1) {

                                if (userArray.get(i).driver == pojoWithData.user) {
                                    if (userArray.get(i).points != null) {

                                        rating = rating + userArray.get(i).points
                                    userRatedCount = userRatedCount + 1
                                }
                            }
                        }

                        Log.d("dtsbjnkd",rating.toString())


                        if (userRatedCount != 0 && userRatedCount != null) {
                            var sum = userRatedCount * 5
                            var finalRating = (rating * 5) / sum
                            tvRating.text = finalRating.toString()+"/5 ratings"
                        }

                    }


                    progressDialog.dismiss()
                }
            }, object : Response.ErrorListener {
                override fun onErrorResponse(error: VolleyError) {
                    VolleyLog.d("volley", "Error: " + error.message)
                    error.printStackTrace()
                    Log.e("Responceis",  "Error: " + error.message)

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
                return params
            }
        }
        request!!.add(jsonObjRequest)


    }

    private fun fetchLatLog() {
        mapRide.visibility = View.VISIBLE
        mapRide.elevation = 10F

        var animZoomIn = AnimationUtils.loadAnimation(getApplicationContext(),
            R.anim.fade_in);
        mapRide.startAnimation(animZoomIn);
        Handler().postDelayed({
            var animZoomIn = AnimationUtils.loadAnimation(getApplicationContext(),
                R.anim.fade_out);
            mapRide.startAnimation(animZoomIn);
            mapRide.visibility = View.GONE
        }, 400)
    }


    public override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)

        askGalleryPermissionLocation()
        var mapViewBundle = outState.getBundle(MAP_VIEW_BUNDLE_KEY)
        if (mapViewBundle == null) {
            mapViewBundle = Bundle()
            outState.putBundle(MAP_VIEW_BUNDLE_KEY, mapViewBundle)
        }

        mapView.onSaveInstanceState(mapViewBundle)
    }

    override fun onLocationChanged(location: Location) {

        val geocoder = Geocoder(this, Locale.getDefault())
        var addresses: List<Address>? = null
        try {
            addresses = geocoder.getFromLocation(location.latitude, location.longitude, 1)
        } catch (e: IOException) {
            e.printStackTrace()
        }

        setAddress(addresses!![0])

    }


    private fun getCurrentLocation() {

        fusedLocationProviderClient =
            LocationServices.getFusedLocationProviderClient(this@RideDetails)

        try {

            @SuppressLint("MissingPermission") val location =
                fusedLocationProviderClient!!.getLastLocation()


            location.addOnCompleteListener(object : OnCompleteListener<Location> {
                override fun onComplete(p0: Task<Location>) {
                    if (p0.isSuccessful) {

                        val currentLocation = p0.result as Location?
                        if (currentLocation != null) {
                            moveCamera(
                                LatLng(currentLocation.latitude, currentLocation.longitude),
                                DEFAULT_ZOOM
                            )

                        }
                    } else {
                        Utils.showSnackMSG(mapView,"Current Location Not Found")
                    }
                }


            })


        } catch (se: Exception) {
            Log.e("TAG", "Security Exception")
        }


    }


    override fun onCameraIdle() {


        setAddress(
            Utils.getAddreddFromLatLong(
                this,
                mMap!!.getCameraPosition().target.latitude,
                mMap!!.getCameraPosition().target.longitude
            )
        )
    }

    override fun onCameraMove() {

    }

    override fun onCameraMoveStarted(i: Int) {


    }

    private fun moveCamera(latLng: LatLng, zoom: Float) {


        mMap!!.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, zoom))
    }

    private fun setAddress(addresses: Address?) {

        if (addresses != null) {


            if (addresses.getAddressLine(0) != null) {

                if (addresses.latitude  != null && addresses.longitude  != null ) {

                    lat = addresses.latitude
                    log = addresses.longitude
                }


            }
            if (addresses.getAddressLine(1) != null) {



            }
        }
    }



    private fun deleteRideAPI(customers: BookRidesPojoItem) {

        progressDialog = ProgressDialog(this)
        progressDialog.setMessage("Wait a Sec....Deleting Ride")
        progressDialog.setCancelable(false)
        progressDialog.show()


        val url = Configure.BASE_URL + Configure.OFFER_RIDE_URL+ "${customers.id}/"

        val jsonObjRequest: StringRequest = object : StringRequest(
            Method.DELETE,
            url,
            object : Response.Listener<String?> {
                override fun onResponse(response: String?) {
                    Log.d("jukjbkjf", response.toString())

                        Toast.makeText(this@RideDetails,"Ride Successfully Deleted.",Toast.LENGTH_LONG).show()
                    var int = Intent(this@RideDetails,
                        MainActivity::class.java)
                    val bundle =
                        ActivityOptionsCompat.makeCustomAnimation(
                            this@RideDetails ,
                            R.anim.fade_in, R.anim.fade_out
                        ).toBundle()
                    int.putExtra("type", "")
                    startActivity(int,bundle)

                    progressDialog.dismiss()

                }
            }, object : Response.ErrorListener {
                override fun onErrorResponse(error: VolleyError) {
                    VolleyLog.d("volley", "Error: " + error.message)
                    error.printStackTrace()
                    Log.e("Responceis",  "Error: " + error.message)

                    if (error.networkResponse.statusCode == 204) {
                        Toast.makeText(this@RideDetails,"Ride Successfully Canceled.",Toast.LENGTH_LONG).show()

                        var int = Intent(this@RideDetails,
                            MainActivity::class.java)
                        val bundle =
                            ActivityOptionsCompat.makeCustomAnimation(
                                this@RideDetails ,
                                R.anim.fade_in, R.anim.fade_out
                            ).toBundle()
                        startActivity(int,bundle)

                    } else {
                        Toast.makeText(this@RideDetails,"Something Went Wrong ! Please try after some time",
                            Toast.LENGTH_LONG).show()
                    }

                    progressDialog.dismiss()
                }
            }) {


            override fun getHeaders(): MutableMap<String, String> {

                Log.d("jukjbkj", LOGIN_TOKEN.toString())

                var params = java.util.HashMap<String, String>()
                params.put("Content-Type", "application/json; charset=UTF-8");
                params.put("Authorization", "Token "+LOGIN_TOKEN!!);
                return params;
            }

            @Throws(AuthFailureError::class)
            override fun getParams(): Map<String, String> {
                val params: MutableMap<String, String> =
                    HashMap()
//                params.put("ride",customers.id.toString())
//                params.put("passenger",USER_ID_KEY)

                return params
            }
        }
        request!!.add(jsonObjRequest)
    }



    private fun CancelRide( ) {
        progressDialog = ProgressDialog(this)
        progressDialog.setMessage("Wait a Sec....Canceling Ride")
        progressDialog.setCancelable(false)
        progressDialog.show()


        val url = Configure.BASE_URL + Configure.Book_RIDE_URL+ "${IDToCancel}/"

        val jsonObjRequest: StringRequest = object : StringRequest(
            Method.DELETE,
            url,
            object : Response.Listener<String?> {
                override fun onResponse(response: String?) {
                    Log.d("jukjbkjf", response.toString())

                    Toast.makeText(this@RideDetails,"Ride Successfully Canceled.",Toast.LENGTH_LONG).show()
                    var int = Intent(this@RideDetails,
                        MainActivity::class.java)
                    val bundle =
                        ActivityOptionsCompat.makeCustomAnimation(
                            this@RideDetails ,
                            R.anim.fade_in, R.anim.fade_out
                        ).toBundle()
                    int.putExtra("type", "")
                    startActivity(int,bundle)

                    progressDialog.dismiss()

                }
            }, object : Response.ErrorListener {
                override fun onErrorResponse(error: VolleyError) {
                    VolleyLog.d("volley", "Error: " + error.message)
                    error.printStackTrace()
                    Log.e("Responceis",  "Error: " + error.message)

                    if (error.networkResponse.statusCode == 204) {
                        Toast.makeText(this@RideDetails,"Ride Successfully Canceled.",Toast.LENGTH_LONG).show()

                        var int = Intent(this@RideDetails,
                            MainActivity::class.java)
                        val bundle =
                            ActivityOptionsCompat.makeCustomAnimation(
                                this@RideDetails ,
                                R.anim.fade_in, R.anim.fade_out
                            ).toBundle()
                        startActivity(int,bundle)

                    } else {
                        Toast.makeText(this@RideDetails,"Something Went Wrong ! Please try after some time",
                            Toast.LENGTH_LONG).show()
                    }

                    progressDialog.dismiss()
                }
            }) {


            override fun getHeaders(): MutableMap<String, String> {

                Log.d("jukjbkj", LOGIN_TOKEN.toString())

                var params = java.util.HashMap<String, String>()
                params.put("Content-Type", "application/json; charset=UTF-8");
                params.put("Authorization", "Token "+LOGIN_TOKEN!!);
                return params;
            }

            @Throws(AuthFailureError::class)
            override fun getParams(): Map<String, String> {
                val params: MutableMap<String, String> =
                    HashMap()
//                params.put("ride",customers.id.toString())
//                params.put("passenger",USER_ID_KEY)

                return params
            }
        }
        request!!.add(jsonObjRequest)
    }


    private fun bookRideAPI(customers: BookRidesPojoItem) {

        progressDialog = ProgressDialog(this)
        progressDialog.setMessage("Wait a Sec....Booking Ride for you..")
        progressDialog.setCancelable(false)
        progressDialog.show()


        val url = Configure.BASE_URL + Configure.Book_RIDE_URL+"?ride=${customers.id}"

        val jsonObjRequest: StringRequest = object : StringRequest(
            Method.GET,
            url,
            object : Response.Listener<String?> {
                override fun onResponse(response: String?) {
                    Log.d("jukjbkjf", response.toString())

                    val gson = Gson()

                    val userArray : ArrayList<bookrideItem> =
                        gson.fromJson(response, bookride ::class.java)

                    if (userArray.size == customers.passenger!!.toInt() ) {
                        progressDialog.dismiss()
                        Toast.makeText(this@RideDetails,"No seat available for this Ride. Choose another ride.",Toast.LENGTH_LONG).show()
                    } else {
                        boookRideAllowed(customers)
                    }

                }
            }, object : Response.ErrorListener {
                override fun onErrorResponse(error: VolleyError) {
                    VolleyLog.d("volley", "Error: " + error.message)
                    error.printStackTrace()
                    Log.e("Responceis",  "Error: " + error.message)

                    Toast.makeText(this@RideDetails,"Something Went Wrong ! Please try after some time",
                        Toast.LENGTH_LONG).show()

                    progressDialog.dismiss()
                }
            }) {


            override fun getHeaders(): MutableMap<String, String> {

                Log.d("jukjbkj", LOGIN_TOKEN.toString())

                var params = java.util.HashMap<String, String>()
                params.put("Content-Type", "application/json; charset=UTF-8");
                params.put("Authorization", "Token "+LOGIN_TOKEN!!);
                return params;
            }

            @Throws(AuthFailureError::class)
            override fun getParams(): Map<String, String> {
                val params: MutableMap<String, String> =
                    HashMap()
//                params.put("ride",customers.id.toString())
//                params.put("passenger",USER_ID_KEY)

                return params
            }
        }
        request!!.add(jsonObjRequest)
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


    private fun boookRideAllowed(customers: BookRidesPojoItem) {

        val url = Configure.BASE_URL + Configure.Book_RIDE_URL

        val jsonObjRequest: StringRequest = object : StringRequest(
            Method.POST,
            url,
            object : Response.Listener<String?> {
                override fun onResponse(response: String?) {

                    val gson = Gson()

                    val userArray :bookrideItem =
                        gson.fromJson(response, bookrideItem ::class.java)

                    sendMessage(customers)

                }
            }, object : Response.ErrorListener {
                override fun onErrorResponse(error: VolleyError) {
                    VolleyLog.d("volley", "Error: " + error.message)
                    error.printStackTrace()
                    Log.e("Responceis",  "Error: " + error.message)

                    Toast.makeText(this@RideDetails,"Something Went Wrong ! Please try after some time",
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
                params.put("ride",customers.id.toString())
                params.put("passenger",USER_ID_KEY)
                params.put("plat",lat.toString())
                params.put("plog",log.toString())
                params.put("comment"," ")

                return params
            }
        }
        request!!.add(jsonObjRequest)
    }

    private fun sendMessage(customers: BookRidesPojoItem) {

        var text = ""

        if (customers.is_direct == true) {
            text = "Ride from ${customers.leaving} to ${customers.going} has been booked by me. " +
                    "Please provide me more details regarding that."
        } else {
            text = "Hello Sir, I am interested to book your Ride from ${customers.leaving} to ${customers.going}. " +
                    "Please provide me more details regarding that."
        }


        val fromId = USER_ID_KEY
        val toId = customers.user.toString()

        if (fromId == null) return

        val reference = FirebaseDatabase.getInstance().getReference("/user-messages/$fromId/$toId").push()

        val toReference = FirebaseDatabase.getInstance().getReference("/user-messages/$toId/$fromId").push()

        val chatMessage = ChatMessage(reference.key!!, text, fromId, toId, System.currentTimeMillis() / 1000)

        reference.setValue(chatMessage)
            .addOnSuccessListener {
                progressDialog.dismiss()
                Toast.makeText(this@RideDetails,"Ride Booked Successfully.",Toast.LENGTH_LONG).show()

                var int = Intent(this@RideDetails,
                    BookedSuccess::class.java)
                val bundle =
                    ActivityOptionsCompat.makeCustomAnimation(
                        this@RideDetails ,
                        R.anim.fade_in, R.anim.fade_out
                    ).toBundle()
                startActivity(int,bundle)
            }
            .addOnFailureListener {
                progressDialog.dismiss()
                Toast.makeText(this@RideDetails,"Ride Booked Successfully.",Toast.LENGTH_LONG).show()

                var int = Intent(this@RideDetails,
                    BookedSuccess::class.java)
                val bundle =
                    ActivityOptionsCompat.makeCustomAnimation(
                        this@RideDetails ,
                        R.anim.fade_in, R.anim.fade_out
                    ).toBundle()
                startActivity(int,bundle)
            }

        toReference.setValue(chatMessage)

        val latestMessageRef = FirebaseDatabase.getInstance().getReference("/latest-messages/$fromId/$toId")
        latestMessageRef.setValue(chatMessage)

        val latestMessageToRef = FirebaseDatabase.getInstance().getReference("/latest-messages/$toId/$fromId")
        latestMessageToRef.setValue(chatMessage)
    }


}
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
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.View
import android.view.animation.AnimationUtils
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.app.ActivityOptionsCompat
import com.android.volley.*
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.bumptech.glide.Glide
import com.ddsio.productionapp.sharesavari.CommonUtils.Utils
import com.ddsio.productionapp.sharesavari.CommonUtils.Utils.convertDateFormat
import com.ddsio.productionapp.sharesavari.CommonUtils.Utils.getDayOfDate
import com.ddsio.productionapp.sharesavari.HomeScreen.Child.EditRide
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
import com.productionapp.amhimemekar.CommonUtils.*
import com.productionapp.amhimemekar.CommonUtils.Configure.BASE_URL
import com.productionapp.amhimemekar.CommonUtils.Configure.ONESIGNAL
import com.productionapp.amhimemekar.CommonUtils.Configure.RATING
import io.github.inflationx.viewpump.ViewPumpContextWrapper
import kotlinx.android.synthetic.main.activity_hault.*
import kotlinx.android.synthetic.main.activity_ride_detail.*
import kotlinx.android.synthetic.main.ride_booking_type.view.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import java.io.IOException
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.*

class RideDetails : AppCompatActivity(), OnMapReadyCallback,
    com.google.android.gms.location.LocationListener, GoogleMap.OnCameraMoveListener, GoogleMap.OnCameraMoveStartedListener, GoogleMap.OnCameraIdleListener {

    lateinit var progressDialog: ProgressDialog
    var LOGIN_TOKEN = ""
    var USER_UPDATE_ID = ""
    lateinit var USER_ID_KEY : String

    var player_id = ""
    lateinit var userProf : FetchProfileData
    lateinit var currentUser : FetchProfileData
    var lat = 0.0
    var log = 0.0
    lateinit var convidPoster: AlertDialog
    var request: RequestQueue? = null

    private var fusedLocationProviderClient: FusedLocationProviderClient? = null
    private var mMap: GoogleMap? = null
    private val DEFAULT_ZOOM = 15f

    lateinit var mapView: MapView
    var bookedSeatCount = 0
    private val MAP_VIEW_BUNDLE_KEY = "MapViewBundleKey"

    lateinit var pojoWithData : BookRidesPojoItem
    lateinit var rvCoPas : LinearLayout

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

        rvCoPas = findViewById<LinearLayout>(R.id.rvCoPas)

        player_id = Utils.getStringFromPreferences(Configure.PLAYER_ID, "", this@RideDetails)!!

        val bundle: Bundle? = intent.extras
        pojoWithData = bundle!!.get("pojoWithData") as BookRidesPojoItem
        var screen  = bundle!!.get("screen") as String
        IDToCancel  = bundle!!.get("IDToCancel") as String


        if (screen == "home") {
            rlBottom.visibility = View.GONE
            rlBottomDelete.visibility = View.VISIBLE
            rlBottomEdit.visibility = View.VISIBLE
            rvPendReq.visibility = View.VISIBLE

            if (pojoWithData.is_direct == false) {
                rvPendReq.visibility = View.VISIBLE
            } else {
                rvPendReq.visibility = View.GONE
            }

            rlBottomCancel.visibility = View.GONE
        } else if (screen == "Booked"){
            rlBottom.visibility = View.GONE
            rlBottomCancel.visibility = View.VISIBLE
            rlBottomDelete.visibility = View.GONE
            rlBottomEdit.visibility = View.GONE
            rvPendReq.visibility = View.GONE
        } else {
            rlBottomDelete.visibility = View.GONE
            rlBottomEdit.visibility = View.GONE
            rlBottom.visibility = View.VISIBLE
            rvPendReq.visibility = View.GONE
            rlBottomCancel.visibility = View.GONE
        }


        LOGIN_TOKEN = Utils.getStringFromPreferences(Configure.LOGIN_KEY,"",this)!!
        USER_UPDATE_ID = Utils.getStringFromPreferences(Configure.USER_UPDATE_ID,"",this)!!
        USER_ID_KEY = Utils.getStringFromPreferences(Configure.USER_ID_KEY,"",this)!!
        request= Volley.newRequestQueue(this)

        askGalleryPermissionLocation()
        bookedSeatsCount(pojoWithData)

       tvFromAdd.text = pojoWithData.lline+", "+pojoWithData.lcity
       tvToAdd.text = pojoWithData.gline+", "+pojoWithData.gcity
        tvCo.text = bookedSeatCount.toString()

        var dateFormat = pojoWithData.date
        val separated =
            dateFormat!!.split("-".toRegex()).toTypedArray()
        var yearFrom = separated[0]
        var monthFrom =  separated[1]
        var dateFrom =  separated[2]
        var dateFFRom =  dateFrom +"-" +monthFrom +"-"+yearFrom
        tvDate.text = dateFFRom

        tvLTime.text = convertDateFormat(pojoWithData.date.toString()) +" " +
                "(${pojoWithData.time})"


        tvFromFullAdd.text = "("+ pojoWithData.leaving+")"
        tvToFullAdd.text = "("+ pojoWithData.going+")"


        tvCom.text = pojoWithData.comment

        Log.d("sssssssss",pojoWithData.carname.toString())

        if (pojoWithData.carname == "" || pojoWithData.carname.isNullOrBlank()) {
            llCar.visibility = View.GONE
        } else {
            tvCar.text = pojoWithData.carname
            tvCarColor.text = "("+ pojoWithData.carcolor+")"
        }


        if (pojoWithData.stitle == null || pojoWithData.stitle == "") {
            llStopPoint.visibility = View.GONE
            tvSP.visibility = View.GONE
        } else {
            tvStopPoint.text = pojoWithData.stitle
            tvSP.text = pojoWithData.stitle
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

        rvCoPas.setOnClickListener {
            Log.d("huhuhujh","clickedc")

            var intent = Intent(this@RideDetails,
                CoPasList::class.java)
            intent.putExtra("pojoWithData",pojoWithData)
            startActivity(intent)
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


         tvPrice.text = "₹ "+pojoWithData.price.toString()

        if (pojoWithData.image != null ) {
            Glide.with(this).load(pojoWithData.image).into(ivprof)
        }

        if (pojoWithData.date == pojoWithData.tddate ) {
            tvDay.text = getDayOfDate(pojoWithData.date.toString()) +", "+ convertDateFormat(pojoWithData.date.toString())
            tvDay.visibility = View.VISIBLE
            tvRTime.text =pojoWithData.tdtime
            tvLTime.text =pojoWithData.time

        } else {
            tvRTime.text = convertDateFormat(pojoWithData.date.toString()) +" " +
                    "(${pojoWithData.tdtime})"
            tvLTime.text = convertDateFormat(pojoWithData.tddate.toString()) +" " +
                    "(${pojoWithData.time})"
            tvDay.visibility = View.GONE
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
        findUser()
        findCust()
        getRating()

        ivCloseScreen.setOnClickListener {
            onBackPressed()
        }



        rvPendReq.setOnClickListener {
            var int = Intent(this,
                PendingReq ::class.java)
            int.putExtra("pojoWithData",pojoWithData)
            startActivity(int)
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
            int.putExtra("type","driver")
            if (USER_ID_KEY == pojoWithData.user.toString()) {
                int.putExtra("cust",pojoWithData.user.toString())
            } else {
                int.putExtra("cust","0")
            }


            startActivity(int,bundle)
        }

        btnBook.setOnClickListener {
                showRideType(pojoWithData)
            }

        btnDelete.setOnClickListener {
            showDeleteDialog(pojoWithData)
        }


        disableCancelrideButton()


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
            if (pojoWithData.stitle == null || pojoWithData.stitle == "") {
                int.putExtra("stoppointReturn",  "")
            } else {
                int.putExtra("stoppointReturn", pojoWithData.stitle)
            }

            startActivity(int,bundle)
        }

    }

    private fun disableCancelrideButton() {
        val c = Calendar.getInstance()
        val df = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
        val formattedDate = df.format(c.time)

        if(getTimeStamp(pojoWithData.date+" "+pojoWithData.time) < getTimeStamp(formattedDate)) {
            btnDelete.visibility = View.GONE
            btnEdit.visibility = View.GONE
        }
    }

    private fun getTimeStamp(s: String): Long {

        Log.d("timestampdatsi",s)

        val formatter: DateFormat = SimpleDateFormat("yyyy-MM-dd hh:mm:ss")
        val date = formatter.parse(s) as Date

        Log.d("timestampdatsi",date.time.toString())

        return date.time
    }


    private fun showRideType(pojoWithData: BookRidesPojoItem) {

        var availabl = pojoWithData.passenger!!.toInt() - bookedSeatCount

        val inflater = getLayoutInflater()
        var count = 1
        val alertLayout = inflater.inflate(R.layout.ride_booking_type, null)

        val showOTP = AlertDialog.Builder(this!!)
        showOTP.setView(alertLayout)
        showOTP.setCancelable(false)
        convidPoster = showOTP.create()
        convidPoster.show()

        if (pojoWithData.is_direct == true) {
            alertLayout.tvNotice.text = "Are you sure you want to book this Ride?? If you click on CONTINUE Ride will be Booked."
        } else {
            alertLayout.tvNotice.text = "This Ride is not Instant Booking Ride. Once you click on CONTINUE request will be sent to Driver and " +
                    "driver will decide weather to confirm or not. You will be get notified once your request is confirmed."
        }
        alertLayout.tvCount.setText(count.toString())

        alertLayout.cvMinus.setOnClickListener {
            if (count != 1) {
                count--
                alertLayout.tvCount.setText(count.toString())
            }
        }

        alertLayout.cvAdd.setOnClickListener {
            if (count < pojoWithData.passenger!!.toInt() ) {
                count++
                alertLayout.tvCount.setText(count.toString())
            } else {
                Toast.makeText(alertLayout.cvAdd.context,"Only ${pojoWithData.passenger} Passenger's are allowed in this Ride.",Toast.LENGTH_SHORT).show()
            }
        }

        alertLayout.cvContinue.setOnClickListener {
            if (count != 0) {
                if (count <= availabl) {
                    bookRideAPI(this.pojoWithData,count)
                    convidPoster.dismiss()
                } else {
                    Toast.makeText(alertLayout.cvAdd.context,"Only ${availabl} Seats's are Available.",Toast.LENGTH_SHORT).show()
                }
            }
        }

        alertLayout.ccvCancel.setOnClickListener {
            convidPoster.dismiss()
        }
    }



    private fun showDeleteDialog(pojoWithData: BookRidesPojoItem) {
        val inflater = getLayoutInflater()
        val alertLayout = inflater.inflate(R.layout.delete_ride_dialog, null)

        val showOTP = AlertDialog.Builder(this!!)
        showOTP.setView(alertLayout)
        showOTP.setCancelable(false)
        convidPoster = showOTP.create()
        convidPoster.show()

            alertLayout.tvNotice.text = "Are you sure you want to Delete this Ride ? "


        alertLayout.cvContinue.setOnClickListener {
            deleteRideAPI(pojoWithData)
        }

        alertLayout.ccvCancel.setOnClickListener {
            convidPoster.dismiss()
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

    @Subscribe
    fun onBackFromPending(fileName : String?) {
        if (fileName == "Pending") {
            bookedSeatsCount(pojoWithData)
        }
    }

    override fun onStart() {
        super.onStart()
        if (!EventBus.getDefault().isRegistered(this@RideDetails)) EventBus.getDefault().register(this)
    }

    override fun onDestroy() {
        super.onDestroy()
        EventBus.getDefault().unregister(this@RideDetails)
    }

    private fun findUser( ) {
        val url = BASE_URL+ Configure.GET_USER_DETAILS +pojoWithData.user+"/"

        val jsonObjRequest: StringRequest = object : StringRequest(
            Method.GET,
            url,
            object : Response.Listener<String?> {
                override fun onResponse(response: String?) {
                    Log.d("driverprof", response.toString())

                    val gson = Gson()

                    if (response != null ) {

                        val userArray: FetchProfileData =
                            gson.fromJson(response, FetchProfileData ::class.java)
                        tvOfferedby.text = userArray.first_name

                        userProf = userArray

                    }
                    progressDialog.dismiss()
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

                return params
            }
        }
        request!!.add(jsonObjRequest)

    }




    private fun findCust( ) {
        val url = BASE_URL+ Configure.GET_USER_DETAILS +USER_ID_KEY+"/"

        val jsonObjRequest: StringRequest = object : StringRequest(
            Method.GET,
            url,
            object : Response.Listener<String?> {
                override fun onResponse(response: String?) {
                    Log.d("driverprof", response.toString())

                    val gson = Gson()

                    if (response != null ) {

                        val userArray: FetchProfileData =
                            gson.fromJson(response, FetchProfileData ::class.java)

                        currentUser = userArray

                    }
                    progressDialog.dismiss()
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

        Log.d("jbububu",url)

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

                var params = HashMap<String, String>()
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


    private fun bookRideAPI(
        customers: BookRidesPojoItem,
        count: Int
    ) {

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
//                        for (i in 0..count-1) {
                            boookRideAllowed(customers,count )
//                        }

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



    private fun bookedSeatsCount(customers: BookRidesPojoItem) {

        bookedSeatCount = 0

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


                    if (userArray != null) {
                        for (i in 0..userArray.size - 1) {
//                                adapter.add(ridesClass(userArray.get(i)))

                            if (userArray.get(i).is_confirm) {
                               bookedSeatCount = bookedSeatCount + userArray.get(i).seats.toInt()
                            }
                        }
                        tvCo.text = bookedSeatCount.toString()+"/"+ pojoWithData.passenger
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


    private fun boookRideAllowed(
        customers: BookRidesPojoItem,

        count: Int
    ) {

        val url = Configure.BASE_URL + Configure.Book_RIDE_URL

        val jsonObjRequest: StringRequest = object : StringRequest(
            Method.POST,
            url,
            object : Response.Listener<String?> {
                override fun onResponse(response: String?) {

                    Log.d("bookedrodeis",response.toString())

                    val gson = Gson()

                    val userArray :bookrideItem =
                        gson.fromJson(response, bookrideItem ::class.java)

                    sendNotificationSelf(customers)
                    sendNotification(customers)
                    sendSMSSelf(customers)
                    sendSMS(customers)

                        sendMessage(customers,count)


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
                params.put("seats",count.toString())
                params.put("comment"," ")
                params.put("is_confirm",pojoWithData.is_direct.toString())

                return params
            }
        }
        request!!.add(jsonObjRequest)
    }

    private fun sendSMS(customers: BookRidesPojoItem) {
        var msg = ""

        if (pojoWithData.is_direct == true) {
           msg = "You%20booked%20a%20Ride%20from%20${pojoWithData.leaving}%20to%20${pojoWithData.going}%20on%20${pojoWithData.date}." +
                   "%20Please%20check%20into%20app%20for%20more%20details."

        } else {
           msg = "Your%20Request%20to%20book%20a%20Ride%20from%20${pojoWithData.leaving}%20to%20${pojoWithData.going}%20on%20${pojoWithData.date}%20has%20been%20sent," +
                   "%20Please%20check%20into%20app%20for%20more%20details."
        }

        val url = "http://login.bulksmsgateway.in/sendmessage.php?user=prasadbirari&password=Janardan1&mobile=${currentUser.mobile}&message=${msg}&sender=MSGSAY&type=3"

        Log.d("smsentvjjnd", url.toString())
        Log.d("smsentvjjnd", currentUser.mobile!!)
        val jsonObjRequest: StringRequest = object : StringRequest(
            Method.GET,
            url,
            object : Response.Listener<String?> {
                override fun onResponse(response: String?) {
                    Log.d("smsentvjjnd", response.toString())

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
                params.put("Content-Type", "application/x-www-form-urlencoded");
                params.put("Cache-Control", "no-cache");
                return params;
            }

            @Throws(AuthFailureError::class)
            override fun getParams(): Map<String, String> {
                val params: MutableMap<String, String> =
                    java.util.HashMap()
//                params.put("ride",customers.id.toString())
//                params.put("passenger",USER_ID_KEY)

                return params
            }
        }
        request!!.add(jsonObjRequest)

    }

    private fun sendNotification(customers: BookRidesPojoItem) {

        val jsonObjRequest: StringRequest = object : StringRequest(
            Method.POST,
            ONESIGNAL,
            object : Response.Listener<String?> {
                override fun onResponse(response: String?) {
                     Log.d("ssssss","sendNoSuccess")
                }
            }, object : Response.ErrorListener {
                override fun onErrorResponse(error: VolleyError) {
                    Log.d("ssssss","sendNoFailed"+" "+error.networkResponse.statusCode+" "+error.message)
                }
            }) {

            @Throws(AuthFailureError::class)
            override fun getParams(): Map<String, String> {
                val params: MutableMap<String, String> =
                    HashMap()
                if (pojoWithData.is_direct == true) {
                    params.put("message","You booked a Ride from ${pojoWithData.leaving} to  ${pojoWithData.going} on  ${pojoWithData.date}. Please check into app for more details.")

                } else {
                    params.put("message","Your Request to book a Ride from ${pojoWithData.leaving} to  ${pojoWithData.going} on  ${pojoWithData.date} has been sent," +
                            " Please check into app for more details.")
                }

                params.put("user",player_id)
                return params
            }
        }
        request!!.add(jsonObjRequest)
    }



    private fun sendNotificationSelf(customers: BookRidesPojoItem) {

        val jsonObjRequest: StringRequest = object : StringRequest(
            Method.POST,
            ONESIGNAL,
            object : Response.Listener<String?> {
                override fun onResponse(response: String?) {
                    Log.d("ssssss","sendNoSuccessSelf")
                }
            }, object : Response.ErrorListener {
                override fun onErrorResponse(error: VolleyError) {
                    Log.d("ssssss","sendNoSelffail"+" "+error.networkResponse.statusCode+" "+error.message)
                }
            }) {

            @Throws(AuthFailureError::class)
            override fun getParams(): Map<String, String> {
                val params: MutableMap<String, String> =
                    HashMap()

                if (pojoWithData.is_direct == true) {
                    params.put("message","Your Ride has been booked by ${currentUser.first_name}. Please check into app for more details.")

                } else {
                    params.put("message"," ${currentUser.first_name} has requested to book your Ride. Please check into app for more details.")
                }

                params.put("user",userProf.oneid.toString())
                return params
            }
        }
        request!!.add(jsonObjRequest)
    }

    private fun sendSMSSelf(customers: BookRidesPojoItem) {

        var msg = ""

        if (pojoWithData.is_direct == true) {
            msg = "Your%20Ride%20has%20been%20booked%20by%20${currentUser.first_name}.%20Please%20check%20into%20app%20for%20more%20details."

        } else {
           msg = "${currentUser.first_name}%20has%20requested%20to%20book%20your%20Ride.%20Please%20check%20into%20app%20for%20more%20details."

        }

        val url = "http://login.bulksmsgateway.in/sendmessage.php?user=prasadbirari&password=Janardan1&mobile=${userProf.mobile}&message=${msg}&sender=MSGSAY&type=3"

        Log.d("smsentvjjnd", url.toString())
        Log.d("smsentvjjnd", userProf.mobile!!)
        val jsonObjRequest: StringRequest = object : StringRequest(
            Method.GET,
            url,
            object : Response.Listener<String?> {
                override fun onResponse(response: String?) {
                    Log.d("smsentvjjnd", response.toString())


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
                params.put("Content-Type", "application/x-www-form-urlencoded");
                params.put("Cache-Control", "no-cache");
                return params;
            }

            @Throws(AuthFailureError::class)
            override fun getParams(): Map<String, String> {
                val params: MutableMap<String, String> =
                    java.util.HashMap()
//                params.put("ride",customers.id.toString())
//                params.put("passenger",USER_ID_KEY)

                return params
            }
        }
        request!!.add(jsonObjRequest)

    }

    private fun sendMessage(
        customers: BookRidesPojoItem,
        count: Int
    ) {

        var text = ""

        if (customers.is_direct == true) {
            text = "Ride from ${customers.leaving} to ${customers.going} has been booked by me for ${count} Passengers. " +
                    "Please provide me more details regarding that."
        } else {
            text = "Hello Sir, I am interested to book your Ride from ${customers.leaving} to ${customers.going} for ${count} Passengers." +
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

                if (pojoWithData.is_direct == true) {
                    Toast.makeText(this@RideDetails,"Ride Booked Successfully.",Toast.LENGTH_LONG).show()

                    var int = Intent(this@RideDetails,
                        BookedSuccess::class.java)
                    val bundle =
                        ActivityOptionsCompat.makeCustomAnimation(
                            this@RideDetails ,
                            R.anim.fade_in, R.anim.fade_out
                        ).toBundle()
                    startActivity(int,bundle)

                } else {
//                    sendMessageSelf(  customers,
//                        count )
                    Toast.makeText(this@RideDetails,"Request sent Successfully. You will get notified soon.",Toast.LENGTH_LONG).show()
                }

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


    private fun sendMessageSelf(
        customers: BookRidesPojoItem,
        count: Int
    ) {

        var text = "Hello Sir, we got your request. Request status will be updated soon."


        val fromId = customers.user.toString()
        val toId = USER_ID_KEY


        if (fromId == null) return

        val reference = FirebaseDatabase.getInstance().getReference("/user-messages/$fromId/$toId").push()

        val toReference = FirebaseDatabase.getInstance().getReference("/user-messages/$toId/$fromId").push()

        val chatMessage = ChatMessage(reference.key!!, text, fromId, toId, System.currentTimeMillis() / 1000)

        reference.setValue(chatMessage)
            .addOnSuccessListener {
                progressDialog.dismiss()

                if (pojoWithData.is_direct == true) {
                    Toast.makeText(this@RideDetails,"Ride Booked Successfully.",Toast.LENGTH_LONG).show()
                } else {
                    Toast.makeText(this@RideDetails,"Request sent Successfully. You will get notified soon.",Toast.LENGTH_LONG).show()
                }


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
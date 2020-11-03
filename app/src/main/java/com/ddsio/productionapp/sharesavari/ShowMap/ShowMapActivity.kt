package com.ddsio.productionapp.sharesavari.ShowMap

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.view.animation.AnimationUtils
import android.widget.SearchView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.core.app.ActivityCompat
import com.ddsio.productionapp.sharesavari.CommonUtils.Utils
import com.ddsio.productionapp.sharesavari.R
import com.github.florent37.runtimepermission.kotlin.askPermission
import com.google.android.gms.common.api.Status
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationListener
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.MapView
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.Task
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.widget.Autocomplete
import com.google.android.libraries.places.widget.AutocompleteActivity
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode
import com.productionapp.amhimemekar.CommonUtils.BookRideScreenFetchCity
import io.github.inflationx.viewpump.ViewPumpContextWrapper
import kotlinx.android.synthetic.main.activity_show_map.*
import org.greenrobot.eventbus.EventBus
import java.io.IOException
import java.util.*
import kotlin.collections.ArrayList

class ShowMapActivity : AppCompatActivity(), OnMapReadyCallback, LocationListener, GoogleMap.OnCameraMoveListener, GoogleMap.OnCameraMoveStartedListener, GoogleMap.OnCameraIdleListener {

    lateinit var tvCurrentAddress: TextView
    var city = ""

    private var fusedLocationProviderClient: FusedLocationProviderClient? = null
    private var mMap: GoogleMap? = null
    private val DEFAULT_ZOOM = 15f

    lateinit var mapView: MapView

    private val MAP_VIEW_BUNDLE_KEY = "MapViewBundleKey"


    var SLat = "0"
    var SLog = "0"
    var gline = "0"
    var gcity = "0"
    var type = ""

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
        setContentView(R.layout.activity_show_map)

        val bundle: Bundle? = intent.extras
        type = bundle!!.getString("typeis")!!

        askGalleryPermissionLocation()

        var cv = findViewById<CardView>(R.id.cvFromLocation)


        Places.initialize(applicationContext,"AIzaSyDBY9Hc3WUJqYMH3n3q2vYeNaTEjytB94s")

        tvAddSearch.isFocusable = false
        tvAddSearch.setOnClickListener {
            var fieldList : List<Place.Field> = arrayListOf(Place.Field.ADDRESS,Place.Field.LAT_LNG,Place.Field.NAME)

            var intent = Autocomplete.IntentBuilder(AutocompleteActivityMode.OVERLAY,fieldList).build(this)
            startActivityForResult(intent,100)
        }

        Utils.checkConnection(this@ShowMapActivity,cv)
            if (!Utils.CheckGpsStatus(this@ShowMapActivity)) {
                Utils.enableGPS(this@ShowMapActivity)
            }

        var mapViewBundle: Bundle? = null
        if (savedInstanceState != null) {
            askGalleryPermissionLocation()
            mapViewBundle = savedInstanceState.getBundle(MAP_VIEW_BUNDLE_KEY)
        }

        mapView = findViewById(R.id.map1)
        mapView.onCreate(mapViewBundle)
        mapView.getMapAsync(this)



        tvCurrentAddress = findViewById<TextView>(R.id.tvCurrentAddress)


        ivBack.setOnClickListener {
            onBackPressed()
        }

        svAddSearch.onActionViewExpanded()

        svAddSearch.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(p0: String?): Boolean {

                var location = svAddSearch.query.toString()
                var addressList : List<Address>? = null;
                if (location != null || location!= "") {
                    var geocoder = Geocoder(this@ShowMapActivity)
                    try {
                        addressList= geocoder.getFromLocationName(location,1)
                    }catch (e : Exception) {
                        e.printStackTrace()
                    }

                    var address = addressList!!.get(0)
                    var latLng = LatLng(address.latitude,address.longitude)


                    mMap!!.addMarker(MarkerOptions().position(latLng).title(location))
                    mMap!!.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng,10f))

                }

                return false
            }

            override fun onQueryTextChange(p0: String?): Boolean {

                return false
            }
        })

        mapView.getMapAsync(this)

        tvAddSearch.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {

            }

            override fun beforeTextChanged(
                s: CharSequence?,
                start: Int,
                count: Int,
                after: Int
            ) {
            }

            override fun onTextChanged(
                s: CharSequence?,
                start: Int,
                before: Int,
                count: Int
            ) {

                if (count == 0) {
                    ivClear.visibility = View.GONE
                } else {
//                    ivClear.visibility = View.VISIBLE
                }

            }
        })

        ivClear.setOnClickListener {
            tvAddSearch.setText("")
        }

        fabDone.setOnClickListener {
            checkFields()
        }

        rlCurrentLoc.setOnClickListener {
//            askGalleryPermissionLocation()
            rlCurrentLoc.visibility = View.GONE
            ivMap.visibility = View.VISIBLE
            ivMap.elevation = 10F

            var animZoomIn = AnimationUtils.loadAnimation(getApplicationContext(),
                 R.anim.fade_in);
            ivMap.startAnimation(animZoomIn);

        }
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == 100 && resultCode == Activity.RESULT_OK) {
            var place = Autocomplete.getPlaceFromIntent(data!!)
             tvAddSearch.setText(place.address)
            mapView.getMapAsync(this)

        } else if (resultCode == AutocompleteActivity.RESULT_ERROR) {
            var status = Autocomplete.getStatusFromIntent(data!!)

            Toast.makeText(this,status.statusMessage,Toast.LENGTH_LONG).show()
        }
    }

    private fun checkFields() {

        if (tvAddSearch.text.toString().isEmpty() || tvAddSearch.text.toString() == "") {
            Toast.makeText(this,"Please Select Correct Address",
                Toast.LENGTH_LONG).show()
        }  else {
            var pojo = BookRideScreenFetchCity()
            pojo.city = city
            pojo.type = type
            pojo.lat = SLat
            pojo.long = SLog
            pojo.gline = gline
            pojo.gcity = gcity

            Log.d("khsnkunkdjv",pojo.city + pojo.type + pojo.lat + pojo.long)

            EventBus.getDefault().post(pojo)
            onBackPressed()
        }
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


    private fun getCurrentLocation() {

        fusedLocationProviderClient =
            LocationServices.getFusedLocationProviderClient(this@ShowMapActivity)

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

//                        Toast.makeText(
//                            this@UserDetails,
//                            "Current location not found.",
//                            Toast.LENGTH_SHORT
//                        ).show()


                        Utils.showSnackMSG(mapView,"Current Location Not Found")

                    }
                }


            })


        } catch (se: Exception) {
            Log.e("TAG", "Security Exception")
        }


    }

    private fun moveCamera(latLng: LatLng, zoom: Float) {

        mMap!!.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, zoom))
    }


    private fun setAddress(addresses: Address?) {

        if (addresses != null) {


            if (addresses.getAddressLine(0) != null) {

                tvCurrentAddress!!.setText(addresses.getAddressLine(0))

                Log.d("latis",addresses.latitude.toString())
                if ( addresses.locality != null && addresses.locality.isNotEmpty() ) {
                    city = addresses.locality
                    tvAddSearch!!.setText(city)
                    gline = addresses.adminArea
                    gcity = addresses.locality

                } else {
                                    tvAddSearch!!.setText(addresses.getAddressLine(0))
                }

                if ( addresses.longitude != null && addresses.latitude != null ) {
                    SLat = addresses.latitude.toString()
                    SLog = addresses.longitude.toString()
                }



            }
            if (addresses.getAddressLine(1) != null) {

                tvCurrentAddress!!.setText(
                    tvCurrentAddress.getText().toString() + addresses.getAddressLine(
                        1
                    )
                )

                tvAddSearch!!.setText(
                    tvAddSearch.getText().toString() + addresses.getAddressLine(
                        1
                    )
                )

            }
        }
    }


    override fun onLocationChanged(location: Location) {

        val geocoder = Geocoder(this, Locale.getDefault())
        var addresses: List<Address>? = null
        try {
            addresses = geocoder.getFromLocation(location.latitude, location.longitude, 1)

            SLat = location.latitude.toString()
            SLog = location.longitude.toString()
        } catch (e: IOException) {
            e.printStackTrace()
        }

        setAddress(addresses!![0])

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

}
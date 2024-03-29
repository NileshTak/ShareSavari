package com.ddsio.productionapp.sharesawaari.OfferScreen

import android.Manifest
import android.annotation.SuppressLint
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
import android.widget.ImageView
import android.widget.SearchView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.app.ActivityOptionsCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationListener
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.MapView
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.Task
import com.ddsio.productionapp.sharesawaari.CommonUtils.Utils
import com.ddsio.productionapp.sharesawaari.R
import com.github.florent37.runtimepermission.kotlin.askPermission
import com.google.android.gms.maps.model.MarkerOptions
import com.productionapp.amhimemekar.CommonUtils.Configure
import com.productionapp.amhimemekar.CommonUtils.offerRideModel
import io.github.inflationx.viewpump.ViewPumpContextWrapper
import kotlinx.android.synthetic.main.activity_show_map.fabDone
import kotlinx.android.synthetic.main.activity_show_map.ivBack
import kotlinx.android.synthetic.main.activity_show_map.ivClear
import kotlinx.android.synthetic.main.activity_show_map.ivMap
import kotlinx.android.synthetic.main.activity_show_map.rlCurrentLoc
import kotlinx.android.synthetic.main.activity_show_map_pick_up.*
import java.io.IOException
import java.util.*

class ShowMapActivityPickUp : AppCompatActivity(), OnMapReadyCallback, LocationListener, GoogleMap.OnCameraMoveListener, GoogleMap.OnCameraMoveStartedListener, GoogleMap.OnCameraIdleListener {

    lateinit var tvCurrentAddress: TextView

    private var fusedLocationProviderClient: FusedLocationProviderClient? = null
    private var mMap: GoogleMap? = null
    private val DEFAULT_ZOOM = 15f

    lateinit var mapView: MapView

    lateinit var svSearchViewPickup: SearchView

    var screen = ""
    var LOGIN_TOKEN = ""
    var USER_ID_KEY = ""
    private val MAP_VIEW_BUNDLE_KEY = "MapViewBundleKey"
    lateinit var pojoWithData : offerRideModel

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
        setContentView(R.layout.activity_show_map_pick_up)

        svSearchViewPickup = findViewById<SearchView>(R.id.svAddSearchPickup)

        var cv = findViewById<ImageView>(R.id.ivCloseScreen)

        val bundle: Bundle? = intent.extras
        screen = bundle!!.getString("screen")!!

        pojoWithData = offerRideModel()

        LOGIN_TOKEN = Utils.getStringFromPreferences(Configure.LOGIN_KEY,"",this)!!
        USER_ID_KEY = Utils.getStringFromPreferences(Configure.USER_ID_KEY,"",this)!!

        if (screen == "Drop") {
            tvTitleTool.text = "Drop-off"
        }

            Utils.checkConnection(this@ShowMapActivityPickUp,cv)
            if (!Utils.CheckGpsStatus(this@ShowMapActivityPickUp)) {
                Utils.enableGPS(this@ShowMapActivityPickUp)
            }

        askGalleryPermissionLocation()

        var mapViewBundle: Bundle? = null
        if (savedInstanceState != null) {
            askGalleryPermissionLocation()
            mapViewBundle = savedInstanceState.getBundle(MAP_VIEW_BUNDLE_KEY)
        }

        mapView = findViewById(R.id.map1)
        mapView.onCreate(mapViewBundle)
        mapView.getMapAsync(this)

        tvCurrentAddress = findViewById<TextView>(R.id.tvCurrentAddress)

        svSearchViewPickup.onActionViewExpanded()

        svSearchViewPickup.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(p0: String?): Boolean {

                var location = svSearchViewPickup.query.toString()
                var addressList : List<Address>? = null;
                if (location != null || location!= "") {
                    var geocoder = Geocoder(this@ShowMapActivityPickUp)
                    try {
                        addressList= geocoder.getFromLocationName(location,1)
                    }catch (e : Exception) {
                        e.printStackTrace()
                    }

                    if (addressList!!.size != 0) {
                        var address = addressList!!.get(0)
                        var latLng = LatLng(address.latitude,address.longitude)
                        mMap!!.addMarker(MarkerOptions().position(latLng).title(location))
                        mMap!!.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng,10f))
                    } else {

                        Toast.makeText(this@ShowMapActivityPickUp,"Enter valid City Name",
                            Toast.LENGTH_LONG).show()
                    }
                }

                return false
            }

            override fun onQueryTextChange(p0: String?): Boolean {

                return false
            }



        })

        mapView.getMapAsync(this)


        ivCloseScreen.setOnClickListener {
            onBackPressed()
        }


        ivBack.setOnClickListener {
            onBackPressed()
        }

        tvAddSearchPickUp.addTextChangedListener(object : TextWatcher {
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
            tvAddSearchPickUp.setText("")
        }

        fabDone.setOnClickListener {
            checkFields()
        }

        rlCurrentLoc.setOnClickListener {
//            rlCurrentLoc.visibility = View.GONE
            ivMap.visibility = View.VISIBLE
            ivMap.elevation = 10F

            var animZoomIn = AnimationUtils.loadAnimation(getApplicationContext(),
                 R.anim.fade_in);
            ivMap.startAnimation(animZoomIn);

        }

    }

    private fun checkFields() {

        if (tvAddSearchPickUp.text.toString().isEmpty() || tvAddSearchPickUp.text.toString() == "") {
            Toast.makeText(this,"Please Select Correct PickUp Address",
                Toast.LENGTH_LONG).show()
        } else if (svSearchViewPickup.query.toString().isEmpty() || svSearchViewPickup.query.toString() == "") {
            Toast.makeText(this,"Please Select Correct PickUp Address",
                Toast.LENGTH_LONG).show()
        } else {
            pojoWithData.user = USER_ID_KEY
            pojoWithData.leaving =  tvAddSearchPickUp.text.toString()


            var int = Intent(this,
                ShowMapActivityDrop::class.java)
            val bundle =
                ActivityOptionsCompat.makeCustomAnimation(
                    this ,
                    R.anim.fade_in, R.anim.fade_out
                ).toBundle()
            int.putExtra("pojoWithData",pojoWithData)
            startActivity(int,bundle)
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
            LocationServices.getFusedLocationProviderClient(this@ShowMapActivityPickUp)

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

                tvAddSearchPickUp!!.setText(addresses.getAddressLine(0))

                if ( addresses.locality != null && addresses.locality.isNotEmpty() ) {
                    pojoWithData.lline = addresses.adminArea
                    pojoWithData.lcity = addresses.locality
                }
                if ( addresses.longitude != null && addresses.latitude != null ) {
                    pojoWithData.llat = addresses.latitude.toString()
                    pojoWithData.llog = addresses.longitude.toString()
                }
            }
            if (addresses.getAddressLine(1) != null) {

                tvCurrentAddress!!.setText(
                    tvCurrentAddress.getText().toString() + addresses.getAddressLine(
                        1
                    )
                )


                tvAddSearchPickUp!!.setText(
                    tvAddSearchPickUp.getText().toString() + addresses.getAddressLine(
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

            pojoWithData.llat = location.latitude.toString()
            pojoWithData.llog = location.longitude.toString()


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
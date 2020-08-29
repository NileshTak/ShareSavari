package com.ddsio.productionapp.sharesavari.OfferScreen

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
import android.widget.TextView
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
import com.ddsio.productionapp.sharesavari.CommonUtils.Utils
import com.ddsio.productionapp.sharesavari.R
import com.productionapp.amhimemekar.CommonUtils.offerRideModel
import io.github.inflationx.viewpump.ViewPumpContextWrapper
import kotlinx.android.synthetic.main.activity_show_map.fabDone
import kotlinx.android.synthetic.main.activity_show_map.ivBack
import kotlinx.android.synthetic.main.activity_show_map.ivClear
import kotlinx.android.synthetic.main.activity_show_map.ivMap
import kotlinx.android.synthetic.main.activity_show_map.rlCurrentLoc
import kotlinx.android.synthetic.main.activity_show_map_drop.*
import kotlinx.android.synthetic.main.activity_show_map_pick_up.*
import kotlinx.android.synthetic.main.activity_show_map_pick_up.ivCloseScreen
import java.io.IOException
import java.util.*

class ShowMapActivityDrop : AppCompatActivity(), OnMapReadyCallback, LocationListener, GoogleMap.OnCameraMoveListener, GoogleMap.OnCameraMoveStartedListener, GoogleMap.OnCameraIdleListener {

    lateinit var tvCurrentAddress: TextView

    private var fusedLocationProviderClient: FusedLocationProviderClient? = null
    private var mMap: GoogleMap? = null
    private val DEFAULT_ZOOM = 15f

    lateinit var mapView: MapView

   lateinit var pojoWithData : offerRideModel


    private val MAP_VIEW_BUNDLE_KEY = "MapViewBundleKey"

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
        setContentView(R.layout.activity_show_map_drop)

        val bundle: Bundle? = intent.extras
          pojoWithData = bundle!!.get("pojoWithData") as offerRideModel

        var mapViewBundle: Bundle? = null
        if (savedInstanceState != null) {
            mapViewBundle = savedInstanceState.getBundle(MAP_VIEW_BUNDLE_KEY)
        }

        mapView = findViewById(R.id.map1)
        mapView.onCreate(mapViewBundle)
        mapView.getMapAsync(this)

        tvCurrentAddress = findViewById<TextView>(R.id.tvCurrentAddress)

        ivCloseScreen.setOnClickListener {
            onBackPressed()
        }


        ivBack.setOnClickListener {
            onBackPressed()
        }

        fabNextDate.setOnClickListener {

            pojoWithData.going =  tvAddSearchDrop.text.toString()

            var int = Intent(this,
                GoingDateAndTime::class.java)
            val bundle =
                ActivityOptionsCompat.makeCustomAnimation(
                    this ,
                    R.anim.fade_in, R.anim.fade_out
                ).toBundle()
            int.putExtra("pojoWithData",pojoWithData)
            startActivity(int,bundle)
        }

        tvAddSearchDrop.addTextChangedListener(object : TextWatcher {
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
                    ivClear.visibility = View.VISIBLE
                }

            }
        })

        ivClear.setOnClickListener {
            tvAddSearchDrop.setText("")
        }

//        fabDone.setOnClickListener {
////            Utils.writeStringToPreferences("selectedAddress",tvCurrentAddress.text.toString(),this)
////            EventBus.getDefault().post(tvCurrentAddress.text.toString());
////            onBackPressed()
//
////            if (screen == "Drop") {
//
//                var int = Intent(this,
//                    ShowMapActivityDrop::class.java)
//                val bundle =
//                    ActivityOptionsCompat.makeCustomAnimation(
//                        this ,
//                        R.anim.fade_in, R.anim.fade_out
//                    ).toBundle()
//                int.putExtra("screen","Drop")
//                startActivity(int,bundle)
////            }
////            else if (screen != "Offer" && screen != "Drop"){
////
////                Toast.makeText(this,"Booked",Toast.LENGTH_LONG).show()
////                var int = Intent(this,
////                    MainActivity::class.java)
////                val bundle =
////                    ActivityOptionsCompat.makeCustomAnimation(
////                        this ,
////                        R.anim.fade_in, R.anim.fade_out
////                    ).toBundle()
////                startActivity(int,bundle)
////                finishAffinity()
////
////            }
//
//
//        }

        rlCurrentLoc.setOnClickListener {
//            rlCurrentLoc.visibility = View.GONE
            ivMap.visibility = View.VISIBLE
            ivMap.elevation = 10F

            var animZoomIn = AnimationUtils.loadAnimation(getApplicationContext(),
                 R.anim.fade_in);
            ivMap.startAnimation(animZoomIn);

//            Handler().postDelayed(
//                Runnable {
//                    var aniZoom = AnimationUtils.loadAnimation(getApplicationContext(),
//          R.anim.fade_out);
//                    ivMap.startAnimation(aniZoom)
//
//                    Handler().postDelayed(
//                        Runnable {
//                            ivMap.visibility = View.GONE
//                            ivMap.elevation = 0F
//
//                        },1000
//                    )
//
//                },4000
//            )
        }

    }




    public override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)

        var mapViewBundle = outState.getBundle(MAP_VIEW_BUNDLE_KEY)
        if (mapViewBundle == null) {
            mapViewBundle = Bundle()
            outState.putBundle(MAP_VIEW_BUNDLE_KEY, mapViewBundle)
        }

        mapView.onSaveInstanceState(mapViewBundle)
    }




    private fun getCurrentLocation() {

        fusedLocationProviderClient =
            LocationServices.getFusedLocationProviderClient(this@ShowMapActivityDrop)

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

                tvAddSearchDrop!!.setText(addresses.getAddressLine(0))


                pojoWithData.gline = addresses.adminArea
                pojoWithData.gcity = addresses.locality
            }
            if (addresses.getAddressLine(1) != null) {

                tvCurrentAddress!!.setText(
                    tvCurrentAddress.getText().toString() + addresses.getAddressLine(
                        1
                    )
                )

                tvAddSearchDrop!!.setText(
                    tvAddSearchDrop.getText().toString() + addresses.getAddressLine(
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

            pojoWithData.glat = location.latitude.toString()
            pojoWithData.glog = location.longitude.toString()

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
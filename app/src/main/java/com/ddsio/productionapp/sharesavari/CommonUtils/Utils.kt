package com.ddsio.productionapp.sharesavari.CommonUtils

import android.content.Context
import android.content.Intent
import android.location.Address
import android.location.Geocoder
import android.location.LocationManager
import android.net.ConnectivityManager
import android.net.NetworkInfo
import android.os.Handler
import android.preference.PreferenceManager
import android.provider.Settings
import android.view.LayoutInflater
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat.startActivity
import com.android.volley.DefaultRetryPolicy
import com.android.volley.Request
import com.ddsio.productionapp.sharesavari.MainActivity
import com.ddsio.productionapp.sharesavari.R
import com.google.android.material.snackbar.Snackbar
import org.greenrobot.eventbus.EventBus
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileInputStream
import java.io.IOException
import java.util.*


object Utils {
    fun writeStringToPreferences(key: String, value: String, activity: Context?) {
        if (activity == null) {
            return
        }
        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(activity)
        val sharedPrefEditor = sharedPreferences.edit()
        sharedPrefEditor.putString(key, value)
        sharedPrefEditor.apply()
    }

    fun getStringFromPreferences(key: String, defaultValue: String, activity: Context?): String? {
        if (activity == null) {
            return defaultValue
        }
        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(activity)
        return sharedPreferences.getString(key, defaultValue)
    }

    fun CheckGpsStatus(context: Context) : Boolean {
       var locationManager =
            context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        var GpsStatus : Boolean = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
        return GpsStatus
    }




    fun enableGPS(context: Context) : Boolean {

          var res  = false

        val dialog =
            AlertDialog.Builder(context).create()

          dialog.setCancelable(false);
          val view: View =
            LayoutInflater.from(context).inflate(R.layout.custom_enable_gps, null)
        var cvCancel = view.findViewById<CardView>(R.id.cvCancel)
        var cvEnable = view.findViewById<CardView>(R.id.cvEnable)


        cvEnable.setOnClickListener {
            var int = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
            res = true

            context.startActivity(int);
            dialog.setCancelable(true);
            dialog.dismiss()
        }

        cvCancel.setOnClickListener {

            res = false
            dialog.dismiss()

        }


        dialog.setView(view)

        dialog.show()

          return  res
    }


    fun setVolleyRetryPolicy(req: Request<*>) {
        req.retryPolicy = DefaultRetryPolicy(
            40000,
            DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
            DefaultRetryPolicy.DEFAULT_BACKOFF_MULT
        )
    }

    fun getAddreddFromLatLong(context: Context, lat: Double, lng: Double): Address {

        var addresses: List<Address>? = null
        val geocoder = Geocoder(context, Locale.getDefault())
        try {

            addresses = geocoder.getFromLocation(lat, lng, 1)

            return addresses!![0]

        } catch (e: IndexOutOfBoundsException) {
            e.printStackTrace()
        } catch (e: IOException) {
            e.printStackTrace()
        }

        return Address(Locale(""))

    }



    fun showSnackMSG(view: View,msg : String) {
        val snackbar = Snackbar
            .make(view, msg, Snackbar.LENGTH_LONG)
            .setAction("Okey") {   }

        snackbar.view.setBackgroundResource(R.drawable.gradient)
        snackbar.view.elevation = 6.0f
        snackbar.setActionTextColor(view.resources.getColor(R.color.md_white_1000))

        snackbar.show()


    }

    fun fileToBytes(input: File?): ByteArray? {
        var objFileIS: FileInputStream? = null
        return try {
            objFileIS = FileInputStream(input)
            val objByteArrayOS = ByteArrayOutputStream()
            val byteBufferString = ByteArray(1024)
            var readNum: Int
            while (objFileIS.read(byteBufferString).also { readNum = it } != -1) {
                objByteArrayOS.write(byteBufferString, 0, readNum)
            }
            objByteArrayOS.toByteArray()
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }


    fun checkConnection(context: Context, view: View) : Boolean {
        val handler = Handler()
        val delay = 4000 //milliseconds
        var boo = false

        handler.postDelayed(object : Runnable {
            override fun run() {
                if (!isNetworkAvailable(
                        context
                    )
                ) {
                    showSnack(
                        view
                    )
                    boo = true
                }
                handler.postDelayed(this, delay.toLong())
            }
        }, delay.toLong())

        return boo
    }


    private fun showSnack(view: View) {
        val snackbar = Snackbar
            .make(view, "Low Internet Connection", Snackbar.LENGTH_LONG)
            .setAction("Okey") { view ->
//                showSnack(view)

            }

//        snackbar.view.setBackgroundResource(R.drawable.gradient)
        snackbar.view.elevation = 6.0f
//        snackbar.setActionTextColor(view.resources.getColor(R.color.md_white_1000))

        snackbar.show()


    }


    fun isNetworkAvailable(context: Context): Boolean {
        var status = false
        try {
            val cm = context
                .getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            var netInfo = cm.getNetworkInfo(0)

            if (netInfo != null && netInfo.state == NetworkInfo.State.CONNECTED) {
                status = true
            } else {
                netInfo = cm.getNetworkInfo(1)
                if (netInfo != null && netInfo.state == NetworkInfo.State.CONNECTED)
                    status = true
            }
        } catch (e: Exception) {
            e.printStackTrace()
            return false
        }

        return status
    }


}
package com.productionapp.amhimemekar.CommonUtils

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkInfo
import android.os.Handler
import android.preference.PreferenceManager
import android.view.View
import com.google.android.material.snackbar.Snackbar
import com.nil.productionapp.sharesavari.R
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileInputStream

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
                if (!isNetworkAvailable(context)) {
                    showSnack(view)
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
            .setAction("Okey") { view -> showSnack(view) }

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
package com.productionapp.amhimemekar.CommonUtils

import android.content.Context
import android.preference.PreferenceManager
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

}
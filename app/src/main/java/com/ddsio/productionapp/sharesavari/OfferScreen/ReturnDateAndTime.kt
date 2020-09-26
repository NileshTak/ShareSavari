package com.ddsio.productionapp.sharesavari.OfferScreen

import android.Manifest
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.view.View
import android.widget.ImageView
import android.widget.TimePicker
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityOptionsCompat
import androidx.fragment.app.DialogFragment
import com.ddsio.productionapp.sharesavari.CommonUtils.TimePickerFragment
import com.ddsio.productionapp.sharesavari.CommonUtils.Utils
import com.ddsio.productionapp.sharesavari.R
import com.github.florent37.runtimepermission.kotlin.askPermission
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.productionapp.amhimemekar.CommonUtils.offerRideModel
import kotlinx.android.synthetic.main.activity_going_date_and_time.*
import kotlinx.android.synthetic.main.activity_number_of_passeners_to_take.*
import kotlinx.android.synthetic.main.activity_return_date_and_time.*
import kotlinx.android.synthetic.main.activity_show_map_drop.*
import java.text.SimpleDateFormat
import java.util.*


class ReturnDateAndTime : AppCompatActivity(),TimePickerFragment.TimePickerListener {

    lateinit var datePickerdialog: DatePickerDialog

    lateinit var pojoWithData : offerRideModel

    val currentDate = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())
    var formate = SimpleDateFormat("yyyy-MM-dd", Locale.US)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_return_date_and_time)

        val bundle: Bundle? = intent.extras
        pojoWithData = bundle!!.get("pojoWithData") as offerRideModel
        var cv = findViewById<FloatingActionButton>(R.id.fabNextReturn)

        askGalleryPermissionLocation()
            Utils.checkConnection(this@ReturnDateAndTime,cv)
            if (!Utils.CheckGpsStatus(this@ReturnDateAndTime)) {
                Utils.enableGPS(this@ReturnDateAndTime)
            }

        etSelectDateReturn.setOnClickListener {
            datePicker()
        }

        etSelectDateReturn.setOnFocusChangeListener(object : View.OnFocusChangeListener {
            override fun onFocusChange(p0: View?, p1: Boolean) {
                if (p1 == true) {
                    datePicker()
                }
            }
        })


        etReachBDate.setOnFocusChangeListener(object : View.OnFocusChangeListener {
            override fun onFocusChange(p0: View?, p1: Boolean) {
                if (p1 == true) {
                    datePickerReach()
                }
            }
        })


        etReachBDate.setOnClickListener {
            datePickerReach()
        }

        etReachBTime.setOnClickListener {
            shoeReachTimeDialog()
        }


        etReachBTime.setOnFocusChangeListener(object : View.OnFocusChangeListener {
            override fun onFocusChange(p0: View?, p1: Boolean) {
                if (p1 == true) {
                    shoeReachTimeDialog()
                }
            }
        })

        ivBackReturn.setOnClickListener {
            onBackPressed()
        }

        etSelectTimeReturn.setOnClickListener {
            val timePickerFragment: DialogFragment = TimePickerFragment()
            timePickerFragment.setCancelable(false)
            timePickerFragment.show(supportFragmentManager, "timePicker")
        }

        etSelectTimeReturn.setOnFocusChangeListener(object : View.OnFocusChangeListener {
            override fun onFocusChange(p0: View?, p1: Boolean) {
                if (p1 == true) {
                    val timePickerFragment: DialogFragment = TimePickerFragment()
                    timePickerFragment.setCancelable(false)
                    timePickerFragment.show(supportFragmentManager, "timePicker")
                }
            }
        })

        fabNextReturn.setOnClickListener {
         checkFields()
        }

    }

    private fun shoeReachTimeDialog() {
        val c = Calendar.getInstance()
        var mHour = c[Calendar.HOUR_OF_DAY]
        var mMinute = c[Calendar.MINUTE]

        // Launch Time Picker Dialog

        val timePickerDialog = TimePickerDialog(
            this,
            object : TimePickerDialog.OnTimeSetListener  {
                override fun onTimeSet(
                    view: TimePicker?, hourOfDay: Int,
                    minute: Int
                ) {
                    etReachBTime.setText("$hourOfDay : $minute")

                    pojoWithData.brtime = "$hourOfDay:$minute"
                }
            }, mHour, mMinute, false
        )
        timePickerDialog.show()
    }


    private fun datePickerReach() {
        val now = Calendar.getInstance()
        datePickerdialog = DatePickerDialog(
            this@ReturnDateAndTime, DatePickerDialog.OnDateSetListener { view, year, month, dayOfMonth ->
                val selectedDate = Calendar.getInstance()
                selectedDate.set(Calendar.YEAR, year)
                selectedDate.set(Calendar.MONTH, month)
                selectedDate.set(Calendar.DAY_OF_MONTH, dayOfMonth)
                val date = formate.format(selectedDate.time)
                etReachBDate.setText(date)
                pojoWithData.brdate = date
            },
            now.get(Calendar.YEAR), now.get(Calendar.MONTH), now.get(Calendar.DAY_OF_MONTH)
        )
        datePickerdialog.show()
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

        if (etSelectDateReturn.text.toString().isEmpty() || etSelectDateReturn.text.toString() == "") {
            Toast.makeText(this,"Please Select Correct Drop Date ",
                Toast.LENGTH_LONG).show()
        } else  if (etSelectTimeReturn.text.toString().isEmpty() || etSelectTimeReturn.text.toString() == "") {
            Toast.makeText(this,"Please Select Correct Drop Time",
                Toast.LENGTH_LONG).show()
        }else if (etReachBDate.text.toString().isEmpty() || etReachBDate.text.toString() == "") {
            Toast.makeText(this,"Please Select Correct Reaching Date ",
                Toast.LENGTH_LONG).show()
        } else  if (etReachBTime.text.toString().isEmpty() || etReachBTime.text.toString() == "") {
            Toast.makeText(this,"Please Select Correct Reaching Time",
                Toast.LENGTH_LONG).show()
        } else  if (etReachBDate.text.toString() < etSelectDateReturn.text.toString() ) {
            Toast.makeText(this,"Reaching Date should be greater then or equals to Leaving Date",
                Toast.LENGTH_LONG).show()
        }else  if  (pojoWithData.date.toString()  > etSelectDateReturn.text.toString() ) {
            Toast.makeText(this,"Return Date should be greater then or equals to Leaving Date",
                Toast.LENGTH_LONG).show()
        } else {
            var int = Intent(this,
                MaxSeat::class.java)
            val bundle =
                ActivityOptionsCompat.makeCustomAnimation(
                    this ,
                    R.anim.fade_in, R.anim.fade_out
                ).toBundle()
            int.putExtra("pojoWithData",pojoWithData)
            startActivity(int,bundle)
        }
    }

    override fun onTimeSet(timePicker: TimePicker?, hour: Int, minute: Int) {

        etSelectTimeReturn.setText(" $hour : $minute")

        pojoWithData.rtime = "$hour:$minute"
    }

    private fun datePicker() {
        val now = Calendar.getInstance()
        datePickerdialog = DatePickerDialog(
            this@ReturnDateAndTime, DatePickerDialog.OnDateSetListener { view, year, month, dayOfMonth ->
                val selectedDate = Calendar.getInstance()
                selectedDate.set(Calendar.YEAR, year)
                selectedDate.set(Calendar.MONTH, month)
                selectedDate.set(Calendar.DAY_OF_MONTH, dayOfMonth)
                val date = formate.format(selectedDate.time)
                etSelectDateReturn.setText(date)
                pojoWithData.rdate = date
            },
            now.get(Calendar.YEAR), now.get(Calendar.MONTH), now.get(Calendar.DAY_OF_MONTH)
        )
        datePickerdialog.show()
    }
}
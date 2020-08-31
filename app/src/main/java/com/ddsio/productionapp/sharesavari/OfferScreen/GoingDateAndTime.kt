package com.ddsio.productionapp.sharesavari.OfferScreen

import android.app.DatePickerDialog
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.TimePicker
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityOptionsCompat
import androidx.fragment.app.DialogFragment
import com.ddsio.productionapp.sharesavari.CommonUtils.TimePickerFragment
import com.ddsio.productionapp.sharesavari.R
import com.productionapp.amhimemekar.CommonUtils.offerRideModel
import kotlinx.android.synthetic.main.activity_going_date_and_time.*
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.activity_return_date_and_time.*
import kotlinx.android.synthetic.main.activity_show_map.*
import kotlinx.android.synthetic.main.activity_show_map_drop.*
import kotlinx.android.synthetic.main.activity_show_map_pick_up.*
import java.text.SimpleDateFormat
import java.util.*


class GoingDateAndTime : AppCompatActivity(),TimePickerFragment.TimePickerListener {

    lateinit var datePickerdialog: DatePickerDialog

    lateinit var pojoWithData : offerRideModel

    val currentDate = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())
    var formate = SimpleDateFormat("yyyy-MM-dd", Locale.US)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_going_date_and_time)

        val bundle: Bundle? = intent.extras
        pojoWithData = bundle!!.get("pojoWithData") as offerRideModel




        etSelectDate.setOnFocusChangeListener(object : View.OnFocusChangeListener {
            override fun onFocusChange(p0: View?, p1: Boolean) {
                if (p1 == true) {
                    datePicker()
                }
            }
        })



        etSelectDate.setOnClickListener {
            datePicker()
        }


        etSelectTime.setOnClickListener {
            val timePickerFragment: DialogFragment = TimePickerFragment()
            timePickerFragment.setCancelable(false)
            timePickerFragment.show(supportFragmentManager, "timePicker")
        }


        etSelectTime.setOnFocusChangeListener(object : View.OnFocusChangeListener {
            override fun onFocusChange(p0: View?, p1: Boolean) {
                if (p1 == true) {
                    val timePickerFragment: DialogFragment = TimePickerFragment()
                    timePickerFragment.setCancelable(false)
                    timePickerFragment.show(supportFragmentManager, "timePicker")
                }
            }
        })

        ivBackGoing.setOnClickListener {
            onBackPressed()
        }


        fabNext.setOnClickListener {
          checkFields()
        }

    }


    private fun checkFields() {

        if (etSelectDate.text.toString().isEmpty() || etSelectDate.text.toString() == "") {
            Toast.makeText(this,"Please Select Correct Leaving Date ",
                Toast.LENGTH_LONG).show()
        } else  if (etSelectTime.text.toString().isEmpty() || etSelectTime.text.toString() == "") {
            Toast.makeText(this,"Please Select Correct Leaving Time",
                Toast.LENGTH_LONG).show()
        }  else {
            var int = Intent(this,
                ReturnDateAndTime::class.java)
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
        etSelectTime.setText(" $hour : $minute")

        pojoWithData.time = "$hour:$minute"
    }

    private fun datePicker() {
        val now = Calendar.getInstance()
        datePickerdialog = DatePickerDialog(
            this@GoingDateAndTime, DatePickerDialog.OnDateSetListener { view, year, month, dayOfMonth ->
                val selectedDate = Calendar.getInstance()
                selectedDate.set(Calendar.YEAR, year)
                selectedDate.set(Calendar.MONTH, month)
                selectedDate.set(Calendar.DAY_OF_MONTH, dayOfMonth)
                val date = formate.format(selectedDate.time)
                etSelectDate.setText(date)
                pojoWithData.date = date
            },
            now.get(Calendar.YEAR), now.get(Calendar.MONTH), now.get(Calendar.DAY_OF_MONTH)
        )
        datePickerdialog.show()
    }
}
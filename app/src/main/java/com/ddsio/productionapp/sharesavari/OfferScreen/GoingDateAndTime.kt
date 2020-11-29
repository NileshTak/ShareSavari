package com.ddsio.productionapp.sharesavari.OfferScreen

import android.Manifest
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
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
import com.productionapp.amhimemekar.CommonUtils.BookRideScreenFetchCity
import com.productionapp.amhimemekar.CommonUtils.offerRideModel
import io.github.inflationx.viewpump.ViewPumpContextWrapper
import kotlinx.android.synthetic.main.activity_going_date_and_time.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import java.text.SimpleDateFormat
import java.util.*


class GoingDateAndTime : AppCompatActivity(),TimePickerFragment.TimePickerListener {

    lateinit var datePickerdialog: DatePickerDialog

    lateinit var pojoWithData : offerRideModel

    var reachHour = ""

    var goingHour = ""

    var reachMin = ""

    var goingMin = ""

    val currentDate = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())
    var formate = SimpleDateFormat("yyyy-MM-dd", Locale.US)


    override fun attachBaseContext(newBase: Context) {
        super.attachBaseContext(ViewPumpContextWrapper.wrap(newBase))
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_going_date_and_time)

        val bundle: Bundle? = intent.extras
        pojoWithData = bundle!!.get("pojoWithData") as offerRideModel

        askGalleryPermissionLocation()


        var cv = findViewById<FloatingActionButton>(R.id.fabNext)

            Utils.checkConnection(this@GoingDateAndTime,cv)
            if (!Utils.CheckGpsStatus(this@GoingDateAndTime)) {
                Utils.enableGPS(this@GoingDateAndTime)
            }


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


        etReachDate.setOnFocusChangeListener(object : View.OnFocusChangeListener {
            override fun onFocusChange(p0: View?, p1: Boolean) {
                if (p1 == true) {
                    datePickerReach()
                }
            }
        })



        etReachDate.setOnClickListener {
            datePickerReach()
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


        etReachTime.setOnClickListener {
           shoeReachTimeDialog()
        }


        etReachTime.setOnFocusChangeListener(object : View.OnFocusChangeListener {
            override fun onFocusChange(p0: View?, p1: Boolean) {
                if (p1 == true) {
                    shoeReachTimeDialog()
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


    override fun onStart() {
        super.onStart()
        if (!EventBus.getDefault().isRegistered(this@GoingDateAndTime)) EventBus.getDefault().register(this)
    }

    override fun onDestroy() {
        super.onDestroy()
        EventBus.getDefault().unregister(this@GoingDateAndTime)
    }


    @Subscribe
    fun OnAddSelected(add : BookRideScreenFetchCity?) {

        Log.d("CITYIS",add!!.city.toString() +"jubhjbjbj")
        if (add!!.type == "stop") {
            tvStop.text = add!!.city
            pojoWithData.stitle = add!!.city
            pojoWithData.slat = add.lat
            pojoWithData.slog = add.long

        }
    }

    private fun shoeReachTimeDialog() {
        val c = Calendar.getInstance()
        var mHour = c[Calendar.HOUR_OF_DAY]
        var mMinute = c[Calendar.MINUTE]

        // Launch Time Picker Dialog

        // Launch Time Picker Dialog
        val timePickerDialog = TimePickerDialog(
            this,android.R.style.Theme_Holo_Dialog,
            object : TimePickerDialog.OnTimeSetListener  {
                override fun onTimeSet(
                    view: TimePicker?, hourOfDay: Int,
                    minute: Int
                ) {
                    etReachTime.setText("$hourOfDay : $minute")

                    reachHour = "$hourOfDay"
                    reachMin = "$minute"

                    pojoWithData.tdtime = "$hourOfDay:$minute"
                }
            }, mHour, mMinute, false
        )
        timePickerDialog.show()
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

        Log.d("ddddddddd",etReachDate.text.toString() +"  "+ etSelectDate.text.toString() +"  "+ reachHour +"  "+ goingHour)

        if (etSelectDate.text.toString().isEmpty() || etSelectDate.text.toString() == "") {
            Toast.makeText(this,"Please Select Correct Leaving Date ",
                Toast.LENGTH_LONG).show()
        } else  if (etSelectTime.text.toString().isEmpty() || etSelectTime.text.toString() == "") {
            Toast.makeText(this,"Please Select Correct Leaving Time",
                Toast.LENGTH_LONG).show()
        } else if (etReachDate.text.toString().isEmpty() || etReachDate.text.toString() == "") {
            Toast.makeText(this,"Please Select Correct Reaching Date ",
                Toast.LENGTH_LONG).show()
        } else  if (etReachTime.text.toString().isEmpty() || etReachTime.text.toString() == "") {
            Toast.makeText(this,"Please Select Correct Reaching Time",
                Toast.LENGTH_LONG).show()
        }else  if (etReachDate.text.toString() < etSelectDate.text.toString() ) {
            Toast.makeText(this,"Reaching Date should be greater then or equals to Leaving Date",
                Toast.LENGTH_LONG).show()
        } else  if (etReachDate.text.toString() == etSelectDate.text.toString() && reachHour.toInt()  < goingHour.toInt() ) {

                Toast.makeText(this,"Reaching Time should be greater then or equals to Leaving Time",
                    Toast.LENGTH_LONG).show()

        }else  if (etReachDate.text.toString() == etSelectDate.text.toString() && reachHour.toInt()  == goingHour.toInt() && reachMin.toInt()  < goingMin.toInt()  ) {

            Toast.makeText(this,"Reaching Time should be greater then or equals to Leaving Time",
                Toast.LENGTH_LONG).show()

        } else {

            pojoWithData.stitle = etStopPoint.text.toString()
            pojoWithData.slog = "0"
            pojoWithData.slat = "0"

            if (cvReturnRide.isChecked) {
                pojoWithData.is_return = "true"

                var int = Intent(this,
                    ReturnDateAndTime::class.java)
                val bundle =
                    ActivityOptionsCompat.makeCustomAnimation(
                        this ,
                        R.anim.fade_in, R.anim.fade_out
                    ).toBundle()
                int.putExtra("pojoWithData",pojoWithData)
                startActivity(int,bundle)
            } else {
                pojoWithData.rtime = pojoWithData.time
                pojoWithData.rdate = pojoWithData.date

                pojoWithData.brtime = pojoWithData.time
                pojoWithData.brdate = pojoWithData.date

                pojoWithData.is_return = "false"

                var int = Intent(this,
                    MaxSeat::class.java)
                val bundle =
                    ActivityOptionsCompat.makeCustomAnimation(
                        this ,
                        R.anim.fade_in, R.anim.fade_out
                    ).toBundle()
                int.putExtra("pojoWithData",pojoWithData)
                int.putExtra("stoppointReturn", "")
                startActivity(int,bundle)
            }


        }
    }


    fun getFirstWord(str : String) : String {
        val arr = str.split(" ".toRegex(), 3).toTypedArray()

        val firstWord = arr[0]

        Log.d("dddd",firstWord)

        return  firstWord
    }


    override fun onTimeSet(timePicker: TimePicker?, hour: Int, minute: Int) {
        etSelectTime.setText(" $hour : $minute")

        goingHour = "$hour"
        goingMin = "$minute"
        pojoWithData.time = "$hour:$minute"
    }

    private fun datePicker() {
        val now = Calendar.getInstance()
        datePickerdialog = DatePickerDialog(
            this@GoingDateAndTime,android.R.style.Theme_Holo_Dialog, DatePickerDialog.OnDateSetListener { view, year, month, dayOfMonth ->
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


    private fun datePickerReach() {
        val now = Calendar.getInstance()
        datePickerdialog = DatePickerDialog(
            this@GoingDateAndTime,android.R.style.Theme_Holo_Dialog, DatePickerDialog.OnDateSetListener { view, year, month, dayOfMonth ->
                val selectedDate = Calendar.getInstance()
                selectedDate.set(Calendar.YEAR, year)
                selectedDate.set(Calendar.MONTH, month)
                selectedDate.set(Calendar.DAY_OF_MONTH, dayOfMonth)
                val date = formate.format(selectedDate.time)
                etReachDate.setText(date)
                pojoWithData.tddate = date
            },
            now.get(Calendar.YEAR), now.get(Calendar.MONTH), now.get(Calendar.DAY_OF_MONTH)
        )
        datePickerdialog.show()
    }
}
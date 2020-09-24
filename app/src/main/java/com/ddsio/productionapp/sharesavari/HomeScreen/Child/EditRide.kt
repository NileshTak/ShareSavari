package com.ddsio.productionapp.sharesavari.HomeScreen.Child

import android.app.DatePickerDialog
import android.app.ProgressDialog
import android.app.TimePickerDialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.widget.RadioButton
import android.widget.TimePicker
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.fragment.app.DialogFragment
import com.android.volley.*
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.ddsio.productionapp.sharesavari.CommonUtils.TimePickerFragment
import com.ddsio.productionapp.sharesavari.CommonUtils.Utils
import com.ddsio.productionapp.sharesavari.MainActivity
import com.ddsio.productionapp.sharesavari.R
import com.ddsio.productionapp.sharesavari.SearchScreen.child.RideDetails
import com.ddsio.productionapp.sharesavari.ShowMap.ShowMapActivity
import com.google.gson.Gson
import com.productionapp.amhimemekar.CommonUtils.*
import io.github.inflationx.viewpump.ViewPumpContextWrapper
import kotlinx.android.synthetic.main.activity_edit_ride.*
import kotlinx.android.synthetic.main.activity_edit_ride.etStopPoint
import kotlinx.android.synthetic.main.activity_edit_ride.rbBookInstantly
import kotlinx.android.synthetic.main.activity_edit_ride.rbMyself
import kotlinx.android.synthetic.main.activity_going_date_and_time.*
import kotlinx.android.synthetic.main.activity_number_of_passeners_to_take.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.json.JSONException
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.*

class EditRide : AppCompatActivity(),TimePickerFragment.TimePickerListener {


    var count = 1
    var request: RequestQueue? = null
    lateinit var pojoWithData : offerRideModel
    lateinit var previousPojo : BookRidesPojoItem
    var totalAmt = 0
    var maxSeatCount = ""
    lateinit var datePickerdialog: DatePickerDialog

    val currentDate = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())
    var formate = SimpleDateFormat("yyyy-MM-dd", Locale.US)

    var LOGIN_TOKEN = ""
    var USER_UPDATE_ID = ""
    lateinit var USER_ID_KEY : String
    lateinit var progressDialog: ProgressDialog
    var pets = ""
    var smoking = ""
    var cbBookInstant = ""

    override fun attachBaseContext(newBase: Context) {
        super.attachBaseContext(ViewPumpContextWrapper.wrap(newBase))
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_ride)


        var cv = findViewById<CardView>(R.id.cvMinusSave)

        LOGIN_TOKEN = Utils.getStringFromPreferences(Configure.LOGIN_KEY,"",this)!!
        USER_UPDATE_ID = Utils.getStringFromPreferences(Configure.USER_UPDATE_ID,"",this)!!
        USER_ID_KEY = Utils.getStringFromPreferences(Configure.USER_ID_KEY,"",this)!!
        val bundle: Bundle? = intent.extras
        pojoWithData = offerRideModel()

        previousPojo = bundle!!.get("pojoWithData") as BookRidesPojoItem

        pojoWithData.comment = previousPojo.comment
        pojoWithData.date= previousPojo.date
        pojoWithData.gcity=  previousPojo.gcity
        pojoWithData.glat= previousPojo.glat
        pojoWithData.gline= previousPojo.gline
        pojoWithData.glog = previousPojo.glog
        pojoWithData.going= previousPojo.going
        pojoWithData.id= previousPojo.id.toString()
        pojoWithData.image= previousPojo.image
        pojoWithData.is_return= previousPojo.is_return.toString()
        pojoWithData.lcity= previousPojo.lcity
        pojoWithData.leaving= previousPojo.leaving
        pojoWithData.llat= previousPojo.llat
        pojoWithData.lline= previousPojo.lline
        pojoWithData.llog= previousPojo.llog
        pojoWithData.passenger=  previousPojo.passenger.toString()
        pojoWithData.price= previousPojo.price.toString()
        pojoWithData.rdate= previousPojo.rdate
        pojoWithData.rtime= previousPojo.rtime
        pojoWithData.time=  previousPojo.time
        pojoWithData.url= previousPojo.url
        pojoWithData.user=  previousPojo.user.toString()
        pojoWithData.username=  previousPojo.username
        pojoWithData.tddate= previousPojo.tddate
        pojoWithData.tdtime= previousPojo.tdtime
        pojoWithData.is_direct=   previousPojo.is_direct
        pojoWithData.carcolor=   previousPojo.carcolor
        pojoWithData.max_back_2=   previousPojo.max_back_2
        pojoWithData.max_back_3=   previousPojo.max_back_3
        pojoWithData.comment = previousPojo.comment


        request= Volley.newRequestQueue(this);

        Log.d("gtujkl", tvPickupSave.text.toString())

        etPriceSave.setText(previousPojo.price.toString())
        etSelectDateGoing.setText(previousPojo.date.toString())
        etReachingDate.setText(previousPojo.tddate.toString())
        etSelectDateReturnSave.setText(previousPojo.rdate.toString())
        etCarColorSave.setText(previousPojo.carcolor.toString())

        etCommentSave.setText(previousPojo.comment.toString())
        etSelectTimeGoing.setText(previousPojo.time.toString())
        etReachingTime.setText(previousPojo.tdtime.toString())
        etSelectTimeReturnSave.setText(previousPojo.rtime.toString())

        tvPickupSave.setText(previousPojo.lcity.toString())
        tvDropSave.setText(previousPojo.gcity.toString())

        etStopPoint.setText(previousPojo.stitle.toString())

        etCarSave.setText(previousPojo.carname.toString())

        if (previousPojo.id.toString().isEmpty() || previousPojo.id.toString() == ""){
            tvSave.text = "Publish"
        } else {
            tvSave.text = "Save"
        }


        if (previousPojo.max_back_2 == true) {
            maxSeatCount =  "Max 2 seat in back"
        } else {
            maxSeatCount =  "Max 3 seat in back"
        }

        var price = previousPojo.price
        var pass = previousPojo.passenger
        count = previousPojo.passenger!!.toInt()


        var totalAMt = price!!.toInt() * pass!!.toInt()

        tvTotalPriceSave.text =  "${totalAMt}₹"

        tvCountSave.setText(previousPojo.passenger.toString())


        if (previousPojo.is_return.toString() == "true") {
            cvReturnRideSave.isChecked = true
            llReturnSave.visibility = View.VISIBLE
        } else {
            cvReturnRideSave.isChecked = false
            llReturnSave.visibility = View.GONE
        }


        if (previousPojo.pets == true) {
            cbPetsEdit.isChecked = true
        } else {
            cbPetsEdit.isChecked = false
        }

        if (previousPojo.smoking == true) {
            cbSmokingEdit.isChecked = true
        } else {
            cbSmokingEdit.isChecked = false
        }



        if (previousPojo.is_direct.toString() == "true") {
            rbBookInstantly.isChecked = true
        } else {
            rbMyself.isChecked = true
        }


        if (previousPojo.max_back_2.toString() == "true") {
            rbMax2SeatSave.isChecked = true
        } else {
            rbMax3SeatSave.isChecked = true
        }



        etSelectDateGoing.setOnFocusChangeListener(object : View.OnFocusChangeListener {
            override fun onFocusChange(p0: View?, p1: Boolean) {
                if (p1 == true) {
                    datePicker()
                }
            }
        })



        etSelectDateGoing.setOnClickListener {
            datePicker()
        }

        etReachingDate.setOnClickListener {
            datePickerReach()
        }



        etSelectTimeGoing.setOnClickListener {
            val timePickerFragment: DialogFragment = TimePickerFragment()
            timePickerFragment.setCancelable(false)
            timePickerFragment.show(supportFragmentManager, "timePicker")
        }


        etSelectTimeGoing.setOnFocusChangeListener(object : View.OnFocusChangeListener {
            override fun onFocusChange(p0: View?, p1: Boolean) {
                if (p1 == true) {
                    val timePickerFragment: DialogFragment = TimePickerFragment()
                    timePickerFragment.setCancelable(false)
                    timePickerFragment.show(supportFragmentManager, "timePicker")
                }
            }
        })


        etReachingTime.setOnClickListener {
            shoeReachTimeDialog()
        }



        etSelectDateReturnSave.setOnClickListener {
            datePickerReturn()
        }

        etSelectDateReturnSave.setOnFocusChangeListener(object : View.OnFocusChangeListener {
            override fun onFocusChange(p0: View?, p1: Boolean) {
                if (p1 == true) {
                    datePickerReturn()
                }
            }
        })



        etSelectTimeReturnSave.setOnClickListener {
            shoeReturnTimeDialog()
        }

        etSelectTimeReturnSave.setOnFocusChangeListener(object : View.OnFocusChangeListener {
            override fun onFocusChange(p0: View?, p1: Boolean) {
                if (p1 == true) {
                    shoeReturnTimeDialog()
                }
            }
        })


        etReachingTime.setOnFocusChangeListener(object : View.OnFocusChangeListener {
            override fun onFocusChange(p0: View?, p1: Boolean) {
                if (p1 == true) {
                    shoeReachTimeDialog()
                }
            }
        })


        cv.setOnClickListener {
            if (count != 0) {
                count--
                tvCountSave.setText(count.toString())

                if (etPriceSave.text.toString() != null && etPriceSave.text.toString() != "") {
                    totalAmt = etPriceSave.text.toString().toInt() * tvCountSave.text.toString().toInt()
                    tvTotalPriceSave.text = totalAmt.toString() + "₹"
                }

            }
        }


        cvAddSave.setOnClickListener {
            count++
            tvCountSave.setText(count.toString())

            if (etPriceSave.text.toString() != null && etPriceSave.text.toString() != "") {
                totalAmt = etPriceSave.text.toString().toInt() * tvCountSave.text.toString().toInt()
                tvTotalPriceSave.text = totalAmt.toString() + "₹"
            }
        }

        ivBacknumberSave.setOnClickListener {
            onBackPressed()
        }

        cvFromLocationSave.setOnClickListener {

            val intent =  (Intent(this, ShowMapActivity::class.java))
            intent.putExtra("typeis","from")
            startActivity(intent)

        }

        cvToLocationSave.setOnClickListener {
            val intent =  (Intent(this, ShowMapActivity::class.java))
            intent.putExtra("typeis","to")
            startActivity(intent)

        }


        etPriceSave.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                if (etPriceSave.text.toString() != null && etPriceSave.text.toString() != "") {
                    totalAmt = etPriceSave.text.toString().toInt() * tvCountSave.text.toString().toInt()
                    tvTotalPriceSave.text = totalAmt.toString() + "₹"
                }
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if (etPriceSave.text.toString() != null && etPriceSave.text.toString() != "" && etPriceSave.text.toString().isNotEmpty()) {
                    totalAmt =
                        etPriceSave.text.toString().toInt() * tvCountSave.text.toString().toInt()
                    tvTotalPriceSave.text = totalAmt.toString() + "₹"
                }
            }

        })

        rbBookInstantly.setOnClickListener {
            radio_button_click(rbBookInstantly)
        }

        rbMyself.setOnClickListener {
            radio_button_click(rbMyself)
        }



        rbMax2SeatSave.setOnClickListener {
            radio_button_click_seat(rbBookInstantly)
        }

        rbMax3SeatSave.setOnClickListener {
            radio_button_click_seat(rbMyself)
        }


        cvReturnRideSave.setOnCheckedChangeListener { buttonView, isChecked ->
            if (isChecked) {
                llReturnSave.visibility = View.VISIBLE
            } else {
                llReturnSave.visibility = View.GONE
            }
        }

        btnSave.setOnClickListener {
            checkFields()
        }
        getUserData()
    }



    fun getUserData( ) {

        progressDialog = ProgressDialog(this)
        progressDialog.setMessage("Wait a Sec....Loading Details..")
        progressDialog.setCancelable(false)
        progressDialog.show()

        val url = Configure.BASE_URL + Configure.GET_USER_DETAILS +USER_ID_KEY+"/"
        val jsonObjRequest: StringRequest = object : StringRequest(
            Method.GET,
            url,
            object : Response.Listener<String?> {
                override fun onResponse(response: String?) {
                    Log.d("jukjbkj", response.toString())

                    val gson = Gson()

                    if (response != null ) {

                        val userArray: FetchProfileData =
                            gson.fromJson(response, FetchProfileData ::class.java)

                        if (userArray != null) {

                            pets = userArray.pets.toString()
                            smoking = userArray.smoking.toString()


                            Log.d("kjukj",pets +"        "+smoking)

                        }
                    }

                    progressDialog.dismiss()
                }
            }, object : Response.ErrorListener {
                override fun onErrorResponse(error: VolleyError) {
                    VolleyLog.d("volley", "Error: " + error.message)
                    error.printStackTrace()
                    Log.e("Responceis",  "Error: " + error.message)

                    Toast.makeText(this@EditRide,"Something Went Wrong ! Please try after some time",
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

    fun radio_button_click_seat(view: View){
        // Get the clicked radio button instance
        val radio: RadioButton = findViewById(radio_groupSeatSave.checkedRadioButtonId)

        maxSeatCount = radio.text.toString()

        Toast.makeText(applicationContext, maxSeatCount,
            Toast.LENGTH_SHORT).show()
    }

    fun radio_button_click(view: View){
        // Get the clicked radio button instance
        val radio: RadioButton = findViewById(radiogroup.checkedRadioButtonId)

        cbBookInstant = radio.text.toString()

        Toast.makeText(applicationContext, cbBookInstant,
            Toast.LENGTH_SHORT).show()
    }

    private fun checkFields() {

        Log.d("jkhuihuj","CLick")

     if (cvReturnRideSave.isChecked){
            if (etSelectDateReturnSave.text.toString().isEmpty() || etSelectDateReturnSave.text.toString() == "") {
                Toast.makeText(this,"Please Select Correct Return Date ",
                    Toast.LENGTH_LONG).show()
            }else  if (etSelectTimeReturnSave.text.toString().isEmpty() || etSelectTimeReturnSave.text.toString() == "") {
                Toast.makeText(this,"Please Select Correct Return Time",
                    Toast.LENGTH_LONG).show()
            }
        }

            pojoWithData.stitle = etStopPoint.text.toString()
            pojoWithData.slog = "0"
            pojoWithData.slat = "0"

        if (tvPickupSave.text.toString().isEmpty() || tvPickupSave.text.toString() == "") {
            Toast.makeText(this,"Please Select Correct PickUp Address",
                Toast.LENGTH_LONG).show()
        }else if (tvDropSave.text.toString().isEmpty() || tvDropSave.text.toString() == "") {
            Toast.makeText(this,"Please Select Correct Drop Address",
                Toast.LENGTH_LONG).show()
        }else if (etSelectTimeGoing.text.toString().isEmpty() || etSelectTimeGoing.text.toString() == "") {
            Toast.makeText(this,"Please Select Correct PickUp Time",
                Toast.LENGTH_LONG).show()
        }else if (etCarColorSave.text.toString().isEmpty() || etCarColorSave.text.toString() == "") {
            Toast.makeText(this,"Please Enter Valid Car Color",
                Toast.LENGTH_LONG).show()
        } else if (etReachingTime.text.toString().isEmpty() || etReachingTime.text.toString() == "") {
            Toast.makeText(this,"Please Select Correct Reaching Time",
                Toast.LENGTH_LONG).show()
        }else if (maxSeatCount.isEmpty() ||maxSeatCount == "") {
            Toast.makeText(this,"Please select valid Back Seat count ",
                Toast.LENGTH_LONG).show()
        }
        else if (etPriceSave.text.toString().isEmpty() || etPriceSave.text.toString() == "") {
            Toast.makeText(this,"Please Select Correct Riding Price for per Passenger ",
                Toast.LENGTH_LONG).show()
        }  else  if (etReachingDate.text.toString() < etSelectDateGoing.text.toString()) {
            Toast.makeText(this,"Reaching Date should be greater then or equals to Leaving Date",
                Toast.LENGTH_LONG).show()
        }else  if (etSelectDateGoing.text.toString() > etSelectDateReturnSave.text.toString()) {
            Toast.makeText(this,"Return Date should be greater then or equals to Leaving Date",
                Toast.LENGTH_LONG).show()
        }  else if (etCarSave.text.toString().isEmpty() || etCarSave.text.toString() == "") {
            Toast.makeText(this,"Please enter valid Car Name ",
                Toast.LENGTH_LONG).show()
        } else {
            Log.d("jkhuihuj","el")
            if (cvReturnRideSave.isChecked) {
                pojoWithData.is_return = "true"
                Log.d("jkhuihuj","CLick2")
            } else {
                pojoWithData.rtime = pojoWithData.time
                pojoWithData.rdate = pojoWithData.date
                pojoWithData.is_return = "false"

                Log.d("jkhuihuj","CLick2")
            }


            if (cbPetsEdit.isChecked) {
                pojoWithData.pets = true
            } else {
                pojoWithData.pets = false
            }


            if (cbSmokingEdit.isChecked) {
                pojoWithData.smoking = true
            } else {
                pojoWithData.smoking = false
            }

            if (cbBookInstant == "Book Instantly") {
                pojoWithData.is_direct = true
            } else {
                pojoWithData.is_direct = false
            }


            pojoWithData.price = etPriceSave.text.toString()
            pojoWithData.comment = etCommentSave.text.toString()
            pojoWithData.carcolor = etCarColorSave.text.toString()
            pojoWithData.passenger = tvCountSave.text.toString()
            pojoWithData.carname = etCarSave.text.toString()
            pojoWithData.user = USER_ID_KEY
            pojoWithData.leaving =  tvPickupSave.text.toString()
            pojoWithData.going =  tvDropSave.text.toString()

            Log.d("jkhuihuj","else")

            progressDialog = ProgressDialog(this)
            progressDialog.setMessage("Wait a Sec....Updating your Ride")
            progressDialog.setCancelable(false)
            progressDialog.show()

            if (previousPojo.id.toString() != "" || previousPojo.id.toString().isNotEmpty()) {
                UpdateRide(pojoWithData)
            } else {

                hitOfferRideAPI()
            }

        }
    }



    private fun hitOfferRideAPI() {

        if (cbBookInstant == "Book Instantly") {
            pojoWithData.is_direct = true
        } else {
            pojoWithData.is_direct = false
        }



        if (maxSeatCount == "Max 2 seat in back") {
            pojoWithData.max_back_2 = true
            pojoWithData.max_back_3 = false
        } else {
            pojoWithData.max_back_3 = true
            pojoWithData.max_back_2 = false
        }


        progressDialog = ProgressDialog(this)
        progressDialog.setMessage("Wait a Sec....Creating your Ride")
        progressDialog.setCancelable(false)
        progressDialog.show()

        pojoWithData.id =  ""
        pojoWithData.username =  ""
        pojoWithData.url =  ""
        pojoWithData.image =  ""
        pojoWithData.comment =  ""

//        if ( pojoWithData.rdate == null || pojoWithData.rdate!!.isEmpty()) {
//            pojoWithData.is_return = "false"
//        } else {
//            pojoWithData.is_return = "true"
//        }

        val url = Configure.BASE_URL + Configure.OFFER_RIDE_URL
        Log.d("jukjbkj", url.toString())

        val jsonObjRequest: StringRequest = object : StringRequest(
            Method.POST,
            url,
            object : Response.Listener<String?> {
                override fun onResponse(response: String?) {
                    Log.d("jukjbkj", response.toString())
                    Toast.makeText(this@EditRide,"Ride Offered Successfully...",
                        Toast.LENGTH_LONG).show()

                    val mainActivity =
                        Intent(applicationContext, MainActivity::class.java)

                    mainActivity.putExtra("type", "RideBooked")
                    progressDialog.dismiss()
                    startActivity(mainActivity)
                    finish()


                }
            }, object : Response.ErrorListener {
                override fun onErrorResponse(error: VolleyError) {
                    VolleyLog.d("volley", "Error: " + error.message)
                    error.printStackTrace()
                    Log.e("jukjbkj",  "Error: " + error.message)

                    Toast.makeText(this@EditRide,"Something Went Wrong ! Please try after some time",
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
                params["id"] =  pojoWithData.id.toString()
                params["url"] = pojoWithData.url.toString()
                params["username"] = pojoWithData.username.toString()
                params["user"] =  pojoWithData.user.toString()
                params["image"] = pojoWithData.image.toString()
                params["leaving"] = pojoWithData.leaving.toString()
                params["lline"] =  pojoWithData.lline.toString()
                params["lcity"] = pojoWithData.lcity.toString()
                params["llat"] = pojoWithData.llat.toString()
                params["llog"] =  pojoWithData.llog.toString()
                params["going"] = pojoWithData.going.toString()
                params["glog"] = pojoWithData.glog.toString()
                params["glat"] = pojoWithData.glat.toString()
                params["gcity"] = pojoWithData.gcity.toString()
                params["gline"] = pojoWithData.gline.toString()
                params["date"] =  pojoWithData.date.toString()
                params["time"] = pojoWithData.time.toString()
                params["rdate"] = pojoWithData.rdate.toString()
                params["rtime"] =  pojoWithData.rtime.toString()
                params["price"] = pojoWithData.price.toString()
                params["passenger"] = pojoWithData.passenger.toString()
                params["comment"] = etCommentSave.text.toString()
                params["is_return"] = pojoWithData.is_return.toString()
                params["tddate"] = pojoWithData.tddate.toString()
                params["tdtime"] = pojoWithData.tdtime.toString()
                params["is_direct"] = pojoWithData.is_direct.toString()
                params["carname"] = pojoWithData.carname.toString()
                params["carcolor"] = pojoWithData.carcolor.toString()
                params["stitle"] = pojoWithData.stitle.toString()
                params["slat"] = pojoWithData.slat.toString()
                params["slog"] = pojoWithData.slog.toString()
                params["pets"] = pets
                params["smoking"] = smoking
                params["max_back_2"] = pojoWithData.max_back_2.toString()
                params["max_back_3"] = pojoWithData.max_back_3.toString()
                return params
            }
        }
        request!!.add(jsonObjRequest)

    }

    private fun UpdateRide(pojoWithData: offerRideModel) {

        if (cbBookInstant == "Book Instantly") {
            pojoWithData.is_direct = true
        } else {
            pojoWithData.is_direct = false
        }

        if (rbBookInstantly.isChecked) {
            pojoWithData.is_direct = true
        } else {
            pojoWithData.is_direct = false
        }


        if (maxSeatCount == "Max 2 seat in back") {
            pojoWithData.max_back_2 = true
            pojoWithData.max_back_3 = false
        } else {
            pojoWithData.max_back_3 = true
            pojoWithData.max_back_2 = false
        }


        pojoWithData.id =  ""
        pojoWithData.username =  ""
        pojoWithData.url =  ""
        pojoWithData.image =  ""
        pojoWithData.comment =  ""

        val url = Configure.BASE_URL + Configure.OFFER_RIDE_URL+previousPojo.id+"/"
        Log.d("jukjbkj", url.toString())

        val jsonObjRequest: StringRequest = object : StringRequest(
            Method.PUT,
            url,
            object : Response.Listener<String?> {
                override fun onResponse(response: String?) {
                    Log.d("jukjbkj", response.toString())

                    try {
                        // get JSONObject from JSON file
                        val obj = JSONObject(response.toString())
                        // fetch JSONObject named employee
//                        val employee: JSONObject = obj.getJSONObject("employee")

                        var ride = BookRidesPojoItem()
                        ride.comment =  obj.getString("comment")
                        ride.date= obj.getString("date")
                        ride.gcity= obj.getString("gcity")
                        ride.glat= obj.getString("glat")
                        ride.gline= obj.getString("gline")
                        ride.glog= obj.getString("glog")
                        ride.going= obj.getString("going")
                        ride.id= obj.getString("id")
                        ride.image= obj.getString("image")
                        ride.is_return= obj.getBoolean("is_return")
                        ride.lcity= obj.getString("lcity")
                        ride.leaving= obj.getString("leaving")
                        ride.llat= obj.getString("llat")
                        ride.lline= obj.getString("lline")
                        ride.llog= obj.getString("llog")
                        ride.passenger= obj.getInt("passenger").toString()
                        ride.price= obj.getInt("price")
                        ride.rdate= obj.getString("rdate")
                        ride.rtime= obj.getString("rtime")
                        ride.time= obj.getString("time")
                        ride.url= obj.getString("url")
                        ride.user= obj.getInt("user")
                        ride.username= obj.getString("username")
                        ride.tddate= obj.getString("tddate")
                        ride.tdtime= obj.getString("tdtime")
                        ride.is_direct= obj.getBoolean("is_direct")
                        ride.pets= obj.getBoolean("pets")
                        ride.smoking= obj.getBoolean("smoking")


                        Log.d("juguiuih",ride.leaving.toString())

                        var int = Intent( this@EditRide,
                            MainActivity::class.java)
                        Toast.makeText(this@EditRide,"Ride Updated Successfully...",
                            Toast.LENGTH_LONG).show()
                        int.putExtra("type","Updated")
                        startActivity(int)
                        finish()
                        progressDialog.dismiss()


                    } catch (e: JSONException) {
                        e.printStackTrace()

                        progressDialog.dismiss()

                    }


                    }
            }, object : Response.ErrorListener {
                override fun onErrorResponse(error: VolleyError) {
                    VolleyLog.d("volley", "Error: " + error.message)
                    error.printStackTrace()
                    Log.e("jukjbkj",  "Error: " + error.message)

                    Toast.makeText(this@EditRide,"Something Went Wrong ! Please try after some time",
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
                params["id"] =  pojoWithData.id.toString()
                params["url"] = pojoWithData.url.toString()
                params["username"] = pojoWithData.username.toString()
                params["user"] =  pojoWithData.user.toString()
                params["image"] = pojoWithData.image.toString()
                params["leaving"] = pojoWithData.leaving.toString()
                params["lline"] =  pojoWithData.lline.toString()
                params["lcity"] = pojoWithData.lcity.toString()
                params["llat"] = pojoWithData.llat.toString()
                params["llog"] =  pojoWithData.llog.toString()
                params["going"] = pojoWithData.going.toString()
                params["glog"] = pojoWithData.glog.toString()
                params["glat"] = pojoWithData.glat.toString()
                params["gcity"] = pojoWithData.gcity.toString()
                params["gline"] = pojoWithData.gline.toString()
                params["date"] =  pojoWithData.date.toString()
                params["time"] = pojoWithData.time.toString()
                params["rdate"] = pojoWithData.rdate.toString()
                params["rtime"] =  pojoWithData.rtime.toString()
                params["price"] = pojoWithData.price.toString()
                params["passenger"] = pojoWithData.passenger.toString()
                params["comment"] = etCommentSave.text.toString()
                params["is_return"] = pojoWithData.is_return.toString()
                params["tddate"] = pojoWithData.tddate.toString()
                params["tdtime"] = pojoWithData.tdtime.toString()
                params["is_direct"] = pojoWithData.is_direct.toString()
                params["carname"] = pojoWithData.carname.toString()
                params["stitle"] = pojoWithData.stitle.toString()
                params["slat"] = pojoWithData.slat.toString()
                params["slog"] = pojoWithData.slog.toString()
                params["pets"] = pets
                params["smoking"] = smoking
                params["carcolor"] = pojoWithData.carcolor.toString()
                params["max_back_2"] = pojoWithData.max_back_2.toString()
                params["max_back_3"] = pojoWithData.max_back_3.toString()

                return params
            }
        }
        request!!.add(jsonObjRequest)
    }


    private fun shoeReachTimeDialog() {
        val c = Calendar.getInstance()
        var mHour = c[Calendar.HOUR_OF_DAY]
        var mMinute = c[Calendar.MINUTE]

        // Launch Time Picker Dialog

        // Launch Time Picker Dialog
        val timePickerDialog = TimePickerDialog(
            this,
            object : TimePickerDialog.OnTimeSetListener  {
                override fun onTimeSet(
                    view: TimePicker?, hourOfDay: Int,
                    minute: Int
                ) {
                    etReachingTime.setText("$hourOfDay : $minute")

                    pojoWithData.tdtime = "$hourOfDay:$minute"
                }
            }, mHour, mMinute, false
        )
        timePickerDialog.show()
    }




    private fun shoeReturnTimeDialog() {
        val c = Calendar.getInstance()
        var mHour = c[Calendar.HOUR_OF_DAY]
        var mMinute = c[Calendar.MINUTE]

        // Launch Time Picker Dialog

        // Launch Time Picker Dialog
        val timePickerDialog = TimePickerDialog(
            this,
            object : TimePickerDialog.OnTimeSetListener  {
                override fun onTimeSet(
                    view: TimePicker?, hourOfDay: Int,
                    minute: Int
                ) {
                    etSelectTimeReturnSave.setText("$hourOfDay : $minute")

                    pojoWithData.rtime = "$hourOfDay:$minute"
                }
            }, mHour, mMinute, false
        )
        timePickerDialog.show()
    }



    override fun onTimeSet(timePicker: TimePicker?, hour: Int, minute: Int) {
        etSelectTimeGoing.setText(" $hour : $minute")

        pojoWithData.time = "$hour:$minute"
    }

    private fun datePicker() {
        val now = Calendar.getInstance()
        datePickerdialog = DatePickerDialog(
            this@EditRide, DatePickerDialog.OnDateSetListener { view, year, month, dayOfMonth ->
                val selectedDate = Calendar.getInstance()
                selectedDate.set(Calendar.YEAR, year)
                selectedDate.set(Calendar.MONTH, month)
                selectedDate.set(Calendar.DAY_OF_MONTH, dayOfMonth)
                val date = formate.format(selectedDate.time)
                etSelectDateGoing.setText(date)
                pojoWithData.date = date
            },
            now.get(Calendar.YEAR), now.get(Calendar.MONTH), now.get(Calendar.DAY_OF_MONTH)
        )
        datePickerdialog.show()
    }



    private fun datePickerReturn() {
        val now = Calendar.getInstance()
        datePickerdialog = DatePickerDialog(
            this@EditRide, DatePickerDialog.OnDateSetListener { view, year, month, dayOfMonth ->
                val selectedDate = Calendar.getInstance()
                selectedDate.set(Calendar.YEAR, year)
                selectedDate.set(Calendar.MONTH, month)
                selectedDate.set(Calendar.DAY_OF_MONTH, dayOfMonth)
                val date = formate.format(selectedDate.time)
                etSelectDateReturnSave.setText(date)
                pojoWithData.rdate = date
            },
            now.get(Calendar.YEAR), now.get(Calendar.MONTH), now.get(Calendar.DAY_OF_MONTH)
        )
        datePickerdialog.show()
    }



    private fun datePickerReach() {
        val now = Calendar.getInstance()
        datePickerdialog = DatePickerDialog(
            this@EditRide, DatePickerDialog.OnDateSetListener { view, year, month, dayOfMonth ->
                val selectedDate = Calendar.getInstance()
                selectedDate.set(Calendar.YEAR, year)
                selectedDate.set(Calendar.MONTH, month)
                selectedDate.set(Calendar.DAY_OF_MONTH, dayOfMonth)
                val date = formate.format(selectedDate.time)
                etReachingDate.setText(date)
                pojoWithData.tddate = date
            },
            now.get(Calendar.YEAR), now.get(Calendar.MONTH), now.get(Calendar.DAY_OF_MONTH)
        )
        datePickerdialog.show()
    }


    @Subscribe
    fun OnAddSelected(add : BookRideScreenFetchCity?) {

        Log.d("CITYIS",add!!.city.toString() +"jubhjbjbj")

        if (add!!.type == "from") {

            Log.d("gtujkl",add!!.city.toString())

            tvPickupSave.text = add!!.city
            pojoWithData.lline = add.gline
            pojoWithData.lcity = add.gcity
            pojoWithData.llat = add.lat
            pojoWithData.llog = add.long
        } else if (add!!.type == "to") {
            tvDropSave.text = add!!.city
            pojoWithData.gline = add.gline
            pojoWithData.gcity = add.gcity
            pojoWithData.glat = add.lat
            pojoWithData.glog = add.long
        } else if (add!!.type == "stop") {
            tvStopSave.text = add!!.city
            pojoWithData.stitle = add!!.city
            pojoWithData.slat = add.lat
            pojoWithData.slog = add.long
            pojoWithData.gline = add.gline
            pojoWithData.gcity = add.gcity
        }else {
            tvStopSave.text = add!!.city
            pojoWithData.stitle = add!!.city
            pojoWithData.slat = add.lat
            pojoWithData.slog = add.long
            pojoWithData.gline = add.gline
            pojoWithData.gcity = add.gcity
        }

    }


    override fun onStart() {
        super.onStart()
        if (!EventBus.getDefault().isRegistered(this@EditRide)) EventBus.getDefault().register(this)
    }

    override fun onDestroy() {
        super.onDestroy()
        EventBus.getDefault().unregister(this@EditRide)
    }


}
package com.ddsio.productionapp.sharesavari.SearchScreen.child

import android.Manifest
import android.app.ProgressDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.RatingBar
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.android.volley.*
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.bumptech.glide.Glide
import com.ddsio.productionapp.sharesavari.CommonUtils.Utils
import com.ddsio.productionapp.sharesavari.InboxScreen.Child.ChatLogActivity
import com.ddsio.productionapp.sharesavari.InboxScreen.Child.NewMessageActivity
import com.ddsio.productionapp.sharesavari.R
import com.ddsio.productionapp.sharesavari.User
import com.github.florent37.runtimepermission.kotlin.askPermission
import com.google.gson.Gson
import com.productionapp.amhimemekar.CommonUtils.*
import com.productionapp.amhimemekar.CommonUtils.Configure.BASE_URL
import com.productionapp.amhimemekar.CommonUtils.Configure.COMPLAINT
import com.productionapp.amhimemekar.CommonUtils.Configure.GET_USER_DETAILS
import com.productionapp.amhimemekar.CommonUtils.Configure.OFFER_RIDE_URL
import com.productionapp.amhimemekar.CommonUtils.Configure.RATING
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.ViewHolder
import kotlinx.android.synthetic.main.activity_driver_profile.*
import kotlinx.android.synthetic.main.activity_driver_profile.ivCloseScreen
import kotlinx.android.synthetic.main.reset_password_dialog.view.*
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.HashMap
import kotlin.math.roundToInt

class DriverProfile : AppCompatActivity() {

    lateinit var pojoWithData : BookRidesPojoItem
    lateinit var progressDialog: ProgressDialog
    lateinit var USER_ID_KEY : String
    var LOGIN_TOKEN = ""
    var request: RequestQueue? = null
    var phoneNumber = ""
    lateinit var dialog_otp: AlertDialog
    lateinit var ratingB : RatingBar
    lateinit var btnSubmitB : Button
    var driverId = 0
    var USER_UPDATE_ID = ""
    private val REQUEST_CALL = 1
    lateinit var cust : String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_driver_profile)

        val bundle: Bundle? = intent.extras
        pojoWithData = bundle!!.get("pojoWithData") as BookRidesPojoItem
        cust = bundle!!.get("cust") as String

        LOGIN_TOKEN = Utils.getStringFromPreferences(Configure.LOGIN_KEY,"",this)!!
        USER_UPDATE_ID = Utils.getStringFromPreferences(Configure.USER_UPDATE_ID,"",this)!!
        USER_ID_KEY = Utils.getStringFromPreferences(Configure.USER_ID_KEY,"",this)!!
        request= Volley.newRequestQueue(this);
        ratingB = findViewById<RatingBar>(R.id.rating)
        btnSubmitB = findViewById<Button>(R.id.btnSubmit)

        ratingB.isEnabled = false

        hitCopasAPI(cust)

        ivCloseScreen.setOnClickListener {
            onBackPressed()
        }

        if (cust != "0") {
            getDriverData(cust)
        } else {
            getDriverData(pojoWithData.user.toString())
        }

        Log.d("hhjbh", pojoWithData.image.toString())

        if (pojoWithData.image != null ) {
            Glide.with(this).load(pojoWithData.image).into(cvProf)
        }

//        if (USER_ID_KEY == pojoWithData.user.toString() ) {
//            llContact.visibility = View.VISIBLE
//            cvCall.visibility = View.VISIBLE
//        } else {
//            llContact.visibility = View.GONE
//        }

        ratingB.setOnRatingBarChangeListener(object : RatingBar.OnRatingBarChangeListener{
            override fun onRatingChanged(p0: RatingBar?, p1: Float, p2: Boolean) {
                 if (p0!!.rating > 0 ) {
                     btnSubmitB.visibility = View.VISIBLE
                 } else {
                     btnSubmitB.visibility = View.GONE
                 }
            }
        })

        cvCall.setOnClickListener {
            Log.d("hbuhbjb",phoneNumber)
            askCallingPermission("+91"+phoneNumber)
        }

        cvChat.setOnClickListener {

            var user = User(driverId.toString(), tvName.text.toString(),pojoWithData.image.toString())

            val intent = Intent(this, ChatLogActivity::class.java)
            intent.putExtra(NewMessageActivity.USER_KEY,user)
            startActivity(intent)
        }


        btnSubmitB.setOnClickListener {
            showRestPassDialog()
        }

        rvReview.setOnClickListener {
            var int = Intent(this,
                ReviewsList::class.java)
            int.putExtra("pojoWithData",pojoWithData)
            int.putExtra("driverid",pojoWithData.user.toString())
            startActivity(int)
        }

        if (USER_ID_KEY == pojoWithData.user.toString()) {
            rvRating.visibility = View.GONE
        } else {
            rvRating.visibility = View.VISIBLE
        }


        tvReport.setOnClickListener {
            showComplaintDialog()
        }


        if (cust != "0") {
            getAllRating(cust)
        } else {
            getAllRating(pojoWithData.user.toString())
        }

Log.d("dddddddddddd",cust+"   "+USER_ID_KEY+"     "+pojoWithData.user.toString())
        if (cust == USER_ID_KEY || pojoWithData.user.toString() == USER_ID_KEY) {
            ratingB.isEnabled = false
            tvReport.visibility = View.GONE
        }
    }

    private fun checkRatingTime(s: String) {

        val c = Calendar.getInstance()
        val df = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
        val formattedDate = df.format(c.time)

        val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
        val date = sdf.parse(s)
        val calendar = Calendar.getInstance()
        calendar.time = date
        calendar.add(Calendar.HOUR, 24)

        val Year = calendar[Calendar.YEAR]
        val Month = calendar[Calendar.MONTH]+1
        val Day = calendar[Calendar.DAY_OF_MONTH]
        val Hour = calendar[Calendar.HOUR]
        val Minute = calendar[Calendar.MINUTE]
        val Second = calendar[Calendar.SECOND]

        var datePlus = Year.toString()+"-"+Month+"-"+Day+" "+Hour+":"+Minute+":"+Second


        val calendar1 = Calendar.getInstance()
        calendar1.time = date

        val Year1 = calendar1[Calendar.YEAR]
        val Month1 = calendar1[Calendar.MONTH] +1
        val Day1 = calendar1[Calendar.DAY_OF_MONTH]
        val Hour1 = calendar1[Calendar.HOUR]
        val Minute1 = calendar1[Calendar.MINUTE]
        val Second1 = calendar1[Calendar.SECOND]

        var datePlus1 = Year1.toString()+"-"+Month1+"-"+Day1+" "+Hour1+":"+Minute1+":"+Second1

        Log.d("jjjjjjjjj",datePlus)
        Log.d("jjjjjjjjj",datePlus1)
        Log.d("jjjjjjjjj",formattedDate)

        if (getTimeStamp(formattedDate) > getTimeStamp(datePlus1)) {
            if (getTimeStamp(formattedDate) < getTimeStamp(datePlus)) {
                ratingB.isEnabled = true
            } else {
                ratingB.isEnabled = false
            }
        } else {
            ratingB.isEnabled = false
            Log.d("jjjjjjjjj","gu")
        }

    }

    private fun checkRideComplete(s: String) {

        val c = Calendar.getInstance()
        val df = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
        val formattedDate = df.format(c.time)

        val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
        val date = sdf.parse(s)
        val calendar = Calendar.getInstance()
        calendar.time = date

        val Year = calendar[Calendar.YEAR]
        val Month = calendar[Calendar.MONTH]
        val Day = calendar[Calendar.DAY_OF_MONTH]
        val Hour = calendar[Calendar.HOUR]
        val Minute = calendar[Calendar.MINUTE]
        val Second = calendar[Calendar.SECOND]
        var datePlus = Year.toString()+"-"+Month+"-"+Day+" "+Hour+":"+Minute+":"+Second

        if(getTimeStamp(datePlus) < getTimeStamp(formattedDate)) {
            ratingB.isEnabled = false
        }
    }


    private fun getTimeStamp(s: String): Long {

        Log.d("timestampdatsi",s)

        val formatter: DateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
        val date = formatter.parse(s) as Date

        Log.d("timestampdatsi",date.time.toString())

        return date.time
    }


    private fun askCallingPermission(s: String) {
        askPermission(Manifest.permission.CALL_PHONE,Manifest.permission.READ_CONTACTS,
            Manifest.permission.GET_ACCOUNTS){
            //       Toast.makeText(applicationContext,"Calling"+finalworker.mobile,Toast.LENGTH_LONG).show()
            makeCall(s)
        }.onDeclined { e ->
            if (e.hasDenied()) {
                //the list of denied permissions
                e.denied.forEach {
                }

                AlertDialog.Builder(this@DriverProfile)
                    .setMessage("Please accept our permissions.. Otherwise you will not be able to use some of our Important Features.")
                    .setPositiveButton("yes") { dialog, which ->
                        e.askAgain()
                    } //ask again
                    .setNegativeButton("no") { dialog, which ->
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

    private fun makeCall(number : String) {
        if (number.trim({ it <= ' ' }).length > 0) {

            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.CALL_PHONE
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.CALL_PHONE), REQUEST_CALL
                )
            } else {
                val dial = "tel:$number"
                startActivity(Intent(Intent.ACTION_CALL, Uri.parse(dial)))
            }

        } else {
            Toast.makeText(this, "Enter Phone Number", Toast.LENGTH_SHORT).show()
        }
    }

    private fun showComplaintDialog() {
        val inflater = getLayoutInflater()
        val alertLayout = inflater.inflate(R.layout.submit_complaint, null)

        alertLayout.cvReset!!.setOnClickListener {
            val verificationCode = alertLayout.loginetEmail!!.text!!.toString()
            if (verificationCode.isEmpty()) {
//                Toast.makeText(this@Authentication, "Enter verification code", Toast.LENGTH_SHORT).show()
                Toast.makeText(this,"Please Enter Valid Complaint",Toast.LENGTH_LONG).show()

            } else {

                submitComplaint(verificationCode)
                dialog_otp.dismiss()
            }
        }

        val showOTP = AlertDialog.Builder(this!!)
        showOTP.setView(alertLayout)
        showOTP.setCancelable(false)
        dialog_otp = showOTP.create()
        dialog_otp.show()

        alertLayout.ivCloseReset.setOnClickListener {
            dialog_otp.dismiss()
        }

    }



    private fun showRestPassDialog() {
        val inflater = getLayoutInflater()
        val alertLayout = inflater.inflate(R.layout.rating, null)

        alertLayout.cvReset!!.setOnClickListener {
            val verificationCode = alertLayout.loginetEmail!!.text!!.toString()
            if (verificationCode.isEmpty()) {
//                Toast.makeText(this@Authentication, "Enter verification code", Toast.LENGTH_SHORT).show()
                Toast.makeText(this, "Please Enter Valid Review", Toast.LENGTH_LONG).show()

            } else {
                submitRating(verificationCode)
                dialog_otp.dismiss()
            }
        }

        val showOTP = AlertDialog.Builder(this!!)
        showOTP.setView(alertLayout)
        showOTP.setCancelable(false)
        dialog_otp = showOTP.create()
        dialog_otp.show()

        alertLayout.ivCloseReset.setOnClickListener {
            dialog_otp.dismiss()
        }

    }




    private fun getRating() {
        val url = BASE_URL+ RATING +"?passenger=" + USER_ID_KEY
        val jsonObjRequest: StringRequest = object : StringRequest(
            Method.GET,
            url,
            object : Response.Listener<String?> {
                override fun onResponse(response: String?) {
                    Log.d("driverprofRate", response.toString())
                    val gson = Gson()

                    val userArray: ArrayList<RatingModelItem> =
                        gson.fromJson(response, RatingModel ::class.java)

                    if (userArray != null) {

                        for (i in 0..userArray.size - 1) {

                            if (userArray.get(i).driver.toString() == pojoWithData.user.toString()) {
                                btnSubmitB.isEnabled = false
                                ratingB.isEnabled = false
                                ratingB.rating = userArray.get(i).points.toFloat()
                            }
                        }
                    }


                    progressDialog.dismiss()
                }
            }, object : Response.ErrorListener {
                override fun onErrorResponse(error: VolleyError) {
                    VolleyLog.d("volley", "Error: " + error.message)
                    error.printStackTrace()
                    Log.e("Responceis",  "Error: " + error.message)

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
                    java.util.HashMap()
                return params
            }
        }
        request!!.add(jsonObjRequest)
    }




    private fun getAllRating(cust: String) {

        var rating = 0
        var userRatedCount = 0

        val url = BASE_URL+ RATING
        val jsonObjRequest: StringRequest = object : StringRequest(
            Method.GET,
            url,
            object : Response.Listener<String?> {
                override fun onResponse(response: String?) {
                    Log.d("driverprofRate", response.toString())
                    val gson = Gson()

                    val userArray: ArrayList<RatingModelItem> =
                        gson.fromJson(response, RatingModel ::class.java)

                    if (userArray != null) {

                        for (i in 0..userArray.size-1) {

                            if (userArray.get(i).driver.toString() == cust) {
                                if (userArray.get(i).points != null) {

                                    rating = rating + userArray.get(i).points
                                    userRatedCount = userRatedCount + 1
                                }
                            }
                        }

                        Log.d("dtsbjnkd",rating.toString())


                        if (userRatedCount != 0 && userRatedCount != null) {
                            var sum = userRatedCount * 5
                            var finalRating = (rating * 5) / sum
                            tvRatings.text = finalRating.toString()+"/5 Ratings"
                        }

                    }
                }
            }, object : Response.ErrorListener {
                override fun onErrorResponse(error: VolleyError) {
                    VolleyLog.d("volley", "Error: " + error.message)
                    error.printStackTrace()
                    Log.e("Responceis",  "Error: " + error.message)

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
                return params
            }
        }
        request!!.add(jsonObjRequest)
    }


    private fun submitComplaint(verificationCode: String) {
        progressDialog = ProgressDialog(this)
        progressDialog.setMessage("Wait a Sec.... ")
        progressDialog.setCancelable(false)
        progressDialog.show()


        val url = BASE_URL+ COMPLAINT
        val jsonObjRequest: StringRequest = object : StringRequest(
            Method.POST,
            url,
            object : Response.Listener<String?> {
                override fun onResponse(response: String?) {
                    Log.d("driverprof", response.toString())

                    Toast.makeText(this@DriverProfile,"Complaint submitted successfully...",
                        Toast.LENGTH_LONG).show()

                    progressDialog.dismiss()
                }
            }, object : Response.ErrorListener {
                override fun onErrorResponse(error: VolleyError) {
                    VolleyLog.d("volley", "Error: " + error.message)
                    error.printStackTrace()
                    Log.e("Responceis",  "Error: " + error.message)

                    Toast.makeText(this@DriverProfile,"Something Went Wrong ! Please try after some time",
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
                params["driver"] = pojoWithData.user.toString()
                params["passenger"] = USER_ID_KEY
                params["comment"] = verificationCode

                return params
            }
        }
        request!!.add(jsonObjRequest)

    }

    private fun submitRating(verificationCode: String) {
        progressDialog = ProgressDialog(this)
        progressDialog.setMessage("Wait a Sec.... ")
        progressDialog.setCancelable(false)
        progressDialog.show()

        val url = BASE_URL+ RATING
        val jsonObjRequest: StringRequest = object : StringRequest(
            Method.POST,
            url,
            object : Response.Listener<String?> {
                override fun onResponse(response: String?) {
                    Log.d("driverprof", response.toString())

                    Toast.makeText(this@DriverProfile,"Rated Successfully...",
                        Toast.LENGTH_LONG).show()

                    btnSubmitB.isEnabled = false
                    ratingB.isEnabled = false
                    progressDialog.dismiss()
                }
            }, object : Response.ErrorListener {
                override fun onErrorResponse(error: VolleyError) {
                    VolleyLog.d("volley", "Error: " + error.message)
                    error.printStackTrace()
                    Log.e("Responceis",  "Error: " + error.message)

                    Toast.makeText(this@DriverProfile,"Something Went Wrong ! Please try after some time",
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
                params["driver"] = pojoWithData.user.toString()
                params["passenger"] = USER_ID_KEY
                params["points"] = rating.rating.roundToInt().toString()
                params["comment"] = verificationCode

                return params
            }
        }
        request!!.add(jsonObjRequest)
    }


    fun getDriverData(cust: String) {

        progressDialog = ProgressDialog(this)
        progressDialog.setMessage("Wait a Sec....Loading Details..")
        progressDialog.setCancelable(false)
        progressDialog.show()


        if (cust == USER_ID_KEY) {
            llContact.visibility = View.GONE
        } else {
            llContact.visibility = View.VISIBLE
        }

        val url = BASE_URL+ GET_USER_DETAILS+cust+"/"
//        val url = "https://ddsio.com/sharesawaari/rest/users/22/"


        val jsonObjRequest: StringRequest = object : StringRequest(
            Method.GET,
            url,
            object : Response.Listener<String?> {
                override fun onResponse(response: String?) {
                    Log.d("driverprof", response.toString())

                    val gson = Gson()

                    if (response != null ) {

                        val userArray: FetchProfileData =
                            gson.fromJson(response, FetchProfileData ::class.java)

                        if (userArray != null) {
                            val image = userArray.image

                            phoneNumber = userArray.mobile!!
                            driverId = userArray.id!!

                            Glide.with(this@DriverProfile).load(userArray.image).into(cvProf)

                            tvName.text =  userArray.first_name
                            if (userArray.bio == "" || userArray.bio!!.isEmpty()) {
                                tvDetail.text = "No Bio"
                            } else {
                                tvDetail.text = userArray.bio
                            }


                            calculateAge(userArray.birthdate)

                            hitFindOfferedRideAPI(cust)

                            if (userArray.verification == "False") {
                                ivAlert.setImageDrawable(resources.getDrawable(R.drawable.alert))
                                ivAlertProf.setImageDrawable(resources.getDrawable(R.drawable.alert))

                            } else {
                                ivAlert.setImageDrawable(resources.getDrawable(R.drawable.tick))
                                ivAlertProf.setImageDrawable(resources.getDrawable(R.drawable.tick))
                            }

                        }

                    }

                    progressDialog.dismiss()
                }
            }, object : Response.ErrorListener {
                override fun onErrorResponse(error: VolleyError) {
                    VolleyLog.d("volley", "Error: " + error.message)
                    error.printStackTrace()
                    Log.e("Responceis",  "Error: " + error.message)

                    Toast.makeText(this@DriverProfile,"Something Went Wrong ! Please try after some time",
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

    private fun calculateAge(birthdate: String?) {
        val separated =
            birthdate!!.split("-".toRegex()).toTypedArray()
        Log.d("ageis",separated[0]+"?"+separated[1]+"?"+separated[2])


        val dob: Calendar = Calendar.getInstance()
        val today: Calendar = Calendar.getInstance()

        dob.set(separated[0].toInt(), separated[2].toInt(), separated[1].toInt())

        var age: Int = today.get(Calendar.YEAR) - dob.get(Calendar.YEAR)

        if (today.get(Calendar.DAY_OF_YEAR) < dob.get(Calendar.DAY_OF_YEAR)) {
            age--
        }

        val ageInt = age
        tvAgeYear.text = ageInt.toString() +" Years Old"
    }


    private fun hitCopasAPI(cust: String) {

        var arr = arrayListOf<String>()

        val url = Configure.BASE_URL + Configure.Book_RIDE_URL+"?ride=${pojoWithData.id}"

        val jsonObjRequest: StringRequest = object : StringRequest(
            Method.GET,
            url,
            object : Response.Listener<String?> {
                override fun onResponse(response: String?) {
                    Log.d("jukjbkjf", response.toString())

                    val gson = Gson()

                    val userArray : ArrayList<bookrideItem> =
                        gson.fromJson(response, bookride ::class.java)


                    if (userArray != null) {
                        for (i in 0..userArray.size-1)   {
//                                adapter.add(ridesClass(userArray.get(i)))

                            if (userArray.get(i).is_confirm) {
                                arr.add(userArray.get(i).passenger.toString())
                            }

                        }

                        checkPassengerBooked(arr)
                    }




                }
            }, object : Response.ErrorListener {
                override fun onErrorResponse(error: VolleyError) {
                    VolleyLog.d("volley", "Error: " + error.message)
                    error.printStackTrace()
                    Log.e("Responceis",  "Error: " + error.message)


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
                    java.util.HashMap()
//                params.put("ride",customers.id.toString())
//                params.put("passenger",USER_ID_KEY)

                return params
            }
        }
        request!!.add(jsonObjRequest)
    }

    private fun checkPassengerBooked(arr: ArrayList<String>) {
        for (i in 0..arr.size-1) {

            Log.d("bbbb","Booked" + arr.get(i)  +"  "+USER_ID_KEY )

            if (arr.get(i) == USER_ID_KEY) {

                Log.d("bbbb","Booked" + cust )


                if (cust != USER_ID_KEY) {

                    rating.isEnabled = true
                    tvReport.visibility = View.VISIBLE

                    if (pojoWithData.is_return!!) {
                        checkRatingTime(pojoWithData.brdate+" "+pojoWithData.brtime)
                    } else {
                        checkRatingTime(pojoWithData.tddate+" "+pojoWithData.tdtime)
                    }

                }

            } else {
                Log.d("bbbb","Not Booked")
                rating.isEnabled = false
                tvReport.visibility = View.GONE
            }
        }

    }


    var i = 0
    private fun hitFindBookedRideAPI(cust: String) {

        val adapter = GroupAdapter<ViewHolder>()

        val url = Configure.BASE_URL + Configure.Book_RIDE_URL +"?passenger=${USER_ID_KEY}"

        val jsonObjRequest: StringRequest = object : StringRequest(
            Method.GET,
            url,
            object : Response.Listener<String?> {
                override fun onResponse(response: String?) {

                    val gson = Gson()


                    val userArray: ArrayList<bookrideItem> =
                        gson.fromJson(response, bookride ::class.java)

                    for (rides in userArray) {
                        if (rides != null && i == 0) {
                            if (rides.ride.toString() == pojoWithData.id.toString()) {
                                if (rides.is_confirm == false) {
                                    cvCall.visibility = View.GONE
                                } else {
                                    cvCall.visibility = View.VISIBLE
                                    i = 1
                                }
                            }
                        }

                    }

                }
            }, object : Response.ErrorListener {
                override fun onErrorResponse(error: VolleyError) {
                    VolleyLog.d("volley", "Error: " + error.message)
                    error.printStackTrace()
                    Log.e("Responceis",  "Error: " + error.message)

                    Toast.makeText(this@DriverProfile,"Something Went Wrong ! Please try after some time",
                        Toast.LENGTH_LONG).show()

                    progressDialog.dismiss()
                }
            }) {


            override fun getHeaders(): MutableMap<String, String> {

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

    private fun hitFindOfferedRideAPI(cust: String) {

        val url = BASE_URL+ OFFER_RIDE_URL+"?user=${cust}"

        val jsonObjRequest: StringRequest = object : StringRequest(
            Method.GET,
            url,
            object : Response.Listener<String?> {
                override fun onResponse(response: String?) {

                    val gson = Gson()

                    val userArray: ArrayList<BookRidesPojoItem> =
                        gson.fromJson(response, BookRidesPojo ::class.java)

//                    hitFindBookedRideAPI(cust)
                    getRating()

                    tvRidesC.text = " ${userArray.size} rides published"

                }
            }, object : Response.ErrorListener {
                override fun onErrorResponse(error: VolleyError) {
                    VolleyLog.d("volley", "Error: " + error.message)
                    error.printStackTrace()
                    Log.e("Responceis",  "Error: " + error.message)

                    Toast.makeText(this@DriverProfile,"Something Went Wrong ! Please try after some time",
                        Toast.LENGTH_LONG).show()

                    progressDialog.dismiss()
                }
            }) {


            override fun getHeaders(): MutableMap<String, String> {

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

}
package com.ddsio.productionapp.sharesavari.SearchScreen.child

import android.app.ProgressDialog
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.RatingBar
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import com.android.volley.*
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.bumptech.glide.Glide
import com.ddsio.productionapp.sharesavari.CommonUtils.Utils
import com.ddsio.productionapp.sharesavari.R
import com.google.gson.Gson
import com.productionapp.amhimemekar.CommonUtils.BookRidesPojoItem
import com.productionapp.amhimemekar.CommonUtils.Configure
import com.productionapp.amhimemekar.CommonUtils.Configure.BASE_URL
import com.productionapp.amhimemekar.CommonUtils.Configure.COMPLAINT
import com.productionapp.amhimemekar.CommonUtils.Configure.GET_USER_DETAILS
import com.productionapp.amhimemekar.CommonUtils.Configure.RATING
import com.productionapp.amhimemekar.CommonUtils.FetchProfileData
import kotlinx.android.synthetic.main.activity_driver_profile.*
import kotlinx.android.synthetic.main.activity_driver_profile.ivCloseScreen
import kotlinx.android.synthetic.main.reset_password_dialog.view.*
import kotlin.math.roundToInt

class DriverProfile : AppCompatActivity() {

    lateinit var pojoWithData : BookRidesPojoItem
    lateinit var progressDialog: ProgressDialog
    lateinit var USER_ID_KEY : String
    var LOGIN_TOKEN = ""
    var request: RequestQueue? = null
    lateinit var dialog_otp: AlertDialog
    var USER_UPDATE_ID = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_driver_profile)

        val bundle: Bundle? = intent.extras
        pojoWithData = bundle!!.get("pojoWithData") as BookRidesPojoItem


        LOGIN_TOKEN = Utils.getStringFromPreferences(Configure.LOGIN_KEY,"",this)!!
        USER_UPDATE_ID = Utils.getStringFromPreferences(Configure.USER_UPDATE_ID,"",this)!!
        USER_ID_KEY = Utils.getStringFromPreferences(Configure.USER_ID_KEY,"",this)!!
        request= Volley.newRequestQueue(this);


        ivCloseScreen.setOnClickListener {
            onBackPressed()
        }

        getDriverData()

        Log.d("hhjbh", pojoWithData.image.toString())

        if (pojoWithData.image != null ) {
            Glide.with(this).load(pojoWithData.image).into(cvProf)
        }

        tvName.text = pojoWithData.username

        rating.setOnRatingBarChangeListener(object : RatingBar.OnRatingBarChangeListener{
            override fun onRatingChanged(p0: RatingBar?, p1: Float, p2: Boolean) {
                 if (p0!!.rating > 0 ) {
                     btnSubmit.visibility = View.VISIBLE
                 } else {
                     btnSubmit.visibility = View.GONE
                 }
            }
        })

        btnSubmit.setOnClickListener {
            submitRating()
        }


        tvReport.setOnClickListener {
            showComplaintDialog()
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

    private fun submitRating() {
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

                return params
            }
        }
        request!!.add(jsonObjRequest)

    }


    fun getDriverData( ) {

        progressDialog = ProgressDialog(this)
        progressDialog.setMessage("Wait a Sec....Loading Details..")
        progressDialog.setCancelable(false)
        progressDialog.show()


        val url = BASE_URL+ GET_USER_DETAILS+pojoWithData.user+"/"
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

                            tvName.text =  userArray.first_name +" "+userArray.last_name

                            if (userArray.verification == "False") {
                                ivAlert.setImageDrawable(resources.getDrawable(R.drawable.alert))
                            } else {
                                ivAlert.setImageDrawable(resources.getDrawable(R.drawable.tick))
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
}
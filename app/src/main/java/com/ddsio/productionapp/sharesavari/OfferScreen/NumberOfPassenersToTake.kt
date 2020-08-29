package com.ddsio.productionapp.sharesavari.OfferScreen

import android.app.ProgressDialog
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.android.volley.*
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.ddsio.productionapp.sharesavari.CommonUtils.Utils
import com.ddsio.productionapp.sharesavari.MainActivity
import com.ddsio.productionapp.sharesavari.R
import com.productionapp.amhimemekar.CommonUtils.Configure
import com.productionapp.amhimemekar.CommonUtils.offerRideModel
import kotlinx.android.synthetic.main.activity_number_of_passeners_to_take.*

class NumberOfPassenersToTake : AppCompatActivity() {

    var count = 1
    var request: RequestQueue? = null
    lateinit var pojoWithData : offerRideModel
    var totalAmt = 0

    var LOGIN_TOKEN = ""
    lateinit var progressDialog: ProgressDialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_number_of_passeners_to_take)


        LOGIN_TOKEN = Utils.getStringFromPreferences(Configure.LOGIN_KEY,"",this)!!
        val bundle: Bundle? = intent.extras
        pojoWithData = bundle!!.get("pojoWithData") as offerRideModel
        request= Volley.newRequestQueue(this);

        tvTotalPrice.text =  "0₹"

        tvCount.setText(count.toString())


        cvMinus.setOnClickListener {
            if (count != 0) {
                count--
                tvCount.setText(count.toString())
            }
        }


        ivBacknumber.setOnClickListener {
            onBackPressed()
        }

        cvAdd.setOnClickListener {
            count++
            tvCount.setText(count.toString())
        }

        etPrice.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                 totalAmt = etPrice.text.toString().toInt() * tvCount.text.toString().toInt()
                tvTotalPrice.text = totalAmt.toString() + "₹"
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {

               totalAmt = etPrice.text.toString().toInt() * tvCount.text.toString().toInt()
                tvTotalPrice.text = totalAmt.toString() + "₹"
            }

        })

        fabFinish.setOnClickListener {

            pojoWithData.price = totalAmt.toString()
            pojoWithData.passenger = tvCount.text.toString()

            hitOfferRideAPI()
        }
    }

    private fun hitOfferRideAPI() {

        progressDialog = ProgressDialog(this)
        progressDialog.setMessage("Wait a Sec....Loging In")
        progressDialog.setCancelable(false)
        progressDialog.show()

        pojoWithData.id =  ""
        pojoWithData.username =  ""
        pojoWithData.url =  ""
        pojoWithData.image =  ""
        pojoWithData.comment =  ""

        if (pojoWithData.rdate!!.isEmpty()) {
            pojoWithData.is_return = "false"
        } else {
            pojoWithData.is_return = "true"
        }

        val url = Configure.BASE_URL + Configure.OFFER_RIDE_URL
        Log.d("jukjbkj", url.toString())

        val jsonObjRequest: StringRequest = object : StringRequest(
            Method.POST,
            url,
            object : Response.Listener<String?> {
                override fun onResponse(response: String?) {
                    Log.d("jukjbkj", response.toString())
                    Toast.makeText(this@NumberOfPassenersToTake,"Ride Offered Successfully...",
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

                    Toast.makeText(this@NumberOfPassenersToTake,"Something Went Wrong ! Please try after some time",
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
                params["id"] =  pojoWithData.id.toString()
                params["url"] = pojoWithData.url.toString()
                params["username"] = pojoWithData.username.toString()
                params["user"] =  pojoWithData.user.toString()
                params["image"] = pojoWithData.image.toString()
                params["leaving"] = pojoWithData.leaving.toString()
                params["lline"] =  pojoWithData.lline.toString()
                params["lcity"] = pojoWithData.lcity.toString()
//                params["llat"] = pojoWithData.llat.toString()
//                params["llog"] =  pojoWithData.llog.toString()
                params["llog"] =  "20.0"
                params["llat"] = "10.0"
                params["going"] = pojoWithData.going.toString()
//                params["glog"] = pojoWithData.glog.toString()
//                params["glat"] = pojoWithData.glat.toString()
                params["glog"] =  "20.0"
                params["glat"] = "10.0"
                params["gcity"] = pojoWithData.gcity.toString()
                params["gline"] = pojoWithData.gline.toString()
                params["date"] =  pojoWithData.date.toString()
                params["time"] = pojoWithData.time.toString()
                params["rdate"] = pojoWithData.rdate.toString()
                params["rtime"] =  pojoWithData.rtime.toString()
                params["price"] = pojoWithData.price.toString()
                params["passenger"] = pojoWithData.passenger.toString()
                params["comment"] = pojoWithData.comment.toString()
                params["is_return"] = pojoWithData.is_return.toString()

                return params
            }
        }
        request!!.add(jsonObjRequest)

    }


}
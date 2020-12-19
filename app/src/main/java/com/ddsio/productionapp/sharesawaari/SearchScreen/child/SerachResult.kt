package com.ddsio.productionapp.sharesawaari.SearchScreen.child

import android.app.ProgressDialog
import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.animation.AnimationUtils
import android.view.animation.LayoutAnimationController
import android.widget.TextView
import android.widget.Toast
import androidx.core.app.ActivityOptionsCompat
import androidx.recyclerview.widget.RecyclerView
import com.android.volley.*
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.bumptech.glide.Glide
import com.ddsio.productionapp.sharesawaari.CommonUtils.Utils
import com.ddsio.productionapp.sharesawaari.R
import com.google.gson.Gson
import com.productionapp.amhimemekar.CommonUtils.*
import com.productionapp.amhimemekar.CommonUtils.Configure.BASE_URL
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.Item
import com.xwray.groupie.ViewHolder
import de.hdodenhof.circleimageview.CircleImageView
import io.github.inflationx.viewpump.ViewPumpContextWrapper
import kotlinx.android.synthetic.main.activity_serach_result.*
import kotlinx.android.synthetic.main.custom_search_rides.view.*
import kotlinx.android.synthetic.main.custom_show_list_rides.view.ivDirect
import kotlinx.android.synthetic.main.custom_show_list_rides.view.ivuser
import kotlinx.android.synthetic.main.custom_show_list_rides.view.tvFromCity
import kotlinx.android.synthetic.main.custom_show_list_rides.view.tvName
import kotlinx.android.synthetic.main.custom_show_list_rides.view.tvToCity
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

class SerachResult : AppCompatActivity() {

    var request: RequestQueue? = null
    lateinit var progressDialog: ProgressDialog
    var LOGIN_TOKEN = ""
    var USER_UPDATE_ID = ""
    lateinit var USER_ID_KEY : String
    lateinit var rvRides : RecyclerView

    lateinit var from : String
    lateinit var to : String
    lateinit var date : String
    lateinit var passenger : String
    val adapter = GroupAdapter<ViewHolder>()

    override fun attachBaseContext(newBase: Context) {
        super.attachBaseContext(ViewPumpContextWrapper.wrap(newBase))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_serach_result)

        LOGIN_TOKEN = Utils.getStringFromPreferences(Configure.LOGIN_KEY,"",this)!!
        USER_UPDATE_ID = Utils.getStringFromPreferences(Configure.USER_UPDATE_ID,"",this)!!
        USER_ID_KEY = Utils.getStringFromPreferences(Configure.USER_ID_KEY,"",this)!!
        request= Volley.newRequestQueue(this)

        val bundle: Bundle? = intent.extras
        from = bundle!!.getString("from")!!
        to  = bundle!!.getString("to")!!
        date  = bundle!!.getString("date")!!
        passenger  = bundle!!.getString("passenger")!!


        tvFromCity.text = to.toString()
        tvToCity.text = from.toString()
        tvDates.text =  Utils.convertDateFormat(date)
        tvPass.text = passenger.toString() +" Passengers"

        rvRides =  findViewById<RecyclerView>(R.id.rvRides)

        ivB.setOnClickListener {
            onBackPressed()
        }

        hitSearchAPI()

    }

    private fun hitSearchAPI() {

        val cTime = Calendar.getInstance()
        val dfTime = SimpleDateFormat("HH:mm:ss")
        val formattedTime = dfTime.format(cTime.time)


        val c = Calendar.getInstance()
        val df = SimpleDateFormat("yyyy-MM-dd")
        val formattedDate = df.format(c.time)

        progressDialog = ProgressDialog(this)
        progressDialog.setMessage("Wait a Sec....Loading Rides..")
        progressDialog.setCancelable(false)
        progressDialog.show()

        val url = BASE_URL+ Configure.OFFER_RIDE_URL +"?gcity="+from+"&lcity="+to+"&date="+date

        val jsonObjRequest: StringRequest = object : StringRequest(
            Method.GET,
            url,
            object : Response.Listener<String?> {
                override fun onResponse(response: String?) {
                    Log.d("jukjbkj", date+"   "+formattedDate.toString()+"   "+formattedTime.toString())

                    val gson = Gson()

                    val userArray: java.util.ArrayList<BookRidesPojoItem> =
                        gson.fromJson(response, BookRidesPojo ::class.java)

                    if (userArray.size == 0) {
                        progressDialog.dismiss()
                        Toast.makeText(this@SerachResult,"Sorry, No Rides available at this Route",Toast.LENGTH_LONG).show()

                    } else{

                        for (rides in userArray) {
                            if (rides != null) {
                                if (date == formattedDate) {
                                    if (rides.user.toString() != USER_ID_KEY && rides.time.toString() > formattedTime.toString()) {
                                        checkPass(rides)
                                    }
                                } else if (rides.user.toString() != USER_ID_KEY) {
                                    checkPass(rides)
                                }

                            }
                        }

//                        if (adapter.itemCount == 0) {
//                            Toast.makeText(this@SerachResult,"No Ride Found",
//                                Toast.LENGTH_LONG).show()
//                        }

                        runAnimation(rvRides,2)
                        rvRides.adapter = adapter
                        rvRides.adapter!!.notifyDataSetChanged()
                        rvRides.scheduleLayoutAnimation()
                        progressDialog.dismiss()

                    }

                }
            }, object : Response.ErrorListener {
                override fun onErrorResponse(error: VolleyError) {
                    VolleyLog.d("volley", "Error: " + error.message)
                    error.printStackTrace()
                    Log.e("Responceis",  "Error: " + error.message)

                    Toast.makeText(this@SerachResult,"Something Went Wrong ! Please try after some time",
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
                    java.util.HashMap()

                return params
            }
        }
        request!!.add(jsonObjRequest)

    }


    private fun checkPass(customers: BookRidesPojoItem) {

        val url = Configure.BASE_URL + Configure.Book_RIDE_URL+"?ride=${customers.id}"

        val jsonObjRequest: StringRequest = object : StringRequest(
            Method.GET,
            url,
            object : Response.Listener<String?> {
                override fun onResponse(response: String?) {
                    Log.d("jukjbkjf", response.toString())

                    val gson = Gson()

                    val userArray : ArrayList<bookrideItem> =
                        gson.fromJson(response, bookride ::class.java)

                    if (userArray.size < customers.passenger!!.toInt()) {
                        var remainingSeats = customers.passenger!!.toInt() - userArray.size
                        if (remainingSeats >= passenger.toInt()) {
                            adapter.add(ridesClass(customers))
                        } else {
                            progressDialog.dismiss()
                        }
                    } else {
                        progressDialog.dismiss()
                    }
                }
            }, object : Response.ErrorListener {
                override fun onErrorResponse(error: VolleyError) {
                    VolleyLog.d("volley", "Error: " + error.message)
                    error.printStackTrace()
                    Log.e("Responceis",  "Error: " + error.message)

                    Toast.makeText(this@SerachResult,"Something Went Wrong ! Please try after some time",
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
                    java.util.HashMap()
//                params.put("ride",customers.id.toString())
//                params.put("passenger",USER_ID_KEY)

                return params
            }
        }
        request!!.add(jsonObjRequest)

    }




    private fun runAnimation(recyclerview_xml_list_userprof: RecyclerView?, type : Int) {
        var context = recyclerview_xml_list_userprof!!.context
        lateinit var controller : LayoutAnimationController

        if(type == 2)
            controller = AnimationUtils.loadLayoutAnimation(context,R.anim.layout_slide_from_right)

        recyclerview_xml_list_userprof.layoutAnimation = controller
    }


    inner class ridesClass(var customers: BookRidesPojoItem) : Item<ViewHolder>() {
        override fun getLayout(): Int {
            return R.layout.custom_search_rides
        }

        override fun bind(viewHolder: ViewHolder, position: Int) {

            viewHolder.itemView.tvFromCity.text = customers.lcity
            viewHolder.itemView.tvToCity.text = customers.gcity
            viewHolder.itemView.tvLTime.text = customers.time
            viewHolder.itemView.tvETime.text = customers.tdtime
//            viewHolder.itemView.tvName.text = customers.username
            viewHolder.itemView.tvPrices.text = "₹ "+customers.price.toString()

            viewHolder.itemView.tvETime.text = customers.tdtime

            getRating(customers,viewHolder.itemView.tvR)

            findUser(customers.user.toString(),viewHolder.itemView.tvName,viewHolder.itemView.ivuser)

            Log.d("jukjbkjf",customers.user.toString())

            if (customers.is_direct == true) {
                viewHolder.itemView.ivDirect.visibility = View.VISIBLE
            } else {
                viewHolder.itemView.ivDirect.visibility = View.GONE
            }

            viewHolder.itemView.setOnClickListener {

                Log.d("jukjbkjf",customers.id.toString())

                var int = Intent( viewHolder.itemView.tvFromCity.context,
                    RideDetails::class.java)
                val bundle =
                    ActivityOptionsCompat.makeCustomAnimation(
                        viewHolder.itemView.tvFromCity.context ,
                        R.anim.fade_in, R.anim.fade_out
                    ).toBundle()
                int.putExtra("pojoWithData",customers)
                int.putExtra("screen","search")
                int.putExtra("IDToCancel","0")
                startActivity(int,bundle)
            }

        }
    }


    private fun findUser(
        passenger: String,
        tvName: TextView,
        ivprof: CircleImageView
    ) {

        val url = BASE_URL+ Configure.GET_USER_DETAILS +passenger+"/"
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
                        tvName.text = userArray.first_name

                        if (userArray.image != null) {
                            Glide.with(this@SerachResult).load(userArray.image).into(ivprof)
                        }
                    }
                    progressDialog.dismiss()
                }
            }, object : Response.ErrorListener {
                override fun onErrorResponse(error: VolleyError) {
                    VolleyLog.d("volley", "Error: " + error.message)
                    error.printStackTrace()
                    Log.e("Responceis",  "Error: " + error.message)

                    Toast.makeText(this@SerachResult,"Something Went Wrong ! Please try after some time",
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


    private fun getRating(
        customers: BookRidesPojoItem,
        tvRating: TextView
    ) {

        var rating = 0
        var userRatedCount = 0

        val url = BASE_URL+ Configure.RATING
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

                            if (userArray.get(i).driver == customers.user) {
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
                            tvRating.text = finalRating.toString() +"/5"

                        }

                    } else {
                        tvRating.text = "0"+"/5"
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
}
package com.ddsio.productionapp.sharesawaari.SearchScreen.child

import android.app.ProgressDialog
import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.animation.AnimationUtils
import android.view.animation.LayoutAnimationController
import android.widget.TextView
import android.widget.Toast
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
import kotlinx.android.synthetic.main.activity_reviews_list.*
import kotlinx.android.synthetic.main.activity_ride_detail.view.tvRating
import kotlinx.android.synthetic.main.custom_reviews.view.*
import kotlinx.android.synthetic.main.custom_reviews.view.tvName
import org.json.JSONException
import org.json.JSONObject
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

class ReviewsList : AppCompatActivity() {

    lateinit var progressDialog: ProgressDialog
    lateinit var pojoWithData : BookRidesPojoItem
    var LOGIN_TOKEN = ""
    var USER_UPDATE_ID = ""
    lateinit var USER_ID_KEY : String
    val adapter = GroupAdapter<ViewHolder>()
    var request: RequestQueue? = null
    lateinit var driverid : String
    lateinit var type : String

    override fun attachBaseContext(newBase: Context) {
        super.attachBaseContext(ViewPumpContextWrapper.wrap(newBase))
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_reviews_list)

        progressDialog = ProgressDialog(this)

        val bundle: Bundle? = intent.extras
        driverid = bundle!!.getString("driverid")!!
        type = bundle!!.getString("type")!!
        pojoWithData = bundle!!.get("pojoWithData") as BookRidesPojoItem

        LOGIN_TOKEN = Utils.getStringFromPreferences(Configure.LOGIN_KEY,"",this)!!
        USER_UPDATE_ID = Utils.getStringFromPreferences(Configure.USER_UPDATE_ID,"",this)!!
        USER_ID_KEY = Utils.getStringFromPreferences(Configure.USER_ID_KEY,"",this)!!
        request= Volley.newRequestQueue(this)

        ivBacR.setOnClickListener {
            onBackPressed()
        }

        if (type == "self"  ) {
            Log.d("aaaa","if" + driverid)
            getRatingSelf(driverid)
        } else if ( type == "copas") {
            getRatingSelf(driverid)
        } else {
            Log.d("aaaa","if" + pojoWithData.user)
            getRatingSelf(pojoWithData.user.toString())
        }
    }

    private fun showRatings(
        user: RatingModelItem,
        ride: BookRidesPojoItem
    ) {
        val c = Calendar.getInstance()
        val df = SimpleDateFormat("yyyy-MM-dd")
        val formattedDate = df.format(c.time)

        val cTime = Calendar.getInstance()
        val dfTime = SimpleDateFormat("HH:mm:ss")
        val formattedTime = dfTime.format(cTime.time)


        val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm")


        var date = sdf.parse(ride.rdate +" "+ride.rtime)
        var time = ""

        if (ride.is_return == false ) {
            date = sdf.parse(ride.tddate +" "+ride.tdtime)
            time = ride.tdtime.toString()

        } else {
            date = sdf.parse(ride.brdate +" "+ride.brtime)
            time = ride.brtime.toString()
        }


        val calendar = Calendar.getInstance()
        calendar.time = date
        calendar.add(Calendar.HOUR, 24)

        val Year = calendar[Calendar.YEAR]
        var Month = calendar[Calendar.MONTH]
        var Day = calendar[Calendar.DAY_OF_MONTH]
        val Hour = calendar[Calendar.HOUR]
        val Minute = calendar[Calendar.MINUTE]
        val Second = calendar[Calendar.SECOND]

        Month = Month+1
        Day = Day-1

        var datePlus = Year.toString()+"-"+Month+"-"+Day
        var timePlus =  Hour.toString()+":"+Minute+":"+Second


        val calendar2 = Calendar.getInstance()
        calendar2.time = date
        calendar2.add(Calendar.HOUR, 48)

        val Year2 = calendar[Calendar.YEAR]
        var Month2 = calendar[Calendar.MONTH]
        var Day2 = calendar[Calendar.DAY_OF_MONTH]
        val Hour2 = calendar[Calendar.HOUR]
        val Minute2 = calendar[Calendar.MINUTE]
        val Second2 = calendar[Calendar.SECOND]

        Month2 = Month2+1
        Day2 = Day2-1

        var datePlus48 = Year2.toString()+"-"+Month2+"-"+Day2

        Log.d("timeis:",datePlus+"   "+formattedDate+"   "+time.toString() +"      "+formattedTime+"    "+user.comment)

            if (formattedDate > datePlus48) {
                adapter.add(ridesClass(user))
                progressDialog.dismiss()
            }

          else {
            progressDialog.dismiss()
//            Toast.makeText(this@ReviewsList, "No Review Found. Given Reviews Will appear after 24 hours", Toast.LENGTH_LONG).show()
        }
    }



    private fun getTimeStamp(s: String): Long {
        val formatter: DateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
        val date = formatter.parse(s) as Date
        return date.time
    }


    private fun getRating(user: String) {



        val url = Configure.BASE_URL + Configure.RATING +"?driver=" + user

        Log.d("ddddd",user)

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

                        if (userArray.size == 0) {
                            Toast.makeText(this@ReviewsList, "No Review Found", Toast.LENGTH_LONG).show()
                        }
                        for (i in 0..userArray.size-1)   {
//                                adapter.add(ridesClass(userArray.get(i)))

                            if (!userArray.get(i).passenger.toString().equals(user)) {
//                                adapter.add(ridesClass(userArray.get(i)))
                            }
                        }
                    } else {
                        Toast.makeText(this@ReviewsList, "No Review Found", Toast.LENGTH_LONG).show()
                    }
                    progressDialog.dismiss()
                }
            }, object : Response.ErrorListener {
                override fun onErrorResponse(error: VolleyError) {
                    VolleyLog.d("volley", "Error: " + error.message)
                    error.printStackTrace()
                    Log.e("Responceis",  "Error: " + error.message)
                    Toast.makeText(this@ReviewsList, "Please try after sometime", Toast.LENGTH_LONG).show()
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



    private fun getRatingSelf(user: String) {

        progressDialog.setMessage("Wait a Sec.... ")
        progressDialog.setCancelable(false)
        progressDialog.show()
        val url = Configure.BASE_URL + Configure.RATING +"?driver=" + user

        Log.d("ddddd",user)

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

                        if (userArray.size == 0) {
                            Toast.makeText(this@ReviewsList, "No Review Found", Toast.LENGTH_LONG).show()
                        }
                        for (i in 0..userArray.size-1)   {
//                                adapter.add(ridesClass(userArray.get(i)))

                            if (!userArray.get(i).passenger.toString().equals(user)) {

                                hitRideSearch(userArray.get(i) )
                            }
                        }
                        runAnimation(recReview,2)
                        recReview.adapter = adapter
                        recReview.adapter!!.notifyDataSetChanged()
                        recReview.scheduleLayoutAnimation()
                        progressDialog.dismiss()
                    } else {
                        progressDialog.dismiss()
                        Toast.makeText(this@ReviewsList, "No Review Found", Toast.LENGTH_LONG).show()
                    }

                }
            }, object : Response.ErrorListener {
                override fun onErrorResponse(error: VolleyError) {
                    VolleyLog.d("volley", "Error: " + error.message)
                    error.printStackTrace()
                    Log.e("Responceis",  "Error: " + error.message)
                    Toast.makeText(this@ReviewsList, "Please try after sometime", Toast.LENGTH_LONG).show()
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



    private fun hitRideSearch(ratePojo: RatingModelItem) {

        val url = Configure.BASE_URL + Configure.OFFER_RIDE_URL + "${ratePojo.ride}/"

        val jsonObjRequest: StringRequest = object : StringRequest(
            Method.GET,
            url,
            object : Response.Listener<String?> {
                override fun onResponse(response: String?) {

                    try {
                        // get JSONObject from JSON file
                        val obj = JSONObject(response.toString())
                        // fetch JSONObject named employee
//                        val employee: JSONObject = obj.getJSONObject("employee")

                        var ride = BookRidesPojoItem()
                        ride.comment = obj.getString("comment")
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
                        ride.carname= obj.getString("carname")
                        ride.carcolor= obj.getString("carcolor")
                        ride.is_direct= obj.getBoolean("is_direct")
                        ride.stitle= obj.getString("stitle")
                        ride.brdate= obj.getString("brdate")
                        ride.brtime= obj.getString("brtime")

                        showRatings(ratePojo,ride)

                    } catch (e: JSONException) {
                        e.printStackTrace()

                        progressDialog.dismiss()

                    }


                }
            }, object : Response.ErrorListener {
                override fun onErrorResponse(error: VolleyError) {
                    VolleyLog.d("volley", "Error: " + error.message)
                    error.printStackTrace()
                    Log.e("Responceis",  "Error: " + error.message)

                    Toast.makeText(this@ReviewsList,"Something Went Wrong ! Please try after some time",
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


    private fun runAnimation(recyclerview_xml_list_userprof: RecyclerView?, type : Int) {
        var context = recyclerview_xml_list_userprof!!.context
        lateinit var controller : LayoutAnimationController

        if(type == 2)
            controller = AnimationUtils.loadLayoutAnimation(context,R.anim.layout_slide_from_right)

        recyclerview_xml_list_userprof.layoutAnimation = controller
    }


    inner class ridesClass(var customers: RatingModelItem) : Item<ViewHolder>() {
        override fun getLayout(): Int {
            return R.layout.custom_reviews
        }

        override fun bind(viewHolder: ViewHolder, position: Int) {
            viewHolder.itemView.tvRating.text = customers.points.toString()+"/5 Rating"
            viewHolder.itemView.tvReview.text = customers.comment

            if (customers.comment.isEmpty() || customers.comment == "") {
                viewHolder.itemView.tvReview.visibility = View.GONE
            }

            findUser(customers.passenger,viewHolder.itemView.tvName,viewHolder.itemView.ivprof)
        }
    }

    private fun findUser(
        passenger: Int,
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
                            Glide.with(this@ReviewsList).load(userArray.image).into(ivprof)
                        }
                    }
                    progressDialog.dismiss()
                }
            }, object : Response.ErrorListener {
                override fun onErrorResponse(error: VolleyError) {
                    VolleyLog.d("volley", "Error: " + error.message)
                    error.printStackTrace()
                    Log.e("Responceis",  "Error: " + error.message)

                    Toast.makeText(this@ReviewsList,"Something Went Wrong ! Please try after some time",
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
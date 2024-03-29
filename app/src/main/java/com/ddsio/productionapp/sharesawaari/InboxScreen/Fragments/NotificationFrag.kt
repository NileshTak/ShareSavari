package com.ddsio.productionapp.sharesawaari.InboxScreen.Fragments

import android.Manifest
import android.app.ProgressDialog
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.view.animation.LayoutAnimationController
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import com.android.volley.*
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.ddsio.productionapp.sharesawaari.CommonUtils.Utils
import com.ddsio.productionapp.sharesawaari.R
import com.ddsio.productionapp.sharesawaari.SearchScreen.child.RideDetails
import com.github.florent37.runtimepermission.kotlin.askPermission
import com.google.gson.Gson
import com.productionapp.amhimemekar.CommonUtils.*
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.Item
import com.xwray.groupie.ViewHolder
import kotlinx.android.synthetic.main.custom_booked_rides.view.*
import kotlinx.android.synthetic.main.custom_booked_rides.view.tvFromCity
import kotlinx.android.synthetic.main.custom_booked_rides.view.tvRate
import kotlinx.android.synthetic.main.custom_booked_rides.view.tvToCity
import org.json.JSONException
import org.json.JSONObject
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

class NotificationFrag : Fragment() {

    lateinit var progressDialog: ProgressDialog
    var LOGIN_TOKEN = ""
    var USER_UPDATE_ID = ""
    lateinit var USER_ID_KEY : String
    var request: RequestQueue? = null

    lateinit var rvOfferedRides : RecyclerView
    lateinit var tvTitleTool : TextView
    val adapter = GroupAdapter<ViewHolder>()

    var ridelist = arrayListOf<Int>()


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view =  inflater.inflate(R.layout.fragment_notification, container, false)


        request= Volley.newRequestQueue(activity);
        LOGIN_TOKEN = Utils.getStringFromPreferences(Configure.LOGIN_KEY,"",activity)!!
        USER_UPDATE_ID = Utils.getStringFromPreferences(Configure.USER_UPDATE_ID,"",activity)!!
        USER_ID_KEY = Utils.getStringFromPreferences(Configure.USER_ID_KEY,"",activity)!!

        rvOfferedRides = view.findViewById<RecyclerView>(R.id.rvBookedRides)
        tvTitleTool = view.findViewById<TextView>(R.id.tvTitleBooked)

        progressDialog = ProgressDialog(activity)
        progressDialog.setMessage("Wait a Sec....Loading Rides..")
        progressDialog.setCancelable(false)
        progressDialog.show()


//        askGalleryPermissionLocation()
        hitFindBookedRideAPI()

        return view
    }


    fun getCurrentTimeStamp() : Long {
        val c = Calendar.getInstance()
        val df = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
        val formattedDate = df.format(c.time)
        return getTimeStamp(formattedDate) 
    }

    private fun getTimeStamp(s: String): Long {

        Log.d("timestampdatsi",s)

        val formatter: DateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
        val date = formatter.parse(s) as Date

        Log.d("timestampdatsi",date.time.toString())

        return date.time
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

                AlertDialog.Builder(activity!!)
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


    private fun hitFindBookedRideAPI() {

        var arr = ArrayList<bookrideItem>()

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

                    if (userArray.size == 0) {
                        tvTitleTool.text = "No Booked Rides Found"
                        progressDialog.dismiss()
                    } else {
                        tvTitleTool.text = "Booked Rides : "
                        tvTitleTool.visibility = View.GONE
                    }

                    for (i in 0..userArray.size-1) {

                        arr.clear()

                        if (i != null) {

//                            for (j in 0..userArray.size-1) {
//
//                                 if (userArray.get(i).ride == userArray.get(j).ride) {
//                                     arr.add(userArray.get(i))
//                                 }
//                            }
                            hitRideSearch(userArray.get(i),arr)
                        }
                    }


                }
            }, object : Response.ErrorListener {
                override fun onErrorResponse(error: VolleyError) {
                    VolleyLog.d("volley", "Error: " + error.message)
                    error.printStackTrace()
                    Log.e("Responceis",  "Error: " + error.message)

                    Toast.makeText(activity,"Something Went Wrong ! Please try after some time",
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



    inner class ridesOfferedClass(
        var customers: BookRidesPojoItem,
        var idToCancel: bookrideItem,
        var arr: ArrayList<bookrideItem>
    ) : Item<ViewHolder>() {
        override fun getLayout(): Int {
            return R.layout.custom_booked_rides
        }

        override fun bind(viewHolder: ViewHolder, position: Int) {


                viewHolder.itemView.tvFromCity.text = customers.lcity

                viewHolder.itemView.tvSeats.text = "Seats Booked : "+idToCancel.seats

                viewHolder.itemView.tvToCity.text = customers.gcity

                viewHolder.itemView.tvRate.text = "₹ "+ customers.price.toString()

                viewHolder.itemView.tvDatebs.text = Utils.convertDateFormat(
                    customers.date.toString()
                ) +", " +customers.time.toString()


            viewHolder.itemView.setOnClickListener {

                var int = Intent( activity,
                    RideDetails::class.java)

                int.putExtra("pojoWithData",customers)
                int.putExtra("screen","Booked")
                int.putExtra("IDToCancel", idToCancel.id.toString())
                startActivity(int)
            }
        }
    }


    fun getTimeStampPlusOneDay(s: String) : Long {

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

        return getTimeStamp(Year.toString()+"-"+Month+"-"+Day+" "+Hour+":"+Minute+":"+Second )
    }

    private fun hitRideSearch(
        customers: bookrideItem,
        arr: ArrayList<bookrideItem>
    ) {

        val url = Configure.BASE_URL + Configure.OFFER_RIDE_URL + "${customers.ride}/"

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

//                        if (!ridelist.contains(arr.get(0).ride)) {
//                            ridelist.add(arr.get(0).ride)

                        if (ride.is_return == false) {
                            if (getTimeStampPlusOneDay(ride.tddate.toString() +" " +ride.tdtime.toString()) > getCurrentTimeStamp() ) {
                                adapter.add(ridesOfferedClass(ride, customers, arr))
                            }
                        } else {
                            if (getTimeStampPlusOneDay(ride.brdate.toString() +" " +ride.brtime.toString()) > getCurrentTimeStamp() ) {
                                adapter.add(ridesOfferedClass(ride, customers, arr))
                            }
                        }

//                        }
                    } catch (e: JSONException) {
                        e.printStackTrace()

                        progressDialog.dismiss()

                    }

                    runAnimation(rvOfferedRides,2)
                    rvOfferedRides.adapter = adapter
                    rvOfferedRides.adapter!!.notifyDataSetChanged()
                    rvOfferedRides.scheduleLayoutAnimation()
                    progressDialog.dismiss()

                }
            }, object : Response.ErrorListener {
                override fun onErrorResponse(error: VolleyError) {
                    VolleyLog.d("volley", "Error: " + error.message)
                    error.printStackTrace()
                    Log.e("Responceis",  "Error: " + error.message)

                    Toast.makeText(activity,"Something Went Wrong ! Please try after some time",
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
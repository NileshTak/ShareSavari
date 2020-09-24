package com.ddsio.productionapp.sharesavari.InboxScreen.Fragments

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
import androidx.core.app.ActivityOptionsCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import com.android.volley.*
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.ddsio.productionapp.sharesavari.CommonUtils.Utils
import com.ddsio.productionapp.sharesavari.R
import com.ddsio.productionapp.sharesavari.SearchScreen.child.RideDetails
import com.github.florent37.runtimepermission.kotlin.askPermission
import com.google.gson.Gson
import com.productionapp.amhimemekar.CommonUtils.*
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.Item
import com.xwray.groupie.ViewHolder
import kotlinx.android.synthetic.main.custom_booked_rides.view.*
import org.json.JSONException
import org.json.JSONObject

class NotificationFrag : Fragment() {


    lateinit var progressDialog: ProgressDialog
    var LOGIN_TOKEN = ""
    var USER_UPDATE_ID = ""
    lateinit var USER_ID_KEY : String
    var request: RequestQueue? = null

    lateinit var rvOfferedRides : RecyclerView
    lateinit var tvTitleTool : TextView


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


        askGalleryPermissionLocation()


        return view
    }
    private fun askGalleryPermissionLocation() {
        askPermission(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ) {
            hitFindBookedRideAPI()
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
                    }

                    for (rides in userArray) {
                        if (rides != null) {
                            adapter.add(ridesOfferedClass(rides))
                        }
                        runAnimation(rvOfferedRides,2)
                        rvOfferedRides.adapter = adapter
                        rvOfferedRides.adapter!!.notifyDataSetChanged()
                        rvOfferedRides.scheduleLayoutAnimation()
                        progressDialog.dismiss()
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



    inner class ridesOfferedClass(var customers: bookrideItem) : Item<ViewHolder>() {
        override fun getLayout(): Int {
            return R.layout.custom_booked_rides
        }

        override fun bind(viewHolder: ViewHolder, position: Int) {

//            viewHolder.itemView.tvFromAdd.text = customers.leaving
//            viewHolder.itemView.tvToAdd.text = customers.going
//            viewHolder.itemView.tvDate.text = customers.date +"  ("+customers.time+")"
//            viewHolder.itemView.tvRDate.text = customers.rdate +"  ("+customers.rtime+")"
//            viewHolder.itemView.tvComment.text = customers.comment
//            viewHolder.itemView.tvOfferedby.text = customers.username
//            viewHolder.itemView.tvPass.text = customers.passenger.toString()
//            viewHolder.itemView.tvPrice.text = customers.price.toString()
//            viewHolder.itemView.tvReturn.text = customers.is_return.toString()
//
//
//            viewHolder.itemView.btnBook.visibility = View.GONE


            var add = customers.ridename
            val separated =
                add.split("to".toRegex()).toTypedArray()
            var from = separated[0]

           var to =  separated[1]


            viewHolder.itemView.tvFromFullAdd.text = from
            viewHolder.itemView.tvToFullAdd.text = to

            viewHolder.itemView.tvDate.text = "Dat enot found"

            Log.d("ihdui0",customers.ride.toString())


            viewHolder.itemView.setOnClickListener {
//                var int = Intent( viewHolder.itemView.context,
//                    RideDetails::class.java)
//                val bundle =
//                    ActivityOptionsCompat.makeCustomAnimation(
//                        viewHolder.itemView.context ,
//                        R.anim.fade_in, R.anim.fade_out
//                    ).toBundle()
//
//
//                int.putExtra("pojoWithData",customers)
//                int.putExtra("screen","Booked")
//                startActivity(int,bundle)

                hitRideSearch(customers)
            }

        }
    }

    private fun hitRideSearch(customers: bookrideItem) {

        progressDialog = ProgressDialog(activity)
        progressDialog.setMessage("Wait a Sec.... ")
        progressDialog.setCancelable(false)
        progressDialog.show()

        val adapter = GroupAdapter<ViewHolder>()

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
                        ride.is_direct= obj.getBoolean("is_direct")


                        Log.d("juguiuih",ride.leaving.toString())

                        var int = Intent( activity,
                            RideDetails::class.java)

                        int.putExtra("pojoWithData",ride)
                        int.putExtra("screen","Booked")
                        int.putExtra("IDToCancel",customers.id.toString())
                        startActivity(int)


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
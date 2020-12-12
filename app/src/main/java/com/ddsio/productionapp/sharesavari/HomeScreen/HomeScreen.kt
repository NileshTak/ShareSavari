package com.ddsio.productionapp.sharesavari.HomeScreen

import android.Manifest
import android.app.ProgressDialog
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.view.animation.LayoutAnimationController
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityOptionsCompat
import androidx.recyclerview.widget.RecyclerView
import com.android.volley.*
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.ddsio.productionapp.sharesavari.CommonUtils.Utils
import com.ddsio.productionapp.sharesavari.OfferScreen.NumberOfPassenersToTake
import com.ddsio.productionapp.sharesavari.R
import com.ddsio.productionapp.sharesavari.SearchScreen.child.RideDetails
import com.github.florent37.runtimepermission.kotlin.askPermission
import com.google.gson.Gson
import com.onesignal.OSSubscriptionObserver
import com.onesignal.OSSubscriptionStateChanges
import com.productionapp.amhimemekar.CommonUtils.BookRideScreenFetchCity
import com.productionapp.amhimemekar.CommonUtils.BookRidesPojo
import com.productionapp.amhimemekar.CommonUtils.BookRidesPojoItem
import com.productionapp.amhimemekar.CommonUtils.Configure
import com.productionapp.amhimemekar.CommonUtils.Configure.BASE_URL
import com.productionapp.amhimemekar.CommonUtils.Configure.OFFER_RIDE_URL
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.Item
import com.xwray.groupie.ViewHolder
import kotlinx.android.synthetic.main.activity_number_of_passeners_to_take.*
import kotlinx.android.synthetic.main.custom_show_list_rides.view.*
import kotlinx.android.synthetic.main.fragment_home_screen.*
import kotlinx.android.synthetic.main.fragment_profile_screen.*
import kotlinx.android.synthetic.main.fragment_search.*
import org.greenrobot.eventbus.Subscribe

class HomeScreen : Fragment()
//    , OSSubscriptionObserver
{

    lateinit var progressDialog: ProgressDialog
    var LOGIN_TOKEN = ""
    var USER_UPDATE_ID = ""
    lateinit var USER_ID_KEY : String
    var request: RequestQueue? = null
    var player_id = ""
    lateinit var rvOfferedRides : RecyclerView
    lateinit var tvTitleTool : TextView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        var view = inflater.inflate(R.layout.fragment_home_screen, container, false)

        request= Volley.newRequestQueue(activity);
        LOGIN_TOKEN = Utils.getStringFromPreferences(Configure.LOGIN_KEY,"",activity)!!
        USER_UPDATE_ID = Utils.getStringFromPreferences(Configure.USER_UPDATE_ID,"",activity)!!
        USER_ID_KEY = Utils.getStringFromPreferences(Configure.USER_ID_KEY,"",activity)!!

        rvOfferedRides = view.findViewById<RecyclerView>(R.id.rvOfferedRides)
        tvTitleTool = view.findViewById<TextView>(R.id.tvTitleTool)

        progressDialog = ProgressDialog(activity)
        progressDialog.setMessage("Wait a Sec....Loading Rides..")
        progressDialog.setCancelable(false)
        progressDialog.show()

        sendMobielVerifiedTrueAPI()
        hitFindOfferedRideAPI()

        return view
    }

//    override fun onOSSubscriptionChanged(stateChanges: OSSubscriptionStateChanges?) {
//        if (!stateChanges!!.getFrom().getSubscribed() &&
//            stateChanges.getTo().getSubscribed()) {
//
//            Log.d("ONESIGNALIS",stateChanges.to.userId)
//            player_id = stateChanges.to.userId
//            Utils.writeStringToPreferences(Configure.PLAYER_ID, stateChanges.to.userId.toString(), activity)
//        }
//    }



    private fun hitFindOfferedRideAPI() {

        val adapter = GroupAdapter<ViewHolder>()


        val url = BASE_URL+ OFFER_RIDE_URL+"?user=${USER_ID_KEY}"
//        val url = "https://ddsio.com/sharesawaari/rest/rides/?user=35"


        val jsonObjRequest: StringRequest = object : StringRequest(
            Method.GET,
            url,
            object : Response.Listener<String?> {
                override fun onResponse(response: String?) {

                    val gson = Gson()

                    val userArray: ArrayList<BookRidesPojoItem> =
                        gson.fromJson(response, BookRidesPojo ::class.java)

                    if (userArray.size == 0) {
                        tvTitleTool.text = "No Offered Rides Found"
                        progressDialog.dismiss()
                    } else {
                        tvTitleTool.text = "Offered Rides : "
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

                        updateOneID()

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


    private fun sendMobielVerifiedTrueAPI() {

//        progressDialog = ProgressDialog(activity)
//        progressDialog.setMessage("Wait a Sec....Loading Details..")
//        progressDialog.setCancelable(false)
//        progressDialog.show()


        val url = Configure.BASE_URL + Configure.UPDATE_USER_DETAILS+USER_UPDATE_ID+"/"

        val jsonObjRequest: StringRequest = object : StringRequest(
            Method.PUT,
            url,
            object : Response.Listener<String?> {
                override fun onResponse(response: String?) {
                    Log.d("jukjbkj", response.toString())

//                    progressDialog.dismiss()

                }
            }, object : Response.ErrorListener {
                override fun onErrorResponse(error: VolleyError) {
                    VolleyLog.d("volley", "Error: " + error.message)
                    error.printStackTrace()
                    Log.e("jukjbkj",  "Error: " + error.message)
//                    progressDialog.dismiss()
                    Toast.makeText(activity,"Something Went Wrong ! Please try after some time",
                        Toast.LENGTH_LONG).show()
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
                params["user"] = USER_ID_KEY
                params["mobile_status"] = "true"
                return params
            }
        }

        Utils.setVolleyRetryPolicy(jsonObjRequest)
        request!!.add(jsonObjRequest)

    }



    fun updateOneID( ) {

        if (player_id == null || player_id.isEmpty() || player_id == " ") {
            player_id = Utils.getStringFromPreferences(Configure.PLAYER_ID,"",activity)!!
        }

       var update_ID = Utils.getStringFromPreferences(Configure.USER_UPDATE_ID,"",activity)!!

        val url = Configure.BASE_URL + Configure.UPDATE_USER_DETAILS+ update_ID +"/"

        Log.d("aaaaaaa",player_id+"  sss");

        val jsonObjRequest: StringRequest = object : StringRequest(
            Method.PUT,
            url,
            object : Response.Listener<String?> {
                override fun onResponse(response: String?) {

                    Log.d("responceis",response.toString());

                }
            }, object : Response.ErrorListener {
                override fun onErrorResponse(error: VolleyError) {
                    VolleyLog.d("volley", "Error: " + error.message)
                    error.printStackTrace()

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
                params.put("oneid",player_id)

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



    inner class ridesOfferedClass(var customers: BookRidesPojoItem) : Item<ViewHolder>() {
        override fun getLayout(): Int {
            return R.layout.custom_show_list_rides
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

            viewHolder.itemView.tvFromCity.text = customers.lcity
            viewHolder.itemView.tvToCity.text = customers.gcity
            viewHolder.itemView.tvRate.text = "₹ "+customers.price.toString()
            viewHolder.itemView.tvDates.text = Utils.convertDateFormat(
                customers.date.toString()
            ) +", " +customers.time.toString()

            Log.d("jukjbkjf",customers.user.toString())

            if (customers.is_direct == true) {
                viewHolder.itemView.ivDirect.visibility = View.VISIBLE
            } else {
                viewHolder.itemView.ivDirect.visibility = View.GONE
            }

//            if (customers.is_return == true) {
//                viewHolder.itemView.ivarr.setImageResource(R.drawable.ic_baseline_compare_arrows_24)
//            } else {
//                viewHolder.itemView.ivarr.setImageResource(R.drawable.ic_arrow_forward_black_24dp)
//            }


            viewHolder.itemView.setOnClickListener {
                var int = Intent( viewHolder.itemView.tvFromCity.context,
                    RideDetails::class.java)
                val bundle =
                    ActivityOptionsCompat.makeCustomAnimation(
                        viewHolder.itemView.tvFromCity.context ,
                        R.anim.fade_in, R.anim.fade_out
                    ).toBundle()
                int.putExtra("pojoWithData",customers)
                int.putExtra("screen","home")
                int.putExtra("IDToCancel","0")
                startActivity(int,bundle)
            }

        }
    }


}
package com.ddsio.productionapp.sharesavari.SearchScreen.child

import android.app.ProgressDialog
import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.animation.AnimationUtils
import android.view.animation.LayoutAnimationController
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.android.volley.*
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.bumptech.glide.Glide
import com.ddsio.productionapp.sharesavari.CommonUtils.Utils
import com.ddsio.productionapp.sharesavari.R
import com.google.gson.Gson
import com.productionapp.amhimemekar.CommonUtils.*
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.Item
import com.xwray.groupie.ViewHolder
import io.github.inflationx.viewpump.ViewPumpContextWrapper
import kotlinx.android.synthetic.main.activity_pending_req.*
import kotlinx.android.synthetic.main.activity_ride_detail.view.ivprof
import kotlinx.android.synthetic.main.activity_ride_detail.view.tvOfferedby
import kotlinx.android.synthetic.main.custom_copas_list.view.*

class PendingReq : AppCompatActivity() {


    lateinit var progressDialog: ProgressDialog
    var LOGIN_TOKEN = ""
    var USER_UPDATE_ID = ""
    lateinit var USER_ID_KEY : String
    val adapter = GroupAdapter<ViewHolder>()
    var request: RequestQueue? = null

    lateinit var pojoWithData : BookRidesPojoItem

    override fun attachBaseContext(newBase: Context) {
        super.attachBaseContext(ViewPumpContextWrapper.wrap(newBase))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pending_req)

        val bundle: Bundle? = intent.extras
        pojoWithData = bundle!!.get("pojoWithData") as BookRidesPojoItem

        LOGIN_TOKEN = Utils.getStringFromPreferences(Configure.LOGIN_KEY,"",this)!!
        USER_UPDATE_ID = Utils.getStringFromPreferences(Configure.USER_UPDATE_ID,"",this)!!
        USER_ID_KEY = Utils.getStringFromPreferences(Configure.USER_ID_KEY,"",this)!!
        request= Volley.newRequestQueue(this)

        ivBacK.setOnClickListener {
            onBackPressed()
        }

        hitPendingReq()

    }

    private fun hitPendingReq() {
        progressDialog = ProgressDialog(this)
        progressDialog.setMessage("Wait a Sec....Loading Rides..")
        progressDialog.setCancelable(false)
        progressDialog.show()


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
                            if (userArray.get(i).is_confirm == false) {
//                                adapter.add(ridesClass(userArray.get(i)))
                                getUserData(userArray.get(i) )
                            }
                        }

                        runAnimation(rvPendingReq,2)
                        rvPendingReq.adapter = adapter
                        rvPendingReq.adapter!!.notifyDataSetChanged()
                        rvPendingReq.scheduleLayoutAnimation()
                        progressDialog.dismiss()
                    }

                }
            }, object : Response.ErrorListener {
                override fun onErrorResponse(error: VolleyError) {
                    VolleyLog.d("volley", "Error: " + error.message)
                    error.printStackTrace()
                    Log.e("Responceis",  "Error: " + error.message)

                    Toast.makeText(this@PendingReq,"Something Went Wrong ! Please try after some time",
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


    fun getUserData(passenger: bookrideItem) {
        val url = Configure.BASE_URL + Configure.GET_USER_DETAILS +passenger.passenger+"/"

        val jsonObjRequest: StringRequest = object : StringRequest(
            Method.GET,
            url,
            object : Response.Listener<String?> {
                override fun onResponse(response: String?) {
                    Log.d("jukjbkjdd", response.toString())

                    val gson = Gson()

                    if (response != null ) {

                        val userArray: FetchProfileData =
                            gson.fromJson(response, FetchProfileData ::class.java)

                        adapter.add(ridesClass(userArray,passenger))

                    }

                }
            }, object : Response.ErrorListener {
                override fun onErrorResponse(error: VolleyError) {
                    VolleyLog.d("volley", "Error: " + error.message)
                    error.printStackTrace()
                    Log.e("Responceis",  "Error: " + error.message)

                    Toast.makeText(this@PendingReq,"Something Went Wrong ! Please try after some time",
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




    private fun runAnimation(recyclerview_xml_list_userprof: RecyclerView?, type : Int) {
        var context = recyclerview_xml_list_userprof!!.context
        lateinit var controller : LayoutAnimationController

        if(type == 2)
            controller = AnimationUtils.loadLayoutAnimation(context,R.anim.layout_slide_from_right)

        recyclerview_xml_list_userprof.layoutAnimation = controller
    }


    inner class ridesClass(
        var customers: FetchProfileData,
        var passenger: bookrideItem
    ) : Item<ViewHolder>() {
        override fun getLayout(): Int {
            return R.layout.custom_copas_list
        }

        override fun bind(viewHolder: ViewHolder, position: Int) {
            viewHolder.itemView.tvOfferedby.text = customers.first_name
            Glide.with( viewHolder.itemView.tvOfferedby.context).load(customers.image).into(viewHolder.itemView.ivprof)
            viewHolder.itemView.llBtn.visibility = View.VISIBLE

            viewHolder.itemView.cvAccept.setOnClickListener {
                acceptReq(customers,passenger)
            }
        }
    }

    private fun acceptReq(
        customers: FetchProfileData,
        passenger: bookrideItem
    ) {
        progressDialog = ProgressDialog(this)
        progressDialog.setMessage("Wait a Sec....Accepting Rides..")
        progressDialog.setCancelable(false)
        progressDialog.show()


        val url = Configure.BASE_URL + Configure.Book_RIDE_URL+"/${passenger.id}/"

        val jsonObjRequest: StringRequest = object : StringRequest(
            Method.PUT,
            url,
            object : Response.Listener<String?> {
                override fun onResponse(response: String?) {
                    Log.d("jukjbkjf", response.toString())

                    val gson = Gson()

                    val userArray : ArrayList<bookrideItem> =
                        gson.fromJson(response, bookride ::class.java)
                    progressDialog.dismiss()

                    hitPendingReq()

                }
            }, object : Response.ErrorListener {
                override fun onErrorResponse(error: VolleyError) {
                    VolleyLog.d("volley", "Error: " + error.message)
                    error.printStackTrace()
                    Log.e("Responceis",  "Error: " + error.message)

                    Toast.makeText(this@PendingReq,"Something Went Wrong ! Please try after some time",
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
                    java.util.HashMap()
                params.put("ride",passenger.ride.toString())
                params.put("passenger",passenger.passenger.toString())
                params.put("is_confirm","true")

                return params
            }
        }
        request!!.add(jsonObjRequest)
    }
}
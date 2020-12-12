package com.ddsio.productionapp.sharesavari.SearchScreen.child

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
import com.ddsio.productionapp.sharesavari.CommonUtils.Utils
import com.ddsio.productionapp.sharesavari.InboxScreen.Child.ChatLogActivity
import com.ddsio.productionapp.sharesavari.InboxScreen.Child.NewMessageActivity
import com.ddsio.productionapp.sharesavari.R
import com.ddsio.productionapp.sharesavari.User
import com.google.gson.Gson
import com.productionapp.amhimemekar.CommonUtils.*
import com.productionapp.amhimemekar.CommonUtils.Configure.BASE_URL
import com.productionapp.amhimemekar.CommonUtils.Configure.GET_USER_DETAILS
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.Item
import com.xwray.groupie.ViewHolder
import io.github.inflationx.viewpump.ViewPumpContextWrapper
import kotlinx.android.synthetic.main.activity_co_pas_list.*
import kotlinx.android.synthetic.main.activity_ride_detail.view.ivprof
import kotlinx.android.synthetic.main.activity_ride_detail.view.tvOfferedby
import kotlinx.android.synthetic.main.activity_ride_detail.view.tvRating
import kotlinx.android.synthetic.main.custom_copas_list.view.*

class CoPasList : AppCompatActivity() {

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
        setContentView(R.layout.activity_co_pas_list)

        val bundle: Bundle? = intent.extras
        pojoWithData = bundle!!.get("pojoWithData") as BookRidesPojoItem

        LOGIN_TOKEN = Utils.getStringFromPreferences(Configure.LOGIN_KEY,"",this)!!
        USER_UPDATE_ID = Utils.getStringFromPreferences(Configure.USER_UPDATE_ID,"",this)!!
        USER_ID_KEY = Utils.getStringFromPreferences(Configure.USER_ID_KEY,"",this)!!
        request= Volley.newRequestQueue(this)

        ivBac.setOnClickListener {
            onBackPressed()
        }

        hitCopasAPI()
    }

    private fun hitCopasAPI() {

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
//                                adapter.add(ridesClass(userArray.get(i)))

                                if (userArray.get(i).is_confirm) {
                                    getUserData(userArray.get(i) )
                                }

                            }

                            runAnimation(rvCOPas,2)
                            rvCOPas.adapter = adapter
                            rvCOPas.adapter!!.notifyDataSetChanged()
                            rvCOPas.scheduleLayoutAnimation()
                            progressDialog.dismiss()
                        }

                }
            }, object : Response.ErrorListener {
                override fun onErrorResponse(error: VolleyError) {
                    VolleyLog.d("volley", "Error: " + error.message)
                    error.printStackTrace()
                    Log.e("Responceis",  "Error: " + error.message)

                    Toast.makeText(this@CoPasList,"Something Went Wrong ! Please try after some time",
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
        val url = BASE_URL+ GET_USER_DETAILS+passenger.passenger+"/"

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

                    Toast.makeText(this@CoPasList,"Something Went Wrong ! Please try after some time",
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
            viewHolder.itemView.cvAccept.visibility = View.GONE
            Glide.with( viewHolder.itemView.tvOfferedby.context).load(customers.image).into(viewHolder.itemView.ivprof)
            getRating(customers,viewHolder.itemView.tvRating)
            viewHolder.itemView.tvSeats.text = "Seats :"+passenger.seats

            viewHolder.itemView.setOnClickListener {
                var int = Intent(viewHolder.itemView.tvOfferedby.context,
                    DriverProfile::class.java)
                val bundle =
                    ActivityOptionsCompat.makeCustomAnimation(
                        viewHolder.itemView.tvOfferedby.context ,
                        R.anim.fade_in, R.anim.fade_out
                    ).toBundle()
                int.putExtra("pojoWithData",pojoWithData)
                int.putExtra("cust",customers.id.toString())
                int.putExtra("type","copas")
                startActivity(int,bundle)
            }

            viewHolder.itemView.cvContact.setOnClickListener {
                var user = User(customers.id.toString() , customers.first_name!!,pojoWithData.image.toString())

                val intent = Intent(viewHolder.itemView.cvContact.context, ChatLogActivity::class.java)
                intent.putExtra(NewMessageActivity.USER_KEY,user)
                startActivity(intent)
            }
        }
    }


    private fun getRating(
        customers: FetchProfileData,
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

                            if (userArray.get(i).driver == customers.id) {
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
                            tvRating.text = finalRating.toString()+"/5 ratings"

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

}
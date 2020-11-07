package com.ddsio.productionapp.sharesavari.SearchScreen.child

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
import com.ddsio.productionapp.sharesavari.CommonUtils.Utils
import com.ddsio.productionapp.sharesavari.R
import com.google.gson.Gson
import com.productionapp.amhimemekar.CommonUtils.*
import com.productionapp.amhimemekar.CommonUtils.Configure.BASE_URL
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.Item
import com.xwray.groupie.ViewHolder
import de.hdodenhof.circleimageview.CircleImageView
import io.github.inflationx.viewpump.ViewPumpContextWrapper
import kotlinx.android.synthetic.main.activity_co_pas_list.*
import kotlinx.android.synthetic.main.activity_driver_profile.*
import kotlinx.android.synthetic.main.activity_reviews_list.*
import kotlinx.android.synthetic.main.activity_ride_detail.view.tvRating
import kotlinx.android.synthetic.main.custom_reviews.view.*
import kotlinx.android.synthetic.main.custom_reviews.view.tvName
import kotlinx.android.synthetic.main.fragment_profile_screen.*

class ReviewsList : AppCompatActivity() {


    lateinit var progressDialog: ProgressDialog
    var LOGIN_TOKEN = ""
    var USER_UPDATE_ID = ""
    lateinit var USER_ID_KEY : String
    val adapter = GroupAdapter<ViewHolder>()
    var request: RequestQueue? = null
    lateinit var driverid : String



    override fun attachBaseContext(newBase: Context) {
        super.attachBaseContext(ViewPumpContextWrapper.wrap(newBase))
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_reviews_list)

        val bundle: Bundle? = intent.extras
        driverid = bundle!!.getString("driverid")!!

        LOGIN_TOKEN = Utils.getStringFromPreferences(Configure.LOGIN_KEY,"",this)!!
        USER_UPDATE_ID = Utils.getStringFromPreferences(Configure.USER_UPDATE_ID,"",this)!!
        USER_ID_KEY = Utils.getStringFromPreferences(Configure.USER_ID_KEY,"",this)!!
        request= Volley.newRequestQueue(this)

        ivBacR.setOnClickListener {
            onBackPressed()
        }

        getRating()

    }


    private fun getRating() {

        progressDialog = ProgressDialog(this)
        progressDialog.setMessage("Wait a Sec.... ")
        progressDialog.setCancelable(false)
        progressDialog.show()




        val url = Configure.BASE_URL + Configure.RATING +"?driver=" + driverid
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
                            adapter.add(ridesClass(userArray.get(i)))
                        }

                        runAnimation(recReview,2)
                        recReview.adapter = adapter
                        recReview.adapter!!.notifyDataSetChanged()
                        recReview.scheduleLayoutAnimation()
                        progressDialog.dismiss()
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
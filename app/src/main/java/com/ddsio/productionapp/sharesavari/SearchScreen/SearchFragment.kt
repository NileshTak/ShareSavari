package com.ddsio.productionapp.sharesavari.SearchScreen

import android.app.ProgressDialog
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.view.animation.LayoutAnimationController
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.cardview.widget.CardView
import androidx.core.app.ActivityOptionsCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import com.android.volley.*
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.ddsio.productionapp.sharesavari.CommonUtils.Utils
import com.ddsio.productionapp.sharesavari.R
import com.ddsio.productionapp.sharesavari.SearchScreen.child.RideDetails
import com.ddsio.productionapp.sharesavari.ShowMap.ShowMapActivity
import com.google.gson.Gson
import com.productionapp.amhimemekar.CommonUtils.*
import com.productionapp.amhimemekar.CommonUtils.Configure.BASE_URL
import com.productionapp.amhimemekar.CommonUtils.Configure.Book_RIDE_URL
import com.productionapp.amhimemekar.CommonUtils.Configure.OFFER_RIDE_URL
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.Item
import com.xwray.groupie.ViewHolder
import kotlinx.android.synthetic.main.custom_show_list_rides.view.*
import kotlinx.android.synthetic.main.fragment_search.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import java.util.*

class SearchFragment : Fragment() {

    lateinit var cvFromLocation : CardView
    lateinit var cvToLocation : CardView
    lateinit var tvFromAdd : TextView
    lateinit var btnSearch : Button
    lateinit var progressDialog: ProgressDialog

    lateinit var rvRides : RecyclerView

    var LOGIN_TOKEN = ""
    var USER_UPDATE_ID = ""
    lateinit var USER_ID_KEY : String
    var request: RequestQueue? = null


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_search, container, false)

        request= Volley.newRequestQueue(activity);

        cvFromLocation = view.findViewById(R.id.cvFromLocation)
        cvToLocation = view.findViewById(R.id.cvToLocation)
        tvFromAdd = view.findViewById<TextView>(R.id.tvFromAdd)
        btnSearch = view.findViewById<Button>(R.id.btnSearch)
        rvRides = view.findViewById<RecyclerView>(R.id.rvRides)


        LOGIN_TOKEN = Utils.getStringFromPreferences(Configure.LOGIN_KEY,"",activity)!!
        USER_UPDATE_ID = Utils.getStringFromPreferences(Configure.USER_UPDATE_ID,"",activity)!!
        USER_ID_KEY = Utils.getStringFromPreferences(Configure.USER_ID_KEY,"",activity)!!


        cvFromLocation.setOnClickListener {

            val intent =  (Intent(activity, ShowMapActivity::class.java))
            intent.putExtra("typeis","from")
            startActivity(intent)

        }


        cvToLocation.setOnClickListener {

            val intent =  (Intent(activity, ShowMapActivity::class.java))
            intent.putExtra("typeis","to")
            startActivity(intent)

        }

        btnSearch.setOnClickListener {


            checkFields()

        }

        return view
    }

    private fun checkFields() {
        progressDialog = ProgressDialog(activity)
        progressDialog.setMessage("Wait a Sec....Loading Rides..")
        progressDialog.setCancelable(false)
        progressDialog.show()

        if (tvFromAdd.text.isEmpty() || tvFromAdd.text == "") {
            Toast.makeText(activity,"Please Enter Correct Leaving Address",
                Toast.LENGTH_LONG).show()

            progressDialog.dismiss()
        } else if ( tvToAdd.text.isEmpty() || tvToAdd.text == "") {
            Toast.makeText(activity,"Please Enter Correct Going Address",
                Toast.LENGTH_LONG).show()
            progressDialog.dismiss()
        } else {
            hitFindRideAPI()
        }
    }

    private fun hitFindRideAPI() {

        val adapter = GroupAdapter<ViewHolder>()


            val url = BASE_URL+ OFFER_RIDE_URL+"?gcity="+tvToAdd.text.toString()+"&lcity="+tvFromAdd.text.toString()
//        val url = "https://ddsio.com/sharesawaari/rest/users/22/"


            val jsonObjRequest: StringRequest = object : StringRequest(
                Method.GET,
                url,
                object : Response.Listener<String?> {
                    override fun onResponse(response: String?) {
                        Log.d("jukjbkj", response.toString())

                        val gson = Gson()

                        val userArray: ArrayList<BookRidesPojoItem> =
                            gson.fromJson(response, BookRidesPojo ::class.java)
                        for (rides in userArray) {
                            if (rides != null) {
                                if (rides.user.toString() != USER_ID_KEY) {

                                    adapter.add(ridesClass(rides))
                                }
                            }
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

                        Toast.makeText(activity,"Something Went Wrong ! Please try after some time",
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



    @Subscribe
    fun OnAddSelected(add : BookRideScreenFetchCity?) {
        if (add!!.type == "from") {
            tvFromAdd.text = add!!.city
        } else {
            tvToAdd.text = add!!.city
        }

    }

    override fun onStart() {
        super.onStart()
        if (!EventBus.getDefault().isRegistered(this)) EventBus.getDefault().register(this)
    }

    override fun onDestroy() {
        super.onDestroy()
        EventBus.getDefault().unregister(this)
    }


    inner class ridesClass(var customers: BookRidesPojoItem) : Item<ViewHolder>() {
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


                viewHolder.itemView.tvFromCity.text = customers.lcity
                viewHolder.itemView.tvToCity.text = customers.gcity


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
                startActivity(int,bundle)
            }



        }
    }
}
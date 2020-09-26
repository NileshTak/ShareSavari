package com.ddsio.productionapp.sharesavari.SearchScreen

import android.Manifest
import android.app.DatePickerDialog
import android.app.ProgressDialog
import android.app.TimePickerDialog
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
import android.widget.TimePicker
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
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
import com.ddsio.productionapp.sharesavari.SearchScreen.child.SerachResult
import com.ddsio.productionapp.sharesavari.ShowMap.ShowMapActivity
import com.github.florent37.runtimepermission.kotlin.askPermission
import com.google.gson.Gson
import com.productionapp.amhimemekar.CommonUtils.*
import com.productionapp.amhimemekar.CommonUtils.Configure.BASE_URL
import com.productionapp.amhimemekar.CommonUtils.Configure.OFFER_RIDE_URL
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.Item
import com.xwray.groupie.ViewHolder
import kotlinx.android.synthetic.main.custom_show_list_rides.view.*
import kotlinx.android.synthetic.main.fragment_search.tvToAdd
import kotlinx.android.synthetic.main.select_passenger_number.view.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import java.text.SimpleDateFormat
import java.util.*

class SearchFragment : Fragment()  {

    lateinit var cvFromLocation : CardView
    lateinit var cvToLocation : CardView
    lateinit var tvFromAdd : TextView
    lateinit var tvPassenger : TextView
    lateinit var tvDate : TextView
    var selectedDateFinal = ""
    lateinit var btnSearch : Button
    lateinit var progressDialog: ProgressDialog
    lateinit var datePickerdialog: DatePickerDialog
    val currentDate = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())
    var formate = SimpleDateFormat("yyyy-MM-dd", Locale.US)
    lateinit var rvRides : RecyclerView

    var LOGIN_TOKEN = ""
    var USER_UPDATE_ID = ""
    lateinit var USER_ID_KEY : String
    var request: RequestQueue? = null
    lateinit var dialog_otp: AlertDialog


    var dateAPi = currentDate
    var passengerCount = 1


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_search, container, false)

        request= Volley.newRequestQueue(activity);

        cvFromLocation = view.findViewById(R.id.cvFromLocation)
        cvToLocation = view.findViewById(R.id.cvToLocation)
        tvFromAdd = view.findViewById<TextView>(R.id.tvFromAdd)
        tvPassenger = view.findViewById<TextView>(R.id.tvPassenger)
        tvDate = view.findViewById<TextView>(R.id.tvDate)
        btnSearch = view.findViewById<Button>(R.id.btnSearch)
//        rvRides = view.findViewById<RecyclerView>(R.id.rvRides)


        LOGIN_TOKEN = Utils.getStringFromPreferences(Configure.LOGIN_KEY,"",activity)!!
        USER_UPDATE_ID = Utils.getStringFromPreferences(Configure.USER_UPDATE_ID,"",activity)!!
        USER_ID_KEY = Utils.getStringFromPreferences(Configure.USER_ID_KEY,"",activity)!!

        askGalleryPermissionLocation()

        cvFromLocation.setOnClickListener {
            val intent =  (Intent(activity, ShowMapActivity::class.java))
            intent.putExtra("typeis","from")
            startActivity(intent)
        }

        tvDate.setOnClickListener {
            datePicker()
        }


        tvPassenger.setOnClickListener {
            showDialog()
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

    private fun showDialog() {

        var count = 1

        val inflater = getLayoutInflater()
        val alertLayout = inflater.inflate(R.layout.select_passenger_number, null)

        alertLayout.cvAdd.setOnClickListener {
            count++
            alertLayout.tvCount.setText(count.toString())
        }


        alertLayout.setOnClickListener {
            if (count != 0) {
                count--
                alertLayout.tvCount.setText(count.toString())
            }
        }

        alertLayout.cvDone.setOnClickListener {

            passengerCount = alertLayout.tvCount.text.toString().toInt()

            tvPassenger.setText(alertLayout.tvCount.text.toString() +" Passenger")
            dialog_otp.dismiss()
        }

        val showOTP = AlertDialog.Builder(activity!!)
        showOTP.setView(alertLayout)
        showOTP.setCancelable(false)
        dialog_otp = showOTP.create()
        dialog_otp.show()

        alertLayout.ivCloseP.setOnClickListener {
            dialog_otp.dismiss()
        }

    }

    private fun datePicker() {
        val now = Calendar.getInstance()
        datePickerdialog = DatePickerDialog(
            activity!! , DatePickerDialog.OnDateSetListener { view, year, month, dayOfMonth ->
                val selectedDate = Calendar.getInstance()
                selectedDate.set(Calendar.YEAR, year)
                selectedDate.set(Calendar.MONTH, month)
                selectedDate.set(Calendar.DAY_OF_MONTH, dayOfMonth)
                val date = formate.format(selectedDate.time)

                selectedDateFinal = date

                dateAPi = selectedDateFinal

//                showTimeDialog()
                tvDate.setText(selectedDateFinal)
            },
            now.get(Calendar.YEAR), now.get(Calendar.MONTH), now.get(Calendar.DAY_OF_MONTH)
        )
        datePickerdialog.show()
    }

    private fun showTimeDialog() {
        // Get Current Time

        // Get Current Time
        val c = Calendar.getInstance()
        var mHour = c[Calendar.HOUR_OF_DAY]
        var mMinute = c[Calendar.MINUTE]

        // Launch Time Picker Dialog

        // Launch Time Picker Dialog
        val timePickerDialog = TimePickerDialog(
            activity,
            object : TimePickerDialog.OnTimeSetListener  {
                override fun onTimeSet(
                    view: TimePicker?, hourOfDay: Int,
                    minute: Int
                ) {
                    tvDate.setText(selectedDateFinal+ ", $hourOfDay:$minute")
                }
            }, mHour, mMinute, false
        )
        timePickerDialog.show()
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
        }  else if ( passengerCount == 0) {
            Toast.makeText(activity,"Passesenger Count must be more then 0",
                Toast.LENGTH_LONG).show()
            progressDialog.dismiss()
        }else {
//            hitFindRideAPI()
            val intent = Intent(activity, SerachResult::class.java)
            intent.putExtra("from",tvToAdd.text.toString())
            intent.putExtra("to",tvFromAdd.text.toString())
            intent.putExtra("date",dateAPi)
            intent.putExtra("passenger",passengerCount.toString())
            progressDialog.dismiss()
            startActivity(intent)
        }
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


    private fun hitFindRideAPI() {
        val adapter = GroupAdapter<ViewHolder>()

            val url = BASE_URL+ OFFER_RIDE_URL+"?gcity="+tvToAdd.text.toString()+"&lcity="+tvFromAdd.text.toString()+"&date="+dateAPi

        Log.d("datlmkm",url)

            val jsonObjRequest: StringRequest = object : StringRequest(
                Method.GET,
                url,
                object : Response.Listener<String?> {
                    override fun onResponse(response: String?) {
                        Log.d("jukjbkj", response.toString())

                        val gson = Gson()

                        val userArray: ArrayList<BookRidesPojoItem> =
                            gson.fromJson(response, BookRidesPojo ::class.java)

                        if (userArray.size == 0) {
                            progressDialog.dismiss()
                            Toast.makeText(activity,"No Ride Found",
                                Toast.LENGTH_LONG).show()

                        } else{

                            for (rides in userArray) {
                                if (rides != null) {
                                    if (rides.user.toString() != USER_ID_KEY) {

//                                        adapter.add(ridesClass(rides))
                                    }
                                }
                                runAnimation(rvRides,2)
                                rvRides.adapter = adapter
                                rvRides.adapter!!.notifyDataSetChanged()
                                rvRides.scheduleLayoutAnimation()
                                progressDialog.dismiss()
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
        } else if (add!!.type == "to") {
            tvToAdd.text = add!!.city
        }else {
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

}
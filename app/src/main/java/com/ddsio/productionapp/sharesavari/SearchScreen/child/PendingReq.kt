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
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityOptionsCompat
import androidx.recyclerview.widget.RecyclerView
import com.android.volley.*
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.bumptech.glide.Glide
import com.ddsio.productionapp.sharesavari.CommonUtils.Utils
import com.ddsio.productionapp.sharesavari.ProfileScreen.Child.ChatPas
import com.ddsio.productionapp.sharesavari.R
import com.google.firebase.database.FirebaseDatabase
import com.google.gson.Gson
import com.letsbuildthatapp.kotlinmessenger.models.ChatMessage
import com.productionapp.amhimemekar.CommonUtils.*
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.Item
import com.xwray.groupie.ViewHolder
import io.github.inflationx.viewpump.ViewPumpContextWrapper
import kotlinx.android.synthetic.main.activity_pending_req.*
import kotlinx.android.synthetic.main.activity_ride_detail.view.ivprof
import kotlinx.android.synthetic.main.activity_ride_detail.view.tvOfferedby
import kotlinx.android.synthetic.main.custom_copas_list.view.*
import kotlinx.android.synthetic.main.delete_ride_dialog.view.*
import kotlinx.android.synthetic.main.ride_booking_type.view.ccvCancel
import kotlinx.android.synthetic.main.ride_booking_type.view.cvContinue
import kotlinx.android.synthetic.main.ride_booking_type.view.tvNotice
import org.greenrobot.eventbus.EventBus

class PendingReq : AppCompatActivity() {


    lateinit var progressDialog: ProgressDialog
    var LOGIN_TOKEN = ""
    var USER_UPDATE_ID = ""
    lateinit var USER_ID_KEY : String
    val adapter = GroupAdapter<ViewHolder>()
    var request: RequestQueue? = null

    lateinit var custProfile : FetchProfileData
    lateinit var driverProfile : FetchProfileData

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


    override fun onBackPressed() {
        super.onBackPressed()
        EventBus.getDefault().post("Pending");
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


                    if (userArray != null && userArray.size != 0) {
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
                    } else {
                        Toast.makeText(this@PendingReq,"No Request Found",
                            Toast.LENGTH_LONG).show()

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

                    } else {
                        Toast.makeText(this@PendingReq,"No Request Found",
                            Toast.LENGTH_LONG).show()
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



    fun getDriverData(customers : FetchProfileData) {
        val url = Configure.BASE_URL + Configure.GET_USER_DETAILS +pojoWithData.user+"/"

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

                        driverProfile = userArray

                        sendNotificationSelf("You had accepted the request of ${customers.first_name} for Ride ${pojoWithData.leaving} to ${pojoWithData.going} on ${pojoWithData.date}. " +
                                "Please check into app for more details.", driverProfile)


                        var msg = "You%20had%20accepted%20the%20request%20of%20${customers.first_name}%20for%20Ride%20${pojoWithData.leaving}%20to%20${pojoWithData.going}%20on%20${pojoWithData.date}.%20" +
                                "Please%20check%20into%20app%20for%20more%20details."

                        sendSMS(driverProfile, msg)

                    } else {
                        Toast.makeText(this@PendingReq,"No Request Found",
                            Toast.LENGTH_LONG).show()
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
            viewHolder.itemView.tvSeats.text = "Seats :"+passenger.seats

            viewHolder.itemView.cvAccept.setOnClickListener {
                showRideDialog(customers,passenger)
            }


            viewHolder.itemView.setOnClickListener {
                var int = Intent(viewHolder.itemView.tvOfferedby.context,
                    DriverProfile::class.java)
                val bundle =
                    ActivityOptionsCompat.makeCustomAnimation(
                        viewHolder.itemView.tvOfferedby.context ,
                        R.anim.fade_in, R.anim.fade_out
                    ).toBundle()
                int.putExtra("pojoWithData",pojoWithData)
                int.putExtra("type","driver")
                int.putExtra("cust",customers.id.toString())
                startActivity(int,bundle)
            }


            viewHolder.itemView.cvContact.setOnClickListener {
//                var user = User(customers.id.toString() , customers.first_name!!,pojoWithData.image.toString())
//
//                val intent = Intent(viewHolder.itemView.cvContact.context, ChatLogActivity::class.java)
//                intent.putExtra(NewMessageActivity.USER_KEY,user)
//                startActivity(intent)

                val intent = Intent(viewHolder.itemView.cvContact.context, ChatPas::class.java)
//            intent.putExtra(NewMessageActivity.USER_KEY,user)
                val bundle =
                    ActivityOptionsCompat.makeCustomAnimation(
                        viewHolder.itemView.cvContact.context ,
                        R.anim.fade_in, R.anim.fade_out
                    ).toBundle()
                intent.putExtra("driverid" ,customers.id.toString() )
                startActivity(intent,bundle)
            }
        }
    }
    private fun sendNotificationSelf(
        msg: String,
        customers: FetchProfileData
    ) {

        val jsonObjRequest: StringRequest = object : StringRequest(
            Method.POST,
            Configure.ONESIGNAL,
            object : Response.Listener<String?> {
                override fun onResponse(response: String?) {

                }
            }, object : Response.ErrorListener {
                override fun onErrorResponse(error: VolleyError) {

                }
            }) {

            @Throws(AuthFailureError::class)
            override fun getParams(): Map<String, String> {
                val params: MutableMap<String, String> =
                    java.util.HashMap()

                params.put("message",msg)
                params.put("user", customers.oneid.toString())
                return params
            }
        }
        request!!.add(jsonObjRequest)
    }




    private fun acceptReq(
        customers: FetchProfileData,
        passenger: bookrideItem
    ) {
        progressDialog = ProgressDialog(this)
        progressDialog.setMessage("Wait a Sec....Accepting Rides..")
        progressDialog.setCancelable(false)
        progressDialog.show()


        val url = Configure.BASE_URL + Configure.Book_RIDE_URL+"${passenger.id}/"

        val jsonObjRequest: StringRequest = object : StringRequest(
            Method.PUT,
            url,
            object : Response.Listener<String?> {
                override fun onResponse(response: String?) {
                    Log.d("jukjbkjf", response.toString())

                    sendNotificationSelf("Your Request for Ride ${pojoWithData.leaving} to ${pojoWithData.going} on ${pojoWithData.date} has been Accepted by Driver and you can contact directly to driver from Driver's Public Profile'. " +
                            "Please check into app for more details.", customers)

                    var msg = "Your%20Request%20for%20Ride%20${pojoWithData.leaving}%20to%20${pojoWithData.going}%20on%20${pojoWithData.date}%20has%20been%20Accepted%20by%20Driver%20and%20you%20can%20contact%20directly%20to%20driver%20from%20Driver's%20Public%20Profile'.%20" +
                            "Please%20check%20into%20app%20for%20more%20details."

                    getDriverData(passenger ,customers)

                    sendMessage(customers)

                    sendSMS(customers, msg)

                    progressDialog.dismiss()

                    showRideAcceptedDialog(customers, passenger)

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

    fun getDriverData(
        cust: bookrideItem,
        customers: FetchProfileData
    ) {

        val url = Configure.BASE_URL + Configure.GET_USER_DETAILS +cust.passenger+"/"

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

                        if (userArray != null) {
                            custProfile = userArray
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


    private fun sendMessage(
        userArray: FetchProfileData
    ) {

        var text = "Your Ride has been Accepted by Driver."


        val fromId = USER_ID_KEY
        val toId = userArray.id.toString()

        if (fromId == null) return

        val reference = FirebaseDatabase.getInstance().getReference("/user-messages/$fromId/$toId").push()

        val toReference = FirebaseDatabase.getInstance().getReference("/user-messages/$toId/$fromId").push()

        val chatMessage = ChatMessage(reference.key!!, text, fromId, toId, System.currentTimeMillis() / 1000)

        reference.setValue(chatMessage)
            .addOnSuccessListener {
                progressDialog.dismiss()

                if (pojoWithData.is_direct == true) {

                } else {
                      }

            }
            .addOnFailureListener {
                progressDialog.dismiss()
            }

        toReference.setValue(chatMessage)

        val latestMessageRef = FirebaseDatabase.getInstance().getReference("/latest-messages/$fromId/$toId")
        latestMessageRef.setValue(chatMessage)

        val latestMessageToRef = FirebaseDatabase.getInstance().getReference("/latest-messages/$toId/$fromId")
        latestMessageToRef.setValue(chatMessage)
    }


    private fun sendSMS(
        customers: FetchProfileData,
        msg: String
    ) {

        val url = "http://login.bulksmsgateway.in/sendmessage.php?user=prasadbirari&password=Janardan1&mobile=${customers.mobile}&message=${msg}&sender=MSGSAY&type=3"

        val jsonObjRequest: StringRequest = object : StringRequest(
            Method.GET,
            url,
            object : Response.Listener<String?> {
                override fun onResponse(response: String?) {
                    Log.d("smsentvjjnd", response.toString())


                    getDriverData(customers)


                }
            }, object : Response.ErrorListener {
                override fun onErrorResponse(error: VolleyError) {
                    VolleyLog.d("volley", "Error: " + error.message)
                    error.printStackTrace()
                    Log.e("Responceis",  "Error: " + error.message)


                    progressDialog.dismiss()
                }
            }) {


            override fun getHeaders(): MutableMap<String, String> {

                Log.d("jukjbkj", LOGIN_TOKEN.toString())

                var params = java.util.HashMap<String, String>()
                params.put("Content-Type", "application/x-www-form-urlencoded");
                params.put("Cache-Control", "no-cache");
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



    lateinit var convidPoster: AlertDialog
    private fun showRideDialog(
        customers: FetchProfileData,
        passenger: bookrideItem
    ) {
        val inflater = getLayoutInflater()
        val alertLayout = inflater.inflate(R.layout.delete_ride_dialog, null)

        val showOTP = AlertDialog.Builder(this!!)
        showOTP.setView(alertLayout)
        showOTP.setCancelable(false)
        convidPoster = showOTP.create()
        convidPoster.show()

            alertLayout.tvNotice.text = "Are you sure you want to Accept this Ride Request? "


        alertLayout.cvContinue.setOnClickListener {
            acceptReq(customers,passenger)
            convidPoster.dismiss()
        }

        alertLayout.ccvCancel.setOnClickListener {
            convidPoster.dismiss()
        }

    }



    private fun showRideAcceptedDialog(
        customers: FetchProfileData,
        passenger: bookrideItem
    ) {
        val inflater = getLayoutInflater()
        val alertLayout = inflater.inflate(R.layout.delete_ride_dialog, null)

        val showOTP = AlertDialog.Builder(this!!)
        showOTP.setView(alertLayout)
        showOTP.setCancelable(false)
        alertLayout.okbtn.text = "OK"
        convidPoster = showOTP.create()
        convidPoster.show()

        alertLayout.tvNotice.text = "Request Accepted "
        alertLayout.tvNotice.setTextSize( 22F)

        alertLayout.lo.visibility = View.GONE

        alertLayout.cvContinue.setOnClickListener {
            convidPoster.dismiss()
            onBackPressed()
        }

        alertLayout.ccvCancel.setOnClickListener {
            rvPendingReq.removeAllViewsInLayout()
            rvPendingReq.removeAllViews()
            adapter.clear()

            hitPendingReq()
            convidPoster.dismiss()

        }

    }
}
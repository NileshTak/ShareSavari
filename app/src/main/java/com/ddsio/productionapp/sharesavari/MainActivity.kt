package com.ddsio.productionapp.sharesavari

import android.Manifest
import android.app.DatePickerDialog
import android.app.ProgressDialog
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.util.Patterns
import android.view.View
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.core.app.ActivityCompat
import androidx.core.app.ActivityOptionsCompat
import androidx.core.widget.NestedScrollView
import androidx.lifecycle.lifecycleScope
import com.android.volley.*
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.ddsio.productionapp.sharesavari.CommonUtils.FileUtil
import com.ddsio.productionapp.sharesavari.CommonUtils.Utils
import com.ddsio.productionapp.sharesavari.CommonUtils.VolleyMultipartRequest
import com.ddsio.productionapp.sharesavari.CommonUtils.VolleyMultipartRequest.VolleyProgressListener
import com.ddsio.productionapp.sharesavari.CommonUtils.VolleySingleton
import com.ddsio.productionapp.sharesavari.HomeScreen.HomeScreen
import com.ddsio.productionapp.sharesavari.InboxScreen.InboxScreen
import com.ddsio.productionapp.sharesavari.OfferScreen.OfferScreen
import com.ddsio.productionapp.sharesavari.OfferScreen.ShowMapActivityPickUp
import com.ddsio.productionapp.sharesavari.ProfileScreen.ProfileScreen
import com.ddsio.productionapp.sharesavari.SearchScreen.SearchFragment
import com.github.florent37.runtimepermission.kotlin.askPermission
import com.google.firebase.FirebaseApp
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import com.google.gson.Gson
import com.letsbuildthatapp.kotlinmessenger.models.User
import com.productionapp.amhimemekar.CommonUtils.*
import com.productionapp.amhimemekar.CommonUtils.Configure.LOGIN_KEY
import id.zelory.compressor.Compressor
import id.zelory.compressor.constraint.format
import id.zelory.compressor.constraint.quality
import id.zelory.compressor.constraint.resolution
import id.zelory.compressor.constraint.size
import id.zelory.compressor.loadBitmap
import io.github.inflationx.viewpump.ViewPumpContextWrapper
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.convid_poster_layout.view.*
import kotlinx.android.synthetic.main.reset_password_dialog.view.*
import kotlinx.coroutines.launch
import org.json.JSONException
import org.json.JSONObject
import java.io.File
import java.io.IOException
import java.lang.Math.log10
import java.net.URLConnection
import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.util.*
import java.util.regex.Pattern
import kotlin.collections.ArrayList
import kotlin.collections.HashMap
import kotlin.math.pow

class MainActivity : AppCompatActivity() {

    var GALLERY_REQUEST = 1666
    var ADHAR_REQUEST = 1888
    var ADHARB_REQUEST = 1777
    lateinit var frameContainer: FrameLayout
    lateinit var llSearch: LinearLayout
    lateinit var llLogin: LinearLayout
    lateinit var tvSignUp: TextView
    lateinit var tvLogin: TextView
    lateinit var tvForgotPass: TextView
    var imageUri = ""

    lateinit var USER_ID_KEY : String

    var type = ""
    lateinit var nsvSignUp: NestedScrollView

    lateinit var dialog_otp: AlertDialog

    lateinit var convidPoster: AlertDialog

    lateinit var bitmap: Bitmap


    lateinit var emailtxtlogin: String
    lateinit var passtxtlogin: String


    var request: RequestQueue? = null
    lateinit var progressDialog: ProgressDialog
    lateinit var datePickerdialog: DatePickerDialog

    var LOGIN_TOKEN = ""

    private var actualProfImage: File? = null
    private var compressedProfImage: File? = null
    private var actualAdharImage: File? = null
    private var compressedAdharImage: File? = null
    private var actualAdharImageB: File? = null
    private var compressedAdharImageB: File? = null

    var selectedPhotoUri: Uri? = null
    lateinit var cvSignUp: CardView

    lateinit var etFN: EditText
    lateinit var etLN: EditText
    lateinit var etUserName: EditText


    var fn = ""
    var ln = ""
    var un = ""

    var profPicURL: String? = null
    var adharPicURL: String? = null
    var adharPicURLB: String? = null

    var emailPattern = "[a-zA-Z0-9._-]+@[a-z]+\\.+[a-z]+"

    val currentDate = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())
    var formate = SimpleDateFormat("yyyy-MM-dd", Locale.US)

    override fun attachBaseContext(newBase: Context) {
        super.attachBaseContext(ViewPumpContextWrapper.wrap(newBase))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val bundle: Bundle? = intent.extras
        type = bundle!!.getString("type")!!

        request = Volley.newRequestQueue(this);
        FirebaseApp.initializeApp(this)

        tvSignUp = findViewById<TextView>(R.id.tvSignUp)
        tvForgotPass = findViewById<TextView>(R.id.tvForgotPass)
        nsvSignUp = findViewById<NestedScrollView>(R.id.nsvSignUp)
        tvLogin = findViewById<TextView>(R.id.tvLogin)
        frameContainer = findViewById<FrameLayout>(R.id.frame)
        llSearch = findViewById<LinearLayout>(R.id.llSearch)
        llLogin = findViewById<LinearLayout>(R.id.llLogin)
        cvSignUp = findViewById<CardView>(R.id.cvSignUp)

        etFN = findViewById<EditText>(R.id.etFN)
        etLN = findViewById<EditText>(R.id.etLN)
        etUserName = findViewById<EditText>(R.id.etLN)

//        Utils.writeStringToPreferences(LOGIN_KEY, "",this)

        LOGIN_TOKEN = Utils.getStringFromPreferences(LOGIN_KEY, "", this)!!


        Handler().postDelayed({
            Utils.checkConnection(this@MainActivity, frameContainer)
            if (!Utils.CheckGpsStatus(this@MainActivity)) {
                Utils.enableGPS(this@MainActivity)
            }
        }, 2000)


        loadScreens()

        if (type == "SignUp") {
            nsvSignUp.visibility = View.VISIBLE
            llLogin.visibility = View.GONE
            llNav.visibility = View.GONE
            frame.visibility = View.GONE
        } else if (type == "LogIn") {
            llLogin.visibility = View.VISIBLE
            llNav.visibility = View.GONE
            nsvSignUp.visibility = View.GONE
            frame.visibility = View.GONE
        } else {
            loadScreens()
        }


        cvLoginFB.setOnClickListener {
            Toast.makeText(this, "Coming Soon.....", Toast.LENGTH_LONG).show()
        }

        cvSignUpFB.setOnClickListener {
            Toast.makeText(this, "Coming Soon.....", Toast.LENGTH_LONG).show()
        }


        tvForgotPass.setOnClickListener {
            showRestPassDialog()
        }

        cvLogin.setOnClickListener {

            emailtxtlogin = loginetEmail.text.toString()
            passtxtlogin = loginetPass.text.toString()

            checkFieldsLogin(emailtxtlogin, passtxtlogin)

        }

        etBirthDate.setText(currentDate)
        etBirthDate.setOnClickListener {
            getBirthDate()
        }


        etGender.setOnFocusChangeListener(object : View.OnFocusChangeListener {
            override fun onFocusChange(p0: View?, p1: Boolean) {
                if (p1 == true) {
                    setGenderList()
                }
            }
        })


        cvSignUp.setOnClickListener {
            progressDialog = ProgressDialog(this@MainActivity)
            progressDialog.setMessage("Wait a Sec....Uploading Files")
            progressDialog.setCancelable(false)
            progressDialog.show()

            checkFieldsSignUp()
        }

        llSearch.setOnClickListener {
            changeIconColor(ivSearch, tvSearch, "Search")
            if (LOGIN_TOKEN != null && LOGIN_TOKEN != "") {
                loadSearchFrag(fragHome = SearchFragment())
            } else {
                llLogin.visibility = View.VISIBLE
                nsvSignUp.visibility = View.GONE
                frame.visibility = View.GONE
            }
        }

        llOffer.setOnClickListener {

            changeIconColor(ivOffer, tvOffer, "Offer")
            if (LOGIN_TOKEN != null && LOGIN_TOKEN != "") {

                var int = Intent(
                    this,
                    ShowMapActivityPickUp::class.java
                )
                val bundle =
                    ActivityOptionsCompat.makeCustomAnimation(
                        this@MainActivity,
                        R.anim.fade_in,
                        R.anim.fade_out
                    ).toBundle()
                int.putExtra("screen", "Offer")
                startActivity(int, bundle)
            } else {
                llLogin.visibility = View.VISIBLE
                nsvSignUp.visibility = View.GONE
                frame.visibility = View.GONE
            }

        }

        llHome.setOnClickListener {
            changeIconColor(ivHome, tvHome, "Home")
            if (LOGIN_TOKEN != null && LOGIN_TOKEN != "") {
                loadHomeFrag(fragHome = HomeScreen())
            } else {
                llLogin.visibility = View.VISIBLE
                nsvSignUp.visibility = View.GONE
                frame.visibility = View.GONE
            }

        }

        llInbox.setOnClickListener {
            changeIconColor(ivInbox, tvInbox, "Inbox")
            if (LOGIN_TOKEN != null && LOGIN_TOKEN != "") {
                loadInboxFrag(fragHome = InboxScreen())
            } else {
                llLogin.visibility = View.VISIBLE
                nsvSignUp.visibility = View.GONE
                frame.visibility = View.GONE
            }

        }


        civProfImg.setOnClickListener {
            askCameraPermission(GALLERY_REQUEST)

        }


        civAdharImg.setOnClickListener {
            askCameraPermission(ADHAR_REQUEST)
        }


        civAdharImgB.setOnClickListener {
            askCameraPermission(ADHARB_REQUEST)
        }

        llProfile.setOnClickListener {
            changeIconColor(ivProfile, tvProfile, "Profile")
            if (LOGIN_TOKEN != null && LOGIN_TOKEN != "") {
                loadProfileFrag(fragHome = ProfileScreen())
            } else {
                llLogin.visibility = View.VISIBLE
                nsvSignUp.visibility = View.GONE
                frame.visibility = View.GONE
            }
        }

        tvSignUp.setOnClickListener {
            nsvSignUp.visibility = View.VISIBLE
            llLogin.visibility = View.GONE
            frame.visibility = View.GONE
        }

        tvLogin.setOnClickListener {
            llLogin.visibility = View.VISIBLE
            nsvSignUp.visibility = View.GONE
            frame.visibility = View.GONE
        }


    }

    private fun showConvidPoster() {
        val inflater = getLayoutInflater()
        val alertLayout = inflater.inflate(R.layout.convid_poster_layout, null)

        val showOTP = AlertDialog.Builder(this!!)
        showOTP.setView(alertLayout)
        showOTP.setCancelable(false)
        convidPoster = showOTP.create()
        convidPoster.show()

        alertLayout.cvGotIt.setOnClickListener {
            convidPoster.dismiss()
        }

    }

    private fun showRestPassDialog() {
        val inflater = getLayoutInflater()
        val alertLayout = inflater.inflate(R.layout.reset_password_dialog, null)

        alertLayout.cvReset!!.setOnClickListener {
            val verificationCode = alertLayout.loginetEmail!!.text!!.toString()
            if (verificationCode.isEmpty()) {
//                Toast.makeText(this@Authentication, "Enter verification code", Toast.LENGTH_SHORT).show()
                Toast.makeText(this, "Please Enter Valid Email Address", Toast.LENGTH_LONG).show()

            } else {

                hitResetAPI(verificationCode)
                dialog_otp.dismiss()
            }
        }

        val showOTP = AlertDialog.Builder(this!!)
        showOTP.setView(alertLayout)
        showOTP.setCancelable(false)
        dialog_otp = showOTP.create()
        dialog_otp.show()

        alertLayout.ivCloseReset.setOnClickListener {
            dialog_otp.dismiss()
        }

    }

    private fun hitResetAPI(email: String) {

        progressDialog = ProgressDialog(this@MainActivity)
        progressDialog.setMessage("Wait a Sec....Loging In")
        progressDialog.setCancelable(false)
        progressDialog.show()

        var url = Configure.BASE_URL + Configure.REST_PASS

        Log.d("Responceis", url)

        val jsonObjRequest: StringRequest = object : StringRequest(
            Method.POST,
            url,
            object : Response.Listener<String?> {
                override fun onResponse(response: String?) {

                    Log.i("Responceis", response.toString())

                    progressDialog.dismiss()

                }
            }, object : Response.ErrorListener {
                override fun onErrorResponse(error: VolleyError) {
                    VolleyLog.d("volley", "Error: " + error.message)
                    error.printStackTrace()

                    Toast.makeText(
                        applicationContext,
                        "RESET E-Mail has been sent to your given E-Mail Address.",
                        Toast.LENGTH_LONG
                    ).show()

                    progressDialog.dismiss()

                }
            }) {
            override fun getBodyContentType(): String {
                return "application/x-www-form-urlencoded; charset=UTF-8"
            }

            @Throws(AuthFailureError::class)
            override fun getParams(): Map<String, String> {
                val params: MutableMap<String, String> =
                    HashMap()
                params["email"] = email

                return params
            }
        }
        jsonObjRequest.setRetryPolicy(
            DefaultRetryPolicy(
                5000,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT
            )
        )
        request!!.add(jsonObjRequest)
    }


    override fun onBackPressed() {
        super.onBackPressed()
        ActivityCompat.finishAffinity(this)
    }


    private fun askCameraPermission(RequestType: Int) {
        askPermission(
            Manifest.permission.CAMERA,
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
        ) {
            val intent = Intent(Intent.ACTION_GET_CONTENT)
            intent.type = "image/*"
            startActivityForResult(intent, RequestType)
        }.onDeclined { e ->
            if (e.hasDenied()) {
                //the list of denied permissions
                e.denied.forEach {
                }

                AlertDialog.Builder(this)
                    .setMessage("Please accept our permissions.. Otherwise you will not be able to use some of our Important Features.")
                    .setPositiveButton("yes") { dialog, which ->
                        e.askAgain()
                    } //ask again
                    .setNegativeButton("no") { dialog, which ->
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


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == AppCompatActivity.RESULT_OK) {
            if (data == null) {
                Toast.makeText(this, "null", Toast.LENGTH_LONG).show()
                return
            }
            try {
                if (requestCode == GALLERY_REQUEST) {
                    selectedPhotoUri = data.data

                    actualProfImage = FileUtil.from(this, data.data!!)?.also {

                        civProfImg.setImageBitmap(loadBitmap(it))
                    }
                } else if (requestCode == ADHAR_REQUEST) {
                    actualAdharImage = FileUtil.from(this, data.data!!)?.also {

                        civAdharImg.setImageBitmap(loadBitmap(it))
                    }
                } else {
                    actualAdharImageB = FileUtil.from(this, data.data!!)?.also {

                        civAdharImgB.setImageBitmap(loadBitmap(it))
                    }
                }

            } catch (e: IOException) {
                Toast.makeText(this, "failed to read", Toast.LENGTH_LONG).show()
                e.printStackTrace()
            }
        }
    }

    private fun setGenderList() {
        val Choice =
            arrayOf<CharSequence>("Male", "Female", "Other")

        val builder =
            android.app.AlertDialog.Builder(this@MainActivity)
        builder.setTitle("Select Gender")
        builder.setItems(Choice) { dialog, which -> // the user clicked on colors[which]
            if (which == 0) {
                etGender.setText("Male")
            } else if (which == 1) {
                etGender.setText("Female")
            } else if (which == 2) {
                etGender.setText("Other")
            }
        }
        builder.show()
    }


    private fun getBirthDate() {
        val now = Calendar.getInstance()
        datePickerdialog = DatePickerDialog(
            this@MainActivity, DatePickerDialog.OnDateSetListener { view, year, month, dayOfMonth ->
                val selectedDate = Calendar.getInstance()
                selectedDate.set(Calendar.YEAR, year)
                selectedDate.set(Calendar.MONTH, month)
                selectedDate.set(Calendar.DAY_OF_MONTH, dayOfMonth)
                val date = formate.format(selectedDate.time)
//                Toast.makeText(applicationContext, "date : " + date, Toast.LENGTH_SHORT).show()
                etBirthDate.setText(date)
            },
            now.get(Calendar.YEAR), now.get(Calendar.MONTH), now.get(Calendar.DAY_OF_MONTH)
        )
        datePickerdialog.show()
    }

    fun putUserNameData() {

        val url = Configure.BASE_URL + Configure.USER_DETAILS_URL

        val jsonObjRequest: StringRequest = object : StringRequest(
            Method.PUT,
            url,
            object : Response.Listener<String?> {
                override fun onResponse(response: String?) {
                    Log.d("jukjbkj", response.toString())

                    val gson = Gson()

                    val userArray: UserDetailsModel =
                        gson.fromJson(response, UserDetailsModel::class.java)

                    val userid = userArray.pk

                    Utils.writeStringToPreferences(
                        Configure.USER_ID_KEY,
                        userid.toString(),
                        this@MainActivity
                    )

                    uploadImage(userid)

                }
            }, object : Response.ErrorListener {
                override fun onErrorResponse(error: VolleyError) {
                    VolleyLog.d("volley", "Error: " + error.message)
                    error.printStackTrace()
                    Log.e("jukjbkj", "Error: " + error.message)
                    progressDialog.dismiss()
                    Toast.makeText(
                        this@MainActivity, "Something Went Wrong ! Please try after some time",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }) {


            override fun getHeaders(): MutableMap<String, String> {

                Log.d("jukjbkj", LOGIN_TOKEN.toString())

                var params = java.util.HashMap<String, String>()
//                params.put("Content-Type", "application/json; charset=UTF-8");
                params.put("Authorization", "Token " + LOGIN_TOKEN!!);
                return params;
            }

            @Throws(AuthFailureError::class)
            override fun getParams(): Map<String, String> {
                val params: MutableMap<String, String> =
                    HashMap()
                params["first_name"] = fn
                params["last_name"] = ln
                params["username"] = un

                return params
            }
        }

        Utils.setVolleyRetryPolicy(jsonObjRequest)
        request!!.add(jsonObjRequest)

    }


    fun uploadImage(userid: Int) {

        val url = Configure.BASE_URL + Configure.UPDATE_USER_DETAILS
        Log.e("proceddod", "enterUpload")

        val multipartRequest: VolleyMultipartRequest = object : VolleyMultipartRequest(
            Method.POST,
            url,
            Response.Listener<NetworkResponse> { response ->

                val resultResponse = String(response.data)
                Log.i("ResponceisData", resultResponse.toString())


                try {
                    val result = JSONObject(resultResponse)
                    val ID = result.getString("id")
                    Log.i("Responceis", ID.toString())
                    Utils.writeStringToPreferences(Configure.USER_UPDATE_ID, ID.toString(), this)

                    addUserToFirebase()

                } catch (e: JSONException) {
                    e.printStackTrace()
                }
            },
            Response.ErrorListener { error ->
                progressDialog.dismiss()
                VolleyLog.d("volley", "Error: " + error.message)
                error.printStackTrace()
                Log.e("jukjbkj", "Error: " + error.message)
                Toast.makeText(this, "Something Went Wrong", Toast.LENGTH_LONG).show()

            },

            VolleyProgressListener { }) {
            override fun getHeaders(): Map<String, String>? {
                var params = java.util.HashMap<String, String>()
//                params.put("Content-Type", "application/json; charset=UTF-8");
                params.put("Authorization", "Token " + LOGIN_TOKEN!!);
                return params;
            }

            override fun getParams(): Map<String, String> {
                val params = HashMap<String, String>()
                params.put("user", userid.toString())
                params.put("mobile", etMobile.text.toString())
                params.put("mobile_status", "false")
                params.put("bio", "")
                params.put("birthdate", etBirthDate.text.toString())

                var gender = 1
                if (etGender.text.toString() == "Male") {
                    gender = 1
                } else if (etGender.text.toString() == "Female") {
                    gender = 2
                } else if (etGender.text.toString() == "Other") {
                    gender = 3
                }
                params.put("gender", gender.toString())
                return params
            }

            override fun getByteData(): Map<String, DataPart>? {
                val params: MutableMap<String, DataPart> =
                    java.util.HashMap()
                val mimeType =
                    URLConnection.guessContentTypeFromName(actualProfImage!!.name)
                val mimeTypeAdhar =
                    URLConnection.guessContentTypeFromName(actualAdharImage!!.name)
                val mimeTypeAdharB =
                    URLConnection.guessContentTypeFromName(actualAdharImageB!!.name)
                params["profile_image"] =
                    DataPart(actualProfImage!!.name, Utils.fileToBytes(actualProfImage), mimeType)
                params["adhar_image_f"] = DataPart(
                    actualAdharImage!!.name,
                    Utils.fileToBytes(actualAdharImage),
                    mimeTypeAdhar
                )
                params["adhar_image_b"] = DataPart(
                    actualAdharImageB!!.name,
                    Utils.fileToBytes(actualAdharImageB),
                    mimeTypeAdharB
                )
//                params["adhar_image"] = DataPart(null,null)
                return params
            }

            override fun parseNetworkError(volleyError: VolleyError): VolleyError? {
                try {
                    Log.e("VOL ERR", volleyError.toString())
                } catch (ex: java.lang.Exception) {
                }
                progressDialog.dismiss()
                return super.parseNetworkError(volleyError)
            }
        }
        Utils.setVolleyRetryPolicy(multipartRequest)
        VolleySingleton.getInstance(this).addToRequestQueue(multipartRequest, "POST_COMMENTS")
    }

    private fun addUserToFirebase() {
        val filename = UUID.randomUUID().toString()
        val ref = FirebaseStorage.getInstance().getReference("/images/$filename")

        ref.putFile(selectedPhotoUri!!)
            .addOnSuccessListener {
                Log.d("ImageUpload", "Successfully uploaded image: ${it.metadata?.path}")

                ref.downloadUrl.addOnSuccessListener {
                    Log.d("ImageUpload", "File Location: $it")

                    saveUserToFirebaseDatabase(it.toString())
                }
            }
            .addOnFailureListener {
                Log.d("ImageUpload", "Failed to upload image to storage: ${it.message}")
                progressDialog.dismiss()
            }
    }



    private fun saveUserToFirebaseDatabase(profileImageUrl: String) {
        USER_ID_KEY = Utils.getStringFromPreferences(Configure.USER_ID_KEY,"",this)!!

        val ref = FirebaseDatabase.getInstance().getReference("/users/$USER_ID_KEY")

        val user = User(USER_ID_KEY, fn, profileImageUrl)

        ref.setValue(user)
            .addOnSuccessListener {
                Log.d("ImageUpload", "Finally we saved the user to Firebase Database")

                Toast.makeText(
                    this@MainActivity, "Successfully Registered",
                    Toast.LENGTH_LONG
                ).show()

                loadScreens()
                showConvidPoster()
                progressDialog.dismiss()

            }
            .addOnFailureListener {
                Log.d("ImageUpload", "Failed to set value to database: ${it.message}")
                progressDialog.dismiss()
            }
    }




    fun getUserNameData() {

        val url = Configure.BASE_URL + Configure.USER_DETAILS_URL

        val jsonObjRequest: StringRequest = object : StringRequest(
            Method.GET,
            url,
            object : Response.Listener<String?> {
                override fun onResponse(response: String?) {
                    Log.d("jukjbkj", response.toString())

                    val gson = Gson()

                    val userArray: UserDetailsModel =
                        gson.fromJson(response, UserDetailsModel::class.java)

                    val userid = userArray.pk

                    Utils.writeStringToPreferences(
                        Configure.USER_ID_KEY,
                        userid.toString(),
                        this@MainActivity
                    )

                    getData(userid)

                }
            }, object : Response.ErrorListener {
                override fun onErrorResponse(error: VolleyError) {
                    VolleyLog.d("volley", "Error: " + error.message)
                    error.printStackTrace()
                    Log.e("Responceis", "Error: " + error.message)

                    Toast.makeText(
                        this@MainActivity, "Something Went Wrong ! Please try after some time",
                        Toast.LENGTH_LONG
                    ).show()

                    progressDialog.dismiss()
                }
            }) {


            override fun getHeaders(): MutableMap<String, String> {

                Log.d("jukjbkj", LOGIN_TOKEN.toString())

                var params = java.util.HashMap<String, String>()
                params.put("Content-Type", "application/json; charset=UTF-8");
                params.put("Authorization", "Token " + LOGIN_TOKEN!!);
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

    private fun getData(userid: Int) {
        val url = Configure.BASE_URL + Configure.UPDATE_USER_DETAILS +"?user=${userid}"

        val jsonObjRequest: StringRequest = object : StringRequest(
            Method.GET,
            url,
            object : Response.Listener<String?> {
                override fun onResponse(response: String?) {
                    Log.d("jukjbkjdb", response.toString())

                    val gson = Gson()

                    val userArray: List<FetchProfileData> = gson.fromJson(response , Array<FetchProfileData>::class.java).toList()

                    Utils.writeStringToPreferences(Configure.USER_UPDATE_ID, userArray.get(0)!!.id.toString(), this@MainActivity)

                    Toast.makeText(
                        applicationContext, "Successfully Logged In...",
                        Toast.LENGTH_LONG
                    ).show()
                    loadScreens()
                    showConvidPoster()
                    progressDialog.dismiss()
                }
            }, object : Response.ErrorListener {
                override fun onErrorResponse(error: VolleyError) {
                    VolleyLog.d("volley", "Error: " + error.message)
                    error.printStackTrace()
                    Log.e("Responceis", "Error: " + error.message)

                    Toast.makeText(
                        this@MainActivity, "Something Went Wrong ! Please try after some time",
                        Toast.LENGTH_LONG
                    ).show()

                    progressDialog.dismiss()
                }
            }) {


            override fun getHeaders(): MutableMap<String, String> {

                Log.d("jukjbkj", LOGIN_TOKEN.toString())

                var params = java.util.HashMap<String, String>()
                params.put("Content-Type", "application/json; charset=UTF-8");
                params.put("Authorization", "Token " + LOGIN_TOKEN!!);
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

    private fun checkFieldsLogin(value: String, lazyMessage: String) {
        if (value.isEmpty()) {
            etEmail.error = "Enter Valid Email Address"
        } else if (!validEmail(value)) {
            Toast.makeText(this, "Enter valid e-mail!", Toast.LENGTH_LONG).show()
            progressDialog.dismiss()
        } else if (lazyMessage.isEmpty()) {
            etPass.error = "Enter Valid Password"
        } else {
            progressDialog = ProgressDialog(this@MainActivity)
            progressDialog.setMessage("Wait a Sec....Loging In")
            progressDialog.setCancelable(false)
            progressDialog.show()

            hitLoginAPI()
        }
    }


    private fun validEmail(email: String): Boolean {
        val pattern: Pattern = Patterns.EMAIL_ADDRESS
        return pattern.matcher(email).matches()
    }

    private fun checkFieldsSignUp() {

        if (etEmail.text.toString().isEmpty()) {
            etEmail.error = "Enter Valid Email Address"
            progressDialog.dismiss()
        } else if (!validEmail(etEmail.text.toString())) {
            Toast.makeText(this, "Enter valid e-mail!", Toast.LENGTH_LONG).show()
            progressDialog.dismiss()
        } else if (etGender.text.toString().isEmpty()) {
            etGender.error = "Please Select Gender"
            progressDialog.dismiss()
        } else if (etMobile.text.toString().isEmpty()) {
            etMobile.error = "Please enter valid Mobile Number"
            progressDialog.dismiss()
        } else if (etMobile.text.toString().length != 10) {
            etMobile.error = "Please enter valid Mobile Number"
            progressDialog.dismiss()
        } else if (etPass.text.toString().isEmpty()) {
            etPass.error = "Enter Valid Password"
            progressDialog.dismiss()
        } else if (etPass.text.toString().length <= 8) {
            etPass.error = "Password Length must be greater the 8 "
            progressDialog.dismiss()
        } else if (actualProfImage == null) {
            Toast.makeText(this, "Please select valid Profile Picture.", Toast.LENGTH_LONG).show()
            progressDialog.dismiss()
        } else if (actualAdharImage == null) {
            Toast.makeText(this, "Please select valid Adhar Front Picture.", Toast.LENGTH_LONG)
                .show()
            progressDialog.dismiss()
        } else if (actualAdharImageB == null) {
            Toast.makeText(this, "Please select valid Adhar Back Picture.", Toast.LENGTH_LONG)
                .show()
            progressDialog.dismiss()
        } else if (etConPass.text.toString().isEmpty()) {
            etConPass.error = "Enter Valid Password"
            progressDialog.dismiss()
        } else if (etFN.text.toString().isEmpty()) {
            etFN.error = "Enter Valid First Name"
            progressDialog.dismiss()
        } else if (etLN.text.toString().isEmpty()) {
            etLN.error = "Enter Valid Last Name"
            progressDialog.dismiss()
        } else if (etUserName.text.toString().isEmpty()) {
            etUserName.error = "Enter Valid UserName"
            progressDialog.dismiss()
        } else if (!isValidPassword(etPass.text.toString())) {
            etPass.error = "Password must be combination of Numbers and Alphabets"
            progressDialog.dismiss()
        } else if (etPass.text.toString() != etConPass.text.toString()) {
            Toast.makeText(
                this,
                "Password and Confirm Password should be same...",
                Toast.LENGTH_LONG
            ).show()

            progressDialog.dismiss()
        } else {

            fn = etFN.text.toString()
            ln = etLN.text.toString()
            un = etUserName.text.toString()
            compressImage()

        }
    }


    private fun compressImage() {

        actualProfImage?.let { imageFile ->
            lifecycleScope.launch {
                // Default compression

                actualAdharImage?.let { imageAdharFile ->
                    lifecycleScope.launch {

                        actualAdharImageB?.let { imageAdharFileB ->
                            lifecycleScope.launch {


                                compressedProfImage =
                                    Compressor.compress(this@MainActivity, imageFile) {
                                        size(100_000)
                                        resolution(600, 600)
                                        quality(60)
                                        format(Bitmap.CompressFormat.JPEG)
                                    }
                                compressedAdharImage =
                                    Compressor.compress(this@MainActivity, imageAdharFile) {
                                        size(100_000)
                                        resolution(600, 600)
                                        quality(60)
                                        format(Bitmap.CompressFormat.JPEG)

                                    }

                                compressedAdharImageB =
                                    Compressor.compress(this@MainActivity, imageAdharFileB) {
                                        size(100_000)
                                        resolution(600, 600)
                                        quality(60)
                                        format(Bitmap.CompressFormat.JPEG)

                                    }
                                setCompressedImage()

                            }
                        } ?: Log.d("receiveddata", "Please Choose an Image")
//                progressDialog.dismiss()
                    }
                } ?: Log.d("receiveddata", "Please Choose an Image")
            }
        } ?: Log.d("receiveddata", "Please Choose an Image")
//        progressDialog.dismiss()
    }


    private fun setCompressedImage() {
        compressedProfImage?.let {
            civProfImg.setImageBitmap(BitmapFactory.decodeFile(it.absolutePath))
            val uri = Uri.fromFile(it)
            profPicURL = uri.toString()

            Log.d("Compressor", "CompressedProf image size in " + getReadableFileSize(it.length()))

            compressedAdharImage?.let {
                civAdharImg.setImageBitmap(BitmapFactory.decodeFile(it.absolutePath))
                val uri = Uri.fromFile(it)
                adharPicURL = uri.toString()

                Log.d(
                    "Compressor",
                    "CompressedAdhar image size in " + getReadableFileSize(it.length())
                )
                compressedAdharImageB?.let {
                    civAdharImgB.setImageBitmap(BitmapFactory.decodeFile(it.absolutePath))
                    val uri = Uri.fromFile(it)
                    adharPicURLB = uri.toString()

                    Log.d(
                        "Compressor",
                        "CompressedAdhar image size in " + getReadableFileSize(it.length())
                    )

                    hitSignUpAPI()

                    Log.d("Compressor", "Compressed image save in adhar " + it.path)
                } ?: Log.d("Compressor", "Compressed image save in " + it.path)


                Log.d("Compressor", "Compressed image save in adhar " + it.path)
            } ?: Log.d("Compressor", "Compressed image save in " + it.path)
        } ?: Toast.makeText(this@MainActivity, "File not Found ", Toast.LENGTH_LONG).show()

    }


    private fun getReadableFileSize(size: Long): String {
        if (size <= 0) {
            return "0"
        }
        val units = arrayOf("B", "KB", "MB", "GB", "TB")
        val digitGroups = (log10(size.toDouble()) / log10(1024.0)).toInt()
        return DecimalFormat("#,##0.#").format(size / 1024.0.pow(digitGroups.toDouble())) + " " + units[digitGroups]
    }

    private fun hitLoginAPI() {

        var url = Configure.BASE_URL + Configure.LOGIN_URL

        val jsonObjRequest: StringRequest = object : StringRequest(
            Method.POST,
            url,
            object : Response.Listener<String?> {
                override fun onResponse(response: String?) {

                    Log.i("Responceiskey", response.toString())

                    val obj = JSONObject(response)
                    val key = obj.get("key")

                    Log.i("Responceiskey", key.toString())

                    Utils.writeStringToPreferences(LOGIN_KEY, key.toString(), this@MainActivity)
                    LOGIN_TOKEN = Utils.getStringFromPreferences(LOGIN_KEY, "", this@MainActivity)!!


                    getUserNameData()

                }
            }, object : Response.ErrorListener {
                override fun onErrorResponse(error: VolleyError) {
                    VolleyLog.d("volley", "Error: " + error.message)
                    error.printStackTrace()
                    Log.e(
                        "loginerror",
                        "Error: " + error.message + "Error: " + error.networkResponse.statusCode
                    )
                    progressDialog.dismiss()

                    if (error.networkResponse.statusCode == 400) {
                        Toast.makeText(
                            applicationContext,
                            "Username or Mail ID Not Registered Yet. Please Create One..",
                            Toast.LENGTH_LONG
                        ).show()
                    } else {
                        Toast.makeText(
                            applicationContext, "Something Went Wrong ! Please try after some time",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }
            }) {
            override fun getBodyContentType(): String {
                return "application/x-www-form-urlencoded; charset=UTF-8"
            }

            @Throws(AuthFailureError::class)
            override fun getParams(): Map<String, String> {
                val params: MutableMap<String, String> =
                    HashMap()
                params["email"] = emailtxtlogin
                params["password"] = passtxtlogin
                return params
            }
        }

        request!!.add(jsonObjRequest)
//            request!!.start()

//            AppController.getInstance().addToRequestQueue(jsonObjRequest)

    }


    private fun hitSignUpAPI() {

        progressDialog.setCancelable(false)
        progressDialog.show()

        var url = Configure.BASE_URL + Configure.REGISTRATION_URL

        Log.d("Responceis", url)

        val jsonObjRequest: StringRequest = object : StringRequest(
            Method.POST,
            url,
            object : Response.Listener<String?> {
                override fun onResponse(response: String?) {

                    Log.i("Responceis", response.toString())

                    val obj = JSONObject(response)
                    val key = obj.get("key")

                    Log.i("Responceis", key.toString())

                    Utils.writeStringToPreferences(LOGIN_KEY, key.toString(), this@MainActivity)
                    LOGIN_TOKEN = Utils.getStringFromPreferences(LOGIN_KEY, "", this@MainActivity)!!

                    putUserNameData()

                }
            }, object : Response.ErrorListener {
                override fun onErrorResponse(error: VolleyError) {
                    VolleyLog.d("volley", "Error: " + error.message)
                    error.printStackTrace()
                    if (error.networkResponse.statusCode == 400) {
                        Toast.makeText(
                            applicationContext,
                            "A user is already registered with this e-mail address",
                            Toast.LENGTH_LONG
                        ).show()
                    } else {
                        Toast.makeText(
                            applicationContext, "Something Went Wrong ! Please try after some time",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                    progressDialog.dismiss()

                }
            }) {
            override fun getBodyContentType(): String {
                return "application/x-www-form-urlencoded; charset=UTF-8"
            }

            @Throws(AuthFailureError::class)
            override fun getParams(): Map<String, String> {
                val params: MutableMap<String, String> =
                    HashMap()
                params["email"] = etEmail.text.toString()
                params["password1"] = etPass.text.toString()
                params["password2"] = etConPass.text.toString()

                return params
            }
        }
        jsonObjRequest.setRetryPolicy(
            DefaultRetryPolicy(
                5000,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT
            )
        )
        request!!.add(jsonObjRequest)

    }


    fun isValidPassword(password: String?): Boolean {
        password?.let {
            val passwordPattern = "^(?=.*[0-9])(?=.*[a-z]).{4,}$"
            val passwordMatcher = Regex(passwordPattern)

            return passwordMatcher.find(password) != null
        } ?: return false
    }

    private fun loadScreens() {
        changeIconColor(ivHome, tvHome, "Home")
        if (LOGIN_TOKEN != null && LOGIN_TOKEN != "") {
            llNav.visibility = View.VISIBLE
            loadHomeFrag(fragHome = HomeScreen())
        } else {
            askGalleryPermissionLocation()
            llLogin.visibility = View.VISIBLE
            nsvSignUp.visibility = View.GONE
            frame.visibility = View.GONE
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

                AlertDialog.Builder(this@MainActivity)
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


    fun loadSearchFrag(fragHome : SearchFragment) {

        llLogin.visibility = View.GONE
        nsvSignUp.visibility = View.GONE
        frame.visibility = View.VISIBLE

        val fm = supportFragmentManager.beginTransaction()

        fm.setCustomAnimations(android.R.anim.fade_in,
            android.R.anim.fade_out)

        changeIconColor(ivSearch,tvSearch,"Search")

        fm.replace(R.id.frame,fragHome)
        fm.commit()
    }


    fun loadOfferFrag(fragHome : OfferScreen) {

        llLogin.visibility = View.GONE
        nsvSignUp.visibility = View.GONE
        frame.visibility = View.VISIBLE

        val fm = supportFragmentManager.beginTransaction()

        fm.setCustomAnimations(android.R.anim.fade_in,
            android.R.anim.fade_out)


        fm.replace(R.id.frame,fragHome)
        fm.commit()
    }



    fun loadHomeFrag(fragHome : HomeScreen) {

        llLogin.visibility = View.GONE
        nsvSignUp.visibility = View.GONE
        frame.visibility = View.VISIBLE
        val fm = supportFragmentManager.beginTransaction()
        fm.setCustomAnimations(android.R.anim.fade_in,
            android.R.anim.fade_out)
        fm.replace(R.id.frame,fragHome)
        fm.commit()
    }


    fun loadProfileFrag(fragHome : ProfileScreen) {

        llLogin.visibility = View.GONE
        nsvSignUp.visibility = View.GONE
        frame.visibility = View.VISIBLE

        val fm = supportFragmentManager.beginTransaction()

        fm.setCustomAnimations(android.R.anim.fade_in,
            android.R.anim.fade_out)

        fm.replace(R.id.frame,fragHome)
        fm.commit()
    }

    fun loadInboxFrag(fragHome : InboxScreen) {

        llLogin.visibility = View.GONE
        nsvSignUp.visibility = View.GONE
        frame.visibility = View.VISIBLE

        val fm = supportFragmentManager.beginTransaction()

        fm.setCustomAnimations(android.R.anim.fade_in,
            android.R.anim.fade_out)

        changeIconColor(ivInbox,tvInbox,"Inbox")

        fm.replace(R.id.frame,fragHome)
        fm.commit()
    }


    private fun changeIconColor(
        image: ImageView?,
        text: TextView?,
        s: String
    ) {
        image!!.setColorFilter(resources.getColor(R.color.colorPrimary))
        text!!.setTextColor(resources.getColor(R.color.colorPrimary))

        when(s) {
            "Search" -> {
                ivHome!!.setColorFilter(resources.getColor(R.color.bottomNavText))
                tvHome!!.setTextColor(resources.getColor(R.color.bottomNavText))
                ivOffer!!.setColorFilter(resources.getColor(R.color.bottomNavText))
                tvOffer!!.setTextColor(resources.getColor(R.color.bottomNavText))
                ivInbox!!.setColorFilter(resources.getColor(R.color.bottomNavText))
                tvInbox!!.setTextColor(resources.getColor(R.color.bottomNavText))
                ivProfile!!.setColorFilter(resources.getColor(R.color.bottomNavText))
                tvProfile!!.setTextColor(resources.getColor(R.color.bottomNavText))
            }

            "Home" -> {
                ivSearch!!.setColorFilter(resources.getColor(R.color.bottomNavText))
                tvSearch!!.setTextColor(resources.getColor(R.color.bottomNavText))
                ivOffer!!.setColorFilter(resources.getColor(R.color.bottomNavText))
                tvOffer!!.setTextColor(resources.getColor(R.color.bottomNavText))
                ivInbox!!.setColorFilter(resources.getColor(R.color.bottomNavText))
                tvInbox!!.setTextColor(resources.getColor(R.color.bottomNavText))
                ivProfile!!.setColorFilter(resources.getColor(R.color.bottomNavText))
                tvProfile!!.setTextColor(resources.getColor(R.color.bottomNavText))
            }

            "Offer" -> {
                ivHome!!.setColorFilter(resources.getColor(R.color.bottomNavText))
                tvHome!!.setTextColor(resources.getColor(R.color.bottomNavText))
                ivSearch!!.setColorFilter(resources.getColor(R.color.bottomNavText))
                tvSearch!!.setTextColor(resources.getColor(R.color.bottomNavText))
                ivInbox!!.setColorFilter(resources.getColor(R.color.bottomNavText))
                tvInbox!!.setTextColor(resources.getColor(R.color.bottomNavText))
                ivProfile!!.setColorFilter(resources.getColor(R.color.bottomNavText))
                tvProfile!!.setTextColor(resources.getColor(R.color.bottomNavText))
            }

            "Inbox" -> {
                ivHome!!.setColorFilter(resources.getColor(R.color.bottomNavText))
                tvHome!!.setTextColor(resources.getColor(R.color.bottomNavText))
                ivSearch!!.setColorFilter(resources.getColor(R.color.bottomNavText))
                tvSearch!!.setTextColor(resources.getColor(R.color.bottomNavText))
                ivOffer!!.setColorFilter(resources.getColor(R.color.bottomNavText))
                tvOffer!!.setTextColor(resources.getColor(R.color.bottomNavText))
                ivProfile!!.setColorFilter(resources.getColor(R.color.bottomNavText))
                tvProfile!!.setTextColor(resources.getColor(R.color.bottomNavText))
            }

            "Profile" -> {
                ivHome!!.setColorFilter(resources.getColor(R.color.bottomNavText))
                tvHome!!.setTextColor(resources.getColor(R.color.bottomNavText))
                ivSearch!!.setColorFilter(resources.getColor(R.color.bottomNavText))
                tvSearch!!.setTextColor(resources.getColor(R.color.bottomNavText))
                ivOffer!!.setColorFilter(resources.getColor(R.color.bottomNavText))
                tvOffer!!.setTextColor(resources.getColor(R.color.bottomNavText))
                ivInbox!!.setColorFilter(resources.getColor(R.color.bottomNavText))
                tvInbox!!.setTextColor(resources.getColor(R.color.bottomNavText))
            }
        }


    }
}
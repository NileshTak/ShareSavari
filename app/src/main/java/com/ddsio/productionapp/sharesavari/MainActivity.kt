package com.ddsio.productionapp.sharesavari

import android.Manifest
import android.app.Activity
import android.app.DatePickerDialog
import android.app.ProgressDialog
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityOptionsCompat
import androidx.core.widget.NestedScrollView
import com.android.volley.*
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.ddsio.productionapp.sharesavari.CommonUtils.ImagePickerActivity
import com.ddsio.productionapp.sharesavari.CommonUtils.Utils
import com.ddsio.productionapp.sharesavari.HomeScreen.HomeScreen
import com.ddsio.productionapp.sharesavari.InboxScreen.InboxScreen
import com.ddsio.productionapp.sharesavari.OfferScreen.OfferScreen
import com.ddsio.productionapp.sharesavari.ProfileScreen.ProfileScreen
import com.ddsio.productionapp.sharesavari.SearchScreen.SearchFragment
import com.ddsio.productionapp.sharesavari.OfferScreen.ShowMapActivityPickUp
import com.github.florent37.runtimepermission.kotlin.askPermission
import com.google.gson.Gson
import com.productionapp.amhimemekar.CommonUtils.Configure
import com.productionapp.amhimemekar.CommonUtils.Configure.LOGIN_KEY
import com.productionapp.amhimemekar.CommonUtils.UserDetailsModel
import io.github.inflationx.viewpump.ViewPumpContextWrapper
import kotlinx.android.synthetic.main.activity_main.*
import org.json.JSONObject
import java.io.*
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.HashMap

class MainActivity : AppCompatActivity() {

    val REQUEST_IMAGE = 100

    lateinit var frameContainer : FrameLayout
    lateinit var llSearch : LinearLayout
    lateinit var llLogin : LinearLayout
    lateinit var tvSignUp : TextView
    lateinit var tvLogin : TextView
    var imageUri = ""
    var type  = ""
    lateinit var nsvSignUp : NestedScrollView

    lateinit var bitmap : Bitmap


    lateinit var emailtxtlogin : String
    lateinit var passtxtlogin : String


    var request: RequestQueue? = null
    lateinit var progressDialog: ProgressDialog
    lateinit var datePickerdialog: DatePickerDialog

    var LOGIN_TOKEN = ""

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

        request= Volley.newRequestQueue(this);

        tvSignUp = findViewById<TextView>(R.id.tvSignUp)
        nsvSignUp = findViewById<NestedScrollView>(R.id.nsvSignUp)
        tvLogin = findViewById<TextView>(R.id.tvLogin)
        frameContainer = findViewById<FrameLayout>(R.id.frame)
        llSearch = findViewById<LinearLayout>(R.id.llSearch)
        llLogin = findViewById<LinearLayout>(R.id.llLogin)

//        Utils.writeStringToPreferences(LOGIN_KEY, "",this)

        LOGIN_TOKEN = Utils.getStringFromPreferences(LOGIN_KEY,"",this)!!


        Utils.checkConnection(this@MainActivity,frameContainer)
        askGalleryPermissionLocation()
        loadScreens()

        if (type == "SignUp") {
            nsvSignUp.visibility = View.VISIBLE
            llLogin.visibility = View.GONE
            frame.visibility = View.GONE
        } else if (type == "LogIn") {
            llLogin.visibility = View.VISIBLE
            nsvSignUp.visibility = View.GONE
            frame.visibility = View.GONE
        } else {
            loadScreens()
        }

        cvLogin.setOnClickListener {

            emailtxtlogin = loginetEmail.text.toString()
            passtxtlogin = loginetPass.text.toString()

            checkFieldsLogin(emailtxtlogin,passtxtlogin)

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

            progressDialog = ProgressDialog(this)
            progressDialog.setMessage("Wait a Sec....Loging In")
            progressDialog.setCancelable(false)
            progressDialog.show()
            checkFieldsSignUp()

        }

        llSearch.setOnClickListener {
            loadSearchFrag(fragHome = SearchFragment())
        }

        llOffer.setOnClickListener {
//            changeIconColor(ivOffer,tvOffer,"Offer")
//            if (LOGIN_TOKEN != null && LOGIN_TOKEN != "") {
//                loadOfferFrag(fragHome = OfferScreen())
//            }else {
//                llLogin.visibility = View.VISIBLE
//                nsvSignUp.visibility = View.GONE
//                frame.visibility = View.GONE
//            }

            var int = Intent(this,
                ShowMapActivityPickUp::class.java)
            val bundle =
                ActivityOptionsCompat.makeCustomAnimation(
                    this@MainActivity,
                    R.anim.fade_in,
                    R.anim.fade_out
                ).toBundle()
            int.putExtra("screen","Offer")
            startActivity(int,bundle)
        }

        llHome.setOnClickListener {
            changeIconColor(ivHome,tvHome,"Home")
            if (LOGIN_TOKEN != null && LOGIN_TOKEN != "") {
                loadHomeFrag(fragHome = HomeScreen())
            }else {
                llLogin.visibility = View.VISIBLE
                nsvSignUp.visibility = View.GONE
                frame.visibility = View.GONE
            }

        }

        llInbox.setOnClickListener {
            loadInboxFrag(fragHome = InboxScreen())
        }


        civProfImg.setOnClickListener {
            askCameraPermission()
        }

        llProfile.setOnClickListener {
            changeIconColor(ivProfile,tvProfile,"Profile")
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


    private fun askCameraPermission() {
        askPermission(
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_EXTERNAL_STORAGE
        ) {
//            selectPhoto()
//            showImagePickerOptions()

            launchGalleryIntent()
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


    private fun showImagePickerOptions() {
        ImagePickerActivity.showImagePickerOptions(this!! , object :
            ImagePickerActivity.PickerOptionListener {
            override fun onTakeCameraSelected() {
                launchCameraIntent()
            }

            override fun onChooseGallerySelected() {
                launchGalleryIntent()
            }
        })
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_IMAGE) {
            if (resultCode == Activity.RESULT_OK) {
                val uri = data!!.getParcelableExtra<Uri>("path")
                Log.d("receiveddata",uri.toString())
                try { // You can update this bitmap to your server
                      bitmap =
                        MediaStore.Images.Media.getBitmap(this!!.getContentResolver(), uri)

                    civProfImg.setImageBitmap(bitmap)

                    imageUri = getImageUriFromBitmap(this,bitmap).toString()

                } catch (e: IOException) {
                    e.printStackTrace()
                    Log.d("receiveddata",e.toString())
                }
            }
        }
    }

    fun getImageUriFromBitmap(context: Context, bitmap: Bitmap): Uri{
        val bytes = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, bytes)
        val path = MediaStore.Images.Media.insertImage(context.contentResolver, bitmap, "Title", null)
        return Uri.parse(path.toString())
    }



    private fun launchCameraIntent() {
        val intent = Intent(this, ImagePickerActivity::class.java)
        intent.putExtra(
            ImagePickerActivity.INTENT_IMAGE_PICKER_OPTION,
            ImagePickerActivity.REQUEST_IMAGE_CAPTURE
        )
        // setting aspect ratio
        intent.putExtra(ImagePickerActivity.INTENT_LOCK_ASPECT_RATIO, true)
        intent.putExtra(ImagePickerActivity.INTENT_ASPECT_RATIO_X, 1) // 16x9, 1x1, 3:4, 3:2
        intent.putExtra(ImagePickerActivity.INTENT_ASPECT_RATIO_Y, 1)
        // setting maximum bitmap width and height
        intent.putExtra(ImagePickerActivity.INTENT_SET_BITMAP_MAX_WIDTH_HEIGHT, true)
        intent.putExtra(ImagePickerActivity.INTENT_BITMAP_MAX_WIDTH, 1000)
        intent.putExtra(ImagePickerActivity.INTENT_BITMAP_MAX_HEIGHT, 1000)
        startActivityForResult(intent,  REQUEST_IMAGE)
    }

    private fun launchGalleryIntent() {
        val intent = Intent(this, ImagePickerActivity::class.java)
        intent.putExtra(
            ImagePickerActivity.INTENT_IMAGE_PICKER_OPTION,
            ImagePickerActivity.REQUEST_GALLERY_IMAGE
        )
        // setting aspect ratio
        intent.putExtra(ImagePickerActivity.INTENT_LOCK_ASPECT_RATIO, true)
        intent.putExtra(ImagePickerActivity.INTENT_ASPECT_RATIO_X, 1) // 16x9, 1x1, 3:4, 3:2
        intent.putExtra(ImagePickerActivity.INTENT_ASPECT_RATIO_Y, 1)
        if (intent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(intent, REQUEST_IMAGE)
        }
    }


    private fun setGenderList() {
        val Choice =
            arrayOf<CharSequence>("Male", "Female","Other")

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

    fun putUserNameData( ) {

        val url = Configure.BASE_URL + Configure.USER_DETAILS_URL

        val jsonObjRequest: StringRequest = object : StringRequest(
            Method.PUT,
            url,
            object : Response.Listener<String?> {
                override fun onResponse(response: String?) {
                    Log.d("jukjbkj", response.toString())

                    Toast.makeText(this@MainActivity,"Successfully Registered",
                        Toast.LENGTH_LONG).show()

                    val gson = Gson()

                    val userArray: UserDetailsModel =
                        gson.fromJson(response, UserDetailsModel ::class.java)

                    val userid = userArray.pk

                    Utils.writeStringToPreferences(Configure.USER_ID_KEY,userid.toString(),this@MainActivity)

                    loadScreens()

                    progressDialog.dismiss()


                }
            }, object : Response.ErrorListener {
                override fun onErrorResponse(error: VolleyError) {
                    VolleyLog.d("volley", "Error: " + error.message)
                    error.printStackTrace()
                    Log.e("jukjbkj",  "Error: " + error.message)

                    Toast.makeText(this@MainActivity,"Something Went Wrong ! Please try after some time",
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
                params["first_name"] = etFN.text.toString()
                params["last_name"] = etLN.text.toString()
                params["username"] = etUserName.text.toString()

                return params
            }
        }
        request!!.add(jsonObjRequest)

    }



    fun getUserNameData( ) {

        val url = Configure.BASE_URL + Configure.USER_DETAILS_URL

        val jsonObjRequest: StringRequest = object : StringRequest(
            Method.GET,
            url,
            object : Response.Listener<String?> {
                override fun onResponse(response: String?) {
                    Log.d("jukjbkj", response.toString())

                    val gson = Gson()

                    val userArray: UserDetailsModel =
                        gson.fromJson(response, UserDetailsModel ::class.java)

                    val userid = userArray.pk

                    Utils.writeStringToPreferences(Configure.USER_ID_KEY,userid.toString(),this@MainActivity)

                    Toast.makeText(applicationContext,"Successfully Logged In...",
                        Toast.LENGTH_LONG).show()
                    loadScreens()

                    progressDialog.dismiss()


                }
            }, object : Response.ErrorListener {
                override fun onErrorResponse(error: VolleyError) {
                    VolleyLog.d("volley", "Error: " + error.message)
                    error.printStackTrace()
                    Log.e("Responceis",  "Error: " + error.message)

                    Toast.makeText(this@MainActivity,"Something Went Wrong ! Please try after some time",
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

    private fun checkFieldsLogin(value: String, lazyMessage: String) {
        if(value.isEmpty()) {
            etEmail.error = "Enter Valid Email Address"
        }
        else if(lazyMessage.isEmpty()) {
            etPass.error = "Enter Valid Password"
        } else {
            progressDialog = ProgressDialog(this@MainActivity)
            progressDialog.setMessage("Wait a Sec....Loging In")
            progressDialog.setCancelable(false)
            progressDialog.show()

            hitLoginAPI()
        }
    }




    private fun checkFieldsSignUp() {
        if(etEmail.text.toString().isEmpty()) {
            etEmail.error = "Enter Valid Email Address"
            progressDialog.dismiss()
        }
        else if(etPass.text.toString().isEmpty()) {
            etPass.error = "Enter Valid Password"
            progressDialog.dismiss()
        }
//        else if(imageUri.isEmpty() || imageUri =="") {
//            Toast.makeText(this,"Please select valid Profile Picture.",Toast.LENGTH_LONG).show()
//            progressDialog.dismiss()
//        }
        else if(etConPass.text.toString().isEmpty()) {
            etConPass.error = "Enter Valid Password"
            progressDialog.dismiss()
        } else if(etFN.text.toString().isEmpty()) {
            etFN.error = "Enter Valid First Name"
            progressDialog.dismiss()
        } else if(etLN.text.toString().isEmpty()) {
            etLN.error = "Enter Valid Last Name"
            progressDialog.dismiss()
        } else if(etUserName.text.toString().isEmpty()) {
            etUserName.error = "Enter Valid UserName"
            progressDialog.dismiss()
        } else if(!isValidPassword(etPass.text.toString())) {
            etPass.error = "Password must be combination of Numbers and Alphabets"
            progressDialog.dismiss()
        } else {
            hitSignUpAPI()
        }
    }


    private fun hitLoginAPI() {

        var url = Configure.BASE_URL + Configure.LOGIN_URL

        val jsonObjRequest: StringRequest = object : StringRequest(
            Method.POST,
            url,
            object : Response.Listener<String?> {
                override fun onResponse(response: String?) {

                    Log.i( "Responceiskey", response.toString())

                    val obj = JSONObject(response)
                    val key =obj.get("key")

                    Log.i( "Responceiskey", key.toString())

                    Utils.writeStringToPreferences(LOGIN_KEY, key.toString(),this@MainActivity)
                    LOGIN_TOKEN = Utils.getStringFromPreferences(LOGIN_KEY,"",this@MainActivity)!!


                  getUserNameData()

                }
            }, object : Response.ErrorListener {
                override fun onErrorResponse(error: VolleyError) {
                    VolleyLog.d("volley", "Error: " + error.message)
                    error.printStackTrace()
                    Log.e("loginerror",  "Error: " + error.message+"Error: " + error.networkResponse.statusCode)
                    progressDialog.dismiss()

                    if (error.networkResponse.statusCode == 400) {
                        Toast.makeText(applicationContext,"Username or Mail ID Not Registered Yet. Please Create One..",
                            Toast.LENGTH_LONG).show()
                    } else {
                        Toast.makeText(applicationContext,"Something Went Wrong ! Please try after some time",
                            Toast.LENGTH_LONG).show()
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

        var url =  Configure.BASE_URL + Configure.REGISTRATION_URL

        Log.d("Responceis",url)

        val jsonObjRequest: StringRequest = object : StringRequest(
            Method.POST,
            url,
            object : Response.Listener<String?> {
                override fun onResponse(response: String?) {

                    Log.i( "Responceis", response.toString())

                    val obj = JSONObject(response)
                    val key =obj.get("key")

                    Log.i( "Responceis", key.toString())

                    Utils.writeStringToPreferences(LOGIN_KEY,key.toString(),this@MainActivity)
                    LOGIN_TOKEN = Utils.getStringFromPreferences(LOGIN_KEY,"",this@MainActivity)!!


                    putUserNameData()

                }
            }, object : Response.ErrorListener {
                override fun onErrorResponse(error: VolleyError) {
                    VolleyLog.d("volley", "Error: " + error.message)
                    error.printStackTrace()
//                    if (error.networkResponse.statusCode == 400) {
//                        Toast.makeText(applicationContext,"A user is already registered with this e-mail address",
//                            Toast.LENGTH_LONG).show()
//                    } else {
                        Toast.makeText(applicationContext,"Something Went Wrong ! Please try after some time",
                            Toast.LENGTH_LONG).show()
//                    }
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



    fun isValidPassword(password: String?) : Boolean {
        password?.let {
            val passwordPattern = "^(?=.*[0-9])(?=.*[a-z]).{4,}$"
            val passwordMatcher = Regex(passwordPattern)

            return passwordMatcher.find(password) != null
        } ?: return false
    }

    private fun loadScreens() {
        changeIconColor(ivHome,tvHome,"Home")
        if (LOGIN_TOKEN != null && LOGIN_TOKEN != "") {
            loadHomeFrag(fragHome = HomeScreen())
        }else {
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
package com.ddsio.productionapp.sharesavari.ProfileScreen

import android.Manifest
import android.animation.ValueAnimator
import android.app.ProgressDialog
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.preference.PreferenceManager
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.core.app.ActivityOptionsCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.airbnb.lottie.LottieAnimationView
import com.android.volley.*
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.bumptech.glide.Glide
import com.ddsio.productionapp.sharesavari.CommonUtils.CircularProgress.CircleProgressBar
import com.ddsio.productionapp.sharesavari.CommonUtils.FileUtil
import com.ddsio.productionapp.sharesavari.CommonUtils.Utils
import com.ddsio.productionapp.sharesavari.CommonUtils.VolleyMultipartRequest
import com.ddsio.productionapp.sharesavari.CommonUtils.VolleyMultipartRequest.VolleyProgressListener
import com.ddsio.productionapp.sharesavari.CommonUtils.VolleySingleton
import com.ddsio.productionapp.sharesavari.LogInSignUpQues.LogInSignUpQues
import com.ddsio.productionapp.sharesavari.R
import com.ddsio.productionapp.sharesavari.SearchScreen.child.DriverProfile
import com.github.florent37.runtimepermission.kotlin.askPermission
import com.google.firebase.FirebaseException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthProvider
import com.google.gson.Gson
import com.productionapp.amhimemekar.CommonUtils.BookRidesPojoItem
import com.productionapp.amhimemekar.CommonUtils.Configure
import com.productionapp.amhimemekar.CommonUtils.Configure.BASE_URL
import com.productionapp.amhimemekar.CommonUtils.Configure.GET_USER_DETAILS
import com.productionapp.amhimemekar.CommonUtils.FetchProfileData
import de.hdodenhof.circleimageview.CircleImageView
import id.zelory.compressor.Compressor
import id.zelory.compressor.constraint.format
import id.zelory.compressor.constraint.quality
import id.zelory.compressor.constraint.resolution
import id.zelory.compressor.constraint.size
import id.zelory.compressor.loadBitmap
import kotlinx.android.synthetic.main.activity_authentication.view.*
import kotlinx.android.synthetic.main.fragment_profile_screen.*
import kotlinx.coroutines.launch
import org.json.JSONException
import org.json.JSONObject
import java.io.File
import java.io.IOException
import java.net.URLConnection
import java.util.concurrent.TimeUnit

class ProfileScreen : Fragment() {


    lateinit var dialog_verifying: AlertDialog
    lateinit var dialog_otp: AlertDialog
    lateinit var firebaseAuth: FirebaseAuth

    lateinit var pets : String
    lateinit var smoking : String

    lateinit var fetchProfileData : FetchProfileData
    lateinit var bookRidepOjo : BookRidesPojoItem

    var vari = "False"
    private var mResendToken: PhoneAuthProvider.ForceResendingToken? = null

    lateinit var mCallbacks: PhoneAuthProvider.OnVerificationStateChangedCallbacks
    lateinit var mAuth: FirebaseAuth
    lateinit var cpv : CircleProgressBar
lateinit var cbPetsP : CheckBox
    lateinit var cbSmokingP : CheckBox


    var GALLERY_REQUEST = 1666
    var LOGIN_TOKEN = ""
    lateinit var lottieSelectImage : LottieAnimationView
    lateinit var rlParent : RelativeLayout
    lateinit var ivProf : CircleImageView
    lateinit var progressDialog: ProgressDialog
    lateinit var selectedImageUri : Uri
    lateinit var cvSave : CardView
    var request: RequestQueue? = null
    lateinit var ivLogout : ImageView
    private var compressedImage: File? = null
    lateinit var bitmap: Bitmap



    var USER_UPDATE_ID = ""

    lateinit var btnVerify : Button

      var resultUri : Uri? = null

    lateinit var USER_ID_KEY : String

    var destinationURL: String? = null
    private var mVerificationId: String? = null

    lateinit var tvAlert : ImageView
    lateinit var ivVerified : ImageView
    lateinit var cvBio : CardView
    lateinit var cvPublic : CardView
    var ADHARB_REQUEST = 1777
    private var actualProfImage: File? = null
    lateinit var ivEdit : ImageView
    lateinit var ivEdi : ImageView
    lateinit var ivEditB : ImageView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_profile_screen, container, false)

        LOGIN_TOKEN = Utils.getStringFromPreferences(Configure.LOGIN_KEY,"",activity)!!
        USER_UPDATE_ID = Utils.getStringFromPreferences(Configure.USER_UPDATE_ID,"",activity)!!
        USER_ID_KEY = Utils.getStringFromPreferences(Configure.USER_ID_KEY,"",activity)!!

        mAuth = FirebaseAuth.getInstance()

        request= Volley.newRequestQueue(activity);

        bookRidepOjo = BookRidesPojoItem()

        lottieSelectImage = view.findViewById<LottieAnimationView>(R.id.lottieSelectImage)
        rlParent = view.findViewById<RelativeLayout>(R.id.rlParent)
        ivProf = view.findViewById<CircleImageView>(R.id.ivProf)
        ivLogout = view.findViewById<ImageView>(R.id.ivLogout)
        cvSave = view.findViewById<CardView>(R.id.cvSave)
        tvAlert = view.findViewById<ImageView>(R.id.tvAlert)
        ivVerified = view.findViewById<ImageView>(R.id.ivVerified)
        cvBio = view.findViewById<CardView>(R.id.cvBio)
        cvPublic = view.findViewById<CardView>(R.id.cvPublic)
        ivEdit = view.findViewById<ImageView>(R.id.ivEdit)
        ivEdi = view.findViewById<ImageView>(R.id.ivEdi)
        ivEditB = view.findViewById<ImageView>(R.id.ivEditB)
        btnVerify = view.findViewById<Button>(R.id.btnVerify)
        cbPetsP =view.findViewById<CheckBox>(R.id.cbPetsP)
        cbSmokingP =view.findViewById<CheckBox>(R.id.cbSmokingP)


        ivEdi.setOnClickListener {

            showPopup(ivEdi)
        }


        cvPublic.setOnClickListener {
            var int = Intent(activity!!,
                DriverProfile::class.java)
            val bundle =
                ActivityOptionsCompat.makeCustomAnimation(
                    activity!! ,
                    R.anim.fade_in, R.anim.fade_out
                ).toBundle()
            int.putExtra("pojoWithData",bookRidepOjo)
            int.putExtra("cust",USER_ID_KEY)
            int.putExtra("type","self")
            startActivity(int,bundle)
        }

        tvAlert.setOnClickListener {
            if (vari == "False" ) {
                Toast.makeText(activity,"Number not validated yet...",Toast.LENGTH_LONG).show()
            }
        }

        btnVerify.setOnClickListener {
            sendCode()
        }


        ivEdit.setOnClickListener {
            askCameraPermission(ADHARB_REQUEST)

        }

        ivEditB.setOnClickListener {
            etBio.visibility = View.VISIBLE
            tvBio.visibility = View.GONE
            cvSave.visibility = View.VISIBLE
        }

        ivLogout.setOnClickListener {

            val preferences = PreferenceManager.getDefaultSharedPreferences(activity)

            val editor = preferences.edit()

            preferences.getString(Configure.LOGIN_KEY,"")
            editor.clear()
            editor.commit()

            preferences.getString(Configure.USER_ID_KEY,"")
            editor.clear()
            editor.commit()

            Toast.makeText(activity,"Successfully Logged Out",
                Toast.LENGTH_LONG).show()

            ivLogout.visibility = View.GONE

            val i = Intent(context, LogInSignUpQues::class.java)
            i.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(i)
            activity!!.finish()

        }

        cvSave.setOnClickListener {
            if (actualProfImage != null) {
                compressImage()
            } else {
                progressDialog = ProgressDialog(activity)
                progressDialog.setMessage("Wait a Sec....Uploading Files")
                progressDialog.setCancelable(false)
                progressDialog.show()
                updateData( )
            }

        }

        lottieSelectImage.setOnClickListener {
            askGalleryPermissionCamera()
        }

        getUserData()

        mCallbacks = object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
            override fun onVerificationCompleted(Credential: PhoneAuthCredential) {
                //        Toast.makeText(applicationContext,"Verfication Process",Toast.LENGTH_SHORT).show()
//                val inflater = getLayoutInflater()
//                val alertLayout = inflater.inflate(R.layout.processing_dialog, null)
//                val show = AlertDialog.Builder(activity!!)
//                show.setView(alertLayout)
//                show.setCancelable(false)
//                dialog_verifying = show.create()
//                dialog_verifying.show()
                signInWithPhoneAuthCredential(Credential)
            }

            override fun onVerificationFailed(e: FirebaseException) {
//                Toast.makeText(applicationContext, "Something Went Wrong", Toast.LENGTH_SHORT).show()
                Utils.showSnackMSG(lottieSelectImage,"Please Try After Some Time")

                Log.d("Failure",e.toString())
            }

            override fun onCodeSent(verificationId: String ,
                                    token: PhoneAuthProvider.ForceResendingToken ) {

                // Save verification ID and resending token so we can use them later
                mVerificationId = verificationId
                mResendToken = token

            }
        }


        return view
    }


    fun showPopup(v: View?) {
        val popup = PopupMenu(activity, v)
        //Inflating the Popup using xml file
        popup.menuInflater.inflate(R.menu.file_menu, popup.menu)

        //registering popup with OnMenuItemClickListener
        popup.setOnMenuItemClickListener(PopupMenu.OnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.logout -> {
                    val preferences = PreferenceManager.getDefaultSharedPreferences(activity)

                    val editor = preferences.edit()

                    preferences.getString(Configure.LOGIN_KEY,"")
                    editor.clear()
                    editor.commit()

                    preferences.getString(Configure.USER_UPDATE_ID,"")
                    editor.clear()
                    editor.commit()

                    preferences.getString(Configure.SERACH_LAT_KEY,"")
                    editor.clear()
                    editor.commit()

                    preferences.getString(Configure.SERACH_LOG_KEY,"")
                    editor.clear()
                    editor.commit()

                    preferences.getString(Configure.USER_UPDATE_ID,"")
                    editor.clear()
                    editor.commit()


                    preferences.getString(Configure.PLAYER_ID,"")
                    editor.clear()
                    editor.commit()


                    preferences.getString(Configure.USER_ID_KEY,"")
                    editor.clear()
                    editor.commit()

                    Toast.makeText(activity,"Successfully Logged Out",
                        Toast.LENGTH_LONG).show()

                    activity!!.getSharedPreferences("com.ddsio.productionapp.sharesavari_preferences", 0).edit().clear().commit()


                    val i = Intent(context, LogInSignUpQues::class.java)
                    i.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK
                    startActivity(i)
                    activity!!.finish()

                    return@OnMenuItemClickListener true
                }
                R.id.editProfile -> {
                    val intent = Intent(activity, EditProf::class.java)
                    intent.putExtra("userPojo",fetchProfileData)
                    startActivity(intent)
                }

                R.id.tnc -> {
                    val intent = Intent(
                        Intent.ACTION_VIEW,
                        Uri.parse("http://sharesawaari.com/tandc.html")
                    )
                    startActivity(intent)
                }

                R.id.privacy -> {
                    val intent = Intent(
                        Intent.ACTION_VIEW,
                        Uri.parse("http://sharesawaari.com/pp.html")
                    )
                    startActivity(intent)
                }

                R.id.help -> {
                    val to: String = "support@sharesawaari.com"
                    val sharingIntent = Intent(Intent.ACTION_SEND)
                    sharingIntent.type = "text/plain"
                    sharingIntent.setPackage("com.google.android.gm")
                    val shareBody = ""
                    sharingIntent.putExtra(Intent.EXTRA_EMAIL,arrayOf(to))
                    sharingIntent.putExtra(Intent.EXTRA_SUBJECT, "Help Support")
                    sharingIntent.putExtra(Intent.EXTRA_TEXT, shareBody)
                    startActivity(sharingIntent)
                }
            }
            true
        })
        popup.show()
    }


    private fun askCameraPermission(RequestType: Int) {
        askPermission(
            Manifest.permission.CAMERA,
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
        ) {
            cvSave.visibility = View.VISIBLE
            val intent = Intent(Intent.ACTION_GET_CONTENT)
            intent.type = "image/*"
            startActivityForResult(intent, RequestType)
        }.onDeclined { e ->
            if (e.hasDenied()) {
                //the list of denied permissions
                e.denied.forEach {
                }

                AlertDialog.Builder(activity!!)
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



    private fun signInWithPhoneAuthCredential(credential: PhoneAuthCredential) {
        mAuth.signInWithCredential(credential)
            .addOnCompleteListener( activity!!) { task ->
                if (task.isSuccessful) {
                    // Sign in success, update UI with the signed-in user's information
//                    Log.d(Authentication.TAG, "signInWithCredential:success")

                    sendMobielVerifiedTrueAPI()

                } else {

                    dialog_verifying.dismiss()
                    dialog_otp.dismiss()
                    progressDialog.dismiss()
//                    Toast.makeText(this@Authentication, "Incorrect OTP", Toast.LENGTH_SHORT).show()
                    Utils.showSnackMSG(lottieSelectImage,"Incorrect OTP")

//                    Log.w(Authentication.TAG, "signInWithCredential:failure", task.exception)
                    if (task.exception is FirebaseAuthInvalidCredentialsException) {

                    }
                }
            }
    }

    private fun sendMobielVerifiedTrueAPI() {

        progressDialog = ProgressDialog(activity)
        progressDialog.setMessage("Wait a Sec....Loading Details..")
        progressDialog.setCancelable(false)
        progressDialog.show()


        val url = Configure.BASE_URL + Configure.UPDATE_USER_DETAILS+USER_UPDATE_ID+"/"

        val jsonObjRequest: StringRequest = object : StringRequest(
            Method.PUT,
            url,
            object : Response.Listener<String?> {
                override fun onResponse(response: String?) {
                    Log.d("jukjbkj", response.toString())

//                    val gson = Gson()
//
//                    val userArray: UserDetailsModel =
//                        gson.fromJson(response, UserDetailsModel ::class.java)
                    progressDialog.dismiss()
                    dialog_otp.dismiss()
                    dialog_verifying.dismiss()
                    etBio.visibility = View.GONE
                    tvBio.visibility = View.VISIBLE
                    cvSave.visibility = View.GONE

                    getUserData()


                    Utils.showSnackMSG(lottieSelectImage,"Mobile Number Verified Successfully")

                }
            }, object : Response.ErrorListener {
                override fun onErrorResponse(error: VolleyError) {
                    VolleyLog.d("volley", "Error: " + error.message)
                    error.printStackTrace()
                    Log.e("jukjbkj",  "Error: " + error.message)
                    progressDialog.dismiss()
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
                params.put("bio", fetchProfileData.bio!!)
//                params.put("pets",fetchProfileData.pets)
//                params.put("smoking",fetchProfileData.smoking)
                return params
            }
        }

        Utils.setVolleyRetryPolicy(jsonObjRequest)
        request!!.add(jsonObjRequest)

    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == AppCompatActivity.RESULT_OK) {
            if (data == null) {
                Toast.makeText(activity, "null", Toast.LENGTH_LONG).show()
                return
            }
            try {
                if (requestCode == ADHARB_REQUEST) {
                    actualProfImage = FileUtil.from( activity!!, data.data!!)?.also {
                        ivProf.setImageBitmap(loadBitmap(it))
                }
                }

            } catch (e: IOException) {
                Toast.makeText(activity, "failed to read", Toast.LENGTH_LONG).show()
                e.printStackTrace()
            }
        }
    }


    private fun sendCode()
    {
        val phoneNumber = "+91" +tvMN!!.text.toString()

        createOTPEnterDialog(phoneNumber)

            PhoneAuthProvider.getInstance().verifyPhoneNumber(
                phoneNumber, // Phone number to verify
                60, // Timeout duration
                TimeUnit.SECONDS, // Unit of timeout
              activity!!, // Activity (for callback binding)
                mCallbacks
            )        // OnVerificationStateChangedCallbacks

    }

    private fun createOTPEnterDialog(phoneNumber: String) {
        val inflater = getLayoutInflater()
        val alertLayout = inflater.inflate(R.layout.activity_authentication, null)

        alertLayout.phonenumberText.text = phoneNumber

        alertLayout.cpv.setProgressFormatter { progress, max -> progress.toString() }

        alertLayout.tvDidntGotCode.setOnClickListener {
            dialog_otp.dismiss()
            sendCode()
        }

        setProgress(  alertLayout.cpv,alertLayout.tvDidntGotCode)

        alertLayout.verifyCodeButton!!.setOnClickListener {
            val verificationCode = alertLayout.pinView!!.text!!.toString()
            if (verificationCode.isEmpty()) {
//                Toast.makeText(this@Authentication, "Enter verification code", Toast.LENGTH_SHORT).show()
                Utils.showSnackMSG(alertLayout.parentAuth,"Please Enter Verification Code")

            } else {

                val inflater = getLayoutInflater()
                val alertLayout = inflater.inflate(R.layout.processing_dialog, null)
                val show = AlertDialog.Builder(activity!!)
                show.setView(alertLayout)
                show.setCancelable(false)
                dialog_verifying = show.create()
                dialog_verifying.show()
                val handler = Handler()
                handler.postDelayed({
                    val credential = PhoneAuthProvider.getCredential(this!!.mVerificationId.toString(), verificationCode)
                    signInWithPhoneAuthCredential(credential)
                },3000)

            }
        }

        val showOTP = AlertDialog.Builder(activity!!)
        showOTP.setView(alertLayout)
        showOTP.setCancelable(false)
        dialog_otp = showOTP.create()
        dialog_otp.show()

        alertLayout.ivClose.setOnClickListener {
            dialog_otp.dismiss()
        }

    }

    private fun setProgress(
        cpv: CircleProgressBar,
        tvDidntGotCode: TextView
    ) {
        val animator = ValueAnimator.ofInt(100, 0)
        animator.addUpdateListener { animation ->
            val progress = animation.animatedValue as Int
            cpv.setProgress(progress)

            if (cpv.progress == 0) {
                tvDidntGotCode.visibility = View.VISIBLE
               cpv.visibility = View.GONE
            }

        }
        //        animator.setRepeatCount(ValueAnimator.INFINITE);
        animator.duration = 30000
        animator.start()
    }

    fun getUserData( ) {

        progressDialog = ProgressDialog(activity)
        progressDialog.setMessage("Wait a Sec....Loading Details..")
        progressDialog.setCancelable(false)
        progressDialog.show()

        val url = BASE_URL+ GET_USER_DETAILS+USER_ID_KEY+"/"

        val jsonObjRequest: StringRequest = object : StringRequest(
            Method.GET,
            url,
            object : Response.Listener<String?> {
                override fun onResponse(response: String?) {
                    Log.d("jukjbkj", response.toString())

                    val gson = Gson()

                    if (response != null ) {

                    val userArray: FetchProfileData =
                        gson.fromJson(response, FetchProfileData ::class.java)

                        fetchProfileData = userArray

                        vari = userArray.verification.toString()
                    if (userArray != null) {
                        val image = userArray.image

                        lottieSelectImage.visibility= View.GONE
                        rlParent.visibility= View.VISIBLE

                        if (userArray.verification == "False") {
                            tvAlert.setImageDrawable(resources.getDrawable(R.drawable.alert))
                            ivVerified.setImageDrawable(resources.getDrawable(R.drawable.alert))
                        } else {
                            tvAlert.setImageDrawable(resources.getDrawable(R.drawable.tick))
                            ivVerified.setImageDrawable(resources.getDrawable(R.drawable.tick))
                            btnVerify.visibility = View.GONE
                        }

                        pets = userArray.pets.toString()
                        smoking = userArray.smoking.toString()


//                        if (userArray.pets == "True" ||userArray.pets == "true" ) {
//                            cbPetsP.isChecked = true
//                        } else {
//                            cbPetsP.isChecked = false
//                        }
//
//                        if (userArray.smoking == "True" || userArray.smoking == "true") {
//                            cbSmokingP.isChecked = true
//                        } else {
//                            cbSmokingP.isChecked = false
//                        }


                        if ( image != null ) {
                            Glide.with(activity!!).load(image).into(ivProf)
                        }

                        tvBio.visibility = View.VISIBLE
                        tvFN.text = userArray.first_name
                        tvLN.text = userArray.last_name
                        tvBio.setText(userArray.bio)
                        etBio.setText(userArray.bio)
                        tvDate.text = userArray.birthdate
                        tvEMail.text = userArray.email
                        tvMN.text = userArray.mobile
                        tvVerified.text = userArray.verification
                        tvName.text = userArray.first_name+" "+userArray.last_name
                        if (userArray.gender == "1" ) {
                            tvGender.text = "Male"
                        } else if (userArray.gender == "2" ) {
                            tvGender.text = "Female"
                        } else {
                            tvGender.text = "Other"
                        }
                    }
                    }

                    progressDialog.dismiss()
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



    fun uploadImage(path: File ) {


//        if (cbPetsP.isChecked) {
//            pets = "true"
//        } else {
//            pets = "false"
//        }
//
//        if (cbSmokingP.isChecked) {
//            smoking = "true"
//        } else {
//            smoking = "false"
//        }


        val url = Configure.BASE_URL + Configure.UPDATE_USER_DETAILS+USER_UPDATE_ID+"/"
//        val url = "https://ddsio.com/sharesawaari/rest/user/details/11/"

        Log.e("proceddod", "enterUpload")

        val multipartRequest: VolleyMultipartRequest = object : VolleyMultipartRequest(
            Method.PUT,
            url,
            Response.Listener<NetworkResponse> { response ->

                val resultResponse = String(response.data)
                Log.i( "Responceis", resultResponse.toString())
                try {
                    val result = JSONObject(resultResponse)
                    val ID = result.getString("id")
                    Log.i( "Responceis", ID.toString())
                    Toast.makeText(activity,"Successfully Updated",Toast.LENGTH_LONG).show()
                    Utils.writeStringToPreferences(Configure.USER_UPDATE_ID,ID.toString(),activity)
                    progressDialog.dismiss()
                    getUserData()
                    etBio.visibility = View.GONE
                    cvSave.visibility = View.GONE

                } catch (e: JSONException) {
                    e.printStackTrace()
                }
            },
            Response.ErrorListener { error ->

                VolleyLog.d("volley", "Error: " + error.message)
                error.printStackTrace()
                Log.e("jukjbkj",  "Error: " + error.message)
                Toast.makeText(activity,"Something Went Wrong",Toast.LENGTH_LONG).show()
                progressDialog.dismiss()
            },

            VolleyProgressListener { }) {
            override fun getHeaders(): Map<String, String>? {
                var params = java.util.HashMap<String, String>()
//                params.put("Content-Type", "application/json; charset=UTF-8");
                params.put("Authorization", "Token " + LOGIN_TOKEN!!);
                return params;
            }

            override fun getParams(): Map<String, String> {
                val params= HashMap<String, String>()

                params.put("bio",etBio.text.toString())
                params.put("user",USER_ID_KEY)
//                params.put("pets",pets.toString())
                params["mobile_status"] = fetchProfileData.verification.toString()
//                params.put("smoking",smoking.toString())

                return params
            }

            override fun getByteData(): Map<String, DataPart>? {
                val params: MutableMap<String, DataPart> =
                    java.util.HashMap()
                val mimeType =
                    URLConnection.guessContentTypeFromName(path.name)
                params["profile_image"] = DataPart(path.name, Utils.fileToBytes(path), mimeType)
                return params
            }

            override fun parseNetworkError(volleyError: VolleyError): VolleyError? {
                try {
                    Log.e("VOL ERR", volleyError.toString())
                } catch (ex: java.lang.Exception) {
                }
                return super.parseNetworkError(volleyError)
            }
        }
        Utils.setVolleyRetryPolicy(multipartRequest)
        VolleySingleton.getInstance(activity).addToRequestQueue(multipartRequest, "POST_COMMENTS")
    }


    fun updateData( ) {

        if (cbPetsP.isChecked) {
            pets = "true"
        } else {
            pets = "false"
        }

        if (cbSmokingP.isChecked) {
            smoking = "true"
        } else {
            smoking = "false"
        }


        val url = Configure.BASE_URL + Configure.UPDATE_USER_DETAILS+USER_UPDATE_ID+"/"

        val jsonObjRequest: StringRequest = object : StringRequest(
            Method.PUT,
            url,
            object : Response.Listener<String?> {
                override fun onResponse(response: String?) {
                    Log.d("jukjbkj", response.toString())
                    Toast.makeText(activity,"Successfully Updated",Toast.LENGTH_LONG).show()
                    progressDialog.dismiss()
                    etBio.visibility = View.GONE
                    cvSave.visibility = View.GONE
                    getUserData()


                }
            }, object : Response.ErrorListener {
                override fun onErrorResponse(error: VolleyError) {
                    VolleyLog.d("volley", "Error: " + error.message)
                    error.printStackTrace()
                    Log.e("jukjbkj",  "Error: " + error.message)

                    Toast.makeText(activity,"Something Went Wrong ! Please try after some time",
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
                    HashMap()

                params.put("bio",etBio.text.toString())
                params.put("user",USER_ID_KEY)
//                params.put("pets",pets.toString())
//                params.put("smoking",smoking.toString())

                return params
            }
        }
        request!!.add(jsonObjRequest)

    }



    private fun askGalleryPermissionCamera() {
        askPermission(
            Manifest.permission.CAMERA ,
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
        ) {

//            val Choice =
//                arrayOf<CharSequence>("From Camera", "From Gallery")
//
//            val builder =
//                android.app.AlertDialog.Builder(activity)
//            builder.setTitle("Select")
//            builder.setItems(Choice) { dialog, which -> // the user clicked on colors[which]
//                if (which == 0) {
//
//                    var cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE);
//                    startActivityForResult(cameraIntent, CAMERA_REQUEST)
//
//                } else if (which == 1) {
//
//                    val intent = Intent(Intent.ACTION_GET_CONTENT)
//                    intent.type = "image/*"
//                    startActivityForResult(intent, GALLERY_REQUEST)
//                }
//            }
//            builder.show()

            val intent = Intent(Intent.ACTION_GET_CONTENT)
            intent.type = "image/*"
            startActivityForResult(intent, GALLERY_REQUEST)

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

    private fun compressImage() {
        actualProfImage?.let { imageFile ->
            lifecycleScope.launch {
                // Default compression


                progressDialog = ProgressDialog(activity)
                progressDialog.setMessage("Wait a Sec....Uploading Files")
                progressDialog.setCancelable(false)
                progressDialog.show()

                compressedImage = Compressor.compress( activity!!, imageFile){
                    size(100_000)
                    resolution(600, 600)
                    quality(60)
                    format(Bitmap.CompressFormat.JPEG)
                }
                setCompressedImage()
            }
        } ?:
        Log.d("receiveddata","Please Choose an Image")
    }




    private fun setCompressedImage() {
        compressedImage?.let {
            lottieSelectImage.visibility= View.GONE
            rlParent.visibility= View.VISIBLE
            ivProf.setImageBitmap(BitmapFactory.decodeFile(it.absolutePath))
            val uri = Uri.fromFile(it)
            destinationURL = uri.toString()


                uploadImage(compressedImage!! )
//                val uri = Uri.fromFile(compressedImage)
//                convertUriToFile(uri)


//            Toast.makeText(activity, "Compressed image save in " + it.path, Toast.LENGTH_LONG).show()
            Log.d("Compressor", "Compressed image save in " + it.path)
        } ?:
        Toast.makeText(activity, "File not Found " , Toast.LENGTH_LONG).show()

    }


}
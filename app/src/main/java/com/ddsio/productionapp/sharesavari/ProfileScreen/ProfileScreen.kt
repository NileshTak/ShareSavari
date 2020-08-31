package com.ddsio.productionapp.sharesavari.ProfileScreen

import android.Manifest
import android.app.ProgressDialog
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.preference.PreferenceManager
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.airbnb.lottie.LottieAnimationView
import com.android.volley.*
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.bumptech.glide.Glide
import com.ddsio.productionapp.sharesavari.CommonUtils.*
import com.ddsio.productionapp.sharesavari.CommonUtils.VolleyMultipartRequest.VolleyProgressListener
import com.ddsio.productionapp.sharesavari.LogInSignUpQues.LogInSignUpQues
import com.ddsio.productionapp.sharesavari.R
import com.github.florent37.runtimepermission.kotlin.askPermission
import com.google.gson.Gson
import com.productionapp.amhimemekar.CommonUtils.Configure
import com.productionapp.amhimemekar.CommonUtils.Configure.BASE_URL
import com.productionapp.amhimemekar.CommonUtils.Configure.GET_USER_DETAILS
import com.productionapp.amhimemekar.CommonUtils.FetchProfileData
import com.productionapp.amhimemekar.CommonUtils.UserDetailsModel
import de.hdodenhof.circleimageview.CircleImageView
import id.zelory.compressor.Compressor
import id.zelory.compressor.loadBitmap
import kotlinx.android.synthetic.main.fragment_profile_screen.*
import kotlinx.coroutines.launch
import org.json.JSONException
import org.json.JSONObject
import java.io.*
import java.net.URLConnection

class ProfileScreen : Fragment() {

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

    private var actualImage: File? = null

      var resultUri : Uri? = null

    lateinit var USER_ID_KEY : String

    var destinationURL: String? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_profile_screen, container, false)

        LOGIN_TOKEN = Utils.getStringFromPreferences(Configure.LOGIN_KEY,"",activity)!!
        USER_UPDATE_ID = Utils.getStringFromPreferences(Configure.USER_UPDATE_ID,"",activity)!!
        USER_ID_KEY = Utils.getStringFromPreferences(Configure.USER_ID_KEY,"",activity)!!

        request= Volley.newRequestQueue(activity);

        lottieSelectImage = view.findViewById<LottieAnimationView>(R.id.lottieSelectImage)
        rlParent = view.findViewById<RelativeLayout>(R.id.rlParent)
        ivProf = view.findViewById<CircleImageView>(R.id.ivProf)
        ivLogout = view.findViewById<ImageView>(R.id.ivLogout)
        cvSave = view.findViewById<CardView>(R.id.cvSave)




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
            compressImage()
        }

        lottieSelectImage.setOnClickListener {
            askGalleryPermissionCamera()
        }

        getUserData()

        return view
    }


    fun getUserData( ) {

        progressDialog = ProgressDialog(activity)
        progressDialog.setMessage("Wait a Sec....Loading Details..")
        progressDialog.setCancelable(false)
        progressDialog.show()


        val url = BASE_URL+ GET_USER_DETAILS+USER_ID_KEY+"/"
//        val url = "https://ddsio.com/sharesawaari/rest/users/22/"


        val jsonObjRequest: StringRequest = object : StringRequest(
            Method.GET,
            url,
            object : Response.Listener<String?> {
                override fun onResponse(response: String?) {
                    Log.d("jukjbkj", response.toString())

                    val gson = Gson()

                    val userArray: FetchProfileData =
                        gson.fromJson(response, FetchProfileData ::class.java)

                    val image = userArray.image

                    if (image.isNotEmpty() || image != null || image != "") {
                        lottieSelectImage.visibility= View.GONE
                        rlParent.visibility= View.VISIBLE
                        Glide.with(activity!!).load(image).into(ivProf)

                        tvFN.text = userArray.first_name
                        tvLN.text = userArray.last_name
                        tvBio.text = userArray.bio
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


    private fun convertUriToFile(data: Uri? ) {
        var bm: Bitmap? = null

        if (data != null) {
            try {

                var dataF = getImageUri(activity!!.applicationContext,bitmap)

                bm = MediaStore.Images.Media.getBitmap(activity!!.getContentResolver(), dataF)

                val filesDir = Environment.getExternalStorageDirectory()

                val imageFile = File(filesDir, "img" + ".jpg")

                val os: OutputStream
                try {
                    os = FileOutputStream(imageFile)
                    bm.compress(Bitmap.CompressFormat.JPEG, 100, os)
                    os.flush()
                    os.close()
                } catch (e: java.lang.Exception) {
                    Log.e(javaClass.simpleName, "Error writing bitmap", e)
                }

//                if (temp == null) {
                uploadImage(imageFile ,USER_ID_KEY)
//                } else {
//                    convertTempURI(temp, imageFile, data)
//                }

            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }


    fun getImageUri( inContext : Context, inImage : Bitmap) : Uri {
        var bytes = ByteArrayOutputStream();
        inImage.compress(Bitmap.CompressFormat.JPEG, 100, bytes)
        var tsLong = System.currentTimeMillis()/1000;
        var path = MediaStore.Images.Media.insertImage(inContext.getContentResolver(), inImage, tsLong.toString(), null);
        return Uri.parse(path);
    }

//    @Subscribe
//    fun callAPI(resCode: String) {
//        Log.d("proceddod","done")
//        if (resCode == "200"){
//            refreshScene()
//        }
//    }


    fun uploadImage(path: File , pk: String) {

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
                params.put("Content-Type", "application/json; charset=UTF-8");
                params.put("Authorization", "Token " + LOGIN_TOKEN!!);
                return params;
            }

            override fun getParams(): Map<String, String> {
                val params= HashMap<String, String>()
                params.put("user",pk.toString())
                params.put("mobile","8446613467")
                params.put("mobile_status","false")
                params.put("bio","j")
                params.put("birthdate","2020-8-10")
                params.put("gender","1")
                return params
            }

            override fun getByteData(): Map<String, DataPart>? {
                val params: MutableMap<String, DataPart> =
                    java.util.HashMap()
                val mimeType =
                    URLConnection.guessContentTypeFromName(path.name)
                params["profile_image"] = DataPart(path.name, Utils.fileToBytes(path), mimeType)
                params["adhar_image"] = DataPart(path.name, Utils.fileToBytes(path), mimeType)


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
        actualImage?.let { imageFile ->
            lifecycleScope.launch {
                // Default compression


                progressDialog = ProgressDialog(activity)
                progressDialog.setMessage("Wait a Sec....Uploading Files")
                progressDialog.setCancelable(false)
                progressDialog.show()

                compressedImage = Compressor.compress( activity!!, imageFile)
                setCompressedImage()
            }
        } ?:
        Log.d("receiveddata","Please Choose an Image")
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == GALLERY_REQUEST && resultCode == AppCompatActivity.RESULT_OK) {
            if (data == null) {
                Toast.makeText(activity, "null", Toast.LENGTH_LONG).show()
                return
            }
            try {
                actualImage = FileUtil.from(activity!!, data.data!!)?.also {
                    lottieSelectImage.visibility= View.GONE
                    rlParent.visibility= View.VISIBLE
                    ivProf.setImageBitmap(loadBitmap(it))
                }
            } catch (e: IOException) {
                Toast.makeText(activity, "failed to read", Toast.LENGTH_LONG).show()
                e.printStackTrace()
            }
        }
    }

    private fun setCompressedImage() {
        compressedImage?.let {
            lottieSelectImage.visibility= View.GONE
            rlParent.visibility= View.VISIBLE
            ivProf.setImageBitmap(BitmapFactory.decodeFile(it.absolutePath))
            val uri = Uri.fromFile(it)
            destinationURL = uri.toString()


                uploadImage(compressedImage!!,USER_ID_KEY)
//                val uri = Uri.fromFile(compressedImage)
//                convertUriToFile(uri)


            Toast.makeText(activity, "Compressed image save in " + it.path, Toast.LENGTH_LONG).show()
            Log.d("Compressor", "Compressed image save in " + it.path)
        } ?:
        Toast.makeText(activity, "File not Found " , Toast.LENGTH_LONG).show()

    }


}
package com.ddsio.productionapp.sharesavari.ProfileScreen

import android.Manifest
import android.app.Activity
import android.app.ProgressDialog
import android.content.ContentResolver
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.preference.PreferenceManager
import android.provider.MediaStore
import android.provider.OpenableColumns
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.cardview.widget.CardView
import androidx.fragment.app.Fragment
import com.airbnb.lottie.LottieAnimationView
import com.android.volley.*
import com.android.volley.toolbox.Volley
import com.ddsio.productionapp.sharesavari.CommonUtils.Utils
import com.ddsio.productionapp.sharesavari.CommonUtils.VolleyMultipartRequest
import com.ddsio.productionapp.sharesavari.CommonUtils.VolleyMultipartRequest.VolleyProgressListener
import com.ddsio.productionapp.sharesavari.CommonUtils.VolleySingleton
import com.ddsio.productionapp.sharesavari.LogInSignUpQues.LogInSignUpQues
import com.ddsio.productionapp.sharesavari.LogInSignUpQues.QuesBottomSheet
import com.ddsio.productionapp.sharesavari.MainActivity
import com.ddsio.productionapp.sharesavari.R
import com.github.florent37.runtimepermission.kotlin.askPermission
import com.productionapp.amhimemekar.CommonUtils.Configure
import com.yalantis.ucrop.UCrop
import com.yalantis.ucrop.model.AspectRatio
import de.hdodenhof.circleimageview.CircleImageView
import java.io.*
import java.net.URLConnection

class ProfileScreen : Fragment() {

    var CAMERA_REQUEST = 1888
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

    lateinit var bitmap: Bitmap

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
            progressDialog = ProgressDialog(activity)
            progressDialog.setMessage("Wait a Sec....Loging In")
            progressDialog.setCancelable(false)
            progressDialog.show()

            if (destinationURL != null) {
                convertUriToFile(Uri.parse(destinationURL)!!)
            }

        }

        lottieSelectImage.setOnClickListener {
            askGalleryPermissionCamera()
        }

        return view
    }


    private fun convertUriToFile(data: Uri? ) {

        Log.d("proceddod","conert")

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

                uploadImage(imageFile, data,USER_ID_KEY)


            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }


    private fun startCrop(uri: Uri) {
//        val destinationFileName = "ACb"
//        var uCrop = UCrop.of(
//            uri,
//            Uri.fromFile(
//                File(
//                    Environment.getExternalStorageDirectory(),
//                    destinationFileName
//                )
//            )
//        )
//        uCrop.withAspectRatio(1f, 1f)
//        uCrop.useSourceImageAspectRatio()
//        uCrop = advancedConfig(uCrop)
//        Log.e("proceddod", "200f")
//        uCrop.start(activity!!, 200)


        val destinationUri = Uri.fromFile(
            File(
                activity!!.cacheDir, queryNameTemp(activity!!.contentResolver, uri)
            )
        )

        destinationURL = destinationUri.toString()

        bitmap =
            MediaStore.Images.Media.getBitmap(activity!!.getContentResolver(), selectedImageUri)

        ivProf.setImageBitmap(bitmap)
    }


    fun queryNameTemp(resolver: ContentResolver, uri: Uri?): String {
        val returnCursor = resolver.query(uri!!, null, null, null, null)!!
        val nameIndex = returnCursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
        returnCursor.moveToFirst()
        val name = returnCursor.getString(nameIndex)
        returnCursor.close()
        return name
    }


//    @Subscribe
//    fun callAPI(resCode: String) {
//        Log.d("proceddod","done")
//        if (resCode == "200"){
//            refreshScene()
//        }
//    }

    private fun advancedConfig(uCrop: UCrop): UCrop? {
        val options = UCrop.Options()
        options.setToolbarTitle("")
        //        options.setMaxScaleMultiplier(5);
//        options.setImageToCropBoundsAnimDuration(666);
//        options.setDimmedLayerColor(Color.CYAN);
        options.setCircleDimmedLayer(true)
        options.setShowCropFrame(false)
        //        options.setCropGridStrokeWidth(20);
//        options.setCropGridColor(Color.GREEN);
        options.setCropGridColumnCount(0)
        options.setCropGridRowCount(0)
        //        options.setToolbarCropDrawable(R.drawable.your_crop_icon);
//        options.setToolbarCancelDrawable(R.drawable.your_cancel_icon);
//        // Color palette
        options.setToolbarColor(Color.BLACK)
        options.setStatusBarColor(Color.BLACK)
        //        options.setActiveWidgetColor(ContextCompat.getColor(this, R.color.your_color_res));
//        options.setToolbarWidgetColor(ContextCompat.getColor(this, R.color.your_color_res));
//        options.setRootViewBackgroundColor(ContextCompat.getColor(this, R.color.your_color_res));
        // Aspect ratio options
        options.setAspectRatioOptions(
            0,
            AspectRatio("1 : 1", 1F, 1F)
        )
        return uCrop.withOptions(options)
    }

    fun getImageUri(inContext: Context, inImage: Bitmap): Uri {
        val bytes = ByteArrayOutputStream()
        inImage.compress(Bitmap.CompressFormat.JPEG, 100, bytes)
        val path = MediaStore.Images.Media.insertImage(
            inContext.contentResolver,
            inImage,
            "Title",
            null
        )
        return Uri.parse(path)
    }


    fun uploadImage(path: File, data: Uri?, pk: String) {

        val url = "https://ddsio.com/sharesawaari/rest/user/details/"

        Log.e("proceddod", "enterUpload")

        val multipartRequest: VolleyMultipartRequest = object : VolleyMultipartRequest(
            Method.POST,
            url,
            Response.Listener { response ->
                try {

                    Toast.makeText(activity,"Success",Toast.LENGTH_LONG).show()
                    progressDialog.dismiss()

                } catch (e: java.lang.Exception) {
                    e.printStackTrace()
                    progressDialog.dismiss()
                }
            },
            Response.ErrorListener { error ->
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


    private fun onSelectFromGalleryResult(data: Uri?) {

        Log.e("proceddod", "bitmap")
        var bm: Bitmap? = null
        if (data != null) {
            try {
                Log.e("proceddod", "try")
                bm = MediaStore.Images.Media.getBitmap(activity!!.getContentResolver(), data)
                val filesDir = Environment.getExternalStorageDirectory()
                val imageFile = File(filesDir, "img" + ".jpg")
                val os: OutputStream
                try {
                    Log.e("proceddod", "try2")
                    os = FileOutputStream(imageFile)
                    bm.compress(Bitmap.CompressFormat.JPEG, 100, os)
                    os.flush()
                    os.close()
                } catch (e: java.lang.Exception) {
                    Log.e("proceddod", "catch2")
                    Log.e(javaClass.simpleName, "Error writing bitmap", e)
                }
                Log.e("proceddod", imageFile.toString())
                uploadImage(imageFile, data,USER_ID_KEY)
            } catch (e: IOException) {
                Log.e("proceddod", "catch")
                e.printStackTrace()
            }
        }
    }

    fun putUserExtraDetails( pk: String) {
//        val file: File = imageFile

        val url = "https://ddsio.com/sharesawaari/rest/user/details/"

        val multipartRequest: VolleyMultipartRequest = object : VolleyMultipartRequest(
            Request.Method.POST,
            url,
            Response.Listener<NetworkResponse> { response ->
                Log.i("dataIsForUplod", response.toString())
                progressDialog.dismiss()

            },
            Response.ErrorListener { error ->
                try {
                    Log.i("dataIsForUplod", error.message!!)
                    Toast.makeText(
                        activity, "Failed to Upload ! Please try after some time",
                        Toast.LENGTH_LONG
                    ).show()
                    progressDialog.dismiss()
                } catch (ex: Exception) {
                    Toast.makeText(
                        activity, "Failed to Upload ! Please try after some time",
                        Toast.LENGTH_LONG
                    ).show()
                    progressDialog.dismiss()
                }
                progressDialog.dismiss()
            }) {

            override fun getHeaders(): MutableMap<String, String> {

                var params = java.util.HashMap<String, String>()
                params.put("Content-Type", "application/json; charset=UTF-8");
                params.put("Authorization", "Token "+LOGIN_TOKEN);

                Log.d("Dataos",LOGIN_TOKEN)

                return params;
            }

//            override fun getByteData(): MutableMap<String, DataPart> {
//                val params: MutableMap<String, DataPart> = java.util.HashMap()
//
//                val mimeType =
//                    URLConnection.guessContentTypeFromName(file.name)
//                params["profile_image"] = DataPart(file.name, Utils.fileToBytes(file), mimeType)
//                Log.d("Dataos",params.get("profile_image").toString() )
//                return params
//            }

            @Throws(AuthFailureError::class)
            override fun getParams(): Map<String, String> {

                var gender = 0

                val params= HashMap<String, String>()
                params.put("user",pk.toString())
                params.put("mobile","8446613467")
                params.put("mobile_status","false")
                params.put("bio","j")

                params.put("birthdate","2020-8-10")
//                if (etGender.text.toString() == "Male") {
//                    gender = 1
//                } else if (etGender.text.toString() == "Female") {
//                    gender = 2
//                } else {
//                    gender = 3
//                }
                params.put("gender","1")




                Log.d("Dataos",pk.toString())

                return params
            }
        }
        request!!.add(multipartRequest)
    }

    private fun askGalleryPermissionCamera() {
        askPermission(
            Manifest.permission.CAMERA ,
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
        ) {

            val Choice =
                arrayOf<CharSequence>("From Camera", "From Gallery")

            val builder =
                android.app.AlertDialog.Builder(activity)
            builder.setTitle("Select")
            builder.setItems(Choice) { dialog, which -> // the user clicked on colors[which]
                if (which == 0) {

                    var cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                    startActivityForResult(cameraIntent, CAMERA_REQUEST)

                } else if (which == 1) {

                    val galleryIntent = Intent(
                        Intent.ACTION_PICK,
                        MediaStore.Images.Media.EXTERNAL_CONTENT_URI
                    )
                    startActivityForResult(galleryIntent, GALLERY_REQUEST)
                }
            }
            builder.show()


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


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        val imageUri: Uri? = null
        Log.e("proceddod", data.toString())

        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == GALLERY_REQUEST) {
                  selectedImageUri = data!!.data!!
                val bitmap =
                    MediaStore.Images.Media.getBitmap(activity!!.getContentResolver(), selectedImageUri)
                lottieSelectImage.visibility= View.GONE
                rlParent.visibility= View.VISIBLE
                ivProf.setImageBitmap(bitmap)

                if (selectedImageUri != null) {
                    startCrop(selectedImageUri)
                } else {
                    Toast.makeText(activity, "Done", Toast.LENGTH_SHORT).show()
                }

            }  else if (requestCode == CAMERA_REQUEST) {
                try {
                    val thumbnail = MediaStore.Images.Media.getBitmap(
                        activity!!.getContentResolver(), imageUri
                    )
                    val imageurl = getImageUri(activity!!, thumbnail)
                    startCrop(imageurl)
                } catch (e: java.lang.Exception) {
                    e.printStackTrace()
                }
            }
        }
        super.onActivityResult(requestCode, resultCode, data)
    }




}
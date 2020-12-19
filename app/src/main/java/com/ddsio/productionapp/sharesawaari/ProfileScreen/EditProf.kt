package com.ddsio.productionapp.sharesawaari.ProfileScreen

import android.app.ProgressDialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.android.volley.*
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.ddsio.productionapp.sharesawaari.CommonUtils.Utils
import com.ddsio.productionapp.sharesawaari.MainActivity
import com.ddsio.productionapp.sharesawaari.R
import com.productionapp.amhimemekar.CommonUtils.Configure
import com.productionapp.amhimemekar.CommonUtils.FetchProfileData
import io.github.inflationx.viewpump.ViewPumpContextWrapper
import kotlinx.android.synthetic.main.activity_edit_prof.*

class EditProf : AppCompatActivity() {

    lateinit var pojoWithData : FetchProfileData
    var LOGIN_TOKEN = ""
    var USER_UPDATE_ID = ""
    lateinit var USER_ID_KEY : String
    var request: RequestQueue? = null

    lateinit var progressDialog: ProgressDialog


    override fun attachBaseContext(newBase: Context) {
        super.attachBaseContext(ViewPumpContextWrapper.wrap(newBase))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_prof)

        LOGIN_TOKEN = Utils.getStringFromPreferences(Configure.LOGIN_KEY,"",this)!!
        USER_UPDATE_ID = Utils.getStringFromPreferences(Configure.USER_UPDATE_ID,"",this)!!
        USER_ID_KEY = Utils.getStringFromPreferences(Configure.USER_ID_KEY,"",this)!!
        request= Volley.newRequestQueue(this);

        val bundle: Bundle? = intent.extras

        pojoWithData = bundle!!.get("userPojo") as FetchProfileData


        ivBa.setOnClickListener {
            onBackPressed()
        }

        tvEMail.setText(pojoWithData.email)

        etBioE.setText(pojoWithData.bio)


        cvSaveE.setOnClickListener {

                progressDialog = ProgressDialog(this)
                progressDialog.setMessage("Wait a Sec....Uploading Files")
                progressDialog.setCancelable(false)
                progressDialog.show()
                updateData( )


        }
    }


    fun updateData( ) {



        val url = Configure.BASE_URL + Configure.UPDATE_USER_DETAILS+USER_UPDATE_ID+"/"

        val jsonObjRequest: StringRequest = object : StringRequest(
            Method.PUT,
            url,
            object : Response.Listener<String?> {
                override fun onResponse(response: String?) {
                    Log.d("jukjbkj", response.toString())
                    Toast.makeText(this@EditProf,"Successfully Updated", Toast.LENGTH_LONG).show()
                    progressDialog.dismiss()

                    var int = Intent(this@EditProf,
                        MainActivity::class.java)
                    int.putExtra("type", "")
                    startActivity(int)



                }
            }, object : Response.ErrorListener {
                override fun onErrorResponse(error: VolleyError) {
                    VolleyLog.d("volley", "Error: " + error.message)
                    error.printStackTrace()
                    Log.e("jukjbkj",  "Error: " + error.message)

                    Toast.makeText(this@EditProf,"Something Went Wrong ! Please try after some time",
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

                params.put("bio",etBioE.text.toString())
                params.put("email",tvEMail.text.toString())
                params.put("user",USER_ID_KEY)

                return params
            }
        }
        request!!.add(jsonObjRequest)

    }

}
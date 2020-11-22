package com.ddsio.productionapp.sharesavari.InboxScreen.Child

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.android.volley.*
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.ddsio.productionapp.sharesavari.CommonUtils.Utils
import com.ddsio.productionapp.sharesavari.InboxScreen.Fragments.MessagesFrag
import com.ddsio.productionapp.sharesavari.R
import com.ddsio.productionapp.sharesavari.User
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.gson.Gson
import com.letsbuildthatapp.kotlinmessenger.models.ChatMessage
import com.productionapp.amhimemekar.CommonUtils.Configure
import com.productionapp.amhimemekar.CommonUtils.FetchProfileData
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.ViewHolder
import io.github.inflationx.viewpump.ViewPumpContextWrapper
import kotlinx.android.synthetic.main.activity_chat_log.*
import java.util.regex.Matcher
import java.util.regex.Pattern

class ChatLogActivity : AppCompatActivity() {

    companion object {
        val TAG = "ChatLog"
    }

  var toPlayerId = ""

    var LOGIN_TOKEN = ""
    var USER_UPDATE_ID = ""
    lateinit var USER_ID_KEY: String
    val adapter = GroupAdapter<ViewHolder>()

  var request: RequestQueue? = null

    var toUser: User? = null

    override fun attachBaseContext(newBase: Context) {
        super.attachBaseContext(ViewPumpContextWrapper.wrap(newBase))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat_log)


        LOGIN_TOKEN = Utils.getStringFromPreferences(Configure.LOGIN_KEY, "", this)!!
        USER_UPDATE_ID = Utils.getStringFromPreferences(Configure.USER_UPDATE_ID, "", this)!!
        USER_ID_KEY = Utils.getStringFromPreferences(Configure.USER_ID_KEY, "", this)!!

      request= Volley.newRequestQueue(this)

        recyclerview_chat_log.adapter = adapter

        toUser = intent.getParcelableExtra<User>(NewMessageActivity.USER_KEY)

        supportActionBar?.title = toUser?.username

//    setupDummyData()
        listenForMessages()
        ivBacknumberSave.setOnClickListener {
            onBackPressed()
        }


        send_button_chat_log.setOnClickListener {
            Log.d(TAG, "Attempt to send message....")
            performSendMessage()
        }
    }

    private fun listenForMessages() {
//    val fromId = FirebaseAuth.getInstance().uid
        val toId = toUser?.uid
        val ref = FirebaseDatabase.getInstance().getReference("/user-messages/$USER_ID_KEY/$toId")

        ref.addChildEventListener(object : ChildEventListener {

            override fun onChildAdded(p0: DataSnapshot, p1: String?) {
                val chatMessage = p0.getValue(ChatMessage::class.java)

                if (chatMessage != null) {
                    Log.d(TAG, chatMessage.text)

                    if (chatMessage.fromId == USER_ID_KEY) {
                        val currentUser = MessagesFrag.currentUser ?: return
                        adapter.add(ChatFromItem(chatMessage.text, currentUser))
                    } else {
                        adapter.add(ChatToItem(chatMessage.text, toUser!!))
                    }
                }

                recyclerview_chat_log.scrollToPosition(adapter.itemCount - 1)

            }

            override fun onCancelled(p0: DatabaseError) {

            }

            override fun onChildChanged(p0: DataSnapshot, p1: String?) {

            }

            override fun onChildMoved(p0: DataSnapshot, p1: String?) {

            }

            override fun onChildRemoved(p0: DataSnapshot) {

            }

        })

    }

    private fun performSendMessage() {

        if (edittext_chat_log.text.toString().isEmpty()) {
            edittext_chat_log.error = "Enter Valid Message"
        } else if (getNumbers(edittext_chat_log.text.toString())) {
                edittext_chat_log.error = "Mobile Number not allowed"
        } else if (getTweleveNumbers(edittext_chat_log.text.toString())) {
            edittext_chat_log.error = "Mobile Number not allowed"
        } else {
                val text = edittext_chat_log.text.toString()

                val fromId = USER_ID_KEY
                val user = intent.getParcelableExtra<User>(NewMessageActivity.USER_KEY)
                val toId = user!!.uid

                getUserData(user!!.uid.toString())

                if (fromId == null) return

//    val reference = FirebaseDatabase.getInstance().getReference("/messages").push()
                val reference =
                    FirebaseDatabase.getInstance().getReference("/user-messages/$fromId/$toId").push()

                val toReference =
                    FirebaseDatabase.getInstance().getReference("/user-messages/$toId/$fromId").push()

                val chatMessage =
                    ChatMessage(reference.key!!, text, fromId, toId, System.currentTimeMillis() / 1000)

                reference.setValue(chatMessage)
                    .addOnSuccessListener {
                        Log.d(TAG, "Saved our chat message: ${reference.key}")
                        edittext_chat_log.text.clear()
                        recyclerview_chat_log.scrollToPosition(adapter.itemCount - 1)
                    }

                toReference.setValue(chatMessage)

                val latestMessageRef =
                    FirebaseDatabase.getInstance().getReference("/latest-messages/$fromId/$toId")
                latestMessageRef.setValue(chatMessage)

                val latestMessageToRef =
                    FirebaseDatabase.getInstance().getReference("/latest-messages/$toId/$fromId")
                latestMessageToRef.setValue(chatMessage)
            }
        }


    private fun getNumbers(toString: String): Boolean {
        val p = Pattern.compile("\\b-?\\d+\\b")
        val m: Matcher = p.matcher(toString)
        while (m.find()) {
            if (m.group().length == 10) {
                return checkNumber(m.group())
            }
        }
        return false
    }

    private fun getTweleveNumbers(toString: String): Boolean {
        val p = Pattern.compile("\\b-?\\d+\\b")
        val m: Matcher = p.matcher(toString)
        while (m.find()) {
            if (m.group().length == 10) {
                return checkTweleveNumber(m.group())
            }
        }
        return false
    }

    private fun checkNumber(toString: String ): Boolean {

            if(Pattern.matches("[0-9]{10}", toString)) {
                return true
            }
            return false
        }


    private fun checkTweleveNumber(toString: String ): Boolean {

        if(Pattern.matches("[0-9]{12}", toString)) {
            return true
        }
        return false
    }



    fun getUserData(userId: String) {
        val url = Configure.BASE_URL + Configure.GET_USER_DETAILS + userId + "/"

        val jsonObjRequest: StringRequest = object : StringRequest(
            Method.GET,
            url,
            object : Response.Listener<String?> {
                override fun onResponse(response: String?) {
                    Log.d("jukjbkjdd", response.toString())

                    val gson = Gson()

                    if (response != null) {

                        val userArray: FetchProfileData =
                            gson.fromJson(response, FetchProfileData::class.java)

                      sendNotification(userArray.oneid.toString())

                    }

                }
            }, object : Response.ErrorListener {
                override fun onErrorResponse(error: VolleyError) {
                    VolleyLog.d("volley", "Error: " + error.message)
                    error.printStackTrace()
                    Log.e("Responceis", "Error: " + error.message)

                    Toast.makeText(
                        this@ChatLogActivity, "Something Went Wrong ! Please try after some time",
                        Toast.LENGTH_LONG
                    ).show()
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


  private fun sendNotification(oneId: String) {

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

        params.put("message","You got a New Message. Please check into app for more details.")

        params.put("user",oneId)
        return params
      }
    }
    request!!.add(jsonObjRequest)
  }

}

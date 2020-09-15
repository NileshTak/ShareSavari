package com.ddsio.productionapp.sharesavari.InboxScreen.Child

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.ddsio.productionapp.sharesavari.CommonUtils.Utils
import com.ddsio.productionapp.sharesavari.InboxScreen.Fragments.MessagesFrag
import com.ddsio.productionapp.sharesavari.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.letsbuildthatapp.kotlinmessenger.models.ChatMessage
import com.letsbuildthatapp.kotlinmessenger.models.User
import com.productionapp.amhimemekar.CommonUtils.Configure
import com.squareup.picasso.Picasso
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.Item
import com.xwray.groupie.ViewHolder
import kotlinx.android.synthetic.main.activity_chat_log.*

class ChatLogActivity : AppCompatActivity() {

  companion object {
    val TAG = "ChatLog"
  }
  var LOGIN_TOKEN = ""
  var USER_UPDATE_ID = ""
  lateinit var USER_ID_KEY : String
  val adapter = GroupAdapter<ViewHolder>()

  var toUser: User? = null

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_chat_log)


    LOGIN_TOKEN = Utils.getStringFromPreferences(Configure.LOGIN_KEY,"",this)!!
    USER_UPDATE_ID = Utils.getStringFromPreferences(Configure.USER_UPDATE_ID,"",this)!!
    USER_ID_KEY = Utils.getStringFromPreferences(Configure.USER_ID_KEY,"",this)!!

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

    ref.addChildEventListener(object: ChildEventListener {

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
    // how do we actually send a message to firebase...
    val text = edittext_chat_log.text.toString()

    val fromId = USER_ID_KEY
    val user = intent.getParcelableExtra<User>(NewMessageActivity.USER_KEY)
    val toId = user!!.uid

    if (fromId == null) return

//    val reference = FirebaseDatabase.getInstance().getReference("/messages").push()
    val reference = FirebaseDatabase.getInstance().getReference("/user-messages/$fromId/$toId").push()

    val toReference = FirebaseDatabase.getInstance().getReference("/user-messages/$toId/$fromId").push()

    val chatMessage = ChatMessage(reference.key!!, text, fromId, toId, System.currentTimeMillis() / 1000)

    reference.setValue(chatMessage)
        .addOnSuccessListener {
          Log.d(TAG, "Saved our chat message: ${reference.key}")
          edittext_chat_log.text.clear()
          recyclerview_chat_log.scrollToPosition(adapter.itemCount - 1)
        }

    toReference.setValue(chatMessage)

    val latestMessageRef = FirebaseDatabase.getInstance().getReference("/latest-messages/$fromId/$toId")
    latestMessageRef.setValue(chatMessage)

    val latestMessageToRef = FirebaseDatabase.getInstance().getReference("/latest-messages/$toId/$fromId")
    latestMessageToRef.setValue(chatMessage)
  }
}

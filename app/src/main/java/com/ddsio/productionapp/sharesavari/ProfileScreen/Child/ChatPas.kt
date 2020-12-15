package com.ddsio.productionapp.sharesavari.ProfileScreen.Child

import android.app.ProgressDialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.RecyclerView
import com.ddsio.productionapp.sharesavari.CommonUtils.Utils
import com.ddsio.productionapp.sharesavari.InboxScreen.Child.ChatLogActivity
import com.ddsio.productionapp.sharesavari.InboxScreen.Child.LatestMessageRow
import com.ddsio.productionapp.sharesavari.InboxScreen.Child.NewMessageActivity
import com.ddsio.productionapp.sharesavari.InboxScreen.Fragments.MessagesFrag
import com.ddsio.productionapp.sharesavari.MainActivity
import com.ddsio.productionapp.sharesavari.R
import com.ddsio.productionapp.sharesavari.User
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.database.*
import com.letsbuildthatapp.kotlinmessenger.models.ChatMessage
import com.productionapp.amhimemekar.CommonUtils.Configure
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.ViewHolder
import io.github.inflationx.viewpump.ViewPumpContextWrapper
import kotlinx.android.synthetic.main.activity_chat_pas.*

class ChatPas : AppCompatActivity() {

    companion object {
        var currentUser: User? = null
        val TAG = "LatestMessages"
    }

    lateinit var recyclerview_latest_messages : RecyclerView
    lateinit var fabNewMsg : FloatingActionButton
    var LOGIN_TOKEN = ""
    var USER_UPDATE_ID = ""
    lateinit var USER_ID_KEY : String

    var driverid = ""

    lateinit var progressDialog: ProgressDialog

    override fun attachBaseContext(newBase: Context) {
        super.attachBaseContext(ViewPumpContextWrapper.wrap(newBase))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat_pas)

        recyclerview_latest_messages = findViewById<RecyclerView>(R.id.recyclerview_latest_messagesChat)
        fabNewMsg = findViewById<FloatingActionButton>(R.id.fabNewMsgChat)

        LOGIN_TOKEN = Utils.getStringFromPreferences(Configure.LOGIN_KEY,"",this)!!
        USER_UPDATE_ID = Utils.getStringFromPreferences(Configure.USER_UPDATE_ID,"",this)!!
        USER_ID_KEY = Utils.getStringFromPreferences(Configure.USER_ID_KEY,"",this)!!
        val bundle: Bundle? = intent.extras
        driverid = bundle!!.get("driverid") as String

        recyclerview_latest_messages.adapter = adapter
        recyclerview_latest_messages.addItemDecoration(DividerItemDecoration(this, DividerItemDecoration.VERTICAL))


        ivBacSChat.setOnClickListener {
            onBackPressed()
        }

        fabNewMsg.setOnClickListener {
            val intent = Intent(this, NewMessageActivity::class.java)
            startActivity(intent)
        }

        progressDialog = ProgressDialog(this)
        progressDialog.setMessage("Wait a Sec....Loading Chats")
        progressDialog.setCancelable(false)
        progressDialog.show()


        // set item click listener on your adapter
        adapter.setOnItemClickListener { item, view ->

            val intent = Intent(this, ChatLogActivity::class.java)

            // we are missing the chat partner user

            val row = item as LatestMessageRow
            intent.putExtra(NewMessageActivity.USER_KEY, row.chatPartnerUser)
            startActivity(intent)
        }

//    setupDummyRows()
        listenForLatestMessages()

        fetchCurrentUser()

        verifyUserIsLoggedIn()
    }

    val latestMessagesMap = HashMap<String, ChatMessage>()

    private fun refreshRecyclerViewMessages() {
        adapter.clear()
//        if (latestMessagesMap.size < 1) {
//            llNoMsg.visibility = View.VISIBLE
//        } else {
        llNoMsgChat.visibility = View.GONE
//        }
        latestMessagesMap.values.forEach {
            if (it.toId == driverid) {
                adapter.add(LatestMessageRow(it))
            }
        }
        progressDialog.dismiss()
    }

    private fun listenForLatestMessages() {
        val ref = FirebaseDatabase.getInstance().getReference("/latest-messages/$USER_ID_KEY")
        ref.addChildEventListener(object: ChildEventListener {
            override fun onChildAdded(p0: DataSnapshot, p1: String?) {
                val chatMessage = p0.getValue(ChatMessage::class.java) ?: return
                latestMessagesMap[p0.key!!] = chatMessage
                refreshRecyclerViewMessages()
                progressDialog.dismiss()
            }

            override fun onChildChanged(p0: DataSnapshot, p1: String?) {
                val chatMessage = p0.getValue(ChatMessage::class.java) ?: return
                latestMessagesMap[p0.key!!] = chatMessage
                refreshRecyclerViewMessages()
            }

            override fun onChildMoved(p0: DataSnapshot, p1: String?) {

            }
            override fun onChildRemoved(p0: DataSnapshot) {

            }
            override fun onCancelled(p0: DatabaseError) {
                progressDialog.dismiss()
            }
        })
    }

    val adapter = GroupAdapter<ViewHolder>()

//  private fun setupDummyRows() {
//
//
//    adapter.add(LatestMessageRow())
//    adapter.add(LatestMessageRow())
//    adapter.add(LatestMessageRow())
//  }

    private fun fetchCurrentUser() {
        val ref = FirebaseDatabase.getInstance().getReference("/users/$USER_ID_KEY")
        ref.addListenerForSingleValueEvent(object: ValueEventListener {

            override fun onDataChange(p0: DataSnapshot) {
                MessagesFrag.currentUser = p0.getValue(User::class.java)
                Log.d("LatestMessages", "Current user ${MessagesFrag.currentUser?.profileImageUrl}")
                progressDialog.dismiss()
            }

            override fun onCancelled(p0: DatabaseError) {
                progressDialog.dismiss()
            }
        })
    }

    private fun verifyUserIsLoggedIn() {
        if (USER_ID_KEY == null || USER_ID_KEY.isEmpty() ) {
            val intent = Intent(this, MainActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intent)
        }
    }
}

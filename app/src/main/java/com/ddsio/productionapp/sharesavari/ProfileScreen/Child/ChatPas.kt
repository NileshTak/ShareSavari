package com.ddsio.productionapp.sharesavari.ProfileScreen.Child

import android.app.ProgressDialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityOptionsCompat
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.RecyclerView
import com.ddsio.productionapp.sharesavari.CommonUtils.Utils
import com.ddsio.productionapp.sharesavari.InboxScreen.Child.ChatLogActivity
import com.ddsio.productionapp.sharesavari.InboxScreen.Child.LatestMessageRow
import com.ddsio.productionapp.sharesavari.InboxScreen.Child.NewMessageActivity
import com.ddsio.productionapp.sharesavari.InboxScreen.Child.UserItem
import com.ddsio.productionapp.sharesavari.InboxScreen.Fragments.MessagesFrag
import com.ddsio.productionapp.sharesavari.MainActivity
import com.ddsio.productionapp.sharesavari.R
import com.ddsio.productionapp.sharesavari.SearchScreen.child.BookedSuccess
import com.ddsio.productionapp.sharesavari.User
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.database.*
import com.letsbuildthatapp.kotlinmessenger.models.ChatMessage
import com.productionapp.amhimemekar.CommonUtils.BookRidesPojoItem
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

    lateinit var recyclerUser : RecyclerView
    lateinit var fabNewMsg : FloatingActionButton
    var LOGIN_TOKEN = ""
    var USER_UPDATE_ID = ""
    lateinit var USER_ID_KEY : String

    var driverid = ""
    var type = ""
    var screen = ""

    lateinit var progressDialog: ProgressDialog


    val adapterWithOUtBook = GroupAdapter<ViewHolder>()

    override fun attachBaseContext(newBase: Context) {
        super.attachBaseContext(ViewPumpContextWrapper.wrap(newBase))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat_pas)

        recyclerview_latest_messages = findViewById<RecyclerView>(R.id.recyclerview_latest_messagesChat)
        recyclerUser = findViewById<RecyclerView>(R.id.recyclerUser)

        fabNewMsg = findViewById<FloatingActionButton>(R.id.fabNewMsgChat)

        LOGIN_TOKEN = Utils.getStringFromPreferences(Configure.LOGIN_KEY,"",this)!!
        USER_UPDATE_ID = Utils.getStringFromPreferences(Configure.USER_UPDATE_ID,"",this)!!
        USER_ID_KEY = Utils.getStringFromPreferences(Configure.USER_ID_KEY,"",this)!!
        val bundle: Bundle? = intent.extras
        driverid = bundle!!.get("driverid") as String
        type = bundle!!.get("type") as String
        screen = bundle!!.get("screen") as String

        recyclerview_latest_messages.adapter = adapter
        recyclerview_latest_messages.addItemDecoration(DividerItemDecoration(this, DividerItemDecoration.VERTICAL))


        recyclerUser.adapter = adapter
        recyclerUser.addItemDecoration(DividerItemDecoration(this, DividerItemDecoration.VERTICAL))


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


        llNoMsgChat.visibility = View.GONE

        fetchCurrentUser()

        verifyUserIsLoggedIn()

        if (screen == "search") {

            recyclerUser.visibility = View.VISIBLE
            recyclerview_latest_messages.visibility = View.GONE

            fetchUsers()
            sendMessage()


        } else {

            recyclerUser.visibility = View.GONE
            recyclerview_latest_messages.visibility = View.VISIBLE
            listenForLatestMessages()
        }
    }


    private fun sendMessage(
    ) {

        var text =  " "


        val fromId = USER_ID_KEY
        val toId = driverid


        if (fromId == null) return

        val reference = FirebaseDatabase.getInstance().getReference("/user-messages/$fromId/$toId").push()

        val toReference = FirebaseDatabase.getInstance().getReference("/user-messages/$toId/$fromId").push()

        val chatMessage = ChatMessage(reference.key!!, text, fromId, toId, System.currentTimeMillis() / 1000 )

        reference.setValue(chatMessage)
            .addOnSuccessListener {
                progressDialog.dismiss()
                listenForLatestMessages()
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



    private fun fetchUsers() {
        val ref = FirebaseDatabase.getInstance().getReference("/users")
        ref.addListenerForSingleValueEvent(object: ValueEventListener {

            override fun onDataChange(p0: DataSnapshot) {


                p0.children.forEach {
                    Log.d("NewMessage", it.toString())
                    val user = it.getValue(User::class.java)
                    if (user != null && user.uid == driverid) {
                        adapter.add(UserItem(user))
                    }
                }
            }

            override fun onCancelled(p0: DatabaseError) {

            }
        })
    }



val latestMessagesMap = HashMap<String, ChatMessage>()

    private fun refreshRecyclerViewMessages() {
        adapter.clear()
//        if (latestMessagesMap.size < 1) {
//            llNoMsg.visibility = View.VISIBLE
//        } else {
//        llNoMsgChat.visibility = View.GONE
//        }

        latestMessagesMap.values.forEach {

            if (type == "from" && it.fromId == driverid) {
                adapter.add(LatestMessageRow(it))
            } else if (it.toId == driverid) {
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


    private fun fetchCurrentUser() {
        val ref = FirebaseDatabase.getInstance().getReference("/users/$USER_ID_KEY")
        ref.addListenerForSingleValueEvent(object: ValueEventListener {

            override fun onDataChange(p0: DataSnapshot) {
                MessagesFrag.currentUser = p0.getValue(User::class.java)

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

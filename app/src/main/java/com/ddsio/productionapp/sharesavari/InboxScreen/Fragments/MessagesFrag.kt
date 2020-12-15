package com.ddsio.productionapp.sharesavari.InboxScreen.Fragments

import android.app.ProgressDialog
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.ddsio.productionapp.sharesavari.CommonUtils.Utils
import com.ddsio.productionapp.sharesavari.InboxScreen.Child.ChatLogActivity
import com.ddsio.productionapp.sharesavari.InboxScreen.Child.LatestMessageRow
import com.ddsio.productionapp.sharesavari.InboxScreen.Child.NewMessageActivity
import com.ddsio.productionapp.sharesavari.MainActivity
import com.ddsio.productionapp.sharesavari.R
import com.ddsio.productionapp.sharesavari.User
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.database.*
import com.letsbuildthatapp.kotlinmessenger.models.ChatMessage
import com.productionapp.amhimemekar.CommonUtils.Configure
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.ViewHolder
import kotlinx.android.synthetic.main.fragment_messages.*


class MessagesFrag : Fragment() {

    companion object {
        var currentUser: User? = null
        val TAG = "LatestMessages"
    }

    lateinit var recyclerview_latest_messages : RecyclerView
    lateinit var fabNewMsg : FloatingActionButton
    var LOGIN_TOKEN = ""
    var USER_UPDATE_ID = ""
    lateinit var USER_ID_KEY : String

    lateinit var progressDialog: ProgressDialog

    lateinit var llNoMsgFrag : LinearLayout

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {


        val view = inflater.inflate(R.layout.fragment_messages, container, false)
        recyclerview_latest_messages = view.findViewById<RecyclerView>(R.id.recyclerview_latest_messages)
        fabNewMsg = view.findViewById<FloatingActionButton>(R.id.fabNewMsg)
        llNoMsgFrag = view.findViewById<LinearLayout>(R.id.llNoMsgFrag)

        LOGIN_TOKEN = Utils.getStringFromPreferences(Configure.LOGIN_KEY,"",activity)!!
        USER_UPDATE_ID = Utils.getStringFromPreferences(Configure.USER_UPDATE_ID,"",activity)!!
        USER_ID_KEY = Utils.getStringFromPreferences(Configure.USER_ID_KEY,"",activity)!!


        recyclerview_latest_messages.adapter = adapter
        recyclerview_latest_messages.addItemDecoration(DividerItemDecoration(activity, DividerItemDecoration.VERTICAL))


        fabNewMsg.setOnClickListener {
            val intent = Intent(activity, NewMessageActivity::class.java)
            startActivity(intent)
        }

        progressDialog = ProgressDialog(activity)
        progressDialog.setMessage("Wait a Sec....Loading Chats")
        progressDialog.setCancelable(false)
        progressDialog.show()


        // set item click listener on your adapter
        adapter.setOnItemClickListener { item, view ->

            val intent = Intent(activity, ChatLogActivity::class.java)

            // we are missing the chat partner user

            val row = item as LatestMessageRow
            intent.putExtra(NewMessageActivity.USER_KEY, row.chatPartnerUser)
            startActivity(intent)
        }

//    setupDummyRows()
        listenForLatestMessages()

        fetchCurrentUser()

        verifyUserIsLoggedIn()

        return view
    }

    val latestMessagesMap = HashMap<String, ChatMessage>()

    private fun refreshRecyclerViewMessages() {
        adapter.clear()
        if (latestMessagesMap.size < 1) {
            llNoMsgFrag.visibility = View.VISIBLE
        } else {
            llNoMsgFrag.visibility = View.GONE
        }
        latestMessagesMap.values.forEach {
            adapter.add(LatestMessageRow(it))
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
                currentUser = p0.getValue(User::class.java)
                Log.d("LatestMessages", "Current user ${currentUser?.profileImageUrl}")
                progressDialog.dismiss()
            }

            override fun onCancelled(p0: DatabaseError) {
                progressDialog.dismiss()
            }
        })
    }

    private fun verifyUserIsLoggedIn() {
        if (USER_ID_KEY == null || USER_ID_KEY.isEmpty() ) {
            val intent = Intent(activity, MainActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intent)
        }
    }


}
